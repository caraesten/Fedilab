/* Copyright 2018 Thomas Schneider
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

import java.lang.ref.WeakReference;

import fr.gouv.etalab.mastodon.activities.MainActivity;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.PeertubeAPI;
import fr.gouv.etalab.mastodon.interfaces.OnRetrievePeertubeInterface;


/**
 * Created by Thomas on 15/10/2018.
 * Retrieves peertube single
 */

public class RetrievePeertubeSingleAsyncTask extends AsyncTask<Void, Void, Void> {



    private APIResponse apiResponse;
    private String videoId;
    private OnRetrievePeertubeInterface listener;
    private WeakReference<Context> contextReference;
    private String instanceName;



    public RetrievePeertubeSingleAsyncTask(Context context, String instanceName, String videoId, OnRetrievePeertubeInterface onRetrievePeertubeInterface){
        this.contextReference = new WeakReference<>(context);
        this.videoId = videoId;
        this.listener = onRetrievePeertubeInterface;
        this.instanceName = instanceName;
    }



    @Override
    protected Void doInBackground(Void... params) {
        if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON) {
            API api = new API(this.contextReference.get());
            apiResponse = api.getSinglePeertube(this.instanceName, videoId);
        }else if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE){
            PeertubeAPI peertubeAPI = new PeertubeAPI(this.contextReference.get());
            apiResponse = peertubeAPI.getSinglePeertube(this.instanceName, videoId);
            if (apiResponse.getPeertubes() != null && apiResponse.getPeertubes().size() > 0) {
                String rate = new PeertubeAPI(this.contextReference.get()).getRating(videoId);
                if( rate != null)
                    apiResponse.getPeertubes().get(0).setMyRating(rate);
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrievePeertube(apiResponse);
    }
}
