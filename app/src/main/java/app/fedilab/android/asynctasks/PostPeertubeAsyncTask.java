/* Copyright 2019 Thomas Schneider
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

import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Peertube;
import app.fedilab.android.client.PeertubeAPI;
import app.fedilab.android.interfaces.OnRetrievePeertubeInterface;


/**
 * Created by Thomas on 09/01/2019.
 * Update a Peertube video
 */

public class PostPeertubeAsyncTask extends AsyncTask<Void, Void, Void> {



    private APIResponse apiResponse;
    private OnRetrievePeertubeInterface listener;
    private WeakReference<Context> contextReference;
    private Peertube peertube;



    public PostPeertubeAsyncTask(Context context, Peertube peertube, OnRetrievePeertubeInterface onRetrievePeertubeInterface){
        this.contextReference = new WeakReference<>(context);
        this.listener = onRetrievePeertubeInterface;
        this.peertube = peertube;
    }



    @Override
    protected Void doInBackground(Void... params) {
        PeertubeAPI peertubeAPI = new PeertubeAPI(this.contextReference.get());
        apiResponse = peertubeAPI.updateVideo(peertube);
        if( apiResponse != null && apiResponse.getPeertubes() != null && apiResponse.getPeertubes().size() > 0)
            apiResponse.getPeertubes().get(0).setUpdate(true);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrievePeertube(apiResponse);
    }
}
