package app.fedilab.android.client.Entities;

import java.util.LinkedHashMap;
import java.util.List;

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

public class NotificationCharts {

    private List<String> xLabels;
    private List<String> yLabels;
    private LinkedHashMap<Long, Integer> reblogs;
    private LinkedHashMap<Long, Integer> favourites;
    private LinkedHashMap<Long, Integer> mentions;
    private LinkedHashMap<Long, Integer> follows;
    private LinkedHashMap<Long, Integer> polls;
    private int minYVal = 0;

    public List<String> getxLabels() {
        return xLabels;
    }

    public void setxLabels(List<String> xLabels) {
        this.xLabels = xLabels;
    }

    public List<String> getyLabels() {
        return yLabels;
    }

    public void setyLabels(List<String> yLabels) {
        this.yLabels = yLabels;
    }

    public LinkedHashMap<Long, Integer> getReblogs() {
        return reblogs;
    }

    public void setReblogs(LinkedHashMap<Long, Integer> reblogs) {
        this.reblogs = reblogs;
    }

    public LinkedHashMap<Long, Integer> getFavourites() {
        return favourites;
    }

    public void setFavourites(LinkedHashMap<Long, Integer> favourites) {
        this.favourites = favourites;
    }

    public LinkedHashMap<Long, Integer> getMentions() {
        return mentions;
    }

    public void setMentions(LinkedHashMap<Long, Integer> mentions) {
        this.mentions = mentions;
    }

    public LinkedHashMap<Long, Integer> getFollows() {
        return follows;
    }

    public void setFollows(LinkedHashMap<Long, Integer> follows) {
        this.follows = follows;
    }

    public LinkedHashMap<Long, Integer> getPolls() {
        return polls;
    }

    public void setPolls(LinkedHashMap<Long, Integer> polls) {
        this.polls = polls;
    }

    public int getMinYVal() {
        return minYVal;
    }

    public void setMinYVal(int minYVal) {
        this.minYVal = minYVal;
    }
}
