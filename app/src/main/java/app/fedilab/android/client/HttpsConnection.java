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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.Html;
import android.text.SpannableString;
import android.util.Log;

import com.google.common.io.ByteStreams;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import app.fedilab.android.R;
import app.fedilab.android.activities.MediaActivity;
import app.fedilab.android.activities.TootActivity;
import app.fedilab.android.client.Entities.Error;
import app.fedilab.android.helper.FileNameCleaner;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.interfaces.OnDownloadInterface;
import app.fedilab.android.interfaces.OnHttpResponseInterface;
import info.guardianproject.netcipher.NetCipher;
import info.guardianproject.netcipher.client.StrongConnectionBuilder;
import info.guardianproject.netcipher.proxy.OrbotHelper;


/**
 * Created by Thomas on 17/11/2017.
 * Manage http queries
 */

public class HttpsConnection implements OnHttpResponseInterface {


    
    private HttpURLConnection httpURLConnection;
    private String since_id, max_id;
    private Context context;
    private int CHUNK_SIZE = 4096;
    private SharedPreferences sharedpreferences;
    private Proxy proxy;
    private String instance;
    public static volatile boolean orbotConnected = false;

    public HttpsConnection(Context context, String instance){
        this.instance  = instance;
        this.context = context;
    }

    private HttpURLConnection initialize(URL url){

        sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        boolean proxyEnabled = sharedpreferences.getBoolean(Helper.SET_PROXY_ENABLED, false);
        int type = sharedpreferences.getInt(Helper.SET_PROXY_TYPE, 0);
        proxy = null;
        String host = null;
        int port = 8118;
        if( proxyEnabled ){
            try {
                host = sharedpreferences.getString(Helper.SET_PROXY_HOST, "127.0.0.1");
                port = sharedpreferences.getInt(Helper.SET_PROXY_PORT, 8118);
                if( type == 0 )
                    proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
                else
                    proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(host, port));
                final String login = sharedpreferences.getString(Helper.SET_PROXY_LOGIN, null);
                final String pwd = sharedpreferences.getString(Helper.SET_PROXY_PASSWORD, null);
                if( login != null) {
                    Authenticator authenticator = new Authenticator() {
                        public PasswordAuthentication getPasswordAuthentication() {
                            assert pwd != null;
                            return (new PasswordAuthentication(login,
                                    pwd.toCharArray()));
                        }
                    };
                    Authenticator.setDefault(authenticator);
                }
            }catch (Exception e){
                proxy = null;
            }
        }
        try {
            httpURLConnection = NetCipher.getHttpURLConnection(url);
            if( proxyEnabled){
                SocketAddress sa = new InetSocketAddress(host, port);
                if( type == 0 ) {
                    NetCipher.setProxy(new Proxy(Proxy.Type.HTTP, sa));
                }else{
                    NetCipher.setProxy(new Proxy(Proxy.Type.SOCKS, sa));
                }
            }
            if( url.toString().startsWith("https")) {
                ((HttpsURLConnection) httpURLConnection).setSSLSocketFactory(new TLSSocketFactory());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.v(Helper.TAG,"httpURLConnection: " + httpURLConnection);
        return httpURLConnection;
    }


    @SuppressWarnings("ConstantConditions")
    public String get(String urlConnection, int timeout, HashMap<String, String> paramaters, String token) throws IOException, HttpsConnectionException {

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
            postData.append(String.valueOf(param.getValue()));
        }
        URL url = new URL(urlConnection + "?" + postData);
        httpURLConnection = initialize(url);
        httpURLConnection.setConnectTimeout(timeout * 1000);
        httpURLConnection.setRequestProperty("http.keepAlive", "false");
        httpURLConnection.setRequestProperty("User-Agent", Helper.USER_AGENT);
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        httpURLConnection.setRequestProperty("Accept", "application/json");
        // httpURLConnection.setSSLSocketFactory(new TLSSocketFactory(this.instance));
        if (token != null && !token.startsWith("Basic "))
            httpURLConnection.setRequestProperty("Authorization", "Bearer " + token);
        else if( token != null && token.startsWith("Basic "))
            httpURLConnection.setRequestProperty("Authorization", token);
        httpURLConnection.setRequestMethod("GET");
        String response;
        if (httpURLConnection.getResponseCode() >= 200 && httpURLConnection.getResponseCode() < 400) {
            response = converToString(httpURLConnection.getInputStream());
        } else {
            String error = null;
            if( httpURLConnection.getErrorStream() != null) {
                InputStream stream = httpURLConnection.getErrorStream();
                if (stream == null) {
                    stream = httpURLConnection.getInputStream();
                }
                try (Scanner scanner = new Scanner(stream)) {
                    scanner.useDelimiter("\\Z");
                    error = scanner.next();
                }catch (Exception e){e.printStackTrace();}
            }
            int responseCode = httpURLConnection.getResponseCode();
            throw new HttpsConnectionException(responseCode, error);
        }
        getSinceMaxId();
        httpURLConnection.getInputStream().close();
        return response;
    }



