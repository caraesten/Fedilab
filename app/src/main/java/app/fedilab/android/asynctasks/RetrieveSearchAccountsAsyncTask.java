/* Copyright 2017 Thomas Schneider
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
package app.fedilab.android.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

import app.fedilab.android.client.API;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.interfaces.OnRetrieveSearcAccountshInterface;


/**
 * Created by Thomas on 25/05/2017.
 * Retrieves accounts from search (ie: starting with @ when writing a toot)
 */

public class RetrieveSearchAccountsAsyncTask extends AsyncTask<Void, Void, Void> {

    private String query;
    private APIResponse apiResponse;
    private OnRetrieveSearcAccountshInterface listener;
    private WeakReference<Context> contextReference;
    private boolean following;

    public RetrieveSearchAccountsAsyncTask(Context context, String query, OnRetrieveSearcAccountshInterface onRetrieveSearcAccountshInterface){
        this.contextReference = new WeakReference<>(context);
        this.query = query;
        this.listener = onRetrieveSearcAccountshInterface;
        this.following = false;
    }

    public RetrieveSearchAccountsAsyncTask(Context context, String query, boolean following, OnRetrieveSearcAccountshInterface onRetrieveSearcAccountshInterface){
        this.contextReference = new WeakReference<>(context);
        this.query = query;
        this.listener = onRetrieveSearcAccountshInterface;
        this.following = following;
    }

    @Override
    protected Void doInBackground(Void... params) {
        API api = new API(this.contextReference.get());
        if( !following)
            apiResponse = api.searchAccounts(query, 20);
        else
            apiResponse = new API(contextReference.get()).searchAccounts(query, 20, true);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if( !following)
            listener.onRetrieveSearchAccounts(apiResponse);
        else
            listener.onRetrieveContact(apiResponse);
    }

}
