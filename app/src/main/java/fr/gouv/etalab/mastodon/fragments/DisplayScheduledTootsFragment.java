package fr.gouv.etalab.mastodon.fragments;
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
 * You should have received a copy of the GNU General Public License along with Thomas Schneider; if not,
 * see <http://www.gnu.org/licenses>. */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import java.util.List;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveScheduledTootsAsyncTask;
import fr.gouv.etalab.mastodon.client.Entities.StoredStatus;
import fr.gouv.etalab.mastodon.drawers.ScheduledTootsListAdapter;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveScheduledTootsInterface;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import fr.gouv.etalab.mastodon.sqlite.StatusStoredDAO;
import mastodon.etalab.gouv.fr.mastodon.R;


/**
 * Created by Thomas on 16/07/2017.
 * Fragment to display scheduled toots
 */
public class DisplayScheduledTootsFragment extends Fragment implements OnRetrieveScheduledTootsInterface {


    private Context context;
    private AsyncTask<Void, Void, Void> asyncTask;
    private RelativeLayout mainLoader, textviewNoAction;
    private ListView lv_scheduled_toots;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_scheduled_toots, container, false);
        context = getContext();

        lv_scheduled_toots = (ListView) rootView.findViewById(R.id.lv_scheduled_toots);

        mainLoader = (RelativeLayout) rootView.findViewById(R.id.loader);
        textviewNoAction = (RelativeLayout) rootView.findViewById(R.id.no_action);
        mainLoader.setVisibility(View.VISIBLE);

        //Removes all scheduled toots that have sent
        SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        new StatusStoredDAO(context, db).removeAllSent();
        return rootView;
    }


    @Override
    public void onResume(){
        super.onResume();
        //Retrieves scheduled toots
        asyncTask = new RetrieveScheduledTootsAsyncTask(context, DisplayScheduledTootsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onCreate(Bundle saveInstance)
    {
        super.onCreate(saveInstance);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public void onDestroy() {
        super.onDestroy();
        if(asyncTask != null && asyncTask.getStatus() == AsyncTask.Status.RUNNING)
            asyncTask.cancel(true);
    }



    @Override
    public void onRetrieveScheduledToots(List<StoredStatus> storedStatuses) {

        mainLoader.setVisibility(View.GONE);
        if( storedStatuses != null && storedStatuses.size() > 0 ){
            ScheduledTootsListAdapter scheduledTootsListAdapter = new ScheduledTootsListAdapter(context, storedStatuses);
            lv_scheduled_toots.setAdapter(scheduledTootsListAdapter);
        }else {
            textviewNoAction.setVisibility(View.VISIBLE);
        }
    }
}
