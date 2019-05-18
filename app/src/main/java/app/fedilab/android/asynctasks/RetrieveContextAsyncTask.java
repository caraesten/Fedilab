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
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

import app.fedilab.android.client.API;
import app.fedilab.android.client.Entities.Error;
import app.fedilab.android.client.GNUAPI;
import app.fedilab.android.activities.MainActivity;
import app.fedilab.android.interfaces.OnRetrieveContextInterface;


/**
 * Created by Thomas on 23/04/2017.
 * Retrieves context for a status
 */

public class RetrieveContextAsyncTask extends AsyncTask<Void, Void, Void> {

    private String statusId;
    private app.fedilab.android.client.Entities.Context statusContext;
    private OnRetrieveContextInterface listener;
    private Error error;
    private WeakReference<Context> contextReference;
    private boolean expanded;
    private boolean directtimeline;


    public RetrieveContextAsyncTask(Context context, boolean expanded,  boolean directtimeline, String statusId, OnRetrieveContextInterface onRetrieveContextInterface){
        this.contextReference = new WeakReference<>(context);
        this.statusId = statusId;
        this.listener = onRetrieveContextInterface;
        this.expanded = expanded;
        this.directtimeline = directtimeline;
    }

    @Override
    protected Void doInBackground(Void... params) {
        if(MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.GNU && MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA) {
            API api = new API(this.contextReference.get());
            statusContext = api.getStatusContext(statusId);
            //Retrieves the first toot
            if (expanded && statusContext != null && statusContext.getAncestors() != null && statusContext.getAncestors().size() > 0) {
                statusContext = api.getStatusContext(statusContext.getAncestors().get(0).getId());
            }
            error = api.getError();
        }else{
            GNUAPI gnuapi = new GNUAPI(this.contextReference.get());
            statusContext = gnuapi.getStatusContext(statusId, directtimeline);
            //Retrieves the first toot
            if (expanded && statusContext != null && statusContext.getAncestors() != null && statusContext.getAncestors().size() > 0) {
                statusContext = gnuapi.getStatusContext(statusContext.getAncestors().get(0).getId(), directtimeline);
            }
            error = gnuapi.getError();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveContext(statusContext, error);
    }

}
