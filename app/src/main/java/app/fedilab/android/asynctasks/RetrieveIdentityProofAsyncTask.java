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

import app.fedilab.android.interfaces.OnRetrieveIdentityProofInterface;


/**
 * Created by Thomas on 17/12/2019.
 * Retrieves identity
 */

public class RetrieveIdentityProofAsyncTask extends AsyncTask<Void, Void, Void> {


    private APIResponse apiResponse;
    private OnRetrieveIdentityProofInterface listener;
    private WeakReference<Context> contextReference;
    private String userId;


    public RetrieveIdentityProofAsyncTask(Context context, String userId, OnRetrieveIdentityProofInterface onRetrieveHowToInterface) {
        this.contextReference = new WeakReference<>(context);
        this.listener = onRetrieveHowToInterface;
        this.userId = userId;
    }


    @Override
    protected Void doInBackground(Void... params) {
        API api = new API(this.contextReference.get());
        apiResponse = api.getIdentityProof(userId);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onIdentityProof(apiResponse);
    }
}
