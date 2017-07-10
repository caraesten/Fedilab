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
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveAccountsInterface;


/**
 * Created by Thomas on 07/06/2017.
 * Retrieves follow requests sent for the authenticated account
 */

public class RetrieveFollowRequestSentAsyncTask extends AsyncTask<Void, Void, Void> {

    private Context context;
    private APIResponse apiResponse;
    private String max_id;
    private OnRetrieveAccountsInterface listener;


    public RetrieveFollowRequestSentAsyncTask(Context context, String max_id, OnRetrieveAccountsInterface onRetrieveAccountsInterface){
        this.context = context;
        this.max_id = max_id;
        this.listener = onRetrieveAccountsInterface;
    }

    @Override
    protected Void doInBackground(Void... params) {

        apiResponse = new API(context).getFollowRequest(max_id);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveAccounts(apiResponse);
    }

}
