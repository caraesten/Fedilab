package app.fedilab.android.client.Entities;

import java.util.List;

/**
 * Created by Thomas on 05/05/2017.
 * Manage Results for search
 */

public class Results {

    private List<Account> accounts;
    private List<Status> statuses;
    private List<String> hashtags;
    private List<Trends> trends;

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

    public List<Trends> getTrends() {
        return trends;
    }

    public void setTrends(List<Trends> trends) {
        this.trends = trends;
    }
}
