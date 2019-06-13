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

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import java.util.HashMap;
import java.util.Map;
import app.fedilab.android.R;
import app.fedilab.android.helper.Helper;

/**
 * Created by Thomas on 13/06/2019.
 * Register activity class
 */

public class MastodonRegisterActivity extends BaseActivity {




    private Map<String, String> createMap() {
        Map<String,String> categories = new HashMap<>();
        categories.put("general", getString(R.string.category_general));
        categories.put("regional", getString(R.string.category_regional));
        categories.put("art", getString(R.string.category_art));
        categories.put("journalism", getString(R.string.category_journalism));
        categories.put("activism", getString(R.string.category_activism));
        categories.put("lgbt", "LGBTQ+");
        categories.put("games", getString(R.string.category_games));
        categories.put("tech", getString(R.string.category_tech));
        categories.put("adult", getString(R.string.category_adult));
        categories.put("furry", getString(R.string.category_furry));
        categories.put("food", getString(R.string.category_food));
        return categories;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


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

        setContentView(R.layout.activity_login);
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
            toolbar_title.setText(R.string.add_account);
            if (theme == Helper.THEME_LIGHT){
                Toolbar toolbar = actionBar.getCustomView().findViewById(R.id.toolbar);
                Helper.colorizeToolbar(toolbar, R.color.black, MastodonRegisterActivity.this);
            }
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

}