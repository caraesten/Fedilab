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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;


import java.util.ArrayList;
import java.util.List;

import fr.gouv.etalab.mastodon.asynctasks.PostActionAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveAccountAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveAccountsAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveFeedsAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveRelationshipAsyncTask;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Error;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.drawers.StatusListAdapter;
import fr.gouv.etalab.mastodon.fragments.DisplayAccountsFragment;
import fr.gouv.etalab.mastodon.fragments.DisplayStatusFragment;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnPostActionInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveAccountInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveFeedsAccountInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveRelationshipInterface;
import mastodon.etalab.gouv.fr.mastodon.R;
import fr.gouv.etalab.mastodon.client.Entities.Relationship;


/**
 * Created by Thomas on 01/05/2017.
 * Show account activity class
 */

public class ShowAccountActivity extends AppCompatActivity implements OnPostActionInterface, OnRetrieveAccountInterface, OnRetrieveFeedsAccountInterface, OnRetrieveRelationshipInterface {


    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private List<Status> statuses;
    private StatusListAdapter statusListAdapter;
    private Button account_follow;

    private static final int NUM_PAGES = 3;
    private ViewPager mPager;
    private String accountId;
    private TabLayout tabLayout;
    private BroadcastReceiver hide_header;
    private TextView account_note;
    private String userId;
    private static boolean isHiddingShowing = false;

    public enum action{
        FOLLOW,
        UNFOLLOW,
        UNBLOCK,
        NOTHING
    }

