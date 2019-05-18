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

public class PollOptions implements Parcelable {

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getVotes_count() {
        return votes_count;
    }

    public void setVotes_count(int votes_count) {
        this.votes_count = votes_count;
    }

    private String title;
    private int votes_count;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeInt(this.votes_count);
    }

    public PollOptions() {
    }

    protected PollOptions(Parcel in) {
        this.title = in.readString();
        this.votes_count = in.readInt();
    }

    public static final Creator<PollOptions> CREATOR = new Creator<PollOptions>() {
        @Override
        public PollOptions createFromParcel(Parcel source) {
            return new PollOptions(source);
        }

        @Override
        public PollOptions[] newArray(int size) {
            return new PollOptions[size];
        }
    };
}
