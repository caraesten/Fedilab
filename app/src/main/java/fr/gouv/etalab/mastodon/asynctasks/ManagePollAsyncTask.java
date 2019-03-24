/* Copyright 2019 Thomas Schneider
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
import fr.gouv.etalab.mastodon.client.Entities.Poll;
import fr.gouv.etalab.mastodon.interfaces.OnPollInterface;

/**
 * Created by Thomas on 23/03/2019.
 * Manage Poll
 */

public class ManagePollAsyncTask extends AsyncTask<Void, Void, Void> {

    private OnPollInterface listener;
    private APIResponse apiResponse;
    private fr.gouv.etalab.mastodon.client.Entities.Status status;
    private int[] choices;
    private WeakReference<Context> contextReference;
    private Poll poll;
    private type_s type;

    public enum type_s{
        SUBMIT,
        REFRESH
    }

    public ManagePollAsyncTask(Context context, type_s type, fr.gouv.etalab.mastodon.client.Entities.Status status, int[] choices, OnPollInterface onPollInterface){
        this.contextReference = new WeakReference<>(context);
        this.listener = onPollInterface;
        this.status = status;
        this.choices = choices;
        this.type = type;
    }

    @Override
    protected Void doInBackground(Void... params) {
        if( status.getPoll().getId() == null)
            return null;
        if (type == type_s.SUBMIT){
            poll = new API(contextReference.get()).submiteVote(status.getPoll().getId(),choices);
        }else if( type == type_s.REFRESH){
            poll = new API(contextReference.get()).getPoll(status.getPoll().getId());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onPoll(status, poll);
    }

}
