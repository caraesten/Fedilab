package app.fedilab.android.client.Entities;
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


import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.List;

/**
 * Created by Thomas on 01/11/2019.
 */

public class PixelFedStory implements Parcelable {

    public static final Parcelable.Creator<PixelFedStory> CREATOR = new Parcelable.Creator<PixelFedStory>() {
        @Override
        public PixelFedStory createFromParcel(Parcel source) {
            return new PixelFedStory(source);
        }

        @Override
        public PixelFedStory[] newArray(int size) {
            return new PixelFedStory[size];
        }
    };
    private String id;
    private String photo;
    private String name;
    private String link;
    private Date lastUpdated;
    private boolean seen;
    private List<PixelFedStoryItem> pixelFedStoryItems;

    public PixelFedStory() {
    }

    protected PixelFedStory(Parcel in) {
        this.id = in.readString();
        this.photo = in.readString();
        this.name = in.readString();
        this.link = in.readString();
        long tmpLastUpdated = in.readLong();
        this.lastUpdated = tmpLastUpdated == -1 ? null : new Date(tmpLastUpdated);
        this.seen = in.readByte() != 0;
        this.pixelFedStoryItems = in.createTypedArrayList(PixelFedStoryItem.CREATOR);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public List<PixelFedStoryItem> getPixelFedStoryItems() {
        return pixelFedStoryItems;
    }

    public void setPixelFedStoryItems(List<PixelFedStoryItem> pixelFedStoryItems) {
        this.pixelFedStoryItems = pixelFedStoryItems;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.photo);
        dest.writeString(this.name);
        dest.writeString(this.link);
        dest.writeLong(this.lastUpdated != null ? this.lastUpdated.getTime() : -1);
        dest.writeByte(this.seen ? (byte) 1 : (byte) 0);
        dest.writeTypedList(this.pixelFedStoryItems);
    }
}
