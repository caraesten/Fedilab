package fr.gouv.etalab.mastodon.sqlite;
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

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.gouv.etalab.mastodon.helper.Helper;


/**
 * Created by Thomas on 04/01/2018.
 * Manage temp mute in DB
 */
public class TempMuteDAO {

    private SQLiteDatabase db;
    public Context context;
    private String userId;

    public TempMuteDAO(Context context, SQLiteDatabase db) {
        //Creation of the DB with tables
        this.context = context;
        this.db = db;
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);

    }


    //------- INSERTIONS  -------

    /**
     * Insert a keyword in database
     * @param targeted_id String
     * @param date Date
     */
    public void insert(String targeted_id, Date date) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        ContentValues values = new ContentValues();
        values.put(Sqlite.COL_TARGETED_USER_ID, targeted_id);
        values.put(Sqlite.COL_USER_ID, userId);
        values.put(Sqlite.COL_DATE_CREATION, Helper.dateToString(context, new Date()));
        values.put(Sqlite.COL_DATE_END, Helper.dateToString(context, date));
        //Inserts temp mute
        try{
            db.insert(Sqlite.TABLE_TEMP_MUTE, null, values);
        }catch (Exception ignored) {}
    }


    //------- REMOVE  -------

    /***
     * Remove mute by its id
     * @return int
     */
    public int remove(String targeted_id){
        return db.delete(Sqlite.TABLE_TEMP_MUTE,  Sqlite.COL_TARGETED_USER_ID + " = \"" + targeted_id + "\" AND " + Sqlite.COL_USER_ID + " = \"" + userId+ "\"", null);
    }

    //------- GETTERS  -------

    /**
     * Returns all search in db for a user
     * @return search List<String>
     */
    public List<String> getAllSearch(){
        try {
            Cursor c = db.query(Sqlite.TABLE_SEARCH, null, Sqlite.COL_USER_ID + " = '" + userId+ "'", null, null, null, Sqlite.COL_KEYWORDS + " ASC", null);
            return cursorToListSearch(c);
        } catch (Exception e) {
            return null;
        }
    }



    /**
     * Returns search by its keyword in db
     * @return keywords List<String>
     */
    public List<String> getSearchStartingBy(String keyword){
        try {
            Cursor c = db.query(Sqlite.TABLE_SEARCH, null, Sqlite.COL_KEYWORDS + " LIKE \"%" + keyword + "%\" AND " + Sqlite.COL_USER_ID + " = \"" + userId+ "\"", null, null, null, null, null);
            return cursorToListSearch(c);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns search by its keyword in db
     * @return keywords List<String>
     */
    public List<String> getSearchByKeyword(String keyword){
        try {
            Cursor c = db.query(Sqlite.TABLE_SEARCH, null, Sqlite.COL_KEYWORDS + " = \"" + keyword + "\" AND " + Sqlite.COL_USER_ID + " = \"" + userId+ "\"", null, null, null, null, null);
            return cursorToListSearch(c);
        } catch (Exception e) {
            return null;
        }
    }


    /***
     * Method to hydrate stored search from database
     * @param c Cursor
     * @return List<String>
     */
    private List<String> cursorToListSearch(Cursor c){
        //No element found
        if (c.getCount() == 0)
            return null;
        List<String> searches = new ArrayList<>();
        while (c.moveToNext() ) {
            searches.add(c.getString(c.getColumnIndex(Sqlite.COL_KEYWORDS)));
        }
        //Close the cursor
        c.close();
        //Search list is returned
        return searches;
    }
}
