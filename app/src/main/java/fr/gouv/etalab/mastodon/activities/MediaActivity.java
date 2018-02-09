/* Copyright 2017 Thomas Schneider
 *
 * This file is a part of Mastalab
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Mastalab is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Mastalab; if not,
 * see <http://www.gnu.org/licenses>. */
package fr.gouv.etalab.mastodon.activities;


import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.github.chrisbanes.photoview.PhotoView;
import java.io.File;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.client.Entities.Attachment;
import fr.gouv.etalab.mastodon.client.Entities.Error;
import fr.gouv.etalab.mastodon.client.HttpsConnection;
import fr.gouv.etalab.mastodon.client.TLSSocketFactory;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnDownloadInterface;


import static fr.gouv.etalab.mastodon.helper.Helper.EXTERNAL_STORAGE_REQUEST_CODE;
import static fr.gouv.etalab.mastodon.helper.Helper.changeDrawableColor;


/**
 * Created by Thomas on 25/06/2017.
 * Media Activity
 */

public class MediaActivity extends BaseActivity implements OnDownloadInterface {


    private RelativeLayout loader;
    private ArrayList<Attachment>  attachments;
    private PhotoView imageView;
    private VideoView videoView;
    private float downX;
    private int mediaPosition;
    MediaActivity.actionSwipe currentAction;
    static final int MIN_DISTANCE = 100;
    private String finalUrlDownload;
    private String preview_url;
    private ImageView prev, next;
    private boolean isHiding;
    private Bitmap downloadedImage;
    private File fileVideo;
    private TextView progress;
    private ProgressBar pbar_inf;
    private TextView message_ready;
    private boolean canSwipe;

    private enum actionSwipe{
        RIGHT_TO_LEFT,
        LEFT_TO_RIGHT,
        POP
    }

    private FloatingActionButton media_save, media_close;
    private boolean scheduleHidden;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if( theme == Helper.THEME_LIGHT){
            setTheme(R.style.AppTheme_NoActionBar);
        }else {
            setTheme(R.style.AppThemeDark_NoActionBar);
        }
        setContentView(R.layout.activity_media);
        attachments = getIntent().getParcelableArrayListExtra("mediaArray");
        if( getIntent().getExtras() != null)
            mediaPosition = getIntent().getExtras().getInt("position", 1);
        if( attachments == null || attachments.size() == 0)
            finish();

        RelativeLayout main_container_media = findViewById(R.id.main_container_media);
        if( theme == Helper.THEME_LIGHT){
            main_container_media.setBackgroundResource(R.color.mastodonC2);
        }else {
            main_container_media.setBackgroundResource(R.color.mastodonC1_);
        }
        message_ready = findViewById(R.id.message_ready);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        media_save = findViewById(R.id.media_save);
        media_close = findViewById(R.id.media_close);

