package app.fedilab.android.client;
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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.Html;
import android.text.SpannableString;

import com.google.gson.JsonObject;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

import org.apache.poi.util.IOUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import app.fedilab.android.R;
import app.fedilab.android.activities.SlideMediaActivity;
import app.fedilab.android.activities.TootActivity;
import app.fedilab.android.client.Entities.Error;
import app.fedilab.android.helper.FileNameCleaner;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.interfaces.OnDownloadInterface;
import okhttp3.Cache;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static app.fedilab.android.helper.Helper.urlPattern;


/**
 * Created by Thomas on 17/11/2017.
 * Manage http queries
 */

public class HttpsConnection {


    private HttpsURLConnection httpsURLConnection;
    private HttpURLConnection httpURLConnection;
    private String since_id, max_id;
    private Context context;
    private int CHUNK_SIZE = 4096;
    private SharedPreferences sharedpreferences;
    private Proxy proxy;
    private String instance;
    private String USER_AGENT;
    private int cacheSize = 30*1024*1024;

    public HttpsConnection(Context context, String instance) {
        this.instance = instance;
        this.context = context;
        sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        boolean proxyEnabled = sharedpreferences.getBoolean(Helper.SET_PROXY_ENABLED, false);
        int type = sharedpreferences.getInt(Helper.SET_PROXY_TYPE, 0);
        proxy = null;

        USER_AGENT = sharedpreferences.getString(Helper.SET_CUSTOM_USER_AGENT, Helper.USER_AGENT);
        if (proxyEnabled) {
            try {
                String host = sharedpreferences.getString(Helper.SET_PROXY_HOST, "127.0.0.1");
                int port = sharedpreferences.getInt(Helper.SET_PROXY_PORT, 8118);
                if (type == 0)
                    proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
                else
                    proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(host, port));
                final String login = sharedpreferences.getString(Helper.SET_PROXY_LOGIN, null);
                final String pwd = sharedpreferences.getString(Helper.SET_PROXY_PASSWORD, null);
                if (login != null) {
                    Authenticator authenticator = new Authenticator() {
                        public PasswordAuthentication getPasswordAuthentication() {
                            assert pwd != null;
                            return (new PasswordAuthentication(login,
                                    pwd.toCharArray()));
                        }
                    };
                    Authenticator.setDefault(authenticator);
                }
            } catch (Exception e) {
                proxy = null;
            }

        }

