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


import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Status;
import app.fedilab.android.drawers.StatusListAdapter;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.sqlite.AccountDAO;
import app.fedilab.android.sqlite.Sqlite;
import es.dmoral.toasty.Toasty;
import app.fedilab.android.R;
import app.fedilab.android.asynctasks.RetrieveContextAsyncTask;
import app.fedilab.android.asynctasks.UpdateAccountInfoAsyncTask;
import app.fedilab.android.interfaces.OnRetrieveContextInterface;


/**
 * Created by Thomas on 04/05/2017.
 * Show conversation activity class
 */

public class ShowConversationActivity extends BaseActivity implements OnRetrieveContextInterface {


    private Status initialStatus;
    private Status detailsStatus;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView lv_status;
    private List<Status> statuses;
    private StatusListAdapter statusListAdapter;
    private boolean expanded;
    private BroadcastReceiver receive_action;
    private String conversationId;
    private boolean spoilerShown, spoilerBehaviour;
    private LinearLayout loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        switch (theme) {
            case Helper.THEME_LIGHT:
                setTheme(R.style.AppTheme_NoActionBar_Fedilab);
                break;
            case Helper.THEME_BLACK:
                setTheme(R.style.AppThemeBlack_NoActionBar);
                break;
            default:
                setTheme(R.style.AppThemeDark_NoActionBar);
        }
        setContentView(R.layout.activity_show_conversation);
        lv_status = findViewById(R.id.lv_status);
        spoilerShown = spoilerBehaviour = sharedpreferences.getBoolean(Helper.SET_EXPAND_CW, false);
        Bundle b = getIntent().getExtras();
        statuses = new ArrayList<>();
        if (b != null) {
            detailsStatus = b.getParcelable("status");
            expanded = b.getBoolean("expanded", false);
            initialStatus = b.getParcelable("initialStatus");
            conversationId = b.getString("conversationId", null);
        }
        if (detailsStatus == null || detailsStatus.getId() == null)
            finish();

        loader = findViewById(R.id.loader);
        loader.setVisibility(View.VISIBLE);
        detailsStatus.setFocused(true);

