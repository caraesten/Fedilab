package fr.gouv.etalab.mastodon.fragments;
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
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

import fr.gouv.etalab.mastodon.activities.MainActivity;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveFeedsAsyncTask;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Status;
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
    private String max_id;
    private List<Notification> notifications;
    private RelativeLayout mainLoader, nextElementLoader, textviewNoAction;
    private boolean firstLoad;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean swiped;
    private ListView lv_notifications;
    private String lastReadNotifications;
    private String userId;
    public static ArrayList<Notification> tempNotifications = new ArrayList<>();

    public DisplayNotificationsFragment(){
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_notifications, container, false);
        max_id = null;
        context = getContext();
        firstLoad = true;
        flag_loading = true;
        notifications = new ArrayList<>();
        swiped = false;

        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeContainer);
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        lv_notifications = (ListView) rootView.findViewById(R.id.lv_notifications);
        mainLoader = (RelativeLayout) rootView.findViewById(R.id.loader);
        nextElementLoader = (RelativeLayout) rootView.findViewById(R.id.loading_next_notifications);
        textviewNoAction = (RelativeLayout) rootView.findViewById(R.id.no_action);
        mainLoader.setVisibility(View.VISIBLE);
        nextElementLoader.setVisibility(View.GONE);
        boolean isOnWifi = Helper.isOnWIFI(context);
        int behaviorWithAttachments = sharedpreferences.getInt(Helper.SET_ATTACHMENT_ACTION, Helper.ATTACHMENT_ALWAYS);
        userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        lastReadNotifications = sharedpreferences.getString(Helper.LAST_NOTIFICATION_MAX_ID + userId, null);
        notificationsListAdapter = new NotificationsListAdapter(context,isOnWifi, behaviorWithAttachments,this.notifications);
        lv_notifications.setAdapter(notificationsListAdapter);
        lv_notifications.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if(firstVisibleItem + visibleItemCount == totalItemCount ) {
                    if(!flag_loading ) {
                        flag_loading = true;
                        asyncTask = new RetrieveNotificationsAsyncTask(context, null, null, max_id, null, null,DisplayNotificationsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
                swiped = true;
                MainActivity.countNewNotifications = 0;
                asyncTask = new RetrieveNotificationsAsyncTask(context, null, null, max_id, null, null, DisplayNotificationsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.mastodonC4,
                R.color.mastodonC2,
                R.color.mastodonC3);

        asyncTask = new RetrieveNotificationsAsyncTask(context, null, null, max_id, null, null, DisplayNotificationsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


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

    public void onDestroy() {
        super.onDestroy();
        if(asyncTask != null && asyncTask.getStatus() == AsyncTask.Status.RUNNING)
            asyncTask.cancel(true);
    }


    @Override
    public void onResume() {
        super.onResume();
        for(Notification notification: tempNotifications){
            int index = lv_notifications.getFirstVisiblePosition() + 1;
            View v = lv_notifications.getChildAt(0);
            int top = (v == null) ? 0 : v.getTop();
            notifications.add(0,notification);
            notificationsListAdapter.notifyDataSetChanged();
            lv_notifications.setSelectionFromTop(index, top);
            if (textviewNoAction.getVisibility() == View.VISIBLE)
                textviewNoAction.setVisibility(View.GONE);
        }
        ((MainActivity)context).updateNotifCounter();
        tempNotifications.clear();
        tempNotifications = new ArrayList<>();
    }

    @Override
    public void onRetrieveNotifications(APIResponse apiResponse, String acct, String userId, boolean refreshData) {
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        mainLoader.setVisibility(View.GONE);
        nextElementLoader.setVisibility(View.GONE);
        if( apiResponse.getError() != null){
            boolean show_error_messages = sharedpreferences.getBoolean(Helper.SET_SHOW_ERROR_MESSAGES, true);
            if( show_error_messages)
                Toast.makeText(context, apiResponse.getError().getError(),Toast.LENGTH_LONG).show();
            flag_loading = false;
            swipeRefreshLayout.setRefreshing(false);
            swiped = false;
            return;
        }
        max_id = apiResponse.getMax_id();
        List<Notification> notifications = apiResponse.getNotifications();
        //The initial call comes from a classic tab refresh
        flag_loading = (max_id == null );
        if( !swiped && firstLoad && (notifications == null || notifications.size() == 0))
            textviewNoAction.setVisibility(View.VISIBLE);
        else
            textviewNoAction.setVisibility(View.GONE);
        if( swiped ){
            boolean isOnWifi = Helper.isOnWIFI(context);
            int behaviorWithAttachments = sharedpreferences.getInt(Helper.SET_ATTACHMENT_ACTION, Helper.ATTACHMENT_ALWAYS);
            notificationsListAdapter = new NotificationsListAdapter(context,isOnWifi, behaviorWithAttachments, this.notifications);
            lv_notifications.setAdapter(notificationsListAdapter);
            swiped = false;
        }
        if( notifications != null && notifications.size() > 0) {
            for(Notification tmpNotification: notifications){
                if( Long.parseLong(tmpNotification.getId()) > Long.parseLong(lastReadNotifications))
                    MainActivity.countNewNotifications++;
                this.notifications.add(tmpNotification);
            }
            notificationsListAdapter.notifyDataSetChanged();
        }
        if( firstLoad )
            ((MainActivity)context).updateNotifCounter();
        swipeRefreshLayout.setRefreshing(false);
        firstLoad = false;
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if( context == null)
            return;
        //Store last notification id to avoid to notify for those that have been already seen
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        if (visible && notifications != null && notifications.size() > 0) {
            editor.putString(Helper.LAST_NOTIFICATION_MAX_ID + this.userId, notifications.get(0).getId());
            editor.apply();
            lastReadNotifications = notifications.get(0).getId();
        }
    }

    public void scrollToTop(){
        if( lv_notifications != null)
            lv_notifications.setAdapter(notificationsListAdapter);
        //Store last toot id for home timeline to avoid to notify for those that have been already seen
        //Store last notification id to avoid to notify for those that have been already seen
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        if (notifications != null && notifications.size() > 0) {
            editor.putString(Helper.LAST_NOTIFICATION_MAX_ID + this.userId, notifications.get(0).getId());
            editor.apply();
            lastReadNotifications = notifications.get(0).getId();
        }
    }


    public void refresh(Notification notification){
        if( context == null)
            return;
        if( notification != null){
            int index = lv_notifications.getFirstVisiblePosition() + 1;
            View v = lv_notifications.getChildAt(0);
            int top = (v == null) ? 0 : v.getTop();
            notifications.add(0, notification);
            notificationsListAdapter.notifyDataSetChanged();
            lv_notifications.setSelectionFromTop(index, top);
            if( textviewNoAction.getVisibility() == View.VISIBLE)
                textviewNoAction.setVisibility(View.GONE);
        }
    }
}
