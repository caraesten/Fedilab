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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;


import java.io.File;
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
import fr.gouv.etalab.mastodon.client.PatchBaseImageDownloader;
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

import static fr.gouv.etalab.mastodon.helper.Helper.changeDrawableColor;


/**
 * Created by Thomas on 01/05/2017.
 * Show account activity class
 */

public class ShowAccountActivity extends AppCompatActivity implements OnPostActionInterface, OnRetrieveAccountInterface, OnRetrieveFeedsAccountInterface, OnRetrieveRelationshipInterface {


    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private List<Status> statuses;
    private StatusListAdapter statusListAdapter;
    private FloatingActionButton account_follow;

    private static final int NUM_PAGES = 3;
    private ViewPager mPager;
    private String accountId;
    private TabLayout tabLayout;
    private TextView account_note, account_follow_request;
    private String userId;
    private static int instanceValue = 0;
    private Relationship relationship;
    private boolean showMediaOnly;
    private ImageView pp_actionBar;

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
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if( theme == Helper.THEME_LIGHT){
            setTheme(R.style.AppTheme);
        }else {
            setTheme(R.style.AppThemeDark);
        }
        setContentView(R.layout.activity_show_account);
        setTitle("");
        instanceValue += 1;
        Bundle b = getIntent().getExtras();
        account_follow = (FloatingActionButton) findViewById(R.id.account_follow);
        account_follow_request = (TextView) findViewById(R.id.account_follow_request);

