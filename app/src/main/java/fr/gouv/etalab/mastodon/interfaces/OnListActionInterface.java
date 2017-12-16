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
package fr.gouv.etalab.mastodon.interfaces;


import fr.gouv.etalab.mastodon.asynctasks.ManageListsAsyncTask;
import fr.gouv.etalab.mastodon.client.APIResponse;

/**
 * Created by Thomas on 13/12/2017.
 * Interface when actions have been done with lists
 */
public interface OnListActionInterface {
    void onActionDone(ManageListsAsyncTask.action actionType, APIResponse apiResponse, int statusCode);
}
