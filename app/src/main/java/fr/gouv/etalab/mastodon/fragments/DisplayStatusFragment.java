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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import fr.gouv.etalab.mastodon.activities.MainActivity;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveMissingFeedsAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveRepliesAsyncTask;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.drawers.StatusListAdapter;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveMissingFeedsInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveRepliesInterface;
import fr.gouv.etalab.mastodon.services.StreamingFederatedTimelineService;
import mastodon.etalab.gouv.fr.mastodon.R;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveFeedsAsyncTask;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveFeedsInterface;


/**
 * Created by Thomas on 24/04/2017.
 * Fragment to display content related to status
 */
public class DisplayStatusFragment extends Fragment implements OnRetrieveFeedsInterface, OnRetrieveRepliesInterface, OnRetrieveMissingFeedsInterface {


    private boolean flag_loading;
    private Context context;
    private AsyncTask<Void, Void, Void> asyncTask;
    private StatusListAdapter statusListAdapter;
    private String max_id;
    private List<Status> statuses;
    private RetrieveFeedsAsyncTask.Type type;
    private RelativeLayout mainLoader, nextElementLoader, textviewNoAction;
    private boolean firstLoad;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String targetedId;
    private String tag;
    private boolean swiped;
    private ListView lv_status;
    private boolean isOnWifi;
    private int behaviorWithAttachments;
    private boolean showMediaOnly, showPinned;
    private int positionSpinnerTrans;
    private boolean hideHeader;
    private String instanceValue;
    private String lastReadStatus;
    private Intent streamingFederatedIntent;
    private Date lastRefreshPublic;

