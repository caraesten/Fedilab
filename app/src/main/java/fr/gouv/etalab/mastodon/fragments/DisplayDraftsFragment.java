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
 * You should have received a copy of the GNU General Public License along with Mastalab; if not,
 * see <http://www.gnu.org/licenses>. */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import java.util.List;
import fr.gouv.etalab.mastodon.client.Entities.StoredStatus;
import fr.gouv.etalab.mastodon.drawers.DraftsListAdapter;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import fr.gouv.etalab.mastodon.sqlite.StatusStoredDAO;
import mastodon.etalab.gouv.fr.mastodon.R;


/**
 * Created by Thomas on 19/09/2017.
 * Fragment to display drafts toots
 */
public class DisplayDraftsFragment extends Fragment {


    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_drafts, container, false);
        context = getContext();

        ListView lv_draft_toots = (ListView) rootView.findViewById(R.id.lv_draft_toots);

        RelativeLayout mainLoader = (RelativeLayout) rootView.findViewById(R.id.loader);
        RelativeLayout textviewNoAction = (RelativeLayout) rootView.findViewById(R.id.no_action);
        mainLoader.setVisibility(View.VISIBLE);

        final SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        //Removes all scheduled toots that have sent
        new StatusStoredDAO(context, db).removeAllSent();
        final List<StoredStatus> drafts = new StatusStoredDAO(context, db).getAllDrafts();
        if( drafts != null && drafts.size() > 0) {
            final DraftsListAdapter draftsListAdapter = new DraftsListAdapter(context, drafts, true);
            lv_draft_toots.setAdapter(draftsListAdapter);
            draftsListAdapter.notifyDataSetChanged();
        }else {
            textviewNoAction.setVisibility(View.VISIBLE);
        }
        mainLoader.setVisibility(View.GONE);

        return rootView;
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


}
