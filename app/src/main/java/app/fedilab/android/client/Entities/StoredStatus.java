package app.fedilab.android.client.Entities;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;


/**
 * Created by Thomas on 15/07/2017.
 * Manage Stored status
 */

public class StoredStatus implements Parcelable {

    private int id;
    private Date creation_date;
    private Date scheduled_date;
    private Date sent_date;
    private int jobId;
    private boolean isSent;
    private Status status;
    private Status statusReply;
    private String instance;
    private String userId;
    private String scheduledServerdId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getCreation_date() {
        return creation_date;
    }

    public void setCreation_date(Date creation_date) {
        this.creation_date = creation_date;
    }

    public Date getScheduled_date() {
        return scheduled_date;
    }

    public void setScheduled_date(Date scheduled_date) {
        this.scheduled_date = scheduled_date;
    }

    public Date getSent_date() {
        return sent_date;
    }

    public void setSent_date(Date sent_date) {
        this.sent_date = sent_date;
    }


    public boolean isSent() {
        return isSent;
    }

    public void setSent(boolean sent) {
        isSent = sent;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Status getStatusReply() {
        return statusReply;
    }

    public void setStatusReply(Status statusReply) {
        this.statusReply = statusReply;
    }


    public String getScheduledServerdId() {
        return scheduledServerdId;
    }

    public void setScheduledServerdId(String scheduledServerdId) {
        this.scheduledServerdId = scheduledServerdId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeLong(this.creation_date != null ? this.creation_date.getTime() : -1);
        dest.writeLong(this.scheduled_date != null ? this.scheduled_date.getTime() : -1);
        dest.writeLong(this.sent_date != null ? this.sent_date.getTime() : -1);
        dest.writeInt(this.jobId);
        dest.writeByte(this.isSent ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.status, flags);
        dest.writeParcelable(this.statusReply, flags);
        dest.writeString(this.instance);
        dest.writeString(this.userId);
        dest.writeString(this.scheduledServerdId);
    }

    public StoredStatus() {
    }

    protected StoredStatus(Parcel in) {
        this.id = in.readInt();
        long tmpCreation_date = in.readLong();
        this.creation_date = tmpCreation_date == -1 ? null : new Date(tmpCreation_date);
        long tmpScheduled_date = in.readLong();
        this.scheduled_date = tmpScheduled_date == -1 ? null : new Date(tmpScheduled_date);
        long tmpSent_date = in.readLong();
        this.sent_date = tmpSent_date == -1 ? null : new Date(tmpSent_date);
        this.jobId = in.readInt();
        this.isSent = in.readByte() != 0;
        this.status = in.readParcelable(Status.class.getClassLoader());
        this.statusReply = in.readParcelable(Status.class.getClassLoader());
        this.instance = in.readString();
        this.userId = in.readString();
        this.scheduledServerdId = in.readString();
    }

    public static final Creator<StoredStatus> CREATOR = new Creator<StoredStatus>() {
        @Override
        public StoredStatus createFromParcel(Parcel source) {
            return new StoredStatus(source);
        }

        @Override
        public StoredStatus[] newArray(int size) {
            return new StoredStatus[size];
        }
    };
}
