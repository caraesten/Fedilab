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

import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveNotificationsInterface;



/**
 * Created by Thomas on 28/04/2017.
 * Retrieves notifications on the instance
 */

public class RetrieveNotificationsAsyncTask extends AsyncTask<Void, Void, Void> {


    private APIResponse apiResponse;
    private String max_id;
    private Account account;
    private OnRetrieveNotificationsInterface listener;
    private boolean refreshData;
    private WeakReference<Context> contextReference;
    private boolean display;

    public RetrieveNotificationsAsyncTask(Context context, boolean display, Account account, String max_id, OnRetrieveNotificationsInterface onRetrieveNotificationsInterface){
        this.contextReference = new WeakReference<>(context);
        this.max_id = max_id;
        this.listener = onRetrieveNotificationsInterface;
        this.account = account;
        this.refreshData = true;
        this.display = display;
    }


    @Override
    protected Void doInBackground(Void... params) {
        API api = new API(this.contextReference.get(), account.getInstance(), account.getToken());
        if( account == null)
            apiResponse = api.getNotifications(max_id, display);
        else
            apiResponse = api.getNotificationsSince(max_id, display);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveNotifications(apiResponse, account, refreshData);
    }

}
