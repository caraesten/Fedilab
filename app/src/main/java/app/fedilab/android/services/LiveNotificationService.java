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

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.SpannableString;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.Headers;
import com.koushikdutta.async.http.WebSocket;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import app.fedilab.android.activities.BaseMainActivity;
import app.fedilab.android.client.API;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Notification;
import app.fedilab.android.client.TLSSocketFactory;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.sqlite.AccountDAO;
import app.fedilab.android.sqlite.Sqlite;
import app.fedilab.android.R;
import app.fedilab.android.activities.MainActivity;
import me.leolin.shortcutbadger.ShortcutBadger;

import static androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY;
import static app.fedilab.android.helper.Helper.getMainLogo;
import static app.fedilab.android.helper.Helper.getNotificationIcon;


/**
 * Created by Thomas on 29/11/2017.
 * Manage service for streaming api and new notifications
 */

public class LiveNotificationService extends Service implements NetworkStateReceiver.NetworkStateReceiverListener {

    static {
        Helper.installProvider();
    }

    public static String CHANNEL_ID = "live_notifications";
    protected Account account;
    private static HashMap<String, Thread> threads = new HashMap<>();
    private static HashMap<String, String> lastNotification = new HashMap<>();
    private NetworkStateReceiver networkStateReceiver;
    private static HashMap<String, WebSocket> webSocketFutures = new HashMap<>();
    private NotificationChannel channel;
    public static int totalAccount = 0;
    public static int eventsCount = 0;
    public static int liveNotifBadge = 0;

