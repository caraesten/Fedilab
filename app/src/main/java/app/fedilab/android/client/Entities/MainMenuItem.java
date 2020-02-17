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
package app.fedilab.android.client.Entities;

public class MainMenuItem {

    private boolean
            nav_news = true,
            nav_trends = true,
            nav_list = true,
            nav_scheduled = true,
            nav_archive = true,
            nav_archive_notifications = true,
            nav_peertube = true,
            nav_filters = true,
            nav_how_to_follow = true,
            nav_blocked = true,
            nav_muted = true,
            nav_blocked_domains = true,
            nav_howto = true;

    public boolean isNav_news() {
        return nav_news;
    }

    public void setNav_news(boolean nav_news) {
        this.nav_news = nav_news;
    }

    public boolean isNav_list() {
        return nav_list;
    }

    public void setNav_list(boolean nav_list) {
        this.nav_list = nav_list;
    }

    public boolean isNav_scheduled() {
        return nav_scheduled;
    }

    public void setNav_scheduled(boolean nav_scheduled) {
        this.nav_scheduled = nav_scheduled;
    }

    public boolean isNav_archive() {
        return nav_archive;
    }

    public void setNav_archive(boolean nav_archive) {
        this.nav_archive = nav_archive;
    }

    public boolean isNav_archive_notifications() {
        return nav_archive_notifications;
    }

    public void setNav_archive_notifications(boolean nav_archive_notifications) {
        this.nav_archive_notifications = nav_archive_notifications;
    }

    public boolean isNav_peertube() {
        return nav_peertube;
    }

    public void setNav_peertube(boolean nav_peertube) {
        this.nav_peertube = nav_peertube;
    }

    public boolean isNav_filters() {
        return nav_filters;
    }

    public void setNav_filters(boolean nav_filters) {
        this.nav_filters = nav_filters;
    }

    public boolean isNav_how_to_follow() {
        return nav_how_to_follow;
    }

    public void setNav_how_to_follow(boolean nav_how_to_follow) {
        this.nav_how_to_follow = nav_how_to_follow;
    }

    public boolean isNav_blocked() {
        return nav_blocked;
    }

    public void setNav_blocked(boolean nav_blocked) {
        this.nav_blocked = nav_blocked;
    }

    public boolean isNav_muted() {
        return nav_muted;
    }

    public void setNav_muted(boolean nav_muted) {
        this.nav_muted = nav_muted;
    }

    public boolean isNav_blocked_domains() {
        return nav_blocked_domains;
    }

    public void setNav_blocked_domains(boolean nav_blocked_domains) {
        this.nav_blocked_domains = nav_blocked_domains;
    }

    public boolean isNav_howto() {
        return nav_howto;
    }

    public void setNav_howto(boolean nav_howto) {
        this.nav_howto = nav_howto;
    }

    public boolean isNav_trends() {
        return nav_trends;
    }

    public void setNav_trends(boolean nav_trends) {
        this.nav_trends = nav_trends;
    }
}
