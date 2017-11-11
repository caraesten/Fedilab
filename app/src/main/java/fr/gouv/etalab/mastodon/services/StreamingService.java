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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;


import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import fr.gouv.etalab.mastodon.activities.BaseMainActivity;
import fr.gouv.etalab.mastodon.activities.MainActivity;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Notification;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.client.PatchBaseImageDownloader;
import fr.gouv.etalab.mastodon.client.TLSSocketFactory;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import mastodon.etalab.gouv.fr.mastodon.R;

import static fr.gouv.etalab.mastodon.helper.Helper.INTENT_ACTION;
import static fr.gouv.etalab.mastodon.helper.Helper.NOTIFICATION_INTENT;
import static fr.gouv.etalab.mastodon.helper.Helper.PREF_KEY_ID;
import static fr.gouv.etalab.mastodon.helper.Helper.notify_user;


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
    private static HashMap<String, HttpsURLConnection> httpsURLConnections = new HashMap<>();

    public enum EventStreaming{
        UPDATE,
        NOTIFICATION,
        DELETE,
        NONE
    }
    protected Account account;

    public void onCreate() {
        super.onCreate();
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        editor.putBoolean(Helper.SHOULD_CONTINUE_STREAMING+userId, true);
        editor.apply();
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        boolean liveNotifications = sharedpreferences.getBoolean(Helper.SET_LIVE_NOTIFICATIONS, true);
        boolean notify = sharedpreferences.getBoolean(Helper.SET_NOTIFY, true);
        if( liveNotifications && notify){
            Iterator it = httpsURLConnections.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                if( pair.getValue() != null)
                    ((HttpsURLConnection)pair.getValue()).disconnect();
                it.remove();
            }
            SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
            List<Account> accountStreams = new AccountDAO(getApplicationContext(), db).getAllAccount();
            for(Account accountStream: accountStreams){
                streamOnUser(accountStream, true);
            }
        }else {
            String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
            if( httpsURLConnection != null)
                httpsURLConnection.disconnect();
            if( userId != null) {
                SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                Account accountStream = new AccountDAO(getApplicationContext(), db).getAccountByID(userId);
                streamOnUser(accountStream, false);
            }
        }
        
    }
    
    private void streamOnUser(Account accountStream, boolean liveNotifications){
        InputStream inputStream;
        BufferedReader reader = null;
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
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
                if( liveNotifications)
                    httpsURLConnections.put(accountStream.getAcct()+accountStream.getInstance(),httpsURLConnection);
                else
                    StreamingService.httpsURLConnection = httpsURLConnection;
                inputStream = new BufferedInputStream(httpsURLConnection.getInputStream());
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String event;
                EventStreaming eventStreaming;
                while((event = reader.readLine()) != null) {
                    if( !sharedpreferences.getBoolean(Helper.SHOULD_CONTINUE_STREAMING + accountStream.getId(), true) )
                        stopSelf();
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

    public void onRetrieveStreaming(EventStreaming event, final Account account, JSONObject response) {
        if(  response == null )
            return;
        //No previous notifications in cache, so no notification will be sent
        Status status ;
        final Notification notification;
        String dataId = null;

        Bundle b = new Bundle();
        if( event == EventStreaming.NOTIFICATION){
            notification = API.parseNotificationResponse(getApplicationContext(), response);
            b.putParcelable("data", notification);
            boolean activityPaused;
            try {
                activityPaused = BaseMainActivity.activityState();
            }catch (Exception e){
                activityPaused = true;
            }
            final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            boolean liveNotifications = sharedpreferences.getBoolean(Helper.SET_LIVE_NOTIFICATIONS, true);
            boolean canNotify = Helper.canNotify(getApplicationContext());
            boolean notify = sharedpreferences.getBoolean(Helper.SET_NOTIFY, true);
            if(activityPaused && liveNotifications && canNotify && notify) {
                boolean notif_follow = sharedpreferences.getBoolean(Helper.SET_NOTIF_FOLLOW, true);
                boolean notif_add = sharedpreferences.getBoolean(Helper.SET_NOTIF_ADD, true);
                boolean notif_mention = sharedpreferences.getBoolean(Helper.SET_NOTIF_MENTION, true);
                boolean notif_share = sharedpreferences.getBoolean(Helper.SET_NOTIF_SHARE, true);
                boolean somethingToPush = (notif_follow || notif_add || notif_mention || notif_share);
                String title = null;
                if( somethingToPush){
                    switch (notification.getType()){
                        case "mention":
                            if(notif_mention){
                                if( notification.getAccount().getDisplay_name() != null && notification.getAccount().getDisplay_name().length() > 0 )
                                    title = String.format("@%s %s", Helper.shortnameToUnicode(notification.getAccount().getDisplay_name(), true),getApplicationContext().getString(R.string.notif_mention));
                                else
                                    title = String.format("@%s %s", notification.getAccount().getUsername(),getApplicationContext().getString(R.string.notif_mention));
                            }
                            break;
                        case "reblog":
                            if(notif_share){
                                if( notification.getAccount().getDisplay_name() != null && notification.getAccount().getDisplay_name().length() > 0 )
                                    title = String.format("@%s %s", Helper.shortnameToUnicode(notification.getAccount().getDisplay_name(), true),getApplicationContext().getString(R.string.notif_reblog));
                                else
                                    title = String.format("@%s %s", notification.getAccount().getUsername(),getApplicationContext().getString(R.string.notif_reblog));
                            }
                            break;
                        case "favourite":
                            if(notif_add){
                                if( notification.getAccount().getDisplay_name() != null && notification.getAccount().getDisplay_name().length() > 0 )
                                    title = String.format("@%s %s", Helper.shortnameToUnicode(notification.getAccount().getDisplay_name(), true),getApplicationContext().getString(R.string.notif_favourite));
                                else
                                    title = String.format("@%s %s", notification.getAccount().getUsername(),getApplicationContext().getString(R.string.notif_favourite));
                            }
                            break;
                        case "follow":
                            if(notif_follow){
                                if( notification.getAccount().getDisplay_name() != null && notification.getAccount().getDisplay_name().length() > 0 )
                                    title = String.format("@%s %s", Helper.shortnameToUnicode(notification.getAccount().getDisplay_name(), true),getApplicationContext().getString(R.string.notif_follow));
                                else
                                    title = String.format("@%s %s", notification.getAccount().getUsername(),getApplicationContext().getString(R.string.notif_follow));
                            }
                            break;
                        default:
                    }
                    //Some others notification
                    final Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK );
                    intent.putExtra(INTENT_ACTION, NOTIFICATION_INTENT);
                    intent.putExtra(PREF_KEY_ID, account.getId());
                    long notif_id = Long.parseLong(account.getId());
                    final int notificationId = ((notif_id + 1) > 2147483647) ? (int) (2147483647 - notif_id - 1) : (int) (notif_id + 1);
                    if( account.getAvatar() != null ) {
                        ImageLoader imageLoaderNoty = ImageLoader.getInstance();
                        File cacheDir = new File(getApplicationContext().getCacheDir(), getApplicationContext().getString(R.string.app_name));
                        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                                .imageDownloader(new PatchBaseImageDownloader(getApplicationContext()))
                                .threadPoolSize(5)
                                .threadPriority(Thread.MIN_PRIORITY + 3)
                                .denyCacheImageMultipleSizesInMemory()
                                .diskCache(new UnlimitedDiskCache(cacheDir))
                                .build();
                        imageLoaderNoty.init(config);
                        DisplayImageOptions options = new DisplayImageOptions.Builder().displayer(new SimpleBitmapDisplayer()).cacheInMemory(false)
                                .cacheOnDisk(true).resetViewBeforeLoading(true).build();

                        final String finalTitle = title;
                        if( title != null) {
                            imageLoaderNoty.loadImage(account.getAvatar(), options, new SimpleImageLoadingListener() {
                                @Override
                                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                    super.onLoadingComplete(imageUri, view, loadedImage);
                                    notify_user(getApplicationContext(), intent, notificationId, loadedImage, finalTitle, "");
                                    String lastNotif = sharedpreferences.getString(Helper.LAST_NOTIFICATION_MAX_ID + account.getId(), null);
                                    if (lastNotif == null || Long.parseLong(notification.getId()) > Long.parseLong(lastNotif)) {
                                        SharedPreferences.Editor editor = sharedpreferences.edit();
                                        editor.putString(Helper.LAST_NOTIFICATION_MAX_ID + account.getId(), notification.getId());
                                        editor.apply();
                                    }
                                }

                                @Override
                                public void onLoadingFailed(java.lang.String imageUri, android.view.View view, FailReason failReason) {
                                    notify_user(getApplicationContext(), intent, notificationId, BitmapFactory.decodeResource(getApplicationContext().getResources(),
                                            R.drawable.mastodonlogo), finalTitle, "");
                                    String lastNotif = sharedpreferences.getString(Helper.LAST_NOTIFICATION_MAX_ID + account.getId(), null);
                                    if (lastNotif == null || Long.parseLong(notification.getId()) > Long.parseLong(lastNotif)) {
                                        SharedPreferences.Editor editor = sharedpreferences.edit();
                                        editor.putString(Helper.LAST_NOTIFICATION_MAX_ID + account.getId(), notification.getId());
                                        editor.apply();
                                    }
                                }
                            });
                        }
                    }
                }
            }
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
