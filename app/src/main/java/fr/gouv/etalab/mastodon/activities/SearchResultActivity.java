/* Copyright 2017 Thomas Schneider
 *
 * This file is a part of Mastodon Etalab for mastodon.etalab.gouv.fr
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Mastodon Etalab is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Thomas Schneider; if not,
 * see <http://www.gnu.org/licenses>. */
package fr.gouv.etalab.mastodon.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import fr.gouv.etalab.mastodon.asynctasks.RetrieveSearchAsyncTask;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Results;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.fragments.DisplayAccountsFragment;
import fr.gouv.etalab.mastodon.fragments.DisplayStatusFragment;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveSearchInterface;
import mastodon.etalab.gouv.fr.mastodon.R;


/**
 * Created by Thomas on 26/05/2017.
 * Show search results within two tabs: Toots and accounts
 */

public class SearchResultActivity extends AppCompatActivity implements OnRetrieveSearchInterface {


    private static final int NUM_PAGES = 2;
    private ViewPager mPager;
    private TabLayout tabLayout;
    private boolean searchDone = false;
    private List<Account> accounts;
    private List<Status> statuses;
    private String search;
    private RelativeLayout loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);


        statuses = new ArrayList<>();
        accounts = new ArrayList<>();

        loader = (RelativeLayout) findViewById(R.id.loader);
        tabLayout = (TabLayout) findViewById(R.id.search_tabLayout);
        loader.setVisibility(View.VISIBLE);
        tabLayout.setVisibility(View.GONE);
        Bundle b = getIntent().getExtras();
        if(b != null){
            search = b.getString("search");
            if( search != null)
                new RetrieveSearchAsyncTask(getApplicationContext(), search.trim(), SearchResultActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            else
                Toast.makeText(this,R.string.toast_error_loading_account,Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(this,R.string.toast_error_loading_account,Toast.LENGTH_LONG).show();
        }
        if( getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(search);



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
    public void onRetrieveSearch(Results results) {
        if(  results == null ){
            Toast.makeText(getApplicationContext(),R.string.toast_error,Toast.LENGTH_LONG).show();
            loader.setVisibility(View.GONE);
            tabLayout.setVisibility(View.VISIBLE);
            return;
        }
        accounts = results.getAccounts();
        statuses = results.getStatuses();
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.toots)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.accounts)));
        loader.setVisibility(View.GONE);
        tabLayout.setVisibility(View.VISIBLE);
        mPager = (ViewPager) findViewById(R.id.search_viewpager);
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


    /*
     * Pager adapter for the 2 fragments (status and accounts) coming from search results
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            Bundle bundle = new Bundle();
            switch (position){
                case 0:
                    DisplayStatusFragment displayStatusFragment = new DisplayStatusFragment();
                    bundle.putParcelableArrayList("statuses", new ArrayList<>(statuses));
                    displayStatusFragment.setArguments(bundle);
                    return displayStatusFragment;
                case 1:
                    DisplayAccountsFragment displayAccountsFragment = new DisplayAccountsFragment();
                    bundle.putParcelableArrayList("accounts", new ArrayList<>(accounts));
                    displayAccountsFragment.setArguments(bundle);
                    return displayAccountsFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

}
