package fr.gouv.etalab.mastodon.client.Entities;
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


import android.content.Context;
import android.content.SharedPreferences;

import fr.gouv.etalab.mastodon.activities.MainActivity;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveFeedsAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.UpdateAccountInfoAsyncTask;
import fr.gouv.etalab.mastodon.helper.Helper;


public class ManageTimelines {

    private int position;
    private int id;
    private boolean displayed;
    private Type type;
    private static String userId;
    private static String instance;
    private RemoteInstance remoteInstance;
    private TagTimeline tagTimeline;
    private List listTimeline;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isDisplayed() {
        return displayed;
    }

    public void setDisplayed(boolean displayed) {
        this.displayed = displayed;
    }

    public ManageTimelines.Type getType() {
        return type;
    }

    public void setType(ManageTimelines.Type type) {
        this.type = type;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }


    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public RemoteInstance getRemoteInstance() {
        return remoteInstance;
    }

    public void setRemoteInstance(RemoteInstance remoteInstance) {
        this.remoteInstance = remoteInstance;
    }

    public TagTimeline getTagTimeline() {
        return tagTimeline;
    }

    public void setTagTimeline(TagTimeline tagTimeline) {
        this.tagTimeline = tagTimeline;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List getListTimeline() {
        return listTimeline;
    }

    public void setListTimeline(List listTimeline) {
        this.listTimeline = listTimeline;
    }

    public enum Type{
        HOME,
        DIRECT,
        NOTIFICATION,
        LOCAL,
        PUBLIC,
        ART,
        PEERTUBE,
        TAG,
        LIST,
        INSTANCE
    }


    public static Type typeFromDb(String value){
        switch (value){
            case "HOME":
                return Type.HOME;
            case "DIRECT":
                return Type.DIRECT;
            case "NOTIFICATION":
                return Type.NOTIFICATION;
            case "LOCAL":
                return Type.LOCAL;
            case "PUBLIC":
                return Type.PUBLIC;
            case "ART":
                return Type.ART;
            case "PEERTUBE":
                return Type.PEERTUBE;
            case "TAG":
                return Type.TAG;
            case "LIST":
                return Type.LIST;
            case "INSTANCE":
                return Type.INSTANCE;
        }
        return null;
    }

    public static String typeToDb(Type type){
        switch (type){
            case HOME:
                return "HOME";
            case DIRECT:
                return "DIRECT";
            case NOTIFICATION:
                return "NOTIFICATION";
            case LOCAL:
                return "LOCAL";
            case PUBLIC:
                return "PUBLIC";
            case ART:
                return "ART";
            case PEERTUBE:
                return "PEERTUBE";
            case TAG:
                return "TAG";
            case LIST:
                return "LIST";
            case INSTANCE:
                return "INSTANCE";
        }
        return null;
    }


    public static RetrieveFeedsAsyncTask.Type transform(Context context, Type type){

        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        if(MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON || MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA ){
            switch (type){
                case HOME:
                    return RetrieveFeedsAsyncTask.Type.HOME;
                case DIRECT:
                    userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
                    instance = sharedpreferences.getString(Helper.PREF_INSTANCE, Helper.getLiveInstance(context));
                    String instanceVersion = sharedpreferences.getString(Helper.INSTANCE_VERSION + userId + instance, null);
                    if (instanceVersion != null) {
                        Version currentVersion = new Version(instanceVersion);
                        Version minVersion = new Version("2.6");
                        if (currentVersion.compareTo(minVersion) == 1 || currentVersion.equals(minVersion)) {
                            return RetrieveFeedsAsyncTask.Type.CONVERSATION;
                        } else {
                            return RetrieveFeedsAsyncTask.Type.DIRECT;
                        }
                    } else {
                        return RetrieveFeedsAsyncTask.Type.DIRECT;
                    }
                case NOTIFICATION:
                    return RetrieveFeedsAsyncTask.Type.NOTIFICATION;
                case PUBLIC:
                    return RetrieveFeedsAsyncTask.Type.PUBLIC;
                case LOCAL:
                    return RetrieveFeedsAsyncTask.Type.LOCAL;
                case ART:
                    return RetrieveFeedsAsyncTask.Type.ART;
                case PEERTUBE:
                    return RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE;
                case INSTANCE:
                    return RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE;
                case TAG:
                    return RetrieveFeedsAsyncTask.Type.TAG;
                case LIST:
                    return RetrieveFeedsAsyncTask.Type.LIST;
            }
            return null;
        }else if(MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA){
            switch (type) {
                case HOME:
                    return RetrieveFeedsAsyncTask.Type.GNU_HOME;
                case NOTIFICATION:
                    return RetrieveFeedsAsyncTask.Type.GNU_NOTIFICATION;
                case DIRECT:
                    return RetrieveFeedsAsyncTask.Type.GNU_DM;
                case LOCAL:
                    return RetrieveFeedsAsyncTask.Type.GNU_LOCAL;
            }
            return null;
        }else if(MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.GNU){
            switch (type) {
                case HOME:
                    return RetrieveFeedsAsyncTask.Type.GNU_HOME;
                case NOTIFICATION:
                    return RetrieveFeedsAsyncTask.Type.GNU_NOTIFICATION;
                case DIRECT:
                    return RetrieveFeedsAsyncTask.Type.GNU_DM;
                case LOCAL:
                    return RetrieveFeedsAsyncTask.Type.GNU_LOCAL;
                case PUBLIC:
                    return RetrieveFeedsAsyncTask.Type.GNU_WHOLE;
                case TAG:
                    return RetrieveFeedsAsyncTask.Type.GNU_TAG;
            }
            return null;
        }
        return null;
    }


}