        if (instance != null && instance.endsWith(".onion")) {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @SuppressLint("BadHostnameVerifier")
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });
        }
    }


    /**
     * Get calls
     * @param urlConnection String url
     * @param timeout int timeout
     * @param paramaters HashMap<String, String> paramaters
     * @param token String token
     * @return String
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws HttpsConnectionException
     */
    public String get(String urlConnection, int timeout, HashMap<String, String> paramaters, String token) throws IOException, NoSuchAlgorithmException, KeyManagementException, HttpsConnectionException {


        Map<String, Object> params = new LinkedHashMap<>();
        if (paramaters != null) {
            Iterator it = paramaters.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                params.put(pair.getKey().toString(), pair.getValue());
                it.remove();
            }
        }
        StringBuilder postData = new StringBuilder();
        URL url;
        if( params.size() > 0 ) {
            for (Map.Entry<String, Object> param : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(param.getKey());
                postData.append('=');
                postData.append(param.getValue());
            }
            url = new URL(urlConnection + "?" + postData);
        }else{
            url = new URL(urlConnection);
        }

        if (Build.VERSION.SDK_INT >= 21) {
            Cache cache = new Cache(context.getCacheDir(), cacheSize);
            OkHttpClient.Builder builder = new OkHttpClient.Builder().connectTimeout(timeout, TimeUnit.SECONDS).cache(cache);
            if (proxy != null) {
                builder.proxy(proxy);
            }
            OkHttpClient client = builder.build();
            Request.Builder requestBuilder = new Request.Builder()
                    .url(urlConnection);
            HttpUrl.Builder httpBuider = Objects.requireNonNull(HttpUrl.parse(url.toString())).newBuilder();
            if (token != null && !token.startsWith("Basic ")) {
                requestBuilder.addHeader("Authorization", "Bearer " + token);
            } else if (token != null && token.startsWith("Basic ")) {
                requestBuilder.addHeader("Authorization", token);
            }
            Request requesthttp = requestBuilder

                    .url(httpBuider.build())
                    .build();
            try (Response httpresponse = client.newCall(requesthttp).execute()) {
                assert httpresponse.body() != null;
                String response = httpresponse.body().string();
                int code = httpresponse.code();
                String error = httpresponse.message();
                if (code >= 200 && code < 400) {
                    getOKHttpHeader(httpresponse.headers().toMultimap());
                    return response;
                } else {
                    throw new HttpsConnectionException(code, error);
                }
            } finally {
                if (!cache.isClosed()) {
                    try {
                        cache.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }else{

            if (proxy != null)
                httpsURLConnection = (HttpsURLConnection) url.openConnection(proxy);
            else
                httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.setConnectTimeout(timeout * 1000);
            httpsURLConnection.setRequestProperty("http.keepAlive", "false");
            httpsURLConnection.setRequestProperty("User-Agent", USER_AGENT);
            httpsURLConnection.setRequestProperty("Content-Type", "application/json");
            httpsURLConnection.setRequestProperty("Accept", "application/json");
            httpsURLConnection.setSSLSocketFactory(new TLSSocketFactory(this.instance));
            if (token != null && !token.startsWith("Basic "))
                httpsURLConnection.setRequestProperty("Authorization", "Bearer " + token);
            else if (token != null && token.startsWith("Basic "))
                httpsURLConnection.setRequestProperty("Authorization", token);
            httpsURLConnection.setRequestMethod("GET");
            String response;
            if (httpsURLConnection.getResponseCode() >= 200 && httpsURLConnection.getResponseCode() < 400) {
                response = converToString(httpsURLConnection.getInputStream());
            } else {
                String error = null;
                if (httpsURLConnection.getErrorStream() != null) {
                    InputStream stream = httpsURLConnection.getErrorStream();
                    if (stream == null) {
                        stream = httpsURLConnection.getInputStream();
                    }
                    try (Scanner scanner = new Scanner(stream)) {
                        scanner.useDelimiter("\\Z");
                        if (scanner.hasNext()) {
                            error = scanner.next();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                int responseCode = httpsURLConnection.getResponseCode();
                try {
                    httpsURLConnection.getInputStream().close();
                } catch (Exception ignored) {
                }
                throw new HttpsConnectionException(responseCode, error);
            }
            getSinceMaxId();
            httpsURLConnection.getInputStream().close();
            return response;
        }
    }


    /**
     * Will check if the current url is redirecting
     * @param urlConnection String the url to check
     * @return String null|string url redirection
     */
    public String checkUrl(String urlConnection){
        URL url;
        String redirect = null;
        try {
            url = new URL(urlConnection);
            if (proxy != null)
                httpsURLConnection = (HttpsURLConnection) url.openConnection(proxy);
            else
                httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.setRequestProperty("http.keepAlive", "false");
            httpsURLConnection.setInstanceFollowRedirects(false);
            httpsURLConnection.setSSLSocketFactory(new TLSSocketFactory(this.instance));
            httpsURLConnection.setRequestMethod("HEAD");
            if( httpsURLConnection.getResponseCode() == 301) {
                Map<String, List<String>> map = httpsURLConnection.getHeaderFields();
                for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                    if (entry.toString().toLowerCase().startsWith("location")) {
                        Matcher matcher = urlPattern.matcher(entry.toString());
                        if (matcher.find()) {
                            redirect = matcher.group(1);
                        }
                    }
                }
            }
            httpsURLConnection.getInputStream().close();
            if (redirect != null && redirect.compareTo(urlConnection)!=0){
                URL redirectURL = new URL(redirect);
                String host = redirectURL.getHost();
                String protocol = redirectURL.getProtocol();
                if( protocol == null || host == null){
                    redirect = null;
                }
            }
            return redirect;
        } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
        return null;
    }


    public String get(String urlConnection) throws IOException, NoSuchAlgorithmException, KeyManagementException, HttpsConnectionException {

        if (Build.VERSION.SDK_INT >= 21) {
            Cache cache = new Cache(context.getCacheDir(), cacheSize);
            OkHttpClient.Builder builder = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).cache(cache);
            if (proxy != null) {
                builder.proxy(proxy);
            }
            if( !urlConnection.startsWith("http")){
                urlConnection = "http://" + urlConnection;
            }
            OkHttpClient client = builder.build();
            Request.Builder requestBuilder = new Request.Builder()
                    .url(urlConnection);
            HttpUrl.Builder httpBuider = Objects.requireNonNull(HttpUrl.parse(urlConnection)).newBuilder();
            Request requesthttp = requestBuilder

                    .url(httpBuider.build())
                    .build();
            try (Response httpresponse = client.newCall(requesthttp).execute()) {
                assert httpresponse.body() != null;
                String response = httpresponse.body().string();
                int code = httpresponse.code();
                String error = httpresponse.message();
                if (code >= 200 && code < 400) {
                    getOKHttpHeader(httpresponse.headers().toMultimap());
                    return response;
                } else {
                    throw new HttpsConnectionException(code, error);
                }
            }finally {
                if (!cache.isClosed()) {
                    try {
                        cache.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        } else {
            URL url = new URL(urlConnection);
            if (proxy != null)
                httpsURLConnection = (HttpsURLConnection) url.openConnection(proxy);
            else
                httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.setConnectTimeout(30 * 1000);
            httpsURLConnection.setRequestProperty("http.keepAlive", "false");
            httpsURLConnection.setRequestProperty("Content-Type", "application/json");
            httpsURLConnection.setRequestProperty("Accept", "application/json");
            httpsURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1667.0 Safari/537.36");
            httpsURLConnection.setSSLSocketFactory(new TLSSocketFactory(this.instance));
            httpsURLConnection.setRequestMethod("GET");
            String response;
            if (httpsURLConnection.getResponseCode() >= 200 && httpsURLConnection.getResponseCode() < 400) {
                getSinceMaxId();
                response = converToString(httpsURLConnection.getInputStream());
            } else {
                String error = null;
                if (httpsURLConnection.getErrorStream() != null) {
                    InputStream stream = httpsURLConnection.getErrorStream();
                    if (stream == null) {
                        stream = httpsURLConnection.getInputStream();
                    }
                    try (Scanner scanner = new Scanner(stream)) {
                        scanner.useDelimiter("\\Z");
                        if (scanner.hasNext()) {
                            error = scanner.next();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                int responseCode = httpsURLConnection.getResponseCode();
                try {
                    httpsURLConnection.getInputStream().close();
                } catch (Exception ignored) {
                }
                throw new HttpsConnectionException(responseCode, error);
            }
            getSinceMaxId();
            httpsURLConnection.getInputStream().close();
            return response;
        }
    }


    public String post(String urlConnection, int timeout, HashMap<String, String> paramaters, String token) throws IOException, NoSuchAlgorithmException, KeyManagementException, HttpsConnectionException {
        if (urlConnection.startsWith("https://")) {
            URL url = new URL(urlConnection);
            Map<String, Object> params = new LinkedHashMap<>();
            if (paramaters != null) {
                Iterator it = paramaters.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    params.put(pair.getKey().toString(), pair.getValue());
                    it.remove();
                }
            }
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, Object> param : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(param.getKey());
                postData.append('=');
                postData.append(param.getValue());
            }
            byte[] postDataBytes = postData.toString().getBytes(StandardCharsets.UTF_8);
            if (proxy != null)
                httpsURLConnection = (HttpsURLConnection) url.openConnection(proxy);
            else
                httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.setRequestProperty("User-Agent", USER_AGENT);
            httpsURLConnection.setConnectTimeout(timeout * 1000);
            httpsURLConnection.setDoOutput(true);
            httpsURLConnection.setSSLSocketFactory(new TLSSocketFactory(this.instance));
            httpsURLConnection.setRequestMethod("POST");
            if (token != null && !token.startsWith("Basic "))
                httpsURLConnection.setRequestProperty("Authorization", "Bearer " + token);
            else if (token != null && token.startsWith("Basic "))
                httpsURLConnection.setRequestProperty("Authorization", token);
            httpsURLConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));


            httpsURLConnection.getOutputStream().write(postDataBytes);
            String response;
            if (httpsURLConnection.getResponseCode() >= 200 && httpsURLConnection.getResponseCode() < 400) {
                getSinceMaxId();
                response = converToString(httpsURLConnection.getInputStream());
            } else {
                String error = null;
                if (httpsURLConnection.getErrorStream() != null) {
                    InputStream stream = httpsURLConnection.getErrorStream();
                    if (stream == null) {
                        stream = httpsURLConnection.getInputStream();
                    }
                    try (Scanner scanner = new Scanner(stream)) {
                        scanner.useDelimiter("\\Z");
                        if (scanner.hasNext()) {
                            error = scanner.next();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                int responseCode = httpsURLConnection.getResponseCode();
                try {
                    httpsURLConnection.getInputStream().close();
                } catch (Exception ignored) {
                }
                throw new HttpsConnectionException(responseCode, error);
            }
            getSinceMaxId();
            httpsURLConnection.getInputStream().close();
            return response;
        } else {
            URL url = new URL(urlConnection);
            Map<String, Object> params = new LinkedHashMap<>();
            if (paramaters != null) {
                Iterator it = paramaters.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    params.put(pair.getKey().toString(), pair.getValue());
                    it.remove();
                }
            }
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, Object> param : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(param.getKey());
                postData.append('=');
                postData.append(param.getValue());
            }
            byte[] postDataBytes = postData.toString().getBytes(StandardCharsets.UTF_8);

            if (proxy != null)
                httpURLConnection = (HttpURLConnection) url.openConnection(proxy);
            else
                httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestProperty("User-Agent", USER_AGENT);
            httpURLConnection.setConnectTimeout(timeout * 1000);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("POST");
            if (token != null && !token.startsWith("Basic "))
                httpURLConnection.setRequestProperty("Authorization", "Bearer " + token);
            else if (token != null && token.startsWith("Basic "))
                httpURLConnection.setRequestProperty("Authorization", token);
            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));

            httpURLConnection.getOutputStream().write(postDataBytes);
            String response;
            if (httpURLConnection.getResponseCode() >= 200 && httpURLConnection.getResponseCode() < 400) {
                getSinceMaxId();
                response = converToString(httpURLConnection.getInputStream());
            } else {
                String error = null;
                if (httpURLConnection.getErrorStream() != null) {
                    InputStream stream = httpURLConnection.getErrorStream();
                    if (stream == null) {
                        stream = httpURLConnection.getInputStream();
                    }
                    try (Scanner scanner = new Scanner(stream)) {
                        scanner.useDelimiter("\\Z");
                        if (scanner.hasNext()) {
                            error = scanner.next();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                int responseCode = httpURLConnection.getResponseCode();
                try {
                    httpURLConnection.getInputStream().close();
                } catch (Exception ignored) {
                }
                throw new HttpsConnectionException(responseCode, error);
            }
            getSinceMaxId();
            httpURLConnection.getInputStream().close();
            return response;
        }

    }


    public String postJson(String urlConnection, int timeout, JsonObject jsonObject, String token) throws IOException, NoSuchAlgorithmException, KeyManagementException, HttpsConnectionException {
        if (urlConnection.startsWith("https://")) {
            URL url = new URL(urlConnection);
            byte[] postDataBytes = new byte[0];
            postDataBytes = jsonObject.toString().getBytes(StandardCharsets.UTF_8);
            if (proxy != null)
                httpsURLConnection = (HttpsURLConnection) url.openConnection(proxy);
            else
                httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.setRequestProperty("User-Agent", USER_AGENT);
            httpsURLConnection.setConnectTimeout(timeout * 1000);
            httpsURLConnection.setDoOutput(true);
            httpsURLConnection.setSSLSocketFactory(new TLSSocketFactory(this.instance));
            httpsURLConnection.setRequestProperty("Content-Type", "application/json");
            httpsURLConnection.setRequestProperty("Accept", "application/json");
            httpsURLConnection.setRequestMethod("POST");
            if (token != null && !token.startsWith("Basic "))
                httpsURLConnection.setRequestProperty("Authorization", "Bearer " + token);
            else if (token != null && token.startsWith("Basic "))
                httpsURLConnection.setRequestProperty("Authorization", token);
            httpsURLConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));


            httpsURLConnection.getOutputStream().write(postDataBytes);
            String response;
            if (httpsURLConnection.getResponseCode() >= 200 && httpsURLConnection.getResponseCode() < 400) {
                getSinceMaxId();
                response = converToString(httpsURLConnection.getInputStream());
            } else {
                String error = null;
                if (httpsURLConnection.getErrorStream() != null) {
                    InputStream stream = httpsURLConnection.getErrorStream();
                    if (stream == null) {
                        stream = httpsURLConnection.getInputStream();
                    }
                    try (Scanner scanner = new Scanner(stream)) {
                        scanner.useDelimiter("\\Z");
                        if (scanner.hasNext()) {
                            error = scanner.next();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                int responseCode = httpsURLConnection.getResponseCode();
                try {
                    httpsURLConnection.getInputStream().close();
                } catch (Exception ignored) {
                }
                throw new HttpsConnectionException(responseCode, error);
            }
            getSinceMaxId();
            httpsURLConnection.getInputStream().close();
            return response;
        } else {
            URL url = new URL(urlConnection);
            byte[] postDataBytes = jsonObject.toString().getBytes(StandardCharsets.UTF_8);

            if (proxy != null)
                httpURLConnection = (HttpURLConnection) url.openConnection(proxy);
            else
                httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestProperty("User-Agent", USER_AGENT);
            httpURLConnection.setConnectTimeout(timeout * 1000);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("POST");
            if (token != null && !token.startsWith("Basic "))
                httpURLConnection.setRequestProperty("Authorization", "Bearer " + token);
            else if (token != null && token.startsWith("Basic "))
                httpURLConnection.setRequestProperty("Authorization", token);
            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));

            httpURLConnection.getOutputStream().write(postDataBytes);
            String response;
            if (httpURLConnection.getResponseCode() >= 200 && httpURLConnection.getResponseCode() < 400) {
                getSinceMaxId();
                response = converToString(httpURLConnection.getInputStream());
            } else {
                String error = null;
                if (httpURLConnection.getErrorStream() != null) {
                    InputStream stream = httpURLConnection.getErrorStream();
                    if (stream == null) {
                        stream = httpURLConnection.getInputStream();
                    }
                    try (Scanner scanner = new Scanner(stream)) {
                        scanner.useDelimiter("\\Z");
                        if (scanner.hasNext()) {
                            error = scanner.next();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                int responseCode = httpURLConnection.getResponseCode();
                try {
                    httpURLConnection.getInputStream().close();
                } catch (Exception ignored) {
                }
                throw new HttpsConnectionException(responseCode, error);
            }
            getSinceMaxId();
            httpURLConnection.getInputStream().close();
            return response;
        }

    }

    public String postMisskey(String urlConnection, int timeout, JSONObject paramaters, String token) throws IOException, NoSuchAlgorithmException, KeyManagementException, HttpsConnectionException {
        URL url = new URL(urlConnection);
        byte[] postDataBytes = paramaters.toString().getBytes(StandardCharsets.UTF_8);

        if (proxy != null)
            httpsURLConnection = (HttpsURLConnection) url.openConnection(proxy);
        else
            httpsURLConnection = (HttpsURLConnection) url.openConnection();
        httpsURLConnection.setRequestProperty("User-Agent", USER_AGENT);
        httpsURLConnection.setConnectTimeout(timeout * 1000);
        httpsURLConnection.setDoOutput(true);
        httpsURLConnection.setSSLSocketFactory(new TLSSocketFactory(this.instance));
        httpsURLConnection.setRequestMethod("POST");
        if (token != null && !token.startsWith("Basic "))
            httpsURLConnection.setRequestProperty("Authorization", "Bearer " + token);
        else if (token != null && token.startsWith("Basic "))
            httpsURLConnection.setRequestProperty("Authorization", token);
        httpsURLConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));


        httpsURLConnection.getOutputStream().write(postDataBytes);
        String response;
        if (httpsURLConnection.getResponseCode() >= 200 && httpsURLConnection.getResponseCode() < 400) {
            getSinceMaxId();
            response = converToString(httpsURLConnection.getInputStream());
        } else {
            String error = null;
            if (httpsURLConnection.getErrorStream() != null) {
                InputStream stream = httpsURLConnection.getErrorStream();
                if (stream == null) {
                    stream = httpsURLConnection.getInputStream();
                }
                try (Scanner scanner = new Scanner(stream)) {
                    scanner.useDelimiter("\\Z");
                    if (scanner.hasNext()) {
                        error = scanner.next();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            int responseCode = httpsURLConnection.getResponseCode();
            try {
                httpsURLConnection.getInputStream().close();
            } catch (Exception ignored) {
            }
            throw new HttpsConnectionException(responseCode, error);
        }
        getSinceMaxId();
        httpsURLConnection.getInputStream().close();
        return response;
    }


    /***
     * Download method which works for http and https connections
     * @param downloadUrl String download url
     * @param listener OnDownloadInterface, listener which manages progress
     */
    public void download(final String downloadUrl, final OnDownloadInterface listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                URL url;
                HttpsURLConnection httpsURLConnection = null;
                HttpURLConnection httpURLConnection = null;
                if (downloadUrl.startsWith("https://")) {
                    try {
                        url = new URL(downloadUrl);
                        if (proxy != null)
                            httpsURLConnection = (HttpsURLConnection) url.openConnection(proxy);
                        else
                            httpsURLConnection = (HttpsURLConnection) url.openConnection();
                        httpsURLConnection.setRequestProperty("User-Agent", USER_AGENT);
                        int responseCode = httpsURLConnection.getResponseCode();

                        // always check HTTP response code first
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            String fileName = "";
                            String disposition = httpsURLConnection.getHeaderField("Content-Disposition");

                            if (disposition != null) {
                                // extracts file name from header field
                                int index = disposition.indexOf("filename=");
                                if (index > 0) {
                                    fileName = disposition.substring(index + 10,
                                            disposition.length() - 1);
                                }
                            } else {
                                // extracts file name from URL
                                fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1
                                );
                            }
                            fileName = FileNameCleaner.cleanFileName(fileName);
                            // opens input stream from the HTTP connection
                            InputStream inputStream = httpsURLConnection.getInputStream();
                            File saveDir = context.getCacheDir();
                            final String saveFilePath = saveDir + File.separator + fileName;

                            // opens an output stream to save into file
                            FileOutputStream outputStream = new FileOutputStream(saveFilePath);

                            int bytesRead;
                            byte[] buffer = new byte[CHUNK_SIZE];
                            int contentSize = httpsURLConnection.getContentLength();
                            int downloadedFileSize = 0;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                                downloadedFileSize += bytesRead;
                                if (context instanceof SlideMediaActivity) {
                                    final int currentProgress = (downloadedFileSize * 100) / contentSize;
                                    ((SlideMediaActivity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            listener.onUpdateProgress(currentProgress > 0 ? currentProgress : 101);
                                        }
                                    });
                                }
                            }
                            outputStream.close();
                            inputStream.close();
                            if (context instanceof TootActivity)
                                ((TootActivity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        listener.onDownloaded(saveFilePath, downloadUrl, null);
                                    }
                                });
                            if (context instanceof SlideMediaActivity)
                                ((SlideMediaActivity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        listener.onDownloaded(saveFilePath, downloadUrl, null);
                                    }
                                });
                        } else {
                            final Error error = new Error();
                            error.setError(String.valueOf(responseCode));
                            if (context instanceof TootActivity)
                                ((TootActivity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        listener.onDownloaded(null, downloadUrl, error);
                                    }
                                });
                            if (context instanceof SlideMediaActivity)
                                ((SlideMediaActivity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        listener.onDownloaded(null, downloadUrl, error);
                                    }
                                });

                        }
                    } catch (IOException e) {
                        Error error = new Error();
                        error.setError(context.getString(R.string.toast_error));
                    }

                } else {
                    try {
                        url = new URL(downloadUrl);
                        if (proxy != null)
                            httpURLConnection = (HttpURLConnection) url.openConnection(proxy);
                        else
                            httpURLConnection = (HttpURLConnection) url.openConnection();
                        httpURLConnection.setRequestProperty("User-Agent", USER_AGENT);
                        int responseCode = httpURLConnection.getResponseCode();

                        // always check HTTP response code first
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            String fileName = "";
                            String disposition = httpURLConnection.getHeaderField("Content-Disposition");

                            if (disposition != null) {
                                // extracts file name from header field
                                int index = disposition.indexOf("filename=");
                                if (index > 0) {
                                    fileName = disposition.substring(index + 10,
                                            disposition.length() - 1);
                                }
                            } else {
                                // extracts file name from URL
                                fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1
                                );
                            }
                            fileName = FileNameCleaner.cleanFileName(fileName);
                            // opens input stream from the HTTP connection
                            InputStream inputStream = httpURLConnection.getInputStream();
                            File saveDir = context.getCacheDir();
                            final String saveFilePath = saveDir + File.separator + fileName;

                            // opens an output stream to save into file
                            FileOutputStream outputStream = new FileOutputStream(saveFilePath);

                            int bytesRead;
                            byte[] buffer = new byte[CHUNK_SIZE];
                            int contentSize = httpURLConnection.getContentLength();
                            int downloadedFileSize = 0;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                                downloadedFileSize += bytesRead;
                                if (context instanceof SlideMediaActivity) {
                                    final int currentProgress = (downloadedFileSize * 100) / contentSize;
                                    ((SlideMediaActivity) context).runOnUiThread(new Runnable() {
                                        public void run() {
                                            listener.onUpdateProgress(currentProgress > 0 ? currentProgress : 101);
                                        }
                                    });
                                }
                            }
                            outputStream.close();
                            inputStream.close();
                            if (context instanceof TootActivity)
                                ((TootActivity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        listener.onDownloaded(saveFilePath, downloadUrl, null);
                                    }
                                });
                            if (context instanceof SlideMediaActivity)
                                ((SlideMediaActivity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        listener.onDownloaded(saveFilePath, downloadUrl, null);
                                    }
                                });
                        } else {
                            final Error error = new Error();
                            error.setError(String.valueOf(responseCode));
                            if (context instanceof TootActivity)
                                ((TootActivity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        listener.onDownloaded(null, downloadUrl, error);
                                    }
                                });
                            if (context instanceof SlideMediaActivity)
                                ((SlideMediaActivity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        listener.onDownloaded(null, downloadUrl, error);
                                    }
                                });

                        }
                    } catch (IOException e) {
                        Error error = new Error();
                        error.setError(context.getString(R.string.toast_error));
                    }

                }
            }
        }).start();
    }


    public InputStream getPicture(final String downloadUrl) {
        if (downloadUrl.startsWith("https://")) {
            try {
                URL url = new URL(downloadUrl);
                if (proxy != null)
                    httpsURLConnection = (HttpsURLConnection) url.openConnection(proxy);
                else
                    httpsURLConnection = (HttpsURLConnection) url.openConnection();
                httpsURLConnection.setSSLSocketFactory(new TLSSocketFactory(this.instance));
                httpsURLConnection.setRequestProperty("User-Agent", USER_AGENT);
                int responseCode = httpsURLConnection.getResponseCode();
                // always check HTTP response code first
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // opens input stream from the HTTP connection
                    return httpsURLConnection.getInputStream();
                }
                httpsURLConnection.getInputStream().close();
            } catch (IOException | NoSuchAlgorithmException | KeyManagementException ignored) {
            }
            if (httpsURLConnection != null)
                try {
                    httpsURLConnection.getInputStream().close();
                } catch (Exception ignored) {
                }
            return null;
        } else {
            try {
                URL url = new URL(downloadUrl);
                if (proxy != null)
                    httpURLConnection = (HttpURLConnection) url.openConnection(proxy);
                else
                    httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestProperty("User-Agent", USER_AGENT);
                int responseCode = httpURLConnection.getResponseCode();
                // always check HTTP response code first
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // opens input stream from the HTTP connection
                    return httpURLConnection.getInputStream();
                }
                httpURLConnection.getInputStream().close();
            } catch (IOException ignored) {
            }
            if (httpURLConnection != null)
                try {
                    httpURLConnection.getInputStream().close();
                } catch (Exception ignored) {
                }
            return null;
        }
    }

    private void uploadMedia(String urlConnection, InputStream avatar, InputStream header, String filename) {
        UploadNotificationConfig uploadConfig = new UploadNotificationConfig();
        uploadConfig.getCompleted().autoClear = true;
        File file = new File(context.getCacheDir() + "/" + filename);
        OutputStream outputStream;
        try {
            outputStream = new FileOutputStream(file);
            if (avatar != null) {
                IOUtils.copy(avatar, outputStream);
            } else {
                IOUtils.copy(header, outputStream);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            String token = sharedpreferences.getString(Helper.PREF_KEY_OAUTH_TOKEN, null);
            MultipartUploadRequest m = new MultipartUploadRequest(context, urlConnection)
                    .setMethod("PATCH");
            if (avatar != null) {
                m.addFileToUpload(file.getPath(), "avatar");
            } else {
                m.addFileToUpload(file.getPath(), "header");
            }
            m.addParameter("name", filename)
                    .addHeader("Authorization", "Bearer " + token)
                    .setNotificationConfig(uploadConfig)
                    .setDelegate(new UploadStatusDelegate() {
                        @Override
                        public void onProgress(Context context, UploadInfo uploadInfo) {
                            // your code here
                        }

                        @Override
                        public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse,
                                            Exception exception) {
                            // your code here
                            file.delete();
                        }

                        @Override
                        public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
                            file.delete();
                        }

                        @Override
                        public void onCancelled(Context context, UploadInfo uploadInfo) {
                            file.delete();
                        }
                    })
                    .startUpload();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("SameParameterValue")
    public String patch(String urlConnection, int timeout, HashMap<String, String> paramaters, InputStream avatar, String avatarName, InputStream header, String headerName, String token) throws IOException, NoSuchAlgorithmException, KeyManagementException, HttpsConnectionException {
        if (urlConnection.startsWith("https://")) {
            URL url = new URL(urlConnection);
            Map<String, Object> params = new LinkedHashMap<>();
            if (paramaters != null) {
                Iterator it = paramaters.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    params.put(pair.getKey().toString(), pair.getValue());
                    it.remove();
                }
            }
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, Object> param : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(param.getKey());
                postData.append('=');
                postData.append(param.getValue());
            }
            byte[] postDataBytes = (postData.toString()).getBytes(StandardCharsets.UTF_8);


            if (proxy != null)
                httpsURLConnection = (HttpsURLConnection) url.openConnection(proxy);
            else
                httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.setRequestProperty("User-Agent", USER_AGENT);
            httpsURLConnection.setConnectTimeout(timeout * 1000);
            httpsURLConnection.setSSLSocketFactory(new TLSSocketFactory(this.instance));
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                httpsURLConnection.setRequestMethod("PATCH");
            } else {
                httpsURLConnection.setRequestProperty("X-HTTP-Method-Override", "PATCH");
                httpsURLConnection.setRequestMethod("POST");
            }
            if (token != null && !token.startsWith("Basic "))
                httpsURLConnection.setRequestProperty("Authorization", "Bearer " + token);
            else if (token != null && token.startsWith("Basic "))
                httpsURLConnection.setRequestProperty("Authorization", token);
            httpsURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpsURLConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            httpsURLConnection.setDoOutput(true);

            String response;
            OutputStream outputStream = httpsURLConnection.getOutputStream();
            outputStream.write(postDataBytes);
            if (avatar != null) {
                uploadMedia(urlConnection, avatar, null, avatarName);
            }
            if (header != null) {
                uploadMedia(urlConnection, null, header, headerName);
            }
            if (httpsURLConnection.getResponseCode() >= 200 && httpsURLConnection.getResponseCode() < 400) {
                response = converToString(httpsURLConnection.getInputStream());
            } else {
                String error = null;
                if (httpsURLConnection.getErrorStream() != null) {
                    InputStream stream = httpsURLConnection.getErrorStream();
                    if (stream == null) {
                        stream = httpsURLConnection.getInputStream();
                    }
                    try (Scanner scanner = new Scanner(stream)) {
                        scanner.useDelimiter("\\Z");
                        if (scanner.hasNext()) {
                            error = scanner.next();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                int responseCode = httpsURLConnection.getResponseCode();
                try {
                    httpsURLConnection.getInputStream().close();
                } catch (Exception ignored) {
                }
                try {
                    httpsURLConnection.getInputStream().close();
                } catch (Exception ignored) {
                }
                throw new HttpsConnectionException(responseCode, error);
            }
            httpsURLConnection.getInputStream().close();
            return response;
        } else {
            URL url = new URL(urlConnection);
            Map<String, Object> params = new LinkedHashMap<>();
            if (paramaters != null) {
                Iterator it = paramaters.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    params.put(pair.getKey().toString(), pair.getValue());
                    it.remove();
                }
            }
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, Object> param : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(param.getKey());
                postData.append('=');
                postData.append(param.getValue());
            }
            byte[] postDataBytes = (postData.toString()).getBytes(StandardCharsets.UTF_8);

            if (proxy != null)
                httpURLConnection = (HttpsURLConnection) url.openConnection(proxy);
            else
                httpURLConnection = (HttpsURLConnection) url.openConnection();
            httpURLConnection.setRequestProperty("User-Agent", USER_AGENT);
            httpURLConnection.setConnectTimeout(timeout * 1000);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                httpURLConnection.setRequestMethod("PATCH");
            } else {
                httpURLConnection.setRequestProperty("X-HTTP-Method-Override", "PATCH");
                httpURLConnection.setRequestMethod("POST");
            }
            if (token != null && !token.startsWith("Basic "))
                httpURLConnection.setRequestProperty("Authorization", "Bearer " + token);
            else if (token != null && token.startsWith("Basic "))
                httpURLConnection.setRequestProperty("Authorization", token);
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            httpURLConnection.setDoOutput(true);


            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(postDataBytes);
            if (avatar != null) {
                uploadMedia(urlConnection, avatar, null, avatarName);
            }
            if (header != null) {
                uploadMedia(urlConnection, null, header, headerName);
            }
            String response;
            if (httpURLConnection.getResponseCode() >= 200 && httpURLConnection.getResponseCode() < 400) {
                response = converToString(httpsURLConnection.getInputStream());
            } else {
                String error = null;
                if (httpURLConnection.getErrorStream() != null) {
                    InputStream stream = httpURLConnection.getErrorStream();
                    if (stream == null) {
                        stream = httpURLConnection.getInputStream();
                    }
                    try (Scanner scanner = new Scanner(stream)) {
                        scanner.useDelimiter("\\Z");
                        if (scanner.hasNext()) {
                            error = scanner.next();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                int responseCode = httpURLConnection.getResponseCode();
                try {
                    httpURLConnection.getInputStream().close();
                } catch (Exception ignored) {
                }

                throw new HttpsConnectionException(responseCode, error);
            }
            httpURLConnection.getInputStream().close();
            return response;
        }

    }

    public String put(String urlConnection, int timeout, HashMap<String, String> paramaters, String token) throws IOException, NoSuchAlgorithmException, KeyManagementException, HttpsConnectionException {
        if (urlConnection.startsWith("https://")) {
            URL url = new URL(urlConnection);
            Map<String, Object> params = new LinkedHashMap<>();
            if (paramaters != null) {
                Iterator it = paramaters.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    params.put(pair.getKey().toString(), pair.getValue());
                    it.remove();
                }
            }
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, Object> param : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(param.getKey());
                postData.append('=');
                postData.append(param.getValue());
            }
            byte[] postDataBytes = postData.toString().getBytes(StandardCharsets.UTF_8);

            if (proxy != null)
                httpsURLConnection = (HttpsURLConnection) url.openConnection(proxy);
            else
                httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.setRequestProperty("User-Agent", USER_AGENT);
            httpsURLConnection.setConnectTimeout(timeout * 1000);
            httpsURLConnection.setSSLSocketFactory(new TLSSocketFactory(this.instance));
            if (token != null && !token.startsWith("Basic "))
                httpsURLConnection.setRequestProperty("Authorization", "Bearer " + token);
            else if (token != null && token.startsWith("Basic "))
                httpsURLConnection.setRequestProperty("Authorization", token);
            httpsURLConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));

            httpsURLConnection.setRequestMethod("PUT");
            httpsURLConnection.setDoInput(true);
            httpsURLConnection.setDoOutput(true);

            httpsURLConnection.getOutputStream().write(postDataBytes);
            String response;
            if (httpsURLConnection.getResponseCode() >= 200 && httpsURLConnection.getResponseCode() < 400) {
                getSinceMaxId();
                response = converToString(httpsURLConnection.getInputStream());
            } else {
                String error = null;
                if (httpsURLConnection.getErrorStream() != null) {
                    InputStream stream = httpsURLConnection.getErrorStream();
                    if (stream == null) {
                        stream = httpsURLConnection.getInputStream();
                    }
                    try (Scanner scanner = new Scanner(stream)) {
                        scanner.useDelimiter("\\Z");
                        if (scanner.hasNext()) {
                            error = scanner.next();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                int responseCode = httpsURLConnection.getResponseCode();
                try {
                    httpsURLConnection.getInputStream().close();
                } catch (Exception ignored) {
                }
                throw new HttpsConnectionException(responseCode, error);
            }
            getSinceMaxId();
            httpsURLConnection.getInputStream().close();
            return response;
        } else {
            URL url = new URL(urlConnection);
            Map<String, Object> params = new LinkedHashMap<>();
            if (paramaters != null) {
                Iterator it = paramaters.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    params.put(pair.getKey().toString(), pair.getValue());
                    it.remove();
                }
            }
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, Object> param : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(param.getKey());
                postData.append('=');
                postData.append(param.getValue());
            }
            byte[] postDataBytes = postData.toString().getBytes(StandardCharsets.UTF_8);

            if (proxy != null)
                httpURLConnection = (HttpURLConnection) url.openConnection(proxy);
            else
                httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestProperty("User-Agent", USER_AGENT);
            httpURLConnection.setConnectTimeout(timeout * 1000);
            if (token != null && !token.startsWith("Basic "))
                httpURLConnection.setRequestProperty("Authorization", "Bearer " + token);
            else if (token != null && token.startsWith("Basic "))
                httpURLConnection.setRequestProperty("Authorization", token);
            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));

            httpURLConnection.setRequestMethod("PUT");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);

            httpURLConnection.getOutputStream().write(postDataBytes);
            String response;
            if (httpURLConnection.getResponseCode() >= 200 && httpURLConnection.getResponseCode() < 400) {
                getSinceMaxId();
                response = converToString(httpURLConnection.getInputStream());
            } else {
                String error = null;
                if (httpURLConnection.getErrorStream() != null) {
                    InputStream stream = httpURLConnection.getErrorStream();
                    if (stream == null) {
                        stream = httpURLConnection.getInputStream();
                    }
                    try (Scanner scanner = new Scanner(stream)) {
                        scanner.useDelimiter("\\Z");
                        if (scanner.hasNext()) {
                            error = scanner.next();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                int responseCode = httpURLConnection.getResponseCode();
                try {
                    httpURLConnection.getInputStream().close();
                } catch (Exception ignored) {
                }
                throw new HttpsConnectionException(responseCode, error);
            }
            getSinceMaxId();
            httpURLConnection.getInputStream().close();
            return response;
        }

    }

    public int delete(String urlConnection, int timeout, HashMap<String, String> paramaters, String token) throws IOException, NoSuchAlgorithmException, KeyManagementException, HttpsConnectionException {
        if (urlConnection.startsWith("https://")) {
            URL url = new URL(urlConnection);
            Map<String, Object> params = new LinkedHashMap<>();
            if (paramaters != null) {
                Iterator it = paramaters.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    params.put(pair.getKey().toString(), pair.getValue());
                    it.remove();
                }
            }
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, Object> param : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(param.getKey());
                postData.append('=');
                postData.append(param.getValue());
            }
            byte[] postDataBytes = postData.toString().getBytes(StandardCharsets.UTF_8);

            if (proxy != null)
                httpsURLConnection = (HttpsURLConnection) url.openConnection(proxy);
            else
                httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.setRequestProperty("User-Agent", USER_AGENT);
            httpsURLConnection.setSSLSocketFactory(new TLSSocketFactory(this.instance));
            if (token != null && !token.startsWith("Basic "))
                httpsURLConnection.setRequestProperty("Authorization", "Bearer " + token);
            else if (token != null && token.startsWith("Basic "))
                httpsURLConnection.setRequestProperty("Authorization", token);
            httpsURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpsURLConnection.setRequestMethod("DELETE");
            httpsURLConnection.setConnectTimeout(timeout * 1000);
            httpsURLConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));

            httpsURLConnection.getOutputStream().write(postDataBytes);
            if (httpsURLConnection.getResponseCode() >= 200 && httpsURLConnection.getResponseCode() < 400) {
                getSinceMaxId();
                httpsURLConnection.getInputStream().close();
                return httpsURLConnection.getResponseCode();
            } else {
                String error = null;
                if (httpsURLConnection.getErrorStream() != null) {
                    InputStream stream = httpsURLConnection.getErrorStream();
                    if (stream == null) {
                        stream = httpsURLConnection.getInputStream();
                    }
                    try (Scanner scanner = new Scanner(stream)) {
                        scanner.useDelimiter("\\Z");
                        if (scanner.hasNext()) {
                            error = scanner.next();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                int responseCode = httpsURLConnection.getResponseCode();
                try {
                    httpsURLConnection.getInputStream().close();
                } catch (Exception ignored) {
                }
                throw new HttpsConnectionException(responseCode, error);
            }
        } else {
            URL url = new URL(urlConnection);
            Map<String, Object> params = new LinkedHashMap<>();
            if (paramaters != null) {
                Iterator it = paramaters.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    params.put(pair.getKey().toString(), pair.getValue());
                    it.remove();
                }
            }
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, Object> param : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(param.getKey());
                postData.append('=');
                postData.append(param.getValue());
            }
            byte[] postDataBytes = postData.toString().getBytes(StandardCharsets.UTF_8);

            if (proxy != null)
                httpURLConnection = (HttpURLConnection) url.openConnection(proxy);
            else
                httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestProperty("User-Agent", USER_AGENT);
            if (token != null && !token.startsWith("Basic "))
                httpsURLConnection.setRequestProperty("Authorization", "Bearer " + token);
            else if (token != null && token.startsWith("Basic "))
                httpsURLConnection.setRequestProperty("Authorization", token);
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpURLConnection.setRequestMethod("DELETE");
            httpURLConnection.setConnectTimeout(timeout * 1000);
            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));

            httpURLConnection.getOutputStream().write(postDataBytes);


            if (httpURLConnection.getResponseCode() >= 200 && httpURLConnection.getResponseCode() < 400) {
                getSinceMaxId();
                httpURLConnection.getInputStream().close();
                return httpURLConnection.getResponseCode();
            } else {
                String error = null;
                if (httpURLConnection.getErrorStream() != null) {
                    InputStream stream = httpURLConnection.getErrorStream();
                    if (stream == null) {
                        stream = httpURLConnection.getInputStream();
                    }
                    try (Scanner scanner = new Scanner(stream)) {
                        scanner.useDelimiter("\\Z");
                        if (scanner.hasNext()) {
                            error = scanner.next();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                int responseCode = httpURLConnection.getResponseCode();
                try {
                    httpURLConnection.getInputStream().close();
                } catch (Exception ignored) {
                }
                throw new HttpsConnectionException(responseCode, error);
            }
        }
    }

    public String getSince_id() {
        return since_id;
    }

    public String getMax_id() {
        return max_id;
    }

    private void getOKHttpHeader(Map<String, List<String>> headers){
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (entry.toString().startsWith("Link") || entry.toString().startsWith("link")) {
                Pattern patternMaxId = Pattern.compile("max_id=([0-9a-zA-Z]{1,}).*");
                Matcher matcherMaxId = patternMaxId.matcher(entry.toString());
                if (matcherMaxId.find()) {
                    max_id = matcherMaxId.group(1);
                }
                if (entry.toString().startsWith("Link")) {
                    Pattern patternSinceId = Pattern.compile("since_id=([0-9a-zA-Z]{1,}).*");
                    Matcher matcherSinceId = patternSinceId.matcher(entry.toString());
                    if (matcherSinceId.find()) {
                        since_id = matcherSinceId.group(1);
                    }

                }
            }else  if (entry.toString().startsWith("Min-Id") || entry.toString().startsWith("min-id")) {
                Pattern patternMaxId = Pattern.compile("min-id=\\[([0-9a-zA-Z]{1,}).*\\]");
                Matcher matcherMaxId = patternMaxId.matcher(entry.toString());
                if (matcherMaxId.find()) {
                    max_id = matcherMaxId.group(1);
                }
            }
        }
    }

    private void getSinceMaxId() {
        if (Helper.getLiveInstanceWithProtocol(context) == null)
            return;
        if (Helper.getLiveInstanceWithProtocol(context).startsWith("https://")) {
            if (httpsURLConnection == null)
                return;
            Map<String, List<String>> map = httpsURLConnection.getHeaderFields();

            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                if (entry.toString().startsWith("Link") || entry.toString().startsWith("link")) {
                    Pattern patternMaxId = Pattern.compile("max_id=([0-9a-zA-Z]{1,}).*");
                    Matcher matcherMaxId = patternMaxId.matcher(entry.toString());
                    if (matcherMaxId.find()) {
                        max_id = matcherMaxId.group(1);
                    }
                    if (entry.toString().startsWith("Link")) {
                        Pattern patternSinceId = Pattern.compile("since_id=([0-9a-zA-Z]{1,}).*");
                        Matcher matcherSinceId = patternSinceId.matcher(entry.toString());
                        if (matcherSinceId.find()) {
                            since_id = matcherSinceId.group(1);
                        }

                    }
                }else  if (entry.toString().startsWith("Min-Id") || entry.toString().startsWith("min-id")) {
                    Pattern patternMaxId = Pattern.compile("min-id=\\[([0-9a-zA-Z]{1,}).*\\]");
                    Matcher matcherMaxId = patternMaxId.matcher(entry.toString());
                    if (matcherMaxId.find()) {
                        max_id = matcherMaxId.group(1);
                    }
                }
            }
        } else {
            if (httpURLConnection == null)
                return;
            Map<String, List<String>> map = httpURLConnection.getHeaderFields();
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                if (entry.toString().startsWith("Link") || entry.toString().startsWith("link")) {
                    Pattern patternMaxId = Pattern.compile("max_id=([0-9a-zA-Z]{1,}).*");
                    Matcher matcherMaxId = patternMaxId.matcher(entry.toString());
                    if (matcherMaxId.find()) {
                        max_id = matcherMaxId.group(1);
                    }
                    if (entry.toString().startsWith("Link")) {
                        Pattern patternSinceId = Pattern.compile("since_id=([0-9a-zA-Z]{1,}).*");
                        Matcher matcherSinceId = patternSinceId.matcher(entry.toString());
                        if (matcherSinceId.find()) {
                            since_id = matcherSinceId.group(1);
                        }

                    }
                }
            }
        }
    }

    private String converToString(InputStream inputStream) throws IOException {
        java.util.Scanner s = new java.util.Scanner(inputStream).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    int getActionCode() {
        if (Helper.getLiveInstanceWithProtocol(context).startsWith("https://")) {
            try {
                return httpsURLConnection.getResponseCode();
            } catch (IOException e) {
                return -1;
            }
        } else {
            try {
                return httpURLConnection.getResponseCode();
            } catch (IOException e) {
                return -1;
            }
        }
    }


    enum imageType {
        AVATAR,
        BANNER
    }

    public class HttpsConnectionException extends Exception {

        private int statusCode;
        private String message;

        HttpsConnectionException(int statusCode, String message) {
            this.statusCode = statusCode;
            SpannableString spannableString;
            if (message != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    spannableString = new SpannableString(Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY));
                else
                    //noinspection deprecation
                    spannableString = new SpannableString(Html.fromHtml(message));
            } else {
                spannableString = new SpannableString(context.getString(R.string.toast_error));
            }
            this.message = spannableString.toString();
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
