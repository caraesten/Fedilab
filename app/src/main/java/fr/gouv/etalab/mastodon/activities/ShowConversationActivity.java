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
 * You should have received a copy of the GNU General Public License along with Thomas Schneider; if not,
 * see <http://www.gnu.org/licenses>. */
package fr.gouv.etalab.mastodon.activities;


import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import fr.gouv.etalab.mastodon.asynctasks.RetrieveContextAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveFeedsAsyncTask;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Context;
import fr.gouv.etalab.mastodon.client.Entities.Error;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.drawers.StatusListAdapter;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveContextInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveFeedsInterface;
import mastodon.etalab.gouv.fr.mastodon.R;


/**
 * Created by Thomas on 04/05/2017.
 * Show conversation activity class
 */

public class ShowConversationActivity extends AppCompatActivity implements OnRetrieveFeedsInterface, OnRetrieveContextInterface {


    private String statusId;
    private Status initialStatus;
    public static int position;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView lv_status;
    private boolean isRefreshed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if( theme == Helper.THEME_LIGHT){
            setTheme(R.style.AppTheme);
        }else {
            setTheme(R.style.AppThemeDark);
        }
        setContentView(R.layout.activity_show_conversation);

        if( getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle b = getIntent().getExtras();
        if(b != null)
            statusId = b.getString("statusId", null);
        if( statusId == null)
            finish();
        isRefreshed = false;
        setTitle(R.string.conversation);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        new RetrieveFeedsAsyncTask(getApplicationContext(), RetrieveFeedsAsyncTask.Type.ONESTATUS, statusId,null, ShowConversationActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        if( theme == Helper.THEME_LIGHT) {
            swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent,
                    R.color.colorPrimary,
                    R.color.colorPrimaryDark);
        }else {
            swipeRefreshLayout.setColorSchemeResources(R.color.colorAccentD,
                    R.color.colorPrimaryD,
                    R.color.colorPrimaryDarkD);
        }
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isRefreshed = true;
                new RetrieveFeedsAsyncTask(getApplicationContext(), RetrieveFeedsAsyncTask.Type.ONESTATUS, statusId,null, ShowConversationActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
        lv_status = (ListView) findViewById(R.id.lv_status);
        lv_status.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == android.view.MotionEvent.ACTION_UP) {

                    if (lv_status.getLastVisiblePosition() == lv_status.getAdapter().getCount() -1 &&  lv_status.getFirstVisiblePosition() > 0 &&
                            lv_status.getChildAt(lv_status.getChildCount() - 1).getBottom() <= lv_status.getHeight()) {

                        swipeRefreshLayout.setRefreshing(true);
                        ( new Handler()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                isRefreshed = true;
                                new RetrieveFeedsAsyncTask(getApplicationContext(), RetrieveFeedsAsyncTask.Type.ONESTATUS, statusId,null, ShowConversationActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            }
                        }, 1000);

                    }
                }
                return false;
            }
        });
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
        if( apiResponse.getError() != null){
            final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
            boolean show_error_messages = sharedpreferences.getBoolean(Helper.SET_SHOW_ERROR_MESSAGES, true);
            if( show_error_messages)
                Toast.makeText(getApplicationContext(), apiResponse.getError().getError(),Toast.LENGTH_LONG).show();
            return;
        }
        List<Status> statuses = apiResponse.getStatuses();
        if( statuses != null && statuses.size() > 0 ){
            initialStatus = statuses.get(0);
            new RetrieveContextAsyncTask(getApplicationContext(), initialStatus.getId(), ShowConversationActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void onRetrieveFeeds(Context context, Error error) {
        swipeRefreshLayout.setRefreshing(false);
        if( error != null){
            final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
            boolean show_error_messages = sharedpreferences.getBoolean(Helper.SET_SHOW_ERROR_MESSAGES, true);
            if( show_error_messages)
                Toast.makeText(getApplicationContext(), error.getError(),Toast.LENGTH_LONG).show();
            return;
        }
        boolean isOnWifi = Helper.isOnWIFI(getApplicationContext());
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        int behaviorWithAttachments = sharedpreferences.getInt(Helper.SET_ATTACHMENT_ACTION, Helper.ATTACHMENT_ALWAYS);
        position = 0;
        List<Status> statuses = new ArrayList<>();
        if( context.getAncestors() != null && context.getAncestors().size() > 0){
            for(Status status: context.getAncestors()){
                statuses.add(status);
                position++;
            }
        }
        statuses.add(initialStatus);
        if( context.getDescendants() != null && context.getDescendants().size() > 0){
            for(Status status: context.getDescendants()){
                statuses.add(status);
            }
        }
        RelativeLayout loader = (RelativeLayout) findViewById(R.id.loader);
        StatusListAdapter statusListAdapter = new StatusListAdapter(ShowConversationActivity.this, RetrieveFeedsAsyncTask.Type.CONTEXT, isOnWifi, behaviorWithAttachments, statuses);
        lv_status.setAdapter(statusListAdapter);
        statusListAdapter.notifyDataSetChanged();
        loader.setVisibility(View.GONE);
        lv_status.setVisibility(View.VISIBLE);
        if( isRefreshed){
            position = statuses.size()-1;
            lv_status.setSelection(position);
        }else {
            lv_status.smoothScrollToPosition(position);
        }

    }
}
