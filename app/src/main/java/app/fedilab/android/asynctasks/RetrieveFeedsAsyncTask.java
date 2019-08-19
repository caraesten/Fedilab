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
package app.fedilab.android.asynctasks;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;


import java.lang.ref.WeakReference;
import java.util.List;

import app.fedilab.android.client.API;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.ManageTimelines;
import app.fedilab.android.client.Entities.Peertube;
import app.fedilab.android.client.Entities.RemoteInstance;
import app.fedilab.android.client.Entities.Results;
import app.fedilab.android.client.Entities.RetrieveFeedsParam;
import app.fedilab.android.client.Entities.Status;
import app.fedilab.android.client.GNUAPI;
import app.fedilab.android.client.PeertubeAPI;
import app.fedilab.android.helper.FilterToots;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.sqlite.InstancesDAO;
import app.fedilab.android.sqlite.PeertubeFavoritesDAO;
import app.fedilab.android.sqlite.Sqlite;
import app.fedilab.android.sqlite.StatusCacheDAO;
import app.fedilab.android.sqlite.TimelineCacheDAO;
import app.fedilab.android.sqlite.TimelinesDAO;
import app.fedilab.android.activities.MainActivity;
import app.fedilab.android.interfaces.OnRetrieveFeedsInterface;


/**
 * Created by Thomas on 23/04/2017.
 * Retrieves toots on the instance
 */

public class RetrieveFeedsAsyncTask extends AsyncTask<Void, Void, Void> {


    private Type action;
    private APIResponse apiResponse;
    private String max_id;
    private OnRetrieveFeedsInterface listener;
    private String targetedID;
    private String tag;
    private boolean showMediaOnly = false;
    private boolean showPinned = false;
    private boolean showReply = false;
    private WeakReference<Context> contextReference;
    private FilterToots filterToots;
    private String instanceName,remoteInstance, name;
    private boolean cached = false;
    private int timelineId;
    private String currentfilter;
    private String social;

    public enum Type{
        HOME,
        LOCAL,
        DIRECT,
        CONVERSATION,
        PUBLIC,
        HASHTAG,
        LIST,
        USER,
        FAVOURITES,
        ONESTATUS,
        CONTEXT,
        TAG,
        REMOTE_INSTANCE,
        REMOTE_INSTANCE_FILTERED,
        ART,
        PEERTUBE,
        NOTIFICATION,
        SEARCH,
        NEWS,

        PSUBSCRIPTIONS,
        POVERVIEW,
        PTRENDING,
        PRECENTLYADDED,
        PMYVIDEOS,
        PLOCAL,
        CHANNEL,
        MYVIDEOS,
        PEERTUBE_HISTORY,

        PIXELFED,
        PF_HOME,
        PF_LOCAL,
        PF_DISCOVER,
        PF_NOTIFICATION,


        GNU_HOME,
        GNU_LOCAL,
        GNU_WHOLE,
        GNU_NOTIFICATION,
        GNU_DM,
        GNU_ART,
        GNU_TAG,
        GNU_GROUP_TIMELINE,

        SCHEDULED_TOOTS,
        CACHE_BOOKMARKS,
        CACHE_BOOKMARKS_PEERTUBE,
        CACHE_STATUS,

    }


    public RetrieveFeedsAsyncTask(Context context, FilterToots filterToots, String max_id, OnRetrieveFeedsInterface onRetrieveFeedsInterface){
        this.contextReference = new WeakReference<>(context);
        this.action = Type.CACHE_STATUS;
        this.max_id = max_id;
        this.listener = onRetrieveFeedsInterface;
        this.filterToots = filterToots;
    }


    public RetrieveFeedsAsyncTask(Context context, Type action, String max_id, OnRetrieveFeedsInterface onRetrieveFeedsInterface){
        this.contextReference = new WeakReference<>(context);
        this.action = action;
        this.max_id = max_id;
        this.listener = onRetrieveFeedsInterface;
    }

