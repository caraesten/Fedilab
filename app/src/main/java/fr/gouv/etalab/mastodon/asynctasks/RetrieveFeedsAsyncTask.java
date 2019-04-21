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
package fr.gouv.etalab.mastodon.asynctasks;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;
import java.util.List;

import fr.gouv.etalab.mastodon.activities.MainActivity;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Peertube;
import fr.gouv.etalab.mastodon.client.Entities.RemoteInstance;
import fr.gouv.etalab.mastodon.client.Entities.TagTimeline;
import fr.gouv.etalab.mastodon.client.GNUAPI;
import fr.gouv.etalab.mastodon.client.PeertubeAPI;
import fr.gouv.etalab.mastodon.helper.FilterToots;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveFeedsInterface;
import fr.gouv.etalab.mastodon.sqlite.InstancesDAO;
import fr.gouv.etalab.mastodon.sqlite.PeertubeFavoritesDAO;
import fr.gouv.etalab.mastodon.sqlite.SearchDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import fr.gouv.etalab.mastodon.sqlite.StatusCacheDAO;


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
        ART,
        PEERTUBE,
        NOTIFICATION,
        SEARCH,

        PSUBSCRIPTIONS,
        POVERVIEW,
        PTRENDING,
        PRECENTLYADDED,
        PMYVIDEOS,
        PLOCAL,
        CHANNEL,
        MYVIDEOS,

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

    @Override
    protected Void doInBackground(Void... params) {
        API api = new API(this.contextReference.get());
        SQLiteDatabase db = Sqlite.getInstance(this.contextReference.get(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        switch (action){
            case HOME:
                apiResponse = api.getHomeTimeline(max_id);
                break;
            case LOCAL:
                apiResponse = api.getPublicTimeline(true, max_id);
                break;
            case PUBLIC:
                apiResponse = api.getPublicTimeline(false, max_id);
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
            case REMOTE_INSTANCE:
                if( this.name != null && this.remoteInstance != null){ //For Peertube channels
                    apiResponse = api.getPeertubeChannelVideos(this.remoteInstance, this.name);
                }else{ //For other remote instance
                    List<RemoteInstance> remoteInstanceObj = new InstancesDAO(this.contextReference.get(), db).getInstanceByName(this.instanceName);
                    if( remoteInstanceObj != null && remoteInstanceObj.size() > 0 && remoteInstanceObj.get(0).getType().equals("MASTODON")) {
                        apiResponse = api.getPublicTimeline(this.instanceName, false, max_id);
                        List<fr.gouv.etalab.mastodon.client.Entities.Status> statusesTemp = apiResponse.getStatuses();
                        if( statusesTemp != null){
                            for(fr.gouv.etalab.mastodon.client.Entities.Status status: statusesTemp){
                                status.setType(action);
                            }
                        }
                    }else if(remoteInstanceObj != null && remoteInstanceObj.size() > 0 && remoteInstanceObj.get(0).getType().equals("MISSKEY")){
                        apiResponse = api.getMisskey(this.instanceName, max_id);
                        List<fr.gouv.etalab.mastodon.client.Entities.Status> statusesTemp = apiResponse.getStatuses();
                        if( statusesTemp != null){
                            for(fr.gouv.etalab.mastodon.client.Entities.Status status: statusesTemp){
                                status.setType(action);
                            }
                        }
                    } else if(remoteInstanceObj != null && remoteInstanceObj.size() > 0 && remoteInstanceObj.get(0).getType().equals("PIXELFED") ) {
                        apiResponse = api.getPixelfedTimeline(instanceName, max_id);
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
            case CHANNEL:
                peertubeAPI = new PeertubeAPI(this.contextReference.get());
                apiResponse = peertubeAPI.getVideosChannel(targetedID, max_id);
                break;
            case ONESTATUS:
                apiResponse = api.getStatusbyId(targetedID);
                break;
            case SEARCH:
                apiResponse = api.search2(tag, API.searchType.STATUSES, max_id);
                break;
            case TAG:
                if( MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.GNU && MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA) {
                    List<TagTimeline> tagTimelines = new SearchDAO(contextReference.get(), db).getTimelineInfo(tag);
                    if (tagTimelines != null && tagTimelines.size() > 0) {
                        TagTimeline tagTimeline = tagTimelines.get(0);
                        boolean isArt = tagTimeline.isART();
                        if (isArt)
                            apiResponse = api.getCustomArtTimeline(false, tag, max_id, tagTimelines.get(0).getAny(), tagTimelines.get(0).getAll(), tagTimelines.get(0).getNone());
                        else
                            apiResponse = api.getPublicTimelineTag(tag, false, max_id, tagTimelines.get(0).getAny(), tagTimelines.get(0).getAll(), tagTimelines.get(0).getNone());
                    } else {
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
                List<fr.gouv.etalab.mastodon.client.Entities.Status> statuses = new StatusCacheDAO(contextReference.get(), db).getAllStatus(StatusCacheDAO.BOOKMARK_CACHE);
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
        }
        if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON || MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA) {
            List<String> bookmarks = new StatusCacheDAO(contextReference.get(), db).getAllStatusId(StatusCacheDAO.BOOKMARK_CACHE);
            if (apiResponse != null && apiResponse.getStatuses() != null && bookmarks != null && apiResponse.getStatuses().size() > 0) {
                List<fr.gouv.etalab.mastodon.client.Entities.Status> statuses = apiResponse.getStatuses();
                for (fr.gouv.etalab.mastodon.client.Entities.Status status : statuses) {
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
