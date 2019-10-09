package app.fedilab.android.fragments;
/* Copyright 2019 Thomas Schneider
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

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.cleveroad.audiovisualization.DbmHandler;
import com.cleveroad.audiovisualization.GLAudioVisualizationView;
import com.github.chrisbanes.photoview.OnMatrixChangedListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.gw.swipeback.SwipeBackLayout;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

import app.fedilab.android.R;
import app.fedilab.android.activities.MediaActivity;
import app.fedilab.android.asynctasks.RetrieveFeedsAsyncTask;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Attachment;
import app.fedilab.android.client.Entities.Error;
import app.fedilab.android.client.Entities.Status;
import app.fedilab.android.client.HttpsConnection;
import app.fedilab.android.client.TLSSocketFactory;
import app.fedilab.android.drawers.ImageAdapter;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.interfaces.OnDownloadInterface;
import app.fedilab.android.interfaces.OnRetrieveFeedsInterface;
import app.fedilab.android.webview.MastalabWebChromeClient;
import app.fedilab.android.webview.MastalabWebViewClient;
import cafe.adriel.androidaudiorecorder.VisualizerHandler;
import es.dmoral.toasty.Toasty;

import static android.content.Context.MODE_PRIVATE;
import static app.fedilab.android.helper.Helper.changeDrawableColor;
import static cafe.adriel.androidaudiorecorder.Util.formatSeconds;
import static cafe.adriel.androidaudiorecorder.Util.getDarkerColor;


/**
 * Created by Thomas on 09/10/2019.
 * Fragment to display media from SlideMediaActivity
 */
public class MediaSliderFragment extends Fragment implements OnDownloadInterface, MediaPlayer.OnCompletionListener {


    private boolean flag_loading;
    private Context context;
    private AsyncTask<Void, Void, Void> asyncTask;
    private String max_id;
    private RelativeLayout mainLoader, nextElementLoader, textviewNoAction;
    private boolean firstLoad;
    private String targetedId;
    private boolean showMediaOnly, showPinned, showReply;
    boolean firstTootsLoaded;
    private SharedPreferences sharedpreferences;
    private ArrayList<Status> statuses;
    private ImageAdapter gridAdaper;
    private RecyclerView gridview;
    private int mediaPosition;



    private WebView webview_video;
    private ImageButton media_save, media_share, media_close;
    private boolean scheduleHidden, scheduleHiddenDescription;
    private SimpleExoPlayer player;
    private boolean isSHaring;
    private String instance;
    private RelativeLayout content_audio;
    private MediaPlayer playeraudio;
    private Timer timer;
    private int playerSecondsElapsed;
    private static final Handler HANDLER = new Handler();
    private String url;

