package app.fedilab.android.services;
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

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.List;

import app.fedilab.android.R;
import app.fedilab.android.activities.MainActivity;
import app.fedilab.android.client.API;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Notification;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.sqlite.AccountDAO;
import app.fedilab.android.sqlite.NotificationCacheDAO;
import app.fedilab.android.sqlite.Sqlite;
import es.dmoral.toasty.Toasty;


/**
 * Created by Thomas on 24/08/2019.
 * Manage service for owner notification backup in database
 */

public class BackupNotificationInDataBaseService extends IntentService {


    private static int instanceRunning = 0;
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    @SuppressWarnings("unused")
    public BackupNotificationInDataBaseService(String name) {
        super(name);
    }
    @SuppressWarnings("unused")
    public BackupNotificationInDataBaseService() {
        super("BackupNotificationInDataBaseService");
    }



    public void onCreate() {
        super.onCreate();
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        boolean toastMessage = true;
        String userId = null;
        String instance = null;

        if( intent != null && intent.hasExtra("userid") && intent.hasExtra("instance")){
            userId = intent.getStringExtra("userid");
            instance = intent.getStringExtra("instance");
            toastMessage = false;
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
        SQLiteDatabase db = Sqlite.getInstance(BackupNotificationInDataBaseService.this, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        Account account = new AccountDAO(getApplicationContext(), db).getUniqAccount(userId, instance);
        API api = new API(getApplicationContext(), account.getInstance(), account.getToken());
        //new NotificationCacheDAO(getApplicationContext(), db).removeAll();
        try {
            //Starts from the last recorded ID
            String lastId = new NotificationCacheDAO(BackupNotificationInDataBaseService.this, db).getLastNotificationIDCache(userId, instance);
            String max_id = null;
            List<Notification> backupNotifications = new ArrayList<>();
            boolean canContinue = true;
            do {
                APIResponse apiResponse = api.getNotifications(max_id);

                max_id = apiResponse.getMax_id();
                List<Notification> notifications = apiResponse.getNotifications();
                for(Notification tmpNotification : notifications) {
                    if(lastId != null && tmpNotification.getId().compareTo(lastId) <= 0){
                        canContinue = false;
                        break;
                    }

                    new NotificationCacheDAO(BackupNotificationInDataBaseService.this, db).insertNotification(tmpNotification, userId, instance);
                    backupNotifications.add(tmpNotification);
                }
                SystemClock.sleep(1000);
            }while (max_id != null && canContinue);

            if(backupNotifications.size() > 0){
                Intent backupIntent = new Intent(Helper.INTENT_BACKUP_FINISH);
                LocalBroadcastManager.getInstance(this).sendBroadcast(backupIntent);
            }
            message = getString(R.string.data_backup_success, String.valueOf(backupNotifications.size()));
            Intent mainActivity = new Intent(BackupNotificationInDataBaseService.this, MainActivity.class);
            mainActivity.putExtra(Helper.INTENT_ACTION, Helper.BACKUP_NOTIFICATION_INTENT);
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
