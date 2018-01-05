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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.helper.Helper;


/**
 * Created by Thomas on 04/01/2018.
 * Manage temp mute in DB
 */
public class TempMuteDAO {

    private SQLiteDatabase db;
    public Context context;

    public TempMuteDAO(Context context, SQLiteDatabase db) {
        //Creation of the DB with tables
        this.context = context;
        this.db = db;
    }


    //------- INSERTIONS  -------

    /**
     * Insert a keyword in database
     * @param account Account
     * @param targeted_id String
     * @param date Date
     */
    public void insert(Account account, String targeted_id, Date date) {
        ContentValues values = new ContentValues();
        values.put(Sqlite.COL_TARGETED_USER_ID, targeted_id);
        values.put(Sqlite.COL_ACCT, account.getAcct());
        values.put(Sqlite.COL_INSTANCE, account.getInstance());
        values.put(Sqlite.COL_DATE_CREATION, Helper.dateToString(context, new Date()));
        values.put(Sqlite.COL_DATE_END, Helper.dateToString(context, date));
        //Inserts temp mute
        try{
            db.insert(Sqlite.TABLE_TEMP_MUTE, null, values);
        }catch (Exception ignored) {}
    }

    //------- REMOVE  -------

    /***
     * Remove mute by its id for the authenticated user
     * @return int
     */
    public int remove(Account account, String targeted_id){
        return db.delete(Sqlite.TABLE_TEMP_MUTE,  Sqlite.COL_TARGETED_USER_ID + " = \"" + targeted_id + "\" AND " + Sqlite.COL_ACCT + " = \"" + account.getAcct()+ "\" AND " + Sqlite.COL_INSTANCE + " = \"" + account.getInstance()+ "\"", null);
    }

    /***
     * Remove mute by its id
     */
    public void removeOld(){
        db.delete(Sqlite.TABLE_TEMP_MUTE, Sqlite.COL_DATE_END + " < \"" + Helper.dateToString(context, new Date())+ "\"", null);
    }

    //------- GETTERS  -------

    /**
     * Returns all id of timed mute in db
     * @return time muted List<String>
     */
    public List<String> getAllTimeMuted(Account account){
        try {
            Cursor c = db.query(Sqlite.TABLE_TEMP_MUTE, null, Sqlite.COL_DATE_END + " >= \"" + Helper.dateToString(context, new Date())+ "\" AND " + Sqlite.COL_ACCT + " = \"" + account.getAcct()+ "\" AND " + Sqlite.COL_INSTANCE + " = \"" + account.getInstance()+ "\"", null, null, null, null, null);
            return cursorToTimeMute(c);
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * Returns true if user is temp muted by the authenticated user
     * @return keywords List<String>
     */
    public boolean isTempMuted(Account account, String targeted_id){
        try {
            Cursor c = db.query(Sqlite.TABLE_TEMP_MUTE, null, Sqlite.COL_TARGETED_USER_ID + " = \"" + targeted_id + "\" AND " + Sqlite.COL_DATE_END + " >= \"" + Helper.dateToString(context, new Date())+ "\" AND "  + Sqlite.COL_ACCT + " = \"" + account.getAcct()+ "\" AND " + Sqlite.COL_INSTANCE + " = \"" + account.getInstance()+ "\"", null, null, null, null, null);
            return cursorToTimeMute(c) != null;
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * Returns true if user is temp muted by the authenticated user
     * @return keywords List<String>
     */
    public String getMuteDateByID(Account account, String targeted_id){
        try {
            Cursor c = db.query(Sqlite.TABLE_TEMP_MUTE, null, Sqlite.COL_TARGETED_USER_ID + " = \"" + targeted_id + "\" AND " + Sqlite.COL_DATE_END + " >= \"" + Helper.dateToString(context, new Date())+ "\" AND " + Sqlite.COL_ACCT + " = \"" + account.getAcct()+ "\" AND " + Sqlite.COL_INSTANCE + " = \"" + account.getInstance()+ "\"", null, null, null, Sqlite.COL_DATE_END + " DESC", "1");
            return cursorToDate(c);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



    /***
     * Method to hydrate time mute id  from database
     * @param c Cursor
     * @return List<String>
     */
    private List<String> cursorToTimeMute(Cursor c){
        //No element found
        if (c.getCount() == 0)
            return null;
        List<String> timeMutes = new ArrayList<>();
        while (c.moveToNext() ) {
            timeMutes.add(c.getString(c.getColumnIndex(Sqlite.COL_TARGETED_USER_ID)));
        }
        //Close the cursor
        c.close();
        //Time mute id list is returned
        return timeMutes;
    }

    /***
     * Method to get date for the timed muted account by its id
     * @param c Cursor
     * @return String
     */
    private String cursorToDate(Cursor c){
        //No element found
        if (c.getCount() == 0)
            return null;
        c.moveToNext();
        String date = c.getString(c.getColumnIndex(Sqlite.COL_DATE_END));
        //Close the cursor
        c.close();
        //Time mute  date is returned
        return date;
    }
}
