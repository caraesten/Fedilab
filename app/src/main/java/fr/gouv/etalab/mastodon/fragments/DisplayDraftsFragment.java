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
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import fr.gouv.etalab.mastodon.activities.MainActivity;
import fr.gouv.etalab.mastodon.client.Entities.StoredStatus;
import fr.gouv.etalab.mastodon.drawers.DraftsListAdapter;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import fr.gouv.etalab.mastodon.sqlite.StatusStoredDAO;
import mastodon.etalab.gouv.fr.mastodon.R;

import static fr.gouv.etalab.mastodon.helper.Helper.changeDrawableColor;


/**
 * Created by Thomas on 19/09/2017.
 * Fragment to display drafts toots
 */
public class DisplayDraftsFragment extends Fragment {


    private Context context;
    private List<StoredStatus> drafts;
    private DraftsListAdapter draftsListAdapter;
    private RelativeLayout textviewNoAction;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_drafts, container, false);
        context = getContext();

        final ListView lv_draft_toots = rootView.findViewById(R.id.lv_draft_toots);

        RelativeLayout mainLoader = rootView.findViewById(R.id.loader);
        textviewNoAction = rootView.findViewById(R.id.no_action);
        mainLoader.setVisibility(View.VISIBLE);
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        final int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if( theme == Helper.THEME_DARK){
            changeDrawableColor(context, R.drawable.ic_cancel,R.color.dark_text);
        }else {
            changeDrawableColor(context, R.drawable.ic_cancel,R.color.black);
        }
        final SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        //Removes all scheduled toots that have sent
        new StatusStoredDAO(context, db).removeAllSent();
        drafts = new StatusStoredDAO(context, db).getAllDrafts();
        if( drafts != null && drafts.size() > 0) {
            draftsListAdapter = new DraftsListAdapter(context, drafts, true);
            lv_draft_toots.setAdapter(draftsListAdapter);
            draftsListAdapter.notifyDataSetChanged();
        }else {
            textviewNoAction.setVisibility(View.VISIBLE);
        }
        mainLoader.setVisibility(View.GONE);
        FloatingActionButton delete_all = null;
        try {
            delete_all = ((MainActivity) context).findViewById(R.id.delete_all);
        }catch (Exception ignored){}
        if( delete_all != null)
            delete_all.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(R.string.delete_all);
                    builder.setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogConfirm, int which) {
                                    new StatusStoredDAO(context, db).removeAllDrafts();
                                    drafts = new ArrayList<>();
                                    drafts.clear();
                                    draftsListAdapter = new DraftsListAdapter(context, drafts, true);
                                    lv_draft_toots.setAdapter(draftsListAdapter);
                                    draftsListAdapter.notifyDataSetChanged();
                                    textviewNoAction.setVisibility(View.VISIBLE);
                                    dialogConfirm.dismiss();
                                }
                            })
                            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogConfirm, int which) {
                                    dialogConfirm.dismiss();
                                }
                            })
                            .show();
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
