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
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.activities.BaseMainActivity;
import fr.gouv.etalab.mastodon.activities.MainActivity;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveMissingFeedsAsyncTask;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.drawers.StatusListAdapter;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveMissingFeedsInterface;
import fr.gouv.etalab.mastodon.services.StreamingFederatedTimelineService;
import fr.gouv.etalab.mastodon.services.StreamingLocalTimelineService;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveFeedsAsyncTask;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveFeedsInterface;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import fr.gouv.etalab.mastodon.sqlite.TempMuteDAO;


/**
 * Created by Thomas on 24/04/2017.
 * Fragment to display content related to status
 */
public class DisplayStatusFragment extends Fragment implements OnRetrieveFeedsInterface, OnRetrieveMissingFeedsInterface {


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
    private RecyclerView lv_status;
    private boolean showMediaOnly, showPinned;
    private Intent streamingFederatedIntent, streamingLocalIntent;
    LinearLayoutManager mLayoutManager;
    boolean firstTootsLoaded;
    private String userId, instance;
    private SharedPreferences sharedpreferences;


    public DisplayStatusFragment(){
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_status, container, false);
        statuses = new ArrayList<>();
        context = getContext();
        Bundle bundle = this.getArguments();
        showMediaOnly = false;
        //Will allow to load first toots if bookmark != null
        firstTootsLoaded = true;
        showPinned = false;
        if (bundle != null) {
            type = (RetrieveFeedsAsyncTask.Type) bundle.get("type");
            targetedId = bundle.getString("targetedId", null);
            tag = bundle.getString("tag", null);
            showMediaOnly = bundle.getBoolean("showMediaOnly",false);
            showPinned = bundle.getBoolean("showPinned",false);
        }
        max_id = null;
        flag_loading = true;
        firstLoad = true;
        assert context != null;
        sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        boolean isOnWifi = Helper.isOnWIFI(context);
        int positionSpinnerTrans = sharedpreferences.getInt(Helper.SET_TRANSLATOR, Helper.TRANS_YANDEX);
        swipeRefreshLayout = rootView.findViewById(R.id.swipeContainer);
        int behaviorWithAttachments = sharedpreferences.getInt(Helper.SET_ATTACHMENT_ACTION, Helper.ATTACHMENT_ALWAYS);
        lv_status = rootView.findViewById(R.id.lv_status);
        mainLoader =  rootView.findViewById(R.id.loader);
        nextElementLoader = rootView.findViewById(R.id.loading_next_status);
        textviewNoAction =  rootView.findViewById(R.id.no_action);
        mainLoader.setVisibility(View.VISIBLE);
        nextElementLoader.setVisibility(View.GONE);

        userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        if( type == RetrieveFeedsAsyncTask.Type.HOME) {
            SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
            Account account = new AccountDAO(context, db).getAccountByID(userId);
            List<String> mutedAccount = new TempMuteDAO(context, db).getAllTimeMuted(account);
            statusListAdapter = new StatusListAdapter(context,mutedAccount, type, targetedId, isOnWifi, behaviorWithAttachments, positionSpinnerTrans, this.statuses);
        }else{
            statusListAdapter = new StatusListAdapter(context, type, targetedId, isOnWifi, behaviorWithAttachments, positionSpinnerTrans, this.statuses);
        }


        lv_status.setAdapter(statusListAdapter);
        mLayoutManager = new LinearLayoutManager(context);
        lv_status.setLayoutManager(mLayoutManager);


        instance = sharedpreferences.getString(Helper.PREF_INSTANCE, context!=null?Helper.getLiveInstance(context):null);

