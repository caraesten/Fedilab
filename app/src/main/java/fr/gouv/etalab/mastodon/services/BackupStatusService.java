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
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import org.json.JSONObject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        if( userId == null)
            return;
        SQLiteDatabase db = Sqlite.getInstance(BackupStatusService.this, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        Account account = new AccountDAO(getApplicationContext(), db).getAccountByID(userId);
        API api = new API(getApplicationContext(), account.getId(), account.getToken());

        String max_id = "0";
        int statusToBackUp = account.getStatuses_count();
        List<Status> backupStatus = new ArrayList<>();
        while (max_id != null){
            APIResponse apiResponse = api.getStatus(userId, null);
            max_id = apiResponse.getMax_id();
            List<Status> statuses = apiResponse.getStatuses();
            if (statuses.size() > 0)
                backupStatus.addAll(statuses);
        }
        String message;
        String fileName = account.getAcct()+"@"+account.getInstance()+ Helper.dateToString(getApplicationContext(), new Date())+".csv";
        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        String fullPath = filePath+"/"+fileName;
        try {
            FileWriter fw = new FileWriter(fullPath);
            fw.append("id");
            fw.append(',');
            fw.append("uri");
            fw.append(',');
            fw.append("url");
            fw.append(',');
            fw.append("account");
            fw.append(',');
            fw.append("in_reply_to_id");
            fw.append(',');
            fw.append("in_reply_to_account_id");
            fw.append(',');
            fw.append("content");
            fw.append(',');
            fw.append("created_at");
            fw.append(',');
            fw.append("reblogs_count");
            fw.append(',');
            fw.append("favourites_count");
            fw.append(',');
            fw.append("sensitive");
            fw.append(',');
            fw.append("spoiler_text");
            fw.append(',');
            fw.append("visibility");
            fw.append(',');
            fw.append("media_attachments");
            fw.append('\n');
            for( Status status: backupStatus){
                fw.append(status.getId());
                fw.append(',');
                fw.append(status.getUri());
                fw.append(',');
                fw.append(status.getUrl());
                fw.append(',');
                fw.append(status.getAccount().getAcct());
                fw.append(',');
                fw.append(status.getIn_reply_to_id());
                fw.append(',');
                fw.append(status.getIn_reply_to_account_id());
                fw.append(',');
                String content;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    content = Html.fromHtml(status.getContentTranslated(), Html.FROM_HTML_MODE_LEGACY).toString();
                else
                    //noinspection deprecation
                    content = Html.fromHtml(status.getContentTranslated()).toString();
                fw.append(content);
                fw.append(',');
                fw.append(Helper.shortDateTime(getApplicationContext(), status.getCreated_at()));
                fw.append(',');
                fw.append(String.valueOf(status.getReblogs_count()));
                fw.append(',');
                fw.append(String.valueOf(status.getFavourites_count()));
                fw.append(',');
                fw.append(String.valueOf(status.isSensitive()));
                fw.append(',');
                fw.append(status.getSpoiler_text() !=null?status.getSpoiler_text():"");
                fw.append(',');
                fw.append(status.getVisibility());
                fw.append(',');
                if( status.getMedia_attachments() != null && status.getMedia_attachments().size() > 0){
                    for(Attachment attachment: status.getMedia_attachments()){
                        fw.append(attachment.getRemote_url()).append("\n");
                    }
                }else {
                    fw.append("");
                }
                fw.append('\n');
            }
            fw.flush();
            fw.close();
            message = getString(R.string.data_export_success, account.getAcct());
        } catch (IOException e) {
            e.printStackTrace();
            message = getString(R.string.data_export_error, account.getAcct());
        }
        long notif_id = Long.parseLong(account.getId());
        int notificationId = ((notif_id + 3) > 2147483647) ? (int) (2147483647 - notif_id - 3) : (int) (notif_id + 3);
        Intent intentOpen = new Intent();
        intentOpen.setAction(android.content.Intent.ACTION_VIEW);
        Uri uri = Uri.parse("file://" + fullPath);
        intentOpen.setDataAndType(uri, "text/csv");
        notify_user(getApplicationContext(), intentOpen, notificationId, BitmapFactory.decodeResource(getResources(),
                R.drawable.mastodonlogo), getString(R.string.data_export), message);
    }


}
