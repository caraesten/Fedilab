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

package fr.gouv.etalab.mastodon.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Thomas on 23/04/2017.
 * Manage the  DataBase
 */

public class Sqlite extends SQLiteOpenHelper {

    public static final int DB_VERSION = 8;
    public static final String DB_NAME = "mastodon_etalab_db";
    public static SQLiteDatabase db;
    private static Sqlite sInstance;

    /***
     * List of tables to manage users and data
     */
    //Table of owned accounts
    static final String TABLE_USER_ACCOUNT = "USER_ACCOUNT";
    //Table of stored status
    static final String TABLE_STATUSES_STORED = "STATUSES_STORED";
    //Table for custom emoji
    static final String TABLE_CUSTOM_EMOJI = "CUSTOM_EMOJI";
    //Table for search
    static final String TABLE_SEARCH = "SEARCH";

    //Table for temp muting
    static final String TABLE_TEMP_MUTE = "TEMP_MUTE";

    static final String COL_USER_ID = "USER_ID";
    static final String COL_USERNAME = "USERNAME";
    static final String COL_ACCT = "ACCT";
    static final String COL_DISPLAYED_NAME = "DISPLAYED_NAME";
    static final String COL_LOCKED = "LOCKED";
    static final String COL_CREATED_AT = "CREATED_AT";
    static final String COL_FOLLOWERS_COUNT = "FOLLOWERS_COUNT";
    static final String COL_FOLLOWING_COUNT = "FOLLOWING_COUNT";
    static final String COL_STATUSES_COUNT = "STATUSES_COUNT";
    static final String COL_NOTE = "NOTE";
    static final String COL_URL = "URL";
    static final String COL_AVATAR = "AVATAR";
    static final String COL_AVATAR_STATIC = "AVATAR_STATIC";
    static final String COL_HEADER = "HEADER";
    static final String COL_HEADER_STATIC = "HEADER_STATIC";
    static final String COL_INSTANCE = "INSTANCE";
    static final String COL_OAUTHTOKEN = "OAUTH_TOKEN";



    private static final String CREATE_TABLE_USER_ACCOUNT = "CREATE TABLE " + TABLE_USER_ACCOUNT + " ("
            + COL_USER_ID + " TEXT PRIMARY KEY, " + COL_USERNAME + " TEXT NOT NULL, " + COL_ACCT + " TEXT NOT NULL, "
            + COL_DISPLAYED_NAME + " TEXT NOT NULL, " + COL_LOCKED + " INTEGER NOT NULL, "
            + COL_FOLLOWERS_COUNT + " INTEGER NOT NULL, " + COL_FOLLOWING_COUNT + " INTEGER NOT NULL, " + COL_STATUSES_COUNT + " INTEGER NOT NULL, "
            + COL_NOTE + " TEXT NOT NULL, "+ COL_URL + " TEXT NOT NULL, "
            + COL_AVATAR + " TEXT NOT NULL, "+ COL_AVATAR_STATIC + " TEXT NOT NULL, "
            + COL_HEADER + " TEXT NOT NULL, "+ COL_HEADER_STATIC + " TEXT NOT NULL, "
            + COL_INSTANCE + " TEXT NOT NULL, " + COL_OAUTHTOKEN + " TEXT NOT NULL, " + COL_CREATED_AT + " TEXT NOT NULL)";


    static final String COL_ID = "ID";
    static final String COL_STATUS_SERIALIZED = "STATUS_SERIALIZED";
    static final String COL_STATUS_REPLY_SERIALIZED = "STATUS_REPLY_SERIALIZED";
    static final String COL_DATE_CREATION = "DATE_CREATION";
    static final String COL_IS_SCHEDULED = "IS_SCHEDULED";
    static final String COL_DATE_SCHEDULED = "DATE_SCHEDULED";
    static final String COL_SENT = "SENT";
    static final String COL_DATE_SENT = "DATE_SENT";

