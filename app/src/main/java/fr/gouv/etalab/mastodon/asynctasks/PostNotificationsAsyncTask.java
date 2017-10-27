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
import fr.gouv.etalab.mastodon.client.Entities.Notification;
import fr.gouv.etalab.mastodon.interfaces.OnPostNotificationsActionInterface;


/**
 * Created by Thomas on 29/07/2017.
 * Posts to delete one or all notifications
 */

public class PostNotificationsAsyncTask extends AsyncTask<Void, Void, Void> {

    private OnPostNotificationsActionInterface listener;
    private APIResponse apiResponse;
    private Notification notification;
    private WeakReference<Context> contextReference;

    public PostNotificationsAsyncTask(Context context, Notification notification, OnPostNotificationsActionInterface onPostNotificationsActionInterface){
        this.contextReference = new WeakReference<>(context);
        this.listener = onPostNotificationsActionInterface;
        this.notification = notification;
    }

    @Override
    protected Void doInBackground(Void... params) {
        if( notification  != null)
            apiResponse = new API(this.contextReference.get()).postNoticationAction(notification.getId());
        else //Delete all notifications
            apiResponse = new API(this.contextReference.get()).postNoticationAction(null);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onPostNotificationsAction(apiResponse, notification);
    }

}
