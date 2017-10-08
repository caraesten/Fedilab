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

import java.util.ArrayList;

import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveSearchDevelopersAccountshInterface;


/**
 * Created by Thomas on 03/06/2017.
 * Retrieves developer from search (ie: starting with @ when writing a toot)
 */

public class RetrieveDeveloperAccountsAsyncTask extends AsyncTask<Void, Void, Void> {

    private Context context;
    private OnRetrieveSearchDevelopersAccountshInterface listener;
    private ArrayList<Account> accounts;

    public RetrieveDeveloperAccountsAsyncTask(Context context, OnRetrieveSearchDevelopersAccountshInterface onRetrieveSearchDevelopersAccountshInterface){
        this.context = context;
        this.listener = onRetrieveSearchDevelopersAccountshInterface;
    }

    @Override
    protected Void doInBackground(Void... params) {
        API api = new API(context);
        accounts = new ArrayList<>();
        APIResponse apiResponse = api.searchAccounts("@tschneider@mastodon.etalab.gouv.fr", 1);
        if( apiResponse.getAccounts() != null && apiResponse.getAccounts().size() > 0)
            accounts.add(apiResponse.getAccounts().get(0));
        apiResponse = api.searchAccounts("@daycode@mastodon.social",1);
        if( apiResponse.getAccounts() != null && apiResponse.getAccounts().size() > 0)
            accounts.add(apiResponse.getAccounts().get(0));
        apiResponse = api.searchAccounts("@PhotonQyv@mastodon.xyz",1);
        if( apiResponse.getAccounts() != null && apiResponse.getAccounts().size() > 0)
            accounts.add(apiResponse.getAccounts().get(0));
        apiResponse = api.searchAccounts("@angrytux@social.tchncs.de",1);
        if( apiResponse.getAccounts() != null && apiResponse.getAccounts().size() > 0)
            accounts.add(apiResponse.getAccounts().get(0));
        return null;
    }
    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveSearchDevelopersAccounts(accounts);
    }

}
