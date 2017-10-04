package fr.gouv.etalab.mastodon.helper;

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

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.widget.BaseAdapter;
import java.util.ArrayList;
import java.util.List;
import fr.gouv.etalab.mastodon.asynctasks.PostActionAsyncTask;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.drawers.AccountsSearchAdapter;
import fr.gouv.etalab.mastodon.interfaces.OnPostActionInterface;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import mastodon.etalab.gouv.fr.mastodon.R;

/**
 * Will handle cross actions between accounts boost/favourites/pin and replies
 */
public class CrossActions {

    public static void doCrossAction(final Context context, final Status status, final API.StatusAction doAction, final BaseAdapter baseAdapter, final OnPostActionInterface onPostActionInterface){
        List<Account> accounts = connectedAccounts(context);
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);

        boolean undoAction = (doAction == API.StatusAction.UNPIN || doAction == API.StatusAction.UNREBLOG || doAction == API.StatusAction.UNFAVOURITE );
        //Undo actions won't ask for choosing a user
        if( accounts.size() == 1 || undoAction) {
            boolean confirmation = sharedpreferences.getBoolean(Helper.SET_NOTIF_VALIDATION_FAV, false);
            if (confirmation)
                displayConfirmationDialog(context, doAction, status, baseAdapter, onPostActionInterface);
            else {
                if( doAction == API.StatusAction.REBLOG || doAction == API.StatusAction.UNREBLOG)
                    reblogAction(context, status, baseAdapter, onPostActionInterface);
                else if( doAction == API.StatusAction.FAVOURITE || doAction == API.StatusAction.UNFAVOURITE)
                    favouriteAction(context, status, baseAdapter, onPostActionInterface);
                else if ( doAction == API.StatusAction.PIN || doAction == API.StatusAction.UNPIN)
                    pinAction(context, status, baseAdapter, onPostActionInterface);
            }
        }else {
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);
            builderSingle.setTitle(context.getString(R.string.choose_accounts));
            final AccountsSearchAdapter accountsSearchAdapter = new AccountsSearchAdapter(context, accounts, true);
            final Account[] accountArray = new Account[accounts.size()];
            int i = 0;
            for(Account account: accounts){
                accountArray[i] = account;
                i++;
            }
            builderSingle.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builderSingle.setAdapter(accountsSearchAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Account selectedAccount = accountArray[which];
                    String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
                    SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                    Account loggedAccount = new AccountDAO(context, db).getAccountByID(userId);
                    if(loggedAccount.getInstance().equals(selectedAccount.getInstance())){
                        new PostActionAsyncTask(context, selectedAccount, doAction, status.getId(), onPostActionInterface).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }else{ //Account is from another instance
                        new PostActionAsyncTask(context, selectedAccount, status, doAction, onPostActionInterface).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                    if( selectedAccount.getInstance().equals(loggedAccount.getInstance()) && selectedAccount.getId().equals(loggedAccount.getId())) {
                        if (doAction == API.StatusAction.REBLOG) {
                            status.setReblogged(true);
                        } else if (doAction == API.StatusAction.FAVOURITE) {
                            status.setFavourited(true);
                        } else if (doAction == API.StatusAction.PIN) {
                            status.setPinned(true);
                        }
                        baseAdapter.notifyDataSetChanged();
                    }
                    dialog.dismiss();
                }
            });
            builderSingle.show();
        }
    }



    /**
     * Display a validation message
     * @param action int
     * @param status Status
     */
    private static void displayConfirmationDialog(final Context context, final API.StatusAction action, final Status status, final BaseAdapter baseAdapter, final OnPostActionInterface onPostActionInterface){

        String title = null;
        if( action == API.StatusAction.FAVOURITE){
            if( status.isFavourited() || ( status.getReblog() != null && status.getReblog().isFavourited()))
                title = context.getString(R.string.favourite_remove);
            else
                title = context.getString(R.string.favourite_add);
        }else if( action == API.StatusAction.REBLOG ){
            if( status.isReblogged() || (status.getReblog() != null && status.getReblog().isReblogged()))
                title = context.getString(R.string.reblog_remove);
            else
                title = context.getString(R.string.reblog_add);
        }else if ( action == API.StatusAction.PIN) {
            title = context.getString(R.string.pin_add);
        }else if (action == API.StatusAction.UNPIN) {
            title = context.getString(R.string.pin_remove);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            builder.setMessage(Html.fromHtml(status.getContent(), Html.FROM_HTML_MODE_LEGACY));
        else
            //noinspection deprecation
            builder.setMessage(Html.fromHtml(status.getContent()));
        builder.setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(title)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if( action == API.StatusAction.REBLOG)
                            reblogAction(context, status, baseAdapter, onPostActionInterface);
                        else if( action == API.StatusAction.FAVOURITE)
                            favouriteAction(context, status, baseAdapter, onPostActionInterface);
                        else if ( action == API.StatusAction.PIN)
                            pinAction(context, status, baseAdapter, onPostActionInterface);
                        else if ( action == API.StatusAction.UNPIN)
                            pinAction(context, status, baseAdapter, onPostActionInterface);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }

                })
                .show();
    }

    /**
     * Favourites/Unfavourites a status
     * @param status Status
     */
    private static void favouriteAction(Context context, Status status, BaseAdapter baseAdapter, OnPostActionInterface onPostActionInterface){
        if( status.isFavourited() || (status.getReblog() != null && status.getReblog().isFavourited())){
            new PostActionAsyncTask(context, API.StatusAction.UNFAVOURITE, status.getId(), onPostActionInterface).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            status.setFavourited(false);
        }else{
            new PostActionAsyncTask(context, API.StatusAction.FAVOURITE, status.getId(), onPostActionInterface).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            status.setFavourited(true);
        }
        baseAdapter.notifyDataSetChanged();
    }

    /**
     * Reblog/Unreblog a status
     * @param status Status
     */
    private static void reblogAction(Context context, Status status, BaseAdapter baseAdapter, OnPostActionInterface onPostActionInterface){
        if( status.isReblogged() || (status.getReblog()!= null && status.getReblog().isReblogged())){
            new PostActionAsyncTask(context, API.StatusAction.UNREBLOG, status.getId(), onPostActionInterface).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            status.setReblogged(false);
        }else{
            new PostActionAsyncTask(context, API.StatusAction.REBLOG, status.getId(), onPostActionInterface).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            status.setReblogged(true);
        }
        baseAdapter.notifyDataSetChanged();
    }

    /**
     * Pin or unpin a status
     * @param status Status
     */
    private static void pinAction(Context context, Status status, BaseAdapter baseAdapter, OnPostActionInterface onPostActionInterface) {

        if (status.isPinned()) {
            new PostActionAsyncTask(context, API.StatusAction.UNPIN, status.getId(), onPostActionInterface).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            status.setPinned(false);
        } else {
            new PostActionAsyncTask(context, API.StatusAction.PIN, status.getId(), onPostActionInterface).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            status.setPinned(true);
        }
        baseAdapter.notifyDataSetChanged();
    }


    /**
     * Returns the list of connected accounts when cross actions are allowed otherwise, returns the current account
     * @param context Context
     * @return List<Account>
     */
    private static List<Account> connectedAccounts(Context context){
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        List<Account> accounts = new AccountDAO(context, db).getAllAccount();
        if( sharedpreferences.getBoolean(Helper.SET_ALLOW_CROSS_ACTIONS, true) && accounts.size() > 1 ){
            return accounts;
        }else {
            List<Account> oneAccount = new ArrayList<>();
            String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
            Account account = new AccountDAO(context, db).getAccountByID(userId);
            oneAccount.add(account);
            return  oneAccount;
        }
    }


}
