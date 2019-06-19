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
import java.util.List;

public class Report implements Parcelable {

    private String id;
    private String action_taken;
    private String comment;
    private Date created_at;
    private Date updated_at;
    private Account account;
    private Account target_account;
    private Account assigned_account;
    private String action_taken_by_account_id;
    private List<Status> statuses;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAction_taken() {
        return action_taken;
    }

    public void setAction_taken(String action_taken) {
        this.action_taken = action_taken;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public Date getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(Date updated_at) {
        this.updated_at = updated_at;
    }


    public String getAction_taken_by_account_id() {
        return action_taken_by_account_id;
    }

    public void setAction_taken_by_account_id(String action_taken_by_account_id) {
        this.action_taken_by_account_id = action_taken_by_account_id;
    }

    public List<Status> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<Status> statuses) {
        this.statuses = statuses;
    }


    public Report() {
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Account getTarget_account() {
        return target_account;
    }

    public void setTarget_account(Account target_account) {
        this.target_account = target_account;
    }

    public Account getAssigned_account() {
        return assigned_account;
    }

    public void setAssigned_account(Account assigned_account) {
        this.assigned_account = assigned_account;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.action_taken);
        dest.writeString(this.comment);
        dest.writeLong(this.created_at != null ? this.created_at.getTime() : -1);
        dest.writeLong(this.updated_at != null ? this.updated_at.getTime() : -1);
        dest.writeParcelable(this.account, flags);
        dest.writeParcelable(this.target_account, flags);
        dest.writeParcelable(this.assigned_account, flags);
        dest.writeString(this.action_taken_by_account_id);
        dest.writeTypedList(this.statuses);
    }

    protected Report(Parcel in) {
        this.id = in.readString();
        this.action_taken = in.readString();
        this.comment = in.readString();
        long tmpCreated_at = in.readLong();
        this.created_at = tmpCreated_at == -1 ? null : new Date(tmpCreated_at);
        long tmpUpdated_at = in.readLong();
        this.updated_at = tmpUpdated_at == -1 ? null : new Date(tmpUpdated_at);
        this.account = in.readParcelable(Account.class.getClassLoader());
        this.target_account = in.readParcelable(Account.class.getClassLoader());
        this.assigned_account = in.readParcelable(Account.class.getClassLoader());
        this.action_taken_by_account_id = in.readString();
        this.statuses = in.createTypedArrayList(Status.CREATOR);
    }

    public static final Creator<Report> CREATOR = new Creator<Report>() {
        @Override
        public Report createFromParcel(Parcel source) {
            return new Report(source);
        }

        @Override
        public Report[] newArray(int size) {
            return new Report[size];
        }
    };
}
