package app.fedilab.android.fragments;
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

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import app.fedilab.android.client.Entities.StoredStatus;
import app.fedilab.android.drawers.DraftsListAdapter;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.sqlite.Sqlite;
import app.fedilab.android.sqlite.StatusStoredDAO;
import app.fedilab.android.activities.MainActivity;
import app.fedilab.android.R;

import static app.fedilab.android.helper.Helper.changeDrawableColor;


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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_drafts, container, false);
        context = getContext();

        final ListView lv_draft_toots = rootView.findViewById(R.id.lv_draft_toots);

        RelativeLayout mainLoader = rootView.findViewById(R.id.loader);
        textviewNoAction = rootView.findViewById(R.id.no_action);
        mainLoader.setVisibility(View.VISIBLE);
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        final int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if( theme == Helper.THEME_DARK){
            Helper.changeDrawableColor(context, R.drawable.ic_cancel,R.color.dark_text);
        }else {
            Helper.changeDrawableColor(context, R.drawable.ic_cancel,R.color.black);
        }
        final SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        //Removes all scheduled toots that have sent
        new StatusStoredDAO(context, db).removeAllSent();
        drafts = new StatusStoredDAO(context, db).getAllDrafts();
        if( drafts != null && drafts.size() > 0) {
            draftsListAdapter = new DraftsListAdapter(context, drafts, true, textviewNoAction);
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
                    final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                    int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
                    int style;
                    if (theme == Helper.THEME_DARK) {
                        style = R.style.DialogDark;
                    } else if (theme == Helper.THEME_BLACK){
                        style = R.style.DialogBlack;
                    }else {
                        style = R.style.Dialog;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(context, style);
                    builder.setTitle(R.string.delete_all);
                    builder.setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogConfirm, int which) {
                                    new StatusStoredDAO(context, db).removeAllDrafts();
                                    drafts = new ArrayList<>();
                                    drafts.clear();
                                    draftsListAdapter = new DraftsListAdapter(context, drafts, true, textviewNoAction);
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
