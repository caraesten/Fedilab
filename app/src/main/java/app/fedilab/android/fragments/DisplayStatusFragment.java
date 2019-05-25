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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Conversation;
import app.fedilab.android.client.Entities.Peertube;
import app.fedilab.android.client.Entities.RemoteInstance;
import app.fedilab.android.client.Entities.RetrieveFeedsParam;
import app.fedilab.android.client.Entities.Status;
import app.fedilab.android.client.Entities.TagTimeline;
import app.fedilab.android.drawers.ArtListAdapter;
import app.fedilab.android.drawers.PeertubeAdapter;
import app.fedilab.android.drawers.PixelfedListAdapter;
import app.fedilab.android.drawers.StatusListAdapter;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.services.StreamingFederatedTimelineService;
import app.fedilab.android.services.StreamingLocalTimelineService;
import app.fedilab.android.sqlite.AccountDAO;
import app.fedilab.android.sqlite.InstancesDAO;
import app.fedilab.android.sqlite.SearchDAO;
import app.fedilab.android.sqlite.Sqlite;
import app.fedilab.android.sqlite.TempMuteDAO;
import es.dmoral.toasty.Toasty;
import app.fedilab.android.R;
import app.fedilab.android.activities.BaseMainActivity;
import app.fedilab.android.activities.MainActivity;
import app.fedilab.android.asynctasks.ManageListsAsyncTask;
import app.fedilab.android.asynctasks.RetrieveFeedsAfterBookmarkAsyncTask;
import app.fedilab.android.asynctasks.RetrieveFeedsAsyncTask;
import app.fedilab.android.asynctasks.RetrieveMissingFeedsAsyncTask;
import app.fedilab.android.asynctasks.RetrievePeertubeSearchAsyncTask;
import app.fedilab.android.asynctasks.UpdateAccountInfoAsyncTask;
import app.fedilab.android.interfaces.OnListActionInterface;
import app.fedilab.android.interfaces.OnRetrieveFeedsAfterBookmarkInterface;
import app.fedilab.android.interfaces.OnRetrieveFeedsInterface;
import app.fedilab.android.interfaces.OnRetrieveMissingFeedsInterface;


/**
 * Created by Thomas on 24/04/2017.
 * Fragment to display content related to status
 */
public class DisplayStatusFragment extends Fragment implements OnRetrieveFeedsInterface, OnRetrieveMissingFeedsInterface, OnRetrieveFeedsAfterBookmarkInterface, OnListActionInterface {


    private boolean flag_loading;
    private Context context;
    private AsyncTask<Void, Void, Void> asyncTask;
    private StatusListAdapter statusListAdapter;
    private PeertubeAdapter peertubeAdapater;
    private ArtListAdapter artListAdapter;
    private PixelfedListAdapter pixelfedListAdapter;
    private String max_id;
    private List<Status> statuses;
    private List<Peertube> peertubes;
    private RetrieveFeedsAsyncTask.Type type;
    private RelativeLayout mainLoader, nextElementLoader, textviewNoAction;
    private boolean firstLoad;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String targetedId;
    private String tag;
    private RecyclerView lv_status;
    private boolean showMediaOnly, showPinned, showReply;
    private Intent streamingFederatedIntent, streamingLocalIntent;
    LinearLayoutManager mLayoutManager;
    boolean firstTootsLoaded;
    private String userId, instance;
    private SharedPreferences sharedpreferences;
    private boolean isSwipped;
    private String remoteInstance;
    private List<String> mutedAccount;
    private String instanceType;
    private String search_peertube, remote_channel_name;
    private String initialBookMark;
    private TagTimeline tagTimeline;
    private String updatedBookMark;
    private String lastReadToot;
    private TextView textviewNoActionText;
    private boolean ischannel;
    private boolean ownVideos;
    private BroadcastReceiver receive_action;
    private BroadcastReceiver  receive_data;
    private Date lastReadTootDate, initialBookMarkDate, updatedBookMarkDate;
    private int timelineId;
    private String currentfilter;
    public DisplayStatusFragment(){
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_status, container, false);
        statuses = new ArrayList<>();
        peertubes = new ArrayList<>();
        context = getContext();
        Bundle bundle = this.getArguments();
        showMediaOnly = false;
        //Will allow to load first toots if bookmark != null
        firstTootsLoaded = false;
        showPinned = false;
        showReply = false;
        tagTimeline = null;
        if (bundle != null) {
            type = (RetrieveFeedsAsyncTask.Type) bundle.get("type");
            targetedId = bundle.getString("targetedid", null);
            ownVideos = bundle.getBoolean("ownvideos", false); //Peetube account watching its videos
            tag = bundle.getString("tag", null);
            showMediaOnly = bundle.getBoolean("showMediaOnly",false);
            showPinned = bundle.getBoolean("showPinned",false);
            showReply = bundle.getBoolean("showReply",false);
            remoteInstance = bundle.getString("remote_instance", "");
            search_peertube = bundle.getString("search_peertube", null);
            remote_channel_name = bundle.getString("remote_channel_name", null);
            instanceType  = bundle.getString("instanceType", "MASTODON");
            ischannel = bundle.getBoolean("ischannel",false);
            timelineId = bundle.getInt("timelineId");
            currentfilter = bundle.getString("currentfilter", null);

        }
        if( ischannel)
            type = RetrieveFeedsAsyncTask.Type.CHANNEL;

        SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        //instanceType should not be null only for Peertube accounts

        if( remoteInstance != null && !remoteInstance.equals("") && instanceType == null){
            List<RemoteInstance> remoteInstanceObj = new InstancesDAO(context, db).getInstanceByName(remoteInstance);
            if( remoteInstanceObj != null && remoteInstanceObj.size() > 0)
                instanceType = remoteInstanceObj.get(0).getType();
        }
        isSwipped = false;
        max_id = null;
        flag_loading = true;
        firstLoad = true;
        initialBookMark = null;

        assert context != null;
        sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        boolean isOnWifi = Helper.isOnWIFI(context);
        swipeRefreshLayout = rootView.findViewById(R.id.swipeContainer);
        lv_status = rootView.findViewById(R.id.lv_status);
        mainLoader =  rootView.findViewById(R.id.loader);
        nextElementLoader = rootView.findViewById(R.id.loading_next_status);
        textviewNoAction =  rootView.findViewById(R.id.no_action);
        textviewNoActionText = rootView.findViewById(R.id.no_action_text);
        mainLoader.setVisibility(View.VISIBLE);
        nextElementLoader.setVisibility(View.GONE);
        userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        instance = sharedpreferences.getString(Helper.PREF_INSTANCE, context!=null?Helper.getLiveInstance(context):null);
        Account account = new AccountDAO(context, db).getAccountByID(userId);
        mutedAccount = new TempMuteDAO(context, db).getAllTimeMuted(account);

        //For Home timeline, fetch stored values for bookmark and last read toot
        if( type == RetrieveFeedsAsyncTask.Type.HOME) {
            initialBookMark = sharedpreferences.getString(Helper.BOOKMARK_ID + userId + instance, null);
            initialBookMarkDate =  Helper.stringToDate(context, sharedpreferences.getString(Helper.BOOKMARK_DATE + userId + instance, null));
            lastReadToot = sharedpreferences.getString(Helper.LAST_READ_TOOT_ID + userId + instance, null);
            lastReadTootDate  = Helper.stringToDate(context, sharedpreferences.getString(Helper.LAST_READ_TOOT_DATE + userId + instance, null));
        }
        if( instanceType == null || instanceType.equals("MASTODON") ||  instanceType.equals("MISSKEY") || instanceType.equals("GNU") ){
            if( type == RetrieveFeedsAsyncTask.Type.TAG && tag != null) {
                BaseMainActivity.displayPeertube = null;
                List<TagTimeline> tagTimelines = new SearchDAO(context, db).getTimelineInfo(tag);
                if( tagTimelines != null && tagTimelines.size() > 0) {
                    tagTimeline = tagTimelines.get(0);
                    statusListAdapter = new StatusListAdapter(context, tagTimeline, targetedId, isOnWifi, this.statuses);
                    lv_status.setAdapter(statusListAdapter);
                }
            }else{
                BaseMainActivity.displayPeertube = null;
                statusListAdapter = new StatusListAdapter(context, type, targetedId, isOnWifi, this.statuses);
                lv_status.setAdapter(statusListAdapter);
            }
        }else if( instanceType.equals("PEERTUBE")){
            if( remoteInstance != null && MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE) //if it's a Peertube account connected
                remoteInstance = account.getInstance();
            BaseMainActivity.displayPeertube = remoteInstance;
            peertubeAdapater = new PeertubeAdapter(context, remoteInstance, ownVideos, this.peertubes);
            lv_status.setAdapter(peertubeAdapater);
        }else if( instanceType.equals("PIXELFED")){
            if( remoteInstance != null && MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PIXELFED) //if it's a Peertube account connected
                remoteInstance = account.getInstance();
            pixelfedListAdapter = new PixelfedListAdapter(context, type, this.statuses);
            lv_status.setAdapter(pixelfedListAdapter);
        }else if( instanceType.equals("ART")){
            if ( type == RetrieveFeedsAsyncTask.Type.TAG) {
                List<TagTimeline> tagTimelines = new SearchDAO(context, db).getTimelineInfo(tag);
                if (tagTimelines != null && tagTimelines.size() > 0) {
                    tagTimeline = tagTimelines.get(0);
                }
            }
            artListAdapter = new ArtListAdapter(context, this.statuses);
            lv_status.setAdapter(artListAdapter);
        }
        mLayoutManager = new LinearLayoutManager(context);
        lv_status.setLayoutManager(mLayoutManager);


