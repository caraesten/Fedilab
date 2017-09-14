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
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import javax.net.ssl.HttpsURLConnection;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Notification;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.client.TLSSocketFactory;
import fr.gouv.etalab.mastodon.helper.Helper;


/**
 * Created by Thomas on 28/08/2017.
 * Manage service for streaming api and new notifications
 */

public class StreamingService extends Service {


    private boolean isConnectingHashMap = false;
    private HttpsURLConnection httpsURLConnection;

    private EventStreaming lastEvent;
    public enum EventStreaming{
        UPDATE,
        NOTIFICATION,
        DELETE,
        NONE
    }
    private final IBinder iBinder = new StreamingServiceBinder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public class StreamingServiceBinder extends Binder {
        public StreamingService getService() {
            return StreamingService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }


    public void disconnect(){
        Thread readThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if( httpsURLConnection != null){
                    httpsURLConnection.disconnect();
            }
        }});
        readThread.start();
    }


    /**
     * Task in background starts here.
     */
    public void connect(final Account account) {
        //If an Internet connection and user agrees with notification refresh
        final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        //Check which notifications the user wants to see
        boolean notif_follow = sharedpreferences.getBoolean(Helper.SET_NOTIF_FOLLOW, true);
        boolean notif_add = sharedpreferences.getBoolean(Helper.SET_NOTIF_ADD, true);
        boolean notif_ask = sharedpreferences.getBoolean(Helper.SET_NOTIF_ASK, true);
        boolean notif_mention = sharedpreferences.getBoolean(Helper.SET_NOTIF_MENTION, true);
        boolean notif_share = sharedpreferences.getBoolean(Helper.SET_NOTIF_SHARE, true);
        //User disagree with all notifications
        if( !notif_follow && !notif_add && !notif_ask && !notif_mention && !notif_share)
            return; //Nothing is done
        //No account connected, the service is stopped
        if(!Helper.isLoggedIn(getApplicationContext()))
            return;
        //If WIFI only and on WIFI OR user defined any connections to use the service.
        if( isConnectingHashMap)
            return;
        if(!sharedpreferences.getBoolean(Helper.SET_WIFI_ONLY, false) || Helper.isOnWIFI(getApplicationContext())) {
            Thread readThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        boolean connectionAlive = false;
                        isConnectingHashMap = true;
                        if( httpsURLConnection != null) {
                            try {
                                connectionAlive = (httpsURLConnection.getResponseCode() == 200);
                            } catch (Exception e) {
                                connectionAlive = false;
                            }
                        }
                        if( connectionAlive) {
                            if( httpsURLConnection != null)
                                httpsURLConnection.disconnect();
                        }
                        try {
                            URL url = new URL("https://" + account.getInstance() + "/api/v1/streaming/user");
                            httpsURLConnection = (HttpsURLConnection) url.openConnection();
                            httpsURLConnection.setRequestProperty("Content-Type", "application/json");
                            httpsURLConnection.setRequestProperty("Authorization", "Bearer " + account.getToken());
                            httpsURLConnection.setRequestProperty("Connection", "Keep-Alive");
                            httpsURLConnection.setRequestProperty("Keep-Alive", "header");
                            httpsURLConnection.setRequestProperty("Connection", "close");
                            httpsURLConnection.setSSLSocketFactory(new TLSSocketFactory());
                            InputStream inputStream = new BufferedInputStream(httpsURLConnection.getInputStream());
                            readStream(inputStream, account);
                        } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
                            e.printStackTrace();
                            forceRestart(account);
                        }
                    } catch (Exception ignored) {
                    }
                }
            });
            readThread.start();
        }
    }




    @SuppressWarnings("ConstantConditions")
    private String readStream(InputStream inputStream, final Account account) {
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String event;
            EventStreaming eventStreaming;
            //noinspection InfiniteLoopStatement
            while(true){
                try {
                    event = reader.readLine();
                }catch (Exception e){
                    e.printStackTrace();
                    forceRestart(account);
                    break;
                }
                if (event !=null){

                    if( (lastEvent == EventStreaming.NONE || lastEvent == null) && !event.startsWith("data: ")) {
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
                    }else{
                        if( !event.startsWith("data: ")){
                            lastEvent = EventStreaming.NONE;
                            continue;
                        }
                        event = event.substring(6);
                        if(lastEvent == EventStreaming.UPDATE) {
                            eventStreaming = EventStreaming.UPDATE;
                        }else if(lastEvent == EventStreaming.NOTIFICATION) {
                            eventStreaming = EventStreaming.NOTIFICATION;
                        }else if( lastEvent == EventStreaming.DELETE) {
                            eventStreaming = EventStreaming.DELETE;
                            event = "{id:" + event + "}";
                        }else {
                            eventStreaming = EventStreaming.UPDATE;
                        }
                        lastEvent = EventStreaming.NONE;
                        try {
                            JSONObject eventJson = new JSONObject(event);
                            onRetrieveStreaming(eventStreaming, eventJson, account.getId());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(reader != null){
                try{
                    reader.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            forceRestart(account);
        }
        return null;
    }

    private void forceRestart(Account account){
        isConnectingHashMap = false;
        SystemClock.sleep(1000);
        Intent intent = new Intent(getApplicationContext(), StreamingService.class);
        intent.putExtra("accountId", account.getId());
        intent.putExtra("accountAcct", account.getAcct());
        startService(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent intent = new Intent(getApplicationContext(), StreamingService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + 5000, pendingIntent);
        super.onTaskRemoved(rootIntent);
    }


    public void onRetrieveStreaming(EventStreaming event, JSONObject response, String userId) {
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
        Intent intentBC = new Intent(Helper.RECEIVE_DATA);
        intentBC.putExtra("eventStreaming", event);
        intentBC.putExtras(b);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentBC);
    }
}
