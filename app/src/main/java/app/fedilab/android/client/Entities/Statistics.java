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
import java.util.HashMap;
import java.util.Map;

public class Statistics {


    private int total_statuses = 0;
    private int number_boosts = 0;
    private int number_replies = 0;
    private int number_status = 0;
    private int number_with_media = 0;
    private int number_with_cw = 0;
    private int number_with_sensitive_media = 0;
    private int v_public = 0;
    private int v_unlisted = 0;
    private int v_private = 0;
    private Date firstTootDate;
    private Date lastTootDate;
    private float frequency;
    private Map<String, Integer> tagsTrend = new HashMap<>();

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

    public int getTotal_statuses() {
        return total_statuses;
    }

    public void setTotal_statuses(int total_statuses) {
        this.total_statuses = total_statuses;
    }

    public int getNumber_boosts() {
        return number_boosts;
    }

    public void setNumber_boosts(int number_boosts) {
        this.number_boosts = number_boosts;
    }

    public int getNumber_replies() {
        return number_replies;
    }

    public void setNumber_replies(int number_replies) {
        this.number_replies = number_replies;
    }

    public int getNumber_status() {
        return number_status;
    }

    public void setNumber_status(int number_status) {
        this.number_status = number_status;
    }

    public int getNumber_with_media() {
        return number_with_media;
    }

    public void setNumber_with_media(int number_with_media) {
        this.number_with_media = number_with_media;
    }

    public int getNumber_with_cw() {
        return number_with_cw;
    }

    public void setNumber_with_cw(int number_with_cw) {
        this.number_with_cw = number_with_cw;
    }

    public int getNumber_with_sensitive_media() {
        return number_with_sensitive_media;
    }

    public void setNumber_with_sensitive_media(int number_with_sensitive_media) {
        this.number_with_sensitive_media = number_with_sensitive_media;
    }

    public int getV_public() {
        return v_public;
    }

    public void setV_public(int v_public) {
        this.v_public = v_public;
    }

    public int getV_unlisted() {
        return v_unlisted;
    }

    public void setV_unlisted(int v_unlisted) {
        this.v_unlisted = v_unlisted;
    }

    public int getV_private() {
        return v_private;
    }

    public void setV_private(int v_private) {
        this.v_private = v_private;
    }

    public int getV_direct() {
        return v_direct;
    }

    public void setV_direct(int v_direct) {
        this.v_direct = v_direct;
    }

    private int v_direct;

    public Map<String, Integer> getTagsTrend() {
        return tagsTrend;
    }

    public void setTagsTrend(Map<String, Integer> tagsTrend) {
        this.tagsTrend = tagsTrend;
    }
}
