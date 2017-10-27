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
package fr.gouv.etalab.mastodon.asynctasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnUpdateAccountInfoInterface;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;

/**
 * Created by Thomas on 17/05/2017.
 * Manage the synchronization with the authenticated account and update the db not
 */

public class UpdateAccountInfoByIDAsyncTask extends AsyncTask<Void, Void, Void> {


    private OnUpdateAccountInfoInterface listener;
    private WeakReference<Context> contextReference;

    public UpdateAccountInfoByIDAsyncTask(Context context, OnUpdateAccountInfoInterface onUpdateAccountInfoInterface){
        this.contextReference = new WeakReference<>(context);
        this.listener = onUpdateAccountInfoInterface;
    }

    @Override
    protected Void doInBackground(Void... params) {

        SharedPreferences sharedpreferences = this.contextReference.get().getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        Account account = new API(this.contextReference.get()).getAccount(userId);
        SQLiteDatabase db = Sqlite.getInstance(this.contextReference.get(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        boolean userExists = new AccountDAO(this.contextReference.get(), db).userExist(account);
        if( userExists) {
            Account accountDb = new AccountDAO(this.contextReference.get(), db).getAccountByID(userId);
            if( accountDb != null){
                account.setInstance(accountDb.getInstance());
                account.setToken(accountDb.getToken());
                new AccountDAO(this.contextReference.get(), db).updateAccount(account);
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onUpdateAccountInfo(false);
    }

}
