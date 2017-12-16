package fr.gouv.etalab.mastodon.client;
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
    private List<Relationship> relationships = null;
    private List<fr.gouv.etalab.mastodon.client.Entities.List> lists = null;
    private List<Emojis> emojis = null;
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

    public List<Relationship> getRelationships() {
        return relationships;
    }

    public void setRelationships(List<Relationship> relationships) {
        this.relationships = relationships;
    }

    public List<Emojis> getEmojis() {
        return emojis;
    }

    public void setEmojis(List<Emojis> emojis) {
        this.emojis = emojis;
    }

    public List<fr.gouv.etalab.mastodon.client.Entities.List> getLists() {
        return lists;
    }

    public void setLists(List<fr.gouv.etalab.mastodon.client.Entities.List> lists) {
        this.lists = lists;
    }
}
