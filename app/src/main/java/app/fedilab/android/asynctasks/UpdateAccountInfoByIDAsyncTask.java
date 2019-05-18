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
package app.fedilab.android.asynctasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

import app.fedilab.android.client.API;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Emojis;
import app.fedilab.android.client.GNUAPI;
import app.fedilab.android.client.PeertubeAPI;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.sqlite.AccountDAO;
import app.fedilab.android.sqlite.CustomEmojiDAO;
import app.fedilab.android.sqlite.Sqlite;
import app.fedilab.android.interfaces.OnUpdateAccountInfoInterface;

/**
 * Created by Thomas on 17/05/2017.
 * Manage the synchronization with the authenticated account and update the db not
 */

public class UpdateAccountInfoByIDAsyncTask extends AsyncTask<Void, Void, Void> {


    private OnUpdateAccountInfoInterface listener;
    private WeakReference<Context> contextReference;
    private UpdateAccountInfoAsyncTask.SOCIAL social;

    public UpdateAccountInfoByIDAsyncTask(Context context, UpdateAccountInfoAsyncTask.SOCIAL social, OnUpdateAccountInfoInterface onUpdateAccountInfoInterface){
        this.contextReference = new WeakReference<>(context);
        this.listener = onUpdateAccountInfoInterface;
        this.social = social;
    }

    @Override
    protected Void doInBackground(Void... params) {

        SharedPreferences sharedpreferences = this.contextReference.get().getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        Account account = null;
        if( social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON || social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA)
            account = new API(this.contextReference.get()).verifyCredentials();
        else if( social == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE) {
            account = new PeertubeAPI(this.contextReference.get()).verifyCredentials();
            account.setSocial("PEERTUBE");
        }else if ( social == UpdateAccountInfoAsyncTask.SOCIAL.GNU || social == UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA){
            account = new GNUAPI(this.contextReference.get()).verifyCredentials();
        }
        if( account == null)
            return null;
        account.setInstance(Helper.getLiveInstance(contextReference.get()));
        SQLiteDatabase db = Sqlite.getInstance(this.contextReference.get(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        boolean userExists = new AccountDAO(this.contextReference.get(), db).userExist(account);
        if( userExists) {
            Account accountDb = new AccountDAO(this.contextReference.get(), db).getAccountByID(userId);

            if( accountDb != null){
                account.setInstance(accountDb.getInstance());
                account.setToken(accountDb.getToken());
                new AccountDAO(this.contextReference.get(), db).updateAccountCredential(account);
            }
        }
        if( social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON || social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA) {
            try {
                APIResponse response = new API(contextReference.get()).getCustomEmoji();
                if (response != null && response.getEmojis() != null && response.getEmojis().size() > 0) {
                    new CustomEmojiDAO(contextReference.get(), db).removeAll();
                    for (Emojis emojis : response.getEmojis()) {
                        new CustomEmojiDAO(contextReference.get(), db).insertEmoji(emojis);
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onUpdateAccountInfo(false);
    }

}
