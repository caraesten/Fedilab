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
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Filters;
import fr.gouv.etalab.mastodon.interfaces.OnFilterActionInterface;


/**
 * Created by Thomas on 05/09/2018.
 * Async works to manage Filters
 */

public class ManageFiltersAsyncTask extends AsyncTask<Void, Void, Void> {

    public enum action{
        GET_FILTER,
        GET_ALL_FILTER,
        CREATE_FILTER,
        DELETE_FILTER,
        UPDATE_FILTER,
    }

    private OnFilterActionInterface listener;
    private APIResponse apiResponse;
    private int statusCode;
    private action apiAction;
    private WeakReference<Context> contextReference;
    private Filters filter;

    public ManageFiltersAsyncTask(Context context, action apiAction, Filters filter, OnFilterActionInterface onFilterActionInterface){
        contextReference = new WeakReference<>(context);
        this.listener = onFilterActionInterface;
        this.filter = filter;
        this.apiAction = apiAction;
    }


    @Override
    protected Void doInBackground(Void... params) {
        if (apiAction == action.GET_ALL_FILTER) {
            apiResponse = new API(contextReference.get()).getFilters();
        }else if(apiAction == action.GET_FILTER){
            apiResponse = new API(contextReference.get()).getFilters(filter.getId());
        }else if(apiAction == action.CREATE_FILTER){
            apiResponse = new API(contextReference.get()).addFilters(filter);
        }else if( apiAction == action.UPDATE_FILTER){
            apiResponse = new API(contextReference.get()).updateFilters(filter);
        }else if(apiAction == action.DELETE_FILTER){
            statusCode = new API(contextReference.get()).deleteFilters(filter);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onActionDone(this.apiAction, apiResponse, statusCode);
    }

}
