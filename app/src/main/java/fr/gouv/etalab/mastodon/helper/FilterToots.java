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
package fr.gouv.etalab.mastodon.helper;

/**
 * Created by Thomas on 17/02/2018.
 * Helper class for filtering toots
 */

public class FilterToots {

    public enum typeFilter{
        NONE,
        ONLY,
        BOTH
    }

    private typeFilter pinned = typeFilter.BOTH;
    private typeFilter boosts = typeFilter.BOTH;
    private typeFilter replies = typeFilter.BOTH;
    private typeFilter media = typeFilter.BOTH;

    private boolean v_public = true;
    private boolean v_unlisted = true;
    private boolean v_direct = true;
    private boolean v_private = true;

    private String filter = null;


    private String dateIni = null;
    private String dateEnd = null;

    public typeFilter getPinned() {
        return pinned;
    }

    public void setPinned(typeFilter pinned) {
        this.pinned = pinned;
    }

    public typeFilter getBoosts() {
        return boosts;
    }

    public void setBoosts(typeFilter boosts) {
        this.boosts = boosts;
    }

    public typeFilter getReplies() {
        return replies;
    }

    public void setReplies(typeFilter replies) {
        this.replies = replies;
    }

    public typeFilter getMedia() {
        return media;
    }

    public void setMedia(typeFilter media) {
        this.media = media;
    }

    public boolean isV_public() {
        return v_public;
    }

    public void setV_public(boolean v_public) {
        this.v_public = v_public;
    }

    public boolean isV_unlisted() {
        return v_unlisted;
    }

    public void setV_unlisted(boolean v_unlisted) {
        this.v_unlisted = v_unlisted;
    }

    public boolean isV_direct() {
        return v_direct;
    }

    public void setV_direct(boolean v_direct) {
        this.v_direct = v_direct;
    }

    public boolean isV_private() {
        return v_private;
    }

    public void setV_private(boolean v_private) {
        this.v_private = v_private;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getDateIni() {
        return dateIni;
    }

    public void setDateIni(String dateIni) {
        this.dateIni = dateIni;
    }

    public String getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(String dateEnd) {
        this.dateEnd = dateEnd;
    }
}
