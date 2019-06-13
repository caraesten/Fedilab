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

public class InstanceReg {

    private String domain;
    private String version;
    private String description;
    private String language;
    private String category;
    private String proxied_thumbnail;
    private int total_users;
    private int last_week_users;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getProxied_thumbnail() {
        return proxied_thumbnail;
    }

    public void setProxied_thumbnail(String proxied_thumbnail) {
        this.proxied_thumbnail = proxied_thumbnail;
    }

    public int getTotal_users() {
        return total_users;
    }

    public void setTotal_users(int total_users) {
        this.total_users = total_users;
    }

    public int getLast_week_users() {
        return last_week_users;
    }

    public void setLast_week_users(int last_week_users) {
        this.last_week_users = last_week_users;
    }
}
