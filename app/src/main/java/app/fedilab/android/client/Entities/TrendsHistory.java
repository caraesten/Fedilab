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

/**
 * Created by Thomas on 30/11/2019.
 * Manage Tag trends history
 */

public class TrendsHistory implements Parcelable {

    private long days;
    private int uses;
    private int accounts;

    public long getDays() {
        return days;
    }

    public void setDays(long days) {
        this.days = days;
    }

    public int getUses() {
        return uses;
    }

    public void setUses(int uses) {
        this.uses = uses;
    }

    public int getAccounts() {
        return accounts;
    }

    public void setAccounts(int accounts) {
        this.accounts = accounts;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.days);
        dest.writeInt(this.uses);
        dest.writeInt(this.accounts);
    }

    public TrendsHistory() {
    }

    protected TrendsHistory(Parcel in) {
        this.days = in.readLong();
        this.uses = in.readInt();
        this.accounts = in.readInt();
    }

    public static final Parcelable.Creator<TrendsHistory> CREATOR = new Parcelable.Creator<TrendsHistory>() {
        @Override
        public TrendsHistory createFromParcel(Parcel source) {
            return new TrendsHistory(source);
        }

        @Override
        public TrendsHistory[] newArray(int size) {
            return new TrendsHistory[size];
        }
    };
}
