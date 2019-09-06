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


import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import app.fedilab.android.R;

import app.fedilab.android.client.Entities.MainMenuItem;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.sqlite.MainMenuDAO;
import app.fedilab.android.sqlite.Sqlite;


/**
 * Created by Thomas on 28/08/2019.
 * Hide menu items activity
 */

public class HideItemActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        switch (theme) {
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

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            View view = inflater.inflate(R.layout.simple_bar, new LinearLayout(getApplicationContext()), false);
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
            toolbar_title.setText(R.string.hide_menu_items);
            if (theme == Helper.THEME_LIGHT) {
                Toolbar toolbar = actionBar.getCustomView().findViewById(R.id.toolbar);
                Helper.colorizeToolbar(toolbar, R.color.black, HideItemActivity.this);
            }
        }
        setContentView(R.layout.activity_hide_menu_items);

        SQLiteDatabase db = Sqlite.getInstance(HideItemActivity.this, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        MainMenuItem mainMenu = new MainMenuDAO(getApplicationContext(), db).getMainMenu();
        if (mainMenu == null) {
            mainMenu = new MainMenuItem();
        }
        CheckBox nav_news = findViewById(R.id.nav_news);
        CheckBox nav_list = findViewById(R.id.nav_list);
        CheckBox nav_scheduled = findViewById(R.id.nav_scheduled);
        CheckBox nav_archive = findViewById(R.id.nav_archive);
        CheckBox nav_archive_notifications = findViewById(R.id.nav_archive_notifications);
        CheckBox nav_peertube = findViewById(R.id.nav_peertube);
        CheckBox nav_filters = findViewById(R.id.nav_filters);
        CheckBox nav_who_to_follow = findViewById(R.id.nav_who_to_follow);
        CheckBox nav_blocked = findViewById(R.id.nav_blocked);
        CheckBox nav_muted = findViewById(R.id.nav_muted);
        CheckBox nav_blocked_domains = findViewById(R.id.nav_blocked_domains);
        CheckBox nav_how_to = findViewById(R.id.nav_how_to);
        Button validate = findViewById(R.id.validate);


        nav_news.setChecked(mainMenu.isNav_news());
        nav_list.setChecked(mainMenu.isNav_list());
        nav_scheduled.setChecked(mainMenu.isNav_scheduled());
        nav_archive.setChecked(mainMenu.isNav_archive());
        nav_archive_notifications.setChecked(mainMenu.isNav_archive_notifications());
        nav_peertube.setChecked(mainMenu.isNav_peertube());
        nav_filters.setChecked(mainMenu.isNav_filters());
        nav_who_to_follow.setChecked(mainMenu.isNav_how_to_follow());
        nav_blocked.setChecked(mainMenu.isNav_blocked());
        nav_muted.setChecked(mainMenu.isNav_muted());
        nav_blocked_domains.setChecked(mainMenu.isNav_blocked_domains());
        nav_how_to.setChecked(mainMenu.isNav_howto());


        validate.setOnClickListener(view -> {
            MainMenuItem mainMenuItem = new MainMenuItem();
            mainMenuItem.setNav_news(nav_news.isChecked());
            mainMenuItem.setNav_list(nav_list.isChecked());
            mainMenuItem.setNav_scheduled(nav_scheduled.isChecked());
            mainMenuItem.setNav_archive(nav_archive.isChecked());
            mainMenuItem.setNav_archive_notifications(nav_archive_notifications.isChecked());
            mainMenuItem.setNav_peertube(nav_peertube.isChecked());
            mainMenuItem.setNav_filters(nav_filters.isChecked());
            mainMenuItem.setNav_how_to_follow(nav_who_to_follow.isChecked());
            mainMenuItem.setNav_blocked(nav_blocked.isChecked());
            mainMenuItem.setNav_muted(nav_muted.isChecked());
            mainMenuItem.setNav_blocked_domains(nav_blocked_domains.isChecked());
            mainMenuItem.setNav_howto(nav_how_to.isChecked());
            MainMenuItem mainMenuItem1 = new MainMenuDAO(getApplicationContext(), db).getMainMenu();

            if (mainMenuItem1 != null) {
                new MainMenuDAO(getApplicationContext(), db).updateMenu(mainMenuItem);
            } else {
                new MainMenuDAO(getApplicationContext(), db).insertMenu(mainMenuItem);
            }
            Intent mainActivity = new Intent(HideItemActivity.this, MainActivity.class);
            mainActivity.putExtra(Helper.INTENT_ACTION, Helper.REDRAW_MENU);
            startActivity(mainActivity);
            finish();
        });
    }
}