    private action doAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_account);

        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        imageLoader = ImageLoader.getInstance();
        statuses = new ArrayList<>();
        boolean isOnWifi = Helper.isOnWIFI(getApplicationContext());
        int behaviorWithAttachments = sharedpreferences.getInt(Helper.SET_ATTACHMENT_ACTION, Helper.ATTACHMENT_ALWAYS);
        statusListAdapter = new StatusListAdapter(getApplicationContext(), RetrieveFeedsAsyncTask.Type.USER, isOnWifi, behaviorWithAttachments, this.statuses);
        options = new DisplayImageOptions.Builder().displayer(new SimpleBitmapDisplayer()).cacheInMemory(false)
                .cacheOnDisk(true).resetViewBeforeLoading(true).build();
        account_follow = (Button) findViewById(R.id.account_follow);
        account_follow.setEnabled(false);
        Bundle b = getIntent().getExtras();
        if(b != null){
            accountId = b.getString("accountId");
            new RetrieveRelationshipAsyncTask(getApplicationContext(), accountId,ShowAccountActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            new RetrieveAccountAsyncTask(getApplicationContext(),accountId, ShowAccountActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
            if( accountId != null && accountId.equals(userId)){
                account_follow.setVisibility(View.GONE);
            }
        }else{
            Toast.makeText(this,R.string.toast_error_loading_account,Toast.LENGTH_LONG).show();
        }
        if( getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        tabLayout = (TabLayout) findViewById(R.id.account_tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.status)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.following)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.followers)));

        mPager = (ViewPager) findViewById(R.id.account_viewpager);
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


        account_note = (TextView) findViewById(R.id.account_note);
        //Register LocalBroadcast to receive selected accounts after search
        hide_header = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if( !isHiddingShowing ){
                    isHiddingShowing = true;
                    ImageView account_pp = (ImageView) findViewById(R.id.account_pp);
                    TextView account_ac = (TextView) findViewById(R.id.account_ac);
                    boolean hide = intent.getBooleanExtra("hide", false);
                    if( hide){
                        account_follow.setVisibility(View.GONE);
                        account_note.setVisibility(View.GONE);
                        tabLayout.setVisibility(View.GONE);
                        account_ac.setVisibility(View.GONE);
                        account_pp.getLayoutParams().width = (int) Helper.convertDpToPixel(50, context);
                        account_pp.getLayoutParams().height = (int) Helper.convertDpToPixel(50, context);
                    }else {
                        account_follow.setVisibility(View.VISIBLE);
                        if( accountId != null && accountId.equals(userId)){
                            account_follow.setVisibility(View.GONE);
                        }
                        account_ac.setVisibility(View.VISIBLE);
                        account_pp.getLayoutParams().width = (int) Helper.convertDpToPixel(80, context);
                        account_pp.getLayoutParams().height = (int) Helper.convertDpToPixel(80, context);
                        tabLayout.setVisibility(View.VISIBLE);
                        account_note.setVisibility(View.VISIBLE);
                    }
                    account_pp.requestLayout();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isHiddingShowing = false;
                        }
                    }, 1000);
                }

            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(hide_header, new IntentFilter(Helper.HEADER_ACCOUNT));

        //Follow button
        account_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( doAction == action.NOTHING){
                    Toast.makeText(getApplicationContext(), R.string.nothing_to_do, Toast.LENGTH_LONG).show();
                }else if( doAction == action.FOLLOW){
                    account_follow.setEnabled(false);
                    new PostActionAsyncTask(getApplicationContext(), API.StatusAction.FOLLOW, accountId, ShowAccountActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }else if( doAction == action.UNFOLLOW){
                    account_follow.setEnabled(false);
                    new PostActionAsyncTask(getApplicationContext(), API.StatusAction.UNFOLLOW, accountId, ShowAccountActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }else if( doAction == action.UNBLOCK){
                    account_follow.setEnabled(false);
                    new PostActionAsyncTask(getApplicationContext(), API.StatusAction.UNBLOCK, accountId, ShowAccountActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        });
    }


    @Override
    public void onPostAction(int statusCode,API.StatusAction statusAction, String targetedId, Error error) {
        if( error != null){
            final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            boolean show_error_messages = sharedpreferences.getBoolean(Helper.SET_SHOW_ERROR_MESSAGES, true);
            if( show_error_messages)
                Toast.makeText(getApplicationContext(), error.getError(),Toast.LENGTH_LONG).show();
            return;
        }
        Helper.manageMessageStatusCode(getApplicationContext(), statusCode, statusAction);
        new RetrieveRelationshipAsyncTask(getApplicationContext(), accountId,ShowAccountActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
    public void onRetrieveAccount(Account account, Error error) {
        if( error != null){
            final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            boolean show_error_messages = sharedpreferences.getBoolean(Helper.SET_SHOW_ERROR_MESSAGES, true);
            if( show_error_messages)
                Toast.makeText(getApplicationContext(), error.getError(),Toast.LENGTH_LONG).show();
            return;
        }
        ImageView account_pp = (ImageView) findViewById(R.id.account_pp);
        TextView account_dn = (TextView) findViewById(R.id.account_dn);
        TextView account_un = (TextView) findViewById(R.id.account_un);
        TextView account_ac = (TextView) findViewById(R.id.account_ac);

        if( account != null){
            setTitle(account.getAcct());
            account_dn.setText(Helper.shortnameToUnicode(account.getDisplay_name(), true));
            account_un.setText(String.format("@%s", account.getUsername()));
            if( account.getAcct() != null && account.getAcct().equals(account.getUsername()))
                account_ac.setVisibility(View.GONE);
            else
                account_ac.setText(account.getAcct());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                account_note.setText(Html.fromHtml(account.getNote(), Html.FROM_HTML_MODE_COMPACT));
            else
                //noinspection deprecation
                account_note.setText(Html.fromHtml(account.getNote()));
            tabLayout.getTabAt(0).setText(getString(R.string.status_cnt, account.getStatuses_count()));
            tabLayout.getTabAt(1).setText(getString(R.string.following_cnt, account.getFollowing_count()));
            tabLayout.getTabAt(2).setText(getString(R.string.followers_cnt, account.getFollowers_count()));
            imageLoader.displayImage(account.getAvatar(), account_pp, options);
        }
    }

    @Override
    public void onRetrieveFeedsAccount(List<Status> statuses) {
        if( statuses != null) {
            for(Status tmpStatus: statuses){
                this.statuses.add(tmpStatus);
            }
            statusListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(hide_header);
    }

    @Override
    public void onRetrieveRelationship(Relationship relationship, Error error) {

        if( error != null){
            final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            boolean show_error_messages = sharedpreferences.getBoolean(Helper.SET_SHOW_ERROR_MESSAGES, true);
            if( show_error_messages)
                Toast.makeText(getApplicationContext(), error.getError(),Toast.LENGTH_LONG).show();
            return;
        }
        account_follow.setEnabled(true);
        if( relationship.isBlocking()){
            account_follow.setText(R.string.action_unblock);
            doAction = action.UNBLOCK;
        }else if( relationship.isRequested()){
            account_follow.setText(R.string.request_sent);
            account_follow.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
            doAction = action.NOTHING;
        }else if( relationship.isFollowing()){
            account_follow.setText(R.string.action_unfollow);
            doAction = action.UNFOLLOW;
        }else if( !relationship.isFollowing()){
            account_follow.setText(R.string.action_follow);
            doAction = action.FOLLOW;
        }else{
            account_follow.setText(R.string.action_no_action);
            account_follow.getBackground().setColorFilter(ContextCompat.getColor(this, R.color.red_1), PorterDuff.Mode.MULTIPLY);
            doAction = action.NOTHING;
        }



        //The authenticated account is followed by the account
        if( relationship.isFollowed_by()){
            TextView account_followed_by = (TextView) findViewById(R.id.account_followed_by);
            account_followed_by.setVisibility(View.VISIBLE);
        }

    }


    /**
     * Pager adapter for the 3 fragments
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
                    bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.USER);
                    bundle.putString("targetedId", accountId);
                    bundle.putBoolean("hideHeader",true);
                    displayStatusFragment.setArguments(bundle);
                    return displayStatusFragment;
                case 1:
                    DisplayAccountsFragment displayAccountsFragment = new DisplayAccountsFragment();
                    bundle.putSerializable("type", RetrieveAccountsAsyncTask.Type.FOLLOWING);
                    bundle.putString("targetedId", accountId);
                    bundle.putBoolean("hideHeader",true);
                    displayAccountsFragment.setArguments(bundle);
                    return displayAccountsFragment;
                case 2:
                    displayAccountsFragment = new DisplayAccountsFragment();
                    bundle.putSerializable("type", RetrieveAccountsAsyncTask.Type.FOLLOWERS);
                    bundle.putString("targetedId", accountId);
                    bundle.putBoolean("hideHeader",true);
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
