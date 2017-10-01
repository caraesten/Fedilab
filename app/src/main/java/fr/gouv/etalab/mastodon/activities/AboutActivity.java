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

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import fr.gouv.etalab.mastodon.asynctasks.RetrieveDeveloperAccountsAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveRelationshipAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveRemoteAccountsAsyncTask;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Error;
import fr.gouv.etalab.mastodon.client.Entities.Relationship;
import fr.gouv.etalab.mastodon.drawers.AccountSearchDevAdapter;
import fr.gouv.etalab.mastodon.helper.ExpandableHeightListView;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveRelationshipInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveRemoteAccountInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveSearchDevelopersAccountshInterface;
import mastodon.etalab.gouv.fr.mastodon.R;


/**
 * Created by Thomas on 05/05/2017.
 * About activity
 */

public class AboutActivity extends AppCompatActivity implements OnRetrieveRemoteAccountInterface, OnRetrieveSearchDevelopersAccountshInterface, OnRetrieveRelationshipInterface {

    private List<Account> developers = new ArrayList<>();
    private List<Account> contributors = new ArrayList<>();
    private AccountSearchDevAdapter accountSearchWebAdapterDeveloper;
    private AccountSearchDevAdapter accountSearchWebAdapterContributors;
    @SuppressWarnings("FieldCanBeLocal")
    private int DEV_COUNT = 1, CONTRIBUTOR_COUNT = 2;

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
        if( getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_about);
        TextView about_version = (TextView) findViewById(R.id.about_version);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            about_version.setText(getResources().getString(R.string.about_vesrion, version));
        } catch (PackageManager.NameNotFoundException ignored) {}

        ExpandableHeightListView lv_developers = (ExpandableHeightListView) findViewById(R.id.lv_developers);
        ExpandableHeightListView lv_contributors = (ExpandableHeightListView) findViewById(R.id.lv_contributors);
        Button about_code = (Button) findViewById(R.id.about_code);
        Button about_license = (Button) findViewById(R.id.about_license);
        Button about_thekinrar = (Button) findViewById(R.id.about_thekinrar);

        about_code.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://bitbucket.org/tom79/mastodon_etalab/src"));
               startActivity(browserIntent);
           }
        });

        about_thekinrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://instances.social/"));
                startActivity(browserIntent);
            }
        });

        about_license.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.gnu.org/licenses/quick-guide-gplv3.fr.html"));
                startActivity(browserIntent);
            }
        });
        Button about_translation = (Button) findViewById(R.id.about_translation);
        about_translation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://translate.yandex.com/"));
                startActivity(browserIntent);
            }
        });


        if( theme == Helper.THEME_LIGHT) {
            about_code.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            about_thekinrar.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            about_translation.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            about_license.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        }

        TextView about_website = (TextView) findViewById(R.id.about_website);
        about_website.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://tom79.bitbucket.io"));
                startActivity(browserIntent);
            }
        });

        lv_contributors.setExpanded(true);
        lv_developers.setExpanded(true);
        accountSearchWebAdapterContributors = new AccountSearchDevAdapter(AboutActivity.this, contributors);
        lv_contributors.setAdapter(accountSearchWebAdapterContributors);
        accountSearchWebAdapterDeveloper = new AccountSearchDevAdapter(AboutActivity.this, developers);
        lv_developers.setAdapter(accountSearchWebAdapterDeveloper);
        new RetrieveDeveloperAccountsAsyncTask(getApplicationContext(), AboutActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
    public void onRetrieveRemoteAccount(boolean error, String name, String username, String instance_name, boolean locked, String avatar, String bio, String statusCount, String followingCount, String followersCount) {
        if( error){
            return;
        }
        Account account = new Account();
        account.setInstance(instance_name);
        account.setAcct(username + "@" + instance_name);
        account.setAvatar(avatar);
        account.setDisplay_name(username);
        account.setStatuses_count_str(statusCount);
        account.setFollowers_count_str(followersCount);
        account.setFollowing_count_str(followingCount);
        account.setUsername(name);
        account.setLocked(locked);
        account.setNote(bio);
        account.setFollowing(false);
        account.setRemote(true);

        if( username.equals("@tschneider")) {
            developers.add(account);
            accountSearchWebAdapterDeveloper.notifyDataSetChanged();
        }else {
            contributors.add(account);
            accountSearchWebAdapterContributors.notifyDataSetChanged();
        }

    }

    @Override
    public void onRetrieveSearchDevelopersAccounts(ArrayList<Account> accounts) {
        if( accounts == null || accounts.size() == 0) {
            new RetrieveRemoteAccountsAsyncTask("tschneider", "mastodon.etalab.gouv.fr", AboutActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            new RetrieveRemoteAccountsAsyncTask("PhotonQyv", "mastodon.xyz", AboutActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            new RetrieveRemoteAccountsAsyncTask("angrytux", "social.tchncs.de", AboutActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            return;
        }
        boolean tschneider = false;
        boolean PhotonQyv = false;
        boolean angrytux = false;

        for(Account account: accounts){
            if( account.getUsername().equals("tschneider")){
                account.setFollowing(false);
                account.setRemote(false);
                developers.add(account);
                accountSearchWebAdapterDeveloper.notifyDataSetChanged();
                tschneider = true;
                new RetrieveRelationshipAsyncTask(getApplicationContext(), account.getId(),AboutActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
            if( account.getUsername().equals("PhotonQyv")){
                account.setFollowing(false);
                account.setRemote(false);
                contributors.add(account);
                accountSearchWebAdapterContributors.notifyDataSetChanged();
                PhotonQyv = true;
                new RetrieveRelationshipAsyncTask(getApplicationContext(), account.getId(),AboutActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
            if( account.getUsername().equals("angrytux")){
                account.setFollowing(false);
                account.setRemote(false);
                contributors.add(account);
                accountSearchWebAdapterContributors.notifyDataSetChanged();
                angrytux = true;
                new RetrieveRelationshipAsyncTask(getApplicationContext(), account.getId(),AboutActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
        if( !tschneider)
            new RetrieveRemoteAccountsAsyncTask("tschneider", "mastodon.etalab.gouv.fr", AboutActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        if( !PhotonQyv)
            new RetrieveRemoteAccountsAsyncTask("PhotonQyv", "mastodon.xyz", AboutActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        if( !angrytux)
            new RetrieveRemoteAccountsAsyncTask("angrytux", "social.tchncs.de", AboutActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onResume(){
        super.onResume();
        if( developers != null && developers.size() == DEV_COUNT){
            for(Account account: developers){
                new RetrieveRelationshipAsyncTask(getApplicationContext(), account.getId(),AboutActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
        if( contributors != null && contributors.size() == CONTRIBUTOR_COUNT){
            for(Account account: contributors){
                new RetrieveRelationshipAsyncTask(getApplicationContext(), account.getId(),AboutActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }
    @Override
    public void onRetrieveRelationship(Relationship relationship, Error error) {
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, "");
        if( error != null){
            return;
        }
        for( int i = 0 ; i < developers.size() ; i++){
            if( contributors.get(i).getId() != null && developers.get(i).getId().equals(relationship.getId())){
                developers.get(i).setFollowing(relationship.isFollowing() || userId.trim().equals(relationship.getId()));
                accountSearchWebAdapterDeveloper.notifyDataSetChanged();
                break;
            }
        }
        for( int i = 0 ; i < contributors.size() ; i++){
            if( contributors.get(i).getId() != null && contributors.get(i).getId().equals(relationship.getId())){
                contributors.get(i).setFollowing(relationship.isFollowing() || userId.trim().equals(relationship.getId()));
                accountSearchWebAdapterContributors.notifyDataSetChanged();
                break;
            }
        }
    }
}
