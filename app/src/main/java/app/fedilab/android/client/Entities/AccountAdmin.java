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

import java.util.Date;

public class AccountAdmin implements Parcelable {

    private String id;
    private String username;
    private Date created_at;
    private String email;
    private String role;
    private String ip;
    private boolean confirmed;
    private boolean suspended;
    private boolean silenced;
    private boolean disabled;
    private Account account;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public boolean isSilenced() {
        return silenced;
    }

    public void setSilenced(boolean silenced) {
        this.silenced = silenced;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }




    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.username);
        dest.writeLong(this.created_at != null ? this.created_at.getTime() : -1);
        dest.writeString(this.email);
        dest.writeString(this.role);
        dest.writeString(this.ip);
        dest.writeByte(this.confirmed ? (byte) 1 : (byte) 0);
        dest.writeByte(this.suspended ? (byte) 1 : (byte) 0);
        dest.writeByte(this.silenced ? (byte) 1 : (byte) 0);
        dest.writeByte(this.disabled ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.account, flags);
    }

    public AccountAdmin() {
    }

    protected AccountAdmin(Parcel in) {
        this.id = in.readString();
        this.username = in.readString();
        long tmpCreated_at = in.readLong();
        this.created_at = tmpCreated_at == -1 ? null : new Date(tmpCreated_at);
        this.email = in.readString();
        this.role = in.readString();
        this.ip = in.readString();
        this.confirmed = in.readByte() != 0;
        this.suspended = in.readByte() != 0;
        this.silenced = in.readByte() != 0;
        this.disabled = in.readByte() != 0;
        this.account = in.readParcelable(Account.class.getClassLoader());
    }

    public static final Parcelable.Creator<AccountAdmin> CREATOR = new Parcelable.Creator<AccountAdmin>() {
        @Override
        public AccountAdmin createFromParcel(Parcel source) {
            return new AccountAdmin(source);
        }

        @Override
        public AccountAdmin[] newArray(int size) {
            return new AccountAdmin[size];
        }
    };
}
