/* Copyright 2017 Thomas Schneider
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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveSearchAsyncTask;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Error;
import fr.gouv.etalab.mastodon.client.Entities.Results;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.drawers.SearchListAdapter;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveSearchInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveSearchStatusInterface;

import static fr.gouv.etalab.mastodon.helper.Helper.THEME_LIGHT;


/**
 * Created by Thomas on 26/05/2017.
 * Show search results within two tabs: Toots and accounts
 */

public class SearchResultActivity extends BaseActivity implements OnRetrieveSearchInterface, OnRetrieveSearchStatusInterface {


    private String search;
    private ListView lv_search;
    private RelativeLayout loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
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

        setContentView(R.layout.activity_search_result);

        loader = findViewById(R.id.loader);
        lv_search = findViewById(R.id.lv_search);

        Bundle b = getIntent().getExtras();
        if(b != null){
            search = b.getString("search");
            if( search != null)
                new RetrieveSearchAsyncTask(getApplicationContext(), search.trim(), SearchResultActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            else
                Toasty.error(this,getString(R.string.toast_error_search),Toast.LENGTH_LONG).show();
        }else{
            Toasty.error(this,getString(R.string.toast_error_search),Toast.LENGTH_LONG).show();
        }
        if( getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null ) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
            toolbar_title.setText(search);
            if (theme == THEME_LIGHT){
                Toolbar toolbar = actionBar.getCustomView().findViewById(R.id.toolbar);
                Helper.colorizeToolbar(toolbar, R.color.black, SearchResultActivity.this);
            }
        }
        setTitle(search);
        loader.setVisibility(View.VISIBLE);
        lv_search.setVisibility(View.GONE);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onRetrieveSearch(APIResponse apiResponse) {
        loader.setVisibility(View.GONE);
        if( apiResponse.getError() != null){
            if( apiResponse.getError().getError() != null)
                Toasty.error(getApplicationContext(), apiResponse.getError().getError(),Toast.LENGTH_LONG).show();
            else
                Toasty.error(getApplicationContext(), getString(R.string.toast_error),Toast.LENGTH_LONG).show();
            return;
        }
        if( apiResponse.getResults() == null || ( apiResponse.getResults().getAccounts().size() == 0 &&  apiResponse.getResults().getStatuses().size() == 0 &&  apiResponse.getResults().getHashtags().size() == 0)){
            RelativeLayout no_result = findViewById(R.id.no_result);
            no_result.setVisibility(View.VISIBLE);
            return;
        }
        lv_search.setVisibility(View.VISIBLE);
        List<String> tags =  apiResponse.getResults().getHashtags();
        List<Account> accounts =  apiResponse.getResults().getAccounts();
        List<Status> statuses =  apiResponse.getResults().getStatuses();

        SearchListAdapter searchListAdapter = new SearchListAdapter(SearchResultActivity.this, statuses, accounts, tags);
        lv_search.setAdapter(searchListAdapter);
        searchListAdapter.notifyDataSetChanged();

    }


    @Override
    public void onRetrieveSearchStatus(APIResponse apiResponse, Error error) {
        loader.setVisibility(View.GONE);
        if( apiResponse.getError() != null){
            Toasty.error(getApplicationContext(), error.getError(),Toast.LENGTH_LONG).show();
            return;
        }
        lv_search.setVisibility(View.VISIBLE);
        List<String> tags = new ArrayList<>();
        List<Account> accounts = new ArrayList<>();
        List<Status> statuses = apiResponse.getStatuses();

        SearchListAdapter searchListAdapter = new SearchListAdapter(SearchResultActivity.this, statuses, accounts, tags);
        lv_search.setAdapter(searchListAdapter);
        searchListAdapter.notifyDataSetChanged();
    }
}
