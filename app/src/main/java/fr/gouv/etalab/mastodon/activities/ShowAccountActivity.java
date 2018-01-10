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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.UnderlineSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;
import java.util.List;


import fr.gouv.etalab.mastodon.R;
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
import fr.gouv.etalab.mastodon.drawers.StatusListAdapter;
import fr.gouv.etalab.mastodon.fragments.DisplayAccountsFragment;
import fr.gouv.etalab.mastodon.fragments.DisplayStatusFragment;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnPostActionInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveAccountInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveFeedsAccountInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveFeedsInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveRelationshipInterface;
import fr.gouv.etalab.mastodon.client.Entities.Relationship;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import fr.gouv.etalab.mastodon.sqlite.TempMuteDAO;

import static fr.gouv.etalab.mastodon.helper.Helper.THEME_DARK;
import static fr.gouv.etalab.mastodon.helper.Helper.changeDrawableColor;
import static fr.gouv.etalab.mastodon.helper.Helper.withSuffix;


/**
 * Created by Thomas on 01/05/2017.
 * Show account activity class
 */

public class ShowAccountActivity extends BaseActivity implements OnPostActionInterface, OnRetrieveAccountInterface, OnRetrieveFeedsAccountInterface, OnRetrieveRelationshipInterface, OnRetrieveFeedsInterface {


    private List<Status> statuses;
    private StatusListAdapter statusListAdapter;
    private FloatingActionButton account_follow;

    private static final int NUM_PAGES = 3;
    private ViewPager mPager;
    private String accountId;
    private TabLayout tabLayout;
    private TextView account_note, account_follow_request;
    private String userId;
    private Relationship relationship;
    private boolean showMediaOnly, showPinned;
    private ImageView pp_actionBar;
    private FloatingActionButton header_edit_profile;
    private List<Status> pins;
    private String accountUrl;
    private int maxScrollSize;
    private boolean avatarShown = true;
    private DisplayStatusFragment displayStatusFragment;
    private ImageView account_pp;
    private TextView account_dn;
    private TextView account_un;
    private Account account;
    private boolean show_boosts, show_replies;

    public enum action{
        FOLLOW,
        UNFOLLOW,
        UNBLOCK,
        NOTHING
    }

    private action doAction;
    private API.StatusAction doActionAccount;

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
        pins = new ArrayList<>();
        Bundle b = getIntent().getExtras();
        account_follow = findViewById(R.id.account_follow);
        account_follow_request = findViewById(R.id.account_follow_request);
        header_edit_profile = findViewById(R.id.header_edit_profile);
        account_follow.setEnabled(false);
        account_pp = findViewById(R.id.account_pp);
        account_dn = findViewById(R.id.account_dn);
        account_un = findViewById(R.id.account_un);

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
        show_boosts = true;
        show_replies = true;

        statuses = new ArrayList<>();
        boolean isOnWifi = Helper.isOnWIFI(getApplicationContext());
        int behaviorWithAttachments = sharedpreferences.getInt(Helper.SET_ATTACHMENT_ACTION, Helper.ATTACHMENT_ALWAYS);
        int positionSpinnerTrans = sharedpreferences.getInt(Helper.SET_TRANSLATOR, Helper.TRANS_YANDEX);

        statusListAdapter = new StatusListAdapter(getApplicationContext(), RetrieveFeedsAsyncTask.Type.USER, accountId, isOnWifi, behaviorWithAttachments, positionSpinnerTrans, this.statuses);




