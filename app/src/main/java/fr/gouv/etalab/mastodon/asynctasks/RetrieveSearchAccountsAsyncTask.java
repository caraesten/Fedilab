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
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveSearcAccountshInterface;


/**
 * Created by Thomas on 25/05/2017.
 * Retrieves accounts from search (ie: starting with @ when writing a toot)
 */

public class RetrieveSearchAccountsAsyncTask extends AsyncTask<Void, Void, Void> {

    private String query;
    private APIResponse apiResponse;
    private OnRetrieveSearcAccountshInterface listener;
    private WeakReference<Context> contextReference;

    public RetrieveSearchAccountsAsyncTask(Context context, String query, OnRetrieveSearcAccountshInterface onRetrieveSearcAccountshInterface){
        this.contextReference = new WeakReference<>(context);
        this.query = query;
        this.listener = onRetrieveSearcAccountshInterface;
    }


    @Override
    protected Void doInBackground(Void... params) {
        API api = new API(this.contextReference.get());
        apiResponse = api.searchAccounts(query, 20);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveSearchAccounts(apiResponse);
    }

}
