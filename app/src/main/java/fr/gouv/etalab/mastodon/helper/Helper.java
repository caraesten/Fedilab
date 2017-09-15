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


package fr.gouv.etalab.mastodon.helper;


import android.app.Activity;
import android.app.NotificationManager;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.BuildConfig;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.gouv.etalab.mastodon.activities.EditProfileActivity;
import fr.gouv.etalab.mastodon.activities.HashTagActivity;
import fr.gouv.etalab.mastodon.activities.LoginActivity;
import fr.gouv.etalab.mastodon.activities.MainActivity;
import fr.gouv.etalab.mastodon.activities.ShowAccountActivity;
import fr.gouv.etalab.mastodon.activities.TootActivity;
import fr.gouv.etalab.mastodon.activities.WebviewActivity;
import fr.gouv.etalab.mastodon.asynctasks.RemoveAccountAsyncTask;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Mention;
import fr.gouv.etalab.mastodon.client.Entities.Notification;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.client.PatchBaseImageDownloader;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import mastodon.etalab.gouv.fr.mastodon.R;

import static android.content.Context.DOWNLOAD_SERVICE;


/**
 * Created by Thomas on 23/04/2017.
 * - Constants are defined here.
 * - Reusable methods are implemented in this section
 */

public class Helper {


    @SuppressWarnings("unused")
    public static  final String TAG = "mastodon_etalab";
    public static final String CLIENT_NAME_VALUE = "Mastalab";
    public static final String DEVELOPER_INSTANCE = "mastodon.etalab.gouv.fr";
    public static final String OAUTH_SCOPES = "read write follow";
    public static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
    public static final String PREF_KEY_ID = "userID";
    public static final String REDIRECT_CONTENT = "urn:ietf:wg:oauth:2.0:oob";
    public static final String REDIRECT_CONTENT_WEB = "mastalab://backtomastalab";
    public static final int EXTERNAL_STORAGE_REQUEST_CODE = 84;
    public static final int REQ_CODE_SPEECH_INPUT = 132;

    //Thekinrar's API: https://instances.social/api/doc/
    public static final String THEKINRAR_SECRET_TOKEN = "jGj9gW3z9ptyIpB8CMGhAlTlslcemMV6AgoiImfw3vPP98birAJTHOWiu5ZWfCkLvcaLsFZw9e3Pb7TIwkbIyrj3z6S7r2oE6uy6EFHvls3YtapP8QKNZ980p9RfzTb4";

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
    public static final String WEBSITE_VALUE = "https://tom79.bitbucket.io/";
    public static final String SHOW_BATTERY_SAVER_MESSAGE = "show_battery_saver_message";
    public static final String LAST_NOTIFICATION_MAX_ID = "last_notification_max_id";
    public static final String LAST_HOMETIMELINE_MAX_ID = "last_hometimeline_max_id";
    public static final String CLIP_BOARD = "clipboard";
    //Notifications
    public static final int NOTIFICATION_INTENT = 1;
    public static final int HOME_TIMELINE_INTENT = 2;
    public static final int CHANGE_THEME_INTENT = 3;
    public static final int CHANGE_USER_INTENT = 4;
    //Settings
    public static final String SET_TOOTS_PER_PAGE = "set_toots_per_page";
    public static final String SET_ACCOUNTS_PER_PAGE = "set_accounts_per_page";
    public static final String SET_NOTIFICATIONS_PER_PAGE = "set_notifications_per_page";
    public static final String SET_ATTACHMENT_ACTION = "set_attachment_action";
    public static final String SET_THEME = "set_theme";
    public static final String SET_TIME_FROM = "set_time_from";
    public static final String SET_TIME_TO = "set_time_to";
    public static final String SET_AUTO_STORE = "set_auto_store";
    public static final String SET_POPUP_PUSH = "set_popup_push";
    public static final String SET_NSFW_TIMEOUT = "set_nsfw_timeout";
    public static final String SET_TABS = "set_tabs";
    public static final String SET_MEDIA_URLS = "set_media_urls";
    public static final String SET_TEXT_SIZE = "set_text_size";
    public static final String SET_ICON_SIZE = "set_icon_size";
    public static final String SET_PREVIEW_REPLIES = "set_preview_replies";
    public static final String SET_PREVIEW_REPLIES_PP = "set_preview_replies_pp";
    public static final String SET_TRANSLATOR = "set_translator";
    public static final String SET_LED_COLOUR = "set_led_colour";
    private static final String SET_TEMP_STATUS = "set_temp_status";
    private static final String SET_TEMP_NOTIFICATIONS = "set_temp_notifications";