        //Manage broadcast receiver for Mastodon timelines
        if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON || MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA|| MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.GNU ||  MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA) {
            if( receive_action != null)
                LocalBroadcastManager.getInstance(context).unregisterReceiver(receive_action);
            receive_action = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Bundle b = intent.getExtras();
                    assert b != null;
                    Status status = b.getParcelable("status");
                    if( status != null && statusListAdapter != null) {
                        statusListAdapter.notifyStatusWithActionChanged(status);
                    }
                }
            };
            LocalBroadcastManager.getInstance(context).registerReceiver(receive_action, new IntentFilter(Helper.RECEIVE_ACTION));

            if( type == RetrieveFeedsAsyncTask.Type.HOME || type == RetrieveFeedsAsyncTask.Type.LOCAL || type == RetrieveFeedsAsyncTask.Type.PUBLIC){

                if (receive_data != null)
                    LocalBroadcastManager.getInstance(context).unregisterReceiver(receive_data);
                receive_data = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Bundle b = intent.getExtras();
                        assert b != null;
                        String userIdService = b.getString("userIdService", null);
                        if (userIdService != null && userIdService.equals(userId)) {
                            Status status = b.getParcelable("data");
                            refresh(status);
                        }
                    }
                };
            }
            if( type == RetrieveFeedsAsyncTask.Type.PUBLIC)
                LocalBroadcastManager.getInstance(context).registerReceiver(receive_data, new IntentFilter(Helper.RECEIVE_FEDERATED_DATA));
            else if( type == RetrieveFeedsAsyncTask.Type.HOME)
                LocalBroadcastManager.getInstance(context).registerReceiver(receive_data, new IntentFilter(Helper.RECEIVE_HOME_DATA));
            else if( type == RetrieveFeedsAsyncTask.Type.LOCAL)
                LocalBroadcastManager.getInstance(context).registerReceiver(receive_data, new IntentFilter(Helper.RECEIVE_LOCAL_DATA));
        }

        if( type == RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE  && search_peertube != null)
            ((Activity)context).setTitle(remoteInstance + " - " + search_peertube);
        if( type == RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE  && remote_channel_name != null)
            ((Activity)context).setTitle(remote_channel_name + " - " + remoteInstance);
        if( type != RetrieveFeedsAsyncTask.Type.POVERVIEW ) //No paginations for Peertube Overviews (it's a fixed size content
            lv_status.addOnScrollListener(new RecyclerView.OnScrollListener() {
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy)
                {
                    if (type != RetrieveFeedsAsyncTask.Type.ART  && context instanceof  BaseMainActivity ) {
                        if( dy < 0 && !((BaseMainActivity)context).getFloatingVisibility() )
                            ((BaseMainActivity) context).manageFloatingButton(true);
                        if( dy > 0 && ((BaseMainActivity)context).getFloatingVisibility() )
                            ((BaseMainActivity) context).manageFloatingButton(false);
                    }
                    int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                    if(dy > 0){
                        int visibleItemCount = mLayoutManager.getChildCount();
                        int totalItemCount = mLayoutManager.getItemCount();
                        if(firstVisibleItem + visibleItemCount == totalItemCount && context != null) {
                            if(!flag_loading ) {
                                flag_loading = true;
                                manageAsyncTask(true);
                                nextElementLoader.setVisibility(View.VISIBLE);
                            }
                        } else {
                            nextElementLoader.setVisibility(View.GONE);
                        }
                    }
                    if(type == RetrieveFeedsAsyncTask.Type.HOME && statuses != null && statuses.size() > firstVisibleItem && firstVisibleItem >= 0) {
                        Date bookmarkL = statuses.get(firstVisibleItem).getCreated_at();
                        updatedBookMark = statuses.get(firstVisibleItem).getId();
                        updatedBookMarkDate = statuses.get(firstVisibleItem).getCreated_at();
                        if( lastReadTootDate == null || (bookmarkL != null && bookmarkL.after(lastReadTootDate))){
                         //Last read toot, only incremented if the id of the toot is greater than the recorded one
                            lastReadTootDate = bookmarkL;
                        }
                    }
                }
            });


        if( instanceType == null || !instanceType.equals("PEERTUBE"))
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if( type == RetrieveFeedsAsyncTask.Type.HOME)
                        MainActivity.countNewStatus = 0;
                    isSwipped = true;
                    if( type != RetrieveFeedsAsyncTask.Type.CONVERSATION)
                        retrieveMissingToots(null);
                    else{
                        if( statuses.size() > 0)
                            retrieveMissingToots(statuses.get(0).getId());
                        else
                            retrieveMissingToots(null);
                    }

                }
            });
        else
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if( peertubes.size() > 0) {
                        int size = peertubes.size();
                        isSwipped = true;
                        peertubes.clear();
                        peertubes = new ArrayList<>();
                        max_id = "0";
                        peertubeAdapater.notifyItemRangeRemoved(0, size);
                        asyncTask = new RetrieveFeedsAsyncTask(context, type, remoteInstance, "0", DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        if( context != null) {
            //Load data depending of the value
            manageAsyncTask(false);
        }else {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if( context != null){
                        manageAsyncTask(false);
                    }
                }
            }, 500);
        }
        return rootView;
    }

    @Override
    public void onPause(){
        super.onPause();
        //Store bookmark on pause
        if (context instanceof BaseMainActivity && type == RetrieveFeedsAsyncTask.Type.HOME) {
            SharedPreferences.Editor editor = sharedpreferences.edit();
            if(updatedBookMark != null)
                editor.putString(Helper.BOOKMARK_ID + userId + instance, updatedBookMark);
            if(updatedBookMarkDate != null)
                editor.putString(Helper.BOOKMARK_DATE + userId + instance, Helper.dateToString(updatedBookMarkDate));
            if( lastReadToot != null)
                editor.putString(Helper.LAST_READ_TOOT_ID + userId + instance, lastReadToot);
            if( lastReadTootDate != null)
                editor.putString(Helper.LAST_READ_TOOT_DATE + userId + instance, Helper.dateToString(lastReadTootDate));
            editor.apply();
        }
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
        if( receive_action != null)
            LocalBroadcastManager.getInstance(context).unregisterReceiver(receive_action);
        if( receive_data != null)
            LocalBroadcastManager.getInstance(context).unregisterReceiver(receive_data);
    }

    @Override
    public void onRetrieveFeeds(APIResponse apiResponse) {
        //hide loaders
        mainLoader.setVisibility(View.GONE);
        nextElementLoader.setVisibility(View.GONE);

        //handle other API error but discards 404 - error which can often happen due to toots which have been deleted
        if( this.peertubes == null ||  this.statuses == null || apiResponse == null || (apiResponse.getError() != null && apiResponse.getError().getStatusCode() != 404) ){
            if( apiResponse == null)
                Toasty.error(context, context.getString(R.string.toast_error),Toast.LENGTH_LONG).show();
            else
                Toasty.error(context, apiResponse.getError().getError(),Toast.LENGTH_LONG).show();
            swipeRefreshLayout.setRefreshing(false);
            flag_loading = false;
            return;
        }

        //For remote Peertube remote instances
        if(instanceType.equals("PEERTUBE")){
            int previousPosition = this.peertubes.size();
            if( max_id == null)
                max_id = "0";
            //max_id needs to work like an offset
            max_id = String.valueOf(Integer.valueOf(max_id) + 50);
            if( apiResponse.getPeertubes() == null){
                return;
            }
            this.peertubes.addAll(apiResponse.getPeertubes());
            //If no item were inserted previously the adapter is created
            if( previousPosition == 0) {
                peertubeAdapater = new PeertubeAdapter(context, remoteInstance, ownVideos, this.peertubes);
                lv_status.setAdapter(peertubeAdapater);
            }else
                peertubeAdapater.notifyItemRangeInserted(previousPosition, apiResponse.getPeertubes().size());
            //remove handlers
            swipeRefreshLayout.setRefreshing(false);
            if( firstLoad && (apiResponse.getPeertubes() == null || apiResponse.getPeertubes().size() ==0)){
                textviewNoActionText.setText(R.string.no_video_to_display);
                textviewNoAction.setVisibility(View.VISIBLE);
            }
            flag_loading = false;
            firstLoad = false;
        }else {

            //When Mastodon statuses have been fetched.
            if( type == RetrieveFeedsAsyncTask.Type.CONVERSATION ){ //Conversation timeline
                //this timeline is dealt differently because it is embedded in Conversation entity and  not directly in statuses
                List<Conversation> conversations = apiResponse.getConversations();
                //Statuses from conversation entity are retrieved
                List<Status> statusesConversations = new ArrayList<>();
                if( conversations != null) {
                    for (Conversation conversation : conversations) {
                        Status status = conversation.getLast_status();
                        if (status != null) {
                            status.setConversationId(conversation.getId());
                            List<String> ppConversation = new ArrayList<>();
                            for (Account account : conversation.getAccounts())
                                ppConversation.add(account.getAvatar());
                            status.setConversationProfilePicture(ppConversation);
                        }
                        statusesConversations.add(status);
                    }
                }
                apiResponse.setStatuses(statusesConversations);
            }
            int previousPosition = this.statuses.size();
            List<Status> statuses;

            if( apiResponse.getResults() != null && apiResponse.getResults().getStatuses() != null)
                statuses = apiResponse.getResults().getStatuses();
            else
                statuses = apiResponse.getStatuses();
            //At this point all statuses are in "List<Status> statuses"
            //Pagination for Pixelfed
            if(instanceType.equals("PIXELFED")) {
                if( max_id == null)
                    max_id = "1";
                //max_id needs to work like an offset
                max_id = String.valueOf(Integer.valueOf(max_id) + 1);
            }else if( type == RetrieveFeedsAsyncTask.Type.SEARCH) {
                if(max_id == null)
                    max_id = "0";
                max_id = String.valueOf(Integer.valueOf(max_id) + 20);
            } else{
                max_id = apiResponse.getMax_id();
            }

            //while max_id is different from null, there are some more toots to load when scrolling
            flag_loading = (max_id == null );
            //If it's the first load and the reply doesn't contain any toots, a message is displayed.
            if( firstLoad && (statuses == null || statuses.size() == 0)) {
                textviewNoAction.setVisibility(View.VISIBLE);
                lv_status.setVisibility(View.GONE);
            }else {
                lv_status.setVisibility(View.VISIBLE);
                textviewNoAction.setVisibility(View.GONE);
            }

            //First toot are loaded as soon as the bookmark has been retrieved
            //Only for the Home timeline

            if( type == RetrieveFeedsAsyncTask.Type.HOME && !firstTootsLoaded){
                boolean remember_position_home = sharedpreferences.getBoolean(Helper.SET_REMEMBER_POSITION_HOME, true);
                if( remember_position_home) {
                    asyncTask = new RetrieveFeedsAfterBookmarkAsyncTask(context, null, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                firstTootsLoaded = true;
            }
            //Let's deal with statuses
            if( statuses != null && statuses.size() > 0) {
                if ( statusListAdapter != null && ( instanceType.equals("MASTODON") || instanceType.equals("MISSKEY") || instanceType.equals("GNU"))) {
                    this.statuses.addAll(statuses);
                    statusListAdapter.notifyItemRangeInserted(previousPosition, statuses.size());
                }else if(artListAdapter != null && instanceType.equals("ART") ) {
                    boolean show_nsfw = sharedpreferences.getBoolean(Helper.SET_ART_WITH_NSFW, false);
                    if( !show_nsfw) {
                        ArrayList<Status> safeStatuses = new ArrayList<>();
                        for(Status status: statuses){
                            if( !status.isSensitive())
                                safeStatuses.add(status);
                        }
                        this.statuses.addAll(safeStatuses);
                        artListAdapter.notifyItemRangeInserted(previousPosition, safeStatuses.size());
                    }else {
                        this.statuses.addAll(statuses);
                        artListAdapter.notifyItemRangeInserted(previousPosition, statuses.size());
                    }

                }else if(pixelfedListAdapter != null && instanceType.equals("PIXELFED") ) {
                    this.statuses.addAll(statuses);
                    pixelfedListAdapter.notifyItemRangeInserted(previousPosition, statuses.size());
                }
            }
            swipeRefreshLayout.setRefreshing(false);
            firstLoad = false;

        }
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
                && status.getCreated_at().after(statuses.get(0).getCreated_at()) ) {
            List<Status> tempTootResult = new ArrayList();
            tempTootResult.add(status);
            if( tempTootResult.size() > 0)
                status = tempTootResult.get(0);
            if (type == RetrieveFeedsAsyncTask.Type.HOME) {

                //Makes sure the status is not already displayed
                if( !statuses.contains(status)){
                    //Update the id of the last toot retrieved
                    MainActivity.lastHomeId = status.getId();
                    statuses.add(0, status);
                    try {
                        ((MainActivity) context).updateHomeCounter();
                    }catch (Exception ignored){}

                    statusListAdapter.notifyItemInserted(0);
                }

            } else if (type == RetrieveFeedsAsyncTask.Type.PUBLIC || type == RetrieveFeedsAsyncTask.Type.LOCAL|| type == RetrieveFeedsAsyncTask.Type.DIRECT|| type == RetrieveFeedsAsyncTask.Type.GNU_DM) {

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


    @Override
    public void onResume(){
        super.onResume();
        boolean liveNotifications = sharedpreferences.getBoolean(Helper.SET_LIVE_NOTIFICATIONS, true);
        int batteryProfile = sharedpreferences.getInt(Helper.SET_BATTERY_PROFILE, Helper.BATTERY_PROFILE_NORMAL);
        if (type == RetrieveFeedsAsyncTask.Type.HOME){
            if( getUserVisibleHint() ){
                statusListAdapter.updateMuted(mutedAccount);
                if( statuses != null && statuses.size() > 0 && asyncTask.getStatus() != AsyncTask.Status.RUNNING) {
                    retrieveMissingToots(statuses.get(0).getId());
                }
            }
        } else if( type == RetrieveFeedsAsyncTask.Type.PUBLIC){
            if( getUserVisibleHint() ){
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SHOULD_CONTINUE_STREAMING_FEDERATED + userId + instance, true);
                editor.apply();
                if(liveNotifications && batteryProfile == Helper.BATTERY_PROFILE_NORMAL) {
                    streamingFederatedIntent = new Intent(context, StreamingFederatedTimelineService.class);
                    try {
                        context.startService(streamingFederatedIntent);
                    }catch (Exception ignored){}
                }
                if( statuses != null && statuses.size() > 0)
                    retrieveMissingToots(statuses.get(0).getId());
            }
        }else if (type == RetrieveFeedsAsyncTask.Type.LOCAL){

            if( getUserVisibleHint() ){
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SHOULD_CONTINUE_STREAMING_LOCAL + userId + instance, true);
                editor.apply();
                if( liveNotifications  && batteryProfile == Helper.BATTERY_PROFILE_NORMAL) {
                    streamingLocalIntent = new Intent(context, StreamingLocalTimelineService.class);
                    try {
                        context.startService(streamingLocalIntent);
                    }catch (Exception ignored){}
                }
                if( statuses != null && statuses.size() > 0)
                    retrieveMissingToots(statuses.get(0).getId());
            }
        }else if (type == RetrieveFeedsAsyncTask.Type.DIRECT|| type == RetrieveFeedsAsyncTask.Type.GNU_DM){
            if( getUserVisibleHint() ){
                if( statuses != null && statuses.size() > 0)
                    retrieveMissingToots(statuses.get(0).getId());
            }
        }else if (type == RetrieveFeedsAsyncTask.Type.CONVERSATION){
            if( getUserVisibleHint() ){
                if( statuses != null && statuses.size() > 0)
                    retrieveMissingToots(statuses.get(0).getId());
            }
        }else if (type == RetrieveFeedsAsyncTask.Type.TAG){
            if( getUserVisibleHint() ){
                if( statuses != null && statuses.size() > 0)
                    retrieveMissingToots(statuses.get(0).getId());
            }
        }
    }

    /**
     * Called from main activity in onResume to retrieve missing toots (home timeline)
     * @param sinceId String
     */
    public void retrieveMissingToots(String sinceId){

        if( type == RetrieveFeedsAsyncTask.Type.HOME)
            asyncTask = new RetrieveFeedsAfterBookmarkAsyncTask(context, null, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        if (type == RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE )
            asyncTask = new RetrieveMissingFeedsAsyncTask(context, remoteInstance, sinceId, type, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else if(type == RetrieveFeedsAsyncTask.Type.TAG)
            asyncTask = new RetrieveMissingFeedsAsyncTask(context, tag, sinceId, type, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            asyncTask = new RetrieveMissingFeedsAsyncTask(context, sinceId, type, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }


    public void retrieveMissingHome(){
        if( statusListAdapter != null && statuses != null && lv_status != null && mLayoutManager != null){
            int firstVisible = mLayoutManager.findFirstVisibleItemPosition();
            Iterator<Status> s = statuses.iterator();
            int i = 0;
            while (s.hasNext() && i < firstVisible) {
                Status status = s.next();
                s.remove();
                statusListAdapter.notifyItemRemoved(0);
                statusListAdapter.notifyItemChanged(0);
                i++;
            }
            if( statuses.size() > 0)
                initialBookMarkDate = statuses.get(0).getCreated_at();
            asyncTask = new RetrieveFeedsAfterBookmarkAsyncTask(context, null, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
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
        int batteryProfile = sharedpreferences.getInt(Helper.SET_BATTERY_PROFILE, Helper.BATTERY_PROFILE_NORMAL);
        //Store last toot id for home timeline to avoid to notify for those that have been already seen
        if (type == RetrieveFeedsAsyncTask.Type.HOME ) {
            if (visible) {
                if( statuses != null && statuses.size() > 0) {
                    retrieveMissingToots(statuses.get(0).getId());
                }
            }
        } else if( type == RetrieveFeedsAsyncTask.Type.PUBLIC ){
            if (visible) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SHOULD_CONTINUE_STREAMING_FEDERATED + userId + instance, true);
                editor.apply();
                if(liveNotifications  && batteryProfile == Helper.BATTERY_PROFILE_NORMAL) {
                    streamingFederatedIntent = new Intent(context, StreamingFederatedTimelineService.class);
                    try {
                        context.startService(streamingFederatedIntent);
                    }catch (Exception ignored){}
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
                if( liveNotifications  && batteryProfile == Helper.BATTERY_PROFILE_NORMAL) {
                    streamingLocalIntent = new Intent(context, StreamingLocalTimelineService.class);
                    try {
                        context.startService(streamingLocalIntent);
                    }catch (Exception ignored){}
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
        if( lv_status != null && instanceType != null) {
            if( statusListAdapter != null && (instanceType.equals("MASTODON") || instanceType.equals("MISSKEY") || instanceType.equals("GNU")))
                lv_status.setAdapter(statusListAdapter);
            else if( pixelfedListAdapter != null && instanceType.equals("PIXELFED"))
                lv_status.setAdapter(pixelfedListAdapter);
            else if( artListAdapter != null && instanceType.equals("ART"))
                lv_status.setAdapter(artListAdapter);
            else if( peertubeAdapater != null && instanceType.equals("PEERTUBE"))
                lv_status.setAdapter(peertubeAdapater);
        }
    }

    /**
     * Refresh status in list
     */
    public void refreshFilter(){

        if( instanceType.equals("MASTODON") || instanceType.equals("MISSKEY")|| instanceType.equals("GNU"))
            statusListAdapter.notifyDataSetChanged();
        else if( instanceType.equals("PIXELFED"))
            pixelfedListAdapter.notifyDataSetChanged();
        else if( instanceType.equals("ART"))
            artListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRetrieveMissingFeeds(List<Status> statuses) {
        if(swipeRefreshLayout == null)
            return;
        //Clean label new
        swipeRefreshLayout.setRefreshing(false);
        if( isSwipped && this.statuses != null && this.statuses.size() > 0) {
            for (Status status : this.statuses) {
                status.setNew(false);
            }
            switch (instanceType) {
                case "MASTODON":
                case "MISSKEY":
                case "GNU":
                    statusListAdapter.notifyItemRangeChanged(0, this.statuses.size());
                    break;
                case "PIXELFED":
                    pixelfedListAdapter.notifyItemRangeChanged(0, this.statuses.size());
                    break;
                case "ART":
                    artListAdapter.notifyItemRangeChanged(0, this.statuses.size());
                    break;
            }
        }

        isSwipped = false;

        if( statuses != null && statuses.size() > 0) {
            if( textviewNoAction.getVisibility() == View.VISIBLE){
                textviewNoAction.setVisibility(View.GONE);
                lv_status.setVisibility(View.VISIBLE);
            }
            int inserted = 0;
            int insertedConversation = 0;
            if(type == RetrieveFeedsAsyncTask.Type.CONVERSATION){ //Remove conversation already displayed if new messages
                int position = 0;
                insertedConversation = statuses.size();
                if( this.statuses != null) {
                    for (Iterator<Status> it = this.statuses.iterator(); it.hasNext(); ) {
                        Status status = it.next();
                        for (Status status1 : statuses) {
                            if (status.getConversationId() != null && status.getConversationId().equals(status1.getConversationId())) {
                                if( instanceType.equals("MASTODON") || instanceType.equals("MISSKEY")|| instanceType.equals("GNU"))
                                    statusListAdapter.notifyItemRemoved(position);
                                else if( instanceType.equals("PIXELFED"))
                                    pixelfedListAdapter.notifyItemRemoved(position);
                                else if( instanceType.equals("ART"))
                                    artListAdapter.notifyItemRemoved(position);
                                it.remove();
                            }
                        }
                        position++;
                    }
                }
            }
            for (int i = statuses.size() - 1; i >= 0; i--) {
                if( this.statuses != null) {
                    if( type != RetrieveFeedsAsyncTask.Type.HOME){
                        if( tagTimeline != null && instanceType.equals("ART") && !tagTimeline.isNSFW() ){
                            if( !statuses.get(i).isSensitive()) {
                                this.statuses.add(0, statuses.get(i));
                                inserted++;
                            }
                        }else {
                            if (this.statuses.size() == 0 || statuses.get(i).getCreated_at().after(this.statuses.get(0).getCreated_at())) {
                                inserted++;
                                this.statuses.add(0, statuses.get(i));
                            }
                        }
                    }else {
                        if( lastReadTootDate != null && statuses.get(i).getCreated_at().after(lastReadTootDate)) {
                            if( !this.statuses.contains(statuses.get(i)) ) {
                                statuses.get(i).setNew(true);
                                MainActivity.countNewStatus++;
                                inserted++;
                                this.statuses.add(0, statuses.get(i));
                            }
                        }
                    }
                }
            }
            if( statusListAdapter != null && (instanceType.equals("MASTODON") || instanceType.equals("MISSKEY")|| instanceType.equals("GNU")))
                statusListAdapter.notifyItemRangeInserted(0, inserted);
            else if( pixelfedListAdapter != null && instanceType.equals("PIXELFED"))
                pixelfedListAdapter.notifyItemRangeInserted(0, inserted);
            else if( artListAdapter != null && instanceType.equals("ART"))
                artListAdapter.notifyItemRangeInserted(0, inserted);
            try {
                if( type == RetrieveFeedsAsyncTask.Type.HOME)
                    ((MainActivity) context).updateHomeCounter();
                else {
                    if( type != RetrieveFeedsAsyncTask.Type.CONVERSATION)
                        ((MainActivity) context).manageTab(type, inserted);
                    else
                        ((MainActivity) context).manageTab(type, insertedConversation);
                }
            }catch (Exception ignored){}
        }
    }

    public void fetchMore(String max_id){
        asyncTask = new RetrieveFeedsAfterBookmarkAsyncTask(context, max_id, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onRetrieveFeedsAfterBookmark(APIResponse apiResponse) {

        if( statusListAdapter == null)
            return;
        if( apiResponse == null || (apiResponse.getError() != null && apiResponse.getError().getStatusCode() != 404) ){
            if( apiResponse == null)
                Toasty.error(context, context.getString(R.string.toast_error),Toast.LENGTH_LONG).show();
            else
                Toasty.error(context, apiResponse.getError().getError(),Toast.LENGTH_LONG).show();
            swipeRefreshLayout.setRefreshing(false);
            flag_loading = false;
            return;
        }
        List<Status> statuses = apiResponse.getStatuses();

        if( statuses == null || statuses.size() == 0 || this.statuses == null )
            return;
        //Find the position of toots between those already present
        int position = 0;

        if( position < this.statuses.size() && statuses.get(0).getCreated_at() != null && this.statuses.get(position).getCreated_at() != null) {
            while (position < this.statuses.size() && statuses.get(0).getCreated_at().before(this.statuses.get(position).getCreated_at())) {
                position++;
            }
        }
        ArrayList<Status> tmpStatuses = new ArrayList<>();
        for (Status tmpStatus : statuses) {
            //Put the toot at its place in the list (id desc)
            if (this.statuses.size() == 0){
                tmpStatuses.add(tmpStatus);
            }else if( tmpStatus.getCreated_at().after(this.statuses.get(0).getCreated_at())) { //Element not already added
                //Mark status at new ones when their id is greater than the last read toot id
                if (type == RetrieveFeedsAsyncTask.Type.HOME && lastReadTootDate != null && tmpStatus.getCreated_at().after(lastReadTootDate) ) {
                    tmpStatus.setNew(true);
                    MainActivity.countNewStatus++;
                }
                tmpStatuses.add(tmpStatus);
            }
        }
        try {
            ((MainActivity) context).updateHomeCounter();
        }catch (Exception ignored){}
        int tootPerPage = sharedpreferences.getInt(Helper.SET_TOOTS_PER_PAGE, 40);
        //Display the fetch more toot button
        if( tmpStatuses.size()  >= tootPerPage) {
            if (initialBookMarkDate != null &&  tmpStatuses.size() > 0 && tmpStatuses.get(tmpStatuses.size() - 1).getCreated_at().after(initialBookMarkDate)) {
                if( StatusListAdapter.fetch_all_more && statuses.size() > 0){
                    fetchMore(tmpStatuses.get(tmpStatuses.size() - 1).getId());
                }else{
                    tmpStatuses.get(tmpStatuses.size() - 1).setFetchMore(true);
                    StatusListAdapter.fetch_all_more = false;
                }
            }
        }else{
            StatusListAdapter.fetch_all_more = false;
        }
        this.statuses.addAll(position, tmpStatuses);
        boolean display_content_after_fetch_more = sharedpreferences.getBoolean(Helper.SET_DISPLAY_CONTENT_AFTER_FM, true);
        if( position > 0 && display_content_after_fetch_more)
            lv_status.scrollToPosition(position + tmpStatuses.size());
        statusListAdapter.notifyItemRangeInserted(position, tmpStatuses.size());
        if( textviewNoAction.getVisibility() == View.VISIBLE && tmpStatuses.size() > 0){
            textviewNoAction.setVisibility(View.GONE);
            lv_status.setVisibility(View.VISIBLE);
        }
    }

    //Update last read toots value when pressing tab button
    public void updateLastReadToot(){
        if (type == RetrieveFeedsAsyncTask.Type.HOME && this.statuses != null && this.statuses.size() > 0) {
            lastReadToot = this.statuses.get(0).getId();
        }
    }

    private void manageAsyncTask(boolean pagination) {
        //Message for an account
        if (type == RetrieveFeedsAsyncTask.Type.USER || type == RetrieveFeedsAsyncTask.Type.CHANNEL)
            asyncTask = new RetrieveFeedsAsyncTask(context, type, targetedId, max_id, showMediaOnly, showPinned, showReply, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            //Tag timelines
        else if (type == RetrieveFeedsAsyncTask.Type.SEARCH)
            asyncTask = new RetrieveFeedsAsyncTask(context, type, tag, targetedId, max_id, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else if (type == RetrieveFeedsAsyncTask.Type.TAG )
            asyncTask = new RetrieveFeedsAsyncTask(context, type, timelineId, max_id, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else if (type == RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE) {
            //Remote instances
            if (search_peertube == null) { //Not a Peertube search
                if (remote_channel_name == null) { //Not a channel
                    asyncTask = new RetrieveFeedsAsyncTask(context, type, remoteInstance, max_id, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else
                    asyncTask = new RetrieveFeedsAsyncTask(context, remoteInstance, remote_channel_name, null, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else
                asyncTask = new RetrievePeertubeSearchAsyncTask(context, remoteInstance, search_peertube, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }else if(type == RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE_FILTERED){
            RetrieveFeedsParam retrieveFeedsParam = new RetrieveFeedsParam();
            retrieveFeedsParam.setAction(type);
            retrieveFeedsParam.setCurrentfilter(currentfilter);
            retrieveFeedsParam.setRemoteInstance(remoteInstance);
            retrieveFeedsParam.setMax_id(max_id);
            retrieveFeedsParam.setSocial(instanceType);
            asyncTask = new RetrieveFeedsAsyncTask(context, retrieveFeedsParam, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }else if( type == RetrieveFeedsAsyncTask.Type.LIST){
            new ManageListsAsyncTask(context,targetedId, max_id ,null, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }else {
            if( !pagination) {
                if (type == RetrieveFeedsAsyncTask.Type.HOME) {
                    if (context instanceof BaseMainActivity) {
                        boolean remember_position_home = sharedpreferences.getBoolean(Helper.SET_REMEMBER_POSITION_HOME, true);
                        if(remember_position_home )
                            asyncTask = new RetrieveFeedsAsyncTask(context, type, initialBookMark, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        else
                            asyncTask = new RetrieveFeedsAsyncTask(context, type, null, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                } else { //Most classical search will be done by this call
                    asyncTask = new RetrieveFeedsAsyncTask(context, type, null, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }else {
                if( type == RetrieveFeedsAsyncTask.Type.HOME){
                    asyncTask = new RetrieveFeedsAsyncTask(context, type, max_id, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }else {//Most classical search will be done by this call for pagination
                    asyncTask = new RetrieveFeedsAsyncTask(context, type, max_id, DisplayStatusFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        }
    }


    @Override
    public void onActionDone(ManageListsAsyncTask.action actionType, APIResponse apiResponse, int statusCode) {
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        mainLoader.setVisibility(View.GONE);
        nextElementLoader.setVisibility(View.GONE);
        //Discards 404 - error which can often happen due to toots which have been deleted
        if (apiResponse.getError() != null) {
            if ( !apiResponse.getError().getError().startsWith("404 -"))
                Toasty.error(context, apiResponse.getError().getError(), Toast.LENGTH_LONG).show();
            swipeRefreshLayout.setRefreshing(false);
            isSwipped = false;
            flag_loading = false;
            return;
        }
        if( actionType == ManageListsAsyncTask.action.GET_LIST_TIMELINE) {

            int previousPosition = this.statuses.size();
            List<Status> statuses = apiResponse.getStatuses();
            max_id = apiResponse.getMax_id();
            flag_loading = (max_id == null);
            if (!isSwipped && firstLoad && (statuses == null || statuses.size() == 0))
                textviewNoAction.setVisibility(View.VISIBLE);
            else
                textviewNoAction.setVisibility(View.GONE);

            if (isSwipped) {
                if (previousPosition > 0) {
                    for (int i = 0; i < previousPosition; i++) {
                        this.statuses.remove(0);
                    }
                    statusListAdapter.notifyItemRangeRemoved(0, previousPosition);
                }
                isSwipped = false;
            }
            if (statuses != null && statuses.size() > 0) {
                this.statuses.addAll(statuses);
                statusListAdapter.notifyItemRangeInserted(previousPosition, statuses.size());
            }
            swipeRefreshLayout.setRefreshing(false);
            firstLoad = false;
        }
    }
}
