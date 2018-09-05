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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveInstanceAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveMetaDataAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveRemoteDataAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.UpdateAccountInfoByIDAsyncTask;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Notification;
import fr.gouv.etalab.mastodon.client.Entities.Results;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.client.Entities.Version;
import fr.gouv.etalab.mastodon.fragments.DisplayAccountsFragment;
import fr.gouv.etalab.mastodon.fragments.DisplayBookmarksFragment;
import fr.gouv.etalab.mastodon.fragments.DisplayDraftsFragment;
import fr.gouv.etalab.mastodon.fragments.DisplayFiltersFragment;
import fr.gouv.etalab.mastodon.fragments.DisplayFollowRequestSentFragment;
import fr.gouv.etalab.mastodon.fragments.DisplayListsFragment;
import fr.gouv.etalab.mastodon.fragments.DisplayNotificationsFragment;
import fr.gouv.etalab.mastodon.fragments.DisplayScheduledTootsFragment;
import fr.gouv.etalab.mastodon.fragments.DisplaySearchFragment;
import fr.gouv.etalab.mastodon.helper.CrossActions;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveInstanceInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveMetaDataInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveRemoteAccountInterface;
import fr.gouv.etalab.mastodon.interfaces.OnUpdateAccountInfoInterface;
import fr.gouv.etalab.mastodon.services.BackupStatusService;
import fr.gouv.etalab.mastodon.services.LiveNotificationService;
import fr.gouv.etalab.mastodon.sqlite.SearchDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveAccountsAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveFeedsAsyncTask;
import fr.gouv.etalab.mastodon.fragments.DisplayStatusFragment;
import fr.gouv.etalab.mastodon.fragments.TabLayoutSettingsFragment;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import static fr.gouv.etalab.mastodon.helper.Helper.ADD_USER_INTENT;
import static fr.gouv.etalab.mastodon.helper.Helper.BACKUP_INTENT;
import static fr.gouv.etalab.mastodon.helper.Helper.CHANGE_THEME_INTENT;
import static fr.gouv.etalab.mastodon.helper.Helper.CHANGE_USER_INTENT;
import static fr.gouv.etalab.mastodon.helper.Helper.EXTERNAL_STORAGE_REQUEST_CODE;
import static fr.gouv.etalab.mastodon.helper.Helper.HOME_TIMELINE_INTENT;
import static fr.gouv.etalab.mastodon.helper.Helper.INTENT_ACTION;
import static fr.gouv.etalab.mastodon.helper.Helper.INTENT_TARGETED_ACCOUNT;
import static fr.gouv.etalab.mastodon.helper.Helper.NOTIFICATION_INTENT;
import static fr.gouv.etalab.mastodon.helper.Helper.PREF_KEY_ID;
import static fr.gouv.etalab.mastodon.helper.Helper.SEARCH_REMOTE;
import static fr.gouv.etalab.mastodon.helper.Helper.SEARCH_URL;
import static fr.gouv.etalab.mastodon.helper.Helper.THEME_BLACK;
import static fr.gouv.etalab.mastodon.helper.Helper.changeDrawableColor;
import static fr.gouv.etalab.mastodon.helper.Helper.changeUser;
import static fr.gouv.etalab.mastodon.helper.Helper.menuAccounts;
import static fr.gouv.etalab.mastodon.helper.Helper.unCheckAllMenuItems;
import static fr.gouv.etalab.mastodon.helper.Helper.updateHeaderAccountInfo;
import android.support.v4.app.FragmentStatePagerAdapter;


public abstract class BaseMainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnUpdateAccountInfoInterface, OnRetrieveMetaDataInterface, OnRetrieveInstanceInterface, OnRetrieveRemoteAccountInterface {

    private FloatingActionButton toot, delete_all, add_new;
    private HashMap<String, String> tagTile = new HashMap<>();
    private HashMap<String, Integer> tagItem = new HashMap<>();
    private TextView toolbarTitle;
    private SearchView toolbar_search;
    private View headerLayout;
    public static String currentLocale;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private RelativeLayout main_app_container;
    private Stack<Integer> stackBack = new Stack<>();

