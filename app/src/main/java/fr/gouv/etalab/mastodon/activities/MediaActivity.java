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
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.github.chrisbanes.photoview.OnMatrixChangedListener;
import com.github.chrisbanes.photoview.PhotoView;
import java.io.File;
import java.util.ArrayList;
import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.client.Entities.Attachment;
import fr.gouv.etalab.mastodon.client.Entities.Error;
import fr.gouv.etalab.mastodon.client.HttpsConnection;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnDownloadInterface;


import static fr.gouv.etalab.mastodon.helper.Helper.EXTERNAL_STORAGE_REQUEST_CODE;
import static fr.gouv.etalab.mastodon.helper.Helper.changeDrawableColor;


/**
 * Created by Thomas on 25/06/2017.
 * Media Activity
 */

public class MediaActivity extends AppCompatActivity implements OnDownloadInterface {


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
    private boolean canSwipe;




    private enum actionSwipe{
        RIGHT_TO_LEFT,
        LEFT_TO_RIGHT,
        POP
    }

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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if( getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.picture_actionbar, null);
            getSupportActionBar().setCustomView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);


            ImageView action_save = getSupportActionBar().getCustomView().findViewById(R.id.action_save);
            ImageView close = getSupportActionBar().getCustomView().findViewById(R.id.close);
            if( close != null){
                close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
            }
            action_save.setOnClickListener(new View.OnClickListener() {
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
            Handler h = new Handler();

            h.postDelayed(new Runnable() {

                @Override
                public void run() {
                    // DO DELAYED STUFF
                    if(canSwipe)
                        getSupportActionBar().hide();
                }
            }, 2000);
        }


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
        imageView.setOnMatrixChangeListener(new OnMatrixChangedListener() {
            @Override
            public void onMatrixChanged(RectF rect) {
                canSwipe = (imageView.getScale() == 1 );
                if( !canSwipe && getSupportActionBar() != null && getSupportActionBar().isShowing()){
                    getSupportActionBar().hide();
                }
            }
        });

        progress = findViewById(R.id.loader_progress);
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

        if( event.getAction() == MotionEvent.ACTION_DOWN){
            if( getSupportActionBar() != null && !getSupportActionBar().isShowing() && canSwipe) {
                getSupportActionBar().show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getSupportActionBar().hide();
                    }
                }, 2000);
            }
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
                    }, 1000);
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
                Glide.with(getApplicationContext())
                        .asBitmap()
                        .load(url)
                        .listener(new RequestListener<Bitmap>() {

                            @Override
                            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                                loader.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                loader.setVisibility(View.GONE);
                                imageView.setVisibility(View.VISIBLE);
                                downloadedImage = resource;
                                imageView.setImageBitmap(resource);
                                fileVideo = null;
                            }
                        });
                break;
            case "video":
            case "gifv":

                File file = new File(getCacheDir() + "/" + Helper.md5(url)+".mp4");
                if(file.exists()) {
                    Uri uri = Uri.parse(file.getAbsolutePath());
                    videoView.setVisibility(View.VISIBLE);
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
                    videoView.setVisibility(View.VISIBLE);
                    fileVideo = file;
                    downloadedImage = null;
                }else{
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
        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null){
            TextView picture_actionbar_title = actionBar.getCustomView().findViewById(R.id.toolbar_title);
            picture_actionbar_title.setText(filename);
        }else {
            setTitle(url);
        }
    }

    @Override
    public void onDownloaded(String path, Error error) {
        progress.setVisibility(View.GONE);
        videoView.setVisibility(View.VISIBLE);
        videoView.setVideoURI(Uri.parse(path));
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
        videoView.setVisibility(View.VISIBLE);
    }


    @Override
    public void onUpdateProgress(int progressPercentage) {
        progress.setText(String.format("%s%%",String.valueOf(progressPercentage)));
    }

}
