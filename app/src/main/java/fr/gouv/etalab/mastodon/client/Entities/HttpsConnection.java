package fr.gouv.etalab.mastodon.client.Entities;
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
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import fr.gouv.etalab.mastodon.client.TLSSocketFactory;

/**
 * Created by Thomas on 17/11/2017.
 * Manage http queries
 */

public class HttpsConnection {



    public String post(String urlConnection, HashMap<String, String> paramaters, String token) throws IOException, NoSuchAlgorithmException, KeyManagementException {
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

        HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP)
            conn.setSSLSocketFactory(new TLSSocketFactory());
        conn.setRequestMethod("POST");
        if( token != null)
            conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        conn.setDoOutput(true);
        conn.getOutputStream().write(postDataBytes);
        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (int c; (c = in.read()) >= 0;)
            sb.append((char)c);
        return sb.toString();
    }

    public String get(String urlConnection, HashMap<String, String> paramaters, String token) throws IOException, NoSuchAlgorithmException, KeyManagementException {

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
        HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP)
            conn.setSSLSocketFactory(new TLSSocketFactory());
        if( token != null)
            conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestMethod("GET");
        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (int c; (c = in.read()) >= 0;)
            sb.append((char)c);
        return sb.toString();
    }

}
