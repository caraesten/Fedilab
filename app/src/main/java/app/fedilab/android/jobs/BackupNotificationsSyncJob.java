package app.fedilab.android.jobs;
/* Copyright 2019 Thomas Schneider
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
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.services.BackupNotificationInDataBaseService;
import app.fedilab.android.sqlite.AccountDAO;
import app.fedilab.android.sqlite.Sqlite;


/**
 * Created by Thomas on 24/08/2019.
 * backup notifications
 */

public class BackupNotificationsSyncJob extends Job {

    public static final String BACKUP_NOTIFICATIONS_SYNC = "job_backup_notification";

    static {
        Helper.installProvider();
    }

    public static int schedule(boolean updateCurrent) {

        Set<JobRequest> jobRequests = JobManager.instance().getAllJobRequestsForTag(BACKUP_NOTIFICATIONS_SYNC);
        if (!jobRequests.isEmpty() && !updateCurrent) {
            return jobRequests.iterator().next().getJobId();
        }

        int jobRequestschedule = -1;
        try {
            jobRequestschedule = new JobRequest.Builder(BackupNotificationsSyncJob.BACKUP_NOTIFICATIONS_SYNC)
                    .setPeriodic(TimeUnit.MINUTES.toMillis(Helper.MINUTES_BETWEEN_BACKUP), TimeUnit.MINUTES.toMillis(5))
                    .setUpdateCurrent(updateCurrent)
                    .setRequiredNetworkType(JobRequest.NetworkType.METERED)
                    .setRequirementsEnforced(false)
                    .build()
                    .schedule();
        } catch (Exception ignored) {
        }

        return jobRequestschedule;
    }

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        //Code refresh here

        backupService();
        return Result.SUCCESS;
    }

    /**
     * Task in background starts here.
     */
    private void backupService() {
        SQLiteDatabase db = Sqlite.getInstance(getContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        final List<Account> accounts = new AccountDAO(getContext(), db).getAllAccount();
        SharedPreferences sharedpreferences = getContext().getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        for (Account account : accounts) {
            boolean autobackup = sharedpreferences.getBoolean(Helper.SET_AUTO_BACKUP_NOTIFICATIONS + account.getId() + account.getInstance(), false);
            if (autobackup) {
                try {
                    Intent backupIntent = new Intent(getContext(), BackupNotificationInDataBaseService.class);
                    backupIntent.putExtra("userid", account.getId());
                    backupIntent.putExtra("instance", account.getInstance());
                    getContext().startService(backupIntent);
                } catch (Exception ignored) {
                }
            }
        }
    }

}