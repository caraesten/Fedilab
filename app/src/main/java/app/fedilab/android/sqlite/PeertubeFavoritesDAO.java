package app.fedilab.android.sqlite;
/* Copyright 2018 Thomas Schneider
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
import app.fedilab.android.client.Entities.Peertube;
import app.fedilab.android.helper.Helper;


/**
 * Created by Thomas on 21/10/2018.
 * Manage Peertube favorites
 */
public class PeertubeFavoritesDAO {

    private SQLiteDatabase db;
    public Context context;

    public PeertubeFavoritesDAO(Context context, SQLiteDatabase db) {
        //Creation of the DB with tables
        this.context = context;
        this.db = db;
    }


    //------- INSERTIONS  -------
    /**
     * Insert a status in database
     * @param peertube Peertube
     * @return boolean
     */
    public long insert(Peertube peertube) {
        ContentValues values = new ContentValues();
        values.put(Sqlite.COL_UUID, peertube.getUuid());
        values.put(Sqlite.COL_INSTANCE, peertube.getInstance());
        values.put(Sqlite.COL_DATE, Helper.dateToString(new Date()));
        values.put(Sqlite.COL_CACHE, peertube.getCache().toString());
        //Inserts cached peertube
        long last_id;
        try{
            last_id = db.insert(Sqlite.TABLE_PEERTUBE_FAVOURITES, null, values);
        }catch (Exception e) {
            last_id =  -1;
        }
        return last_id;
    }
    //------- REMOVE  -------

    /***
     * Remove stored status
     * @return int
     */
    public int remove(Peertube peertube){
        return db.delete(Sqlite.TABLE_PEERTUBE_FAVOURITES,  Sqlite.COL_UUID + " = \""+ peertube.getUuid() +"\" AND " + Sqlite.COL_INSTANCE + " = \"" + peertube.getInstance() + "\"", null);
    }

    /***
     * Remove stored status
     * @return int
     */
    public int removeAll(){
        return db.delete(Sqlite.TABLE_PEERTUBE_FAVOURITES,  null, null);
    }



    //------- GETTERS  -------

    /**
     * Returns all cached Peertube
     * @return stored peertube List<Peertube>
     */
    public List<Peertube> getAllPeertube(){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        try {
            Cursor c = db.query(Sqlite.TABLE_PEERTUBE_FAVOURITES, null,  null, null, null, null, Sqlite.COL_DATE+ " DESC");
            return cursorToListPeertube(c);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns a cached Peertube
     * @return stored peertube List<Peertube>
     */
    public List<Peertube> getSinglePeertube(Peertube peertube){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        try {
            Cursor c = db.query(Sqlite.TABLE_PEERTUBE_FAVOURITES, null,  Sqlite.COL_UUID + " = \""+ peertube.getUuid() +"\" AND " + Sqlite.COL_INSTANCE + " = \"" + peertube.getInstance() + "\"", null, null, null, Sqlite.COL_DATE+ " DESC");
            return cursorToListPeertube(c);
        } catch (Exception e) {
            return null;
        }
    }

    /***
     * Method to hydrate cached statuses from database
     * @param c Cursor
     * @return List<Peertube>
     */
    private List<Peertube> cursorToListPeertube(Cursor c){
        //No element found
        if (c.getCount() == 0) {
            c.close();
            return null;
        }
        List<Peertube> peertubes = new ArrayList<>();
        while (c.moveToNext() ) {
            //Restore cached status
            try {
                Peertube peertube = API.parsePeertube(context, c.getString(c.getColumnIndex(Sqlite.COL_INSTANCE)), new JSONObject(c.getString(c.getColumnIndex(Sqlite.COL_CACHE))));
                peertubes.add(peertube);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //Close the cursor
        c.close();
        //Peertubes list is returned
        return peertubes;
    }
}
