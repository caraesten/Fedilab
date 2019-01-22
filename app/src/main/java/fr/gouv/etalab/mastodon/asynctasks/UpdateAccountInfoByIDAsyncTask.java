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
import java.util.HashMap;

import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Emojis;
import fr.gouv.etalab.mastodon.client.HttpsConnection;
import fr.gouv.etalab.mastodon.client.PeertubeAPI;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnUpdateAccountInfoInterface;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.CustomEmojiDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;

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
        if( social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON)
            account = new API(this.contextReference.get()).getAccount(userId);
        else if( social == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE) {

            try {
                account = new PeertubeAPI(this.contextReference.get()).verifyCredentials();
                account.setSocial("PEERTUBE");
            }catch (HttpsConnection.HttpsConnectionException exception){
                if(exception.getStatusCode() == 401){
                    SQLiteDatabase db = Sqlite.getInstance(this.contextReference.get(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                    String token = sharedpreferences.getString(Helper.PREF_KEY_OAUTH_TOKEN, null);
                    account = new AccountDAO(this.contextReference.get(), db).getAccountByToken(token);
                    HashMap<String, String> values = new PeertubeAPI(this.contextReference.get()).refreshToken(account.getClient_id(), account.getClient_secret(), account.getRefresh_token());
                    if( values != null) {
                        String newtoken = values.get("access_token");
                        String refresh_token = values.get("refresh_token");
                        if (newtoken != null)
                            account.setToken(newtoken);
                        if (refresh_token != null)
                            account.setRefresh_token(refresh_token);
                        new AccountDAO(this.contextReference.get(), db).updateAccount(account);
                    }
                }
            }

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
                new AccountDAO(this.contextReference.get(), db).updateAccount(account);
            }
        }
        if( social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON) {
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
