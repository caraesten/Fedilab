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
package fr.gouv.etalab.mastodon.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveFeedsInterface;


/**
 * Created by Thomas on 23/04/2017.
 * Retrieves toots on the instance
 */

public class RetrieveFeedsAsyncTask extends AsyncTask<Void, Void, Void> {

    private Context context;
    private Type action;
    private List<fr.gouv.etalab.mastodon.client.Entities.Status> statuses;
    private String max_id;
    private OnRetrieveFeedsInterface listener;
    private String targetedID;
    private fr.gouv.etalab.mastodon.client.Entities.Status status;
    private String tag;
    private API api;

    public enum Type{
        HOME,
        LOCAL,
        PUBLIC,
        HASHTAG,
        USER,
        FAVOURITES,
        ONESTATUS,
        CONTEXT,
        TAG
    }

    public RetrieveFeedsAsyncTask(Context context, Type action, String max_id, OnRetrieveFeedsInterface onRetrieveFeedsInterface){
        this.context = context;
        this.action = action;
        this.max_id = max_id;
        this.listener = onRetrieveFeedsInterface;
    }

    public RetrieveFeedsAsyncTask(Context context, Type action, String targetedID, String max_id, OnRetrieveFeedsInterface onRetrieveFeedsInterface){
        this.context = context;
        this.action = action;
        this.max_id = max_id;
        this.listener = onRetrieveFeedsInterface;
        this.targetedID = targetedID;
    }
    public RetrieveFeedsAsyncTask(Context context, Type action, String tag, String targetedID, String max_id, OnRetrieveFeedsInterface onRetrieveFeedsInterface){
        this.context = context;
        this.action = action;
        this.max_id = max_id;
        this.listener = onRetrieveFeedsInterface;
        this.targetedID = targetedID;
        this.tag = tag;
    }
    @Override
    protected Void doInBackground(Void... params) {

        api = new API(context);
        switch (action){
            case HOME:
                statuses = api.getHomeTimeline(max_id);
                break;
            case LOCAL:
                statuses = api.getPublicTimeline(true, max_id);
                break;
            case PUBLIC:
                statuses = api.getPublicTimeline(false, max_id);
                break;
            case FAVOURITES:
                statuses = api.getFavourites(max_id);
                break;
            case USER:
                statuses = api.getStatus(targetedID, max_id);
                break;
            case ONESTATUS:
                statuses = api.getStatusbyId(targetedID);
                break;
            case TAG:
                statuses = api.getPublicTimelineTag(tag, false, max_id);
                break;
            case HASHTAG:
                break;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveFeeds(statuses, api.getError());
    }

}
