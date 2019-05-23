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

package app.fedilab.android.activities;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import app.fedilab.android.client.HttpsConnection;
import app.fedilab.android.helper.Helper;
import es.dmoral.toasty.Toasty;
import app.fedilab.android.R;
import app.fedilab.android.asynctasks.UpdateAccountInfoAsyncTask;

/**
 * Created by Thomas on 24/04/2017.
 * Webview to connect accounts
 */
public class WebviewConnectActivity extends BaseActivity {


    private WebView webView;
    private AlertDialog alert;
    private String clientId, clientSecret;
    private String instance;
    private UpdateAccountInfoAsyncTask.SOCIAL social;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        switch (theme){
            case Helper.THEME_LIGHT:
                setTheme(R.style.AppTheme);
                break;
            case Helper.THEME_DARK:
                setTheme(R.style.AppThemeDark);
                break;
            case Helper.THEME_BLACK:
                setTheme(R.style.AppThemeBlack);
                break;
            default:
                setTheme(R.style.AppThemeDark);
        }

        setContentView(R.layout.activity_webview_connect);
        Bundle b = getIntent().getExtras();
        if(b != null) {
            instance = b.getString("instance");
            social = (UpdateAccountInfoAsyncTask.SOCIAL) b.getSerializable("social");
        }
        if( instance == null)
            finish();
        clientId = sharedpreferences.getString(Helper.CLIENT_ID, null);
        clientSecret = sharedpreferences.getString(Helper.CLIENT_SECRET, null);
        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null ) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.simple_bar, null);
            actionBar.setCustomView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            ImageView toolbar_close = actionBar.getCustomView().findViewById(R.id.toolbar_close);
            TextView toolbar_title = actionBar.getCustomView().findViewById(R.id.toolbar_title);
            toolbar_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            toolbar_title.setText(R.string.add_account);
            if (theme == Helper.THEME_LIGHT){
                Toolbar toolbar = actionBar.getCustomView().findViewById(R.id.toolbar);
                Helper.colorizeToolbar(toolbar, R.color.black, WebviewConnectActivity.this);
            }
        }
        webView = findViewById(R.id.webviewConnect);
        webView.getSettings().setJavaScriptEnabled(true);
        clearCookies(getApplicationContext());
        final ProgressBar pbar = findViewById(R.id.progress_bar);
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
                if( url.contains(Helper.REDIRECT_CONTENT_WEB)){
                    String val[] = url.split("code=");
                    if (val.length< 2){
                        Toasty.error(getApplicationContext(), getString(R.string.toast_code_error), Toast.LENGTH_LONG).show();
                        Intent myIntent = new Intent(WebviewConnectActivity.this, LoginActivity.class);
                        startActivity(myIntent);
                        finish();
                        return false;
                    }
                    String code = val[1];
                    final String action = "/oauth/token";
                    final HashMap<String, String> parameters = new HashMap<>();
                    parameters.put(Helper.CLIENT_ID, clientId);
                    parameters.put(Helper.CLIENT_SECRET, clientSecret);
                    parameters.put(Helper.REDIRECT_URI,Helper.REDIRECT_CONTENT_WEB);
                    parameters.put("grant_type", "authorization_code");
                    parameters.put("code",code);
                    new Thread(new Runnable(){
                        @Override
                        public void run() {
                            try {
                                final String response = new HttpsConnection(WebviewConnectActivity.this, instance).post(Helper.instanceWithProtocol(instance) + action, 30, parameters, null);
                                JSONObject resobj;
                                try {
                                    resobj = new JSONObject(response);
                                    String token = resobj.get("access_token").toString();
                                    String refresh_token = null;
                                    if( resobj.has("refresh_token"))
                                        refresh_token = resobj.get("refresh_token").toString();
                                    SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedpreferences.edit();
                                    editor.putString(Helper.PREF_KEY_OAUTH_TOKEN, token);
                                    editor.apply();
                                    //Update the account with the token;
                                    new UpdateAccountInfoAsyncTask(WebviewConnectActivity.this, token, clientId, clientSecret, refresh_token, instance, social).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                } catch (JSONException ignored) {}
                            } catch (Exception ignored) {}
                        }}).start();
                    return true;
                }
                return false;
            }

        });
        webView.loadUrl(LoginActivity.redirectUserToAuthorizeAndLogin(social, clientId, instance));
    }


    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
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
        } else {
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