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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Thomas on 14/02/2019.
 * Manage domain block in DB
 */
public class DomainBlockDAO {

    private SQLiteDatabase db;
    public Context context;

    public DomainBlockDAO(Context context, SQLiteDatabase db) {
        //Creation of the DB with tables
        this.context = context;
        this.db = db;
    }

    //------- INSERTIONS  -------

    /**
     * Insert a domain in database
     * @param domain String
     */
    private void insert(String domain) {
        ContentValues values = new ContentValues();
        values.put(Sqlite.COL_DOMAIN, domain);
        try{
            db.insert(Sqlite.TABLE_TRACKING_BLOCK, null, values);
        }catch (Exception ignored) {}
    }

    /**
     * Insert domains in database
     * @param domains List<String>
     */
    public void set(List<String> domains) {
        removeAll();
        for (String domain : domains)
            insert(domain);

    }

    public int getTotalCount(){
        Cursor mCount= db.rawQuery("SELECT Count(*) FROM " + Sqlite.TABLE_TRACKING_BLOCK, null);
        mCount.moveToFirst();
        int count = mCount.getInt(0);
        mCount.close();
        return count;
    }
    /***
     * Remove all domains
     */
    private void removeAll(){
        db.delete(Sqlite.TABLE_TRACKING_BLOCK, null, null);
    }


    /**
     * Returns all domains in db
     * @return string domain List<String>
     */
    public List<String> getAll(){
        try {
            Cursor c = db.query(Sqlite.TABLE_TRACKING_BLOCK, null, null, null, null, null, null, null);
            return cursorToDomain(c);
        } catch (Exception e) {
            return null;
        }
    }



    /***
     * Method to hydrate domain from database
     * @param c Cursor
     * @return List<String>
     */
    private List<String> cursorToDomain(Cursor c){
        //No element found
        if (c.getCount() == 0)
            return null;
        List<String> domains = new ArrayList<>();
        while (c.moveToNext() ) {
            domains.add(c.getString(c.getColumnIndex(Sqlite.COL_DOMAIN)));
        }
        //Close the cursor
        c.close();
        //domains list is returned
        return domains;
    }
}
