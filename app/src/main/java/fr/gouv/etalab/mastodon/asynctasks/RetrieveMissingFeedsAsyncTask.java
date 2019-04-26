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
import java.util.ArrayList;
import java.util.List;

import fr.gouv.etalab.mastodon.activities.MainActivity;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Conversation;
import fr.gouv.etalab.mastodon.client.Entities.TagTimeline;
import fr.gouv.etalab.mastodon.client.GNUAPI;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveMissingFeedsInterface;
import fr.gouv.etalab.mastodon.sqlite.SearchDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;


/**
 * Created by Thomas on 27/09/2017.
 * Retrieves missing toots since last pause
 */

public class RetrieveMissingFeedsAsyncTask extends AsyncTask<Void, Void, Void> {

    private String since_id;
    private OnRetrieveMissingFeedsInterface listener;
    private List<fr.gouv.etalab.mastodon.client.Entities.Status> statuses = new ArrayList<>();
    private RetrieveFeedsAsyncTask.Type type;
    private WeakReference<Context> contextReference;
    private String remoteInstance;

    public RetrieveMissingFeedsAsyncTask(Context context, String since_id, RetrieveFeedsAsyncTask.Type type, OnRetrieveMissingFeedsInterface onRetrieveMissingFeedsInterface){
        this.contextReference = new WeakReference<>(context);
        this.since_id = since_id;
        this.listener = onRetrieveMissingFeedsInterface;
        this.type = type;
    }

    public RetrieveMissingFeedsAsyncTask(Context context, String remoteInstance, String since_id, RetrieveFeedsAsyncTask.Type type, OnRetrieveMissingFeedsInterface onRetrieveMissingFeedsInterface){
        this.contextReference = new WeakReference<>(context);
        this.since_id = since_id;
        this.listener = onRetrieveMissingFeedsInterface;
        this.type = type;
        this.remoteInstance = remoteInstance;
    }


