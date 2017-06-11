/* Copyright 2017 Thomas Schneider
 *
 * This file is a part of Mastodon Etalab for mastodon.etalab.gouv.fr
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Mastodon Etalab is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Thomas Schneider; if not,
 * see <http://www.gnu.org/licenses>. */


package fr.gouv.etalab.mastodon.helper;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.BuildConfig;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;
import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import fr.gouv.etalab.mastodon.activities.LoginActivity;
import fr.gouv.etalab.mastodon.activities.ShowAccountActivity;
import fr.gouv.etalab.mastodon.asynctasks.RemoveAccountAsyncTask;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import mastodon.etalab.gouv.fr.mastodon.R;
import fr.gouv.etalab.mastodon.client.API;

import static android.app.Notification.DEFAULT_SOUND;
import static android.app.Notification.DEFAULT_VIBRATE;
import static android.content.Context.DOWNLOAD_SERVICE;


/**
 * Created by Thomas on 23/04/2017.
 * - Constants are defined here.
 * - Reusable methods are implemented in this section
 */

public class Helper {


    @SuppressWarnings("unused")
    public static  final String TAG = "mastodon_etalab";
    public static final String OAUTH_REDIRECT_HOST = "fr.gouv.etalab.mastodon";
    public static final String INSTANCE = "mastodon.etalab.gouv.fr";
    public static final String OAUTH_SCOPES = "read write follow";
    public static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
    public static final String PREF_KEY_ID = "userID";
    public static final String REDIRECT_CONTENT = "urn:ietf:wg:oauth:2.0:oob";
    public static final String REDIRECT_CONTENT_WEB = "mastalab://backtomastalab";
    public static final int EXTERNAL_STORAGE_REQUEST_CODE = 84;

    //Some definitions
    public static final String CLIENT_NAME = "client_name";
    public static final String APP_PREFS = "app_prefs";
    public static final String ID = "id";
    public static final String CLIENT_ID = "client_id";
    public static final String CLIENT_SECRET = "client_secret";
    public static final String REDIRECT_URI = "redirect_uri";
    public static final String REDIRECT_URIS = "redirect_uris";
    public static final String RESPONSE_TYPE = "response_type";
    public static final String SCOPE = "scope";
    public static final String SCOPES = "scopes";
    public static final String WEBSITE = "website";
    public static final String LAST_NOTIFICATION_MAX_ID = "last_notification_max_id";
    public static final String LAST_HOMETIMELINE_MAX_ID = "last_hometimeline_max_id";
    public static final String CLIP_BOARD = "clipboard";
    //Notifications
    public static final int NOTIFICATION_INTENT = 1;
    public static final int HOME_TIMELINE_INTENT = 2;

    //Settings
    public static final String SET_TOOTS_PER_PAGE = "set_toots_per_page";
    public static final String SET_ACCOUNTS_PER_PAGE = "set_accounts_per_page";
    public static final String SET_NOTIFICATIONS_PER_PAGE = "set_notifications_per_page";
    public static final String SET_ATTACHMENT_ACTION = "set_attachment_action";
    public static final int ATTACHMENT_ALWAYS = 1;
    public static final int ATTACHMENT_WIFI = 2;
    public static final int ATTACHMENT_ASK = 3;


    public static final String SET_NOTIF_FOLLOW = "set_notif_follow";
    public static final String SET_NOTIF_ADD = "set_notif_follow_add";
    public static final String SET_NOTIF_ASK = "set_notif_follow_ask";
    public static final String SET_NOTIF_MENTION = "set_notif_follow_mention";
    public static final String SET_NOTIF_SHARE = "set_notif_follow_share";
    public static final String SET_NOTIF_VALIDATION = "set_share_validation";
    public static final String SET_WIFI_ONLY = "set_wifi_only";
    public static final String SET_NOTIF_HOMETIMELINE = "set_notif_hometimeline";
    public static final String SET_NOTIF_SILENT = "set_notif_silent";
    public static final String SET_SHOW_REPLY = "set_show_reply";
    public static final String SET_SHOW_ERROR_MESSAGES = "set_show_error_messages";
    //End points
    public static final String EP_AUTHORIZE = "/oauth/authorize";