    public String get(String urlConnection) throws IOException, HttpsConnectionException {

            URL url = new URL(urlConnection);
            initialize(url);
            httpURLConnection.setConnectTimeout(30 * 1000);
            httpURLConnection.setRequestProperty("http.keepAlive", "false");
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            httpURLConnection.setRequestProperty("Accept", "application/json");
            httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1667.0 Safari/537.36");
            //httpURLConnection.setSSLSocketFactory(new TLSSocketFactory(this.instance));
            httpURLConnection.setRequestMethod("GET");
            String response;
            if (httpURLConnection.getResponseCode() >= 200 && httpURLConnection.getResponseCode() < 400) {
                getSinceMaxId();
                response = converToString(httpURLConnection.getInputStream());
            }else {
                String error = null;
                if( httpURLConnection.getErrorStream() != null) {
                    InputStream stream = httpURLConnection.getErrorStream();
                    if (stream == null) {
                        stream = httpURLConnection.getInputStream();
                    }
                    try (Scanner scanner = new Scanner(stream)) {
                        scanner.useDelimiter("\\Z");
                        error = scanner.next();
                    }catch (Exception e){e.printStackTrace();}
                }
                int responseCode = httpURLConnection.getResponseCode();
                throw new HttpsConnectionException(responseCode, error);
            }
            getSinceMaxId();
            httpURLConnection.getInputStream().close();
            return response;

    }



