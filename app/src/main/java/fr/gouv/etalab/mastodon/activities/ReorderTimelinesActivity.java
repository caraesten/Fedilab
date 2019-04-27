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
package fr.gouv.etalab.mastodon.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.List;
import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.client.Entities.ManageTimelines;
import fr.gouv.etalab.mastodon.drawers.ReorderTabAdapter;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.helper.itemtouchhelper.OnStartDragListener;
import fr.gouv.etalab.mastodon.helper.itemtouchhelper.OnUndoListener;
import fr.gouv.etalab.mastodon.helper.itemtouchhelper.SimpleItemTouchHelperCallback;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import fr.gouv.etalab.mastodon.sqlite.TimelinesDAO;

import static fr.gouv.etalab.mastodon.helper.Helper.INTENT_ACTION;
import static fr.gouv.etalab.mastodon.helper.Helper.REFRESH_TIMELINE;
import static fr.gouv.etalab.mastodon.helper.Helper.THEME_LIGHT;


/**
 * Created by Thomas on 26/04/2019.
 * Reorder timelines activity
 */

public class ReorderTimelinesActivity extends BaseActivity implements OnStartDragListener, OnUndoListener {

    public static boolean updated;
    private ItemTouchHelper touchHelper;
    private RelativeLayout undo_container;
    private TextView undo_action;
    private  List<ManageTimelines> timelines;
    private ReorderTabAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        switch (theme){
            case Helper.THEME_LIGHT:
                setTheme(R.style.AppTheme);
                break;
            case Helper.THEME_DARK:
                setTheme(R.style.AppThemeDark);
                break;
            case Helper.THEME_BLACK:
                setTheme(R.style.AppThemeBlack);
                break;
            default:
                setTheme(R.style.AppThemeDark);
        }

        if( getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null ) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.simple_bar, null);
            actionBar.setCustomView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            ImageView toolbar_close = actionBar.getCustomView().findViewById(R.id.toolbar_close);
            TextView toolbar_title = actionBar.getCustomView().findViewById(R.id.toolbar_title);
            toolbar_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            toolbar_title.setText(R.string.action_reorder_timeline);
            if (theme == THEME_LIGHT){
                Toolbar toolbar = actionBar.getCustomView().findViewById(R.id.toolbar);
                Helper.colorizeToolbar(toolbar, R.color.black, ReorderTimelinesActivity.this);
            }
        }
        setContentView(R.layout.activity_reorder_tabs);



        updated = false;
        RecyclerView lv_reorder_tabs = findViewById(R.id.lv_reorder_tabs);

        SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        timelines = new TimelinesDAO(getApplicationContext(), db).getAllTimelines();
        adapter = new ReorderTabAdapter(getApplicationContext(), timelines, ReorderTimelinesActivity.this, ReorderTimelinesActivity.this);

        ItemTouchHelper.Callback callback =
                new SimpleItemTouchHelperCallback(adapter);
        touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(lv_reorder_tabs);
        undo_action = findViewById(R.id.undo_action);
        undo_container = findViewById(R.id.undo_container);
        lv_reorder_tabs.setAdapter(adapter);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        lv_reorder_tabs.setLayoutManager(mLayoutManager);
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

    @Override
    public void onStop(){
        super.onStop();
        if( updated ) {
            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            intent.putExtra(INTENT_ACTION, REFRESH_TIMELINE);
            startActivity(intent);
            updated = false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
