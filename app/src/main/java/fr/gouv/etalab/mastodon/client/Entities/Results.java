package fr.gouv.etalab.mastodon.client.Entities;

import java.util.List;

/**
 * Created by Thomas on 05/05/2017.
 * Manage Results for search
 */

public class Results {

    private List<Account> accounts;
    private List<Status> statuses;
    private List<String> hashtags;

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

    public List<String> getHashtags() {
        return hashtags;
    }

    public void setHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
    }
}
