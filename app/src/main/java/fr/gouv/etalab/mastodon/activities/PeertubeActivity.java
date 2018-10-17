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


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveFeedsAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrievePeertubeSingleAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrievePeertubeSingleCommentsAsyncTask;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Peertube;
import fr.gouv.etalab.mastodon.client.Entities.Results;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.client.TLSSocketFactory;
import fr.gouv.etalab.mastodon.drawers.StatusListAdapter;
import fr.gouv.etalab.mastodon.helper.FullScreenMediaController;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrievePeertubeInterface;

import static fr.gouv.etalab.mastodon.helper.Helper.changeDrawableColor;


/**
 * Created by Thomas on 14/10/20178.
 * Peertube activity
 */

public class PeertubeActivity extends BaseActivity implements OnRetrievePeertubeInterface {

    private String peertubeInstance, videoId;
    private String peertubeLinkToFetch;
    private FullScreenMediaController.fullscreen fullscreen;
    private VideoView videoView;
    private RelativeLayout loader;
    private TextView peertube_view_count, peertube_like_count, peertube_dislike_count, peertube_share, peertube_download, peertube_description, peertube_title;
    private ScrollView peertube_information_container;
    private ProgressDialog pDialog;
    public static final int progress_bar_type = 0;

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
        peertube_view_count = findViewById(R.id.peertube_view_count);
        peertube_like_count = findViewById(R.id.peertube_like_count);
        peertube_dislike_count = findViewById(R.id.peertube_dislike_count);
        peertube_share = findViewById(R.id.peertube_share);
        peertube_download = findViewById(R.id.peertube_download);
        peertube_description = findViewById(R.id.peertube_description);
        peertube_title = findViewById(R.id.peertube_title);
        peertube_information_container = findViewById(R.id.peertube_information_container);

        loader.setVisibility(View.VISIBLE);
        Bundle b = getIntent().getExtras();
        if(b != null) {
            peertubeInstance = b.getString("peertube_instance", null);
            videoId = b.getString("video_id", null);
            peertubeLinkToFetch = b.getString("peertubeLinkToFetch", null);
        }
        if( peertubeLinkToFetch == null)
            finish();
        if( getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        videoView = findViewById(R.id.media_video);
        new RetrievePeertubeSingleAsyncTask(PeertubeActivity.this, peertubeInstance, videoId, PeertubeActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new RetrievePeertubeSingleCommentsAsyncTask(PeertubeActivity.this, peertubeInstance, videoId, PeertubeActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void change(){
        if(fullscreen == FullScreenMediaController.fullscreen.ON){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            Objects.requireNonNull(getSupportActionBar()).hide();
            peertube_information_container.setVisibility(View.GONE);
        }else{
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
            Objects.requireNonNull(getSupportActionBar()).show();
            peertube_information_container.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_webview, menu);
        menu.findItem(R.id.action_go).setVisible(false);
        menu.findItem(R.id.action_comment).setVisible(true);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_comment:
                Toast.makeText(PeertubeActivity.this, R.string.retrieve_remote_status, Toast.LENGTH_LONG).show();
                new AsyncTask<Void, Void, Void>() {

                    private List<fr.gouv.etalab.mastodon.client.Entities.Status> remoteStatuses;
                    private WeakReference<Context> contextReference = new WeakReference<>(PeertubeActivity.this);

                    @Override
                    protected Void doInBackground(Void... voids) {

                        if(peertubeLinkToFetch != null) {
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
        Peertube peertube = apiResponse.getPeertubes().get(0);
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);

        setTitle(peertube.getName());
        peertube_description.setText(peertube.getDescription());
        peertube_title.setText(peertube.getName());
        peertube_dislike_count.setText(String.valueOf(peertube.getDislike()));
        peertube_like_count.setText(String.valueOf(peertube.getLike()));
        peertube_view_count.setText(String.valueOf(peertube.getView()));

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
                //mp.start();
            }
        });
        peertube_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DownloadFileFromURL().execute(peertube.getStreamURL());
            }
        });
    }

    @Override
    public void onRetrievePeertubeComments(APIResponse apiResponse) {
        if( apiResponse == null || (apiResponse.getError() != null && apiResponse.getError().getStatusCode() != 404) ){
            if( apiResponse == null)
                Toast.makeText(PeertubeActivity.this, R.string.toast_error,Toast.LENGTH_LONG).show();
            else
                Toast.makeText(PeertubeActivity.this, apiResponse.getError().getError(),Toast.LENGTH_LONG).show();
            return;
        }
        List<Status> statuses = apiResponse.getStatuses();
        Log.v(Helper.TAG,"statuses " + statuses);
        if( statuses != null)
            Log.v(Helper.TAG,"size " + statuses.size());
        if( statuses == null || statuses.size() == 0){
            RelativeLayout no_action = findViewById(R.id.no_action);
            no_action.setVisibility(View.VISIBLE);
            RecyclerView lv_comments = findViewById(R.id.peertube_comments);
            lv_comments.setVisibility(View.GONE);
        }else {
            SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            boolean isOnWifi = Helper.isOnWIFI(PeertubeActivity.this);
            int behaviorWithAttachments = sharedpreferences.getInt(Helper.SET_ATTACHMENT_ACTION, Helper.ATTACHMENT_ALWAYS);
            int positionSpinnerTrans = sharedpreferences.getInt(Helper.SET_TRANSLATOR, Helper.TRANS_YANDEX);
            String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
            StatusListAdapter statusListAdapter = new StatusListAdapter(PeertubeActivity.this, RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE, userId, isOnWifi, behaviorWithAttachments, positionSpinnerTrans, statuses);
            RecyclerView lv_comments = findViewById(R.id.peertube_comments);
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(PeertubeActivity.this);
            lv_comments.setLayoutManager(mLayoutManager);
            lv_comments.setNestedScrollingEnabled(false);
            lv_comments.setAdapter(statusListAdapter);

        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case progress_bar_type: // we set this to 0
                pDialog = new ProgressDialog(PeertubeActivity.this);
                pDialog.setMessage(getString(R.string.download_wait));
                pDialog.setIndeterminate(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCancelable(true);
                pDialog.show();
                return pDialog;
            default:
                return null;
        }
    }


    /**
     * https://stackoverflow.com/a/15758953
     */
    @SuppressLint("StaticFieldLeak")
    class DownloadFileFromURL extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(progress_bar_type);
        }
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection conection = url.openConnection();
                conection.connect();
                int lenghtOfFile = conection.getContentLength();
                InputStream input = new BufferedInputStream(url.openStream(),
                        8192);
                OutputStream output = new FileOutputStream(Environment
                        .getExternalStorageDirectory().toString()
                        + "/2011.kml");
                byte data[] = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();

            } catch (Exception e) {
            }

            return null;
        }
        protected void onProgressUpdate(String... progress) {
            pDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String file_url) {
            dismissDialog(progress_bar_type);
        }

    }
}
