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
import android.os.SystemClock;

import java.lang.ref.WeakReference;
import java.util.List;

import app.fedilab.android.client.API;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.interfaces.OnSyncBookmarksInterface;
import app.fedilab.android.sqlite.Sqlite;
import app.fedilab.android.sqlite.StatusCacheDAO;


/**
 * Created by Thomas on 15/11/2019.
 * Sync bookmarks
 */

public class SyncBookmarksAsyncTask extends AsyncTask<Void, Void, Void> {

    private List<app.fedilab.android.client.Entities.Status> statusList;
    private OnSyncBookmarksInterface listener;
    private WeakReference<Context> contextReference;
    private sync type;
    public SyncBookmarksAsyncTask(Context context, sync type, OnSyncBookmarksInterface onSyncBookmarksInterface) {
        this.contextReference = new WeakReference<>(context);
        this.type = type;
        this.listener = onSyncBookmarksInterface;
    }

    @Override
    protected Void doInBackground(Void... params) {
        SQLiteDatabase db = Sqlite.getInstance(contextReference.get(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        if (type == sync.IMPORT) {
            String max_id = null;
            do {
                APIResponse apiResponse = new API(contextReference.get()).getBookmarks(max_id);
                max_id = apiResponse.getMax_id();
                List<app.fedilab.android.client.Entities.Status> statuses = apiResponse.getStatuses();
                for (app.fedilab.android.client.Entities.Status tmpStatus : statuses) {
                    app.fedilab.android.client.Entities.Status status = new StatusCacheDAO(contextReference.get(), db).getStatus(StatusCacheDAO.BOOKMARK_CACHE, tmpStatus.getId());
                    if (status == null) {
                        new StatusCacheDAO(contextReference.get(), db).insertStatus(StatusCacheDAO.BOOKMARK_CACHE, tmpStatus);
                    }
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    SystemClock.sleep(200);
                }
            } while (max_id != null);
        } else {
            List<app.fedilab.android.client.Entities.Status> statuses = new StatusCacheDAO(contextReference.get(), db).getAllStatus(StatusCacheDAO.BOOKMARK_CACHE);
            if (statuses != null) {
                for (app.fedilab.android.client.Entities.Status tmpStatus : statuses) {
                    new API(contextReference.get()).postAction(API.StatusAction.BOOKMARK, tmpStatus.getId());
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        SystemClock.sleep(200);
                    }
                }
            }
        }
        statusList = new StatusCacheDAO(contextReference.get(), db).getAllStatus(StatusCacheDAO.BOOKMARK_CACHE);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveBookmarks(statusList);
    }

    public enum sync {
        EXPORT,
        IMPORT
    }

}
