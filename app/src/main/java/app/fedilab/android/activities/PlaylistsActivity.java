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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import app.fedilab.android.R;
import app.fedilab.android.asynctasks.ManagePlaylistsAsyncTask;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Peertube;
import app.fedilab.android.client.Entities.Playlist;
import app.fedilab.android.drawers.PeertubeAdapter;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.interfaces.OnPlaylistActionInterface;
import es.dmoral.toasty.Toasty;

import static app.fedilab.android.asynctasks.ManagePlaylistsAsyncTask.action.GET_LIST_VIDEOS;


/**
 * Created by Thomas on 26/05/2019.
 * Display playlists for Peertube
 */

public class PlaylistsActivity extends BaseActivity implements OnPlaylistActionInterface {


    private RelativeLayout mainLoader, nextElementLoader, textviewNoAction;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean swiped;
    private List<Peertube> peertubes;
    private String max_id;
    private Playlist playlist;
    private boolean firstLoad;
    private boolean flag_loading;
    private PeertubeAdapter peertubeAdapter;
    LinearLayoutManager mLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        switch (theme){
            case Helper.THEME_LIGHT:
                setTheme(R.style.AppTheme_NoActionBar);
                break;
            case Helper.THEME_DARK:
                setTheme(R.style.AppThemeDark_NoActionBar);
                break;
            case Helper.THEME_BLACK:
                setTheme(R.style.AppThemeBlack_NoActionBar);
                break;
            default:
                setTheme(R.style.AppThemeDark_NoActionBar);
        }
        if( getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null ) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.simple_bar, null);
            actionBar.setCustomView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            ImageView toolbar_close = actionBar.getCustomView().findViewById(R.id.toolbar_close);
            TextView toolbar_title = actionBar.getCustomView().findViewById(R.id.toolbar_title);
            toolbar_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            toolbar_title.setText(R.string.upload_video);
            if (theme == Helper.THEME_LIGHT){
                Toolbar toolbar = actionBar.getCustomView().findViewById(R.id.toolbar);
                Helper.colorizeToolbar(toolbar, R.color.black, PlaylistsActivity.this);
            }
        }
        setContentView(R.layout.activity_playlists);
        Toolbar toolbar = findViewById(R.id.toolbar);
        if( theme == Helper.THEME_BLACK)
            toolbar.setBackgroundColor(ContextCompat.getColor(PlaylistsActivity.this, R.color.black));
        setSupportActionBar(toolbar);
        if( getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        peertubes = new ArrayList<>();

        RecyclerView lv_playlist = findViewById(R.id.lv_playlist);
        mainLoader =  findViewById(R.id.loader);
        nextElementLoader = findViewById(R.id.loading_next_status);
        textviewNoAction =  findViewById(R.id.no_action);
        mainLoader.setVisibility(View.VISIBLE);
        swipeRefreshLayout = findViewById(R.id.swipeContainer);
        max_id = null;
        flag_loading = true;
        firstLoad = true;
        swiped = false;


        mainLoader.setVisibility(View.VISIBLE);
        nextElementLoader.setVisibility(View.GONE);
        boolean isOnWifi = Helper.isOnWIFI(PlaylistsActivity.this);

        peertubeAdapter = new PeertubeAdapter(PlaylistsActivity.this, Helper.getLiveInstance(PlaylistsActivity.this), false, this.peertubes);

        lv_playlist.setAdapter(peertubeAdapter);
        mLayoutManager = new LinearLayoutManager(PlaylistsActivity.this);
        lv_playlist.setLayoutManager(mLayoutManager);

        Bundle b = getIntent().getExtras();
        if(b != null){
            playlist = b.getParcelable("playlist");
        }else{
            Toasty.error(this,getString(R.string.toast_error_search),Toast.LENGTH_LONG).show();
            return;
        }
        if( getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(playlist.getDisplayName());


        lv_playlist.addOnScrollListener(new RecyclerView.OnScrollListener() {
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy)
            {
                int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                if(dy > 0){
                    int visibleItemCount = mLayoutManager.getChildCount();
                    int totalItemCount = mLayoutManager.getItemCount();
                    if(firstVisibleItem + visibleItemCount == totalItemCount ) {
                        if(!flag_loading ) {
                            flag_loading = true;
                            new ManagePlaylistsAsyncTask(PlaylistsActivity.this,GET_LIST_VIDEOS, playlist, null, max_id , PlaylistsActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            nextElementLoader.setVisibility(View.VISIBLE);
                        }
                    } else {
                        nextElementLoader.setVisibility(View.GONE);
                    }
                }
            }
        });


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                max_id = null;
                firstLoad = true;
                flag_loading = true;
                swiped = true;
                MainActivity.countNewStatus = 0;
                new ManagePlaylistsAsyncTask(PlaylistsActivity.this,GET_LIST_VIDEOS, playlist, null, null , PlaylistsActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        switch (theme){
            case Helper.THEME_LIGHT:
                swipeRefreshLayout.setColorSchemeResources(R.color.mastodonC4,
                        R.color.mastodonC2,
                        R.color.mastodonC3);
                swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(PlaylistsActivity.this, R.color.white));
                break;
            case Helper.THEME_DARK:
                swipeRefreshLayout.setColorSchemeResources(R.color.mastodonC4__,
                        R.color.mastodonC4,
                        R.color.mastodonC4);
                swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(PlaylistsActivity.this, R.color.mastodonC1_));
                break;
            case Helper.THEME_BLACK:
                swipeRefreshLayout.setColorSchemeResources(R.color.dark_icon,
                        R.color.mastodonC2,
                        R.color.mastodonC3);
                swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(PlaylistsActivity.this, R.color.black_3));
                break;
        }

        new ManagePlaylistsAsyncTask(PlaylistsActivity.this,GET_LIST_VIDEOS, playlist, null, null , PlaylistsActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onActionDone(ManagePlaylistsAsyncTask.action actionType, APIResponse apiResponse, int statusCode) {
        final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        mainLoader.setVisibility(View.GONE);
        nextElementLoader.setVisibility(View.GONE);
        //Discards 404 - error which can often happen due to toots which have been deleted
        if (apiResponse.getError() != null) {
            if ( !apiResponse.getError().getError().startsWith("404 -"))
                Toasty.error(PlaylistsActivity.this, apiResponse.getError().getError(), Toast.LENGTH_LONG).show();
            swipeRefreshLayout.setRefreshing(false);
            swiped = false;
            flag_loading = false;
            return;
        }
        if( actionType == GET_LIST_VIDEOS) {

            int previousPosition = this.peertubes.size();
            List<Peertube> videos = apiResponse.getPeertubes();
            max_id = apiResponse.getMax_id();
            flag_loading = (max_id == null);
            if (!swiped && firstLoad && (videos == null || videos.size() == 0))
                textviewNoAction.setVisibility(View.VISIBLE);
            else
                textviewNoAction.setVisibility(View.GONE);

            if (swiped) {
                if (previousPosition > 0) {
                    for (int i = 0; i < previousPosition; i++) {
                        this.peertubes.remove(0);
                    }
                    peertubeAdapter.notifyItemRangeRemoved(0, previousPosition);
                }
                swiped = false;
            }
            if (videos != null && videos.size() > 0) {
                this.peertubes.addAll(videos);
                peertubeAdapter.notifyItemRangeInserted(previousPosition, videos.size());
            }
            swipeRefreshLayout.setRefreshing(false);
            firstLoad = false;
        }
    }
}
