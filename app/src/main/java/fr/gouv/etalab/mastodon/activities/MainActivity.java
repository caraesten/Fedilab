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
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.io.File;
import java.util.HashMap;

import fr.gouv.etalab.mastodon.asynctasks.UpdateAccountInfoByIDAsyncTask;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.fragments.DisplayAccountsFragment;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //Test if user is still log in
        if( ! Helper.isLoggedIn(getApplicationContext())) {
            //It is not, the user is redirected to the login page
            Intent myIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(myIntent);
            finish();
            return;
        }
        //Here, the user is authenticated
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //Image loader configuration
        imageLoader = ImageLoader.getInstance();
        File cacheDir = new File(getCacheDir(), getString(R.string.app_name));
        ImageLoaderConfiguration configImg = new ImageLoaderConfiguration.Builder(this)
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

        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String prefKeyOauthTokenT = sharedpreferences.getString(Helper.PREF_KEY_OAUTH_TOKEN, null);
        Account account = new AccountDAO(getApplicationContext(), db).getAccountByToken(prefKeyOauthTokenT);
        updateHeaderAccountInfo(account);
        boolean menuWasSelected = false;
        if( getIntent() != null && getIntent().getExtras() != null ){
            Bundle extras = getIntent().getExtras();
            if (extras.getInt(INTENT_ACTION) == NOTIFICATION_INTENT){
                navigationView.setCheckedItem(R.id.nav_notification);
                navigationView.getMenu().performIdentifierAction(R.id.nav_notification, 0);
                menuWasSelected = true;
            }else if( extras.getInt(INTENT_ACTION) == HOME_TIMELINE_INTENT){
                navigationView.setCheckedItem(R.id.nav_home);
                navigationView.getMenu().performIdentifierAction(R.id.nav_home, 0);
                menuWasSelected = true;
            }
        }
        if (savedInstanceState == null && !menuWasSelected) {
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
        if( intent == null || intent.getExtras() == null )
            return;
        Bundle extras = intent.getExtras();
        if( extras.containsKey(INTENT_ACTION) ){
            final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            if (extras.getInt(INTENT_ACTION) == NOTIFICATION_INTENT){
                navigationView.setCheckedItem(R.id.nav_notification);
                navigationView.getMenu().performIdentifierAction(R.id.nav_notification, 0);
            }else if( extras.getInt(INTENT_ACTION) == HOME_TIMELINE_INTENT){
                navigationView.setCheckedItem(R.id.nav_home);
                navigationView.getMenu().performIdentifierAction(R.id.nav_home, 0);
            }
        }
        intent.replaceExtras(new Bundle());
        intent.setAction("");
        intent.setData(null);
        intent.setFlags(0);
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
        }else if(id == R.id.action_search){

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
                            DisplayStatusFragment statusFragment = new DisplayStatusFragment();
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.TAG);
                            bundle.putString("tag", searchTag);
                            statusFragment.setArguments(bundle);
                            FragmentManager fragmentManager = getSupportFragmentManager();
                            fragmentManager.beginTransaction()
                                    .replace(R.id.main_app_container, statusFragment).commit();
                            View view = this.getCurrentFocus();
                            //Hide keyboard
                            if (view != null) {
                                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            }
                            return true;
                        }

                    }
                }
                //Open the search bar
                EditText search = new EditText(getApplicationContext());
                search.setSingleLine(true);
                search.setLayoutParams( new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,1.0f));
                toolbar.addView(search);
                search.requestFocus();
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
        if (id == R.id.nav_home) {
            toot.setVisibility(View.VISIBLE);
            statusFragment = new DisplayStatusFragment();
            bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.HOME);
            statusFragment.setArguments(bundle);
            fragmentTag = "HOME_TIMELINE";
            if(! first)
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, statusFragment, fragmentTag).addToBackStack(fragmentTag).commit();
            else{
                fragmentManager.beginTransaction()
                        .replace(R.id.main_app_container, statusFragment, fragmentTag).commit();
                first = false;
            }
        } else if (id == R.id.nav_local) {
            toot.setVisibility(View.VISIBLE);
            statusFragment = new DisplayStatusFragment();
            bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.LOCAL);
            statusFragment.setArguments(bundle);
            fragmentTag = "LOCAL_TIMELINE";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, statusFragment, fragmentTag).addToBackStack(fragmentTag).commit();

        } else if (id == R.id.nav_global) {
            toot.setVisibility(View.VISIBLE);
            statusFragment = new DisplayStatusFragment();
            bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.PUBLIC);
            statusFragment.setArguments(bundle);
            fragmentTag = "PUBLIC_TIMELINE";
            fragmentManager.beginTransaction()
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
            fragmentTag = "BLOCKS";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, accountsFragment, fragmentTag).addToBackStack(fragmentTag).commit();
        }else if( id == R.id.nav_notification){
            toot.setVisibility(View.GONE);
            DisplayNotificationsFragment notificationsFragment = new DisplayNotificationsFragment();
            fragmentTag = "NOTIFICATIONS";
            fragmentManager.beginTransaction()
                    .replace(R.id.main_app_container, notificationsFragment, fragmentTag).addToBackStack(fragmentTag).commit();
        }


        setTitle(item.getTitle());
        populateTitleWithTag(fragmentTag, item.getTitle().toString(), item.getItemId());
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void updateHeaderAccountInfo(Account account){
        ImageView profilePicture = (ImageView) headerLayout.findViewById(R.id.profilePicture);
        TextView username = (TextView) headerLayout.findViewById(R.id.username);
        TextView displayedName = (TextView) headerLayout.findViewById(R.id.displayedName);
        TextView ownerStatus = (TextView) headerLayout.findViewById(R.id.owner_status);
        TextView ownerFollowing = (TextView) headerLayout.findViewById(R.id.owner_following);
        TextView ownerFollowers = (TextView) headerLayout.findViewById(R.id.owner_followers);
        //Something wrong happened with the account recorded in db (ie: bad token)
        if( account == null ) {
            Helper.logout(getApplicationContext());
            Intent myIntent = new Intent(MainActivity.this, LoginActivity.class);
            Toast.makeText(getApplicationContext(),R.string.toast_error, Toast.LENGTH_LONG).show();
            startActivity(myIntent);
            finish(); //User is logged out to get a new token
        }else {
            ownerStatus.setText(String.valueOf(account.getStatuses_count()));
            ownerFollowers.setText(String.valueOf(account.getFollowers_count()));
            ownerFollowing.setText(String.valueOf(account.getFollowing_count()));
            username.setText(String.format("@%s",account.getUsername()));
            displayedName.setText(account.getDisplay_name());
            imageLoader.displayImage(account.getAvatar(), profilePicture, options);
        }
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
            updateHeaderAccountInfo(account);
        }
    }
}
