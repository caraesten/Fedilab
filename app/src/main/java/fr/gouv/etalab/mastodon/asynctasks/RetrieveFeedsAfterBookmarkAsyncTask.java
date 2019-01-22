/* Copyright 2018 Thomas Schneider
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

import java.lang.ref.WeakReference;

import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveFeedsAfterBookmarkInterface;


/**
 * Created by Thomas on 22/12/2018.
 * Retrieves toots younger than the bookmarks
 */

public class RetrieveFeedsAfterBookmarkAsyncTask extends AsyncTask<Void, Void, Void> {


    private APIResponse apiResponse;
    private OnRetrieveFeedsAfterBookmarkInterface listener;
    private WeakReference<Context> contextReference;
    private String max_id;


    public RetrieveFeedsAfterBookmarkAsyncTask(Context context, String max_id, OnRetrieveFeedsAfterBookmarkInterface onRetrieveFeedsAfterBookmarkInterface){
        this.contextReference = new WeakReference<>(context);
        this.listener = onRetrieveFeedsAfterBookmarkInterface;
        this.max_id = max_id;
    }

    @Override
    protected Void doInBackground(Void... params) {
        API api = new API(this.contextReference.get());
        apiResponse = api.getHomeTimeline(max_id);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveFeedsAfterBookmark(apiResponse);
    }
}
