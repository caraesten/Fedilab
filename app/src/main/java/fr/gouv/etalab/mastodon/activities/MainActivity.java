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

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;

import fr.gouv.etalab.mastodon.asynctasks.UpdateAccountInfoByIDAsyncTask;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.PatchBaseImageDownloader;
import fr.gouv.etalab.mastodon.fragments.DisplayAccountsFragment;
import fr.gouv.etalab.mastodon.fragments.DisplayFollowRequestSentFragment;
import fr.gouv.etalab.mastodon.fragments.DisplayNotificationsFragment;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnUpdateAccountInfoInterface;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveAccountsAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveFeedsAsyncTask;
import fr.gouv.etalab.mastodon.fragments.DisplayStatusFragment;
import fr.gouv.etalab.mastodon.fragments.TabLayoutSettingsFragment;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import mastodon.etalab.gouv.fr.mastodon.R;

import static fr.gouv.etalab.mastodon.helper.Helper.HOME_TIMELINE_INTENT;
import static fr.gouv.etalab.mastodon.helper.Helper.INTENT_ACTION;
import static fr.gouv.etalab.mastodon.helper.Helper.NOTIFICATION_INTENT;
import static fr.gouv.etalab.mastodon.helper.Helper.PREF_KEY_ID;
import static fr.gouv.etalab.mastodon.helper.Helper.changeUser;
import static fr.gouv.etalab.mastodon.helper.Helper.menuAccounts;
import static fr.gouv.etalab.mastodon.helper.Helper.updateHeaderAccountInfo;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnUpdateAccountInfoInterface {

    private FloatingActionButton toot;
    private boolean first = true;
    private HashMap<String, String> tagTile = new HashMap<>();
    private HashMap<String, Integer> tagItem = new HashMap<>();
    private Toolbar toolbar;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private View headerLayout;
    static final int MIN_DISTANCE = 100;
    private float downX, downY;
    private int currentScreen = 1;
    private actionSwipe currentAction;
    public static String currentLocale;

    private enum actionSwipe{
        RIGHT_TO_LEFT,
        LEFT_TO_RIGHT,
        POP
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);

        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_LIGHT);
        if( theme == Helper.THEME_LIGHT){
            setTheme(R.style.AppTheme_NoActionBar);
        }else {
            setTheme(R.style.AppThemeDark_NoActionBar);
        }
        setContentView(R.layout.activity_main);
        //Test if user is still log in
        if( ! Helper.isLoggedIn(getApplicationContext())) {
            //It is not, the user is redirected to the login page
            Intent myIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(myIntent);
            finish();
            return;
        }
        Helper.fillMapEmoji(getApplicationContext());
        //Here, the user is authenticated
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


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
        SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        options = new DisplayImageOptions.Builder().displayer(new RoundedBitmapDisplayer(90)).cacheInMemory(false)
                .cacheOnDisk(true).resetViewBeforeLoading(true).build();


        headerLayout = navigationView.getHeaderView(0);

        String prefKeyOauthTokenT = sharedpreferences.getString(Helper.PREF_KEY_OAUTH_TOKEN, null);
        Account account = new AccountDAO(getApplicationContext(), db).getAccountByToken(prefKeyOauthTokenT);
        updateHeaderAccountInfo(MainActivity.this, account, headerLayout, imageLoader, options);

        //Locked account can see follow request
        if (account.isLocked()) {
            navigationView.getMenu().findItem(R.id.nav_follow_request).setVisible(true);
        } else {
            navigationView.getMenu().findItem(R.id.nav_follow_request).setVisible(false);
        }

        LinearLayout owner_container = (LinearLayout) headerLayout.findViewById(R.id.owner_container);
        owner_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuAccounts(MainActivity.this);
            }
        });
        boolean matchingIntent = mamageNewIntent(getIntent());
        if (savedInstanceState == null && !matchingIntent) {
            navigationView.setCheckedItem(R.id.nav_home);
            navigationView.getMenu().performIdentifierAction(R.id.nav_home, 0);
        }
        //Title and menu selection when back pressed
        getSupportFragmentManager().addOnBackStackChangedListener(
            new FragmentManager.OnBackStackChangedListener() {
                public void onBackStackChanged() {
                    FragmentManager fm = getSupportFragmentManager();
                    if( fm != null && fm.getBackStackEntryCount() > 0) {
                        String fragmentTag = fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1).getName();
                        if( fragmentTag != null) {
                            if( tagTile.get(fragmentTag) != null)
                                setTitle(tagTile.get(fragmentTag));
                            if( tagItem.get(fragmentTag) != null) {
                                unCheckAllMenuItems(navigationView.getMenu());
                                if( navigationView.getMenu().findItem(tagItem.get(fragmentTag)) != null)
                                    navigationView.getMenu().findItem(tagItem.get(fragmentTag)).setChecked(true);
                            }
                        }
                    }
                }
        });

    }

    private void unCheckAllMenuItems(@NonNull final Menu menu) {
        int size = menu.size();
        for (int i = 0; i < size; i++) {
            final MenuItem item = menu.getItem(i);
            if(item.hasSubMenu()) {
                unCheckAllMenuItems(item.getSubMenu());
            } else {
                item.setChecked(false);
            }
        }
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
            SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null); //Id of the authenticated account
            final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            userIdIntent = extras.getString(PREF_KEY_ID); //Id of the account in the intent
            if (extras.getInt(INTENT_ACTION) == NOTIFICATION_INTENT){
                if( userId!= null && !userId.equals(userIdIntent)) //Connected account is different from the id in the intent
                    changeUser(MainActivity.this, userIdIntent); //Connects the account which is related to the notification
                unCheckAllMenuItems(navigationView.getMenu());
                navigationView.getMenu().performIdentifierAction(R.id.nav_notification, 0);
                if( navigationView.getMenu().findItem(R.id.nav_notification) != null)
                    navigationView.getMenu().findItem(R.id.nav_notification).setChecked(true);
                matchingIntent = true;
            }else if( extras.getInt(INTENT_ACTION) == HOME_TIMELINE_INTENT){
                if( userId!= null && !userId.equals(userIdIntent))  //Connected account is different from the id in the intent
                    changeUser(MainActivity.this, userIdIntent); //Connects the account which is related to the notification
                unCheckAllMenuItems(navigationView.getMenu());
                navigationView.getMenu().performIdentifierAction(R.id.nav_home, 0);
                if( navigationView.getMenu().findItem(R.id.nav_home) != null)
                    navigationView.getMenu().findItem(R.id.nav_home).setChecked(true);
                matchingIntent = true;
            }
        }else if( Intent.ACTION_SEND.equals(action) && type != null ){
            if ("text/plain".equals(type)) {
                String sharedSubject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (sharedText != null) {
                    Intent intentToot = new Intent(getApplicationContext(), TootActivity.class);
                    Bundle b = new Bundle();
                    b.putString("sharedSubject", sharedSubject);
                    b.putString("sharedContent", sharedText);
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
            super.onBackPressed();
            //Hide search bar on back pressed
            if( toolbar.getChildCount() > 0) {
                for (int i = 0; i < toolbar.getChildCount(); i++) {
                    if (toolbar.getChildAt(i) instanceof EditText) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow((toolbar.getChildAt(i)).getWindowToken(), 0);
                        toolbar.removeViewAt(i);
                        break;
                    }
                }
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.action_logout) {
            Helper.logout(getApplicationContext());
            Intent myIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(myIntent);
            finish();
            return true;
        }else if(id == R.id.action_about){
            Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
            startActivity(intent);
        }else if(id == R.id.action_privacy){
            Intent intent = new Intent(getApplicationContext(), PrivacyActivity.class);
            startActivity(intent);
        }else if(id == R.id.action_about_instance){
            Intent intent = new Intent(getApplicationContext(), InstanceActivity.class);
            startActivity(intent);
        } else if( id == R.id.action_cache){ //Cache clear feature
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
            builder.setMessage(getString(R.string.cache_message, String.format("%s Mo", String.format(Locale.getDefault(), "%.2f", cacheSize))))
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
                            Toast.makeText(MainActivity.this, getString(R.string.toast_cache_clear,String.format("%s Mo", String.format(Locale.getDefault(), "%.2f", finalCacheSize))), Toast.LENGTH_LONG).show();
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
        } else if(id == R.id.action_search){

            if( toolbar.getChildCount() > 0){
                for(int i = 0 ; i < toolbar.getChildCount() ; i++){
                    if(toolbar.getChildAt(i) instanceof EditText){
                        //Nothing in the search bar
                        if( ((EditText) toolbar.getChildAt(i)).getText().toString().trim().equals("")){
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow((toolbar.getChildAt(i)).getWindowToken(), 0);
                            toolbar.removeViewAt(i);
                            return true;
                        }else{
                            String searchTag = ((EditText) toolbar.getChildAt(i)).getText().toString();
                            toot.setVisibility(View.VISIBLE);
                            View view = this.getCurrentFocus();
                            //Hide keyboard
                            if (view != null) {
                                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            }

                            Intent intent = new Intent(MainActivity.this, SearchResultActivity.class);
                            intent.putExtra("search", searchTag);
                            startActivity(intent);
                            return true;
                        }

                    }
                }
                //Open the search bar
                final EditText search = new EditText(MainActivity.this);
                search.setSingleLine(true);
                search.setLayoutParams( new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,1.0f));
                toolbar.addView(search);
                search.requestFocus();
                search.setOnKeyListener(new View.OnKeyListener() {
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                                (keyCode == KeyEvent.KEYCODE_ENTER)) {
                            String searchTag = search.getText().toString();
                            toot.setVisibility(View.VISIBLE);
                            View view = getCurrentFocus();
                            //Hide keyboard
                            if (view != null) {
                                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            }

                            Intent intent = new Intent(MainActivity.this, SearchResultActivity.class);
                            intent.putExtra("search", searchTag);
                            startActivity(intent);
                            return true;
                        }
                        return false;
                    }
                });
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
            }

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume(){
        super.onResume();
        //Proceeds to update of the authenticated account
        if(Helper.isLoggedIn(getApplicationContext()))
            new UpdateAccountInfoByIDAsyncTask(getApplicationContext(), MainActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }




    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        //Remove the search bar
        if( toolbar.getChildCount() > 0) {
            for (int i = 0; i < toolbar.getChildCount(); i++) {
                if (toolbar.getChildAt(i) instanceof EditText) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow((toolbar.getChildAt(i)).getWindowToken(), 0);
                    toolbar.removeViewAt(i);
                    break;
                }
            }
        }
        DisplayStatusFragment statusFragment;
        DisplayAccountsFragment accountsFragment;
        Bundle bundle = new Bundle();
        FragmentManager fragmentManager = getSupportFragmentManager();
        String fragmentTag = null;
        currentScreen = -1;
        if (id == R.id.nav_home) {
            toot.setVisibility(View.VISIBLE);
            statusFragment = new DisplayStatusFragment();
            bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.HOME);
            statusFragment.setArguments(bundle);
            fragmentTag = "HOME_TIMELINE";
            currentScreen = 1;
            if(! first) {
                if( currentAction == actionSwipe.RIGHT_TO_LEFT)
                fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.main_app_container, statusFragment, fragmentTag).addToBackStack(fragmentTag).commit();
                else if( currentAction == actionSwipe.LEFT_TO_RIGHT)
                    fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left)
                            .replace(R.id.main_app_container, statusFragment, fragmentTag).addToBackStack(fragmentTag).commit();
                else
                    fragmentManager.beginTransaction().setCustomAnimations(R.anim.fadein, R.anim.fadeout)
                            .replace(R.id.main_app_container, statusFragment, fragmentTag).addToBackStack(fragmentTag).commit();
            }else{
                if( currentAction == actionSwipe.RIGHT_TO_LEFT)
                    fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                        .replace(R.id.main_app_container, statusFragment, fragmentTag).commit();
                else if( currentAction == actionSwipe.LEFT_TO_RIGHT)
                    fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left)
                            .replace(R.id.main_app_container, statusFragment, fragmentTag).commit();
                else
                    fragmentManager.beginTransaction().setCustomAnimations(R.anim.fadein, R.anim.fadeout)
                            .replace(R.id.main_app_container, statusFragment, fragmentTag).commit();
                first = false;
            }
        } else if (id == R.id.nav_local) {
            toot.setVisibility(View.VISIBLE);
            statusFragment = new DisplayStatusFragment();
            bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.LOCAL);
            statusFragment.setArguments(bundle);
            fragmentTag = "LOCAL_TIMELINE";
            currentScreen = 2;
            if( currentAction == actionSwipe.RIGHT_TO_LEFT)
                fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                    .replace(R.id.main_app_container, statusFragment, fragmentTag).addToBackStack(fragmentTag).commit();
            else if( currentAction == actionSwipe.LEFT_TO_RIGHT)
                fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left)
                        .replace(R.id.main_app_container, statusFragment, fragmentTag).addToBackStack(fragmentTag).commit();
            else
                fragmentManager.beginTransaction().setCustomAnimations(R.anim.fadein, R.anim.fadeout)
                        .replace(R.id.main_app_container, statusFragment, fragmentTag).addToBackStack(fragmentTag).commit();
        } else if (id == R.id.nav_global) {
            toot.setVisibility(View.VISIBLE);
            statusFragment = new DisplayStatusFragment();
            bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.PUBLIC);
            statusFragment.setArguments(bundle);
            fragmentTag = "PUBLIC_TIMELINE";
            currentScreen = 3;
            if( currentAction == actionSwipe.RIGHT_TO_LEFT)
                fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                    .replace(R.id.main_app_container, statusFragment, fragmentTag).addToBackStack(fragmentTag).commit();
            else if( currentAction == actionSwipe.LEFT_TO_RIGHT)
                fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left)
                        .replace(R.id.main_app_container, statusFragment, fragmentTag).addToBackStack(fragmentTag).commit();
            else
                fragmentManager.beginTransaction().setCustomAnimations(R.anim.fadein, R.anim.fadeout)
                        .replace(R.id.main_app_container, statusFragment, fragmentTag).addToBackStack(fragmentTag).commit();
        } else if (id == R.id.nav_settings) {
            toot.setVisibility(View.GONE);
            TabLayoutSettingsFragment tabLayoutSettingsFragment= new TabLayoutSettingsFragment();
            fragmentTag = "TABLAYOUT_SETTINGS";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, tabLayoutSettingsFragment, fragmentTag).addToBackStack(fragmentTag).commit();

        } else if (id == R.id.nav_favorites) {
            toot.setVisibility(View.GONE);
            statusFragment = new DisplayStatusFragment();
            bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.FAVOURITES);
            statusFragment.setArguments(bundle);
            fragmentTag = "FAVOURITES";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, statusFragment, fragmentTag).addToBackStack(fragmentTag).commit();
        } else if (id == R.id.nav_blocked) {
            toot.setVisibility(View.GONE);
            accountsFragment = new DisplayAccountsFragment();
            bundle.putSerializable("type", RetrieveAccountsAsyncTask.Type.BLOCKED);
            accountsFragment.setArguments(bundle);
            fragmentTag = "BLOCKS";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, accountsFragment, fragmentTag).addToBackStack(fragmentTag).commit();
        }else if (id == R.id.nav_muted) {
            toot.setVisibility(View.GONE);
            accountsFragment = new DisplayAccountsFragment();
            bundle.putSerializable("type", RetrieveAccountsAsyncTask.Type.MUTED);
            accountsFragment.setArguments(bundle);
            fragmentTag = "MUTED";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, accountsFragment, fragmentTag).addToBackStack(fragmentTag).commit();
        }else if( id == R.id.nav_notification){
            toot.setVisibility(View.GONE);
            DisplayNotificationsFragment notificationsFragment = new DisplayNotificationsFragment();
            fragmentTag = "NOTIFICATIONS";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, notificationsFragment, fragmentTag).addToBackStack(fragmentTag).commit();
        }else if( id == R.id.nav_follow_request){
            toot.setVisibility(View.GONE);
            DisplayFollowRequestSentFragment followRequestSentFragment = new DisplayFollowRequestSentFragment();
            fragmentTag = "FOLLOW_REQUEST_SENT";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, followRequestSentFragment, fragmentTag).addToBackStack(fragmentTag).commit();
        }
        setTitle(item.getTitle());
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
        if( getSupportActionBar() != null )
            getSupportActionBar().setTitle(title);
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

    /**
     * Manage touch event
     * Allows to swipe from timelines
     * @param event MotionEvent
     * @return boolean
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        //Default dispatchTouchEvent is returned when not in timeline page
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        //Menu is opened returns default action
        if( drawer.isDrawerOpen(GravityCompat.START))
            return super.dispatchTouchEvent(event);
        //Current screen is not one of the timelines
        if( currentScreen >3 || currentScreen < 1)
            return super.dispatchTouchEvent(event);

        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN: {
                downX = event.getX();
                downY = event.getY();
                return super.dispatchTouchEvent(event);
            }
            case MotionEvent.ACTION_UP: {
                float upX = event.getX();
                float upY = event.getY();
                float deltaX = downX - upX;
                float deltaY = downY - upY;
                // swipe horizontal
                if( downX > MIN_DISTANCE & (Math.abs(deltaX) > MIN_DISTANCE && Math.abs(deltaY) < MIN_DISTANCE)){
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                    if(deltaX < 0) { switchOnSwipe(actionSwipe.LEFT_TO_RIGHT); drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);return true; }
                    if(deltaX > 0) { switchOnSwipe(actionSwipe.RIGHT_TO_LEFT); drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);return true; }
                }else{
                    currentAction = actionSwipe.POP;
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }


    private void switchOnSwipe(actionSwipe action){
        currentScreen = (action == actionSwipe.LEFT_TO_RIGHT)?currentScreen-1:currentScreen+1;
        if( currentScreen > 3 )
            currentScreen = 1;
        if( currentScreen < 1)
            currentScreen = 3;
        currentAction = action;
        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        switch (currentScreen){
            case 1:
                unCheckAllMenuItems(navigationView.getMenu());
                navigationView.getMenu().performIdentifierAction(R.id.nav_home, 0);
                if( navigationView.getMenu().findItem(R.id.nav_home) != null)
                    navigationView.getMenu().findItem(R.id.nav_home).setChecked(true);
                break;
            case 2:
                unCheckAllMenuItems(navigationView.getMenu());
                navigationView.getMenu().performIdentifierAction(R.id.nav_local, 0);
                if( navigationView.getMenu().findItem(R.id.nav_local) != null)
                    navigationView.getMenu().findItem(R.id.nav_local).setChecked(true);
                break;
            case 3:
                unCheckAllMenuItems(navigationView.getMenu());
                navigationView.getMenu().performIdentifierAction(R.id.nav_global, 0);
                if( navigationView.getMenu().findItem(R.id.nav_global) != null)
                    navigationView.getMenu().findItem(R.id.nav_global).setChecked(true);
                break;
            default:
                break;
        }
    }


}
