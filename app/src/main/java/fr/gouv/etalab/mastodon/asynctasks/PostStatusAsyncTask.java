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
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.regex.Matcher;

import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnPostStatusActionInterface;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import fr.gouv.etalab.mastodon.sqlite.TagsCacheDAO;


/**
 * Created by Thomas on 21/07/2017.
 * Posts status (live version) - scheduled toots are sent via classic post feature in api
 */

public class PostStatusAsyncTask extends AsyncTask<Void, Void, Void> {

    private OnPostStatusActionInterface listener;
    private APIResponse apiResponse;
    private fr.gouv.etalab.mastodon.client.Entities.Status status;
    private Account account;
    private WeakReference<Context> contextReference;


    public PostStatusAsyncTask(Context context, Account account, fr.gouv.etalab.mastodon.client.Entities.Status status, OnPostStatusActionInterface onPostStatusActionInterface){
        this.contextReference = new WeakReference<>(context);
        this.listener = onPostStatusActionInterface;
        this.status = status;
        this.account = account;
    }

    @Override
    protected Void doInBackground(Void... params) {
        if( account == null)
            apiResponse = new API(this.contextReference.get()).postStatusAction(status);
        else
            apiResponse = new API(this.contextReference.get(), account.getInstance(), account.getToken()).postStatusAction(status);

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onPostStatusAction(apiResponse);
        //Search for tag with upper cases to store them locally
        Log.v(Helper.TAG,"ici");
        Thread thread = new Thread() {
            @Override
            public void run() {
                String content = status.getContent();
                if( content != null && content.length() > 0){
                    SQLiteDatabase db = Sqlite.getInstance(contextReference.get(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                    Matcher matcher = Helper.hashtagPattern.matcher(content);
                    while (matcher.find()){
                        int matchStart = matcher.start(1);
                        int matchEnd = matcher.end();
                        //Get cached tags
                        List<String> cachedTag = new TagsCacheDAO(contextReference.get(), db).getAll();
                        String tag = content.substring(matchStart, matchEnd);
                        tag = tag.replace("#","");
                        if( cachedTag == null){
                            new TagsCacheDAO(contextReference.get(), db).insert(tag);
                        }else {
                            //If cache doesn't contain the tag and the tag has upper case
                            if(!cachedTag.contains(tag) && !tag.toLowerCase().equals(tag)){
                                new TagsCacheDAO(contextReference.get(), db).insert(tag);
                            }
                        }
                    }
                }
            }
        };
        thread.start();
    }

}
