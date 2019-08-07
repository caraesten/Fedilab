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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import app.fedilab.android.R;
import app.fedilab.android.activities.BaseMainActivity;
import app.fedilab.android.activities.MainActivity;
import app.fedilab.android.activities.OwnerStatusActivity;
import app.fedilab.android.client.API;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Notification;
import app.fedilab.android.fragments.DisplayNotificationsFragment;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.services.BackupStatusInDataBaseService;
import app.fedilab.android.services.BackupStatusService;
import app.fedilab.android.sqlite.AccountDAO;
import app.fedilab.android.sqlite.Sqlite;

import static app.fedilab.android.helper.Helper.INTENT_ACTION;
import static app.fedilab.android.helper.Helper.INTENT_TARGETED_ACCOUNT;
import static app.fedilab.android.helper.Helper.NOTIFICATION_INTENT;
import static app.fedilab.android.helper.Helper.PREF_INSTANCE;
import static app.fedilab.android.helper.Helper.PREF_KEY_ID;
import static app.fedilab.android.helper.Helper.canNotify;
import static app.fedilab.android.helper.Helper.notify_user;


/**
 * Created by Thomas on 06/01/2019.
 * backup statuses
 */

public class BackupStatusesSyncJob extends Job {

    static final String BACKUP_SYNC = "job_backup";
    static {
        Helper.installProvider();
    }

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        //Code refresh here

        backupService();
        return Result.SUCCESS;
    }


    public static int schedule(boolean updateCurrent) {

        Set<JobRequest> jobRequests = JobManager.instance().getAllJobRequestsForTag(BACKUP_SYNC);
        if (!jobRequests.isEmpty() && !updateCurrent) {
            return jobRequests.iterator().next().getJobId();
        }

        int jobRequestschedule = -1;
        try {
            jobRequestschedule = new JobRequest.Builder(BackupStatusesSyncJob.BACKUP_SYNC)
                    .setPeriodic(TimeUnit.MINUTES.toMillis(Helper.MINUTES_BETWEEN_BACKUP), TimeUnit.MINUTES.toMillis(5))
                    .setUpdateCurrent(updateCurrent)
                    .setRequiredNetworkType(JobRequest.NetworkType.METERED)
                    .setRequirementsEnforced(false)
                    .build()
                    .schedule();
        }catch (Exception ignored){}

        return jobRequestschedule;
    }


    /**
     * Task in background starts here.
     */
    private void backupService() {
        SQLiteDatabase db = Sqlite.getInstance(getContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        final List<Account> accounts = new AccountDAO(getContext(), db).getAllAccount();
        SharedPreferences sharedpreferences = getContext().getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        for(Account account: accounts) {
            boolean autobackup =  sharedpreferences.getBoolean(Helper.SET_AUTO_BACKUP_STATUSES + account.getId() + account.getInstance(), false);
            if( autobackup) {
                Intent backupIntent = new Intent(getContext(), BackupStatusInDataBaseService.class);
                backupIntent.putExtra("userid", account.getId());
                backupIntent.putExtra("instance", account.getInstance());
                getContext().startService(backupIntent);
            }
        }
    }

}