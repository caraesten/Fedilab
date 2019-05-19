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

import app.fedilab.android.client.API;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Attachment;
import app.fedilab.android.interfaces.OnRetrieveAttachmentInterface;


/**
 * Created by Thomas on 27/10/2017.
 * Updates media description
 */

public class UpdateDescriptionAttachmentAsyncTask extends AsyncTask<Void, Void, Void> {

    private OnRetrieveAttachmentInterface listener;
    private Attachment attachment;
    private String mediaId, description;
    private Account account;
    private API api;
    private WeakReference<Context> contextReference;

    public UpdateDescriptionAttachmentAsyncTask(Context context, String mediaId, String description, Account account, OnRetrieveAttachmentInterface onRetrieveAttachmentInterface){
        this.contextReference = new WeakReference<>(context);
        this.listener = onRetrieveAttachmentInterface;
        this.description = description;
        this.mediaId = mediaId;
        this.account = account;
    }

    @Override
    protected Void doInBackground(Void... params) {
        if( account == null)
            api = new API(this.contextReference.get());
        else
            api = new API(this.contextReference.get(), account.getInstance(), account.getToken());
        attachment = api.updateDescription(mediaId, description);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveAttachment(attachment, null, api.getError());
    }

}
