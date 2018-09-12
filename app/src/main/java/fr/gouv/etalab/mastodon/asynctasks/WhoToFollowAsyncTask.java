/* Copyright 2018 Thomas Schneider
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
import java.util.ArrayList;
import java.util.List;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.Entities.TrunkAccount;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveWhoToFollowInterface;


/**
 * Created by Thomas on 10/09/2018.
 * Retrieves who to follow list
 */

public class WhoToFollowAsyncTask extends AsyncTask<Void, Void, Void> {

    private String name;
    private List<String> response;
    private OnRetrieveWhoToFollowInterface listener;
    private WeakReference<Context> contextReference;

    public WhoToFollowAsyncTask(Context context, String name, OnRetrieveWhoToFollowInterface onRetrieveWhoToFollowInterface){
        this.contextReference = new WeakReference<>(context);
        this.name = name;
        this.listener = onRetrieveWhoToFollowInterface;
    }


    @Override
    protected Void doInBackground(Void... params) {
        API api = new API(this.contextReference.get());
        if( name != null)
            response = api.getCommunitywikiList(name);
        else
            response = api.getCommunitywikiList();
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if( name == null)
            listener.onRetrieveWhoToFollowList(response);
        else {
            List<TrunkAccount> trunkAccounts = null;
            if(response != null) {
                trunkAccounts = new ArrayList<>();
                for (String res : response) {
                    TrunkAccount trunkAccount = new TrunkAccount();
                    trunkAccount.setAcct(res);
                    trunkAccounts.add(trunkAccount);
                }
            }
            listener.onRetrieveWhoToFollowAccount(trunkAccounts);
        }

    }

}