    public DisplayStatusFragment(){
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_status, container, false);
        statuses = new ArrayList<>();
        context = getContext();
        Bundle bundle = this.getArguments();
        boolean comesFromSearch = false;
        hideHeader = false;
        showMediaOnly = false;
        showPinned = false;
        if (bundle != null) {
            type = (RetrieveFeedsAsyncTask.Type) bundle.get("type");
            targetedId = bundle.getString("targetedId", null);
            tag = bundle.getString("tag", null);
            instanceValue = bundle.getString("hideHeaderValue", null);
            hideHeader = bundle.getBoolean("hideHeader", false);
            showMediaOnly = bundle.getBoolean("showMediaOnly",false);
            showPinned = bundle.getBoolean("showPinned",false);
            if( bundle.containsKey("statuses")){
                ArrayList<Parcelable> statusesReceived = bundle.getParcelableArrayList("statuses");
                assert statusesReceived != null;
                for(Parcelable status: statusesReceived){
                    statuses.add((Status) status);
                }
                comesFromSearch = true;
            }
        }
        max_id = null;
        flag_loading = true;
        firstLoad = true;
        swiped = false;

        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        isOnWifi = Helper.isOnWIFI(context);
        positionSpinnerTrans = sharedpreferences.getInt(Helper.SET_TRANSLATOR, Helper.TRANS_YANDEX);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeContainer);
        behaviorWithAttachments = sharedpreferences.getInt(Helper.SET_ATTACHMENT_ACTION, Helper.ATTACHMENT_ALWAYS);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        if( type == RetrieveFeedsAsyncTask.Type.HOME)
            lastReadStatus = sharedpreferences.getString(Helper.LAST_HOMETIMELINE_MAX_ID + userId, null);
        lv_status = (ListView) rootView.findViewById(R.id.lv_status);
        mainLoader = (RelativeLayout) rootView.findViewById(R.id.loader);
        nextElementLoader = (RelativeLayout) rootView.findViewById(R.id.loading_next_status);
        textviewNoAction = (RelativeLayout) rootView.findViewById(R.id.no_action);
        mainLoader.setVisibility(View.VISIBLE);
        nextElementLoader.setVisibility(View.GONE);
        statusListAdapter = new StatusListAdapter(context, type, targetedId, isOnWifi, behaviorWithAttachments, positionSpinnerTrans, this.statuses);
        lv_status.setAdapter(statusListAdapter);
        if( !comesFromSearch){

            //Hide account header when scrolling for ShowAccountActivity
            if (hideHeader && Build.VERSION.SDK_INT >= 21)
                ViewCompat.setNestedScrollingEnabled(lv_status, true);

            lv_status.setOnScrollListener(new AbsListView.OnScrollListener() {
                int lastFirstVisibleItem = 0;
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {

                }
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (hideHeader && Build.VERSION.SDK_INT < 21) {
                        if(firstVisibleItem == 0 && Helper.listIsAtTop(lv_status)){
                            Intent intent = new Intent(Helper.HEADER_ACCOUNT+instanceValue);
                            intent.putExtra("hide", false);
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                        }else if (view.getId() == lv_status.getId() && totalItemCount > visibleItemCount) {
                            final int currentFirstVisibleItem = lv_status.getFirstVisiblePosition();
                            if (currentFirstVisibleItem > lastFirstVisibleItem) {
                                Intent intent = new Intent(Helper.HEADER_ACCOUNT + instanceValue);
                                intent.putExtra("hide", true);
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                            } else if (currentFirstVisibleItem < lastFirstVisibleItem) {
                                Intent intent = new Intent(Helper.HEADER_ACCOUNT + instanceValue);
                                intent.putExtra("hide", false);
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                            }
                            lastFirstVisibleItem = currentFirstVisibleItem;
                        }
                    }
                    if(firstVisibleItem + visibleItemCount == totalItemCount ) {
                        if(!flag_loading ) {
                            if( type == RetrieveFeedsAsyncTask.Type.PUBLIC)
                                lastRefreshPublic = new Date();
                            flag_loading = true;
                            if( type == RetrieveFeedsAsyncTask.Type.USER)
                                asyncTask = new RetrieveFeedsAsyncTask(context, type, targetedId, max_id, showMediaOnly, showPinned, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            else if( type == RetrieveFeedsAsyncTask.Type.TAG)
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


            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    max_id = null;
                    statuses = new ArrayList<>();
                    firstLoad = true;
                    flag_loading = true;
                    swiped = true;
                    MainActivity.countNewStatus = 0;
                    if( type == RetrieveFeedsAsyncTask.Type.PUBLIC)
                        lastRefreshPublic = new Date();
                    if( type == RetrieveFeedsAsyncTask.Type.USER)
                        asyncTask = new RetrieveFeedsAsyncTask(context, type, targetedId, max_id, showMediaOnly, showPinned, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    else if( type == RetrieveFeedsAsyncTask.Type.TAG)
                        asyncTask = new RetrieveFeedsAsyncTask(context, type, tag, targetedId, max_id, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    else
                        asyncTask = new RetrieveFeedsAsyncTask(context, type, max_id, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            });
            swipeRefreshLayout.setColorSchemeResources(R.color.mastodonC4,
                    R.color.mastodonC2,
                    R.color.mastodonC3);
            if( type == RetrieveFeedsAsyncTask.Type.PUBLIC)
                lastRefreshPublic = new Date();
            if( type == RetrieveFeedsAsyncTask.Type.USER)
                asyncTask = new RetrieveFeedsAsyncTask(context, type, targetedId, max_id, showMediaOnly, showPinned, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            else if( type == RetrieveFeedsAsyncTask.Type.TAG)
                asyncTask = new RetrieveFeedsAsyncTask(context, type, tag, targetedId, max_id, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            else {
                asyncTask = new RetrieveFeedsAsyncTask(context, type, max_id, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }else {
            statusListAdapter.notifyDataSetChanged();
            mainLoader.setVisibility(View.GONE);
            nextElementLoader.setVisibility(View.GONE);
            if( statuses == null || statuses.size() == 0 )
                textviewNoAction.setVisibility(View.VISIBLE);
        }

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

    @Override
    public void onDestroy (){
        super.onDestroy();
        if(asyncTask != null && asyncTask.getStatus() == AsyncTask.Status.RUNNING)
            asyncTask.cancel(true);
    }



    @Override
    public void onRetrieveFeeds(APIResponse apiResponse) {
        mainLoader.setVisibility(View.GONE);
        nextElementLoader.setVisibility(View.GONE);
        //Discards 404 - error which can often happen due to toots which have been deleted
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        if( apiResponse.getError() != null && !apiResponse.getError().getError().startsWith("404 -")){
            boolean show_error_messages = sharedpreferences.getBoolean(Helper.SET_SHOW_ERROR_MESSAGES, true);
            if( show_error_messages)
                Toast.makeText(context, apiResponse.getError().getError(),Toast.LENGTH_LONG).show();
            swipeRefreshLayout.setRefreshing(false);
            swiped = false;
            flag_loading = false;
            return;
        }
        List<Status> statuses = apiResponse.getStatuses();
        max_id = apiResponse.getMax_id();

        flag_loading = (max_id == null );
        if( !swiped && firstLoad && (statuses == null || statuses.size() == 0))
            textviewNoAction.setVisibility(View.VISIBLE);
        else
            textviewNoAction.setVisibility(View.GONE);
        if( swiped ){
            statusListAdapter = new StatusListAdapter(context, type, targetedId, isOnWifi, behaviorWithAttachments, positionSpinnerTrans, this.statuses);
            lv_status.setAdapter(statusListAdapter);
            swiped = false;
        }

        if( statuses != null && statuses.size() > 0) {
            ArrayList<String> knownId = new ArrayList<>();
            for(Status st: this.statuses){
                knownId.add(st.getId());
            }
            for(Status tmpStatus: statuses){
                if( !knownId.contains(tmpStatus.getId())) {
                    if( type == RetrieveFeedsAsyncTask.Type.HOME && firstLoad && lastReadStatus != null && Long.parseLong(tmpStatus.getId()) > Long.parseLong(lastReadStatus)){
                        tmpStatus.setNew(true);
                        MainActivity.countNewStatus++;
                    }else {
                        tmpStatus.setNew(false);
                    }
                    this.statuses.add(tmpStatus);
                }
            }

            if( firstLoad && type == RetrieveFeedsAsyncTask.Type.HOME) {
                //Update the id of the last toot retrieved
                MainActivity.lastHomeId = statuses.get(0).getId();
                SharedPreferences.Editor editor = sharedpreferences.edit();
                String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
                editor.putString(Helper.LAST_HOMETIMELINE_MAX_ID + userId, statuses.get(0).getId());
                editor.apply();
                lastReadStatus = statuses.get(0).getId();
            }
            statusListAdapter.notifyDataSetChanged();
            if( firstLoad && type == RetrieveFeedsAsyncTask.Type.HOME)
            //Display new value in counter
            try {
                ((MainActivity) context).updateHomeCounter();
            }catch (Exception ignored){}
        }
        swipeRefreshLayout.setRefreshing(false);
        firstLoad = false;

        //Retrieves replies
        if(statuses != null && statuses.size()  > 0 && type == RetrieveFeedsAsyncTask.Type.HOME ) {
            boolean showPreview = sharedpreferences.getBoolean(Helper.SET_PREVIEW_REPLIES, false);
            //Retrieves attached replies to a toot
            if (showPreview) {
                new RetrieveRepliesAsyncTask(context, statuses, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    /**
     * Deals with new status coming from the streaming api
     * @param status Status
     */
    public void refresh(Status status){
        //New data are available
        if( type == RetrieveFeedsAsyncTask.Type.HOME) {
            if (context == null)
                return;
            if (status != null) {
                //Update the id of the last toot retrieved
                MainActivity.lastHomeId = status.getId();
                int index = lv_status.getFirstVisiblePosition() + 1;
                View v = lv_status.getChildAt(0);
                int top = (v == null) ? 0 : v.getTop();
                status.setReplies(new ArrayList<Status>());
                statuses.add(0,status);
                SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
                if( !status.getAccount().getId().equals(userId))
                    MainActivity.countNewStatus++;
                statusListAdapter.notifyDataSetChanged();
                lv_status.setSelectionFromTop(index, top);
                if (textviewNoAction.getVisibility() == View.VISIBLE)
                    textviewNoAction.setVisibility(View.GONE);
            }
        }else if(type == RetrieveFeedsAsyncTask.Type.PUBLIC){
            if (context == null)
                return;
            //Avoids the array to be too big...

            if (status != null) {

                if (lv_status.getFirstVisiblePosition() < 3) {
                    if( statuses.size() >  40)
                        statuses.subList(0, 40).clear();
                    status.setReplies(new ArrayList<Status>());
                    status.setNew(false);
                    statuses.add(0, status);
                    statusListAdapter.notifyDataSetChanged();
                } else {
                    status.setReplies(new ArrayList<Status>());
                    statuses.add(0, status);
                }
                if (textviewNoAction.getVisibility() == View.VISIBLE)
                    textviewNoAction.setVisibility(View.GONE);
            }
        }

    }

    @Override
    public void onResume(){
        super.onResume();
        if( type == RetrieveFeedsAsyncTask.Type.PUBLIC){
            if( isVisible() ){
                SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
                editor.putBoolean(Helper.SHOULD_CONTINUE_STREAMING_FEDERATED+userId, true);
                editor.apply();
                streamingFederatedIntent = new Intent(context, StreamingFederatedTimelineService.class);
                context.startService(streamingFederatedIntent);
            }

            Calendar date = Calendar.getInstance();
            long t = date.getTimeInMillis();
            Date newDate = new Date(t - TimeUnit.SECONDS.toMillis(20));
            if( lastRefreshPublic.before(newDate)){
                lastRefreshPublic = new Date();
                max_id = null;
                statuses = new ArrayList<>();
                firstLoad = true;
                flag_loading = true;
                swiped = true;
                asyncTask = new RetrieveFeedsAsyncTask(context, type, null, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }

        }
    }

    /**
     * Called from main activity in onResume to retrieve missing toots (home timeline)
     * @param sinceId String
     */
    public void retrieveMissingToots(String sinceId){
        asyncTask = new RetrieveMissingFeedsAsyncTask(context, sinceId, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * When tab comes visible, first displayed toot is defined as read
     * @param visible boolean
     */
    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if( context == null)
            return;
        //Store last toot id for home timeline to avoid to notify for those that have been already seen
        if (type == RetrieveFeedsAsyncTask.Type.HOME && visible && statuses != null && statuses.size() > 0) {
            SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
            editor.putString(Helper.LAST_HOMETIMELINE_MAX_ID + userId, statuses.get(0).getId());
            lastReadStatus = statuses.get(0).getId();
            editor.apply();
        }
        if( type == RetrieveFeedsAsyncTask.Type.PUBLIC ){
            if (visible) {
                SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
                editor.putBoolean(Helper.SHOULD_CONTINUE_STREAMING_FEDERATED+userId, true);
                editor.apply();
                streamingFederatedIntent = new Intent(context, StreamingFederatedTimelineService.class);
                context.startService(streamingFederatedIntent);
            }else {
                if( streamingFederatedIntent != null){
                    SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
                    editor.putBoolean(Helper.SHOULD_CONTINUE_STREAMING_FEDERATED+userId, false);
                    editor.apply();
                }
            }
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        if( type == RetrieveFeedsAsyncTask.Type.PUBLIC && streamingFederatedIntent != null){
            SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
            editor.putBoolean(Helper.SHOULD_CONTINUE_STREAMING_FEDERATED+userId, false);
            editor.apply();
        }
    }

    public void scrollToTop(){
        if( lv_status != null) {
            lv_status.setAdapter(statusListAdapter);
            //Store last toot id for home timeline to avoid to notify for those that have been already seen
            if (type == RetrieveFeedsAsyncTask.Type.HOME && statuses != null && statuses.size() > 0) {
                SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
                editor.putString(Helper.LAST_HOMETIMELINE_MAX_ID + userId, statuses.get(0).getId());
                lastReadStatus = statuses.get(0).getId();
                editor.apply();
            }
        }
    }

    @Override
    public void onRetrieveReplies(APIResponse apiResponse) {
        if( apiResponse.getError() != null || apiResponse.getStatuses() == null || apiResponse.getStatuses().size() == 0){
            return;
        }
        List<Status> modifiedStatus = apiResponse.getStatuses();
        for(Status stmp: modifiedStatus){
            for(Status status: statuses){
                if( status.getId().equals(stmp.getId()))
                    if( stmp.getReplies() != null )
                        status.setReplies(stmp.getReplies());
                    else
                        status.setReplies(new ArrayList<Status>());
            }
        }
        statusListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRetrieveMissingFeeds(List<Status> statuses) {
        if( statuses != null && statuses.size() > 0) {
            ArrayList<String> knownId = new ArrayList<>();
            for (Status st : this.statuses) {
                knownId.add(st.getId());
            }
            for (int i = statuses.size()-1 ; i >= 0 ; i--) {
                if (!knownId.contains(statuses.get(i).getId())) {
                    statuses.get(i).setNew(true);
                    MainActivity.countNewStatus++;
                    this.statuses.add(0, statuses.get(i));
                }
            }
            statusListAdapter.notifyDataSetChanged();
            try {
                ((MainActivity) context).updateHomeCounter();
            }catch (Exception ignored){}
        }
    }
}