        media_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT >= 23 ){
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {
                        ActivityCompat.requestPermissions(MediaActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_REQUEST_CODE);
                    } else {
                        Helper.manageMoveFileDownload(MediaActivity.this, preview_url, finalUrlDownload, downloadedImage, fileVideo);
                    }
                }else{
                    Helper.manageMoveFileDownload(MediaActivity.this, preview_url, finalUrlDownload, downloadedImage, fileVideo);
                }
            }
        });
        media_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        Handler h = new Handler();
        scheduleHidden = true;
        h.postDelayed(new Runnable() {

            @Override
            public void run() {
                // DO DELAYED STUFF
                media_close.setVisibility(View.GONE);
                media_save.setVisibility(View.GONE);
                scheduleHidden = false;
            }
        }, 2000);
        canSwipe = true;
        loader = findViewById(R.id.loader);
        imageView = findViewById(R.id.media_picture);
        videoView = findViewById(R.id.media_video);
        prev = findViewById(R.id.media_prev);
        next = findViewById(R.id.media_next);
        changeDrawableColor(getApplicationContext(), prev,R.color.mastodonC4);
        changeDrawableColor(getApplicationContext(), next,R.color.mastodonC4);
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

        progress = findViewById(R.id.loader_progress);
        pbar_inf = findViewById(R.id.pbar_inf);
        setTitle("");

        isHiding = false;
        setTitle("");
        displayMediaAtPosition(actionSwipe.POP);
    }



    /**
     * Manage touch event
     * Allows to swipe from timelines
     * @param event MotionEvent
     * @return boolean
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        if( event.getAction() == MotionEvent.ACTION_DOWN && !scheduleHidden){
            scheduleHidden = true;
            media_close.setVisibility(View.VISIBLE);
            media_save.setVisibility(View.VISIBLE);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    media_close.setVisibility(View.GONE);
                    media_save.setVisibility(View.GONE);
                    scheduleHidden = false;
                }
            }, 2000);
        }
        if( !canSwipe || mediaPosition > attachments.size() || mediaPosition < 1 || attachments.size() <= 1)
            return super.dispatchTouchEvent(event);
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN: {
                downX = event.getX();
                //Displays navigation left/right buttons
                if( attachments != null && attachments.size() > 1 && !isHiding){
                    prev.setVisibility(View.VISIBLE);
                    next.setVisibility(View.VISIBLE);
                    isHiding = true;
                    new Handler().postDelayed(new Runnable(){
                        public void run() {
                            prev.setVisibility(View.GONE);
                            next.setVisibility(View.GONE);
                            isHiding = false;
                        }
                    }, 2000);
                }
                return super.dispatchTouchEvent(event);
            }
            case MotionEvent.ACTION_UP: {
                float upX = event.getX();
                float deltaX = downX - upX;
                // swipe horizontal

                if( downX > MIN_DISTANCE & (Math.abs(deltaX) > MIN_DISTANCE ) ){
                    if(deltaX < 0) { switchOnSwipe(MediaActivity.actionSwipe.LEFT_TO_RIGHT); return true; }
                    if(deltaX > 0) { switchOnSwipe(MediaActivity.actionSwipe.RIGHT_TO_LEFT); return true; }
                }else{
                    currentAction = MediaActivity.actionSwipe.POP;
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
        final Attachment attachment = attachments.get(mediaPosition-1);
        String type = attachment.getType();
        String url = attachment.getUrl();
        finalUrlDownload = url;
        videoView.setVisibility(View.GONE);
        if( videoView.isPlaying()) {
            videoView.stopPlayback();
        }
        imageView.setVisibility(View.GONE);
        preview_url = attachment.getPreview_url();
        if( type.equals("unknown")){
            preview_url = attachment.getRemote_url();
            if( preview_url.endsWith(".png") || preview_url.endsWith(".jpg")|| preview_url.endsWith(".jpeg")) {
                type = "image";
            }else if( preview_url.endsWith(".mp4")) {
                type = "video";
            }
            url = attachment.getRemote_url();
            attachment.setType(type);
        }
        final String finalUrl = url;
        switch (type){
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
                            imageView.setImageBitmap(resource);
                            Glide.with(getApplicationContext())
                                .asBitmap()
                                .load(finalUrl).into(
                                new SimpleTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(@NonNull final Bitmap resource, Transition<? super Bitmap> transition) {
                                        loader.setVisibility(View.GONE);
                                        if( imageView.getScale() < 1.1) {
                                            downloadedImage = resource;
                                            imageView.setImageBitmap(resource);
                                        }else{
                                            message_ready.setVisibility(View.VISIBLE);
                                        }
                                        message_ready.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                downloadedImage = resource;
                                                imageView.setImageBitmap(resource);
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
            case "gifv":
                pbar_inf.setIndeterminate(false);
                pbar_inf.setScaleY(3f);
                File file = new File(getCacheDir() + "/" + Helper.md5(url)+".mp4");
                if(file.exists()) {
                    Uri uri = Uri.parse(file.getAbsolutePath());
                    videoView.setVisibility(View.VISIBLE);
                    try {
                        HttpsURLConnection.setDefaultSSLSocketFactory(new TLSSocketFactory());
                    } catch (KeyManagementException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    videoView.setVideoURI(uri);
                    videoView.start();
                    MediaController mc = new MediaController(MediaActivity.this);
                    videoView.setMediaController(mc);
                    videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            loader.setVisibility(View.GONE);
                            mp.start();
                            mp.setLooping(true);
                        }
                    });
                    fileVideo = file;
                    downloadedImage = null;
                }else{
                    videoView.setVisibility(View.VISIBLE);
                    Uri uri = Uri.parse(url);
                    videoView.setVideoURI(uri);
                    videoView.start();
                    MediaController mc = new MediaController(MediaActivity.this);
                    mc.setAnchorView(videoView);
                    videoView.setMediaController(mc);
                    videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mp.start();
                            mp.setLooping(true);
                        }
                    });
                    videoView.start();
                    progress.setText("0 %");
                    progress.setVisibility(View.VISIBLE);
                    new HttpsConnection(MediaActivity.this).download(finalUrl, MediaActivity.this );
                }
                break;
        }
        String filename = URLUtil.guessFileName(url, null, null);
        if( filename == null)
            filename = url;
        if( attachments.size() > 1 )
            filename = String.format("%s  (%s/%s)",filename, mediaPosition, attachments.size());
    }

    @Override
    public void onDownloaded(String path, String originUrl, Error error) {

        File response = new File(path);
        File dir = getCacheDir();
        File from = new File(dir, response.getName());
        File to = new File(dir, Helper.md5(originUrl) + ".mp4");
        if (from.exists())
            //noinspection ResultOfMethodCallIgnored
            from.renameTo(to);
        fileVideo = to;
        downloadedImage = null;
        if( progress != null)
            progress.setVisibility(View.GONE);
        if( loader != null)
            loader.setVisibility(View.GONE);
    }


    @Override
    public void onUpdateProgress(int progressPercentage) {
        progress.setText(String.format("%s%%",String.valueOf(progressPercentage)));
        pbar_inf.setProgress(progressPercentage);
    }

}
