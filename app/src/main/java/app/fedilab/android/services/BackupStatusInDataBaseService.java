package app.fedilab.android.services;
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

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import app.fedilab.android.client.API;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Status;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.sqlite.AccountDAO;
import app.fedilab.android.sqlite.Sqlite;
import app.fedilab.android.sqlite.StatusCacheDAO;
import es.dmoral.toasty.Toasty;
import app.fedilab.android.R;
import app.fedilab.android.activities.MainActivity;


/**
 * Created by Thomas on 17/02/2018.
 * Manage service for owner status backup in database
 */

public class BackupStatusInDataBaseService extends IntentService {


    private static int instanceRunning = 0;
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    @SuppressWarnings("unused")
    public BackupStatusInDataBaseService(String name) {
        super(name);
    }
    @SuppressWarnings("unused")
    public BackupStatusInDataBaseService() {
        super("BackupStatusInDataBaseService");
    }



    public void onCreate() {
        super.onCreate();
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        boolean toastMessage = false;
        String userId = null;
        String instance = null;
        if( intent != null){
            userId = intent.getStringExtra("userId");
            instance = intent.getStringExtra("instance");
            toastMessage = true;
        }
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        if( userId == null || instance == null) {
            userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
            instance = sharedpreferences.getString(Helper.PREF_INSTANCE, null);
        }
        boolean finalToastMessage = toastMessage;
        if( instanceRunning == 0 ){
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if(finalToastMessage) {
                        Toasty.info(getApplicationContext(), getString(R.string.data_export_start), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if(finalToastMessage) {
                        Toasty.info(getApplicationContext(), getString(R.string.data_export_running), Toast.LENGTH_LONG).show();
                    }
                }
            });
            return;
        }
        instanceRunning++;
        String message;
        SQLiteDatabase db = Sqlite.getInstance(BackupStatusInDataBaseService.this, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        Account account = new AccountDAO(getApplicationContext(), db).getUniqAccount(userId, instance);
        API api = new API(getApplicationContext(), account.getInstance(), account.getToken());
        try {
            //Starts from the last recorded ID
            String since_id = new StatusCacheDAO(BackupStatusInDataBaseService.this, db).getLastTootIDCache(StatusCacheDAO.ARCHIVE_CACHE);
            String max_id = null;
            List<Status> backupStatus = new ArrayList<>();
            boolean canContinue = true;
            do {
                APIResponse apiResponse = api.getStatus(userId, max_id);
                max_id = apiResponse.getMax_id();
                List<Status> statuses = apiResponse.getStatuses();
                for(Status tmpStatus : statuses) {
                    if(since_id != null && max_id != null && Long.parseLong(tmpStatus.getId()) <= Long.parseLong(since_id)){
                        canContinue = false;
                        break;
                    }
                    new StatusCacheDAO(BackupStatusInDataBaseService.this, db).insertStatus(StatusCacheDAO.ARCHIVE_CACHE, tmpStatus);
                    backupStatus.add(tmpStatus);
                }
            }while (max_id != null && canContinue);

            if(backupStatus.size() > 0){
                Intent backupIntent = new Intent(Helper.INTENT_BACKUP_FINISH);
                LocalBroadcastManager.getInstance(this).sendBroadcast(backupIntent);
            }
            message = getString(R.string.data_backup_success, String.valueOf(backupStatus.size()));
            Intent mainActivity = new Intent(BackupStatusInDataBaseService.this, MainActivity.class);
            mainActivity.putExtra(Helper.INTENT_ACTION, Helper.BACKUP_INTENT);
            String title = getString(R.string.data_backup_toots, account.getAcct());
            if(finalToastMessage) {
                Helper.notify_user(getApplicationContext(), account, mainActivity, BitmapFactory.decodeResource(getResources(),
                        R.drawable.mastodonlogo), Helper.NotifType.BACKUP, title, message);
            }
        } catch (Exception e) {
            e.printStackTrace();
            message = getString(R.string.data_export_error, account.getAcct());
            final String finalMessage = message;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if(finalToastMessage) {
                        Toasty.error(getApplicationContext(), finalMessage, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        instanceRunning--;

    }


}
