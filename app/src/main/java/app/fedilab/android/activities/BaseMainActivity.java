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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import android.text.Html;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import app.fedilab.android.BuildConfig;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Filters;
import app.fedilab.android.client.Entities.Instance;
import app.fedilab.android.client.Entities.ManageTimelines;
import app.fedilab.android.client.Entities.Results;
import app.fedilab.android.client.Entities.Status;
import app.fedilab.android.client.Entities.TagTimeline;
import app.fedilab.android.client.Entities.Version;
import app.fedilab.android.fragments.DisplayAccountsFragment;
import app.fedilab.android.fragments.DisplayBookmarksFragment;
import app.fedilab.android.fragments.DisplayDraftsFragment;
import app.fedilab.android.fragments.DisplayFavoritesPeertubeFragment;
import app.fedilab.android.fragments.DisplayFiltersFragment;
import app.fedilab.android.fragments.DisplayFollowRequestSentFragment;
import app.fedilab.android.fragments.DisplayHowToFragment;
import app.fedilab.android.fragments.DisplayListsFragment;
import app.fedilab.android.fragments.DisplayMutedInstanceFragment;
import app.fedilab.android.fragments.DisplayNotificationsFragment;
import app.fedilab.android.fragments.DisplayPeertubeNotificationsFragment;
import app.fedilab.android.fragments.DisplayPlaylistsFragment;
import app.fedilab.android.fragments.DisplayStatusFragment;
import app.fedilab.android.fragments.SettingsPeertubeFragment;
import app.fedilab.android.fragments.TabLayoutNotificationsFragment;
import app.fedilab.android.fragments.TabLayoutScheduleFragment;
import app.fedilab.android.fragments.WhoToFollowFragment;
import app.fedilab.android.helper.CrossActions;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.helper.MenuFloating;
import app.fedilab.android.services.BackupStatusService;
import app.fedilab.android.services.LiveNotificationService;
import app.fedilab.android.sqlite.AccountDAO;
import app.fedilab.android.sqlite.Sqlite;
import app.fedilab.android.sqlite.TempMuteDAO;
import app.fedilab.android.sqlite.TimelineCacheDAO;
import app.fedilab.android.sqlite.TimelinesDAO;
import es.dmoral.toasty.Toasty;
import app.fedilab.android.R;
import app.fedilab.android.asynctasks.ManageFiltersAsyncTask;
import app.fedilab.android.asynctasks.RetrieveAccountsAsyncTask;
import app.fedilab.android.asynctasks.RetrieveFeedsAsyncTask;
import app.fedilab.android.asynctasks.RetrieveInstanceAsyncTask;
import app.fedilab.android.asynctasks.RetrieveMetaDataAsyncTask;
import app.fedilab.android.asynctasks.RetrievePeertubeInformationAsyncTask;
import app.fedilab.android.asynctasks.RetrieveRemoteDataAsyncTask;
import app.fedilab.android.asynctasks.SyncTimelinesAsyncTask;
import app.fedilab.android.asynctasks.UpdateAccountInfoAsyncTask;
import app.fedilab.android.asynctasks.UpdateAccountInfoByIDAsyncTask;
import app.fedilab.android.interfaces.OnFilterActionInterface;
import app.fedilab.android.interfaces.OnRetrieveEmojiAccountInterface;
import app.fedilab.android.interfaces.OnRetrieveInstanceInterface;
import app.fedilab.android.interfaces.OnRetrieveMetaDataInterface;
import app.fedilab.android.interfaces.OnRetrieveRemoteAccountInterface;
import app.fedilab.android.interfaces.OnSyncTimelineInterface;
import app.fedilab.android.interfaces.OnUpdateAccountInfoInterface;

import static app.fedilab.android.asynctasks.ManageFiltersAsyncTask.action.GET_ALL_FILTER;
import static app.fedilab.android.helper.Helper.changeDrawableColor;


public abstract class BaseMainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnUpdateAccountInfoInterface, OnRetrieveMetaDataInterface, OnRetrieveInstanceInterface, OnRetrieveRemoteAccountInterface, OnRetrieveEmojiAccountInterface, OnFilterActionInterface, OnSyncTimelineInterface {


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
    public static List<Filters> filters = new ArrayList<>();
    public static int countNewStatus;
    public static int countNewNotifications;
    public static String lastHomeId = null, lastNotificationId = null;
    private AppBarLayout appBar;
    private String userId;
    private String instance;
    private PagerAdapter adapter;
    private ImageView delete_instance, display_timeline;
    public static String displayPeertube = null;
    private int style;
    private Activity activity;
    public static UpdateAccountInfoAsyncTask.SOCIAL social;
    private final int PICK_IMPORT = 5556;
    private List<ManageTimelines> timelines;
    private BroadcastReceiver hidde_menu;

