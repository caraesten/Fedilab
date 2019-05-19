/* Copyright 2018 Thomas Schneider
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
package app.fedilab.android.client.Entities;

import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Thomas on 29/09/2018.
 * Manage how to videos
 */
public class Peertube {

    private String id;
    private String uuid;
    private String name;
    private String description;
    private String host;
    private String thumbnailPath;
    private String previewPath;
    private String embedPath;
    private int view;
    private int like;
    private int dislike;
    private Date created_at;
    private int duration;
    private String instance;
    private Account account;
    private Account channel;
    private List<String> resolution;
    private List<String> tags;
    private boolean commentsEnabled;
    private boolean sensitive;
    private HashMap<Integer, String> category;
    private HashMap<Integer, String>  license;
    private HashMap<String, String>  language;
    private HashMap<Integer, String> privacy;
    private HashMap<String, String> channelForUpdate;
    private String myRating = "none";
    private boolean isUpdate = false; // I allow to set it to true when dealing with API updates
    private String headerType = null;//For overview timeline
    private String headerTypeValue = null;//For overview timeline
    private JSONObject cache;

    public Peertube() {
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public String getPreviewPath() {
        return previewPath;
    }

    public void setPreviewPath(String previewPath) {
        this.previewPath = previewPath;
    }

    public String getEmbedPath() {
        return embedPath;
    }

    public void setEmbedPath(String embedPath) {
        this.embedPath = embedPath;
    }


    public int getView() {
        return view;
    }

    public void setView(int view) {
        this.view = view;
    }

    public int getLike() {
        return like;
    }

    public void setLike(int like) {
        this.like = like;
    }

    public int getDislike() {
        return dislike;
    }

    public void setDislike(int dislike) {
        this.dislike = dislike;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getFileUrl(String resolution) {
        if( resolution == null)
            resolution = this.getResolution().get(0);
        if(resolution == null)
            return null;
        return "https://" + this.host + "/static/webseed/" + getUuid()+ "-" + resolution + ".mp4";
    }


    public String getTorrentUrl(String resolution) {
        if( resolution == null)
            resolution = this.getResolution().get(0);
        if(resolution == null)
            return null;
        return "https://" + this.host + "/static/torrents/" + getUuid()+ "-" + resolution + ".torrent";

    }

    public String getTorrentDownloadUrl(String resolution) {
        if( resolution == null)
            resolution = this.getResolution().get(0);
        if(resolution == null)
            return null;
        return "https://" + this.host + "/download/torrents/" + getUuid()+ "-" + resolution + ".torrent";

    }
    public String getFileDownloadUrl(String resolution) {
        if( resolution == null)
            resolution = this.getResolution().get(0);
        if(resolution == null)
            return null;
        return "https://" + this.host + "/download/videos/" + getUuid()+ "-" + resolution + ".mp4";
    }

    public List<String> getResolution() {
        return resolution;
    }

    public void setResolution(List<String> resolution) {
        this.resolution = resolution;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public boolean isCommentsEnabled() {
        return commentsEnabled;
    }

    public void setCommentsEnabled(boolean commentsEnabled) {
        this.commentsEnabled = commentsEnabled;
    }

    public JSONObject getCache() {
        return cache;
    }

    public void setCache(JSONObject cache) {
        this.cache = cache;
    }

    public boolean isSensitive() {
        return sensitive;
    }

    public void setSensitive(boolean sensitive) {
        this.sensitive = sensitive;
    }




    public String getMyRating() {
        return myRating;
    }

    public void setMyRating(String myRating) {
        this.myRating = myRating;
    }

    public Account getChannel() {
        return channel;
    }

    public void setChannel(Account channel) {
        this.channel = channel;
    }


    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public HashMap<Integer, String> getCategory() {
        return category;
    }

    public void setCategory(HashMap<Integer, String> category) {
        this.category = category;
    }

    public HashMap<Integer, String> getLicense() {
        return license;
    }

    public void setLicense(HashMap<Integer, String> license) {
        this.license = license;
    }

    public HashMap<String, String> getLanguage() {
        return language;
    }

    public void setLanguage(HashMap<String, String> language) {
        this.language = language;
    }

    public HashMap<Integer, String> getPrivacy() {
        return privacy;
    }

    public void setPrivacy(HashMap<Integer, String> privacy) {
        this.privacy = privacy;
    }

    public HashMap<String, String> getChannelForUpdate() {
        return channelForUpdate;
    }

    public void setChannelForUpdate(HashMap<String, String> channelForUpdate) {
        this.channelForUpdate = channelForUpdate;
    }

    public boolean isUpdate() {
        return isUpdate;
    }

    public void setUpdate(boolean update) {
        isUpdate = update;
    }

    public String getHeaderType() {
        return headerType;
    }

    public void setHeaderType(String headerType) {
        this.headerType = headerType;
    }

    public String getHeaderTypeValue() {
        return headerTypeValue;
    }

    public void setHeaderTypeValue(String headerTypeValue) {
        this.headerTypeValue = headerTypeValue;
    }
}
