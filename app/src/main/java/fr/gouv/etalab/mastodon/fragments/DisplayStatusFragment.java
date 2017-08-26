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
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

import fr.gouv.etalab.mastodon.activities.MainActivity;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveRepliesAsyncTask;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.drawers.StatusListAdapter;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveRepliesInterface;
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
public class DisplayStatusFragment extends Fragment implements OnRetrieveFeedsInterface, OnRetrieveRepliesInterface {


    private boolean flag_loading;
    private Context context;
    private AsyncTask<Void, Void, Void> asyncTask;
    private StatusListAdapter statusListAdapter;
    private String max_id;
    private List<Status> statuses, statusesTmp;
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
    private boolean showMediaOnly;
    private DisplayStatusFragment displayStatusFragment;
    private TextView new_data;
    private int positionSpinnerTrans;
    private String since_id;

    public DisplayStatusFragment(){
        displayStatusFragment = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_status, container, false);
        statuses = new ArrayList<>();
        context = getContext();
        Bundle bundle = this.getArguments();
        boolean comesFromSearch = false;
        boolean hideHeader = false;
        showMediaOnly = false;
        if (bundle != null) {
            type = (RetrieveFeedsAsyncTask.Type) bundle.get("type");
            targetedId = bundle.getString("targetedId", null);
            tag = bundle.getString("tag", null);
            hideHeader = bundle.getBoolean("hideHeader", false);
            showMediaOnly = bundle.getBoolean("showMediaOnly",false);
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

        lv_status = (ListView) rootView.findViewById(R.id.lv_status);
        mainLoader = (RelativeLayout) rootView.findViewById(R.id.loader);
        nextElementLoader = (RelativeLayout) rootView.findViewById(R.id.loading_next_status);
        textviewNoAction = (RelativeLayout) rootView.findViewById(R.id.no_action);
        mainLoader.setVisibility(View.VISIBLE);
        nextElementLoader.setVisibility(View.GONE);
        statusListAdapter = new StatusListAdapter(context, type, targetedId, isOnWifi, behaviorWithAttachments, positionSpinnerTrans, this.statuses);
        lv_status.setAdapter(statusListAdapter);
        new_data = (TextView) rootView.findViewById(R.id.new_data);
        if( !comesFromSearch){

            //Hide account header when scrolling for ShowAccountActivity
            if(hideHeader) {
                ViewCompat.setNestedScrollingEnabled(lv_status,true);
            }else{
                lv_status.setOnScrollListener(new AbsListView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {

                    }
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                        if(firstVisibleItem + visibleItemCount == totalItemCount ) {
                            if(!flag_loading ) {
                                flag_loading = true;
                                if( type == RetrieveFeedsAsyncTask.Type.USER)
                                    asyncTask = new RetrieveFeedsAsyncTask(context, type, targetedId, max_id, showMediaOnly, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
            }

            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    max_id = null;
                    statuses = new ArrayList<>();
                    firstLoad = true;
                    flag_loading = true;
                    swiped = true;
                    new_data.setVisibility(View.GONE);
                    if( type == RetrieveFeedsAsyncTask.Type.USER)
                        asyncTask = new RetrieveFeedsAsyncTask(context, type, targetedId, max_id, showMediaOnly, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    else if( type == RetrieveFeedsAsyncTask.Type.TAG)
                        asyncTask = new RetrieveFeedsAsyncTask(context, type, tag, targetedId, max_id, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    else
                        asyncTask = new RetrieveFeedsAsyncTask(context, type, max_id, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            });
            swipeRefreshLayout.setColorSchemeResources(R.color.mastodonC4,
                    R.color.mastodonC2,
                    R.color.mastodonC3);


            if( type == RetrieveFeedsAsyncTask.Type.USER)
                asyncTask = new RetrieveFeedsAsyncTask(context, type, targetedId, max_id, showMediaOnly, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            else if( type == RetrieveFeedsAsyncTask.Type.TAG)
                asyncTask = new RetrieveFeedsAsyncTask(context, type, tag, targetedId, max_id, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            else
                asyncTask = new RetrieveFeedsAsyncTask(context, type, max_id, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }else {
            statusListAdapter.notifyDataSetChanged();
            mainLoader.setVisibility(View.GONE);
            nextElementLoader.setVisibility(View.GONE);
            if( statuses == null || statuses.size() == 0 )
                textviewNoAction.setVisibility(View.VISIBLE);
        }

        new_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( statusesTmp != null){
                    boolean isOnWifi = Helper.isOnWIFI(context);
                    int behaviorWithAttachments = sharedpreferences.getInt(Helper.SET_ATTACHMENT_ACTION, Helper.ATTACHMENT_ALWAYS);
                    statuses = new ArrayList<>();
                    for(Status status: statusesTmp){
                        statuses.add(status);
                    }
                    //The user clicked on the banner to refresh values so, the pointer is changed
                    if( statusesTmp.size() > 0 ) {
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
                        editor.putString(Helper.LAST_MAX_ID_BUBBLE_HOME + userId, statusesTmp.get(0).getId());
                        editor.apply();
                    }
                    if( statusesTmp.size() > 0 && textviewNoAction.getVisibility() == View.VISIBLE)
                        textviewNoAction.setVisibility(View.GONE);
                    statusListAdapter = new StatusListAdapter(context, type, targetedId, isOnWifi, behaviorWithAttachments, positionSpinnerTrans, statuses);
                    lv_status.setAdapter(statusListAdapter);
                    statusesTmp = new ArrayList<>();
                }
                new_data.setVisibility(View.GONE);
            }
        });

        return rootView;
    }


    @Override
    public void onCreate(Bundle saveInstance)
    {
        super.onCreate(saveInstance);
    }



    @Override
    public void onResume() {
        super.onResume();
        //New data are available
        if (getUserVisibleHint() && statusesTmp != null && statusesTmp.size() > 0 ) {
            final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            boolean isOnWifi = Helper.isOnWIFI(context);
            int behaviorWithAttachments = sharedpreferences.getInt(Helper.SET_ATTACHMENT_ACTION, Helper.ATTACHMENT_ALWAYS);
            int positionSpinnerTrans = sharedpreferences.getInt(Helper.SET_TRANSLATOR, Helper.TRANS_YANDEX);
            statuses = new ArrayList<>();
            for(Status status: statusesTmp){
                statuses.add(status);
            }
            //The user clicked on the tab to refresh values so, the pointer is changed
            SharedPreferences.Editor editor = sharedpreferences.edit();
            String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
            editor.putString(Helper.LAST_MAX_ID_BUBBLE_HOME + userId, statusesTmp.get(0).getId());
            editor.apply();
            statusListAdapter = new StatusListAdapter(context, type, targetedId, isOnWifi, behaviorWithAttachments, positionSpinnerTrans, statuses);
            lv_status.setAdapter(statusListAdapter);
            statusesTmp = new ArrayList<>();
        }
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
    public void onRetrieveFeeds(APIResponse apiResponse, boolean refreshData) {
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
        since_id = apiResponse.getSince_id();
        max_id = apiResponse.getMax_id();
        //Special case for home timeline
        if( type == RetrieveFeedsAsyncTask.Type.HOME){
            //Retrieves some values
            SharedPreferences.Editor editor = sharedpreferences.edit();
            String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
            String bubble_max_id = sharedpreferences.getString(Helper.LAST_MAX_ID_BUBBLE_HOME + userId, null);
            //The initial call comes from a classic tab refresh

            if( refreshData ) {

                manageStatus(statuses, max_id, since_id);
                //The current tab is displayed, so user is supposed to have seen status
                if( since_id != null && displayStatusFragment.getUserVisibleHint()) {
                    editor.putString(Helper.LAST_MAX_ID_BUBBLE_HOME + userId,  since_id);
                    editor.apply();
                }else if(!displayStatusFragment.getUserVisibleHint()){
                    //Current fragment was loaded but not displayed to the user.
                    //So the bubble counter will be displayed
                    int countData = 0;
                    //Retrieves new status count
                    if( bubble_max_id != null) {
                        for (Status st : statuses) {
                            if (st.getId().trim().equals(bubble_max_id.trim()))
                                break;
                            countData++;
                        }
                    }
                    ((MainActivity)context).updateHomeCounter(countData);
                }
            }else { //Here, new values have been retrieved on the onResume call (forced mode)
                int countData = 0;
                if( bubble_max_id != null) {
                    for (Status st : statuses) {
                        if (st.getId().trim().equals(bubble_max_id.trim()))
                            break;
                        countData++;
                    }
                }

                if( statuses != null && statuses.size() > 0 && countData > 0) {
                    max_id = null;
                    firstLoad = true;
                    statusesTmp = new ArrayList<>();
                    for (Status tmpStatus : statuses) {
                        this.statusesTmp.add(tmpStatus);
                    }
                    //New status will be counted
                    //The fragment is not displayed, so the bubble counter should be shown
                    if (!displayStatusFragment.getUserVisibleHint()) {
                        ((MainActivity) context).updateHomeCounter(countData);
                    } else {
                        //The current fragment is visible, but for avoiding to populate with new values
                        //Values are put in temp and the banned is displayed
                        new_data.setVisibility(View.VISIBLE);
                    }
                }
            }
        }else {
            manageStatus(statuses, max_id, since_id);
        }



    }

    private void manageStatus(List<Status> statuses, String max_id, String since_id){
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
            for(Status tmpStatus: statuses){
                this.statuses.add(tmpStatus);
            }
            statusListAdapter.notifyDataSetChanged();
        }
        swipeRefreshLayout.setRefreshing(false);

        //Store last toot id for home timeline to avoid to notify for those that have been already seen
        if(statuses != null && statuses.size()  > 0 && type == RetrieveFeedsAsyncTask.Type.HOME ){
            final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            //acct is null when used in Fragment, data need to be retrieved via shared preferences and db
            String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
            SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
            Account currentAccount = new AccountDAO(context, db).getAccountByID(userId);
            if( currentAccount != null && firstLoad && since_id != null){
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(Helper.LAST_HOMETIMELINE_MAX_ID + currentAccount.getId(), since_id);
                editor.apply();
            }
        }
        firstLoad = false;

        //Retrieves replies
        if(statuses != null && statuses.size()  > 0 && type == RetrieveFeedsAsyncTask.Type.HOME ) {
            final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            boolean showPreview = sharedpreferences.getBoolean(Helper.SET_PREVIEW_REPLIES, true);
            //Retrieves attached replies to a toot
            if (showPreview) {
                new RetrieveRepliesAsyncTask(context, statuses, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    public void scrollToTop(){
        if( lv_status != null)
            lv_status.setAdapter(statusListAdapter);
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
                    status.setReplies(stmp.getReplies());
            }
        }
        statusListAdapter.notifyDataSetChanged();
    }
    public void update() {
        if( context != null) {
            asyncTask = new RetrieveFeedsAsyncTask(context, type, null, false, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void refreshData(){
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        if(context != null && this.statusesTmp != null && this.statusesTmp.size() > 0){

            boolean isOnWifi = Helper.isOnWIFI(context);
            int behaviorWithAttachments = sharedpreferences.getInt(Helper.SET_ATTACHMENT_ACTION, Helper.ATTACHMENT_ALWAYS);
            int positionSpinnerTrans = sharedpreferences.getInt(Helper.SET_TRANSLATOR, Helper.TRANS_YANDEX);

            statuses = new ArrayList<>();
            for(Status status: statusesTmp){
                statuses.add(status);
            }
            if( textviewNoAction.getVisibility() == View.VISIBLE)
                textviewNoAction.setVisibility(View.GONE);
            statusListAdapter = new StatusListAdapter(context, type, targetedId, isOnWifi, behaviorWithAttachments, positionSpinnerTrans, statuses);
            lv_status.setAdapter(statusListAdapter);
            statusesTmp = new ArrayList<>();
        }
        if( since_id != null){
            //The user clicked on the tab to refresh values so, the pointer is changed
            SharedPreferences.Editor editor = sharedpreferences.edit();
            String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
            editor.putString(Helper.LAST_MAX_ID_BUBBLE_HOME + userId, since_id);
            editor.apply();
        }
    }
}