    private RelativeLayout loader;
    private ArrayList<Attachment> attachments;
    private PhotoView imageView;
    private SimpleExoPlayerView videoView;
    private float downX;
    private float downY;
    static final int MIN_DISTANCE = 100;
    private String finalUrlDownload;
    private String preview_url;
    private ImageView prev, next;
    private boolean isControlElementShown = true;
    private Bitmap downloadedImage;
    private File fileVideo;
    private TextView progress;
    private ProgressBar pbar_inf;
    private TextView message_ready;
    private boolean canSwipe;
    private TextView media_description;
    private Attachment attachment;
    private float imageScale = 0;
    private RelativeLayout action_bar_container;
    private VisualizerHandler visualizerHandler;
    private TextView statusView;
    private TextView timerView;
    private ImageButton playView;
    private GLAudioVisualizationView visualizerView;
    public MediaSliderFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_slide_media, container, false);

        context = getContext();
        Bundle bundle = this.getArguments();

        if (bundle != null) {
            mediaPosition = bundle.getInt("position", 1);
            attachment = bundle.getParcelable("attachment");
        }
        Log.v(Helper.TAG,mediaPosition + " -> " + attachment);
        media_description = rootView.findViewById(R.id.media_description);
        message_ready = rootView.findViewById(R.id.message_ready);
        media_save = rootView.findViewById(R.id.media_save);
        media_share = rootView.findViewById(R.id.media_share);
        media_close = rootView.findViewById(R.id.media_close);
        progress = rootView.findViewById(R.id.loader_progress);
        webview_video = rootView.findViewById(R.id.webview_video);
        content_audio = rootView.findViewById(R.id.content_audio);
        media_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isSHaring = false;
                if (attachment.getType().toLowerCase().equals("video") || attachment.getType().toLowerCase().equals("audio") || attachment.getType().toLowerCase().equals("gifv") || attachment.getType().toLowerCase().equals("web")) {
                    if (attachment != null) {
                        progress.setText("0 %");
                        progress.setVisibility(View.VISIBLE);
                        new HttpsConnection(context, instance).download(attachment.getUrl(), MediaSliderFragment.this);
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions((Activity)context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Helper.EXTERNAL_STORAGE_REQUEST_CODE);
                        } else {
                            Helper.manageMoveFileDownload(context, preview_url, finalUrlDownload, downloadedImage, fileVideo, false);
                        }
                    } else {
                        Helper.manageMoveFileDownload(context, preview_url, finalUrlDownload, downloadedImage, fileVideo, false);
                    }
                }
            }
        });
        media_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isSHaring = true;
                if (attachment.getType().toLowerCase().equals("video") || attachment.getType().toLowerCase().equals("audio") || attachment.getType().toLowerCase().equals("gifv")) {
                    if (attachment != null) {
                        progress.setText("0 %");
                        progress.setVisibility(View.VISIBLE);
                        new HttpsConnection(context, instance).download(attachment.getUrl(), MediaSliderFragment.this);
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions((Activity)context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Helper.EXTERNAL_STORAGE_REQUEST_CODE);
                        } else {
                            Helper.manageMoveFileDownload(context, preview_url, finalUrlDownload, downloadedImage, fileVideo, true);
                        }
                    } else {
                        Helper.manageMoveFileDownload(context, preview_url, finalUrlDownload, downloadedImage, fileVideo, true);
                    }
                }
            }
        });
        media_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((Activity)context).finish();
            }
        });
        sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);

        loader = rootView.findViewById(R.id.loader);
        imageView = rootView.findViewById(R.id.media_picture);
        videoView = rootView.findViewById(R.id.media_video);
        prev = rootView.findViewById(R.id.media_prev);
        next = rootView.findViewById(R.id.media_next);
        if (theme == Helper.THEME_BLACK) {
            changeDrawableColor(context, prev, R.color.dark_icon);
            changeDrawableColor(context, next, R.color.dark_icon);
        } else if (theme == Helper.THEME_LIGHT) {
            changeDrawableColor(context, prev, R.color.mastodonC4);
            changeDrawableColor(context, next, R.color.mastodonC4);
        } else {
            changeDrawableColor(context, prev, R.color.white);
            changeDrawableColor(context, next, R.color.white);
        }
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPosition--;
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPosition++;
            }
        });

        url = attachment.getUrl();
        imageView.setOnMatrixChangeListener(new OnMatrixChangedListener() {
            @Override
            public void onMatrixChanged(RectF rect) {
                imageScale = imageView.getScale();
                canSwipe = (imageView.getScale() == 1);
            }
        });
        if (attachments != null && attachments.size() > 1) {
            prev.setVisibility(View.VISIBLE);
            next.setVisibility(View.VISIBLE);
        }
        pbar_inf = rootView.findViewById(R.id.pbar_inf);
        String type = attachment.getType();
        preview_url = attachment.getPreview_url();
        if (type.equals("unknown")) {
            preview_url = attachment.getRemote_url();
            if (preview_url.endsWith(".png") || preview_url.endsWith(".jpg") || preview_url.endsWith(".jpeg") || preview_url.endsWith(".gif")) {
                type = "image";
            } else if (preview_url.endsWith(".mp4") || preview_url.endsWith(".mp3")) {
                type = "video";
            }
            url = attachment.getRemote_url();
            attachment.setType(type);
        }

        switch (type.toLowerCase()) {
            case "image":
                pbar_inf.setScaleY(1f);
                imageView.setVisibility(View.VISIBLE);
                fileVideo = null;
                pbar_inf.setIndeterminate(true);
                loader.setVisibility(View.VISIBLE);
                fileVideo = null;
                if (!url.endsWith(".gif")) {
                    Glide.with(context)
                            .asBitmap()
                            .load(preview_url).into(
                            new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull final Bitmap resource, Transition<? super Bitmap> transition) {
                                    Bitmap imageCompressed = Helper.compressImageIfNeeded(context, resource);
                                    imageView.setImageBitmap(imageCompressed);
                                    Glide.with(context)
                                            .asBitmap()
                                            .load(url).into(
                                            new SimpleTarget<Bitmap>() {
                                                @Override
                                                public void onResourceReady(@NonNull final Bitmap resource, Transition<? super Bitmap> transition) {
                                                    loader.setVisibility(View.GONE);
                                                    Bitmap imageCompressed = Helper.compressImageIfNeeded(context, resource);
                                                    if (imageView.getScale() < 1.1) {
                                                        downloadedImage = resource;
                                                        imageView.setImageBitmap(imageCompressed);
                                                    } else {
                                                        message_ready.setVisibility(View.VISIBLE);
                                                    }
                                                    message_ready.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            downloadedImage = resource;
                                                            imageView.setImageBitmap(imageCompressed);
                                                            message_ready.setVisibility(View.GONE);
                                                        }
                                                    });
                                                }
                                            }
                                    );
                                }
                            }
                    );
                } else {
                    loader.setVisibility(View.GONE);
                    Glide.with(context)
                            .load(url).into(imageView);
                }
                break;
            case "video":
            case "gifv":
                pbar_inf.setIndeterminate(false);
                pbar_inf.setScaleY(3f);
                try {
                    HttpsURLConnection.setDefaultSSLSocketFactory(new TLSSocketFactory(instance));
                } catch (KeyManagementException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                videoView.setVisibility(View.VISIBLE);
                Uri uri = Uri.parse(url);
                DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context,
                        Util.getUserAgent(context, "Fedilab"), null);
                ExtractorMediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(uri);
                player = ExoPlayerFactory.newSimpleInstance(context);
                if (type.toLowerCase().equals("gifv"))
                    player.setRepeatMode(Player.REPEAT_MODE_ONE);
                videoView.setPlayer(player);
                loader.setVisibility(View.GONE);
                player.prepare(videoSource);
                player.setPlayWhenReady(true);
                break;
            case "web":
                loader.setVisibility(View.GONE);
                webview_video = Helper.initializeWebview((Activity)context, R.id.webview_video);
                webview_video.setVisibility(View.VISIBLE);
                FrameLayout webview_container = rootView.findViewById(R.id.main_media_frame);
                final ViewGroup videoLayout = rootView.findViewById(R.id.videoLayout);

                MastalabWebChromeClient mastalabWebChromeClient = new MastalabWebChromeClient((Activity)context, webview_video, webview_container, videoLayout);
                mastalabWebChromeClient.setOnToggledFullscreen(new MastalabWebChromeClient.ToggledFullscreenCallback() {
                    @Override
                    public void toggledFullscreen(boolean fullscreen) {

                        if (fullscreen) {
                            videoLayout.setVisibility(View.VISIBLE);
                            WindowManager.LayoutParams attrs = ((Activity)context).getWindow().getAttributes();
                            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                            attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                            ((Activity)context).getWindow().setAttributes(attrs);
                            ((Activity)context).getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                        } else {
                            WindowManager.LayoutParams attrs = ((Activity)context).getWindow().getAttributes();
                            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                            attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                            ((Activity)context).getWindow().setAttributes(attrs);
                            ((Activity)context).getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                            videoLayout.setVisibility(View.GONE);
                        }
                    }
                });
                webview_video.getSettings().setAllowFileAccess(true);
                webview_video.setWebChromeClient(mastalabWebChromeClient);
                webview_video.getSettings().setDomStorageEnabled(true);
                webview_video.getSettings().setAppCacheEnabled(true);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    webview_video.getSettings().setMediaPlaybackRequiresUserGesture(false);
                }
                webview_video.setWebViewClient(new MastalabWebViewClient((Activity)context));
                webview_video.loadUrl(attachment.getUrl());
                break;
            case "audio":
                loader.setVisibility(View.GONE);
                content_audio.setVisibility(View.VISIBLE);
                int color = getResources().getColor(R.color.mastodonC1);
                visualizerView = new GLAudioVisualizationView.Builder(context)
                        .setLayersCount(1)
                        .setWavesCount(6)
                        .setWavesHeight(R.dimen.aar_wave_height)
                        .setWavesFooterHeight(R.dimen.aar_footer_height)
                        .setBubblesPerLayer(20)
                        .setBubblesSize(R.dimen.aar_bubble_size)
                        .setBubblesRandomizeSize(true)
                        .setBackgroundColor(getDarkerColor(color))
                        .setLayerColors(new int[]{color})
                        .build();

                statusView = rootView.findViewById(R.id.status);
                timerView = rootView.findViewById(R.id.timer);
                playView = rootView.findViewById(R.id.play);
                content_audio.setBackgroundColor(getDarkerColor(color));
                content_audio.addView(visualizerView, 0);
                playView.setVisibility(View.INVISIBLE);
                this.url = attachment.getUrl();

                startPlaying();
                break;
        }
        return rootView;
    }


    private void startPlaying() {
        try {

            playeraudio = new MediaPlayer();
            playeraudio.setDataSource(url);
            playeraudio.prepare();
            playeraudio.start();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                        PackageManager.PERMISSION_GRANTED) {
                    visualizerView.linkTo(DbmHandler.Factory.newVisualizerHandler(context, playeraudio));
                }

            }
            visualizerView.post(new Runnable() {
                @Override
                public void run() {
                    playeraudio.setOnCompletionListener(MediaSliderFragment.this);
                }
            });

            timerView.setText("00:00:00");
            playView.setVisibility(View.VISIBLE);
            statusView.setText(R.string.aar_playing);
            statusView.setVisibility(View.VISIBLE);
            playView.setImageResource(R.drawable.aar_ic_stop);

            playerSecondsElapsed = 0;
            startTimer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void stopPlaying() {
        statusView.setText("");
        statusView.setVisibility(View.INVISIBLE);
        playView.setImageResource(R.drawable.aar_ic_play);

        visualizerView.release();
        if (visualizerHandler != null) {
            visualizerHandler.stop();
        }

        if (playeraudio != null) {
            try {
                playeraudio.pause();
            } catch (Exception ignored) {
            }
        }

        stopTimer();
    }

    private void startTimer() {
        stopTimer();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateTimer();
            }
        }, 0, 1000);
    }

    private void updateTimer() {
        ((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                playerSecondsElapsed++;
                timerView.setText(formatSeconds(playerSecondsElapsed));
            }
        });
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    private boolean isPlaying() {
        try {
            return playeraudio != null && playeraudio.isPlaying();
        } catch (Exception e) {
            return false;
        }
    }



    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);
    }



    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        this.context = context;
    }


    @Override
    public void onPause() {
        super.onPause();
        if (player != null) {
            player.setPlayWhenReady(false);
        }
        if (playeraudio != null) {
            playeraudio.pause();
        }
        try {
            visualizerView.onPause();
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onDestroy() {
        try {
            if (visualizerView != null) {
                visualizerView.release();
            }
            if (player != null) {
                player.release();
            }
            if (playeraudio != null) {
                playeraudio.release();
            }
        } catch (Exception ignored) {
        }
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (player != null) {
            player.setPlayWhenReady(true);
        }
        if (playeraudio != null) {
            playeraudio.start();
        }
        try {
            visualizerView.onResume();
        } catch (Exception e) {
        }
    }



    @Override
    public void onDownloaded(String path, String originUrl, Error error) {
        if (path != null) {
            File response = new File(path);
            File dir = context.getCacheDir();
            File from = new File(dir, response.getName());
            File to = new File(dir, Helper.md5(originUrl) + ".mp4");
            if (from.exists())
                //noinspection ResultOfMethodCallIgnored
                from.renameTo(to);
            fileVideo = to;
            downloadedImage = null;
        }
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity)context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Helper.EXTERNAL_STORAGE_REQUEST_CODE);
            } else {
                Helper.manageMoveFileDownload(context, preview_url, finalUrlDownload, downloadedImage, fileVideo, isSHaring);
            }
        } else {
            Helper.manageMoveFileDownload(context, preview_url, finalUrlDownload, downloadedImage, fileVideo, isSHaring);
        }
        if (progress != null)
            progress.setVisibility(View.GONE);
        if (loader != null)
            loader.setVisibility(View.GONE);
    }


    public boolean canSwipe(){
        return canSwipe;
    }

    @Override
    public void onUpdateProgress(int progressPercentage) {
        progress.setText(String.format("%s%%", String.valueOf(progressPercentage)));
        pbar_inf.setProgress(progressPercentage);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        stopPlaying();
    }
}
