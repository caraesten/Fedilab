package fr.gouv.etalab.mastodon.client.Entities;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import fr.gouv.etalab.mastodon.helper.Helper;

/**
 * Created by Thomas on 17/11/2017.
 */

public class HttpsConnection {


    public String post(String urlConnection, HashMap<String, String> paramaters, String token) throws IOException {
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

    public String get(String urlConnection, HashMap<String, String> paramaters, String token) throws IOException {

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
        Log.v(Helper.TAG,"GET: " + urlConnection + "?" + postData);
        URL url = new URL(urlConnection + "?" + postData);
        HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
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
