package app.fedilab.android.helper;

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

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import app.fedilab.android.client.API;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Mention;
import app.fedilab.android.client.Entities.Results;
import app.fedilab.android.client.Entities.Status;
import es.dmoral.toasty.Toasty;
import app.fedilab.android.R;
import app.fedilab.android.activities.BaseActivity;
import app.fedilab.android.activities.ShowAccountActivity;
import app.fedilab.android.activities.ShowConversationActivity;
import app.fedilab.android.activities.TootActivity;
import app.fedilab.android.asynctasks.PostActionAsyncTask;
import app.fedilab.android.asynctasks.RetrieveFeedsAsyncTask;
import app.fedilab.android.drawers.AccountsSearchAdapter;
import app.fedilab.android.drawers.PixelfedListAdapter;
import app.fedilab.android.drawers.StatusListAdapter;
import app.fedilab.android.interfaces.OnPostActionInterface;
import app.fedilab.android.sqlite.AccountDAO;
import app.fedilab.android.sqlite.Sqlite;
import app.fedilab.android.sqlite.StatusCacheDAO;

/**
 * Will handle cross actions between accounts boost/favourites/pin and replies
 */
public class CrossActions {


    private static int style;

    /**
     * Returns the list of connected accounts when cross actions are allowed otherwise, returns the current account
     * @param context Context
     * @return List<Account>
     */
    private static List<Account> connectedAccounts(Context context, Status status, boolean limitedToOwner){
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        List<Account> accountstmp = new AccountDAO(context, db).getAllAccountCrossAction();
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = sharedpreferences.getString(Helper.PREF_INSTANCE, null);
        Account currentAccount = new AccountDAO(context, db).getUniqAccount(userId, instance);
        List<Account> accounts = new ArrayList<>();
        if( accountstmp != null && !limitedToOwner && accountstmp.size() > 1 ){
            //It's for a reply
            if( status != null){
                //Status is private or direct
                if( status.getVisibility().equals("private") || status.getVisibility().equals("direct") ){
                    //Retrieves mentioned accounts and compares them to the list of accounts in the device.
                    List<Mention> mentions = status.getMentions();
                    List<String> addedAccount = new ArrayList<>();
                    //Adds the owner
                    accounts.add(currentAccount);
                    addedAccount.add(currentAccount.getId() + "|" + currentAccount.getAcct());
                    for(Mention mention: mentions){
                        for(Account account: accountstmp){
                            String mentionAcct = (mention.getAcct().contains("@"))?mention.getAcct():mention.getAcct()+"@"+currentAccount.getInstance();
                            if( (account.getAcct() + "@" + account.getInstance()).equals(mentionAcct) && !addedAccount.contains(account.getId() + "|" + account.getAcct())) {
                                if( account.getSocial() == null || account.getSocial().equals("MASTODON") || account.getSocial().equals("PLEROMA"))
                                    accounts.add(account);
                            }
                        }
                    }
                    for(Account account: accountstmp){
                        Account tootOwner = status.getAccount();
                        String mentionAcct = (tootOwner.getAcct().contains("@"))?tootOwner.getAcct():tootOwner.getAcct()+"@"+currentAccount.getInstance();
                        if( (account.getAcct() + "@" + account.getInstance()).equals(mentionAcct) && !addedAccount.contains(account.getId() + "|" + account.getAcct())) {
                            if( account.getSocial() == null || account.getSocial().equals("MASTODON")|| account.getSocial().equals("PLEROMA"))
                                accounts.add(account);
                        }
                    }
                }else {
                    accounts = accountstmp;
                }
            }else {
                accounts = accountstmp;
            }
            return accounts;
        }else {
            List<Account> oneAccount = new ArrayList<>();
            Account account = new AccountDAO(context, db).getUniqAccount(userId, instance);
            oneAccount.add(account);
            return  oneAccount;
        }
    }


