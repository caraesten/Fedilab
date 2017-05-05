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
import fr.gouv.etalab.mastodon.client.Entities.Results;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveSearchInterface;


/**
 * Created by Thomas on 05/05/2017.
 * Retrieves accounts from search (ie: starting with @ when writing a toot)
 */

public class RetrieveSearchAsyncTask extends AsyncTask<Void, Void, Void> {

    private Context context;
    private String query;
    private Results results;
    private OnRetrieveSearchInterface listener;


    public RetrieveSearchAsyncTask(Context context, String query, OnRetrieveSearchInterface onRetrieveSearchInterface){
        this.context = context;
        this.query = query;
        this.listener = onRetrieveSearchInterface;
    }

    @Override
    protected Void doInBackground(Void... params) {

        results = new API(context).search(query);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveSearch(results);
    }

}
