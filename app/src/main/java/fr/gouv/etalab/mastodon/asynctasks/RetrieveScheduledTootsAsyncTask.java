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
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.util.List;
import fr.gouv.etalab.mastodon.client.Entities.StoredStatus;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveScheduledTootsInterface;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import fr.gouv.etalab.mastodon.sqlite.StatusStoredDAO;


/**
 * Created by Thomas on 16/07/2017.
 * Retrieves scheduled toots for an account
 */

public class RetrieveScheduledTootsAsyncTask extends AsyncTask<Void, Void, Void> {

    private Context context;
    private OnRetrieveScheduledTootsInterface listener;
    private List<StoredStatus> storedStatuses;


    public RetrieveScheduledTootsAsyncTask(Context context, OnRetrieveScheduledTootsInterface onRetrieveScheduledTootsInterface){
        this.context = context;
        this.listener = onRetrieveScheduledTootsInterface;

    }

    @Override
    protected Void doInBackground(Void... params) {
        SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        storedStatuses = new StatusStoredDAO(context, db).getAllScheduled();
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveScheduledToots(storedStatuses);
    }

}
