package fr.gouv.etalab.mastodon.jobs;
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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;


import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;


import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import fr.gouv.etalab.mastodon.activities.MainActivity;
import fr.gouv.etalab.mastodon.client.PatchBaseImageDownloader;
import fr.gouv.etalab.mastodon.helper.Helper;
import mastodon.etalab.gouv.fr.mastodon.R;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveNotificationsAsyncTask;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Notification;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveNotificationsInterface;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;

import static fr.gouv.etalab.mastodon.helper.Helper.INTENT_ACTION;
import static fr.gouv.etalab.mastodon.helper.Helper.NOTIFICATION_INTENT;
import static fr.gouv.etalab.mastodon.helper.Helper.PREF_KEY_ID;
import static fr.gouv.etalab.mastodon.helper.Helper.notify_user;


/**
 * Created by Thomas on 29/04/2017.
 * Notifications refresh job
 */

public class NotificationsSyncJob extends Job implements OnRetrieveNotificationsInterface{

    static final String NOTIFICATION_REFRESH = "job_notification";
    private int notificationId;

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        //Code refresh here
        callAsynchronousTask();
        return Result.SUCCESS;
    }


    public static int schedule(boolean updateCurrent){

        Set<JobRequest> jobRequests = JobManager.instance().getAllJobRequestsForTag(NOTIFICATION_REFRESH);
        if (!jobRequests.isEmpty() && !updateCurrent) {
            return jobRequests.iterator().next().getJobId();
        }

        return new JobRequest.Builder(NotificationsSyncJob.NOTIFICATION_REFRESH)
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
        //If WIFI only and on WIFI OR user defined any connections to use the service.
        if(!sharedpreferences.getBoolean(Helper.SET_WIFI_ONLY, false) || Helper.isOnWIFI(getContext())) {
            List<Account> accounts = new AccountDAO(getContext(),db).getAllAccount();
            //It means there is no user in DB.
            if( accounts == null )
                return;
            //Retrieve users in db that owner has.
            for (Account account: accounts) {
                String max_id = sharedpreferences.getString(Helper.LAST_NOTIFICATION_MAX_ID + account.getId(), null);
                notificationId = (int) Math.round(Double.parseDouble(account.getId())/1000);
                new RetrieveNotificationsAsyncTask(getContext(), account.getInstance(), max_id, account.getAcct(), account.getId(), NotificationsSyncJob.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }



    @Override
    public void onRetrieveNotifications(List<Notification> notifications, String acct, String userId) {
        if( notifications == null || notifications.size() == 0)
            return;
        Bitmap icon_notification = null;
        final SharedPreferences sharedpreferences = getContext().getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        boolean notif_follow = sharedpreferences.getBoolean(Helper.SET_NOTIF_FOLLOW, true);
        boolean notif_add = sharedpreferences.getBoolean(Helper.SET_NOTIF_ADD, true);
        //boolean notif_ask = sharedpreferences.getBoolean(Helper.SET_NOTIF_ASK, true);
        boolean notif_mention = sharedpreferences.getBoolean(Helper.SET_NOTIF_MENTION, true);
        boolean notif_share = sharedpreferences.getBoolean(Helper.SET_NOTIF_SHARE, true);
        String max_id = sharedpreferences.getString(Helper.LAST_NOTIFICATION_MAX_ID + userId, null);
        //No previous notifications in cache, so no notification will be sent
        if( max_id != null ){
            int newFollows = 0;
            int newAdds = 0;
            int newAsks = 0;
            int newMentions = 0;
            int newShare = 0;
            String notificationUrl = null;
            String title = null;
            String message;
            for(Notification notification: notifications){
                //The notification associated to max_id is discarded as it is supposed to have already been sent
                if( notification.getId().equals(max_id))
                    continue;
                switch (notification.getType()){
                    case "mention":
                        if(notif_mention){
                            newMentions++;
                            if( notificationUrl == null){
                                notificationUrl = notification.getAccount().getAvatar();
                                title = String.format("@%s %s", notification.getAccount().getAcct(),getContext().getString(R.string.notif_mention));
                            }
                        }
                    break;
                    case "reblog":
                        if(notif_share){
                            newShare++;
                            if( notificationUrl == null){
                                notificationUrl = notification.getAccount().getAvatar();
                                title = String.format("@%s %s", notification.getAccount().getAcct(),getContext().getString(R.string.notif_reblog));
                            }
                        }
                        break;
                    case "favourite":
                        if(notif_add){
                            newAdds++;
                            if( notificationUrl == null){
                                notificationUrl = notification.getAccount().getAvatar();
                                title = String.format("@%s %s", notification.getAccount().getAcct(),getContext().getString(R.string.notif_favourite));
                            }
                        }
                        break;
                    case "follow":
                        if(notif_follow){
                            newFollows++;
                            if( notificationUrl == null){
                                notificationUrl = notification.getAccount().getAvatar();
                                title = String.format("@%s %s", notification.getAccount().getAcct(),getContext().getString(R.string.notif_follow));
                            }
                        }
                        break;
                    default:
                }
                if( notificationUrl != null && icon_notification == null){
                    try {
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
                        icon_notification = imageLoaderNoty.loadImageSync(notificationUrl);
                    }catch (Exception e){
                        icon_notification = BitmapFactory.decodeResource(getContext().getResources(),
                                R.drawable.mastodonlogo);
                    }
                }
            }
            int allNotifCount = newFollows + newAdds + newAsks + newMentions + newShare;
            if( allNotifCount > 0){
                //Some others notification
                int other = allNotifCount -1;
                if(other > 0 )
                    message = getContext().getResources().getQuantityString(R.plurals.other_notifications, other, other);
                else
                    message = "";
                final Intent intent = new Intent(getContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK );
                intent.putExtra(INTENT_ACTION, NOTIFICATION_INTENT);
                intent.putExtra(PREF_KEY_ID, userId);
                notify_user(getContext(), intent, notificationId, icon_notification,title,message);
            }
        }
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(Helper.LAST_NOTIFICATION_MAX_ID + userId, notifications.get(0).getId());
        editor.apply();

    }

}
