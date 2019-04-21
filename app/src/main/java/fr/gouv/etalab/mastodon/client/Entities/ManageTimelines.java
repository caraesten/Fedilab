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



public class ManageTimelines {

    private int position;
    private int id;
    private boolean displayed;
    private Type type;
    private String referencedBy;
    private String userId;
    private String instance;
    private RemoteInstance remoteInstance;
    private TagTimeline tagTimeline;

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


    public String getReferencedBy() {
        return referencedBy;
    }

    public void setReferencedBy(String referencedBy) {
        this.referencedBy = referencedBy;
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



}
