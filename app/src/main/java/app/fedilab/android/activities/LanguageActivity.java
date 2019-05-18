/* Copyright 2018 Thomas Schneider
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
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
import app.fedilab.android.interfaces.OnRetrieveRelationshipInterface;
import app.fedilab.android.interfaces.OnRetrieveRemoteAccountInterface;


/**
 * Created by Thomas on 22/12/2018.
 * Language activity
 */

public class LanguageActivity extends BaseActivity implements OnRetrieveRemoteAccountInterface, OnRetrieveRelationshipInterface {

    private List<Account> translators = new ArrayList<>();

    private AccountSearchDevAdapter translatorManager;
    private int count2 = 0;

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
            toolbar_title.setText(R.string.languages);
            if (theme == Helper.THEME_LIGHT){
                Toolbar toolbar = actionBar.getCustomView().findViewById(R.id.toolbar);
                Helper.colorizeToolbar(toolbar, R.color.black, LanguageActivity.this);
            }
        }
        setContentView(R.layout.activity_language);



        Button about_translation = findViewById(R.id.about_translation);


        about_translation.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://crowdin.com/project/mastalab"));
               startActivity(browserIntent);
           }
        });

        ExpandableHeightListView lv_translator_manager = findViewById(R.id.lv_translator_manager);

        setTitle(R.string.languages);
        lv_translator_manager.setExpanded(true);

        translatorManager = new AccountSearchDevAdapter(LanguageActivity.this, translators);
        lv_translator_manager.setAdapter(translatorManager);


        new RetrieveRemoteDataAsyncTask(getApplicationContext(), "ButterflyOfFire", "mstdn.fr", LanguageActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);



        String currentLanguage = sharedpreferences.getString(Helper.SET_DEFAULT_LOCALE_NEW, Helper.localeToStringStorage(Locale.getDefault()));
        Locale currentLocale = Helper.restoreLocaleFromString(currentLanguage);
        final Spinner set_change_locale = findViewById(R.id.set_change_locale);
        ArrayAdapter<String> adapterLocale = new ArrayAdapter<>(LanguageActivity.this,
                android.R.layout.simple_spinner_dropdown_item, Helper.getLocales(getApplicationContext()));

        set_change_locale.setAdapter(adapterLocale);

        int positionSpinnerLanguage = Helper.languageSpinnerPosition(getApplicationContext());
        set_change_locale.setSelection(positionSpinnerLanguage);
        set_change_locale.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if( count2 > 0 ) {
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    switch (position) {
                        case 0:
                            editor.remove(Helper.SET_DEFAULT_LOCALE_NEW);
                            editor.commit();
                            break;
                        case 1:
                            editor.putString(Helper.SET_DEFAULT_LOCALE_NEW, "en");
                            editor.commit();
                            break;
                        case 2:
                            editor.putString(Helper.SET_DEFAULT_LOCALE_NEW, "fr");
                            editor.commit();
                            break;
                        case 3:
                            editor.putString(Helper.SET_DEFAULT_LOCALE_NEW, "de");
                            editor.commit();
                            break;
                        case 4:
                            editor.putString(Helper.SET_DEFAULT_LOCALE_NEW, "it");
                            editor.commit();
                            break;
                        case 5:
                            editor.putString(Helper.SET_DEFAULT_LOCALE_NEW, "ja");
                            editor.commit();
                            break;
                        case 6:
                            editor.putString(Helper.SET_DEFAULT_LOCALE_NEW,"zh-TW");
                            editor.commit();
                            break;
                        case 7:
                            editor.putString(Helper.SET_DEFAULT_LOCALE_NEW, "zh-CN");
                            editor.commit();
                            break;
                        case 8:
                            editor.putString(Helper.SET_DEFAULT_LOCALE_NEW, "eu");
                            editor.commit();
                            break;
                        case 9:
                            editor.putString(Helper.SET_DEFAULT_LOCALE_NEW, "ar");
                            editor.commit();
                            break;
                        case 10:
                            editor.putString(Helper.SET_DEFAULT_LOCALE_NEW, "nl");
                            editor.commit();
                            break;
                        case 11:
                            editor.putString(Helper.SET_DEFAULT_LOCALE_NEW, "gl");
                            editor.commit();
                            break;
                        case 12:
                            editor.putString(Helper.SET_DEFAULT_LOCALE_NEW, "el");
                            editor.commit();
                            break;
                        case 13:
                            editor.putString(Helper.SET_DEFAULT_LOCALE_NEW, "pt");
                            editor.commit();
                            break;
                        case 14:
                            editor.putString(Helper.SET_DEFAULT_LOCALE_NEW, "es");
                            editor.commit();
                            break;
                        case 15:
                            editor.putString(Helper.SET_DEFAULT_LOCALE_NEW, "pl");
                            editor.commit();
                            break;
                        case 16:
                            editor.putString(Helper.SET_DEFAULT_LOCALE_NEW, "sr");
                            editor.commit();
                            break;
                        case 17:
                            editor.putString(Helper.SET_DEFAULT_LOCALE_NEW, "uk");
                            editor.commit();
                        case 18:
                            editor.putString(Helper.SET_DEFAULT_LOCALE_NEW, "ru");
                            editor.commit();
                        case 19:
                            editor.putString(Helper.SET_DEFAULT_LOCALE_NEW, "no");
                            editor.commit();
                            break;
                    }

                    PackageManager packageManager = getPackageManager();
                    Intent intent = packageManager.getLaunchIntentForPackage(getPackageName());
                    assert intent != null;
                    ComponentName componentName = intent.getComponent();
                    Intent mainIntent = Intent.makeRestartActivityTask(componentName);
                    startActivity(mainIntent);
                    Runtime.getRuntime().exit(0);
                }
                count2++;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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
                case "ButterflyOfFire":
                    translators.add(account);
                    translatorManager.notifyDataSetChanged();
                    break;
            }
            new RetrieveRelationshipAsyncTask(getApplicationContext(), account.getId(),LanguageActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }


    @Override
    public void onResume(){
        super.onResume();
        if( translators != null){
            for(Account account: translators){
                new RetrieveRelationshipAsyncTask(getApplicationContext(), account.getId(),LanguageActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        for( int i = 0 ; i < translators.size() ; i++){
            if( translators.get(i).getId() != null && translators.get(i).getId().equals(relationship.getId())){
                translators.get(i).setFollowing(relationship.isFollowing() || userId.trim().equals(relationship.getId()));
                translatorManager.notifyDataSetChanged();
                break;
            }
        }
    }
}
