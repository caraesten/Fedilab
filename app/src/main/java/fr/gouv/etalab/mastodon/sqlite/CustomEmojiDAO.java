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
import fr.gouv.etalab.mastodon.client.Entities.Emojis;
import fr.gouv.etalab.mastodon.helper.Helper;


/**
 * Created by Thomas on 01/11/2017.
 * Manage custom emoji in DB
 */
public class CustomEmojiDAO {

    private SQLiteDatabase db;
    public Context context;


    public CustomEmojiDAO(Context context, SQLiteDatabase db) {
        //Creation of the DB with tables
        this.context = context;
        this.db = db;
    }


    //------- INSERTIONS  -------

    /**
     * Insert an emoji in database
     * @param emoji Emoji
     */
    public void insertEmoji(Emojis emoji) {
        ContentValues values = new ContentValues();

        String instance = Helper.getLiveInstance(context);

        values.put(Sqlite.COL_SHORTCODE, emoji.getShortcode());
        values.put(Sqlite.COL_INSTANCE, instance);
        values.put(Sqlite.COL_URL, emoji.getUrl());
        values.put(Sqlite.COL_URL_STATIC, emoji.getStatic_url());
        values.put(Sqlite.COL_DATE_CREATION, Helper.dateToString(context, new Date()));
        //Inserts emoji
        long last_id;
        try{
            last_id = db.insert(Sqlite.TABLE_CUSTOM_EMOJI, null, values);
        }catch (Exception e) {
            last_id =  -1;
        }
    }

    //------- UPDATES  -------

    /**
     * Update an emoji in database
     * @param emoji Emojis
     */
    public void updateEmoji(Emojis emoji ) {
        ContentValues values = new ContentValues();
        String instance = Helper.getLiveInstance(context);
        values.put(Sqlite.COL_URL, emoji.getUrl());
        values.put(Sqlite.COL_URL_STATIC, emoji.getStatic_url());
        values.put(Sqlite.COL_DATE_CREATION, Helper.dateToString(context, new Date()));
        db.update(Sqlite.TABLE_CUSTOM_EMOJI,
                values, Sqlite.COL_SHORTCODE + " =  ? AND " + Sqlite.COL_INSTANCE + " =  ? ",
                new String[]{emoji.getShortcode(), instance});
    }

    //------- REMOVE  -------

    /***
     * Remove emoji by id
     * @return int
     */
    public int remove(Emojis emoji){
        String instance = Helper.getLiveInstance(context);
        return db.delete(Sqlite.TABLE_CUSTOM_EMOJI,  Sqlite.COL_SHORTCODE + " = \"" + emoji.getShortcode() + "\" AND " + Sqlite.COL_INSTANCE + " = \"" + instance+ "\"", null);
    }

    //------- GETTERS  -------

    /**
     * Returns all emojis in db for an instance
     * @return emojis List<Emojis>
     */
    public List<Emojis> getAllEmojis(){
        String instance = Helper.getLiveInstance(context);
        try {
            Cursor c = db.query(Sqlite.TABLE_CUSTOM_EMOJI, null, Sqlite.COL_INSTANCE + " = '" + instance+ "'", null, null, null, Sqlite.COL_SHORTCODE + " ASC", null);
            return cursorToListEmojis(c);
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * Returns an emoji by its shortcode in db
     * @return emoji Emojis
     */
    public Emojis getEmoji(String shortCode){
        try {
            String instance = Helper.getLiveInstance(context);
            Cursor c = db.query(Sqlite.TABLE_CUSTOM_EMOJI, null, Sqlite.COL_SHORTCODE + " = \"" + shortCode + "\" AND " + Sqlite.COL_INSTANCE + " = \"" + instance+ "\"", null, null, null, null, null);
            return cursorToEmoji(c);
        } catch (Exception e) {
            return null;
        }
    }


    /***
     * Method to hydrate emoji from database
     * @param c Cursor
     * @return Emojis
     */
    private Emojis cursorToEmoji(Cursor c){
        //No element found
        if (c.getCount() == 0)
            return null;
        //Take the first element
        c.moveToFirst();
        //New user
        Emojis emoji = new Emojis();
        emoji.setShortcode(c.getString(c.getColumnIndex(Sqlite.COL_SHORTCODE)));
        emoji.setUrl(c.getString(c.getColumnIndex(Sqlite.COL_URL)));
        emoji.setStatic_url(c.getString(c.getColumnIndex(Sqlite.COL_URL_STATIC)));
        //Close the cursor
        c.close();
        //Stored emoji is returned
        return emoji;
    }

    /***
     * Method to hydrate stored emojis from database
     * @param c Cursor
     * @return List<Emojis>
     */
    private List<Emojis> cursorToListEmojis(Cursor c){
        //No element found
        if (c.getCount() == 0)
            return null;
        List<Emojis> emojis = new ArrayList<>();
        while (c.moveToNext() ) {
            //Restore the emojis
            Emojis emoji = new Emojis();
            emoji.setShortcode(c.getString(c.getColumnIndex(Sqlite.COL_SHORTCODE)));
            emoji.setUrl(c.getString(c.getColumnIndex(Sqlite.COL_URL)));
            emoji.setStatic_url(c.getString(c.getColumnIndex(Sqlite.COL_URL_STATIC)));
            emojis.add(emoji);
        }
        //Close the cursor
        c.close();
        //Emjois list is returned
        return emojis;
    }
}
