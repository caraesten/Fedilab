/* Copyright 2017 Thomas Schneider
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

import java.util.Date;

/**
 * Created by Thomas on 23/04/2017.
 */

public class Notification implements Parcelable {

    private String id;
    private String type;
    private Date created_at;
    private Account account;
    private Status status;

    protected Notification(Parcel in) {
        id = in.readString();
        type = in.readString();
        account = in.readParcelable(Account.class.getClassLoader());
        status = in.readParcelable(Status.class.getClassLoader());
    }

    public Notification(){};

    public static final Creator<Notification> CREATOR = new Creator<Notification>() {
        @Override
        public Notification createFromParcel(Parcel in) {
            return new Notification(in);
        }

        @Override
        public Notification[] newArray(int size) {
            return new Notification[size];
        }
    };

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

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(type);
        dest.writeParcelable(account, flags);
        dest.writeParcelable(status, flags);
    }

    @Override
    public boolean equals(Object otherNotifications) {
        return otherNotifications != null && (otherNotifications == this || otherNotifications instanceof Notification && this.getId().equals(((Notification) otherNotifications).getId()));
    }
}
