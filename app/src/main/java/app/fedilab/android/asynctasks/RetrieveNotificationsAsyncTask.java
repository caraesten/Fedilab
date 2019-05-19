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
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Error;
import app.fedilab.android.client.GNUAPI;
import app.fedilab.android.fragments.DisplayNotificationsFragment;
import app.fedilab.android.activities.MainActivity;
import app.fedilab.android.interfaces.OnRetrieveNotificationsInterface;



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
    private DisplayNotificationsFragment.Type type;

    public RetrieveNotificationsAsyncTask(Context context, DisplayNotificationsFragment.Type type, boolean display, Account account, String max_id, OnRetrieveNotificationsInterface onRetrieveNotificationsInterface){
        this.contextReference = new WeakReference<>(context);
        this.max_id = max_id;
        this.listener = onRetrieveNotificationsInterface;
        this.account = account;
        this.refreshData = true;
        this.display = display;
        this.type = type;
    }


    @Override
    protected Void doInBackground(Void... params) {
        if(MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.GNU && MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA) {
            API api;
            if (account == null) {
                api = new API(this.contextReference.get());
                apiResponse = api.getNotifications(type, max_id, display);
            } else {
                if (this.contextReference.get() == null) {
                    apiResponse.setError(new Error());
                    return null;
                }
                api = new API(this.contextReference.get(), account.getInstance(), account.getToken());
                apiResponse = api.getNotificationsSince(type, max_id, display);
            }
        }else{
            GNUAPI gnuapi;
            if (account == null) {
                gnuapi = new GNUAPI(this.contextReference.get());
                apiResponse = gnuapi.getNotifications(type, max_id, display);
            } else {
                if (this.contextReference.get() == null) {
                    apiResponse.setError(new Error());
                    return null;
                }
                gnuapi = new GNUAPI(this.contextReference.get(), account.getInstance(), account.getToken());
                apiResponse = gnuapi.getNotificationsSince(type, max_id, display);
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveNotifications(apiResponse, account, refreshData);
    }

}
