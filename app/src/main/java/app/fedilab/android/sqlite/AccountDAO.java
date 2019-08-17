package app.fedilab.android.sqlite;
/* Copyright 2017 Thomas Schneider
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
import java.util.Date;
import java.util.List;

import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.helper.Helper;


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
        if( account.getCreated_at() == null)
            account.setCreated_at(new Date());
        if( account.getNote() == null)
            account.setNote("");
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
        values.put(Sqlite.COL_CREATED_AT, Helper.dateToString(account.getCreated_at()));
        values.put(Sqlite.COL_INSTANCE, account.getInstance());
        values.put(Sqlite.COL_EMOJIS, Helper.emojisToStringStorage(account.getEmojis()));
        values.put(Sqlite.COL_SOCIAL, account.getSocial());
        if( account.getClient_id() != null && account.getClient_secret() != null && account.getRefresh_token() != null) {
            values.put(Sqlite.COL_CLIENT_ID, account.getClient_id());
            values.put(Sqlite.COL_CLIENT_SECRET, account.getClient_secret());
            values.put(Sqlite.COL_REFRESH_TOKEN, account.getRefresh_token());
        }
        if( account.getToken() != null)
            values.put(Sqlite.COL_OAUTHTOKEN, account.getToken());

        values.put(Sqlite.COL_SENSITIVE, account.isSensitive());
        values.put(Sqlite.COL_PRIVACY, account.getPrivacy());
        //Inserts account
        try{
            db.insertOrThrow(Sqlite.TABLE_USER_ACCOUNT, null, values);

        }catch (Exception e) {
            e.printStackTrace();
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
        if( account.getNote() == null)
            account.setNote("");
        if( account.getCreated_at() == null)
            account.setCreated_at(new Date());
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
        values.put(Sqlite.COL_CREATED_AT, Helper.dateToString(account.getCreated_at()));
        values.put(Sqlite.COL_EMOJIS, Helper.emojisToStringStorage(account.getEmojis()));
        values.put(Sqlite.COL_SOCIAL, account.getSocial());
        if( account.getClient_id() != null && account.getClient_secret() != null && account.getRefresh_token() != null) {
            values.put(Sqlite.COL_CLIENT_ID, account.getClient_id());
            values.put(Sqlite.COL_CLIENT_SECRET, account.getClient_secret());
            values.put(Sqlite.COL_REFRESH_TOKEN, account.getRefresh_token());
        }
        if( account.getToken() != null)
            values.put(Sqlite.COL_OAUTHTOKEN, account.getToken());
        return db.update(Sqlite.TABLE_USER_ACCOUNT,
                values, Sqlite.COL_USER_ID + " =  ? AND " + Sqlite.COL_INSTANCE + " =?",
                new String[]{account.getId(), account.getInstance()});
    }


    /**
     * Update an Account in database
     * @param account Account
     * @return boolean
     */
    public int updateAccountCredential(Account account)
    {
        ContentValues values = new ContentValues();
        if( account.getNote() == null)
            account.setNote("");
        if( account.getCreated_at() == null)
            account.setCreated_at(new Date());
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
        values.put(Sqlite.COL_CREATED_AT, Helper.dateToString(account.getCreated_at()));
        values.put(Sqlite.COL_EMOJIS, Helper.emojisToStringStorage(account.getEmojis()));
        values.put(Sqlite.COL_SOCIAL, account.getSocial());
        if( account.getClient_id() != null && account.getClient_secret() != null && account.getRefresh_token() != null) {
            values.put(Sqlite.COL_CLIENT_ID, account.getClient_id());
            values.put(Sqlite.COL_CLIENT_SECRET, account.getClient_secret());
            values.put(Sqlite.COL_REFRESH_TOKEN, account.getRefresh_token());
        }
        if( account.getToken() != null)
            values.put(Sqlite.COL_OAUTHTOKEN, account.getToken());
        values.put(Sqlite.COL_SENSITIVE, account.isSensitive());
        values.put(Sqlite.COL_PRIVACY, account.getPrivacy());
        return db.update(Sqlite.TABLE_USER_ACCOUNT,
                values, Sqlite.COL_USER_ID + " =  ? AND " + Sqlite.COL_INSTANCE + " =?",
                new String[]{account.getId(), account.getInstance()});
    }


    public int removeUser(Account account){
        return db.delete(Sqlite.TABLE_USER_ACCOUNT,  Sqlite.COL_USER_ID + " = '" +account.getId() +
                "' AND " + Sqlite.COL_INSTANCE + " = '" +  account.getInstance()+ "'", null);
    }


    /**
     * Returns last used account
     * @return Account
     */
    public Account getLastUsedAccount(){

        try {
            Cursor c = db.query(Sqlite.TABLE_USER_ACCOUNT, null, Sqlite.COL_OAUTHTOKEN + " != 'null'", null, null, null, Sqlite.COL_UPDATED_AT + " DESC", "1");
            return cursorToUser(c);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns an Account by its id and acct
     * @param accountId String
     * @param accountAcct String
     * @return Account
     */
    public Account getAccountByIDAcct(String accountId, String accountAcct){
        try {
            Cursor c = db.query(Sqlite.TABLE_USER_ACCOUNT, null, Sqlite.COL_USER_ID + " = '" + accountId + "' AND " + Sqlite.COL_ACCT + " = '" + accountAcct + "'", null, null, null, null, "1");
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
            Cursor c = db.query(Sqlite.TABLE_USER_ACCOUNT, null, null, null, null, null, Sqlite.COL_INSTANCE + " ASC", null);
            return cursorToListUser(c);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns all Account in db
     * @return Account List<Account>
     */
    public List<Account> getAllAccountActivated(){

        try {
            Cursor c = db.query(Sqlite.TABLE_USER_ACCOUNT, null,  Sqlite.COL_OAUTHTOKEN + " != 'null'", null, null, null, Sqlite.COL_INSTANCE + " ASC", null);
            return cursorToListUser(c);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns all Account in db
     * @return Account List<Account>
     */
    public List<Account> getAllAccountCrossAction(){

        try {
            Cursor c = db.query(Sqlite.TABLE_USER_ACCOUNT, null, Sqlite.COL_SOCIAL + " != 'PEERTUBE' AND " + Sqlite.COL_OAUTHTOKEN + " != 'null'", null, null, null, Sqlite.COL_INSTANCE + " ASC", null);
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
     * Returns an Account by token
     * @param userId String
     * @param instance String
     * @return Account
     */
    public Account getUniqAccount(String userId, String instance){

        try {
            Cursor c = db.query(Sqlite.TABLE_USER_ACCOUNT, null, Sqlite.COL_USER_ID + " = \"" + userId + "\" AND " + Sqlite.COL_INSTANCE + " = \"" + instance + "\"", null, null, null, null, "1");
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
                + " where " + Sqlite.COL_ACCT + " = '" + account.getAcct() + "' AND " + Sqlite.COL_INSTANCE + " = '" +  account.getInstance()+ "'", null);
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
        if (c.getCount() == 0) {
            c.close();
            return null;
        }
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
        account.setUpdated_at(Helper.stringToDate(context, c.getString(c.getColumnIndex(Sqlite.COL_UPDATED_AT))));
        account.setCreated_at(Helper.stringToDate(context, c.getString(c.getColumnIndex(Sqlite.COL_CREATED_AT))));
        account.setInstance(c.getString(c.getColumnIndex(Sqlite.COL_INSTANCE)));
        account.setEmojis(Helper.restoreEmojisFromString(c.getString(c.getColumnIndex(Sqlite.COL_EMOJIS))));
        account.setToken(c.getString(c.getColumnIndex(Sqlite.COL_OAUTHTOKEN)));
        account.setSocial(c.getString(c.getColumnIndex(Sqlite.COL_SOCIAL))!=null?c.getString(c.getColumnIndex(Sqlite.COL_SOCIAL)):"MASTODON");
        account.setClient_id(c.getString(c.getColumnIndex(Sqlite.COL_CLIENT_ID)));
        account.setClient_secret(c.getString(c.getColumnIndex(Sqlite.COL_CLIENT_SECRET)));
        account.setRefresh_token(c.getString(c.getColumnIndex(Sqlite.COL_REFRESH_TOKEN)));
        account.setSensitive(c.getInt(c.getColumnIndex(Sqlite.COL_SENSITIVE)) == 1);
        account.setPrivacy((c.getString(c.getColumnIndex(Sqlite.COL_PRIVACY))));
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
        if (c.getCount() == 0) {
            c.close();
            return null;
        }
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
            account.setUpdated_at(Helper.stringToDate(context, c.getString(c.getColumnIndex(Sqlite.COL_UPDATED_AT))));
            account.setCreated_at(Helper.stringToDate(context, c.getString(c.getColumnIndex(Sqlite.COL_CREATED_AT))));
            account.setInstance(c.getString(c.getColumnIndex(Sqlite.COL_INSTANCE)));
            account.setToken(c.getString(c.getColumnIndex(Sqlite.COL_OAUTHTOKEN)));
            account.setSocial(c.getString(c.getColumnIndex(Sqlite.COL_SOCIAL))!=null?c.getString(c.getColumnIndex(Sqlite.COL_SOCIAL)):"MASTODON");
            account.setClient_id(c.getString(c.getColumnIndex(Sqlite.COL_CLIENT_ID)));
            account.setClient_secret(c.getString(c.getColumnIndex(Sqlite.COL_CLIENT_SECRET)));
            account.setRefresh_token(c.getString(c.getColumnIndex(Sqlite.COL_REFRESH_TOKEN)));
            account.setSensitive(c.getInt(c.getColumnIndex(Sqlite.COL_SENSITIVE)) == 1);
            account.setPrivacy((c.getString(c.getColumnIndex(Sqlite.COL_PRIVACY))));
            accounts.add(account);
        }
        //Close the cursor
        c.close();
        //Users list is returned
        return accounts;
    }


}