        tabLayout = findViewById(R.id.account_tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.toots)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.following)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.followers)));

        mPager = findViewById(R.id.account_viewpager);
        mPager.setOffscreenPageLimit(3);
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
                Fragment fragment = null;
                if( mPager.getAdapter() != null)
                    fragment = (Fragment) mPager.getAdapter().instantiateItem(mPager, tab.getPosition());
                switch (tab.getPosition()){
                    case 0:
                        if( displayStatusFragment != null )
                            displayStatusFragment.scrollToTop();
                        break;
                    case 1:
                    case 2:
                        if( fragment != null) {
                            DisplayAccountsFragment displayAccountsFragment = ((DisplayAccountsFragment) fragment);
                            displayAccountsFragment.scrollToTop();
                        }
                        break;
                }
            }
        });

        account_note = findViewById(R.id.account_note);

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


        final ImageButton account_menu = findViewById(R.id.account_menu);
        ImageButton action_more = findViewById(R.id.action_more);
        account_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenu(account_menu);
            }
        });
        action_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenu(account_menu);
            }
        });
    }

    private void showMenu(View account_menu){
        if( account == null)
            return;
        final PopupMenu popup = new PopupMenu(ShowAccountActivity.this, account_menu);
        popup.getMenuInflater()
                .inflate(R.menu.main_showaccount, popup.getMenu());
        if( !Helper.canPin ) {
            popup.getMenu().findItem(R.id.action_show_pinned).setVisible(false);
        }
        final String[] stringArrayConf;
        final boolean isOwner = account.getId().equals(userId);
        if( isOwner) {
            popup.getMenu().findItem(R.id.action_block).setVisible(false);
            popup.getMenu().findItem(R.id.action_mute).setVisible(false);
            popup.getMenu().findItem(R.id.action_mention).setVisible(false);
            stringArrayConf =  getResources().getStringArray(R.array.more_action_owner_confirm);
        }else {
            popup.getMenu().findItem(R.id.action_block).setVisible(true);
            popup.getMenu().findItem(R.id.action_mute).setVisible(true);
            popup.getMenu().findItem(R.id.action_mention).setVisible(true);
            stringArrayConf =  getResources().getStringArray(R.array.more_action_confirm);
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                AlertDialog.Builder builderInner;
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
                    case R.id.action_mention:
                        Intent intent = new Intent(getApplicationContext(), TootActivity.class);
                        Bundle b = new Bundle();
                        b.putString("mentionAccount", account.getAcct());
                        intent.putExtras(b);
                        startActivity(intent);
                        return true;
                    case R.id.action_mute:
                        builderInner = new AlertDialog.Builder(ShowAccountActivity.this);
                        builderInner.setTitle(stringArrayConf[0]);
                        doActionAccount = API.StatusAction.MUTE;
                        break;
                    case R.id.action_block:
                        builderInner = new AlertDialog.Builder(ShowAccountActivity.this);
                        builderInner.setTitle(stringArrayConf[1]);
                        doActionAccount = API.StatusAction.BLOCK;
                        break;
                    default:
                        return true;
                }
                builderInner.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int which) {
                        dialog.dismiss();
                    }
                });
                builderInner.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int which) {
                        new PostActionAsyncTask(getApplicationContext(), doActionAccount, account.getId(), ShowAccountActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        dialog.dismiss();
                    }
                });
                builderInner.show();
                return true;
            }
        });
        popup.show();
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
    public void onRetrieveAccount(final Account account, Error error) {
        if( error != null){
            final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            boolean show_error_messages = sharedpreferences.getBoolean(Helper.SET_SHOW_ERROR_MESSAGES, true);
            if( show_error_messages)
                Toast.makeText(getApplicationContext(), error.getError(),Toast.LENGTH_LONG).show();
            return;
        }
        this.account = account;
        accountUrl = account.getUrl();
        final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if( theme == Helper.THEME_DARK){
            changeDrawableColor(getApplicationContext(), R.drawable.ic_lock_outline,R.color.mastodonC4);
        }else {
            changeDrawableColor(getApplicationContext(), R.drawable.ic_lock_outline,R.color.mastodonC4);
        }
        String urlHeader = account.getHeader();
        if (urlHeader.startsWith("/")) {
            urlHeader = "https://" + Helper.getLiveInstance(ShowAccountActivity.this) + account.getHeader();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && !urlHeader.contains("missing.png")) {

            Glide.with(getApplicationContext())
                    .asBitmap()
                    .load(urlHeader)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            ImageView banner_pp = findViewById(R.id.banner_pp);
                            banner_pp.setImageBitmap(resource);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                banner_pp.setImageAlpha(60);
                            }else {
                                banner_pp.setAlpha(60);
                            }
                        }
                    });

        }
        //Redraws icon for locked accounts
        final float scale = getResources().getDisplayMetrics().density;
        if(account.isLocked()){
            Drawable img = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_lock_outline);
            assert img != null;
            img.setBounds(0,0,(int) (20 * scale + 0.5f),(int) (20 * scale + 0.5f));
            account_dn.setCompoundDrawables( img, null, null, null);
        }else{
            account_dn.setCompoundDrawables( null, null, null, null);
        }


        TextView actionbar_title = findViewById(R.id.show_account_title);
        if( account.getAcct() != null)
            actionbar_title.setText(account.getAcct());
        pp_actionBar = findViewById(R.id.pp_actionBar);
        String url = account.getAvatar();
        if( url.startsWith("/") ){
            url = "https://" + Helper.getLiveInstance(getApplicationContext()) + account.getAvatar();
        }
        Glide.with(getApplicationContext())
                .asBitmap()
                .load(url)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        BitmapDrawable ppDrawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(resource, (int) Helper.convertDpToPixel(25, getApplicationContext()), (int) Helper.convertDpToPixel(25, getApplicationContext()), true));
                        if( pp_actionBar != null){
                            pp_actionBar.setImageDrawable(ppDrawable);
                        }
                    }
                });



        final AppBarLayout appBar = findViewById(R.id.appBar);
        maxScrollSize = appBar.getTotalScrollRange();

        final TextView warning_message = findViewById(R.id.warning_message);
        final SpannableString content = new SpannableString(getString(R.string.disclaimer_full));
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        warning_message.setText(content);
        warning_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), WebviewActivity.class);
                Bundle b = new Bundle();
                if( !accountUrl.startsWith("http://") && ! accountUrl.startsWith("https://"))
                    accountUrl = "http://" + accountUrl;
                b.putString("url", accountUrl);
                intent.putExtras(b);
                startActivity(intent);
            }
        });
        //Timed muted account
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        final SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        final Account authenticatedAccount = new AccountDAO(getApplicationContext(), db).getAccountByID(userId);
        boolean isTimedMute = new TempMuteDAO(getApplicationContext(), db).isTempMuted(authenticatedAccount, account.getId());
        if( isTimedMute){
            String date_mute = new TempMuteDAO(getApplicationContext(), db).getMuteDateByID(authenticatedAccount, account.getId());
            if( date_mute != null) {
                final TextView temp_mute = findViewById(R.id.temp_mute);
                temp_mute.setVisibility(View.VISIBLE);
                SpannableString content_temp_mute = new SpannableString(getString(R.string.timed_mute_profile, account.getAcct(), date_mute));
                content_temp_mute.setSpan(new UnderlineSpan(), 0, content_temp_mute.length(), 0);
                temp_mute.setText(content_temp_mute);
                temp_mute.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new TempMuteDAO(getApplicationContext(), db).remove(authenticatedAccount, account.getId());
                        Toast.makeText(getApplicationContext(), R.string.toast_unmute, Toast.LENGTH_LONG).show();
                        temp_mute.setVisibility(View.GONE);
                    }
                });
            }
        }
        //This account was moved to another one
        if( account.getMoved_to_account() != null){
            TextView account_moved = findViewById(R.id.account_moved);
            account_moved.setVisibility(View.VISIBLE);
            if( theme == THEME_DARK)
                changeDrawableColor(ShowAccountActivity.this, R.drawable.ic_card_travel,R.color.dark_icon);
            else
                changeDrawableColor(ShowAccountActivity.this, R.drawable.ic_card_travel,R.color.black);
            Drawable imgTravel = ContextCompat.getDrawable(ShowAccountActivity.this, R.drawable.ic_card_travel);
            assert imgTravel != null;
            imgTravel.setBounds(0,0,(int) (20 * scale + 0.5f),(int) (20  * scale + 0.5f));
            account_moved.setCompoundDrawables(imgTravel, null, null, null);
            //Retrieves content and make account names clickable
            SpannableString spannableString = account.moveToText(ShowAccountActivity.this);
            account_moved.setText(spannableString, TextView.BufferType.SPANNABLE);
            account_moved.setMovementMethod(LinkMovementMethod.getInstance());
        }

        if( account.getAcct().contains("@") )
            warning_message.setVisibility(View.VISIBLE);
        else
            warning_message.setVisibility(View.GONE);
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
                    warning_message.setVisibility(View.GONE);
                }
                if (percentage <= 40 && !avatarShown) {
                    avatarShown = true;
                    account_pp.animate()
                            .scaleY(1).scaleX(1)
                            .start();
                    if( account.getAcct().contains("@") )
                        warning_message.setVisibility(View.VISIBLE);
                    else
                        warning_message.setVisibility(View.GONE);
                }
            }
        });

        account_dn.setText(Helper.shortnameToUnicode(account.getDisplay_name(), true));
        account_un.setText(String.format("@%s", account.getAcct()));
        SpannableString spannableString = Helper.clickableElementsDescription(ShowAccountActivity.this, account.getNote());
        account_note.setText(spannableString, TextView.BufferType.SPANNABLE);
        account_note.setMovementMethod(LinkMovementMethod.getInstance());
        if (tabLayout.getTabAt(0) != null && tabLayout.getTabAt(1) != null && tabLayout.getTabAt(2) != null) {
            //noinspection ConstantConditions
            tabLayout.getTabAt(0).setText(getString(R.string.status_cnt, withSuffix(account.getStatuses_count())));
            //noinspection ConstantConditions
            tabLayout.getTabAt(1).setText(getString(R.string.following_cnt, withSuffix(account.getFollowing_count())));
            //noinspection ConstantConditions
            tabLayout.getTabAt(2).setText(getString(R.string.followers_cnt, withSuffix(account.getFollowers_count())));

            //Allows to filter by long click
            final LinearLayout tabStrip = (LinearLayout) tabLayout.getChildAt(0);
            tabStrip.getChildAt(0).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    PopupMenu popup = new PopupMenu(ShowAccountActivity.this, tabStrip.getChildAt(0));
                    popup.getMenuInflater()
                            .inflate(R.menu.option_filter_toots_account, popup.getMenu());
                    Menu menu = popup.getMenu();

                    if( !Helper.canPin ) {
                        popup.getMenu().findItem(R.id.action_show_pinned).setVisible(false);
                    }
                    final MenuItem itemShowPined = menu.findItem(R.id.action_show_pinned);
                    final MenuItem itemShowMedia = menu.findItem(R.id.action_show_media);
                    final MenuItem itemShowBoosts = menu.findItem(R.id.action_show_boosts);
                    final MenuItem itemShowReplies = menu.findItem(R.id.action_show_replies);

                    itemShowMedia.setChecked(showMediaOnly);
                    itemShowPined.setChecked(showPinned);
                    itemShowBoosts.setChecked(show_boosts);
                    itemShowReplies.setChecked(show_replies);

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
                                    break;
                                case R.id.action_show_media:
                                    showMediaOnly = !showMediaOnly;
                                    break;
                                case R.id.action_show_boosts:
                                    show_boosts = !show_boosts;

                                    break;
                                case R.id.action_show_replies:
                                    show_replies = !show_replies;
                                    break;
                            }
                            if( tabLayout.getTabAt(0) != null)
                                //noinspection ConstantConditions
                                tabLayout.getTabAt(0).select();
                            PagerAdapter mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
                            mPager.setAdapter(mPagerAdapter);
                            itemShowMedia.setChecked(showMediaOnly);
                            itemShowPined.setChecked(showPinned);
                            itemShowReplies.setChecked(show_replies);
                            itemShowBoosts.setChecked(show_boosts);
                            return false;
                        }
                    });
                    popup.show();
                    return true;
                }
            });


        }
        boolean disableGif = sharedpreferences.getBoolean(Helper.SET_DISABLE_GIF, false);
        if( !disableGif)
            Glide.with(getApplicationContext()).load(account.getAvatar()).apply(RequestOptions.circleCropTransform()).into(account_pp);
        else
            Glide.with(getApplicationContext())
                    .asBitmap()
                    .load(account.getAvatar())
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), Helper.addBorder(resource, account_pp.getContext()));
                            circularBitmapDrawable.setCircular(true);
                            account_pp.setImageDrawable(circularBitmapDrawable);
                        }
                    });


    }



    @Override
    public void onRetrieveFeedsAccount(List<Status> statuses) {
        if( statuses != null) {
            this.statuses.addAll(statuses);
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
                this.statuses.addAll(pins);
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
            TextView account_followed_by = findViewById(R.id.account_followed_by);
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
            header_edit_profile.bringToFront();
        }else if( relationship.isBlocking()){
            account_follow.setImageResource(R.drawable.ic_lock_open);
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
                    bundle.putBoolean("showMediaOnly",showMediaOnly);
                    bundle.putBoolean("showPinned",showPinned);
                    displayStatusFragment.setArguments(bundle);
                    return displayStatusFragment;
                case 1:
                    DisplayAccountsFragment displayAccountsFragment = new DisplayAccountsFragment();
                    bundle.putSerializable("type", RetrieveAccountsAsyncTask.Type.FOLLOWING);
                    bundle.putString("targetedId", accountId);
                    displayAccountsFragment.setArguments(bundle);
                    return displayAccountsFragment;
                case 2:
                    displayAccountsFragment = new DisplayAccountsFragment();
                    bundle.putSerializable("type", RetrieveAccountsAsyncTask.Type.FOLLOWERS);
                    bundle.putString("targetedId", accountId);
                    displayAccountsFragment.setArguments(bundle);
                    return displayAccountsFragment;
            }
            return null;
        }

        @NonNull
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

    public boolean showReplies(){
        return show_replies;
    }

    public boolean showBoosts(){
        return show_boosts;
    }

}