    public RetrieveFeedsAsyncTask(Context context, Type action, String instanceName, String max_id, OnRetrieveFeedsInterface onRetrieveFeedsInterface){
        this.contextReference = new WeakReference<>(context);
        this.action = action;
        this.max_id = max_id;
        this.listener = onRetrieveFeedsInterface;
        this.instanceName = instanceName;
    }

    public RetrieveFeedsAsyncTask(Context context, Type action, int timelineId, String max_id, OnRetrieveFeedsInterface onRetrieveFeedsInterface){
        this.contextReference = new WeakReference<>(context);
        this.action = action;
        this.max_id = max_id;
        this.listener = onRetrieveFeedsInterface;
        this.timelineId = timelineId;
    }

    public RetrieveFeedsAsyncTask(Context context, Type action, String targetedID, String max_id, boolean showMediaOnly, boolean showPinned, OnRetrieveFeedsInterface onRetrieveFeedsInterface){
        this.contextReference = new WeakReference<>(context);
        this.action = action;
        this.max_id = max_id;
        this.listener = onRetrieveFeedsInterface;
        this.targetedID = targetedID;
        this.showMediaOnly = showMediaOnly;
        this.showPinned = showPinned;
    }
    public RetrieveFeedsAsyncTask(Context context, Type action, String targetedID, String max_id, boolean showMediaOnly, boolean showPinned, boolean showReply, OnRetrieveFeedsInterface onRetrieveFeedsInterface){
        this.contextReference = new WeakReference<>(context);
        this.action = action;
        this.max_id = max_id;
        this.listener = onRetrieveFeedsInterface;
        this.targetedID = targetedID;
        this.showMediaOnly = showMediaOnly;
        this.showPinned = showPinned;
        this.showReply = showReply;
    }
    public RetrieveFeedsAsyncTask(Context context, Type action, String tag, String targetedID, String max_id, OnRetrieveFeedsInterface onRetrieveFeedsInterface){
        this.contextReference = new WeakReference<>(context);
        this.action = action;
        this.max_id = max_id;
        this.listener = onRetrieveFeedsInterface;
        this.targetedID = targetedID;
        this.tag = tag;
    }

    public RetrieveFeedsAsyncTask(Context context, String  remoteInstance, String name, String max_id, OnRetrieveFeedsInterface onRetrieveFeedsInterface){
        this.contextReference = new WeakReference<>(context);
        this.remoteInstance = remoteInstance;
        this.max_id = max_id;
        this.listener = onRetrieveFeedsInterface;
        this.name = name;
        this.action = Type.REMOTE_INSTANCE;
    }

    public RetrieveFeedsAsyncTask(Context context, RetrieveFeedsParam retrieveFeedsParam, OnRetrieveFeedsInterface onRetrieveFeedsInterface){
        this.contextReference = new WeakReference<>(context);
        this.listener = onRetrieveFeedsInterface;
        this.action = retrieveFeedsParam.getAction();
        this.max_id = retrieveFeedsParam.getMax_id();
        this.targetedID = retrieveFeedsParam.getTargetedID();
        this.tag = retrieveFeedsParam.getTag();
        this.showMediaOnly = retrieveFeedsParam.isShowMediaOnly();
        this.showPinned = retrieveFeedsParam.isShowPinned();
        this.showReply = retrieveFeedsParam.isShowReply();
        this.name = retrieveFeedsParam.getName();
        this.currentfilter = retrieveFeedsParam.getCurrentfilter();
        this.social = retrieveFeedsParam.getSocial();
        this.instanceName = retrieveFeedsParam.getInstanceName();
        this.remoteInstance = retrieveFeedsParam.getRemoteInstance();
    }


