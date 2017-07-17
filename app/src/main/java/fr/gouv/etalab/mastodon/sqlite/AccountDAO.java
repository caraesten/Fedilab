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
 * You should have received a copy of the GNU General Public License along with Thomas Schneider; if not,
 * see <http://www.gnu.org/licenses>. */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.helper.Helper;


/**
 * Created by Thomas on 24/04/2017.
 * Manage Account in DB
 */
public class AccountDAO {

    private SQLiteDatabase db;
    public Context context;


    public AccountDAO(Context context, SQLiteDatabase db) {
        //Creation of the DB with tables
        this.context = context;
        this.db = db;
    }


    /**
     * Insert an Account in database
     * @param account Account
     * @return boolean
     */
    public boolean insertAccount(Account account)
    {
        ContentValues values = new ContentValues();

        values.put(Sqlite.COL_USER_ID, account.getId());
        values.put(Sqlite.COL_USERNAME, account.getUsername());
        values.put(Sqlite.COL_ACCT, account.getAcct());
        values.put(Sqlite.COL_DISPLAYED_NAME, account.getDisplay_name());
        values.put(Sqlite.COL_LOCKED,account.isLocked());
        values.put(Sqlite.COL_FOLLOWERS_COUNT,account.getFollowers_count());
        values.put(Sqlite.COL_FOLLOWING_COUNT,account.getFollowing_count());
        values.put(Sqlite.COL_STATUSES_COUNT,account.getStatuses_count());
        values.put(Sqlite.COL_NOTE,account.getNote());
        values.put(Sqlite.COL_URL,account.getUrl());
        values.put(Sqlite.COL_AVATAR,account.getAvatar());
        values.put(Sqlite.COL_AVATAR_STATIC,account.getAvatar_static());
        values.put(Sqlite.COL_HEADER,account.getHeader());
        values.put(Sqlite.COL_HEADER_STATIC,account.getHeader_static());
        values.put(Sqlite.COL_CREATED_AT, Helper.dateToString(context, account.getCreated_at()));
        values.put(Sqlite.COL_INSTANCE, account.getInstance());
        if( account.getToken() != null)
            values.put(Sqlite.COL_OAUTHTOKEN, account.getToken());

        //Inserts account
        try{
            db.insert(Sqlite.TABLE_USER_ACCOUNT, null, values);

        }catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Update an Account in database
     * @param account Account
     * @return boolean
     */
    public int updateAccount(Account account)
    {
        ContentValues values = new ContentValues();

        values.put(Sqlite.COL_ACCT, account.getAcct());
        values.put(Sqlite.COL_DISPLAYED_NAME, account.getDisplay_name());
        values.put(Sqlite.COL_LOCKED,account.isLocked());
        values.put(Sqlite.COL_FOLLOWERS_COUNT,account.getFollowers_count());
        values.put(Sqlite.COL_FOLLOWING_COUNT,account.getFollowing_count());
        values.put(Sqlite.COL_STATUSES_COUNT,account.getStatuses_count());
        values.put(Sqlite.COL_NOTE,account.getNote());
        values.put(Sqlite.COL_URL,account.getUrl());
        values.put(Sqlite.COL_AVATAR,account.getAvatar());
        values.put(Sqlite.COL_AVATAR_STATIC,account.getAvatar_static());
        values.put(Sqlite.COL_HEADER,account.getHeader());
        values.put(Sqlite.COL_HEADER_STATIC,account.getHeader_static());
        values.put(Sqlite.COL_CREATED_AT, Helper.dateToString(context, account.getCreated_at()));
        values.put(Sqlite.COL_INSTANCE, account.getInstance());
        if( account.getToken() != null)
            values.put(Sqlite.COL_OAUTHTOKEN, account.getToken());

        return db.update(Sqlite.TABLE_USER_ACCOUNT,
                values, Sqlite.COL_USER_ID + " =  ? AND " + Sqlite.COL_USERNAME + " =?",
                new String[]{account.getId(), account.getUsername()});
    }


    public int removeUser(Account account){
        return db.delete(Sqlite.TABLE_USER_ACCOUNT,  Sqlite.COL_USER_ID + " = '" +account.getId() +
                "' AND " + Sqlite.COL_USERNAME + " = '" +  account.getUsername()+ "'", null);
    }

    /**
     * Returns an Account by id
     * @param accountId String
     * @return Account
     */
    public Account getAccountByID(String accountId){

        try {
            Cursor c = db.query(Sqlite.TABLE_USER_ACCOUNT, null, Sqlite.COL_USER_ID + " = '" + accountId + "'", null, null, null, null, "1");
            return cursorToUser(c);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns an Account by id and instance
     * @param accountId String
     * @param instance String
     * @return Account
     */
    public Account getAccountByUserIDInstance(String accountId, String instance){
        try {
            Cursor c = db.query(Sqlite.TABLE_USER_ACCOUNT, null, Sqlite.COL_USER_ID + " = '" + accountId + "' AND " + Sqlite.COL_INSTANCE + "= '"+ instance +"'", null, null, null, null, "1");
            return cursorToUser(c);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns all Account in db
     * @return Account List<Account>
     */
    public List<Account> getAllAccount(){

        try {
            Cursor c = db.query(Sqlite.TABLE_USER_ACCOUNT, null, null, null, null, null, null, null);
            return cursorToListUser(c);
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * Returns an Account by token
     * @param token String
     * @return Account
     */
    public Account getAccountByToken(String token){

        try {
            Cursor c = db.query(Sqlite.TABLE_USER_ACCOUNT, null, Sqlite.COL_OAUTHTOKEN + " = \"" + token + "\"", null, null, null, null, "1");
            return cursorToUser(c);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Test if the current user is already stored in data base
     * @param account Account
     * @return boolean
     */
    public boolean userExist(Account account)
    {
        Cursor mCount= db.rawQuery("select count(*) from " + Sqlite.TABLE_USER_ACCOUNT
                + " where " + Sqlite.COL_USER_ID + " = '" + account.getId() + "' AND " + Sqlite.COL_USERNAME + " = '" +  account.getUsername()+ "'", null);
        mCount.moveToFirst();
        int count = mCount.getInt(0);
        mCount.close();
        return (count > 0);
    }


    /***
     * Method to hydrate an Account from database
     * @param c Cursor
     * @return Account
     */
    private Account cursorToUser(Cursor c){
        //No element found
        if (c.getCount() == 0)
            return null;
        //Take the first element
        c.moveToFirst();
        //New user
        Account account = new Account();

        account.setId(c.getString(c.getColumnIndex(Sqlite.COL_USER_ID)));
        account.setUsername(c.getString(c.getColumnIndex(Sqlite.COL_USERNAME)));
        account.setAcct(c.getString(c.getColumnIndex(Sqlite.COL_ACCT)));
        account.setDisplay_name(c.getString(c.getColumnIndex(Sqlite.COL_DISPLAYED_NAME)));
        account.setLocked(c.getInt(c.getColumnIndex(Sqlite.COL_LOCKED)) == 1);
        account.setFollowers_count(c.getInt(c.getColumnIndex(Sqlite.COL_FOLLOWERS_COUNT)));
        account.setFollowing_count(c.getInt(c.getColumnIndex(Sqlite.COL_FOLLOWING_COUNT)));
        account.setStatuses_count(c.getInt(c.getColumnIndex(Sqlite.COL_STATUSES_COUNT)));
        account.setNote(c.getString(c.getColumnIndex(Sqlite.COL_NOTE)));
        account.setUrl(c.getString(c.getColumnIndex(Sqlite.COL_URL)));
        account.setAvatar(c.getString(c.getColumnIndex(Sqlite.COL_AVATAR)));
        account.setAvatar_static(c.getString(c.getColumnIndex(Sqlite.COL_AVATAR_STATIC)));
        account.setHeader(c.getString(c.getColumnIndex(Sqlite.COL_HEADER)));
        account.setHeader_static(c.getString(c.getColumnIndex(Sqlite.COL_HEADER_STATIC)));
        account.setCreated_at(Helper.stringToDate(context, c.getString(c.getColumnIndex(Sqlite.COL_CREATED_AT))));
        account.setInstance(c.getString(c.getColumnIndex(Sqlite.COL_INSTANCE)));
        account.setToken(c.getString(c.getColumnIndex(Sqlite.COL_OAUTHTOKEN)));

        //Close the cursor
        c.close();

        //User is returned
        return account;
    }

    /***
     * Method to hydrate an Accounts from database
     * @param c Cursor
     * @return List<Account>
     */
    private List<Account> cursorToListUser(Cursor c){
        //No element found
        if (c.getCount() == 0)
            return null;
        List<Account> accounts = new ArrayList<>();
        while (c.moveToNext() ) {
            //New user
            Account account = new Account();

            account.setId(c.getString(c.getColumnIndex(Sqlite.COL_USER_ID)));
            account.setUsername(c.getString(c.getColumnIndex(Sqlite.COL_USERNAME)));
            account.setAcct(c.getString(c.getColumnIndex(Sqlite.COL_ACCT)));
            account.setDisplay_name(c.getString(c.getColumnIndex(Sqlite.COL_DISPLAYED_NAME)));
            account.setLocked(c.getInt(c.getColumnIndex(Sqlite.COL_LOCKED)) == 1);
            account.setFollowers_count(c.getInt(c.getColumnIndex(Sqlite.COL_FOLLOWERS_COUNT)));
            account.setFollowing_count(c.getInt(c.getColumnIndex(Sqlite.COL_FOLLOWING_COUNT)));
            account.setStatuses_count(c.getInt(c.getColumnIndex(Sqlite.COL_STATUSES_COUNT)));
            account.setNote(c.getString(c.getColumnIndex(Sqlite.COL_NOTE)));
            account.setUrl(c.getString(c.getColumnIndex(Sqlite.COL_URL)));
            account.setAvatar(c.getString(c.getColumnIndex(Sqlite.COL_AVATAR)));
            account.setAvatar_static(c.getString(c.getColumnIndex(Sqlite.COL_AVATAR_STATIC)));
            account.setHeader(c.getString(c.getColumnIndex(Sqlite.COL_HEADER)));
            account.setHeader_static(c.getString(c.getColumnIndex(Sqlite.COL_HEADER_STATIC)));
            account.setCreated_at(Helper.stringToDate(context, c.getString(c.getColumnIndex(Sqlite.COL_CREATED_AT))));
            account.setInstance(c.getString(c.getColumnIndex(Sqlite.COL_INSTANCE)));
            account.setToken(c.getString(c.getColumnIndex(Sqlite.COL_OAUTHTOKEN)));
            accounts.add(account);
        }
        //Close the cursor
        c.close();
        //Users list is returned
        return accounts;
    }


}
