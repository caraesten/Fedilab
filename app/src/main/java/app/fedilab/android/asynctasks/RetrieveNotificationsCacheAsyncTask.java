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
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;
import java.util.List;

import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Notification;
import app.fedilab.android.helper.FilterNotifications;
import app.fedilab.android.interfaces.OnRetrieveCacheNotificationsInterface;
import app.fedilab.android.sqlite.NotificationCacheDAO;
import app.fedilab.android.sqlite.Sqlite;


/**
 * Created by Thomas on 28/04/2019
 * Retrieves notifications on the instance
 */

public class RetrieveNotificationsCacheAsyncTask extends AsyncTask<Void, Void, Void> {


    private APIResponse apiResponse;
    private String max_id;
    private OnRetrieveCacheNotificationsInterface listener;
    private WeakReference<Context> contextReference;
    private FilterNotifications filterNotifications;

    public RetrieveNotificationsCacheAsyncTask(Context context, FilterNotifications filterNotifications, String max_id, OnRetrieveCacheNotificationsInterface onRetrieveNotificationsInterface) {
        this.contextReference = new WeakReference<>(context);
        this.max_id = max_id;
        this.listener = onRetrieveNotificationsInterface;
        this.filterNotifications = filterNotifications;
    }

    @Override
    protected Void doInBackground(Void... params) {
        SQLiteDatabase db = Sqlite.getInstance(contextReference.get(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        List<Notification> notifications = new NotificationCacheDAO(contextReference.get(), db).getNotificationsFromID(filterNotifications, max_id);
        apiResponse = new APIResponse();
        apiResponse.setNotifications(notifications);
        if (notifications != null && notifications.size() > 0) {
            apiResponse.setMax_id(notifications.get(notifications.size() - 1).getId());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveNotifications(apiResponse);
    }

}
