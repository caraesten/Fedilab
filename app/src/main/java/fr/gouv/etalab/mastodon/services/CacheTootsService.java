package fr.gouv.etalab.mastodon.services;
/* Copyright 2018 Thomas Schneider
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

import android.app.IntentService;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import fr.gouv.etalab.mastodon.sqlite.StatusCacheDAO;


/**
 * Created by Thomas on 17/11/2018.
 * Manage service for caching status
 */

public class CacheTootsService extends IntentService {

    static {
        Helper.installProvider();
    }
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    @SuppressWarnings("unused")
    public CacheTootsService(String name) {
        super(name);
    }
    @SuppressWarnings("unused")
    public CacheTootsService() {
        super("CacheTootsService");
    }

    private static HttpsURLConnection httpsURLConnection;
    protected Account account;

    public void onCreate() {
        super.onCreate();
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if( intent == null || intent.getExtras() == null)
            return;
        String response = intent.getExtras().getString("response");
        String payload = intent.getExtras().getString("payload");

        if( payload == null && response == null)
            return;
        try {
            List<Status> statuses;
            if( response != null)
                statuses = API.parseStatuses(getApplicationContext(), new JSONArray(response));
            else {
                statuses = new ArrayList<>();
                Status status = API.parseStatuses(getApplicationContext(), new JSONObject(payload));
                statuses.add(status);
            }
            SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();

            List<Status> alreadyCached = new StatusCacheDAO(getApplicationContext(), db).getAllStatus(StatusCacheDAO.STATUS_CACHE);
            ArrayList<String> cachedId = new ArrayList<>();
            if(alreadyCached != null){
                for(Status status: alreadyCached){
                    cachedId.add(status.getId());
                }
            }
            for(Status status: statuses){
                if(!cachedId.contains(status.getId())){
                    new StatusCacheDAO(getApplicationContext(), db).insertStatus(StatusCacheDAO.STATUS_CACHE, status);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
