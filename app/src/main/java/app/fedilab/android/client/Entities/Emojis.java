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
 * Created by Thomas on 20/10/2017.
 */

public class Emojis implements Parcelable {

    private String shortcode;
    private String static_url;
    private String url;
    private boolean visible_in_picker;

    public Emojis(){}

    public String getShortcode() {
        return shortcode;
    }

    public void setShortcode(String shortcode) {
        this.shortcode = shortcode;
    }

    public String getStatic_url() {
        return static_url;
    }

    public void setStatic_url(String static_url) {
        this.static_url = static_url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isVisible_in_picker() {
        return visible_in_picker;
    }

    public void setVisible_in_picker(boolean visible_in_picker) {
        this.visible_in_picker = visible_in_picker;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.shortcode);
        dest.writeString(this.static_url);
        dest.writeString(this.url);
        dest.writeByte(this.visible_in_picker ? (byte) 1 : (byte) 0);
    }

    protected Emojis(Parcel in) {
        this.shortcode = in.readString();
        this.static_url = in.readString();
        this.url = in.readString();
        this.visible_in_picker = in.readByte() != 0;
    }

    public static final Creator<Emojis> CREATOR = new Creator<Emojis>() {
        @Override
        public Emojis createFromParcel(Parcel source) {
            return new Emojis(source);
        }

        @Override
        public Emojis[] newArray(int size) {
            return new Emojis[size];
        }
    };
}
