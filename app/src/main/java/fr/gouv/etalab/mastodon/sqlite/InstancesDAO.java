package fr.gouv.etalab.mastodon.sqlite;
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
 * Created by Thomas on 20/08/2018.
 * Manage instance names in DB
 */
public class InstancesDAO {

    private SQLiteDatabase db;
    public Context context;
    private String userId;

    public InstancesDAO(Context context, SQLiteDatabase db) {
        //Creation of the DB with tables
        this.context = context;
        this.db = db;
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);

    }


    //------- INSERTIONS  -------

    /**
     * Insert an instance name in database
     * @param instanceName String
     */
    public void insertInstance(String instanceName) {
        ContentValues values = new ContentValues();
        values.put(Sqlite.COL_INSTANCE, instanceName);
        values.put(Sqlite.COL_USER_ID, userId);
        values.put(Sqlite.COL_DATE_CREATION, Helper.dateToString(new Date()));
        //Inserts search
        try{
            db.insert(Sqlite.TABLE_INSTANCES, null, values);
        }catch (Exception ignored) {}
    }


    //------- REMOVE  -------

    /***
     * Remove instance by its name
     * @return int
     */
    public int remove(String instanceName){
        return db.delete(Sqlite.TABLE_INSTANCES,  Sqlite.COL_INSTANCE + " = \"" + instanceName + "\" AND " + Sqlite.COL_USER_ID + " = \"" + userId+ "\"", null);
    }

    //------- REMOVE  -------

    /***
     * Remove instance by its name
     * @return int
     */
    public int cleanDoublon(){
        return db.delete(Sqlite.TABLE_INSTANCES,  Sqlite.COL_ID + " NOT IN (" +
                "  SELECT MIN("+Sqlite.COL_ID+")" +
                "  FROM " + Sqlite.TABLE_INSTANCES +
                "  GROUP BY "+ Sqlite.COL_INSTANCE + "," + Sqlite.COL_USER_ID +")", null);
    }

    //------- GETTERS  -------

    /**
     * Returns all instances in db for a user
     * @return instances List<String>
     */
    public List<String> getAllInstances(){
        try {
            Cursor c = db.query(Sqlite.TABLE_INSTANCES, null, Sqlite.COL_USER_ID + " = '" + userId+ "'", null, null, null, Sqlite.COL_INSTANCE + " ASC", null);
            return cursorToListSearch(c);
        } catch (Exception e) {
            return null;
        }
    }



    /**
     * Returns instance by its nale in db
     * @return instance List<String>
     */
    public List<String> getInstanceByName(String keyword){
        try {
            Cursor c = db.query(Sqlite.TABLE_INSTANCES, null, Sqlite.COL_INSTANCE + " = \"" + keyword + "\" AND " + Sqlite.COL_USER_ID + " = \"" + userId+ "\"", null, null, null, null, null);
            return cursorToListSearch(c);
        } catch (Exception e) {
            return null;
        }
    }


    /***
     * Method to hydrate stored instances from database
     * @param c Cursor
     * @return List<String>
     */
    private List<String> cursorToListSearch(Cursor c){
        //No element found
        if (c.getCount() == 0)
            return null;
        List<String> instances = new ArrayList<>();
        while (c.moveToNext() ) {
            instances.add(c.getString(c.getColumnIndex(Sqlite.COL_INSTANCE)));
        }
        //Close the cursor
        c.close();
        //Search list is returned
        return instances;
    }
}
