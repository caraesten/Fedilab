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
import java.util.List;

import app.fedilab.android.client.API;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.GNUAPI;
import app.fedilab.android.activities.MainActivity;
import app.fedilab.android.interfaces.OnRetrieveManyRelationshipsInterface;

/**
 * Created by Thomas on 16/09/2017.
 * Retrieves many relationship between the authenticated user and other accounts
 */

public class RetrieveManyRelationshipsAsyncTask extends AsyncTask<Void, Void, Void> {


    private List<Account> accounts;
    private OnRetrieveManyRelationshipsInterface listener;
    private APIResponse apiResponse;
    private WeakReference<Context> contextReference;

    public RetrieveManyRelationshipsAsyncTask(Context context, List<Account> accounts, OnRetrieveManyRelationshipsInterface onRetrieveManyRelationshipsInterface){
        this.contextReference = new WeakReference<>(context);
        this.listener = onRetrieveManyRelationshipsInterface;
        this.accounts = accounts;
    }

    @Override
    protected Void doInBackground(Void... params) {
        if(MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.GNU && MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA)
            apiResponse = new API(this.contextReference.get()).getRelationship(accounts);
        else
            apiResponse = new GNUAPI(this.contextReference.get()).getRelationship(accounts);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveRelationship(apiResponse);
    }

}
