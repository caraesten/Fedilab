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
package fr.gouv.etalab.mastodon.asynctasks;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;
import java.util.List;

import fr.gouv.etalab.mastodon.client.Entities.Emojis;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveEmojiInterface;
import fr.gouv.etalab.mastodon.sqlite.CustomEmojiDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;


/**
 * Created by Thomas on 01/11/2017.
 * Retrieves emojis
 */

public class RetrieveEmojiAsyncTask extends AsyncTask<Void, Void, Void> {

    private String shortcode;
    private List<Emojis> emojis;
    private OnRetrieveEmojiInterface listener;
    private WeakReference<Context> contextReference;

    public RetrieveEmojiAsyncTask(Context context, String shortcode, OnRetrieveEmojiInterface onRetrieveEmojiInterface){
        this.contextReference = new WeakReference<>(context);
        this.shortcode = shortcode;
        this.listener = onRetrieveEmojiInterface;
    }


    @Override
    protected Void doInBackground(Void... params) {
        SQLiteDatabase db = Sqlite.getInstance(contextReference.get(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        emojis = new CustomEmojiDAO(contextReference.get(), db).getEmojiStartingBy(shortcode);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveSearchEmoji(emojis);
    }

}