    public static HashMap<Integer, Fragment> mPageReferenceMap = new HashMap<>();
    private static boolean notificationChecked = false;
    public static HashMap<String, Integer> poll_limits = new HashMap<>();
    private Instance instanceClass;
    public static List<String> mutedAccount = new ArrayList<>();
    public static String regex_home, regex_local, regex_public;
    public static boolean show_boosts, show_replies, show_art_nsfw;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);


        userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        instance = sharedpreferences.getString(Helper.PREF_INSTANCE, Helper.getLiveInstance(getApplicationContext()));
        SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        boolean displayFollowInstance = sharedpreferences.getBoolean(Helper.SET_DISPLAY_FOLLOW_INSTANCE, true);
        Account account = new AccountDAO(getApplicationContext(), db).getUniqAccount(userId, instance);
        if( account == null){
            Helper.logout(getApplicationContext());
            Intent myIntent = new Intent(BaseMainActivity.this, LoginActivity.class);
            startActivity(myIntent);
            finish();
            return;
        }

        //Update the static variable which manages account type
        if( account.getSocial() == null || account.getSocial().equals("MASTODON"))
            social = UpdateAccountInfoAsyncTask.SOCIAL.MASTODON;
        else if( account.getSocial().equals("PEERTUBE"))
            social = UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE;
        else if( account.getSocial().equals("PIXELFED"))
            social = UpdateAccountInfoAsyncTask.SOCIAL.PIXELFED;
        else if( account.getSocial().equals("PLEROMA"))
            social = UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA;
        else if( account.getSocial().equals("GNU"))
            social = UpdateAccountInfoAsyncTask.SOCIAL.GNU;
        else if( account.getSocial().equals("FRIENDICA"))
            social = UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA;
        countNewStatus = 0;
        countNewNotifications = 0;


        regex_home = sharedpreferences.getString(Helper.SET_FILTER_REGEX_HOME, null);
        regex_local = sharedpreferences.getString(Helper.SET_FILTER_REGEX_LOCAL, null);
        regex_public = sharedpreferences.getString(Helper.SET_FILTER_REGEX_PUBLIC, null);
        show_art_nsfw = sharedpreferences.getBoolean(Helper.SET_ART_WITH_NSFW, false);
        show_boosts = sharedpreferences.getBoolean(Helper.SET_SHOW_BOOSTS, true);
        show_replies = sharedpreferences.getBoolean(Helper.SET_SHOW_REPLIES, true);

        if (!isTaskRoot()
                && getIntent().hasCategory(Intent.CATEGORY_LAUNCHER)
                && getIntent().getAction() != null
                && getIntent().getAction().equals(Intent.ACTION_MAIN)) {

            finish();
            return;
        }
        final int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        switch (theme){
            case Helper.THEME_LIGHT:
                setTheme(R.style.AppTheme_NoActionBar_Fedilab);
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

        //Test if user is still log in
        if( ! Helper.isLoggedIn(getApplicationContext())) {
            //It is not, the user is redirected to the login page
            Intent myIntent = new Intent(BaseMainActivity.this, LoginActivity.class);
            startActivity(myIntent);
            finish();
            return;
        }
        activity = this;
        rateThisApp();

        //Intialize Peertube information
        //This task will allow to instance a static PeertubeInformation class
        if( social == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE){
            try{
                new RetrievePeertubeInformationAsyncTask(getApplicationContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }catch (Exception ignored){}
        }
        //For old Mastodon releases that can't pin, this support could be removed
        Helper.canPin = false;
        Helper.fillMapEmoji(getApplicationContext());
        //Here, the user is authenticated
        appBar = findViewById(R.id.appBar);
        Toolbar toolbar = findViewById(R.id.toolbar);
        if( theme == Helper.THEME_BLACK)
            toolbar.setBackgroundColor(ContextCompat.getColor(BaseMainActivity.this, R.color.black));
        setSupportActionBar(toolbar);
        toolbarTitle  = toolbar.findViewById(R.id.toolbar_title);
        toolbar_search = toolbar.findViewById(R.id.toolbar_search);
        delete_instance = findViewById(R.id.delete_instance);
        display_timeline = findViewById(R.id.display_timeline);
        if( theme == Helper.THEME_LIGHT) {
            ImageView icon = toolbar_search.findViewById(R.id.search_button);
            ImageView close = toolbar_search.findViewById(R.id.search_close_btn);
            if( icon != null)
                icon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_icon));
            if( close != null)
                close.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_icon));
            EditText editText = toolbar_search.findViewById(R.id.search_src_text);
            editText.setHintTextColor(getResources().getColor(R.color.dark_icon));
            editText.setTextColor(getResources().getColor(R.color.dark_icon));
            changeDrawableColor(BaseMainActivity.this,delete_instance, R.color.dark_icon);
            changeDrawableColor(BaseMainActivity.this,display_timeline, R.color.dark_icon);
        }

        tabLayout = findViewById(R.id.tabLayout);

        viewPager = findViewById(R.id.viewpager);

        display_timeline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( timelines == null || timelines.size() <= 0 ){
                    return;
                }
                PopupMenu popup = new PopupMenu(BaseMainActivity.this, display_timeline);
                try {
                    @SuppressLint("PrivateApi") Method method = popup.getMenu().getClass().getDeclaredMethod("setOptionalIconsVisible", boolean.class);
                    method.setAccessible(true);
                    method.invoke(popup.getMenu(), true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                int i = 0;
                for (ManageTimelines tl : timelines) {
                    MenuItem item = null;
                    switch (tl.getType()){
                        case LIST:
                            item = popup.getMenu().add(0, 0, Menu.NONE, tl.getListTimeline().getTitle());
                            item.setIcon(R.drawable.ic_list_top_menu);
                            break;
                        case TAG:
                            String name = (tl.getTagTimeline().getDisplayname()!= null && tl.getTagTimeline().getDisplayname().length() > 0)?tl.getTagTimeline().getDisplayname():tl.getTagTimeline().getName();
                            item = popup.getMenu().add(0, 0, Menu.NONE, name);
                            item.setIcon(R.drawable.ic_label_top_menu);
                            break;
                        case INSTANCE:
                            item = popup.getMenu().add(0, 0, Menu.NONE, tl.getRemoteInstance().getHost());
                            switch (tl.getRemoteInstance().getType()) {
                                case "MASTODON":
                                    item.setIcon(R.drawable.mastodon_icon_item);
                                    break;
                                case "PEERTUBE":
                                    item.setIcon(R.drawable.peertube_icon);
                                    break;
                                case "GNU":
                                    item.setIcon(R.drawable.ic_gnu_social);
                                    break;
                                case "MISSKEY":
                                    item.setIcon(R.drawable.misskey);
                                    break;
                                case "PIXELFED":
                                    item.setIcon(R.drawable.pixelfed);
                                    break;
                            }
                            break;
                    }
                    if( item != null){
                        int finalI = i;
                        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                if( finalI < tabLayout.getTabCount() && tabLayout.getTabAt(finalI) != null) {
                                    tabLayout.getTabAt(finalI).select();
                                }
                                return false;
                            }
                        });
                    }
                    i++;
                }
                popup.show();
            }
        });

        final NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Helper.hideMenuItem(navigationView.getMenu());


        toot = findViewById(R.id.toot);
        tootShow();
        delete_all = findViewById(R.id.delete_all);
        add_new = findViewById(R.id.add_new);

        main_app_container = findViewById(R.id.main_app_container);
        if( social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON || social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA || social == UpdateAccountInfoAsyncTask.SOCIAL.GNU || social == UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA) {
            new SyncTimelinesAsyncTask(BaseMainActivity.this, 0, BaseMainActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        }else if (social == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE){
            TabLayout.Tab pTabsub = tabLayout.newTab();
            TabLayout.Tab pTabOver = tabLayout.newTab();
            TabLayout.Tab pTabTrend = tabLayout.newTab();
            TabLayout.Tab pTabAdded = tabLayout.newTab();
            TabLayout.Tab pTabLocal = tabLayout.newTab();

            pTabsub.setCustomView(R.layout.tab_badge);
            pTabOver.setCustomView(R.layout.tab_badge);
            pTabTrend.setCustomView(R.layout.tab_badge);
            pTabAdded.setCustomView(R.layout.tab_badge);
            pTabLocal.setCustomView(R.layout.tab_badge);


            @SuppressWarnings("ConstantConditions") @SuppressLint("CutPasteId")
            ImageView iconSub = pTabsub.getCustomView().findViewById(R.id.tab_icon);

            iconSub.setImageResource(R.drawable.ic_subscriptions);

            if (theme == Helper.THEME_BLACK)
                iconSub.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_icon), PorterDuff.Mode.SRC_IN);
            else
                iconSub.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.mastodonC4), PorterDuff.Mode.SRC_IN);


            @SuppressWarnings("ConstantConditions") @SuppressLint("CutPasteId")
            ImageView iconOver = pTabOver.getCustomView().findViewById(R.id.tab_icon);
            iconOver.setImageResource(R.drawable.ic_overview);


            @SuppressWarnings("ConstantConditions") @SuppressLint("CutPasteId")
            ImageView iconTrend = pTabTrend.getCustomView().findViewById(R.id.tab_icon);
            iconTrend.setImageResource(R.drawable.ic_trending_up);

            @SuppressWarnings("ConstantConditions") @SuppressLint("CutPasteId")
            ImageView iconAdded = pTabAdded.getCustomView().findViewById(R.id.tab_icon);
            iconAdded.setImageResource(R.drawable.ic_recently_added);

            @SuppressWarnings("ConstantConditions") @SuppressLint("CutPasteId")
            ImageView iconLocal = pTabLocal.getCustomView().findViewById(R.id.tab_icon);
            iconLocal.setImageResource(R.drawable.ic_home);



            iconSub.setContentDescription(getString(R.string.subscriptions));
            iconOver.setContentDescription(getString(R.string.overview));
            iconTrend.setContentDescription(getString(R.string.trending));
            iconAdded.setContentDescription(getString(R.string.recently_added));
            iconLocal.setContentDescription(getString(R.string.local));


            if (theme == Helper.THEME_LIGHT) {
                iconSub.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.action_light_header), PorterDuff.Mode.SRC_IN);
                iconOver.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.action_light_header), PorterDuff.Mode.SRC_IN);
                iconTrend.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.action_light_header), PorterDuff.Mode.SRC_IN);
                iconAdded.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.action_light_header), PorterDuff.Mode.SRC_IN);
                iconLocal.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.action_light_header), PorterDuff.Mode.SRC_IN);
            } else {
                iconSub.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_text), PorterDuff.Mode.SRC_IN);
                iconOver.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_text), PorterDuff.Mode.SRC_IN);
                iconTrend.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_text), PorterDuff.Mode.SRC_IN);
                iconAdded.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_text), PorterDuff.Mode.SRC_IN);
                iconLocal.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_text), PorterDuff.Mode.SRC_IN);
            }

            toot.setImageResource(R.drawable.ic_cloud_upload);

            tabLayout.addTab(pTabsub);
            tabLayout.addTab(pTabOver);
            tabLayout.addTab(pTabTrend);
            tabLayout.addTab(pTabAdded);
            tabLayout.addTab(pTabLocal);




            adapter = new PagerAdapter
                    (getSupportFragmentManager(), tabLayout.getTabCount());
            viewPager.setAdapter(adapter);
            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    viewPager.setCurrentItem(tab.getPosition());
                    main_app_container.setVisibility(View.GONE);
                    viewPager.setVisibility(View.VISIBLE);
                    manageTimelineList(true);
                    delete_instance.setVisibility(View.GONE);
                    Helper.switchLayout(BaseMainActivity.this);
                    tootShow();
                    DrawerLayout drawer = findViewById(R.id.drawer_layout);
                    drawer.closeDrawer(GravityCompat.START);
                    if( tab.getCustomView() != null) {
                        ImageView icon = tab.getCustomView().findViewById(R.id.tab_icon);
                        if( icon != null)
                            if( theme == Helper.THEME_BLACK)
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
                            if( theme == Helper.THEME_LIGHT)
                                icon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_icon), PorterDuff.Mode.SRC_IN);
                            else
                                icon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_text), PorterDuff.Mode.SRC_IN);
                    }
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                    if( tab.getCustomView() != null) {
                        ImageView icon = tab.getCustomView().findViewById(R.id.tab_icon);
                        if( icon != null)
                            if( theme == Helper.THEME_BLACK)
                                icon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_icon), PorterDuff.Mode.SRC_IN);
                            else
                                icon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.mastodonC4), PorterDuff.Mode.SRC_IN);
                        if( viewPager.getAdapter() != null) {
                            Fragment fragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, tab.getPosition());
                            DisplayStatusFragment displayStatusFragment = ((DisplayStatusFragment) fragment);
                            displayStatusFragment.scrollToTop();
                        }
                    }
                }
            });

            //Scroll to top when top bar is clicked for favourites/blocked/muted
            toolbarTitle.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Fragment fragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, tabLayout.getSelectedTabPosition());
                    DisplayStatusFragment displayStatusFragment = ((DisplayStatusFragment) fragment);
                    displayStatusFragment.scrollToTop();
                }
            });
        }else if (social == UpdateAccountInfoAsyncTask.SOCIAL.PIXELFED){
            TabLayout.Tab pfTabHome = tabLayout.newTab();
            TabLayout.Tab pfTabLocal = tabLayout.newTab();
            TabLayout.Tab pfTabNotification = tabLayout.newTab();
            //TabLayout.Tab pfTabDiscover = tabLayout.newTab();



            pfTabHome.setCustomView(R.layout.tab_badge);
            pfTabLocal.setCustomView(R.layout.tab_badge);
            pfTabNotification.setCustomView(R.layout.tab_badge);
            //pfTabDiscover.setCustomView(R.layout.tab_badge);


            @SuppressWarnings("ConstantConditions") @SuppressLint("CutPasteId")
            ImageView iconHome = pfTabHome.getCustomView().findViewById(R.id.tab_icon);

            iconHome.setImageResource(R.drawable.ic_home);

            if (theme == Helper.THEME_BLACK)
                iconHome.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_icon), PorterDuff.Mode.SRC_IN);
            else
                iconHome.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.mastodonC4), PorterDuff.Mode.SRC_IN);


            @SuppressWarnings("ConstantConditions") @SuppressLint("CutPasteId")
            ImageView iconLocal = pfTabLocal.getCustomView().findViewById(R.id.tab_icon);
            iconLocal.setImageResource(R.drawable.ic_people);


              @SuppressWarnings("ConstantConditions") @SuppressLint("CutPasteId")
            ImageView iconNotif = pfTabNotification.getCustomView().findViewById(R.id.tab_icon);
            iconNotif.setImageResource(R.drawable.ic_notifications);

            /*@SuppressWarnings("ConstantConditions") @SuppressLint("CutPasteId")
            ImageView iconDiscover = pfTabDiscover.getCustomView().findViewById(R.id.tab_icon);
            iconDiscover.setImageResource(R.drawable.ic_people);*/



            iconHome.setContentDescription(getString(R.string.home_menu));
           // iconDiscover.setContentDescription(getString(R.string.overview));
            iconLocal.setContentDescription(getString(R.string.local));
            iconNotif.setContentDescription(getString(R.string.notifications));

            if (theme == Helper.THEME_LIGHT) {
                iconHome.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.action_light_header), PorterDuff.Mode.SRC_IN);
              //  iconDiscover.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.action_light_header), PorterDuff.Mode.SRC_IN);
                iconLocal.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.action_light_header), PorterDuff.Mode.SRC_IN);
                iconNotif.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.action_light_header), PorterDuff.Mode.SRC_IN);
            } else {
                iconHome.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_text), PorterDuff.Mode.SRC_IN);
              //  iconDiscover.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_text), PorterDuff.Mode.SRC_IN);
                iconLocal.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_text), PorterDuff.Mode.SRC_IN);
                iconNotif.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_text), PorterDuff.Mode.SRC_IN);
            }



            tabLayout.addTab(pfTabHome);
            tabLayout.addTab(pfTabLocal);
            tabLayout.addTab(pfTabNotification);
        //    tabLayout.addTab(pfTabDiscover);
            tabLayout.setTabMode(TabLayout.MODE_FIXED);
            tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

            adapter = new PagerAdapter
                    (getSupportFragmentManager(), tabLayout.getTabCount());
            viewPager.setAdapter(adapter);
            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    viewPager.setCurrentItem(tab.getPosition());
                    main_app_container.setVisibility(View.GONE);
                    viewPager.setVisibility(View.VISIBLE);
                    manageTimelineList(true);
                    delete_instance.setVisibility(View.GONE);
                    Helper.switchLayout(BaseMainActivity.this);
                    tootShow();
                    DrawerLayout drawer = findViewById(R.id.drawer_layout);
                    drawer.closeDrawer(GravityCompat.START);
                    if( tab.getCustomView() != null) {
                        ImageView icon = tab.getCustomView().findViewById(R.id.tab_icon);
                        if( icon != null)
                            if( theme == Helper.THEME_BLACK)
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
                            if( theme == Helper.THEME_LIGHT)
                                icon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_icon), PorterDuff.Mode.SRC_IN);
                            else
                                icon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_text), PorterDuff.Mode.SRC_IN);
                    }
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                    if( tab.getCustomView() != null) {
                        ImageView icon = tab.getCustomView().findViewById(R.id.tab_icon);
                        if( icon != null)
                            if( theme == Helper.THEME_BLACK)
                                icon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_icon), PorterDuff.Mode.SRC_IN);
                            else
                                icon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.mastodonC4), PorterDuff.Mode.SRC_IN);
                    }
                }
            });

            //Scroll to top when top bar is clicked for favourites/blocked/muted
            toolbarTitle.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Fragment fragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, tabLayout.getSelectedTabPosition());
                    DisplayStatusFragment displayStatusFragment = ((DisplayStatusFragment) fragment);
                    displayStatusFragment.scrollToTop();
                }
            });
        }

        if (theme == Helper.THEME_DARK) {
            style = R.style.DialogDark;
        } else if (theme == Helper.THEME_BLACK){
            style = R.style.DialogBlack;
        }else {
            style = R.style.Dialog;
        }


        if( theme == Helper.THEME_LIGHT){
            changeDrawableColor(getApplicationContext(), R.drawable.ic_home,R.color.dark_icon);
            changeDrawableColor(getApplicationContext(), R.drawable.ic_notifications,R.color.dark_icon);
            changeDrawableColor(getApplicationContext(), R.drawable.ic_direct_messages,R.color.dark_icon);
            changeDrawableColor(getApplicationContext(), R.drawable.ic_people,R.color.dark_icon);
            changeDrawableColor(getApplicationContext(), R.drawable.ic_public,R.color.dark_icon);
            changeDrawableColor(getApplicationContext(), R.drawable.ic_color_lens,R.color.dark_icon);

            changeDrawableColor(getApplicationContext(), R.drawable.ic_subscriptions,R.color.dark_icon);
            changeDrawableColor(getApplicationContext(), R.drawable.ic_overview,R.color.dark_icon);
            changeDrawableColor(getApplicationContext(), R.drawable.ic_trending_up,R.color.dark_icon);
            changeDrawableColor(getApplicationContext(), R.drawable.ic_recently_added,R.color.dark_icon);

        }else {
            changeDrawableColor(getApplicationContext(), R.drawable.ic_home,R.color.dark_text);
            changeDrawableColor(getApplicationContext(), R.drawable.ic_notifications,R.color.dark_text);
            changeDrawableColor(getApplicationContext(), R.drawable.ic_direct_messages,R.color.dark_text);
            changeDrawableColor(getApplicationContext(), R.drawable.ic_people,R.color.dark_text);
            changeDrawableColor(getApplicationContext(), R.drawable.ic_public,R.color.dark_text);
            changeDrawableColor(getApplicationContext(), R.drawable.ic_color_lens,R.color.dark_text);

            changeDrawableColor(getApplicationContext(), R.drawable.ic_subscriptions,R.color.dark_text);
            changeDrawableColor(getApplicationContext(), R.drawable.ic_overview,R.color.dark_text);
            changeDrawableColor(getApplicationContext(), R.drawable.ic_trending_up,R.color.dark_text);
            changeDrawableColor(getApplicationContext(), R.drawable.ic_recently_added,R.color.dark_text);
        }

        boolean live_notification = sharedpreferences.getBoolean(Helper.SET_LIVE_NOTIFICATIONS, true);

        if( live_notification && (social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON || social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA))
            startSreaming();

        if( hidde_menu != null)
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(hidde_menu);
        hidde_menu = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle b = intent.getExtras();
                assert b != null;
                String menu = b.getString("menu");
                if( menu != null){
                    if( menu.equals("hide_admin")){
                        NavigationView navigationView = findViewById(R.id.nav_view);
                        MenuItem admin = navigationView.getMenu().findItem(R.id.nav_administration);
                        if( admin != null){
                            admin.setVisible(false);
                        }
                    }else if(menu.equals("show_admin")){
                        NavigationView navigationView = findViewById(R.id.nav_view);
                        MenuItem admin = navigationView.getMenu().findItem(R.id.nav_administration);
                        if( admin != null){
                            admin.setVisible(true);
                        }
                    }else if(menu.equals("hide_news")){
                        NavigationView navigationView = findViewById(R.id.nav_view);
                        MenuItem news = navigationView.getMenu().findItem(R.id.nav_news);
                        if( news != null){
                            news.setVisible(false);
                        }
                    }else if(menu.equals("show_news")){
                        NavigationView navigationView = findViewById(R.id.nav_view);
                        MenuItem news = navigationView.getMenu().findItem(R.id.nav_news);
                        if( news != null){
                            news.setVisible(true);
                        }
                    }else if(menu.equals("show_list_button")){
                        displayTimelineMoreButton(true);
                    }else if(menu.equals("hide_list_button")){
                        displayTimelineMoreButton(false);
                    }else if(menu.equals("theme")){
                        recreate();
                    }
                }

            }
        };
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(hidde_menu, new IntentFilter(Helper.RECEIVE_HIDE_ITEM));

        toolbar_search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Hide keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                assert imm != null;
                imm.hideSoftInputFromWindow(toolbar_search.getWindowToken(), 0);
                String peertube = null;

                query= query.replaceAll("^#+", "");
                //It's not a peertube search
                //Peertube search
                if(tabLayout != null && timelines != null && (timelines.get(tabLayout.getSelectedTabPosition()).getType() == ManageTimelines.Type.PEERTUBE || (timelines.get(tabLayout.getSelectedTabPosition()).getRemoteInstance() != null && timelines.get(tabLayout.getSelectedTabPosition()).getRemoteInstance().getType().equals("PEERTUBE")))){
                    DisplayStatusFragment statusFragment;
                    Bundle bundle = new Bundle();
                    statusFragment = new DisplayStatusFragment();
                    bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE);
                    String instance = "peertube.fedilab.app";
                    if(timelines.get(tabLayout.getSelectedTabPosition()).getRemoteInstance() != null && timelines.get(tabLayout.getSelectedTabPosition()).getRemoteInstance().getType().equals("PEERTUBE"))
                        instance = timelines.get(tabLayout.getSelectedTabPosition()).getRemoteInstance().getHost();
                    bundle.putString("remote_instance", instance);
                    bundle.putString("instanceType", "PEERTUBE");
                    bundle.putString("search_peertube", query);
                    statusFragment.setArguments(bundle);
                    String fragmentTag = "REMOTE_INSTANCE";
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.main_app_container, statusFragment, fragmentTag).commit();
                    if( main_app_container.getVisibility() == View.GONE){

                        main_app_container.setVisibility(View.VISIBLE);
                        toolbarTitle.setVisibility(View.VISIBLE);
                        delete_instance.setVisibility(View.VISIBLE);
                        viewPager.setVisibility(View.GONE);
                        manageTimelineList(false);
                        tabLayout.setVisibility(View.GONE);
                    }
                }else if ( social == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE) {
                    DisplayStatusFragment statusFragment;
                    Bundle bundle = new Bundle();
                    statusFragment = new DisplayStatusFragment();
                    bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE);
                    bundle.putString("remote_instance", instance);
                    bundle.putString("instanceType", "PEERTUBE");
                    bundle.putString("search_peertube", query);
                    statusFragment.setArguments(bundle);
                    String fragmentTag = "REMOTE_INSTANCE";
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.main_app_container, statusFragment, fragmentTag).commit();
                    if( main_app_container.getVisibility() == View.GONE){

                        main_app_container.setVisibility(View.VISIBLE);
                        toolbarTitle.setVisibility(View.VISIBLE);
                        delete_instance.setVisibility(View.VISIBLE);
                        viewPager.setVisibility(View.GONE);
                        manageTimelineList(false);
                        tabLayout.setVisibility(View.GONE);
                    }
                }else{
                    if( social != UpdateAccountInfoAsyncTask.SOCIAL.GNU) {
                        boolean isAccount = false;
                        if( query.split("@").length > 1 ){
                            isAccount = true;
                        }
                        if( (social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON || social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA)
                                && !query.contains("http://") && !query.contains("https://") && !isAccount){
                            Intent intent = new Intent(BaseMainActivity.this, SearchResultTabActivity.class);
                            intent.putExtra("search", query);
                            startActivity(intent);
                        }else{
                            Intent intent = new Intent(BaseMainActivity.this, SearchResultActivity.class);
                            intent.putExtra("search", query);
                            startActivity(intent);
                        }

                    }else{
                        Intent intent = new Intent(BaseMainActivity.this, HashTagActivity.class);
                        Bundle b = new Bundle();
                        b.putString("tag", query.trim());
                        intent.putExtras(b);
                        startActivity(intent);
                    }
                }
                toolbar_search.setQuery("", false);
                toolbar_search.setIconified(true);
                if( main_app_container.getVisibility() == View.VISIBLE){
                    main_app_container.setVisibility(View.VISIBLE);
                    viewPager.setVisibility(View.GONE);
                    manageTimelineList(false);
                    delete_instance.setVisibility(View.GONE);
                    tabLayout.setVisibility(View.GONE);
                    toolbarTitle.setVisibility(View.VISIBLE);
                }else {
                    main_app_container.setVisibility(View.GONE);
                    viewPager.setVisibility(View.VISIBLE);
                    manageTimelineList(true);
                    tabLayout.setVisibility(View.VISIBLE);
                    delete_instance.setVisibility(View.GONE);
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
                    manageTimelineList(false);
                    tabLayout.setVisibility(View.GONE);
                    toolbarTitle.setVisibility(View.VISIBLE);
                }else {
                    main_app_container.setVisibility(View.GONE);
                    viewPager.setVisibility(View.VISIBLE);
                    manageTimelineList(true);
                    tabLayout.setVisibility(View.VISIBLE);
                    toolbarTitle.setVisibility(View.GONE);
                }
                delete_instance.setVisibility(View.GONE);
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
                        manageTimelineList(false);
                        tabLayout.setVisibility(View.GONE);
                        toolbarTitle.setVisibility(View.VISIBLE);
                    }else {
                        main_app_container.setVisibility(View.GONE);
                        viewPager.setVisibility(View.VISIBLE);
                        manageTimelineList(true);
                        tabLayout.setVisibility(View.VISIBLE);
                        toolbarTitle.setVisibility(View.GONE);
                    }
                }else {
                    toolbarTitle.setVisibility(View.GONE);
                    tabLayout.setVisibility(View.GONE);
                    manageTimelineList(false);
                }
                delete_instance.setVisibility(View.GONE);
            }
        });

        //Hide the default title
        if( getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().getThemedContext().setTheme(R.style.AppThemeBlack);
        }
        //Defines the current locale of the device in a static variable
        currentLocale = Helper.currentLocale(getApplicationContext());

        /*if( tabLayout.getTabAt(0) == null) {
            Helper.logout(BaseMainActivity.this);
            return;
        }
        tabLayout.getTabAt(0).select();
        */
        if( social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON || social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA || social == UpdateAccountInfoAsyncTask.SOCIAL.GNU || social == UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA) {
            toot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), TootActivity.class);
                    startActivity(intent);
                }
            });
            toot.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    CrossActions.doCrossReply(BaseMainActivity.this, null, null, false);
                    return false;
                }
            });
        }else if(social == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE){
            toot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), PeertubeUploadActivity.class);
                    startActivity(intent);
                }
            });
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
                drawer.openDrawer(GravityCompat.START);
            }
        });
        Helper.loadPictureIcon(BaseMainActivity.this, account,iconbar);
        headerLayout = navigationView.getHeaderView(0);

        final ImageView menuMore = headerLayout.findViewById(R.id.header_option_menu);
        menuMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(BaseMainActivity.this, menuMore);
                popup.getMenuInflater()
                        .inflate(R.menu.main, popup.getMenu());

                if( social != UpdateAccountInfoAsyncTask.SOCIAL.MASTODON){
                    MenuItem action_about_instance = popup.getMenu().findItem(R.id.action_about_instance);
                    if( action_about_instance != null)
                        action_about_instance.setVisible(false);
                    MenuItem action_export = popup.getMenu().findItem(R.id.action_export);
                    if( action_export != null)
                        action_export.setVisible(false);
                    MenuItem action_send_invitation = popup.getMenu().findItem(R.id.action_send_invitation);
                    if( action_send_invitation != null)
                        action_send_invitation.setVisible(false);
                }
                if( social == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE) {
                    MenuItem action_size = popup.getMenu().findItem(R.id.action_size);
                    if (action_size != null)
                        action_size.setVisible(false);
                }

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_logout:
                                Helper.logout(getApplicationContext());
                                Intent myIntent = new Intent(BaseMainActivity.this, LoginActivity.class);
                                startActivity(myIntent);
                                finish();
                                return true;
                            case R.id.action_logout_account:
                                Helper.logoutCurrentUser(BaseMainActivity.this);
                                return true;
                            case R.id.action_privacy:
                                Intent intent = new Intent(getApplicationContext(), PrivacyActivity.class);
                                startActivity(intent);
                                return true;
                            case R.id.action_about_instance:
                                intent = new Intent(getApplicationContext(), InstanceActivity.class);
                                startActivity(intent);
                                return true;
                            case R.id.action_send_invitation:
                                if( instanceClass != null){
                                    if(instanceClass.isRegistration()){
                                        Intent sendIntent = new Intent(Intent.ACTION_SEND);
                                        String extra_text = getString(R.string.join_instance, Helper.getLiveInstance(getApplicationContext()),
                                                "https://f-droid.org/en/packages/fr.gouv.etalab.mastodon/",
                                                "https://play.google.com/store/apps/details?id=app.fedilab.android",
                                                "https://fedilab.app/registration_helper/" + Helper.getLiveInstance(getApplicationContext()));
                                        sendIntent.putExtra(Intent.EXTRA_TEXT, extra_text);
                                        sendIntent.setType("text/plain");
                                        startActivity(Intent.createChooser(sendIntent, getString(R.string.share_with)));
                                    }else{
                                        Toasty.info(getApplicationContext(), getString(R.string.registration_closed), Toast.LENGTH_SHORT).show();
                                    }
                                }
                                return true;
                            case R.id.action_cache:
                                new Helper.CacheTask(BaseMainActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                return true;
                            case R.id.action_size:
                                final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
                                int textSize = sharedpreferences.getInt(Helper.SET_TEXT_SIZE,110);
                                int iconSize = sharedpreferences.getInt(Helper.SET_ICON_SIZE,130);

                                AlertDialog.Builder builder = new AlertDialog.Builder(BaseMainActivity.this, style);
                                builder.setTitle(R.string.text_size);

                                View popup_quick_settings = getLayoutInflater().inflate( R.layout.popup_text_size, new LinearLayout(getApplicationContext()), false );
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
                                        ActivityCompat.requestPermissions(BaseMainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Helper.EXTERNAL_STORAGE_REQUEST_CODE);
                                    } else {
                                        Intent backupIntent = new Intent(BaseMainActivity.this, BackupStatusService.class);
                                        startService(backupIntent);
                                    }
                                }else{
                                    Intent backupIntent = new Intent(BaseMainActivity.this, BackupStatusService.class);
                                    startService(backupIntent);
                                }
                                return true;

                            case R.id.action_import_data:
                                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    if (ContextCompat.checkSelfPermission(BaseMainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                                            PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(BaseMainActivity.this,
                                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                                TootActivity.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                                        return true;
                                    }
                                }
                                intent = new Intent(Intent.ACTION_GET_CONTENT);
                                intent.addCategory(Intent.CATEGORY_OPENABLE);
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                                    intent.setType("*/*");
                                    String[] mimetypes = {"*/*"};
                                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                                    startActivityForResult(intent, PICK_IMPORT);
                                }else {
                                    intent.setType("*/*");
                                    Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    Intent chooserIntent = Intent.createChooser(intent, getString(R.string.toot_select_import));
                                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});
                                    startActivityForResult(chooserIntent, PICK_IMPORT);
                                }
                                return true;
                            case R.id.action_export_data:
                                if(Build.VERSION.SDK_INT >= 23 ){
                                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {
                                        ActivityCompat.requestPermissions(BaseMainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Helper.EXTERNAL_STORAGE_REQUEST_CODE);
                                        return true;
                                    }
                                }
                                Sqlite.exportDB(BaseMainActivity.this);
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
        if( social != UpdateAccountInfoAsyncTask.SOCIAL.MASTODON)
            optionInfo.setVisibility(View.GONE);
        MenuFloating.tags = new ArrayList<>();
        Helper.updateHeaderAccountInfo(activity, account, headerLayout);
        //Locked account can see follow request
        if (account.isLocked()) {
            navigationView.getMenu().findItem(R.id.nav_follow_request).setVisible(true);
        } else {
            navigationView.getMenu().findItem(R.id.nav_follow_request).setVisible(false);
        }
        MenuItem news = navigationView.getMenu().findItem(R.id.nav_news);
        if( news != null){
            boolean display_news = sharedpreferences.getBoolean(Helper.SET_DISPLAY_NEWS_FROM_FEDILAB, true);
            if( ! display_news ){
                news.setVisible(false);
            }
        }
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
        if (!BuildConfig.DONATIONS) {
            MenuItem openCollectiveItem = navigationView.getMenu().findItem(R.id.nav_opencollective);
            if( openCollectiveItem != null){
                openCollectiveItem.setVisible(false);
            }
            MenuItem partnerShipItem = navigationView.getMenu().findItem(R.id.nav_partnership);
            if( partnerShipItem != null){
                partnerShipItem.setVisible(false);
            }
        }
        if( MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.MASTODON && MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA){
            MenuItem adminItem = navigationView.getMenu().findItem(R.id.nav_administration);
            if( adminItem != null){
                adminItem.setVisible(false);
            }
        }else{
            boolean display_admin_menu = sharedpreferences.getBoolean(Helper.SET_DISPLAY_ADMIN_MENU + userId + instance, false);
            if( !display_admin_menu){
                MenuItem adminItem = navigationView.getMenu().findItem(R.id.nav_administration);
                if( adminItem != null){
                    adminItem.setVisible(false);
                }
            }
        }
        LinearLayout owner_container = headerLayout.findViewById(R.id.main_header_container);
        owner_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.menuAccounts(BaseMainActivity.this);
                if( main_app_container.getVisibility() == View.VISIBLE){
                    main_app_container.setVisibility(View.VISIBLE);
                    viewPager.setVisibility(View.GONE);
                    manageTimelineList(false);
                    tabLayout.setVisibility(View.GONE);
                    toolbarTitle.setVisibility(View.VISIBLE);
                }else {
                    main_app_container.setVisibility(View.GONE);
                    viewPager.setVisibility(View.VISIBLE);
                    manageTimelineList(true);
                    tabLayout.setVisibility(View.VISIBLE);
                    toolbarTitle.setVisibility(View.GONE);
                }
                delete_instance.setVisibility(View.GONE);
            }
        });




        // Asked once for notification opt-in
        boolean popupShown = sharedpreferences.getBoolean(Helper.SET_POPUP_PUSH, false);
        if( !popupShown && (social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON || social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA)){
            AlertDialog.Builder dialogBuilderOptin = new AlertDialog.Builder(BaseMainActivity.this, style);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.popup_quick_settings, new LinearLayout(getApplicationContext()), false);
            dialogBuilderOptin.setView(dialogView);

            //final SwitchCompat set_push_hometimeline = dialogView.findViewById(R.id.set_push_hometimeline);
            final SwitchCompat set_push_notification = dialogView.findViewById(R.id.set_push_notification);
            boolean notif_follow = sharedpreferences.getBoolean(Helper.SET_NOTIF_FOLLOW, true);
            boolean notif_add = sharedpreferences.getBoolean(Helper.SET_NOTIF_ADD, true);
            boolean notif_mention = sharedpreferences.getBoolean(Helper.SET_NOTIF_MENTION, true);
            boolean notif_share = sharedpreferences.getBoolean(Helper.SET_NOTIF_SHARE, true);
            boolean notifif_notifications = !( !notif_follow &&  !notif_add && !notif_mention && !notif_share);
            //set_push_hometimeline.setChecked(notif_hometimeline);
            set_push_notification.setChecked(notifif_notifications);

            dialogBuilderOptin.setTitle(R.string.settings_popup_title);
            dialogBuilderOptin.setCancelable(false);
            dialogBuilderOptin.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putBoolean(Helper.SET_NOTIF_FOLLOW, set_push_notification.isChecked());
                    editor.putBoolean(Helper.SET_NOTIF_ADD, set_push_notification.isChecked());
                    editor.putBoolean(Helper.SET_NOTIF_MENTION, set_push_notification.isChecked());
                    editor.putBoolean(Helper.SET_NOTIF_SHARE, set_push_notification.isChecked());
                    editor.putBoolean(Helper.SET_NOTIF_POLL, set_push_notification.isChecked());
                    editor.putBoolean(Helper.SET_POPUP_PUSH, true);
                    editor.apply();
                    /*if( set_push_notification.isChecked() ){
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            try {
                                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                                intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                                startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }*/
                }
            });
            try {
                dialogBuilderOptin.show();
            }catch (Exception ignored){};

        }
        Helper.switchLayout(BaseMainActivity.this);



        mamageNewIntent(getIntent());

        if( social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON || social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA || social == UpdateAccountInfoAsyncTask.SOCIAL.GNU || social == UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA) {

            // Retrieves instance
            new RetrieveInstanceAsyncTask(getApplicationContext(), BaseMainActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            // Retrieves filters
            if( social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON || social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA) {
                new ManageFiltersAsyncTask(getApplicationContext(), GET_ALL_FILTER, null, BaseMainActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }

        /* Clean cache for statuses */
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Date date = new Date( System.currentTimeMillis() - TimeUnit.DAYS.toMillis(10));
                String dateString = Helper.dateToString(date);
                new TimelineCacheDAO(BaseMainActivity.this, db).removeAfterDate(dateString);
            }
        });

        mutedAccount = new TempMuteDAO(getApplicationContext(), db).getAllTimeMuted(account);

    }

    private void manageTimelineList(boolean displayed){
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        boolean display_timeline_in_list = sharedpreferences.getBoolean(Helper.SET_DISPLAY_TIMELINE_IN_LIST, false);
        if( !display_timeline_in_list){
            display_timeline.setVisibility(View.GONE);
        }else{
            if( displayed){
                display_timeline.setVisibility(View.VISIBLE);
            }else{
                display_timeline.setVisibility(View.GONE);
            }
        }
    }

    protected abstract void rateThisApp();


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
        String userIdIntent, instanceIntent;
        if( extras != null && extras.containsKey(Helper.INTENT_ACTION) ){
            final NavigationView navigationView = findViewById(R.id.nav_view);
            userIdIntent = extras.getString(Helper.PREF_KEY_ID); //Id of the account in the intent
            instanceIntent = extras.getString(Helper.PREF_INSTANCE);
            if (extras.getInt(Helper.INTENT_ACTION) == Helper.NOTIFICATION_INTENT){
                Helper.changeUser(BaseMainActivity.this, userIdIntent, instanceIntent,true); //Connects the account which is related to the notification
                Helper.unCheckAllMenuItems(navigationView);
                notificationChecked = true;
                if( extras.getString(Helper.INTENT_TARGETED_ACCOUNT) != null ){
                    Intent intentShow = new Intent(BaseMainActivity.this, ShowAccountActivity.class);
                    Bundle b = new Bundle();
                    b.putString("accountId", extras.getString(Helper.INTENT_TARGETED_ACCOUNT));
                    intentShow.putExtras(b);
                    startActivity(intentShow);
                }
            }else if( extras.getInt(Helper.INTENT_ACTION) == Helper.RELOAD_MYVIDEOS){
                Bundle bundle = new Bundle();
                DisplayStatusFragment fragment = new DisplayStatusFragment();
                bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.MYVIDEOS);
                bundle.putString("instanceType","PEERTUBE");
                SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
                String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
                String instance = sharedpreferences.getString(Helper.PREF_INSTANCE, Helper.getLiveInstance(getApplicationContext()));
                Account account = new AccountDAO(getApplicationContext(), db).getUniqAccount(userId, instance);
                bundle.putString("targetedid",account.getUsername());
                bundle.putBoolean("ownvideos", true);
                fragment.setArguments(bundle);
                String fragmentTag = "MY_VIDEOS";
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.main_app_container, fragment, fragmentTag).commit();
            }
            else if( extras.getInt(Helper.INTENT_ACTION) == Helper.SEARCH_INSTANCE){
                String instance = extras.getString(Helper.INSTANCE_NAME);
                DisplayStatusFragment statusFragment;
                Bundle bundle = new Bundle();
                statusFragment = new DisplayStatusFragment();
                bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE);
                bundle.putString("remote_instance", instance);
                statusFragment.setArguments(bundle);
                String fragmentTag = "REMOTE_INSTANCE";
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.main_app_container, statusFragment, fragmentTag).commit();
                main_app_container.setVisibility(View.VISIBLE);
                toolbarTitle.setVisibility(View.VISIBLE);
                delete_instance.setVisibility(View.VISIBLE);
                viewPager.setVisibility(View.GONE);
                manageTimelineList(false);
                tabLayout.setVisibility(View.GONE);
                toolbarTitle.setText(instance);
            }else if( extras.getInt(Helper.INTENT_ACTION) == Helper.HOME_TIMELINE_INTENT){
                Helper.changeUser(BaseMainActivity.this, userIdIntent, instanceIntent,false); //Connects the account which is related to the notification
            }else if( extras.getInt(Helper.INTENT_ACTION) == Helper.BACK_TO_SETTINGS){
                Helper.unCheckAllMenuItems(navigationView);
                navigationView.setCheckedItem(R.id.nav_settings);
                navigationView.getMenu().performIdentifierAction(R.id.nav_settings, 0);
                toolbarTitle.setText(R.string.settings);
            }else if( extras.getInt(Helper.INTENT_ACTION) == Helper.BACK_TO_SETTINGS){
                Helper.unCheckAllMenuItems(navigationView);
                navigationView.setCheckedItem(R.id.nav_peertube_settings);
                navigationView.getMenu().performIdentifierAction(R.id.nav_peertube_settings, 0);
                toolbarTitle.setText(R.string.settings);
            }else if (extras.getInt(Helper.INTENT_ACTION) == Helper.ADD_USER_INTENT){
                this.recreate();
            }else if( extras.getInt(Helper.INTENT_ACTION) == Helper.BACKUP_INTENT){
                Intent myIntent = new Intent(BaseMainActivity.this, OwnerStatusActivity.class);
                startActivity(myIntent);
            }else if( extras.getInt(Helper.INTENT_ACTION) == Helper.SEARCH_TAG){
                new SyncTimelinesAsyncTask(BaseMainActivity.this, -1, BaseMainActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else if( extras.getInt(Helper.INTENT_ACTION) == Helper.REFRESH_TIMELINE){
                int position = 0;
                if( tabLayout != null)
                    position = tabLayout.getSelectedTabPosition();
                new SyncTimelinesAsyncTask(BaseMainActivity.this, position, BaseMainActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else if (extras.getInt(Helper.INTENT_ACTION) == Helper.SEARCH_REMOTE) {
                String url = extras.getString(Helper.SEARCH_URL);
                intent.replaceExtras(new Bundle());
                intent.setAction("");
                intent.setData(null);
                intent.setFlags(0);
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
                boolean shouldRetrieveMetaData = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE).getBoolean(Helper.SET_RETRIEVE_METADATA_IF_URL_FROM_EXTERAL, true);

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
                    new RetrieveMetaDataAsyncTask(BaseMainActivity.this, shouldRetrieveMetaData, sharedSubject, sharedText, url,BaseMainActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }

            } else if (type.startsWith("image/") || type.startsWith("video/")) {

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
            if (type.startsWith("image/")  || type.startsWith("video/")) {

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

            if( url == null) {
                intent.replaceExtras(new Bundle());
                intent.setAction("");
                intent.setData(null);
                intent.setFlags(0);
                return;
            }
            Matcher matcher;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT)
                matcher = Patterns.WEB_URL.matcher(url);
            else
                matcher = Helper.urlPattern.matcher(url);
            boolean isUrl = false;
            while (matcher.find()){
                isUrl = true;
            }
            if(!isUrl) {
                intent.replaceExtras(new Bundle());
                intent.setAction("");
                intent.setData(null);
                intent.setFlags(0);
                return;
            }
            //Here we know that the intent contains a valid URL
            if( !url.contains("medium.com")) {
                new RetrieveRemoteDataAsyncTask(BaseMainActivity.this, url, BaseMainActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }else{
                forwardToBrowser(intent);
            }
        }
        intent.replaceExtras(new Bundle());
        intent.setAction("");
        intent.setData(null);
        intent.setFlags(0);
    }

    private void forwardToBrowser(Intent i) {
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(i.getData(), i.getType());
        List<ResolveInfo> activities = getPackageManager().queryIntentActivities(intent, 0);
        ArrayList<Intent> targetIntents = new ArrayList<Intent>();
        String thisPackageName = getApplicationContext().getPackageName();
        for (ResolveInfo currentInfo : activities) {
            String packageName = currentInfo.activityInfo.packageName;
            if (!thisPackageName.equals(packageName)) {
                Intent targetIntent = new Intent(android.content.Intent.ACTION_VIEW);
                targetIntent.setDataAndType(intent.getData(),intent.getType());
                targetIntent.setPackage(intent.getPackage());
                targetIntent.setComponent(new ComponentName(packageName, currentInfo.activityInfo.name));
                targetIntents.add(targetIntent);
            }
        }
        if(targetIntents.size() > 0) {
            Intent chooserIntent = Intent.createChooser(targetIntents.remove(0), getString(R.string.open_with));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetIntents.toArray(new Parcelable[] {}));
            startActivity(chooserIntent);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            displayPeertube = null;
            //Hide search bar on back pressed
            if( !toolbar_search.isIconified()){
                toolbar_search.setIconified(true);
                return;
            }
            if( viewPager.getVisibility() == View.VISIBLE){
                super.onBackPressed();
            } else {
                Helper.switchLayout(BaseMainActivity.this);
                main_app_container.setVisibility(View.GONE);
                viewPager.setVisibility(View.VISIBLE);
                manageTimelineList(true);
                tabLayout.setVisibility(View.VISIBLE);
                toolbarTitle.setVisibility(View.GONE);
                delete_instance.setVisibility(View.GONE);
                delete_all.hide();

                add_new.hide();
                final NavigationView navigationView = findViewById(R.id.nav_view);
                Helper.unCheckAllMenuItems(navigationView);
                tootShow();
            }

        }

    }

    @Override
    public void onResume(){
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("isMainActivityRunning", true).apply();

        //Proceeds to update of the authenticated account
        if(Helper.isLoggedIn(getApplicationContext())) {
            new UpdateAccountInfoByIDAsyncTask(getApplicationContext(), social, BaseMainActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        String datestr = sharedpreferences.getString(Helper.HOME_LAST_READ + userId + instance,null);

        if( timelines != null && timelines.size() > 0 && mPageReferenceMap != null && datestr != null){
            Date date = Helper.stringToDate(getApplicationContext(), datestr);
            Date dateAllowed = new Date( System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(30));
            //Refresh home if needed
            if( dateAllowed.after(date) ) {
                for (ManageTimelines tl : timelines) {
                    if (tl.getType() == ManageTimelines.Type.HOME && mPageReferenceMap.containsKey(tl.getPosition())) {
                        DisplayStatusFragment homeTimeline = (DisplayStatusFragment) mPageReferenceMap.get(tl.getPosition());
                        if (homeTimeline != null) {
                            homeTimeline.retrieveMissingHome();
                        }
                        break;
                    }
                }
            }
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        if( userId != null && instance != null) {
            SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(Helper.HOME_LAST_READ + userId + instance, Helper.dateToString(new Date()));
            editor.apply();
        }
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("isMainActivityRunning", false).apply();
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        boolean backgroundProcess = sharedpreferences.getBoolean(Helper.SET_KEEP_BACKGROUND_PROCESS, true);
        if(!backgroundProcess)
            sendBroadcast(new Intent("StopLiveNotificationService"));
        if( hidde_menu != null)
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(hidde_menu);

        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("isMainActivityRunning", false).apply();
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
        }else if(id == R.id.nav_drag_timelines){
            Intent intent = new Intent(getApplicationContext(), ReorderTimelinesActivity.class);
            startActivity(intent);
            return false;
        }else if(id == R.id.nav_administration){
            Intent intent = new Intent(getApplicationContext(), AdminActivity.class);
            startActivity(intent);
            return false;
        } else if( id == R.id.nav_about) {
            Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
            startActivity(intent);
            return false;
        }else if (id == R.id.nav_opencollective) {
            Intent intent = new Intent(getApplicationContext(), OpencollectiveActivity.class);
            startActivity(intent);
            return false;
        } else if( id == R.id.nav_upload) {
            Intent intent = new Intent(getApplicationContext(), PeertubeUploadActivity.class);
            startActivity(intent);
            return false;
        } else if( id == R.id.nav_language) {
            Intent intent = new Intent(getApplicationContext(), LanguageActivity.class);
            startActivity(intent);
            return false;
        } else if( id == R.id.nav_partnership) {
            Intent intent = new Intent(getApplicationContext(), PartnerShipActivity.class);
            startActivity(intent);
            return false;
        }else if (id == R.id.nav_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
            return false;
        } else if(id == R.id.nav_blocked_domains){
            Intent intent = new Intent(getApplicationContext(), MutedInstanceActivity.class);
            startActivity(intent);
            return false;
        } else if(id == R.id.nav_bug_report){
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"incoming+tom79/mastalab@incoming.gitlab.com"});
            try {
                startActivity(Intent.createChooser(i, getString(R.string.bug_report_mail)));
            } catch (android.content.ActivityNotFoundException ex) {
                Toasty.info(getApplicationContext(), getString(R.string.no_mail_client), Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        final NavigationView navigationView = findViewById(R.id.nav_view);
        Helper.unCheckAllMenuItems(navigationView);
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
        manageTimelineList(false);
        tabLayout.setVisibility(View.GONE);
        toolbarTitle.setVisibility(View.VISIBLE);
        delete_instance.setVisibility(View.GONE);
        appBar.setExpanded(true);
        if (id != R.id.nav_drafts && id != R.id.nav_bookmarks && id != R.id.nav_peertube ) {
            delete_all.hide();
        }else{
            delete_all.show();
        }
        if( id != R.id.nav_list && id != R.id.nav_filters && id != R.id.nav_peertube_playlists && id != R.id.nav_blocked_domains){
            add_new.hide();
        }else{
            add_new.show();
        }
        if (id == R.id.nav_peertube_settings) {
            toot.hide();
            SettingsPeertubeFragment settingsPeertubeFragment= new SettingsPeertubeFragment();
            fragmentTag = "TABLAYOUT_PEERTUBE_SETTINGS";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, settingsPeertubeFragment, fragmentTag).commit();

        }else if (id == R.id.nav_peertube_notifications) {
            toot.hide();
            DisplayPeertubeNotificationsFragment displayPeertubeNotificationsFragment= new DisplayPeertubeNotificationsFragment();
            fragmentTag = "PEERTUBE_NOTIFICATIONS";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, displayPeertubeNotificationsFragment, fragmentTag).commit();

        }else if (id == R.id.nav_favorites || id == R.id.nav_pixelfed_favorites) {
            toot.hide();
            statusFragment = new DisplayStatusFragment();
            bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.FAVOURITES);
            statusFragment.setArguments(bundle);
            fragmentTag = "FAVOURITES";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, statusFragment, fragmentTag).commit();
        } else if (id == R.id.nav_my_video) {
            bundle = new Bundle();
            DisplayStatusFragment fragment = new DisplayStatusFragment();
            bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.MYVIDEOS);
            bundle.putString("instanceType","PEERTUBE");
            SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
            SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
            String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
            String instance = sharedpreferences.getString(Helper.PREF_INSTANCE, Helper.getLiveInstance(getApplicationContext()));
            Account account = new AccountDAO(getApplicationContext(), db).getUniqAccount(userId, instance);
            bundle.putString("targetedid",account.getUsername());
            bundle.putBoolean("ownvideos", true);
            fragment.setArguments(bundle);
            fragmentTag = "MY_VIDEOS";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, fragment, fragmentTag).commit();
        } else if (id == R.id.nav_peertube_history) {
            bundle = new Bundle();
            DisplayStatusFragment fragment = new DisplayStatusFragment();
            bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.PEERTUBE_HISTORY);
            bundle.putString("instanceType","PEERTUBE");
            SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
            SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
            String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
            String instance = sharedpreferences.getString(Helper.PREF_INSTANCE, Helper.getLiveInstance(getApplicationContext()));
            Account account = new AccountDAO(getApplicationContext(), db).getUniqAccount(userId, instance);
            bundle.putString("targetedid",account.getUsername());
            fragment.setArguments(bundle);
            fragmentTag = "MY_HISTORY";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, fragment, fragmentTag).commit();
        } else if (id == R.id.nav_news) {
            toot.hide();
            statusFragment = new DisplayStatusFragment();
            bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.NEWS);
            statusFragment.setArguments(bundle);
            fragmentTag = "NEWS";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, statusFragment, fragmentTag).commit();
        } else if (id == R.id.nav_blocked || id == R.id.nav_pixelfed_blocked) {
            toot.hide();
            accountsFragment = new DisplayAccountsFragment();
            bundle.putSerializable("type", RetrieveAccountsAsyncTask.Type.BLOCKED);
            accountsFragment.setArguments(bundle);
            fragmentTag = "BLOCKS";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, accountsFragment, fragmentTag).commit();
        }else if (id == R.id.nav_how_to) {
            toot.hide();
            DisplayHowToFragment displayHowToFragment = new DisplayHowToFragment();
            fragmentTag = "HOW_TO_VIDEOS";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, displayHowToFragment, fragmentTag).commit();
        }else if (id == R.id.nav_muted || id == R.id.nav_pixelfed_muted) {
            toot.hide();
            accountsFragment = new DisplayAccountsFragment();
            bundle.putSerializable("type", RetrieveAccountsAsyncTask.Type.MUTED);
            accountsFragment.setArguments(bundle);
            fragmentTag = "MUTED";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, accountsFragment, fragmentTag).commit();
        }else if (id == R.id.nav_scheduled) {
            tootShow();
            TabLayoutScheduleFragment tabLayoutScheduleFragment = new TabLayoutScheduleFragment();
            fragmentTag = "SCHEDULED";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, tabLayoutScheduleFragment, fragmentTag).commit();
        }else if (id == R.id.nav_drafts) {
            DisplayDraftsFragment displayDraftsFragment = new DisplayDraftsFragment();
            fragmentTag = "DRAFTS";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, displayDraftsFragment, fragmentTag).commit();
            toot.hide();
        }else if (id == R.id.nav_bookmarks) {
            DisplayBookmarksFragment displayBookmarksFragment = new DisplayBookmarksFragment();
            fragmentTag = "BOOKMARKS";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, displayBookmarksFragment, fragmentTag).commit();
            toot.hide();
        }else if (id == R.id.nav_peertube) {
            DisplayFavoritesPeertubeFragment displayFavoritesPeertubeFragment = new DisplayFavoritesPeertubeFragment();
            fragmentTag = "BOOKMARKS_PEERTUBE";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, displayFavoritesPeertubeFragment, fragmentTag).commit();
            toot.hide();
        }else if( id == R.id.nav_follow_request){
            toot.hide();
            DisplayFollowRequestSentFragment followRequestSentFragment = new DisplayFollowRequestSentFragment();
            fragmentTag = "FOLLOW_REQUEST_SENT";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, followRequestSentFragment, fragmentTag).commit();
        }else if(id == R.id.nav_list){
            toot.hide();
            DisplayListsFragment displayListsFragment = new DisplayListsFragment();
            fragmentTag = "LISTS";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, displayListsFragment, fragmentTag).commit();
        }else if(id == R.id.nav_group){
            toot.hide();
            accountsFragment = new DisplayAccountsFragment();
            bundle.putSerializable("type", RetrieveAccountsAsyncTask.Type.GROUPS);
            accountsFragment.setArguments(bundle);
            fragmentTag = "GROUPS";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, accountsFragment, fragmentTag).commit();
        }else if(id == R.id.nav_peertube_playlists){
            toot.hide();
            DisplayPlaylistsFragment displayPlaylistsFragment = new DisplayPlaylistsFragment();
            fragmentTag = "PLAYLISTS";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, displayPlaylistsFragment, fragmentTag).commit();
        }else if(id == R.id.nav_filters){
            toot.hide();
            DisplayFiltersFragment displayFiltersFragment = new DisplayFiltersFragment();
            fragmentTag = "FILTERS";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, displayFiltersFragment, fragmentTag).commit();
        }else if(id == R.id.nav_who_to_follow){
            toot.hide();
            WhoToFollowFragment whoToFollowFragment = new WhoToFollowFragment();
            fragmentTag = "WHO_TO_FOLLOW";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, whoToFollowFragment, fragmentTag).commit();
        }

        populateTitleWithTag(fragmentTag, item.getTitle().toString(), item.getItemId());
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void populateTitleWithTag(String tag, String title, int index){
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
            Account account = new AccountDAO(getApplicationContext(), db).getUniqAccount(userId, instance);
            Helper.updateHeaderAccountInfo(activity, account, headerLayout);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMPORT && resultCode == RESULT_OK) {
            if (data == null || data.getData() == null) {
                Toasty.error(getApplicationContext(),getString(R.string.toot_select_file_error),Toast.LENGTH_LONG).show();
                return;
            }
            String filename = Helper.getFilePathFromURI(getApplicationContext(), data.getData());
            Sqlite.importDB(BaseMainActivity.this, filename);

        }else if(requestCode == PICK_IMPORT ){
            Toasty.error(getApplicationContext(),getString(R.string.toot_select_file_error),Toast.LENGTH_LONG).show();
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

        instanceClass = apiResponse.getInstance();
        poll_limits = apiResponse.getInstance().getPoll_limits();
        Version currentVersion = new Version(apiResponse.getInstance().getVersion());
        Version minVersion = new Version("1.6");
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(Helper.INSTANCE_VERSION + userId + instance, apiResponse.getInstance().getVersion());
        editor.apply();
        Helper.canPin = (currentVersion.compareTo(minVersion) == 1 || currentVersion.equals(minVersion));
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
            b.putParcelable("account", accounts.get(0));
            intent.putExtras(b);
            startActivity(intent);
        }else if( statuses != null && statuses.size() > 0){
            Intent intent = new Intent(getApplicationContext(), ShowConversationActivity.class);
            Bundle b = new Bundle();
            b.putParcelable("status", statuses.get(0));
            intent.putExtras(b);
            startActivity(intent);
        }
    }

    @Override
    public void onRetrieveEmojiAccount(Account account) {
        TextView displayedName = headerLayout.findViewById(R.id.displayedName);
        displayedName.setText(account.getdisplayNameSpan(), TextView.BufferType.SPANNABLE);
    }

    @Override
    public void onActionDone(ManageFiltersAsyncTask.action actionType, APIResponse apiResponse, int statusCode) {
        if( apiResponse != null && apiResponse.getFilters() != null && apiResponse.getFilters().size() > 0){
            filters = apiResponse.getFilters();
        }
    }


    public void displayTimelineMoreButton(boolean displayed){
        if (displayed) {
            display_timeline.setVisibility(View.VISIBLE);
        } else {
            display_timeline.setVisibility(View.GONE);
        }
    }

    @Override
    public void syncedTimelines(List<ManageTimelines> manageTimelines, int position) {
        ReorderTimelinesActivity.updated = false;
        tabLayout = new ManageTimelines().createTabs(BaseMainActivity.this, tabLayout, manageTimelines);
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        final NavigationView navigationView = findViewById(R.id.nav_view);


        timelines = manageTimelines;
        if( position >= manageTimelines.size()){
            position = manageTimelines.size()-1;
        }
        if( position == -1)
            position = (timelines.size()-1);
        if( position < 0)
            position = 0;
        if( toolbarTitle != null)
            toolbarTitle.setVisibility(View.GONE);
        viewPager.setOffscreenPageLimit(2);
        main_app_container = findViewById(R.id.main_app_container);

        boolean iconOnly = true;
        for(ManageTimelines tl: timelines){
            if( tl.getType() == ManageTimelines.Type.INSTANCE || tl.getType() == ManageTimelines.Type.TAG || tl.getType() == ManageTimelines.Type.LIST){
                iconOnly = false;
            }
        }

        if( iconOnly && timelines.size() < 5){
            tabLayout.setTabMode(TabLayout.MODE_FIXED);
        }else{
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        }

        boolean display_timeline_in_list = sharedpreferences.getBoolean(Helper.SET_DISPLAY_TIMELINE_IN_LIST, false);
        displayTimelineMoreButton(display_timeline_in_list);

        adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                main_app_container.setVisibility(View.GONE);
                viewPager.setVisibility(View.VISIBLE);
                manageTimelineList(true);
                delete_instance.setVisibility(View.GONE);
                Helper.switchLayout(BaseMainActivity.this);
                if( manageTimelines.size() > tab.getPosition() && (manageTimelines.get(tab.getPosition()).getType() == ManageTimelines.Type.NOTIFICATION ||manageTimelines.get(tab.getPosition()).getType() == ManageTimelines.Type.ART || manageTimelines.get(tab.getPosition()).getType() == ManageTimelines.Type.PEERTUBE)) {
                    toot.hide();
                }else {
                    tootShow();
                }
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                if( tab.getCustomView() != null) {
                    ImageView icon = tab.getCustomView().findViewById(R.id.tab_icon);
                    TextView tv = tab.getCustomView().findViewById(R.id.host_name);

                    if( icon != null)
                        if( theme == Helper.THEME_BLACK)
                            icon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_icon), PorterDuff.Mode.SRC_IN);
                        else
                            icon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.mastodonC4), PorterDuff.Mode.SRC_IN);
                    else if( tv != null){
                        tv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.mastodonC4));
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if( tab.getCustomView() != null) {
                    ImageView icon = tab.getCustomView().findViewById(R.id.tab_icon);
                    TextView tv = tab.getCustomView().findViewById(R.id.host_name);
                    if( icon != null)
                        if( theme == Helper.THEME_LIGHT)
                            icon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_icon), PorterDuff.Mode.SRC_IN);
                        else
                            icon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_text), PorterDuff.Mode.SRC_IN);
                    else if( tv != null){
                        if( theme == Helper.THEME_LIGHT)
                            tv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_icon));
                        else
                            tv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_text));
                    }
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if( viewPager.getVisibility() == View.GONE){
                    viewPager.setVisibility(View.VISIBLE);
                    manageTimelineList(true);
                    delete_instance.setVisibility(View.GONE);
                    Helper.switchLayout(BaseMainActivity.this);
                    main_app_container.setVisibility(View.GONE);
                    DrawerLayout drawer = findViewById(R.id.drawer_layout);
                    drawer.closeDrawer(GravityCompat.START);
                }
                if( manageTimelines.size() > tab.getPosition() && (manageTimelines.get(tab.getPosition()).getType() == ManageTimelines.Type.ART || manageTimelines.get(tab.getPosition()).getType() == ManageTimelines.Type.PEERTUBE)) {
                    toot.hide();
                }else {
                    tootShow();
                }

                if( viewPager.getAdapter() != null) {
                    Fragment fragment = (Fragment) viewPager.getAdapter().instantiateItem(viewPager, tab.getPosition());
                    ManageTimelines tl = timelines.get(tab.getPosition());
                    DisplayStatusFragment displayStatusFragment;
                    if (tl.getType() == ManageTimelines.Type.HOME) {
                        displayStatusFragment = ((DisplayStatusFragment) fragment);
                        countNewStatus = 0;
                        updateHomeCounter();
                        displayStatusFragment.scrollToTop();
                        displayStatusFragment.updateLastReadToot();
                    } else if(tl.getType() == ManageTimelines.Type.NOTIFICATION) {
                        countNewNotifications = 0;
                        updateNotifCounter();
                    }else {
                        View tabCustom = tab.getCustomView();
                        if( tabCustom != null) {
                            TextView tabCountertCustom = tabCustom.findViewById(R.id.tab_counter);
                            if( tabCountertCustom != null) {
                                tabCountertCustom.setText(String.valueOf(0));
                                tabCountertCustom.setVisibility(View.GONE);
                            }
                        }
                        displayStatusFragment = ((DisplayStatusFragment) fragment);
                        displayStatusFragment.scrollToTop();
                    }
                }
                if( tab.getCustomView() != null) {
                    ImageView icon = tab.getCustomView().findViewById(R.id.tab_icon);
                    if( icon != null)
                        if( theme == Helper.THEME_BLACK)
                            icon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_icon), PorterDuff.Mode.SRC_IN);
                        else
                            icon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.mastodonC4), PorterDuff.Mode.SRC_IN);
                }
            }
        });

        if( tabLayout.getTabCount() > position) {
            TabLayout.Tab tab = tabLayout.getTabAt(position);
            if( tab != null) {
                tab.select();
                if( tab.getCustomView() != null){
                    ImageView icon = tab.getCustomView().findViewById(R.id.tab_icon);
                    if( icon != null){
                        if( theme == Helper.THEME_BLACK)
                            icon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.dark_icon), PorterDuff.Mode.SRC_IN);
                        else
                            icon.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.mastodonC4), PorterDuff.Mode.SRC_IN);
                    }else{
                        TextView tv = tabLayout.getChildAt(0).findViewById(android.R.id.title);
                        if( tv != null)
                            if( theme == Helper.THEME_BLACK)
                                tv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_icon));
                            else
                                tv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.mastodonC4));
                    }
                }
            }
        }

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
                        ManageTimelines tl = timelines.get(pos);
                        if( tl.getType() != ManageTimelines.Type.NOTIFICATION){
                            DisplayStatusFragment displayStatusFragment = ((DisplayStatusFragment) fragment);
                            displayStatusFragment.scrollToTop();
                        }
                    }
                }
            }
        });
        if( notificationChecked){
            notificationChecked = false;
            int i = 0;
            for(ManageTimelines tl: timelines){
                if( tl.getType() == ManageTimelines.Type.NOTIFICATION){
                    if( tabLayout.getTabAt(i) != null) {
                        tabLayout.getTabAt(i).select();
                    }
                    break;
                }
                i++;
            }
        }
    }

    /**
     * Page Adapter for Mastodon & Peertube & PixelFed
     */
    public class PagerAdapter extends FragmentStatePagerAdapter  {
        int mNumOfTabs;

        private PagerAdapter(FragmentManager fm, int NumOfTabs) {
            super(fm);
            this.mNumOfTabs = NumOfTabs;
        }

        @Override
        public Parcelable saveState() {
            return null;
        }
        @Override
        public Fragment getItem(int position) {
            if( social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON || social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA || social == UpdateAccountInfoAsyncTask.SOCIAL.GNU || social == UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA) {
                //Remove the search bar
                if (!toolbar_search.isIconified()) {
                    toolbarTitle.setVisibility(View.VISIBLE);
                    tabLayout.setVisibility(View.VISIBLE);
                    toolbar_search.setIconified(true);
                }

                //Selection comes from another menu, no action to do
                Bundle bundle = new Bundle();
                ManageTimelines tl = null;
                if( position < timelines.size())
                    tl = timelines.get(position);
                if( tl == null)
                    return null;
                if( tl.getType() != ManageTimelines.Type.NOTIFICATION){
                    DisplayStatusFragment displayStatusFragment = new DisplayStatusFragment();
                    RetrieveFeedsAsyncTask.Type type = ManageTimelines.transform(BaseMainActivity.this, tl.getType());
                    bundle.putSerializable("type", type);

                    if (tl.getType() == ManageTimelines.Type.TAG) {
                        TagTimeline ttl = tl.getTagTimeline();
                        bundle.putString("tag", ttl.getName());
                        if( ttl.isART() )
                            bundle.putString("instanceType","ART");
                    }else if (tl.getType() == ManageTimelines.Type.ART) {
                        bundle.putString("instanceType", "ART");
                    }else if (tl.getType() == ManageTimelines.Type.PEERTUBE) {
                        bundle.putString("instanceType", "PEERTUBE");
                        bundle.putString("remote_instance", "peertube.fedilab.app");
                        bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE);
                    }else if( tl.getType() == ManageTimelines.Type.INSTANCE){
                        if( tl.getRemoteInstance().getFilteredWith() == null){
                            bundle.putSerializable("type",  RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE);
                        }else{
                            bundle.putString("currentfilter", tl.getRemoteInstance().getFilteredWith());
                            bundle.putSerializable("type",  RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE_FILTERED);
                        }
                        bundle.putString("remote_instance", tl.getRemoteInstance().getHost()!=null?tl.getRemoteInstance().getHost():"");
                        bundle.putString("instanceType", tl.getRemoteInstance().getType());
                    }else if( tl.getType() == ManageTimelines.Type.LIST){
                        bundle.putString("targetedid", tl.getListTimeline().getId());
                    }
                    bundle.putInt("timelineId", tl.getId());
                    displayStatusFragment.setArguments(bundle);
                    mPageReferenceMap.put(tl.getPosition(), displayStatusFragment);
                    return displayStatusFragment;
                }else{
                    TabLayoutNotificationsFragment tabLayoutNotificationsFragment = new TabLayoutNotificationsFragment();
                    mPageReferenceMap.put(tl.getPosition(), tabLayoutNotificationsFragment);
                    return tabLayoutNotificationsFragment;
                }

            }else if (social == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE){
                //Remove the search bar
                if( !toolbar_search.isIconified() ) {
                    toolbarTitle.setVisibility(View.VISIBLE);
                    tabLayout.setVisibility(View.VISIBLE);
                    toolbar_search.setIconified(true);
                }
                //Selection comes from another menu, no action to do
                Bundle bundle = new Bundle();
                DisplayStatusFragment fragment = new DisplayStatusFragment();
                if (position == 0) {
                    bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.PSUBSCRIPTIONS);
                }else if( position == 1) {
                    bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.POVERVIEW);
                }else if( position == 2) {
                    bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.PTRENDING);
                }else if( position == 3) {
                    bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.PRECENTLYADDED);
                }else if( position == 4) {
                    bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.PLOCAL);
                }
                bundle.putString("instanceType","PEERTUBE");
                fragment.setArguments(bundle);
                return fragment;
            }else if (social == UpdateAccountInfoAsyncTask.SOCIAL.PIXELFED){
                //Remove the search bar
                if( !toolbar_search.isIconified() ) {
                    toolbarTitle.setVisibility(View.VISIBLE);
                    tabLayout.setVisibility(View.VISIBLE);
                    toolbar_search.setIconified(true);
                }
                //Selection comes from another menu, no action to do
                Bundle bundle = new Bundle();

                if (position == 0) {
                    DisplayStatusFragment fragment = new DisplayStatusFragment();
                    bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.PF_HOME);
                    bundle.putString("instanceType","PIXELFED");
                    fragment.setArguments(bundle);
                    return fragment;
                }else if( position == 1) {
                    DisplayStatusFragment fragment = new DisplayStatusFragment();
                    bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.PF_LOCAL);
                    bundle.putString("instanceType","PIXELFED");
                    fragment.setArguments(bundle);
                    return fragment;
                }else if( position == 2){
                    DisplayNotificationsFragment fragment = new DisplayNotificationsFragment();
                    bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.PF_NOTIFICATION);
                    fragment.setArguments(bundle);
                    return fragment;
                }
                /*else if( position == 3) {
                    bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.PF_DISCOVER);
                }*/

                return null;
            }
            return null;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            mPageReferenceMap.remove(position);
            super.destroyItem(container, position, object);
        }
        @Override
        public int getCount() {
            return mNumOfTabs;
        }
    }




    public void updateHomeCounter(){
        int i = 0;
        if( timelines != null && timelines.size() > 0){
            for(ManageTimelines tl: timelines){
                if( tl.getType() == ManageTimelines.Type.HOME){
                    if( tabLayout.getTabCount() > i) {
                        View tabHome = tabLayout.getTabAt(i).getCustomView();
                        if( tabHome != null){
                            TextView tabCounterHome = tabHome.findViewById(R.id.tab_counter);
                            if(countNewStatus == Helper.TOOTS_PER_PAGE){
                                tabCounterHome.setText(String.format(Locale.getDefault(), "%d+", countNewStatus));
                            }else{
                                tabCounterHome.setText(String.valueOf(countNewStatus));
                            }
                            if( countNewStatus> 0){
                                //New data are available
                                //The fragment is not displayed, so the counter is displayed
                                tabCounterHome.setVisibility(View.VISIBLE);
                            }else {
                                tabCounterHome.setVisibility(View.GONE);
                            }
                        }
                    }
                }
                i++;
            }

        }
    }

    public void manageTab( RetrieveFeedsAsyncTask.Type type, int value){
        SQLiteDatabase db = Sqlite.getInstance(BaseMainActivity.this, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        List<ManageTimelines> tls = new TimelinesDAO(BaseMainActivity.this, db).getDisplayedTimelines();
        for (ManageTimelines tl: tls){
            if( type == ManageTimelines.transform(BaseMainActivity.this, tl.getType())){
                View tabCustom = tabLayout.getTabAt(tl.getPosition()).getCustomView();
                assert tabCustom != null;
                TextView tabCountertCustom = tabCustom.findViewById(R.id.tab_counter);
                tabCountertCustom.setText(String.valueOf(value));
                if( value > 0){
                    tabCountertCustom.setVisibility(View.VISIBLE);
                }else {
                    tabCountertCustom.setVisibility(View.GONE);
                }
                break;
            }
        }
    }

    public void updateNotifCounter(){
        if( timelines == null)
            return;
        int i = 0;
        int position = -1;
        for(ManageTimelines tl: timelines){
            if( tl.getType() == ManageTimelines.Type.NOTIFICATION){
                if( tabLayout.getTabAt(i) != null) {
                    position = i;
                }
                break;
            }
            i++;
        }
        if( position == -1)
            return;
        View tabNotif = tabLayout.getTabAt(position).getCustomView();
        if( tabNotif == null)
            return;
        TextView tabCounterNotif = tabNotif.findViewById(R.id.tab_counter);
        if( tabCounterNotif == null)
            return;
        if(countNewNotifications == Helper.NOTIFICATIONS_PER_PAGE){
            tabCounterNotif.setText(String.format(Locale.getDefault(), "%d+", countNewNotifications));
        }else{
            tabCounterNotif.setText(String.valueOf(countNewNotifications));
        }
        if( countNewNotifications > 0){
            tabCounterNotif.setVisibility(View.VISIBLE);
        }else {
            tabCounterNotif.setVisibility(View.GONE);
        }
    }


    public void startSreaming(){
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        boolean liveNotifications = sharedpreferences.getBoolean(Helper.SET_LIVE_NOTIFICATIONS, true);
        if( liveNotifications) {
            ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            assert manager != null;
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (LiveNotificationService.class.getName().equals(service.service.getClassName())) {
                    return;
                }
            }
            try {
                Intent streamingIntent = new Intent(this, LiveNotificationService.class);
                startService(streamingIntent);
            }catch(Exception ignored){}
        }

    }

    public void manageFloatingButton(boolean display){
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        if( social == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE ||social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON || social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA|| social == UpdateAccountInfoAsyncTask.SOCIAL.GNU|| social == UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA) {
            if (display) {
                tootShow();
            } else {
                toot.hide();
            }
        }else {
            toot.hide();
        }
    }
    public void tootShow(){
        if(  social == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE ||social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON || social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA|| social == UpdateAccountInfoAsyncTask.SOCIAL.GNU|| social == UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA) {
            toot.show();
        }else{
            toot.hide();
        }
    }





    public boolean getFloatingVisibility(){
        return toot.getVisibility() == View.VISIBLE;
    }


}