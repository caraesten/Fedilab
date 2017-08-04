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
import android.os.AsyncTask;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveHomeTimelineServiceInterface;

/**
 * Created by Thomas on 20/05/2017.
 * Retrieves home timeline for the authenticated user - used in the service
 */

public class RetrieveHomeTimelineServiceAsyncTask extends AsyncTask<Void, Void, Void> {

    private Context context;
    private APIResponse apiResponse;
    private String since_id;
    private String acct, userId;
    private OnRetrieveHomeTimelineServiceInterface listener;
    private String instance;
    private String token;

    public RetrieveHomeTimelineServiceAsyncTask(Context context, String instance, String token, String since_id, String acct, String userId, OnRetrieveHomeTimelineServiceInterface onRetrieveHomeTimelineServiceInterface){
        this.context = context;
        this.since_id = since_id;
        this.listener = onRetrieveHomeTimelineServiceInterface;
        this.acct = acct;
        this.instance = instance;
        this.userId = userId;
        this.token = token;
    }

    @Override
    protected Void doInBackground(Void... params) {
        API api = new API(context, instance, token);
        apiResponse = api.getHomeTimelineSinceId(since_id);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveHomeTimelineService(apiResponse, acct, userId);
    }

}
