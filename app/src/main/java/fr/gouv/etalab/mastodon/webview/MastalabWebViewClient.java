package fr.gouv.etalab.mastodon.webview;
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
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import fr.gouv.etalab.mastodon.activities.WebviewActivity;
import mastodon.etalab.gouv.fr.mastodon.R;

/**
 * Created by Thomas on 25/06/2017.
 * Custom WebViewClient
 */

public class MastalabWebViewClient extends WebViewClient {

    private Activity activity;

    public MastalabWebViewClient(Activity activity){
        this.activity = activity;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
    }


    @Override
    public void onPageStarted (WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
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
