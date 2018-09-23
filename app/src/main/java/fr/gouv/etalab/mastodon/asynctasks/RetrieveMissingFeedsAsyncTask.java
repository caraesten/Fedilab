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
import java.util.ArrayList;
import java.util.List;
import fr.gouv.etalab.mastodon.activities.MainActivity;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveMissingFeedsInterface;


/**
 * Created by Thomas on 27/09/2017.
 * Retrieves missing toots since last pause
 */

public class RetrieveMissingFeedsAsyncTask extends AsyncTask<Void, Void, Void> {

    private String since_id;
    private OnRetrieveMissingFeedsInterface listener;
    private List<fr.gouv.etalab.mastodon.client.Entities.Status> statuses = new ArrayList<>();
    private RetrieveFeedsAsyncTask.Type type;
    private WeakReference<Context> contextReference;
    private String remoteInstance;

    public RetrieveMissingFeedsAsyncTask(Context context, String since_id, RetrieveFeedsAsyncTask.Type type, OnRetrieveMissingFeedsInterface onRetrieveMissingFeedsInterface){
        this.contextReference = new WeakReference<>(context);
        this.since_id = since_id;
        this.listener = onRetrieveMissingFeedsInterface;
        this.type = type;
    }

    public RetrieveMissingFeedsAsyncTask(Context context, String remoteInstance, String since_id, RetrieveFeedsAsyncTask.Type type, OnRetrieveMissingFeedsInterface onRetrieveMissingFeedsInterface){
        this.contextReference = new WeakReference<>(context);
        this.since_id = since_id;
        this.listener = onRetrieveMissingFeedsInterface;
        this.type = type;
        this.remoteInstance = remoteInstance;
    }


    @Override
    protected Void doInBackground(Void... params) {
        API api = new API(this.contextReference.get());
        List<fr.gouv.etalab.mastodon.client.Entities.Status> tempStatus;
        APIResponse apiResponse = null;
        if( type == RetrieveFeedsAsyncTask.Type.HOME)
            apiResponse = api.getHomeTimeline(since_id);
        else if( type == RetrieveFeedsAsyncTask.Type.LOCAL)
            apiResponse = api.getPublicTimelineSinceId(true, since_id);
        else if( type == RetrieveFeedsAsyncTask.Type.PUBLIC)
            apiResponse = api.getPublicTimelineSinceId(false, since_id);
        else if (type == RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE)
            apiResponse = api.getInstanceTimelineSinceId(remoteInstance, since_id);
        if (apiResponse != null) {
            tempStatus = apiResponse.getStatuses();
            if( tempStatus != null)
                statuses.addAll(0, tempStatus);
        }
        if (type == RetrieveFeedsAsyncTask.Type.HOME && statuses.size() > 0) {
            MainActivity.lastHomeId = statuses.get(0).getId();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveMissingFeeds(statuses);
    }
}
