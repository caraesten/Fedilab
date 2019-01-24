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
package fr.gouv.etalab.mastodon.client.Entities;



import java.util.Date;

/**
 * Created by Thomas on 23/01/2019.
 */

public class PeertubeNotification  {

    private String id;
    private boolean read;
    private Date updatedAt;
    private int type;
    private PeertubeComment peertubeComment;
    private PeertubeVideoNotification peertubeVideoNotification;
    private PeertubeActorFollow peertubeActorFollow;

    public PeertubeNotification(){};

    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public PeertubeComment getPeertubeComment() {
        return peertubeComment;
    }

    public void setPeertubeComment(PeertubeComment peertubeComment) {
        this.peertubeComment = peertubeComment;
    }


    public PeertubeActorFollow getPeertubeActorFollow() {
        return peertubeActorFollow;
    }

    public void setPeertubeActorFollow(PeertubeActorFollow peertubeActorFollow) {
        this.peertubeActorFollow = peertubeActorFollow;
    }

    public PeertubeVideoNotification getPeertubeVideoNotification() {
        return peertubeVideoNotification;
    }

    public void setPeertubeVideoNotification(PeertubeVideoNotification peertubeVideoNotification) {
        this.peertubeVideoNotification = peertubeVideoNotification;
    }
}