    @Override
    protected Void doInBackground(Void... params) {
        if( this.contextReference.get() == null)
            return null;
        if(MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.GNU && MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA) {
            API api = new API(this.contextReference.get());
            List<fr.gouv.etalab.mastodon.client.Entities.Status> tempStatus = null;
            APIResponse apiResponse = null;
            if (type == RetrieveFeedsAsyncTask.Type.HOME) {
                apiResponse = api.getHomeTimelineSinceId(since_id);
            } else if (type == RetrieveFeedsAsyncTask.Type.DIRECT)
                apiResponse = api.getDirectTimelineSinceId(since_id);
            else if (type == RetrieveFeedsAsyncTask.Type.CONVERSATION)
                apiResponse = api.getConversationTimelineSinceId(since_id);
            else if (type == RetrieveFeedsAsyncTask.Type.LOCAL)
                apiResponse = api.getPublicTimelineSinceId(true, since_id);
            else if (type == RetrieveFeedsAsyncTask.Type.PUBLIC)
                apiResponse = api.getPublicTimelineSinceId(false, since_id);
            else if (type == RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE)
                apiResponse = api.getInstanceTimelineSinceId(remoteInstance, since_id);
            else if (type == RetrieveFeedsAsyncTask.Type.TAG) {
                SQLiteDatabase db = Sqlite.getInstance(contextReference.get(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                List<TagTimeline> tagTimelines = new SearchDAO(contextReference.get(), db).getTimelineInfo(remoteInstance);
                if (tagTimelines != null && tagTimelines.size() > 0) {
                    TagTimeline tagTimeline = tagTimelines.get(0);
                    boolean isArt = tagTimeline.isART();
                    if (isArt)
                        apiResponse = api.getCustomArtTimelineSinceId(false, remoteInstance, since_id, tagTimelines.get(0).getAny(), tagTimelines.get(0).getAll(), tagTimelines.get(0).getNone());
                    else
                        apiResponse = api.getPublicTimelineTagSinceId(remoteInstance, false, since_id, tagTimelines.get(0).getAny(), tagTimelines.get(0).getAll(), tagTimelines.get(0).getNone());
                } else {
                    apiResponse = api.getPublicTimelineTag(remoteInstance, false, since_id, null, null, null);
                }
            } else if (type == RetrieveFeedsAsyncTask.Type.ART)
                apiResponse = api.getArtTimelineSinceId(false, since_id, null, null, null);
            if (apiResponse != null) {
                if (type != RetrieveFeedsAsyncTask.Type.CONVERSATION)
                    tempStatus = apiResponse.getStatuses();
                else {
                    List<Conversation> conversations = apiResponse.getConversations();
                    tempStatus = new ArrayList<>();
                    if (conversations != null && conversations.size() > 0) {
                        for (Conversation conversation : conversations) {
                            fr.gouv.etalab.mastodon.client.Entities.Status status = conversation.getLast_status();
                            List<String> ppConversation = new ArrayList<>();
                            for (Account account : conversation.getAccounts())
                                ppConversation.add(account.getAvatar());
                            status.setConversationProfilePicture(ppConversation);
                            status.setConversationId(conversation.getId());
                            tempStatus.add(status);
                        }
                    }
                }

                if (tempStatus != null)
                    statuses.addAll(0, tempStatus);
            }
            if (type == RetrieveFeedsAsyncTask.Type.HOME && statuses.size() > 0) {
                MainActivity.lastHomeId = statuses.get(0).getId();
            }
        }else{
            GNUAPI gnuapi = new GNUAPI(this.contextReference.get());
            List<fr.gouv.etalab.mastodon.client.Entities.Status> tempStatus = null;
            APIResponse apiResponse = null;
            if (type == RetrieveFeedsAsyncTask.Type.GNU_HOME) {
                apiResponse = gnuapi.getHomeTimelineSinceId(since_id);
            }else if (type == RetrieveFeedsAsyncTask.Type.GNU_LOCAL)
                apiResponse = gnuapi.getPublicTimelineSinceId(true, since_id);
            else if (type == RetrieveFeedsAsyncTask.Type.GNU_WHOLE)
                apiResponse = gnuapi.getPublicTimelineSinceId(false, since_id);
            else if (type == RetrieveFeedsAsyncTask.Type.GNU_TAG) {
                SQLiteDatabase db = Sqlite.getInstance(contextReference.get(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                List<TagTimeline> tagTimelines = new SearchDAO(contextReference.get(), db).getTimelineInfo(remoteInstance);
                if (tagTimelines != null && tagTimelines.size() > 0) {
                    TagTimeline tagTimeline = tagTimelines.get(0);
                    boolean isArt = tagTimeline.isART();
                    if (isArt)
                        apiResponse = gnuapi.getCustomArtTimelineSinceId(false, remoteInstance, since_id, tagTimelines.get(0).getAny(), tagTimelines.get(0).getAll(), tagTimelines.get(0).getNone());
                    else
                        apiResponse = gnuapi.getPublicTimelineTagSinceId(remoteInstance, false, since_id, tagTimelines.get(0).getAny(), tagTimelines.get(0).getAll(), tagTimelines.get(0).getNone());
                } else {
                    apiResponse = gnuapi.getPublicTimelineTag(remoteInstance, false, since_id, tagTimelines.get(0).getAny(), tagTimelines.get(0).getAll(), tagTimelines.get(0).getNone());
                }
            }
            if (apiResponse != null) {
                if (type != RetrieveFeedsAsyncTask.Type.CONVERSATION)
                    tempStatus = apiResponse.getStatuses();
                else {
                    List<Conversation> conversations = apiResponse.getConversations();
                    tempStatus = new ArrayList<>();
                    if (conversations != null && conversations.size() > 0) {
                        for (Conversation conversation : conversations) {
                            fr.gouv.etalab.mastodon.client.Entities.Status status = conversation.getLast_status();
                            List<String> ppConversation = new ArrayList<>();
                            for (Account account : conversation.getAccounts())
                                ppConversation.add(account.getAvatar());
                            status.setConversationProfilePicture(ppConversation);
                            status.setConversationId(conversation.getId());
                            tempStatus.add(status);
                        }
                    }
                }

                if (tempStatus != null)
                    statuses.addAll(0, tempStatus);
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveMissingFeeds(statuses);
    }
}
