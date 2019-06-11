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
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
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
import app.fedilab.android.client.Entities.PeertubeNotification;
import app.fedilab.android.drawers.PeertubeNotificationsListAdapter;
import app.fedilab.android.helper.Helper;
import es.dmoral.toasty.Toasty;
import app.fedilab.android.R;
import app.fedilab.android.activities.MainActivity;
import app.fedilab.android.asynctasks.RetrievePeertubeNotificationsAsyncTask;
import app.fedilab.android.interfaces.OnRetrievePeertubeNotificationsInterface;


/**
 * Created by Thomas on 24/01/2019.
 * Fragment to display peertube notifications
 */
public class DisplayPeertubeNotificationsFragment extends Fragment implements  OnRetrievePeertubeNotificationsInterface {



    private boolean flag_loading;
    private Context context;
    private AsyncTask<Void, Void, Void> asyncTask;
    private PeertubeNotificationsListAdapter notificationsListAdapter;
    private String max_id;
    private List<PeertubeNotification> notifications;
    private RelativeLayout mainLoader, nextElementLoader, textviewNoAction;
    private boolean firstLoad;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean swiped;
    private RecyclerView lv_notifications;
    private String userId, instance;
    private SharedPreferences sharedpreferences;
    LinearLayoutManager mLayoutManager;


    //Peertube notification type
    public static int NEW_VIDEO_FROM_SUBSCRIPTION = 1;
    public static int NEW_COMMENT_ON_MY_VIDEO = 2;
    public static int NEW_VIDEO_ABUSE_FOR_MODERATORS = 3;
    public static int BLACKLIST_ON_MY_VIDEO = 4;
    public static int UNBLACKLIST_ON_MY_VIDEO = 5;
    public static int MY_VIDEO_PUBLISHED = 6;
    public static int MY_VIDEO_IMPORT_SUCCESS = 7;
    public static int MY_VIDEO_IMPORT_ERROR = 8;
    public static int NEW_USER_REGISTRATION = 9;
    public static int NEW_FOLLOW = 10;
    public static int COMMENT_MENTION = 11;

