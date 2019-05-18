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

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Error;
import app.fedilab.android.client.Entities.Relationship;
import app.fedilab.android.client.Entities.Results;
import app.fedilab.android.drawers.AccountSearchDevAdapter;
import app.fedilab.android.helper.ExpandableHeightListView;
import app.fedilab.android.helper.Helper;
import es.dmoral.toasty.Toasty;
import app.fedilab.android.R;
import app.fedilab.android.asynctasks.RetrieveRelationshipAsyncTask;
import app.fedilab.android.asynctasks.RetrieveRemoteDataAsyncTask;
import app.fedilab.android.asynctasks.UpdateAccountInfoAsyncTask;
import app.fedilab.android.interfaces.OnRetrieveRelationshipInterface;
import app.fedilab.android.interfaces.OnRetrieveRemoteAccountInterface;


/**
 * Created by Thomas on 05/05/2017.
 * About activity
 */

public class AboutActivity extends BaseActivity implements OnRetrieveRemoteAccountInterface, OnRetrieveRelationshipInterface {

    private List<Account> developers = new ArrayList<>();
    private List<Account> contributors = new ArrayList<>();
    private List<Account> designers = new ArrayList<>();
    private List<Account> banners = new ArrayList<>();
    private List<Account> uxuidesigners = new ArrayList<>();
    private List<Account> support = new ArrayList<>();

    private AccountSearchDevAdapter accountSearchWebAdapterDeveloper;
    private AccountSearchDevAdapter accountSearchWebAdapterDesigner;
    private AccountSearchDevAdapter accountSearchWebAdapterContributors;
    private AccountSearchDevAdapter accountSearchWebAdapterBanners;
    private AccountSearchDevAdapter accountSearchWebAdapterUxUiDesigners;
    private AccountSearchDevAdapter accountSearchWebAdapterSupport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        switch (theme){
            case Helper.THEME_LIGHT:
                setTheme(R.style.AppTheme);
                break;
            case Helper.THEME_DARK:
                setTheme(R.style.AppThemeDark);
                break;
            case Helper.THEME_BLACK:
                setTheme(R.style.AppThemeBlack);
                break;
            default:
                setTheme(R.style.AppThemeDark);
        }

