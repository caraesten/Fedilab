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
import java.io.Reader;
import java.net.HttpURLConnection;
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
import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.activities.MediaActivity;
import fr.gouv.etalab.mastodon.activities.TootActivity;
import fr.gouv.etalab.mastodon.client.Entities.Attachment;
import fr.gouv.etalab.mastodon.client.Entities.Error;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnDownloadInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveAttachmentInterface;


/**
 * Created by Thomas on 17/11/2017.
 * Manage http queries
 */

public class HttpsConnection {



    private HttpsURLConnection httpsURLConnection;
    private String since_id, max_id;
    private Context context;


    public HttpsConnection(){}

    public HttpsConnection(Context context){
        this.context = context;
    }

    @SuppressWarnings("ConstantConditions")
    public String get(String urlConnection, int timeout, HashMap<String, String> paramaters, String token) throws IOException, NoSuchAlgorithmException, KeyManagementException, HttpsConnectionException {


        Map<String,Object> params = new LinkedHashMap<>();
        if( paramaters != null) {
            Iterator it = paramaters.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                params.put(pair.getKey().toString(), pair.getValue());
                it.remove();
            }
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
        httpsURLConnection.setRequestProperty("http.keepAlive", "false");
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP)
            httpsURLConnection.setSSLSocketFactory(new TLSSocketFactory());
        if( token != null)
            httpsURLConnection.setRequestProperty("Authorization", "Bearer " + token);
        httpsURLConnection.setRequestMethod("GET");
        if (httpsURLConnection.getResponseCode() >= 200 && httpsURLConnection.getResponseCode() < 400) {
            Reader in = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (int c; (c = in.read()) >= 0; )
                sb.append((char) c);
            getSinceMaxId();
            httpsURLConnection.disconnect();
            in.close();
            return sb.toString();
        }else {
            Reader in = new BufferedReader(new InputStreamReader(httpsURLConnection.getErrorStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (int c; (c = in.read()) >= 0; )
                sb.append((char) c);
            httpsURLConnection.disconnect();
            throw new HttpsConnectionException(httpsURLConnection.getResponseCode(), sb.toString());
        }
    }



    public String post(String urlConnection, int timeout, HashMap<String, String> paramaters, String token) throws IOException, NoSuchAlgorithmException, KeyManagementException, HttpsConnectionException {
        URL url = new URL(urlConnection);
        Map<String,Object> params = new LinkedHashMap<>();
        if( paramaters != null) {
            Iterator it = paramaters.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                params.put(pair.getKey().toString(), pair.getValue());
                it.remove();
            }
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
        httpsURLConnection.setDoOutput(true);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP)
            httpsURLConnection.setSSLSocketFactory(new TLSSocketFactory());
        httpsURLConnection.setRequestMethod("POST");
        if( token != null)
            httpsURLConnection.setRequestProperty("Authorization", "Bearer " + token);
        httpsURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        httpsURLConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));


        httpsURLConnection.getOutputStream().write(postDataBytes);
        Reader in = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (int c; (c = in.read()) >= 0;)
            sb.append((char)c);
        getSinceMaxId();
        httpsURLConnection.disconnect();
        in.close();
        return sb.toString();

    }


    public void download(final String downloadUrl, final OnDownloadInterface listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                URL url;
                try {
                    url = new URL(downloadUrl);
                    httpsURLConnection = (HttpsURLConnection) url.openConnection();
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
                            fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1,
                                    downloadUrl.length());
                        }
                        // opens input stream from the HTTP connection
                        InputStream inputStream = httpsURLConnection.getInputStream();
                        File saveDir = context.getCacheDir();
                        final String saveFilePath = saveDir + File.separator + fileName;

                        // opens an output stream to save into file
                        FileOutputStream outputStream = new FileOutputStream(saveFilePath);

                        int bytesRead;
                        byte[] buffer = new byte[4096];
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        outputStream.close();
                        inputStream.close();
                        if(context instanceof TootActivity)
                        ((TootActivity)context).runOnUiThread(new Runnable() {
                            public void run() {
                                listener.onDownloaded(saveFilePath, null);
                            }});
                        if(context instanceof MediaActivity)
                            ((MediaActivity)context).runOnUiThread(new Runnable() {
                                public void run() {
                                    listener.onDownloaded(saveFilePath, null);
                                }});
                    } else {
                        final Error error = new Error();
                        error.setError(String.valueOf(responseCode));
                        if(context instanceof TootActivity)
                            ((TootActivity)context).runOnUiThread(new Runnable() {
                                public void run() {
                                    listener.onDownloaded(null, error);
                                }});
                        if(context instanceof MediaActivity)
                            ((MediaActivity)context).runOnUiThread(new Runnable() {
                                public void run() {
                                    listener.onDownloaded(null, error);
                                }});

                    }
                    httpsURLConnection.disconnect();
                } catch (IOException e) {
                    Error error = new Error();
                    error.setError(context.getString(R.string.toast_error));
                    if(httpsURLConnection != null)
                        httpsURLConnection.disconnect();
                    e.printStackTrace();
                }

            }
        }).start();


    }

    public void upload(final InputStream inputStream, final OnRetrieveAttachmentInterface listener) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    String twoHyphens = "--";
                    String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";
                    String lineEnd = "\r\n";

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
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
                        httpsURLConnection.setSSLSocketFactory(new TLSSocketFactory());
                    httpsURLConnection.setDoInput(true);
                    httpsURLConnection.setDoOutput(true);
                    httpsURLConnection.setUseCaches(false);
                    httpsURLConnection.setRequestMethod("POST");
                    if (token != null)
                        httpsURLConnection.setRequestProperty("Authorization", "Bearer " + token);
                    httpsURLConnection.setRequestProperty("Connection", "Keep-Alive");
                    httpsURLConnection.setRequestProperty("Cache-Control", "no-cache");
                    httpsURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+ boundary);
                    httpsURLConnection.setChunkedStreamingMode(-1);

                    DataOutputStream request = new DataOutputStream(httpsURLConnection.getOutputStream());

                    request.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                    request.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\"picture.png\"" + lineEnd);
                    request.writeBytes(lineEnd);

                    //request.write(pixels);

                    int totalSize = pixels.length;
                    int bytesTransferred = 0;
                    int chunkSize = 2048;

                    while (bytesTransferred < totalSize) {
                        int nextChunkSize = totalSize - bytesTransferred;
                        if (nextChunkSize > chunkSize) {
                            nextChunkSize = chunkSize;
                        }
                        request.write(pixels, bytesTransferred, nextChunkSize);
                        bytesTransferred += nextChunkSize;

                        final int progress = 100 * bytesTransferred / totalSize;
                        ((TootActivity)context).runOnUiThread(new Runnable() {
                            public void run() {
                                listener.onUpdateProgress(progress);
                            }});
                        request.flush();
                    }
                    request.writeBytes(lineEnd);
                    request.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                    request.flush();
                    request.close();

                    if (200 != httpsURLConnection.getResponseCode()) {
                        Reader in = new BufferedReader(new InputStreamReader(httpsURLConnection.getErrorStream(), "UTF-8"));
                        StringBuilder sb = new StringBuilder();
                        for (int c; (c = in.read()) >= 0; )
                            sb.append((char) c);
                        httpsURLConnection.disconnect();
                        throw new HttpsConnectionException(httpsURLConnection.getResponseCode(), context.getString(R.string.toast_error));
                    }

                    InputStream responseStream = new BufferedInputStream(httpsURLConnection.getInputStream());

                    BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));

                    String line;
                    StringBuilder stringBuilder = new StringBuilder();

                    while ((line = responseStreamReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    ((TootActivity)context).runOnUiThread(new Runnable() {
                        public void run() {
                            listener.onUpdateProgress(101);
                        }});


                    String response = stringBuilder.toString();
                    final Attachment attachment = API.parseAttachmentResponse(new JSONObject(response));
                    responseStreamReader.close();
                    responseStream.close();
                    httpsURLConnection.disconnect();

                    ((TootActivity)context).runOnUiThread(new Runnable() {
                        public void run() {
                            listener.onRetrieveAttachment(attachment, null);
                        }});
                }catch (Exception e) {
                    e.printStackTrace();
                    ((TootActivity)context).runOnUiThread(new Runnable() {
                        public void run() {
                            listener.onUpdateProgress(101);
                        }});
                    final Error error = new Error();
                    error.setError(e.getMessage());
                    if(httpsURLConnection != null)
                        httpsURLConnection.disconnect();
                    ((TootActivity)context).runOnUiThread(new Runnable() {
                        public void run() {
                            listener.onRetrieveAttachment(null, error);
                        }});

                }
            }
        }).start();


    }

    public String put(String urlConnection, int timeout, HashMap<String, String> paramaters, String token) throws IOException, NoSuchAlgorithmException, KeyManagementException, HttpsConnectionException {
        URL url = new URL(urlConnection);
        Map<String,Object> params = new LinkedHashMap<>();
        if( paramaters != null) {
            Iterator it = paramaters.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                params.put(pair.getKey().toString(), pair.getValue());
                it.remove();
            }
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

        httpsURLConnection.setRequestMethod("PUT");
        httpsURLConnection.setDoInput(true);
        httpsURLConnection.setDoOutput(true);

        if (httpsURLConnection.getResponseCode() >= 200 && httpsURLConnection.getResponseCode() < 400) {
            DataOutputStream dataOutputStream = new DataOutputStream(httpsURLConnection.getOutputStream());
            dataOutputStream.write(postDataBytes);
            Reader in = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (int c; (c = in.read()) >= 0;)
                sb.append((char)c);
            getSinceMaxId();
            httpsURLConnection.disconnect();
            in.close();
            return sb.toString();
        }else {
            Reader in = new BufferedReader(new InputStreamReader(httpsURLConnection.getErrorStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (int c; (c = in.read()) >= 0; )
                sb.append((char) c);
            httpsURLConnection.disconnect();
            throw new HttpsConnectionException(httpsURLConnection.getResponseCode(), sb.toString());
        }

    }



    public String patch(String urlConnection, int timeout, HashMap<String, String> paramaters, String token) throws IOException, NoSuchAlgorithmException, KeyManagementException, HttpsConnectionException {
        URL url = new URL(urlConnection);
        Map<String,Object> params = new LinkedHashMap<>();
        if( paramaters != null) {
            Iterator it = paramaters.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                params.put(pair.getKey().toString(), pair.getValue());
                it.remove();
            }
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

        if (httpsURLConnection.getResponseCode() >= 200 && httpsURLConnection.getResponseCode() < 400) {
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
            httpsURLConnection.disconnect();
            throw new HttpsConnectionException(httpsURLConnection.getResponseCode(), sb.toString());
        }

    }


    public int delete(String urlConnection, int timeout, HashMap<String, String> paramaters, String token) throws IOException, NoSuchAlgorithmException, KeyManagementException, HttpsConnectionException {
        URL url = new URL(urlConnection);
        Map<String,Object> params = new LinkedHashMap<>();
        if( paramaters != null) {
            Iterator it = paramaters.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                params.put(pair.getKey().toString(), pair.getValue());
                it.remove();
            }
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
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP)
            httpsURLConnection.setSSLSocketFactory(new TLSSocketFactory());
        if( token != null)
            httpsURLConnection.setRequestProperty("Authorization", "Bearer " + token);
        httpsURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        httpsURLConnection.setRequestMethod("DELETE");
        httpsURLConnection.setConnectTimeout(timeout * 1000);
        httpsURLConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));

        getSinceMaxId();



        if (httpsURLConnection.getResponseCode() >= 200 && httpsURLConnection.getResponseCode() < 400) {
            httpsURLConnection.disconnect();
            return httpsURLConnection.getResponseCode();
        }else {
            Reader in = new BufferedReader(new InputStreamReader(httpsURLConnection.getErrorStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (int c; (c = in.read()) >= 0; )
                sb.append((char) c);
            httpsURLConnection.disconnect();
            throw new HttpsConnectionException(httpsURLConnection.getResponseCode(), sb.toString());
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
        try {
            return httpsURLConnection.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
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
