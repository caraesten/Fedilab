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

public class InstanceNodeInfo {

    private String name;
    private String title;
    private String version;
    private boolean openRegistrations;
    private boolean connectionError;
    private int numberOfUsers = 0;
    private int numberOfPosts = 0;
    private int numberOfInstance = 0;
    private String staffAccountStr;
    private Account staffAccount;
    private String nodeName;
    private String nodeDescription;
    private String thumbnail;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isOpenRegistrations() {
        return openRegistrations;
    }

    public void setOpenRegistrations(boolean openRegistrations) {
        this.openRegistrations = openRegistrations;
    }

    public boolean isConnectionError() {
        return connectionError;
    }

    public void setConnectionError(boolean connectionError) {
        this.connectionError = connectionError;
    }

    public int getNumberOfUsers() {
        return numberOfUsers;
    }

    public void setNumberOfUsers(int numberOfUsers) {
        this.numberOfUsers = numberOfUsers;
    }

    public int getNumberOfPosts() {
        return numberOfPosts;
    }

    public void setNumberOfPosts(int numberOfPosts) {
        this.numberOfPosts = numberOfPosts;
    }

    public String getStaffAccountStr() {
        return staffAccountStr;
    }

    public void setStaffAccountStr(String staffAccountStr) {
        this.staffAccountStr = staffAccountStr;
    }

    public Account getStaffAccount() {
        return staffAccount;
    }

    public void setStaffAccount(Account staffAccount) {
        this.staffAccount = staffAccount;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeDescription() {
        return nodeDescription;
    }

    public void setNodeDescription(String nodeDescription) {
        this.nodeDescription = nodeDescription;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public int getNumberOfInstance() {
        return numberOfInstance;
    }

    public void setNumberOfInstance(int numberOfInstance) {
        this.numberOfInstance = numberOfInstance;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
