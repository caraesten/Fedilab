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
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveHomeTimelineServiceInterface;

/**
 * Created by Thomas on 20/05/2017.
 * Retrieves home timeline for the authenticated user - used in the service
 */

public class RetrieveHomeTimelineServiceAsyncTask extends AsyncTask<Void, Void, Void> {

    private APIResponse apiResponse;
    private String since_id;
    private Account account;
    private OnRetrieveHomeTimelineServiceInterface listener;
    private WeakReference<Context> contextReference;

    public RetrieveHomeTimelineServiceAsyncTask(Context context, Account account, String since_id, OnRetrieveHomeTimelineServiceInterface onRetrieveHomeTimelineServiceInterface){
        this.contextReference = new WeakReference<>(context);
        this.since_id = since_id;
        this.listener = onRetrieveHomeTimelineServiceInterface;
        this.account = account;
    }

    @Override
    protected Void doInBackground(Void... params) {
        API api = new API(this.contextReference.get(), this.account.getInstance(), this.account.getToken());
        apiResponse = api.getHomeTimelineSinceId(since_id);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveHomeTimelineService(apiResponse, this.account);
    }

}
