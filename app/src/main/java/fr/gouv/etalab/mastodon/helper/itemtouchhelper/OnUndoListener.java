package fr.gouv.etalab.mastodon.helper.itemtouchhelper;

/* Copyright 2019 Thomas Schneider
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


import fr.gouv.etalab.mastodon.client.Entities.ManageTimelines;

/**
 * Listener for manual initiation of a undo.
 */
public interface OnUndoListener {

    /**
     * Called when an item is removed
     *
     * @param timelines The timeline ManageTimelines
     * @param position The position of the item in tablayout
     */
    void onUndo(ManageTimelines timelines, int position);

}
