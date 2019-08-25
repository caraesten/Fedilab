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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import app.fedilab.android.client.Entities.Charts;
import app.fedilab.android.client.Entities.Notification;
import app.fedilab.android.client.Entities.Statistics;
import app.fedilab.android.client.Entities.Status;
import app.fedilab.android.client.Entities.Tag;
import app.fedilab.android.helper.FilterToots;
import app.fedilab.android.helper.Helper;


/**
 * Created by Thomas on 24/28/2019.
 * Manage notification in cache
 */
public class NotificationCacheDAO {

    private SQLiteDatabase db;
    public Context context;


    public NotificationCacheDAO(Context context, SQLiteDatabase db) {
        //Creation of the DB with tables
        this.context = context;
        this.db = db;
    }


    //------- INSERTIONS  -------
    /**
     * Insert a status in database
     * @param notification Notification
     * @param userId String
     * @param instance String
     * @return long
     */
    public long insertNotification(Notification notification, String userId, String instance) {
        ContentValues values = new ContentValues();
        values.put(Sqlite.COL_NOTIFICATION_ID, notification.getId());
        values.put(Sqlite.COL_USER_ID, userId);
        values.put(Sqlite.COL_INSTANCE, instance);
        long id = -1;
        if( notification.getStatus() != null) {
            values.put(Sqlite.COL_STATUS_ID, notification.getStatus().getId());
            //Check if status exists in cache;
            Status status = new StatusCacheDAO(context, db).getStatus(StatusCacheDAO.NOTIFICATION_CACHE, notification.getStatus().getId(), userId, instance);
            if( status == null){
                id = new StatusCacheDAO(context, db).insertStatus(StatusCacheDAO.NOTIFICATION_CACHE, notification.getStatus(), userId, instance);
            }else {
                id = status.getDb_id();
            }
        }
        values.put(Sqlite.COL_STATUS_ID_CACHE, id);
        values.put(Sqlite.COL_ACCOUNT, Helper.accountToStringStorage(notification.getAccount()));
        values.put(Sqlite.COL_CREATED_AT, Helper.dateToString(notification.getCreated_at()));
        values.put(Sqlite.COL_TYPE, notification.getType());

        //Inserts cached status
        long last_id;
        try{
            last_id = db.insert(Sqlite.TABLE_NOTIFICATION_CACHE, null, values);
        }catch (Exception e) {
            last_id =  -1;
            e.printStackTrace();
        }
        return last_id;
    }

    //------- INSERTIONS  -------
    /**
     * Insert a status in database
     * @param notification Notification
     * @return long
     */
    public long insertNotification(Notification notification) {

        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);

        ContentValues values = new ContentValues();
        values.put(Sqlite.COL_NOTIFICATION_ID, notification.getId());
        values.put(Sqlite.COL_USER_ID, userId);
        values.put(Sqlite.COL_INSTANCE, instance);
        long id = -1;
        if( notification.getStatus() != null) {
            values.put(Sqlite.COL_STATUS_ID, notification.getStatus().getId());
            //Check if status exists in cache;
            Status status = new StatusCacheDAO(context, db).getStatus(StatusCacheDAO.NOTIFICATION_CACHE, notification.getStatus().getId(), userId, instance);
            if( status == null){
                id = new StatusCacheDAO(context, db).insertStatus(StatusCacheDAO.NOTIFICATION_CACHE, notification.getStatus(), userId, instance);
            }else {
                id = status.getDb_id();
            }
        }
        values.put(Sqlite.COL_STATUS_ID_CACHE, id);
        values.put(Sqlite.COL_ACCOUNT, Helper.accountToStringStorage(notification.getAccount()));
        values.put(Sqlite.COL_CREATED_AT, Helper.dateToString(notification.getCreated_at()));
        values.put(Sqlite.COL_TYPE, notification.getType());