    //Refresh job
    public static final int MINUTES_BETWEEN_NOTIFICATIONS_REFRESH = 15;
    public static final int MINUTES_BETWEEN_HOME_TIMELINE = 30;

    //Intent
    public static final String INTENT_ACTION = "intent_action";

    //Receiver
    public static final String SEARCH_VALIDATE_ACCOUNT = "search_validate_account";
    public static final String HEADER_ACCOUNT = "header_account";

    //User agent
    public static final String USER_AGENT = "Mastalab/"+ BuildConfig.VERSION_NAME + " Android/"+ Build.VERSION.RELEASE;


    private static boolean menuAccountsOpened = false;

    /***
     *  Check if the user is connected to Internet
     * @return boolean
     */
    @SuppressWarnings("unused")
    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if ( ni != null && ni.isConnected()) {
            try {
                //Google is used for the ping
                InetAddress ipAddr = InetAddress.getByName("google.com");
                return !ipAddr.toString().equals("");
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Returns boolean depending if the user is authenticated
     * @param context Context
     * @return boolean
     */
    public static boolean isLoggedIn(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String prefKeyOauthTokenT = sharedpreferences.getString(PREF_KEY_OAUTH_TOKEN, null);
        return ( prefKeyOauthTokenT != null);
    }

    /**
     * Log out the authenticated user by removing its token
     * @param context Context
     */
    public static void logout(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(Helper.PREF_KEY_OAUTH_TOKEN, null);
        editor.putString(Helper.CLIENT_ID, null);
        editor.putString(Helper.CLIENT_SECRET, null);
        editor.putString(Helper.PREF_KEY_ID, null);
        editor.putString(Helper.ID, null);
        editor.apply();
    }


    /**
     * Convert String date from Mastodon
     * @param context Context
     * @param date String
     * @return Date
     */
    public static Date mstStringToDate(Context context, String date){
        Locale userLocale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            userLocale = context.getResources().getConfiguration().getLocales().get(0);
        } else {
            //noinspection deprecation
            userLocale = context.getResources().getConfiguration().locale;
        }
        final String STRING_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(STRING_DATE_FORMAT, userLocale);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("gmt"));
        simpleDateFormat.setLenient(true);
        try {
            return simpleDateFormat.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }


    /**
     * Convert a date in String -> format yyyy-MM-dd HH:mm:ss
     * @param context Context
     * @param date Date
     * @return String
     */
    public static String dateToString(Context context, Date date) {
        Locale userLocale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            userLocale = context.getResources().getConfiguration().getLocales().get(0);
        } else {
            //noinspection deprecation
            userLocale = context.getResources().getConfiguration().locale;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",userLocale);
        return dateFormat.format(date);
    }

    /**
     * Convert String date from db to Date Object
     * @param stringDate date to convert
     * @return Date
     */
    public static Date stringToDate(Context context, String stringDate) {
        if( stringDate == null)
            return null;
        Locale userLocale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            userLocale = context.getResources().getConfiguration().getLocales().get(0);
        } else {
            //noinspection deprecation
            userLocale = context.getResources().getConfiguration().locale;
        }
        SimpleDateFormat  dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",userLocale);
        Date date = null;
        try {
            date = dateFormat.parse(stringDate);
        } catch (java.text.ParseException ignored) {

        }
        return date;
    }

    /**
     * Check if WIFI is opened
     * @param context Context
     * @return boolean
     */
    public static boolean isOnWIFI(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connManager.getActiveNetworkInfo();
        return (activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI);
    }


