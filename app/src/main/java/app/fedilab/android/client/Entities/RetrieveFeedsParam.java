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

import app.fedilab.android.asynctasks.RetrieveFeedsAsyncTask;
import app.fedilab.android.helper.FilterToots;


public class RetrieveFeedsParam {

    private FilterToots filterToots;
    private String max_id;
    private RetrieveFeedsAsyncTask.Type action;
    private String targetedID;
    private String instanceName;
    private String tag;
    private String currentfilter;
    private String remoteInstance;
    private int timelineId;
    private boolean showMediaOnly;
    private boolean showPinned;
    private boolean showReply;
    private String name;
    private String social;

    public FilterToots getFilterToots() {
        return filterToots;
    }

    public void setFilterToots(FilterToots filterToots) {
        this.filterToots = filterToots;
    }

    public String getMax_id() {
        return max_id;
    }

    public void setMax_id(String max_id) {
        this.max_id = max_id;
    }

    public RetrieveFeedsAsyncTask.Type getAction() {
        return action;
    }

    public void setAction(RetrieveFeedsAsyncTask.Type action) {
        this.action = action;
    }

    public String getTargetedID() {
        return targetedID;
    }

    public void setTargetedID(String targetedID) {
        this.targetedID = targetedID;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getTimelineId() {
        return timelineId;
    }

    public void setTimelineId(int timelineId) {
        this.timelineId = timelineId;
    }

    public boolean isShowMediaOnly() {
        return showMediaOnly;
    }

    public void setShowMediaOnly(boolean showMediaOnly) {
        this.showMediaOnly = showMediaOnly;
    }

    public boolean isShowPinned() {
        return showPinned;
    }

    public void setShowPinned(boolean showPinned) {
        this.showPinned = showPinned;
    }

    public boolean isShowReply() {
        return showReply;
    }

    public void setShowReply(boolean showReply) {
        this.showReply = showReply;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCurrentfilter() {
        return currentfilter;
    }

    public void setCurrentfilter(String currentfilter) {
        this.currentfilter = currentfilter;
    }

    public String getRemoteInstance() {
        return remoteInstance;
    }

    public void setRemoteInstance(String remoteInstance) {
        this.remoteInstance = remoteInstance;
    }

    public String getSocial() {
        return social;
    }

    public void setSocial(String social) {
        this.social = social;
    }
}
