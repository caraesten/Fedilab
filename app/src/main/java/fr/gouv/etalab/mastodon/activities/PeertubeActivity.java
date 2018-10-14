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


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import java.lang.ref.WeakReference;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.asynctasks.RetrievePeertubeSingleAsyncTask;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Peertube;
import fr.gouv.etalab.mastodon.client.Entities.Results;
import fr.gouv.etalab.mastodon.client.TLSSocketFactory;
import fr.gouv.etalab.mastodon.helper.FullScreenMediaController;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrievePeertubeInterface;


/**
 * Created by Thomas on 14/10/20178.
 * Peertube activity
 */

public class PeertubeActivity extends BaseActivity implements OnRetrievePeertubeInterface {

    private String url, stream_url, peertubeInstance, videoId;
    private String peertubeLinkToFetch;
    private boolean peertubeLink;
    private FullScreenMediaController.fullscreen fullscreen;
    private VideoView videoView;
    private RelativeLayout loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        fullscreen = FullScreenMediaController.fullscreen.OFF;
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        switch (theme){
            case Helper.THEME_LIGHT:
                setTheme(R.style.AppTheme);
                break;
            case Helper.THEME_DARK:
                setTheme(R.style.AppThemeDark);
                break;
            case Helper.THEME_BLACK:
                setTheme(R.style.AppThemeBlack);
                break;
            default:
                setTheme(R.style.AppThemeDark);
        }

        setContentView(R.layout.activity_peertube);
        loader = findViewById(R.id.loader);
        loader.setVisibility(View.VISIBLE);
        Bundle b = getIntent().getExtras();
        if(b != null) {
            peertubeInstance = b.getString("peertube_instance", null);
            videoId = b.getString("video_id", null);
            stream_url = b.getString("stream_url", null);
            peertubeLinkToFetch = b.getString("peertubeLinkToFetch", null);
            peertubeLink = b.getBoolean("peertubeLink", true);
            url = b.getString("url", null);
        }


        if( url == null)
            finish();
        if( getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        videoView = findViewById(R.id.media_video);
        new RetrievePeertubeSingleAsyncTask(PeertubeActivity.this, peertubeInstance, videoId, PeertubeActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        /*if(fullscreen == FullScreenMediaController.fullscreen.ON){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getSupportActionBar().hide();
        }else{
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
            getSupportActionBar().show();
        }*/
    }

    public void change(){
        if(fullscreen == FullScreenMediaController.fullscreen.ON){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            Objects.requireNonNull(getSupportActionBar()).hide();
        }else{
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
            Objects.requireNonNull(getSupportActionBar()).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_webview, menu);
        if( peertubeLink ){
            menu.findItem(R.id.action_go).setVisible(false);
            menu.findItem(R.id.action_comment).setVisible(true);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_go:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                try {
                    startActivity(browserIntent);
                }catch (Exception e){
                    Toast.makeText(PeertubeActivity.this, R.string.toast_error, Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.action_comment:
                Toast.makeText(PeertubeActivity.this, R.string.retrieve_remote_status, Toast.LENGTH_LONG).show();
                new AsyncTask<Void, Void, Void>() {

                    private List<fr.gouv.etalab.mastodon.client.Entities.Status> remoteStatuses;
                    private WeakReference<Context> contextReference = new WeakReference<>(PeertubeActivity.this);

                    @Override
                    protected Void doInBackground(Void... voids) {

                        if(url != null) {
                            Results search = new API(contextReference.get()).search(peertubeLinkToFetch);
                            if (search != null) {
                                remoteStatuses = search.getStatuses();
                            }
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        Intent intent = new Intent(contextReference.get(), TootActivity.class);
                        Bundle b = new Bundle();
                        if( remoteStatuses == null || remoteStatuses.size() == 0){
                            Toast.makeText(contextReference.get(), R.string.toast_error, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if( remoteStatuses.get(0).getReblog() != null ) {
                            b.putParcelable("tootReply", remoteStatuses.get(0).getReblog());
                        }else {
                            b.putParcelable("tootReply", remoteStatuses.get(0));
                        }
                        intent.putExtras(b); //Put your id to your next Intent
                        contextReference.get().startActivity(intent);
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR );
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public FullScreenMediaController.fullscreen getFullscreen() {
        return fullscreen;
    }

    public void setFullscreen(FullScreenMediaController.fullscreen fullscreen) {
        this.fullscreen = fullscreen;
    }

    @Override
    public void onRetrievePeertube(APIResponse apiResponse) {

        if( apiResponse == null || (apiResponse.getError() != null) || apiResponse.getPeertubes() == null || apiResponse.getPeertubes().size() == 0){
            Toast.makeText(PeertubeActivity.this, R.string.toast_error,Toast.LENGTH_LONG).show();
            loader.setVisibility(View.GONE);
            return;
        }
        if( apiResponse.getPeertubes().get(0).getStreamURL() == null){
            Toast.makeText(PeertubeActivity.this, R.string.toast_error,Toast.LENGTH_LONG).show();
            loader.setVisibility(View.GONE);
            return;
        }

        Uri uri = Uri.parse(apiResponse.getPeertubes().get(0).getStreamURL());
        try {
            HttpsURLConnection.setDefaultSSLSocketFactory(new TLSSocketFactory());
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        videoView.setVideoURI(uri);
        FullScreenMediaController mc = new FullScreenMediaController(PeertubeActivity.this);
        mc.setAnchorView(videoView);
        videoView.setMediaController(mc);
        videoView.start();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                loader.setVisibility(View.GONE);
                mp.start();
            }
        });
    }
}