    public DisplayPeertubeNotificationsFragment(){
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_notifications, container, false);
        max_id = null;
        context = getContext();
        firstLoad = true;
        flag_loading = true;
        notifications = new ArrayList<>();
        swiped = false;
        swipeRefreshLayout = rootView.findViewById(R.id.swipeContainer);
        sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);


        lv_notifications = rootView.findViewById(R.id.lv_notifications);
        mainLoader = rootView.findViewById(R.id.loader);
        nextElementLoader = rootView.findViewById(R.id.loading_next_notifications);
        textviewNoAction = rootView.findViewById(R.id.no_action);
        mainLoader.setVisibility(View.VISIBLE);
        nextElementLoader.setVisibility(View.GONE);
        boolean isOnWifi = Helper.isOnWIFI(context);
        int behaviorWithAttachments = sharedpreferences.getInt(Helper.SET_ATTACHMENT_ACTION, Helper.ATTACHMENT_ALWAYS);
        userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        instance = sharedpreferences.getString(Helper.PREF_INSTANCE, context!=null?Helper.getLiveInstance(context):null);
        notificationsListAdapter = new PeertubeNotificationsListAdapter(context,this.notifications);
        lv_notifications.setAdapter(notificationsListAdapter);
        mLayoutManager = new LinearLayoutManager(context);
        lv_notifications.setLayoutManager(mLayoutManager);
        lv_notifications.addOnScrollListener(new RecyclerView.OnScrollListener() {
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                if(dy > 0) {
                    int visibleItemCount = mLayoutManager.getChildCount();
                    int totalItemCount = mLayoutManager.getItemCount();
                    int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                    if (firstVisibleItem + visibleItemCount == totalItemCount && context != null) {
                        if (!flag_loading) {
                            flag_loading = true;
                            asyncTask = new RetrievePeertubeNotificationsAsyncTask(context,  null,  max_id,   DisplayPeertubeNotificationsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
                firstLoad = true;
                flag_loading = true;
                swiped = true;
                try {
                    ((MainActivity) context).updateNotifCounter();
                }catch (Exception ignored){}
                if( context != null) {
                    asyncTask = new RetrievePeertubeNotificationsAsyncTask(context, null, max_id, DisplayPeertubeNotificationsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
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
        if( context != null)
            asyncTask = new RetrievePeertubeNotificationsAsyncTask(context, null,  max_id,  DisplayPeertubeNotificationsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if( context != null)
                        asyncTask = new RetrievePeertubeNotificationsAsyncTask(context, null,  max_id,  DisplayPeertubeNotificationsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }, 500);
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



    public void scrollToTop(){
        if( lv_notifications != null)
            lv_notifications.setAdapter(notificationsListAdapter);
        //Store last toot id for home timeline to avoid to notify for those that have been already seen
        if (this.notifications != null && this.notifications.size() > 0) {
            updateNotificationLastId(this.notifications.get(0).getId());
        }
    }

    public void refreshAll(){
        if( context == null)
            return;
        max_id = null;
        firstLoad = true;
        flag_loading = true;
        swiped = true;
        asyncTask = new RetrievePeertubeNotificationsAsyncTask(context, null,  null,   DisplayPeertubeNotificationsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }








    /**
     * Records the id of the notification only if its greater than the previous one.
     * @param notificationId String current notification id to check
     */
    private void updateNotificationLastId(String notificationId){

        String lastNotif = sharedpreferences.getString(Helper.LAST_NOTIFICATION_MAX_ID + userId + instance, null);
        if( lastNotif == null || Long.parseLong(notificationId) > Long.parseLong(lastNotif)){
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(Helper.LAST_NOTIFICATION_MAX_ID + userId + instance, notificationId);
            editor.apply();
        }
    }

    @Override
    public void onRetrievePeertubeNotifications(APIResponse apiResponse, Account account) {
        mainLoader.setVisibility(View.GONE);
        nextElementLoader.setVisibility(View.GONE);
        String lastReadNotifications = sharedpreferences.getString(Helper.LAST_NOTIFICATION_MAX_ID + userId + instance, null);
        if( apiResponse.getError() != null){
            Toasty.error(context, apiResponse.getError().getError(),Toast.LENGTH_LONG).show();
            flag_loading = false;
            swipeRefreshLayout.setRefreshing(false);
            swiped = false;
            return;
        }

        int previousPosition = notifications.size();
        max_id = apiResponse.getMax_id();
        List<PeertubeNotification> notifications = apiResponse.getPeertubeNotifications();

        if( !swiped && firstLoad && (notifications == null || notifications.size() == 0))
            textviewNoAction.setVisibility(View.VISIBLE);
        else
            textviewNoAction.setVisibility(View.GONE);
        if( swiped ){
            if (previousPosition > 0) {
                for (int i = 0; i < previousPosition; i++) {
                    this.notifications.remove(0);
                }
                notificationsListAdapter.notifyItemRangeRemoved(0, previousPosition);
            }
            swiped = false;
        }

        if( notifications != null && notifications.size() > 0) {
            for(PeertubeNotification tmpNotification: notifications){

                try {
                    ((MainActivity) context).updateNotifCounter();
                }catch (Exception ignored){}
                this.notifications.add(tmpNotification);
            }
            if( firstLoad) {
                //Update the id of the last notification retrieved
                if( MainActivity.lastNotificationId == null || Long.parseLong(notifications.get(0).getId()) > Long.parseLong(MainActivity.lastNotificationId))
                    MainActivity.lastNotificationId = notifications.get(0).getId();
                updateNotificationLastId(notifications.get(0).getId());
            }
            notificationsListAdapter.notifyItemRangeInserted(previousPosition, notifications.size());
        }else {
            if( firstLoad)
                textviewNoAction.setVisibility(View.VISIBLE);
        }
        if( firstLoad )
            ((MainActivity)context).updateNotifCounter();
        swipeRefreshLayout.setRefreshing(false);
        firstLoad = false;
        //The initial call comes from a classic tab refresh
        flag_loading = (max_id == null );
    }
}
