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
import java.util.List;

import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.helper.Helper;


/**
 * Created by Thomas on 15/02/2018.
 * Manage Status in cache
 */
public class StatusCacheDAO {

    private SQLiteDatabase db;
    public Context context;

    //Type of cache
    public static int BOOKMARK_CACHE = 0;
    public static int ARCHIVE_CACHE = 1;

    public StatusCacheDAO(Context context, SQLiteDatabase db) {
        //Creation of the DB with tables
        this.context = context;
        this.db = db;
    }


    //------- INSERTIONS  -------

    /**
     * Insert a status in database
     * @param cacheType int cache type
     * @param status Status
     * @return boolean
     */
    public long insertStatus(int cacheType, Status status) {
        ContentValues values = new ContentValues();
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        values.put(Sqlite.COL_USER_ID, userId);
        values.put(Sqlite.COL_CACHED_ACTION, cacheType);
        values.put(Sqlite.COL_INSTANCE, instance);
        values.put(Sqlite.COL_STATUS_ID, status.getId());
        values.put(Sqlite.COL_URI, status.getUri());
        values.put(Sqlite.COL_URL, status.getUrl());
        values.put(Sqlite.COL_ACCOUNT, Helper.accountToStringStorage(status.getAccount()));
        values.put(Sqlite.COL_IN_REPLY_TO_ID, status.getIn_reply_to_id());
        values.put(Sqlite.COL_IN_REPLY_TO_ACCOUNT_ID, status.getIn_reply_to_account_id());
        values.put(Sqlite.COL_REBLOG, status.getReblog()!=null?Helper.statusToStringStorage(status.getReblog()):null);
        values.put(Sqlite.COL_CONTENT, status.getContent());
        values.put(Sqlite.COL_EMOJIS, status.getEmojis()!=null?Helper.emojisToStringStorage(status.getEmojis()):null);
        values.put(Sqlite.COL_REBLOGS_COUNT, status.getReblogs_count());
        values.put(Sqlite.COL_FAVOURITES_COUNT, status.getFavourites_count());
        values.put(Sqlite.COL_REBLOGGED, status.isReblogged());
        values.put(Sqlite.COL_FAVOURITED, status.isFavourited());
        values.put(Sqlite.COL_MUTED, status.isMuted());
        values.put(Sqlite.COL_CREATED_AT, Helper.dateToString(context, status.getCreated_at()));
        values.put(Sqlite.COL_SENSITIVE, status.isSensitive());
        values.put(Sqlite.COL_SPOILER_TEXT, status.getSpoiler_text());
        values.put(Sqlite.COL_VISIBILITY, status.getVisibility());
        values.put(Sqlite.COL_MEDIA_ATTACHMENTS, status.getMedia_attachments()!=null?Helper.attachmentToStringStorage(status.getMedia_attachments()):null);
        values.put(Sqlite.COL_MENTIONS, status.getMentions()!=null?Helper.mentionToStringStorage(status.getMentions()):null);
        values.put(Sqlite.COL_TAGS, status.getTags()!=null?Helper.tagToStringStorage(status.getTags()):null);
        values.put(Sqlite.COL_APPLICATION, status.getApplication()!=null?Helper.applicationToStringStorage(status.getApplication()):null);
        values.put(Sqlite.COL_LANGUAGE, status.getLanguage());
        values.put(Sqlite.COL_PINNED, status.isPinned());

        //Inserts cached status
        long last_id;
        try{
            last_id = db.insert(Sqlite.TABLE_STATUSES_CACHE, null, values);
        }catch (Exception e) {
            last_id =  -1;
        }
        return last_id;
    }

    //------- UPDATES  -------

    /**
     * Update a Status cached in database
     * @param status Status
     * @return boolean
     */
    public int updateStatus(int cacheType, Status status ) {
        ContentValues values = new ContentValues();
        String instance = Helper.getLiveInstance(context);
        values.put(Sqlite.COL_REBLOGS_COUNT, status.getReblogs_count());
        values.put(Sqlite.COL_FAVOURITES_COUNT, status.getFavourites_count());
        values.put(Sqlite.COL_REBLOGGED, status.isReblogged());
        values.put(Sqlite.COL_FAVOURITED, status.isFavourited());
        values.put(Sqlite.COL_MUTED, status.isMuted());
        values.put(Sqlite.COL_PINNED, status.isPinned());
        return db.update(Sqlite.TABLE_STATUSES_CACHE,
                values, Sqlite.COL_STATUS_ID + " =  ? AND " + Sqlite.COL_INSTANCE + " =  ? " + Sqlite.COL_CACHED_ACTION + " = ?",
                new String[]{String.valueOf(status.getId()), instance, String.valueOf(cacheType)});
    }