    public String post(String urlConnection, int timeout, HashMap<String, String> paramaters, String token) throws IOException, HttpsConnectionException {
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
            postData.append(String.valueOf(param.getValue()));
        }
        byte[] postDataBytes = postData.toString().getBytes("UTF-8");
        initialize(url);
        httpURLConnection.setRequestProperty("User-Agent", Helper.USER_AGENT);
        httpURLConnection.setConnectTimeout(timeout * 1000);
        httpURLConnection.setDoOutput(true);
        //httpURLConnection.setSSLSocketFactory(new TLSSocketFactory(this.instance));
        httpURLConnection.setRequestMethod("POST");
        if (token != null && !token.startsWith("Basic "))
            httpURLConnection.setRequestProperty("Authorization", "Bearer " + token);
        else if( token != null && token.startsWith("Basic "))
            httpURLConnection.setRequestProperty("Authorization", token);
        httpURLConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));


        httpURLConnection.getOutputStream().write(postDataBytes);
        String response;
        if (httpURLConnection.getResponseCode() >= 200 && httpURLConnection.getResponseCode() < 400) {
            getSinceMaxId();
            response = converToString(httpURLConnection.getInputStream());
        } else {
            String error = null;
            if( httpURLConnection.getErrorStream() != null) {
                InputStream stream = httpURLConnection.getErrorStream();
                if (stream == null) {
                    stream = httpURLConnection.getInputStream();
                }
                try (Scanner scanner = new Scanner(stream)) {
                    scanner.useDelimiter("\\Z");
                    error = scanner.next();
                }catch (Exception e){e.printStackTrace();}
            }
            int responseCode = httpURLConnection.getResponseCode();
            throw new HttpsConnectionException(responseCode, error);
        }
        getSinceMaxId();
        httpURLConnection.getInputStream().close();
        return response;
    }


    String postJson(String urlConnection, int timeout, JsonObject jsonObject, String token) throws IOException,  HttpsConnectionException {
        URL url = new URL(urlConnection);
        byte[] postDataBytes;
        postDataBytes = jsonObject.toString().getBytes("UTF-8");
        initialize(url);
        httpURLConnection.setRequestProperty("User-Agent", Helper.USER_AGENT);
        httpURLConnection.setConnectTimeout(timeout * 1000);
        httpURLConnection.setDoOutput(true);
        // httpURLConnection.setSSLSocketFactory(new TLSSocketFactory(this.instance));
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        httpURLConnection.setRequestProperty("Accept", "application/json");
        httpURLConnection.setRequestMethod("POST");
        if (token != null && !token.startsWith("Basic "))
            httpURLConnection.setRequestProperty("Authorization", "Bearer " + token);
        else if( token != null && token.startsWith("Basic "))
            httpURLConnection.setRequestProperty("Authorization", token);
        httpURLConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));


        httpURLConnection.getOutputStream().write(postDataBytes);
        String response;
        if (httpURLConnection.getResponseCode() >= 200 && httpURLConnection.getResponseCode() < 400) {
            getSinceMaxId();
            response = converToString(httpURLConnection.getInputStream());
        } else {
            String error = null;
            if( httpURLConnection.getErrorStream() != null) {
                InputStream stream = httpURLConnection.getErrorStream();
                if (stream == null) {
                    stream = httpURLConnection.getInputStream();
                }
                try (Scanner scanner = new Scanner(stream)) {
                    scanner.useDelimiter("\\Z");
                    error = scanner.next();
                }catch (Exception e){e.printStackTrace();}
            }
            int responseCode = httpURLConnection.getResponseCode();
            throw new HttpsConnectionException(responseCode, error);
        }
        getSinceMaxId();
        httpURLConnection.getInputStream().close();
        return response;

    }

    String postMisskey(String urlConnection, int timeout, JSONObject paramaters, String token) throws IOException, HttpsConnectionException {
        URL url = new URL(urlConnection);
        byte[] postDataBytes = paramaters.toString().getBytes("UTF-8");

        initialize(url);
        httpURLConnection.setRequestProperty("User-Agent", Helper.USER_AGENT);
        httpURLConnection.setConnectTimeout(timeout * 1000);
        httpURLConnection.setDoOutput(true);
        //httpURLConnection.setSSLSocketFactory(new TLSSocketFactory(this.instance));
        httpURLConnection.setRequestMethod("POST");
        if (token != null && !token.startsWith("Basic "))
            httpURLConnection.setRequestProperty("Authorization", "Bearer " + token);
        else if( token != null && token.startsWith("Basic "))
            httpURLConnection.setRequestProperty("Authorization", token);
        httpURLConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));


        httpURLConnection.getOutputStream().write(postDataBytes);
        String response;
        if (httpURLConnection.getResponseCode() >= 200 && httpURLConnection.getResponseCode() < 400) {
            getSinceMaxId();
            response = converToString(httpURLConnection.getInputStream());
        } else {
            String error = null;
            if( httpURLConnection.getErrorStream() != null) {
                InputStream stream = httpURLConnection.getErrorStream();
                if (stream == null) {
                    stream = httpURLConnection.getInputStream();
                }
                try (Scanner scanner = new Scanner(stream)) {
                    scanner.useDelimiter("\\Z");
                    error = scanner.next();
                }catch (Exception e){e.printStackTrace();}
            }
            int responseCode = httpURLConnection.getResponseCode();
            throw new HttpsConnectionException(responseCode, error);
        }
        getSinceMaxId();
        httpURLConnection.getInputStream().close();
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
                HttpURLConnection httpURLConnection = null;
                try {
                    url = new URL(downloadUrl);
                    httpURLConnection = initialize(url);
                    httpURLConnection.setRequestProperty("User-Agent", Helper.USER_AGENT);
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
                            fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1,
                                    downloadUrl.length());
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
                            if (context instanceof MediaActivity) {
                                final int currentProgress = (downloadedFileSize * 100) / contentSize;
                                ((MediaActivity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        listener.onUpdateProgress(currentProgress>0?currentProgress:101);
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
                        if (context instanceof MediaActivity)
                            ((MediaActivity) context).runOnUiThread(new Runnable() {
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
                        if (context instanceof MediaActivity)
                            ((MediaActivity) context).runOnUiThread(new Runnable() {
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
        }).start();
    }



    public InputStream getPicture(final String downloadUrl) {
        try {
            URL url = new URL(downloadUrl);
            initialize(url);
           // httpURLConnection.setSSLSocketFactory(new TLSSocketFactory(this.instance));
            httpURLConnection.setRequestProperty("User-Agent", Helper.USER_AGENT);
            int responseCode = httpURLConnection.getResponseCode();
            // always check HTTP response code first
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // opens input stream from the HTTP connection
                return httpURLConnection.getInputStream();
            }
            httpURLConnection.getInputStream().close();
        } catch (IOException  ignored) {
        }
        if (httpURLConnection != null)
            try {
                httpURLConnection.getInputStream().close();
            } catch (Exception ignored) { }
        return null;
    }

    @Override
    public void onResponse(HttpURLConnection httpURLConnection) {

    }

    enum imageType{
        AVATAR,
        BANNER
    }

    @SuppressWarnings("SameParameterValue")
    private void patchImage(String urlConnection, int timeout, imageType it, InputStream image, String fileName, String token) throws IOException, HttpsConnectionException {
        fileName = FileNameCleaner.cleanFileName(fileName);
        String twoHyphens = "--";
        String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";
        String lineEnd = "\r\n";
        HttpURLConnection httpURLConnection;
        URL url = new URL(urlConnection);
        int lengthSentImage = 0;
        byte[] pixelsImage = new byte[0];
        if( image != null) {
            ByteArrayOutputStream ous = null;
            try {
                try {
                    byte[] buffer = new byte[CHUNK_SIZE];
                    ous = new ByteArrayOutputStream();
                    int read;
                    while ((read = image.read(buffer)) != -1) {
                        ous.write(buffer, 0, read);
                    }
                    ous.flush();
                } finally {
                    if (ous != null)
                        ous.close();
                }
            } catch (FileNotFoundException ignored) {
            } catch (IOException ignored) {
            }
            pixelsImage = ous.toByteArray();

            lengthSentImage = pixelsImage.length;
            lengthSentImage += 2 * (twoHyphens + boundary + twoHyphens + lineEnd).getBytes().length;
            if( it == imageType.AVATAR)
                lengthSentImage += ("Content-Disposition: form-data; name=\"avatar\";filename=\""+fileName+"\"" + lineEnd).getBytes().length;
            else
                lengthSentImage += ("Content-Disposition: form-data; name=\"header\";filename=\""+fileName+"\"" + lineEnd).getBytes().length;
            lengthSentImage += 2 * (lineEnd).getBytes().length;
        }



        int lengthSent = lengthSentImage + (twoHyphens + boundary + twoHyphens + lineEnd).getBytes().length;
        httpURLConnection = initialize(url);
        httpURLConnection.setRequestProperty("User-Agent", Helper.USER_AGENT);
        httpURLConnection.setConnectTimeout(timeout * 1000);
        //httpURLConnection.setSSLSocketFactory(new TLSSocketFactory(this.instance));
        httpURLConnection.setDoInput(true);
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setUseCaches(false);
        if( Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT ){
            httpURLConnection.setRequestMethod("PATCH");
        }else {
            httpURLConnection.setRequestProperty("X-HTTP-Method-Override", "PATCH");
            httpURLConnection.setRequestMethod("POST");
        }
        httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
        httpURLConnection.setRequestProperty("Cache-Control", "no-cache");
        httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+ boundary);
        if (token != null && !token.startsWith("Basic "))
            httpURLConnection.setRequestProperty("Authorization", "Bearer " + token);
        else if( token != null && token.startsWith("Basic "))
            httpURLConnection.setRequestProperty("Authorization", token);
        httpURLConnection.setFixedLengthStreamingMode(lengthSent);

        OutputStream outputStream = httpURLConnection.getOutputStream();
        outputStream.write((twoHyphens + boundary + twoHyphens + lineEnd).getBytes("UTF-8"));
        if(lengthSentImage > 0){
            DataOutputStream request = new DataOutputStream(outputStream);
            int totalSize = pixelsImage.length;
            int bytesTransferred = 0;
            request.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            if( it == imageType.AVATAR)
                request.writeBytes("Content-Disposition: form-data; name=\"avatar\";filename=\""+fileName+"\"" + lineEnd);
            else
                request.writeBytes("Content-Disposition: form-data; name=\"header\";filename=\""+fileName+"\"" + lineEnd);

            request.writeBytes(lineEnd);
            while (bytesTransferred < totalSize) {
                int nextChunkSize = totalSize - bytesTransferred;
                if (nextChunkSize > CHUNK_SIZE) {
                    nextChunkSize = CHUNK_SIZE;
                }
                request.write(pixelsImage, bytesTransferred, nextChunkSize);
                bytesTransferred += nextChunkSize;
                request.flush();
            }
            request.writeBytes(lineEnd);
            request.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            request.flush();
        }

        if (httpURLConnection.getResponseCode() >= 200 && httpURLConnection.getResponseCode() < 400) {
            new String(ByteStreams.toByteArray(httpURLConnection.getInputStream()));
        } else {
            String error = null;
            if( httpURLConnection.getErrorStream() != null) {
                InputStream stream = httpURLConnection.getErrorStream();
                if (stream == null) {
                    stream = httpURLConnection.getInputStream();
                }
                try (Scanner scanner = new Scanner(stream)) {
                    scanner.useDelimiter("\\Z");
                    error = scanner.next();
                }catch (Exception e){e.printStackTrace();}
            }
            int responseCode = httpURLConnection.getResponseCode();
            try {
                httpURLConnection.getInputStream().close();
            }catch (Exception ignored){}

            throw new HttpsConnectionException(responseCode, error);
        }
        httpURLConnection.getInputStream().close();

    }




        @SuppressWarnings("SameParameterValue")
        public void patch(String urlConnection, int timeout, HashMap<String, String> paramaters, InputStream avatar, String avatarName, InputStream header, String headerName, String token) throws IOException, HttpsConnectionException {
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
                postData.append(String.valueOf(param.getValue()));
            }
            byte[] postDataBytes = (postData.toString()).getBytes("UTF-8");



            initialize(url);
            httpURLConnection.setRequestProperty("User-Agent", Helper.USER_AGENT);
            httpURLConnection.setConnectTimeout(timeout * 1000);
            //httpURLConnection.setSSLSocketFactory(new TLSSocketFactory(this.instance));
            if( Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT ){
                httpURLConnection.setRequestMethod("PATCH");
            }else {
                httpURLConnection.setRequestProperty("X-HTTP-Method-Override", "PATCH");
                httpURLConnection.setRequestMethod("POST");
            }
            if (token != null && !token.startsWith("Basic "))
                httpURLConnection.setRequestProperty("Authorization", "Bearer " + token);
            else if( token != null && token.startsWith("Basic "))
                httpURLConnection.setRequestProperty("Authorization", token);
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            httpURLConnection.setDoOutput(true);


            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(postDataBytes);
            if( avatar != null)
                patchImage(urlConnection,120,imageType.AVATAR, avatar,avatarName,token);
            if( header != null)
                patchImage(urlConnection,120,imageType.BANNER, header,headerName,token);
            if (httpURLConnection.getResponseCode() >= 200 && httpURLConnection.getResponseCode() < 400) {
                new String(ByteStreams.toByteArray(httpURLConnection.getInputStream()));
            } else {
                String error = null;
                if( httpURLConnection.getErrorStream() != null) {
                    InputStream stream = httpURLConnection.getErrorStream();
                    if (stream == null) {
                        stream = httpURLConnection.getInputStream();
                    }
                    try (Scanner scanner = new Scanner(stream)) {
                        scanner.useDelimiter("\\Z");
                        error = scanner.next();
                    }catch (Exception e){e.printStackTrace();}
                }
                int responseCode = httpURLConnection.getResponseCode();
                try {
                    httpURLConnection.getInputStream().close();
                }catch (Exception ignored){}
                throw new HttpsConnectionException(responseCode, error);
            }
            httpURLConnection.getInputStream().close();
    }



    public String put(String urlConnection, int timeout, HashMap<String, String> paramaters, String token) throws IOException, HttpsConnectionException {
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
            postData.append(String.valueOf(param.getValue()));
        }
        byte[] postDataBytes = postData.toString().getBytes("UTF-8");

        initialize(url);
        httpURLConnection.setRequestProperty("User-Agent", Helper.USER_AGENT);
        httpURLConnection.setConnectTimeout(timeout * 1000);
        //httpURLConnection.setSSLSocketFactory(new TLSSocketFactory(this.instance));
        if (token != null && !token.startsWith("Basic "))
            httpURLConnection.setRequestProperty("Authorization", "Bearer " + token);
        else if( token != null && token.startsWith("Basic "))
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
            if( httpURLConnection.getErrorStream() != null) {
                InputStream stream = httpURLConnection.getErrorStream();
                if (stream == null) {
                    stream = httpURLConnection.getInputStream();
                }
                try (Scanner scanner = new Scanner(stream)) {
                    scanner.useDelimiter("\\Z");
                    error = scanner.next();
                }catch (Exception e){e.printStackTrace();}
            }
            int responseCode = httpURLConnection.getResponseCode();
            throw new HttpsConnectionException(responseCode, error);
        }
        getSinceMaxId();
        httpURLConnection.getInputStream().close();
        return response;
    }






    public int delete(String urlConnection, int timeout, HashMap<String, String> paramaters, String token) throws IOException, HttpsConnectionException {
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
            postData.append(String.valueOf(param.getValue()));
        }
        byte[] postDataBytes = postData.toString().getBytes("UTF-8");

        initialize(url);
        httpURLConnection.setRequestProperty("User-Agent", Helper.USER_AGENT);
       // httpURLConnection.setSSLSocketFactory(new TLSSocketFactory(this.instance));
        if (token != null && !token.startsWith("Basic "))
            httpURLConnection.setRequestProperty("Authorization", "Bearer " + token);
        else if( token != null && token.startsWith("Basic "))
            httpURLConnection.setRequestProperty("Authorization", token);
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
            if( httpURLConnection.getErrorStream() != null) {
                InputStream stream = httpURLConnection.getErrorStream();
                if (stream == null) {
                    stream = httpURLConnection.getInputStream();
                }
                try (Scanner scanner = new Scanner(stream)) {
                    scanner.useDelimiter("\\Z");
                    error = scanner.next();
                }catch (Exception e){e.printStackTrace();}
            }
            int responseCode = httpURLConnection.getResponseCode();
            throw new HttpsConnectionException(responseCode, error);
        }
    }

    public String getSince_id() {
        return since_id;
    }

    public String getMax_id() {
        return max_id;
    }


    private void getSinceMaxId(){
        if( Helper.getLiveInstanceWithProtocol(context) == null)
            return;
        if (httpURLConnection == null)
            return;
        Map<String, List<String>> map = httpURLConnection.getHeaderFields();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            if (entry.toString().startsWith("Link") || entry.toString().startsWith("link") ) {
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

    private String converToString(InputStream inputStream) throws IOException {
        java.util.Scanner s = new java.util.Scanner(inputStream).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }


    int getActionCode() {
        if( Helper.getLiveInstanceWithProtocol(context).startsWith("https://")) {
            try {
                return httpURLConnection.getResponseCode();
            } catch (IOException e) {
                return -1;
            }
        }else {
            try {
                return httpURLConnection.getResponseCode();
            } catch (IOException e) {
                return -1;
            }
        }
    }

    public class HttpsConnectionException extends Exception {

        private int statusCode;
        private String message;
        HttpsConnectionException(int statusCode, String message) {
            this.statusCode = statusCode;
            SpannableString spannableString;
            if( message != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    spannableString = new SpannableString(Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY));
                else
                    //noinspection deprecation
                    spannableString = new SpannableString(Html.fromHtml(message));
            }else {
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
