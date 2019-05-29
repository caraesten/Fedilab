/* Copyright 2019 Thomas Schneider
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

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.HashMap;


/**
 * Created by Thomas on 26/05/2019.
 * Manage List
 */

public class Playlist implements Parcelable {

    private String id;
    private String uuid;
    private String displayName;
    private String description;
    private String videoChannelId;
    private Date createdAt;
    private boolean isLocal;
    private Account ownerAccount;
    private HashMap<Integer, String> privacy;
    private String thumbnailPath;
    private HashMap<Integer, String> type;
    private Date updatedAt;
    private int videosLength;


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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVideoChannelId() {
        return videoChannelId;
    }

    public void setVideoChannelId(String videoChannelId) {
        this.videoChannelId = videoChannelId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isLocal() {
        return isLocal;
    }

    public void setLocal(boolean local) {
        isLocal = local;
    }

    public Account getOwnerAccount() {
        return ownerAccount;
    }

    public void setOwnerAccount(Account ownerAccount) {
        this.ownerAccount = ownerAccount;
    }

    public HashMap<Integer, String> getPrivacy() {
        return privacy;
    }

    public void setPrivacy(HashMap<Integer, String> privacy) {
        this.privacy = privacy;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public HashMap<Integer, String> getType() {
        return type;
    }

    public void setType(HashMap<Integer, String> type) {
        this.type = type;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getVideosLength() {
        return videosLength;
    }

    public void setVideosLength(int videosLength) {
        this.videosLength = videosLength;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.uuid);
        dest.writeString(this.displayName);
        dest.writeString(this.description);
        dest.writeString(this.videoChannelId);
        dest.writeLong(this.createdAt != null ? this.createdAt.getTime() : -1);
        dest.writeByte(this.isLocal ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.ownerAccount, flags);
        dest.writeSerializable(this.privacy);
        dest.writeString(this.thumbnailPath);
        dest.writeSerializable(this.type);
        dest.writeLong(this.updatedAt != null ? this.updatedAt.getTime() : -1);
        dest.writeInt(this.videosLength);
    }

    public Playlist() {
    }

    protected Playlist(Parcel in) {
        this.id = in.readString();
        this.uuid = in.readString();
        this.displayName = in.readString();
        this.description = in.readString();
        this.videoChannelId = in.readString();
        long tmpCreatedAt = in.readLong();
        this.createdAt = tmpCreatedAt == -1 ? null : new Date(tmpCreatedAt);
        this.isLocal = in.readByte() != 0;
        this.ownerAccount = in.readParcelable(Account.class.getClassLoader());
        this.privacy = (HashMap<Integer, String>) in.readSerializable();
        this.thumbnailPath = in.readString();
        this.type = (HashMap<Integer, String>) in.readSerializable();
        long tmpUpdatedAt = in.readLong();
        this.updatedAt = tmpUpdatedAt == -1 ? null : new Date(tmpUpdatedAt);
        this.videosLength = in.readInt();
    }

    public static final Parcelable.Creator<Playlist> CREATOR = new Parcelable.Creator<Playlist>() {
        @Override
        public Playlist createFromParcel(Parcel source) {
            return new Playlist(source);
        }

        @Override
        public Playlist[] newArray(int size) {
            return new Playlist[size];
        }
    };
}