    //------- REMOVE  -------

    /***
     * Remove stored status
     * @return int
     */
    public int remove(int cacheType, Status status){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        return db.delete(Sqlite.TABLE_STATUSES_CACHE,  Sqlite.COL_CACHED_ACTION + " = \""+ cacheType +"\" AND " + Sqlite.COL_STATUS_ID + " = \"" + status.getId() + "\" AND " + Sqlite.COL_INSTANCE + " = \"" + instance + "\" AND " + Sqlite.COL_USER_ID + " = '" + userId+ "'", null);
    }

    public int removeAllStatus(int cacheType){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        return db.delete(Sqlite.TABLE_STATUSES_CACHE,  Sqlite.COL_CACHED_ACTION + " = \""+ cacheType +"\" AND " + Sqlite.COL_INSTANCE + " = '" + instance+ "' AND " + Sqlite.COL_USER_ID + " = '" + userId+ "'", null);
    }

    //------- GETTERS  -------

    /**
     * Returns all cached Statuses in db depending of their cache type
     * @return stored status List<StoredStatus>
     */
    public List<Status> getAllStatus(int cacheType){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        try {
            Cursor c = db.query(Sqlite.TABLE_STATUSES_CACHE, null, Sqlite.COL_CACHED_ACTION + " = '" + cacheType+ "' AND " + Sqlite.COL_INSTANCE + " = '" + instance+ "' AND " + Sqlite.COL_USER_ID + " = '" + userId+ "'", null, null, null, Sqlite.COL_CREATED_AT + " DESC", null);
            return cursorToListStatuses(c);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns a cached status by id in db
     * @return stored status StoredStatus
     */
    public Status getStatus(String id){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        try {
            Cursor c = db.query(Sqlite.TABLE_STATUSES_CACHE, null, Sqlite.COL_STATUS_ID + " = '" + id + "' AND " + Sqlite.COL_INSTANCE + " = '" + instance +"' AND " + Sqlite.COL_USER_ID + " = '" + userId+ "'", null, null, null, null, null);
            return cursorToStoredStatus(c);
        } catch (Exception e) {
            return null;
        }
    }


    /***
     * Method to hydrate statuses from database
     * @param c Cursor
     * @return Status
     */
    private Status cursorToStoredStatus(Cursor c){
        //No element found
        if (c.getCount() == 0)
            return null;
        //Take the first element
        c.moveToFirst();
        //New status
        Status status = new Status();
        status.setId(c.getString(c.getColumnIndex(Sqlite.COL_STATUS_ID)));
        status.setUri(c.getString(c.getColumnIndex(Sqlite.COL_URI)));
        status.setUrl(c.getString(c.getColumnIndex(Sqlite.COL_URL)));
        status.setAccount(Helper.restoreAccountFromString(c.getString(c.getColumnIndex(Sqlite.COL_ACCOUNT))));
        status.setIn_reply_to_id(c.getString(c.getColumnIndex(Sqlite.COL_IN_REPLY_TO_ID)));
        status.setIn_reply_to_account_id(c.getString(c.getColumnIndex(Sqlite.COL_IN_REPLY_TO_ACCOUNT_ID)));
        status.setReblog(Helper.restoreStatusFromString(c.getString(c.getColumnIndex(Sqlite.COL_REBLOG))));
        status.setContent(c.getString(c.getColumnIndex(Sqlite.COL_CONTENT)));
        status.setCreated_at(Helper.stringToDate(context, c.getString(c.getColumnIndex(Sqlite.COL_CREATED_AT))));
        status.setEmojis(Helper.restoreEmojisFromString(c.getString(c.getColumnIndex(Sqlite.COL_EMOJIS))));
        status.setReblogs_count(c.getInt(c.getColumnIndex(Sqlite.COL_REBLOGS_COUNT)));
        status.setFavourites_count(c.getInt(c.getColumnIndex(Sqlite.COL_FAVOURITES_COUNT)));
        status.setReblogged(c.getInt(c.getColumnIndex(Sqlite.COL_REBLOGGED))==1);
        status.setFavourited(c.getInt(c.getColumnIndex(Sqlite.COL_FAVOURITED))==1);
        status.setMuted(c.getInt(c.getColumnIndex(Sqlite.COL_MUTED))==1);
        status.setSensitive(c.getInt(c.getColumnIndex(Sqlite.COL_SENSITIVE))==1);
        status.setPinned(c.getInt(c.getColumnIndex(Sqlite.COL_PINNED))==1);
        status.setSpoiler_text(c.getString(c.getColumnIndex(Sqlite.COL_SPOILER_TEXT)));
        status.setVisibility(c.getString(c.getColumnIndex(Sqlite.COL_VISIBILITY)));
        status.setMedia_attachments(Helper.restoreAttachmentFromString(c.getString(c.getColumnIndex(Sqlite.COL_MEDIA_ATTACHMENTS))));
        status.setMentions(Helper.restoreMentionFromString(c.getString(c.getColumnIndex(Sqlite.COL_MENTIONS))));
        status.setTags(Helper.restoreTagFromString(c.getString(c.getColumnIndex(Sqlite.COL_TAGS))));
        status.setApplication(Helper.restoreApplicationFromString(c.getString(c.getColumnIndex(Sqlite.COL_APPLICATION))));
        status.setLanguage(c.getString(c.getColumnIndex(Sqlite.COL_LANGUAGE)));
        //Close the cursor
        c.close();
        //Cached status is returned
        return status;
    }

    /***
     * Method to hydrate cached statuses from database
     * @param c Cursor
     * @return List<Status>
     */
    private List<Status> cursorToListStatuses(Cursor c){
        //No element found
        if (c.getCount() == 0)
            return null;
        List<Status> statuses = new ArrayList<>();
        while (c.moveToNext() ) {
            //Restore cached status
            Status status = new Status();
            status.setId(c.getString(c.getColumnIndex(Sqlite.COL_STATUS_ID)));
            status.setUri(c.getString(c.getColumnIndex(Sqlite.COL_URI)));
            status.setUrl(c.getString(c.getColumnIndex(Sqlite.COL_URL)));
            status.setAccount(Helper.restoreAccountFromString(c.getString(c.getColumnIndex(Sqlite.COL_ACCOUNT))));
            status.setIn_reply_to_id(c.getString(c.getColumnIndex(Sqlite.COL_IN_REPLY_TO_ID)));
            status.setIn_reply_to_account_id(c.getString(c.getColumnIndex(Sqlite.COL_IN_REPLY_TO_ACCOUNT_ID)));
            status.setReblog(Helper.restoreStatusFromString(c.getString(c.getColumnIndex(Sqlite.COL_REBLOG))));
            status.setContent(c.getString(c.getColumnIndex(Sqlite.COL_CONTENT)));
            status.setCreated_at(Helper.stringToDate(context, c.getString(c.getColumnIndex(Sqlite.COL_CREATED_AT))));
            status.setEmojis(Helper.restoreEmojisFromString(c.getString(c.getColumnIndex(Sqlite.COL_EMOJIS))));
            status.setReblogs_count(c.getInt(c.getColumnIndex(Sqlite.COL_REBLOGS_COUNT)));
            status.setFavourites_count(c.getInt(c.getColumnIndex(Sqlite.COL_FAVOURITES_COUNT)));
            status.setReblogged(c.getInt(c.getColumnIndex(Sqlite.COL_REBLOGGED))==1);
            status.setFavourited(c.getInt(c.getColumnIndex(Sqlite.COL_FAVOURITED))==1);
            status.setMuted(c.getInt(c.getColumnIndex(Sqlite.COL_MUTED))==1);
            status.setSensitive(c.getInt(c.getColumnIndex(Sqlite.COL_SENSITIVE))==1);
            status.setPinned(c.getInt(c.getColumnIndex(Sqlite.COL_PINNED))==1);
            status.setSpoiler_text(c.getString(c.getColumnIndex(Sqlite.COL_SPOILER_TEXT)));
            status.setVisibility(c.getString(c.getColumnIndex(Sqlite.COL_VISIBILITY)));
            status.setMedia_attachments(Helper.restoreAttachmentFromString(c.getString(c.getColumnIndex(Sqlite.COL_MEDIA_ATTACHMENTS))));
            status.setMentions(Helper.restoreMentionFromString(c.getString(c.getColumnIndex(Sqlite.COL_MENTIONS))));
            status.setTags(Helper.restoreTagFromString(c.getString(c.getColumnIndex(Sqlite.COL_TAGS))));
            status.setApplication(Helper.restoreApplicationFromString(c.getString(c.getColumnIndex(Sqlite.COL_APPLICATION))));
            status.setLanguage(c.getString(c.getColumnIndex(Sqlite.COL_LANGUAGE)));
            statuses.add(status);
        }
        //Close the cursor
        c.close();
        //Statuses list is returned
        return statuses;
    }
}