    /***
     * Returns a String depending of the date
     * @param context Context
     * @param dateToot Date
     * @return String
     */
    public static String dateDiff(Context context, Date dateToot){
        Date now = new Date();
        long diff = now.getTime() - dateToot.getTime();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long months = days / 30;
        long years = days / 365;

        if( years > 0)
            return context.getResources().getQuantityString(R.plurals.date_year, (int)years, (int)years);
        else if( months > 0)
            return context.getResources().getQuantityString(R.plurals.date_month, (int)months, (int)months);
        else if( days > 2)
            return context.getString(R.string.date_day,days);
        else if(days == 2 )
            return context.getString(R.string.date_day_before_yesterday);
        else if(days == 1 )
            return context.getString(R.string.date_yesterday);
        else if(hours > 0)
            return context.getResources().getQuantityString(R.plurals.date_hours, (int)hours, (int)hours);
        else if(minutes > 0)
            return context.getResources().getQuantityString(R.plurals.date_minutes, (int)minutes, (int)minutes);
        else
            return context.getResources().getQuantityString(R.plurals.date_seconds, (int)seconds, (int)seconds);
    }

    /***
     * Toast message depending of the status code and the initial action
     * @param context Context
     * @param statusCode int the status code
     * @param statusAction API.StatusAction the initial action
     */
    public static void manageMessageStatusCode(Context context, int statusCode,API.StatusAction statusAction){
        String message = "";
        if( statusCode == 200){
            if( statusAction == API.StatusAction.BLOCK){
                message = context.getString(R.string.toast_block);
            }else if(statusAction == API.StatusAction.UNBLOCK){
                message = context.getString(R.string.toast_unblock);
            }else if(statusAction == API.StatusAction.REBLOG){
                message = context.getString(R.string.toast_reblog);
            }else if(statusAction == API.StatusAction.UNREBLOG){
                message = context.getString(R.string.toast_unreblog);
            }else if(statusAction == API.StatusAction.MUTE){
                message = context.getString(R.string.toast_mute);
            }else if(statusAction == API.StatusAction.UNMUTE){
                message = context.getString(R.string.toast_unmute);
            }else if(statusAction == API.StatusAction.FOLLOW){
                message = context.getString(R.string.toast_follow);
            }else if(statusAction == API.StatusAction.UNFOLLOW){
                message = context.getString(R.string.toast_unfollow);
            }else if(statusAction == API.StatusAction.FAVOURITE){
                message = context.getString(R.string.toast_favourite);
            }else if(statusAction == API.StatusAction.UNFAVOURITE){
                message = context.getString(R.string.toast_unfavourite);
            }else if(statusAction == API.StatusAction.REPORT){
                message = context.getString(R.string.toast_report);
            }else if(statusAction == API.StatusAction.UNSTATUS){
                message = context.getString(R.string.toast_unstatus);
            }   
        }else {
            message = context.getString(R.string.toast_error);
        }
        if( !message.trim().equals(""))
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }


