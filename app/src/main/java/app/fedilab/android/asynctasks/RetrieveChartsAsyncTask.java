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
import java.util.Date;

import app.fedilab.android.client.Entities.Charts;
import app.fedilab.android.interfaces.OnRetrieveChartsInterface;
import app.fedilab.android.sqlite.Sqlite;
import app.fedilab.android.sqlite.StatusCacheDAO;


/**
 * Created by Thomas on 28/07/2019.
 * Creates charts for an account
 */

public class RetrieveChartsAsyncTask extends AsyncTask<Void, Void, Void> {


    private OnRetrieveChartsInterface listener;
    private WeakReference<Context> contextReference;
    private Charts charts;
    private Date dateIni;
    private Date dateEnd;

    public RetrieveChartsAsyncTask(Context context, Date dateIni, Date dateEnd, OnRetrieveChartsInterface onRetrieveChartsInterface){
        this.contextReference = new WeakReference<>(context);
        this.listener = onRetrieveChartsInterface;
        this.dateIni = dateIni;
        this.dateEnd = dateEnd;
    }


    @Override
    protected Void doInBackground(Void... params) {

        SQLiteDatabase db = Sqlite.getInstance(contextReference.get(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        charts = new StatusCacheDAO(contextReference.get(), db).getCharts(dateIni, dateEnd);

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onCharts(charts);
    }

}
