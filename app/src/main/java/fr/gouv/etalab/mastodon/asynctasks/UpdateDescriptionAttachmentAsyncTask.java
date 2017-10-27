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
import fr.gouv.etalab.mastodon.client.Entities.Attachment;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveAttachmentInterface;


/**
 * Created by Thomas on 27/10/2017.
 * Updates media description
 */

public class UpdateDescriptionAttachmentAsyncTask extends AsyncTask<Void, Void, Void> {

    private OnRetrieveAttachmentInterface listener;
    private Attachment attachment;
    private String mediaId, description;
    private API api;
    private WeakReference<Context> contextReference;

    public UpdateDescriptionAttachmentAsyncTask(Context context, String mediaId, String description, OnRetrieveAttachmentInterface onRetrieveAttachmentInterface){
        this.contextReference = new WeakReference<>(context);
        this.listener = onRetrieveAttachmentInterface;
        this.description = description;
        this.mediaId = mediaId;
    }

    @Override
    protected Void doInBackground(Void... params) {
        api = new API(this.contextReference.get());
        attachment = api.updateDescription(mediaId, description);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveAttachment(attachment, api.getError());
    }

}
