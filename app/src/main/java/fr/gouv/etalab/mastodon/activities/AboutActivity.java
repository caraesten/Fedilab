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
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import fr.gouv.etalab.mastodon.asynctasks.RetrieveDeveloperAccountsAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveRemoteAccountsAsyncTask;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.drawers.AccountSearchWebAdapter;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveRemoteAccountInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveSearcAccountshInterface;
import mastodon.etalab.gouv.fr.mastodon.R;


/**
 * Created by Thomas on 05/05/2017.
 * About activity
 */

public class AboutActivity extends AppCompatActivity implements OnRetrieveRemoteAccountInterface {

    private ListView lv_developers;
    private ListView lv_contributors;
    private List<Account> contributors = new ArrayList<>();
    private AccountSearchWebAdapter accountSearchWebAdapter;

    @SuppressWarnings("deprecation")
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

        lv_developers = (ListView) findViewById(R.id.lv_developers);
        lv_contributors = (ListView) findViewById(R.id.lv_contributors);
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

        accountSearchWebAdapter = new AccountSearchWebAdapter(AboutActivity.this, contributors);
        lv_contributors.setAdapter(accountSearchWebAdapter);
        new RetrieveRemoteAccountsAsyncTask("tschneider", "mastodon.etalab.gouv.fr", AboutActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new RetrieveRemoteAccountsAsyncTask("PhotonQyv", "mastodon.xyz", AboutActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new RetrieveRemoteAccountsAsyncTask("angrytux", "social.tchncs.de", AboutActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
    public void onRetrieveRemoteAccount(boolean error, String name, String username, String instance_name, boolean locked, String avatar, String bio, int statusCount, int followingCount, int followersCount) {
        if( error){
            return;
        }
        Account account = new Account();
        account.setInstance(instance_name);
        account.setAcct(username + "@" + instance_name);
        account.setAvatar(avatar);
        account.setDisplay_name(username);
        account.setStatuses_count(statusCount);
        account.setFollowers_count(followersCount);
        account.setFollowing_count(followingCount);
        account.setUsername(name);
        account.setLocked(locked);
        account.setNote(bio);


        if( username.equals("tschneider")) {
            List<Account> selectedAccount = new ArrayList<>();
            selectedAccount.add(account);
            AccountSearchWebAdapter accountSearchWebAdapter = new AccountSearchWebAdapter(AboutActivity.this, selectedAccount);
            lv_developers.setAdapter(accountSearchWebAdapter);
        }else {
            contributors.add(account);
            accountSearchWebAdapter.notifyDataSetChanged();
        }

    }
}
