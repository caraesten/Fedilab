package fr.gouv.etalab.mastodon.webview;
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
import android.app.Activity;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import mastodon.etalab.gouv.fr.mastodon.R;

/**
 * Created by Thomas on 25/06/2017.
 * Custom WebChromeClient
 */

public class MastalabWebChromeClient extends WebChromeClient implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

        private FrameLayout videoViewContainer;
        private WebChromeClient.CustomViewCallback videoViewCallback;

        private ToggledFullscreenCallback toggledFullscreenCallback;

        private WebView webView;
        private View activityNonVideoView;
        private ViewGroup activityVideoView;
        private ProgressBar pbar;
        private boolean isVideoFullscreen;
        private Activity activity;


        public interface ToggledFullscreenCallback {
            void toggledFullscreen(boolean fullscreen);
        }

        public MastalabWebChromeClient(Activity activity, WebView webView, FrameLayout activityNonVideoView, ViewGroup activityVideoView){
            this.activity = activity;
            this.isVideoFullscreen = false;
            this.webView = webView;
            this.pbar = (ProgressBar) activity.findViewById(R.id.progress_bar);
            this.activityNonVideoView = activityNonVideoView;
            this.activityVideoView = activityVideoView;
        }

        @Override
        public void onProgressChanged(WebView view, int progress) {
            if( pbar != null){
                if (progress < 100 && pbar.getVisibility() == ProgressBar.GONE) {
                    pbar.setVisibility(ProgressBar.VISIBLE);
                }
                pbar.setProgress(progress);
                if (progress == 100) {
                    pbar.setVisibility(ProgressBar.GONE);
                }
            }
        }


        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            super.onReceivedIcon(view, icon);
            LayoutInflater mInflater = LayoutInflater.from(activity);
            ActionBar actionBar = ((AppCompatActivity) activity).getSupportActionBar();
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
                activity.setTitle(view.getTitle());
            }

        }

        //FULLSCREEN VIDEO
        //Code from https://stackoverflow.com/a/16179544/3197259

        /**
         * Set a callback that will be fired when the video starts or finishes displaying using a custom view (typically full-screen)
         * @param callback A VideoEnabledWebChromeClient.ToggledFullscreenCallback callback
         */
    public void setOnToggledFullscreen(ToggledFullscreenCallback callback) {
        this.toggledFullscreenCallback = callback;
    }

    @Override
    public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback) {
        if (view instanceof FrameLayout) {
            if( ((AppCompatActivity) activity).getSupportActionBar() != null)
                ((AppCompatActivity) activity).getSupportActionBar().hide();
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
    public void onShowCustomView(View view, int requestedOrientation, WebChromeClient.CustomViewCallback callback) {
        onShowCustomView(view, callback);
    }

    @Override
    public void onHideCustomView() {
        if( ((AppCompatActivity) activity).getSupportActionBar() != null)
            ((AppCompatActivity) activity).getSupportActionBar().show();
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

