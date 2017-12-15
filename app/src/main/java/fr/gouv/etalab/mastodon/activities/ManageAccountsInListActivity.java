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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.asynctasks.ManageListsAsyncTask;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.drawers.AccountsInAListAdapter;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnListActionInterface;



/**
 * Created by Thomas on 15/12/2017.
 * Manage accounts in Lists
 */

public class ManageAccountsInListActivity extends BaseActivity implements OnListActionInterface {

    private TextView list_title;
    private EditText search_account;
    private LinearLayout container, main_account_container;
    private RelativeLayout loader, no_action;
    private RecyclerView lv_accounts_current, lv_accounts_search;
    private String title, listId;
    private java.util.List<Account> accounts;
    private AccountsInAListAdapter accountsInAListAdapter, accountsSearchInAListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setTheme(R.style.AppThemeDark_NoActionBar);
        setContentView(R.layout.activity_manage_accounts_list);
        getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if( getSupportActionBar() != null)
            getSupportActionBar().hide();

        Bundle b = getIntent().getExtras();
        if(b != null){
            title = b.getString("title");
            listId = b.getString("id");
        }else{
            Toast.makeText(this,R.string.toast_error,Toast.LENGTH_LONG).show();
        }

        container = findViewById(R.id.container);
        main_account_container = findViewById(R.id.main_account_container);
        loader = findViewById(R.id.loader);
        list_title = findViewById(R.id.list_title);
        search_account = findViewById(R.id.search_account);
        lv_accounts_search = findViewById(R.id.lv_accounts_search);
        lv_accounts_current = findViewById(R.id.lv_accounts_current);
        no_action = findViewById(R.id.no_action);
        this.accounts = new ArrayList<>();
        accountsInAListAdapter = new AccountsInAListAdapter(ManageAccountsInListActivity.this, AccountsInAListAdapter.type.CURRENT, listId, this.accounts);
        lv_accounts_current.setAdapter(accountsInAListAdapter);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(ManageAccountsInListActivity.this);
        lv_accounts_current.setLayoutManager(mLayoutManager);

        accountsSearchInAListAdapter = new AccountsInAListAdapter(ManageAccountsInListActivity.this, AccountsInAListAdapter.type.SEARCH, listId, this.accounts);
        lv_accounts_search.setAdapter(accountsSearchInAListAdapter);
        LinearLayoutManager mLayoutManager1 = new LinearLayoutManager(ManageAccountsInListActivity.this);
        lv_accounts_search.setLayoutManager(mLayoutManager1);


        list_title.setText(title);
        loader.setVisibility(View.VISIBLE);
        new ManageListsAsyncTask(ManageAccountsInListActivity.this, ManageListsAsyncTask.action.GET_LIST_ACCOUNT, null, null, listId, null, ManageAccountsInListActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

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
    public void onActionDone(ManageListsAsyncTask.action actionType, APIResponse apiResponse, int statusCode) {
        loader.setVisibility(View.GONE);
        if( apiResponse.getError() != null){
            final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            boolean show_error_messages = sharedpreferences.getBoolean(Helper.SET_SHOW_ERROR_MESSAGES, true);
            if( show_error_messages)
                Toast.makeText(ManageAccountsInListActivity.this, apiResponse.getError().getError(),Toast.LENGTH_LONG).show();
            return;
        }

        if( actionType == ManageListsAsyncTask.action.GET_LIST_ACCOUNT){
            if (apiResponse.getAccounts() != null && apiResponse.getAccounts().size() > 0) {
                this.accounts.addAll(apiResponse.getAccounts());
                accountsInAListAdapter.notifyDataSetChanged();
                main_account_container.setVisibility(View.VISIBLE);
            } else {
                no_action.setVisibility(View.VISIBLE);
            }
        }
    }
}
