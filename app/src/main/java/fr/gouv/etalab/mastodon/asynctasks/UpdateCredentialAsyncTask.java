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

import java.lang.ref.WeakReference;

import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.interfaces.OnUpdateCredentialInterface;

/**
 * Created by Thomas on 05/06/2017.
 * Update account credential
 */

public class UpdateCredentialAsyncTask extends AsyncTask<Void, Void, Void> {

    private String display_name, note, avatar, header;
    private APIResponse apiResponse;
    private OnUpdateCredentialInterface listener;
    private WeakReference<Context> contextReference;

    public UpdateCredentialAsyncTask(Context context, String display_name, String note, String avatar, String header, OnUpdateCredentialInterface onUpdateCredentialInterface){
        this.contextReference = new WeakReference<>(context);
        this.display_name = display_name;
        this.note = note;
        this.avatar = avatar;
        this.header = header;
        this.listener = onUpdateCredentialInterface;
    }

    @Override
    protected Void doInBackground(Void... params) {
        apiResponse = new API(this.contextReference.get()).updateCredential(display_name, note, avatar, header);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onUpdateCredential(apiResponse);
    }

}