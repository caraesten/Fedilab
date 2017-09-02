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
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

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

import static fr.gouv.etalab.mastodon.helper.Helper.HOME_TIMELINE_INTENT;
import static fr.gouv.etalab.mastodon.helper.Helper.INTENT_ACTION;
import static fr.gouv.etalab.mastodon.helper.Helper.NOTIFICATION_INTENT;
import static fr.gouv.etalab.mastodon.helper.Helper.PREF_KEY_ID;
import static fr.gouv.etalab.mastodon.helper.Helper.canNotify;
import static fr.gouv.etalab.mastodon.helper.Helper.notify_user;

/**
 * Created by Thomas on 28/08/2017.
 * Manage service for streaming api and new notifications
 */

public class StreamingService extends Service {

    private String message;
    private int notificationId;
    private Intent intent;
    private String lastPreviousContent;
    private static HashMap<String, HttpURLConnection> connectionHashMap;

    @Override
    public void onCreate(){
        callAsynchronousTask();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /**
     * Task in background starts here.
     */
    private void callAsynchronousTask() {
        SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
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
        if(!sharedpreferences.getBoolean(Helper.SET_WIFI_ONLY, false) || Helper.isOnWIFI(getApplicationContext())) {
            List<Account> accounts = new AccountDAO(getApplicationContext(),db).getAllAccount();
            //It means there is no user in DB.
            if( accounts == null )
                return;
            //Retrieve users in db that owner has.
            for (final Account account: accounts) {
                //new StreamingUserAsyncTask(account.getInstance(), account.getToken(), account.getAcct(), account.getId(), StreamingService.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                Thread readThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            proceeds(account);
                        } catch (Exception ignored) {
                        }
                    }
                });
                readThread.start();
            }
        }
    }


    private EventStreaming lastEvent;

    public enum EventStreaming{
        UPDATE,
        NOTIFICATION,
        DELETE,
        NONE
    }
    private void proceeds(Account account){
        if( connectionHashMap == null)
            connectionHashMap = new HashMap<>();
        boolean connectionAlive = false;
        if( connectionHashMap.get(account.getAcct()+account.getId()) != null) {
            try {
                connectionAlive = (connectionHashMap.get(account.getAcct()+account.getId()).getResponseCode() == 200);
            } catch (Exception e) {
                connectionAlive = false;
            }
        }
        if( connectionAlive)
            connectionHashMap.get(account.getAcct()+account.getId()).disconnect();
        try {
            URL url = new URL("https://" + account.getInstance() + "/api/v1/streaming/user");
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Authorization", "Bearer " + account.getToken());
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("Keep-Alive", "header");
            urlConnection.setRequestProperty("Connection", "close");
            urlConnection.setSSLSocketFactory(new TLSSocketFactory());
            connectionHashMap.put(account.getAcct()+account.getId(), urlConnection);
            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
            readStream(inputStream, urlConnection, account);
        } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
            forceRestart();
        }
    }




    private String readStream(InputStream inputStream, HttpsURLConnection urlConnection, final Account account) {
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String event;
            EventStreaming eventStreaming = null;
            //noinspection InfiniteLoopStatement
            while(true){
                try {
                    event = reader.readLine();
                }catch (Exception e){
                    e.printStackTrace();
                    urlConnection.disconnect();
                    URL url;
                    try {
                        url = new URL("https://" + account.getInstance() + "/api/v1/streaming/user");
                        urlConnection = (HttpsURLConnection) url.openConnection();
                        urlConnection.setRequestProperty("Content-Type", "application/json");
                        urlConnection.setRequestProperty("Authorization", "Bearer " + account.getToken());
                        urlConnection.setRequestProperty("Connection", "Keep-Alive");
                        urlConnection.setRequestProperty("Keep-Alive", "header");
                        urlConnection.setRequestProperty("Connection", "close");
                        urlConnection.setSSLSocketFactory(new TLSSocketFactory());
                        connectionHashMap.put(account.getAcct()+account.getId(), urlConnection);
                        inputStream = new BufferedInputStream(urlConnection.getInputStream());
                        reader = new BufferedReader(new InputStreamReader(inputStream));
                    } catch (NoSuchAlgorithmException | KeyManagementException | IOException ee) {
                        ee.printStackTrace();
                    }
                    SystemClock.sleep(5000);
                    event = null;
                }
                if (event !=null){
                    if( lastEvent == EventStreaming.NONE || lastEvent == null) {

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
                        }
                        lastEvent = EventStreaming.NONE;
                        try {
                            JSONObject eventJson = new JSONObject(event);
                            onRetrieveStreaming(eventStreaming, eventJson, account.getAcct(), account.getId());
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
            forceRestart();
        }
        return null;
    }

    private void forceRestart(){
        Iterator it = connectionHashMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) pair.getValue();
            if( httpsURLConnection != null)
                httpsURLConnection.disconnect();
            it.remove(); // avoids a ConcurrentModificationException
        }
        SystemClock.sleep(1000);
        Intent intent = new Intent(getApplicationContext(), StreamingService.class);
        startService(intent);
        StreamingService.this.stopSelf();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent intent = new Intent(getApplicationContext(), StreamingService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + 5000, pendingIntent);
        super.onTaskRemoved(rootIntent);
    }


    public void onRetrieveStreaming(EventStreaming event, JSONObject response, String acct, String userId) {
        if(  response == null )
            return;
        String max_id_notif = null;
        String max_id_home = null;
        final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        boolean notif_follow = sharedpreferences.getBoolean(Helper.SET_NOTIF_FOLLOW, true);
        boolean notif_add = sharedpreferences.getBoolean(Helper.SET_NOTIF_ADD, true);
        boolean notif_mention = sharedpreferences.getBoolean(Helper.SET_NOTIF_MENTION, true);
        boolean notif_share = sharedpreferences.getBoolean(Helper.SET_NOTIF_SHARE, true);

        //No previous notifications in cache, so no notification will be sent
        boolean notify = false;

        String notificationUrl = null;
        String title = null;
        Status status = null;
        Notification notification = null;
        String dataId = null;
        if( event == EventStreaming.NOTIFICATION){
            notification = API.parseNotificationResponse(getApplicationContext(), response);
            max_id_notif = notification.getId();
            switch (notification.getType()){
                case "mention":
                    if(notif_mention){
                        lastPreviousContent = notification.getStatus().getContent();
                        notify = true;
                        notificationUrl = notification.getAccount().getAvatar();
                        if( notification.getAccount().getDisplay_name() != null && notification.getAccount().getDisplay_name().length() > 0 )
                            title = String.format("%s %s", Helper.shortnameToUnicode(notification.getAccount().getDisplay_name(), true),getString(R.string.notif_mention));
                        else
                            title = String.format("%s %s", notification.getAccount().getUsername(),getString(R.string.notif_mention));
                    }
                    break;
                case "reblog":
                    if(notif_share){
                        notify = true;
                        notificationUrl = notification.getAccount().getAvatar();
                        if( notification.getAccount().getDisplay_name() != null && notification.getAccount().getDisplay_name().length() > 0 )
                            title = String.format("%s %s", Helper.shortnameToUnicode(notification.getAccount().getDisplay_name(), true),getString(R.string.notif_reblog));
                        else
                            title = String.format("%s %s", notification.getAccount().getUsername(),getString(R.string.notif_reblog));
                    }
                    break;
                case "favourite":
                    if(notif_add){
                        notify = true;
                        notificationUrl = notification.getAccount().getAvatar();
                        if( notification.getAccount().getDisplay_name() != null && notification.getAccount().getDisplay_name().length() > 0 )
                            title = String.format("%s %s", Helper.shortnameToUnicode(notification.getAccount().getDisplay_name(), true),getString(R.string.notif_favourite));
                        else
                            title = String.format("%s %s", notification.getAccount().getUsername(),getString(R.string.notif_favourite));
                    }
                    break;
                case "follow":
                    if(notif_follow){
                        notify = true;
                        notificationUrl = notification.getAccount().getAvatar();
                        if( notification.getAccount().getDisplay_name() != null && notification.getAccount().getDisplay_name().length() > 0 )
                            title = String.format("%s %s", Helper.shortnameToUnicode(notification.getAccount().getDisplay_name(), true),getString(R.string.notif_follow));
                        else
                            title = String.format("%s %s", notification.getAccount().getUsername(),getString(R.string.notif_follow));
                    }
                    break;
                default:
                    break;
            }
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(Helper.LAST_NOTIFICATION_MAX_ID + userId, notification.getId());
            editor.apply();
            if( notification.getStatus().getContent()!= null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    message = Html.fromHtml(notification.getStatus().getContent(), Html.FROM_HTML_MODE_LEGACY).toString();
                else
                    //noinspection deprecation
                    message = Html.fromHtml(notification.getStatus().getContent()).toString();
                message = message.substring(0, message.length()>49?49:message.length());
                message = message + "…";
            }else{
                message = "";
            }

        }else if ( event ==  EventStreaming.UPDATE){
            status = API.parseStatuses(getApplicationContext(), response);
            status.setReplies(new ArrayList<Status>()); //Force to don't display replies.
            max_id_home = status.getId();
            if( status.getContent() != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    message = Html.fromHtml(status.getContent(), Html.FROM_HTML_MODE_LEGACY).toString();
                else
                    //noinspection deprecation
                    message = Html.fromHtml(status.getContent()).toString();
                message = message.substring(0, message.length()>49?49:message.length());
                message = message + "…";
            }else{
                message = "";
            }
            title = getString(R.string.notif_pouet, status.getAccount().getUsername());
            notificationUrl = status.getAccount().getAvatar();
        }else if( event == EventStreaming.DELETE){
            try {
                dataId = response.getString("id");

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        if( max_id_notif != null){
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(Helper.LAST_NOTIFICATION_MAX_ID + userId, max_id_notif);
            editor.apply();
        }
        if( max_id_home != null){
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(Helper.LAST_HOMETIMELINE_MAX_ID + userId, max_id_home);
            editor.apply();
        }

        //Check which user is connected and if activity is to front
        boolean activityVisible = false;
        try{
            activityVisible = MainActivity.isActivityVisible();
        }catch (Exception ignored){}
        String userconnected = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        Account account = new AccountDAO(getApplicationContext(), db).getAccountByID(userconnected);
        //User receiving the notification is connected and application is to front, notification won't be pushed
        //Instead, the interaction is done in the activity


        if( isCurrentAccountLoggedIn(acct, userId)){
            notify = false;
            Intent intentBC = new Intent(Helper.RECEIVE_DATA);
            intentBC.putExtra("eventStreaming", event);
            Bundle b = new Bundle();
            if( event == EventStreaming.UPDATE)
                b.putParcelable("data", status);
            else if(event == EventStreaming.NOTIFICATION)
                b.putParcelable("data", notification);
            else if(event == EventStreaming.DELETE)
                b.putString("id", dataId);
            intentBC.putExtras(b);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentBC);
        }

        if( activityVisible && isCurrentAccountLoggedIn(acct, userId)){
            notify = false;
        }else if(event == EventStreaming.NOTIFICATION ){
            notify = true;

        }else if(event == EventStreaming.UPDATE ){
            //lastPreviousContent contains the content of the last notification, if it was a mention it will avoid to push two notifications
            if( account == null || (lastPreviousContent != null && lastPreviousContent.equals(status.getContent()))) {
                notify = false;
            }else {
                notify = true;
                //Retrieve users in db that owner has, and if the toot matches one of them we don't notify
                List<Account> accounts = new AccountDAO(getApplicationContext(),db).getAllAccount();
                for(Account act_tmp: accounts) {
                    if(notify && act_tmp.getAcct().trim().equals(status.getAccount().getAcct()) && act_tmp.getId().trim().equals(status.getAccount().getId().trim())){
                        notify = false;
                    }
                }
                //Here we check if the user wants home timeline notifications
                if( notify )
                    notify = sharedpreferences.getBoolean(Helper.SET_NOTIF_HOMETIMELINE, true);
            }
            lastPreviousContent = status.getContent();
        }
        //All is good here for a notification, we will know check if it can be done depending of the hour
        if( notify)
            notify = canNotify(getApplicationContext());
        if( notify && event == EventStreaming.NOTIFICATION){
            intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK );
            intent.putExtra(INTENT_ACTION, NOTIFICATION_INTENT);
            intent.putExtra(PREF_KEY_ID, userId);
            long notif_id = Long.parseLong(userId);
            notificationId = ((notif_id + 1) > 2147483647) ? (int) (2147483647 - notif_id - 1) : (int) (notif_id + 1);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(Helper.LAST_NOTIFICATION_MAX_ID + userId, notification.getId());
            editor.apply();
        }
        if( notify && event == EventStreaming.UPDATE) {
            intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(INTENT_ACTION, HOME_TIMELINE_INTENT);
            intent.putExtra(PREF_KEY_ID, userId);
            long notif_id = Long.parseLong(userId);
            notificationId = ((notif_id + 2) > 2147483647) ? (int) (2147483647 - notif_id - 2) : (int) (notif_id + 2);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(Helper.LAST_HOMETIMELINE_MAX_ID + userId, status.getId());
            editor.apply();
        }

        if( notify){
            if( notificationUrl != null){
                ImageLoader imageLoaderNoty = ImageLoader.getInstance();
                File cacheDir = new File(getApplicationContext().getCacheDir(), getString(R.string.app_name));
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
                imageLoaderNoty.loadImage(notificationUrl, options, new SimpleImageLoadingListener(){
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        super.onLoadingComplete(imageUri, view, loadedImage);
                        notify_user(getApplicationContext(), intent, notificationId, loadedImage, finalTitle, message);

                    }
                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason){
                        notify_user(getApplicationContext(), intent, notificationId, BitmapFactory.decodeResource(getApplicationContext().getResources(),
                                R.drawable.mastodonlogo), finalTitle, message);
                    }});
            }
        }
    }

    private boolean isCurrentAccountLoggedIn(String acct, String userId){
        final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userconnected = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        Account account = new AccountDAO(getApplicationContext(), db).getAccountByID(userconnected);
        return acct.trim().equals(account.getAcct().trim()) && userId.trim().equals(account.getId().trim());
    }
}
