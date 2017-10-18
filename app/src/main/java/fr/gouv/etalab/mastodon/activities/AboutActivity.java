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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import fr.gouv.etalab.mastodon.asynctasks.RetrieveDeveloperAccountsAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveRelationshipAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveRemoteAccountsAsyncTask;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Error;
import fr.gouv.etalab.mastodon.client.Entities.Relationship;
import fr.gouv.etalab.mastodon.client.Entities.Results;
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

public class AboutActivity extends AppCompatActivity implements OnRetrieveRemoteAccountInterface, OnRetrieveRelationshipInterface {

    private List<Account> developers = new ArrayList<>();
    private List<Account> contributors = new ArrayList<>();
    private List<Account> designers = new ArrayList<>();
    private AccountSearchDevAdapter accountSearchWebAdapterDeveloper;
    private AccountSearchDevAdapter accountSearchWebAdapterDesigner;
    private AccountSearchDevAdapter accountSearchWebAdapterContributors;


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
        ExpandableHeightListView lv_designers = (ExpandableHeightListView) findViewById(R.id.lv_designers);
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
        setTitle(R.string.action_about);
        lv_contributors.setExpanded(true);
        lv_developers.setExpanded(true);
        lv_designers.setExpanded(true);

        accountSearchWebAdapterContributors = new AccountSearchDevAdapter(AboutActivity.this, contributors);
        lv_contributors.setAdapter(accountSearchWebAdapterContributors);
        accountSearchWebAdapterDesigner = new AccountSearchDevAdapter(AboutActivity.this, designers);
        lv_designers.setAdapter(accountSearchWebAdapterDesigner);
        accountSearchWebAdapterDeveloper = new AccountSearchDevAdapter(AboutActivity.this, developers);
        lv_developers.setAdapter(accountSearchWebAdapterDeveloper);

        new RetrieveRemoteAccountsAsyncTask(getApplicationContext(), "tom79", "mastodon.social", AboutActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new RetrieveRemoteAccountsAsyncTask(getApplicationContext(), "daycode", "mastodon.social", AboutActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new RetrieveRemoteAccountsAsyncTask(getApplicationContext(), "PhotonQyv", "mastodon.xyz", AboutActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new RetrieveRemoteAccountsAsyncTask(getApplicationContext(), "angrytux", "social.tchncs.de", AboutActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
    public void onRetrieveRemoteAccount(Results results) {
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        if( results == null){
            boolean show_error_messages = sharedpreferences.getBoolean(Helper.SET_SHOW_ERROR_MESSAGES, true);
            if( show_error_messages)
                Toast.makeText(getApplicationContext(), R.string.toast_error,Toast.LENGTH_LONG).show();
            return;
        }
        List<Account> accounts = results.getAccounts();
        Account account;
        if( accounts != null && accounts.size() > 0){
            account = accounts.get(0);
            account.setFollowing(true);
            switch (account.getUsername()) {
                case "tom79":
                    developers.add(account);
                    accountSearchWebAdapterDeveloper.notifyDataSetChanged();
                    break;
                case "daycode":
                    designers.add(account);
                    accountSearchWebAdapterDesigner.notifyDataSetChanged();
                    break;
                default:
                    contributors.add(account);
                    accountSearchWebAdapterContributors.notifyDataSetChanged();
                    break;
            }
            new RetrieveRelationshipAsyncTask(getApplicationContext(), account.getId(),AboutActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }


    @Override
    public void onResume(){
        super.onResume();
        if( developers != null){
            for(Account account: developers){
                new RetrieveRelationshipAsyncTask(getApplicationContext(), account.getId(),AboutActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
        if( designers != null){
            for(Account account: designers){
                new RetrieveRelationshipAsyncTask(getApplicationContext(), account.getId(),AboutActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
        if( contributors != null){
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
            if( developers.get(i).getId() != null && developers.get(i).getId().equals(relationship.getId())){
                developers.get(i).setFollowing(relationship.isFollowing() || userId.trim().equals(relationship.getId()));
                accountSearchWebAdapterDeveloper.notifyDataSetChanged();
                break;
            }
        }
        for( int i = 0 ; i < designers.size() ; i++){
            if( designers.get(i).getId() != null && designers.get(i).getId().equals(relationship.getId())){
                designers.get(i).setFollowing(relationship.isFollowing() || userId.trim().equals(relationship.getId()));
                accountSearchWebAdapterDesigner.notifyDataSetChanged();
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
