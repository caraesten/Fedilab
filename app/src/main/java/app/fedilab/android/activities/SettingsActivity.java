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

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import app.fedilab.android.R;
import app.fedilab.android.animatemenu.interfaces.Resourceble;
import app.fedilab.android.animatemenu.interfaces.ScreenShotable;
import app.fedilab.android.animatemenu.model.SlideMenuItem;
import app.fedilab.android.animatemenu.util.ViewAnimator;
import app.fedilab.android.asynctasks.UpdateAccountInfoAsyncTask;
import app.fedilab.android.fragments.ContentSettingsFragment;
import app.fedilab.android.helper.Helper;

/**
 * Created by Thomas on 01/07/2019.
 * Settings activity
 */

public class SettingsActivity extends BaseActivity implements ViewAnimator.ViewAnimatorListener {

    private int res = R.drawable.ic_timeline_menu_s;
    private LinearLayout linearLayout;
    private ViewAnimator viewAnimator;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private List<SlideMenuItem> list = new ArrayList<>();
    public static int position = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        switch (theme){
            case Helper.THEME_LIGHT:
                setTheme(R.style.AppTheme_NoActionBar_Fedilab);
                break;
            case Helper.THEME_DARK:
                setTheme(R.style.AppThemeDark_NoActionBar);
                break;
            case Helper.THEME_BLACK:
                setTheme(R.style.AppThemeBlack_NoActionBar);
                break;
            default:
                setTheme(R.style.AppThemeDark_NoActionBar);
        }
        position = 1;
        if( getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null ) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.simple_bar, null);
            actionBar.setCustomView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            ImageView toolbar_close = actionBar.getCustomView().findViewById(R.id.toolbar_close);
            TextView toolbar_title = actionBar.getCustomView().findViewById(R.id.toolbar_title);
            toolbar_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            toolbar_title.setText(R.string.action_about);
            if (theme == Helper.THEME_LIGHT){
                Toolbar toolbar = actionBar.getCustomView().findViewById(R.id.toolbar);
                Helper.colorizeToolbar(toolbar, R.color.black, SettingsActivity.this);
            }
        }
        setContentView(R.layout.activity_settings);
        ContentSettingsFragment contentSettingsFragment = ContentSettingsFragment.newInstance(R.drawable.ic_list_timeline);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, contentSettingsFragment)
                .commit();
        drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.setScrimColor(Color.TRANSPARENT);
        linearLayout = findViewById(R.id.left_drawer);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawers();
            }
        });


        setActionBar();
        createMenuList();
        viewAnimator = new ViewAnimator<>(this, list, contentSettingsFragment, drawerLayout, this);
    }


    private void setActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.close,
                R.string.open_menu
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                linearLayout.removeAllViews();
                linearLayout.invalidate();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                if (slideOffset > 0.6 && linearLayout.getChildCount() == 0)
                    viewAnimator.showMenuContent();
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawerLayout.addDrawerListener(drawerToggle);
    }

    public void setActionBarTitle(String title) {
        Objects.requireNonNull(getSupportActionBar()).setTitle(title);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    private void createMenuList() {

        SlideMenuItem menuItemClose = new SlideMenuItem(ContentSettingsFragment.type.CLOSE, R.drawable.ic_close);
        SlideMenuItem menuItemTimeline = new SlideMenuItem(ContentSettingsFragment.type.TIMELINES, R.drawable.ic_timeline_menu_s);
        SlideMenuItem menuItemNotification = new SlideMenuItem(ContentSettingsFragment.type.NOTIFICATIONS, R.drawable.ic_notifications_menu);
        SlideMenuItem menuItemAdmin = new SlideMenuItem(ContentSettingsFragment.type.ADMIN, R.drawable.ic_security_admin_menu);
        SlideMenuItem menuItemInterface = new SlideMenuItem(ContentSettingsFragment.type.INTERFACE, R.drawable.ic_tablet_menu);
        SlideMenuItem menuItemEdit = new SlideMenuItem(ContentSettingsFragment.type.COMPOSE, R.drawable.ic_edit_black_menu);
        SlideMenuItem menuItemBattery = new SlideMenuItem(ContentSettingsFragment.type.BATTERY, R.drawable.ic_battery_alert_menu);

        list.add(menuItemClose);
        list.add(menuItemTimeline);
        list.add(menuItemNotification);
        list.add(menuItemInterface);
        list.add(menuItemBattery);
        list.add(menuItemEdit);
        if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON || MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA){
            list.add(menuItemAdmin);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }





    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }




    @Override
    public void onResume(){
        super.onResume();

    }


    private ScreenShotable replaceFragment(ScreenShotable screenShotable, ContentSettingsFragment.type type, int topPosition) {
        this.res = this.res == R.drawable.ic_timeline_menu_s ? R.drawable.ic_notifications_menu : R.drawable.ic_timeline_menu_s;
        View view = findViewById(R.id.content_frame);
        int finalRadius = Math.max(view.getWidth(), view.getHeight());
        Animator animator = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            animator = ViewAnimationUtils.createCircularReveal(view, 0, topPosition, 0, finalRadius);
            animator.setInterpolator(new AccelerateInterpolator());
            animator.setDuration(ViewAnimator.CIRCULAR_REVEAL_ANIMATION_DURATION);
            findViewById(R.id.content_overlay).setBackground(new BitmapDrawable(getResources(), screenShotable.getBitmap()));
            animator.start();
        }



        ContentSettingsFragment contentSettingsFragment = ContentSettingsFragment.newInstance(this.res);
        Bundle bundle = new Bundle();
        bundle.putSerializable("type",type);
        contentSettingsFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, contentSettingsFragment).commit();
        return contentSettingsFragment;
    }


    @Override
    public ScreenShotable onSwitch(Resourceble slideMenuItem, ScreenShotable screenShotable, int position) {

        ContentSettingsFragment.type type = slideMenuItem.getType();
        if (ContentSettingsFragment.type.CLOSE.equals(type)) {
            finish();
            return null;
        }
        return replaceFragment(screenShotable, type, position);
    }

    @Override
    public void disableHomeButton() {
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(false);
    }

    @Override
    public void enableHomeButton() {
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        drawerLayout.closeDrawers();
    }

    @Override
    public void addViewToContainer(View view) {
        linearLayout.addView(view);
    }
}