    public static void doCrossAction(final Context context, RetrieveFeedsAsyncTask.Type type, final Status status, final Account targetedAccount, final API.StatusAction doAction, final RecyclerView.Adapter baseAdapter, final OnPostActionInterface onPostActionInterface, boolean limitedToOwner) {
        List<Account> accounts = connectedAccounts(context, status, limitedToOwner);
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        boolean undoAction = (doAction == API.StatusAction.UNPIN || doAction == API.StatusAction.UNREBLOG || doAction == API.StatusAction.UNFAVOURITE);
        //Undo actions won't ask for choosing a user

        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if (theme == Helper.THEME_DARK) {
            style = R.style.DialogDark;
        } else if (theme == Helper.THEME_BLACK){
            style = R.style.DialogBlack;
        }else {
            style = R.style.Dialog;
        }
        boolean confirmation = false;
        if (doAction == API.StatusAction.UNFAVOURITE || doAction == API.StatusAction.FAVOURITE)
            confirmation = sharedpreferences.getBoolean(Helper.SET_NOTIF_VALIDATION_FAV, false);
        else if (doAction == API.StatusAction.UNREBLOG || doAction == API.StatusAction.REBLOG)
            confirmation = sharedpreferences.getBoolean(Helper.SET_NOTIF_VALIDATION, true);
        if(type == RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE && limitedToOwner){
            String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
            String instance = sharedpreferences.getString(Helper.PREF_INSTANCE, null);
            SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
            Account currentAccount = new AccountDAO(context, db).getUniqAccount(userId, instance);
            if (confirmation)
                displayConfirmationDialogCrossAction(context, currentAccount, doAction, status, onPostActionInterface, baseAdapter);
            else {
                new PostActionAsyncTask(context, currentAccount, status, doAction, onPostActionInterface).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                if( doAction == API.StatusAction.FAVOURITE || doAction == API.StatusAction.UNFAVOURITE){
                    if (doAction == API.StatusAction.FAVOURITE) {
                        status.setFavourited(true);
                        status.setFavAnimated(true);
                    }else{
                        status.setFavourited(false);
                        status.setFavAnimated(false);
                    }
                    if(baseAdapter instanceof PixelfedListAdapter)
                        ((PixelfedListAdapter) baseAdapter).notifyStatusChanged(status);
                    else if(baseAdapter instanceof StatusListAdapter)
                        ((StatusListAdapter) baseAdapter).notifyStatusChanged(status);
                }else if(doAction == API.StatusAction.REBLOG || doAction == API.StatusAction.UNREBLOG){
                    if (doAction == API.StatusAction.REBLOG) {
                        status.setReblogged(true);
                        status.setBoostAnimated(true);
                    }else{
                        status.setReblogged(false);
                        status.setBoostAnimated(false);
                    }
                    if(baseAdapter instanceof PixelfedListAdapter)
                        ((PixelfedListAdapter) baseAdapter).notifyStatusChanged(status);
                    else if(baseAdapter instanceof StatusListAdapter)
                        ((StatusListAdapter) baseAdapter).notifyStatusChanged(status);
                }
            }
        } else if (accounts.size() == 1 || undoAction) {
            if (confirmation)
                displayConfirmationDialog(context, doAction, status, baseAdapter, onPostActionInterface);
            else {
                if (doAction == API.StatusAction.REBLOG || doAction == API.StatusAction.UNREBLOG)
                    reblogAction(context, status, onPostActionInterface);
                else if (doAction == API.StatusAction.FAVOURITE || doAction == API.StatusAction.UNFAVOURITE)
                    favouriteAction(context, status, onPostActionInterface);
                else if (doAction == API.StatusAction.PIN || doAction == API.StatusAction.UNPIN)
                    pinAction(context, status, baseAdapter, onPostActionInterface);

                if( doAction == API.StatusAction.FAVOURITE || doAction == API.StatusAction.UNFAVOURITE){
                    if (doAction == API.StatusAction.FAVOURITE) {
                        status.setFavourited(true);
                        status.setFavAnimated(true);
                    }else{
                        status.setFavourited(false);
                        status.setFavAnimated(false);
                    }
                    if(baseAdapter instanceof PixelfedListAdapter)
                        ((PixelfedListAdapter) baseAdapter).notifyStatusChanged(status);
                    else if(baseAdapter instanceof StatusListAdapter)
                        ((StatusListAdapter) baseAdapter).notifyStatusChanged(status);
                }else if(doAction == API.StatusAction.REBLOG || doAction == API.StatusAction.UNREBLOG){
                    if (doAction == API.StatusAction.REBLOG) {
                        status.setReblogged(true);
                        status.setBoostAnimated(true);
                    }else{
                        status.setReblogged(false);
                        status.setBoostAnimated(false);
                    }
                    if(baseAdapter instanceof PixelfedListAdapter)
                        ((PixelfedListAdapter) baseAdapter).notifyStatusChanged(status);
                    else if(baseAdapter instanceof StatusListAdapter)
                        ((StatusListAdapter) baseAdapter).notifyStatusChanged(status);
                }
            }
        } else {

            AlertDialog.Builder builderSingle = new AlertDialog.Builder(context, style);
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
                    String instance = sharedpreferences.getString(Helper.PREF_INSTANCE, null);
                    SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                    Account loggedAccount = new AccountDAO(context, db).getUniqAccount(userId, instance);
                    if( targetedAccount == null){
                        if(loggedAccount.getInstance().equals(selectedAccount.getInstance())){
                            new PostActionAsyncTask(context, selectedAccount, doAction, status.getId(), onPostActionInterface).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }else{ //Account is from another instance
                            new PostActionAsyncTask(context, selectedAccount, status, doAction, onPostActionInterface).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                        if( selectedAccount.getInstance().equals(loggedAccount.getInstance()) && selectedAccount.getId().equals(loggedAccount.getId())) {
                            if (doAction == API.StatusAction.REBLOG) {
                                status.setReblogged(true);
                                if( type == RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE)
                                    status.setBoostAnimated(true);
                            } else if (doAction == API.StatusAction.FAVOURITE) {
                                status.setFavourited(true);
                                if( type == RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE)
                                    status.setFavAnimated(true);
                            } else if (doAction == API.StatusAction.PIN) {
                                status.setPinned(true);
                            }
                            if( baseAdapter != null)
                                baseAdapter.notifyDataSetChanged();
                        }
                    }else{
                        new PostActionAsyncTask(context, selectedAccount, targetedAccount, doAction, onPostActionInterface).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        if( selectedAccount.getInstance().equals(loggedAccount.getInstance()) && selectedAccount.getId().equals(loggedAccount.getId())) {
                            if (doAction == API.StatusAction.FOLLOW) {
                                targetedAccount.setFollowing(true);
                            }
                            if( baseAdapter != null)
                                baseAdapter.notifyDataSetChanged();
                        }
                    }
                    dialog.dismiss();
                }
            });
            builderSingle.show();
        }
    }


    public static void followPeertubeChannel(final Context context, Account remoteAccount, OnPostActionInterface onPostActionInterface){
        new AsyncTask<Void, Void, Void>() {
            private WeakReference<Context> contextReference = new WeakReference<>(context);
            Results response;

            @Override
            protected void onPreExecute() {
                Toasty.info(contextReference.get(), contextReference.get().getString(R.string.retrieve_remote_account), Toast.LENGTH_SHORT).show();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                API api = new API(contextReference.get());
                String url;
                url = "https://" + remoteAccount.getHost() + "/video-channels/" + remoteAccount.getAcct().split("@")[0];
                APIResponse apiResponse = api.search(url);
                response = apiResponse.getResults();
                return null;
            }
            @Override
            protected void onPostExecute(Void result) {
                if( response == null){
                    return;
                }
                List<Account> remoteAccounts = response.getAccounts();
                if( remoteAccounts != null && remoteAccounts.size() > 0) {
                    new PostActionAsyncTask(context, null, remoteAccounts.get(0), API.StatusAction.FOLLOW, onPostActionInterface).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR );
    }

    public static void doCrossProfile(final Context context, Account remoteAccount){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = sharedpreferences.getString(Helper.PREF_INSTANCE, null);
        SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        Account account = new AccountDAO(context, db).getUniqAccount(userId, instance);

        new AsyncTask<Void, Void, Void>() {
            private WeakReference<Context> contextReference = new WeakReference<>(context);
            Results response;

            @Override
            protected void onPreExecute() {
                Toasty.info(contextReference.get(), contextReference.get().getString(R.string.retrieve_remote_account), Toast.LENGTH_SHORT).show();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                API api = new API(contextReference.get(), account.getInstance(), account.getToken());
                String url = remoteAccount.getUrl();
                if( url == null) {
                    if (remoteAccount.getHost() != null && remoteAccount.getAcct().split("@").length > 1) //Peertube compatibility
                        url = "https://" + remoteAccount.getHost() + "/accounts/" + remoteAccount.getAcct().split("@")[0];
                    else
                        url = "https://" + remoteAccount.getInstance() + "/@" + remoteAccount.getAcct();
                }
                APIResponse apiResponse = api.search2(url, null, null);
                response = apiResponse.getResults();
                return null;
            }
            @Override
            protected void onPostExecute(Void result) {
                if( response == null){
                    return;
                }
                List<Account> remoteAccounts = response.getAccounts();
                if( remoteAccounts != null && remoteAccounts.size() > 0) {

                    Account fetchedAccount = null;
                    if( remoteAccounts.size() == 1){
                        Account acc = remoteAccounts.get(0);
                        if (acc.getUsername().equals(remoteAccount.getUsername())) {
                            fetchedAccount = acc;
                        }
                    }else {
                        remoteAccounts.size();
                        for(Account acc: remoteAccounts){

                            String instance = null;
                            try {
                                URI url = new URI(acc.getUrl());
                                instance = url.getHost();
                            } catch (URISyntaxException e) {
                                e.printStackTrace();
                            }
                            if( instance != null ) {
                                if (acc.getUsername().equals(remoteAccount.getUsername()) && instance.equals(remoteAccount.getInstance())) {
                                    fetchedAccount = acc;
                                    break;
                                }
                            }else{
                                if (acc.getUsername().equals(remoteAccount.getUsername())) {
                                    fetchedAccount = acc;
                                    break;
                                }
                            }
                        }
                    }
                    if(fetchedAccount != null){
                        Intent intent = new Intent(context, ShowAccountActivity.class);
                        Bundle b = new Bundle();
                        //Flag it has a peertube account
                        if( remoteAccount.getHost() != null && remoteAccount.getAcct().split("@").length > 1)
                            b.putBoolean("peertubeaccount", true);
                        b.putParcelable("account", fetchedAccount);
                        intent.putExtras(b);
                        context.startActivity(intent);
                    }

                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR );
    }

    public static void doCrossConversation(final Context context, Status remoteStatus){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = sharedpreferences.getString(Helper.PREF_INSTANCE, null);
        SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        Account account = new AccountDAO(context, db).getUniqAccount(userId, instance);

        new AsyncTask<Void, Void, Void>() {
            private WeakReference<Context> contextReference = new WeakReference<>(context);
            Results response;

            @Override
            protected void onPreExecute() {
                Toasty.info(contextReference.get(), contextReference.get().getString(R.string.retrieve_remote_conversation), Toast.LENGTH_SHORT).show();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                API api = new API(contextReference.get(), account.getInstance(), account.getToken());
                APIResponse apiResponse = api.search(remoteStatus.getUrl());
                response = apiResponse.getResults();
                return null;
            }
            @Override
            protected void onPostExecute(Void result) {
                if( response == null){
                    return;
                }
                List<app.fedilab.android.client.Entities.Status> statuses = response.getStatuses();
                if( statuses != null && statuses.size() > 0) {
                    Intent intent = new Intent(context, ShowConversationActivity.class);
                    Bundle b = new Bundle();
                    b.putParcelable("status", statuses.get(0));
                    intent.putExtras(b);
                    context.startActivity(intent);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR );
    }


    public static void doCrossConversation(final Context context, String url){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = sharedpreferences.getString(Helper.PREF_INSTANCE, null);
        SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        Account account = new AccountDAO(context, db).getUniqAccount(userId, instance);

        new AsyncTask<Void, Void, Void>() {
            private WeakReference<Context> contextReference = new WeakReference<>(context);
            Results response;

            @Override
            protected void onPreExecute() {
                Toasty.info(contextReference.get(), contextReference.get().getString(R.string.retrieve_remote_conversation), Toast.LENGTH_SHORT).show();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                API api = new API(contextReference.get(), account.getInstance(), account.getToken());
                APIResponse apiResponse = api.search(url);
                response =  apiResponse.getResults();
                return null;
            }
            @Override
            protected void onPostExecute(Void result) {
                if( response == null){
                    return;
                }
                List<app.fedilab.android.client.Entities.Status> statuses = response.getStatuses();
                if( statuses != null && statuses.size() > 0) {
                    Intent intent = new Intent(context, ShowConversationActivity.class);
                    Bundle b = new Bundle();
                    b.putParcelable("status", statuses.get(0));
                    intent.putExtras(b);
                    context.startActivity(intent);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR );
    }

    public static void doCrossBookmark(final Context context, final Status status, StatusListAdapter statusListAdapter ){
        List<Account> accounts = connectedAccounts(context, status, false);

        if( accounts.size() == 1) {
            status.setBookmarked(!status.isBookmarked());
            try {
                SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                if (status.isBookmarked()) {
                    new StatusCacheDAO(context, db).insertStatus(StatusCacheDAO.BOOKMARK_CACHE, status);
                    Toasty.success(context, context.getString(R.string.status_bookmarked), Toast.LENGTH_LONG).show();
                } else {
                    new StatusCacheDAO(context, db).remove(StatusCacheDAO.BOOKMARK_CACHE, status);
                    Toasty.success(context, context.getString(R.string.status_unbookmarked), Toast.LENGTH_LONG).show();
                }
                statusListAdapter.notifyStatusChanged(status);
            }catch (Exception e){
                e.printStackTrace();
                Toasty.error(context, context.getString(R.string.toast_error),Toast.LENGTH_LONG).show();
            }
        }else {
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(context, style);
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
                public void onClick(final DialogInterface dialog, int which) {
                    final Account account = accountArray[which];
                    new AsyncTask<Void, Void, Void>() {
                        private WeakReference<Context> contextReference = new WeakReference<>(context);
                        Results response;

                        @Override
                        protected void onPreExecute() {
                            Toasty.info(contextReference.get(), contextReference.get().getString(R.string.retrieve_remote_status), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        protected Void doInBackground(Void... voids) {
                            API api = new API(contextReference.get(), account.getInstance(), account.getToken());
                            APIResponse apiResponse = api.search(status.getUrl());
                            response = apiResponse.getResults();
                            return null;
                        }
                        @Override
                        protected void onPostExecute(Void result) {
                            if( response == null){
                                Toasty.error(contextReference.get(),context.getString(R.string.toast_error),Toast.LENGTH_LONG).show();
                                return;
                            }
                            List<app.fedilab.android.client.Entities.Status> statuses = response.getStatuses();
                            if( statuses != null && statuses.size() > 0) {
                                final SQLiteDatabase db = Sqlite.getInstance(contextReference.get(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                                app.fedilab.android.client.Entities.Status statusBookmarked = new StatusCacheDAO(contextReference.get(), db).getStatus(StatusCacheDAO.BOOKMARK_CACHE, statuses.get(0).getId(), account.getId(), account.getInstance());
                                if (statusBookmarked == null) {
                                    new StatusCacheDAO(contextReference.get(), db).insertStatus(StatusCacheDAO.BOOKMARK_CACHE, statuses.get(0), account.getId(), account.getInstance());
                                    Toasty.success(contextReference.get(), contextReference.get().getString(R.string.status_bookmarked), Toast.LENGTH_LONG).show();
                                } else {
                                    new StatusCacheDAO(contextReference.get(), db).remove(StatusCacheDAO.BOOKMARK_CACHE, statuses.get(0), account.getId(), account.getInstance());
                                    Toasty.success(contextReference.get(), contextReference.get().getString(R.string.status_unbookmarked), Toast.LENGTH_LONG).show();
                                }
                                statusListAdapter.notifyStatusChanged(statuses.get(0));
                            }
                        }
                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR );

                }
            });
            builderSingle.show();
        }
    }



    public static void doCrossReply(final Context context, final Status status, final RetrieveFeedsAsyncTask.Type type, boolean limitedToOwner){
        List<Account> accounts = connectedAccounts(context, status, limitedToOwner);

        if( accounts.size() == 1 && type != RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE && type != RetrieveFeedsAsyncTask.Type.NEWS) {
            Intent intent = new Intent(context, TootActivity.class);
            Bundle b = new Bundle();
            if( status != null && status.getReblog() != null )
                b.putParcelable("tootReply", status.getReblog());
            else
                b.putParcelable("tootReply", status);
            intent.putExtras(b); //Put your id to your next Intent
            context.startActivity(intent);
            if( type == RetrieveFeedsAsyncTask.Type.CONTEXT ){
                try {
                    //Avoid to open multi activities when replying in a conversation
                    ((ShowConversationActivity)context).finish();
                }catch (Exception ignored){}

            }
        }else {
            if( type != RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE && type != RetrieveFeedsAsyncTask.Type.NEWS){
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(context, style);
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
                    public void onClick(final DialogInterface dialog, int which) {
                        final Account account = accountArray[which];
                        if(status != null) {
                            new AsyncTask<Void, Void, Void>() {
                                private List<app.fedilab.android.client.Entities.Status> remoteStatuses;
                                private WeakReference<Context> contextReference = new WeakReference<>(context);

                                @Override
                                protected Void doInBackground(Void... voids) {


                                    API api = new API(contextReference.get(), account.getInstance(), account.getToken());
                                    String uri;
                                    if (status.getReblog() != null) {
                                        if (status.getReblog().getUri().startsWith("http"))
                                            uri = status.getReblog().getUri();
                                        else
                                            uri = status.getReblog().getUrl();
                                    } else {
                                        if (status.getUri().startsWith("http"))
                                            uri = status.getUri();
                                        else
                                            uri = status.getUrl();
                                    }
                                    APIResponse search = api.search(uri);
                                    if (search != null && search.getResults() != null) {
                                        remoteStatuses = search.getResults().getStatuses();
                                    }
                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Void result) {
                                    Intent intent = new Intent(contextReference.get(), TootActivity.class);
                                    Bundle b = new Bundle();
                                    if (remoteStatuses == null || remoteStatuses.size() == 0) {
                                        dialog.dismiss();
                                        intent.putExtras(b); //Put your id to your next Intent
                                        contextReference.get().startActivity(intent);
                                        return;
                                    }
                                    if (remoteStatuses.get(0).getReblog() != null) {
                                        b.putParcelable("tootReply", remoteStatuses.get(0).getReblog());
                                        b.putParcelable("idRedirect", status.getReblog());
                                    } else {
                                        b.putParcelable("tootReply", remoteStatuses.get(0));
                                        b.putParcelable("idRedirect", status);
                                    }
                                    b.putString("accountReplyToken", account.getToken());
                                    intent.putExtras(b); //Put your id to your next Intent
                                    contextReference.get().startActivity(intent);
                                    if (type == RetrieveFeedsAsyncTask.Type.CONTEXT) {
                                        try {
                                            //Avoid to open multi activities when replying in a conversation
                                            ((ShowConversationActivity) contextReference.get()).finish();
                                        } catch (Exception ignored) {
                                        }

                                    }
                                    dialog.dismiss();
                                }
                            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }else{
                            Intent intent = new Intent(context, TootActivity.class);
                            Bundle b = new Bundle();
                            b.putString("accountReplyToken", account.getToken());
                            intent.putExtras(b); //Put your id to your next Intent
                            context.startActivity(intent);
                        }

                    }
                });
                builderSingle.show();
            }else{
                SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
                String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
                String instance = sharedpreferences.getString(Helper.PREF_INSTANCE, null);
                SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                Account account = new AccountDAO(context, db).getUniqAccount(userId, instance);

                new AsyncTask<Void, Void, Void>() {
                    private List<app.fedilab.android.client.Entities.Status> remoteStatuses;
                    private WeakReference<Context> contextReference = new WeakReference<>(context);

                    @Override
                    protected Void doInBackground(Void... voids) {

                        API api = new API(contextReference.get(), account.getInstance(), account.getToken());
                        String uri;
                        if(status.getReblog() != null ){
                            if( status.getReblog().getUri().startsWith("http"))
                                uri = status.getReblog().getUri();
                            else
                                uri = status.getReblog().getUrl();
                        }else {
                            if( status.getUri().startsWith("http"))
                                uri = status.getUri();
                            else
                                uri = status.getUrl();
                        }
                        APIResponse search = api.search(uri);
                        if( search != null && search.getResults() != null){
                            remoteStatuses = search.getResults().getStatuses();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        Intent intent = new Intent(contextReference.get(), TootActivity.class);
                        Bundle b = new Bundle();
                        if( remoteStatuses == null || remoteStatuses.size() == 0){
                            return;
                        }
                        if( remoteStatuses.get(0).getReblog() != null ) {
                            b.putParcelable("tootReply", remoteStatuses.get(0).getReblog());
                            b.putParcelable("idRedirect", remoteStatuses.get(0).getReblog());
                        }else {
                            b.putParcelable("tootReply", remoteStatuses.get(0));
                            b.putParcelable("idRedirect", remoteStatuses.get(0));
                        }
                        b.putString("accountReplyToken", account.getToken());
                        intent.putExtras(b); //Put your id to your next Intent
                        contextReference.get().startActivity(intent);
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR );
            }
        }
    }


    public static void doCrossShare(final Context context, final Bundle bundle){
        List<Account> accounts = connectedAccounts(context, null, false);

        if( accounts.size() == 1) {
            Intent intentToot = new Intent(context, TootActivity.class);
            intentToot.putExtras(bundle);
            context.startActivity(intentToot);
            ((BaseActivity)context).finish();
        }else {
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(context, style);
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
                public void onClick(final DialogInterface dialog, int which) {
                    final Account account = accountArray[which];
                    Intent intentToot = new Intent(context, TootActivity.class);
                    bundle.putString("accountReplyToken", account.getToken());
                    intentToot.putExtras(bundle);
                    context.startActivity(intentToot);
                    ((BaseActivity)context).finish();
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
    private static void displayConfirmationDialog(final Context context, final API.StatusAction action, final Status status, final RecyclerView.Adapter baseAdapter, final OnPostActionInterface onPostActionInterface){

        String title = null;
        if( action == API.StatusAction.FAVOURITE){
            title = context.getString(R.string.favourite_add);
        }else if( action == API.StatusAction.UNFAVOURITE){
            title = context.getString(R.string.favourite_remove);
        }else if( action == API.StatusAction.REBLOG){
            title = context.getString(R.string.reblog_add);
        }else if(action == API.StatusAction.UNREBLOG){
            title = context.getString(R.string.reblog_remove);
        }else if ( action == API.StatusAction.PIN) {
            title = context.getString(R.string.pin_add);
        }else if (action == API.StatusAction.UNPIN) {
            title = context.getString(R.string.pin_remove);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context, style);

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
                        if( action == API.StatusAction.REBLOG || action == API.StatusAction.UNREBLOG)
                            reblogAction(context, status, onPostActionInterface);
                        else if( action == API.StatusAction.FAVOURITE || action == API.StatusAction.UNFAVOURITE)
                            favouriteAction(context, status, onPostActionInterface);
                        else if ( action == API.StatusAction.PIN || action == API.StatusAction.UNPIN)
                            pinAction(context, status, baseAdapter, onPostActionInterface);

                        if( action == API.StatusAction.FAVOURITE || action == API.StatusAction.UNFAVOURITE){
                            if (action == API.StatusAction.FAVOURITE) {
                                status.setFavourited(true);
                                status.setFavAnimated(true);
                            }else{
                                status.setFavourited(false);
                                status.setFavAnimated(false);
                            }
                            if(baseAdapter instanceof PixelfedListAdapter)
                                ((PixelfedListAdapter) baseAdapter).notifyStatusChanged(status);
                            else if(baseAdapter instanceof StatusListAdapter)
                                ((StatusListAdapter) baseAdapter).notifyStatusChanged(status);
                        }else if(action == API.StatusAction.REBLOG || action == API.StatusAction.UNREBLOG){
                            if (action == API.StatusAction.REBLOG) {
                                status.setReblogged(true);
                                status.setBoostAnimated(true);
                            }else{
                                status.setReblogged(false);
                                status.setBoostAnimated(false);
                            }
                            if(baseAdapter instanceof PixelfedListAdapter)
                                ((PixelfedListAdapter) baseAdapter).notifyStatusChanged(status);
                            else if(baseAdapter instanceof StatusListAdapter)
                                ((StatusListAdapter) baseAdapter).notifyStatusChanged(status);
                        }
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
     * Display a validation message
     * @param action int
     * @param status Status
     */
    private static void displayConfirmationDialogCrossAction(final Context context,Account currentAccount, final API.StatusAction action, final Status status, final OnPostActionInterface onPostActionInterface, final RecyclerView.Adapter baseAdapter){

        String title = null;
        if( action == API.StatusAction.FAVOURITE){
            title = context.getString(R.string.favourite_add);
        }else if( action == API.StatusAction.UNFAVOURITE){
            title = context.getString(R.string.favourite_remove);
        }else if( action == API.StatusAction.REBLOG){
            title = context.getString(R.string.reblog_add);
        }else if(action == API.StatusAction.UNREBLOG){
            title = context.getString(R.string.reblog_remove);
        }else if ( action == API.StatusAction.PIN) {
            title = context.getString(R.string.pin_add);
        }else if (action == API.StatusAction.UNPIN) {
            title = context.getString(R.string.pin_remove);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context, style);

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
                        if( action == API.StatusAction.FAVOURITE || action == API.StatusAction.UNFAVOURITE){
                            if (action == API.StatusAction.FAVOURITE) {
                                status.setFavourited(true);
                                status.setFavAnimated(true);
                            }else{
                                status.setFavourited(false);
                                status.setFavAnimated(false);
                            }
                            if(baseAdapter instanceof PixelfedListAdapter)
                                ((PixelfedListAdapter) baseAdapter).notifyStatusChanged(status);
                            else if(baseAdapter instanceof StatusListAdapter)
                                ((StatusListAdapter) baseAdapter).notifyStatusChanged(status);
                        }else if(action == API.StatusAction.REBLOG || action == API.StatusAction.UNREBLOG){
                            if (action == API.StatusAction.REBLOG) {
                                status.setReblogged(true);
                                status.setBoostAnimated(true);
                            }else{
                                status.setReblogged(false);
                                status.setBoostAnimated(false);
                            }
                            if(baseAdapter instanceof PixelfedListAdapter)
                                ((PixelfedListAdapter) baseAdapter).notifyStatusChanged(status);
                            else if(baseAdapter instanceof StatusListAdapter)
                                ((StatusListAdapter) baseAdapter).notifyStatusChanged(status);
                        }
                        new PostActionAsyncTask(context, currentAccount, status, action, onPostActionInterface).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
     * Follow/Unfollow an account
     * @param account Account
     */
    private static void followAction(Context context, Account account, RecyclerView.Adapter baseAdapter, OnPostActionInterface onPostActionInterface){
        if( account.isFollowing()){
            new PostActionAsyncTask(context, API.StatusAction.UNFOLLOW, account.getId(), onPostActionInterface).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            account.setFollowing(false);
        }else{
            new PostActionAsyncTask(context, API.StatusAction.FOLLOW, account.getId(), onPostActionInterface).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            account.setFollowing(true);
        }
        baseAdapter.notifyDataSetChanged();
    }

    /**
     * Favourites/Unfavourites a status
     * @param status Status
     */
    private static void favouriteAction(Context context, Status status,OnPostActionInterface onPostActionInterface){

        if( status.isFavourited() || (status.getReblog() != null && status.getReblog().isFavourited())){
            new PostActionAsyncTask(context, API.StatusAction.UNFAVOURITE, status.getId(), onPostActionInterface).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            status.setFavourited(false);
        }else{
            new PostActionAsyncTask(context, API.StatusAction.FAVOURITE, status.getId(), onPostActionInterface).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            status.setFavourited(true);
        }
    }

    /**
     * Reblog/Unreblog a status
     * @param status Status
     */
    private static void reblogAction(Context context, Status status,  OnPostActionInterface onPostActionInterface){
        if( status.isReblogged() || (status.getReblog()!= null && status.getReblog().isReblogged())){
            String statusId = status.getReblog()!=null?status.getReblog().getId():status.getId();
            new PostActionAsyncTask(context, API.StatusAction.UNREBLOG, statusId, onPostActionInterface).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            status.setReblogged(false);
        }else{
            new PostActionAsyncTask(context, API.StatusAction.REBLOG, status.getId(), onPostActionInterface).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            status.setReblogged(true);
        }
    }

    /**
     * Pin or unpin a status
     * @param status Status
     */
    private static void pinAction(Context context, Status status, RecyclerView.Adapter baseAdapter, OnPostActionInterface onPostActionInterface) {

        if (status.isPinned()) {
            new PostActionAsyncTask(context, API.StatusAction.UNPIN, status.getId(), onPostActionInterface).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            status.setPinned(false);
        } else {
            new PostActionAsyncTask(context, API.StatusAction.PIN, status.getId(), onPostActionInterface).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            status.setPinned(true);
        }
        baseAdapter.notifyDataSetChanged();
    }




}
