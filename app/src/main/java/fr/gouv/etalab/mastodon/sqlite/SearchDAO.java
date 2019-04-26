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

import fr.gouv.etalab.mastodon.client.Entities.TagTimeline;
import fr.gouv.etalab.mastodon.helper.Helper;


/**
 * Created by Thomas on 22/11/2017.
 * Manage search in DB
 */
public class SearchDAO {

    private SQLiteDatabase db;
    public Context context;
    private String userId;

    public SearchDAO(Context context, SQLiteDatabase db) {
        //Creation of the DB with tables
        this.context = context;
        this.db = db;
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);

    }


    //------- INSERTIONS  -------

    /**
     * Insert a keyword in database
     * @param keyword String
     */
    public void insertSearch(String keyword) {
        ContentValues values = new ContentValues();
        if( keyword != null && keyword.trim().length() > 0) {
            values.put(Sqlite.COL_KEYWORDS, keyword.trim());
            values.put(Sqlite.COL_USER_ID, userId);
            values.put(Sqlite.COL_DATE_CREATION, Helper.dateToString(new Date()));
            //Inserts search
            try {
                db.insert(Sqlite.TABLE_SEARCH, null, values);
            } catch (Exception ignored) {
            }
        }
    }


    //------- UPDATES  -------

    /**
     * update tag timeline info in database
     * @param tagTimeline TagTimeline
     */
    public void updateSearch(TagTimeline tagTimeline) {
        ContentValues values = new ContentValues();
        values.put(Sqlite.COL_IS_ART, tagTimeline.isART()?1:0);
        values.put(Sqlite.COL_IS_NSFW, tagTimeline.isNSFW()?1:0);
        List<String> any = tagTimeline.getAny();
        List<String> all = tagTimeline.getAll();
        List<String> none = tagTimeline.getNone();
        String displayname = tagTimeline.getDisplayname();
        values.put(Sqlite.COL_NAME, displayname);
        if( any != null && any.size() > 0)
            values.put(Sqlite.COL_ANY, Helper.arrayToStringStorage(any));
        if( all != null && all.size() > 0)
            values.put(Sqlite.COL_ALL, Helper.arrayToStringStorage(all));
        if( none != null && none.size() > 0)
            values.put(Sqlite.COL_NONE, Helper.arrayToStringStorage(none));
        try{
            db.update(Sqlite.TABLE_SEARCH,  values, Sqlite.COL_ID + " =  ? ", new String[]{String.valueOf(tagTimeline.getId())});
        }catch (Exception ignored) {}
    }


    //------- REMOVE  -------

    /***
     * Remove search by keyword
     * @return int
     */
    public int remove(String keyword){
        return db.delete(Sqlite.TABLE_SEARCH,  Sqlite.COL_KEYWORDS + " = \"" + keyword + "\" AND " + Sqlite.COL_USER_ID + " = \"" + userId+ "\"", null);
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
            if( c.getString(c.getColumnIndex(Sqlite.COL_NAME)) != null)
                searches.add(c.getString(c.getColumnIndex(Sqlite.COL_NAME)));
            else
                searches.add(c.getString(c.getColumnIndex(Sqlite.COL_KEYWORDS)));
        }
        //Close the cursor
        c.close();
        //Search list is returned
        return searches;
    }


    /**
     * Returns TagTimeline information by its keyword in db
     * @return info List<TagTimeline>
     */
    public List<TagTimeline> getTimelineInfo(String keyword){
        try {
            Cursor c = db.query(Sqlite.TABLE_SEARCH, null, Sqlite.COL_KEYWORDS + " = \"" + keyword + "\" AND " + Sqlite.COL_USER_ID + " = \"" + userId+ "\"", null, null, null, null, null);
            return cursorToTagTimelineSearch(c);
        } catch (Exception e) {
            return null;
        }
    }



    /**
     * Returns TagTimeline information by its keyword in db
     * @return info List<TagTimeline>
     */
    public List<TagTimeline> getAll(){
        try {
            Cursor c = db.query(Sqlite.TABLE_SEARCH, null, Sqlite.COL_USER_ID + " = \"" + userId+ "\"", null, null, null, Sqlite.COL_KEYWORDS + " ASC", null);
            return cursorToTagTimelineSearch(c);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns TagTimeline information by its keyword in db
     * @return info List<TagTimeline>
     */
    public List<TagTimeline> getTabInfo(String name){
        try {
            Cursor c = db.query(Sqlite.TABLE_SEARCH, null, Sqlite.COL_NAME + " = \"" + name + "\" AND " + Sqlite.COL_USER_ID + " = \"" + userId+ "\"", null, null, null, null, null);
            return cursorToTagTimelineSearch(c);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns TagTimeline information by its keyword in db
     * @return info List<TagTimeline>
     */
    public List<TagTimeline> getTabInfoKeyword(String name){
        try {
            Cursor c = db.query(Sqlite.TABLE_SEARCH, null, Sqlite.COL_KEYWORDS + " = \"" + name + "\" AND " + Sqlite.COL_USER_ID + " = \"" + userId+ "\"", null, null, null, null, null);
            return cursorToTagTimelineSearch(c);
        } catch (Exception e) {
            return null;
        }
    }

    /***
     * Method to hydrate stored search from database
     * @param c Cursor
     * @return List<String>
     */
    private List<TagTimeline> cursorToTagTimelineSearch(Cursor c){
        //No element found
        if (c.getCount() == 0)
            return null;
        List<TagTimeline> searches = new ArrayList<>();
        while (c.moveToNext() ) {
            TagTimeline tagTimeline = new TagTimeline();
            try {
                tagTimeline.setAny(Helper.restoreArrayFromString(c.getString(c.getColumnIndex(Sqlite.COL_ANY))));
            }catch (Exception ignored){}
            try {
                tagTimeline.setAll(Helper.restoreArrayFromString(c.getString(c.getColumnIndex(Sqlite.COL_ALL))));
            }catch (Exception ignored){}
            try {
                tagTimeline.setNone(Helper.restoreArrayFromString(c.getString(c.getColumnIndex(Sqlite.COL_NONE))));
            }catch (Exception ignored){}
            tagTimeline.setId(c.getInt(c.getColumnIndex(Sqlite.COL_ID)));
            tagTimeline.setName(c.getString(c.getColumnIndex(Sqlite.COL_KEYWORDS)));
            tagTimeline.setDisplayname(c.getString(c.getColumnIndex(Sqlite.COL_NAME)));
            tagTimeline.setART(c.getInt(c.getColumnIndex(Sqlite.COL_IS_ART))==1);
            tagTimeline.setNSFW(c.getInt(c.getColumnIndex(Sqlite.COL_IS_NSFW))==1);
            searches.add(tagTimeline);
        }
        //Close the cursor
        c.close();
        //Search list is returned
        return searches;
    }
}
