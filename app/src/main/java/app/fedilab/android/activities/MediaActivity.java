/* Copyright 2017 Thomas Schneider
 *
 * This file is a part of Fedilab
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Fedilab is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Fedilab; if not,
 * see <http://www.gnu.org/licenses>. */
package app.fedilab.android.activities;


import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.github.chrisbanes.photoview.OnMatrixChangedListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.gw.swipeback.SwipeBackLayout;

import java.io.File;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import app.fedilab.android.client.Entities.Attachment;
import app.fedilab.android.client.Entities.Error;
import app.fedilab.android.client.HttpsConnection;
import app.fedilab.android.client.TLSSocketFactory;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.webview.MastalabWebChromeClient;
import app.fedilab.android.webview.MastalabWebViewClient;
import app.fedilab.android.R;
import app.fedilab.android.interfaces.OnDownloadInterface;

import static app.fedilab.android.helper.Helper.changeDrawableColor;


/**
 * Created by Thomas on 25/06/2017.
 * Media Activity
 */

public class MediaActivity extends BaseActivity implements OnDownloadInterface {


    private RelativeLayout loader;
    private ArrayList<Attachment>  attachments;
    private PhotoView imageView;
    private SimpleExoPlayerView videoView;
    private float downX;
    private float downY;
    private int mediaPosition;
    MediaActivity.actionSwipe currentAction;
    static final int MIN_DISTANCE = 100;
    private String finalUrlDownload;
    private String preview_url;
    private ImageView prev, next;
    private boolean isControlElementShown = true;
    private Bitmap downloadedImage;
    private File fileVideo;
    private TextView progress;
    private ProgressBar pbar_inf;
    private TextView message_ready;
    private boolean canSwipe;
    private TextView media_description;
    private Attachment attachment;
    SwipeBackLayout mSwipeBackLayout;
    private float imageScale = 0;
    private RelativeLayout action_bar_container;
    private enum actionSwipe{
        RIGHT_TO_LEFT,
        LEFT_TO_RIGHT,
        POP
    }
    private WebView webview_video;
    private ImageButton media_save,media_share, media_close;
    private boolean scheduleHidden, scheduleHiddenDescription;
    private SimpleExoPlayer player;
    private boolean isSHaring;
    private String instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if( theme == Helper.THEME_BLACK)
            setTheme(R.style.TransparentBlack);
        super.onCreate(savedInstanceState);
        hideSystemUI();
        setContentView(R.layout.activity_media);
        action_bar_container = (RelativeLayout) findViewById(R.id.action_bar_container);
        mSwipeBackLayout = new SwipeBackLayout(MediaActivity.this);
        mSwipeBackLayout.setDirectionMode(SwipeBackLayout.FROM_TOP);
        mSwipeBackLayout.setMaskAlpha(125);
        mSwipeBackLayout.setSwipeBackFactor(0.5f);
        mSwipeBackLayout.setSwipeBackListener(new SwipeBackLayout.OnSwipeBackListener() {
            @Override
            public void onViewPositionChanged(View mView, float swipeBackFraction, float SWIPE_BACK_FACTOR) {
                canSwipe = swipeBackFraction<0.1;
            }

            @Override
            public void onViewSwipeFinished(View mView, boolean isEnd) {
                if(!isEnd)
                    canSwipe = true;
                else {
                    finish();
                    overridePendingTransition(0, 0);
                }
            }
        });
        instance = Helper.getLiveInstance(MediaActivity.this);
        mSwipeBackLayout.attachToActivity(this);
        attachments = getIntent().getParcelableArrayListExtra("mediaArray");
        if( getIntent().getExtras() != null)
            mediaPosition = getIntent().getExtras().getInt("position", 1);
        if( attachments == null || attachments.size() == 0)
            finish();

        RelativeLayout main_container_media = findViewById(R.id.main_container_media);
        if( theme == Helper.THEME_LIGHT){
            main_container_media.setBackgroundResource(R.color.mastodonC2);
        }else if( theme == Helper.THEME_BLACK){
            main_container_media.setBackgroundResource(R.color.black);
        }else if( theme == Helper.THEME_DARK){
            main_container_media.setBackgroundResource(R.color.mastodonC1_);
        }
        media_description = findViewById(R.id.media_description);
        message_ready = findViewById(R.id.message_ready);
        media_save = findViewById(R.id.media_save);
        media_share = findViewById(R.id.media_share);
        media_close = findViewById(R.id.media_close);
        progress = findViewById(R.id.loader_progress);
        webview_video = findViewById(R.id.webview_video);

