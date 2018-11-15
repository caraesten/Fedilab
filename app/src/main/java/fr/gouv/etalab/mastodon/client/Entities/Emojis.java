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

/**
 * Created by Thomas on 20/10/2017.
 */

public class Emojis implements Parcelable {

    private String shortcode;
    private String static_url;
    private String url;

    public Emojis(){}

    protected Emojis(Parcel in) {
        shortcode = in.readString();
        static_url = in.readString();
        url = in.readString();
    }

    public static final Creator<Emojis> CREATOR = new Creator<Emojis>() {
        @Override
        public Emojis createFromParcel(Parcel in) {
            return new Emojis(in);
        }

        @Override
        public Emojis[] newArray(int size) {
            return new Emojis[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(shortcode);
        dest.writeString(static_url);
        dest.writeString(url);
    }
}
