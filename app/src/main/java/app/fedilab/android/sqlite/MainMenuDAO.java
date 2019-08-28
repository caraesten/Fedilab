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
import app.fedilab.android.client.Entities.MainMenuItem;
import app.fedilab.android.helper.Helper;


/**
 * Created by Thomas on 28/08/2019.
 * Manage menu items in DB
 */
public class MainMenuDAO {

    private SQLiteDatabase db;
    public Context context;

    public MainMenuDAO(Context context, SQLiteDatabase db) {
        //Creation of the DB with tables
        this.context = context;
        this.db = db;
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);

    }


    //------- INSERTIONS  -------

    /**
     * Insert a menu configuration name in database
     * @param mainMenuItem MainMenuItem
     * @param userId String
     * @param instance String
     */
    public void insertInstance(MainMenuItem mainMenuItem, String userId, String instance) {
        ContentValues values = new ContentValues();
        values.put(Sqlite.COL_INSTANCE, instance);
        values.put(Sqlite.COL_USER_ID, userId);
        values.put(Sqlite.COL_NAV_NEWS, mainMenuItem.isNav_news()?1:0);
        values.put(Sqlite.COL_NAV_LIST, mainMenuItem.isNav_list()?1:0);
        values.put(Sqlite.COL_NAV_SCHEDULED, mainMenuItem.isNav_scheduled()?1:0);
        values.put(Sqlite.COL_NAV_ARCHIVE, mainMenuItem.isNav_archive()?1:0);
        values.put(Sqlite.COL_NAV_ARCHIVE_NOTIFICATIONS, mainMenuItem.isNav_archive_notifications()?1:0);
        values.put(Sqlite.COL_NAV_PEERTUBE, mainMenuItem.isNav_peertube()?1:0);
        values.put(Sqlite.COL_NAV_FILTERS, mainMenuItem.isNav_filters()?1:0);
        values.put(Sqlite.COL_NAV_HOW_TO_FOLLOW, mainMenuItem.isNav_how_to_follow()?1:0);
        values.put(Sqlite.COL_NAV_ADMINISTRATION, mainMenuItem.isNav_administration()?1:0);
        values.put(Sqlite.COL_NAV_BLOCKED, mainMenuItem.isNav_blocked()?1:0);
        values.put(Sqlite.COL_NAV_MUTED, mainMenuItem.isNav_muted()?1:0);
        values.put(Sqlite.COL_NAV_BLOCKED_DOMAINS, mainMenuItem.isNav_blocked()?1:0);
        values.put(Sqlite.COL_NAV_HOWTO, mainMenuItem.isNav_howto()?1:0);
        //Inserts menu conf
        try{
            db.insert(Sqlite.TABLE_MAIN_MENU_ITEMS, null, values);
        }catch (Exception ignored) {}
    }

    //------- INSERTIONS  -------

    /**
     * Insert a menu configuration name in database
     * @param mainMenuItem MainMenuItem
     */
    public void insertInstance(MainMenuItem mainMenuItem) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);

        ContentValues values = new ContentValues();
        values.put(Sqlite.COL_INSTANCE, instance);
        values.put(Sqlite.COL_USER_ID, userId);
        values.put(Sqlite.COL_NAV_NEWS, mainMenuItem.isNav_news()?1:0);
        values.put(Sqlite.COL_NAV_LIST, mainMenuItem.isNav_list()?1:0);
        values.put(Sqlite.COL_NAV_SCHEDULED, mainMenuItem.isNav_scheduled()?1:0);
        values.put(Sqlite.COL_NAV_ARCHIVE, mainMenuItem.isNav_archive()?1:0);
        values.put(Sqlite.COL_NAV_ARCHIVE_NOTIFICATIONS, mainMenuItem.isNav_archive_notifications()?1:0);
        values.put(Sqlite.COL_NAV_PEERTUBE, mainMenuItem.isNav_peertube()?1:0);
        values.put(Sqlite.COL_NAV_FILTERS, mainMenuItem.isNav_filters()?1:0);
        values.put(Sqlite.COL_NAV_HOW_TO_FOLLOW, mainMenuItem.isNav_how_to_follow()?1:0);
        values.put(Sqlite.COL_NAV_ADMINISTRATION, mainMenuItem.isNav_administration()?1:0);
        values.put(Sqlite.COL_NAV_BLOCKED, mainMenuItem.isNav_blocked()?1:0);
        values.put(Sqlite.COL_NAV_MUTED, mainMenuItem.isNav_muted()?1:0);
        values.put(Sqlite.COL_NAV_BLOCKED_DOMAINS, mainMenuItem.isNav_blocked()?1:0);
        values.put(Sqlite.COL_NAV_HOWTO, mainMenuItem.isNav_howto()?1:0);
        //Inserts menu conf
        try{
            db.insert(Sqlite.TABLE_MAIN_MENU_ITEMS, null, values);
        }catch (Exception ignored) {}
    }


    //------- UPDATES  -------


    /**
     * update menu items in database
     * @param mainMenuItem MainMenuItem
     */
    public void updateInstance(MainMenuItem mainMenuItem, String userId, String instance) {
        ContentValues values = new ContentValues();
        values.put(Sqlite.COL_INSTANCE, instance);
        values.put(Sqlite.COL_USER_ID, userId);
        values.put(Sqlite.COL_NAV_NEWS, mainMenuItem.isNav_news()?1:0);
        values.put(Sqlite.COL_NAV_LIST, mainMenuItem.isNav_list()?1:0);
        values.put(Sqlite.COL_NAV_SCHEDULED, mainMenuItem.isNav_scheduled()?1:0);
        values.put(Sqlite.COL_NAV_ARCHIVE, mainMenuItem.isNav_archive()?1:0);
        values.put(Sqlite.COL_NAV_ARCHIVE_NOTIFICATIONS, mainMenuItem.isNav_archive_notifications()?1:0);
        values.put(Sqlite.COL_NAV_PEERTUBE, mainMenuItem.isNav_peertube()?1:0);
        values.put(Sqlite.COL_NAV_FILTERS, mainMenuItem.isNav_filters()?1:0);
        values.put(Sqlite.COL_NAV_HOW_TO_FOLLOW, mainMenuItem.isNav_how_to_follow()?1:0);
        values.put(Sqlite.COL_NAV_ADMINISTRATION, mainMenuItem.isNav_administration()?1:0);
        values.put(Sqlite.COL_NAV_BLOCKED, mainMenuItem.isNav_blocked()?1:0);
        values.put(Sqlite.COL_NAV_MUTED, mainMenuItem.isNav_muted()?1:0);
        values.put(Sqlite.COL_NAV_BLOCKED_DOMAINS, mainMenuItem.isNav_blocked()?1:0);
        values.put(Sqlite.COL_NAV_HOWTO, mainMenuItem.isNav_howto()?1:0);
        try{
            db.update(Sqlite.TABLE_MAIN_MENU_ITEMS,  values, Sqlite.COL_USER_ID + " =  ? AND " + Sqlite.COL_INSTANCE + " =  ? " ,
                    new String[]{userId, instance});
        }catch (Exception ignored) {ignored.printStackTrace();}
    }


    /**
     * update menu items in database
     * @param mainMenuItem MainMenuItem
     */
    public void updateInstance(MainMenuItem mainMenuItem) {
        ContentValues values = new ContentValues();
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        values.put(Sqlite.COL_INSTANCE, instance);
        values.put(Sqlite.COL_USER_ID, userId);
        values.put(Sqlite.COL_NAV_NEWS, mainMenuItem.isNav_news()?1:0);
        values.put(Sqlite.COL_NAV_LIST, mainMenuItem.isNav_list()?1:0);
        values.put(Sqlite.COL_NAV_SCHEDULED, mainMenuItem.isNav_scheduled()?1:0);
        values.put(Sqlite.COL_NAV_ARCHIVE, mainMenuItem.isNav_archive()?1:0);
        values.put(Sqlite.COL_NAV_ARCHIVE_NOTIFICATIONS, mainMenuItem.isNav_archive_notifications()?1:0);
        values.put(Sqlite.COL_NAV_PEERTUBE, mainMenuItem.isNav_peertube()?1:0);
        values.put(Sqlite.COL_NAV_FILTERS, mainMenuItem.isNav_filters()?1:0);
        values.put(Sqlite.COL_NAV_HOW_TO_FOLLOW, mainMenuItem.isNav_how_to_follow()?1:0);
        values.put(Sqlite.COL_NAV_ADMINISTRATION, mainMenuItem.isNav_administration()?1:0);
        values.put(Sqlite.COL_NAV_BLOCKED, mainMenuItem.isNav_blocked()?1:0);
        values.put(Sqlite.COL_NAV_MUTED, mainMenuItem.isNav_muted()?1:0);
        values.put(Sqlite.COL_NAV_BLOCKED_DOMAINS, mainMenuItem.isNav_blocked()?1:0);
        values.put(Sqlite.COL_NAV_HOWTO, mainMenuItem.isNav_howto()?1:0);
        try{
            db.update(Sqlite.TABLE_MAIN_MENU_ITEMS,  values, Sqlite.COL_USER_ID + " =  ? AND " + Sqlite.COL_INSTANCE + " =  ? " ,
                    new String[]{userId, instance});
        }catch (Exception ignored) {ignored.printStackTrace();}
    }




    //------- GETTERS  -------


    /**
     * Returns instance by its nale in db
     * @param userId String
     * @param instance String
     * @return MainMenuItem
     */
    public MainMenuItem getMainMenu(String userId, String instance){
        try {
            Cursor c = db.query(Sqlite.TABLE_MAIN_MENU_ITEMS, null, Sqlite.COL_INSTANCE + " = '" + instance+ "' AND " + Sqlite.COL_USER_ID + " = '" + userId+ "'", null, null, null, null, "1");
            return cursorToMainMenu(c);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns instance by its nale in db
     * @return MainMenuItem
     */
    public MainMenuItem getMainMenu(){
        try {
            SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
            String instance = Helper.getLiveInstance(context);
            Cursor c = db.query(Sqlite.TABLE_MAIN_MENU_ITEMS, null, Sqlite.COL_INSTANCE + " = '" + instance+ "' AND " + Sqlite.COL_USER_ID + " = '" + userId+ "'", null, null, null, null, "1");
            return cursorToMainMenu(c);
        } catch (Exception e) {
            return null;
        }
    }



    /***
     * Method to hydrate main menu items from database
     * @param c Cursor
     * @return List<RemoteInstance>
     */
    private MainMenuItem cursorToMainMenu(Cursor c){
        //No element found
        MainMenuItem mainMenuItem = new MainMenuItem();
        if (c.getCount() == 0) {
            c.close();
            return mainMenuItem;
        }
        c.moveToFirst();

        mainMenuItem.setNav_administration(c.getInt(c.getColumnIndex(Sqlite.COL_NAV_ADMINISTRATION)) == 1);
        mainMenuItem.setNav_archive(c.getInt(c.getColumnIndex(Sqlite.COL_NAV_ARCHIVE)) == 1);
        mainMenuItem.setNav_archive_notifications(c.getInt(c.getColumnIndex(Sqlite.COL_NAV_ARCHIVE_NOTIFICATIONS)) == 1);
        mainMenuItem.setNav_blocked(c.getInt(c.getColumnIndex(Sqlite.COL_NAV_BLOCKED)) == 1);
        mainMenuItem.setNav_blocked_domains(c.getInt(c.getColumnIndex(Sqlite.COL_NAV_BLOCKED_DOMAINS)) == 1);
        mainMenuItem.setNav_filters(c.getInt(c.getColumnIndex(Sqlite.COL_NAV_FILTERS)) == 1);
        mainMenuItem.setNav_how_to_follow(c.getInt(c.getColumnIndex(Sqlite.COL_NAV_HOW_TO_FOLLOW)) == 1);
        mainMenuItem.setNav_howto(c.getInt(c.getColumnIndex(Sqlite.COL_NAV_HOWTO)) == 1);
        mainMenuItem.setNav_list(c.getInt(c.getColumnIndex(Sqlite.COL_NAV_LIST)) == 1);
        mainMenuItem.setNav_muted(c.getInt(c.getColumnIndex(Sqlite.COL_NAV_MUTED)) == 1);
        mainMenuItem.setNav_news(c.getInt(c.getColumnIndex(Sqlite.COL_NAV_NEWS)) == 1);
        mainMenuItem.setNav_peertube(c.getInt(c.getColumnIndex(Sqlite.COL_NAV_PEERTUBE)) == 1);
        mainMenuItem.setNav_scheduled(c.getInt(c.getColumnIndex(Sqlite.COL_NAV_SCHEDULED)) == 1);

        //Close the cursor
        c.close();
        return mainMenuItem;
    }
}
