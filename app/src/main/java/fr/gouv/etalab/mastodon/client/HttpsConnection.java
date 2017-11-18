package fr.gouv.etalab.mastodon.client;
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
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import fr.gouv.etalab.mastodon.client.Entities.Attachment;
import fr.gouv.etalab.mastodon.client.Entities.Error;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveAttachmentInterface;


/**
 * Created by Thomas on 17/11/2017.
 * Manage http queries
 */

public class HttpsConnection {



    private HttpsURLConnection httpsURLConnection;
    private String since_id, max_id;
    private int actionCode;
    private Context context;


    public HttpsConnection(){}

    public HttpsConnection(Context context){
        this.context = context;
    }

    public String get(String urlConnection, int timeout, HashMap<String, String> paramaters, String token) throws IOException, NoSuchAlgorithmException, KeyManagementException, HttpsConnectionException {

        Map<String,Object> params = new LinkedHashMap<>();
        Iterator it = paramaters.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            params.put(pair.getKey().toString(), pair.getValue());
            it.remove();
        }
        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String,Object> param : params.entrySet()) {
            if (postData.length() != 0) postData.append('&');
            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
        }
        URL url = new URL(urlConnection + "?" + postData);
        httpsURLConnection = (HttpsURLConnection)url.openConnection();
        httpsURLConnection.setConnectTimeout(timeout * 1000);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP)
            httpsURLConnection.setSSLSocketFactory(new TLSSocketFactory());
        if( token != null)
            httpsURLConnection.setRequestProperty("Authorization", "Bearer " + token);
        httpsURLConnection.setRequestMethod("GET");
        getSinceMaxId();
        actionCode = httpsURLConnection.getResponseCode();
        if (actionCode >= 200 && actionCode < 400) {
            Reader in = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (int c; (c = in.read()) >= 0; )
                sb.append((char) c);
            httpsURLConnection.disconnect();
            in.close();
            return sb.toString();
        }else {
            Reader in = new BufferedReader(new InputStreamReader(httpsURLConnection.getErrorStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (int c; (c = in.read()) >= 0; )
                sb.append((char) c);
            throw new HttpsConnectionException(actionCode, sb.toString());
        }
    }



    public String post(String urlConnection, int timeout, HashMap<String, String> paramaters, String token) throws IOException, NoSuchAlgorithmException, KeyManagementException, HttpsConnectionException {
        URL url = new URL(urlConnection);
        Map<String,Object> params = new LinkedHashMap<>();
        Iterator it = paramaters.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            params.put(pair.getKey().toString(), pair.getValue());
            it.remove();
        }
        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String,Object> param : params.entrySet()) {
            if (postData.length() != 0) postData.append('&');
            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
        }
        byte[] postDataBytes = postData.toString().getBytes("UTF-8");

        httpsURLConnection = (HttpsURLConnection)url.openConnection();
        httpsURLConnection.setConnectTimeout(timeout * 1000);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP)
            httpsURLConnection.setSSLSocketFactory(new TLSSocketFactory());
        httpsURLConnection.setRequestMethod("POST");
        if( token != null)
            httpsURLConnection.setRequestProperty("Authorization", "Bearer " + token);
        httpsURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        httpsURLConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        actionCode = httpsURLConnection.getResponseCode();
        getSinceMaxId();
        httpsURLConnection.setDoOutput(true);
        if (actionCode >= 200 && actionCode < 400) {
            httpsURLConnection.getOutputStream().write(postDataBytes);
            Reader in = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (int c; (c = in.read()) >= 0;)
                sb.append((char)c);
            httpsURLConnection.disconnect();
            in.close();
            return sb.toString();
        }else {
            Reader in = new BufferedReader(new InputStreamReader(httpsURLConnection.getErrorStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (int c; (c = in.read()) >= 0; )
                sb.append((char) c);
            throw new HttpsConnectionException(actionCode, sb.toString());
        }

    }


    public void upload(final InputStream inputStream, final OnRetrieveAttachmentInterface listener) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                    String token = sharedpreferences.getString(Helper.PREF_KEY_OAUTH_TOKEN, null);
                    final URL url = new URL("https://"+Helper.getLiveInstance(context)+"/api/v1/media");
                    ByteArrayOutputStream ous = null;
                    try {
                        try {
                            byte[] buffer = new byte[4 * 1024]; // or other buffer size
                            ous = new ByteArrayOutputStream();
                            int read;
                            while ((read = inputStream.read(buffer)) != -1) {
                                ous.write(buffer, 0, read);
                            }
                            ous.flush();
                        } finally {
                            if (ous != null)
                                ous.close();
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    byte[] pixels = ous.toByteArray();

                    httpsURLConnection = (HttpsURLConnection) url.openConnection();
                    httpsURLConnection.setConnectTimeout(240 * 1000);
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
                        httpsURLConnection.setSSLSocketFactory(new TLSSocketFactory());
                    httpsURLConnection.setDoInput(true);
                    httpsURLConnection.setDoOutput(true);
                    httpsURLConnection.setUseCaches(false);
                    if (token != null)
                        httpsURLConnection.setRequestProperty("Authorization", "Bearer " + token);
                    httpsURLConnection.setRequestMethod("POST");

                    httpsURLConnection.setRequestProperty("Content-Type",  "multipart/form-data");
                    httpsURLConnection.setRequestProperty("Connection", "Keep-Alive");
                    httpsURLConnection.setRequestProperty("Cache-Control", "no-cache");
                    httpsURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=*****");


                    DataOutputStream request = new DataOutputStream(httpsURLConnection.getOutputStream());
                    request.writeBytes("--*****\r\n");
                    request.writeBytes("Content-Disposition: form-data; name=\"picture\";filename=\"picture.png\"\r\n");
                    request.writeBytes("\r\n");

                    request.write(pixels);


                    int totalSize = pixels.length;
                    int bytesTransferred = 0;
                    int chunkSize = 2000;

                    while (bytesTransferred < totalSize) {
                        int nextChunkSize = totalSize - bytesTransferred;
                        if (nextChunkSize > chunkSize) {
                            nextChunkSize = chunkSize;
                        }
                        request.write(pixels, bytesTransferred, nextChunkSize);
                        bytesTransferred += nextChunkSize;

                        int progress = 100 * bytesTransferred / totalSize;
                        listener.onUpdateProgress(progress);
                    }

                    request.writeBytes("\r\n");
                    request.writeBytes("--*****\r\n");
                    request.flush();
                    request.close();


                    InputStream responseStream = new BufferedInputStream(httpsURLConnection.getInputStream());

                    BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));

                    String line = "";
                    StringBuilder stringBuilder = new StringBuilder();

                    while ((line = responseStreamReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    listener.onUpdateProgress(101);
                    responseStreamReader.close();

                    String response = stringBuilder.toString();
                    Attachment attachment = API.parseAttachmentResponse(new JSONObject(response));
                    listener.onRetrieveAttachment(attachment, null);
                }catch (Exception e) {
                    listener.onUpdateProgress(101);
                    Error error = new Error();
                    error.setError(e.getMessage());
                    listener.onRetrieveAttachment(null, error);
                }
            }
        }).start();


    }

    public String put(String urlConnection, int timeout, HashMap<String, String> paramaters, String token) throws IOException, NoSuchAlgorithmException, KeyManagementException, HttpsConnectionException {
        URL url = new URL(urlConnection);
        Map<String,Object> params = new LinkedHashMap<>();
        Iterator it = paramaters.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            params.put(pair.getKey().toString(), pair.getValue());
            it.remove();
        }
        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String,Object> param : params.entrySet()) {
            if (postData.length() != 0) postData.append('&');
            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
        }
        byte[] postDataBytes = postData.toString().getBytes("UTF-8");

        httpsURLConnection = (HttpsURLConnection)url.openConnection();
        httpsURLConnection.setConnectTimeout(timeout * 1000);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP)
            httpsURLConnection.setSSLSocketFactory(new TLSSocketFactory());
        if( token != null)
            httpsURLConnection.setRequestProperty("Authorization", "Bearer " + token);
        httpsURLConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        getSinceMaxId();
        httpsURLConnection.setRequestMethod("PUT");
        httpsURLConnection.setDoInput(true);
        httpsURLConnection.setDoOutput(true);
        actionCode = httpsURLConnection.getResponseCode();
        if (actionCode >= 200 && actionCode < 400) {
            DataOutputStream dataOutputStream = new DataOutputStream(httpsURLConnection.getOutputStream());
            dataOutputStream.write(postDataBytes);
            Reader in = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (int c; (c = in.read()) >= 0;)
                sb.append((char)c);
            httpsURLConnection.disconnect();
            in.close();
            return sb.toString();
        }else {
            Reader in = new BufferedReader(new InputStreamReader(httpsURLConnection.getErrorStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (int c; (c = in.read()) >= 0; )
                sb.append((char) c);
            throw new HttpsConnectionException(actionCode, sb.toString());
        }

    }



    public String patch(String urlConnection, int timeout, HashMap<String, String> paramaters, String token) throws IOException, NoSuchAlgorithmException, KeyManagementException, HttpsConnectionException {
        URL url = new URL(urlConnection);
        Map<String,Object> params = new LinkedHashMap<>();
        Iterator it = paramaters.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            params.put(pair.getKey().toString(), pair.getValue());
            it.remove();
        }
        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String,Object> param : params.entrySet()) {
            if (postData.length() != 0) postData.append('&');
            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
        }
        byte[] postDataBytes = postData.toString().getBytes("UTF-8");

        httpsURLConnection = (HttpsURLConnection)url.openConnection();
        httpsURLConnection.setConnectTimeout(timeout * 1000);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP)
            httpsURLConnection.setSSLSocketFactory(new TLSSocketFactory());
        httpsURLConnection.setRequestMethod("PATCH");
        if( token != null)
            httpsURLConnection.setRequestProperty("Authorization", "Bearer " + token);
        httpsURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        httpsURLConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        httpsURLConnection.setDoOutput(true);
        getSinceMaxId();
        actionCode = httpsURLConnection.getResponseCode();
        if (actionCode >= 200 && actionCode < 400) {
            httpsURLConnection.getOutputStream().write(postDataBytes);
            Reader in = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (int c; (c = in.read()) >= 0;)
                sb.append((char)c);
            httpsURLConnection.disconnect();
            in.close();
            return sb.toString();
        }else {
            Reader in = new BufferedReader(new InputStreamReader(httpsURLConnection.getErrorStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (int c; (c = in.read()) >= 0; )
                sb.append((char) c);
            throw new HttpsConnectionException(actionCode, sb.toString());
        }

    }


    public int delete(String urlConnection, int timeout, HashMap<String, String> paramaters, String token) throws IOException, NoSuchAlgorithmException, KeyManagementException, HttpsConnectionException {
        URL url = new URL(urlConnection);
        Map<String,Object> params = new LinkedHashMap<>();
        Iterator it = paramaters.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            params.put(pair.getKey().toString(), pair.getValue());
            it.remove();
        }
        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String,Object> param : params.entrySet()) {
            if (postData.length() != 0) postData.append('&');
            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
        }
        byte[] postDataBytes = postData.toString().getBytes("UTF-8");

        httpsURLConnection = (HttpsURLConnection)url.openConnection();
        httpsURLConnection.setConnectTimeout(timeout * 1000);
        getSinceMaxId();
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP)
            httpsURLConnection.setSSLSocketFactory(new TLSSocketFactory());
        if( token != null)
            httpsURLConnection.setRequestProperty("Authorization", "Bearer " + token);
        httpsURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        httpsURLConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        httpsURLConnection.setRequestMethod("DELETE");
        actionCode = httpsURLConnection.getResponseCode();
        if (actionCode >= 200 && actionCode < 400) {
            httpsURLConnection.disconnect();
            return actionCode;
        }else {
            Reader in = new BufferedReader(new InputStreamReader(httpsURLConnection.getErrorStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (int c; (c = in.read()) >= 0; )
                sb.append((char) c);
            throw new HttpsConnectionException(actionCode, sb.toString());
        }
    }

    public String getSince_id() {
        return since_id;
    }

    public String getMax_id() {
        return max_id;
    }


    private void getSinceMaxId(){
        Map<String, List<String>> map = httpsURLConnection.getHeaderFields();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {

            if( entry.toString().startsWith("Link: ")){
                Pattern patternMaxId = Pattern.compile("max_id=([0-9]{1,}).*");
                Matcher matcherMaxId = patternMaxId.matcher(entry.toString());
                if (matcherMaxId.find()) {
                    max_id = matcherMaxId.group(1);
                }
                if( entry.toString().startsWith("Link: ")){
                    Pattern patternSinceId = Pattern.compile("since_id=([0-9]{1,}).*");
                    Matcher matcherSinceId = patternSinceId.matcher(entry.toString());
                    if (matcherSinceId.find()) {
                        since_id = matcherSinceId.group(1);
                    }
                }
            }
        }
    }

    public int getActionCode() {
        return actionCode;
    }

    public class HttpsConnectionException extends Exception {

        private int statusCode;
        private String message;

        HttpsConnectionException(int statusCode, String message) {
            this.statusCode = statusCode;
            this.message = message;
        }


        public int getStatusCode() {
            return statusCode;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }
}
