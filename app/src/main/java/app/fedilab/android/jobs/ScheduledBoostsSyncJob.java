package app.fedilab.android.jobs;
/* Copyright 2018 Thomas Schneider
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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import app.fedilab.android.client.API;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Status;
import app.fedilab.android.client.Entities.StoredStatus;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.sqlite.AccountDAO;
import app.fedilab.android.sqlite.BoostScheduleDAO;
import app.fedilab.android.sqlite.Sqlite;


/**
 * Created by Thomas on 09/12/2018.
 * Scheduled a boost a datetime
 */

public class ScheduledBoostsSyncJob extends Job {

    public static final String SCHEDULED_BOOST = "job_scheduled_boost";
    static {
        Helper.installProvider();
    }

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        //Code refresh here
        int jobId = params.getId();
        SQLiteDatabase db = Sqlite.getInstance(getContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        //Retrieves the stored status
        StoredStatus storedStatus = new BoostScheduleDAO(getContext(), db).getStatusScheduled(jobId);
        if( storedStatus != null){
            String userId = storedStatus.getUserId();
            String instance = storedStatus.getInstance();
            if( instance != null && userId != null){
                Account account = new AccountDAO(getContext(), db).getUniqAccount(userId, instance);
                if( account != null){
                    //Retrieves the linked status to toot
                    Status status = storedStatus.getStatus();
                    if( status != null){
                        int statusCode = new API(getContext(), account.getInstance(), account.getToken()).postAction( API.StatusAction.REBLOG, status.getId());

                        //Toot was sent
                        if( statusCode == 200){
                            new BoostScheduleDAO(getContext(), db).updateScheduledDone(jobId, new Date());
                        }
                    }
                }
            }
        }
        return Result.SUCCESS;
    }


    public static int schedule(Context context, Status status, long timestampScheduling){

        long startMs = (timestampScheduling -  new Date().getTime());
        long endMs = startMs + TimeUnit.MINUTES.toMillis(5);
        SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();

        int jobId = new  JobRequest.Builder(ScheduledBoostsSyncJob.SCHEDULED_BOOST)
                .setExecutionWindow(startMs, endMs)
                .setUpdateCurrent(false)
                .setRequiredNetworkType(JobRequest.NetworkType.METERED)
                .setRequirementsEnforced(false)
                .build()
                .schedule();
        new BoostScheduleDAO(context, db).insert(status, jobId, new Date(timestampScheduling));
        return jobId;
    }


    public static int scheduleUpdate(Context context, int tootStoredId, long timestampScheduling){

        long startMs = (timestampScheduling -  new Date().getTime());
        long endMs = startMs + TimeUnit.MINUTES.toMillis(5);
        SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();

        int jobId = new  JobRequest.Builder(ScheduledTootsSyncJob.SCHEDULED_TOOT)
                .setExecutionWindow(startMs, endMs)
                .setUpdateCurrent(false)
                .setRequiredNetworkType(JobRequest.NetworkType.METERED)
                .setRequirementsEnforced(false)
                .build()
                .schedule();
        new BoostScheduleDAO(context, db).updateScheduledDate(tootStoredId, jobId, new Date(timestampScheduling));
        return jobId;
    }


}
