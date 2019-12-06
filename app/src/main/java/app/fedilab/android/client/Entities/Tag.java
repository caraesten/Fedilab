/* Copyright 2017 Thomas Schneider
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thomas on 23/04/2017.
 * Manage Tags
 */

public class Tag implements Parcelable {

    private String name;
    private String url;
    private List<TrendsHistory> trendsHistory;
    public Tag() {
    }

    public List<TrendsHistory> getTrendsHistory() {
        return trendsHistory;
    }

    public void setTrendsHistory(List<TrendsHistory> trendsHistory) {
        this.trendsHistory = trendsHistory;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.url);
        dest.writeList(this.trendsHistory);
    }

    protected Tag(Parcel in) {
        this.name = in.readString();
        this.url = in.readString();
        this.trendsHistory = new ArrayList<TrendsHistory>();
        in.readList(this.trendsHistory, TrendsHistory.class.getClassLoader());
    }

    public static final Creator<Tag> CREATOR = new Creator<Tag>() {
        @Override
        public Tag createFromParcel(Parcel source) {
            return new Tag(source);
        }

        @Override
        public Tag[] newArray(int size) {
            return new Tag[size];
        }
    };
}
