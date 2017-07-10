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
 * You should have received a copy of the GNU General Public License along with Thomas Schneider; if not,
 * see <http://www.gnu.org/licenses>. */
package fr.gouv.etalab.mastodon.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveAccountsInterface;


/**
 * Created by Thomas on 27/04/2017.
 * Retrieves accounts on the instance
 */

public class RetrieveAccountsAsyncTask extends AsyncTask<Void, Void, Void> {

    private Context context;
    private Type action;
    private APIResponse apiResponse;
    private String max_id;
    private OnRetrieveAccountsInterface listener;
    private String targetedId;

    public enum Type{
        BLOCKED,
        MUTED,
        FOLLOWING,
        FOLLOWERS
    }

    public RetrieveAccountsAsyncTask(Context context, Type action, String targetedId, String max_id, OnRetrieveAccountsInterface onRetrieveAccountsInterface){
        this.context = context;
        this.action = action;
        this.max_id = max_id;
        this.listener = onRetrieveAccountsInterface;
        this.targetedId = targetedId;
    }

    public RetrieveAccountsAsyncTask(Context context, Type action, String max_id, OnRetrieveAccountsInterface onRetrieveAccountsInterface){
        this.context = context;
        this.action = action;
        this.max_id = max_id;
        this.listener = onRetrieveAccountsInterface;
    }

    @Override
    protected Void doInBackground(Void... params) {

        API api = new API(context);
        switch (action){
            case BLOCKED:
                apiResponse = api.getBlocks(max_id);
                break;
            case MUTED:
                apiResponse = api.getMuted(max_id);
                break;
            case FOLLOWING:
                apiResponse = api.getFollowing(targetedId, max_id);
                break;
            case FOLLOWERS:
                apiResponse = api.getFollowers(targetedId, max_id);
                break;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveAccounts(apiResponse);
    }

}
