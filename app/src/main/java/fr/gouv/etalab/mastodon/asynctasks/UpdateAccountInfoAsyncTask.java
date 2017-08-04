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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import fr.gouv.etalab.mastodon.activities.MainActivity;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;

/**
 * Created by Thomas on 23/04/2017.
 * Manage the synchronization with the account and update the db
 */

public class UpdateAccountInfoAsyncTask extends AsyncTask<Void, Void, Void> {

    private Context context;
    private String token;
    private String instance;

    public UpdateAccountInfoAsyncTask(Context context, String token, String instance){
        this.context = context;
        this.token = token;
        this.instance = instance;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Account account = new API(context, instance, null).verifyCredentials();
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        if( token == null) {
            token = sharedpreferences.getString(Helper.PREF_KEY_OAUTH_TOKEN, null);
        }
        account.setToken(token);
        //TODO: remove this static value to allow other instances
        account.setInstance(instance);
        SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        boolean userExists = new AccountDAO(context, db).userExist(account);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(Helper.PREF_KEY_ID, account.getId());
        editor.apply();
        if( userExists)
            new AccountDAO(context, db).updateAccount(account);
        else {
            if( account.getUsername() != null && account.getCreated_at() != null)
                new AccountDAO(context, db).insertAccount(account);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {

        Intent mainActivity = new Intent(context, MainActivity.class);
        mainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(mainActivity);
        ((Activity) context).finish();

    }

}