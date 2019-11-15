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


import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrPosition;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import app.fedilab.android.R;
import app.fedilab.android.client.Entities.Attachment;
import app.fedilab.android.client.Entities.Error;
import app.fedilab.android.fragments.MediaSliderFragment;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.interfaces.OnDownloadInterface;


/**
 * Created by Thomas on 10/10/2019.
 * Media Activity
 */

public class SlideMediaActivity extends BaseActivity implements OnDownloadInterface {

    int flags;
    private ArrayList<Attachment> attachments;
    private int mediaPosition;
    private ViewPager mPager;
    private long downloadID;
    private boolean fullscreen;
    private SlidrInterface slidrInterface;
    private TextView media_description;
    private Handler handler;
    private boolean swipeEnabled;
    private int minTouch, maxTouch;
    private float startX;
    private float startY;
    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (downloadID == id) {
                DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                assert manager != null;
                Uri uri = manager.getUriForDownloadedFile(downloadID);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_with));
                ContentResolver cR = context.getContentResolver();
                shareIntent.setType(cR.getType(uri));
                try {
                    startActivity(shareIntent);
                } catch (Exception ignored) {
                }
            }
        }
    };
    private MediaSliderFragment mCurrentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        super.onCreate(savedInstanceState);
        switch (theme) {
            case Helper.THEME_LIGHT:
                setTheme(R.style.TransparentLight);
                break;
            case Helper.THEME_BLACK:
                setTheme(R.style.TransparentBlack);
                break;
            default:
                setTheme(R.style.TransparentDark);
        }
        setContentView(R.layout.activity_media_pager);

        fullscreen = false;
        media_description = findViewById(R.id.media_description);
        flags = getWindow().getDecorView().getSystemUiVisibility();

        swipeEnabled = true;
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            //actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(SlideMediaActivity.this, R.color.cyanea_primary)));
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            View view = inflater.inflate(R.layout.media_action_bar, new LinearLayout(getApplicationContext()), false);
            view.setBackground(new ColorDrawable(ContextCompat.getColor(SlideMediaActivity.this, R.color.cyanea_primary)));
            actionBar.setCustomView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            ImageView toolbar_close = actionBar.getCustomView().findViewById(R.id.toolbar_close);
            TextView toolbar_title = actionBar.getCustomView().findViewById(R.id.toolbar_title);
            ImageView media_save = getSupportActionBar().getCustomView().findViewById(R.id.media_save);
            ImageView media_share = getSupportActionBar().getCustomView().findViewById(R.id.media_share);
            toolbar_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            media_save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = mPager.getCurrentItem();
                    Attachment attachment = attachments.get(position);
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (ContextCompat.checkSelfPermission(SlideMediaActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(SlideMediaActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(SlideMediaActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Helper.EXTERNAL_STORAGE_REQUEST_CODE);
                        } else {
                            Helper.manageDownloadsNoPopup(SlideMediaActivity.this, attachment.getUrl());
                            downloadID = -1;
                        }
                    } else {
                        Helper.manageDownloadsNoPopup(SlideMediaActivity.this, attachment.getUrl());
                        downloadID = -1;
                    }
                }
            });
            media_share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = mPager.getCurrentItem();
                    Attachment attachment = attachments.get(position);
                    if (attachment.getType().toLowerCase().equals("video") || attachment.getType().toLowerCase().equals("audio") || attachment.getType().toLowerCase().equals("gifv")) {
                        downloadID = Helper.manageDownloadsNoPopup(SlideMediaActivity.this, attachment.getUrl());
                    } else {
                        if (Build.VERSION.SDK_INT >= 23) {
                            if (ContextCompat.checkSelfPermission(SlideMediaActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(SlideMediaActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(SlideMediaActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Helper.EXTERNAL_STORAGE_REQUEST_CODE);
                            } else {
                                downloadID = Helper.manageDownloadsNoPopup(SlideMediaActivity.this, attachment.getUrl());
                            }
                        } else {
                            downloadID = Helper.manageDownloadsNoPopup(SlideMediaActivity.this, attachment.getUrl());
                        }
                    }
                }
            });
            toolbar_title.setText("");
        }

        attachments = getIntent().getParcelableArrayListExtra("mediaArray");
        if (getIntent().getExtras() != null)
            mediaPosition = getIntent().getExtras().getInt("position", 1);

        if (attachments == null || attachments.size() == 0)
            finish();
        mPager = findViewById(R.id.media_viewpager);
        PagerAdapter mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        mPager.setCurrentItem(mediaPosition - 1);

        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        String description = attachments.get(mediaPosition - 1).getDescription();
        handler = new Handler();
        if (description != null && description.trim().length() > 0 && description.trim().compareTo("null") != 0) {
            media_description.setText(description);
            media_description.setVisibility(View.VISIBLE);

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    media_description.setVisibility(View.GONE);
                }
            }, 3000);

        } else {
            media_description.setVisibility(View.GONE);
        }
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {
            }

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
                String description = attachments.get(position).getDescription();
                if (handler != null) {
                    handler.removeCallbacksAndMessages(null);
                }
                handler = new Handler();
                if (description != null && description.trim().length() > 0 && description.trim().compareTo("null") != 0) {
                    media_description.setText(description);
                    media_description.setVisibility(View.VISIBLE);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            media_description.setVisibility(View.GONE);
                        }
                    }, 3000);

                } else {
                    media_description.setVisibility(View.GONE);
                }
            }
        });

        SlidrConfig config = new SlidrConfig.Builder()
                .sensitivity(1f)
                .scrimColor(Color.BLACK)
                .scrimStartAlpha(0.8f)
                .scrimEndAlpha(0f)
                .position(SlidrPosition.VERTICAL)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .edgeSize(0.18f)
                .build();


        slidrInterface = Slidr.attach(SlideMediaActivity.this, config);
        setFullscreen(true);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenHeight = size.y;
        minTouch = (int) (screenHeight * 0.1);
        maxTouch = (int) (screenHeight * 0.9);

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                float endX = event.getX();
                float endY = event.getY();
                if (endY > minTouch && endY < maxTouch && isAClick(startX, endX, startY, endY)) {
                    setFullscreen(!fullscreen);
                    if (!fullscreen) {
                        String description = attachments.get(mPager.getCurrentItem()).getDescription();
                        if (handler != null) {
                            handler.removeCallbacksAndMessages(null);
                        }
                        handler = new Handler();
                        if (description != null && description.trim().length() > 0 && description.trim().compareTo("null") != 0) {
                            media_description.setText(description);
                            media_description.setVisibility(View.VISIBLE);

                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    media_description.setVisibility(View.GONE);
                                }
                            }, 3000);

                        } else {
                            media_description.setVisibility(View.GONE);
                        }
                    }
                }
                break;
        }
        try {
            return super.dispatchTouchEvent(event);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;

    }

    public void togglePlaying(View v) {

        if (mCurrentFragment != null) {
            mCurrentFragment.togglePlaying(v);
        }
    }

    private boolean isAClick(float startX, float endX, float startY, float endY) {
        float differenceX = Math.abs(startX - endX);
        float differenceY = Math.abs(startY - endY);
        int CLICK_ACTION_THRESHOLD = 200;
        return !(differenceX > CLICK_ACTION_THRESHOLD/* =5 */ || differenceY > CLICK_ACTION_THRESHOLD);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onDownloadComplete);
    }

    public MediaSliderFragment getCurrentFragment() {
        return mCurrentFragment;
    }

    @Override
    public void onDownloaded(String saveFilePath, String downloadUrl, Error error) {

    }

    @Override
    public void onUpdateProgress(int progress) {

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    public void enableSliding(boolean enable) {
        if (enable && !swipeEnabled) {
            slidrInterface.unlock();
            swipeEnabled = true;
        } else if (!enable && swipeEnabled) {
            slidrInterface.lock();
            swipeEnabled = false;
        }
    }

    public boolean getFullScreen() {
        return this.fullscreen;
    }

    public void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
        if (!fullscreen) {
            showSystemUI();

        } else {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
// except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    /**
     * Media Pager
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @NotNull
        @Override
        public Fragment getItem(int position) {
            Bundle bundle = new Bundle();
            MediaSliderFragment mediaSliderFragment = new MediaSliderFragment();
            bundle.putInt("position", position);
            bundle.putParcelable("attachment", attachments.get(position));
            mediaSliderFragment.setArguments(bundle);
            return mediaSliderFragment;
        }

        @Override
        public void setPrimaryItem(@NotNull ViewGroup container, int position, @NotNull Object object) {
            if (getCurrentFragment() != object) {
                mCurrentFragment = ((MediaSliderFragment) object);
            }
            super.setPrimaryItem(container, position, object);
        }

        @Override
        public int getCount() {
            return attachments.size();
        }
    }
}
