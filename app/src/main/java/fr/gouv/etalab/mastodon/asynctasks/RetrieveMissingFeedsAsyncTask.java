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
import java.util.ArrayList;
import java.util.List;
import fr.gouv.etalab.mastodon.activities.MainActivity;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveMissingFeedsInterface;


/**
 * Created by Thomas on 27/09/2017.
 * Retrieves missing toots since last pause
 */

public class RetrieveMissingFeedsAsyncTask extends AsyncTask<Void, Void, Void> {

    private Context context;
    private String since_id;
    private OnRetrieveMissingFeedsInterface listener;
    private List<fr.gouv.etalab.mastodon.client.Entities.Status> statuses = new ArrayList<>();
    private RetrieveFeedsAsyncTask.Type type;

    public RetrieveMissingFeedsAsyncTask(Context context, String since_id, RetrieveFeedsAsyncTask.Type type, OnRetrieveMissingFeedsInterface onRetrieveMissingFeedsInterface){
        this.context = context;
        this.since_id = since_id;
        this.listener = onRetrieveMissingFeedsInterface;
        this.type = type;
    }


    @Override
    protected Void doInBackground(Void... params) {
        int loopInc = 0;
        API api = new API(context);
        List<fr.gouv.etalab.mastodon.client.Entities.Status> tempStatus;
        APIResponse apiResponse = null;
        while (loopInc < 10){

            if( type == RetrieveFeedsAsyncTask.Type.HOME)
                apiResponse = api.getHomeTimelineSinceId(since_id, 80);
            else if( type == RetrieveFeedsAsyncTask.Type.LOCAL)
                apiResponse = api.getPublicTimelineSinceId(true, since_id, 80);
            else if( type == RetrieveFeedsAsyncTask.Type.PUBLIC)
                apiResponse = api.getPublicTimelineSinceId(false, since_id, 80);
            if (apiResponse == null)
                break;
            String max_id = apiResponse.getMax_id();
            since_id = apiResponse.getSince_id();
            tempStatus = apiResponse.getStatuses();
            if( statuses != null && tempStatus != null)
                statuses.addAll(0, tempStatus);
            loopInc++;
            if( tempStatus == null || max_id == null || max_id.equals(since_id) || tempStatus.size() == 0)
                break;
        }
        if( type == RetrieveFeedsAsyncTask.Type.HOME && statuses != null && statuses.size() > 0) {
            MainActivity.lastHomeId = statuses.get(0).getId();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveMissingFeeds(statuses);
    }
}
