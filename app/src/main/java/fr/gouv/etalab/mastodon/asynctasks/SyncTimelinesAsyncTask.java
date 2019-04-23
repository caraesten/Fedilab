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
import java.util.Iterator;
import java.util.List;

import fr.gouv.etalab.mastodon.activities.MainActivity;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;

import fr.gouv.etalab.mastodon.client.Entities.Instance;
import fr.gouv.etalab.mastodon.client.Entities.ManageTimelines;
import fr.gouv.etalab.mastodon.client.Entities.RemoteInstance;
import fr.gouv.etalab.mastodon.client.Entities.TagTimeline;
import fr.gouv.etalab.mastodon.helper.Helper;
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
    private boolean updateOnly;

    public SyncTimelinesAsyncTask(Context context, boolean updateOnly, OnSyncTimelineInterface onSyncTimelineInterface){
        this.contextReference = new WeakReference<>(context);
        this.listener = onSyncTimelineInterface;
        this.updateOnly = updateOnly;
    }

    public SyncTimelinesAsyncTask(Context context, OnSyncTimelineInterface onSyncTimelineInterface){
        this.contextReference = new WeakReference<>(context);
        this.listener = onSyncTimelineInterface;
        this.updateOnly = false;
    }

    @Override
    protected Void doInBackground(Void... params) {

        SQLiteDatabase db = Sqlite.getInstance(contextReference.get(), DB_NAME, null, Sqlite.DB_VERSION).open();
        manageTimelines = new TimelinesDAO(contextReference.get(), db).getAllTimelines();
        //First time that the timeline is created
        int i = 0;
        if( manageTimelines == null || manageTimelines.size() == 0){
            manageTimelines = new ArrayList<>();
            //Add home TL
            ManageTimelines manageHome = new ManageTimelines();
            manageHome.setDisplayed(true);
            manageHome.setType(ManageTimelines.Type.HOME);
            manageHome.setPosition(i);
            manageTimelines.add(manageHome);
            i++;
            new TimelinesDAO(contextReference.get(), db).insert(manageHome);
            //Add Direct notification TL
            ManageTimelines manageNotif = new ManageTimelines();
            manageNotif.setDisplayed(true);
            manageNotif.setType(ManageTimelines.Type.NOTIFICATION);
            manageNotif.setPosition(i);
            i++;
            manageTimelines.add(manageNotif);
            new TimelinesDAO(contextReference.get(), db).insert(manageNotif);
            //Add Direct message TL
            ManageTimelines manageDirect = new ManageTimelines();
            manageDirect.setDisplayed(true);
            manageDirect.setType(ManageTimelines.Type.DIRECT);
            manageDirect.setPosition(i);
            i++;
            manageTimelines.add(manageDirect);
            new TimelinesDAO(contextReference.get(), db).insert(manageDirect);
            //Add Local TL
            ManageTimelines manageLocal = new ManageTimelines();
            manageLocal.setDisplayed(true);
            manageLocal.setType(ManageTimelines.Type.LOCAL);
            manageLocal.setPosition(i);
            i++;
            manageTimelines.add(manageLocal);
            new TimelinesDAO(contextReference.get(), db).insert(manageLocal);
            if(MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA){
                //Add Public TL
                ManageTimelines managePublic = new ManageTimelines();
                managePublic.setDisplayed(true);
                managePublic.setType(ManageTimelines.Type.PUBLIC);
                managePublic.setPosition(i);
                i++;
                manageTimelines.add(managePublic);
                new TimelinesDAO(contextReference.get(), db).insert(managePublic);
            }
            if(MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON || MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA) {
                //Add Public ART
                ManageTimelines manageArt = new ManageTimelines();
                manageArt.setDisplayed(true);
                manageArt.setType(ManageTimelines.Type.ART);
                manageArt.setPosition(i);
                i++;
                manageTimelines.add(manageArt);
                new TimelinesDAO(contextReference.get(), db).insert(manageArt);
                //Add Public PEERTUBE
                ManageTimelines managePeertube = new ManageTimelines();
                managePeertube.setDisplayed(true);
                managePeertube.setType(ManageTimelines.Type.PEERTUBE);
                managePeertube.setPosition(i);
                i++;
                manageTimelines.add(managePeertube);
                new TimelinesDAO(contextReference.get(), db).insert(managePeertube);
            }

            List<TagTimeline> tagTimelines = new SearchDAO(contextReference.get(), db).getAll();
            if( tagTimelines != null && tagTimelines.size() > 0 ){
                for(TagTimeline ttl: tagTimelines){
                    //Add tag timelines
                    ManageTimelines manageTagTimeline = new ManageTimelines();
                    manageTagTimeline.setDisplayed(true);
                    manageTagTimeline.setType(ManageTimelines.Type.TAG);
                    manageTagTimeline.setPosition(i);
                    manageTagTimeline.setTagTimeline(ttl);
                    manageTimelines.add(manageTagTimeline);
                    new TimelinesDAO(contextReference.get(), db).insert(manageTagTimeline);
                    i++;
                }
            }
            List<RemoteInstance> instances = new InstancesDAO(contextReference.get(), db).getAllInstances();
            if( instances != null && instances.size() > 0 ){
                for(RemoteInstance ritl: instances){
                    //Add remote instances
                    ManageTimelines manageRemoteTimline = new ManageTimelines();
                    manageRemoteTimline.setDisplayed(true);
                    manageRemoteTimline.setType(ManageTimelines.Type.INSTANCE);
                    manageRemoteTimline.setPosition(i);
                    manageRemoteTimline.setRemoteInstance(ritl);
                    manageTimelines.add(manageRemoteTimline);
                    new TimelinesDAO(contextReference.get(), db).insert(manageRemoteTimline);
                    i++;
                }
            }
        }

        List<TagTimeline> tagsInDb = new SearchDAO(contextReference.get(), db).getAll();
        List<RemoteInstance> instancesInDb = new InstancesDAO(contextReference.get(), db).getAllInstances();

        for(TagTimeline tag: tagsInDb){
            boolean isInDb = false;
            ManageTimelines timelines_tmp = null;
            for(ManageTimelines manageTimeline: manageTimelines){
                if( manageTimeline.getTagTimeline() == null )
                    continue;
                if(manageTimeline.getTagTimeline().getId() == tag.getId()){
                    isInDb = true;
                    timelines_tmp = manageTimeline;
                    break;
                }
            }
            if( !isInDb){
                ManageTimelines manageTL = new ManageTimelines();
                manageTL.setTagTimeline(tag);
                manageTL.setDisplayed(true);
                manageTL.setType(ManageTimelines.Type.TAG);
                manageTL.setPosition(manageTimelines.size());
                new TimelinesDAO(contextReference.get(), db).insert(manageTL);
            }else{
                //Update list
                timelines_tmp.setTagTimeline(tag);
                new TimelinesDAO(contextReference.get(), db).update(timelines_tmp);
            }
        }
        for(ManageTimelines manageTimelines: manageTimelines){
            if( manageTimelines.getTagTimeline() == null )
                continue;
            boolean shouldBeRemoved = true;
            for(TagTimeline tag: tagsInDb){
                if( tag.getId() == manageTimelines.getTagTimeline().getId()){
                    shouldBeRemoved = false;
                }
            }
            if( shouldBeRemoved){
                new TimelinesDAO(contextReference.get(), db).remove(manageTimelines);
            }
        }


        for(RemoteInstance instance: instancesInDb){
            boolean isInDb = false;
            ManageTimelines timelines_tmp = null;
            for(ManageTimelines manageTimeline: manageTimelines){
                if( manageTimeline.getRemoteInstance() == null )
                    continue;
                if(manageTimeline.getRemoteInstance().getId().equals(instance.getId())){
                    isInDb = true;
                    timelines_tmp = manageTimeline;
                    break;
                }
            }
            if( !isInDb){
                ManageTimelines manageTL = new ManageTimelines();
                manageTL.setRemoteInstance(instance);
                manageTL.setDisplayed(true);
                manageTL.setType(ManageTimelines.Type.INSTANCE);
                manageTL.setPosition(manageTimelines.size());
                new TimelinesDAO(contextReference.get(), db).insert(manageTL);
            }else{
                //Update list
                timelines_tmp.setRemoteInstance(instance);
                new TimelinesDAO(contextReference.get(), db).update(timelines_tmp);
            }
        }
        for(ManageTimelines manageTimelines: manageTimelines){
            if( manageTimelines.getRemoteInstance() == null )
                continue;
            boolean shouldBeRemoved = true;
            for(RemoteInstance instance: instancesInDb){
                if( instance.getId().equals(manageTimelines.getRemoteInstance().getId())){
                    shouldBeRemoved = false;
                }
            }
            if( shouldBeRemoved){
                new TimelinesDAO(contextReference.get(), db).remove(manageTimelines);
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
                    timelines_tmp.setListTimeline(list);
                    new TimelinesDAO(contextReference.get(), db).update(timelines_tmp);
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

        for (Iterator<ManageTimelines> it = manageTimelines.iterator(); it.hasNext();) {
            if (!it.next().isDisplayed()) {
                it.remove();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.syncedTimelines(manageTimelines, updateOnly);
    }

}
