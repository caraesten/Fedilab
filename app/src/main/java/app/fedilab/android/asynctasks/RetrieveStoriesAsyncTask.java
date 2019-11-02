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
package app.fedilab.android.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import java.lang.ref.WeakReference;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.PixelfedAPI;
import app.fedilab.android.interfaces.OnRetrieveStoriesInterface;


/**
 * Created by Thomas on 02/11/2019.
 * Retrieves stories on the instance
 */

public class RetrieveStoriesAsyncTask extends AsyncTask<Void, Void, Void> {


    private APIResponse apiResponse;
    private String max_id;
    private OnRetrieveStoriesInterface listener;
    private WeakReference<Context> contextReference;
    private type typeOfStory;

    public enum type{
        ME,
        FRIENDS
    }

    public RetrieveStoriesAsyncTask(Context context, String max_id, type typeOfStory, OnRetrieveStoriesInterface onRetrieveStoriesInterface) {
        this.contextReference = new WeakReference<>(context);
        this.max_id = max_id;
        this.listener = onRetrieveStoriesInterface;
        this.typeOfStory = typeOfStory;
    }


    @Override
    protected Void doInBackground(Void... params) {
        PixelfedAPI pixelfedAPI = new PixelfedAPI(this.contextReference.get());
        if( typeOfStory == type.FRIENDS) {
            apiResponse = pixelfedAPI.getFriendStories(max_id);
        }else if (typeOfStory == type.ME){
            apiResponse = pixelfedAPI.getMyStories();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveStories(apiResponse);
    }

}
