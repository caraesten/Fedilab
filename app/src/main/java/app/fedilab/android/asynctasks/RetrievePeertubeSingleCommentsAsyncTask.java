/* Copyright 2018 Thomas Schneider
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
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.interfaces.OnRetrievePeertubeInterface;


/**
 * Created by Thomas on 16/10/2018.
 * Retrieves peertube single
 */

public class RetrievePeertubeSingleCommentsAsyncTask extends AsyncTask<Void, Void, Void> {



    private APIResponse apiResponse;
    private String videoId;
    private OnRetrievePeertubeInterface listener;
    private WeakReference<Context> contextReference;
    private String instanceName;



    public RetrievePeertubeSingleCommentsAsyncTask(Context context, String instanceName, String videoId, OnRetrievePeertubeInterface onRetrievePeertubeInterface){
        this.contextReference = new WeakReference<>(context);
        this.videoId = videoId;
        this.listener = onRetrievePeertubeInterface;
        this.instanceName = instanceName;
    }



    @Override
    protected Void doInBackground(Void... params) {
        API api = new API(this.contextReference.get());
        apiResponse = api.getSinglePeertubeComments(this.instanceName, videoId);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrievePeertubeComments(apiResponse);
    }
}
