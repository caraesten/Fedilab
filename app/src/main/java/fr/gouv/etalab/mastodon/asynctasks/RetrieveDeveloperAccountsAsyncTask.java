/* Copyright 2017 Thomas Schneider
 *
 * This file is a part of Mastodon Etalab for mastodon.etalab.gouv.fr
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Mastodon Etalab is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Thomas Schneider; if not,
 * see <http://www.gnu.org/licenses>. */
package fr.gouv.etalab.mastodon.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import java.util.List;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveSearcAccountshInterface;


/**
 * Created by Thomas on 03/06/2017.
 * Retrieves developer from search (ie: starting with @ when writing a toot)
 */

public class RetrieveDeveloperAccountsAsyncTask extends AsyncTask<Void, Void, Void> {

    private Context context;
    private List<Account> accounts;
    private OnRetrieveSearcAccountshInterface listener;
    private API api;

    public RetrieveDeveloperAccountsAsyncTask(Context context, OnRetrieveSearcAccountshInterface onRetrieveSearcAccountshInterface){
        this.context = context;
        this.listener = onRetrieveSearcAccountshInterface;
    }

    @Override
    protected Void doInBackground(Void... params) {
        api = new API(context);
        accounts = api.searchDeveloper();
        return null;
    }
    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveSearchAccounts(accounts, api.getError());
    }

}
