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
 * Manage Peertube follow
 */
public class PeertubeActorFollow {

    private String id;
    private PeertubeAccountNotification follower;
    private PeertubeAccountNotification following;

    public PeertubeActorFollow() {
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PeertubeAccountNotification getFollower() {
        return follower;
    }

    public void setFollower(PeertubeAccountNotification follower) {
        this.follower = follower;
    }

    public PeertubeAccountNotification getFollowing() {
        return following;
    }

    public void setFollowing(PeertubeAccountNotification following) {
        this.following = following;
    }
}
