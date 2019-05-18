/* Copyright 2017 Thomas Schneider
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

import app.fedilab.android.client.CustomSharing;
import app.fedilab.android.client.CustomSharingResponse;
import app.fedilab.android.interfaces.OnCustomSharingInterface;

/**
 * Created by Curtis on 13/02/2019.
 * Custom share status metadata to remote content aggregator
 */

public class CustomSharingAsyncTask extends AsyncTask<Void, Void, Void> {

    private String encodedCustomSharingURL;
    private CustomSharingResponse customSharingResponse;
    private OnCustomSharingInterface listener;
    private WeakReference<Context> contextReference;

    public CustomSharingAsyncTask(Context context, String encodedCustomSharingURL, OnCustomSharingInterface onCustomSharingInterface){
        this.contextReference = new WeakReference<>(context);
        this.encodedCustomSharingURL = encodedCustomSharingURL;
        this.listener = onCustomSharingInterface;
    }

    @Override
    protected Void doInBackground(Void... params) {
        customSharingResponse = new CustomSharing(this.contextReference.get()).customShare(encodedCustomSharingURL);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onCustomSharing(customSharingResponse);
    }

}