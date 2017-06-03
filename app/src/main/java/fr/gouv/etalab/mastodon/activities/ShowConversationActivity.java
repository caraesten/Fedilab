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


import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_conversation);

        if( getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle b = getIntent().getExtras();
        if(b != null)
            statusId = b.getString("statusId", null);
        if( statusId == null)
            finish();
        setTitle(R.string.conversation);
        new RetrieveFeedsAsyncTask(getApplicationContext(), RetrieveFeedsAsyncTask.Type.ONESTATUS, statusId,null, ShowConversationActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        ListView lv_status = (ListView) findViewById(R.id.lv_status);
        StatusListAdapter statusListAdapter = new StatusListAdapter(ShowConversationActivity.this, RetrieveFeedsAsyncTask.Type.CONTEXT, isOnWifi, behaviorWithAttachments, statuses);
        lv_status.setAdapter(statusListAdapter);
        statusListAdapter.notifyDataSetChanged();
        loader.setVisibility(View.GONE);
        lv_status.setVisibility(View.VISIBLE);
        lv_status.smoothScrollToPosition(position);
    }
}
