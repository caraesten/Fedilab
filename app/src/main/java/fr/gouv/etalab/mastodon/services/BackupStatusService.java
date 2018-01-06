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
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.Html;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Attachment;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;

import static fr.gouv.etalab.mastodon.helper.Helper.notify_user;


/**
 * Created by Thomas on 06/01/2018.
 * Manage service for owner status backup
 */

public class BackupStatusService extends IntentService {


    private static int instanceRunning = 0;
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    @SuppressWarnings("unused")
    public BackupStatusService(String name) {
        super(name);
    }
    @SuppressWarnings("unused")
    public BackupStatusService() {
        super("BackupStatusService");
    }



    public void onCreate() {
        super.onCreate();
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if( instanceRunning == 0 ){
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), R.string.data_export_start, Toast.LENGTH_LONG).show();
                }
            });
        }else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), R.string.data_export_running, Toast.LENGTH_LONG).show();
                }
            });
            return;
        }
        instanceRunning++;
        String message;
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        SQLiteDatabase db = Sqlite.getInstance(BackupStatusService.this, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        Account account = new AccountDAO(getApplicationContext(), db).getAccountByID(userId);
        API api = new API(getApplicationContext(), account.getInstance(), account.getToken());
        try {
            String fullPath;
            Intent intentOpen;
            String max_id = null;
            int statusToBackUp = account.getStatuses_count();
            List<Status> backupStatus = new ArrayList<>();
            do {
                APIResponse apiResponse = api.getStatus(userId, max_id);
                max_id = apiResponse.getMax_id();
                List<Status> statuses = apiResponse.getStatuses();
                if (statuses.size() > 0)
                    backupStatus.addAll(statuses);
            }while (max_id != null);

            String fileName = account.getAcct()+"@"+account.getInstance()+ Helper.dateFileToString(getApplicationContext(), new Date())+".csv";
            String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
            fullPath = filePath+"/"+fileName;
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(fullPath)), "UTF-8"));
            StringBuilder builder = new StringBuilder();
            builder.append("id").append(',');
            builder.append("uri").append(',');
            builder.append("url").append(',');
            builder.append("account").append(',');
            builder.append("in_reply_to_id").append(',');
            builder.append("in_reply_to_account_id").append(',');
            builder.append("content").append(',');
            builder.append("created_at").append(',');
            builder.append("reblogs_count").append(',');
            builder.append("favourites_count").append(',');
            builder.append("sensitive").append(',');
            builder.append("spoiler_text").append(',');
            builder.append("visibility").append(',');
            builder.append("media_attachments");
            builder.append('\n');
            for( Status status: backupStatus){
                //excludes reblog
                if( status.getReblog() != null){
                    statusToBackUp = statusToBackUp - 1;
                    continue;
                }
                builder.append("\"").append(status.getId()).append("\"").append(',');
                builder.append("\"").append(status.getUri()).append("\"").append(',');
                builder.append("\"").append(status.getUrl()).append("\"").append(',');
                builder.append("\"").append(status.getAccount().getAcct()).append("\"").append(',');
                builder.append("\"").append(status.getIn_reply_to_id()).append("\"").append(',');
                builder.append("\"").append(status.getIn_reply_to_account_id()).append("\"").append(',');
                String content;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    content = Html.fromHtml(status.getContent(), Html.FROM_HTML_MODE_LEGACY).toString();
                else
                    //noinspection deprecation
                    content = Html.fromHtml(status.getContent()).toString();
                builder.append("\"").append(content.replace("\"", "'").replace("\n"," ")).append("\"").append(',');
                builder.append("\"").append(Helper.shortDateTime(getApplicationContext(), status.getCreated_at())).append("\"").append(',');
                builder.append("\"").append(String.valueOf(status.getReblogs_count())).append("\"").append(',');
                builder.append("\"").append(String.valueOf(status.getFavourites_count())).append("\"").append(',');
                builder.append("\"").append(String.valueOf(status.isSensitive())).append("\"").append(',');
                builder.append("\"").append(status.getSpoiler_text() !=null?status.getSpoiler_text():"").append("\"").append(',');
                builder.append("\"").append(status.getVisibility()).append("\"").append(',');
                if( status.getMedia_attachments() != null && status.getMedia_attachments().size() > 0){
                    builder.append("\"");
                    for(Attachment attachment: status.getMedia_attachments()){
                        builder.append(attachment.getUrl()).append(" ");
                    }
                    builder.append("\"");
                }else {
                    builder.append("\"\"");
                }
                builder.append('\n');
            }
            pw.write(builder.toString());
            pw.close();
            message = getString(R.string.data_export_success, String.valueOf(statusToBackUp), String.valueOf(backupStatus.size()));
            intentOpen = new Intent();
            intentOpen.setAction(android.content.Intent.ACTION_VIEW);
            Uri uri = Uri.parse("file://" + fullPath);
            intentOpen.setDataAndType(uri, "text/csv");
            long notif_id = Long.parseLong(account.getId());
            int notificationId = ((notif_id + 3) > 2147483647) ? (int) (2147483647 - notif_id - 3) : (int) (notif_id + 3);
            String title = getString(R.string.data_export_toots, account.getAcct());
            notify_user(getApplicationContext(), intentOpen, notificationId, BitmapFactory.decodeResource(getResources(),
                    R.drawable.mastodonlogo), title, message);
        } catch (Exception e) {
            e.printStackTrace();
            message = getString(R.string.data_export_error, account.getAcct());
            final String finalMessage = message;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), finalMessage, Toast.LENGTH_LONG).show();
                }
            });
        }
        instanceRunning--;

    }


}
