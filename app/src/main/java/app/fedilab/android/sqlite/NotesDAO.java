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

import java.util.Date;

import app.fedilab.android.client.Entities.UserNote;
import app.fedilab.android.helper.Helper;


/**
 * Created by Thomas on 12/09/2019.
 * Manage user notes  in DB
 */
public class NotesDAO {

    public Context context;
    private SQLiteDatabase db;

    public NotesDAO(Context context, SQLiteDatabase db) {
        //Creation of the DB with tables
        this.context = context;
        this.db = db;
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);

    }


    //------- INSERTIONS  -------

    /**
     * Insert a note in database
     *
     * @param userNote UserNote
     */
    public void insertInstance(UserNote userNote) {

        UserNote notes = getUserNote(userNote.getAcct());
        //There is a note
        if (userNote.getNote() != null && userNote.getNote().trim().length() > 0) {
            if (notes != null) { //Notes already exist, it needs an update
                ContentValues values = new ContentValues();
                values.put(Sqlite.COL_NOTE, userNote.getNote());
                try {
                    db.update(Sqlite.TABLE_USER_NOTES, values, Sqlite.COL_ACCT + " =  ? ", new String[]{userNote.getAcct()});
                } catch (Exception ignored) {
                }
            } else { //notes don't exist, it's an insert
                ContentValues values = new ContentValues();
                values.put(Sqlite.COL_NOTE, userNote.getNote());
                values.put(Sqlite.COL_ACCT, userNote.getAcct());
                values.put(Sqlite.COL_DATE_CREATION, Helper.dateToString(new Date()));
                //Inserts noted
                try {
                    db.insert(Sqlite.TABLE_USER_NOTES, null, values);
                } catch (Exception ignored) {
                }
            }
        } else { //It's empty, it's a deletion
            db.delete(Sqlite.TABLE_USER_NOTES, Sqlite.COL_ACCT + " = \"" + userNote.getAcct() + "\"", null);
        }

    }


    //------- GETTERS  -------

    /**
     * Returns notes from an account
     *
     * @return UserNote
     */
    public UserNote getUserNote(String acct) {
        try {
            Cursor c = db.query(Sqlite.TABLE_USER_NOTES, null, Sqlite.COL_ACCT + " = \"" + acct + "\"", null, null, null, null, "1");
            return cursorToNote(c);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /***
     * Method to hydrate notes from database
     * @param c Cursor
     * @return List<RemoteInstance>
     */
    private UserNote cursorToNote(Cursor c) {
        //No element found
        if (c.getCount() == 0) {
            c.close();
            return null;
        }
        UserNote userNote = new UserNote();
        c.moveToFirst();
        userNote.setAcct(c.getString(c.getColumnIndex(Sqlite.COL_ACCT)));
        userNote.setNote(c.getString(c.getColumnIndex(Sqlite.COL_NOTE)));
        userNote.setCreation_date(Helper.stringToDate(context, c.getString(c.getColumnIndex(Sqlite.COL_DATE_CREATION))));
        //Close the cursor
        c.close();
        return userNote;
    }
}
