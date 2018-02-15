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
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.activities.MainActivity;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveFeedsAsyncTask;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.drawers.StatusListAdapter;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import fr.gouv.etalab.mastodon.sqlite.StatusCacheDAO;


/**
 * Created by Thomas on 15/02/2018.
 * Fragment to display bookmarks
 */
public class DisplayBookmarksFragment extends Fragment {


    private Context context;
    private List<Status> statuses;
    private StatusListAdapter statusListAdapter;
    private RelativeLayout textviewNoAction;
    private LinearLayoutManager mLayoutManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_bookmarks, container, false);
        context = getContext();

        final RecyclerView lv_status = rootView.findViewById(R.id.lv_status);

        RelativeLayout mainLoader = rootView.findViewById(R.id.loader);
        textviewNoAction = rootView.findViewById(R.id.no_action);
        mainLoader.setVisibility(View.VISIBLE);
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        final SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();

        statuses = new StatusCacheDAO(context, db).getAllStatus(StatusCacheDAO.BOOKMARK_CACHE);
        final boolean isOnWifi = Helper.isOnWIFI(context);
        final int behaviorWithAttachments = sharedpreferences.getInt(Helper.SET_ATTACHMENT_ACTION, Helper.ATTACHMENT_ALWAYS);
        final int positionSpinnerTrans = sharedpreferences.getInt(Helper.SET_TRANSLATOR, Helper.TRANS_YANDEX);
        mLayoutManager = new LinearLayoutManager(context);
        if( statuses != null && statuses.size() > 0) {
            statusListAdapter = new StatusListAdapter(context, RetrieveFeedsAsyncTask.Type.CACHE_BOOKMARKS, null, isOnWifi, behaviorWithAttachments, positionSpinnerTrans, this.statuses);
            lv_status.setAdapter(statusListAdapter);
            lv_status.setLayoutManager(mLayoutManager);
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
                                    new StatusCacheDAO(context, db).removeAllStatus(StatusCacheDAO.BOOKMARK_CACHE);
                                    statuses = new ArrayList<>();
                                    statuses.clear();
                                    statusListAdapter = new StatusListAdapter(context, RetrieveFeedsAsyncTask.Type.CACHE_BOOKMARKS, null, isOnWifi, behaviorWithAttachments, positionSpinnerTrans, statuses);
                                    lv_status.setAdapter(statusListAdapter);
                                    statusListAdapter.notifyDataSetChanged();
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
