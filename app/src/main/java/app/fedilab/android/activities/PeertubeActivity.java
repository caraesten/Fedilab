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
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.lang.ref.WeakReference;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

import app.fedilab.android.asynctasks.ManagePlaylistsAsyncTask;
import app.fedilab.android.client.API;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Error;
import app.fedilab.android.client.Entities.Peertube;
import app.fedilab.android.client.Entities.Playlist;
import app.fedilab.android.client.Entities.Status;
import app.fedilab.android.client.TLSSocketFactory;
import app.fedilab.android.drawers.StatusListAdapter;
import app.fedilab.android.helper.CrossActions;
import app.fedilab.android.helper.FullScreenMediaController;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.interfaces.OnPlaylistActionInterface;
import app.fedilab.android.sqlite.AccountDAO;
import app.fedilab.android.sqlite.PeertubeFavoritesDAO;
import app.fedilab.android.sqlite.Sqlite;
import app.fedilab.android.webview.MastalabWebChromeClient;
import app.fedilab.android.webview.MastalabWebViewClient;
import es.dmoral.toasty.Toasty;
import app.fedilab.android.R;
import app.fedilab.android.asynctasks.PostActionAsyncTask;
import app.fedilab.android.asynctasks.RetrieveFeedsAsyncTask;
import app.fedilab.android.asynctasks.RetrievePeertubeSingleAsyncTask;
import app.fedilab.android.asynctasks.RetrievePeertubeSingleCommentsAsyncTask;
import app.fedilab.android.asynctasks.UpdateAccountInfoAsyncTask;
import app.fedilab.android.interfaces.OnPostActionInterface;
import app.fedilab.android.interfaces.OnRetrievePeertubeInterface;

import static app.fedilab.android.asynctasks.ManagePlaylistsAsyncTask.action.GET_PLAYLIST;
import static app.fedilab.android.asynctasks.ManagePlaylistsAsyncTask.action.GET_PLAYLIST_FOR_VIDEO;
import static app.fedilab.android.helper.Helper.changeDrawableColor;


/**
 * Created by Thomas on 14/10/20178.
 * Peertube activity
 */

public class PeertubeActivity extends BaseActivity implements OnRetrievePeertubeInterface, OnPostActionInterface, OnPlaylistActionInterface {