    public static final int ATTACHMENT_ALWAYS = 1;
    public static final int ATTACHMENT_WIFI = 2;
    public static final int ATTACHMENT_ASK = 3;
    public static final int THEME_LIGHT = 1;
    public static final int THEME_DARK = 2;
    public static final int THEME_TABS = 1;
    public static final int THEME_MENU = 2;
    public static final int THEME_MENU_TABS = 3;

    public static final int LED_COLOUR = 0;

    public static final int TRANS_YANDEX = 0;
    public static final int TRANS_GOOGLE = 1;
    public static final int TRANS_NONE = 2;

    public static final String SET_NOTIF_FOLLOW = "set_notif_follow";
    public static final String SET_NOTIF_ADD = "set_notif_follow_add";
    public static final String SET_NOTIF_ASK = "set_notif_follow_ask";
    public static final String SET_NOTIF_MENTION = "set_notif_follow_mention";
    public static final String SET_NOTIF_SHARE = "set_notif_follow_share";
    public static final String SET_NOTIF_VALIDATION = "set_share_validation";
    public static final String SET_NOTIF_VALIDATION_FAV = "set_share_validation_fav";
    public static final String SET_WIFI_ONLY = "set_wifi_only";
    public static final String SET_NOTIF_HOMETIMELINE = "set_notif_hometimeline";
    public static final String SET_NOTIF_SILENT = "set_notif_silent";
    public static final String SET_SHOW_ERROR_MESSAGES = "set_show_error_messages";
    public static final String SET_EMBEDDED_BROWSER = "set_embedded_browser";
    public static final String SET_JAVASCRIPT = "set_javascript";
    public static final String SET_COOKIES = "set_cookies";
    public static final String SET_FOLDER_RECORD = "set_folder_record";
    public static final String SET_TOOT_VISIBILITY = "set_toot_visibility";
    public static final String SET_DISPLAY_LOCAL = "set_display_local";
    public static final String SET_DISPLAY_GLOBAL = "set_display_global";

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
    public static final String RECEIVE_DATA = "receive_data";
    public static final String RECEIVE_PICTURE = "receive_picture";
    //User agent
    public static final String USER_AGENT = "Mastalab/"+ BuildConfig.VERSION_NAME + " Android/"+ Build.VERSION.RELEASE;


    private static boolean menuAccountsOpened = false;

    public static boolean canPin;

    private static final Pattern SHORTNAME_PATTERN = Pattern.compile(":( |)([-+\\w]+):");

    public static final Pattern urlPattern = Pattern.compile(
            "(?i)\\b((?:[a-z][\\w-]+:(?:/{1,3}|[a-z0-9%])|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,10}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    public static final Pattern hashtagPattern = Pattern.compile("(#[\\w_À-ú-]{1,})");
    /**
     * Converts emojis in input to unicode
     * @param input String
     * @param removeIfUnsupported boolean
     * @return String
     */
    public static String shortnameToUnicode(String input, boolean removeIfUnsupported) {
        Matcher matcher = SHORTNAME_PATTERN.matcher(input);

        boolean supported = Build.VERSION.SDK_INT >= 16;
        while (matcher.find()) {
            String unicode = emoji.get(matcher.group(2));
            if (unicode == null) {
                continue;
            }
            if (supported) {
                if (matcher.group(1).equals(" "))
                    input = input.replace(": " + matcher.group(2) + ":", unicode);
                else
                    input = input.replace(":" + matcher.group(2) + ":", unicode);
            } else if (removeIfUnsupported) {
                if (matcher.group(1).equals(" "))
                    input = input.replace(": " + matcher.group(2) + ":", unicode);
                else
                    input = input.replace(":" + matcher.group(2) + ":", "");
            }
        }

        return input;
    }
    //Emoji manager
    private static Map<String, String> emoji = new HashMap<>();

    public static void fillMapEmoji(Context context) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(context.getAssets().open("emoji.csv")));
            String line;
            while( (line = br.readLine()) != null) {
                String str[] = line.split(",");
                String unicode = null;
                if(str.length == 2)
                    unicode =  new String(new int[] {Integer.parseInt(str[1].replace("0x","").trim(), 16)}, 0, 1);
                else if(str.length == 3)
                    unicode =  new String(new int[] {Integer.parseInt(str[1].replace("0x","").trim(), 16), Integer.parseInt(str[2].replace("0x","").trim(), 16)}, 0, 2);
                else if(str.length == 4)
                    unicode =  new String(new int[] {Integer.parseInt(str[1].replace("0x","").trim(), 16), Integer.parseInt(str[2].replace("0x","").trim(), 16), Integer.parseInt(str[3].replace("0x","").trim(), 16)}, 0, 3);
                else if(str.length == 5)
                    unicode =  new String(new int[] {Integer.parseInt(str[1].replace("0x","").trim(), 16), Integer.parseInt(str[2].replace("0x","").trim(), 16), Integer.parseInt(str[3].replace("0x","").trim(), 16), Integer.parseInt(str[4].replace("0x","").trim(), 16)}, 0, 4);
                if( unicode != null)
                    emoji.put(str[0],unicode);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
            }else if(statusAction == API.StatusAction.PIN){
                message = context.getString(R.string.toast_pin);
            }else if (statusAction == API.StatusAction.UNPIN){
                message = context.getString(R.string.toast_unpin);
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
        final String fileName = URLUtil.guessFileName(url, null, null);
        builder.setMessage(context.getResources().getString(R.string.download_file, fileName));
        builder.setCancelable(false)
                .setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        request.allowScanningByMediaScanner();
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
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

    private static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
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
        intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
            notificationBuilder.setVibrate(new long[] { 500, 500, 500});
        }else {
            String soundUri = ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.getPackageName() +"/";
            notificationBuilder.setSound(Uri.parse(soundUri + R.raw.boop));
        }

        int ledColour = Color.BLUE;

        switch (sharedpreferences.getInt(Helper.SET_LED_COLOUR, Helper.LED_COLOUR)) {
            case 0: // BLUE
                ledColour = Color.BLUE;
                break;
            case 1: // CYAN
                ledColour = Color.CYAN;
                break;
            case 2: // MAGENTA
                ledColour = Color.MAGENTA;
                break;
            case 3: // GREEN
                ledColour = Color.GREEN;
                break;
            case 4: // RED
                ledColour = Color.RED;
                break;
            case 5: // YELLOW
                ledColour = Color.YELLOW;
                break;
            case 6: // WHITE
                ledColour = Color.WHITE;
                break;
        }

        notificationBuilder.setLights(ledColour, 500, 1000);
        notificationBuilder.setContentTitle(title);
        notificationBuilder.setLargeIcon(icon);
        notificationManager.notify(notificationId, notificationBuilder.build());
    }


