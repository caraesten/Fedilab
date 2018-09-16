/* Copyright 2018 Thomas Schneider
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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.asynctasks.ManageListsAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.PostActionAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.WhoToFollowAsyncTask;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Error;
import fr.gouv.etalab.mastodon.client.Entities.TrunkAccount;
import fr.gouv.etalab.mastodon.drawers.WhoToFollowAccountsAdapter;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnListActionInterface;
import fr.gouv.etalab.mastodon.interfaces.OnPostActionInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveWhoToFollowInterface;

import static fr.gouv.etalab.mastodon.helper.Helper.THEME_BLACK;


/**
 * Created by Thomas on 10/09/2018.
 * Display Who to follow accounts
 */

public class WhoToFollowActivity extends BaseActivity implements OnRetrieveWhoToFollowInterface, OnPostActionInterface, OnListActionInterface {



    private String item;
    private List<String> followedId;
    private List<String> accountListId;
    private List<String> toFollowdId;
    private TextView progess_action;
    private List<TrunkAccount> trunkAccounts;
    private RelativeLayout mainLoader;
    private String listId, listTitle;
    private RelativeLayout no_action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        switch (theme){
            case Helper.THEME_LIGHT:
                setTheme(R.style.AppTheme_NoActionBar);
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
        setContentView(R.layout.activity_who_to_follow);
        no_action = findViewById(R.id.no_action);
        Toolbar toolbar = findViewById(R.id.toolbar);
        progess_action = findViewById(R.id.progess_action);
        if( theme == THEME_BLACK)
            toolbar.setBackgroundColor(ContextCompat.getColor(WhoToFollowActivity.this, R.color.black));
        setSupportActionBar(toolbar);
        if( getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mainLoader = findViewById(R.id.loader);
        Bundle b = getIntent().getExtras();
        if(b != null){
            item = b.getString("item");
        }
        followedId = new ArrayList<>();
        String lastDateListNameRefresh = sharedpreferences.getString(Helper.LAST_DATE_LIST_NAME_REFRESH+item, null);
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date( ));
        cal.add(Calendar.MINUTE, -5);
        Date dateAllowed = cal.getTime();
        if( lastDateListNameRefresh == null || Helper.stringToDate(WhoToFollowActivity.this, lastDateListNameRefresh).before(dateAllowed)) {
            new WhoToFollowAsyncTask(WhoToFollowActivity.this, item, WhoToFollowActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            mainLoader.setVisibility(View.VISIBLE);
        }else {
            String lastList = sharedpreferences.getString(Helper.LAST_LIST_NAME + item, null);
            List<String> acctString = Helper.restoreArrayFromString(lastList);
            if( acctString != null) {
                trunkAccounts = new ArrayList<>();
                for (String acct : acctString) {
                    TrunkAccount trunkAccount = new TrunkAccount();
                    trunkAccount.setAcct(acct);
                    trunkAccounts.add(trunkAccount);
                }
            }
            displayResults();
        }
        setTitle(item);
    }


    private void displayResults(){
        mainLoader.setVisibility(View.GONE);
        WhoToFollowAccountsAdapter whoToFollowAccountsAdapter;
        if( trunkAccounts != null){
            if( trunkAccounts.size() > 0) {
                ListView lv_list = findViewById(R.id.lv_list);
                whoToFollowAccountsAdapter = new WhoToFollowAccountsAdapter(WhoToFollowActivity.this, trunkAccounts);
                lv_list.setAdapter(whoToFollowAccountsAdapter);
            }else {
                no_action.setVisibility(View.VISIBLE);
                return;
            }
        }else{
            Toast.makeText(WhoToFollowActivity.this, R.string.toast_error, Toast.LENGTH_SHORT).show();
            return;
        }

        Button follow_accounts_select = findViewById(R.id.follow_accounts_select);
        follow_accounts_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(follow_accounts_select.getText().equals(getString(R.string.select_all))){
                    follow_accounts_select.setText(R.string.unselect_all);
                    for(TrunkAccount trunkAccount: trunkAccounts){
                        trunkAccount.setChecked(true);
                    }
                    whoToFollowAccountsAdapter.notifyDataSetChanged();
                }else {
                    follow_accounts_select.setText(R.string.select_all);
                    for(TrunkAccount trunkAccount: trunkAccounts){
                        trunkAccount.setChecked(false);
                    }
                    whoToFollowAccountsAdapter.notifyDataSetChanged();
                }
            }
        });
        Button follow_accounts = findViewById(R.id.follow_accounts);
        follow_accounts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                follow_accounts.setEnabled(false);
                follow_accounts_select.setEnabled(false);
                progess_action.setVisibility(View.VISIBLE);
                toFollowdId = new ArrayList<>();
                for(TrunkAccount trunkAccount: trunkAccounts){
                    if( trunkAccount.isChecked()){
                        toFollowdId.add(trunkAccount.getAcct());
                    }
                }
                if(toFollowdId.size() > 0){
                    Account account = new Account();
                    String[] val = toFollowdId.get(0).split("@");
                    progess_action.setText(getString(R.string.follow_trunk, toFollowdId.get(0)));
                    if( val.length > 1){
                        account.setAcct(val[0]);
                        account.setInstance(val[1]);
                        new PostActionAsyncTask(WhoToFollowActivity.this, null, account, API.StatusAction.FOLLOW, WhoToFollowActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }else {
                        Toast.makeText(WhoToFollowActivity.this,R.string.toast_impossible_to_follow, Toast.LENGTH_LONG).show();
                        follow_accounts.setEnabled(true);
                        follow_accounts_select.setEnabled(true);
                        progess_action.setVisibility(View.GONE);
                    }
                }
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
    public void onRetrieveWhoToFollowList(List<String> list) {

    }

    @Override
    public void onRetrieveWhoToFollowAccount(List<TrunkAccount> trunkAccounts) {
        if( trunkAccounts != null){
            SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(Helper.LAST_DATE_LIST_NAME_REFRESH + item, Helper.dateToString(new Date()));
            List<String> accounts = new ArrayList<>();
            for(TrunkAccount trunkAccount: trunkAccounts)
                accounts.add(trunkAccount.getAcct());
            editor.putString(Helper.LAST_LIST_NAME + item, Helper.arrayToStringStorage(accounts));
            editor.apply();
            this.trunkAccounts = trunkAccounts;
        }
        displayResults();
    }

    @Override
    public void onPostAction(int statusCode, API.StatusAction statusAction, String userId, Error error) {
        followedId.add(userId);
        if( followedId != null && followedId.size() >= toFollowdId.size()) {
            progess_action.setText(getString(R.string.create_list_trunk, item));
            new ManageListsAsyncTask(WhoToFollowActivity.this, ManageListsAsyncTask.action.CREATE_LIST, null, null, null, item, WhoToFollowActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }else {
            Account account = new Account();
            String[] val = toFollowdId.get(followedId.size()).split("@");
            progess_action.setText(getString(R.string.follow_trunk, toFollowdId.get(followedId.size())));
            if( val.length > 1){
                account.setAcct(val[0]);
                account.setInstance(val[1]);
                new PostActionAsyncTask(WhoToFollowActivity.this, null, account, API.StatusAction.FOLLOW, WhoToFollowActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }

        }
    }

    @Override
    public void onActionDone(ManageListsAsyncTask.action actionType, APIResponse apiResponse, int statusCode) {

        List<fr.gouv.etalab.mastodon.client.Entities.List> lists = apiResponse.getLists();
        if( lists!= null && lists.size() > 0 && actionType == ManageListsAsyncTask.action.CREATE_LIST){
            String[] accountsId = followedId.toArray(new String[0]);
            progess_action.setText(R.string.add_account_list_trunk);
            listId = lists.get(0).getId();
            listTitle = lists.get(0).getTitle();
            new ManageListsAsyncTask(WhoToFollowActivity.this, ManageListsAsyncTask.action.ADD_USERS, new String[]{followedId.get(0)}, null, lists.get(0).getId(), null, WhoToFollowActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            accountListId = new ArrayList<>();
        }else if(accountListId != null){

            if( accountListId.size() >= followedId.size() -1) {
                progess_action.setText(R.string.account_added_list_trunk);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(WhoToFollowActivity.this, ListActivity.class);
                        Bundle b = new Bundle();
                        b.putString("id", listId);
                        b.putString("title", listTitle);
                        intent.putExtras(b);
                        startActivity(intent);
                       finish();
                    }

                }, 1000);

            }else {
                accountListId.add(followedId.get(accountListId.size()));
                progess_action.setText(R.string.adding_account_list_trunk);
                String userIdToAdd = followedId.get(accountListId.size());
                new ManageListsAsyncTask(WhoToFollowActivity.this, ManageListsAsyncTask.action.ADD_USERS, new String[]{userIdToAdd}, null, listId, null, WhoToFollowActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }

    }
}
