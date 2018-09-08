package fr.gouv.etalab.mastodon.activities;
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
import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.StrictMode;
import com.evernote.android.job.JobManager;

import org.acra.ACRA;
import org.acra.BuildConfig;
import org.acra.annotation.AcraDialog;
import org.acra.annotation.AcraMailSender;
import org.acra.config.CoreConfigurationBuilder;

import org.acra.config.LimiterConfigurationBuilder;
import org.acra.config.MailSenderConfigurationBuilder;
import org.acra.data.StringFormat;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.jobs.ApplicationJob;
import fr.gouv.etalab.mastodon.jobs.HomeTimelineSyncJob;
import fr.gouv.etalab.mastodon.jobs.NotificationsSyncJob;

/**
 * Created by Thomas on 29/04/2017.
 * Main application, jobs are launched here.
 */

@AcraDialog(resText = R.string.crash_title,
        resCommentPrompt = R.string.crash_message, resTheme = R.style.AlertDialogDark, resIcon = R.mipmap.ic_launcher)
public class MainApplication extends Application{


    @Override
    public void onCreate() {
        super.onCreate();
        JobManager.create(this).addJobCreator(new ApplicationJob());
        NotificationsSyncJob.schedule(false);
        HomeTimelineSyncJob.schedule(false);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        CoreConfigurationBuilder ACRABuilder = new CoreConfigurationBuilder(this);
        ACRABuilder.setBuildConfigClass(BuildConfig.class).setReportFormat(StringFormat.KEY_VALUE_LIST);
        String version = "";
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException ignored) { }
        ACRABuilder.getPluginConfigurationBuilder(MailSenderConfigurationBuilder.class).setReportAsFile(false).setMailTo("support@mastalab.app").setSubject(" Crash Report for Mastalab " + version).setEnabled(true);
        ACRABuilder.getPluginConfigurationBuilder(LimiterConfigurationBuilder.class).setEnabled(true);
        ACRA.init(this, ACRABuilder);
    }
}
