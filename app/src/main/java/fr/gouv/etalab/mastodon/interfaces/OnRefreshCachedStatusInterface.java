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
package fr.gouv.etalab.mastodon.interfaces;

import fr.gouv.etalab.mastodon.client.Entities.Status;

/**
 * Created by Thomas on 12/05/2019.
 * Interface when refreshing status cache
 */
public interface OnRefreshCachedStatusInterface {
    void onRefresh(Status refreshedStatus);
}
