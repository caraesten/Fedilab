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

import app.fedilab.android.client.Entities.PeertubeInformation;
import app.fedilab.android.client.HttpsConnection;
import app.fedilab.android.client.PeertubeAPI;


/**
 * Created by Thomas on 07/01/2019.
 * Retrieves peertube informations
 */

public class RetrievePeertubeInformationAsyncTask extends AsyncTask<Void, Void, Void> {



    private WeakReference<Context> contextReference;
    public static PeertubeInformation peertubeInformation;



    public RetrievePeertubeInformationAsyncTask(Context context){
        this.contextReference = new WeakReference<>(context);
    }

    @Override
    protected Void doInBackground(Void... params) {
        PeertubeAPI peertubeAPI = new PeertubeAPI(this.contextReference.get());
        try {
            peertubeInformation = peertubeAPI.getPeertubeInformation();
        } catch (HttpsConnection.HttpsConnectionException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
    }
}
