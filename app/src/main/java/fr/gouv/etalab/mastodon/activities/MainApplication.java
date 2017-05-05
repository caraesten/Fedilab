package fr.gouv.etalab.mastodon.activities;
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
import android.app.Application;

import com.evernote.android.job.JobManager;

import fr.gouv.etalab.mastodon.jobs.ApplicationJob;
import fr.gouv.etalab.mastodon.jobs.NotificationsSyncJob;

/**
 * Created by Thomas on 29/04/2017.
 */

public class MainApplication extends Application{


    @Override
    public void onCreate() {
        super.onCreate();
        JobManager.create(this).addJobCreator(new ApplicationJob());
        JobManager.instance().getConfig().setVerbose(false);
        NotificationsSyncJob.schedule(getApplicationContext(), false);
    }
}
