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

import app.fedilab.android.client.API;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.InstanceNodeInfo;
import app.fedilab.android.client.Entities.Results;
import app.fedilab.android.client.GNUAPI;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.sqlite.Sqlite;
import app.fedilab.android.sqlite.TagsCacheDAO;
import app.fedilab.android.activities.MainActivity;
import app.fedilab.android.interfaces.OnRetrieveSearchInterface;


/**
 * Created by Thomas on 25/05/2017.
 * Retrieves accounts and toots from search
 */

public class RetrieveSearchAsyncTask extends AsyncTask<Void, Void, Void> {

    private String query;
    private APIResponse apiResponse;
    private OnRetrieveSearchInterface listener;
    private WeakReference<Context> contextReference;
    private boolean tagsOnly = false;
    private API.searchType type;
    private String max_id;

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

    public RetrieveSearchAsyncTask(Context context, String query, API.searchType searchType, String max_id, OnRetrieveSearchInterface onRetrieveSearchInterface){
        this.contextReference = new WeakReference<>(context);
        this.query = query;
        this.listener = onRetrieveSearchInterface;
        this.type = searchType;
        this.max_id = max_id;
    }

    @Override
    protected Void doInBackground(Void... params) {

        if (this.type == null) {
            if (MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA) {
                API api = new API(this.contextReference.get());
                String[] split = query.trim().split("@");
                if( split.length > 1 ){

                   String domain;
                   String username;
                   if( split.length == 2){
                       username = split[0];
                       domain = split[1];
                   }else{
                       username = split[1];
                       domain = split[2];
                   }
                   if( domain != null && username != null){
                       InstanceNodeInfo node = api.getNodeInfo(domain);
                       String url = null;
                       if( node != null && node.getName() != null){
                           switch (node.getName().trim()){
                               case "MASTODON":
                                   url = "https://" + domain + "/@" + username;
                                   break;
                               case "PEERTUBE":
                                   url = "https://" + domain + "/accounts/" + username;
                                   break;
                               case "PIXELFED":
                                   url = "https://" + domain + "/" + username;
                                   break;
                               case "GNU":
                                   url = "https://" + domain + "/profile/" + username;
                                   break;
                           }
                       }
                       if( url != null)
                           query = url;
                   }

                }

                if (!tagsOnly)
                    apiResponse = api.search(query);
                else {
                    //search tags only
                    apiResponse = api.search(query);
                    SQLiteDatabase db = Sqlite.getInstance(contextReference.get(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                    List<String> cachedTags = new TagsCacheDAO(contextReference.get(), db).getBy(query);
                    if (apiResponse != null && apiResponse.getResults() != null && apiResponse.getResults().getHashtags() != null) {
                        //If cache contains matching tags
                        if (cachedTags != null) {
                            for (String apiTag : apiResponse.getResults().getHashtags()) {
                                //Cache doesn't contain the tags coming from the api (case insensitive)
                                if (!Helper.containsCaseInsensitive(apiTag, cachedTags)) {
                                    cachedTags.add(apiTag); //It's added
                                }
                            }
                            apiResponse.getResults().setHashtags(cachedTags);
                        }
                    } else if (cachedTags != null) {
                        if (apiResponse != null && apiResponse.getResults() == null) {
                            apiResponse.setResults(new Results());
                            apiResponse.getResults().setHashtags(cachedTags);
                        }
                    }
                }
            } else {
                GNUAPI gnuapi = new GNUAPI(this.contextReference.get());
                apiResponse = gnuapi.search(query);
            }
        }else{
            API api = new API(this.contextReference.get());
            apiResponse = api.search2(query, type, max_id);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveSearch(apiResponse);
    }

}
