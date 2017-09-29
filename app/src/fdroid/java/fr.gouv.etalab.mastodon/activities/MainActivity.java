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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SwitchCompat;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Stack;
import java.util.regex.Matcher;

import fr.gouv.etalab.mastodon.asynctasks.RetrieveInstanceAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveMetaDataAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.UpdateAccountInfoByIDAsyncTask;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Notification;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.client.Entities.Version;
import fr.gouv.etalab.mastodon.client.PatchBaseImageDownloader;
import fr.gouv.etalab.mastodon.fragments.DisplayAccountsFragment;
import fr.gouv.etalab.mastodon.fragments.DisplayDraftsFragment;
import fr.gouv.etalab.mastodon.fragments.DisplayFollowRequestSentFragment;
import fr.gouv.etalab.mastodon.fragments.DisplayNotificationsFragment;
import fr.gouv.etalab.mastodon.fragments.DisplayScheduledTootsFragment;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveInstanceInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveMetaDataInterface;
import fr.gouv.etalab.mastodon.interfaces.OnUpdateAccountInfoInterface;
import fr.gouv.etalab.mastodon.services.StreamingFederatedTimelineService;
import fr.gouv.etalab.mastodon.services.StreamingService;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveAccountsAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveFeedsAsyncTask;
import fr.gouv.etalab.mastodon.fragments.DisplayStatusFragment;
import fr.gouv.etalab.mastodon.fragments.TabLayoutSettingsFragment;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import mastodon.etalab.gouv.fr.mastodon.R;

