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
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import app.fedilab.android.R;
import app.fedilab.android.asynctasks.RetrieveSearchAsyncTask;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Error;
import app.fedilab.android.client.Entities.Status;
import app.fedilab.android.client.Entities.Trends;
import app.fedilab.android.drawers.SearchListAdapter;
import app.fedilab.android.drawers.TrendsAdapter;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.interfaces.OnRetrieveSearchInterface;
import app.fedilab.android.interfaces.OnRetrieveSearchStatusInterface;
import es.dmoral.toasty.Toasty;


/**
 * Created by Thomas on 26/05/2017.
 * Show search results within two tabs: Toots and accounts
 */

public class SearchResultActivity extends BaseActivity implements OnRetrieveSearchInterface, OnRetrieveSearchStatusInterface {


    private String search;
    private ListView lv_search;
    private RelativeLayout loader;
    private boolean forTrends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        forTrends = false;
        setContentView(R.layout.activity_search_result);

        loader = findViewById(R.id.loader);
        lv_search = findViewById(R.id.lv_search);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            search = b.getString("search");
            if (search != null)
                new RetrieveSearchAsyncTask(getApplicationContext(), search.trim(), SearchResultActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            else
                Toasty.error(getApplicationContext(), getString(R.string.toast_error_search), Toast.LENGTH_LONG).show();
        } else {
            Toasty.error(getApplicationContext(), getString(R.string.toast_error_search), Toast.LENGTH_LONG).show();
        }
        if( search.compareTo("fedilab_trend") == 0 ) {
            forTrends = true;
        }
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            View view = inflater.inflate(R.layout.simple_bar, new LinearLayout(getApplicationContext()), false);
            view.setBackground(new ColorDrawable(ContextCompat.getColor(SearchResultActivity.this, R.color.cyanea_primary)));
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
            if( !forTrends) {
                toolbar_title.setText(search);
            }else{
                toolbar_title.setText(getString(R.string.trending_now));
            }
        }
        if( !forTrends) {
            setTitle(search);
        }else{
            setTitle(R.string.trending_now);
        }
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
        if (apiResponse.getError() != null) {
            if (apiResponse.getError().getError() != null) {
                if (apiResponse.getError().getError().length() < 100) {
                    Toasty.error(getApplicationContext(), apiResponse.getError().getError(), Toast.LENGTH_LONG).show();
                } else {
                    Toasty.error(getApplicationContext(), getString(R.string.long_api_error, "\ud83d\ude05"), Toast.LENGTH_LONG).show();
                }
            } else
                Toasty.error(getApplicationContext(), getString(R.string.toast_error), Toast.LENGTH_LONG).show();
            return;
        }
        lv_search.setVisibility(View.VISIBLE);
        if (!forTrends) {
            if (apiResponse.getResults() == null || (apiResponse.getResults().getAccounts().size() == 0 && apiResponse.getResults().getStatuses().size() == 0 && apiResponse.getResults().getHashtags().size() == 0)) {
                RelativeLayout no_result = findViewById(R.id.no_result);
                no_result.setVisibility(View.VISIBLE);
                return;
            }
            List<String> tags = apiResponse.getResults().getHashtags();
            List<Account> accounts = apiResponse.getResults().getAccounts();
            List<Status> statuses = apiResponse.getResults().getStatuses();

            SearchListAdapter searchListAdapter = new SearchListAdapter(SearchResultActivity.this, statuses, accounts, tags);
            lv_search.setAdapter(searchListAdapter);
            searchListAdapter.notifyDataSetChanged();
        } else {
            if (apiResponse.getTrends() == null || apiResponse.getTrends().size() == 0 ) {
                RelativeLayout no_result = findViewById(R.id.no_result);
                no_result.setVisibility(View.VISIBLE);
                return;
            }
            List<Trends> trends = apiResponse.getTrends();
            TrendsAdapter trendsAdapter = new TrendsAdapter(SearchResultActivity.this, trends);
            lv_search.setAdapter(trendsAdapter);
            trendsAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onRetrieveSearchStatus(APIResponse apiResponse, Error error) {
        loader.setVisibility(View.GONE);
        if (apiResponse.getError() != null) {
            if (error.getError().length() < 100) {
                Toasty.error(getApplicationContext(), error.getError(), Toast.LENGTH_LONG).show();
            } else {
                Toasty.error(getApplicationContext(), getString(R.string.long_api_error, "\ud83d\ude05"), Toast.LENGTH_LONG).show();
            }
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
