/* Copyright 2017 Thomas Schneider
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

import java.util.HashMap;

/**
 * Created by Thomas on 05/06/2017.
 * Describes instance
 */

public class Instance {

    private String uri;
    private String title;
    private String description;
    private String email;
    private String version;
    private boolean registration;
    private boolean approval_required;

    private HashMap<String, Integer> poll_limits;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public HashMap<String, Integer> getPoll_limits() {
        return poll_limits;
    }

    public void setPoll_limits(HashMap<String, Integer> poll_limits) {
        this.poll_limits = poll_limits;
    }

    public boolean isRegistration() {
        return registration;
    }

    public void setRegistration(boolean registration) {
        this.registration = registration;
    }

    public boolean isApproval_required() {
        return approval_required;
    }

    public void setApproval_required(boolean approval_required) {
        this.approval_required = approval_required;
    }
}
