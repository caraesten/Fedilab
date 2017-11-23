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
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveSearchStatusInterface;


/**
 * Created by Thomas on 22/11/2017.
 * Retrieves toots from search
 */

public class RetrieveTootsAsyncTask extends AsyncTask<Void, Void, Void> {

    private String query;
    private APIResponse apiResponse;
    private OnRetrieveSearchStatusInterface listener;
    private API api;
    private WeakReference<Context> contextReference;

    public RetrieveTootsAsyncTask(Context context, String query, OnRetrieveSearchStatusInterface onRetrieveSearchStatusInterface){
        this.contextReference = new WeakReference<>(context);
        this.query = query;
        this.listener = onRetrieveSearchStatusInterface;
    }


    @Override
    protected Void doInBackground(Void... params) {
        api = new API(this.contextReference.get());
        apiResponse = api.searchStatus(query, 40);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveSearchStatus(apiResponse, api.getError());
    }

}
