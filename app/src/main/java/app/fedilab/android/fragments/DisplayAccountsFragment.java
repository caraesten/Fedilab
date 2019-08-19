package app.fedilab.android.fragments;
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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Relationship;
import app.fedilab.android.drawers.AccountsListAdapter;
import app.fedilab.android.helper.Helper;
import es.dmoral.toasty.Toasty;
import app.fedilab.android.R;
import app.fedilab.android.activities.MainActivity;
import app.fedilab.android.asynctasks.RetrieveAccountsAsyncTask;
import app.fedilab.android.asynctasks.RetrieveManyRelationshipsAsyncTask;
import app.fedilab.android.asynctasks.UpdateAccountInfoAsyncTask;
import app.fedilab.android.interfaces.OnRetrieveAccountsInterface;
import app.fedilab.android.interfaces.OnRetrieveManyRelationshipsInterface;


/**
 * Created by Thomas on 27/04/2017.
 * Fragment to display content related to accounts
 */
public class DisplayAccountsFragment extends Fragment implements OnRetrieveAccountsInterface, OnRetrieveManyRelationshipsInterface {

    private boolean flag_loading;
    private Context context;
    private AsyncTask<Void, Void, Void> asyncTask;
    private AccountsListAdapter accountsListAdapter;
    private String max_id;
    private List<Account> accounts;
    private RetrieveAccountsAsyncTask.Type type;
    private RelativeLayout mainLoader, nextElementLoader, textviewNoAction;
    private boolean firstLoad;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String targetedId, instance, name;
    private boolean swiped;
    private RecyclerView lv_accounts;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_accounts, container, false);

        context = getContext();
        Bundle bundle = this.getArguments();
        accounts = new ArrayList<>();
        if (bundle != null) {
            type = (RetrieveAccountsAsyncTask.Type) bundle.get("type");
            if (bundle.containsKey("tag"))
                targetedId = bundle.getString("tag", null);
            else
                targetedId = bundle.getString("targetedid", null);
            instance = bundle.getString("instance", null);
            name = bundle.getString("name", null);
        }
        max_id = null;
        firstLoad = true;
        flag_loading = true;
        swiped = false;

        swipeRefreshLayout = rootView.findViewById(R.id.swipeContainer);
        lv_accounts = rootView.findViewById(R.id.lv_accounts);
        lv_accounts.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        mainLoader = rootView.findViewById(R.id.loader);
        nextElementLoader = rootView.findViewById(R.id.loading_next_accounts);
        textviewNoAction = rootView.findViewById(R.id.no_action);
        mainLoader.setVisibility(View.VISIBLE);
        nextElementLoader.setVisibility(View.GONE);
        accountsListAdapter = new AccountsListAdapter(type, targetedId, this.accounts);
        lv_accounts.setAdapter(accountsListAdapter);

        final LinearLayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(context);
        lv_accounts.setLayoutManager(mLayoutManager);
        lv_accounts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    int visibleItemCount = mLayoutManager.getChildCount();
                    int totalItemCount = mLayoutManager.getItemCount();
                    int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                    if (firstVisibleItem + visibleItemCount == totalItemCount) {
                        if (!flag_loading) {
                            flag_loading = true;
                            if (type == RetrieveAccountsAsyncTask.Type.SEARCH || type == RetrieveAccountsAsyncTask.Type.FOLLOWERS || type == RetrieveAccountsAsyncTask.Type.FOLLOWING || type == RetrieveAccountsAsyncTask.Type.REBLOGGED || type == RetrieveAccountsAsyncTask.Type.FAVOURITED)
                                asyncTask = new RetrieveAccountsAsyncTask(context, type, targetedId, max_id, DisplayAccountsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            else if (type == RetrieveAccountsAsyncTask.Type.CHANNELS)
                                asyncTask = new RetrieveAccountsAsyncTask(context, instance, name, DisplayAccountsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            else
                                asyncTask = new RetrieveAccountsAsyncTask(context, type, max_id, DisplayAccountsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
                accounts = new ArrayList<>();
                firstLoad = true;
                flag_loading = true;
                swiped = true;
                if (type == RetrieveAccountsAsyncTask.Type.SEARCH || type == RetrieveAccountsAsyncTask.Type.FOLLOWERS || type == RetrieveAccountsAsyncTask.Type.FOLLOWING || type == RetrieveAccountsAsyncTask.Type.REBLOGGED || type == RetrieveAccountsAsyncTask.Type.FAVOURITED)
                    asyncTask = new RetrieveAccountsAsyncTask(context, type, targetedId, max_id, DisplayAccountsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                else if (type == RetrieveAccountsAsyncTask.Type.CHANNELS)
                    asyncTask = new RetrieveAccountsAsyncTask(context, instance, name, DisplayAccountsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                else
                    asyncTask = new RetrieveAccountsAsyncTask(context, type, max_id, DisplayAccountsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        switch (theme) {
            case Helper.THEME_LIGHT:
                swipeRefreshLayout.setColorSchemeResources(R.color.mastodonC4,
                        R.color.mastodonC2,
                        R.color.mastodonC3);
                swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(context, R.color.white));
                break;
            case Helper.THEME_DARK:
                swipeRefreshLayout.setColorSchemeResources(R.color.mastodonC4__,
                        R.color.mastodonC4,
                        R.color.mastodonC4);
                swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(context, R.color.mastodonC1_));
                break;
            case Helper.THEME_BLACK:
                swipeRefreshLayout.setColorSchemeResources(R.color.dark_icon,
                        R.color.mastodonC2,
                        R.color.mastodonC3);
                swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(context, R.color.black_3));
                break;
        }

        if (type == RetrieveAccountsAsyncTask.Type.SEARCH || type == RetrieveAccountsAsyncTask.Type.FOLLOWERS || type == RetrieveAccountsAsyncTask.Type.FOLLOWING || type == RetrieveAccountsAsyncTask.Type.REBLOGGED || type == RetrieveAccountsAsyncTask.Type.FAVOURITED)
            asyncTask = new RetrieveAccountsAsyncTask(context, type, targetedId, max_id, DisplayAccountsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else if (type == RetrieveAccountsAsyncTask.Type.CHANNELS)
            asyncTask = new RetrieveAccountsAsyncTask(context, instance, name, DisplayAccountsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            asyncTask = new RetrieveAccountsAsyncTask(context, type, max_id, DisplayAccountsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        return rootView;
    }

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public void onDestroy() {
        super.onDestroy();
        if (asyncTask != null && asyncTask.getStatus() == AsyncTask.Status.RUNNING)
            asyncTask.cancel(true);
    }

    public void scrollToTop() {
        if (lv_accounts != null)
            lv_accounts.setAdapter(accountsListAdapter);
    }

    @Override
    public void onRetrieveAccounts(APIResponse apiResponse) {
        mainLoader.setVisibility(View.GONE);
        nextElementLoader.setVisibility(View.GONE);
        if (apiResponse.getError() != null) {
            Toasty.error(context, apiResponse.getError().getError(), Toast.LENGTH_LONG).show();
            swipeRefreshLayout.setRefreshing(false);
            swiped = false;
            flag_loading = false;
            return;
        }
        flag_loading = (apiResponse.getMax_id() == null);

        List<Account> accounts;
        if (apiResponse.getResults() != null && apiResponse.getResults().getAccounts() != null)
            accounts = apiResponse.getResults().getAccounts();
        else
            accounts = apiResponse.getAccounts();
        if (!swiped && firstLoad && (accounts == null || accounts.size() == 0))
            textviewNoAction.setVisibility(View.VISIBLE);
        else
            textviewNoAction.setVisibility(View.GONE);

        if (type == RetrieveAccountsAsyncTask.Type.SEARCH) {
            if (max_id == null)
                max_id = "0";
            max_id = String.valueOf(Integer.valueOf(max_id) + 20);
        } else {
            max_id = apiResponse.getMax_id();
        }
        if (swiped) {
            accountsListAdapter = new AccountsListAdapter(type, targetedId, this.accounts);
            lv_accounts.setAdapter(accountsListAdapter);
            swiped = false;
        }
        if (accounts != null && accounts.size() > 0) {
            int currentPosition = this.accounts.size();
            this.accounts.addAll(accounts);
            accountsListAdapter.notifyItemRangeChanged(currentPosition, accounts.size());
        }
        swipeRefreshLayout.setRefreshing(false);
        firstLoad = false;
        if (type != RetrieveAccountsAsyncTask.Type.BLOCKED && (MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON || MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA))
            new RetrieveManyRelationshipsAsyncTask(context, accounts, DisplayAccountsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onRetrieveRelationship(APIResponse apiResponse) {
        if (apiResponse.getError() != null) {
            Toasty.error(context, apiResponse.getError().getError(), Toast.LENGTH_LONG).show();
            return;
        }
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        List<Relationship> relationships = apiResponse.getRelationships();
        if (relationships != null && relationships.size() > 0) {
            for (Relationship relationship : relationships) {
                for (Account account : accounts) {
                    if (account.getId().equals(userId)) {
                        account.setFollowType(Account.followAction.NOTHING);
                        continue;
                    }
                    if (account.getId().equals(relationship.getId())) {
                        account.setMuting_notifications(relationship.isMuting_notifications());
                        if (relationship.isFollowing())
                            account.setFollowType(Account.followAction.FOLLOW);
                        else
                            account.setFollowType(Account.followAction.NOT_FOLLOW);
                        if (relationship.isBlocking())
                            account.setFollowType(Account.followAction.BLOCK);
                        else if (relationship.isMuting()) {
                            account.setFollowType(Account.followAction.MUTE);
                        } else if (relationship.isRequested())
                            account.setFollowType(Account.followAction.REQUEST_SENT);
                        break;
                    }
                }
            }
            accountsListAdapter.notifyDataSetChanged();
        }
    }

    public void pullToRefresh(){
        max_id = null;
        accounts = new ArrayList<>();
        firstLoad = true;
        flag_loading = true;
        swiped = true;
        swipeRefreshLayout.setRefreshing(true);
        if (type == RetrieveAccountsAsyncTask.Type.SEARCH || type == RetrieveAccountsAsyncTask.Type.FOLLOWERS || type == RetrieveAccountsAsyncTask.Type.FOLLOWING || type == RetrieveAccountsAsyncTask.Type.REBLOGGED || type == RetrieveAccountsAsyncTask.Type.FAVOURITED)
            asyncTask = new RetrieveAccountsAsyncTask(context, type, targetedId, max_id, DisplayAccountsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else if (type == RetrieveAccountsAsyncTask.Type.CHANNELS)
            asyncTask = new RetrieveAccountsAsyncTask(context, instance, name, DisplayAccountsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            asyncTask = new RetrieveAccountsAsyncTask(context, type, max_id, DisplayAccountsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
