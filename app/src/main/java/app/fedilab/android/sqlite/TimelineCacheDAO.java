package app.fedilab.android.sqlite;
/* Copyright 2019 Thomas Schneider
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

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import app.fedilab.android.client.API;
import app.fedilab.android.client.Entities.Status;
import app.fedilab.android.helper.Helper;


/**
 * Created by Thomas on 12/05/2019.
 * Manage Timeline Cache
 */
public class TimelineCacheDAO {

    private SQLiteDatabase db;
    public Context context;

    public TimelineCacheDAO(Context context, SQLiteDatabase db) {
        //Creation of the DB with tables
        this.context = context;
        this.db = db;
    }


    //------- INSERTIONS  -------
    /**
     * Insert a status in database
     * @return boolean
     */
    public long insert(String statusId, String jsonString) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        ContentValues values = new ContentValues();
        values.put(Sqlite.COL_INSTANCE, instance);
        values.put(Sqlite.COL_STATUS_ID, statusId);
        values.put(Sqlite.COL_USER_ID, userId);
        values.put(Sqlite.COL_DATE, Helper.dateToString(new Date()));
        values.put(Sqlite.COL_CACHE, jsonString);
        //Inserts cached status
        long last_id;
        try{
            last_id = db.insert(Sqlite.TABLE_TIMELINE_CACHE, null, values);
        }catch (Exception e) {
            last_id =  -1;
            e.printStackTrace();
        }
        return last_id;
    }

    //------- UPDATE  -------
    /**
     * Update a status in database
     */
    public void update(String statusId, String jsonString) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        ContentValues values = new ContentValues();
        values.put(Sqlite.COL_DATE, Helper.dateToString(new Date()));
        values.put(Sqlite.COL_CACHE, jsonString);
        try{
            db.update(Sqlite.TABLE_TIMELINE_CACHE,  values, Sqlite.COL_INSTANCE + " =  ? AND " + Sqlite.COL_STATUS_ID + " = ? AND " + Sqlite.COL_USER_ID + " = ?", new String[]{instance, statusId, userId});
        }catch (Exception ignored) {}
    }

    //------- REMOVE  -------

    /***
     * Remove stored status
     * @return int
     */
    public int remove(String statusId) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        return db.delete(Sqlite.TABLE_TIMELINE_CACHE,  Sqlite.COL_STATUS_ID + " = \""+ statusId +"\" AND " + Sqlite.COL_INSTANCE + " = \"" + instance + "\" AND " + Sqlite.COL_USER_ID + " = \"" + userId + "\"", null);
    }


    /***
     * Remove stored status
     * @return int
     */
    public int removeAfterDate(String date) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        return db.delete(Sqlite.TABLE_TIMELINE_CACHE,  Sqlite.COL_DATE + " < \""+ date +"\" AND " + Sqlite.COL_INSTANCE + " = \"" + instance + "\" AND " + Sqlite.COL_USER_ID + " = \"" + userId + "\"", null);
    }

    /***
     * Remove stored status
     * @return int
     */
    public int removeAll(){
        return db.delete(Sqlite.TABLE_TIMELINE_CACHE,  null, null);
    }



    //------- GETTERS  -------

    /**
     * Returns all cached Statuses
     * @return stored Status List<Status>
     */
    public List<Status> get(String max_id){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        try {
            Cursor c;
            if( max_id != null)
                c = db.query(Sqlite.TABLE_TIMELINE_CACHE, null,   Sqlite.COL_INSTANCE + " = \"" + instance + "\" AND " + Sqlite.COL_USER_ID + " = \"" + userId + "\" AND "+ Sqlite.COL_STATUS_ID + " <= " + max_id, null, null, null, Sqlite.COL_STATUS_ID+ " DESC", "40");
            else
                c = db.query(Sqlite.TABLE_TIMELINE_CACHE, null,   Sqlite.COL_INSTANCE + " = \"" + instance + "\" AND " + Sqlite.COL_USER_ID + " = \"" + userId + "\"", null, null, null, Sqlite.COL_STATUS_ID+ " DESC", "40");
            return cursorToListStatus(c);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns one cached Statuses
     * @return stored Status List<Status>
     */
    public Status getSingle(String statusId){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        try {
            Cursor  c = db.query(Sqlite.TABLE_TIMELINE_CACHE, null,   Sqlite.COL_INSTANCE + " = \"" + instance + "\" AND " + Sqlite.COL_USER_ID + " = \"" + userId + "\" AND "+ Sqlite.COL_STATUS_ID + " ='" + statusId +"'", null, null, null, Sqlite.COL_STATUS_ID+ " DESC", "1");
            return cursorToSingleStatus(c);
        } catch (Exception e) {
            return null;
        }
    }

    /***
     * Method to hydrate one cached status from database
     * @param c Cursor
     * @return Status
     */
    private Status cursorToSingleStatus(Cursor c){
        //No element found
        if (c.getCount() == 0)
            return null;
        c.moveToFirst();
        Status status = null;
        try {
            status = API.parseStatuses(context, new JSONObject(c.getString(c.getColumnIndex(Sqlite.COL_CACHE))));
            status.setcached(true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Close the cursor
        c.close();
        //Statuses list is returned
        return status;
    }

    /***
     * Method to hydrate cached statuses from database
     * @param c Cursor
     * @return List<Status>
     */
    private List<Status> cursorToListStatus(Cursor c){
        //No element found
        if (c.getCount() == 0)
            return null;
        List<Status> statuses = new ArrayList<>();
        while (c.moveToNext() ) {
            //Restore cached status
            try {
                Status status = API.parseStatuses(context, new JSONObject(c.getString(c.getColumnIndex(Sqlite.COL_CACHE))));
                status.setcached(true);
                statuses.add(status);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //Close the cursor
        c.close();
        //Statuses list is returned
        return statuses;
    }
}