    /**
     * Manage downloads with URLs
     * @param context Context
     * @param url String download url
     */
    public static void manageDownloads(final Context context, final String url){

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final DownloadManager.Request request;
        try {
            request = new DownloadManager.Request(Uri.parse(url.trim()));
        }catch (Exception e){
            Toast.makeText(context,R.string.toast_error,Toast.LENGTH_LONG).show();
            return;
        }
        Uri uri =  Uri.parse(url);
        File f = new File("" + uri);
        final String fileName = f.getName();
        builder.setMessage(context.getResources().getString(R.string.download_file, fileName));
        builder.setCancelable(false)
                .setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        request.allowScanningByMediaScanner();
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,fileName);
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        DownloadManager dm = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
                        dm.enqueue(request);
                        dialog.dismiss();
                    }

                })
                .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        if( alert.getWindow() != null )
            alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alert.show();
    }


    /**
     * Sends notification with intent
     * @param context Context
     * @param intent Intent associated to the notifcation
     * @param notificationId int id of the notification
     * @param icon Bitmap profile picture
     * @param title String title of the notification
     * @param message String message for the notification
     */
    public static void notify_user(Context context, Intent intent, int notificationId, Bitmap icon, String title, String message ) {
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        // prepare intent which is triggered if the user click on the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        PendingIntent pIntent = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_ONE_SHOT);

        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        // build notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.notification_icon)
                .setTicker(message)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentIntent(pIntent)
                .setContentText(message);
        if( sharedpreferences.getBoolean(Helper.SET_NOTIF_SILENT,false) ) {
            notificationBuilder.setDefaults(DEFAULT_VIBRATE);
        }else {
            notificationBuilder.setDefaults(DEFAULT_SOUND);
        }
        notificationBuilder.setContentTitle(title);
        notificationBuilder.setLargeIcon(icon);
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    /**
     * Returns the instance of the authenticated user
     * @param context Context
     * @return String domain instance
     */
    public static String getLiveInstance(Context context){
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        if( userId == null) //User not authenticated
            return Helper.INSTANCE;
        Account account = new AccountDAO(context, db).getAccountByID(userId);
        if( account != null){
            return account.getInstance().trim();
        } //User not in db
        else return Helper.INSTANCE;
    }


    /**
     * Converts dp to pixel
     * @param dp float - the value in dp to convert
     * @param context Context
     * @return float - the converted value in pixel
     */
    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }


    /**
     * Toggle for the menu (ie: main menu or accounts menu)
     * @param activity Activity
     */
    public static void menuAccounts(final Activity activity){

        final NavigationView navigationView = (NavigationView) activity.findViewById(R.id.nav_view);
        SharedPreferences mSharedPreferences = activity.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String currrentUserId = mSharedPreferences.getString(Helper.PREF_KEY_ID, null);
        final ImageView arrow  = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.owner_accounts);
        if( currrentUserId == null)
            return;

        if( !menuAccountsOpened ){

            arrow.setImageResource(R.drawable.ic_arrow_drop_up);

            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.menu_accounts);

            SQLiteDatabase db = Sqlite.getInstance(activity, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();

            final List<Account> accounts = new AccountDAO(activity, db).getAllAccount();
            navigationView.setItemIconTintList(null);
            for(final Account account: accounts) {
                if( !currrentUserId.equals(account.getId()) ) {
                    final MenuItem item = navigationView.getMenu().add("@" + account.getAcct() + "@" + account.getInstance());
                    ImageLoader imageLoader;
                    DisplayImageOptions options = new DisplayImageOptions.Builder().displayer(new SimpleBitmapDisplayer()).cacheInMemory(false)
                            .cacheOnDisk(true).resetViewBeforeLoading(true).build();
                    imageLoader = ImageLoader.getInstance();
                    final ImageView imageView = new ImageView(activity);
                    item.setIcon(R.drawable.ic_person);
                    imageLoader.displayImage(account.getAvatar(), imageView, options, new ImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String s, View view) {
                        }

                        @Override
                        public void onLoadingFailed(String s, View view, FailReason failReason) {
                        }

                        @Override
                        public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                            item.setIcon(new BitmapDrawable(activity.getResources(), bitmap));
                        }

                        @Override
                        public void onLoadingCancelled(String s, View view) {
                        }
                    });

                    item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if( ! activity.isFinishing() ) {
                                menuAccountsOpened = false;
                                String userId = account.getId();
                                Toast.makeText(activity, activity.getString(R.string.toast_account_changed, "@" + account.getAcct() + "@" + account.getInstance()), Toast.LENGTH_LONG).show();
                                changeUser(activity, userId);
                                arrow.setImageResource(R.drawable.ic_arrow_drop_down);
                                return true;
                            }
                            return false;
                        }
                    });
                    item.setActionView(R.layout.update_account);
                    ImageView deleteButton = (ImageView) item.getActionView().findViewById(R.id.account_remove_button);
                    deleteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new AlertDialog.Builder(activity)
                                    .setTitle(activity.getString(R.string.delete_account_title))
                                    .setMessage(activity.getString(R.string.delete_account_message, "@" + account.getAcct() + "@" + account.getInstance()))
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            new RemoveAccountAsyncTask(activity, account).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                            item.setVisible(false);
                                        }
                                    })
                                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            // do nothing
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        }
                    });

                }
            }
            MenuItem addItem = navigationView.getMenu().add(R.string.add_account);
            addItem.setIcon(R.drawable.ic_person_add);
            addItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    Intent intent = new Intent(activity, LoginActivity.class);
                    intent.putExtra("addAccount", true);
                    activity.startActivity(intent);
                    return true;
                }
            });
        }else{
            arrow.setImageResource(R.drawable.ic_arrow_drop_down);
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.activity_main_drawer);
            final SharedPreferences sharedpreferences = activity.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            SQLiteDatabase db = Sqlite.getInstance(activity, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
            String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
            Account account = new AccountDAO(activity, db).getAccountByID(userId);
            if( account != null) {
                if (account.isLocked()) {
                    navigationView.getMenu().findItem(R.id.nav_follow_request).setVisible(true);
                } else {
                    navigationView.getMenu().findItem(R.id.nav_follow_request).setVisible(false);
                }
            }
        }
        menuAccountsOpened = !menuAccountsOpened;

    }

    /**
     * Changes the user in shared preferences
     * @param activity Activity
     * @param userID String - the new user id
     */
    public static void changeUser(Activity activity, String userID) {

        final NavigationView navigationView = (NavigationView) activity.findViewById(R.id.nav_view);
        navigationView.getMenu().clear();
        navigationView.inflateMenu(R.menu.activity_main_drawer);
        navigationView.setCheckedItem(R.id.nav_home);
        navigationView.getMenu().performIdentifierAction(R.id.nav_home, 0);
        SQLiteDatabase db = Sqlite.getInstance(activity, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        Account account = new AccountDAO(activity,db).getAccountByID(userID);
        //Locked account can see follow request
        if (account.isLocked()) {
            navigationView.getMenu().findItem(R.id.nav_follow_request).setVisible(true);
        } else {
            navigationView.getMenu().findItem(R.id.nav_follow_request).setVisible(false);
        }
        SharedPreferences sharedpreferences = activity.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(Helper.PREF_KEY_OAUTH_TOKEN, account.getToken());
        editor.putString(Helper.PREF_KEY_ID, account.getId());
        editor.apply();
        ImageLoader imageLoader;
        DisplayImageOptions options = new DisplayImageOptions.Builder().displayer(new SimpleBitmapDisplayer()).cacheInMemory(false)
                .cacheOnDisk(true).resetViewBeforeLoading(true).build();
        imageLoader = ImageLoader.getInstance();
        View headerLayout = navigationView.getHeaderView(0);
        updateHeaderAccountInfo(activity, account, headerLayout, imageLoader, options);
    }


    /**
     * Update the header with the new selected account
     * @param activity Activity
     * @param account Account - new account in use
     * @param headerLayout View - the menu header
     * @param imageLoader ImageLoader - instance of ImageLoader
     * @param options DisplayImageOptions - current configuration of ImageLoader
     */
    public static void updateHeaderAccountInfo(final Activity activity, final Account account, View headerLayout, ImageLoader imageLoader, DisplayImageOptions options){
        ImageView profilePicture = (ImageView) headerLayout.findViewById(R.id.profilePicture);
        TextView username = (TextView) headerLayout.findViewById(R.id.username);
        TextView displayedName = (TextView) headerLayout.findViewById(R.id.displayedName);
        TextView ownerStatus = (TextView) headerLayout.findViewById(R.id.owner_status);
        TextView ownerFollowing = (TextView) headerLayout.findViewById(R.id.owner_following);
        TextView ownerFollowers = (TextView) headerLayout.findViewById(R.id.owner_followers);
        if( account == null ) {
            Helper.logout(activity);
            Intent myIntent = new Intent(activity, LoginActivity.class);
            Toast.makeText(activity,R.string.toast_error, Toast.LENGTH_LONG).show();
            activity.startActivity(myIntent);
            activity.finish(); //User is logged out to get a new token
        }else {
            ownerStatus.setText(String.valueOf(account.getStatuses_count()));
            ownerFollowers.setText(String.valueOf(account.getFollowers_count()));
            ownerFollowing.setText(String.valueOf(account.getFollowing_count()));
            username.setText(String.format("@%s",account.getUsername()));
            displayedName.setText(account.getDisplay_name());
            imageLoader.displayImage(account.getAvatar(), profilePicture, options);
        }
        profilePicture.setOnClickListener(null);
        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (account != null) {
                    Intent intent = new Intent(activity, ShowAccountActivity.class);
                    Bundle b = new Bundle();
                    b.putString("accountId", account.getId());
                    intent.putExtras(b);
                    activity.startActivity(intent);
                }
            }
        });
    }
}
