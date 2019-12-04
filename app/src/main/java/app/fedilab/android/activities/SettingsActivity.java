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
package app.fedilab.android.activities;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import org.jetbrains.annotations.NotNull;

import app.fedilab.android.R;
import app.fedilab.android.fragments.ColorSettingsFragment;
import app.fedilab.android.fragments.ContentSettingsFragment;
import app.fedilab.android.helper.Helper;

/**
 * Created by Thomas on 01/07/2019.
 * Settings activity
 */

public class SettingsActivity extends BaseActivity {

    public static boolean needRestart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        needRestart = false;
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        switch (theme) {
            case Helper.THEME_LIGHT:
                setTheme(R.style.AppTheme_Fedilab);
                break;
            case Helper.THEME_BLACK:
                setTheme(R.style.AppThemeBlack);
                break;
            default:
                setTheme(R.style.AppThemeDark);
        }
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {

            LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            View view = inflater.inflate(R.layout.simple_bar, new LinearLayout(getApplicationContext()), false);
            view.setBackground(new ColorDrawable(ContextCompat.getColor(SettingsActivity.this, R.color.cyanea_primary)));
            actionBar.setCustomView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            ImageView toolbar_close = actionBar.getCustomView().findViewById(R.id.toolbar_close);
            TextView toolbar_title = actionBar.getCustomView().findViewById(R.id.toolbar_title);
            toolbar_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (needRestart) {
                        showDialog();
                    } else {
                        finish();
                    }

                }
            });
            toolbar_title.setText(R.string.settings);
        }
        setContentView(R.layout.activity_settings);


        ViewPager mPager = findViewById(R.id.settings_viewpager);
        TabLayout tabLayout = findViewById(R.id.settings_tablayout);
        tabLayout.setBackgroundColor(ContextCompat.getColor(SettingsActivity.this, R.color.cyanea_primary));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.settings_category_label_timelines)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.notifications)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.settings_category_label_interface)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.compose)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.action_privacy)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.theming)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.hide_menu_items)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.administration)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.languages)));
        PagerAdapter mPagerAdapter = new SettingsPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                TabLayout.Tab tab = tabLayout.getTabAt(position);
                if (tab != null)
                    tab.select();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

        });

    }

    private void showDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SettingsActivity.this);
        dialogBuilder.setMessage(R.string.restart_message);
        dialogBuilder.setTitle(R.string.apply_changes);
        dialogBuilder.setPositiveButton(R.string.restart, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent mStartActivity = new Intent(getApplicationContext(), MainActivity.class);
                int mPendingIntentId = 123456;
                PendingIntent mPendingIntent = PendingIntent.getActivity(getApplicationContext(), mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                assert mgr != null;
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                System.exit(0);
            }
        });
        dialogBuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();

        needRestart = false;
    }

    @Override
    public void onBackPressed() {
        if (needRestart) {
            showDialog();
        } else {
            super.onBackPressed();
        }
    }

    private class SettingsPagerAdapter extends FragmentStatePagerAdapter {

        SettingsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @NotNull
        @Override
        public Fragment getItem(int position) {
            Bundle bundle = new Bundle();
            ContentSettingsFragment.type typeOfSettings;
            switch (position) {
                case 1:
                    typeOfSettings = ContentSettingsFragment.type.NOTIFICATIONS;
                    break;
                case 2:
                    typeOfSettings = ContentSettingsFragment.type.INTERFACE;
                    break;
                case 3:
                    typeOfSettings = ContentSettingsFragment.type.COMPOSE;
                    break;
                case 4:
                    typeOfSettings = ContentSettingsFragment.type.PRIVACY;
                    break;
                case 5:
                    return new ColorSettingsFragment();
                case 6:
                    typeOfSettings = ContentSettingsFragment.type.MENU;
                    break;
                case 7:
                    typeOfSettings = ContentSettingsFragment.type.ADMIN;
                    break;
                case 8:
                    typeOfSettings = ContentSettingsFragment.type.LANGUAGE;
                    break;
                default:
                    typeOfSettings = ContentSettingsFragment.type.TIMELINES;

            }
            ContentSettingsFragment contentSettingsFragment = new ContentSettingsFragment();
            bundle.putSerializable("typeOfSettings", typeOfSettings);
            contentSettingsFragment.setArguments(bundle);
            return contentSettingsFragment;
        }


        @Override
        public int getCount() {
            return 9;
        }
    }
}
