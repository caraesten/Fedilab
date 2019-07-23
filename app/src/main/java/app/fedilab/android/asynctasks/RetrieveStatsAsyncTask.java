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
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import java.lang.ref.WeakReference;
import app.fedilab.android.client.Entities.Statistics;
import app.fedilab.android.interfaces.OnRetrieveStatsInterface;
import app.fedilab.android.sqlite.Sqlite;
import app.fedilab.android.sqlite.StatusCacheDAO;


/**
 * Created by Thomas on 23/07/2019.
 * Retrieves stats for an account
 */

public class RetrieveStatsAsyncTask extends AsyncTask<Void, Void, Void> {


    private OnRetrieveStatsInterface listener;
    private WeakReference<Context> contextReference;
    private Statistics statistics;

    public RetrieveStatsAsyncTask(Context context, OnRetrieveStatsInterface onRetrieveStatsInterface){
        this.contextReference = new WeakReference<>(context);
        this.listener = onRetrieveStatsInterface;
    }


    @Override
    protected Void doInBackground(Void... params) {

        SQLiteDatabase db = Sqlite.getInstance(contextReference.get(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        statistics = new StatusCacheDAO(contextReference.get(), db).getStat();

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onStats(statistics);
    }

}
