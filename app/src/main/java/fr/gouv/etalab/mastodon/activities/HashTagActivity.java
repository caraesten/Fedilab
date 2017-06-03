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


import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import fr.gouv.etalab.mastodon.asynctasks.RetrieveFeedsAsyncTask;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.drawers.StatusListAdapter;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveFeedsInterface;
import mastodon.etalab.gouv.fr.mastodon.R;


/**
 * Created by Thomas on 27/05/2017.
 * Show hashtag stream
 */

public class HashTagActivity extends AppCompatActivity implements OnRetrieveFeedsInterface {


    public static int position;
    private StatusListAdapter statusListAdapter;
    private String max_id;
    private List<Status> statuses;
    private RelativeLayout mainLoader, nextElementLoader, textviewNoAction;
    private boolean firstLoad;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String tag;
    private int tootsPerPage;
    private boolean flag_loading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hashtag);

        if( getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle b = getIntent().getExtras();
        if(b != null)
            tag = b.getString("tag", null);
        if( tag == null)
            finish();
        statuses = new ArrayList<>();
        max_id = null;
        flag_loading = true;
        firstLoad = true;
        boolean isOnWifi = Helper.isOnWIFI(getApplicationContext());
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        int behaviorWithAttachments = sharedpreferences.getInt(Helper.SET_ATTACHMENT_ACTION, Helper.ATTACHMENT_ALWAYS);

        final ListView lv_status = (ListView) findViewById(R.id.lv_status);
        tootsPerPage = sharedpreferences.getInt(Helper.SET_TOOTS_PER_PAGE, 40);
        mainLoader = (RelativeLayout) findViewById(R.id.loader);
        nextElementLoader = (RelativeLayout) findViewById(R.id.loading_next_status);
        textviewNoAction = (RelativeLayout) findViewById(R.id.no_action);
        mainLoader.setVisibility(View.VISIBLE);
        nextElementLoader.setVisibility(View.GONE);
        statusListAdapter = new StatusListAdapter(HashTagActivity.this, RetrieveFeedsAsyncTask.Type.TAG, isOnWifi, behaviorWithAttachments, this.statuses);
        lv_status.setAdapter(statusListAdapter);
        setTitle(tag);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                max_id = null;
                statuses = new ArrayList<>();
                firstLoad = true;
                flag_loading = true;
                new RetrieveFeedsAsyncTask(getApplicationContext(), RetrieveFeedsAsyncTask.Type.TAG, tag,null, max_id, HashTagActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent,
                R.color.colorPrimary,
                R.color.colorPrimaryDark);

        lv_status.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(firstVisibleItem + visibleItemCount == totalItemCount ) {
                    if(!flag_loading ) {
                        flag_loading = true;
                        new RetrieveFeedsAsyncTask(getApplicationContext(), RetrieveFeedsAsyncTask.Type.TAG, tag,null, max_id, HashTagActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                        nextElementLoader.setVisibility(View.VISIBLE);
                    }
                } else {
                    nextElementLoader.setVisibility(View.GONE);
                }
            }
        });
        new RetrieveFeedsAsyncTask(getApplicationContext(), RetrieveFeedsAsyncTask.Type.TAG, tag,null, max_id, HashTagActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

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
    public void onRetrieveFeeds(APIResponse apiResponse) {

        mainLoader.setVisibility(View.GONE);
        nextElementLoader.setVisibility(View.GONE);
        if( apiResponse.getError() != null){
            final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            boolean show_error_messages = sharedpreferences.getBoolean(Helper.SET_SHOW_ERROR_MESSAGES, true);
            if( show_error_messages)
                Toast.makeText(getApplicationContext(), apiResponse.getError().getError(),Toast.LENGTH_LONG).show();
            return;
        }
        List<Status> statuses = apiResponse.getStatuses();
        if( firstLoad && (statuses == null || statuses.size() == 0))
            textviewNoAction.setVisibility(View.VISIBLE);
        else
            textviewNoAction.setVisibility(View.GONE);
        if( statuses != null && statuses.size() > 1)
            max_id =statuses.get(statuses.size()-1).getId();
        else
            max_id = null;
        if( statuses != null) {
            for(Status tmpStatus: statuses){
                this.statuses.add(tmpStatus);
            }
            statusListAdapter.notifyDataSetChanged();
        }
        swipeRefreshLayout.setRefreshing(false);
        firstLoad = false;
        flag_loading = statuses != null && statuses.size() < tootsPerPage;
    }

}
