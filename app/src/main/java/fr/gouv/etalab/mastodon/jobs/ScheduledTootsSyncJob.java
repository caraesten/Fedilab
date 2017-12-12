package fr.gouv.etalab.mastodon.jobs;
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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

import org.conscrypt.Conscrypt;

import java.security.Security;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.client.Entities.StoredStatus;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import fr.gouv.etalab.mastodon.sqlite.StatusStoredDAO;


/**
 * Created by Thomas on 16/07/2017.
 * Scheduled a toot a datetime
 */

public class ScheduledTootsSyncJob extends Job {

    public static final String SCHEDULED_TOOT = "job_scheduled_toot";
    static {
        Security.insertProviderAt(Conscrypt.newProvider("GmsCore_OpenSSL"), 2);
        Security.addProvider(Conscrypt.newProvider());
    }

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        //Code refresh here
        int jobId = params.getId();
        SQLiteDatabase db = Sqlite.getInstance(getContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        //Retrieves the stored status
        StoredStatus storedStatus = new StatusStoredDAO(getContext(), db).getStatusScheduled(jobId);
        if( storedStatus != null){
            String userId = storedStatus.getUserId();
            String instance = storedStatus.getInstance();
            if( instance != null && userId != null){
                Account account = new AccountDAO(getContext(), db).getAccountByUserIDInstance(userId, instance);
                if( account != null){
                    //Retrieves the linked status to toot
                    Status status = storedStatus.getStatus();
                    if( status != null){
                        int statusCode = new API(getContext(), account.getInstance(), account.getToken()).statusAction(status);
                        //Toot was sent
                        if( statusCode == 200){
                            new StatusStoredDAO(getContext(), db).updateScheduledDone(jobId, new Date());
                        }
                    }
                }
            }
        }
        return Result.SUCCESS;
    }


    public static int schedule(Context context, long id, long timestampScheduling){

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
        new StatusStoredDAO(context, db).scheduleStatus(id, jobId, new Date(timestampScheduling));
        return jobId;
    }


}
