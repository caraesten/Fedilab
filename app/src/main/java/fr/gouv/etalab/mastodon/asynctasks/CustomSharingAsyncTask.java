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

import java.io.ByteArrayInputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.CustomSharing;
import fr.gouv.etalab.mastodon.client.CustomSharingResponse;
import fr.gouv.etalab.mastodon.interfaces.OnCustomSharingInterface;
import fr.gouv.etalab.mastodon.interfaces.OnUpdateCredentialInterface;

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