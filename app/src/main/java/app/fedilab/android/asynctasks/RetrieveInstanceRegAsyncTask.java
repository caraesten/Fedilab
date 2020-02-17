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
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.PeertubeAPI;
import app.fedilab.android.interfaces.OnRetrieveInstanceInterface;


/**
 * Created by Thomas on 14/06/2019.
 * Retrieves instances for registration
 */

public class RetrieveInstanceRegAsyncTask extends AsyncTask<Void, Void, Void> {

    private OnRetrieveInstanceInterface listener;
    private APIResponse apiResponse;
    private WeakReference<Context> contextReference;
    private String category;
    private instanceType type;

    public RetrieveInstanceRegAsyncTask(Context context, instanceType type, String category, OnRetrieveInstanceInterface onRetrieveInstanceInterface) {
        this.contextReference = new WeakReference<>(context);
        this.listener = onRetrieveInstanceInterface;
        this.category = category;
        this.type = type;
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (type == instanceType.MASTODON) {
            apiResponse = new API(this.contextReference.get()).getInstanceReg(category);
        } else if (type == instanceType.PEERTUBE) {
            apiResponse = new PeertubeAPI(this.contextReference.get()).getInstanceReg();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveInstance(apiResponse);
    }

    public enum instanceType {
        MASTODON,
        PEERTUBE
    }

}
