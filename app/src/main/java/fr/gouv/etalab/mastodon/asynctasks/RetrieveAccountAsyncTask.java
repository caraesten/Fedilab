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

import fr.gouv.etalab.mastodon.interfaces.OnRetrieveAccountInterface;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.Entities.Account;


/**
 * Created by Thomas on 27/04/2017.
 * Retrieves accounts on the instance
 */

public class RetrieveAccountAsyncTask extends AsyncTask<Void, Void, Void> {

    private Context context;
    private String targetedId;
    private Account account;
    private OnRetrieveAccountInterface listener;
    private API api;

    public RetrieveAccountAsyncTask(Context context, String targetedId, OnRetrieveAccountInterface onRetrieveAccountInterface){
        this.context = context;
        this.targetedId = targetedId;
        this.listener = onRetrieveAccountInterface;
    }

    @Override
    protected Void doInBackground(Void... params) {
        api = new API(context);
        account = api.getAccount(targetedId);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveAccount(account, api.getError());
    }

}
