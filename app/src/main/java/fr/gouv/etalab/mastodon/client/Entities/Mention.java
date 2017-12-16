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

import java.io.Serializable;

/**
 * Created by Thomas on 23/04/2017.
 * Manages mentions in toots
 */

public class Mention implements Parcelable {

    private String url;
    private String username;
    private String acct;
    private String id;

    private Mention(Parcel in) {
        url = in.readString();
        username = in.readString();
        acct = in.readString();
        id = in.readString();
    }

    public Mention(){}

    public static final Creator<Mention> CREATOR = new Creator<Mention>() {
        @Override
        public Mention createFromParcel(Parcel in) {
            return new Mention(in);
        }

        @Override
        public Mention[] newArray(int size) {
            return new Mention[size];
        }
    };

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAcct() {
        return acct;
    }

    public void setAcct(String acct) {
        this.acct = acct;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(username);
        dest.writeString(acct);
        dest.writeString(id);
    }
}
