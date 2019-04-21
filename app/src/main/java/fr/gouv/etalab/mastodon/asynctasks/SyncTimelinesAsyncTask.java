/* Copyright 2019 Thomas Schneider
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
package fr.gouv.etalab.mastodon.asynctasks;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import fr.gouv.etalab.mastodon.activities.MainActivity;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;

import fr.gouv.etalab.mastodon.client.Entities.ManageTimelines;
import fr.gouv.etalab.mastodon.client.Entities.RemoteInstance;
import fr.gouv.etalab.mastodon.client.Entities.TagTimeline;
import fr.gouv.etalab.mastodon.interfaces.OnSyncTimelineInterface;
import fr.gouv.etalab.mastodon.sqlite.InstancesDAO;
import fr.gouv.etalab.mastodon.sqlite.SearchDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import fr.gouv.etalab.mastodon.sqlite.TimelinesDAO;

import static fr.gouv.etalab.mastodon.sqlite.Sqlite.DB_NAME;


/**
 * Created by Thomas on 21/04/2019.
 * Retrieves timelines
 */

public class SyncTimelinesAsyncTask extends AsyncTask<Void, Void, Void> {


    private OnSyncTimelineInterface listener;
    private WeakReference<Context> contextReference;
    private List<ManageTimelines> manageTimelines;


    public SyncTimelinesAsyncTask(Context context, OnSyncTimelineInterface onSyncTimelineInterface){
        this.contextReference = new WeakReference<>(context);
        this.listener = onSyncTimelineInterface;
    }

    @Override
    protected Void doInBackground(Void... params) {

        SQLiteDatabase db = Sqlite.getInstance(contextReference.get(), DB_NAME, null, Sqlite.DB_VERSION).open();
        manageTimelines = new TimelinesDAO(contextReference.get(), db).getAllTimelines();

        //First time that the timeline is created
        if( manageTimelines == null || manageTimelines.size() == 0){
            //Add home TL
            ManageTimelines manageHome = new ManageTimelines();
            manageHome.setDisplayed(true);
            manageHome.setType(ManageTimelines.Type.HOME);
            manageHome.setPosition(0);
            manageTimelines.add(manageHome);
            //Add Direct notification TL
            ManageTimelines manageNotif = new ManageTimelines();
            manageNotif.setDisplayed(true);
            manageNotif.setType(ManageTimelines.Type.NOTIFICATION);
            manageNotif.setPosition(1);
            manageTimelines.add(manageNotif);
            //Add Direct message TL
            ManageTimelines manageDirect = new ManageTimelines();
            manageDirect.setDisplayed(true);
            manageDirect.setType(ManageTimelines.Type.DIRECT);
            manageDirect.setPosition(2);
            manageTimelines.add(manageDirect);
            //Add Local TL
            ManageTimelines manageLocal = new ManageTimelines();
            manageLocal.setDisplayed(true);
            manageLocal.setType(ManageTimelines.Type.LOCAL);
            manageLocal.setPosition(3);
            manageTimelines.add(manageLocal);
            //Add Public TL
            ManageTimelines managePublic = new ManageTimelines();
            managePublic.setDisplayed(true);
            managePublic.setType(ManageTimelines.Type.PUBLIC);
            managePublic.setPosition(4);
            manageTimelines.add(managePublic);
            //Add Public ART
            ManageTimelines manageArt = new ManageTimelines();
            manageArt.setDisplayed(true);
            manageArt.setType(ManageTimelines.Type.ART);
            manageArt.setPosition(5);
            manageTimelines.add(manageArt);
            //Add Public PEERTUBE
            ManageTimelines managePeertube = new ManageTimelines();
            managePeertube.setDisplayed(true);
            managePeertube.setType(ManageTimelines.Type.ART);
            managePeertube.setPosition(6);
            manageTimelines.add(managePeertube);

            int i = 6;
            List<TagTimeline> tagTimelines = new SearchDAO(contextReference.get(), db).getAll();
            if( tagTimelines != null && tagTimelines.size() > 0 ){
                for(TagTimeline ttl: tagTimelines){
                    //Add tag timelines
                    ManageTimelines manageTagTimeline = new ManageTimelines();
                    manageTagTimeline.setDisplayed(true);
                    manageTagTimeline.setType(ManageTimelines.Type.TAG);
                    manageTagTimeline.setPosition(i++);
                    manageTagTimeline.setTagTimeline(ttl);
                    manageTimelines.add(manageTagTimeline);
                }
            }
            List<RemoteInstance> instances = new InstancesDAO(contextReference.get(), db).getAllInstances();
            if( instances != null && instances.size() > 0 ){
                for(RemoteInstance ritl: instances){
                    //Add remote instances
                    ManageTimelines manageRemoteTimline = new ManageTimelines();
                    manageRemoteTimline.setDisplayed(true);
                    manageRemoteTimline.setType(ManageTimelines.Type.INSTANCE);
                    manageRemoteTimline.setPosition(i++);
                    manageRemoteTimline.setRemoteInstance(ritl);
                    manageTimelines.add(manageRemoteTimline);
                }
            }

        }
        APIResponse apiResponse = new API(contextReference.get()).getLists();
        List<fr.gouv.etalab.mastodon.client.Entities.List> lists = apiResponse.getLists();
        if( lists != null && lists.size() > 0){
            //Loop through results
            for(fr.gouv.etalab.mastodon.client.Entities.List list: lists){
                boolean isInDb = false;
                ManageTimelines timelines_tmp = null;
                for(ManageTimelines manageTimeline: manageTimelines){
                    if( manageTimeline.getListTimeline() == null )
                        continue;
                    if(manageTimeline.getListTimeline().getId().equals(list.getId())){
                        isInDb = true;
                        timelines_tmp = manageTimeline;
                        break;
                    }
                }
                if( !isInDb){
                    ManageTimelines manageTL = new ManageTimelines();
                    manageTL.setListTimeline(list);
                    manageTL.setDisplayed(true);
                    manageTL.setType(ManageTimelines.Type.LIST);
                    manageTL.setPosition(manageTimelines.size());
                    new TimelinesDAO(contextReference.get(), db).insert(manageTL);
                }else{
                    //Update list
                    timelines_tmp.getListTimeline().setTitle(list.getTitle());
                }
            }
            for(ManageTimelines manageTimelines: manageTimelines){
                if( manageTimelines.getListTimeline() == null )
                    continue;
                boolean shouldBeRemoved = true;
                for(fr.gouv.etalab.mastodon.client.Entities.List list: lists){
                    if( list.getId().equals(manageTimelines.getListTimeline().getId())){
                        shouldBeRemoved = false;
                    }
                }
                if( shouldBeRemoved){
                    new TimelinesDAO(contextReference.get(), db).remove(manageTimelines);
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.syncedTimelines(manageTimelines);
    }

}
