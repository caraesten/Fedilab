package fr.gouv.etalab.mastodon.services;
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
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;


/**
 * Created by Thomas on 29/11/2017.
 * Manage service for streaming api and new notifications
 */

public class LiveNotificationService extends IntentService {



    protected Account account;
    private static HashMap<String, BackGroundTask> backGroundTaskHashMap = new HashMap<>();
    @SuppressWarnings("unused")
    public LiveNotificationService(String name) {
        super(name);
    }
    @SuppressWarnings("unused")
    public LiveNotificationService() {
        super("LiveNotificationService");
    }

    public void onCreate() {
        super.onCreate();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        boolean liveNotifications = sharedpreferences.getBoolean(Helper.SET_LIVE_NOTIFICATIONS, true);
        boolean notify = sharedpreferences.getBoolean(Helper.SET_NOTIFY, true);
        if( liveNotifications && notify){
            SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
            List<Account> accountStreams = new AccountDAO(getApplicationContext(), db).getAllAccount();
            if(accountStreams != null)
                for(final Account accountStream: accountStreams){
                    if( backGroundTaskHashMap.containsKey(accountStream.getAcct() + accountStream.getInstance()))
                        if(!backGroundTaskHashMap.get(accountStream.getAcct() + accountStream.getInstance()).isCancelled())
                            backGroundTaskHashMap.get(accountStream.getAcct() + accountStream.getInstance()).cancel(true);
                    BackGroundTask task = new BackGroundTask(getApplicationContext(), accountStream);
                    backGroundTaskHashMap.put(accountStream.getAcct() + accountStream.getInstance(), task);
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                }
        }
    }


}
