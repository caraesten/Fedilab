package app.fedilab.android.fragments;
/* Copyright 2019 Thomas Schneider
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import app.fedilab.android.R;
import app.fedilab.android.asynctasks.PostAdminActionAsyncTask;
import app.fedilab.android.asynctasks.RetrieveAccountsAsyncTask;
import app.fedilab.android.client.API;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.AccountAdmin;
import app.fedilab.android.client.Entities.AdminAction;
import app.fedilab.android.drawers.AccountsAdminListAdapter;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.interfaces.OnAdminActionInterface;
import es.dmoral.toasty.Toasty;


/**
 * Created by Thomas on 19/06/2019.
 * Fragment to display content related to accounts
 */
public class DisplayAdminAccountsFragment extends Fragment implements OnAdminActionInterface {

    private boolean flag_loading;
    private Context context;
    private AsyncTask<Void, Void, Void> asyncTask;
    private AccountsAdminListAdapter accountsAdminListAdapter;
    private String max_id;
    private List<AccountAdmin> accountAdmins;
    private RetrieveAccountsAsyncTask.Type type;
    private RelativeLayout mainLoader, nextElementLoader, textviewNoAction;
    private boolean firstLoad;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean swiped;
    private RecyclerView lv_admin_accounts;
    private boolean local, remote, active, pending, disabled, silenced, suspended;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_admin_accounts, container, false);

        context = getContext();

        accountAdmins = new ArrayList<>();


        Bundle bundle = this.getArguments();
        if (bundle != null) {
            local = bundle.getBoolean("local", false);
            remote = bundle.getBoolean("remote", false);
            active = bundle.getBoolean("active", false);
            pending = bundle.getBoolean("pending", false);
            disabled = bundle.getBoolean("disabled", false);
            silenced = bundle.getBoolean("silenced", false);
            suspended = bundle.getBoolean("suspended", false);
        }

        max_id = null;
        firstLoad = true;
        flag_loading = true;
        swiped = false;

        swipeRefreshLayout = rootView.findViewById(R.id.swipeContainer);
        lv_admin_accounts = rootView.findViewById(R.id.lv_admin_accounts);
        lv_admin_accounts.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        mainLoader = rootView.findViewById(R.id.loader);
        nextElementLoader = rootView.findViewById(R.id.loading_next_accounts);
        textviewNoAction = rootView.findViewById(R.id.no_action);
        mainLoader.setVisibility(View.VISIBLE);
        nextElementLoader.setVisibility(View.GONE);
        accountsAdminListAdapter = new AccountsAdminListAdapter(context, this.accountAdmins);
        lv_admin_accounts.setAdapter(accountsAdminListAdapter);

        final LinearLayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(context);
        lv_admin_accounts.setLayoutManager(mLayoutManager);
        lv_admin_accounts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                if(dy > 0) {
                    int visibleItemCount = mLayoutManager.getChildCount();
                    int totalItemCount = mLayoutManager.getItemCount();
                    int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                    if (firstVisibleItem + visibleItemCount == totalItemCount) {
                        if (!flag_loading) {
                            flag_loading = true;
                            AdminAction adminAction = new AdminAction();
                            adminAction.setLocal(local);
                            adminAction.setRemote(remote);
                            adminAction.setActive(active);
                            adminAction.setPending(pending);
                            adminAction.setDisabled(disabled);
                            adminAction.setSilenced(silenced);
                            adminAction.setSuspended(suspended);
                            asyncTask = new PostAdminActionAsyncTask(context, API.adminAction.GET_ACCOUNTS, null, adminAction, DisplayAdminAccountsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
                accountAdmins = new ArrayList<>();
                firstLoad = true;
                flag_loading = true;
                swiped = true;
                AdminAction adminAction = new AdminAction();
                adminAction.setLocal(local);
                adminAction.setRemote(remote);
                adminAction.setActive(active);
                adminAction.setPending(pending);
                adminAction.setDisabled(disabled);
                adminAction.setSilenced(silenced);
                adminAction.setSuspended(suspended);
                asyncTask = new PostAdminActionAsyncTask(context, API.adminAction.GET_ACCOUNTS, null, adminAction, DisplayAdminAccountsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        switch (theme){
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
        AdminAction adminAction = new AdminAction();
        adminAction.setLocal(local);
        adminAction.setRemote(remote);
        adminAction.setActive(active);
        adminAction.setPending(pending);
        adminAction.setDisabled(disabled);
        adminAction.setSilenced(silenced);
        adminAction.setSuspended(suspended);
        asyncTask = new PostAdminActionAsyncTask(context, API.adminAction.GET_ACCOUNTS, null, adminAction, DisplayAdminAccountsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return rootView;
    }

    @Override
    public void onCreate(Bundle saveInstance)
    {
        super.onCreate(saveInstance);
    }



    /**
     * Refresh accounts in list
     */
    public void refreshFilter(){
        accountsAdminListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public void onDestroy() {
        super.onDestroy();
        if(asyncTask != null && asyncTask.getStatus() == AsyncTask.Status.RUNNING)
            asyncTask.cancel(true);
    }

    public void scrollToTop(){
        if( lv_admin_accounts != null)
            lv_admin_accounts.setAdapter(accountsAdminListAdapter);
    }


    @Override
    public void onAdminAction(APIResponse apiResponse) {
        mainLoader.setVisibility(View.GONE);
        nextElementLoader.setVisibility(View.GONE);
        if( apiResponse.getError() != null){
            Toasty.error(context, apiResponse.getError().getError(),Toast.LENGTH_LONG).show();
            swipeRefreshLayout.setRefreshing(false);
            swiped = false;
            flag_loading = false;
            return;
        }
        flag_loading = (apiResponse.getMax_id() == null );
        List<AccountAdmin> accountAdmins = apiResponse.getAccountAdmins();

        if( !swiped && firstLoad && (accountAdmins == null || accountAdmins.size() == 0))
            textviewNoAction.setVisibility(View.VISIBLE);
        else
            textviewNoAction.setVisibility(View.GONE);

        max_id = apiResponse.getMax_id();

        if( swiped ){
            accountsAdminListAdapter = new AccountsAdminListAdapter(context, this.accountAdmins);
            lv_admin_accounts.setAdapter(accountsAdminListAdapter);
            swiped = false;
        }
        if( accountAdmins != null && accountAdmins.size() > 0) {
            int currentPosition = this.accountAdmins.size();
            this.accountAdmins.addAll(accountAdmins);
            accountsAdminListAdapter.notifyItemRangeChanged(currentPosition,accountAdmins.size());
        }
        swipeRefreshLayout.setRefreshing(false);
        firstLoad = false;
    }
}
