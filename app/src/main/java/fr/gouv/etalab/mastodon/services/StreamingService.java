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
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Notification;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.client.TLSSocketFactory;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;


/**
 * Created by Thomas on 28/08/2017.
 * Manage service for streaming api and new notifications
 */

public class StreamingService extends IntentService {


    private EventStreaming lastEvent;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public StreamingService(String name) {
        super(name);
    }
    public StreamingService() {
        super("StreamingService");
    }
    private static HttpsURLConnection httpsURLConnection;
    public enum EventStreaming{
        UPDATE,
        NOTIFICATION,
        DELETE,
        NONE
    }
    protected Account account;

    public void onCreate() {
        super.onCreate();
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        InputStream inputStream;
        BufferedReader reader = null;
        Account accountStream = null;
        if( httpsURLConnection != null)
            httpsURLConnection.disconnect();
        if( userId != null) {
            SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
            accountStream = new AccountDAO(getApplicationContext(), db).getAccountByID(userId);
        }
        if( accountStream != null){
            try {

                URL url = new URL("https://" + accountStream.getInstance() + "/api/v1/streaming/user");
                httpsURLConnection = (HttpsURLConnection) url.openConnection();
                httpsURLConnection.setRequestProperty("Content-Type", "application/json");
                httpsURLConnection.setRequestProperty("Authorization", "Bearer " + accountStream.getToken());
                httpsURLConnection.setRequestProperty("Connection", "Keep-Alive");
                httpsURLConnection.setRequestProperty("Keep-Alive", "header");
                httpsURLConnection.setRequestProperty("Connection", "close");
                httpsURLConnection.setSSLSocketFactory(new TLSSocketFactory());
                httpsURLConnection.setRequestMethod("GET");
                httpsURLConnection.setConnectTimeout(70000);
                httpsURLConnection.setReadTimeout(70000);
                inputStream = new BufferedInputStream(httpsURLConnection.getInputStream());
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String event;
                EventStreaming eventStreaming;
                while((event = reader.readLine()) != null) {
                    if ((lastEvent == EventStreaming.NONE || lastEvent == null) && !event.startsWith("data: ")) {
                        switch (event.trim()) {
                            case "event: update":
                                lastEvent = EventStreaming.UPDATE;
                                break;
                            case "event: notification":
                                lastEvent = EventStreaming.NOTIFICATION;
                                break;
                            case "event: delete":
                                lastEvent = EventStreaming.DELETE;
                                break;
                            default:
                                lastEvent = EventStreaming.NONE;
                        }
                    } else {
                        if (!event.startsWith("data: ")) {
                            lastEvent = EventStreaming.NONE;
                            continue;
                        }
                        event = event.substring(6);
                        if (lastEvent == EventStreaming.UPDATE) {
                            eventStreaming = EventStreaming.UPDATE;
                        } else if (lastEvent == EventStreaming.NOTIFICATION) {
                            eventStreaming = EventStreaming.NOTIFICATION;
                        } else if (lastEvent == EventStreaming.DELETE) {
                            eventStreaming = EventStreaming.DELETE;
                            event = "{id:" + event + "}";
                        } else {
                            eventStreaming = EventStreaming.UPDATE;
                        }
                        lastEvent = EventStreaming.NONE;
                        try {
                            JSONObject eventJson = new JSONObject(event);
                            onRetrieveStreaming(eventStreaming, accountStream, eventJson);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                if(reader != null){
                    try{
                        reader.close();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
                SystemClock.sleep(1000);
                sendBroadcast(new Intent("RestartStreamingService"));
            }
        }
    }

    public void onRetrieveStreaming(EventStreaming event, Account account, JSONObject response) {
        if(  response == null )
            return;
        //No previous notifications in cache, so no notification will be sent
        Status status ;
        Notification notification;
        String dataId = null;

        Bundle b = new Bundle();
        if( event == EventStreaming.NOTIFICATION){
            notification = API.parseNotificationResponse(getApplicationContext(), response);
            b.putParcelable("data", notification);
        }else if ( event ==  EventStreaming.UPDATE){
            status = API.parseStatuses(getApplicationContext(), response);
            status.setReplies(new ArrayList<Status>());
            status.setNew(true);
            b.putParcelable("data", status);
        }else if( event == EventStreaming.DELETE){
            try {
                dataId = response.getString("id");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if( account != null)
            b.putString("userIdService",account.getId());
        Intent intentBC = new Intent(Helper.RECEIVE_DATA);
        intentBC.putExtra("eventStreaming", event);
        intentBC.putExtras(b);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentBC);
    }

}
