package fr.gouv.etalab.mastodon.client.Entities;

import java.util.Date;


/**
 * Created by Thomas on 15/07/2017.
 * Manage Stored status
 */

public class StoredStatus {

    private int id;
    private Date creation_date;
    private Date scheduled_date;
    private Date sent_date;
    private int jobId;
    private boolean isSent;
    private Status status;
    private String instance;
    private String userId;

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
}
