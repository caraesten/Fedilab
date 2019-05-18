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
import android.os.AsyncTask;
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

import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Status;
import app.fedilab.android.drawers.StatusListAdapter;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.sqlite.Sqlite;
import app.fedilab.android.sqlite.StatusCacheDAO;
import app.fedilab.android.R;
import app.fedilab.android.activities.MainActivity;
import app.fedilab.android.asynctasks.RetrieveFeedsAsyncTask;
import app.fedilab.android.interfaces.OnRetrieveFeedsInterface;


/**
 * Created by Thomas on 15/02/2018.
 * Fragment to display bookmarks
 */
public class DisplayBookmarksFragment extends Fragment implements OnRetrieveFeedsInterface {


    private Context context;
    private List<Status> statuses;
    private StatusListAdapter statusListAdapter;
    private RelativeLayout textviewNoAction;
    private RelativeLayout mainLoader;
    private RecyclerView lv_status;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_bookmarks, container, false);
        context = getContext();

         lv_status = rootView.findViewById(R.id.lv_status);

        mainLoader = rootView.findViewById(R.id.loader);
        textviewNoAction = rootView.findViewById(R.id.no_action);
        mainLoader.setVisibility(View.VISIBLE);
        new RetrieveFeedsAsyncTask(context, RetrieveFeedsAsyncTask.Type.CACHE_BOOKMARKS, null, DisplayBookmarksFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

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


    @Override
    public void onRetrieveFeeds(APIResponse apiResponse) {

        final SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        mainLoader.setVisibility(View.GONE);
        FloatingActionButton delete_all = null;
        try {
            delete_all = ((MainActivity) context).findViewById(R.id.delete_all);
        }catch (Exception ignored){}
        final boolean isOnWifi = Helper.isOnWIFI(context);
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        statuses = apiResponse.getStatuses();
        if( statuses != null && statuses.size() > 0) {
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(context);
            statusListAdapter = new StatusListAdapter(context, RetrieveFeedsAsyncTask.Type.CACHE_BOOKMARKS, null, isOnWifi, this.statuses);
            lv_status.setAdapter(statusListAdapter);
            lv_status.setLayoutManager(mLayoutManager);
        }else {
            textviewNoAction.setVisibility(View.VISIBLE);
        }

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
                                    new StatusCacheDAO(context, db).removeAllStatus(StatusCacheDAO.BOOKMARK_CACHE);
                                    new StatusCacheDAO(context, db).removeAllStatus(StatusCacheDAO.STATUS_CACHE);
                                    statuses = new ArrayList<>();
                                    statuses.clear();
                                    statusListAdapter = new StatusListAdapter(context, RetrieveFeedsAsyncTask.Type.CACHE_BOOKMARKS, null, isOnWifi, statuses);
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
    }
}