    private DisplayStatusFragment homeFragment, federatedFragment, localFragment;
    private DisplayNotificationsFragment notificationsFragment;
    private static final int ERROR_DIALOG_REQUEST_CODE = 97;
    private static BroadcastReceiver receive_data, receive_federated_data, receive_local_data;
    private boolean display_local, display_global;
    public static int countNewStatus = 0;
    public static int countNewNotifications = 0;
    private String userIdService;
    public static String lastHomeId = null, lastNotificationId = null;
    boolean notif_follow, notif_add, notif_mention, notif_share, show_boosts, show_replies;
    String show_filtered;
    private AppBarLayout appBar;
    private static boolean activityPaused;
    private String bookmark;
    private String userId;
    private String instance;
    public int countPage;
    private PagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);


        final int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
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
        setContentView(R.layout.activity_main);

        display_local = sharedpreferences.getBoolean(Helper.SET_DISPLAY_LOCAL, true);
        display_global = sharedpreferences.getBoolean(Helper.SET_DISPLAY_GLOBAL, true);

        //Test if user is still log in
        if( ! Helper.isLoggedIn(getApplicationContext())) {
            //It is not, the user is redirected to the login page
            Intent myIntent = new Intent(BaseMainActivity.this, LoginActivity.class);
            startActivity(myIntent);
            finish();
            return;
        }

        rateThisApp();

        SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        Helper.canPin = false;
        Helper.fillMapEmoji(getApplicationContext());
        //Here, the user is authenticated
        appBar = findViewById(R.id.appBar);
        Toolbar toolbar = findViewById(R.id.toolbar);
        if( theme == THEME_BLACK)
            toolbar.setBackgroundColor(ContextCompat.getColor(BaseMainActivity.this, R.color.black));
        setSupportActionBar(toolbar);
        toolbarTitle  = toolbar.findViewById(R.id.toolbar_title);
        toolbar_search = toolbar.findViewById(R.id.toolbar_search);
        tabLayout = findViewById(R.id.tabLayout);
        TabLayout.Tab tabHome = tabLayout.newTab();
        TabLayout.Tab tabNotif = tabLayout.newTab();
        TabLayout.Tab tabLocal = tabLayout.newTab();
        TabLayout.Tab tabPublic = tabLayout.newTab();
        tabHome.setCustomView(R.layout.tab_badge);
        tabNotif.setCustomView(R.layout.tab_badge);
        tabLocal.setCustomView(R.layout.tab_badge);
        tabPublic.setCustomView(R.layout.tab_badge);

        @SuppressWarnings("ConstantConditions") @SuppressLint("CutPasteId")
        ImageView iconHome = tabHome.getCustomView().findViewById(R.id.tab_icon);
        iconHome.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_text), PorterDuff.Mode.SRC_IN);
        iconHome.setImageResource(R.drawable.ic_home);

        if( theme == THEME_BLACK)
            iconHome.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_icon), PorterDuff.Mode.SRC_IN);
        else
            iconHome.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.mastodonC4), PorterDuff.Mode.SRC_IN);


        @SuppressWarnings("ConstantConditions") @SuppressLint("CutPasteId")
        ImageView iconNotif = tabNotif.getCustomView().findViewById(R.id.tab_icon);
        iconNotif.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_text), PorterDuff.Mode.SRC_IN);
        iconNotif.setImageResource(R.drawable.ic_notifications);


        @SuppressWarnings("ConstantConditions") @SuppressLint("CutPasteId")
        ImageView iconLocal = tabLocal.getCustomView().findViewById(R.id.tab_icon);
        iconLocal.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_text), PorterDuff.Mode.SRC_IN);
        iconLocal.setImageResource(R.drawable.ic_people);

        @SuppressWarnings("ConstantConditions") @SuppressLint("CutPasteId")
        ImageView iconGlobal = tabPublic.getCustomView().findViewById(R.id.tab_icon);
        iconGlobal.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_text), PorterDuff.Mode.SRC_IN);
        iconGlobal.setImageResource(R.drawable.ic_public);

       FloatingActionButton federatedTimelines = findViewById(R.id.federated_timeline);

        federatedTimelines.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), InstanceFederatedActivity.class);
                startActivity(intent);
            }
        });

        changeDrawableColor(getApplicationContext(), R.drawable.ic_home,R.color.dark_text);
        changeDrawableColor(getApplicationContext(), R.drawable.ic_notifications,R.color.dark_text);
        changeDrawableColor(getApplicationContext(), R.drawable.ic_people,R.color.dark_text);
        changeDrawableColor(getApplicationContext(), R.drawable.ic_public,R.color.dark_text);
        startSreaming();
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);


        tabLayout.addTab(tabHome);
        tabLayout.addTab(tabNotif);
        if( display_local)
            tabLayout.addTab(tabLocal);
        if( display_global)
            tabLayout.addTab(tabPublic);


        //Display filter for notification when long pressing the tab
        final LinearLayout tabStrip = (LinearLayout) tabLayout.getChildAt(0);
        tabStrip.getChildAt(1).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //Only shown if the tab has focus
                if( notificationsFragment != null && notificationsFragment.getUserVisibleHint()){
                    PopupMenu popup = new PopupMenu(BaseMainActivity.this, tabStrip.getChildAt(1));
                    popup.getMenuInflater()
                            .inflate(R.menu.option_filter_notifications, popup.getMenu());
                    Menu menu = popup.getMenu();
                    final MenuItem itemFavourite = menu.findItem(R.id.action_favorite);
                    final MenuItem itemFollow = menu.findItem(R.id.action_follow);
                    final MenuItem itemMention = menu.findItem(R.id.action_mention);
                    final MenuItem itemBoost = menu.findItem(R.id.action_boost);
                    notif_follow = sharedpreferences.getBoolean(Helper.SET_NOTIF_FOLLOW_FILTER, true);
                    notif_add = sharedpreferences.getBoolean(Helper.SET_NOTIF_ADD_FILTER, true);
                    notif_mention = sharedpreferences.getBoolean(Helper.SET_NOTIF_MENTION_FILTER, true);
                    notif_share = sharedpreferences.getBoolean(Helper.SET_NOTIF_SHARE_FILTER, true);
                    itemFavourite.setChecked(notif_add);
                    itemFollow.setChecked(notif_follow);
                    itemMention.setChecked(notif_mention);
                    itemBoost.setChecked(notif_share);
                    popup.setOnDismissListener(new PopupMenu.OnDismissListener() {
                        @Override
                        public void onDismiss(PopupMenu menu) {
                            if( notificationsFragment != null)
                                notificationsFragment.refreshAll();
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
                                case R.id.action_favorite:
                                    SharedPreferences.Editor editor = sharedpreferences.edit();
                                    notif_add = !notif_add;
                                    editor.putBoolean(Helper.SET_NOTIF_ADD_FILTER, notif_add);
                                    itemFavourite.setChecked(notif_add);
                                    editor.apply();
                                    break;
                                case R.id.action_follow:
                                    editor = sharedpreferences.edit();
                                    notif_follow = !notif_follow;
                                    editor.putBoolean(Helper.SET_NOTIF_FOLLOW_FILTER, notif_follow);
                                    itemFollow.setChecked(notif_follow);
                                    editor.apply();
                                    break;
                                case R.id.action_mention:
                                    editor = sharedpreferences.edit();
                                    notif_mention = !notif_mention;
                                    editor.putBoolean(Helper.SET_NOTIF_MENTION_FILTER, notif_mention);
                                    itemMention.setChecked(notif_mention);
                                    editor.apply();
                                    break;
                                case R.id.action_boost:
                                    editor = sharedpreferences.edit();
                                    notif_share = !notif_share;
                                    editor.putBoolean(Helper.SET_NOTIF_SHARE_FILTER, notif_share);
                                    itemBoost.setChecked(notif_share);
                                    editor.apply();
                                    break;
                            }
                            return false;
                        }
                    });
                    popup.show();
                }
                return true;
            }
        });


        tabStrip.getChildAt(0).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return manageFilters(tabStrip, sharedpreferences);
            }
        });

        if( tabStrip.getChildCount() > 2)
        tabStrip.getChildAt(2).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return manageFilters(tabStrip, sharedpreferences);
            }
        });
        if( tabStrip.getChildCount() == 4)
        tabStrip.getChildAt(3).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return manageFilters(tabStrip, sharedpreferences);
            }
        });


        viewPager = findViewById(R.id.viewpager);
        countPage = 2;
        if( sharedpreferences.getBoolean(Helper.SET_DISPLAY_LOCAL, true))
            countPage++;
        if( sharedpreferences.getBoolean(Helper.SET_DISPLAY_GLOBAL, true))
            countPage++;
        viewPager.setOffscreenPageLimit(countPage);
        main_app_container = findViewById(R.id.main_app_container);
        adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                if (stackBack.empty())
                    stackBack.push(0);
                if (stackBack.contains(tab.getPosition())) {
                    stackBack.remove(stackBack.indexOf(tab.getPosition()));
                    stackBack.push(tab.getPosition());
                } else {
                    stackBack.push(tab.getPosition());
                }
                main_app_container.setVisibility(View.GONE);
                viewPager.setVisibility(View.VISIBLE);
                Helper.switchLayout(BaseMainActivity.this);
                if( tab.getPosition() != 1 )
                    toot.setVisibility(View.VISIBLE);
                else
                    toot.setVisibility(View.GONE);
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                if( tab.getCustomView() != null) {
                    ImageView icon = tab.getCustomView().findViewById(R.id.tab_icon);
                    if( icon != null)
                        if( theme == THEME_BLACK)
                            icon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_icon), PorterDuff.Mode.SRC_IN);
                        else
                            icon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.mastodonC4), PorterDuff.Mode.SRC_IN);

                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if( tab.getCustomView() != null) {
                    ImageView icon = tab.getCustomView().findViewById(R.id.tab_icon);
                    if( icon != null)
                        icon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.white), PorterDuff.Mode.SRC_IN);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if( viewPager.getVisibility() == View.GONE){
                    viewPager.setVisibility(View.VISIBLE);
                    Helper.switchLayout(BaseMainActivity.this);
                    main_app_container.setVisibility(View.GONE);
                    DrawerLayout drawer = findViewById(R.id.drawer_layout);
                    drawer.closeDrawer(GravityCompat.START);
                }
                if( tab.getPosition() != 1 )
                    toot.setVisibility(View.VISIBLE);
                else
                    toot.setVisibility(View.GONE);
                if( viewPager.getAdapter() != null) {
                    Fragment fragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, tab.getPosition());
                    switch (tab.getPosition()) {
                        case 0:
                            DisplayStatusFragment displayStatusFragment = ((DisplayStatusFragment) fragment);
                            countNewStatus = 0;
                            updateHomeCounter();
                            displayStatusFragment.scrollToTop();
                            break;
                        case 2:
                            if (display_local)
                                updateTimeLine(RetrieveFeedsAsyncTask.Type.LOCAL, 0);
                            else if (display_global)
                                updateTimeLine(RetrieveFeedsAsyncTask.Type.PUBLIC, 0);
                            displayStatusFragment = ((DisplayStatusFragment) fragment);
                            displayStatusFragment.scrollToTop();
                            break;
                        case 3:
                            displayStatusFragment = ((DisplayStatusFragment) fragment);
                            displayStatusFragment.scrollToTop();
                            updateTimeLine(RetrieveFeedsAsyncTask.Type.PUBLIC, 0);
                            break;
                        case 1:
                            DisplayNotificationsFragment displayNotificationsFragment = ((DisplayNotificationsFragment) fragment);
                            countNewNotifications = 0;
                            updateNotifCounter();
                            displayNotificationsFragment.scrollToTop();
                            break;
                        default:
                            displayStatusFragment = ((DisplayStatusFragment) fragment);
                            displayStatusFragment.scrollToTop();
                    }
                }
            }
        });
        refreshSearchTab();
        int tabCount = tabLayout.getTabCount();
        for( int j = countPage ; j < tabCount ; j++){
            attacheDelete(j);
        }
        final NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //Scroll to top when top bar is clicked for favourites/blocked/muted
        toolbarTitle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                if( navigationView.getMenu().findItem(R.id.nav_favorites) != null && navigationView.getMenu().findItem(R.id.nav_favorites).isChecked()){
                    DisplayStatusFragment faveFrag = (DisplayStatusFragment) fragmentManager.findFragmentByTag("FAVOURITES");
                    if (faveFrag != null && faveFrag.isVisible()) {
                        faveFrag.scrollToTop();
                    }
                } else if (navigationView.getMenu().findItem(R.id.nav_blocked) != null && navigationView.getMenu().findItem(R.id.nav_blocked).isChecked()) {
                    DisplayAccountsFragment blockFrag = (DisplayAccountsFragment) fragmentManager.findFragmentByTag("BLOCKS");

                    if (blockFrag != null && blockFrag.isVisible()) {
                        blockFrag.scrollToTop();
                    }
                } else if (navigationView.getMenu().findItem(R.id.nav_muted) != null && navigationView.getMenu().findItem(R.id.nav_muted).isChecked()) {
                    DisplayAccountsFragment muteFrag = (DisplayAccountsFragment) fragmentManager.findFragmentByTag("MUTED");

                    if (muteFrag != null && muteFrag.isVisible()) {
                        muteFrag.scrollToTop();
                    }
                    //Scroll to top when top bar is clicked (THEME_MENU only)
                } else {
                    int pos = tabLayout.getSelectedTabPosition();
                    if( viewPager.getAdapter() != null) {
                        Fragment fragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, pos);
                        switch (pos) {
                            case 0:
                            case 2:
                            case 3:
                                DisplayStatusFragment displayStatusFragment = ((DisplayStatusFragment) fragment);
                                displayStatusFragment.scrollToTop();
                                break;
                            case 1:
                                DisplayNotificationsFragment displayNotificationsFragment = ((DisplayNotificationsFragment) fragment);
                                displayNotificationsFragment.scrollToTop();
                                break;
                        }
                    }
                }
            }
        });


        toolbar_search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Hide keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                assert imm != null;
                imm.hideSoftInputFromWindow(toolbar_search.getWindowToken(), 0);
                Intent intent = new Intent(BaseMainActivity.this, SearchResultActivity.class);
                query= query.replaceAll("^#+", "");
                intent.putExtra("search", query);
                startActivity(intent);
                toolbar_search.setQuery("", false);
                toolbar_search.setIconified(true);

                if( main_app_container.getVisibility() == View.VISIBLE){
                    main_app_container.setVisibility(View.VISIBLE);
                    viewPager.setVisibility(View.GONE);
                    tabLayout.setVisibility(View.GONE);
                    toolbarTitle.setVisibility(View.VISIBLE);
                }else {
                    main_app_container.setVisibility(View.GONE);
                    viewPager.setVisibility(View.VISIBLE);
                    tabLayout.setVisibility(View.VISIBLE);
                    toolbarTitle.setVisibility(View.GONE);
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        //Hide/Close the searchview


        toolbar_search.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                if( main_app_container.getVisibility() == View.VISIBLE){
                    main_app_container.setVisibility(View.VISIBLE);
                    viewPager.setVisibility(View.GONE);
                    tabLayout.setVisibility(View.GONE);
                    toolbarTitle.setVisibility(View.VISIBLE);
                }else {
                    main_app_container.setVisibility(View.GONE);
                    viewPager.setVisibility(View.VISIBLE);
                    tabLayout.setVisibility(View.VISIBLE);
                    toolbarTitle.setVisibility(View.GONE);
                }
                //your code here
                return false;
            }
        });
        toolbar_search.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( toolbar_search.isIconified()){
                    if( main_app_container.getVisibility() == View.VISIBLE){
                        main_app_container.setVisibility(View.VISIBLE);
                        viewPager.setVisibility(View.GONE);
                        tabLayout.setVisibility(View.GONE);
                        toolbarTitle.setVisibility(View.VISIBLE);
                    }else {
                        main_app_container.setVisibility(View.GONE);
                        viewPager.setVisibility(View.VISIBLE);
                        tabLayout.setVisibility(View.VISIBLE);
                        toolbarTitle.setVisibility(View.GONE);
                    }
                }else {
                    toolbarTitle.setVisibility(View.GONE);
                    tabLayout.setVisibility(View.GONE);
                }
            }
        });

        //Hide the default title
        if( getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().getThemedContext().setTheme(R.style.AppThemeBlack);
        }
        //Defines the current locale of the device in a static variable
        currentLocale = Helper.currentLocale(getApplicationContext());

        toot = findViewById(R.id.toot);
        delete_all = findViewById(R.id.delete_all);
        add_new = findViewById(R.id.add_new);
        toot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), TootActivity.class);
                startActivity(intent);
            }
        });

        userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        instance = sharedpreferences.getString(Helper.PREF_INSTANCE, Helper.getLiveInstance(getApplicationContext()));

        //Get the previous bookmark value
        //If null try to use the LAST_HOMETIMELINE_MAX_ID
        String lastHomeTimeline = sharedpreferences.getString(Helper.LAST_HOMETIMELINE_MAX_ID + userId + instance, null);
        bookmark = sharedpreferences.getString(Helper.BOOKMARK_ID + userId + instance, lastHomeTimeline);
        Account account = new AccountDAO(getApplicationContext(), db).getAccountByID(userId);
        if( account == null){
            Helper.logout(getApplicationContext());
            Intent myIntent = new Intent(BaseMainActivity.this, LoginActivity.class);
            startActivity(myIntent);
            finish();
            return;
        }
        //Image loader configuration

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.setDrawerIndicatorEnabled(false);
        ImageView iconbar = toolbar.findViewById(R.id.iconbar);
        iconbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(Gravity.START);
            }
        });
        Helper.loadPictureIcon(BaseMainActivity.this, account.getAvatar(),iconbar);
        headerLayout = navigationView.getHeaderView(0);
        mamageNewIntent(getIntent());
        final ImageView menuMore = headerLayout.findViewById(R.id.header_option_menu);
        menuMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(BaseMainActivity.this, menuMore);
                popup.getMenuInflater()
                        .inflate(R.menu.main, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_logout:
                                Helper.logout(getApplicationContext());
                                Intent myIntent = new Intent(BaseMainActivity.this, LoginActivity.class);
                                startActivity(myIntent);
                                finish();
                                return true;
                            case R.id.action_privacy:
                                Intent intent = new Intent(getApplicationContext(), PrivacyActivity.class);
                                startActivity(intent);
                                return true;
                            case R.id.action_about_instance:
                                intent = new Intent(getApplicationContext(), InstanceActivity.class);
                                startActivity(intent);
                                return true;
                            case R.id.action_cache:
                                AlertDialog.Builder builder = new AlertDialog.Builder(BaseMainActivity.this);
                                builder.setTitle(R.string.cache_title);
                                long sizeCache = Helper.cacheSize(getCacheDir());
                                float cacheSize = 0;
                                if( sizeCache > 0 ) {
                                    cacheSize = (float) sizeCache / 1000000.0f;
                                }
                                final float finalCacheSize = cacheSize;
                                builder.setMessage(getString(R.string.cache_message, String.format("%s %s", String.format(Locale.getDefault(), "%.2f", cacheSize), getString(R.string.cache_units))))
                                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                // continue with delete
                                                try {
                                                    String path = getCacheDir().getPath();
                                                    File dir = new File(path);
                                                    if (dir.isDirectory()) {
                                                        Helper.deleteDir(dir);
                                                    }
                                                } catch (Exception ignored) {}
                                                Toast.makeText(BaseMainActivity.this, getString(R.string.toast_cache_clear,String.format("%s %s", String.format(Locale.getDefault(), "%.2f", finalCacheSize), getString(R.string.cache_units))), Toast.LENGTH_LONG).show();
                                                dialog.dismiss();
                                            }
                                        })
                                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        })
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .show();
                                return true;
                            case R.id.action_size:
                                final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                                int textSize = sharedpreferences.getInt(Helper.SET_TEXT_SIZE,110);
                                int iconSize = sharedpreferences.getInt(Helper.SET_ICON_SIZE,130);

                                builder = new AlertDialog.Builder(BaseMainActivity.this);
                                builder.setTitle(R.string.text_size);

                                @SuppressLint("InflateParams") View popup_quick_settings = getLayoutInflater().inflate( R.layout.popup_text_size, null );
                                builder.setView(popup_quick_settings);

                                SeekBar set_text_size = popup_quick_settings.findViewById(R.id.set_text_size);
                                SeekBar set_icon_size = popup_quick_settings.findViewById(R.id.set_icon_size);
                                final TextView set_text_size_value = popup_quick_settings.findViewById(R.id.set_text_size_value);
                                final TextView set_icon_size_value = popup_quick_settings.findViewById(R.id.set_icon_size_value);
                                set_text_size_value.setText(String.format("%s%%",String.valueOf(textSize)));
                                set_icon_size_value.setText(String.format("%s%%",String.valueOf(iconSize)));

                                set_text_size.setMax(20);
                                set_icon_size.setMax(20);

                                set_text_size.setProgress(((textSize-80)/5));
                                set_icon_size.setProgress(((iconSize-80)/5));

                                set_text_size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                    @Override
                                    public void onStopTrackingTouch(SeekBar seekBar) {}
                                    @Override
                                    public void onStartTrackingTouch(SeekBar seekBar) {}
                                    @Override
                                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                                        int value = 80 + progress*5;
                                        set_text_size_value.setText(String.format("%s%%",String.valueOf(value)));
                                        SharedPreferences.Editor editor = sharedpreferences.edit();
                                        editor.putInt(Helper.SET_TEXT_SIZE, value);
                                        editor.apply();
                                    }
                                });
                                set_icon_size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                    @Override
                                    public void onStopTrackingTouch(SeekBar seekBar) {}
                                    @Override
                                    public void onStartTrackingTouch(SeekBar seekBar) {}
                                    @Override
                                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                        int value = 80 + progress*5;
                                        set_icon_size_value.setText(String.format("%s%%",String.valueOf(value)));
                                        SharedPreferences.Editor editor = sharedpreferences.edit();
                                        editor.putInt(Helper.SET_ICON_SIZE, value);
                                        editor.apply();
                                    }
                                });
                                builder.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        BaseMainActivity.this.recreate();
                                        dialog.dismiss();
                                    }
                                })
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .show();
                                return true;
                            case R.id.action_proxy:
                                intent = new Intent(getApplicationContext(), ProxyActivity.class);
                                startActivity(intent);
                                return true;
                            case R.id.action_export:
                                if(Build.VERSION.SDK_INT >= 23 ){
                                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {
                                        ActivityCompat.requestPermissions(BaseMainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_REQUEST_CODE);
                                    } else {
                                        Intent backupIntent = new Intent(BaseMainActivity.this, BackupStatusService.class);
                                        backupIntent.putExtra("userId", userId);
                                        startService(backupIntent);
                                    }
                                }else{
                                    Intent backupIntent = new Intent(BaseMainActivity.this, BackupStatusService.class);
                                    backupIntent.putExtra("userId", userId);
                                    startService(backupIntent);
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
        final ImageView optionInfo = headerLayout.findViewById(R.id.header_option_info);
        optionInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), InstanceHealthActivity.class);
                startActivity(intent);
            }
        });
        updateHeaderAccountInfo(BaseMainActivity.this, account, headerLayout);
        //Locked account can see follow request
        if (account.isLocked()) {
            navigationView.getMenu().findItem(R.id.nav_follow_request).setVisible(true);
        } else {
            navigationView.getMenu().findItem(R.id.nav_follow_request).setVisible(false);
        }

        //Check instance release for lists
        String instanceVersion = sharedpreferences.getString(Helper.INSTANCE_VERSION + userId + instance, null);
        if (instanceVersion != null) {
            Version currentVersion = new Version(instanceVersion);
            Version minVersion = new Version("2.1");
            if (currentVersion.compareTo(minVersion) == 1 || currentVersion.equals(minVersion)) {
                navigationView.getMenu().findItem(R.id.nav_list).setVisible(true);
            } else {
                navigationView.getMenu().findItem(R.id.nav_list).setVisible(false);
            }
        }

        LinearLayout owner_container = headerLayout.findViewById(R.id.main_header_container);
        owner_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuAccounts(BaseMainActivity.this);
                if( main_app_container.getVisibility() == View.VISIBLE){
                    main_app_container.setVisibility(View.VISIBLE);
                    viewPager.setVisibility(View.GONE);
                    tabLayout.setVisibility(View.GONE);
                    toolbarTitle.setVisibility(View.VISIBLE);
                }else {
                    main_app_container.setVisibility(View.GONE);
                    viewPager.setVisibility(View.VISIBLE);
                    tabLayout.setVisibility(View.VISIBLE);
                    toolbarTitle.setVisibility(View.GONE);
                }
            }
        });




        // Asked once for notification opt-in
        boolean popupShown = sharedpreferences.getBoolean(Helper.SET_POPUP_PUSH, false);
        if(!popupShown){
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(BaseMainActivity.this);
            LayoutInflater inflater = getLayoutInflater();
            @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.popup_quick_settings, null);
            dialogBuilder.setView(dialogView);

            final SwitchCompat set_push_hometimeline = dialogView.findViewById(R.id.set_push_hometimeline);
            final SwitchCompat set_push_notification = dialogView.findViewById(R.id.set_push_notification);
            boolean notif_hometimeline = sharedpreferences.getBoolean(Helper.SET_NOTIF_HOMETIMELINE, false);
            boolean notif_follow = sharedpreferences.getBoolean(Helper.SET_NOTIF_FOLLOW, true);
            boolean notif_add = sharedpreferences.getBoolean(Helper.SET_NOTIF_ADD, true);
            boolean notif_ask = sharedpreferences.getBoolean(Helper.SET_NOTIF_ASK, true);
            boolean notif_mention = sharedpreferences.getBoolean(Helper.SET_NOTIF_MENTION, true);
            boolean notif_share = sharedpreferences.getBoolean(Helper.SET_NOTIF_SHARE, true);
            boolean notifif_notifications = !( !notif_follow &&  !notif_add && !notif_ask && !notif_mention && !notif_share);
            set_push_hometimeline.setChecked(notif_hometimeline);
            set_push_notification.setChecked(notifif_notifications);

            dialogBuilder.setTitle(R.string.settings_popup_title);
            dialogBuilder.setCancelable(false);
            dialogBuilder.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putBoolean(Helper.SET_NOTIF_FOLLOW, set_push_notification.isChecked());
                    editor.putBoolean(Helper.SET_NOTIF_ADD, set_push_notification.isChecked());
                    editor.putBoolean(Helper.SET_NOTIF_ASK, set_push_notification.isChecked());
                    editor.putBoolean(Helper.SET_NOTIF_MENTION, set_push_notification.isChecked());
                    editor.putBoolean(Helper.SET_NOTIF_SHARE, set_push_notification.isChecked());
                    editor.putBoolean(Helper.SET_NOTIF_HOMETIMELINE, set_push_hometimeline.isChecked());
                    editor.putBoolean(Helper.SET_POPUP_PUSH, true);
                    editor.apply();
                }
            }).show();
        }
        Helper.switchLayout(BaseMainActivity.this);

        if( receive_data != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receive_data);
        receive_data = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle b = intent.getExtras();
                Helper.EventStreaming eventStreaming = (Helper.EventStreaming) intent.getSerializableExtra("eventStreaming");
                assert b != null;
                userIdService = b.getString("userIdService", null);
                if( userIdService != null && userIdService.equals(userId)) {
                    if (eventStreaming == Helper.EventStreaming.NOTIFICATION) {
                        Notification notification = b.getParcelable("data");
                        if (notificationsFragment != null) {
                            notificationsFragment.refresh(notification);
                        }
                    } else if (eventStreaming == Helper.EventStreaming.UPDATE) {
                        Status status = b.getParcelable("data");
                        if (homeFragment != null) {
                            homeFragment.refresh(status);
                        }
                    } else if (eventStreaming == Helper.EventStreaming.DELETE) {
                        //noinspection unused
                        String id = b.getString("id");
                        if (notificationsFragment != null) {
                            //noinspection StatementWithEmptyBody
                            if (notificationsFragment.getUserVisibleHint()) {

                            } else {

                            }
                        }
                    }
                    updateNotifCounter();
                    updateHomeCounter();
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(receive_data, new IntentFilter(Helper.RECEIVE_DATA));

        // Retrieves instance
        new RetrieveInstanceAsyncTask(getApplicationContext(), BaseMainActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void refreshSearchTab(){
        Helper.addSearchTag(BaseMainActivity.this, tabLayout, adapter);
    }

    public void removeSearchTab(String tag){
        Helper.removeSearchTag(tag, tabLayout, adapter);
        int allTabCount = tabLayout.getTabCount();
        if( allTabCount == countPage){
            main_app_container.setVisibility(View.GONE);
            viewPager.setVisibility(View.VISIBLE);
            tabLayout.setVisibility(View.VISIBLE);
            toolbarTitle.setVisibility(View.GONE);
        }
    }


    protected abstract void rateThisApp();


    private boolean manageFilters(LinearLayout tabStrip, final SharedPreferences sharedpreferences){
        //Only shown if the tab has focus
        if(
                (homeFragment != null && homeFragment.getUserVisibleHint()) ||
                        (federatedFragment != null && federatedFragment.getUserVisibleHint()) ||
                        (localFragment != null && localFragment.getUserVisibleHint())
                ){
            PopupMenu popup = null;
            if(homeFragment != null && homeFragment.getUserVisibleHint())
                popup = new PopupMenu(BaseMainActivity.this, tabStrip.getChildAt(0));
            else if(localFragment != null && localFragment.getUserVisibleHint())
                popup = new PopupMenu(BaseMainActivity.this, tabStrip.getChildAt(2));
            else if(federatedFragment != null && federatedFragment.getUserVisibleHint()){
                if( !display_local && display_global)
                    popup = new PopupMenu(BaseMainActivity.this, tabStrip.getChildAt(2));
                else
                    popup = new PopupMenu(BaseMainActivity.this, tabStrip.getChildAt(3));
            }
            if( popup == null)
                return true;
            popup.getMenuInflater()
                    .inflate(R.menu.option_filter_toots, popup.getMenu());
            Menu menu = popup.getMenu();
            final MenuItem itemShowBoosts = menu.findItem(R.id.action_show_boosts);
            final MenuItem itemShowReplies = menu.findItem(R.id.action_show_replies);
            final MenuItem itemFilter = menu.findItem(R.id.action_filter);
            if((federatedFragment != null && federatedFragment.getUserVisibleHint()) ||
                    (localFragment != null && localFragment.getUserVisibleHint())){
                itemShowBoosts.setVisible(false);
                itemShowReplies.setVisible(false);
                itemFilter.setVisible(true);
            }else {
                itemShowBoosts.setVisible(true);
                itemShowReplies.setVisible(true);
                itemFilter.setVisible(true);
            }
            show_boosts = sharedpreferences.getBoolean(Helper.SET_SHOW_BOOSTS, true);
            show_replies = sharedpreferences.getBoolean(Helper.SET_SHOW_REPLIES, true);

            if(homeFragment != null && homeFragment.getUserVisibleHint())
                show_filtered = sharedpreferences.getString(Helper.SET_FILTER_REGEX_HOME, null);
            if(localFragment != null && localFragment.getUserVisibleHint())
                show_filtered = sharedpreferences.getString(Helper.SET_FILTER_REGEX_LOCAL, null);
            if(federatedFragment != null && federatedFragment.getUserVisibleHint())
                show_filtered = sharedpreferences.getString(Helper.SET_FILTER_REGEX_PUBLIC, null);

            itemShowBoosts.setChecked(show_boosts);
            itemShowReplies.setChecked(show_replies);
            if( show_filtered != null && show_filtered.length() > 0){
                itemFilter.setTitle(show_filtered);
            }

            popup.setOnDismissListener(new PopupMenu.OnDismissListener() {
                @Override
                public void onDismiss(PopupMenu menu) {
                    if(homeFragment != null && homeFragment.getUserVisibleHint())
                        homeFragment.refreshFilter();
                    if(localFragment != null && localFragment.getUserVisibleHint())
                        localFragment.refreshFilter();
                    if(federatedFragment != null && federatedFragment.getUserVisibleHint())
                        federatedFragment.refreshFilter();
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
                    final SharedPreferences.Editor editor = sharedpreferences.edit();
                    switch (item.getItemId()) {
                        case R.id.action_show_boosts:
                            show_boosts = !show_boosts;
                            editor.putBoolean(Helper.SET_SHOW_BOOSTS, show_boosts);
                            itemShowBoosts.setChecked(show_boosts);
                            editor.apply();
                            break;
                        case R.id.action_show_replies:
                            show_replies = !show_replies;
                            editor.putBoolean(Helper.SET_SHOW_REPLIES, show_replies);
                            itemShowReplies.setChecked(show_replies);
                            editor.apply();
                            break;
                        case R.id.action_filter:
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(BaseMainActivity.this);
                            LayoutInflater inflater = getLayoutInflater();
                            @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.filter_regex, null);
                            dialogBuilder.setView(dialogView);
                            final EditText editText = dialogView.findViewById(R.id.filter_regex);
                            Toast alertRegex = Toast.makeText(BaseMainActivity.this, R.string.alert_regex, Toast.LENGTH_LONG);
                            editText.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                }
                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {
                                }
                                @Override
                                public void afterTextChanged(Editable s) {
                                    try {
                                        //noinspection ResultOfMethodCallIgnored
                                        Pattern.compile("(" + s.toString() + ")", Pattern.CASE_INSENSITIVE);
                                    }catch (Exception e){
                                        if( !alertRegex.getView().isShown()){
                                            alertRegex.show();
                                        }
                                    }

                                }
                            });
                            if( show_filtered != null) {
                                editText.setText(show_filtered);
                                editText.setSelection(editText.getText().toString().length());
                            }
                            dialogBuilder.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    itemFilter.setTitle(editText.getText().toString().trim());
                                    if(homeFragment != null && homeFragment.getUserVisibleHint())
                                        editor.putString(Helper.SET_FILTER_REGEX_HOME, editText.getText().toString().trim());
                                    if(localFragment != null && localFragment.getUserVisibleHint())
                                        editor.putString(Helper.SET_FILTER_REGEX_LOCAL, editText.getText().toString().trim());
                                    if(federatedFragment != null && federatedFragment.getUserVisibleHint())
                                        editor.putString(Helper.SET_FILTER_REGEX_PUBLIC, editText.getText().toString().trim());
                                    editor.apply();
                                }
                            });
                            AlertDialog alertDialog = dialogBuilder.create();
                            alertDialog.show();
                            break;
                    }
                    return false;
                }
            });
            popup.show();
        }
        return true;
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mamageNewIntent(intent);
    }

    /**
     * Manages new intents
     * @param intent Intent - intent related to a notification in top bar
     */
    private void mamageNewIntent(Intent intent){

        if( intent == null )
            return;

        String action = intent.getAction();
        String type = intent.getType();
        Bundle extras = intent.getExtras();
        String userIdIntent;
        if( extras != null && extras.containsKey(INTENT_ACTION) ){
            final NavigationView navigationView = findViewById(R.id.nav_view);
            userIdIntent = extras.getString(PREF_KEY_ID); //Id of the account in the intent
            if (extras.getInt(INTENT_ACTION) == NOTIFICATION_INTENT){
                changeUser(BaseMainActivity.this, userIdIntent, false); //Connects the account which is related to the notification
                unCheckAllMenuItems(navigationView);
                if( tabLayout.getTabAt(1) != null)
                    //noinspection ConstantConditions
                    tabLayout.getTabAt(1).select();
                if( extras.getString(INTENT_TARGETED_ACCOUNT) != null ){
                    Intent intentShow = new Intent(BaseMainActivity.this, ShowAccountActivity.class);
                    Bundle b = new Bundle();
                    b.putString("accountId", extras.getString(INTENT_TARGETED_ACCOUNT));
                    intentShow.putExtras(b);
                    startActivity(intentShow);
                }
            }else if( extras.getInt(INTENT_ACTION) == HOME_TIMELINE_INTENT){
                changeUser(BaseMainActivity.this, userIdIntent, true); //Connects the account which is related to the notification
            }else if( extras.getInt(INTENT_ACTION) == CHANGE_THEME_INTENT){
                unCheckAllMenuItems(navigationView);
                navigationView.setCheckedItem(R.id.nav_settings);
                navigationView.getMenu().performIdentifierAction(R.id.nav_settings, 0);
                toolbarTitle.setText(R.string.settings);
            }else if( extras.getInt(INTENT_ACTION) == CHANGE_USER_INTENT){
                unCheckAllMenuItems(navigationView);
                if( tabLayout.getTabAt(0) != null)
                    //noinspection ConstantConditions
                    tabLayout.getTabAt(0).select();
                if( !toolbar_search.isIconified() ) {
                    toolbar_search.setIconified(true);
                }
            }else if (extras.getInt(INTENT_ACTION) == ADD_USER_INTENT){
                this.recreate();
            }else if( extras.getInt(INTENT_ACTION) == BACKUP_INTENT){
                Intent myIntent = new Intent(BaseMainActivity.this, OwnerStatusActivity.class);
                startActivity(myIntent);
            }else if (extras.getInt(INTENT_ACTION) == SEARCH_REMOTE) {
                String url = extras.getString(SEARCH_URL);
                if( url == null)
                    return;
                Matcher matcher;
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT)
                    matcher = Patterns.WEB_URL.matcher(url);
                else
                    matcher = Helper.urlPattern.matcher(url);
                boolean isUrl = false;
                while (matcher.find()){
                    isUrl = true;
                }
                if(!isUrl)
                    return;
                //Here we know that the intent contains a valid URL
                new RetrieveRemoteDataAsyncTask(BaseMainActivity.this, url, BaseMainActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }else if( Intent.ACTION_SEND.equals(action) && type != null ) {
            if ("text/plain".equals(type)) {
                String url = null;
                String sharedSubject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (sharedText != null) {
                    /* Some apps don't send the URL as the first part of the EXTRA_TEXT,
                        the BBC News app being one such, in this case find where the URL
                        is and strip that out into sharedText.
                     */
                    Matcher matcher;
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT)
                        matcher = Patterns.WEB_URL.matcher(sharedText);
                    else
                        matcher = Helper.urlPattern.matcher(sharedText);
                    while (matcher.find()){
                        int matchStart = matcher.start(1);
                        int matchEnd = matcher.end();
                        if(matchStart < matchEnd && sharedText.length() >= matchEnd)
                            url = sharedText.substring(matchStart, matchEnd);
                    }
                    new RetrieveMetaDataAsyncTask(BaseMainActivity.this, sharedSubject, sharedText, url,BaseMainActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }

            } else if (type.startsWith("image/")) {

                if( !TootActivity.active){
                    Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                    if (imageUri != null) {
                        Bundle b = new Bundle();
                        b.putParcelable("sharedUri", imageUri);
                        b.putInt("uriNumberMast", 1);
                        CrossActions.doCrossShare(BaseMainActivity.this, b);
                    }
                }else{
                    Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                    if (imageUri != null) {
                        intent = new Intent(getApplicationContext(), TootActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent .putExtra("imageUri", imageUri.toString());
                        startActivity(intent );
                    }
                }

            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null ) {
            if (type.startsWith("image/")) {

                ArrayList<Uri> imageList = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                if (imageList != null) {
                    Bundle b = new Bundle();
                    b.putParcelableArrayList("sharedUri", imageList);
                    b.putInt("uriNumberMast", imageList.size());
                    CrossActions.doCrossShare(BaseMainActivity.this, b);
                }
            }
        }else if (Intent.ACTION_VIEW.equals(action)) {
            String url = intent.getDataString();
            if( url == null)
                return;
            Matcher matcher;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT)
                matcher = Patterns.WEB_URL.matcher(url);
            else
                matcher = Helper.urlPattern.matcher(url);
            boolean isUrl = false;
            while (matcher.find()){
                isUrl = true;
            }
            if(!isUrl)
                return;
            //Here we know that the intent contains a valid URL
            new RetrieveRemoteDataAsyncTask(BaseMainActivity.this, url, BaseMainActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        intent.replaceExtras(new Bundle());
        intent.setAction("");
        intent.setData(null);
        intent.setFlags(0);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //Hide search bar on back pressed
            if( !toolbar_search.isIconified()){
                toolbar_search.setIconified(true);
                return;
            }
            if( viewPager.getVisibility() == View.VISIBLE){
                if (stackBack.size() > 1) {
                    stackBack.pop();
                    viewPager.setCurrentItem(stackBack.lastElement());
                }else {
                    super.onBackPressed();
                }
            }else {
                Helper.switchLayout(BaseMainActivity.this);
                main_app_container.setVisibility(View.GONE);
                viewPager.setVisibility(View.VISIBLE);
                tabLayout.setVisibility(View.VISIBLE);
                toolbarTitle.setVisibility(View.GONE);
                delete_all.setVisibility(View.GONE);
                add_new.setVisibility(View.GONE);
                final NavigationView navigationView = findViewById(R.id.nav_view);
                unCheckAllMenuItems(navigationView);
                toot.setVisibility(View.VISIBLE);
                switch (viewPager.getCurrentItem()){
                    case 1:
                        toot.setVisibility(View.GONE);
                        break;
                }
            }

        }

    }

    @Override
    public void onResume(){
        super.onResume();
        BaseMainActivity.activityResumed();
        updateNotifCounter();
        updateHomeCounter();
        //Proceeds to update of the authenticated account
        if(Helper.isLoggedIn(getApplicationContext()))
            new UpdateAccountInfoByIDAsyncTask(getApplicationContext(), BaseMainActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        if( lastHomeId != null && homeFragment != null){
            homeFragment.retrieveMissingToots(lastHomeId);
        }
        if( lastNotificationId != null && notificationsFragment != null){
            notificationsFragment.retrieveMissingNotifications(lastNotificationId);
        }
    }


    @Override
    public void onStart(){
        super.onStart();
        if( receive_federated_data != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receive_federated_data);
        receive_federated_data = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle b = intent.getExtras();
                assert b != null;
                userIdService = b.getString("userIdService", null);
                if( userIdService != null && userIdService.equals(userId)) {
                    Status status = b.getParcelable("data");
                    if (federatedFragment != null) {
                        federatedFragment.refresh(status);
                    }
                }
            }
        };
        if( receive_local_data != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receive_local_data);
        receive_local_data = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle b = intent.getExtras();
                assert b != null;
                userIdService = b.getString("userIdService", null);
                if( userIdService != null && userIdService.equals(userId)) {
                    Status status = b.getParcelable("data");
                    if (localFragment != null) {
                        localFragment.refresh(status);
                    }
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(receive_federated_data, new IntentFilter(Helper.RECEIVE_FEDERATED_DATA));
        LocalBroadcastManager.getInstance(this).registerReceiver(receive_local_data, new IntentFilter(Helper.RECEIVE_LOCAL_DATA));

    }

    @Override
    public void onStop(){
        super.onStop();
        if( receive_federated_data != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receive_federated_data);
        if( receive_local_data != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receive_local_data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BaseMainActivity.activityPaused();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if( receive_data != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receive_data);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if( id == R.id.nav_archive) {
            Intent myIntent = new Intent(BaseMainActivity.this, OwnerStatusActivity.class);
            startActivity(myIntent);
            return false;
        } else if( id == R.id.nav_about) {
            Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
            startActivity(intent);
            return false;
        } else if( id == R.id.nav_partnership) {
            Intent intent = new Intent(getApplicationContext(), PartnerShipActivity.class);
            startActivity(intent);
            return false;
        }
        final NavigationView navigationView = findViewById(R.id.nav_view);
        unCheckAllMenuItems(navigationView);
        item.setChecked(true);
        //Remove the search bar
        if (!toolbar_search.isIconified()) {
            toolbar_search.setIconified(true);
        }
        toolbarTitle.setText(item.getTitle());
        DisplayStatusFragment statusFragment;
        DisplayAccountsFragment accountsFragment;
        Bundle bundle = new Bundle();
        FragmentManager fragmentManager = getSupportFragmentManager();
        String fragmentTag = null;

        main_app_container.setVisibility(View.VISIBLE);
        viewPager.setVisibility(View.GONE);
        tabLayout.setVisibility(View.GONE);
        toolbarTitle.setVisibility(View.VISIBLE);
        appBar.setExpanded(true);
        if (id != R.id.nav_drafts && id != R.id.nav_bookmarks ) {
            delete_all.setVisibility(View.GONE);
        }else{
            delete_all.setVisibility(View.VISIBLE);
        }
        if( id != R.id.nav_list){
            add_new.setVisibility(View.GONE);
        }else{
            add_new.setVisibility(View.VISIBLE);
        }
        if (id == R.id.nav_settings) {
            toot.setVisibility(View.GONE);
            TabLayoutSettingsFragment tabLayoutSettingsFragment= new TabLayoutSettingsFragment();
            fragmentTag = "TABLAYOUT_SETTINGS";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, tabLayoutSettingsFragment, fragmentTag).commit();

        }else if (id == R.id.nav_favorites) {
            toot.setVisibility(View.GONE);
            statusFragment = new DisplayStatusFragment();
            bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.FAVOURITES);
            statusFragment.setArguments(bundle);
            fragmentTag = "FAVOURITES";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, statusFragment, fragmentTag).commit();
        } else if (id == R.id.nav_blocked) {
            toot.setVisibility(View.GONE);
            accountsFragment = new DisplayAccountsFragment();
            bundle.putSerializable("type", RetrieveAccountsAsyncTask.Type.BLOCKED);
            accountsFragment.setArguments(bundle);
            fragmentTag = "BLOCKS";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, accountsFragment, fragmentTag).commit();
        }else if (id == R.id.nav_muted) {
            toot.setVisibility(View.GONE);
            accountsFragment = new DisplayAccountsFragment();
            bundle.putSerializable("type", RetrieveAccountsAsyncTask.Type.MUTED);
            accountsFragment.setArguments(bundle);
            fragmentTag = "MUTED";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, accountsFragment, fragmentTag).commit();
        }else if (id == R.id.nav_scheduled) {
            toot.setVisibility(View.VISIBLE);
            DisplayScheduledTootsFragment displayScheduledTootsFragment = new DisplayScheduledTootsFragment();
            fragmentTag = "SCHEDULED";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, displayScheduledTootsFragment, fragmentTag).commit();
        }else if (id == R.id.nav_drafts) {
            DisplayDraftsFragment displayDraftsFragment = new DisplayDraftsFragment();
            fragmentTag = "DRAFTS";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, displayDraftsFragment, fragmentTag).commit();
            toot.setVisibility(View.GONE);
        }else if (id == R.id.nav_bookmarks) {
            DisplayBookmarksFragment displayBookmarksFragment = new DisplayBookmarksFragment();
            fragmentTag = "BOOKMARKS";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, displayBookmarksFragment, fragmentTag).commit();
            toot.setVisibility(View.GONE);
        }else if( id == R.id.nav_follow_request){
            toot.setVisibility(View.GONE);
            DisplayFollowRequestSentFragment followRequestSentFragment = new DisplayFollowRequestSentFragment();
            fragmentTag = "FOLLOW_REQUEST_SENT";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, followRequestSentFragment, fragmentTag).commit();
        }else if(id == R.id.nav_list){
            toot.setVisibility(View.GONE);
            DisplayListsFragment displayListsFragment = new DisplayListsFragment();
            fragmentTag = "LISTS";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, displayListsFragment, fragmentTag).commit();
        }else if(id == R.id.nav_filters){
            toot.setVisibility(View.GONE);
            DisplayFiltersFragment displayFiltersFragment = new DisplayFiltersFragment();
            fragmentTag = "FILTERS";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, displayFiltersFragment, fragmentTag).commit();
        }

        populateTitleWithTag(fragmentTag, item.getTitle().toString(), item.getItemId());
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void populateTitleWithTag(String tag, String title, int index){
        if( tag == null)
            return;
        if ( tagTile.get(tag) == null)
            tagTile.put(tag, title);
        if ( tagItem.get(tag) == null)
            tagItem.put(tag, index);
    }

    @Override
    public void setTitle(CharSequence title) {
        if(toolbarTitle != null )
            toolbarTitle.setText(title);
    }

    @Override
    public void onUpdateAccountInfo(boolean error) {
        if( error){
            //An error occurred,  the user is redirected to the login page
            Helper.logout(getApplicationContext());
            Intent myIntent = new Intent(BaseMainActivity.this, LoginActivity.class);
            startActivity(myIntent);
            finish();
        }else {
            SQLiteDatabase db = Sqlite.getInstance(BaseMainActivity.this, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
            Account account = new AccountDAO(getApplicationContext(), db).getAccountByID(userId);
            updateHeaderAccountInfo(BaseMainActivity.this, account, headerLayout);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //noinspection StatementWithEmptyBody
        if (requestCode == ERROR_DIALOG_REQUEST_CODE) {
            // Adding a fragment via GooglePlayServicesUtil.showErrorDialogFragment
            // before the instance state is restored throws an error. So instead,
            // set a flag here, which will cause the fragment to delay until
            // onPostResume.
        }
    }


    @Override
    public void onRetrieveMetaData(boolean error, String sharedSubject, String sharedText, String image, String title, String description) {
        Bundle b = new Bundle();
        if( !error) {
            b.putString("image", image);
            b.putString("title", title);
            b.putString("description", description);
        }
        b.putString("sharedSubject", sharedSubject);
        b.putString("sharedContent", sharedText);
        CrossActions.doCrossShare(BaseMainActivity.this, b);
    }

    @Override
    public void onRetrieveInstance(APIResponse apiResponse) {
        if( apiResponse.getError() != null){
            return;
        }
        if( apiResponse.getInstance() == null || apiResponse.getInstance().getVersion() == null || apiResponse.getInstance().getVersion().trim().length() == 0)
            return;
        Version currentVersion = new Version(apiResponse.getInstance().getVersion());
        Version minVersion = new Version("1.6");
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(Helper.INSTANCE_VERSION + userId + instance, apiResponse.getInstance().getVersion());
        editor.apply();
        Helper.canPin = (currentVersion.compareTo(minVersion) == 1 || currentVersion.equals(minVersion));
    }

    public String getBookmark() {
        return bookmark;
    }

    public void setBookmark(@SuppressWarnings("SameParameterValue") String bookmark) {
        this.bookmark = bookmark;
    }

    @Override
    public void onRetrieveRemoteAccount(Results results) {
        if (results == null)
            return;
        List<Account> accounts = results.getAccounts();
        List<Status> statuses = results.getStatuses();
        if( accounts !=null && accounts.size() > 0){
            Intent intent = new Intent(BaseMainActivity.this, ShowAccountActivity.class);
            Bundle b = new Bundle();
            b.putString("accountId", accounts.get(0).getId());
            intent.putExtras(b);
            startActivity(intent);
        }else if( statuses != null && statuses.size() > 0){
            Intent intent = new Intent(getApplicationContext(), ShowConversationActivity.class);
            Bundle b = new Bundle();
            b.putString("statusId", statuses.get(0).getId());
            intent.putExtras(b);
            startActivity(intent);
        }
    }


    /**
     * Page Adapter for settings
     */
    public class PagerAdapter extends FragmentStatePagerAdapter  {
        int mNumOfTabs;

        private PagerAdapter(FragmentManager fm, int NumOfTabs) {
            super(fm);
            this.mNumOfTabs = NumOfTabs;
        }

        public void removeTabPage() {
            this.mNumOfTabs--;
            notifyDataSetChanged();
        }

        public void addTabPage(String title) {
            TabLayout.Tab tab = tabLayout.newTab();
            tab.setText(title);
            this.mNumOfTabs++;
            notifyDataSetChanged();
        }

        @Override
        public Fragment getItem(int position) {
            //Remove the search bar
            if( !toolbar_search.isIconified() ) {
                toolbarTitle.setVisibility(View.VISIBLE);
                tabLayout.setVisibility(View.VISIBLE);
                toolbar_search.setIconified(true);
            }
            //Selection comes from another menu, no action to do
            DisplayStatusFragment statusFragment;
            Bundle bundle = new Bundle();
            if (position == 0) {
                homeFragment = new DisplayStatusFragment();
                bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.HOME);
                homeFragment.setArguments(bundle);
                return homeFragment;
            }else if( position == 1) {
                notificationsFragment = new DisplayNotificationsFragment();
                return notificationsFragment;
            }else if( position == 2 && display_local) {
                statusFragment = new DisplayStatusFragment();
                bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.LOCAL);
                statusFragment.setArguments(bundle);
                return statusFragment;
            }else if(position == 2 && display_global){
                statusFragment = new DisplayStatusFragment();
                bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.PUBLIC);
                statusFragment.setArguments(bundle);
                return statusFragment;
            }else if (position == 3 && display_global && display_local){
                statusFragment = new DisplayStatusFragment();
                bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.PUBLIC);
                statusFragment.setArguments(bundle);
                return statusFragment;
            }else{ //Here it's a search fragment
                statusFragment = new DisplayStatusFragment();
                bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.TAG);
                if( tabLayout.getTabAt(position) != null && tabLayout.getTabAt(position).getText() != null)
                    bundle.putString("tag", tabLayout.getTabAt(position).getText().toString());
                statusFragment.setArguments(bundle);
                return statusFragment;
            }
        }

        @NonNull
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
            // save the appropriate reference depending on position
            switch (position) {
                case 0:
                    homeFragment = (DisplayStatusFragment) createdFragment;
                    break;
                case 1:
                    notificationsFragment = (DisplayNotificationsFragment) createdFragment;
                    break;
                case 2:
                    if ( !display_local && display_global)
                        federatedFragment = (DisplayStatusFragment) createdFragment;
                    if( display_local)
                        localFragment = (DisplayStatusFragment) createdFragment;
                    break;
                case 3:
                    if( display_local && display_global)
                        federatedFragment = (DisplayStatusFragment) createdFragment;
                    break;
            }
            return createdFragment;
        }

        @Override
        public int getCount() {
            return mNumOfTabs;
        }
    }


    private void attacheDelete(int position){
        LinearLayout tabStrip = (LinearLayout) tabLayout.getChildAt(0);
        String title = tabLayout.getTabAt(position).getText().toString().trim();
        SQLiteDatabase db = Sqlite.getInstance(BaseMainActivity.this, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        tabStrip.getChildAt(position).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(BaseMainActivity.this);
                dialogBuilder.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        new SearchDAO(BaseMainActivity.this, db).remove(title);
                        String tag;
                        if( position > 0 && tabLayout.getTabAt(position - 1).getText() != null) {
                            tag = tabLayout.getTabAt(position - 1).getText().toString();
                        }else if( tabLayout.getTabCount() > 1 && tabLayout.getTabAt(1).getText() != null) {
                            tag = tabLayout.getTabAt(1).getText().toString();
                        }else //Last element
                            tag = "";
                        Helper.removeTab(tabLayout, adapter, position);
                        adapter = new BaseMainActivity.PagerAdapter
                                (getSupportFragmentManager(), tabLayout.getTabCount());
                        viewPager.setAdapter(adapter);
                        for(int i = 0; i < tabLayout.getTabCount() ; i++ ){
                            if( tabLayout.getTabAt(i).getText() != null && tabLayout.getTabAt(i).getText().equals(tag.trim())){
                                tabLayout.getTabAt(i).select();
                                break;
                            }

                        }
                    }
                });
                dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                dialogBuilder.setMessage(getString(R.string.delete) + ": " + title);
                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        //Hide keyboard
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        assert imm != null;
                        imm.hideSoftInputFromWindow(viewPager.getWindowToken(), 0);
                    }
                });
                if( alertDialog.getWindow() != null )
                    alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                alertDialog.show();
                return false;
            }
        });
    }

    public void updateHomeCounter(){
        if( tabLayout.getTabAt(0) == null )
            return;
        //noinspection ConstantConditions
        View tabHome = tabLayout.getTabAt(0).getCustomView();
        if( tabHome == null)
            return;
        TextView tabCounterHome = tabHome.findViewById(R.id.tab_counter);
        tabCounterHome.setText(String.valueOf(countNewStatus));
        if( countNewStatus> 0){
            //New data are available
            //The fragment is not displayed, so the counter is displayed
            tabCounterHome.setVisibility(View.VISIBLE);
        }else {
            tabCounterHome.setVisibility(View.GONE);
        }
    }

    @SuppressWarnings("ConstantConditions")
    public void updateTimeLine(RetrieveFeedsAsyncTask.Type type, int value){
        if( type == RetrieveFeedsAsyncTask.Type.LOCAL){
            if( tabLayout.getTabAt(2) != null && display_local){
                View tabLocal = tabLayout.getTabAt(2).getCustomView();
                assert tabLocal != null;
                TextView tabCounterLocal = tabLocal.findViewById(R.id.tab_counter);
                tabCounterLocal.setText(String.valueOf(value));
                if( value > 0){
                    tabCounterLocal.setVisibility(View.VISIBLE);
                }else {
                    tabCounterLocal.setVisibility(View.GONE);
                }
            }
        }else if( type == RetrieveFeedsAsyncTask.Type.PUBLIC){
            if( tabLayout.getTabAt(3) != null && display_local){
                View tabPublic = tabLayout.getTabAt(3).getCustomView();
                assert tabPublic != null;
                TextView tabCounterPublic = tabPublic.findViewById(R.id.tab_counter);
                tabCounterPublic.setText(String.valueOf(value));
                if( value > 0){
                    tabCounterPublic.setVisibility(View.VISIBLE);
                }else {
                    tabCounterPublic.setVisibility(View.GONE);
                }
            }else if( tabLayout.getTabAt(2) != null && !display_local && display_global){
                View tabPublic = tabLayout.getTabAt(2).getCustomView();
                assert tabPublic != null;
                TextView tabCounterPublic = tabPublic.findViewById(R.id.tab_counter);
                tabCounterPublic.setText(String.valueOf(value));
                if( value > 0){
                    tabCounterPublic.setVisibility(View.VISIBLE);
                }else {
                    tabCounterPublic.setVisibility(View.GONE);
                }
            }
        }
    }

    public void updateNotifCounter(){
        if(tabLayout.getTabAt(1) == null)
            return;
        //noinspection ConstantConditions
        View tabNotif = tabLayout.getTabAt(1).getCustomView();
        if( tabNotif == null)
            return;
        TextView tabCounterNotif = tabNotif.findViewById(R.id.tab_counter);
        tabCounterNotif.setText(String.valueOf(countNewNotifications));
        if( countNewNotifications > 0){
            tabCounterNotif.setVisibility(View.VISIBLE);
        }else {
            tabCounterNotif.setVisibility(View.GONE);
        }
    }

    private static void activityResumed() {
        activityPaused = false;
    }

    private static void activityPaused() {
        activityPaused = true;
    }

    public static boolean activityState(){
        return activityPaused;
    }


    public void startSreaming(){
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        boolean liveNotifications = sharedpreferences.getBoolean(Helper.SET_LIVE_NOTIFICATIONS, true);
        if( liveNotifications) {
            ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            assert manager != null;
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (LiveNotificationService.class.getName().equals(service.service.getClassName())) {
                    return;
                }
            }
            Intent streamingIntent = new Intent(this, LiveNotificationService.class);
            startService(streamingIntent);
        }

    }

    public DisplayStatusFragment getHomeFragment(){
        return homeFragment;
    }
}