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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;

import app.fedilab.android.client.API;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.GNUAPI;
import app.fedilab.android.client.PeertubeAPI;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.sqlite.AccountDAO;
import app.fedilab.android.sqlite.Sqlite;
import app.fedilab.android.activities.MainActivity;

/**
 * Created by Thomas on 23/04/2017.
 * Manage the synchronization with the account and update the db
 */

public class UpdateAccountInfoAsyncTask extends AsyncTask<Void, Void, Void> {

    private String token, client_id, client_secret, refresh_token;
    private String instance;
    private WeakReference<Context> contextReference;
    private SOCIAL social;

    public enum SOCIAL{
        MASTODON,
        PEERTUBE,
        PIXELFED,
        PLEROMA,
        GNU,
        FRIENDICA
    }
    public UpdateAccountInfoAsyncTask(Context context, String token, String client_id, String client_secret, String refresh_token, String instance, SOCIAL social){
        this.contextReference = new WeakReference<>(context);
        this.token = token;
        this.instance = instance;
        this.social = social;
        this.client_id = client_id;
        this.client_secret = client_secret;
        this.refresh_token = refresh_token;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Account account;
        if( social == SOCIAL.MASTODON || social == SOCIAL.PIXELFED) {
            account = new API(this.contextReference.get(), instance, null).verifyCredentials();
            if( account != null)
                account.setSocial(account.getSocial());
        }else if( social == SOCIAL.PEERTUBE) {
            account = new PeertubeAPI(this.contextReference.get(), instance, null).verifyCredentials();
            if( account != null)
                account.setSocial("PEERTUBE");
        }else{
            account = new GNUAPI(this.contextReference.get(), instance, null).verifyCredentials();
            if( account != null)
                account.setSocial(account.getSocial());
        }
        if (account == null)
            return null;
        try {
            //At the state the instance can be encoded
            instance = URLDecoder.decode(instance, "utf-8");
        } catch (UnsupportedEncodingException ignored) {}

        SharedPreferences sharedpreferences = this.contextReference.get().getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        account.setToken(token);
        account.setClient_id(client_id);
        account.setClient_secret(client_secret);
        account.setRefresh_token(refresh_token);
        account.setInstance(instance);
        SQLiteDatabase db = Sqlite.getInstance(this.contextReference.get(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        boolean userExists = new AccountDAO(this.contextReference.get(), db).userExist(account);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(Helper.PREF_KEY_ID, account.getId());
        editor.putBoolean(Helper.PREF_IS_MODERATOR, account.isModerator());
        editor.putBoolean(Helper.PREF_IS_ADMINISTRATOR, account.isAdmin());
        editor.putString(Helper.PREF_INSTANCE, instance);
        editor.apply();
        if( userExists)
            new AccountDAO(this.contextReference.get(), db).updateAccountCredential(account);
        else {
            if( account.getUsername() != null && account.getCreated_at() != null)
                new AccountDAO(this.contextReference.get(), db).insertAccount(account);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {

        Intent mainActivity = new Intent(this.contextReference.get(), MainActivity.class);
        mainActivity.putExtra(Helper.INTENT_ACTION, Helper.ADD_USER_INTENT);
        this.contextReference.get().startActivity(mainActivity);
        ((Activity) this.contextReference.get()).finish();

    }

}