        //Inserts cached notification
        long last_id;
        try{
            last_id = db.insert(Sqlite.TABLE_NOTIFICATION_CACHE, null, values);
        }catch (Exception e) {
            last_id =  -1;
            e.printStackTrace();
        }
        return last_id;
    }


    //------- REMOVE  -------

    /***
     * Remove stored notification
     * @return int
     */
    public int remove(Notification notification){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        return db.delete(Sqlite.TABLE_NOTIFICATION_CACHE,  Sqlite.COL_NOTIFICATION_ID + " = \"" + notification.getId() + "\" AND " + Sqlite.COL_INSTANCE + " = \"" + instance + "\" AND " + Sqlite.COL_USER_ID + " = '" + userId+ "'", null);
    }


    public void removeDuplicate(){
        db.execSQL("DELETE FROM "+Sqlite.TABLE_NOTIFICATION_CACHE+" WHERE "+Sqlite.COL_ID+" NOT IN (SELECT MIN("+Sqlite.COL_ID+") FROM "+Sqlite.TABLE_NOTIFICATION_CACHE+" GROUP BY "+Sqlite.COL_NOTIFICATION_ID+","+Sqlite.COL_INSTANCE+")");
    }


    /***
     * Remove stored notifications
     * @return int
     */
    public int remove(Notification notification, String userId, String instance){
        return db.delete(Sqlite.TABLE_NOTIFICATION_CACHE,   Sqlite.COL_NOTIFICATION_ID + " = \"" + notification.getId() + "\" AND " + Sqlite.COL_INSTANCE + " = \"" + instance + "\" AND " + Sqlite.COL_USER_ID + " = '" + userId+ "'", null);
    }

    public int removeAllNotification(){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        return db.delete(Sqlite.TABLE_NOTIFICATION_CACHE,  Sqlite.COL_INSTANCE + " = '" + instance+ "' AND " + Sqlite.COL_USER_ID + " = '" + userId+ "'", null);
    }

    public int removeAll(){
        return db.delete(Sqlite.TABLE_NOTIFICATION_CACHE,  null, null);
    }

    //------- GETTERS  -------

    /**
     * Returns all cached Notification in db
     * @return stored notifications List<Notification>
     */
    public List<Notification> getAllNotifications(){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        try {
            Cursor c = db.query(Sqlite.TABLE_NOTIFICATION_CACHE, null, Sqlite.COL_INSTANCE + " = '" + instance+ "' AND " + Sqlite.COL_USER_ID + " = '" + userId+ "'", null, null, null, Sqlite.COL_CREATED_AT + " DESC", null);
            return cursorToListNotifications(c);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Returns all cached Notification in db
     * @return stored notification List<String>
     */
    public List<String> getAllNotificationsId(){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        try {
            Cursor c = db.query(Sqlite.TABLE_NOTIFICATION_CACHE, new String[]{Sqlite.COL_NOTIFICATION_ID},  Sqlite.COL_INSTANCE + " = '" + instance+ "' AND " + Sqlite.COL_USER_ID + " = '" + userId+ "'", null, null, null, Sqlite.COL_CREATED_AT + " DESC", null);
            return cursorToListNotificationsId(c);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns the smaller date
     * @return Date
     */
    public Date getSmallerDate(){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        try {
            Cursor c = db.query(Sqlite.TABLE_NOTIFICATION_CACHE, null, Sqlite.COL_INSTANCE + " = '" + instance+ "' AND " + Sqlite.COL_USER_ID + " = '" + userId+ "'", null, null, null, Sqlite.COL_CREATED_AT + " ASC", "1");
            //No element found
            if (c.getCount() == 0) {
                c.close();
                return null;
            }
            //Take the first element
            c.moveToFirst();
            String date = c.getString(c.getColumnIndex(Sqlite.COL_CREATED_AT));
            c.close();
            return Helper.stringToDate(context, date);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns the smaller date
     * @return Date
     */
    public Date getGreaterDate(){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        try {
            Cursor c = db.query(Sqlite.TABLE_NOTIFICATION_CACHE, null, Sqlite.COL_INSTANCE + " = '" + instance+ "' AND " + Sqlite.COL_USER_ID + " = '" + userId+ "'", null, null, null, Sqlite.COL_CREATED_AT + " DESC", "1");
            //No element found
            if (c.getCount() == 0) {
                c.close();
                return null;
            }
            //Take the first element
            c.moveToFirst();
            String date = c.getString(c.getColumnIndex(Sqlite.COL_CREATED_AT));
            c.close();
            return Helper.stringToDate(context, date);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns the last id of backup for a use
     * @return Date
     */
    public String getLastNotificationIDCache(){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        try {
            Cursor c = db.query(Sqlite.TABLE_NOTIFICATION_CACHE, null, Sqlite.COL_INSTANCE + " = '" + instance+ "' AND " + Sqlite.COL_USER_ID + " = '" + userId+ "'", null, null, null, Sqlite.COL_NOTIFICATION_ID + " DESC", "1");
            //No element found
            if (c.getCount() == 0) {
                c.close();
                return null;
            }
            //Take the first element
            c.moveToFirst();
            String last_id = c.getString(c.getColumnIndex(Sqlite.COL_NOTIFICATION_ID));
            c.close();
            return last_id;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns the last date of backup for a user
     * @return Date
     */
    public Date getLastNotificationDateCache(String userId, String instance){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        if( userId == null || instance == null) {
            userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
            instance = Helper.getLiveInstance(context);
        }
        try {
            Cursor c = db.query(Sqlite.TABLE_NOTIFICATION_CACHE, null, Sqlite.COL_INSTANCE + " = '" + instance+ "' AND " + Sqlite.COL_USER_ID + " = '" + userId+ "'", null, null, null, Sqlite.COL_CREATED_AT + " DESC", "1");
            //No element found
            if (c.getCount() == 0) {
                c.close();
                return null;
            }
            //Take the first element
            c.moveToFirst();
            Date last_id = Helper.stringToDate(context, c.getString(c.getColumnIndex(Sqlite.COL_CREATED_AT)));
            c.close();
            return last_id;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns a cached notification by id in db
     * @return Notification
     */
    public Notification getNotification(String id){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        try {
            Cursor c = db.query(Sqlite.TABLE_NOTIFICATION_CACHE, null,  Sqlite.COL_NOTIFICATION_ID + " = '" + id + "' AND " + Sqlite.COL_INSTANCE + " = '" + instance +"' AND " + Sqlite.COL_USER_ID + " = '" + userId+ "'", null, null, null, null, null);
            return cursorToStoredNotification(c);
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * Returns a cached notification by id in db
     * @return Notification
     */
    public Notification getNotification(int cacheType, String id, String userId, String instance){
        try {
            Cursor c = db.query(Sqlite.TABLE_NOTIFICATION_CACHE, null,  Sqlite.COL_NOTIFICATION_ID + " = '" + id + "' AND " + Sqlite.COL_INSTANCE + " = '" + instance +"' AND " + Sqlite.COL_USER_ID + " = '" + userId+ "'", null, null, null, null, null);
            return cursorToStoredNotification(c);
        } catch (Exception e) {
            return null;
        }
    }



    /***
     * Method to hydrate notification from database
     * @param c Cursor
     * @return Notification
     */
    private Notification cursorToStoredNotification(Cursor c){
        //No element found
        if (c.getCount() == 0) {
            c.close();
            return null;
        }
        //Take the first element
        c.moveToFirst();
        //New status
        Notification notification = new Notification();
        notification.setId(c.getString(c.getColumnIndex(Sqlite.COL_NOTIFICATION_ID)));
        notification.setAccount(Helper.restoreAccountFromString(c.getString(c.getColumnIndex(Sqlite.COL_ACCOUNT))));
        notification.setCreated_at(Helper.stringToDate(context, c.getString(c.getColumnIndex(Sqlite.COL_CREATED_AT))));
        notification.setType(c.getString(c.getColumnIndex(Sqlite.COL_TYPE)));
        if( c.getString(c.getColumnIndex(Sqlite.COL_STATUS_ID)) != null ){
            String status_id = c.getString(c.getColumnIndex(Sqlite.COL_STATUS_ID));
            Status status =   new StatusCacheDAO(context, db).getStatus(StatusCacheDAO.NOTIFICATION_CACHE, status_id);
            notification.setStatus(status);
        }
        //Close the cursor
        c.close();
        //Cached notification is returned
        return notification;
    }

    /***
     * Method to hydrate cached notification from database
     * @param c Cursor
     * @return List<Notification>
     */
    private List<Notification> cursorToListNotifications(Cursor c){
        //No element found
        if (c.getCount() == 0) {
            c.close();
            return null;
        }
        List<Notification> notifications = new ArrayList<>();
        while (c.moveToNext() ) {
            //Restore cached notification
            Notification notification = new Notification();
            notification.setId(c.getString(c.getColumnIndex(Sqlite.COL_NOTIFICATION_ID)));
            notification.setAccount(Helper.restoreAccountFromString(c.getString(c.getColumnIndex(Sqlite.COL_ACCOUNT))));
            notification.setCreated_at(Helper.stringToDate(context, c.getString(c.getColumnIndex(Sqlite.COL_CREATED_AT))));
            notification.setType(c.getString(c.getColumnIndex(Sqlite.COL_TYPE)));
            if( c.getString(c.getColumnIndex(Sqlite.COL_STATUS_ID)) != null ){
                String status_id = c.getString(c.getColumnIndex(Sqlite.COL_STATUS_ID));
                Status status =   new StatusCacheDAO(context, db).getStatus(StatusCacheDAO.NOTIFICATION_CACHE, status_id);
                notification.setStatus(status);
            }
            notifications.add(notification);
        }
        //Close the cursor
        c.close();
        //notifications list is returned
        return notifications;
    }


    /***
     * Method to get cached notifications ID from database
     * @param c Cursor
     * @return List<String>
     */
    private List<String> cursorToListNotificationsId(Cursor c){
        //No element found
        if (c.getCount() == 0) {
            c.close();
            return null;
        }
        List<String> notificationsId = new ArrayList<>();
        while (c.moveToNext() ) {
            //Restore cached notification
            notificationsId.add(c.getString(c.getColumnIndex(Sqlite.COL_NOTIFICATION_ID)));
        }
        //Close the cursor
        c.close();
        //Notification ids list is returned
        return notificationsId;
    }
}
