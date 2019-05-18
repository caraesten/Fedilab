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

/**
 * Created by Thomas on 27/01/2019.
 * Manage Entity PleromaAdmin
 */

public class PleromaAdmin implements Parcelable {

    private String nickname;
    private String email;
    private String password;
    private String tags;

    public PleromaAdmin(){}


    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.nickname);
        dest.writeString(this.email);
        dest.writeString(this.password);
        dest.writeString(this.tags);
    }

    protected PleromaAdmin(Parcel in) {
        this.nickname = in.readString();
        this.email = in.readString();
        this.password = in.readString();
        this.tags = in.readString();
    }

    public static final Creator<PleromaAdmin> CREATOR = new Creator<PleromaAdmin>() {
        @Override
        public PleromaAdmin createFromParcel(Parcel source) {
            return new PleromaAdmin(source);
        }

        @Override
        public PleromaAdmin[] newArray(int size) {
            return new PleromaAdmin[size];
        }
    };
}
