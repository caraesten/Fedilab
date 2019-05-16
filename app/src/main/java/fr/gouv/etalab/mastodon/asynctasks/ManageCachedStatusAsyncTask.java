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
package fr.gouv.etalab.mastodon.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.interfaces.OnRefreshCachedStatusInterface;

/**
 * Created by Thomas on 12/05/2019.
 * Manage refresh for statuses
 */

public class ManageCachedStatusAsyncTask extends AsyncTask<Void, Void, Void> {

    private OnRefreshCachedStatusInterface listener;
    private String statusId;
    private fr.gouv.etalab.mastodon.client.Entities.Status refreshedStatus;
    private WeakReference<Context> contextReference;




    public ManageCachedStatusAsyncTask(Context context, String statusId, OnRefreshCachedStatusInterface onRefreshCachedStatusInterface){
        this.contextReference = new WeakReference<>(context);
        this.listener = onRefreshCachedStatusInterface;
        this.statusId = statusId;

    }

    @Override
    protected Void doInBackground(Void... params) {
        APIResponse apiResponse = new API(contextReference.get()).getStatusbyIdAndCache(statusId);
        if( apiResponse.getStatuses().size() > 0){
            refreshedStatus = apiResponse.getStatuses().get(0);
            if( refreshedStatus != null){
                refreshedStatus.setcached(true);
            }
        }else {
            refreshedStatus = new fr.gouv.etalab.mastodon.client.Entities.Status();
            refreshedStatus.setId(statusId);

        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRefresh(refreshedStatus);
    }

}
