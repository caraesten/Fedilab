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
package fr.gouv.etalab.mastodon.asynctasks;


import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveStreamingInterface;


/**
 * Created by Thomas on 28/08/2017.
 * Calls user streaming api
 */

public class StreamingUserAsyncTask extends AsyncTask {

    private String instance, token, acct, userId;
    private OnRetrieveStreamingInterface listener;
    private static HashMap<String, HttpURLConnection> connectionHashMap;
    private EventStreaming lastEvent;


    public StreamingUserAsyncTask(String instance, String token, String acct, String userId, OnRetrieveStreamingInterface onRetrieveStreamingInterface){
        this.instance = instance;
        this.token = token;
        this.acct = acct;
        this.userId = userId;
        this.listener = onRetrieveStreamingInterface;
    }
    public enum EventStreaming{
        UPDATE,
        NOTIFICATION,
        DELETE,
        NONE
    }


    @Override
    protected Object doInBackground(Object[] params){
        if( connectionHashMap == null)
            connectionHashMap = new HashMap<>();

        boolean connectionAlive = false;
        if( connectionHashMap.get(acct+userId) != null) {
            try {
                connectionAlive = (connectionHashMap.get(acct + userId).getResponseCode() == 200);
            } catch (IOException e) {
                connectionAlive = false;
            }
        }

        if( !connectionAlive) {
            try {
                URL url = new URL("https://" + this.instance + "/api/v1/streaming/user");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Authorization", "Bearer " + this.token);
                urlConnection.setRequestProperty("Connection", "Keep-Alive");
                urlConnection.setRequestProperty("Keep-Alive", "header");
                connectionHashMap.put(acct+userId, urlConnection);

                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                readStream(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private String readStream(InputStream inputStream) {
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String event;
            EventStreaming eventStreaming = null;
            while((event = reader.readLine()) != null){
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
                    event = event.replace("data: ","");
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
                        listener.onRetrieveStreaming(eventStreaming, eventJson, acct, userId);
                    } catch (JSONException e) {
                        e.printStackTrace();
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
        }
        return null;
    }

}
