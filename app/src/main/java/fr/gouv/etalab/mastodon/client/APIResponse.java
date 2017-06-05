package fr.gouv.etalab.mastodon.client;

import android.content.Context;

import java.util.List;

import fr.gouv.etalab.mastodon.client.Entities.*;
import fr.gouv.etalab.mastodon.client.Entities.Error;

/**
 * Created by Thomas on 03/06/2017.
 * Hydrate response from the API
 */

public class APIResponse {

    private List<Account> accounts = null;
    private List<Status> statuses = null;
    private List<Context> contexts = null;
    private List<Notification> notifications = null;
    private fr.gouv.etalab.mastodon.client.Entities.Error error = null;
    private String since_id, max_id;
    private Instance instance;

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public List<Status> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<Status> statuses) {
        this.statuses = statuses;
    }

    public List<Context> getContexts() {
        return contexts;
    }

    public void setContexts(List<Context> contexts) {
        this.contexts = contexts;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }

    public String getMax_id() {
        return max_id;
    }

    public void setMax_id(String max_id) {
        this.max_id = max_id;
    }

    public String getSince_id() {
        return since_id;
    }

    public void setSince_id(String since_id) {
        this.since_id = since_id;
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }
}