    @Override
    protected Void doInBackground(Void... params) {
        API api = new API(this.contextReference.get());
        SQLiteDatabase db = Sqlite.getInstance(this.contextReference.get(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        if( action == null )
            return null;
        switch (action){
            case HOME:
                apiResponse = api.getHomeTimelineCache(max_id);
                break;
            case LOCAL:
                apiResponse = api.getPublicTimeline(true, max_id);
                break;
            case PUBLIC:
                apiResponse = api.getPublicTimeline(false, max_id);
                break;
            case NEWS:
                apiResponse = api.getNews(max_id);
                break;
            case SCHEDULED_TOOTS:
                apiResponse = api.scheduledAction("GET", null, max_id, null);
                break;
            case DIRECT:
                apiResponse = api.getDirectTimeline(max_id);
                break;
            case CONVERSATION:
                apiResponse = api.getConversationTimeline(max_id);
                break;
            case REMOTE_INSTANCE_FILTERED:
                if( this.social != null && this.social.equals("MASTODON")) {
                    apiResponse = api.getPublicTimelineTag(this.currentfilter, true, max_id,this.remoteInstance);
                    if( apiResponse != null){
                        List<app.fedilab.android.client.Entities.Status> statusesTemp = apiResponse.getStatuses();
                        if( statusesTemp != null){
                            for(app.fedilab.android.client.Entities.Status status: statusesTemp){
                                status.setType(action);
                            }
                        }
                    }
                } else if(this.social != null && this.social.equals("GNU") ) {
                    GNUAPI gnuapi = new GNUAPI(this.contextReference.get());
                    apiResponse = gnuapi.searchRemote(this.remoteInstance,currentfilter,max_id);
                }else {
                    apiResponse = api.searchPeertube(this.remoteInstance, currentfilter);
                }
                break;
            case REMOTE_INSTANCE:
                if( this.name != null && this.remoteInstance != null){ //For Peertube channels
                    apiResponse = api.getPeertubeChannelVideos(this.remoteInstance, this.name);
                }else{ //For other remote instance
                    List<RemoteInstance> remoteInstanceObj = new InstancesDAO(this.contextReference.get(), db).getInstanceByName(this.instanceName);
                    if( remoteInstanceObj != null && remoteInstanceObj.size() > 0 && remoteInstanceObj.get(0).getType().equals("MASTODON")) {
                        apiResponse = api.getPublicTimeline(this.instanceName, true, max_id);
                        List<app.fedilab.android.client.Entities.Status> statusesTemp = apiResponse.getStatuses();
                        if( statusesTemp != null){
                            for(app.fedilab.android.client.Entities.Status status: statusesTemp){
                                status.setType(action);
                            }
                        }
                    }else if(remoteInstanceObj != null && remoteInstanceObj.size() > 0 && remoteInstanceObj.get(0).getType().equals("MISSKEY")){
                        apiResponse = api.getMisskey(this.instanceName, max_id);
                        List<app.fedilab.android.client.Entities.Status> statusesTemp = apiResponse.getStatuses();
                        if( statusesTemp != null){
                            for(app.fedilab.android.client.Entities.Status status: statusesTemp){
                                status.setType(action);
                            }
                        }
                    } else if(remoteInstanceObj != null && remoteInstanceObj.size() > 0 && remoteInstanceObj.get(0).getType().equals("PIXELFED") ) {
                        apiResponse = api.getPixelfedTimeline(instanceName, max_id);
                    } else if(remoteInstanceObj != null && remoteInstanceObj.size() > 0 && remoteInstanceObj.get(0).getType().equals("GNU") ) {
                        apiResponse = api.getGNUTimeline(instanceName, max_id);
                    }else {
                        apiResponse = api.getPeertube(this.instanceName, max_id);
                    }
                }
                break;
            case FAVOURITES:
                if( MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.GNU && MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA) {
                    apiResponse = api.getFavourites(max_id);
                }else{
                    GNUAPI gnuapi = new GNUAPI(this.contextReference.get());
                    apiResponse = gnuapi.getFavourites(max_id);
                }
                break;
            case USER:
                if(MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON || MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA) {
                    if (showMediaOnly)
                        apiResponse = api.getStatusWithMedia(targetedID, max_id);
                    else if (showPinned)
                        apiResponse = api.getPinnedStatuses(targetedID, max_id);
                    else
                        apiResponse = api.getAccountTLStatuses(targetedID, max_id, !showReply);
                }else if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.GNU ||  MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA){
                    GNUAPI gnuapi = new GNUAPI(this.contextReference.get());
                    if (showMediaOnly)
                        apiResponse = gnuapi.getStatusWithMedia(targetedID, max_id);
                    else if (showPinned)
                        apiResponse = gnuapi.getPinnedStatuses(targetedID, max_id);
                    else
                        apiResponse = gnuapi.getAccountTLStatuses(targetedID, max_id, !showReply);
                }else if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE){
                    PeertubeAPI peertubeAPI = new PeertubeAPI(this.contextReference.get());
                    apiResponse = peertubeAPI.getVideos(targetedID, max_id);
                }
                break;
            case MYVIDEOS:
                PeertubeAPI peertubeAPI = new PeertubeAPI(this.contextReference.get());
                apiResponse = peertubeAPI.getMyVideos(max_id);
                break;
            case PEERTUBE_HISTORY:
                peertubeAPI = new PeertubeAPI(this.contextReference.get());
                apiResponse = peertubeAPI.getMyHistory(max_id);
                break;
            case CHANNEL:
                peertubeAPI = new PeertubeAPI(this.contextReference.get());
                apiResponse = peertubeAPI.getVideosChannel(targetedID, max_id);
                break;
            case ONESTATUS:
                apiResponse = api.getStatusbyId(targetedID);
                break;
            case SEARCH:

                if( !tag.contains("_cache_")) {
                    apiResponse = api.search2(tag, API.searchType.STATUSES, max_id);
                }else{
                    tag = tag.replace("_cache_","");
                    apiResponse = new APIResponse();
                    Results results = new Results();
                    List<app.fedilab.android.client.Entities.Status> statuses = new TimelineCacheDAO(contextReference.get(), db).search(tag, max_id);
                    results.setStatuses(statuses);
                    if( statuses != null && statuses.size() > 0 ) {
                        apiResponse.setMax_id(statuses.get(statuses.size() - 1).getId());
                    }
                    apiResponse.setResults(results);
                }
                break;
            case TAG:
                if( MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.GNU && MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA) {
                    ManageTimelines manageTimelines = new TimelinesDAO(contextReference.get(), db).getById(timelineId);
                    if( manageTimelines != null && manageTimelines.getTagTimeline() != null){
                        boolean isArt = manageTimelines.getTagTimeline().isART();
                        if (isArt)
                            apiResponse = api.getCustomArtTimeline(false,  manageTimelines.getTagTimeline().getName(), max_id, manageTimelines.getTagTimeline().getAny(), manageTimelines.getTagTimeline().getAll(), manageTimelines.getTagTimeline().getNone());
                        else
                            apiResponse = api.getPublicTimelineTag(manageTimelines.getTagTimeline().getName(), false, max_id, manageTimelines.getTagTimeline().getAny(), manageTimelines.getTagTimeline().getAll(), manageTimelines.getTagTimeline().getNone());
                    }else{
                        apiResponse = api.getPublicTimelineTag(tag, false, max_id, null, null, null);
                    }
                }else{
                    GNUAPI gnuapi = new GNUAPI(this.contextReference.get());
                    apiResponse = gnuapi.search(tag,max_id);
                }

                break;
            case ART:
                apiResponse = api.getArtTimeline(false, max_id, null, null, null);
                break;
            case CACHE_BOOKMARKS:
                apiResponse = new APIResponse();
                db = Sqlite.getInstance(contextReference.get(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                List<app.fedilab.android.client.Entities.Status> statuses = new StatusCacheDAO(contextReference.get(), db).getAllStatus(StatusCacheDAO.BOOKMARK_CACHE);
                apiResponse.setStatuses(statuses);
                break;
            case CACHE_BOOKMARKS_PEERTUBE:
                apiResponse = new APIResponse();
                db = Sqlite.getInstance(contextReference.get(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                List<Peertube> peertubes = new PeertubeFavoritesDAO(contextReference.get(), db).getAllPeertube();
                apiResponse.setPeertubes(peertubes);
                break;
            case CACHE_STATUS:
                apiResponse = new APIResponse();
                db = Sqlite.getInstance(contextReference.get(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                statuses = new StatusCacheDAO(contextReference.get(), db).getStatusFromID(StatusCacheDAO.ARCHIVE_CACHE, filterToots, max_id);
                if( statuses != null && statuses.size() > 0) {
                    apiResponse.setStatuses(statuses);
                    apiResponse.setSince_id(statuses.get(0).getId());
                    apiResponse.setMax_id(statuses.get(statuses.size() - 1).getId());
                }else{
                    apiResponse.setStatuses(null);
                    apiResponse.setMax_id(null);
                    apiResponse.setSince_id(null);
                }
                break;

            case PSUBSCRIPTIONS:
                peertubeAPI = new PeertubeAPI(this.contextReference.get());
                apiResponse = peertubeAPI.getSubscriptionsTL(max_id);
                break;
            case POVERVIEW:
                peertubeAPI = new PeertubeAPI(this.contextReference.get());
                apiResponse = peertubeAPI.getOverviewTL(max_id);
                break;
            case PTRENDING:
                peertubeAPI = new PeertubeAPI(this.contextReference.get());
                apiResponse = peertubeAPI.getTrendingTL(max_id);
                break;
            case PRECENTLYADDED:
                peertubeAPI = new PeertubeAPI(this.contextReference.get());
                apiResponse = peertubeAPI.getRecentlyAddedTL(max_id);
                break;
            case PLOCAL:
                peertubeAPI = new PeertubeAPI(this.contextReference.get());
                apiResponse = peertubeAPI.getLocalTL(max_id);
                break;
            case PMYVIDEOS:
                peertubeAPI = new PeertubeAPI(this.contextReference.get());
                apiResponse = peertubeAPI.getLocalTL(max_id);
                break;
            case PF_HOME:
                api = new API(this.contextReference.get());
                apiResponse = api.getHomeTimeline(max_id);
                break;
            case PF_LOCAL:
                api = new API(this.contextReference.get());
                apiResponse = api.getPublicTimeline(true,max_id);
            case PF_DISCOVER:
                api = new API(this.contextReference.get());
                apiResponse = api.getDiscoverTimeline(true,max_id);
                break;
            case HASHTAG:
                break;

            case GNU_HOME:
                GNUAPI gnuAPI = new GNUAPI(this.contextReference.get());
                apiResponse = gnuAPI.getHomeTimeline(max_id);
                break;
            case GNU_LOCAL:
                gnuAPI = new GNUAPI(this.contextReference.get());
                apiResponse = gnuAPI.getPublicTimeline(true,max_id);
                break;
            case GNU_WHOLE:
                gnuAPI = new GNUAPI(this.contextReference.get());
                apiResponse = gnuAPI.getPublicTimeline(false,max_id);
                break;
            case GNU_DM:
                gnuAPI = new GNUAPI(this.contextReference.get());
                apiResponse = gnuAPI.getDirectTimeline(max_id);
                break;
            case GNU_GROUP_TIMELINE:
                gnuAPI = new GNUAPI(this.contextReference.get());
                apiResponse = gnuAPI.getGroupTimeline(tag.trim(), max_id);
                break;
        }
        if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON || MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA) {
            List<String> bookmarks = new StatusCacheDAO(contextReference.get(), db).getAllStatusId(StatusCacheDAO.BOOKMARK_CACHE);
            if (apiResponse != null && apiResponse.getStatuses() != null && bookmarks != null && apiResponse.getStatuses().size() > 0) {
                List<app.fedilab.android.client.Entities.Status> statuses = apiResponse.getStatuses();
                for (app.fedilab.android.client.Entities.Status status : statuses) {
                    status.setBookmarked(bookmarks.contains(status.getId()));
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveFeeds(apiResponse);
    }
}
