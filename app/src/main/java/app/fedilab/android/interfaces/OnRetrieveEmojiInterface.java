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
package app.fedilab.android.interfaces;

import java.util.List;

import app.fedilab.android.client.Entities.Emojis;
import app.fedilab.android.client.Entities.Notification;
import app.fedilab.android.client.Entities.Status;


/**
 * Created by Thomas on 19/10/2017.
 * Interface when retrieving emojis
 */
public interface OnRetrieveEmojiInterface {
    void onRetrieveEmoji(Status status, boolean fromTranslation);
    void onRetrieveEmoji(Notification notification);
    void onRetrieveSearchEmoji(List<Emojis> emojis);
}