    private static final String CREATE_TABLE_STATUSES_STORED = "CREATE TABLE " + TABLE_STATUSES_STORED + " ("
            + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_USER_ID + " TEXT NOT NULL, " + COL_INSTANCE + " TEXT NOT NULL, "
            + COL_STATUS_SERIALIZED + " TEXT NOT NULL, " + COL_STATUS_REPLY_SERIALIZED + " TEXT, " + COL_DATE_CREATION + " TEXT NOT NULL, "
            + COL_IS_SCHEDULED + " INTEGER NOT NULL, " + COL_DATE_SCHEDULED + " TEXT, "
            + COL_SENT + " INTEGER NOT NULL, " + COL_DATE_SENT + " TEXT)";


    static final String COL_SHORTCODE = "SHORTCODE";
    static final String COL_URL_STATIC = "URL_STATIC";
    private final String CREATE_TABLE_CUSTOM_EMOJI = "CREATE TABLE " + TABLE_CUSTOM_EMOJI + " ("
            + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_SHORTCODE + " TEXT NOT NULL, " + COL_INSTANCE + " TEXT NOT NULL, "
            + COL_URL + " TEXT NOT NULL, " + COL_URL_STATIC + " TEXT NOT NULL, "  + COL_DATE_CREATION + " TEXT NOT NULL)";


    static final String COL_KEYWORDS = "KEYWORDS";
    private final String CREATE_TABLE_SEARCH = "CREATE TABLE " + TABLE_SEARCH + " ("
            + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_KEYWORDS + " TEXT NOT NULL, " + COL_USER_ID + " TEXT NOT NULL, " + COL_DATE_CREATION + " TEXT NOT NULL)";

    static final String COL_TARGETED_USER_ID = "TARGETED_USER_ID";
    static final String COL_DATE_END = "DATE_END";
    private final String CREATE_TABLE_TEMP_MUTE = "CREATE TABLE " + TABLE_TEMP_MUTE + " ("
            + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_ACCT + " TEXT NOT NULL, " + COL_INSTANCE + " TEXT NOT NULL, " + COL_TARGETED_USER_ID + " TEXT NOT NULL, " + COL_DATE_CREATION + " TEXT NOT NULL, " + COL_DATE_END + " TEXT NOT NULL)";


    public Sqlite(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    public static synchronized Sqlite getInstance(Context context, String name, SQLiteDatabase.CursorFactory factory, int version)
    {
        if (sInstance == null) {
            sInstance = new Sqlite(context, name, factory, version);
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USER_ACCOUNT);
        db.execSQL(CREATE_TABLE_STATUSES_STORED);
        db.execSQL(CREATE_TABLE_CUSTOM_EMOJI);
        db.execSQL(CREATE_TABLE_SEARCH);
        db.execSQL(CREATE_TABLE_TEMP_MUTE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
                db.execSQL(CREATE_TABLE_STATUSES_STORED);
            case 2:
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATUSES_STORED);
                db.execSQL(CREATE_TABLE_STATUSES_STORED);
            case 3:
                db.execSQL(CREATE_TABLE_CUSTOM_EMOJI);
            case 4:
                db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CUSTOM_EMOJI + " ("
                        + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + COL_SHORTCODE + " TEXT NOT NULL, " + COL_INSTANCE + " TEXT NOT NULL, "
                        + COL_URL + " TEXT NOT NULL, " + COL_URL_STATIC + " TEXT NOT NULL, "  + COL_DATE_CREATION + " TEXT NOT NULL)");
            case 5:
                db.execSQL("delete from "+ TABLE_CUSTOM_EMOJI); //Reset table due to bugs
            case 6:
                db.execSQL(CREATE_TABLE_SEARCH);
            case 7:
                db.execSQL(CREATE_TABLE_TEMP_MUTE);
            default:
                break;
        }
    }

    public SQLiteDatabase open(){
        //opened with write access
        db = getWritableDatabase();
        return db;
    }

    public void close(){
        //Close the db
        if( db != null && db.isOpen() ) {
            db.close();
        }
    }
}
