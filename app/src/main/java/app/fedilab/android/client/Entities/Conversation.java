package app.fedilab.android.client.Entities;
/* Copyright 2018 Thomas Schneider
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


import java.util.List;

/**
 * Created by Thomas on 26/10/2018.
 * Manage conversation
 */

public class Conversation {

    private String id;
    private List<Account> accounts;
    private Status last_status;
    private boolean unread;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Status getLast_status() {
        return last_status;
    }

    public void setLast_status(Status last_status) {
        this.last_status = last_status;
    }

    public boolean isUnread() {
        return unread;
    }

    public void setUnread(boolean unread) {
        this.unread = unread;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }
}
