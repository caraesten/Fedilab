/* Copyright 2019 Thomas Schneider
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
import app.fedilab.android.client.Entities.Results;
import app.fedilab.android.interfaces.OnRetrieveRemoteAccountInterface;


/**
 * Created by Thomas on 07/02/2019.
 * Retrieve backers and sponsors
 */


public class RetrieveOpenCollectiveAsyncTask extends AsyncTask<Void, Void, Void> {

    private OnRetrieveRemoteAccountInterface listener;
    private String url;
    private Results results;
    private WeakReference<Context> contextReference;
    private Type type;

    public enum Type{
        BACKERS,
        SPONSORS
    }

    public RetrieveOpenCollectiveAsyncTask(Context context, Type type, OnRetrieveRemoteAccountInterface onRetrieveRemoteAccountInterface){
        this.type = type;
        this.listener = onRetrieveRemoteAccountInterface;
        this.contextReference = new WeakReference<>(context);
    }

    @Override
    protected Void doInBackground(Void... params) {
        API api = new API(this.contextReference.get());
        results = api.getOpencollectiveAccounts(type);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveRemoteAccount(results);
    }

}
