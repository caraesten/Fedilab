/* Copyright 2018 Thomas Schneider
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

import java.util.List;

/**
 * Created by Thomas on 15/12/2018.
 * Manage Tags timeline settings
 */

public class TagTimeline {

    private String name;
    private String displayname;
    private boolean isART;
    private boolean isNSFW;
    private List<String> any;
    private List<String> all;
    private List<String> none;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isART() {
        return isART;
    }

    public void setART(boolean ART) {
        isART = ART;
    }

    public boolean isNSFW() {
        return isNSFW;
    }

    public void setNSFW(boolean NSFW) {
        isNSFW = NSFW;
    }

    public List<String> getAny() {
        return any;
    }

    public void setAny(List<String> any) {
        this.any = any;
    }

    public List<String> getAll() {
        return all;
    }

    public void setAll(List<String> all) {
        this.all = all;
    }

    public List<String> getNone() {
        return none;
    }

    public void setNone(List<String> none) {
        this.none = none;
    }

    public String getDisplayname() {
        return displayname;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }
}
