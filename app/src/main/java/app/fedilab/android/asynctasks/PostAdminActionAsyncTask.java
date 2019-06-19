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
import app.fedilab.android.client.API;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.AdminAction;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.interfaces.OnAdminActionInterface;



/**
 * Created by Thomas on 18/06/2019.
 * Makes actions for post admin calls
 */

public class PostAdminActionAsyncTask extends AsyncTask<Void, Void, Void> {

    private OnAdminActionInterface listener;
    private API.adminAction action;
    private String id;
    private WeakReference<Context> contextReference;
    private APIResponse apiResponse;
    private AdminAction adminAction;



    public PostAdminActionAsyncTask(Context context, API.adminAction action, String id, AdminAction adminAction, OnAdminActionInterface onAdminActionInterface){
        this.contextReference = new WeakReference<>(context);
        this.listener = onAdminActionInterface;
        this.action = action;
        this.id = id;
        this.adminAction = adminAction;
    }


    @Override
    protected Void doInBackground(Void... params) {
        switch (action){
            case GET_ACCOUNTS:
            case GET_ONE_ACCOUNT:
            case GET_REPORTS:
            case GET_ONE_REPORT:
                apiResponse = new API(contextReference.get()).adminGet(action, id, null);
                break;
            default:
                apiResponse = new API(contextReference.get()).adminDo(action, id, adminAction);
        }


        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onAdminAction(apiResponse);
    }

}
