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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.Headers;
import com.koushikdutta.async.http.WebSocket;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;

import app.fedilab.android.client.API;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Status;
import app.fedilab.android.client.TLSSocketFactory;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.sqlite.AccountDAO;
import app.fedilab.android.sqlite.Sqlite;


/**
 * Created by Thomas on 29/09/2017.
 * Manage service for streaming api for local timeline
 */

public class StreamingLocalTimelineService extends IntentService {

    static {
        Helper.installProvider();
    }
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    @SuppressWarnings("unused")
    public StreamingLocalTimelineService(String name) {
        super(name);
    }
    @SuppressWarnings("unused")
    public StreamingLocalTimelineService() {
        super("StreamingLocalTimelineService");
    }

    private static HttpsURLConnection httpsURLConnection;
    protected Account account;

    public void onCreate() {
        super.onCreate();
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        boolean display_local = sharedpreferences.getBoolean(Helper.SET_DISPLAY_LOCAL, true);
        if( !display_local){
            stopSelf();
        }
        SharedPreferences.Editor editor = sharedpreferences.edit();
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = sharedpreferences.getString(Helper.PREF_INSTANCE, Helper.getLiveInstance(getApplicationContext()));
        editor.putBoolean(Helper.SHOULD_CONTINUE_STREAMING_LOCAL + userId + instance, true);
        editor.apply();
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = sharedpreferences.getString(Helper.PREF_INSTANCE, null);
        Account accountStream = null;
        if( userId != null) {
            SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
            accountStream = new AccountDAO(getApplicationContext(), db).getUniqAccount(userId, instance);
        }

        if( accountStream != null) {
            Headers headers = new Headers();
            headers.add("Authorization", "Bearer " + accountStream.getToken());
            headers.add("Connection", "Keep-Alive");
            headers.add("method", "GET");
            headers.add("scheme", "https");
            Uri url = Uri.parse("wss://" + accountStream.getInstance() + "/api/v1/streaming/?stream=public:local&access_token="+ accountStream.getToken());
            AsyncHttpRequest.setDefaultHeaders(headers, url);
            Account finalAccountStream = accountStream;
            if( Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT ) {
                try {
                    AsyncHttpClient.getDefaultInstance().getSSLSocketMiddleware().setSSLContext(new TLSSocketFactory().getSSLContext());
                    AsyncHttpClient.getDefaultInstance().getSSLSocketMiddleware().setConnectAllAddresses(true);
                } catch (KeyManagementException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
            AsyncHttpClient.getDefaultInstance().websocket("wss://" + accountStream.getInstance() + "/api/v1/streaming/?stream=public:local&access_token="+ accountStream.getToken(),"wss", new AsyncHttpClient.WebSocketConnectCallback() {
                @Override
                public void onCompleted(Exception ex, WebSocket webSocket) {
                    if (ex != null) {
                        ex.printStackTrace();
                        return;
                    }
                    webSocket.setStringCallback(new WebSocket.StringCallback() {
                        public void onStringAvailable(String s) {
                            if (!sharedpreferences.getBoolean(Helper.SHOULD_CONTINUE_STREAMING_LOCAL + finalAccountStream.getId() + finalAccountStream.getInstance(), true)) {
                                stopSelf();
                                return;
                            }
                            try {
                                JSONObject eventJson = new JSONObject(s);
                                onRetrieveStreaming(finalAccountStream, eventJson);
                            } catch (JSONException ignored) {}
                        }
                    });
                }
            });
        }
    }

    public void onRetrieveStreaming(Account account, JSONObject response) {
        if(  response == null )
            return;
        Status status ;
        Bundle b = new Bundle();
        try {
            if( response.get("event").toString().equals("update")){
                status = API.parseStatuses(getApplicationContext(), new JSONObject(response.get("payload").toString()));
                status.setNew(true);
                b.putParcelable("data", status);
                if( account != null)
                    b.putString("userIdService",account.getId());
                Intent intentBC = new Intent(Helper.RECEIVE_LOCAL_DATA);
                intentBC.putExtras(b);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentBC);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
