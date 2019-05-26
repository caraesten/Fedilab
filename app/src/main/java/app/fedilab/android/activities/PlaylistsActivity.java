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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import app.fedilab.android.R;
import app.fedilab.android.asynctasks.ManageListsAsyncTask;
import app.fedilab.android.asynctasks.ManagePlaylistsAsyncTask;
import app.fedilab.android.asynctasks.RetrieveFeedsAsyncTask;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Peertube;
import app.fedilab.android.client.Entities.Playlist;
import app.fedilab.android.client.Entities.Status;
import app.fedilab.android.drawers.PeertubeAdapter;
import app.fedilab.android.drawers.StatusListAdapter;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.interfaces.OnListActionInterface;
import app.fedilab.android.interfaces.OnPlaylistActionInterface;
import es.dmoral.toasty.Toasty;


/**
 * Created by Thomas on 26/05/2019.
 * Display playlists for Peertube
 */

public class PlaylistsActivity extends BaseActivity implements OnPlaylistActionInterface {


    private String title, listId;
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
        }
        if( getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(title);


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
                            new ManageListsAsyncTask(PlaylistsActivity.this,listId, max_id ,null, PlaylistsActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
                new ManageListsAsyncTask(PlaylistsActivity.this,listId, null ,null, PlaylistsActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

        new ManageListsAsyncTask(PlaylistsActivity.this,listId, null ,null, PlaylistsActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_add_user:
                Intent intent = new Intent(PlaylistsActivity.this, ManageAccountsInListActivity.class);
                intent.putExtra("title", title);
                intent.putExtra("id", listId);
                startActivity(intent);
                return true;
            case R.id.action_edit_list:
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
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(PlaylistsActivity.this, style);
                LayoutInflater inflater = getLayoutInflater();
                @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.add_list, null);
                dialogBuilder.setView(dialogView);
                final EditText editText = dialogView.findViewById(R.id.add_list);
                editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(255)});
                editText.setText(title);
                dialogBuilder.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if( editText.getText() != null && editText.getText().toString().trim().length() > 0 )
                            new ManageListsAsyncTask(PlaylistsActivity.this, ManageListsAsyncTask.action.UPDATE_LIST, null, listId, editText.getText().toString(), editText.getText().toString().trim(), PlaylistsActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        dialog.dismiss();
                    }
                });
                dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });


                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.setTitle(getString(R.string.action_lists_create));
                alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        //Hide keyboard
                        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        assert imm != null;
                        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                    }
                });
                if( alertDialog.getWindow() != null )
                    alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                alertDialog.show();
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
        if( actionType == ManagePlaylistsAsyncTask.action.GET_LIST_VIDEOS) {

            int previousPosition = this.peertubes.size();
            List<Status> statuses = apiResponse.getStatuses();
            max_id = apiResponse.getMax_id();
            flag_loading = (max_id == null);
            if (!swiped && firstLoad && (statuses == null || statuses.size() == 0))
                textviewNoAction.setVisibility(View.VISIBLE);
            else
                textviewNoAction.setVisibility(View.GONE);

            if (swiped) {
                if (previousPosition > 0) {
                    for (int i = 0; i < previousPosition; i++) {
                        this.statuses.remove(0);
                    }
                    statusListAdapter.notifyItemRangeRemoved(0, previousPosition);
                }
                swiped = false;
            }
            if (statuses != null && statuses.size() > 0) {
                this.statuses.addAll(statuses);
                statusListAdapter.notifyItemRangeInserted(previousPosition, statuses.size());
            }
            swipeRefreshLayout.setRefreshing(false);
            firstLoad = false;
        }else if(actionType == ManageListsAsyncTask.action.UPDATE_LIST) {

        }
    }
}