        media_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isSHaring = false;
                if(attachment.getType().toLowerCase().equals("video") || attachment.getType().toLowerCase().equals("audio")  || attachment.getType().toLowerCase().equals("gifv") || attachment.getType().toLowerCase().equals("web")) {
                    if( attachment != null ) {
                        progress.setText("0 %");
                        progress.setVisibility(View.VISIBLE);
                        new HttpsConnection(MediaActivity.this, instance).download(attachment.getUrl(), MediaActivity.this);
                    }
                }else {
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(MediaActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Helper.EXTERNAL_STORAGE_REQUEST_CODE);
                        } else {
                            Helper.manageMoveFileDownload(MediaActivity.this, preview_url, finalUrlDownload, downloadedImage, fileVideo, false);
                        }
                    } else {
                        Helper.manageMoveFileDownload(MediaActivity.this, preview_url, finalUrlDownload, downloadedImage, fileVideo, false);
                    }
                }
            }
        });
        media_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isSHaring = true;
                if(attachment.getType().toLowerCase().equals("video") || attachment.getType().toLowerCase().equals("audio") ||attachment.getType().toLowerCase().equals("gifv")) {
                    if( attachment != null ) {
                        progress.setText("0 %");
                        progress.setVisibility(View.VISIBLE);
                        new HttpsConnection(MediaActivity.this, instance).download(attachment.getUrl(), MediaActivity.this);
                    }
                }else {
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(MediaActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Helper.EXTERNAL_STORAGE_REQUEST_CODE);
                        } else {
                            Helper.manageMoveFileDownload(MediaActivity.this, preview_url, finalUrlDownload, downloadedImage, fileVideo, true);
                        }
                    } else {
                        Helper.manageMoveFileDownload(MediaActivity.this, preview_url, finalUrlDownload, downloadedImage, fileVideo, true);
                    }
                }
            }
        });
        media_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        canSwipe = true;
        loader = findViewById(R.id.loader);
        imageView = findViewById(R.id.media_picture);
        videoView = findViewById(R.id.media_video);
        prev = findViewById(R.id.media_prev);
        next = findViewById(R.id.media_next);
        if( theme == Helper.THEME_BLACK){
            changeDrawableColor(getApplicationContext(), prev, R.color.dark_icon);
            changeDrawableColor(getApplicationContext(), next, R.color.dark_icon);
        }else if(theme == Helper.THEME_LIGHT) {
            changeDrawableColor(getApplicationContext(), prev, R.color.mastodonC4);
            changeDrawableColor(getApplicationContext(), next, R.color.mastodonC4);
        }else{
            changeDrawableColor(getApplicationContext(), prev, R.color.white);
            changeDrawableColor(getApplicationContext(), next, R.color.white);
        }
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPosition--;
                displayMediaAtPosition(actionSwipe.POP);
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPosition++;
                displayMediaAtPosition(actionSwipe.POP);
            }
        });


        imageView.setOnMatrixChangeListener(new OnMatrixChangedListener() {
            @Override
            public void onMatrixChanged(RectF rect) {
                imageScale = imageView.getScale();
                canSwipe = (imageView.getScale() == 1 );
                mSwipeBackLayout.isDisabled(imageView.getScale() != 1 );
            }
        });
        if( attachments != null && attachments.size() > 1){
            prev.setVisibility(View.VISIBLE);
            next.setVisibility(View.VISIBLE);
        }
        pbar_inf = findViewById(R.id.pbar_inf);
        setTitle("");

        //isHiding = false;
        setTitle("");
        displayMediaAtPosition(actionSwipe.POP);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putFloat("scale", imageScale);
        super.onSaveInstanceState(outState);
    }


    /**
     * Manage touch event
     * Allows to swipe from timelines
     * @param event MotionEvent
     * @return boolean
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        Boolean thisControllShown = isControlElementShown;
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN: {
                downX = event.getX();
                downY = event.getY();
                //Displays navigation left/right buttons
                if( attachments != null && attachments.size() > 1){
                    if(thisControllShown){
                        prev.setVisibility(View.GONE);
                        next.setVisibility(View.GONE);
                    }else{
                        prev.setVisibility(View.VISIBLE);
                        next.setVisibility(View.VISIBLE);
                    }
                }
                return super.dispatchTouchEvent(event);
            }
            case MotionEvent.ACTION_UP: {
                float upX = event.getX();
                float deltaX = downX - upX;
                float upY = event.getY();
                float deltaY = downY - upY;
                // swipe horizontal
                if( downX > MIN_DISTANCE & (Math.abs(deltaX) > MIN_DISTANCE ) ){
                    if( !canSwipe || mediaPosition > attachments.size() || mediaPosition < 1 || attachments.size() <= 1)
                        return super.dispatchTouchEvent(event);
                    if(deltaX < 0) { switchOnSwipe(MediaActivity.actionSwipe.LEFT_TO_RIGHT); return true; }
                    if(deltaX > 0) { switchOnSwipe(MediaActivity.actionSwipe.RIGHT_TO_LEFT); return true; }
                }else if(downY > MIN_DISTANCE & (Math.abs(deltaY) > MIN_DISTANCE ) ){
                    if(deltaY > 0 && canSwipe) { finish(); return true; }
                    if(deltaY < 0 && canSwipe) { finish(); return true; }
                } else {
                    currentAction = MediaActivity.actionSwipe.POP;
                    isControlElementShown = !isControlElementShown;
                    if (thisControllShown) {
                        if(event.getY() > action_bar_container.getHeight()) {
                            hideSystemUI();
                            action_bar_container.setVisibility(View.GONE);
                            if (media_description.getVisibility() == View.VISIBLE) {
                                media_description.setVisibility(View.GONE);
                            }
                            if (videoView.getVisibility() == View.VISIBLE)
                                videoView.hideController();
                        }
                    } else {
                        action_bar_container.setVisibility(View.VISIBLE);
                        FullScreencall(thisControllShown);
                        if (attachment != null && attachment.getDescription() != null && !attachment.getDescription().equals("null")) {
                            media_description.setText(attachment.getDescription());
                            media_description.setVisibility(View.VISIBLE);
                            imageView.setContentDescription(attachment.getDescription());
                        } else {
                            media_description.setText("");
                            media_description.setVisibility(View.GONE);
                        }
                        if (videoView.getVisibility() == View.VISIBLE)
                            videoView.showController();
                    }


                }
            }

        }
        return super.dispatchTouchEvent(event);
    }


    private void switchOnSwipe(actionSwipe action){
        loader.setVisibility(View.VISIBLE);
        mediaPosition = (action == actionSwipe.LEFT_TO_RIGHT)?mediaPosition-1:mediaPosition+1;
        displayMediaAtPosition(action);
    }

    private void displayMediaAtPosition(actionSwipe action){
        if( mediaPosition > attachments.size() )
            mediaPosition = 1;
        if( mediaPosition < 1)
            mediaPosition = attachments.size();
        currentAction = action;
        attachment = attachments.get(mediaPosition-1);
        String type = attachment.getType();
        String url = attachment.getUrl();
        finalUrlDownload = url;
        videoView.setVisibility(View.GONE);

        imageView.setVisibility(View.GONE);

        if( attachment.getDescription() != null && !attachment.getDescription().equals("null")){
            media_description.setText(attachment.getDescription());
            media_description.setVisibility(View.VISIBLE);
        }else{
            media_description.setText("");
            media_description.setVisibility(View.GONE);
        }
        preview_url = attachment.getPreview_url();
        if( type.equals("unknown")){
            preview_url = attachment.getRemote_url();
            if( preview_url.endsWith(".png") || preview_url.endsWith(".jpg")|| preview_url.endsWith(".jpeg")) {
                type = "image";
            }else if( preview_url.endsWith(".mp4") || preview_url.endsWith(".mp3")) {
                type = "video";
            }
            url = attachment.getRemote_url();
            attachment.setType(type);
        }
        final String finalUrl = url;
        switch (type.toLowerCase()){
            case "image":
                pbar_inf.setScaleY(1f);
                imageView.setVisibility(View.VISIBLE);
                fileVideo = null;
                pbar_inf.setIndeterminate(true);
                loader.setVisibility(View.VISIBLE);
                fileVideo = null;
                Glide.with(getApplicationContext())
                    .asBitmap()
                    .load(preview_url).into(
                    new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull final Bitmap resource, Transition<? super Bitmap> transition) {
                            Bitmap imageCompressed = Helper.compressImageIfNeeded(MediaActivity.this, resource);
                            imageView.setImageBitmap(imageCompressed);
                            Glide.with(getApplicationContext())
                                .asBitmap()
                                .load(finalUrl).into(
                                new SimpleTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(@NonNull final Bitmap resource, Transition<? super Bitmap> transition) {
                                        loader.setVisibility(View.GONE);
                                        Bitmap imageCompressed = Helper.compressImageIfNeeded(MediaActivity.this, resource);
                                        if( imageView.getScale() < 1.1) {
                                            downloadedImage = resource;
                                            imageView.setImageBitmap(imageCompressed);
                                        }else{
                                            message_ready.setVisibility(View.VISIBLE);
                                        }
                                        message_ready.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                downloadedImage = resource;
                                                imageView.setImageBitmap(imageCompressed);
                                                message_ready.setVisibility(View.GONE);
                                            }
                                        });
                                    }
                                }
                            );
                        }
                    }
                );
                break;
            case "video":
            case "audio":
            case "gifv":
                pbar_inf.setIndeterminate(false);
                pbar_inf.setScaleY(3f);
                try {
                    HttpsURLConnection.setDefaultSSLSocketFactory(new TLSSocketFactory());
                } catch (KeyManagementException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                videoView.setVisibility(View.VISIBLE);
                Uri uri = Uri.parse(url);
                DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getApplicationContext(),
                        Util.getUserAgent(getApplicationContext(), "Mastalab"), null);
                ExtractorMediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(uri);
                player = ExoPlayerFactory.newSimpleInstance(MediaActivity.this);
                if( type.toLowerCase().equals("gifv"))
                    player.setRepeatMode(Player.REPEAT_MODE_ONE);
                videoView.setPlayer(player);
                loader.setVisibility(View.GONE);
                player.prepare(videoSource);
                player.setPlayWhenReady(true);
                break;
            case "web":
                loader.setVisibility(View.GONE);
                webview_video = Helper.initializeWebview(MediaActivity.this, R.id.webview_video);
                webview_video.setVisibility(View.VISIBLE);
                FrameLayout webview_container = findViewById(R.id.main_media_frame);
                final ViewGroup videoLayout = findViewById(R.id.videoLayout);

                MastalabWebChromeClient mastalabWebChromeClient = new MastalabWebChromeClient(MediaActivity.this, webview_video, webview_container, videoLayout);
                mastalabWebChromeClient.setOnToggledFullscreen(new MastalabWebChromeClient.ToggledFullscreenCallback() {
                    @Override
                    public void toggledFullscreen(boolean fullscreen) {

                        if (fullscreen) {
                            videoLayout.setVisibility(View.VISIBLE);
                            WindowManager.LayoutParams attrs = getWindow().getAttributes();
                            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                            attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                            getWindow().setAttributes(attrs);
                            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                        } else {
                            WindowManager.LayoutParams attrs = getWindow().getAttributes();
                            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                            attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                            getWindow().setAttributes(attrs);
                            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                            videoLayout.setVisibility(View.GONE);
                        }
                    }
                });
                webview_video.getSettings().setAllowFileAccess(true);
                webview_video.setWebChromeClient(mastalabWebChromeClient);
                webview_video.getSettings().setDomStorageEnabled(true);
                webview_video.getSettings().setAppCacheEnabled(true);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    webview_video.getSettings().setMediaPlaybackRequiresUserGesture(false);
                }
                webview_video.setWebViewClient(new MastalabWebViewClient(MediaActivity.this));
                webview_video.loadUrl(attachment.getUrl());
                break;
        }
    }

    @Override
    public void onDownloaded(String path, String originUrl, Error error) {

        if( path != null) {
            File response = new File(path);
            File dir = getCacheDir();
            File from = new File(dir, response.getName());
            File to = new File(dir, Helper.md5(originUrl) + ".mp4");
            if (from.exists())
                //noinspection ResultOfMethodCallIgnored
                from.renameTo(to);
            fileVideo = to;
            downloadedImage = null;
        }
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MediaActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Helper.EXTERNAL_STORAGE_REQUEST_CODE);
            } else {
                Helper.manageMoveFileDownload(MediaActivity.this, preview_url, finalUrlDownload, downloadedImage, fileVideo, isSHaring);
            }
        } else {
            Helper.manageMoveFileDownload(MediaActivity.this, preview_url, finalUrlDownload, downloadedImage, fileVideo, isSHaring);
        }
        if( progress != null)
            progress.setVisibility(View.GONE);
        if( loader != null)
            loader.setVisibility(View.GONE);
    }

    @Override
    public void onPause(){
        super.onPause();
        if( player != null) {
            player.setPlayWhenReady(false);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if( player != null) {
            player.setPlayWhenReady(true);
        }

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        FullScreencall(false);
    }

    public void FullScreencall(Boolean shouldFullscreen) {
        if(Build.VERSION.SDK_INT < 19) {
            View v = this.getWindow().getDecorView();
            if(shouldFullscreen){
                v.setSystemUiVisibility(View.GONE);
            }else {
                v.setSystemUiVisibility(View.VISIBLE);
            }
        } else {
            View decorView = getWindow().getDecorView();
            if(shouldFullscreen){
                decorView.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_IMMERSIVE
                                // Set the content to appear under the system bars so that the
                                // content doesn't resize when the system bars hide and show.
                                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                // Hide the nav bar and status bar
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }else{
                decorView.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            }
        }
    }

    @Override
    public void onUpdateProgress(int progressPercentage) {
        progress.setText(String.format("%s%%",String.valueOf(progressPercentage)));
        pbar_inf.setProgress(progressPercentage);
    }


    private void hideSystemUI() {
        View mDecorView = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mDecorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE);
        }
    }

}
