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
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.regex.Matcher;

import app.fedilab.android.client.API;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Error;
import app.fedilab.android.client.GNUAPI;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.sqlite.Sqlite;
import app.fedilab.android.sqlite.TagsCacheDAO;
import app.fedilab.android.R;
import app.fedilab.android.interfaces.OnPostStatusActionInterface;


/**
 * Created by Thomas on 21/07/2017.
 * Posts status (live version) - scheduled toots are sent via classic post feature in api
 */

public class PostStatusAsyncTask extends AsyncTask<Void, Void, Void> {

    private OnPostStatusActionInterface listener;
    private APIResponse apiResponse;
    private app.fedilab.android.client.Entities.Status status;
    private Account account;
    private WeakReference<Context> contextReference;
    private UpdateAccountInfoAsyncTask.SOCIAL social;

    public PostStatusAsyncTask(Context context, UpdateAccountInfoAsyncTask.SOCIAL social, Account account, app.fedilab.android.client.Entities.Status status, OnPostStatusActionInterface onPostStatusActionInterface){
        this.contextReference = new WeakReference<>(context);
        this.listener = onPostStatusActionInterface;
        this.status = status;
        this.account = account;
        this.social = social;
    }

    @Override
    protected Void doInBackground(Void... params) {

        if(social != UpdateAccountInfoAsyncTask.SOCIAL.GNU && social != UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA){
            boolean isconnected = Helper.isConnectedToInternet(contextReference.get(), Helper.getLiveInstance(contextReference.get()));
            if( isconnected) {
                if (account == null) {
                    apiResponse = new API(this.contextReference.get()).postStatusAction(status);
                } else
                    apiResponse = new API(this.contextReference.get(), account.getInstance(), account.getToken()).postStatusAction(status);
            }else {
                apiResponse = new APIResponse();
                Error error = new Error();
                error.setError(contextReference.get().getString(R.string.no_internet));
                error.setStatusCode(-33);
                apiResponse.setError(error);
            }
        }else {
            boolean isconnected = Helper.isConnectedToInternet(contextReference.get(), Helper.getLiveInstance(contextReference.get()));
            if( isconnected) {
                if (account == null) {
                    apiResponse = new GNUAPI(this.contextReference.get()).postStatusAction(status);
                } else
                    apiResponse = new GNUAPI(this.contextReference.get(), account.getInstance(), account.getToken()).postStatusAction(status);
            }else {
                apiResponse = new APIResponse();
                Error error = new Error();
                error.setError(contextReference.get().getString(R.string.no_internet));
                error.setStatusCode(-33);
                apiResponse.setError(error);
            }
        }



        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onPostStatusAction(apiResponse);
        //Search for tag with upper cases to store them locally
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