        if( getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null ) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.simple_bar, null);
            actionBar.setCustomView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            ImageView toolbar_close = actionBar.getCustomView().findViewById(R.id.toolbar_close);
            TextView toolbar_title = actionBar.getCustomView().findViewById(R.id.toolbar_title);
            toolbar_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            toolbar_title.setText(R.string.action_about);
            if (theme == Helper.THEME_LIGHT){
                Toolbar toolbar = actionBar.getCustomView().findViewById(R.id.toolbar);
                Helper.colorizeToolbar(toolbar, R.color.black, AboutActivity.this);
            }
        }
        setContentView(R.layout.activity_about);
        TextView about_version = findViewById(R.id.about_version);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            about_version.setText(getResources().getString(R.string.about_vesrion, version));
        } catch (PackageManager.NameNotFoundException ignored) {}

        ExpandableHeightListView lv_developers = findViewById(R.id.lv_developers);
        ExpandableHeightListView lv_designers = findViewById(R.id.lv_designers);
        ExpandableHeightListView lv_contributors = findViewById(R.id.lv_contributors);
        ExpandableHeightListView lv_banners = findViewById(R.id.lv_banners);
        ExpandableHeightListView lv_ux = findViewById(R.id.lv_ux);
        ExpandableHeightListView lv_support = findViewById(R.id.lv_support);

        Button about_code = findViewById(R.id.about_code);
        Button about_license = findViewById(R.id.about_license);
        Button about_thekinrar = findViewById(R.id.about_thekinrar);
        Button about_trunk = findViewById(R.id.about_trunk);


        TextView txt_developers, txt_ux, txt_support, txt_banners, txt_designers, txt_thankyou1, txt_thankyou2;
        txt_developers = findViewById(R.id.txt_developers);
        txt_ux = findViewById(R.id.txt_ux);
        txt_support = findViewById(R.id.txt_support);
        txt_banners = findViewById(R.id.txt_banners);
        txt_designers = findViewById(R.id.txt_designers);
        txt_thankyou1 = findViewById(R.id.txt_thankyou1);
        txt_thankyou2 = findViewById(R.id.txt_thankyou2);


        about_code.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://gitlab.com/tom79/mastalab"));
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

        about_trunk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://communitywiki.org/trunk"));
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
        Button about_translation = findViewById(R.id.about_translation);
        about_translation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://translate.yandex.com/"));
                startActivity(browserIntent);
            }
        });

        TextView about_wiki = findViewById(R.id.about_wiki);
        SpannableString content = new SpannableString(about_wiki.getText().toString());
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        about_wiki.setText(content);
        about_wiki.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://fedilab.app/page/howto/"));
                startActivity(browserIntent);
            }
        });


        Button about_support = findViewById(R.id.about_support);
        about_support.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://liberapay.com/tom79"));
                startActivity(browserIntent);
            }
        });

        Button paypal = findViewById(R.id.about_support_paypal);
        paypal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.me/Mastalab"));
                startActivity(browserIntent);
            }
        });


        TextView about_website = findViewById(R.id.about_website);
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
        lv_banners.setExpanded(true);
        lv_ux.setExpanded(true);
        lv_support.setExpanded(true);

        accountSearchWebAdapterContributors = new AccountSearchDevAdapter(AboutActivity.this, contributors);
        lv_contributors.setAdapter(accountSearchWebAdapterContributors);
        accountSearchWebAdapterDesigner = new AccountSearchDevAdapter(AboutActivity.this, designers);
        lv_designers.setAdapter(accountSearchWebAdapterDesigner);
        accountSearchWebAdapterDeveloper = new AccountSearchDevAdapter(AboutActivity.this, developers);
        lv_developers.setAdapter(accountSearchWebAdapterDeveloper);
        accountSearchWebAdapterBanners = new AccountSearchDevAdapter(AboutActivity.this, banners);
        lv_banners.setAdapter(accountSearchWebAdapterBanners);
        accountSearchWebAdapterUxUiDesigners = new AccountSearchDevAdapter(AboutActivity.this, uxuidesigners);
        lv_ux.setAdapter(accountSearchWebAdapterUxUiDesigners);
        accountSearchWebAdapterSupport = new AccountSearchDevAdapter(AboutActivity.this, support);
        lv_support.setAdapter(accountSearchWebAdapterSupport);

        if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON || MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA ) {
            new RetrieveRemoteDataAsyncTask(getApplicationContext(), "tom79", "mastodon.social", AboutActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            new RetrieveRemoteDataAsyncTask(getApplicationContext(), "mmarif", "mastodon.social", AboutActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            new RetrieveRemoteDataAsyncTask(getApplicationContext(), "guzzisti", "mastodon.social", AboutActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            new RetrieveRemoteDataAsyncTask(getApplicationContext(), "NateLikesSheep", "mastodon.art", AboutActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            new RetrieveRemoteDataAsyncTask(getApplicationContext(), "kasun", "mastodon.social", AboutActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            new RetrieveRemoteDataAsyncTask(getApplicationContext(), "PhotonQyv", "mastodon.xyz", AboutActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            new RetrieveRemoteDataAsyncTask(getApplicationContext(), "angrytux", "social.tchncs.de", AboutActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }else{
            SpannableString name = new SpannableString("@tom79@mastodon.social");
            name.setSpan(new UnderlineSpan(), 0, name.length(), 0);
            txt_developers.setText(name);
            txt_developers.setVisibility(View.VISIBLE);
            txt_developers.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Helper.openBrowser(AboutActivity.this, "https://mastodon.social/@tom79");
                }
            });
            name = new SpannableString("@mmarif@mastodon.social");
            name.setSpan(new UnderlineSpan(), 0, name.length(), 0);
            txt_ux.setText(name);
            txt_ux.setVisibility(View.VISIBLE);
            txt_ux.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Helper.openBrowser(AboutActivity.this, "https://mastodon.social/@mmarif");
                }
            });
            name = new SpannableString("@guzzisti@mastodon.social");
            name.setSpan(new UnderlineSpan(), 0, name.length(), 0);
            txt_support.setText(name);
            txt_support.setVisibility(View.VISIBLE);
            txt_support.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Helper.openBrowser(AboutActivity.this, "https://mastodon.social/@guzzisti");
                }
            });
            name = new SpannableString("@NateLikesSheep@mastodon.art");
            name.setSpan(new UnderlineSpan(), 0, name.length(), 0);
            txt_banners.setText(name);
            txt_banners.setVisibility(View.VISIBLE);
            txt_banners.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Helper.openBrowser(AboutActivity.this, "https://mastodon.art/@NateLikesSheep");
                }
            });
            name = new SpannableString("@kasun@mastodon.social");
            name.setSpan(new UnderlineSpan(), 0, name.length(), 0);
            txt_designers.setText(name);
            txt_designers.setVisibility(View.VISIBLE);
            txt_designers.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Helper.openBrowser(AboutActivity.this, "https://mastodon.social/@kasun");
                }
            });
            name = new SpannableString("@PhotonQyv@mastodon.xyz");
            name.setSpan(new UnderlineSpan(), 0, name.length(), 0);
            txt_thankyou1.setText(name);
            txt_thankyou1.setVisibility(View.VISIBLE);
            txt_thankyou1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Helper.openBrowser(AboutActivity.this, "https://mastodon.xyz/@PhotonQyv");
                }
            });
            name = new SpannableString("@angrytux@social.tchncs.de");
            name.setSpan(new UnderlineSpan(), 0, name.length(), 0);
            txt_thankyou2.setText(name);
            txt_thankyou2.setVisibility(View.VISIBLE);
            txt_thankyou2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Helper.openBrowser(AboutActivity.this, "https://social.tchncs.de/@angrytux");
                }
            });
        }
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
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        if( results == null){
            Toasty.error(getApplicationContext(), getString(R.string.toast_error),Toast.LENGTH_LONG).show();
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
                case "guzzisti":
                    support.add(account);
                    accountSearchWebAdapterSupport.notifyDataSetChanged();
                    break;
                case "NateLikesSheep":
                    banners.add(account);
                    accountSearchWebAdapterBanners.notifyDataSetChanged();
                    break;
                case "kasun":
                    designers.add(account);
                    accountSearchWebAdapterDesigner.notifyDataSetChanged();
                    break;
                case "mmarif":
                    uxuidesigners.add(account);
                    accountSearchWebAdapterUxUiDesigners.notifyDataSetChanged();
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
        if( banners != null){
            for(Account account: banners){
                new RetrieveRelationshipAsyncTask(getApplicationContext(), account.getId(),AboutActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
        if( contributors != null){
            for(Account account: contributors){
                new RetrieveRelationshipAsyncTask(getApplicationContext(), account.getId(),AboutActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
        if( uxuidesigners != null){
            for(Account account: uxuidesigners){
                new RetrieveRelationshipAsyncTask(getApplicationContext(), account.getId(),AboutActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
        if( support != null){
            for(Account account: support){
                new RetrieveRelationshipAsyncTask(getApplicationContext(), account.getId(),AboutActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    @Override
    public void onRetrieveRelationship(Relationship relationship, Error error) {
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
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
        for( int i = 0 ; i < banners.size() ; i++){
            if( banners.get(i).getId() != null && banners.get(i).getId().equals(relationship.getId())){
                banners.get(i).setFollowing(relationship.isFollowing() || userId.trim().equals(relationship.getId()));
                accountSearchWebAdapterBanners.notifyDataSetChanged();
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
        for( int i = 0 ; i < uxuidesigners.size() ; i++){
            if( uxuidesigners.get(i).getId() != null && uxuidesigners.get(i).getId().equals(relationship.getId())){
                uxuidesigners.get(i).setFollowing(relationship.isFollowing() || userId.trim().equals(relationship.getId()));
                accountSearchWebAdapterUxUiDesigners.notifyDataSetChanged();
                break;
            }
        }
        for( int i = 0 ; i < support.size() ; i++){
            if( support.get(i).getId() != null && support.get(i).getId().equals(relationship.getId())){
                support.get(i).setFollowing(relationship.isFollowing() || userId.trim().equals(relationship.getId()));
                accountSearchWebAdapterSupport.notifyDataSetChanged();
                break;
            }
        }
    }
}
