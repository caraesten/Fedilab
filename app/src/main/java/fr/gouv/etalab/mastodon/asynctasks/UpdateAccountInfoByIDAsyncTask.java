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
 * You should have received a copy of the GNU General Public License along with Thomas Schneider; if not,
 * see <http://www.gnu.org/licenses>. */
package fr.gouv.etalab.mastodon.asynctasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
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

    private Context context;
    private OnUpdateAccountInfoInterface listener;

    public UpdateAccountInfoByIDAsyncTask(Context context, OnUpdateAccountInfoInterface onUpdateAccountInfoInterface){
        this.context = context;
        this.listener = onUpdateAccountInfoInterface;
    }

    @Override
    protected Void doInBackground(Void... params) {

        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        Account account = new API(context).getAccount(userId);
        SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        boolean userExists = new AccountDAO(context, db).userExist(account);
        if( userExists) {
            Account accountDb = new AccountDAO(context, db).getAccountByID(userId);
            if( accountDb != null){
                account.setInstance(accountDb.getInstance());
                account.setToken(accountDb.getToken());
                new AccountDAO(context, db).updateAccount(account);
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onUpdateAccountInfo(false);
    }

}
