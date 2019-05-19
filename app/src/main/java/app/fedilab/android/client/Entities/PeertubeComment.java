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
package app.fedilab.android.client.Entities;

/**
 * Created by Thomas on 23/01/2019.
 * Manage Peertube comments
 */
public class PeertubeComment {

    private String id;
    private String threadId;
    private PeertubeVideoNotification peertubeVideoNotification;
    private PeertubeAccountNotification peertubeAccountNotification;

    public PeertubeComment() {
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public PeertubeVideoNotification getPeertubeVideoNotification() {
        return peertubeVideoNotification;
    }

    public void setPeertubeVideoNotification(PeertubeVideoNotification peertubeVideoNotification) {
        this.peertubeVideoNotification = peertubeVideoNotification;
    }

    public PeertubeAccountNotification getPeertubeAccountNotification() {
        return peertubeAccountNotification;
    }

    public void setPeertubeAccountNotification(PeertubeAccountNotification peertubeAccountNotification) {
        this.peertubeAccountNotification = peertubeAccountNotification;
    }
}
