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

/**
 * Created by Thomas on 01/11/2019.
 */

public class PixelFedStoryItem implements Parcelable {

    public static final Parcelable.Creator<PixelFedStoryItem> CREATOR = new Parcelable.Creator<PixelFedStoryItem>() {
        @Override
        public PixelFedStoryItem createFromParcel(Parcel source) {
            return new PixelFedStoryItem(source);
        }

        @Override
        public PixelFedStoryItem[] newArray(int size) {
            return new PixelFedStoryItem[size];
        }
    };
    private String id;
    private String type;
    private int length;
    private String src;
    private String preview;
    private String link;
    private String linkText;
    private Date time;
    private Date expires_at;
    private boolean seen;

    public PixelFedStoryItem() {
    }

    protected PixelFedStoryItem(Parcel in) {
        this.id = in.readString();
        this.type = in.readString();
        this.length = in.readInt();
        this.src = in.readString();
        this.preview = in.readString();
        this.link = in.readString();
        this.linkText = in.readString();
        long tmpTime = in.readLong();
        this.time = tmpTime == -1 ? null : new Date(tmpTime);
        long tmpExpires_at = in.readLong();
        this.expires_at = tmpExpires_at == -1 ? null : new Date(tmpExpires_at);
        this.seen = in.readByte() != 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getPreview() {
        return preview;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getLinkText() {
        return linkText;
    }

    public void setLinkText(String linkText) {
        this.linkText = linkText;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Date getExpires_at() {
        return expires_at;
    }

    public void setExpires_at(Date expires_at) {
        this.expires_at = expires_at;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.type);
        dest.writeInt(this.length);
        dest.writeString(this.src);
        dest.writeString(this.preview);
        dest.writeString(this.link);
        dest.writeString(this.linkText);
        dest.writeLong(this.time != null ? this.time.getTime() : -1);
        dest.writeLong(this.expires_at != null ? this.expires_at.getTime() : -1);
        dest.writeByte(this.seen ? (byte) 1 : (byte) 0);
    }
}
