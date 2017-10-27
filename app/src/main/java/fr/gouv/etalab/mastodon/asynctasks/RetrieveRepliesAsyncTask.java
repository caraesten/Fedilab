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
import android.os.SystemClock;

import java.lang.ref.WeakReference;
import java.util.List;

import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveRepliesInterface;


/**
 * Created by Thomas on 18/08/2017.
 * Retrieves replies to a toot
 */

public class RetrieveRepliesAsyncTask extends AsyncTask<Void, Void, Void> {

    private APIResponse apiResponse;
    private OnRetrieveRepliesInterface listener;
    private List<fr.gouv.etalab.mastodon.client.Entities.Status> statuses;
    private WeakReference<Context> contextReference;

    public RetrieveRepliesAsyncTask(Context context, List<fr.gouv.etalab.mastodon.client.Entities.Status> statuses, OnRetrieveRepliesInterface onRetrieveRepliesInterface){
        this.contextReference = new WeakReference<>(context);
        this.statuses = statuses;
        this.listener = onRetrieveRepliesInterface;
    }

    @Override
    protected Void doInBackground(Void... params) {

        API api = new API(this.contextReference.get());
        for (fr.gouv.etalab.mastodon.client.Entities.Status status : statuses) {
            fr.gouv.etalab.mastodon.client.Entities.Context statusContext = api.getStatusContext((status.getReblog() != null) ? status.getReblog().getId() : status.getId());
            SystemClock.sleep(25);
            status.setReplies(statusContext.getDescendants());
        }
        apiResponse = new APIResponse();
        apiResponse.setStatuses(statuses);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveReplies(apiResponse);
    }

}
