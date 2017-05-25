package fr.gouv.etalab.mastodon.fragments;
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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RelativeLayout;


import java.util.ArrayList;
import java.util.List;


import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.drawers.StatusListAdapter;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import mastodon.etalab.gouv.fr.mastodon.R;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveFeedsAsyncTask;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveFeedsInterface;


/**
 * Created by Thomas on 24/04/2017.
 * Fragment to display content related to status
 */
public class DisplayStatusFragment extends Fragment implements OnRetrieveFeedsInterface {


    private boolean flag_loading;
    private Context context;
    private AsyncTask<Void, Void, Void> asyncTask;
    private StatusListAdapter statusListAdapter;
    private String max_id;
    private List<Status> statuses;
    private static RetrieveFeedsAsyncTask.Type type;
    private RelativeLayout mainLoader, nextElementLoader, textviewNoAction;
    private boolean firstLoad;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String targetedId;
    private String tag;
    private boolean hideHeader = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_status, container, false);

        context = getContext();
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            type = (RetrieveFeedsAsyncTask.Type) bundle.get("type");
            targetedId = bundle.getString("targetedId", null);
            tag = bundle.getString("tag", null);
            hideHeader = bundle.getBoolean("hideHeader", false);
        }
        max_id = null;
        flag_loading = true;
        firstLoad = true;
        statuses = new ArrayList<>();
        boolean isOnWifi = Helper.isOnWIFI(context);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeContainer);
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int behaviorWithAttachments = sharedpreferences.getInt(Helper.SET_ATTACHMENT_ACTION, Helper.ATTACHMENT_ALWAYS);

        final ListView lv_status = (ListView) rootView.findViewById(R.id.lv_status);

        mainLoader = (RelativeLayout) rootView.findViewById(R.id.loader);
        nextElementLoader = (RelativeLayout) rootView.findViewById(R.id.loading_next_status);
        textviewNoAction = (RelativeLayout) rootView.findViewById(R.id.no_action);
        mainLoader.setVisibility(View.VISIBLE);
        nextElementLoader.setVisibility(View.GONE);
        statusListAdapter = new StatusListAdapter(context, type, isOnWifi, behaviorWithAttachments, this.statuses);
        lv_status.setAdapter(statusListAdapter);
        lv_status.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if(firstVisibleItem + visibleItemCount == totalItemCount ) {
                    if(!flag_loading ) {
                        flag_loading = true;
                        if( type == RetrieveFeedsAsyncTask.Type.USER)
                            asyncTask = new RetrieveFeedsAsyncTask(context, type, targetedId, max_id, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        if( type == RetrieveFeedsAsyncTask.Type.TAG)
                            asyncTask = new RetrieveFeedsAsyncTask(context, type, tag, targetedId, max_id, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        else
                            asyncTask = new RetrieveFeedsAsyncTask(context, type, max_id, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                        nextElementLoader.setVisibility(View.VISIBLE);
                    }
                } else {
                    nextElementLoader.setVisibility(View.GONE);
                }
            }
        });

        //Hide account header when scrolling for ShowAccountActivity
        if( hideHeader ) {
            lv_status.setOnScrollListener(new AbsListView.OnScrollListener() {
                int lastFirstVisibleItem = 0;

                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (view.getId() == lv_status.getId() && totalItemCount > visibleItemCount) {
                        final int currentFirstVisibleItem = lv_status.getFirstVisiblePosition();

                        if (currentFirstVisibleItem > lastFirstVisibleItem) {
                            Intent intent = new Intent(Helper.HEADER_ACCOUNT);
                            intent.putExtra("hide", true);
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                        } else if (currentFirstVisibleItem < lastFirstVisibleItem) {
                            Intent intent = new Intent(Helper.HEADER_ACCOUNT);
                            intent.putExtra("hide", false);
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                        }
                        lastFirstVisibleItem = currentFirstVisibleItem;
                    }
                }
            });
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                max_id = null;
                statuses = new ArrayList<>();
                firstLoad = true;
                flag_loading = true;
                if( type == RetrieveFeedsAsyncTask.Type.USER)
                    asyncTask = new RetrieveFeedsAsyncTask(context, type, targetedId, max_id, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                if( type == RetrieveFeedsAsyncTask.Type.TAG)
                    asyncTask = new RetrieveFeedsAsyncTask(context, type, tag, targetedId, max_id, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                else
                    asyncTask = new RetrieveFeedsAsyncTask(context, type, max_id, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent,
                R.color.colorPrimary,
                R.color.colorPrimaryDark);


        if( type == RetrieveFeedsAsyncTask.Type.USER)
            asyncTask = new RetrieveFeedsAsyncTask(context, type, targetedId, max_id, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        if( type == RetrieveFeedsAsyncTask.Type.TAG)
            asyncTask = new RetrieveFeedsAsyncTask(context, type, tag, targetedId, max_id, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            asyncTask = new RetrieveFeedsAsyncTask(context, type, max_id, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return rootView;
    }



    @Override
    public void onCreate(Bundle saveInstance)
    {
        super.onCreate(saveInstance);
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }


    public void onStop() {
        super.onStop();
        if(asyncTask != null && asyncTask.getStatus() == AsyncTask.Status.RUNNING)
            asyncTask.cancel(true);
    }



    @Override
    public void onRetrieveFeeds(List<Status> statuses) {

        if( firstLoad && (statuses == null || statuses.size() == 0))
            textviewNoAction.setVisibility(View.VISIBLE);
        else
            textviewNoAction.setVisibility(View.GONE);
        if( statuses != null && statuses.size() > 1)
            max_id =statuses.get(statuses.size()-1).getId();
        else
            max_id = null;
        mainLoader.setVisibility(View.GONE);
        nextElementLoader.setVisibility(View.GONE);

        if( statuses != null) {
            for(Status tmpStatus: statuses){
                this.statuses.add(tmpStatus);
            }
            statusListAdapter.notifyDataSetChanged();
        }
        swipeRefreshLayout.setRefreshing(false);
        firstLoad = false;
        flag_loading = false;
        //Store last toot id for home timeline to avoid to notify for those that have been already seen
        if(statuses != null && statuses.size()  > 0 && type == RetrieveFeedsAsyncTask.Type.HOME ){
            final SharedPreferences sharedpreferences = getContext().getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            //acct is null when used in Fragment, data need to be retrieved via shared preferences and db
            String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
            SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
            Account currentAccount = new AccountDAO(context, db).getAccountByID(userId);
            if( currentAccount != null){
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(Helper.LAST_HOMETIMELINE_MAX_ID + currentAccount.getAcct(), statuses.get(0).getId());
                editor.apply();
            }
        }

    }
}
