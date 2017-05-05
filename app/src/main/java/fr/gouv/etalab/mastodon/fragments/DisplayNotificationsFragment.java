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
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import fr.gouv.etalab.mastodon.drawers.NotificationsListAdapter;
import fr.gouv.etalab.mastodon.helper.Helper;
import mastodon.etalab.gouv.fr.mastodon.R;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveNotificationsAsyncTask;
import fr.gouv.etalab.mastodon.client.Entities.Notification;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveNotificationsInterface;


/**
 * Created by Thomas on 28/04/2017.
 * Fragment to display notifications related to accounts
 */
public class DisplayNotificationsFragment extends Fragment implements OnRetrieveNotificationsInterface {



    private boolean flag_loading;
    private Context context;
    private AsyncTask<Void, Void, Void> asyncTask;
    private NotificationsListAdapter notificationsListAdapter;
    private String max_id = null;
    private List<Notification> notifications;
    private RelativeLayout mainLoader, nextElementLoader, textviewNoAction;
    private boolean firstLoad;
    private SwipeRefreshLayout swipeRefreshLayout;
    private int notificationPerPage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_notifications, container, false);

        context = getContext();
        firstLoad = true;
        flag_loading = true;
        notifications = new ArrayList<>();

        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeContainer);
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        notificationPerPage = sharedpreferences.getInt(Helper.SET_NOTIFICATIONS_PER_PAGE, 40);
        ListView lv_notifications = (ListView) rootView.findViewById(R.id.lv_notifications);

        mainLoader = (RelativeLayout) rootView.findViewById(R.id.loader);
        nextElementLoader = (RelativeLayout) rootView.findViewById(R.id.loading_next_notifications);
        textviewNoAction = (RelativeLayout) rootView.findViewById(R.id.no_action);
        mainLoader.setVisibility(View.VISIBLE);
        nextElementLoader.setVisibility(View.GONE);
        notificationsListAdapter = new NotificationsListAdapter(context, this.notifications);
        lv_notifications.setAdapter(notificationsListAdapter);
        lv_notifications.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if(firstVisibleItem + visibleItemCount == totalItemCount ) {
                    if(!flag_loading ) {
                        flag_loading = true;
                        asyncTask = new RetrieveNotificationsAsyncTask(context, max_id, null,DisplayNotificationsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        nextElementLoader.setVisibility(View.VISIBLE);
                    }
                } else {
                    nextElementLoader.setVisibility(View.GONE);
                }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                max_id = null;
                notifications = new ArrayList<>();
                firstLoad = true;
                flag_loading = true;
                asyncTask = new RetrieveNotificationsAsyncTask(context, max_id, null,DisplayNotificationsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent,
                R.color.colorPrimary,
                R.color.colorPrimaryDark);


        asyncTask = new RetrieveNotificationsAsyncTask(context, max_id, null, DisplayNotificationsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
    public void onRetrieveNotifications(List<Notification> notifications, String acct) {

        if( firstLoad && (notifications == null || notifications.size() == 0))
            textviewNoAction.setVisibility(View.VISIBLE);
        else
            textviewNoAction.setVisibility(View.GONE);
        if( notifications != null && notifications.size() > 1)
            max_id =notifications.get(notifications.size()-1).getId();
        else
            max_id = null;
        mainLoader.setVisibility(View.GONE);
        nextElementLoader.setVisibility(View.GONE);

        if( notifications != null) {
            for(Notification tmpNotification: notifications){
                this.notifications.add(tmpNotification);
            }
            notificationsListAdapter.notifyDataSetChanged();
        }
        swipeRefreshLayout.setRefreshing(false);
        firstLoad = false;
        if( notifications != null && notifications.size() < notificationPerPage )
            flag_loading = true;
        else
            flag_loading = false;
    }
}