import static fr.gouv.etalab.mastodon.helper.Helper.CHANGE_THEME_INTENT;
import static fr.gouv.etalab.mastodon.helper.Helper.CHANGE_USER_INTENT;
import static fr.gouv.etalab.mastodon.helper.Helper.HOME_TIMELINE_INTENT;
import static fr.gouv.etalab.mastodon.helper.Helper.INTENT_ACTION;
import static fr.gouv.etalab.mastodon.helper.Helper.NOTIFICATION_INTENT;
import static fr.gouv.etalab.mastodon.helper.Helper.PREF_KEY_ID;
import static fr.gouv.etalab.mastodon.helper.Helper.changeDrawableColor;
import static fr.gouv.etalab.mastodon.helper.Helper.changeUser;
import static fr.gouv.etalab.mastodon.helper.Helper.menuAccounts;
import static fr.gouv.etalab.mastodon.helper.Helper.unCheckAllMenuItems;
import static fr.gouv.etalab.mastodon.helper.Helper.updateHeaderAccountInfo;
import android.support.v4.app.FragmentStatePagerAdapter;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnUpdateAccountInfoInterface, OnRetrieveMetaDataInterface, OnRetrieveInstanceInterface  {

    private FloatingActionButton toot;
    private HashMap<String, String> tagTile = new HashMap<>();
    private HashMap<String, Integer> tagItem = new HashMap<>();
    private TextView toolbarTitle;
    private SearchView toolbar_search;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private View headerLayout;
    public static String currentLocale;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private RelativeLayout main_app_container;
    private Stack<Integer> stackBack = new Stack<>();

    private DisplayStatusFragment homeFragment, federatedFragment;
    private DisplayNotificationsFragment notificationsFragment;
    private BroadcastReceiver receive_data, receive_federated_data;
    private boolean display_local, display_global;
    public static int countNewStatus = 0;
    public static int countNewNotifications = 0;
    private String userIdService;
    private Intent streamingIntent;
    public static String lastHomeId = null, lastNotificationId = null;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);



        final int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if( theme == Helper.THEME_LIGHT){
            setTheme(R.style.AppTheme_NoActionBar);
        }else {
            setTheme(R.style.AppThemeDark_NoActionBar);
        }
        setContentView(R.layout.activity_main);

        display_local = sharedpreferences.getBoolean(Helper.SET_DISPLAY_LOCAL, true);
        display_global = sharedpreferences.getBoolean(Helper.SET_DISPLAY_GLOBAL, true);

        //Test if user is still log in
        if( ! Helper.isLoggedIn(getApplicationContext())) {
            //It is not, the user is redirected to the login page
            Intent myIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(myIntent);
            finish();
            return;
        }
        SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        Helper.canPin = false;
        Helper.fillMapEmoji(getApplicationContext());
        //Here, the user is authenticated
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbarTitle  = (TextView) toolbar.findViewById(R.id.toolbar_title);
        toolbar_search = (SearchView) toolbar.findViewById(R.id.toolbar_search);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        TabLayout.Tab tabHome = tabLayout.newTab();
        TabLayout.Tab tabNotif = tabLayout.newTab();
        TabLayout.Tab tabLocal = tabLayout.newTab();
        TabLayout.Tab tabPublic = tabLayout.newTab();
        tabHome.setCustomView(R.layout.tab_badge);
        tabNotif.setCustomView(R.layout.tab_badge);
        tabLocal.setCustomView(R.layout.tab_badge);
        tabPublic.setCustomView(R.layout.tab_badge);

        @SuppressWarnings("ConstantConditions") @SuppressLint("CutPasteId")
        ImageView iconHome = (ImageView) tabHome.getCustomView().findViewById(R.id.tab_icon);
        iconHome.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_text), PorterDuff.Mode.SRC_IN);
        iconHome.setImageResource(R.drawable.ic_action_home_tl);

        @SuppressWarnings("ConstantConditions") @SuppressLint("CutPasteId")
        ImageView iconNotif = (ImageView) tabNotif.getCustomView().findViewById(R.id.tab_icon);
        iconNotif.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_text), PorterDuff.Mode.SRC_IN);
        iconNotif.setImageResource(R.drawable.ic_notifications_tl);


        @SuppressWarnings("ConstantConditions") @SuppressLint("CutPasteId")
        ImageView iconLocal = (ImageView) tabLocal.getCustomView().findViewById(R.id.tab_icon);
        iconLocal.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_text), PorterDuff.Mode.SRC_IN);
        iconLocal.setImageResource(R.drawable.ic_action_users_tl);

        @SuppressWarnings("ConstantConditions") @SuppressLint("CutPasteId")
        ImageView iconGlobal = (ImageView) tabPublic.getCustomView().findViewById(R.id.tab_icon);
        iconGlobal.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_text), PorterDuff.Mode.SRC_IN);
        iconGlobal.setImageResource(R.drawable.ic_action_globe_tl);

        changeDrawableColor(getApplicationContext(), R.drawable.ic_action_home_tl,R.color.dark_text);
        changeDrawableColor(getApplicationContext(), R.drawable.ic_notifications_tl,R.color.dark_text);
        changeDrawableColor(getApplicationContext(), R.drawable.ic_action_users_tl,R.color.dark_text);
        changeDrawableColor(getApplicationContext(), R.drawable.ic_action_globe_tl,R.color.dark_text);

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);

        tabLayout.addTab(tabHome);
        tabLayout.addTab(tabNotif);
        if( display_local)
            tabLayout.addTab(tabLocal);
        if( display_global)
            tabLayout.addTab(tabPublic);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        int countPage = 2;
        if( sharedpreferences.getBoolean(Helper.SET_DISPLAY_LOCAL, true))
            countPage++;
        if( sharedpreferences.getBoolean(Helper.SET_DISPLAY_GLOBAL, true))
            countPage++;
        viewPager.setOffscreenPageLimit(countPage);
        main_app_container = (RelativeLayout) findViewById(R.id.main_app_container);
        PagerAdapter adapter = new PagerAdapter
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
                Helper.switchLayout(MainActivity.this);
                if( tab.getPosition() != 1 )
                    toot.setVisibility(View.VISIBLE);
                else
                    toot.setVisibility(View.GONE);
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if( viewPager.getVisibility() == View.GONE){
                    viewPager.setVisibility(View.VISIBLE);
                    Helper.switchLayout(MainActivity.this);
                    main_app_container.setVisibility(View.GONE);
                    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                    drawer.closeDrawer(GravityCompat.START);
                }
                if( tab.getPosition() != 1 )
                    toot.setVisibility(View.VISIBLE);
                else
                    toot.setVisibility(View.GONE);
                Fragment fragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, tab.getPosition());
                switch (tab.getPosition()){
                    case 0:
                        DisplayStatusFragment displayStatusFragment = ((DisplayStatusFragment) fragment);
                        countNewStatus = 0;
                        updateHomeCounter();
                        if( displayStatusFragment != null )
                            displayStatusFragment.scrollToTop();
                        break;
                    case 2:
                    case 3:
                        displayStatusFragment = ((DisplayStatusFragment) fragment);
                        if( displayStatusFragment != null )
                            displayStatusFragment.scrollToTop();
                        break;
                    case 1:
                        DisplayNotificationsFragment displayNotificationsFragment = ((DisplayNotificationsFragment) fragment);
                        countNewNotifications = 0;
                        updateNotifCounter();
                        if( displayNotificationsFragment != null )
                            displayNotificationsFragment.scrollToTop();
                        break;
                }
            }
        });

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
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
                    Fragment fragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, pos);
                    switch (pos) {
                        case 0:
                        case 2:
                        case 3:
                            DisplayStatusFragment displayStatusFragment = ((DisplayStatusFragment) fragment);
                            if (displayStatusFragment != null)
                                displayStatusFragment.scrollToTop();
                            break;
                        case 1:
                            DisplayNotificationsFragment displayNotificationsFragment = ((DisplayNotificationsFragment) fragment);
                            if (displayNotificationsFragment != null)
                                displayNotificationsFragment.scrollToTop();
                            break;
                    }
                }
            }
        });


        toolbar_search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Hide keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(toolbar_search.getWindowToken(), 0);
                Intent intent = new Intent(MainActivity.this, SearchResultActivity.class);
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
        if( getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        //Defines the current locale of the device in a static variable
        currentLocale = Helper.currentLocale(getApplicationContext());

        toot = (FloatingActionButton) findViewById(R.id.toot);
        toot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), TootActivity.class);
                startActivity(intent);
            }
        });

        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        Account account = new AccountDAO(getApplicationContext(), db).getAccountByID(userId);

        //Image loader configuration
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
        options = new DisplayImageOptions.Builder().displayer(new RoundedBitmapDisplayer(90)).cacheInMemory(false)
                .cacheOnDisk(true).resetViewBeforeLoading(true).build();

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.setDrawerIndicatorEnabled(false);
        ImageView iconbar = (ImageView) toolbar.findViewById(R.id.iconbar);
        iconbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(Gravity.START);
            }
        });
        Helper.loadPictureIcon(MainActivity.this, account.getAvatar(),iconbar);
        headerLayout = navigationView.getHeaderView(0);
        mamageNewIntent(getIntent());
        final ImageView menuMore = (ImageView) headerLayout.findViewById(R.id.header_option_menu);
        menuMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(MainActivity.this, menuMore);
                popup.getMenuInflater()
                        .inflate(R.menu.main, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_logout:
                                Helper.logout(getApplicationContext());
                                Intent myIntent = new Intent(MainActivity.this, LoginActivity.class);
                                startActivity(myIntent);
                                finish();
                                return true;
                            case R.id.action_about:
                                Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
                                startActivity(intent);
                                return true;
                            case R.id.action_privacy:
                                intent = new Intent(getApplicationContext(), PrivacyActivity.class);
                                startActivity(intent);
                                return true;
                            case R.id.action_about_instance:
                                intent = new Intent(getApplicationContext(), InstanceActivity.class);
                                startActivity(intent);
                                return true;
                            case R.id.action_cache:
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle(R.string.cache_title);
                                long sizeCache = Helper.cacheSize(getCacheDir());
                                float cacheSize = 0;
                                if( sizeCache > 0 ) {
                                    if (sizeCache > 0) {
                                        cacheSize = (float) sizeCache / 1000000.0f;
                                    }
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
                                                Toast.makeText(MainActivity.this, getString(R.string.toast_cache_clear,String.format("%s %s", String.format(Locale.getDefault(), "%.2f", finalCacheSize), getString(R.string.cache_units))), Toast.LENGTH_LONG).show();
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

                                builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle(R.string.text_size);

                                @SuppressLint("InflateParams") View popup_quick_settings = getLayoutInflater().inflate( R.layout.popup_text_size, null );
                                builder.setView(popup_quick_settings);

                                SeekBar set_text_size = (SeekBar) popup_quick_settings.findViewById(R.id.set_text_size);
                                SeekBar set_icon_size = (SeekBar) popup_quick_settings.findViewById(R.id.set_icon_size);
                                final TextView set_text_size_value = (TextView) popup_quick_settings.findViewById(R.id.set_text_size_value);
                                final TextView set_icon_size_value = (TextView) popup_quick_settings.findViewById(R.id.set_icon_size_value);
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
                                        MainActivity.this.recreate();
                                        dialog.dismiss();
                                    }
                                })
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .show();
                                return true;
                            default:
                                return true;
                        }
                    }
                });
                popup.show();
            }
        });


        updateHeaderAccountInfo(MainActivity.this, account, headerLayout, imageLoader, options);
        //Locked account can see follow request
        if (account.isLocked()) {
            navigationView.getMenu().findItem(R.id.nav_follow_request).setVisible(true);
        } else {
            navigationView.getMenu().findItem(R.id.nav_follow_request).setVisible(false);
        }

        LinearLayout owner_container = (LinearLayout) headerLayout.findViewById(R.id.main_header_container);
        owner_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuAccounts(MainActivity.this);
            }
        });



        boolean popupShown = sharedpreferences.getBoolean(Helper.SET_POPUP_PUSH, false);
        if(!popupShown){
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
            LayoutInflater inflater = getLayoutInflater();
            @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.popup_quick_settings, null);
            dialogBuilder.setView(dialogView);

            final SwitchCompat set_push_hometimeline = (SwitchCompat) dialogView.findViewById(R.id.set_push_hometimeline);
            final SwitchCompat set_push_notification = (SwitchCompat) dialogView.findViewById(R.id.set_push_notification);
            boolean notif_hometimeline = sharedpreferences.getBoolean(Helper.SET_NOTIF_HOMETIMELINE, true);
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
        Helper.switchLayout(MainActivity.this);

        // Retrieves instance
        new RetrieveInstanceAsyncTask(getApplicationContext(), MainActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
    private boolean mamageNewIntent(Intent intent){
        if( intent == null || intent.getExtras() == null )
            return false;

        String action = intent.getAction();
        String type = intent.getType();
        Bundle extras = intent.getExtras();
        String userIdIntent;
        boolean matchingIntent = false;
        if( extras.containsKey(INTENT_ACTION) ){
            final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            userIdIntent = extras.getString(PREF_KEY_ID); //Id of the account in the intent
            if (extras.getInt(INTENT_ACTION) == NOTIFICATION_INTENT){
                changeUser(MainActivity.this, userIdIntent, false); //Connects the account which is related to the notification
                unCheckAllMenuItems(navigationView);
                if( tabLayout.getTabAt(1) != null)
                    //noinspection ConstantConditions
                    tabLayout.getTabAt(1).select();
                matchingIntent = true;
            }else if( extras.getInt(INTENT_ACTION) == HOME_TIMELINE_INTENT){
                changeUser(MainActivity.this, userIdIntent, true); //Connects the account which is related to the notification
            }else if( extras.getInt(INTENT_ACTION) == CHANGE_THEME_INTENT){
                unCheckAllMenuItems(navigationView);
                navigationView.setCheckedItem(R.id.nav_settings);
                navigationView.getMenu().performIdentifierAction(R.id.nav_settings, 0);
                toolbarTitle.setText(R.string.settings);
                matchingIntent = true;
            }else if( extras.getInt(INTENT_ACTION) == CHANGE_USER_INTENT){
                unCheckAllMenuItems(navigationView);
                if( tabLayout.getTabAt(0) != null)
                    //noinspection ConstantConditions
                    tabLayout.getTabAt(0).select();
                matchingIntent = true;
            }
        }else if( Intent.ACTION_SEND.equals(action) && type != null ) {
            if ("text/plain".equals(type)) {
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
                        sharedText = sharedText.substring(matchStart, matchEnd);
                    }
                    new RetrieveMetaDataAsyncTask(sharedText, MainActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    Intent intentToot = new Intent(getApplicationContext(), TootActivity.class);
                    Bundle b = new Bundle();
                    b.putString("sharedSubject", sharedSubject);
                    b.putString("sharedContent", sharedText);
                    intentToot.putExtras(b);
                    startActivity(intentToot);
                }

            } else if (type.startsWith("image/")) {

                Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);

                if (imageUri != null) {

                    Intent intentToot = new Intent(getApplicationContext(), TootActivity.class);
                    Bundle b = new Bundle();

                    b.putParcelable("sharedUri", imageUri);
                    b.putInt("uriNumber", 1);
                    intentToot.putExtras(b);
                    startActivity(intentToot);
                }
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null ) {
            if (type.startsWith("image/")) {

                ArrayList<Uri> imageList = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                if (imageList != null) {
                    Intent intentToot = new Intent(getApplicationContext(), TootActivity.class);
                    Bundle b = new Bundle();

                    b.putParcelableArrayList("sharedUri", imageList);
                    b.putInt("uriNumber", imageList.size());
                    intentToot.putExtras(b);
                    startActivity(intentToot);
                }
            }
        }
        intent.replaceExtras(new Bundle());
        intent.setAction("");
        intent.setData(null);
        intent.setFlags(0);
        return matchingIntent;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
                Helper.switchLayout(MainActivity.this);
                main_app_container.setVisibility(View.GONE);
                viewPager.setVisibility(View.VISIBLE);
                tabLayout.setVisibility(View.VISIBLE);
                toolbarTitle.setVisibility(View.GONE);
                final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                unCheckAllMenuItems(navigationView);
                toot.setVisibility(View.VISIBLE);
                //Manages theme for icon colors
                SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
                if( theme == Helper.THEME_DARK){
                    changeDrawableColor(getApplicationContext(), R.drawable.ic_reply,R.color.dark_text);
                    changeDrawableColor(getApplicationContext(), R.drawable.ic_action_more,R.color.dark_text);
                    changeDrawableColor(getApplicationContext(), R.drawable.ic_action_globe,R.color.dark_text);
                    changeDrawableColor(getApplicationContext(), R.drawable.ic_action_lock_open,R.color.dark_text);
                    changeDrawableColor(getApplicationContext(), R.drawable.ic_action_lock_closed,R.color.dark_text);
                    changeDrawableColor(getApplicationContext(), R.drawable.ic_local_post_office,R.color.dark_text);
                    changeDrawableColor(getApplicationContext(), R.drawable.ic_retweet_black,R.color.dark_text);
                    changeDrawableColor(getApplicationContext(), R.drawable.ic_fav_black,R.color.dark_text);
                    changeDrawableColor(getApplicationContext(), R.drawable.ic_photo,R.color.dark_text);
                    changeDrawableColor(getApplicationContext(), R.drawable.ic_remove_red_eye,R.color.dark_text);
                    changeDrawableColor(getApplicationContext(), R.drawable.ic_translate,R.color.dark_text);
                }else {
                    changeDrawableColor(getApplicationContext(), R.drawable.ic_reply,R.color.black);
                    changeDrawableColor(getApplicationContext(), R.drawable.ic_action_more,R.color.black);
                    changeDrawableColor(getApplicationContext(), R.drawable.ic_action_globe,R.color.black);
                    changeDrawableColor(getApplicationContext(), R.drawable.ic_action_lock_open,R.color.black);
                    changeDrawableColor(getApplicationContext(), R.drawable.ic_action_lock_closed,R.color.black);
                    changeDrawableColor(getApplicationContext(), R.drawable.ic_local_post_office,R.color.black);
                    changeDrawableColor(getApplicationContext(), R.drawable.ic_retweet_black,R.color.black);
                    changeDrawableColor(getApplicationContext(), R.drawable.ic_fav_black,R.color.black);
                    changeDrawableColor(getApplicationContext(), R.drawable.ic_photo,R.color.white);
                    changeDrawableColor(getApplicationContext(), R.drawable.ic_remove_red_eye,R.color.white);
                    changeDrawableColor(getApplicationContext(), R.drawable.ic_translate,R.color.white);
                }
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
        MainActivity.activityResumed();
        updateNotifCounter();
        updateHomeCounter();
        //Proceeds to update of the authenticated account
        if(Helper.isLoggedIn(getApplicationContext()))
            new UpdateAccountInfoByIDAsyncTask(getApplicationContext(), MainActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        receive_federated_data = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle b = intent.getExtras();
                userIdService = b.getString("userIdService", null);
                String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
                if( userIdService != null && userIdService.equals(userId)) {
                    Status status = b.getParcelable("data");
                    if (federatedFragment != null) {
                        federatedFragment.refresh(status);
                    }
                }
            }
        };
        receive_data = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle b = intent.getExtras();
                StreamingService.EventStreaming eventStreaming = (StreamingService.EventStreaming) intent.getSerializableExtra("eventStreaming");
                userIdService = b.getString("userIdService", null);
                String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
                if( userIdService != null && userIdService.equals(userId)) {
                    if (eventStreaming == StreamingService.EventStreaming.NOTIFICATION) {
                        Notification notification = b.getParcelable("data");
                        if (notificationsFragment != null) {
                            notificationsFragment.refresh(notification);
                        }
                    } else if (eventStreaming == StreamingService.EventStreaming.UPDATE) {
                        Status status = b.getParcelable("data");
                        if (homeFragment != null) {
                            homeFragment.refresh(status);
                        }
                    } else if (eventStreaming == StreamingService.EventStreaming.DELETE) {
                        String id = b.getString("id");
                        if (notificationsFragment != null) {
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
        streamingIntent = new Intent(this, StreamingService.class);
        startService(streamingIntent);
        LocalBroadcastManager.getInstance(this).registerReceiver(receive_data, new IntentFilter(Helper.RECEIVE_DATA));
        LocalBroadcastManager.getInstance(this).registerReceiver(receive_federated_data, new IntentFilter(Helper.RECEIVE_FEDERATED_DATA));
    }

    @Override
    public void onStop(){
        super.onStop();
        if( streamingIntent != null) {
            SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
            editor.putBoolean(Helper.SHOULD_CONTINUE_STREAMING_FEDERATED+userId, false);
            editor.apply();
        }
        if( receive_data != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receive_data);
        if( receive_federated_data != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receive_federated_data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MainActivity.activityPaused();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if( id == R.id.nav_remote_follow){
            Intent remoteFollow = new Intent(getApplicationContext(), RemoteFollowActivity.class);
            startActivity(remoteFollow);
            return false;
        }
        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        unCheckAllMenuItems(navigationView);
        item.setChecked(true);
        //Remove the search bar
        if( !toolbar_search.isIconified() ) {
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
        if (id == R.id.nav_settings) {
            toot.setVisibility(View.GONE);
            TabLayoutSettingsFragment tabLayoutSettingsFragment= new TabLayoutSettingsFragment();
            fragmentTag = "TABLAYOUT_SETTINGS";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, tabLayoutSettingsFragment, fragmentTag).commit();

        } else if (id == R.id.nav_favorites) {
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
            toot.setVisibility(View.VISIBLE);
            DisplayDraftsFragment displayDraftsFragment = new DisplayDraftsFragment();
            fragmentTag = "DRAFTS";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, displayDraftsFragment, fragmentTag).commit();
        }else if( id == R.id.nav_follow_request){
            toot.setVisibility(View.GONE);
            DisplayFollowRequestSentFragment followRequestSentFragment = new DisplayFollowRequestSentFragment();
            fragmentTag = "FOLLOW_REQUEST_SENT";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, followRequestSentFragment, fragmentTag).commit();
        }

        populateTitleWithTag(fragmentTag, item.getTitle().toString(), item.getItemId());
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
            Intent myIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(myIntent);
            finish();
        }else {
            SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
            SQLiteDatabase db = Sqlite.getInstance(MainActivity.this, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
            Account account = new AccountDAO(getApplicationContext(), db).getAccountByID(userId);
            updateHeaderAccountInfo(MainActivity.this, account, headerLayout, imageLoader, options);
        }
    }


    @Override
    public void onRetrieveMetaData(boolean error, String image, String title, String description) {
        if( !error) {
            Intent intentSendImage = new Intent(Helper.RECEIVE_PICTURE);
            intentSendImage.putExtra("image", image);
            intentSendImage.putExtra("title", title);
            intentSendImage.putExtra("description", description);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentSendImage);
        }
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
        Helper.canPin = (currentVersion.compareTo(minVersion) == 1 || currentVersion.equals(minVersion));
    }


    /**
     * Page Adapter for settings
     */
    private class PagerAdapter extends FragmentStatePagerAdapter  {
        int mNumOfTabs;

        private PagerAdapter(FragmentManager fm, int NumOfTabs) {
            super(fm);
            this.mNumOfTabs = NumOfTabs;
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
            }else if( position == 2 && !display_local){
                    statusFragment = new DisplayStatusFragment();
                    bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.PUBLIC);
                    statusFragment.setArguments(bundle);
                    return statusFragment;
            }else if (position == 3){
                statusFragment = new DisplayStatusFragment();
                bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.PUBLIC);
                statusFragment.setArguments(bundle);
                return statusFragment;
            }
            return null;
        }

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


    public void updateHomeCounter(){
        if( tabLayout.getTabAt(0) == null )
            return;
        //noinspection ConstantConditions
        View tabHome = tabLayout.getTabAt(0).getCustomView();
        if( tabHome == null)
            return;
        TextView tabCounterHome = (TextView) tabHome.findViewById(R.id.tab_counter);
        tabCounterHome.setText(String.valueOf(countNewStatus));
        if( countNewStatus> 0){
            //New data are available
            //The fragment is not displayed, so the counter is displayed
            tabCounterHome.setVisibility(View.VISIBLE);
        }else {
            tabCounterHome.setVisibility(View.GONE);
        }
    }

    public void updateNotifCounter(){
        if(tabLayout.getTabAt(1) == null)
            return;
        //noinspection ConstantConditions
        View tabNotif = tabLayout.getTabAt(1).getCustomView();
        if( tabNotif == null)
            return;
        TextView tabCounterNotif = (TextView) tabNotif.findViewById(R.id.tab_counter);
        tabCounterNotif.setText(String.valueOf(countNewNotifications));
        if( countNewNotifications > 0){
            tabCounterNotif.setVisibility(View.VISIBLE);
        }else {
            tabCounterNotif.setVisibility(View.GONE);
        }
    }

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    private static void activityResumed() {
        activityVisible = true;
    }

    private static void activityPaused() {
        activityVisible = false;
    }

    private static boolean activityVisible;
}
