/* Copyright 2017 Thomas Schneider
 *
 * This file is a part of Mastodon Etalab for mastodon.etalab.gouv.fr
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Mastodon Etalab is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Thomas Schneider; if not,
 * see <http://www.gnu.org/licenses>. */
package fr.gouv.etalab.mastodon.activities;


import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.github.chrisbanes.photoview.OnMatrixChangedListener;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.OnScaleChangedListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import fr.gouv.etalab.mastodon.client.Entities.Attachment;
import fr.gouv.etalab.mastodon.client.MastalabSSLSocketFactory;
import fr.gouv.etalab.mastodon.helper.Helper;
import mastodon.etalab.gouv.fr.mastodon.R;

import static fr.gouv.etalab.mastodon.helper.Helper.EXTERNAL_STORAGE_REQUEST_CODE;


/**
 * Created by Thomas on 25/06/2017.
 * Media Activity
 */

public class MediaActivity extends AppCompatActivity  {


    private RelativeLayout loader;
    private ArrayList<Attachment>  attachments;
    private PhotoView imageView;
    private VideoView videoView;
    private float downX, downY;
    private int mediaPosition;
    MediaActivity.actionSwipe currentAction;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
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
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_LIGHT);
        if( theme == Helper.THEME_LIGHT){
            setTheme(R.style.AppTheme);
        }else {
            setTheme(R.style.AppThemeDark);
        }
        setContentView(R.layout.activity_media);
        attachments = getIntent().getParcelableArrayListExtra("mediaArray");
        mediaPosition = getIntent().getExtras().getInt("position", 1);
        if( attachments == null || attachments.size() == 0)
            finish();
        if( getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RelativeLayout main_container_media = (RelativeLayout) findViewById(R.id.main_container_media);
        if( theme == Helper.THEME_LIGHT){
            main_container_media.setBackgroundResource(R.color.background_image);
        }else {
            main_container_media.setBackgroundResource(R.color.colorPrimaryD);
        }
        canSwipe = true;
        loader = (RelativeLayout) findViewById(R.id.loader);
        imageView = (PhotoView) findViewById(R.id.media_picture);
        videoView = (VideoView) findViewById(R.id.media_video);
        prev = (ImageView) findViewById(R.id.media_prev);
        next = (ImageView) findViewById(R.id.media_next);
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
            }
        });

        progress = (TextView) findViewById(R.id.loader_progress);
        setTitle("");

        isHiding = false;
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder().displayer(new SimpleBitmapDisplayer()).cacheInMemory(false)
                .cacheOnDisk(true).resetViewBeforeLoading(true).build();
        setTitle("");
        displayMediaAtPosition(actionSwipe.POP);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_download:
                if(Build.VERSION.SDK_INT >= 23 ){
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {
                        ActivityCompat.requestPermissions(MediaActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_REQUEST_CODE);
                    } else {
                        Helper.manageMoveFileDownload(MediaActivity.this, preview_url, finalUrlDownload, downloadedImage, fileVideo);
                    }
                }else{
                    Helper.manageMoveFileDownload(MediaActivity.this, preview_url, finalUrlDownload, downloadedImage, fileVideo);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_media, menu);
        return true;
    }



    /**
     * Manage touch event
     * Allows to swipe from timelines
     * @param event MotionEvent
     * @return boolean
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if( !canSwipe || mediaPosition > attachments.size() || mediaPosition < 1 || attachments.size() <= 1)
            return super.dispatchTouchEvent(event);
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN: {
                downX = event.getX();
                downY = event.getY();
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
                float upY = event.getY();
                float deltaX = downX - upX;
                float deltaY = downY - upY;
                // swipe horizontal

                if( downX > MIN_DISTANCE & (Math.abs(deltaX) > MIN_DISTANCE && Math.abs(deltaY) < MIN_DISTANCE)){
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
        final String url = attachment.getUrl();
        finalUrlDownload = url;
        videoView.setVisibility(View.GONE);
        if( videoView.isPlaying()) {
            videoView.stopPlayback();
        }
        imageView.setVisibility(View.GONE);
        preview_url = attachment.getPreview_url();
        switch (type){
            case "image":
                imageLoader.displayImage(url, imageView, options, new SimpleImageLoadingListener(){
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        super.onLoadingComplete(imageUri, view, loadedImage);
                        loader.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);
                        downloadedImage = loadedImage;
                        fileVideo = null;
                    }
                    @Override
                    public void onLoadingFailed(java.lang.String imageUri, android.view.View view, FailReason failReason){
                        imageLoader.displayImage(url, imageView, options);
                        loader.setVisibility(View.GONE);
                    }
                });
                break;
            case "video":
            case "gifv":
                AsyncHttpClient client = new AsyncHttpClient();
                MastalabSSLSocketFactory mastalabSSLSocketFactory;
                try {
                    mastalabSSLSocketFactory = new MastalabSSLSocketFactory(MastalabSSLSocketFactory.getKeystore());
                    mastalabSSLSocketFactory.setHostnameVerifier(MastalabSSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                    client.setSSLSocketFactory(mastalabSSLSocketFactory);
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
                            }
                        });
                        videoView.setVisibility(View.VISIBLE);
                        fileVideo = file;
                        downloadedImage = null;
                    }else{
                        progress.setText("0 %");
                        progress.setVisibility(View.VISIBLE);
                        client.get(url, new FileAsyncHttpResponseHandler(/* Context */ this) {
                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                                progress.setVisibility(View.GONE);
                            }

                            @Override
                            public void onProgress(long bytesWritten, long totalSize) {
                                long progressPercentage = (long)100*bytesWritten/totalSize;
                                progress.setText(String.valueOf(progressPercentage) + " %");
                            }

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, File response) {
                                File dir = getCacheDir();
                                File from = new File(dir, response.getName());
                                File to = new File(dir, Helper.md5(url) + ".mp4");
                                if (from.exists())
                                    from.renameTo(to);
                                fileVideo = to;
                                downloadedImage = null;
                                progress.setVisibility(View.GONE);
                                Uri uri = Uri.parse(to.getAbsolutePath());
                                videoView.setVisibility(View.VISIBLE);
                                videoView.setVideoURI(uri);
                                videoView.start();
                                MediaController mc = new MediaController(MediaActivity.this);
                                videoView.setMediaController(mc);
                                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                    @Override
                                    public void onPrepared(MediaPlayer mp) {
                                        loader.setVisibility(View.GONE);
                                    }
                                });
                                videoView.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException | UnrecoverableKeyException e) {
                    e.printStackTrace();
                }
                break;
        }
        String filename = URLUtil.guessFileName(url, null, null);
        if( filename == null)
            filename = url;
        LayoutInflater mInflater = LayoutInflater.from(MediaActivity.this);
        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null){
            View picture_actionbar = mInflater.inflate(R.layout.picture_actionbar, null);
            TextView picture_actionbar_title = (TextView) picture_actionbar.findViewById(R.id.picture_actionbar);
            picture_actionbar_title.setText(filename);
            actionBar.setCustomView(picture_actionbar);
            actionBar.setDisplayShowCustomEnabled(true);
        }else {
            setTitle(url);
        }
    }


}
