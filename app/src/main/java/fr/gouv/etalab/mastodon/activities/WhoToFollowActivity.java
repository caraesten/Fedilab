/* Copyright 2018 Thomas Schneider
 *
 * This file is a part of Mastalab
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Mastalab is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Mastalab; if not,
 * see <http://www.gnu.org/licenses>. */
package fr.gouv.etalab.mastodon.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.asynctasks.WhoToFollowAsyncTask;
import fr.gouv.etalab.mastodon.drawers.WhoToFollowAccountsAdapter;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveWhoToFollowInterface;

import static fr.gouv.etalab.mastodon.helper.Helper.THEME_BLACK;


/**
 * Created by Thomas on 10/09/2018.
 * Display Who to follow accounts
 */

public class WhoToFollowActivity extends BaseActivity implements OnRetrieveWhoToFollowInterface {



    private String item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        switch (theme){
            case Helper.THEME_LIGHT:
                setTheme(R.style.AppTheme_NoActionBar);
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
        setContentView(R.layout.activity_who_to_follow);
        Toolbar toolbar = findViewById(R.id.toolbar);
        if( theme == THEME_BLACK)
            toolbar.setBackgroundColor(ContextCompat.getColor(WhoToFollowActivity.this, R.color.black));
        setSupportActionBar(toolbar);
        if( getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RelativeLayout mainLoader = findViewById(R.id.loader);
        Bundle b = getIntent().getExtras();
        if(b != null){
            item = b.getString("item");
        }
        String lastDateListNameRefresh = sharedpreferences.getString(Helper.LAST_DATE_LIST_NAME_REFRESH+item, null);
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date( ));
        cal.add(Calendar.MINUTE, -5);
        Date dateAllowed = cal.getTime();
        if( lastDateListNameRefresh == null || Helper.stringToDate(WhoToFollowActivity.this, lastDateListNameRefresh).after(dateAllowed))
            new WhoToFollowAsyncTask(WhoToFollowActivity.this, item, WhoToFollowActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else {
            String lastList = sharedpreferences.getString(Helper.LAST_LIST_NAME + item, null);
            List<String> list = Helper.restoreArrayFromString(lastList);
            displayResults(list);
        }
        mainLoader.setVisibility(View.VISIBLE);
        setTitle(item);
    }


    @Override
    public void onRetrieveWhoToFollow(List<String> list) {
        if( list != null){
            SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(Helper.LAST_DATE_LIST_NAME_REFRESH + item, Helper.dateToString(new Date()));
            editor.putString(Helper.LAST_LIST_NAME + item, Helper.arrayToStringStorage(list));
            editor.apply();
        }
        displayResults(list);
    }

    private void displayResults(List<String> list){

        RelativeLayout mainLoader = findViewById(R.id.loader);
        mainLoader.setVisibility(View.GONE);
        if( list != null){
            ListView lv_list = findViewById(R.id.lv_list);
            WhoToFollowAccountsAdapter whoToFollowAccountsAdapter = new WhoToFollowAccountsAdapter(WhoToFollowActivity.this, list);
            lv_list.setAdapter(whoToFollowAccountsAdapter);
        }else{
            Toast.makeText(WhoToFollowActivity.this, R.string.toast_error, Toast.LENGTH_SHORT).show();
        }
    }
}
