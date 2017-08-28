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
import java.net.MalformedURLException;
import java.net.URL;

import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveStreamingInterface;


/**
 * Created by Thomas on 28/08/2017.
 * Calls user streaming api
 */

public class StreamingUserAsyncTask extends AsyncTask {

    private String instance, token, acct, userId;
    private OnRetrieveStreamingInterface listener;

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
        DELETE
    }

    @Override
    protected Object doInBackground(Object[] params){
        try {
            URL url = new URL("https://" + this.instance + "/api/v1/streaming/user");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Authorization", "Bearer "+this.token);
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("Keep-Alive", "header");
            Log.v(Helper.TAG, "http response: " + urlConnection.getResponseCode());

            //Object inputStream = urlConnection.getContent();
            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
            Log.v(Helper.TAG, readStrem(inputStream)+"");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Log.v(Helper.TAG, "Error on url openConnection: "+e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    private String readStrem(InputStream inputStream) {
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            String event;
            EventStreaming eventStreaming = null;
            while((line = reader.readLine()) != null){
                switch (line.trim()){
                    case "update":
                        event = reader.readLine();
                        eventStreaming = EventStreaming.UPDATE;
                        break;
                    case "notification":
                        event = reader.readLine();
                        eventStreaming = EventStreaming.NOTIFICATION;
                        break;
                    case "delete":
                        event = "{\"id\":" + reader.readLine() + "}";
                        eventStreaming = EventStreaming.DELETE;
                        break;
                    default:
                        event = null;
                }
                if( event != null){
                    try {
                        JSONObject eventJson = new JSONObject(event);
                        listener.onRetrieveStreaming(eventStreaming, eventJson, acct, userId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }catch (IOException e){
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
