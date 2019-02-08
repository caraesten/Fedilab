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

import java.lang.ref.WeakReference;

import fr.gouv.etalab.mastodon.activities.MainActivity;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Error;
import fr.gouv.etalab.mastodon.client.GNUAPI;
import fr.gouv.etalab.mastodon.client.PeertubeAPI;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveAccountInterface;


/**
 * Created by Thomas on 27/04/2017.
 * Retrieves accounts on the instance
 */

public class RetrieveAccountAsyncTask extends AsyncTask<Void, Void, Void> {


    private String targetedId;
    private Account account;
    private OnRetrieveAccountInterface listener;
    private Error error;
    private WeakReference<Context> contextReference;

    public RetrieveAccountAsyncTask(Context context, String targetedId, OnRetrieveAccountInterface onRetrieveAccountInterface){
        this.contextReference = new WeakReference<>(context);
        this.targetedId = targetedId;
        this.listener = onRetrieveAccountInterface;
    }

    @Override
    protected Void doInBackground(Void... params) {
        if(MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON || MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA) {
            API api = new API(this.contextReference.get());
            account = api.getAccount(targetedId);
            error = api.getError();
        }else if(MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE) {
            PeertubeAPI peertubeAPI = new PeertubeAPI(this.contextReference.get());
            account = peertubeAPI.getAccount(targetedId);
            error = peertubeAPI.getError();
        }else if(MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.GNU|| MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA) {
            GNUAPI gnuapi = new GNUAPI(this.contextReference.get());
            account = gnuapi.getAccount(targetedId);
            error = gnuapi.getError();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveAccount(account, error);
    }

}
