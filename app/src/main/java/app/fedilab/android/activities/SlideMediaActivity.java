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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;


import com.gw.swipeback.SwipeBackLayout;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import app.fedilab.android.R;
import app.fedilab.android.client.Entities.Attachment;
import app.fedilab.android.client.Entities.Error;
import app.fedilab.android.client.Entities.Status;
import app.fedilab.android.client.HttpsConnection;
import app.fedilab.android.fragments.MediaSliderFragment;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.interfaces.OnDownloadInterface;


/**
 * Created by Thomas on 10/10/2019.
 * Media Activity
 */

public class SlideMediaActivity extends BaseActivity implements OnDownloadInterface {

    private ArrayList<Attachment> attachments;
    private int mediaPosition;
    private ViewPager mPager;
    private long downloadID;
    public SwipeBackLayout mSwipeBackLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        super.onCreate(savedInstanceState);
        switch (theme) {
            case Helper.THEME_LIGHT:
                setTheme(R.style.TransparentLight);
                break;
            case Helper.THEME_DARK:
                setTheme(R.style.TransparentDark);
                break;
            case Helper.THEME_BLACK:
                setTheme(R.style.TransparentBlack);
                break;
            default:
                setTheme(R.style.TransparentDark);
        }
        setContentView(R.layout.activity_media_pager);
        supportPostponeEnterTransition();
        supportStartPostponedEnterTransition();
        SwipeBackLayout swipeBackLayout = findViewById(R.id.swipeBackLayout);
        if (theme == Helper.THEME_LIGHT) {
            swipeBackLayout.setBackgroundResource(R.color.white);
        } else if (theme == Helper.THEME_BLACK) {
            swipeBackLayout.setBackgroundResource(R.color.black);
        } else if (theme == Helper.THEME_DARK) {
            swipeBackLayout.setBackgroundResource(R.color.mastodonC1);
        }


        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            View view = inflater.inflate(R.layout.media_action_bar, new LinearLayout(getApplicationContext()), false);
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
                    if (attachment.getType().toLowerCase().equals("video") || attachment.getType().toLowerCase().equals("audio") || attachment.getType().toLowerCase().equals("gifv") || attachment.getType().toLowerCase().equals("web")) { ;
                        new HttpsConnection(getApplicationContext(), Helper.getLiveInstance(getApplicationContext())).download(attachment.getUrl(), SlideMediaActivity.this);
                    } else {
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
                }
            });
            media_share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = mPager.getCurrentItem();
                    Attachment attachment = attachments.get(position);
                    if (attachment.getType().toLowerCase().equals("video") || attachment.getType().toLowerCase().equals("audio") || attachment.getType().toLowerCase().equals("gifv")) {
                        new HttpsConnection(getApplicationContext(), Helper.getLiveInstance(getApplicationContext())).download(attachment.getUrl(), SlideMediaActivity.this);
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
            if (theme == Helper.THEME_LIGHT) {
                Toolbar toolbar = actionBar.getCustomView().findViewById(R.id.toolbar);
                Helper.colorizeToolbar(toolbar, R.color.black, SlideMediaActivity.this);
            }
        }

        attachments = getIntent().getParcelableArrayListExtra("mediaArray");
        if (getIntent().getExtras() != null)
            mediaPosition = getIntent().getExtras().getInt("position", 1);

        if (attachments == null || attachments.size() == 0)
            finish();
        mPager = findViewById(R.id.media_viewpager);

        PagerAdapter mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        mPager.setCurrentItem(mediaPosition-1);

        registerReceiver(onDownloadComplete,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));



        mSwipeBackLayout = new SwipeBackLayout(SlideMediaActivity.this);
        mSwipeBackLayout.setDirectionMode(SwipeBackLayout.FROM_TOP);
        mSwipeBackLayout.setMaskAlpha(125);
        mSwipeBackLayout.setSwipeBackFactor(0.5f);
        mSwipeBackLayout.setSwipeBackListener(new SwipeBackLayout.OnSwipeBackListener() {
            @Override
            public void onViewPositionChanged(View mView, float swipeBackFraction, float SWIPE_BACK_FACTOR) {
            }

            @Override
            public void onViewSwipeFinished(View mView, boolean isEnd) {
                if (isEnd){
                    finish();
                    overridePendingTransition(0, 0);
                }
            }
        });

        mSwipeBackLayout.attachToActivity(this);

    }


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
                shareIntent.putExtra(Intent.EXTRA_TEXT, R.string.share_with);
                ContentResolver cR = context.getContentResolver();
                shareIntent.setType(cR.getType(uri));
                try {
                    startActivity(shareIntent);
                }catch (Exception e){}
                manager.remove(downloadID);
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onDownloadComplete);
    }

    private MediaSliderFragment mCurrentFragment;


    public MediaSliderFragment getCurrentFragment() {
        return mCurrentFragment;
    }

    @Override
    public void onDownloaded(String saveFilePath, String downloadUrl, Error error) {

    }

    @Override
    public void onUpdateProgress(int progress) {

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
            Log.v(Helper.TAG,"position: " + position);
            Log.v(Helper.TAG,"attachments.get(position): " + attachments.get(position));
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
