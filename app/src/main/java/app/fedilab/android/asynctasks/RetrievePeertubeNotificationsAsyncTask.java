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
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Error;
import app.fedilab.android.client.PeertubeAPI;
import app.fedilab.android.interfaces.OnRetrievePeertubeNotificationsInterface;


/**
 * Created by Thomas on 23/01/2019.
 * Retrieves Peertube notifications on the instance
 */

public class RetrievePeertubeNotificationsAsyncTask extends AsyncTask<Void, Void, Void> {


    private APIResponse apiResponse;
    private String max_id;
    private Account account;
    private OnRetrievePeertubeNotificationsInterface listener;
    private WeakReference<Context> contextReference;

    public RetrievePeertubeNotificationsAsyncTask(Context context, Account account, String max_id, OnRetrievePeertubeNotificationsInterface onRetrievePeertubeNotificationsInterface){
        this.contextReference = new WeakReference<>(context);
        this.max_id = max_id;
        this.listener = onRetrievePeertubeNotificationsInterface;
        this.account = account;
    }


    @Override
    protected Void doInBackground(Void... params) {
        PeertubeAPI api;
        if( account == null) {
            api = new PeertubeAPI(this.contextReference.get());
            apiResponse = api.getNotifications(max_id);
        }else {
            if( this.contextReference.get() == null) {
                apiResponse.setError(new Error());
                return null;
            }
            api = new PeertubeAPI(this.contextReference.get(), account.getInstance(), account.getToken());
            apiResponse = api.getNotificationsSince(max_id);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrievePeertubeNotifications(apiResponse, account);
    }

}
