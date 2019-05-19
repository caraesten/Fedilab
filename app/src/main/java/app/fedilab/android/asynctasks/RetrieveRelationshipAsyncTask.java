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
import app.fedilab.android.client.Entities.Error;
import app.fedilab.android.client.Entities.Relationship;
import app.fedilab.android.client.GNUAPI;
import app.fedilab.android.client.PeertubeAPI;
import app.fedilab.android.activities.MainActivity;
import app.fedilab.android.interfaces.OnRetrieveRelationshipInterface;

/**
 * Created by Thomas on 01/05/2017.
 * Retrieves relationship between the authenticated user and another account
 */

public class RetrieveRelationshipAsyncTask extends AsyncTask<Void, Void, Void> {


    private String accountId;
    private Relationship relationship;
    private OnRetrieveRelationshipInterface listener;
    private Error error;
    private WeakReference<Context> contextReference;

    public RetrieveRelationshipAsyncTask(Context context, String accountId, OnRetrieveRelationshipInterface onRetrieveRelationshipInterface){
        this.contextReference = new WeakReference<>(context);
        this.listener = onRetrieveRelationshipInterface;
        this.accountId = accountId;
    }

    @Override
    protected Void doInBackground(Void... params) {

        if (MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON || MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA) {
            API api = new API(this.contextReference.get());
            relationship = api.getRelationship(accountId);
            error = api.getError();
        } else if (MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.GNU ||  MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA){
            GNUAPI gnuapi = new GNUAPI(this.contextReference.get());
            relationship = gnuapi.getRelationship(accountId);
            error = gnuapi.getError();
        }else {
            PeertubeAPI api = new PeertubeAPI(this.contextReference.get());
            relationship = new Relationship();
            relationship.setFollowing(api.isFollowing(accountId));
            error = api.getError();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveRelationship(relationship, error);
    }

}
