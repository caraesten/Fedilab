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
 * You should have received a copy of the GNU General Public License along with Thomas Schneider; if not,
 * see <http://www.gnu.org/licenses>. */
package fr.gouv.etalab.mastodon.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveSearcAccountshInterface;


/**
 * Created by Thomas on 25/05/2017.
 * Retrieves accounts from search (ie: starting with @ when writing a toot)
 */

public class RetrieveSearchAccountsAsyncTask extends AsyncTask<Void, Void, Void> {

    private Context context;
    private String query;
    private APIResponse apiResponse;
    private OnRetrieveSearcAccountshInterface listener;

    public RetrieveSearchAccountsAsyncTask(Context context, String query, OnRetrieveSearcAccountshInterface onRetrieveSearcAccountshInterface){
        this.context = context;
        this.query = query;
        this.listener = onRetrieveSearcAccountshInterface;
    }


    @Override
    protected Void doInBackground(Void... params) {
        API api = new API(context);
        apiResponse = api.searchAccounts(query, 10);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveSearchAccounts(apiResponse);
    }

}