        if (MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON) {

            if (receive_action != null)
                LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(receive_action);
            receive_action = new BroadcastReceiver() {
                @Override
                public void onReceive(android.content.Context context, Intent intent) {
                    Bundle b = intent.getExtras();
                    assert b != null;
                    Status status = b.getParcelable("status");
                    if (status != null && statusListAdapter != null) {
                        statusListAdapter.notifyStatusWithActionChanged(status);
                    }
                }
            };
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receive_action, new IntentFilter(Helper.RECEIVE_ACTION));
        }

        TextView title = findViewById(R.id.toolbar_title);
        ImageView pp_actionBar = findViewById(R.id.pp_actionBar);
        ImageView action_refresh = findViewById(R.id.action_refresh);
        ImageView action_expand = findViewById(R.id.action_expand);
        title.setText(R.string.conversation);
        ImageView close_conversation = findViewById(R.id.close_conversation);
        ImageView action_unhide = findViewById(R.id.action_unhide);
        if (expanded)
            action_expand.setImageResource(R.drawable.ic_expand_less);
        else
            action_expand.setImageResource(R.drawable.ic_expand_more);
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (lv_status != null) {
                    lv_status.setAdapter(statusListAdapter);
                }
            }
        });
        if (close_conversation != null) {
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
                Intent intent = new Intent(ShowConversationActivity.this, ShowConversationActivity.class);
                Bundle b = new Bundle();
                b.putParcelable("status", detailsStatus);
                b.putBoolean("expanded", expanded);
                if (expanded && statuses != null && statuses.size() > 0)
                    b.putParcelable("initialStatus", statuses.get(0));
                intent.putExtras(b);
                finish();
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        action_unhide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (statuses != null && statuses.size() > 0) {
                    spoilerShown = !spoilerShown;
                    for (Status status : statuses) {
                        if (spoilerBehaviour && !status.isSpoilerShown()) {
                            status.setAutoHiddenCW(true);
                        } else {
                            status.setAutoHiddenCW(false);
                        }
                        status.setSpoilerShown(spoilerShown);
                        status.setShowSpoiler(spoilerShown);
                    }
                    statusListAdapter.notifyItemRangeChanged(0, statuses.size());
                }

            }
        });
        action_expand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expanded = !expanded;
                Intent intent = new Intent(ShowConversationActivity.this, ShowConversationActivity.class);
                Bundle b = new Bundle();
                b.putParcelable("status", detailsStatus);
                b.putBoolean("expanded", expanded);
                if (expanded && statuses != null && statuses.size() > 0)
                    b.putParcelable("initialStatus", statuses.get(0));
                intent.putExtras(b);
                finish();
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

            }
        });

        SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = sharedpreferences.getString(Helper.PREF_INSTANCE, null);
        Account account = new AccountDAO(getApplicationContext(), db).getUniqAccount(userId, instance);
        if (account.getAvatar() == null) {
            Toasty.error(getApplicationContext(), getString(R.string.toast_error), Toast.LENGTH_LONG).show();
            finish();
        }
        Helper.loadGiF(getApplicationContext(), account.getAvatar(), pp_actionBar);


        swipeRefreshLayout = findViewById(R.id.swipeContainer);
        boolean isOnWifi = Helper.isOnWIFI(getApplicationContext());
        if (initialStatus != null)
            statuses.add(initialStatus);
        else
            statuses.add(detailsStatus);
        statusListAdapter = new StatusListAdapter(0, null, isOnWifi, statuses);

        final LinearLayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(this);
        lv_status.setLayoutManager(mLayoutManager);
        lv_status.setAdapter(statusListAdapter);
        String statusIdToFetch = null;
        if (initialStatus != null)
            statusIdToFetch = initialStatus.getId();
        else if (detailsStatus != null)
            statusIdToFetch = detailsStatus.getId();
        if (statusIdToFetch == null)
            finish();
        if (conversationId != null)
            statusIdToFetch = conversationId;

        new RetrieveContextAsyncTask(getApplicationContext(), expanded, detailsStatus.getVisibility().equals("direct"), statusIdToFetch, ShowConversationActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        swipeRefreshLayout.setDistanceToTriggerSync(500);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Intent intent = new Intent(ShowConversationActivity.this, ShowConversationActivity.class);
                Bundle b = new Bundle();
                b.putParcelable("status", detailsStatus);
                b.putBoolean("expanded", expanded);
                if (expanded && statuses != null && statuses.size() > 0)
                    b.putParcelable("initialStatus", statuses.get(0));
                b.putParcelable("status", detailsStatus);
                intent.putExtras(b);
                finish();
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });


    }

    public void addStatuses(Status status) {
        if (status != null && status.getIn_reply_to_id() != null && this.statuses != null) {
            int position = 0;
            for (Status s : this.statuses) {
                if (status.getIn_reply_to_id().equals(s.getId())) {
                    this.statuses.add(position + 1, status);
                    statusListAdapter.notifyItemInserted(position + 1);
                    break;
                }
                position++;
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (statusListAdapter != null) {
            statusListAdapter.storeToot();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (receive_action != null)
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(receive_action);
    }

    @Override
    public void onRetrieveContext(APIResponse apiResponse) {
        swipeRefreshLayout.setRefreshing(false);
        loader.setVisibility(View.GONE);
        if (apiResponse.getError() != null) {
            if( apiResponse.getError().getError() != null) {
                Toasty.error(getApplicationContext(), apiResponse.getError().getError(), Toast.LENGTH_LONG).show();
            }else{
                Toasty.error(getApplicationContext(), getString(R.string.toast_error), Toast.LENGTH_LONG).show();
            }
            return;
        }
        if (apiResponse.getContext() == null || apiResponse.getContext().getAncestors() == null) {
            return;
        }
        if (BaseMainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.GNU && BaseMainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA) {
            statusListAdapter.setConversationPosition(apiResponse.getContext().getAncestors().size());
            if (!expanded) {
                if (apiResponse.getContext().getAncestors() != null && apiResponse.getContext().getAncestors().size() > 0) {
                    statuses.addAll(0, apiResponse.getContext().getAncestors());
                    statusListAdapter.notifyItemRangeInserted(0, apiResponse.getContext().getAncestors().size());
                }
                int targetedPosition = statuses.size()-1;
                if (apiResponse.getContext().getDescendants() != null && apiResponse.getContext().getDescendants().size() > 0) {
                    statuses.addAll(apiResponse.getContext().getAncestors().size() + 1, apiResponse.getContext().getDescendants());
                    statusListAdapter.notifyItemRangeChanged(apiResponse.getContext().getAncestors().size() + 1, apiResponse.getContext().getDescendants().size());
                }
                decorate(targetedPosition);
            } else {
                List<Status> statusesTemp = apiResponse.getContext().getDescendants();
                int i = 1;
                int position = 0;
                for (Status status : statusesTemp) {
                    statuses.add(status);
                    if (status.getId().equals(detailsStatus.getId())) {
                        statusListAdapter.setConversationPosition(i);
                        detailsStatus = status;
                        position = i;
                    }
                    i++;
                }
                decorate(position);
                statusListAdapter.notifyItemRangeChanged(1, apiResponse.getContext().getDescendants().size());
                lv_status.scrollToPosition(position);
            }
        } else {
            int i = 0;
            if (apiResponse.getContext().getAncestors() != null && apiResponse.getContext().getAncestors().size() > 1) {
                statuses = new ArrayList<>();
                statuses.clear();
                for (Status status : apiResponse.getContext().getAncestors()) {
                    if (detailsStatus.equals(status)) {
                        break;
                    }
                    i++;
                }
                boolean isOnWifi = Helper.isOnWIFI(getApplicationContext());
                for (Status status : apiResponse.getContext().getAncestors()) {
                    statuses.add(0, status);
                }
                statusListAdapter = new StatusListAdapter((statuses.size() - 1 - i), null, isOnWifi, statuses);
                statusListAdapter.setConversationPosition((statuses.size() - 1 - i));
                decorate(0);
                final LinearLayoutManager mLayoutManager;
                mLayoutManager = new LinearLayoutManager(this);
                lv_status.setLayoutManager(mLayoutManager);

                lv_status.setAdapter(statusListAdapter);
            }
        }

    }


    private void decorate(int targetedPosition){
        for(int i =0 ; i < statuses.size() ; i++){
            if (i == targetedPosition) {
                if( targetedPosition < statuses.size()-1 )
                    statuses.get(targetedPosition).setShowBottomLine(true);
                if( targetedPosition > 0 && statuses.get(targetedPosition).getIn_reply_to_id().compareTo(statuses.get(targetedPosition-1).getId()) == 0){
                    statuses.get(targetedPosition-1).setShowBottomLine(true);
                    statuses.get(targetedPosition).setShowTopLine(true);
                }
            } else if (0 < i && i <= statuses.size() - 1) {
                if( statuses.get(i-1).getId().compareTo(statuses.get(i).getIn_reply_to_id()) == 0){
                    statuses.get(i-1).setShowBottomLine(true);
                    statuses.get(i).setShowTopLine(true);
                }
            }
        }
        statusListAdapter.notifyItemRangeChanged(0,statuses.size());
    }

}
