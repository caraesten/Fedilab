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
import java.util.List;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveManyRelationshipsInterface;

/**
 * Created by Thomas on 16/09/2017.
 * Retrieves many relationship between the authenticated user and other accounts
 */

public class RetrieveManyRelationshipsAsyncTask extends AsyncTask<Void, Void, Void> {

    private Context context;
    private List<Account> accounts;
    private OnRetrieveManyRelationshipsInterface listener;
    private APIResponse apiResponse;

    public RetrieveManyRelationshipsAsyncTask(Context context, List<Account> accounts, OnRetrieveManyRelationshipsInterface onRetrieveManyRelationshipsInterface){
        this.context = context;
        this.listener = onRetrieveManyRelationshipsInterface;
        this.accounts = accounts;
    }

    @Override
    protected Void doInBackground(Void... params) {
        apiResponse = new API(context).getRelationship(accounts);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveRelationship(apiResponse);
    }

}
