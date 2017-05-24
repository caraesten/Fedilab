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

import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnPostActionInterface;


/**
 * Created by Thomas on 29/04/2017.
 * Makes actions for post calls
 */

public class PostActionAsyncTask extends AsyncTask<Void, Void, Void> {

    private Context context;
    private OnPostActionInterface listener;
    private int statusCode;
    private API.StatusAction statusAction;
    private String statusId;
    private String comment;
    private Account account;
    private fr.gouv.etalab.mastodon.client.Entities.Status status;

    public PostActionAsyncTask(Context context, API.StatusAction statusAction, String statusId, OnPostActionInterface onPostActionInterface){
        this.context = context;
        this.listener = onPostActionInterface;
        this.statusAction = statusAction;
        this.statusId = statusId;
    }

    public PostActionAsyncTask(Context context, API.StatusAction statusAction, String statusId, fr.gouv.etalab.mastodon.client.Entities.Status status, String comment, OnPostActionInterface onPostActionInterface){
        this.context = context;
        this.listener = onPostActionInterface;
        this.statusAction = statusAction;
        this.statusId = statusId;
        this.comment = comment;
        this.status = status;
    }

    @Override
    protected Void doInBackground(Void... params) {

        if(statusAction ==  API.StatusAction.REPORT)
            statusCode = new API(context).reportAction(status, comment);
        else if(statusAction == API.StatusAction.CREATESTATUS)
            statusCode = new API(context).statusAction(status);
        else
            statusCode = new API(context).postAction(statusAction, statusId);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onPostAction(statusCode, statusAction, statusId);
    }

}
