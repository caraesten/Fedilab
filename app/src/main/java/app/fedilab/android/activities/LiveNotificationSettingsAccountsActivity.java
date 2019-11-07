/* Copyright 2019 Thomas Schneider
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


import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import app.fedilab.android.R;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.drawers.AccountLiveAdapter;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.sqlite.AccountDAO;
import app.fedilab.android.sqlite.Sqlite;


/**
 * Created by Thomas on 03/10/2019.
 * Manage live notifications for accounts
 */

public class LiveNotificationSettingsAccountsActivity extends BaseActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if (theme == Helper.THEME_LIGHT) {
            setTheme(R.style.Cyanea_AlertDialog_Theme_Light);
        } else {
            setTheme(R.style.Cyanea_AlertDialog_Theme_Dark);
        }
        setContentView(R.layout.activity_livenotifications_all_accounts);
        getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        RecyclerView list_of_accounts = findViewById(R.id.list_of_accounts);

        ArrayList<Account> accounts = new ArrayList<>();
        SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        List<Account> accountStreams = new AccountDAO(getApplicationContext(), db).getAllAccountCrossAction();
        for (Account account : accountStreams) {
            if (account.getSocial() == null || account.getSocial().equals("MASTODON") || account.getSocial().equals("PLEROMA") || account.getSocial().equals("PIXELFED")) {
                accounts.add(account);
            }
        }
        AccountLiveAdapter accountLiveAdapter = new AccountLiveAdapter(accounts);
        list_of_accounts.setAdapter(accountLiveAdapter);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        list_of_accounts.setLayoutManager(mLayoutManager);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
