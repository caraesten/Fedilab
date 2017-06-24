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


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import fr.gouv.etalab.mastodon.helper.Helper;
import mastodon.etalab.gouv.fr.mastodon.R;


/**
 * Created by Thomas on 24/06/2017.
 * Webview activity
 */

public class WebviewActivity extends AppCompatActivity {

    private String url;
    private ProgressBar pbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        Bundle b = getIntent().getExtras();
        if(b != null)
            url = b.getString("url", null);
        if( url == null)
            finish();
        if( getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        boolean javascript = sharedpreferences.getBoolean(Helper.SET_JAVASCRIPT, true);



        pbar = (ProgressBar) findViewById(R.id.progress_bar);
        WebView webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(javascript);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setSupportMultipleWindows(false);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            //noinspection deprecation
            webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webView.getSettings().setMediaPlaybackRequiresUserGesture(true);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            boolean cookies = sharedpreferences.getBoolean(Helper.SET_COOKIES, false);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptThirdPartyCookies(webView, cookies);
        }
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);


        setTitle("");
        FrameLayout webview_container = (FrameLayout) findViewById(R.id.webview_container);
        final ViewGroup videoLayout = (ViewGroup) findViewById(R.id.videoLayout); // Your own view, read class comments


        MastalabWebChromeClient mastalabWebChromeClient = new MastalabWebChromeClient(webView, webview_container, videoLayout);
        mastalabWebChromeClient.setOnToggledFullscreen(new ToggledFullscreenCallback() {
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
        webView.setWebViewClient(new MastalabWebViewClient());
        webView.loadUrl(url);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

    }

    private class MastalabWebViewClient extends WebViewClient{
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }
        @Override
        public void onPageStarted (WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            ActionBar actionBar = getSupportActionBar();
            LayoutInflater mInflater = LayoutInflater.from(WebviewActivity.this);
            if( actionBar != null){
                View webview_actionbar = mInflater.inflate(R.layout.webview_actionbar, null);
                TextView webview_title = (TextView) webview_actionbar.findViewById(R.id.webview_title);
                webview_title.setText(url);
                actionBar.setCustomView(webview_actionbar);
                actionBar.setDisplayShowCustomEnabled(true);
            }else {
                setTitle(url);
            }
        }
    }

    interface ToggledFullscreenCallback {
        void toggledFullscreen(boolean fullscreen);
    }


    private class MastalabWebChromeClient extends WebChromeClient implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

        private FrameLayout videoViewContainer;
        private CustomViewCallback videoViewCallback;

        private ToggledFullscreenCallback toggledFullscreenCallback;
        private boolean isVideoFullscreen;
        private WebView webView;
        private View activityNonVideoView;
        private ViewGroup activityVideoView;
        private View loadingView;

        MastalabWebChromeClient(WebView webView, FrameLayout webviewContainer, ViewGroup videoLayout){
            this.isVideoFullscreen = false;
            this.webView = webView;
            this.activityNonVideoView = webviewContainer;
            this.activityVideoView = videoLayout;
        }

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
        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            super.onReceivedIcon(view, icon);
            LayoutInflater mInflater = LayoutInflater.from(WebviewActivity.this);
            ActionBar actionBar = getSupportActionBar();
            if( actionBar != null){
                View webview_actionbar = mInflater.inflate(R.layout.webview_actionbar, null);
                TextView webview_title = (TextView) webview_actionbar.findViewById(R.id.webview_title);
                webview_title.setText(view.getTitle());
                ImageView webview_favicon = (ImageView) webview_actionbar.findViewById(R.id.webview_favicon);
                if( icon != null)
                    webview_favicon.setImageBitmap(icon);
                actionBar.setCustomView(webview_actionbar);
                actionBar.setDisplayShowCustomEnabled(true);
            }else {
                setTitle(view.getTitle());
            }

        }

        //FULLSCREEN VIDEO
        //Code from https://stackoverflow.com/a/16179544/3197259

        /**
         * Set a callback that will be fired when the video starts or finishes displaying using a custom view (typically full-screen)
         * @param callback A VideoEnabledWebChromeClient.ToggledFullscreenCallback callback
         */
        void setOnToggledFullscreen(ToggledFullscreenCallback callback) {
            this.toggledFullscreenCallback = callback;
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            if (view instanceof FrameLayout) {
                if( getSupportActionBar() != null)
                    getSupportActionBar().hide();
                // A video wants to be shown
                FrameLayout frameLayout = (FrameLayout) view;
                View focusedChild = frameLayout.getFocusedChild();

                // Save video related variables
                isVideoFullscreen = true;
                this.videoViewContainer = frameLayout;
                this.videoViewCallback = callback;

                // Hide the non-video view, add the video view, and show it
                activityNonVideoView.setVisibility(View.INVISIBLE);
                activityVideoView.addView(videoViewContainer, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                activityVideoView.setVisibility(View.VISIBLE);
                if (focusedChild instanceof android.widget.VideoView) {
                    // android.widget.VideoView (typically API level <11)
                    android.widget.VideoView videoView = (android.widget.VideoView) focusedChild;
                    // Handle all the required events
                    videoView.setOnPreparedListener(this);
                    videoView.setOnCompletionListener(this);
                    videoView.setOnErrorListener(this);
                } else {
                    // Other classes, including:
                    // - android.webkit.HTML5VideoFullScreen$VideoSurfaceView, which inherits from android.view.SurfaceView (typically API level 11-18)
                    // - android.webkit.HTML5VideoFullScreen$VideoTextureView, which inherits from android.view.TextureView (typically API level 11-18)
                    // - com.android.org.chromium.content.browser.ContentVideoView$VideoSurfaceView, which inherits from android.view.SurfaceView (typically API level 19+)

                    // Handle HTML5 video ended event only if the class is a SurfaceView
                    // Test case: TextureView of Sony Xperia T API level 16 doesn't work fullscreen when loading the javascript below
                    if (webView != null && webView.getSettings().getJavaScriptEnabled() && focusedChild instanceof SurfaceView) {
                        // Run javascript code that detects the video end and notifies the Javascript interface
                        String js = "javascript:";
                        js += "var _ytrp_html5_video_last;";
                        js += "var _ytrp_html5_video = document.getElementsByTagName('video')[0];";
                        js += "if (_ytrp_html5_video != undefined && _ytrp_html5_video != _ytrp_html5_video_last) {";
                        {
                            js += "_ytrp_html5_video_last = _ytrp_html5_video;";
                            js += "function _ytrp_html5_video_ended() {";
                            {
                                js += "_VideoEnabledWebView.notifyVideoEnd();"; // Must match Javascript interface name and method of VideoEnableWebView
                            }
                            js += "}";
                            js += "_ytrp_html5_video.addEventListener('ended', _ytrp_html5_video_ended);";
                        }
                        js += "}";
                        webView.loadUrl(js);
                    }
                }
                // Notify full-screen change
                if (toggledFullscreenCallback != null) {
                    toggledFullscreenCallback.toggledFullscreen(true);
                }
            }
        }

        // Available in API level 14+, deprecated in API level 18+
        @Override @SuppressWarnings("deprecation")
        public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
            onShowCustomView(view, callback);
        }

        @Override
        public void onHideCustomView() {
            if( getSupportActionBar() != null)
                getSupportActionBar().show();
            // This method should be manually called on video end in all cases because it's not always called automatically.
            // This method must be manually called on back key press (from this class' onBackPressed() method).
            if (isVideoFullscreen) {
                // Hide the video view, remove it, and show the non-video view
                activityVideoView.setVisibility(View.INVISIBLE);
                activityVideoView.removeView(videoViewContainer);
                activityNonVideoView.setVisibility(View.VISIBLE);
                // Call back (only in API level <19, because in API level 19+ with chromium webview it crashes)
                if (videoViewCallback != null && !videoViewCallback.getClass().getName().contains(".chromium.")) {
                    videoViewCallback.onCustomViewHidden();
                }

                // Reset video related variables
                isVideoFullscreen = false;
                videoViewContainer = null;
                videoViewCallback = null;

                // Notify full-screen change
                if (toggledFullscreenCallback != null) {
                    toggledFullscreenCallback.toggledFullscreen(false);
                }
            }
        }

        // Video will start loading
        @Override
        public View getVideoLoadingProgressView() {
            return super.getVideoLoadingProgressView();
        }

        // Video will start playing, only called in the case of android.widget.VideoView (typically API level <11)
        @Override
        public void onPrepared(MediaPlayer mp) {
            if (loadingView != null) {
                loadingView.setVisibility(View.GONE);
            }
        }

        // Video finished playing, only called in the case of android.widget.VideoView (typically API level <11)
        @Override
        public void onCompletion(MediaPlayer mp) {
            onHideCustomView();
        }

        // Error while playing video, only called in the case of android.widget.VideoView (typically API level <11)
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            return false; // By returning false, onCompletion() will be called
        }

    }


}
