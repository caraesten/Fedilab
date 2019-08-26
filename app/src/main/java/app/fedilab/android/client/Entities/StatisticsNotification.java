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


import java.util.Date;

public class StatisticsNotification {


    private int total_notification = 0;
    private int number_reblog = 0;
    private int number_favourite = 0;
    private int number_mentions = 0;
    private int number_follow = 0;
    private int number_poll = 0;
    private Date firstTootDate;
    private Date lastTootDate;
    private float frequency;

    public int getTotal_notification() {
        return total_notification;
    }

    public void setTotal_notification(int total_notification) {
        this.total_notification = total_notification;
    }

    public int getNumber_reblog() {
        return number_reblog;
    }

    public void setNumber_reblog(int number_reblog) {
        this.number_reblog = number_reblog;
    }

    public int getNumber_favourite() {
        return number_favourite;
    }

    public void setNumber_favourite(int number_favourite) {
        this.number_favourite = number_favourite;
    }

    public int getNumber_mentions() {
        return number_mentions;
    }

    public void setNumber_mentions(int number_mentions) {
        this.number_mentions = number_mentions;
    }

    public int getNumber_follow() {
        return number_follow;
    }

    public void setNumber_follow(int number_follow) {
        this.number_follow = number_follow;
    }

    public int getNumber_poll() {
        return number_poll;
    }

    public void setNumber_poll(int number_poll) {
        this.number_poll = number_poll;
    }

    public Date getFirstTootDate() {
        return firstTootDate;
    }

    public void setFirstTootDate(Date firstTootDate) {
        this.firstTootDate = firstTootDate;
    }

    public Date getLastTootDate() {
        return lastTootDate;
    }

    public void setLastTootDate(Date lastTootDate) {
        this.lastTootDate = lastTootDate;
    }

    public float getFrequency() {
        return frequency;
    }

    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }
}
