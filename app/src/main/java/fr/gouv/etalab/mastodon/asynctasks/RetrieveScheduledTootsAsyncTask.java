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
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.SystemClock;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import java.util.List;
import java.util.Set;

import fr.gouv.etalab.mastodon.client.Entities.StoredStatus;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveScheduledTootsInterface;
import fr.gouv.etalab.mastodon.jobs.ScheduledTootsSyncJob;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import fr.gouv.etalab.mastodon.sqlite.StatusStoredDAO;


/**
 * Created by Thomas on 16/07/2017.
 * Retrieves scheduled toots for an account
 */

public class RetrieveScheduledTootsAsyncTask extends AsyncTask<Void, Void, Void> {

    private Context context;
    private OnRetrieveScheduledTootsInterface listener;
    private List<StoredStatus> storedStatuses;

    public RetrieveScheduledTootsAsyncTask(Context context, OnRetrieveScheduledTootsInterface onRetrieveScheduledTootsInterface){
        this.context = context;
        this.listener = onRetrieveScheduledTootsInterface;

    }

    @Override
    protected Void doInBackground(Void... params) {
        SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        //Retrieves job asked by the user
        storedStatuses = new StatusStoredDAO(context, db).getAllScheduled();
        //Retrieves real jobs still waiting
        Set<JobRequest> jobRequests = JobManager.instance().getAllJobRequestsForTag(ScheduledTootsSyncJob.SCHEDULED_TOOT);
        int[] jobIds;
        if( jobRequests != null && jobRequests.size() > 0 ){
            int i = 0;
            jobIds = new int[jobRequests.size()];
            for(JobRequest jobRequest : jobRequests){
                jobIds[i] = jobRequest.getJobId();
                i++;
            }
        }else{
            jobIds = new int[]{};
        }
        if( storedStatuses != null && storedStatuses.size() > 0 ){
            for(StoredStatus ss: storedStatuses){
                if (!Helper.isJobPresent(jobIds, ss.getJobId())){
                    //JobId is fixed to -1 which means an error occured (it was never sent)
                    new StatusStoredDAO(context, db).updateJobId(ss.getId(),-1);
                }
            }
            //Lets time to update db before dispaying
            SystemClock.sleep(1000);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveScheduledToots(storedStatuses);
    }

}
