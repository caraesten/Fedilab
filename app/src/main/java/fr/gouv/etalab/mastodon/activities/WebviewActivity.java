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
package fr.gouv.etalab.mastodon.activities;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Results;
import fr.gouv.etalab.mastodon.helper.CountDrawable;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.sqlite.DomainBlockDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import fr.gouv.etalab.mastodon.webview.MastalabWebChromeClient;
import fr.gouv.etalab.mastodon.webview.MastalabWebViewClient;

import static fr.gouv.etalab.mastodon.helper.Helper.EXTERNAL_STORAGE_REQUEST_CODE;
import static fr.gouv.etalab.mastodon.helper.Helper.THEME_LIGHT;
import static fr.gouv.etalab.mastodon.helper.Helper.manageDownloads;


/**
 * Created by Thomas on 24/06/2017.
 * Webview activity
 */

public class WebviewActivity extends BaseActivity {

    private String url;
    private String peertubeLinkToFetch;
    private boolean peertubeLink;
    private WebView webView;
    public static List<String> trackingDomains;
    private Menu defaultMenu;
    private MastalabWebViewClient mastalabWebViewClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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


        setContentView(R.layout.activity_webview);
        Bundle b = getIntent().getExtras();
        if(b != null) {
            url = b.getString("url", null);
            peertubeLinkToFetch = b.getString("peertubeLinkToFetch", null);
            peertubeLink =  b.getBoolean("peertubeLink", false);
        }
        if( url == null)
            finish();
        if( getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        webView = Helper.initializeWebview(WebviewActivity.this, R.id.webview);
        setTitle("");
        FrameLayout webview_container = findViewById(R.id.webview_container);
        final ViewGroup videoLayout = findViewById(R.id.videoLayout); // Your own view, read class comments
        webView.getSettings().setJavaScriptEnabled(true);
        MastalabWebChromeClient mastalabWebChromeClient = new MastalabWebChromeClient(WebviewActivity.this,  webView, webview_container, videoLayout);
        mastalabWebChromeClient.setOnToggledFullscreen(new MastalabWebChromeClient.ToggledFullscreenCallback() {
            @Override
            public void toggledFullscreen(boolean fullscreen) {

                if (fullscreen) {
                    videoLayout.setVisibility(View.VISIBLE);
                    WindowManager.LayoutParams attrs = getWindow().getAttributes();
                    attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    getWindow().setAttributes(attrs);
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                } else {
                    WindowManager.LayoutParams attrs = getWindow().getAttributes();
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    getWindow().setAttributes(attrs);
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    videoLayout.setVisibility(View.GONE);
                }
            }
        });
        webView.setWebChromeClient(mastalabWebChromeClient);
        mastalabWebViewClient = new MastalabWebViewClient(WebviewActivity.this);
        webView.setWebViewClient(mastalabWebViewClient);
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {

                if(Build.VERSION.SDK_INT >= 23 ){
                    if (ContextCompat.checkSelfPermission(WebviewActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(WebviewActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(WebviewActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_REQUEST_CODE);
                    } else {
                        manageDownloads(WebviewActivity.this, url);
                    }
                }else{
                    manageDownloads(WebviewActivity.this, url);
                }
            }
        });
        if( !url.startsWith("http://") && !url.startsWith("https://"))
            url = "http://" + url;
        if( trackingDomains == null){
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    SQLiteDatabase db = Sqlite.getInstance(WebviewActivity.this, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                    trackingDomains = new DomainBlockDAO(WebviewActivity.this, db).getAll();
                    if( trackingDomains == null)
                        trackingDomains = new ArrayList<>();
                    // Get a handler that can be used to post to the main thread
                    Handler mainHandler = new Handler(getMainLooper());
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            webView.loadUrl(url);
                        }
                    };
                    mainHandler.post(myRunnable);

                }
            });
        }else
            webView.loadUrl(url);
    }


    public void setCount(Context context, String count) {
        if( defaultMenu != null && !peertubeLink) {
            MenuItem menuItem = defaultMenu.findItem(R.id.action_block);
            LayerDrawable icon = (LayerDrawable) menuItem.getIcon();

            CountDrawable badge;

            // Reuse drawable if possible
            Drawable reuse = icon.findDrawableByLayerId(R.id.ic_block_count);
            if (reuse instanceof CountDrawable) {
                badge = (CountDrawable) reuse;
            } else {
                badge = new CountDrawable(context);
            }

            badge.setCount(count);
            icon.mutate();
            icon.setDrawableByLayerId(R.id.ic_block_count, badge);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(!peertubeLink)
            setCount(this, "0");
        defaultMenu = menu;
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_webview, menu);
        defaultMenu = menu;
        if( peertubeLink ){
            menu.findItem(R.id.action_go).setVisible(false);
            menu.findItem(R.id.action_block).setVisible(false);
            menu.findItem(R.id.action_comment).setVisible(true);
        }
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if( theme == THEME_LIGHT)
            Helper.colorizeIconMenu(menu, R.color.black);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_block:
                SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
                final int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);

                List<String> domains = mastalabWebViewClient.getDomains();

                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(WebviewActivity.this, R.layout.domains_blocked);
                arrayAdapter.addAll(domains);
                int style;
                if (theme == Helper.THEME_DARK) {
                    style = R.style.DialogDark;
                } else if (theme == Helper.THEME_BLACK){
                    style = R.style.DialogBlack;
                }else {
                    style = R.style.Dialog;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(WebviewActivity.this, style);
                builder.setTitle(R.string.list_of_blocked_domains);

                builder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strName = arrayAdapter.getItem(which);
                        assert strName != null;
                        Toasty.info(WebviewActivity.this, strName, Toast.LENGTH_LONG).show();
                    }
                });
                builder.show();

                return true;
            case R.id.action_go:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                try {
                    startActivity(browserIntent);
                }catch (Exception e){
                    Toasty.error(WebviewActivity.this,getString(R.string.toast_error),Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.action_comment:
                Toasty.info(WebviewActivity.this, getString(R.string.retrieve_remote_status), Toast.LENGTH_LONG).show();
                new AsyncTask<Void, Void, Void>() {

                    private List<fr.gouv.etalab.mastodon.client.Entities.Status> remoteStatuses;
                    private WeakReference<Context> contextReference = new WeakReference<>(WebviewActivity.this);

                    @Override
                    protected Void doInBackground(Void... voids) {

                        if(url != null) {
                            APIResponse search = new API(contextReference.get()).search(peertubeLinkToFetch);
                            if (search != null && search.getResults() != null) {
                                remoteStatuses = search.getResults().getStatuses();
                            }
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        Intent intent = new Intent(contextReference.get(), TootActivity.class);
                        Bundle b = new Bundle();
                        if( remoteStatuses == null || remoteStatuses.size() == 0){
                            Toasty.error(contextReference.get(),getString(R.string.toast_error),Toast.LENGTH_LONG).show();
                            return;
                        }
                        if( remoteStatuses.get(0).getReblog() != null ) {
                            b.putParcelable("tootReply", remoteStatuses.get(0).getReblog());
                        }else {
                            b.putParcelable("tootReply", remoteStatuses.get(0));
                        }
                        intent.putExtras(b); //Put your id to your next Intent
                        contextReference.get().startActivity(intent);
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR );
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setUrl(String newUrl){
        this.url = newUrl;
    }

    @Override
    public void onPause(){
        super.onPause();
        if( webView != null)
            webView.onPause();
    }

    @Override
    public void onResume(){
        super.onResume();
        if( webView != null)
            webView.onResume();
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()){
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if( webView != null)
            webView.destroy();
    }
}