    /**
     * Manage downloads with URLs
     * @param context Context
     * @param url String download url
     */
    public static void manageMoveFileDownload(final Context context, final String preview_url, final String url, Bitmap bitmap, File fileVideo){

        final String fileName = URLUtil.guessFileName(url, null, null);final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String myDir = sharedpreferences.getString(Helper.SET_FOLDER_RECORD, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
 
        try {
            File file;
            if( bitmap != null) {
                file = new File(myDir, fileName);
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                byte[] bitmapdata = bos.toByteArray();

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(bitmapdata);
                fos.flush();
                fos.close();
            }else{
                File fileVideoTargeded = new File(myDir, fileName);
                copy(fileVideo, fileVideoTargeded);
                file = fileVideoTargeded;
            }
            Random r = new Random();
            final int notificationIdTmp = r.nextInt(10000);
            // prepare intent which is triggered if the
            // notification is selected
            final Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            Uri uri = Uri.parse("file://" + file.getAbsolutePath());
            intent.setDataAndType(uri, getMimeType(url));

            DisplayImageOptions options = new DisplayImageOptions.Builder().displayer(new SimpleBitmapDisplayer()).cacheInMemory(false)
                    .cacheOnDisk(true).resetViewBeforeLoading(true).build();
            ImageLoader imageLoaderNoty = ImageLoader.getInstance();
            File cacheDir = new File(context.getCacheDir(), context.getString(R.string.app_name));
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                    .imageDownloader(new PatchBaseImageDownloader(context))
                    .threadPoolSize(5)
                    .threadPriority(Thread.MIN_PRIORITY + 3)
                    .denyCacheImageMultipleSizesInMemory()
                    .diskCache(new UnlimitedDiskCache(cacheDir))
                    .build();
            imageLoaderNoty.init(config);
            imageLoaderNoty.loadImage(preview_url, options, new SimpleImageLoadingListener(){
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    super.onLoadingComplete(imageUri, view, loadedImage);
                    notify_user(context, intent, notificationIdTmp, loadedImage, context.getString(R.string.save_over), context.getString(R.string.download_from, fileName));
                    Toast.makeText(context, R.string.toast_saved,Toast.LENGTH_LONG).show();
                }
                @Override
                public void onLoadingFailed(java.lang.String imageUri, android.view.View view, FailReason failReason){
                    notify_user(context, intent, notificationIdTmp, BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.ic_save), context.getString(R.string.save_over), context.getString(R.string.download_from, fileName));
                    Toast.makeText(context, R.string.toast_saved,Toast.LENGTH_LONG).show();
                }});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Copy a file by transferring bytes from in to out
     * @param src File source file
     * @param dst File targeted file
     * @throws IOException Exception
     */
    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }catch (Exception ignored){}finally {
                out.close();
            }
        } catch (Exception ignored){}finally {
            in.close();
        }
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
            return null;
        Account account = new AccountDAO(context, db).getAccountByID(userId);
        if( account != null){
            return account.getInstance().trim();
        } //User not in db
        return null;
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

        final SharedPreferences sharedpreferences = activity.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if( theme == Helper.THEME_DARK){
            changeDrawableColor(activity, R.drawable.ic_person_add,R.color.dark_text);
            changeDrawableColor(activity, R.drawable.ic_person,R.color.dark_text);
            changeDrawableColor(activity, R.drawable.ic_cancel,R.color.dark_text);
        }else {
            changeDrawableColor(activity, R.drawable.ic_person_add,R.color.black);
            changeDrawableColor(activity, R.drawable.ic_person,R.color.black);
            changeDrawableColor(activity, R.drawable.ic_cancel,R.color.black);
        }

        if( !menuAccountsOpened ){
            arrow.setImageResource(R.drawable.ic_arrow_drop_up);
            SQLiteDatabase db = Sqlite.getInstance(activity, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
            final List<Account> accounts = new AccountDAO(activity, db).getAllAccount();
            String lastInstance = "";
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.menu_accounts);
            Menu mainMenu = navigationView.getMenu();
            SubMenu currentSubmenu = null;
            for(final Account account: accounts) {
                if( !currrentUserId.equals(account.getId()) ) {
                    if( !lastInstance.trim().toUpperCase().equals(account.getInstance().trim().toUpperCase())){
                        lastInstance = account.getInstance().toUpperCase();
                        currentSubmenu = mainMenu.addSubMenu(account.getInstance().toUpperCase());
                    }
                    if( currentSubmenu  == null)
                        continue;
                    final MenuItem item = currentSubmenu.add("@" + account.getAcct());
                    //final MenuItem item = mainMenu.addSubMenu("@" + account.getAcct()).add("@" + account.getAcct());
                    ImageLoader imageLoader;
                    DisplayImageOptions options = new DisplayImageOptions.Builder().displayer(new SimpleBitmapDisplayer()).cacheInMemory(false)
                            .cacheOnDisk(true).resetViewBeforeLoading(true).build();
                    imageLoader = ImageLoader.getInstance();
                    final ImageView imageView = new ImageView(activity);
                    item.setIcon(R.drawable.ic_person);
                    String url = account.getAvatar();
                    if( url.startsWith("/") ){
                        url = "https://" + Helper.getLiveInstance(activity) + account.getAvatar();
                    }
                    imageLoader.displayImage(url, imageView, options, new ImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String s, View view) {
                        }

                        @Override
                        public void onLoadingFailed(String s, View view, FailReason failReason) {
                        }

                        @Override
                        public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                            item.setIcon(new BitmapDrawable(activity.getResources(), bitmap));
                            item.getIcon().setColorFilter(0xFFFFFFFF, PorterDuff.Mode.MULTIPLY);
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
                                changeUser(activity, userId, true);
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
            currentSubmenu = mainMenu.addSubMenu("");
            MenuItem addItem = currentSubmenu.add(R.string.add_account);
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
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.activity_main_drawer);
            arrow.setImageResource(R.drawable.ic_arrow_drop_down);
            switchLayout(activity);
        }
        menuAccountsOpened = !menuAccountsOpened;

    }

    /**
     * Changes the user in shared preferences
     * @param activity Activity
     * @param userID String - the new user id
     */
    public static void changeUser(Activity activity, String userID, boolean checkItem) {

        final NavigationView navigationView = (NavigationView) activity.findViewById(R.id.nav_view);
        navigationView.getMenu().clear();
        navigationView.inflateMenu(R.menu.activity_main_drawer);
        SQLiteDatabase db = Sqlite.getInstance(activity, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        Account account = new AccountDAO(activity,db).getAccountByID(userID);
        //Can happen when an account has been deleted and there is a click on an old notification
        if( account == null)
            return;
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
        activity.recreate();
        if( checkItem ) {
            Intent intent = new Intent(activity, MainActivity.class);
            intent.putExtra(INTENT_ACTION, CHANGE_USER_INTENT);
            activity.startActivity(intent);
        }
    }


    /**
     * Load the profile picture in the current action bar
     * @param activity Activity The current activity
     * @param url String the url of the profile picture
     */
    public static void loadPPInActionBar(final Activity activity, String url){
        ImageLoader imageLoader;
        DisplayImageOptions options = new DisplayImageOptions.Builder().displayer(new SimpleBitmapDisplayer()).cacheInMemory(false)
                .cacheOnDisk(true).resetViewBeforeLoading(true).build();
        imageLoader = ImageLoader.getInstance();
        if( url.startsWith("/") ){
            url = "https://" + Helper.getLiveInstance(activity) + url;
        }
        imageLoader.loadImage(url, options, new SimpleImageLoadingListener(){
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                super.onLoadingComplete(imageUri, view, loadedImage);

                Drawable ppDrawable;
                Toolbar toolBar = (Toolbar) activity.findViewById(R.id.toolbar);
                if( toolBar != null){
                    ppDrawable  = new BitmapDrawable(activity.getResources(), Bitmap.createScaledBitmap(loadedImage, (int) convertDpToPixel(25, activity), (int) convertDpToPixel(25, activity), true));
                    toolBar.findViewById(R.id.pp_actionBar).setBackgroundDrawable(ppDrawable);
                }else{
                    ActionBar supportActionBar = ((TootActivity) activity).getSupportActionBar();
                    if( supportActionBar != null){
                        ppDrawable = new BitmapDrawable(activity.getResources(), Bitmap.createScaledBitmap(loadedImage, (int) convertDpToPixel(20, activity), (int) convertDpToPixel(20, activity), true));
                        supportActionBar.setIcon(ppDrawable);
                    }
                }


            }
            @Override
            public void onLoadingFailed(java.lang.String imageUri, android.view.View view, FailReason failReason){

            }});
    }

    /**
     * Update the header with the new selected account
     * @param activity Activity
     * @param account Account - new account in use
     * @param headerLayout View - the menu header
     * @param imageLoader ImageLoader - instance of ImageLoader
     * @param options DisplayImageOptions - current configuration of ImageLoader
     */
    public static void updateHeaderAccountInfo(final Activity activity, final Account account, final View headerLayout, ImageLoader imageLoader, DisplayImageOptions options){
        ImageView profilePicture = (ImageView) headerLayout.findViewById(R.id.profilePicture);

        TextView username = (TextView) headerLayout.findViewById(R.id.username);
        TextView displayedName = (TextView) headerLayout.findViewById(R.id.displayedName);
        TextView ownerStatus = (TextView) headerLayout.findViewById(R.id.owner_status);
        TextView ownerFollowing = (TextView) headerLayout.findViewById(R.id.owner_following);
        TextView ownerFollowers = (TextView) headerLayout.findViewById(R.id.owner_followers);
        ImageView header_edit_profile = (ImageView) headerLayout.findViewById(R.id.header_edit_profile);
        header_edit_profile.setOnClickListener(null);
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
            String url = account.getAvatar();
            if( url.startsWith("/") ){
                url = "https://" + Helper.getLiveInstance(activity) + account.getAvatar();
            }
            imageLoader.displayImage(url, profilePicture, options);
            String urlHeader = account.getHeader();
            if( urlHeader.startsWith("/") ){
                urlHeader = "https://" + Helper.getLiveInstance(activity) + account.getHeader();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && !urlHeader.contains("missing.png")) {
                DisplayImageOptions optionNew = new DisplayImageOptions.Builder().displayer(new SimpleBitmapDisplayer()).cacheInMemory(false)
                        .cacheOnDisk(true).resetViewBeforeLoading(true).build();
                imageLoader.loadImage(urlHeader, optionNew, new SimpleImageLoadingListener() {
                    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        super.onLoadingComplete(imageUri, view, loadedImage);
                        LinearLayout main_header_container = (LinearLayout) headerLayout.findViewById(R.id.main_header_container);
                        Bitmap workingBitmap = Bitmap.createBitmap(loadedImage);
                        Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
                        Canvas canvas = new Canvas(mutableBitmap);
                        Paint p = new Paint(Color.BLACK);
                        ColorFilter filter = new LightingColorFilter(0xFF7F7F7F, 0x00000000);
                        p.setColorFilter(filter);
                        canvas.drawBitmap(mutableBitmap, new Matrix(), p);
                        BitmapDrawable background = new BitmapDrawable(activity.getResources(), mutableBitmap);
                        main_header_container.setBackground(background);

                    }

                    @Override
                    public void onLoadingFailed(java.lang.String imageUri, android.view.View view, FailReason failReason) {
                        LinearLayout main_header_container = (LinearLayout) headerLayout.findViewById(R.id.main_header_container);
                        main_header_container.setBackgroundResource(R.drawable.side_nav_bar);
                    }
                });
            }
            header_edit_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, EditProfileActivity.class);
                    activity.startActivity(intent);
                }
            });
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


    /**
     * Retrieves the cache size
     * @param directory File
     * @return long value in Mo
     */
    public static long cacheSize(File directory) {
        long length = 0;
        if( directory == null || directory.length() == 0 )
            return -1;
        for (File file : directory.listFiles()) {
            if (file.isFile())
                try {
                    length += file.length();
                }catch (NullPointerException e){
                    return -1;
                }
            else
                length += cacheSize(file);
        }
        return length;
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                boolean success = deleteDir(new File(dir, aChildren));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else{
            return dir != null && dir.isFile() && dir.delete();
        }
    }


    /**
     * Check if the status contents mentions & tags and fills the content with ClickableSpan
     * Click on account => ShowAccountActivity
     * Click on tag => HashTagActivity
     * @param context Context
     * @param fullContent String, should be the st
     * @param mentions List<Mention>
     * @return TextView
     */
    public static SpannableString clickableElements(final Context context, String fullContent, List<Mention> mentions, boolean useHTML) {

        SpannableString spannableString;
        if( useHTML) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                spannableString = new SpannableString(Html.fromHtml(fullContent, Html.FROM_HTML_MODE_LEGACY));
            else
                //noinspection deprecation
                spannableString = new SpannableString(Html.fromHtml(fullContent));
        }else{
            spannableString = new SpannableString(fullContent);
        }
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        boolean embedded_browser = sharedpreferences.getBoolean(Helper.SET_EMBEDDED_BROWSER, true);
        if( embedded_browser){
            Matcher matcher;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT)
                matcher = Patterns.WEB_URL.matcher(spannableString);
            else
                matcher = urlPattern.matcher(spannableString);

            while (matcher.find()){
                int matchStart = matcher.start(1);
                int matchEnd = matcher.end();
                final String url = spannableString.toString().substring(matchStart, matchEnd);
                spannableString.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(View textView) {
                        Intent intent = new Intent(context, WebviewActivity.class);
                        Bundle b = new Bundle();
                        String finalUrl = url;
                        if( !url.startsWith("http://") && ! url.startsWith("https://"))
                            finalUrl = "http://" + url;
                        b.putString("url", finalUrl);
                        intent.putExtras(b);
                        context.startActivity(intent);
                    }
                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                    }
                },
                matchStart, matchEnd,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

            }
        }
        //Deals with mention to make them clickable
        if( mentions != null && mentions.size() > 0 ) {
            //Looping through accounts which are mentioned
            for (final Mention mention : mentions) {
                String targetedAccount = "@" + mention.getUsername();
                if (spannableString.toString().contains(targetedAccount)) {

                    //Accounts can be mentioned several times so we have to loop
                    for(int startPosition = -1 ; (startPosition = spannableString.toString().indexOf(targetedAccount, startPosition + 1)) != -1 ; startPosition++){
                        int endPosition = startPosition + targetedAccount.length();
                        spannableString.setSpan(new ClickableSpan() {
                                @Override
                                public void onClick(View textView) {
                                    Intent intent = new Intent(context, ShowAccountActivity.class);
                                    Bundle b = new Bundle();
                                    b.putString("accountId", mention.getId());
                                    intent.putExtras(b);
                                    context.startActivity(intent);
                                }
                                @Override
                                public void updateDrawState(TextPaint ds) {
                                    super.updateDrawState(ds);
                                }
                            },
                                startPosition, endPosition,
                                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    }
                }

            }
        }
        Matcher matcher = hashtagPattern.matcher(spannableString);
        while (matcher.find()){
            int matchStart = matcher.start(1);
            int matchEnd = matcher.end();
            final String tag = spannableString.toString().substring(matchStart, matchEnd);
            spannableString.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    Intent intent = new Intent(context, HashTagActivity.class);
                    Bundle b = new Bundle();
                    b.putString("tag", tag.substring(1));
                    intent.putExtras(b);
                    context.startActivity(intent);
                }
                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                }
            }, matchStart, matchEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return spannableString;
    }



    /**
     * Check if the account bio contents urls & tags and fills the content with ClickableSpan
     * Click on url => webview or external app
     * Click on tag => HashTagActivity
     * @param context Context
     * @param fullContent String, should be the st
     * @return TextView
     */
    public static SpannableString clickableElementsDescription(final Context context, String fullContent) {

        SpannableString spannableString;
        fullContent = Helper.shortnameToUnicode(fullContent, true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            spannableString = new SpannableString(Html.fromHtml(fullContent, Html.FROM_HTML_MODE_LEGACY));
        else
            //noinspection deprecation
            spannableString = new SpannableString(Html.fromHtml(fullContent));
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        boolean embedded_browser = sharedpreferences.getBoolean(Helper.SET_EMBEDDED_BROWSER, true);
        if( embedded_browser){
            Matcher matcher;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT)
                matcher = Patterns.WEB_URL.matcher(spannableString);
            else
                matcher = urlPattern.matcher(spannableString);
            while (matcher.find()){
                int matchStart = matcher.start(1);
                int matchEnd = matcher.end();
                final String url = spannableString.toString().substring(matchStart, matchEnd);
                spannableString.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(View textView) {
                        Intent intent = new Intent(context, WebviewActivity.class);
                        Bundle b = new Bundle();
                        b.putString("url", url);
                        intent.putExtras(b);
                        context.startActivity(intent);
                    }
                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                    }
                }, matchStart, matchEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }
        }
        Matcher matcher = hashtagPattern.matcher(spannableString);
        while (matcher.find()){
            int matchStart = matcher.start(1);
            int matchEnd = matcher.end();
            final String tag = spannableString.toString().substring(matchStart, matchEnd);
            spannableString.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    Intent intent = new Intent(context, HashTagActivity.class);
                    Bundle b = new Bundle();
                    b.putString("tag", tag.substring(1));
                    intent.putExtras(b);
                    context.startActivity(intent);
                }
                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                }
            }, matchStart, matchEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return spannableString;
    }


    public static WebView initializeWebview(Activity activity, int webviewId){

        WebView webView = (WebView) activity.findViewById(webviewId);
        final SharedPreferences sharedpreferences = activity.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        boolean javascript = sharedpreferences.getBoolean(Helper.SET_JAVASCRIPT, true);

        webView.getSettings().setJavaScriptEnabled(javascript);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setSupportMultipleWindows(false);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            //noinspection deprecation
            webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webView.getSettings().setMediaPlaybackRequiresUserGesture(true);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            boolean cookies = sharedpreferences.getBoolean(Helper.SET_COOKIES, false);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptThirdPartyCookies(webView, cookies);
        }
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

        return webView;
    }


    public static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * change color of a drawable
     * @param drawable int the drawable
     * @param hexaColor example 0xffff00
     */
    public static Drawable changeDrawableColor(Context context, int drawable, int hexaColor){
        int color = Color.parseColor(context.getString(hexaColor));
        Drawable mDrawable = ContextCompat.getDrawable(context, drawable);
        mDrawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        return mDrawable;
    }


    /**
     * Returns the current locale of the device
     * @param context Context
     * @return String locale
     */
    public static String currentLocale(Context context) {
        String locale;
        Locale current;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            current = context.getResources().getConfiguration().getLocales().get(0);
        } else {
            //noinspection deprecation
            current = context.getResources().getConfiguration().locale;
        }
        locale = current.toString();
        locale = locale.split("_")[0];
        return locale;
    }


    /**
     * Compare date with these in shared pref.
     * @param context Context
     * @param newDate String
     * @param shouldBeGreater boolean if date passed as a parameter should be greater
     * @return boolean
     */
    public static boolean compareDate(Context context, String newDate, boolean shouldBeGreater){
        String dateRef;
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        if (shouldBeGreater) {
            dateRef = sharedpreferences.getString(Helper.SET_TIME_FROM, "07:00");
        }else {
            dateRef = sharedpreferences.getString(Helper.SET_TIME_TO, "22:00");
        }
        try{
            Locale userLocale;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                userLocale = context.getResources().getConfiguration().getLocales().get(0);
            } else {
                //noinspection deprecation
                userLocale = context.getResources().getConfiguration().locale;
            }
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", userLocale);
            Date newDateD = formatter.parse(newDate);
            Date dateRefD = formatter.parse(dateRef);
            if (shouldBeGreater) {
                return (newDateD.after(dateRefD));
            }else {
                return (newDateD.before(dateRefD));
            }
        } catch (java.text.ParseException e) {
            return false;
        }
    }


    /**
     * Tells if the the service can notify depending of the current hour and minutes
     * @param context Context
     * @return boolean
     */
    public static boolean canNotify(Context context){
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String dateIni = sharedpreferences.getString(Helper.SET_TIME_FROM, "07:00");
        String dateEnd = sharedpreferences.getString(Helper.SET_TIME_TO, "22:00");
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        String hourS = String.valueOf(hour).length() == 1?"0"+String.valueOf(hour):String.valueOf(hour);
        String minuteS = String.valueOf(minute).length() == 1?"0"+String.valueOf(minute):String.valueOf(minute);
        String currentDate = hourS + ":" + minuteS;
        try{
            Locale userLocale;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                userLocale = context.getResources().getConfiguration().getLocales().get(0);
            } else {
                //noinspection deprecation
                userLocale = context.getResources().getConfiguration().locale;
            }
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", userLocale);
            Date dateIniD = formatter.parse(dateIni);
            Date dateEndD = formatter.parse(dateEnd);
            Date currentDateD = formatter.parse(currentDate);
            return currentDateD.before(dateEndD)&&currentDateD.after(dateIniD);
        } catch (java.text.ParseException e) {
            return true;
        }
    }

    /**
     * Serialized a Status class
     * @param status Status to serialize
     * @return String serialized Status
     */
    public static String statusToStringStorage(Status status){
        Gson gson = new Gson();
        return gson.toJson(status);
    }

    /**
     * Unserialized a Status
     * @param serializedStatus String serialized status
     * @return Status
     */
    public static Status restoreStatusFromString(String serializedStatus){
        Gson gson = new Gson();
        return gson.fromJson(serializedStatus, Status.class);
    }

    /**
     * Check if a job id is in array of ids
     * @param jobIds int[]
     * @param id int id to check
     * @return boolean
     */
    public static boolean isJobPresent(int[] jobIds, int id){
        for(int x:jobIds) {
            if (x == id) {return true;}
        }
        return false;
    }



    public static void unCheckAllMenuItems(NavigationView navigationView){
        navigationView.setCheckedItem(R.id.menu_none);
        unCheckAllMenuItemsRec(navigationView.getMenu());
    }

    private static void unCheckAllMenuItemsRec(@NonNull final Menu menu) {
        int size = menu.size();
        for (int i = 0; i < size; i++) {
            final MenuItem item = menu.getItem(i);
            if(item.hasSubMenu()) {
                unCheckAllMenuItemsRec(item.getSubMenu());
            } else {
                item.setChecked(false);
            }
        }
    }

    /**
     * Returns true if a ListView is at its top position
     * @param listView ListView
     * @return boolean
     */
    public static boolean listIsAtTop(ListView listView) {
        return listView.getChildCount() == 0 || listView.getChildAt(0).getTop() == 0;
    }


    /**
     * Changes the menu layout
     * @param activity Activity must be an instance of MainActivity
     */
    public static void switchLayout(Activity activity){
        //Check if the class calling the method is an instance of MainActivity
        boolean isTablet = activity.getResources().getBoolean(R.bool.isTablet);
        final SharedPreferences sharedpreferences = activity.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int timelineLayout = sharedpreferences.getInt(Helper.SET_TABS, Helper.THEME_TABS);
        final NavigationView navigationView = (NavigationView) activity.findViewById(R.id.nav_view);

        android.support.design.widget.TabLayout tableLayout = (android.support.design.widget.TabLayout) activity.findViewById(R.id.tabLayout);
        LinearLayout toolbar_search_container = (LinearLayout) activity.findViewById(R.id.toolbar_search_container);
        ViewGroup.LayoutParams params = toolbar_search_container.getLayoutParams();
        int heightSearchdp, heightSearchdpAlone;
        if( !isTablet){
            heightSearchdp = 40;
            heightSearchdpAlone = 60;
        }else {
            heightSearchdp = 40;
            heightSearchdpAlone = 60;
        }
        String userID = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        SQLiteDatabase db = Sqlite.getInstance(activity, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        Account account = new AccountDAO(activity,db).getAccountByID(userID);
        if( account != null) {
            if (account.isLocked()) {
                if( navigationView.getMenu().findItem(R.id.nav_follow_request) != null)
                    navigationView.getMenu().findItem(R.id.nav_follow_request).setVisible(true);
            } else {
                if( navigationView.getMenu().findItem(R.id.nav_follow_request) != null)
                    navigationView.getMenu().findItem(R.id.nav_follow_request).setVisible(false);
            }
        }
        if( navigationView.getMenu().findItem(R.id.nav_home) != null){
            switch (timelineLayout){
                case Helper.THEME_TABS:
                    navigationView.getMenu().findItem(R.id.nav_home).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_local).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_global).setVisible(false);
                    navigationView.getMenu().findItem(R.id.nav_notification).setVisible(false);
                    params.height = (int) Helper.convertDpToPixel(heightSearchdp, activity);
                    toolbar_search_container.setLayoutParams(params);
                    tableLayout.setVisibility(View.VISIBLE);
                    break;
                case Helper.THEME_MENU:
                    navigationView.getMenu().findItem(R.id.nav_home).setVisible(true);
                    navigationView.getMenu().findItem(R.id.nav_local).setVisible(true);
                    navigationView.getMenu().findItem(R.id.nav_global).setVisible(true);
                    navigationView.getMenu().findItem(R.id.nav_notification).setVisible(true);
                    params.height = (int) Helper.convertDpToPixel(heightSearchdpAlone, activity);;
                    toolbar_search_container.setLayoutParams(params);
                    tableLayout.setVisibility(View.GONE);
                    break;
                case Helper.THEME_MENU_TABS:
                    navigationView.getMenu().findItem(R.id.nav_home).setVisible(true);
                    navigationView.getMenu().findItem(R.id.nav_local).setVisible(true);
                    navigationView.getMenu().findItem(R.id.nav_global).setVisible(true);
                    navigationView.getMenu().findItem(R.id.nav_notification).setVisible(true);
                    params.height = (int) Helper.convertDpToPixel(heightSearchdp, activity);
                    toolbar_search_container.setLayoutParams(params);
                    tableLayout.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

}
