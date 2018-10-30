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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import java.lang.ref.WeakReference;
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
import fr.gouv.etalab.mastodon.sqlite.PeertubeFavoritesDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;

import static fr.gouv.etalab.mastodon.helper.Helper.EXTERNAL_STORAGE_REQUEST_CODE;
import static fr.gouv.etalab.mastodon.helper.Helper.manageDownloads;


/**
 * Created by Thomas on 14/10/20178.
 * Peertube activity
 */

public class PeertubeActivity extends BaseActivity implements OnRetrievePeertubeInterface {

    private String peertubeInstance, videoId;
    private FullScreenMediaController.fullscreen fullscreen;
    private VideoView videoView;
    private RelativeLayout loader;
    private TextView peertube_view_count, peertube_bookmark, peertube_like_count, peertube_dislike_count, peertube_share, peertube_download, peertube_description, peertube_title;
    private ScrollView peertube_information_container;
    private MediaPlayer mediaPlayer;
    private FullScreenMediaController fullScreenMediaController;
    private int stopPosition;
    private Peertube peertube;

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
        peertube_bookmark = findViewById(R.id.peertube_bookmark);
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
        }
        if( getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        videoView = findViewById(R.id.media_video);
        new RetrievePeertubeSingleAsyncTask(PeertubeActivity.this, peertubeInstance, videoId, PeertubeActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

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

                        if(peertube != null) {
                            Results search = new API(contextReference.get()).search("https://" + peertube.getAccount().getHost() +  "/videos/watch/" + peertube.getUuid());
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
        if( apiResponse.getPeertubes() == null || apiResponse.getPeertubes().get(0) == null || apiResponse.getPeertubes().get(0).getFileUrl(null) == null){
            Toast.makeText(PeertubeActivity.this, R.string.toast_error,Toast.LENGTH_LONG).show();
            loader.setVisibility(View.GONE);
            return;
        }

        peertube = apiResponse.getPeertubes().get(0);
        if( peertube.isCommentsEnabled())
            new RetrievePeertubeSingleCommentsAsyncTask(PeertubeActivity.this, peertubeInstance, videoId, PeertubeActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else {
            RelativeLayout no_action = findViewById(R.id.no_action);
            TextView no_action_text = findViewById(R.id.no_action_text);
            no_action_text.setText(getString(R.string.comment_no_allowed_peertube));
            no_action.setVisibility(View.VISIBLE);
        }
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);

        setTitle(peertube.getName());
        peertube_description.setText(peertube.getDescription());
        peertube_title.setText(peertube.getName());
        peertube_dislike_count.setText(String.valueOf(peertube.getDislike()));
        peertube_like_count.setText(String.valueOf(peertube.getLike()));
        peertube_view_count.setText(String.valueOf(peertube.getView()));

        Uri uri = Uri.parse(apiResponse.getPeertubes().get(0).getFileUrl(null));
        try {
            HttpsURLConnection.setDefaultSSLSocketFactory(new TLSSocketFactory());
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            HttpsURLConnection.setDefaultSSLSocketFactory(new TLSSocketFactory());
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        videoView.setVideoURI(uri);
        videoView.getCurrentPosition();
        fullScreenMediaController = new FullScreenMediaController(PeertubeActivity.this, peertube);
        fullScreenMediaController.setAnchorView(videoView);
        videoView.setMediaController(fullScreenMediaController);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                loader.setVisibility(View.GONE);
                mediaPlayer = mp;
                mp.start();
            }
        });

        videoView.start();

        peertube_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= 23 ){
                    if (ContextCompat.checkSelfPermission(PeertubeActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(PeertubeActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(PeertubeActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_REQUEST_CODE);
                    } else {
                        manageDownloads(PeertubeActivity.this, peertube.getFileDownloadUrl(null));
                    }
                }else{
                    manageDownloads(PeertubeActivity.this, peertube.getFileDownloadUrl(null));
                }
            }
        });
        SQLiteDatabase db = Sqlite.getInstance(PeertubeActivity.this, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        List<Peertube> peertubes = new PeertubeFavoritesDAO(PeertubeActivity.this, db).getSinglePeertube(peertube);

        Drawable img;

        if( peertubes == null || peertubes.size() == 0)
            img = ContextCompat.getDrawable(PeertubeActivity.this, R.drawable.ic_bookmark_peertube_border);
        else
            img = ContextCompat.getDrawable(PeertubeActivity.this, R.drawable.ic_bookmark_peertube);
        peertube_bookmark.setCompoundDrawablesWithIntrinsicBounds(null, img, null, null);

        peertube_bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Peertube> peertubes = new PeertubeFavoritesDAO(PeertubeActivity.this, db).getSinglePeertube(peertube);
                if( peertubes == null || peertubes.size() == 0){
                    new PeertubeFavoritesDAO(PeertubeActivity.this, db).insert(peertube);
                    Toast.makeText(PeertubeActivity.this,R.string.bookmark_add_peertube, Toast.LENGTH_SHORT).show();
                }else{
                    new PeertubeFavoritesDAO(PeertubeActivity.this, db).remove(peertube);
                    Toast.makeText(PeertubeActivity.this,R.string.bookmark_remove_peertube, Toast.LENGTH_SHORT).show();
                }
                if( peertubes != null && peertubes.size() > 0) //Was initially in cache
                    peertube_bookmark.setCompoundDrawablesWithIntrinsicBounds( null, ContextCompat.getDrawable(PeertubeActivity.this, R.drawable.ic_bookmark_peertube_border), null ,null);
                else
                    peertube_bookmark.setCompoundDrawablesWithIntrinsicBounds( null, ContextCompat.getDrawable(PeertubeActivity.this, R.drawable.ic_bookmark_peertube), null ,null);
            }
        });

        peertube_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.shared_via));
                String url;

                url = "https://" +peertube.getInstance() + "/videos/watch/"+ peertube.getUuid();
                boolean share_details = sharedpreferences.getBoolean(Helper.SET_SHARE_DETAILS, true);
                String extra_text;
                if( share_details) {
                    extra_text  = "@" +peertube.getAccount().getAcct();
                    extra_text += "\r\n\r\n" + peertube.getName();
                    extra_text += "\n\n" + Helper.shortnameToUnicode(":link:", true) + " " + url + "\r\n-\n";
                    final String contentToot;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        contentToot = Html.fromHtml(peertube.getDescription(), Html.FROM_HTML_MODE_LEGACY).toString();
                    else
                        //noinspection deprecation
                        contentToot = Html.fromHtml(peertube.getDescription()).toString();
                    extra_text += contentToot;
                }else {
                    extra_text = url;
                }
                sendIntent.putExtra(Intent.EXTRA_TEXT, extra_text);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getString(R.string.share_with)));
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
    protected void onPause() {
        super.onPause();
        if( videoView != null) {
            stopPosition = videoView.getCurrentPosition(); //stopPosition is an int
            videoView.pause();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if( videoView != null) {
            videoView.seekTo(stopPosition);
            videoView.resume();
            videoView.start();
        }
    }

    public void displayResolution(){

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(PeertubeActivity.this);
        builderSingle.setTitle(R.string.pickup_resolution);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(PeertubeActivity.this, android.R.layout.select_dialog_item);
        for(String resolution: peertube.getResolution())
            arrayAdapter.add(resolution+"p");
        builderSingle.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String res = arrayAdapter.getItem(which).substring(0, arrayAdapter.getItem(which).length() - 1);
                if( mediaPlayer != null) {
                    int position = videoView.getCurrentPosition();
                    mediaPlayer.stop();
                    videoView.setVideoURI(Uri.parse(peertube.getFileUrl(res)));
                    fullScreenMediaController.setResolutionVal(res);
                    videoView.seekTo(position);
                    videoView.start();
                }

            }
        });
        builderSingle.show();
    }
}
