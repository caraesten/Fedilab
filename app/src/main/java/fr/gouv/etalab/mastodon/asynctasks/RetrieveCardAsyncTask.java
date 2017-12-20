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
import fr.gouv.etalab.mastodon.client.Entities.Card;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveCardInterface;


/**
 * Created by Thomas on 20/12/2017.
 * Retrieves Card
 */

public class RetrieveCardAsyncTask extends AsyncTask<Void, Void, Void> {

    private String statusId;
    private Card card;
    private OnRetrieveCardInterface listener;
    private WeakReference<Context> contextReference;

    public RetrieveCardAsyncTask(Context context, String statusId, OnRetrieveCardInterface onRetrieveCardInterface){
        this.contextReference = new WeakReference<>(context);
        this.statusId = statusId;
        this.listener = onRetrieveCardInterface;
    }


    @Override
    protected Void doInBackground(Void... params) {
        card = new API(contextReference.get()).getCard(statusId);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveAccount(card);
    }

}
