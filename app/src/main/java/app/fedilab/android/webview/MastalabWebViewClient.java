package app.fedilab.android.webview;
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
import android.app.Activity;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import app.fedilab.android.R;
import app.fedilab.android.activities.WebviewActivity;
import app.fedilab.android.helper.Helper;

/**
 * Created by Thomas on 25/06/2017.
 * Custom WebViewClient
 */

public class MastalabWebViewClient extends WebViewClient {

    private Activity activity;
    private int count = 0;

    public MastalabWebViewClient(Activity activity){
        this.activity = activity;
    }
    public List<String> domains = new ArrayList<>();
    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
    }


    @Override
    public WebResourceResponse shouldInterceptRequest (final WebView view, String url) {
        if( WebviewActivity.trackingDomains != null){
            URI uri;
            try {
                uri = new URI(url);
                String domain = uri.getHost();
                if( domain != null) {
                    domain = domain.startsWith("www.") ? domain.substring(4) : domain;
                }
                if (domain != null && WebviewActivity.trackingDomains.contains(domain)) {
                    if( activity instanceof WebviewActivity){
                        count++;
                        domains.add(url);
                        ((WebviewActivity)activity).setCount(activity, String.valueOf(count));
                    }
                    ByteArrayInputStream nothing = new    ByteArrayInputStream("".getBytes());
                    return new WebResourceResponse("text/plain", "utf-8", nothing);
                }
            } catch (URISyntaxException e) {
                try {
                    url = url.substring(0, 50);
                    uri = new URI(url);
                    String domain = uri.getHost();
                    if (domain != null) {
                        domain = domain.startsWith("www.") ? domain.substring(4) : domain;
                    }
                    if (domain != null && WebviewActivity.trackingDomains.contains(domain)) {
                        if (activity instanceof WebviewActivity) {
                            count++;
                            domains.add(url);
                            ((WebviewActivity) activity).setCount(activity, String.valueOf(count));
                        }
                        ByteArrayInputStream nothing = new ByteArrayInputStream("".getBytes());
                        return new WebResourceResponse("text/plain", "utf-8", nothing);

                    }
                } catch (URISyntaxException ignored) {
                    ignored.printStackTrace();
                }
            }
        }
        return super.shouldInterceptRequest(view, url);
    }

    public List<String> getDomains(){
        return this.domains;
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        String instance = Helper.getLiveInstance(activity);
        if( instance != null && instance.endsWith(".onion")) {
            handler.proceed();
        }else{
            super.onReceivedSslError(view, handler, error);
        }
    }

    @Override
    public void onPageStarted (WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        count = 0;
        domains = new ArrayList<>();
        domains.clear();
        ActionBar actionBar = ((AppCompatActivity) activity).getSupportActionBar();
        LayoutInflater mInflater = LayoutInflater.from(activity);
        if( actionBar != null){
            @SuppressLint("InflateParams") View webview_actionbar = mInflater.inflate(R.layout.webview_actionbar, null);
            TextView webview_title = webview_actionbar.findViewById(R.id.webview_title);
            webview_title.setText(url);
            actionBar.setCustomView(webview_actionbar);
            actionBar.setDisplayShowCustomEnabled(true);
        }else {
            activity.setTitle(url);
        }
        //Changes the url in webview activity so that it can be opened with an external app
        try{
            if( activity instanceof WebviewActivity)
                ((WebviewActivity)activity).setUrl(url);
        }catch (Exception ignore){}

    }

}
