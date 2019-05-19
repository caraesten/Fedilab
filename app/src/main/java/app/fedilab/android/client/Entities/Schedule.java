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

import java.util.Date;
import java.util.List;

/**
 * Created by Thomas on 18/01/2019.
 * Manages scheduled toots
 */

public class Schedule implements Parcelable {

    private String id;
    private Date scheduled_at;
    private Status status;
    private List<Attachment> attachmentList;

    public Schedule(){}


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getScheduled_at() {
        return scheduled_at;
    }

    public void setScheduled_at(Date scheduled_at) {
        this.scheduled_at = scheduled_at;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<Attachment> getAttachmentList() {
        return attachmentList;
    }

    public void setAttachmentList(List<Attachment> attachmentList) {
        this.attachmentList = attachmentList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeLong(this.scheduled_at != null ? this.scheduled_at.getTime() : -1);
        dest.writeParcelable(this.status, flags);
        dest.writeTypedList(this.attachmentList);
    }

    protected Schedule(Parcel in) {
        this.id = in.readString();
        long tmpScheduled_at = in.readLong();
        this.scheduled_at = tmpScheduled_at == -1 ? null : new Date(tmpScheduled_at);
        this.status = in.readParcelable(Status.class.getClassLoader());
        this.attachmentList = in.createTypedArrayList(Attachment.CREATOR);
    }

    public static final Parcelable.Creator<Schedule> CREATOR = new Parcelable.Creator<Schedule>() {
        @Override
        public Schedule createFromParcel(Parcel source) {
            return new Schedule(source);
        }

        @Override
        public Schedule[] newArray(int size) {
            return new Schedule[size];
        }
    };
}
