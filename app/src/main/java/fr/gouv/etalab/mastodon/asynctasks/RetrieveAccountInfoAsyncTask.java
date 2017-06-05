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
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveAccountInterface;

/**
 * Created by Thomas on 04/06/2017.
 * Verify credential
 */

public class RetrieveAccountInfoAsyncTask extends AsyncTask<Void, Void, Void> {

    private Context context;
    private OnRetrieveAccountInterface listener;
    private Account account;
    private API api;

    public RetrieveAccountInfoAsyncTask(Context context, OnRetrieveAccountInterface onRetrieveAccountInterface){
        this.context = context;
        this.listener = onRetrieveAccountInterface;
    }

    @Override
    protected Void doInBackground(Void... params) {
        api = new API(context);
        account = api.verifyCredentials();
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveAccount(account, api.getError());
    }

}