/* Copyright 2017 Thomas Schneider
 *
 * This file is a part of Mastodon Etalab for mastodon.etalab.gouv.fr
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Mastodon Etalab is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Thomas Schneider; if not,
 * see <http://www.gnu.org/licenses>. */
package fr.gouv.etalab.mastodon.client.Entities;

import java.util.List;

/**
 * Created by Thomas on 23/04/2017.
 * Manage status Context
 */

public class Context {

    private List<Status> ancestors;
    private List<Status> descendants;

    public List<Status> getAncestors() {
        return ancestors;
    }

    public void setAncestors(List<Status> ancestors) {
        this.ancestors = ancestors;
    }

    public List<Status> getDescendants() {
        return descendants;
    }

    public void setDescendants(List<Status> descendants) {
        this.descendants = descendants;
    }
}
