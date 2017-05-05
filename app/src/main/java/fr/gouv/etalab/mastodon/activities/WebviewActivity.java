/* Copyright 2017 Thomas Schneider
 *
 * This file is a part of Mastodon Etalab for mastodon.etalab.gouv.fr
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Mastodon Etalab is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Thomas Schneider; if not,
 * see <http://www.gnu.org/licenses>. */

package fr.gouv.etalab.mastodon.activities;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cz.msebera.android.httpclient.Header;
import mastodon.etalab.gouv.fr.mastodon.R;
import fr.gouv.etalab.mastodon.asynctasks.UpdateAccountInfoAsyncTask;
import fr.gouv.etalab.mastodon.client.OauthClient;
import fr.gouv.etalab.mastodon.helper.Helper;

/**
 * Created by Thomas on 24/04/2017.
 * Webview to connect accounts
 */
public class WebviewActivity extends AppCompatActivity {


    private Activity activity;
    private WebView webView;
    private Context context;
    private AlertDialog alert;
    private String clientId, clientSecret;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        this.activity = this;
        this.context = this;
        this.context = this.getApplicationContext();
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        clientId = sharedpreferences.getString(Helper.CLIENT_ID, null);
        clientSecret = sharedpreferences.getString(Helper.CLIENT_SECRET, null);

        webView = (WebView) findViewById(R.id.webviewConnect);
        clearCookies(getApplicationContext());
        final ProgressBar pbar = (ProgressBar) findViewById(R.id.progress_bar);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                if (progress < 100 && pbar.getVisibility() == ProgressBar.GONE) {
                    pbar.setVisibility(ProgressBar.VISIBLE);
                }
                pbar.setProgress(progress);
                if (progress == 100) {
                    pbar.setVisibility(ProgressBar.GONE);
                }
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                super.shouldOverrideUrlLoading(view,url);
                if( url.contains(Helper.REDIRECT_CONTENT)){
                    String val[] = url.split("code=");
                    String code = val[1];

                    String action = "/oauth/token";
                    HashMap<String,String> parameters = new HashMap<>();
                    parameters.put(Helper.CLIENT_ID, clientId);
                    parameters.put(Helper.CLIENT_SECRET, clientSecret);
                    parameters.put(Helper.REDIRECT_URI,"https://" + Helper.INSTANCE + Helper.REDIRECT_CONTENT);
                    parameters.put("grant_type", "authorization_code");
                    parameters.put("code",code);
                    new OauthClient().post(action, parameters, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            String response = new String(responseBody);
                            JSONObject resobj;
                            try {
                                resobj = new JSONObject(response);
                                String token = resobj.get("access_token").toString();
                                SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.putString(Helper.PREF_KEY_OAUTH_TOKEN, token);
                                editor.apply();
                                //Update the account with the token;
                                new UpdateAccountInfoAsyncTask(WebviewActivity.this, true, token).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            error.printStackTrace();
                        }
                    });


                    return true;
                }
                return false;
            }
        });
        webView.loadUrl(redirectUserToAuthorizeAndLogin());
    }


    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }



    private String redirectUserToAuthorizeAndLogin() {

        String queryString = Helper.CLIENT_ID + "="+ clientId;
        queryString += "&" + Helper.REDIRECT_URI + "="+ Uri.encode("https://" + Helper.INSTANCE + "/redirect_mastodon_api");
        queryString += "&" + Helper.RESPONSE_TYPE +"=code";
        queryString += "&" + Helper.SCOPE +"=" + Helper.OAUTH_SCOPES;
        /*try {
            queryString = URLEncoder.encode(queryString, "utf-8");
        } catch (UnsupportedEncodingException ignored) {}*/
        return "https://" + Helper.INSTANCE  + Helper.EP_AUTHORIZE + "?" + queryString;
    }


    private String getOauthRedirectUri() {
        return Helper.OAUTH_SCHEME + "://" + Helper.OAUTH_REDIRECT_HOST + "/";
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (alert != null) {
            alert.dismiss();
            alert = null;
        }
    }

    @SuppressWarnings("deprecation")
    public static void clearCookies(Context context)
    {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else
        {
            CookieSyncManager cookieSyncMngr=CookieSyncManager.createInstance(context);
            cookieSyncMngr.startSync();
            CookieManager cookieManager=CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }
}
