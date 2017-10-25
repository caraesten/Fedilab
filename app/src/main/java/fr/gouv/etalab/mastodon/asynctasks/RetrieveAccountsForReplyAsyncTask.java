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

import java.util.ArrayList;

import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Mention;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveAccountsReplyInterface;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;


/**
 * Created by Thomas on 25/10/2017.
 * Retrieves accounts which are involved in a conversation
 */

public class RetrieveAccountsForReplyAsyncTask extends AsyncTask<Void, Void, Void> {

    private Context context;
    private fr.gouv.etalab.mastodon.client.Entities.Status status;
    private OnRetrieveAccountsReplyInterface listener;
    private ArrayList<Mention> mentions;
    private ArrayList<String> addedAccounts;

    public RetrieveAccountsForReplyAsyncTask(Context context, fr.gouv.etalab.mastodon.client.Entities.Status status, OnRetrieveAccountsReplyInterface onRetrieveAccountsReplyInterface){
        this.context = context;
        this.status = status;
        this.listener = onRetrieveAccountsReplyInterface;
    }

    @Override
    protected Void doInBackground(Void... params) {
        API api = new API(context);
        fr.gouv.etalab.mastodon.client.Entities.Context statusContext = api.getStatusContext(status.getId());
        mentions = new ArrayList<>();
        addedAccounts = new ArrayList<>();
        //Retrieves the first toot
        if( statusContext.getAncestors().size() > 0 ) {
            statusContext = api.getStatusContext(statusContext.getAncestors().get(0).getId());
        }
        if( status != null){
            for(Mention mention : status.getMentions()){
                if( canBeAdded(mention.getAcct())){
                    mentions.add(mention);
                    addedAccounts.add(mention.getAcct());
                }
            }
        }
        if( statusContext != null && statusContext.getDescendants().size() >  0){
            for(fr.gouv.etalab.mastodon.client.Entities.Status status : statusContext.getDescendants())
            for(Mention mention : status.getMentions()){
                if( canBeAdded(mention.getAcct())){
                    mentions.add(mention);
                    addedAccounts.add(mention.getAcct());
                }
            }
        }
        if( statusContext != null && statusContext.getAncestors().size() >  0){
            for(fr.gouv.etalab.mastodon.client.Entities.Status status : statusContext.getAncestors())
            for(Mention mention : status.getMentions()){
                if( canBeAdded(mention.getAcct())){
                    mentions.add(mention);
                    addedAccounts.add(mention.getAcct());
                }
            }
        }
        return null;
    }

    private boolean canBeAdded(String acct){
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        Account currentAccount = new AccountDAO(context, db).getAccountByID(userId);
        return acct != null && !acct.equals(currentAccount.getAcct()) && !addedAccounts.contains(acct);
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveAccountsReply(mentions);
    }

}
