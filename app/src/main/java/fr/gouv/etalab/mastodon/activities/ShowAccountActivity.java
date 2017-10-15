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
import android.content.Intent;
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
import android.support.transition.Visibility;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import fr.gouv.etalab.mastodon.asynctasks.PostActionAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveAccountAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveAccountsAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveFeedsAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveRelationshipAsyncTask;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
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
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveFeedsInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveRelationshipInterface;
import mastodon.etalab.gouv.fr.mastodon.R;
import fr.gouv.etalab.mastodon.client.Entities.Relationship;
import static fr.gouv.etalab.mastodon.helper.Helper.changeDrawableColor;


/**
 * Created by Thomas on 01/05/2017.
 * Show account activity class
 */

public class ShowAccountActivity extends AppCompatActivity implements OnPostActionInterface, OnRetrieveAccountInterface, OnRetrieveFeedsAccountInterface, OnRetrieveRelationshipInterface, OnRetrieveFeedsInterface {


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
    private boolean showMediaOnly, showPinned;
    private ImageView pp_actionBar;
    private ImageView header_edit_profile;
    private List<Status> pins;
    private String accountUrl;
    private int maxScrollSize;
    private boolean avatarShown = true;
    private DisplayStatusFragment displayStatusFragment;

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
            setTheme(R.style.AppTheme_NoActionBar);
        }else {
            setTheme(R.style.AppThemeDark_NoActionBar);
        }
        setContentView(R.layout.activity_show_account);
        setTitle("");
        instanceValue += 1;
        pins = new ArrayList<>();
        Bundle b = getIntent().getExtras();
        account_follow = (FloatingActionButton) findViewById(R.id.account_follow);
        account_follow_request = (TextView) findViewById(R.id.account_follow_request);
        header_edit_profile = (ImageView) findViewById(R.id.header_edit_profile);
        account_follow.setEnabled(false);
        if(b != null){
            accountId = b.getString("accountId");
            new RetrieveRelationshipAsyncTask(getApplicationContext(), accountId,ShowAccountActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            new RetrieveAccountAsyncTask(getApplicationContext(),accountId, ShowAccountActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);

        }else{
            Toast.makeText(this,R.string.toast_error_loading_account,Toast.LENGTH_LONG).show();
        }
        accountUrl = null;
        showMediaOnly = false;
        showPinned = false;
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
        int positionSpinnerTrans = sharedpreferences.getInt(Helper.SET_TRANSLATOR, Helper.TRANS_YANDEX);

        statusListAdapter = new StatusListAdapter(getApplicationContext(), RetrieveFeedsAsyncTask.Type.USER, accountId, isOnWifi, behaviorWithAttachments, positionSpinnerTrans, this.statuses);
        options = new DisplayImageOptions.Builder().displayer(new SimpleBitmapDisplayer()).cacheInMemory(false)
                .cacheOnDisk(true).resetViewBeforeLoading(true).build();



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
                Fragment fragment = (Fragment) mPager.getAdapter().instantiateItem(mPager, tab.getPosition());
                switch (tab.getPosition()){
                    case 0:
                        if( displayStatusFragment != null )
                            displayStatusFragment.scrollToTop();
                        break;
                    case 1:
                    case 2:
                        DisplayAccountsFragment displayAccountsFragment = ((DisplayAccountsFragment) fragment);
                        if (displayAccountsFragment != null)
                            displayAccountsFragment.scrollToTop();
                        break;
                }
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

        header_edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShowAccountActivity.this, EditProfileActivity.class);
                startActivity(intent);
            }
        });





        final ImageButton account_menu = (ImageButton) findViewById(R.id.account_menu);
        account_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(ShowAccountActivity.this, account_menu);
                popup.getMenuInflater()
                        .inflate(R.menu.main_showaccount, popup.getMenu());
                if( !Helper.canPin || !accountId.equals(userId)) {
                    popup.getMenu().findItem(R.id.action_show_pinned).setVisible(false);
                }
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_show_pinned:
                                showPinned = !showPinned;
                                if( tabLayout.getTabAt(0) != null)
                                    //noinspection ConstantConditions
                                    tabLayout.getTabAt(0).select();
                                PagerAdapter mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
                                mPager.setAdapter(mPagerAdapter);
                                return true;
                            case R.id.action_show_media:
                                showMediaOnly = !showMediaOnly;
                                if( tabLayout.getTabAt(0) != null)
                                    //noinspection ConstantConditions
                                    tabLayout.getTabAt(0).select();
                                mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
                                mPager.setAdapter(mPagerAdapter);
                                return true;
                            case R.id.action_open_browser:
                                if( accountUrl != null) {
                                    Intent intent = new Intent(getApplicationContext(), WebviewActivity.class);
                                    Bundle b = new Bundle();
                                    if( !accountUrl.startsWith("http://") && ! accountUrl.startsWith("https://"))
                                        accountUrl = "http://" + accountUrl;
                                    b.putString("url", accountUrl);
                                    intent.putExtras(b);
                                    startActivity(intent);
                                }
                                return true;
                            default:
                                return true;
                        }
                    }
                });
                popup.show();
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
    public void onRetrieveAccount(Account account, Error error) {
        if( error != null){
            final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            boolean show_error_messages = sharedpreferences.getBoolean(Helper.SET_SHOW_ERROR_MESSAGES, true);
            if( show_error_messages)
                Toast.makeText(getApplicationContext(), error.getError(),Toast.LENGTH_LONG).show();
            return;
        }

        accountUrl = account.getUrl();
        final CircleImageView account_pp = (CircleImageView) findViewById(R.id.account_pp);
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
                        ImageView banner_pp = (ImageView) findViewById(R.id.banner_pp);
                        Bitmap workingBitmap = Bitmap.createBitmap(loadedImage);
                        Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
                        Canvas canvas = new Canvas(mutableBitmap);
                        Paint p = new Paint(Color.BLACK);
                        ColorFilter filter = new LightingColorFilter(0xFF7F7F7F, 0x00000000);
                        p.setColorFilter(filter);
                        canvas.drawBitmap(mutableBitmap, new Matrix(), p);
                        BitmapDrawable background = new BitmapDrawable(getResources(), mutableBitmap);
                        banner_pp.setImageDrawable(background);
                    }

                    @Override
                    public void onLoadingFailed(java.lang.String imageUri, android.view.View view, FailReason failReason) {

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


        if( account != null){
            TextView actionbar_title = (TextView) findViewById(R.id.show_account_title);
            if( account.getAcct() != null)
                actionbar_title.setText(account.getAcct());
            pp_actionBar = (ImageView) findViewById(R.id.pp_actionBar);
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
            final AppBarLayout appBar = (AppBarLayout) findViewById(R.id.appBar);
            maxScrollSize = appBar.getTotalScrollRange();


            appBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    LinearLayout toolbarContent = findViewById(R.id.toolbar_content);
                    if( toolbarContent != null) {
                        if (Math.abs(verticalOffset) - appBar.getTotalScrollRange() == 0) {
                            if (toolbarContent.getVisibility() == View.GONE)
                                toolbarContent.setVisibility(View.VISIBLE);
                        } else {
                            if (toolbarContent.getVisibility() == View.VISIBLE)
                                toolbarContent.setVisibility(View.GONE);
                        }
                    }
                    if (maxScrollSize == 0)
                        maxScrollSize = appBarLayout.getTotalScrollRange();

                    int percentage = (Math.abs(verticalOffset)) * 100 / maxScrollSize;

                    if (percentage >= 40 && avatarShown) {
                        avatarShown = false;

                        account_pp.animate()
                                .scaleY(0).scaleX(0)
                                .setDuration(400)
                                .start();
                    }
                    if (percentage <= 40 && !avatarShown) {
                        avatarShown = true;
                        account_pp.animate()
                                .scaleY(1).scaleX(1)
                                .start();
                    }
                }
            });
        }else {
            if(  account != null && account.getAcct() != null)
                setTitle(account.getAcct());
        }
        if( account != null){
            account_dn.setText(Helper.shortnameToUnicode(account.getDisplay_name(), true));
            account_un.setText(String.format("@%s", account.getAcct()));
            SpannableString spannableString = Helper.clickableElementsDescription(ShowAccountActivity.this, account.getNote());
            account_note.setText(spannableString, TextView.BufferType.SPANNABLE);
            account_note.setMovementMethod(LinkMovementMethod.getInstance());
            if (tabLayout.getTabAt(0) != null && tabLayout.getTabAt(1) != null && tabLayout.getTabAt(2) != null) {
                //noinspection ConstantConditions
                tabLayout.getTabAt(0).setText(getString(R.string.status_cnt, account.getStatuses_count()));
                //noinspection ConstantConditions
                tabLayout.getTabAt(1).setText(getString(R.string.following_cnt, account.getFollowing_count()));
                //noinspection ConstantConditions
                tabLayout.getTabAt(2).setText(getString(R.string.followers_cnt, account.getFollowers_count()));

                //Allows to filter by long click
                final LinearLayout tabStrip = (LinearLayout) tabLayout.getChildAt(0);
                tabStrip.getChildAt(0).setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        PopupMenu popup = new PopupMenu(ShowAccountActivity.this, tabStrip.getChildAt(0));
                        popup.getMenuInflater()
                                .inflate(R.menu.option_filter_toots_account, popup.getMenu());
                        Menu menu = popup.getMenu();

                        if( !Helper.canPin || !accountId.equals(userId)) {
                            popup.getMenu().findItem(R.id.action_show_pinned).setVisible(false);
                        }
                        final MenuItem itemShowPined = menu.findItem(R.id.action_show_pinned);
                        final MenuItem itemShowMedia = menu.findItem(R.id.action_show_media);

                        itemShowMedia.setChecked(showMediaOnly);
                        itemShowPined.setChecked(showPinned);
                        popup.setOnDismissListener(new PopupMenu.OnDismissListener() {
                            @Override
                            public void onDismiss(PopupMenu menu) {
                                if( displayStatusFragment != null)
                                    displayStatusFragment.refreshFilter();
                            }
                        });
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem item) {
                                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
                                item.setActionView(new View(getApplicationContext()));
                                item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                                    @Override
                                    public boolean onMenuItemActionExpand(MenuItem item) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onMenuItemActionCollapse(MenuItem item) {
                                        return false;
                                    }
                                });
                                switch (item.getItemId()) {
                                    case R.id.action_show_pinned:
                                        showPinned = !showPinned;
                                        if( tabLayout.getTabAt(0) != null)
                                            //noinspection ConstantConditions
                                            tabLayout.getTabAt(0).select();
                                        PagerAdapter mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
                                        mPager.setAdapter(mPagerAdapter);
                                        break;
                                    case R.id.action_show_media:
                                        showMediaOnly = !showMediaOnly;
                                        if( tabLayout.getTabAt(0) != null)
                                            //noinspection ConstantConditions
                                            tabLayout.getTabAt(0).select();
                                        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
                                        mPager.setAdapter(mPagerAdapter);
                                        break;
                                }
                                itemShowMedia.setChecked(showMediaOnly);
                                itemShowPined.setChecked(showPinned);
                                return false;
                            }
                        });
                        popup.show();
                        return true;
                    }
                });


            }

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
    public void onRetrieveFeeds(APIResponse apiResponse) {
        if( apiResponse.getError() != null){
            final SharedPreferences sharedpreferences = getApplicationContext().getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
            boolean show_error_messages = sharedpreferences.getBoolean(Helper.SET_SHOW_ERROR_MESSAGES, true);
            if( show_error_messages)
                Toast.makeText(getApplicationContext(), apiResponse.getError().getError(),Toast.LENGTH_LONG).show();
            return;
        }

        pins = apiResponse.getStatuses();
        if (pins != null && pins.size() > 0) {
            if( pins.get(0).isPinned()) {

                for (Status pin : pins) {
                    this.statuses.add(pin);
                }
                //noinspection ConstantConditions
                tabLayout.getTabAt(3).setText(getString(R.string.pins_cnt, pins.size()));
                statusListAdapter.notifyDataSetChanged();
            }
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
            header_edit_profile.setVisibility(View.VISIBLE);
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
     * Pager adapter for the 4 fragments
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
                    displayStatusFragment = new DisplayStatusFragment();
                    bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.USER);
                    bundle.putString("targetedId", accountId);
                    bundle.putBoolean("hideHeader",true);
                    bundle.putBoolean("showMediaOnly",showMediaOnly);
                    bundle.putBoolean("showPinned",showPinned);
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
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
            // save the appropriate reference depending on position
            switch (position) {
                case 0:
                    displayStatusFragment = (DisplayStatusFragment) createdFragment;
                    break;
            }
            return createdFragment;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

}
