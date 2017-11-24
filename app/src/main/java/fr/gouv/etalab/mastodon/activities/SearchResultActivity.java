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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

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


/**
 * Created by Thomas on 26/05/2017.
 * Show search results within two tabs: Toots and accounts
 */

public class SearchResultActivity extends AppCompatActivity implements OnRetrieveSearchInterface, OnRetrieveSearchStatusInterface {


    private String search;
    private ListView lv_search;
    private RelativeLayout loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if( theme == Helper.THEME_LIGHT){
            setTheme(R.style.AppTheme);
        }else {
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
                Toast.makeText(this,R.string.toast_error_search,Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(this,R.string.toast_error_search,Toast.LENGTH_LONG).show();
        }
        if( getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
    public void onRetrieveSearch(Results results, Error error) {
        loader.setVisibility(View.GONE);
        if( error != null){
            final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            boolean show_error_messages = sharedpreferences.getBoolean(Helper.SET_SHOW_ERROR_MESSAGES, true);
            if( show_error_messages)
                Toast.makeText(getApplicationContext(), error.getError(),Toast.LENGTH_LONG).show();
            return;
        }
        if( results == null || (results.getAccounts().size() == 0 && results.getStatuses().size() == 0 && results.getHashtags().size() == 0)){
            RelativeLayout no_result = findViewById(R.id.no_result);
            no_result.setVisibility(View.VISIBLE);
            return;
        }
        lv_search.setVisibility(View.VISIBLE);
        List<String> tags = results.getHashtags();
        List<Account> accounts = results.getAccounts();
        List<Status> statuses = results.getStatuses();

        SearchListAdapter searchListAdapter = new SearchListAdapter(SearchResultActivity.this, statuses, accounts, tags);
        lv_search.setAdapter(searchListAdapter);
        searchListAdapter.notifyDataSetChanged();

    }


    @Override
    public void onRetrieveSearchStatus(APIResponse apiResponse, Error error) {
        loader.setVisibility(View.GONE);
        if( apiResponse.getError() != null){
            final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            boolean show_error_messages = sharedpreferences.getBoolean(Helper.SET_SHOW_ERROR_MESSAGES, true);
            if( show_error_messages)
                Toast.makeText(getApplicationContext(), error.getError(),Toast.LENGTH_LONG).show();
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
