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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
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

import es.dmoral.toasty.Toasty;
import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveOpenCollectiveAsyncTask;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Results;
import fr.gouv.etalab.mastodon.drawers.AccountSearchDevAdapter;
import fr.gouv.etalab.mastodon.helper.ExpandableHeightListView;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveRemoteAccountInterface;

import static fr.gouv.etalab.mastodon.helper.Helper.THEME_LIGHT;


/**
 * Created by Thomas on 08/02/2019.
 * Opencollective activity
 */

public class OpencollectiveActivity extends BaseActivity implements OnRetrieveRemoteAccountInterface {

    private List<Account> bakers = new ArrayList<>();
    private List<Account> sponsors = new ArrayList<>();

    private AccountSearchDevAdapter backersAdapter;
    private AccountSearchDevAdapter sponsorsAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
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
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
            toolbar_title.setText("Open Collective");
            if (theme == THEME_LIGHT){
                Toolbar toolbar = actionBar.getCustomView().findViewById(R.id.toolbar);
                Helper.colorizeToolbar(toolbar, R.color.black, OpencollectiveActivity.this);
            }
        }
        setContentView(R.layout.activity_opencollective);


        ExpandableHeightListView lv_backers = findViewById(R.id.lv_backers);
        ExpandableHeightListView lv_sponsors = findViewById(R.id.lv_sponsors);


        Button about_opencollective = findViewById(R.id.about_opencollective);

        about_opencollective.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://opencollective.com/mastalab"));
               startActivity(browserIntent);
           }
        });

        setTitle("Open Collective");
        lv_backers.setExpanded(true);
        lv_sponsors.setExpanded(true);


        backersAdapter = new AccountSearchDevAdapter(OpencollectiveActivity.this, bakers);
        lv_backers.setAdapter(backersAdapter);
        sponsorsAdapter = new AccountSearchDevAdapter(OpencollectiveActivity.this, sponsors);
        lv_sponsors.setAdapter(sponsorsAdapter);

        new RetrieveOpenCollectiveAsyncTask(getApplicationContext(), RetrieveOpenCollectiveAsyncTask.Type.BACKERS, OpencollectiveActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new RetrieveOpenCollectiveAsyncTask(getApplicationContext(),  RetrieveOpenCollectiveAsyncTask.Type.SPONSORS, OpencollectiveActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        if( results == null){
            Toasty.error(getApplicationContext(), getString(R.string.toast_error),Toast.LENGTH_LONG).show();
            return;
        }
        List<Account> accounts = results.getAccounts();
        if( accounts != null && accounts.size() > 0){
            if( accounts.get(0).getSocial().equals("OPENCOLLECTIVE_BACKER")){
                bakers.addAll(accounts);
                backersAdapter.notifyDataSetChanged();
            }else if( accounts.get(0).getSocial().equals("OPENCOLLECTIVE_SPONSOR")){
                sponsors.addAll(accounts);
                sponsorsAdapter.notifyDataSetChanged();
            }
        }

    }

}
