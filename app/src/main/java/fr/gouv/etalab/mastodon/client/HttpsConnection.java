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
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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


/**
 * Created by Thomas on 17/11/2017.
 * Manage http queries
 */

public class HttpsConnection {



    private HttpsURLConnection httpsURLConnection;
    private String since_id, max_id;

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
        httpsURLConnection.setConnectTimeout(timeout);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP)
            httpsURLConnection.setSSLSocketFactory(new TLSSocketFactory());
        if( token != null)
            httpsURLConnection.setRequestProperty("Authorization", "Bearer " + token);
        httpsURLConnection.setRequestMethod("GET");
        getSinceMaxId();
        int statusCode = httpsURLConnection.getResponseCode();
        if (statusCode >= 200 && statusCode < 400) {
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
            throw new HttpsConnectionException(statusCode, sb.toString());
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
        httpsURLConnection.setConnectTimeout(timeout);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP)
            httpsURLConnection.setSSLSocketFactory(new TLSSocketFactory());
        httpsURLConnection.setRequestMethod("POST");
        if( token != null)
            httpsURLConnection.setRequestProperty("Authorization", "Bearer " + token);
        httpsURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        httpsURLConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        int statusCode = httpsURLConnection.getResponseCode();
        getSinceMaxId();
        httpsURLConnection.setDoOutput(true);
        if (statusCode >= 200 && statusCode < 400) {
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
            throw new HttpsConnectionException(statusCode, sb.toString());
        }

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
        httpsURLConnection.setConnectTimeout(timeout);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP)
            httpsURLConnection.setSSLSocketFactory(new TLSSocketFactory());
        if( token != null)
            httpsURLConnection.setRequestProperty("Authorization", "Bearer " + token);
        httpsURLConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        getSinceMaxId();
        httpsURLConnection.setRequestMethod("PUT");
        httpsURLConnection.setDoInput(true);
        httpsURLConnection.setDoOutput(true);
        int statusCode = httpsURLConnection.getResponseCode();
        if (statusCode >= 200 && statusCode < 400) {
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
            throw new HttpsConnectionException(statusCode, sb.toString());
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
        httpsURLConnection.setConnectTimeout(timeout);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP)
            httpsURLConnection.setSSLSocketFactory(new TLSSocketFactory());
        httpsURLConnection.setRequestMethod("PATCH");
        if( token != null)
            httpsURLConnection.setRequestProperty("Authorization", "Bearer " + token);
        httpsURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        httpsURLConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        httpsURLConnection.setDoOutput(true);
        getSinceMaxId();
        int statusCode = httpsURLConnection.getResponseCode();
        if (statusCode >= 200 && statusCode < 400) {
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
            throw new HttpsConnectionException(statusCode, sb.toString());
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
        httpsURLConnection.setConnectTimeout(timeout);
        getSinceMaxId();
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP)
            httpsURLConnection.setSSLSocketFactory(new TLSSocketFactory());
        if( token != null)
            httpsURLConnection.setRequestProperty("Authorization", "Bearer " + token);
        httpsURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        httpsURLConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        httpsURLConnection.setRequestMethod("DELETE");
        int statusCode = httpsURLConnection.getResponseCode();
        if (statusCode >= 200 && statusCode < 400) {
            httpsURLConnection.disconnect();
            return statusCode;
        }else {
            Reader in = new BufferedReader(new InputStreamReader(httpsURLConnection.getErrorStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (int c; (c = in.read()) >= 0; )
                sb.append((char) c);
            throw new HttpsConnectionException(statusCode, sb.toString());
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
