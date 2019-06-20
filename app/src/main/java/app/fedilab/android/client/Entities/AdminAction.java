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
public class AdminAction {


    public enum adminActionType{
        NONE,
        DISABLE,
        SILENCE,
        SUSPEND
    }

    private adminActionType type;
    private boolean send_email_notification;
    private String text;
    private boolean unresolved;


    public boolean isUnresolved() {
        return unresolved;
    }

    public void setUnresolved(boolean unresolved) {
        this.unresolved = unresolved;
    }

    public adminActionType getType() {
        return type;
    }

    public void setType(adminActionType type) {
        this.type = type;
    }

    public boolean isSend_email_notification() {
        return send_email_notification;
    }

    public void setSend_email_notification(boolean send_email_notification) {
        this.send_email_notification = send_email_notification;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }




}
