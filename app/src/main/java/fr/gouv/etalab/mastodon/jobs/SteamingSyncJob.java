package fr.gouv.etalab.mastodon.jobs;
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
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.View;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import fr.gouv.etalab.mastodon.activities.MainActivity;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveNotificationsAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.StreamingUserAsyncTask;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Notification;
import fr.gouv.etalab.mastodon.client.PatchBaseImageDownloader;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveNotificationsInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveStreamingInterface;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import mastodon.etalab.gouv.fr.mastodon.R;

import static fr.gouv.etalab.mastodon.helper.Helper.INTENT_ACTION;
import static fr.gouv.etalab.mastodon.helper.Helper.NOTIFICATION_INTENT;
import static fr.gouv.etalab.mastodon.helper.Helper.PREF_KEY_ID;
import static fr.gouv.etalab.mastodon.helper.Helper.canNotify;
import static fr.gouv.etalab.mastodon.helper.Helper.notify_user;


/**
 * Created by Thomas on 29/04/2017.
 * Notifications refresh job
 */

public class SteamingSyncJob extends Job implements OnRetrieveStreamingInterface {

    static final String STREAMING = "job_streaming";
    private String message;

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        //Code refresh here
        callAsynchronousTask();
        return Result.SUCCESS;
    }


    public static int schedule(boolean updateCurrent){

        Set<JobRequest> jobRequests = JobManager.instance().getAllJobRequestsForTag(STREAMING);
        if (!jobRequests.isEmpty() && !updateCurrent) {
            return jobRequests.iterator().next().getJobId();
        }

        return new JobRequest.Builder(SteamingSyncJob.STREAMING)
                .setPeriodic(TimeUnit.MINUTES.toMillis(Helper.MINUTES_BETWEEN_NOTIFICATIONS_REFRESH), TimeUnit.MINUTES.toMillis(5))
                .setPersisted(true)
                .setUpdateCurrent(updateCurrent)
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .setRequirementsEnforced(false)
                .build()
                .schedule();
    }



    /**
     * Task in background starts here.
     */
    private void callAsynchronousTask() {

        SQLiteDatabase db = Sqlite.getInstance(getContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        //If an Internet connection and user agrees with notification refresh
        final SharedPreferences sharedpreferences = getContext().getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        //Check which notifications the user wants to see
        boolean notif_follow = sharedpreferences.getBoolean(Helper.SET_NOTIF_FOLLOW, true);
        boolean notif_add = sharedpreferences.getBoolean(Helper.SET_NOTIF_ADD, true);
        boolean notif_ask = sharedpreferences.getBoolean(Helper.SET_NOTIF_ASK, true);
        boolean notif_mention = sharedpreferences.getBoolean(Helper.SET_NOTIF_MENTION, true);
        boolean notif_share = sharedpreferences.getBoolean(Helper.SET_NOTIF_SHARE, true);
        //User disagree with all notifications
        if( !notif_follow && !notif_add && !notif_ask && !notif_mention && !notif_share)
            return; //Nothing is done
        //No account connected, the service is stopped
        if(!Helper.isLoggedIn(getContext()))
            return;
        //If WIFI only and on WIFI OR user defined any connections to use the service.
        if(!sharedpreferences.getBoolean(Helper.SET_WIFI_ONLY, false) || Helper.isOnWIFI(getContext())) {
            List<Account> accounts = new AccountDAO(getContext(),db).getAllAccount();
            //It means there is no user in DB.
            if( accounts == null )
                return;
            //Retrieve users in db that owner has.
            for (Account account: accounts) {
                String max_id = sharedpreferences.getString(Helper.LAST_NOTIFICATION_MAX_ID + account.getId(), null);
                new StreamingUserAsyncTask(account.getInstance(), account.getToken(), account.getAcct(), account.getId(), SteamingSyncJob.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }


    @Override
    public void onRetrieveStreaming(StreamingUserAsyncTask.EventStreaming event, JSONObject response, String acct, String userId) {
        if(  response == null )
            return;
        String new_max_id = null;
        final SharedPreferences sharedpreferences = getContext().getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        boolean notif_follow = sharedpreferences.getBoolean(Helper.SET_NOTIF_FOLLOW, true);
        boolean notif_add = sharedpreferences.getBoolean(Helper.SET_NOTIF_ADD, true);
        boolean notif_mention = sharedpreferences.getBoolean(Helper.SET_NOTIF_MENTION, true);
        boolean notif_share = sharedpreferences.getBoolean(Helper.SET_NOTIF_SHARE, true);
        final String max_id = sharedpreferences.getString(Helper.LAST_NOTIFICATION_MAX_ID + userId, null);


        //No previous notifications in cache, so no notification will be sent
        boolean notify = false;

        String notificationUrl = null;
        String title = null;
        if( event == StreamingUserAsyncTask.EventStreaming.NOTIFICATION){
            Notification notification = API.parseNotificationResponse(getContext(), response);
            new_max_id = notification.getId();
            switch (notification.getType()){
                case "mention":
                    if(notif_mention){
                        notify = true;
                        notificationUrl = notification.getAccount().getAvatar();
                        if( notification.getAccount().getDisplay_name() != null && notification.getAccount().getDisplay_name().length() > 0 )
                            title = String.format("@%s %s", Helper.shortnameToUnicode(notification.getAccount().getDisplay_name(), true),getContext().getString(R.string.notif_mention));
                        else
                            title = String.format("@%s %s", notification.getAccount().getUsername(),getContext().getString(R.string.notif_mention));
                    }
                    break;
                case "reblog":
                    if(notif_share){
                        notify = true;
                        notificationUrl = notification.getAccount().getAvatar();
                        if( notification.getAccount().getDisplay_name() != null && notification.getAccount().getDisplay_name().length() > 0 )
                            title = String.format("@%s %s", Helper.shortnameToUnicode(notification.getAccount().getDisplay_name(), true),getContext().getString(R.string.notif_reblog));
                        else
                            title = String.format("@%s %s", notification.getAccount().getUsername(),getContext().getString(R.string.notif_reblog));
                    }
                    break;
                case "favourite":
                    if(notif_add){
                        notify = true;
                        notificationUrl = notification.getAccount().getAvatar();
                        if( notification.getAccount().getDisplay_name() != null && notification.getAccount().getDisplay_name().length() > 0 )
                            title = String.format("@%s %s", Helper.shortnameToUnicode(notification.getAccount().getDisplay_name(), true),getContext().getString(R.string.notif_favourite));
                        else
                            title = String.format("@%s %s", notification.getAccount().getUsername(),getContext().getString(R.string.notif_favourite));
                    }
                    break;
                case "follow":
                    if(notif_follow){
                        notify = true;
                        notificationUrl = notification.getAccount().getAvatar();
                        if( notification.getAccount().getDisplay_name() != null && notification.getAccount().getDisplay_name().length() > 0 )
                            title = String.format("@%s %s", Helper.shortnameToUnicode(notification.getAccount().getDisplay_name(), true),getContext().getString(R.string.notif_follow));
                        else
                            title = String.format("@%s %s", notification.getAccount().getUsername(),getContext().getString(R.string.notif_follow));
                    }
                    break;
                default:
                    break;
            }
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(Helper.LAST_NOTIFICATION_MAX_ID + userId, notification.getId());
            editor.apply();
            message = notification.getStatus().getContent();
            if( message!= null) {
                message = message.substring(0, 17);
                message = message + "...";
            }
        }else if ( event ==  StreamingUserAsyncTask.EventStreaming.UPDATE){

        }else if( event == StreamingUserAsyncTask.EventStreaming.DELETE){

        }
        if( new_max_id != null){
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(Helper.LAST_NOTIFICATION_MAX_ID + userId, new_max_id);
            editor.apply();
            return;
        }

        //Check which user is connected and if activity is to front



        if( notify){
            final Intent intent = new Intent(getContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK );
            intent.putExtra(INTENT_ACTION, NOTIFICATION_INTENT);
            intent.putExtra(PREF_KEY_ID, userId);
            long notif_id = Long.parseLong(userId);
            final int notificationId = ((notif_id + 1) > 2147483647) ? (int) (2147483647 - notif_id - 1) : (int) (notif_id + 1);

            if( notificationUrl != null){
                ImageLoader imageLoaderNoty = ImageLoader.getInstance();
                File cacheDir = new File(getContext().getCacheDir(), getContext().getString(R.string.app_name));
                ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getContext())
                        .imageDownloader(new PatchBaseImageDownloader(getContext()))
                        .threadPoolSize(5)
                        .threadPriority(Thread.MIN_PRIORITY + 3)
                        .denyCacheImageMultipleSizesInMemory()
                        .diskCache(new UnlimitedDiskCache(cacheDir))
                        .build();
                imageLoaderNoty.init(config);
                DisplayImageOptions options = new DisplayImageOptions.Builder().displayer(new SimpleBitmapDisplayer()).cacheInMemory(false)
                        .cacheOnDisk(true).resetViewBeforeLoading(true).build();

                final String finalTitle = title;
                imageLoaderNoty.loadImage(notificationUrl, options, new SimpleImageLoadingListener(){
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        super.onLoadingComplete(imageUri, view, loadedImage);
                        notify_user(getContext(), intent, notificationId, loadedImage, finalTitle, message);

                    }
                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason){
                        notify_user(getContext(), intent, notificationId, BitmapFactory.decodeResource(getContext().getResources(),
                                R.drawable.mastodonlogo), finalTitle, message);
                    }});
            }
        }
    }
}
