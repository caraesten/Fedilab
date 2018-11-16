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
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveContextAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveFeedsAsyncTask;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Card;
import fr.gouv.etalab.mastodon.client.Entities.Context;
import fr.gouv.etalab.mastodon.client.Entities.Error;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.drawers.ConversationDecoration;
import fr.gouv.etalab.mastodon.drawers.StatusListAdapter;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveCardInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveContextInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveFeedsInterface;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;

import static fr.gouv.etalab.mastodon.helper.Helper.THEME_BLACK;
import static fr.gouv.etalab.mastodon.helper.Helper.THEME_LIGHT;


/**
 * Created by Thomas on 04/05/2017.
 * Show conversation activity class
 */

public class ShowConversationActivity extends BaseActivity implements OnRetrieveFeedsInterface, OnRetrieveContextInterface, OnRetrieveCardInterface {


    private String statusId;
    private Status initialStatus;
    private Status detailsStatus;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView lv_status;
    private boolean isRefreshed;
    private ImageView pp_actionBar;
    private List<Status> statuses;
    private StatusListAdapter statusListAdapter;
    private boolean expanded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
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
        expanded = false;

        setContentView(R.layout.activity_show_conversation);
        lv_status = findViewById(R.id.lv_status);
        Toolbar toolbar = findViewById(R.id.toolbar);
        if( theme == THEME_BLACK)
            toolbar.setBackgroundColor(ContextCompat.getColor(ShowConversationActivity.this, R.color.black));
        setSupportActionBar(toolbar);

        Bundle b = getIntent().getExtras();
        statuses = new ArrayList<>();
        if(b != null)
            detailsStatus = b.getParcelable("status");
        if( detailsStatus == null || detailsStatus.getId() == null)
            finish();

