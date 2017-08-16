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
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveContextInterface;


/**
 * Created by Thomas on 23/04/2017.
 * Retrieves context for a status
 */

public class RetrieveContextAsyncTask extends AsyncTask<Void, Void, Void> {

    private Context context;
    private String statusId;
    private fr.gouv.etalab.mastodon.client.Entities.Context statusContext;
    private OnRetrieveContextInterface listener;
    private API api;

    public RetrieveContextAsyncTask(Context context, String statusId, OnRetrieveContextInterface onRetrieveContextInterface){
        this.context = context;
        this.statusId = statusId;
        this.listener = onRetrieveContextInterface;
    }

    @Override
    protected Void doInBackground(Void... params) {
        api = new API(context);
        statusContext = api.getStatusContext(statusId);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveFeeds(statusContext, api.getError());
    }

}
