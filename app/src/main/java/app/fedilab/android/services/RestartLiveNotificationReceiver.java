package app.fedilab.android.services;
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

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import app.fedilab.android.helper.Helper;
import app.fedilab.android.jobs.NotificationsSyncJob;

/**
 * Created by Thomas on 22/09/2017.
 * BroadcastReceiver for restarting the service
 */

public class RestartLiveNotificationReceiver extends BroadcastReceiver {


    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        int type = Helper.liveNotifType(context);
        if (type == Helper.NOTIF_DELAYED) {
            Intent streamingServiceIntent = new Intent(context.getApplicationContext(), LiveNotificationDelayedService.class);
            try {
                context.startService(streamingServiceIntent);
            } catch (Exception ignored) {
            }
        } else if (type == Helper.NOTIF_LIVE) {
            Intent streamingServiceIntent = new Intent(context.getApplicationContext(), LiveNotificationService.class);
            try {
                context.startService(streamingServiceIntent);
            } catch (Exception ignored) {
            }
        }else{
            NotificationsSyncJob.schedule(false);
        }
    }

}