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
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;
import java.util.List;
import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveFeedsAsyncTask;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.drawers.StatusListAdapter;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveFeedsInterface;
import fr.gouv.etalab.mastodon.services.BackupStatusInDataBaseService;
import fr.gouv.etalab.mastodon.services.BackupStatusService;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;


/**
 * Created by Thomas on 17/02/2018.
 * Show owner's toots
 */

public class OwnerStatusActivity extends BaseActivity implements OnRetrieveFeedsInterface {


    private TextView toolbarTitle;
    private RecyclerView lv_status;
    private boolean isRefreshed;
    private ImageView pp_actionBar;
    private StatusListAdapter statusListAdapter;
    private SharedPreferences sharedpreferences;
    private String max_id;
    private List<Status> statuses;
    private RelativeLayout mainLoader, nextElementLoader, textviewNoAction;
    private boolean firstLoad;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean swiped;
    private boolean flag_loading;
    LinearLayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if( theme == Helper.THEME_LIGHT){
            setTheme(R.style.AppTheme_NoActionBar);
        }else {
            setTheme(R.style.AppThemeDark_NoActionBar);
        }
        setContentView(R.layout.activity_ower_status);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null ){
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.toot_action_bar, null);
            actionBar.setCustomView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

            ImageView close_toot = actionBar.getCustomView().findViewById(R.id.close_toot);
            close_toot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            toolbarTitle = actionBar.getCustomView().findViewById(R.id.toolbar_title);
            pp_actionBar = actionBar.getCustomView().findViewById(R.id.pp_actionBar);
            toolbarTitle.setText(getString(R.string.owner_cached_toots));
        }
        statuses = new ArrayList<>();
        lv_status = findViewById(R.id.lv_status);
        mainLoader =  findViewById(R.id.loader);
        nextElementLoader = findViewById(R.id.loading_next_status);
        textviewNoAction =  findViewById(R.id.no_action);
        mainLoader.setVisibility(View.VISIBLE);
        nextElementLoader.setVisibility(View.GONE);
        max_id = null;
        flag_loading = true;
        firstLoad = true;
        swiped = false;
        boolean isOnWifi = Helper.isOnWIFI(OwnerStatusActivity.this);
        int positionSpinnerTrans = sharedpreferences.getInt(Helper.SET_TRANSLATOR, Helper.TRANS_YANDEX);
        int behaviorWithAttachments = sharedpreferences.getInt(Helper.SET_ATTACHMENT_ACTION, Helper.ATTACHMENT_ALWAYS);
        lv_status.addItemDecoration(new DividerItemDecoration(OwnerStatusActivity.this, DividerItemDecoration.VERTICAL));
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        statusListAdapter = new StatusListAdapter(OwnerStatusActivity.this, RetrieveFeedsAsyncTask.Type.CACHE_STATUS, userId, isOnWifi, behaviorWithAttachments, positionSpinnerTrans, this.statuses);
        lv_status.setAdapter(statusListAdapter);
        mLayoutManager = new LinearLayoutManager(OwnerStatusActivity.this);
        lv_status.setLayoutManager(mLayoutManager);

        SQLiteDatabase db = Sqlite.getInstance(OwnerStatusActivity.this, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        Account account = new AccountDAO(OwnerStatusActivity.this,db).getAccountByID(userId);
        String url = account.getAvatar();
        if( url.startsWith("/") ){
            url = Helper.getLiveInstanceWithProtocol(OwnerStatusActivity.this) + account.getAvatar();
        }
        Glide.with(OwnerStatusActivity.this)
                .asBitmap()
                .load(url)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                        BitmapDrawable ppDrawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(resource, (int) Helper.convertDpToPixel(25, OwnerStatusActivity.this), (int) Helper.convertDpToPixel(25, OwnerStatusActivity.this), true));
                        if( pp_actionBar != null){
                            pp_actionBar.setImageDrawable(ppDrawable);
                        } else if( getSupportActionBar() != null){

                            getSupportActionBar().setIcon(ppDrawable);
                            getSupportActionBar().setDisplayShowHomeEnabled(true);
                        }
                    }
                });

        isRefreshed = false;

        swipeRefreshLayout = findViewById(R.id.swipeContainer);
        new RetrieveFeedsAsyncTask(OwnerStatusActivity.this, RetrieveFeedsAsyncTask.Type.CACHE_STATUS, null, OwnerStatusActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        swipeRefreshLayout.setColorSchemeResources(R.color.mastodonC4,
                R.color.mastodonC2,
                R.color.mastodonC3);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isRefreshed = true;
                new RetrieveFeedsAsyncTask(OwnerStatusActivity.this, RetrieveFeedsAsyncTask.Type.CACHE_STATUS, null, OwnerStatusActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        lv_status.addOnScrollListener(new RecyclerView.OnScrollListener() {
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                if(dy > 0){
                    int visibleItemCount = mLayoutManager.getChildCount();
                    int totalItemCount = mLayoutManager.getItemCount();
                    if(firstVisibleItem + visibleItemCount == totalItemCount ) {
                        if(!flag_loading ) {
                            flag_loading = true;
                            new RetrieveFeedsAsyncTask(OwnerStatusActivity.this, RetrieveFeedsAsyncTask.Type.CACHE_STATUS, max_id, OwnerStatusActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            nextElementLoader.setVisibility(View.VISIBLE);
                        }
                    } else {
                        nextElementLoader.setVisibility(View.GONE);
                    }
                }
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_owner_cache, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_sync:
                Intent backupIntent = new Intent(OwnerStatusActivity.this, BackupStatusInDataBaseService.class);
                startService(backupIntent);
                return true;
            case R.id.action_filter:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRetrieveFeeds(APIResponse apiResponse) {
        mainLoader.setVisibility(View.GONE);
        nextElementLoader.setVisibility(View.GONE);
        //Discards 404 - error which can often happen due to toots which have been deleted
        if( apiResponse.getError() != null && apiResponse.getError().getStatusCode() != 404 ){
            boolean show_error_messages = sharedpreferences.getBoolean(Helper.SET_SHOW_ERROR_MESSAGES, true);
            if( show_error_messages )
                Toast.makeText(OwnerStatusActivity.this, apiResponse.getError().getError(),Toast.LENGTH_LONG).show();
            swipeRefreshLayout.setRefreshing(false);
            swiped = false;
            flag_loading = false;
            return;
        }
        int previousPosition = this.statuses.size();
        List<Status> statuses = apiResponse.getStatuses();
        max_id = apiResponse.getMax_id();
        flag_loading = (max_id == null );
        if( !swiped && firstLoad && (statuses == null || statuses.size() == 0))
            textviewNoAction.setVisibility(View.VISIBLE);
        else
            textviewNoAction.setVisibility(View.GONE);

        if( swiped ){
            if (previousPosition > 0) {
                for (int i = 0; i < previousPosition; i++) {
                    this.statuses.remove(0);
                }
                statusListAdapter.notifyItemRangeRemoved(0, previousPosition);
            }
            swiped = false;
        }
        if( statuses != null && statuses.size() > 0) {
            this.statuses.addAll(statuses);
            statusListAdapter.notifyItemRangeInserted(previousPosition, statuses.size());
        }
        swipeRefreshLayout.setRefreshing(false);
        firstLoad = false;

    }


}
