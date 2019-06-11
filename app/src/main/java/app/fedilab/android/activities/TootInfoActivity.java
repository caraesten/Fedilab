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


import android.content.SharedPreferences;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import app.fedilab.android.fragments.DisplayAccountsFragment;
import app.fedilab.android.helper.Helper;
import es.dmoral.toasty.Toasty;
import app.fedilab.android.R;
import app.fedilab.android.asynctasks.RetrieveAccountsAsyncTask;


/**
 * Created by Thomas on 05/11/2018.
 * Toot info activity class
 */

public class TootInfoActivity extends BaseActivity {


    private String toot_id;
    private TabLayout tabLayout;
    private ViewPager mPager;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
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
        setContentView(R.layout.activity_toot_info);
        getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Bundle b = getIntent().getExtras();
        if( getSupportActionBar() != null)
            getSupportActionBar().hide();
        int toot_reblogs_count = 0;
        int toot_favorites_count = 0;
        if( b != null){
            toot_id = b.getString("toot_id", null);
            toot_reblogs_count = b.getInt("toot_reblogs_count", 0);
            toot_favorites_count = b.getInt("toot_favorites_count", 0);
        }
        if( toot_id == null){
            Toasty.error(this, getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
            finish();
        }
        userID = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        tabLayout = findViewById(R.id.tabLayout);
        mPager = findViewById(R.id.viewpager);
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.reblog) + " ("+toot_reblogs_count+")"));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.favourite) + " ("+toot_favorites_count+")"));


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
        PagerAdapter mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                TabLayout.Tab tab = tabLayout.getTabAt(position);
                if( tab != null)
                    tab.select();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Bundle bundle = new Bundle();
            switch (position){
                case 0:
                    DisplayAccountsFragment displayAccountsFragment = new DisplayAccountsFragment();
                    bundle.putSerializable("type", RetrieveAccountsAsyncTask.Type.REBLOGGED);
                    bundle.putString("targetedid", toot_id);
                    displayAccountsFragment.setArguments(bundle);
                    return displayAccountsFragment;
                case 1:
                    displayAccountsFragment = new DisplayAccountsFragment();
                    bundle.putSerializable("type", RetrieveAccountsAsyncTask.Type.FAVOURITED);
                    bundle.putString("targetedid", toot_id);
                    displayAccountsFragment.setArguments(bundle);
                    return displayAccountsFragment;
            }
            return null;
        }


        @Override
        public int getCount() {
            return 2;

        }
    }


}
