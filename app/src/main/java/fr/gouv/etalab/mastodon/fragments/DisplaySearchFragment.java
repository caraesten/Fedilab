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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import fr.gouv.etalab.mastodon.activities.MainActivity;
import fr.gouv.etalab.mastodon.drawers.SearchTootsListAdapter;
import fr.gouv.etalab.mastodon.sqlite.SearchDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import fr.gouv.etalab.mastodon.R;


/**
 * Created by Thomas on 22/11/2017.
 * Fragment to display search with keywords
 */
public class DisplaySearchFragment extends Fragment {


    private Context context;
    private SearchTootsListAdapter searchTootsListAdapter;
    private List<String> searches;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        context = getContext();

        final ListView lv_search_toots = rootView.findViewById(R.id.lv_search_toots);

        RelativeLayout mainLoader = rootView.findViewById(R.id.loader);
        final RelativeLayout textviewNoAction = rootView.findViewById(R.id.no_action);
        mainLoader.setVisibility(View.VISIBLE);
        final SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        searches = new SearchDAO(context, db).getAllSearch();
        if( searches == null)
            searches = new ArrayList<>();
        searchTootsListAdapter = new SearchTootsListAdapter(context, searches, textviewNoAction);
        lv_search_toots.setAdapter(searchTootsListAdapter);
        searchTootsListAdapter.notifyDataSetChanged();
        if( searches.size() == 0) {
            textviewNoAction.setVisibility(View.VISIBLE);
        }
        mainLoader.setVisibility(View.GONE);
        FloatingActionButton add_new = null;
        try {
            add_new = ((MainActivity) context).findViewById(R.id.add_new);
        }catch (Exception ignored){}
        if( add_new != null)
            add_new.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                    LayoutInflater inflater = getLayoutInflater();
                    @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.search_toot, null);
                    dialogBuilder.setView(dialogView);
                    final EditText editText = dialogView.findViewById(R.id.search_toot);
                    dialogBuilder.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            String keyword = editText.getText().toString().trim();
                            //Empty
                            if( keyword.length() == 0)
                                return;
                            //Already in db
                            List<String> s_ = new SearchDAO(context, db).getSearchByKeyword(keyword);
                            if( s_ == null)
                                s_ = new ArrayList<>();
                            if( s_.size() > 0){
                                return;
                            }
                            new SearchDAO(context, db).insertSearch(keyword);
                            searches.add(keyword);
                            if( textviewNoAction.getVisibility() == View.VISIBLE)
                                textviewNoAction.setVisibility(View.GONE);
                            searchTootsListAdapter.notifyDataSetChanged();
                        }
                    });
                    AlertDialog alertDialog = dialogBuilder.create();
                    alertDialog.show();
                }
            });
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
