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
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
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
import fr.gouv.etalab.mastodon.asynctasks.RetrieveCardAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveContextAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveFeedsAsyncTask;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Card;
import fr.gouv.etalab.mastodon.client.Entities.Context;
import fr.gouv.etalab.mastodon.client.Entities.Error;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.drawers.StatusListAdapter;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveCardInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveContextInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveFeedsInterface;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;

import static fr.gouv.etalab.mastodon.helper.Helper.THEME_BLACK;


/**
 * Created by Thomas on 04/05/2017.
 * Show conversation activity class
 */

public class ShowConversationActivity extends BaseActivity implements OnRetrieveFeedsInterface, OnRetrieveContextInterface, OnRetrieveCardInterface {


    private String statusId;
    private Status initialStatus;
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

        Toolbar toolbar = findViewById(R.id.toolbar);
        if( theme == THEME_BLACK)
            toolbar.setBackgroundColor(ContextCompat.getColor(ShowConversationActivity.this, R.color.black));
        setSupportActionBar(toolbar);

        Bundle b = getIntent().getExtras();
        if(b != null)
            statusId = b.getString("statusId", null);
        if( statusId == null)
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
        Glide.with(getApplicationContext())
                .asBitmap()
                .load(url)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                        BitmapDrawable ppDrawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(resource, (int) Helper.convertDpToPixel(25, getApplicationContext()), (int) Helper.convertDpToPixel(25, getApplicationContext()), true));
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
        new RetrieveFeedsAsyncTask(getApplicationContext(), RetrieveFeedsAsyncTask.Type.ONESTATUS, statusId,null, false,false, ShowConversationActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

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
        lv_status = findViewById(R.id.lv_status);
        lv_status.addItemDecoration(new DividerItemDecoration(ShowConversationActivity.this, DividerItemDecoration.VERTICAL));
        final LinearLayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(this);
        lv_status.setLayoutManager(mLayoutManager);

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
            new RetrieveContextAsyncTask(getApplicationContext(), expanded, initialStatus.getId(), ShowConversationActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void onRetrieveContext(Context context, Status statusFirst, Error error) {
        swipeRefreshLayout.setRefreshing(false);
        RelativeLayout loader = findViewById(R.id.loader);
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
        int positionSpinnerTrans = sharedpreferences.getInt(Helper.SET_TRANSLATOR, Helper.TRANS_YANDEX);
        int position = 0;
        boolean positionFound = false;
        statuses = new ArrayList<>();
        if( expanded) {
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
        }else {
            position = 0;
            if (context.getAncestors() != null && context.getAncestors().size() > 0) {
                statuses.addAll(context.getAncestors());
                position = context.getAncestors().size();
            }
            statuses.add(initialStatus);
            if (context.getDescendants() != null && context.getDescendants().size() > 0) {
                statuses.addAll(context.getDescendants());
            }
        }

        statusListAdapter = new StatusListAdapter(ShowConversationActivity.this, position, null, isOnWifi, behaviorWithAttachments, positionSpinnerTrans, statuses);
        lv_status.setAdapter(statusListAdapter);
        statusListAdapter.notifyDataSetChanged();
        loader.setVisibility(View.GONE);
        lv_status.setVisibility(View.VISIBLE);
        if( isRefreshed){
            position = statuses.size()-1;
            lv_status.scrollToPosition(position);
        }else {
            lv_status.smoothScrollToPosition(position);
        }
        new RetrieveCardAsyncTask(getApplicationContext(), initialStatus.getId(), ShowConversationActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

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
