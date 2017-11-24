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
 * Created by Thomas on 24/11/2017.
 * Describes instance from instances.social
 */

public class InstanceSocial {

    private String id;
    private String name;
    private Date added_at;
    private Date updated_at;
    private Date checked_at;
    private float uptime;
    private boolean up;
    private boolean dead;
    private String version;
    private boolean ipv6;
    private int https_score;
    private String https_rank;
    private int obs_score;
    private String obs_rank;
    private long users;
    private long statuses;
    private long connections;
    private boolean open_registrations;
    private String info;
    private String thumbnail;



    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getAdded_at() {
        return added_at;
    }

    public void setAdded_at(Date added_at) {
        this.added_at = added_at;
    }

    public Date getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(Date updated_at) {
        this.updated_at = updated_at;
    }

    public Date getChecked_at() {
        return checked_at;
    }

    public void setChecked_at(Date checked_at) {
        this.checked_at = checked_at;
    }

    public float getUptime() {
        return uptime;
    }

    public void setUptime(float uptime) {
        this.uptime = uptime;
    }

    public boolean isUp() {
        return up;
    }

    public void setUp(boolean up) {
        this.up = up;
    }

    public boolean isDead() {
        return dead;
    }

    public void setDead(boolean dead) {
        this.dead = dead;
    }

    public boolean isIpv6() {
        return ipv6;
    }

    public void setIpv6(boolean ipv6) {
        this.ipv6 = ipv6;
    }

    public int getHttps_score() {
        return https_score;
    }

    public void setHttps_score(int https_score) {
        this.https_score = https_score;
    }

    public String getHttps_rank() {
        return https_rank;
    }

    public void setHttps_rank(String https_rank) {
        this.https_rank = https_rank;
    }

    public int getObs_score() {
        return obs_score;
    }

    public void setObs_score(int obs_score) {
        this.obs_score = obs_score;
    }

    public String getObs_rank() {
        return obs_rank;
    }

    public void setObs_rank(String obs_rank) {
        this.obs_rank = obs_rank;
    }

    public long getUsers() {
        return users;
    }

    public void setUsers(long users) {
        this.users = users;
    }

    public long getStatuses() {
        return statuses;
    }

    public void setStatuses(long statuses) {
        this.statuses = statuses;
    }

    public long getConnections() {
        return connections;
    }

    public void setConnections(long connections) {
        this.connections = connections;
    }

    public boolean isOpen_registrations() {
        return open_registrations;
    }

    public void setOpen_registrations(boolean open_registrations) {
        this.open_registrations = open_registrations;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
}