        lv_status.addOnScrollListener(new RecyclerView.OnScrollListener() {
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                if(dy > 0){
                    int visibleItemCount = mLayoutManager.getChildCount();
                    int totalItemCount = mLayoutManager.getItemCount();
                    if(firstVisibleItem + visibleItemCount == totalItemCount && context != null) {
                        if(!flag_loading ) {
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
                if(type == RetrieveFeedsAsyncTask.Type.HOME && statuses != null && statuses.size() > firstVisibleItem && firstVisibleItem >= 0)
                    if( context instanceof BaseMainActivity){
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        Long bookmarkL = Long.parseLong(statuses.get(firstVisibleItem).getId()) + 1;
                        editor.putString(Helper.BOOKMARK_ID + userId + instance, String.valueOf(bookmarkL));
                        editor.apply();
                    }
            }
        });


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if( statuses.size() > 0)
                    retrieveMissingToots(statuses.get(0).getId());
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.mastodonC4,
                R.color.mastodonC2,
                R.color.mastodonC3);
        if( context != null) {
            if (type == RetrieveFeedsAsyncTask.Type.USER)
                asyncTask = new RetrieveFeedsAsyncTask(context, type, targetedId, max_id, showMediaOnly, showPinned, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            else if (type == RetrieveFeedsAsyncTask.Type.TAG)
                asyncTask = new RetrieveFeedsAsyncTask(context, type, tag, targetedId, max_id, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            else {
                if( type == RetrieveFeedsAsyncTask.Type.HOME ){
                    String bookmark;
                    if( context instanceof BaseMainActivity){
                        bookmark = ((BaseMainActivity) context).getBookmark();
                        asyncTask = new RetrieveFeedsAsyncTask(context, type, bookmark, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        firstTootsLoaded = false;

                    }
                }else {
                    asyncTask = new RetrieveFeedsAsyncTask(context, type, null, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        }else {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if( context != null){
                        if (type == RetrieveFeedsAsyncTask.Type.USER)
                            asyncTask = new RetrieveFeedsAsyncTask(context, type, targetedId, max_id, showMediaOnly, showPinned, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        else if (type == RetrieveFeedsAsyncTask.Type.TAG)
                            asyncTask = new RetrieveFeedsAsyncTask(context, type, tag, targetedId, max_id, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        else {
                            if( type == RetrieveFeedsAsyncTask.Type.HOME ){
                                String bookmark;
                                if( context instanceof BaseMainActivity){
                                    bookmark = ((BaseMainActivity) context).getBookmark();
                                    asyncTask = new RetrieveFeedsAsyncTask(context, type, bookmark, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                    firstTootsLoaded = false;
                                }
                            }else {
                                asyncTask = new RetrieveFeedsAsyncTask(context, type, null, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            }
                        }
                    }
                }
            }, 500);
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
        if( apiResponse.getError() != null && apiResponse.getError().getStatusCode() != 404 ){
            boolean show_error_messages = sharedpreferences.getBoolean(Helper.SET_SHOW_ERROR_MESSAGES, true);
            if( show_error_messages )
                Toast.makeText(context, apiResponse.getError().getError(),Toast.LENGTH_LONG).show();
            swipeRefreshLayout.setRefreshing(false);
            flag_loading = false;
            return;
        }
        int previousPosition = this.statuses.size();
        List<Status> statuses = apiResponse.getStatuses();
        if( type == RetrieveFeedsAsyncTask.Type.HOME) {
            if (max_id == null || (apiResponse.getMax_id() != null && Long.parseLong(max_id) > Long.parseLong(apiResponse.getMax_id())))
                max_id = apiResponse.getMax_id();
        }else {
            max_id = apiResponse.getMax_id();
        }
        flag_loading = (max_id == null );
        if( firstLoad && (statuses == null || statuses.size() == 0))
            textviewNoAction.setVisibility(View.VISIBLE);
        else
            textviewNoAction.setVisibility(View.GONE);

        //First toot are loaded as soon as the bookmark has been retrieved
        if( type == RetrieveFeedsAsyncTask.Type.HOME && !firstTootsLoaded){
            asyncTask = new RetrieveFeedsAsyncTask(context, type, null, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            firstTootsLoaded = true;
        }
        if( statuses != null && statuses.size() > 0) {
            if (type == RetrieveFeedsAsyncTask.Type.HOME) {
                String bookmark = null;
                if( context instanceof BaseMainActivity){
                    bookmark = ((BaseMainActivity) context).getBookmark();
                }
                //Toots are older than the bookmark -> no special treatment with them
                if( bookmark == null || Long.parseLong(statuses.get(0).getId())+1 < Long.parseLong(bookmark)){
                    this.statuses.addAll(statuses);
                    statusListAdapter.notifyItemRangeInserted(previousPosition, statuses.size());
                }else { //Toots are younger than the bookmark
                    String currentMaxId = sharedpreferences.getString(Helper.LAST_HOMETIMELINE_MAX_ID + userId + instance, null);
                    int position = 0;
                    while (position < this.statuses.size() && Long.parseLong(statuses.get(0).getId()) < Long.parseLong(this.statuses.get(position).getId())) {
                        position++;
                    }
                    ArrayList<Status> tmpStatuses = new ArrayList<>();
                    for (Status tmpStatus : statuses) {
                        //Mark status at new ones when their id is greater than the bookmark id / Also increments counter
                        if (currentMaxId != null && Long.parseLong(tmpStatus.getId()) > Long.parseLong(currentMaxId)) {
                            tmpStatus.setNew(true);
                            MainActivity.countNewStatus++;
                        }
                        //Put the toot at its place in the list (id desc)
                        if( !this.statuses.contains(tmpStatus) ) { //Element not already addeds
                            tmpStatuses.add(tmpStatus);
                        }
                    }
                    int tootPerPage = sharedpreferences.getInt(Helper.SET_TOOTS_PER_PAGE, 40);
                    if( tmpStatuses.size()  >= tootPerPage) {
                        if (tmpStatuses.size() > 0 && Long.parseLong(tmpStatuses.get(tmpStatuses.size() - 1).getId()) > Long.parseLong(bookmark)) {
                            tmpStatuses.get(tmpStatuses.size() - 1).setFetchMore(true);
                        }
                    }
                    this.statuses.addAll(position, tmpStatuses);
                    statusListAdapter.notifyItemRangeInserted(position, tmpStatuses.size());
                    if( tmpStatuses.size() < 3) //If new toots are only two
                        lv_status.scrollToPosition(0);
                    else
                        lv_status.scrollToPosition(position+tmpStatuses.size());
                }

            }else {
                this.statuses.addAll(statuses);
                statusListAdapter.notifyItemRangeInserted(previousPosition, statuses.size());
            }
            if( type == RetrieveFeedsAsyncTask.Type.HOME ) {
                //Update the id of the last toot retrieved
                if( MainActivity.lastHomeId == null || Long.parseLong(statuses.get(0).getId()) > Long.parseLong(MainActivity.lastHomeId))
                    MainActivity.lastHomeId = statuses.get(0).getId();
                if( firstLoad )
                    updateStatusLastId(statuses.get(0).getId());
            }
            if( type == RetrieveFeedsAsyncTask.Type.HOME)
                //Display new value in counter
                try {
                    ((MainActivity) context).updateHomeCounter();
                }catch (Exception ignored){}
        }
        swipeRefreshLayout.setRefreshing(false);
        firstLoad = false;


    }

    /**
     * Deals with new status coming from the streaming api
     * @param status Status
     */
    public void refresh(Status status){
        //New data are available
        if (context == null)
            return;
        if( status.getId() != null && statuses != null && statuses.size() > 0 && statuses.get(0)!= null
                && Long.parseLong(status.getId()) > Long.parseLong(statuses.get(0).getId())) {
            if (type == RetrieveFeedsAsyncTask.Type.HOME) {

                //Makes sure the status is not already displayed
                if( !statuses.contains(status)){
                    //Update the id of the last toot retrieved
                    MainActivity.lastHomeId = status.getId();
                    status.setReplies(new ArrayList<Status>());
                    statuses.add(0, status);
                    if (!status.getAccount().getId().equals(userId))
                        MainActivity.countNewStatus++;
                    statusListAdapter.notifyItemInserted(0);
                    if (textviewNoAction.getVisibility() == View.VISIBLE)
                        textviewNoAction.setVisibility(View.GONE);
                }


            } else if (type == RetrieveFeedsAsyncTask.Type.PUBLIC || type == RetrieveFeedsAsyncTask.Type.LOCAL) {

                status.setReplies(new ArrayList<Status>());
                status.setNew(false);
                statuses.add(0, status);
                int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                if (firstVisibleItem > 0)
                    statusListAdapter.notifyItemInserted(0);
                else
                    statusListAdapter.notifyDataSetChanged();
                if (textviewNoAction.getVisibility() == View.VISIBLE)
                    textviewNoAction.setVisibility(View.GONE);

            }
        }
    }

    /**
     * Refresh status in list
     */
    public void refreshFilter(){
        statusListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume(){
        super.onResume();
        boolean liveNotifications = sharedpreferences.getBoolean(Helper.SET_LIVE_NOTIFICATIONS, true);
        if( type == RetrieveFeedsAsyncTask.Type.PUBLIC){

            if( getUserVisibleHint() ){
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SHOULD_CONTINUE_STREAMING_FEDERATED + userId + instance, true);
                editor.apply();
                if(liveNotifications) {
                    streamingFederatedIntent = new Intent(context, StreamingFederatedTimelineService.class);
                    context.startService(streamingFederatedIntent);
                }
                if( statuses != null && statuses.size() > 0)
                    retrieveMissingToots(statuses.get(0).getId());
            }
        }else if (type == RetrieveFeedsAsyncTask.Type.LOCAL){

            if( getUserVisibleHint() ){
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SHOULD_CONTINUE_STREAMING_LOCAL + userId + instance, true);
                editor.apply();
                if( liveNotifications) {
                    streamingLocalIntent = new Intent(context, StreamingLocalTimelineService.class);
                    context.startService(streamingLocalIntent);
                }
                if( statuses != null && statuses.size() > 0)
                    retrieveMissingToots(statuses.get(0).getId());
            }
        }else if (type == RetrieveFeedsAsyncTask.Type.HOME){
            //Cleans old timed mute accounts
            SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
            new TempMuteDAO(context, db).removeOld();
            Account account = new AccountDAO(context, db).getAccountByID(userId);
            List<String> mutedAccount = new TempMuteDAO(context, db).getAllTimeMuted(account);
            statusListAdapter.updateMuted(mutedAccount);

        }
    }

    /**
     * Called from main activity in onResume to retrieve missing toots (home timeline)
     * @param sinceId String
     */
    public void retrieveMissingToots(String sinceId){
        asyncTask = new RetrieveMissingFeedsAsyncTask(context, sinceId, type, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        boolean liveNotifications = sharedpreferences.getBoolean(Helper.SET_LIVE_NOTIFICATIONS, true);
        //Store last toot id for home timeline to avoid to notify for those that have been already seen
        if (type == RetrieveFeedsAsyncTask.Type.HOME && visible && statuses != null && statuses.size() > 0) {
            updateStatusLastId(statuses.get(0).getId());
        } else if( type == RetrieveFeedsAsyncTask.Type.PUBLIC ){
            if (visible) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SHOULD_CONTINUE_STREAMING_FEDERATED + userId + instance, true);
                editor.apply();
                if(liveNotifications) {
                    streamingFederatedIntent = new Intent(context, StreamingFederatedTimelineService.class);
                    context.startService(streamingFederatedIntent);
                }
                if( statuses != null && statuses.size() > 0)
                    retrieveMissingToots(statuses.get(0).getId());
            }else {
                if( streamingFederatedIntent != null ){
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putBoolean(Helper.SHOULD_CONTINUE_STREAMING_FEDERATED + userId + instance, false);
                    editor.apply();
                    context.stopService(streamingFederatedIntent);
                }
            }
        }else if (type == RetrieveFeedsAsyncTask.Type.LOCAL){
            if (visible) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SHOULD_CONTINUE_STREAMING_LOCAL + userId + instance, true);
                editor.apply();
                if( liveNotifications ) {
                    streamingLocalIntent = new Intent(context, StreamingLocalTimelineService.class);
                    context.startService(streamingLocalIntent);
                }
                if( statuses != null && statuses.size() > 0)
                    retrieveMissingToots(statuses.get(0).getId());
            }else {
                if( streamingLocalIntent != null ){
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putBoolean(Helper.SHOULD_CONTINUE_STREAMING_LOCAL + userId + instance, false);
                    editor.apply();
                    context.stopService(streamingLocalIntent);
                }
            }
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        if( type == RetrieveFeedsAsyncTask.Type.PUBLIC && streamingFederatedIntent != null){
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putBoolean(Helper.SHOULD_CONTINUE_STREAMING_FEDERATED + userId + instance, false);
            editor.apply();
            context.stopService(streamingFederatedIntent);
        }else if(type == RetrieveFeedsAsyncTask.Type.LOCAL && streamingLocalIntent != null){
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putBoolean(Helper.SHOULD_CONTINUE_STREAMING_LOCAL + userId + instance, false);
            editor.apply();
            context.stopService(streamingLocalIntent);
        }
    }

    public void scrollToTop(){
        if( lv_status != null) {
            lv_status.setAdapter(statusListAdapter);
            //Store last toot id for home timeline to avoid to notify for those that have been already seen
            if (type == RetrieveFeedsAsyncTask.Type.HOME && statuses != null && statuses.size() > 0) {
                updateStatusLastId(statuses.get(0).getId());
            }
        }
    }

    @Override
    public void onRetrieveMissingFeeds(List<Status> statuses) {
        swipeRefreshLayout.setRefreshing(false);
        if( statuses != null && statuses.size() > 0) {
            int inserted = 0;
            for (int i = statuses.size() - 1; i >= 0; i--) {
                if (this.statuses.size() == 0 ||
                        Long.parseLong(statuses.get(i).getId()) > Long.parseLong(this.statuses.get(0).getId())) {
                    if (type == RetrieveFeedsAsyncTask.Type.HOME)
                        statuses.get(i).setNew(true);
                    inserted++;
                    statuses.get(i).setReplies(new ArrayList<Status>());
                    this.statuses.add(0, statuses.get(i));
                    if (type == RetrieveFeedsAsyncTask.Type.HOME && !statuses.get(i).getAccount().getId().equals(userId))
                        MainActivity.countNewStatus++;
                }
            }
            statusListAdapter.notifyItemRangeInserted(0, inserted);
            try {
                if( type == RetrieveFeedsAsyncTask.Type.HOME)
                    ((MainActivity) context).updateHomeCounter();
                else
                    ((MainActivity) context).updateTimeLine(type, inserted);
            }catch (Exception ignored){}
        }
    }

    public void fetchMore(String max_id){
        asyncTask = new RetrieveFeedsAsyncTask(context, type, max_id, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    /**
     * Records the id of the status only if its greater than the previous one.
     * @param statusId String current status id to check
     */
    private void updateStatusLastId(String statusId){

        String lastNotif = sharedpreferences.getString(Helper.LAST_HOMETIMELINE_MAX_ID + userId + instance, null);
        if( lastNotif == null || Long.parseLong(statusId) > Long.parseLong(lastNotif)){
            MainActivity.countNewStatus = 0;
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(Helper.LAST_HOMETIMELINE_MAX_ID + userId + instance, statusId);
            editor.apply();
        }
    }
}
