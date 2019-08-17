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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Thomas on 30/11/2018.
 * Manage cache tags in DB
 */
public class TagsCacheDAO {

    private SQLiteDatabase db;
    public Context context;

    public TagsCacheDAO(Context context, SQLiteDatabase db) {
        //Creation of the DB with tables
        this.context = context;
        this.db = db;
    }

    //------- INSERTIONS  -------

    /**
     * Insert a tag in database
     * @param tag String
     */
    public void insert(String tag) {
        ContentValues values = new ContentValues();
        values.put(Sqlite.COL_TAGS, tag);
        try{
            db.insert(Sqlite.TABLE_CACHE_TAGS, null, values);
        }catch (Exception ignored) {}
    }

    /**
     * update a tag in database
     * @param oldTag String
     * @param newTag String
     */
    public void update(String oldTag, String newTag) {
        ContentValues values = new ContentValues();
        values.put(Sqlite.COL_TAGS, newTag);
        try{
            db.update(Sqlite.TABLE_CACHE_TAGS, values, Sqlite.COL_TAGS + " = ?",new String[]{ oldTag});
        }catch (Exception ignored) {}
    }

    /***
     * Remove all tags
     */
    public void removeAll(){
        db.delete(Sqlite.TABLE_CACHE_TAGS, null, null);
    }


    public void removeTag(String tag) {
        db.delete(Sqlite.TABLE_CACHE_TAGS, Sqlite.COL_TAGS + " = ?", new String[]{tag});
    }

    /**
     * Returns all tags in db
     * @return string tags List<String>
     */
    public List<String> getAll(){
        try {
            Cursor c = db.query(Sqlite.TABLE_CACHE_TAGS, null, null, null, null, null, Sqlite.COL_TAGS+ " ASC", null);
            return cursorToTag(c);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns tags starting by "search"
     * @return boolean present
     */
    public boolean isPresent(String search){
        Cursor c = db.query(Sqlite.TABLE_CACHE_TAGS, null, Sqlite.COL_TAGS + " = \"" + search + "\"", null, null, null, null, null);
        boolean isPresent = (c!= null && c.getCount() > 0);
        assert c != null;
        c.close();
        return isPresent;
    }

    /**
     * Returns tags starting by "search"
     * @return tags List<String>
     */
    public List<String> getBy(String search){
        Cursor c = db.query(Sqlite.TABLE_CACHE_TAGS, null, Sqlite.COL_TAGS + " LIKE \"%" + search + "%\"", null, null, null, null, null);
        return cursorToTag(c);
    }

    /***
     * Method to hydrate tag from database
     * @param c Cursor
     * @return List<String>
     */
    private List<String> cursorToTag(Cursor c){
        //No element found
        if (c.getCount() == 0) {
            c.close();
            return null;
        }
        List<String> tags = new ArrayList<>();
        while (c.moveToNext() ) {
            tags.add(c.getString(c.getColumnIndex(Sqlite.COL_TAGS)));
        }
        //Close the cursor
        c.close();
        //Tag list is returned
        return tags;
    }
}
