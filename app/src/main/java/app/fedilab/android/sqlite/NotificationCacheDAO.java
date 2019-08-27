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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import app.fedilab.android.client.Entities.Charts;
import app.fedilab.android.client.Entities.Notification;
import app.fedilab.android.client.Entities.NotificationCharts;
import app.fedilab.android.client.Entities.StatisticsNotification;
import app.fedilab.android.client.Entities.Status;
import app.fedilab.android.helper.FilterNotifications;
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




    /**
     * Returns all cached notifications in db after filter
     * @return stored notifications List<Notification>
     */
    public List<Notification> getNotificationsFromID(FilterNotifications filterNotifications, String max_id){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        //That the basic selection for all toots
        StringBuilder selection = new StringBuilder( Sqlite.COL_INSTANCE + " = '" + instance + "' AND " + Sqlite.COL_USER_ID + " = '" + userId + "'");
        if( max_id != null)
            selection.append(" AND " + Sqlite.COL_NOTIFICATION_ID + " < '").append(max_id).append("'");
        //BOOST
        if( filterNotifications == null)
            filterNotifications = new FilterNotifications();
        if(filterNotifications.isBoost() || filterNotifications.isFavorite() || filterNotifications.isFollow() ||filterNotifications.isMention() || filterNotifications.isPoll() ){
            selection.append(" AND ( ");
            if (filterNotifications.isBoost() ) {
                selection.append(Sqlite.COL_TYPE + "='reblog' OR ");
            }
            if (filterNotifications.isPoll() ) {
                selection.append(Sqlite.COL_TYPE + "='poll' OR ");
            }
            if (filterNotifications.isFollow() ) {
                selection.append(Sqlite.COL_TYPE + "='follow' OR ");
            }
            if (filterNotifications.isMention() ) {
                selection.append(Sqlite.COL_TYPE + "='mention' OR ");
            }
            if (filterNotifications.isFavorite() ) {
                selection.append(Sqlite.COL_TYPE + "='favourite' OR ");
            }
            String selectionStr = selection.toString().substring(0, selection.toString().length()-3);
            selection = new StringBuilder(selectionStr);
            selection.append(") ");
        }
        if( filterNotifications.getDateIni() != null)
            selection.append(" AND " + Sqlite.COL_CREATED_AT + " >= '").append(filterNotifications.getDateIni()).append("'");

        if( filterNotifications.getDateEnd() != null)
            selection.append(" AND " + Sqlite.COL_CREATED_AT + " <= '").append(filterNotifications.getDateEnd()).append("'");

        try {
            Cursor c = db.query(Sqlite.TABLE_NOTIFICATION_CACHE, null, selection.toString(), null, null, null, Sqlite.COL_CREATED_AT + " DESC", "40");
            return cursorToListNotifications(c);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
    public String getLastNotificationIDCache(String userId, String instance){
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



    public StatisticsNotification getStat(){

        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);

        StatisticsNotification statistics = new StatisticsNotification();

        //Count All
        Cursor mCount = db.rawQuery("select count(*) from " + Sqlite.TABLE_NOTIFICATION_CACHE
                        + " where " + Sqlite.COL_USER_ID + " = '" + userId + "' AND " + Sqlite.COL_INSTANCE + " = '" + instance +"'"
                , null);
        mCount.moveToFirst();
        statistics.setTotal_notification(mCount.getInt(0));
        mCount.close();

        //Count boosts
        mCount = db.rawQuery("select count(*) from " + Sqlite.TABLE_NOTIFICATION_CACHE
                        + " where " + Sqlite.COL_USER_ID + " = '" + userId + "' AND " + Sqlite.COL_INSTANCE + " = '" + instance +"' AND "
                        + Sqlite.COL_TYPE + " = 'reblog'"
                , null);
        mCount.moveToFirst();
        statistics.setNumber_reblog(mCount.getInt(0));
        mCount.close();

        //Count favorites
        mCount = db.rawQuery("select count(*) from " + Sqlite.TABLE_NOTIFICATION_CACHE
                        + " where " + Sqlite.COL_USER_ID + " = '" + userId + "' AND " + Sqlite.COL_INSTANCE + " = '" + instance +"' AND "
                        + Sqlite.COL_TYPE + " = 'favourite'"
                , null);
        mCount.moveToFirst();
        statistics.setNumber_favourite(mCount.getInt(0));
        mCount.close();


        //Count mentions
        mCount = db.rawQuery("select count(*) from " + Sqlite.TABLE_NOTIFICATION_CACHE
                        + " where " + Sqlite.COL_USER_ID + " = '" + userId + "' AND " + Sqlite.COL_INSTANCE + " = '" + instance +"' AND "
                        + Sqlite.COL_TYPE + " = 'mention'"
                , null);
        mCount.moveToFirst();
        statistics.setNumber_mentions(mCount.getInt(0));
        mCount.close();


        //Count follows
        mCount = db.rawQuery("select count(*) from " + Sqlite.TABLE_NOTIFICATION_CACHE
                        + " where " + Sqlite.COL_USER_ID + " = '" + userId + "' AND " + Sqlite.COL_INSTANCE + " = '" + instance +"' AND "
                        + Sqlite.COL_TYPE + " = 'follow'"
                , null);
        mCount.moveToFirst();
        statistics.setNumber_follow(mCount.getInt(0));
        mCount.close();


        //Count polls
        mCount = db.rawQuery("select count(*) from " + Sqlite.TABLE_NOTIFICATION_CACHE
                        + " where " + Sqlite.COL_USER_ID + " = '" + userId + "' AND " + Sqlite.COL_INSTANCE + " = '" + instance +"' AND "
                        + Sqlite.COL_TYPE + " = 'poll'"
                , null);
        mCount.moveToFirst();
        statistics.setNumber_poll(mCount.getInt(0));
        mCount.close();



        statistics.setFirstTootDate(getSmallerDate());
        statistics.setLastTootDate(getGreaterDate());

        long days = 1;
        if( statistics.getLastTootDate() != null && statistics.getFirstTootDate() != null) {
            long diff = statistics.getLastTootDate().getTime() - statistics.getFirstTootDate().getTime();
            days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        }
        statistics.setFrequency((float)statistics.getTotal_notification()/days);

        return statistics;
    }


    public NotificationCharts getCharts(Date dateIni, Date dateEnd){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        NotificationCharts charts = new NotificationCharts();

        Calendar start = Calendar.getInstance();
        start.setTime(dateIni);
        start.set(Calendar.HOUR_OF_DAY,0);
        start.set(Calendar.MINUTE,0);
        start.set(Calendar.SECOND,0);

        Calendar end = Calendar.getInstance();
        end.setTime(dateEnd);
        end.set(Calendar.HOUR_OF_DAY,23);
        end.set(Calendar.MINUTE,59);
        end.set(Calendar.SECOND,59);

        StringBuilder selection = new StringBuilder(Sqlite.COL_INSTANCE + " = '" + instance + "' AND " + Sqlite.COL_USER_ID + " = '" + userId + "'");
        selection.append(" AND " + Sqlite.COL_CREATED_AT + " >= '").append(Helper.dateToString(start.getTime())).append("'");
        selection.append(" AND " + Sqlite.COL_CREATED_AT + " <= '").append(Helper.dateToString(end.getTime())).append("'");

        List<Notification> data = new ArrayList<>();
        try {
            Cursor c = db.query(Sqlite.TABLE_NOTIFICATION_CACHE, null, selection.toString(), null, null, null, Sqlite.COL_CREATED_AT + " ASC");
            data = cursorToListNotifications(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<String> xLabel = new ArrayList<>();

        charts.setFavourites(new LinkedHashMap<>());
        charts.setFollows(new LinkedHashMap<>());
        charts.setMentions(new LinkedHashMap<>());
        charts.setPolls(new LinkedHashMap<>());
        charts.setReblogs(new LinkedHashMap<>());
        if( data != null) {
            for (Notification notification : data) {
                Calendar tempdate = Calendar.getInstance();
                tempdate.setTime(notification.getCreated_at());
                tempdate.set(Calendar.HOUR_OF_DAY, 0);
                tempdate.set(Calendar.MINUTE, 0);
                tempdate.set(Calendar.SECOND, 0);
                long date = tempdate.getTimeInMillis();
                if (notification.getType().equals("reblog")) {
                    if(charts.getReblogs().containsKey(date)){
                        charts.getReblogs().put(date,(charts.getReblogs().get(date)+1));
                    }else{
                        charts.getReblogs().put(date,1);
                    }

                } else if (notification.getType().equals("favourite")) {
                    if(charts.getFavourites().containsKey(date)){
                        charts.getFavourites().put(date,(charts.getFavourites().get(date)+1));
                    }else{
                        charts.getFavourites().put(date,1);
                    }
                } else if (notification.getType().equals("follow")) {
                    if(charts.getFollows().containsKey(date)){
                        charts.getFollows().put(date,(charts.getFollows().get(date)+1));
                    }else{
                        charts.getFollows().put(date,1);
                    }
                } else if (notification.getType().equals("mention")) {
                    if(charts.getMentions().containsKey(date)){
                        charts.getMentions().put(date,(charts.getMentions().get(date)+1));
                    }else{
                        charts.getMentions().put(date,1);
                    }
                }else if (notification.getType().equals("poll")) {
                    if(charts.getPolls().containsKey(date)){
                        charts.getPolls().put(date,(charts.getPolls().get(date)+1));
                    }else{
                        charts.getPolls().put(date,1);
                    }
                }
            }
        }
        charts.setxLabels(xLabel);
        return charts;
    }


    public NotificationCharts getChartsEvolution(Date dateIni, Date dateEnd){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        NotificationCharts charts = new NotificationCharts();

        Calendar start = Calendar.getInstance();
        start.setTime(dateIni);
        start.set(Calendar.HOUR_OF_DAY,0);
        start.set(Calendar.MINUTE,0);
        start.set(Calendar.SECOND,0);

        Calendar end = Calendar.getInstance();
        end.setTime(dateEnd);
        end.set(Calendar.HOUR_OF_DAY,23);
        end.set(Calendar.MINUTE,59);
        end.set(Calendar.SECOND,59);

        long msDiff = end.getTimeInMillis() - start.getTimeInMillis();
        long daysDiff = TimeUnit.MILLISECONDS.toDays(msDiff);
        List<String> xLabel = new ArrayList<>();

        charts.setFavourites(new LinkedHashMap<>());
        charts.setFollows(new LinkedHashMap<>());
        charts.setMentions(new LinkedHashMap<>());
        charts.setPolls(new LinkedHashMap<>());
        charts.setReblogs(new LinkedHashMap<>());
        int reblogCount = 0;
        int favCount = 0;
       // int pollCount = 0;
        int followCount = 0;
        int mentionCount = 0;
        Date smallestDate = getSmallerDate();
        int minYVal = 0;
        StringBuilder selection = new StringBuilder(Sqlite.COL_INSTANCE + " = '" + instance + "' AND " + Sqlite.COL_USER_ID + " = '" + userId + "'");
        selection.append(" AND " + Sqlite.COL_CREATED_AT + " >= '").append(Helper.dateToString(smallestDate)).append("'");
        selection.append(" AND " + Sqlite.COL_CREATED_AT + " <= '").append(Helper.dateToString(start.getTime())).append("'");
        try {
            Cursor mCount = db.rawQuery("select count(*) from " + Sqlite.TABLE_NOTIFICATION_CACHE
                            + " where " + selection.toString() + " AND "
                            + Sqlite.COL_TYPE + " = 'reblog'"
                    , null);
            mCount.moveToFirst();
            reblogCount = mCount.getInt(0);
            mCount.close();
            minYVal = reblogCount;

            mCount = db.rawQuery("select count(*) from " + Sqlite.TABLE_NOTIFICATION_CACHE
                            + " where " + selection.toString() + " AND "
                            + Sqlite.COL_TYPE + " = 'favourite'"
                    , null);
            mCount.moveToFirst();
            favCount = mCount.getInt(0);
            mCount.close();
            if( favCount < minYVal){
                minYVal = favCount;
            }

            mCount = db.rawQuery("select count(*) from " + Sqlite.TABLE_NOTIFICATION_CACHE
                            + " where " + selection.toString() + " AND "
                            + Sqlite.COL_TYPE + " = 'mention'"
                    , null);
            mCount.moveToFirst();
            mentionCount = mCount.getInt(0);
            mCount.close();
            if( mentionCount < minYVal){
                minYVal = mentionCount;
            }

            mCount = db.rawQuery("select count(*) from " + Sqlite.TABLE_NOTIFICATION_CACHE
                            + " where " + selection.toString() + " AND "
                            + Sqlite.COL_TYPE + " = 'follow'"
                    , null);
            mCount.moveToFirst();
            mCount.moveToFirst();
            followCount = mCount.getInt(0);
            mCount.close();
            if( followCount < minYVal){
                minYVal = followCount;
            }

         /*   mCount = db.rawQuery("select count(*) from " + Sqlite.TABLE_NOTIFICATION_CACHE
                            + " where " + selection.toString() + " AND "
                            + Sqlite.COL_TYPE + " = 'poll'"
                    , null);
            mCount.moveToFirst();
            mCount.moveToFirst();
            pollCount = mCount.getInt(0);
            mCount.close();
            if( pollCount < minYVal){
                minYVal = pollCount;
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (Date date = start.getTime(); start.before(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
            Calendar startTmp = Calendar.getInstance();
            startTmp.setTime(date);
            startTmp.set(Calendar.HOUR_OF_DAY,0);
            startTmp.set(Calendar.MINUTE,0);
            startTmp.set(Calendar.SECOND,0);

            Calendar endTmp = Calendar.getInstance();
            endTmp.setTime(date);
            endTmp.set(Calendar.HOUR_OF_DAY,23);
            endTmp.set(Calendar.MINUTE,59);
            endTmp.set(Calendar.SECOND,59);


            selection = new StringBuilder(Sqlite.COL_INSTANCE + " = '" + instance + "' AND " + Sqlite.COL_USER_ID + " = '" + userId + "'");
            selection.append(" AND " + Sqlite.COL_CREATED_AT + " >= '").append(Helper.dateToString(startTmp.getTime())).append("'");
            selection.append(" AND " + Sqlite.COL_CREATED_AT + " <= '").append(Helper.dateToString(endTmp.getTime())).append("'");
            try {
                Cursor mCount = db.rawQuery("select count(*) from " + Sqlite.TABLE_NOTIFICATION_CACHE
                                + " where " + selection.toString() + " AND "
                                + Sqlite.COL_TYPE + " = 'reblog'"
                        , null);
                mCount.moveToFirst();
                reblogCount += mCount.getInt(0);
                charts.getReblogs().put(date.getTime(), reblogCount);
                mCount.close();


                mCount = db.rawQuery("select count(*) from " + Sqlite.TABLE_NOTIFICATION_CACHE
                                + " where " + selection.toString() + " AND "
                                + Sqlite.COL_TYPE + " = 'favourite'"
                        , null);
                mCount.moveToFirst();
                favCount += mCount.getInt(0);
                charts.getFavourites().put(date.getTime(),favCount);
                mCount.close();

                mCount = db.rawQuery("select count(*) from " + Sqlite.TABLE_NOTIFICATION_CACHE
                                + " where " + selection.toString() + " AND "
                                + Sqlite.COL_TYPE + " = 'mention'"
                        , null);
                mCount.moveToFirst();
                mentionCount += mCount.getInt(0);
                charts.getMentions().put(date.getTime(), mentionCount);
                mCount.close();


                mCount = db.rawQuery("select count(*) from " + Sqlite.TABLE_NOTIFICATION_CACHE
                                + " where " + selection.toString() + " AND "
                                + Sqlite.COL_TYPE + " = 'follow'"
                        , null);
                mCount.moveToFirst();
                followCount += mCount.getInt(0);
                charts.getFollows().put(date.getTime(),followCount);
                mCount.close();

               /* mCount = db.rawQuery("select count(*) from " + Sqlite.TABLE_NOTIFICATION_CACHE
                                + " where " + selection.toString() + " AND "
                                + Sqlite.COL_TYPE + " = 'poll'"
                        , null);
                mCount.moveToFirst();
                pollCount += mCount.getInt(0);
                charts.getPolls().put(date.getTime(), pollCount);
                mCount.close();*/

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        charts.setMinYVal(minYVal);
        charts.setxLabels(xLabel);
        return charts;
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
