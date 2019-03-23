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
package fr.gouv.etalab.mastodon.client.Entities;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Poll implements Parcelable {

    private String id;
    private Date expires_at;
    private boolean expired;
    private boolean multiple;
    private int votes_count;
    private boolean voted;
    private List<PollOptions> optionsList;

    public Date getExpires_at() {
        return expires_at;
    }

    public void setExpires_at(Date expires_at) {
        this.expires_at = expires_at;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    public int getVotes_count() {
        return votes_count;
    }

    public void setVotes_count(int votes_count) {
        this.votes_count = votes_count;
    }

    public boolean isVoted() {
        return voted;
    }

    public void setVoted(boolean voted) {
        this.voted = voted;
    }

    public List<PollOptions> getOptionsList() {
        return optionsList;
    }

    public void setOptionsList(List<PollOptions> optionsList) {
        this.optionsList = optionsList;
    }


    private class PollOptions{
        private String title;
        private String votes_count;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getVotes_count() {
            return votes_count;
        }

        public void setVotes_count(String votes_count) {
            this.votes_count = votes_count;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeLong(this.expires_at != null ? this.expires_at.getTime() : -1);
        dest.writeByte(this.expired ? (byte) 1 : (byte) 0);
        dest.writeByte(this.multiple ? (byte) 1 : (byte) 0);
        dest.writeInt(this.votes_count);
        dest.writeByte(this.voted ? (byte) 1 : (byte) 0);
        dest.writeList(this.optionsList);
    }

    public Poll() {
    }

    protected Poll(Parcel in) {
        this.id = in.readString();
        long tmpExpires_at = in.readLong();
        this.expires_at = tmpExpires_at == -1 ? null : new Date(tmpExpires_at);
        this.expired = in.readByte() != 0;
        this.multiple = in.readByte() != 0;
        this.votes_count = in.readInt();
        this.voted = in.readByte() != 0;
        this.optionsList = new ArrayList<PollOptions>();
        in.readList(this.optionsList, PollOptions.class.getClassLoader());
    }

    public static final Parcelable.Creator<Poll> CREATOR = new Parcelable.Creator<Poll>() {
        @Override
        public Poll createFromParcel(Parcel source) {
            return new Poll(source);
        }

        @Override
        public Poll[] newArray(int size) {
            return new Poll[size];
        }
    };
}
