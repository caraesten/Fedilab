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

import java.lang.ref.WeakReference;
import java.util.List;

import fr.gouv.etalab.mastodon.activities.MainActivity;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.Entities.Error;
import fr.gouv.etalab.mastodon.client.Entities.Results;
import fr.gouv.etalab.mastodon.client.GNUAPI;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveSearchInterface;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import fr.gouv.etalab.mastodon.sqlite.TagsCacheDAO;


/**
 * Created by Thomas on 25/05/2017.
 * Retrieves accounts and toots from search
 */

public class RetrieveSearchAsyncTask extends AsyncTask<Void, Void, Void> {

    private String query;
    private Results results;
    private OnRetrieveSearchInterface listener;
    private Error error;
    private WeakReference<Context> contextReference;
    private boolean tagsOnly = false;

    public RetrieveSearchAsyncTask(Context context, String query, OnRetrieveSearchInterface onRetrieveSearchInterface){
        this.contextReference = new WeakReference<>(context);
        this.query = query;
        this.listener = onRetrieveSearchInterface;
    }

    public RetrieveSearchAsyncTask(Context context, String query, boolean tagsOnly, OnRetrieveSearchInterface onRetrieveSearchInterface){
        this.contextReference = new WeakReference<>(context);
        this.query = query;
        this.listener = onRetrieveSearchInterface;
        this.tagsOnly = tagsOnly;
    }

    @Override
    protected Void doInBackground(Void... params) {
        if(MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.GNU) {
            API api = new API(this.contextReference.get());
            if (!tagsOnly)
                results = api.search(query);
            else {
                //search tags only
                results = api.search(query);
                SQLiteDatabase db = Sqlite.getInstance(contextReference.get(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                List<String> cachedTags = new TagsCacheDAO(contextReference.get(), db).getBy(query);
                if (results != null && results.getHashtags() != null) {
                    //If cache contains matching tags
                    if (cachedTags != null) {
                        for (String apiTag : results.getHashtags()) {
                            //Cache doesn't contain the tags coming from the api (case insensitive)
                            if (!Helper.containsCaseInsensitive(apiTag, cachedTags)) {
                                cachedTags.add(apiTag); //It's added
                            }
                        }
                        results.setHashtags(cachedTags);
                    }
                } else if (cachedTags != null) {
                    if (results == null)
                        results = new Results();
                    results.setHashtags(cachedTags);
                }
            }
            error = api.getError();
        }else{
            GNUAPI gnuapi = new GNUAPI(this.contextReference.get());
            if (!tagsOnly)
                results = gnuapi.search(query);
            else {
                //search tags only
                results = gnuapi.search(query);
                SQLiteDatabase db = Sqlite.getInstance(contextReference.get(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                List<String> cachedTags = new TagsCacheDAO(contextReference.get(), db).getBy(query);
                if (results != null && results.getHashtags() != null) {
                    //If cache contains matching tags
                    if (cachedTags != null) {
                        for (String apiTag : results.getHashtags()) {
                            //Cache doesn't contain the tags coming from the api (case insensitive)
                            if (!Helper.containsCaseInsensitive(apiTag, cachedTags)) {
                                cachedTags.add(apiTag); //It's added
                            }
                        }
                        results.setHashtags(cachedTags);
                    }
                } else if (cachedTags != null) {
                    if (results == null)
                        results = new Results();
                    results.setHashtags(cachedTags);
                }
            }
            error = gnuapi.getError();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveSearch(results, error);
    }

}