    private String peertubeInstance, videoId;
    private FullScreenMediaController.fullscreen fullscreen;
    private RelativeLayout loader;
    private TextView peertube_view_count, peertube_playlist, peertube_bookmark, peertube_like_count, peertube_dislike_count, peertube_share, peertube_download, peertube_description, peertube_title;
    private ScrollView peertube_information_container;
    private int stopPosition;
    private Peertube peertube;
    private TextView toolbar_title;
    public static String video_id;
    private SimpleExoPlayerView playerView;
    private SimpleExoPlayer player;
    private boolean fullScreenMode;
    private Dialog fullScreenDialog;
    private AppCompatImageView fullScreenIcon;
    private TextView resolution;
    private DefaultTrackSelector trackSelector;
    private int mode;
    private LinearLayout write_comment_container;
    private ImageView my_pp, send;
    private TextView add_comment_read;
    private EditText add_comment_write;
    private  String instance;
    private List<String> playlistForVideo;
    private List<Playlist> playlists;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
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
        fullScreenMode = false;
        playlistForVideo = new ArrayList<>();
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
        WebView webview_video = findViewById(R.id.webview_video);
        playerView = findViewById(R.id.media_video);
        write_comment_container = findViewById(R.id.write_comment_container);
        my_pp = findViewById(R.id.my_pp);
        add_comment_read = findViewById(R.id.add_comment_read);
        add_comment_write = findViewById(R.id.add_comment_write);
        peertube_playlist = findViewById(R.id.peertube_playlist);
        send  = findViewById(R.id.send);
        add_comment_read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add_comment_read.setVisibility(View.GONE);
                add_comment_write.setVisibility(View.VISIBLE);
                send.setVisibility(View.VISIBLE);
                add_comment_write.requestFocus();
                add_comment_write.setSelection(add_comment_write.getText().length());

            }
        });
        Helper.changeDrawableColor(getApplicationContext(), send, R.color.mastodonC4);
        if( MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE){
            write_comment_container.setVisibility(View.GONE);
        }
        if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE){
            peertube_playlist.setVisibility(View.VISIBLE);
            peertube_bookmark.setVisibility(View.GONE);
        }

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = add_comment_write.getText().toString();
                if( comment.trim().length() > 0 ) {
                    new PostActionAsyncTask(getApplicationContext(), API.StatusAction.PEERTUBECOMMENT, peertube.getId(), null, comment, PeertubeActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    add_comment_write.setText("");
                    add_comment_read.setVisibility(View.VISIBLE);
                    add_comment_write.setVisibility(View.GONE);
                    send.setVisibility(View.GONE);
                    add_comment_read.requestFocus();
                }
            }
        });

        SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        instance = sharedpreferences.getString(Helper.PREF_INSTANCE, Helper.getLiveInstance(getApplicationContext()));
        Account account = new AccountDAO(getApplicationContext(), db).getUniqAccount(userId, instance);
        Helper.loadGiF(getApplicationContext(),  account.getAvatar_static(), account.getAvatar(), my_pp);
        Bundle b = getIntent().getExtras();
        if(b != null) {
            peertubeInstance = b.getString("peertube_instance", null);
            videoId = b.getString("video_id", null);
        }
        if( getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null ) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.simple_bar, null);
            actionBar.setCustomView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            ImageView toolbar_close = actionBar.getCustomView().findViewById(R.id.toolbar_close);
            toolbar_title = actionBar.getCustomView().findViewById(R.id.toolbar_title);
            toolbar_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            if (theme == Helper.THEME_LIGHT){
                Toolbar toolbar = actionBar.getCustomView().findViewById(R.id.toolbar);
                Helper.colorizeToolbar(toolbar, R.color.black, PeertubeActivity.this);
            }
        }


        mode = sharedpreferences.getInt(Helper.SET_VIDEO_MODE, Helper.VIDEO_MODE_DIRECT);
        if( mode != Helper.VIDEO_MODE_WEBVIEW && mode != Helper.VIDEO_MODE_DIRECT)
            mode = Helper.VIDEO_MODE_DIRECT;
        if( mode == Helper.VIDEO_MODE_WEBVIEW){
            webview_video.setVisibility(View.VISIBLE);
            playerView.setVisibility(View.GONE);

            webview_video = Helper.initializeWebview(PeertubeActivity.this, R.id.webview_video);
            FrameLayout webview_container = findViewById(R.id.main_media_frame);
            final ViewGroup videoLayout = findViewById(R.id.videoLayout);

            MastalabWebChromeClient mastalabWebChromeClient = new MastalabWebChromeClient(PeertubeActivity.this, webview_video, webview_container, videoLayout);
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
                        peertube_information_container.setVisibility(View.GONE);
                    } else {
                        WindowManager.LayoutParams attrs = getWindow().getAttributes();
                        attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                        attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                        getWindow().setAttributes(attrs);
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                        videoLayout.setVisibility(View.GONE);
                        peertube_information_container.setVisibility(View.VISIBLE);
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
            webview_video.setWebViewClient(new MastalabWebViewClient(PeertubeActivity.this));
            webview_video.loadUrl("https://" + peertubeInstance + "/videos/embed/" + videoId);
        }else {
            webview_video.setVisibility(View.GONE);
            playerView.setVisibility(View.VISIBLE);
            loader.setVisibility(View.VISIBLE);
        }


        if( mode != Helper.VIDEO_MODE_WEBVIEW){
            playerView.setControllerShowTimeoutMs(1000);
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            initFullscreenDialog();
            initFullscreenButton();
        }

        if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE){
            new ManagePlaylistsAsyncTask(PeertubeActivity.this,GET_PLAYLIST, null, null, null , PeertubeActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        new RetrievePeertubeSingleAsyncTask(PeertubeActivity.this, peertubeInstance, videoId, PeertubeActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void change(){
        if(fullscreen == FullScreenMediaController.fullscreen.ON){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();

        if ((ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) &&
                v instanceof EditText &&
                v.getId() == R.id.add_comment_write) {
            int scrcoords[] = new int[2];
            v.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + v.getLeft() - scrcoords[0];
            float y = ev.getRawY() + v.getTop() - scrcoords[1];

            if (x < v.getLeft() || x > v.getRight() || y < v.getTop() || y > v.getBottom()) {
                add_comment_read.setVisibility(View.VISIBLE);
                add_comment_write.setVisibility(View.GONE);
                send.setVisibility(View.GONE);
                hideKeyboard(PeertubeActivity.this);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public static void hideKeyboard(Activity activity) {
        if (activity != null && activity.getWindow() != null && activity.getWindow().getDecorView() != null) {
            InputMethodManager imm = (InputMethodManager)activity.getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_webview, menu);
        menu.findItem(R.id.action_go).setVisible(false);
        menu.findItem(R.id.action_block).setVisible(false);
        menu.findItem(R.id.action_comment).setVisible(true);
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if( theme == Helper.THEME_LIGHT)
            Helper.colorizeIconMenu(menu, R.color.black);
        if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE){
            MenuItem item = menu.findItem(R.id.action_comment);
            if( item != null)
                item.setVisible(false);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_comment:
                if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON || MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA) {
                    Toasty.info(PeertubeActivity.this, getString(R.string.retrieve_remote_status), Toast.LENGTH_LONG).show();
                    new AsyncTask<Void, Void, Void>() {

                        private List<app.fedilab.android.client.Entities.Status> remoteStatuses;
                        private WeakReference<Context> contextReference = new WeakReference<>(PeertubeActivity.this);

                        @Override
                        protected Void doInBackground(Void... voids) {

                            if (peertube != null) {
                                APIResponse search = new API(contextReference.get()).search("https://" + peertube.getAccount().getHost() + "/videos/watch/" + peertube.getUuid());
                                if (search != null && search.getResults() != null) {
                                    remoteStatuses = search.getResults().getStatuses();
                                }
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void result) {
                            Intent intent = new Intent(contextReference.get(), TootActivity.class);
                            Bundle b = new Bundle();
                            if (remoteStatuses == null || remoteStatuses.size() == 0) {
                                Toasty.error(contextReference.get(), getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (remoteStatuses.get(0).getReblog() != null) {
                                b.putParcelable("tootReply", remoteStatuses.get(0).getReblog());
                            } else {
                                b.putParcelable("tootReply", remoteStatuses.get(0));
                            }
                            intent.putExtras(b); //Put your id to your next Intent
                            contextReference.get().startActivity(intent);
                        }
                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }else if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE){
                    if(! peertube.isCommentsEnabled()) {
                        Toasty.info(PeertubeActivity.this, getString(R.string.comment_no_allowed_peertube),Toast.LENGTH_LONG).show();
                        return true;
                    }
                    int style;
                    SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
                    int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
                    if (theme == Helper.THEME_DARK) {
                        style = R.style.DialogDark;
                    } else if (theme == Helper.THEME_BLACK){
                        style = R.style.DialogBlack;
                    }else {
                        style = R.style.Dialog;
                    }
                    AlertDialog.Builder builderInner;
                    builderInner = new AlertDialog.Builder(PeertubeActivity.this, style);
                    builderInner.setTitle(R.string.comment);
                    EditText input = new EditText(PeertubeActivity.this);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    input.setLayoutParams(lp);
                    builderInner.setView(input);
                    builderInner.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,int which) {
                            dialog.dismiss();
                        }
                    });
                    builderInner.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,int which) {
                            String comment = input.getText().toString();
                            if( comment.trim().length() > 0 ) {
                                new PostActionAsyncTask(getApplicationContext(), API.StatusAction.PEERTUBECOMMENT, peertube.getId(), null, comment, PeertubeActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                dialog.dismiss();
                            }
                        }
                    });
                    builderInner.show();
                }
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
            Toasty.error(PeertubeActivity.this, getString(R.string.toast_error),Toast.LENGTH_LONG).show();
            loader.setVisibility(View.GONE);
            return;
        }
        if( apiResponse.getPeertubes() == null || apiResponse.getPeertubes().get(0) == null || apiResponse.getPeertubes().get(0).getFileUrl(null) == null){
            Toasty.error(PeertubeActivity.this, getString(R.string.toast_error),Toast.LENGTH_LONG).show();
            loader.setVisibility(View.GONE);
            return;
        }

        peertube = apiResponse.getPeertubes().get(0);

        if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE){
            new ManagePlaylistsAsyncTask(PeertubeActivity.this,GET_PLAYLIST_FOR_VIDEO, null, peertube.getId(), null , PeertubeActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }


        peertube_playlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( playlists != null && peertube.getId() != null) {
                    PopupMenu popup = new PopupMenu(PeertubeActivity.this, peertube_playlist);

                    for(Playlist playlist: playlists){
                        String title = null;
                        for (String id : playlistForVideo) {
                            if (playlist.getId().equals(id)) {
                                title = "✔ " + playlist.getDisplayName();
                                break;
                            }
                        }
                        if( title == null){
                            title = playlist.getDisplayName();
                        }
                        MenuItem item = popup.getMenu().add(0, 0, Menu.NONE, title);
                        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
                                item.setActionView(new View(getApplicationContext()));
                                item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                                    @Override
                                    public boolean onMenuItemActionExpand(MenuItem item) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onMenuItemActionCollapse(MenuItem item) {
                                        return false;
                                    }
                                });
                                if(playlistForVideo.contains(playlist.getId())){
                                    item.setTitle(playlist.getDisplayName());
                                    new ManagePlaylistsAsyncTask(PeertubeActivity.this,ManagePlaylistsAsyncTask.action.DELETE_VIDEOS, playlist, peertube.getId(), null , PeertubeActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                    playlistForVideo.remove(playlist.getId());
                                }else{
                                    item.setTitle( "✔ " + playlist.getDisplayName());
                                    new ManagePlaylistsAsyncTask(PeertubeActivity.this,ManagePlaylistsAsyncTask.action.ADD_VIDEOS, playlist, peertube.getId(), null , PeertubeActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                    playlistForVideo.add(playlist.getId());
                                }
                                return false;
                            }
                        });
                        popup.show();
                    }
                }
            }
        });



        if( peertube.isCommentsEnabled()) {
            new RetrievePeertubeSingleCommentsAsyncTask(PeertubeActivity.this, peertubeInstance, videoId, PeertubeActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE)
                write_comment_container.setVisibility(View.VISIBLE);
        }else {
            RelativeLayout no_action = findViewById(R.id.no_action);
            TextView no_action_text = findViewById(R.id.no_action_text);
            no_action_text.setText(getString(R.string.comment_no_allowed_peertube));
            no_action.setVisibility(View.VISIBLE);
            write_comment_container.setVisibility(View.GONE);
        }
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);

        toolbar_title.setText(peertube.getName());
        peertube_description.setText(peertube.getDescription());
        peertube_title.setText(peertube.getName());
        peertube_dislike_count.setText(String.valueOf(peertube.getDislike()));
        peertube_like_count.setText(String.valueOf(peertube.getLike()));
        peertube_view_count.setText(String.valueOf(peertube.getView()));
        video_id = peertube.getId();

        changeColor();
        initResolution();

        if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE){
            peertube_like_count.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String newState = peertube.getMyRating().equals("like")?"none":"like";
                    new PostActionAsyncTask(getApplicationContext(), API.StatusAction.RATEVIDEO, peertube.getId(), null, newState, PeertubeActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    peertube.setMyRating(newState);
                    changeColor();
                }
            });
            peertube_dislike_count.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String newState = peertube.getMyRating().equals("dislike")?"none":"dislike";
                    new PostActionAsyncTask(getApplicationContext(), API.StatusAction.RATEVIDEO, peertube.getId(), null, newState, PeertubeActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    peertube.setMyRating(newState);
                    changeColor();
                }
            });
        }else{
            peertube_like_count.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String newState = peertube.getMyRating().equals("like")?"none":"like";
                    Status status = new Status();
                    status.setUri("https://" + peertube.getAccount().getHost() + "/videos/watch/" + peertube.getUuid());
                    CrossActions.doCrossAction(getApplicationContext(),  RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE, status, null, API.StatusAction.FAVOURITE, null, PeertubeActivity.this, true);
                    peertube.setMyRating(newState);
                    changeColor();
                }
            });
            peertube_dislike_count.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String newState = peertube.getMyRating().equals("dislike")?"none":"dislike";
                    Status status = new Status();
                    status.setUri("https://" + peertube.getAccount().getHost() + "/videos/watch/" + peertube.getUuid());
                    CrossActions.doCrossAction(getApplicationContext(),  RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE, status, null, API.StatusAction.UNFAVOURITE, null, PeertubeActivity.this, true);
                    peertube.setMyRating(newState);
                    changeColor();
                }
            });
        }

        try {
            HttpsURLConnection.setDefaultSSLSocketFactory(new TLSSocketFactory(instance));
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        if( mode == Helper.VIDEO_MODE_DIRECT){

            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getApplicationContext(),
                    Util.getUserAgent(getApplicationContext(), "Mastalab"), null);

            ExtractorMediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(apiResponse.getPeertubes().get(0).getFileUrl(null)));

            player = ExoPlayerFactory.newSimpleInstance(PeertubeActivity.this);
            playerView.setPlayer(player);
            loader.setVisibility(View.GONE);

            player.prepare(videoSource);
            player.setPlayWhenReady(true);
        }



        peertube_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= 23 ){
                    if (ContextCompat.checkSelfPermission(PeertubeActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(PeertubeActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(PeertubeActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Helper.EXTERNAL_STORAGE_REQUEST_CODE);
                    } else {
                        Helper.manageDownloads(PeertubeActivity.this, peertube.getFileDownloadUrl(null));
                    }
                }else{
                    Helper.manageDownloads(PeertubeActivity.this, peertube.getFileDownloadUrl(null));
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
                    Toasty.success(PeertubeActivity.this,getString(R.string.bookmark_add_peertube), Toast.LENGTH_SHORT).show();
                }else{
                    new PeertubeFavoritesDAO(PeertubeActivity.this, db).remove(peertube);
                    Toasty.success(PeertubeActivity.this,getString(R.string.bookmark_remove_peertube), Toast.LENGTH_SHORT).show();
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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if( mode != Helper.VIDEO_MODE_WEBVIEW) {
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                openFullscreenDialog();
                setFullscreen(FullScreenMediaController.fullscreen.ON);
            } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                closeFullscreenDialog();
                setFullscreen(FullScreenMediaController.fullscreen.OFF);
            }
            change();
        }else {
           final ViewGroup videoLayout = findViewById(R.id.videoLayout);
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                setFullscreen(FullScreenMediaController.fullscreen.ON);
            } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                setFullscreen(FullScreenMediaController.fullscreen.OFF);
            }
            change();


        }

    }

    @Override
    public void onRetrievePeertubeComments(APIResponse apiResponse) {
        if( apiResponse == null || (apiResponse.getError() != null && apiResponse.getError().getStatusCode() != 404) ){
            if( apiResponse == null)
                Toasty.error(PeertubeActivity.this, getString(R.string.toast_error),Toast.LENGTH_LONG).show();
            else
                Toasty.error(PeertubeActivity.this, apiResponse.getError().getError(),Toast.LENGTH_LONG).show();
            return;
        }
        List<Status> statuses = apiResponse.getStatuses();
        RecyclerView lv_comments = findViewById(R.id.peertube_comments);
        if( statuses == null || statuses.size() == 0){
            if( MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE) {
                RelativeLayout no_action = findViewById(R.id.no_action);
                no_action.setVisibility(View.VISIBLE);
                lv_comments.setVisibility(View.GONE);
            }
        }else {
            lv_comments.setVisibility(View.VISIBLE);
            SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
            boolean isOnWifi = Helper.isOnWIFI(PeertubeActivity.this);
            String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
            StatusListAdapter statusListAdapter = new StatusListAdapter(PeertubeActivity.this, RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE, userId, isOnWifi, statuses);
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(PeertubeActivity.this);
            lv_comments.setLayoutManager(mLayoutManager);
            lv_comments.setNestedScrollingEnabled(false);
            lv_comments.setAdapter(statusListAdapter);

        }
    }

    @Override
    public void onRetrievePeertubeChannels(APIResponse apiResponse) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if( player != null)
            player.release();
    }

    @Override
    protected void onPause() {
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

    public void displayResolution(){
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        int style;
        if (theme == Helper.THEME_DARK) {
            style = R.style.DialogDark;
        } else if (theme == Helper.THEME_BLACK){
            style = R.style.DialogBlack;
        }else {
            style = R.style.Dialog;
        }
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(PeertubeActivity.this, style);
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

                if( playerView != null) {
                    loader.setVisibility(View.VISIBLE);
                    long position = player.getCurrentPosition();
                    PlaybackControlView controlView = playerView.findViewById(R.id.exo_controller);
                    resolution = controlView.findViewById(R.id.resolution);
                    resolution.setText(String.format("%sp",res));
                    if( mode == Helper.VIDEO_MODE_DIRECT){
                        if( player != null)
                            player.release();
                        player = ExoPlayerFactory.newSimpleInstance(PeertubeActivity.this);
                        playerView.setPlayer(player);
                        loader.setVisibility(View.GONE);
                        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getApplicationContext(),
                                Util.getUserAgent(getApplicationContext(), "Mastalab"), null);

                        ExtractorMediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                                .createMediaSource(Uri.parse(peertube.getFileUrl(res)));
                        player.prepare(videoSource);
                        player.seekTo(0, position);
                        player.setPlayWhenReady(true);
                    }
                }

            }
        });
        builderSingle.show();
    }

    @Override
    public void onPostAction(int statusCode, API.StatusAction statusAction, String userId, Error error) {

        if( peertube.isCommentsEnabled() && statusAction == API.StatusAction.PEERTUBECOMMENT)
            new RetrievePeertubeSingleCommentsAsyncTask(PeertubeActivity.this, peertubeInstance, videoId, PeertubeActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }


    private void initFullscreenDialog() {

        fullScreenDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
            public void onBackPressed() {
                if (fullScreenMode)
                    closeFullscreenDialog();
                super.onBackPressed();
            }
        };
    }

    private void openFullscreenDialog() {

        ((ViewGroup) playerView.getParent()).removeView(playerView);
        fullScreenDialog.addContentView(playerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        fullScreenIcon.setImageDrawable(ContextCompat.getDrawable(PeertubeActivity.this, R.drawable.ic_fullscreen_exit));
        fullScreenMode = true;
        fullScreenDialog.show();
    }


    private void closeFullscreenDialog() {

        ((ViewGroup) playerView.getParent()).removeView(playerView);
        ((FrameLayout) findViewById(R.id.main_media_frame)).addView(playerView);
        fullScreenMode = false;
        fullScreenDialog.dismiss();
        fullScreenIcon.setImageDrawable(ContextCompat.getDrawable(PeertubeActivity.this, R.drawable.ic_fullscreen));
    }

    private void initFullscreenButton() {

        PlaybackControlView controlView = playerView.findViewById(R.id.exo_controller);
        fullScreenIcon = controlView.findViewById(R.id.exo_fullscreen_icon);
        View fullScreenButton = controlView.findViewById(R.id.exo_fullscreen_button);
        fullScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!fullScreenMode)
                    openFullscreenDialog();
                else
                    closeFullscreenDialog();
            }
        });
    }

    private void initResolution() {
        PlaybackControlView controlView = playerView.findViewById(R.id.exo_controller);
        resolution = controlView.findViewById(R.id.resolution);
        resolution.setText(String.format("%sp",peertube.getResolution().get(0)));
        resolution.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayResolution();
            }
        });
    }



    private void changeColor(){
        if( peertube.getMyRating() != null && peertube.getMyRating().equals("like")){
            changeDrawableColor(getApplicationContext(), R.drawable.ic_thumb_up_peertube,R.color.positive_thumbs);
            changeDrawableColor(getApplicationContext(), R.drawable.ic_thumb_down_peertube,R.color.neutral_thumbs);
        }else if( peertube.getMyRating() != null && peertube.getMyRating().equals("dislike")){
            changeDrawableColor(getApplicationContext(), R.drawable.ic_thumb_up_peertube,R.color.neutral_thumbs);
            changeDrawableColor(getApplicationContext(), R.drawable.ic_thumb_down_peertube,R.color.negative_thumbs);
        }else {
            changeDrawableColor(getApplicationContext(), R.drawable.ic_thumb_up_peertube,R.color.neutral_thumbs);
            changeDrawableColor(getApplicationContext(), R.drawable.ic_thumb_down_peertube,R.color.neutral_thumbs);
        }
        Drawable thumbUp = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_thumb_up_peertube);
        Drawable thumbDown = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_thumb_down_peertube);
        peertube_like_count.setCompoundDrawablesWithIntrinsicBounds( null, thumbUp, null, null);
        peertube_dislike_count.setCompoundDrawablesWithIntrinsicBounds( null, thumbDown, null, null);
    }

    @Override
    public void onActionDone(ManagePlaylistsAsyncTask.action actionType, APIResponse apiResponse, int statusCode) {

        if( actionType == GET_PLAYLIST_FOR_VIDEO && apiResponse != null) {
            playlistForVideo = apiResponse.getPlaylistForVideos();
        }else if( actionType == GET_PLAYLIST && apiResponse != null){
            playlists = apiResponse.getPlaylists();
        }
    }
}
