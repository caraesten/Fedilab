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
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;

/**
 * Created by Thomas on 28/05/2017.
 * Remove an account in db
 */

public class RemoveAccountAsyncTask extends AsyncTask<Void, Void, Void> {

    private WeakReference<Activity> activityReference;
    private Account account;

    public RemoveAccountAsyncTask(Activity activity, Account account){
        activityReference = new WeakReference<>(activity);
        this.account = account;
    }

    @Override
    protected Void doInBackground(Void... params) {
        SQLiteDatabase db = Sqlite.getInstance(activityReference.get(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        new AccountDAO(activityReference.get(), db).removeUser(account);
        return null;
    }


}