        account_follow.setEnabled(false);
        if(b != null){
            accountId = b.getString("accountId");
            new RetrieveRelationshipAsyncTask(getApplicationContext(), accountId,ShowAccountActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            new RetrieveAccountAsyncTask(getApplicationContext(),accountId, ShowAccountActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        }else{
            Toast.makeText(this,R.string.toast_error_loading_account,Toast.LENGTH_LONG).show();
        }
        showMediaOnly = false;
        imageLoader = ImageLoader.getInstance();
        File cacheDir = new File(getCacheDir(), getString(R.string.app_name));
        ImageLoaderConfiguration configImg = new ImageLoaderConfiguration.Builder(this)
                .imageDownloader(new PatchBaseImageDownloader(getApplicationContext()))
                .threadPoolSize(5)
                .threadPriority(Thread.MIN_PRIORITY + 3)
                .denyCacheImageMultipleSizesInMemory()
                .diskCache(new UnlimitedDiskCache(cacheDir))
                .build();
        imageLoader.init(configImg);
        statuses = new ArrayList<>();
        boolean isOnWifi = Helper.isOnWIFI(getApplicationContext());
        int behaviorWithAttachments = sharedpreferences.getInt(Helper.SET_ATTACHMENT_ACTION, Helper.ATTACHMENT_ALWAYS);
        statusListAdapter = new StatusListAdapter(getApplicationContext(), RetrieveFeedsAsyncTask.Type.USER, accountId, isOnWifi, behaviorWithAttachments, this.statuses);
        options = new DisplayImageOptions.Builder().displayer(new RoundedBitmapDisplayer(80)).cacheInMemory(false)
                .cacheOnDisk(true).resetViewBeforeLoading(true).build();


        if( getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        tabLayout = (TabLayout) findViewById(R.id.account_tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.toots)));
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_showaccount, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_show_media:
                showMediaOnly = !showMediaOnly;
                if( showMediaOnly )
                    item.setIcon(R.drawable.ic_clear_all);
                else
                    item.setIcon(R.drawable.ic_perm_media);
                tabLayout.getTabAt(0).select();
                PagerAdapter mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
                mPager.setAdapter(mPagerAdapter);
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
        final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if( theme == Helper.THEME_DARK){
            changeDrawableColor(getApplicationContext(), R.drawable.ic_action_lock_closed,R.color.mastodonC4);
        }else {
            changeDrawableColor(getApplicationContext(), R.drawable.ic_action_lock_closed,R.color.mastodonC4);
        }
        if( account!= null) {
            String urlHeader = account.getHeader();
            if (urlHeader.startsWith("/")) {
                urlHeader = "https://" + Helper.getLiveInstance(ShowAccountActivity.this) + account.getHeader();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && !urlHeader.contains("missing.png")) {

                DisplayImageOptions optionNew = new DisplayImageOptions.Builder().displayer(new SimpleBitmapDisplayer()).cacheInMemory(false)
                        .cacheOnDisk(true).resetViewBeforeLoading(true).build();
                imageLoader.loadImage(urlHeader, optionNew, new SimpleImageLoadingListener() {
                    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        super.onLoadingComplete(imageUri, view, loadedImage);
                        LinearLayout main_header_container = (LinearLayout) findViewById(R.id.main_header_container);
                        Bitmap workingBitmap = Bitmap.createBitmap(loadedImage);
                        Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
                        Canvas canvas = new Canvas(mutableBitmap);
                        Paint p = new Paint(Color.BLACK);
                        ColorFilter filter = new LightingColorFilter(0xFF7F7F7F, 0x00000000);

                        p.setColorFilter(filter);
                        canvas.drawBitmap(mutableBitmap, new Matrix(), p);
                        BitmapDrawable background = new BitmapDrawable(getResources(), mutableBitmap);
                        main_header_container.setBackground(background);
                    }

                    @Override
                    public void onLoadingFailed(java.lang.String imageUri, android.view.View view, FailReason failReason) {
                        LinearLayout main_header_container = (LinearLayout) findViewById(R.id.main_header_container);
                        main_header_container.setBackgroundResource(R.drawable.side_nav_bar);
                    }
                });
            }
        }
        //Redraws icon for locked accounts
        final float scale = getResources().getDisplayMetrics().density;
        if( account != null && account.isLocked()){
            Drawable img = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_action_lock_closed);
            img.setBounds(0,0,(int) (20 * scale + 0.5f),(int) (20 * scale + 0.5f));
            account_dn.setCompoundDrawables( img, null, null, null);
        }else{
            account_dn.setCompoundDrawables( null, null, null, null);
        }

        final ActionBar actionBar = getSupportActionBar();
        LayoutInflater mInflater = LayoutInflater.from(ShowAccountActivity.this);
        if( actionBar != null && account != null){
            View show_account_actionbar = mInflater.inflate(R.layout.showaccount_actionbar, null);
            TextView actionbar_title = (TextView) show_account_actionbar.findViewById(R.id.show_account_title);
            if( account.getAcct() != null)
                actionbar_title.setText(account.getAcct());
            actionBar.setCustomView(show_account_actionbar);
            actionBar.setDisplayShowCustomEnabled(true);
            pp_actionBar = (ImageView) actionBar.getCustomView().findViewById(R.id.pp_actionBar);
            String url = account.getAvatar();
            if( url.startsWith("/") ){
                url = "https://" + Helper.getLiveInstance(getApplicationContext()) + account.getAvatar();
            }
            DisplayImageOptions optionsPP = new DisplayImageOptions.Builder().displayer(new SimpleBitmapDisplayer()).cacheInMemory(false)
                    .cacheOnDisk(true).resetViewBeforeLoading(true).build();
            imageLoader.loadImage(url, optionsPP, new SimpleImageLoadingListener(){
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    super.onLoadingComplete(imageUri, view, loadedImage);
                    BitmapDrawable ppDrawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(loadedImage, (int) Helper.convertDpToPixel(25, getApplicationContext()), (int) Helper.convertDpToPixel(25, getApplicationContext()), true));
                    if( pp_actionBar != null){
                        pp_actionBar.setImageDrawable(ppDrawable);
                    }
                }
                @Override
                public void onLoadingFailed(java.lang.String imageUri, android.view.View view, FailReason failReason){

                }});

            AppBarLayout appBar = (AppBarLayout) findViewById(R.id.appBar);
            appBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    if(verticalOffset == 0){
                        pp_actionBar.setVisibility(View.GONE);
                    }else {
                        pp_actionBar.setVisibility(View.VISIBLE);
                    }

                }
            });
        }else {
            if(  account != null && account.getAcct() != null)
                setTitle(account.getAcct());
        }
        if( account != null){
            account_dn.setText(Helper.shortnameToUnicode(account.getDisplay_name(), true));
            account_un.setText(String.format("@%s", account.getUsername()));
            SpannableString spannableString = Helper.clickableElementsDescription(ShowAccountActivity.this, account.getNote());
            account_note.setText(spannableString, TextView.BufferType.SPANNABLE);
            account_note.setMovementMethod(LinkMovementMethod.getInstance());
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
    public void onRetrieveRelationship(Relationship relationship, Error error) {

        if( error != null){
            final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            boolean show_error_messages = sharedpreferences.getBoolean(Helper.SET_SHOW_ERROR_MESSAGES, true);
            if( show_error_messages)
                Toast.makeText(getApplicationContext(), error.getError(),Toast.LENGTH_LONG).show();
            return;
        }
        this.relationship = relationship;
        manageButtonVisibility();


        //The authenticated account is followed by the account
        if( relationship.isFollowed_by()){
            TextView account_followed_by = (TextView) findViewById(R.id.account_followed_by);
            account_followed_by.setVisibility(View.VISIBLE);
        }

    }

    //Manages the visibility of the button
    private void manageButtonVisibility(){
        if( relationship == null)
            return;
        account_follow.setEnabled(true);
        if( accountId != null && accountId.equals(userId)){
            account_follow.setVisibility(View.GONE);
        }else if( relationship.isBlocking()){
            account_follow.setImageResource(R.drawable.ic_unlock_alt);
            doAction = action.UNBLOCK;
            account_follow.setVisibility(View.VISIBLE);
        }else if( relationship.isRequested()){
            account_follow_request.setVisibility(View.VISIBLE);
            account_follow.setVisibility(View.GONE);
            doAction = action.NOTHING;
        }else if( relationship.isFollowing()){
            account_follow.setImageResource(R.drawable.ic_user_times);
            doAction = action.UNFOLLOW;
            account_follow.setVisibility(View.VISIBLE);
        }else if( !relationship.isFollowing()){
            account_follow.setImageResource(R.drawable.ic_user_plus);
            doAction = action.FOLLOW;
            account_follow.setVisibility(View.VISIBLE);
        }else{
            account_follow.setVisibility(View.GONE);
            doAction = action.NOTHING;
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
                    bundle.putBoolean("showMediaOnly",showMediaOnly);
                    bundle.putString("hideHeaderValue",String.valueOf(instanceValue));
                    displayStatusFragment.setArguments(bundle);
                    return displayStatusFragment;
                case 1:
                    DisplayAccountsFragment displayAccountsFragment = new DisplayAccountsFragment();
                    bundle.putSerializable("type", RetrieveAccountsAsyncTask.Type.FOLLOWING);
                    bundle.putString("targetedId", accountId);
                    bundle.putBoolean("hideHeader",true);
                    bundle.putString("hideHeaderValue",String.valueOf(instanceValue));
                    displayAccountsFragment.setArguments(bundle);
                    return displayAccountsFragment;
                case 2:
                    displayAccountsFragment = new DisplayAccountsFragment();
                    bundle.putSerializable("type", RetrieveAccountsAsyncTask.Type.FOLLOWERS);
                    bundle.putString("targetedId", accountId);
                    bundle.putBoolean("hideHeader",true);
                    bundle.putString("hideHeaderValue",String.valueOf(instanceValue));
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
