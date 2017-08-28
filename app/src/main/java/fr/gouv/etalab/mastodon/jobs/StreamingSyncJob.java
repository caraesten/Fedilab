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

import android.content.Intent;
import android.support.annotation.NonNull;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.services.StreamingService;


/**
 * Created by Thomas on 29/04/2017.
 * Notifications refresh job
 */

public class StreamingSyncJob extends Job  {

    static final String STREAMING = "job_streaming";


    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        //Code refresh here
        getContext().startService(new Intent(getContext(), StreamingService.class));
        return Result.SUCCESS;
    }


    public static int schedule(boolean updateCurrent){

        Set<JobRequest> jobRequests = JobManager.instance().getAllJobRequestsForTag(STREAMING);
        if (!jobRequests.isEmpty() && !updateCurrent) {
            return jobRequests.iterator().next().getJobId();
        }
        return new JobRequest.Builder(StreamingSyncJob.STREAMING)
                .setPeriodic(TimeUnit.MINUTES.toMillis(Helper.MINUTES_BETWEEN_STREAMING_CHECK_ALIVE), TimeUnit.MINUTES.toMillis(5))
                .setPersisted(true)
                .setUpdateCurrent(updateCurrent)
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .setRequirementsEnforced(false)
                .build()
                .schedule();
    }

}
