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
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.SystemClock;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Set;

import app.fedilab.android.client.Entities.StoredStatus;
import app.fedilab.android.fragments.DisplayScheduledTootsFragment;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.jobs.ScheduledBoostsSyncJob;
import app.fedilab.android.jobs.ScheduledTootsSyncJob;
import app.fedilab.android.sqlite.BoostScheduleDAO;
import app.fedilab.android.sqlite.Sqlite;
import app.fedilab.android.sqlite.StatusStoredDAO;
import app.fedilab.android.interfaces.OnRetrieveScheduledTootsInterface;


/**
 * Created by Thomas on 16/07/2017.
 * Retrieves scheduled toots for an account
 */

public class RetrieveScheduledTootsAsyncTask extends AsyncTask<Void, Void, Void> {


    private OnRetrieveScheduledTootsInterface listener;
    private List<StoredStatus> storedStatuses;
    private WeakReference<Context> contextReference;
    private DisplayScheduledTootsFragment.typeOfSchedule type;

    public RetrieveScheduledTootsAsyncTask(Context context, DisplayScheduledTootsFragment.typeOfSchedule type, OnRetrieveScheduledTootsInterface onRetrieveScheduledTootsInterface){
        this.contextReference = new WeakReference<>(context);
        this.listener = onRetrieveScheduledTootsInterface;
        this.type = type;
    }

    @Override
    protected Void doInBackground(Void... params) {
        SQLiteDatabase db = Sqlite.getInstance(this.contextReference.get(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        //Retrieves job asked by the user
        if( type == DisplayScheduledTootsFragment.typeOfSchedule.TOOT)
            storedStatuses = new StatusStoredDAO(this.contextReference.get(), db).getAllScheduled();
        else if(type == DisplayScheduledTootsFragment.typeOfSchedule.BOOST )
            storedStatuses = new BoostScheduleDAO(this.contextReference.get(), db).getAllScheduled();
        //Retrieves real jobs still waiting
        Set<JobRequest> jobRequests = null;
        if( type == DisplayScheduledTootsFragment.typeOfSchedule.TOOT)
            jobRequests = JobManager.instance().getAllJobRequestsForTag(ScheduledTootsSyncJob.SCHEDULED_TOOT);
        else if(type == DisplayScheduledTootsFragment.typeOfSchedule.BOOST )
            jobRequests = JobManager.instance().getAllJobRequestsForTag(ScheduledBoostsSyncJob.SCHEDULED_BOOST);
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
                    if( type == DisplayScheduledTootsFragment.typeOfSchedule.TOOT)
                        new StatusStoredDAO(this.contextReference.get(), db).updateJobId(ss.getId(),-1);
                    else if(type == DisplayScheduledTootsFragment.typeOfSchedule.BOOST )
                        new BoostScheduleDAO(this.contextReference.get(), db).updateJobId(ss.getId(),-1);
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