    public void onCreate() {
        super.onCreate();

        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));

    }

    private void startStream() {

        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        boolean liveNotifications = sharedpreferences.getBoolean(Helper.SET_LIVE_NOTIFICATIONS, true);
        SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        if (liveNotifications) {
            List<Account> accountStreams = new AccountDAO(getApplicationContext(), db).getAllAccountCrossAction();
            if (accountStreams != null) {
                for (final Account accountStream : accountStreams) {
                    if (accountStream.getSocial() == null || accountStream.getSocial().equals("MASTODON") || accountStream.getSocial().equals("PLEROMA")) {
                        startWork(accountStream);
                    }
                }
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent == null || intent.getBooleanExtra("stop", false)) {
            totalAccount = 0;
            if (Build.VERSION.SDK_INT >= 26) {
                stopForeground(true);
            }
        }else{
            SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);

            if (Build.VERSION.SDK_INT >= 26) {
                channel = new NotificationChannel(CHANNEL_ID,
                        "Live notifications",
                        NotificationManager.IMPORTANCE_DEFAULT);

                ((NotificationManager) Objects.requireNonNull(getSystemService(Context.NOTIFICATION_SERVICE))).createNotificationChannel(channel);
                SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                List<Account> accountStreams = new AccountDAO(getApplicationContext(), db).getAllAccountCrossAction();
                totalAccount = 0;
                for (Account account : accountStreams) {
                    if (account.getSocial() == null || account.getSocial().equals("MASTODON") || account.getSocial().equals("PLEROMA")) {
                        boolean allowStream = sharedpreferences.getBoolean(Helper.SET_ALLOW_STREAM + account.getId() + account.getInstance(), true);
                        if (allowStream) {
                            totalAccount++;
                        }
                    }
                }
            }
            if( totalAccount > 0) {
                Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(
                        getApplicationContext(),
                        0,
                        myIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                android.app.Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentTitle(getString(R.string.top_notification))
                        .setContentIntent(pendingIntent)
                        .setSmallIcon(getNotificationIcon(getApplicationContext()))
                        .setContentText(getString(R.string.top_notification_message, String.valueOf(totalAccount), String.valueOf(eventsCount))).build();

                startForeground(1, notification);
            }else{
                stopSelf();
            }
        }
       if( totalAccount > 0) {
           return START_STICKY;
       }
       return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        networkStateReceiver.removeListener(this);
        unregisterReceiver(networkStateReceiver);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        if( totalAccount > 0) {
            restart();
        }
    }

    private void restart() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Intent restartServiceIntent = new Intent(LiveNotificationService.this, LiveNotificationService.class);
            restartServiceIntent.setPackage(getPackageName());
            PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
            AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            assert alarmService != null;
            alarmService.set(
                    AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + 1000,
                    restartServicePendingIntent);
        }
    }

    private void taks(Account account) {

        if (account != null) {
            Headers headers = new Headers();
            headers.add("Authorization", "Bearer " + account.getToken());
            headers.add("Connection", "Keep-Alive");
            headers.add("method", "GET");
            headers.add("scheme", "https");
            String notif_url = "user:notification";
            if (account.getSocial().toUpperCase().equals("PLEROMA"))
                notif_url = "user";
            String urlKey = "wss://" + account.getInstance() + "/api/v1/streaming/?stream=" + notif_url + "&access_token=" + account.getToken();
            Uri url = Uri.parse(urlKey);
            AsyncHttpRequest.setDefaultHeaders(headers, url);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                try {
                    AsyncHttpClient.getDefaultInstance().getSSLSocketMiddleware().setSSLContext(new TLSSocketFactory(account.getInstance()).getSSLContext());
                    AsyncHttpClient.getDefaultInstance().getSSLSocketMiddleware().setConnectAllAddresses(true);
                } catch (KeyManagementException | NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
            String key = account.getAcct() + "@" + account.getInstance();
            if (webSocketFutures.get(key) == null || !Objects.requireNonNull(webSocketFutures.get(key)).isOpen()) {
                AsyncHttpClient.getDefaultInstance().websocket("wss://" + account.getInstance() + "/api/v1/streaming/?stream=" + notif_url + "&access_token=" + account.getToken(), "wss", new AsyncHttpClient.WebSocketConnectCallback() {
                    @Override
                    public void onCompleted(Exception ex, WebSocket webSocket) {
                        webSocketFutures.put(account.getAcct() + "@" + account.getInstance(), webSocket);
                        if (ex != null) {
                            return;
                        }
                        webSocket.setStringCallback(new WebSocket.StringCallback() {
                            public void onStringAvailable(String s) {
                                try {
                                    JSONObject eventJson = new JSONObject(s);
                                    onRetrieveStreaming(account, eventJson);
                                } catch (JSONException ignored) {
                                }
                            }
                        });

                        webSocket.setClosedCallback(new CompletedCallback() {
                            @Override
                            public void onCompleted(Exception ex) {
                                if (networkStateReceiver.connected) {
                                    startWork(account);
                                }
                            }
                        });
                        webSocket.setDataCallback(new DataCallback() {
                            public void onDataAvailable(DataEmitter emitter, ByteBufferList byteBufferList) {
                                // note that this data has been read
                                byteBufferList.recycle();
                            }
                        });
                    }
                });
            }
        }
    }


    private void startWork(Account accountStream) {

        String key = accountStream.getAcct() + "@" + accountStream.getInstance();
        if (!threads.containsKey(key) || !Objects.requireNonNull(threads.get(key)).isAlive()) {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    taks(accountStream);
                }
            };
            thread.start();
            threads.put(accountStream.getAcct() + "@" + accountStream.getInstance(), thread);
        }

    }

    private void onRetrieveStreaming(Account account, JSONObject response) {
        if (response == null)
            return;
        final Notification notification;
        String dataId;
        Bundle b = new Bundle();
        boolean canSendBroadCast = true;
        Helper.EventStreaming event;
        final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        try {
            switch (response.get("event").toString()) {
                case "notification":
                    eventsCount++;
                    if (Build.VERSION.SDK_INT >= 26) {
                        channel = new NotificationChannel(CHANNEL_ID,
                                "Live notifications",
                                NotificationManager.IMPORTANCE_DEFAULT);
                        ((NotificationManager) Objects.requireNonNull(getSystemService(Context.NOTIFICATION_SERVICE))).createNotificationChannel(channel);
                        android.app.Notification notificationChannel = new NotificationCompat.Builder(this, CHANNEL_ID)
                                .setContentTitle(getString(R.string.top_notification))
                                .setSmallIcon(getNotificationIcon(getApplicationContext()))
                                .setContentText(getString(R.string.top_notification_message, String.valueOf(totalAccount), String.valueOf(eventsCount))).build();

                        startForeground(1, notificationChannel);
                    }
                    event = Helper.EventStreaming.NOTIFICATION;
                    notification = API.parseNotificationResponse(getApplicationContext(), new JSONObject(response.get("payload").toString()));
                    if (notification == null) {
                        return;
                    }
                    b.putParcelable("data", notification);
                    boolean liveNotifications = sharedpreferences.getBoolean(Helper.SET_LIVE_NOTIFICATIONS, true);
                    boolean canNotify = Helper.canNotify(getApplicationContext());
                    boolean notify = sharedpreferences.getBoolean(Helper.SET_NOTIFY, true);
                    String targeted_account = null;
                    Helper.NotifType notifType = Helper.NotifType.MENTION;
                    boolean activityRunning = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("isMainActivityRunning", false);
                    String key = account.getAcct() + "@" + account.getInstance();
                    if (lastNotification.containsKey(key) && notification.getId().compareTo(Objects.requireNonNull(lastNotification.get(key))) <= 0) {
                        canNotify = false;
                    }
                    String lastNotif = sharedpreferences.getString(Helper.LAST_NOTIFICATION_MAX_ID + account.getId() + account.getInstance(), null);
                    if (notification.getId().compareTo(Objects.requireNonNull(lastNotif)) <= 0) {
                        canNotify = false;
                    }
                    boolean allowStream = sharedpreferences.getBoolean(Helper.SET_ALLOW_STREAM + account.getId() + account.getInstance(), true);
                    if (!allowStream) {
                        canNotify = false;
                    }


                    if ((userId == null || !userId.equals(account.getId()) || !activityRunning) && liveNotifications && canNotify && notify) {
                        lastNotification.put(key, notification.getId());
                        boolean notif_follow = sharedpreferences.getBoolean(Helper.SET_NOTIF_FOLLOW, true);
                        boolean notif_add = sharedpreferences.getBoolean(Helper.SET_NOTIF_ADD, true);
                        boolean notif_mention = sharedpreferences.getBoolean(Helper.SET_NOTIF_MENTION, true);
                        boolean notif_share = sharedpreferences.getBoolean(Helper.SET_NOTIF_SHARE, true);
                        boolean notif_poll = sharedpreferences.getBoolean(Helper.SET_NOTIF_POLL, true);
                        boolean somethingToPush = (notif_follow || notif_add || notif_mention || notif_share || notif_poll);
                        String message = null;
                        if (somethingToPush) {
                            switch (notification.getType()) {
                                case "mention":
                                    notifType = Helper.NotifType.MENTION;
                                    if (notif_mention) {
                                        if (notification.getAccount().getDisplay_name() != null && notification.getAccount().getDisplay_name().length() > 0)
                                            message = String.format("%s %s", Helper.shortnameToUnicode(notification.getAccount().getDisplay_name(), true), getString(R.string.notif_mention));
                                        else
                                            message = String.format("@%s %s", notification.getAccount().getAcct(), getString(R.string.notif_mention));
                                        if (notification.getStatus() != null) {
                                            if (notification.getStatus().getSpoiler_text() != null && notification.getStatus().getSpoiler_text().length() > 0) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                                    message = "\n" + new SpannableString(Html.fromHtml(notification.getStatus().getSpoiler_text(), FROM_HTML_MODE_LEGACY));
                                                else
                                                    message = "\n" + new SpannableString(Html.fromHtml(notification.getStatus().getSpoiler_text()));
                                            } else {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                                    message = "\n" + new SpannableString(Html.fromHtml(notification.getStatus().getContent(), FROM_HTML_MODE_LEGACY));
                                                else
                                                    message = "\n" + new SpannableString(Html.fromHtml(notification.getStatus().getContent()));
                                            }
                                        }
                                    } else {
                                        canSendBroadCast = false;
                                    }
                                    break;
                                case "reblog":
                                    notifType = Helper.NotifType.BOOST;
                                    if (notif_share) {
                                        if (notification.getAccount().getDisplay_name() != null && notification.getAccount().getDisplay_name().length() > 0)
                                            message = String.format("%s %s", Helper.shortnameToUnicode(notification.getAccount().getDisplay_name(), true), getString(R.string.notif_reblog));
                                        else
                                            message = String.format("@%s %s", notification.getAccount().getAcct(), getString(R.string.notif_reblog));
                                    } else {
                                        canSendBroadCast = false;
                                    }
                                    break;
                                case "favourite":
                                    notifType = Helper.NotifType.FAV;
                                    if (notif_add) {
                                        if (notification.getAccount().getDisplay_name() != null && notification.getAccount().getDisplay_name().length() > 0)
                                            message = String.format("%s %s", Helper.shortnameToUnicode(notification.getAccount().getDisplay_name(), true), getString(R.string.notif_favourite));
                                        else
                                            message = String.format("@%s %s", notification.getAccount().getAcct(), getString(R.string.notif_favourite));
                                    } else {
                                        canSendBroadCast = false;
                                    }
                                    break;
                                case "follow":
                                    notifType = Helper.NotifType.FOLLLOW;
                                    if (notif_follow) {
                                        if (notification.getAccount().getDisplay_name() != null && notification.getAccount().getDisplay_name().length() > 0)
                                            message = String.format("%s %s", Helper.shortnameToUnicode(notification.getAccount().getDisplay_name(), true), getString(R.string.notif_follow));
                                        else
                                            message = String.format("@%s %s", notification.getAccount().getAcct(), getString(R.string.notif_follow));
                                        targeted_account = notification.getAccount().getId();
                                    } else {
                                        canSendBroadCast = false;
                                    }
                                    break;
                                case "poll":
                                    notifType = Helper.NotifType.POLL;
                                    if (notif_poll) {
                                        if (notification.getAccount().getId() != null && notification.getAccount().getId().equals(userId))
                                            message = getString(R.string.notif_poll_self);
                                        else
                                            message = getString(R.string.notif_poll);
                                    } else {
                                        canSendBroadCast = false;
                                    }
                                    break;
                                default:
                            }
                            //Some others notification
                            final Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra(Helper.INTENT_ACTION, Helper.NOTIFICATION_INTENT);
                            intent.putExtra(Helper.PREF_KEY_ID, account.getId());
                            intent.putExtra(Helper.PREF_INSTANCE, account.getInstance());
                            if (targeted_account != null) {
                                intent.putExtra(Helper.INTENT_TARGETED_ACCOUNT, targeted_account);
                            }
                            final String finalMessage = message;
                            Handler mainHandler = new Handler(Looper.getMainLooper());
                            Helper.NotifType finalNotifType = notifType;
                            liveNotifBadge++;
                            ShortcutBadger.applyCount(getApplicationContext(), liveNotifBadge);
                            Runnable myRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    if (finalMessage != null) {
                                        Glide.with(getApplicationContext())
                                                .asBitmap()
                                                .load(notification.getAccount().getAvatar())
                                                .listener(new RequestListener<Bitmap>() {
                                                    @Override
                                                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                                        return false;
                                                    }

                                                    @Override
                                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                                                        Helper.notify_user(getApplicationContext(), account, intent, BitmapFactory.decodeResource(getResources(),
                                                                getMainLogo(getApplicationContext())), finalNotifType, "@" + notification.getAccount().getAcct(), finalMessage);
                                                        return false;
                                                    }
                                                })
                                                .into(new SimpleTarget<Bitmap>() {
                                                    @Override
                                                    public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {

                                                        Helper.notify_user(getApplicationContext(), account, intent, resource, finalNotifType, "@" + notification.getAccount().getAcct(), finalMessage);
                                                    }
                                                });
                                    }
                                }
                            };
                            mainHandler.post(myRunnable);
                        }
                    }

                    if (canSendBroadCast) {
                        b.putString("userIdService", account.getId());
                        Intent intentBC = new Intent(Helper.RECEIVE_DATA);
                        intentBC.putExtra("eventStreaming", event);
                        intentBC.putExtras(b);
                        b.putParcelable("data", notification);
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentBC);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString(Helper.LAST_NOTIFICATION_MAX_ID + account.getId() + account.getInstance(), notification.getId());
                        editor.apply();
                    }
                    break;
                case "delete":
                    event = Helper.EventStreaming.DELETE;
                    try {
                        dataId = response.getString("id");
                        b.putString("dataId", dataId);
                    } catch (JSONException ignored) {
                    }
                    break;
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void networkAvailable() {
        startStream();
    }

    @Override
    public void networkUnavailable() {
        Iterator it = threads.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            if (pair.getValue() == null || !((Thread) pair.getValue()).isAlive()) {
                if ((pair.getValue()) != null)
                    ((Thread) pair.getValue()).interrupt();
            }
            it.remove();
        }
    }
}