        if( getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if( getSupportActionBar() != null) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.conversation_action_bar, null);
            getSupportActionBar().setCustomView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            TextView title = getSupportActionBar().getCustomView().findViewById(R.id.toolbar_title);
            pp_actionBar = getSupportActionBar().getCustomView().findViewById(R.id.pp_actionBar);
            ImageView action_refresh = getSupportActionBar().getCustomView().findViewById(R.id.action_refresh);
            final ImageView action_expand = getSupportActionBar().getCustomView().findViewById(R.id.action_expand);
            title.setText(R.string.conversation);
            ImageView close_conversation = getSupportActionBar().getCustomView().findViewById(R.id.close_conversation);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if( lv_status != null) {
                        lv_status.setAdapter(statusListAdapter);
                    }
                }
            });
            if( close_conversation != null){
                close_conversation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
            }
            action_refresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if( statuses != null) {
                        swipeRefreshLayout.setRefreshing(true);
                        (new Handler()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                isRefreshed = true;
                                statusId = statuses.get(statuses.size() - 1).getId();
                                new RetrieveFeedsAsyncTask(getApplicationContext(), RetrieveFeedsAsyncTask.Type.ONESTATUS, statusId, null, false, false, ShowConversationActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            }
                        }, 1000);
                    }
                }
            });
            action_expand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    expanded = !expanded;
                    if( expanded)
                        action_expand.setImageResource(R.drawable.ic_expand_less);
                    else
                        action_expand.setImageResource(R.drawable.ic_expand_more);
                    new RetrieveFeedsAsyncTask(getApplicationContext(), RetrieveFeedsAsyncTask.Type.ONESTATUS, statusId,null, false,false, ShowConversationActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                }
            });

            if (theme == THEME_LIGHT){
                Helper.colorizeToolbar(getSupportActionBar().getCustomView().findViewById(R.id.toolbar), R.color.black, ShowConversationActivity.this);
            }
        }else{
            setTitle(R.string.conversation);
        }

        SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        Account account = new AccountDAO(getApplicationContext(),db).getAccountByID(userId);
        if( account.getAvatar() == null){
            Toast.makeText(ShowConversationActivity.this,R.string.toast_error, Toast.LENGTH_LONG).show();
            finish();
        }
        String url = account.getAvatar();
        if( url.startsWith("/") ){
            url = Helper.getLiveInstanceWithProtocol(getApplicationContext()) + account.getAvatar();
        }
        Helper.loadGiF(getApplicationContext(), url, pp_actionBar);

        isRefreshed = false;

        swipeRefreshLayout = findViewById(R.id.swipeContainer);
        //new RetrieveFeedsAsyncTask(getApplicationContext(), RetrieveFeedsAsyncTask.Type.ONESTATUS, statusId,null, false,false, ShowConversationActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        boolean isOnWifi = Helper.isOnWIFI(getApplicationContext());
        int behaviorWithAttachments = sharedpreferences.getInt(Helper.SET_ATTACHMENT_ACTION, Helper.ATTACHMENT_ALWAYS);
        int positionSpinnerTrans = sharedpreferences.getInt(Helper.SET_TRANSLATOR, Helper.TRANS_YANDEX);
        statuses.add(detailsStatus);
        statusListAdapter = new StatusListAdapter(ShowConversationActivity.this, 0, null, isOnWifi, behaviorWithAttachments, positionSpinnerTrans, statuses);

        final LinearLayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(this);
        Log.v(Helper.TAG,"statuses= " + statuses.size());
        lv_status.setLayoutManager(mLayoutManager);
        boolean compactMode = sharedpreferences.getBoolean(Helper.SET_COMPACT_MODE, false);
        lv_status.addItemDecoration(new ConversationDecoration(ShowConversationActivity.this, theme, compactMode));
        lv_status.setAdapter(statusListAdapter);
        new RetrieveContextAsyncTask(getApplicationContext(), expanded, detailsStatus.getId(), ShowConversationActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        switch (theme){
            case Helper.THEME_LIGHT:
                swipeRefreshLayout.setColorSchemeResources(R.color.mastodonC4,
                        R.color.mastodonC2,
                        R.color.mastodonC3);
                swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(ShowConversationActivity.this, R.color.white));
                break;
            case Helper.THEME_DARK:
                swipeRefreshLayout.setColorSchemeResources(R.color.mastodonC4__,
                        R.color.mastodonC4,
                        R.color.mastodonC4);
                swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(ShowConversationActivity.this, R.color.mastodonC1_));
                break;
            case Helper.THEME_BLACK:
                swipeRefreshLayout.setColorSchemeResources(R.color.dark_icon,
                        R.color.mastodonC2,
                        R.color.mastodonC3);
                swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(ShowConversationActivity.this, R.color.black_3));
                break;
        }
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isRefreshed = true;
                new RetrieveFeedsAsyncTask(getApplicationContext(), RetrieveFeedsAsyncTask.Type.ONESTATUS, statusId,null, false,false, ShowConversationActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
            Toast.makeText(getApplicationContext(), apiResponse.getError().getError(),Toast.LENGTH_LONG).show();
            return;
        }
        List<Status> statuses = apiResponse.getStatuses();
        if( statuses != null && statuses.size() > 0 ){
            initialStatus = statuses.get(0);
            new RetrieveContextAsyncTask(getApplicationContext(), expanded, initialStatus.getId(), ShowConversationActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void onRetrieveContext(Context context, Status statusFirst, Error error) {
        swipeRefreshLayout.setRefreshing(false);
        if( error != null){
            Toast.makeText(getApplicationContext(), error.getError(),Toast.LENGTH_LONG).show();
            return;
        }

        if( expanded) {
            int position = 0;
            boolean positionFound = false;
            statuses = new ArrayList<>();
            if (statusFirst != null)
                statuses.add(0, statusFirst);
            if (context.getAncestors() != null && context.getAncestors().size() > 0) {
                for (Status status : context.getAncestors()) {
                    statuses.add(status);
                    if (!positionFound)
                        position++;
                    if (status.getId().equals(initialStatus.getId()))
                        positionFound = true;

                }
            } else if (statusFirst == null) {
                statuses.add(0, initialStatus);
                positionFound = true;
            }
            if (context.getDescendants() != null && context.getDescendants().size() > 0) {
                for (Status status : context.getDescendants()) {
                    statuses.add(status);
                    if (!positionFound)
                        position++;
                    if (status.getId().equals(initialStatus.getId()))
                        positionFound = true;

                }
            }
            if( isRefreshed){
                position = statuses.size()-1;
                lv_status.scrollToPosition(position);
            }else {
                lv_status.smoothScrollToPosition(position);
            }

            statusListAdapter.notifyDataSetChanged();
        }else {
            if (context.getAncestors() != null && context.getAncestors().size() > 0) {
                Log.v(Helper.TAG,"getAncestors= " + context.getAncestors().size());
                statuses.addAll(0,context.getAncestors());
                statusListAdapter.notifyItemRangeInserted(0, context.getAncestors().size()-1);
            }

            if (context.getDescendants() != null && context.getDescendants().size() > 0) {
                Log.v(Helper.TAG,"getDescendants= " + context.getDescendants().size());
                statuses.addAll(context.getAncestors().size()+1,context.getDescendants());
                statusListAdapter.notifyItemRangeInserted(context.getAncestors().size()+1, context.getDescendants().size()-1);
            }
            Log.v(Helper.TAG,"statuses= " +statuses.size());
        }
        /*statusListAdapter = new StatusListAdapter(ShowConversationActivity.this, position, null, isOnWifi, behaviorWithAttachments, positionSpinnerTrans, statuses);

        final LinearLayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(this);

        lv_status.setLayoutManager(mLayoutManager);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        boolean compactMode = sharedpreferences.getBoolean(Helper.SET_COMPACT_MODE, false);
        lv_status.addItemDecoration(new ConversationDecoration(ShowConversationActivity.this, theme, compactMode));
        lv_status.setAdapter(statusListAdapter);*/




    }

    @Override
    public void onRetrieveAccount(Card card) {
        int position = 0;
        for(Status status: this.statuses) {
            if( initialStatus.getId().equals(status.getId())) {
                if( card != null) {
                    this.statuses.get(position).setCard(card);
                    initialStatus.setCard(card);
                    statusListAdapter.notifyItemChanged(position);
                }
                return;
            }
            position++;
        }
    }
}
