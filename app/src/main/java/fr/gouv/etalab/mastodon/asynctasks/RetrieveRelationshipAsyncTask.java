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
import android.util.Log;

import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.Entities.Relationship;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveRelationshipInterface;

/**
 * Created by Thomas on 01/05/2017.
 * Retrieves relationship between the authenticated user and another account
 */

public class RetrieveRelationshipAsyncTask extends AsyncTask<Void, Void, Void> {

    private Context context;
    private String accountId;
    private Relationship relationship;
    private OnRetrieveRelationshipInterface listener;
    private API api;

    public RetrieveRelationshipAsyncTask(Context context, String accountId, OnRetrieveRelationshipInterface onRetrieveRelationshipInterface){
        this.context = context;
        this.listener = onRetrieveRelationshipInterface;
        this.accountId = accountId;
    }

    @Override
    protected Void doInBackground(Void... params) {
        api = new API(context);
        relationship = api.getRelationship(accountId);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveRelationship(relationship, api.getError());
    }

}
