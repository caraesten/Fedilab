package fr.gouv.etalab.mastodon.fragments;
/* Copyright 2019 Thomas Schneider
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
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.activities.BaseMainActivity;
import fr.gouv.etalab.mastodon.asynctasks.SyncTimelinesAsyncTask;
import fr.gouv.etalab.mastodon.client.Entities.ManageTimelines;
import fr.gouv.etalab.mastodon.drawers.ReorderTabAdapter;
import fr.gouv.etalab.mastodon.helper.itemtouchhelper.OnStartDragListener;
import fr.gouv.etalab.mastodon.helper.itemtouchhelper.OnUndoListener;
import fr.gouv.etalab.mastodon.helper.itemtouchhelper.SimpleItemTouchHelperCallback;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import fr.gouv.etalab.mastodon.sqlite.TimelinesDAO;


/**
 * Created by Thomas on 31/03/2019.
 * Fragment to display tags
 */
public class DisplayReorderTabFragment extends Fragment implements OnStartDragListener, OnUndoListener {


    private Context context;
    public static boolean updated;
    private ItemTouchHelper touchHelper;
    private RelativeLayout undo_container;
    private TextView undo_action;
    private  List<ManageTimelines> timelines;
    private ReorderTabAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_reorder_tabs, container, false);
        context = getContext();
        updated = false;
        RecyclerView lv_reorder_tabs = rootView.findViewById(R.id.lv_reorder_tabs);

        SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        timelines = new TimelinesDAO(context, db).getAllTimelines();
        adapter = new ReorderTabAdapter(context, timelines, DisplayReorderTabFragment.this, DisplayReorderTabFragment.this);

        ItemTouchHelper.Callback callback =
                new SimpleItemTouchHelperCallback(adapter);
        touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(lv_reorder_tabs);
        undo_action = rootView.findViewById(R.id.undo_action);
        undo_container = rootView.findViewById(R.id.undo_container);
        lv_reorder_tabs.setAdapter(adapter);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(context);
        lv_reorder_tabs.setLayoutManager(mLayoutManager);
        return rootView;
    }

    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }


    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        touchHelper.startDrag(viewHolder);
    }


    @Override
    public void onUndo(ManageTimelines manageTimelines, int position) {
        undo_container.setVisibility(View.VISIBLE);
        undo_action.setPaintFlags(undo_action.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        undo_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timelines.add(position, manageTimelines);
                adapter.notifyItemInserted(position);
                undo_container.setVisibility(View.GONE);
            }
        });
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                undo_container.setVisibility(View.GONE);
            }
        }, 2000);

    }
}
