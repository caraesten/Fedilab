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
 * You should have received a copy of the GNU General Public License along with Thomas Schneider; if not,
 * see <http://www.gnu.org/licenses>. */
package fr.gouv.etalab.mastodon.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.interfaces.OnPostActionInterface;


/**
 * Created by Thomas on 29/04/2017.
 * Makes actions for post calls
 */

public class PostActionAsyncTask extends AsyncTask<Void, Void, Void> {

    private Context context;
    private OnPostActionInterface listener;
    private int statusCode;
    private API.StatusAction apiAction;
    private String targetedId;
    private String comment;
    private fr.gouv.etalab.mastodon.client.Entities.Status status;
    private API api;

    public PostActionAsyncTask(Context context, API.StatusAction apiAction, String targetedId, OnPostActionInterface onPostActionInterface){
        this.context = context;
        this.listener = onPostActionInterface;
        this.apiAction = apiAction;
        this.targetedId = targetedId;
    }

    public PostActionAsyncTask(Context context, API.StatusAction apiAction, String targetedId, fr.gouv.etalab.mastodon.client.Entities.Status status, String comment, OnPostActionInterface onPostActionInterface){
        this.context = context;
        this.listener = onPostActionInterface;
        this.apiAction = apiAction;
        this.targetedId = targetedId;
        this.comment = comment;
        this.status = status;
    }

    @Override
    protected Void doInBackground(Void... params) {

        api = new API(context);
        if(apiAction ==  API.StatusAction.REPORT)
            statusCode = api.reportAction(status, comment);
        else if(apiAction == API.StatusAction.CREATESTATUS)
            statusCode = api.statusAction(status);
        else
            statusCode = api.postAction(apiAction, targetedId);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onPostAction(statusCode, apiAction, targetedId, api.getError());
    }

}
