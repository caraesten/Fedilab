/* Copyright 2019 Thomas Schneider
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
package app.fedilab.android.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import app.fedilab.android.R;
import app.fedilab.android.asynctasks.PostActionAsyncTask;
import app.fedilab.android.asynctasks.RetrieveFeedsAsyncTask;
import app.fedilab.android.asynctasks.SyncBookmarksAsyncTask;
import app.fedilab.android.client.API;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Status;
import app.fedilab.android.drawers.StatusListAdapter;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.interfaces.OnRetrieveFeedsInterface;
import app.fedilab.android.interfaces.OnSyncBookmarksInterface;
import app.fedilab.android.sqlite.AccountDAO;
import app.fedilab.android.sqlite.Sqlite;


/**
 * Created by Thomas on 15/11/2019.
 * Bookmark activity
 */

public class BookmarkActivity extends BaseActivity implements OnRetrieveFeedsInterface, OnSyncBookmarksInterface {

    private List<Status> statuses;
    private StatusListAdapter statusListAdapter;
    private RelativeLayout textviewNoAction;
    private RelativeLayout mainLoader;
    private RecyclerView lv_status;
    private ImageView pp_actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        switch (theme) {
            case Helper.THEME_LIGHT:
                setTheme(R.style.AppTheme_Fedilab);
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

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(BookmarkActivity.this, R.color.cyanea_primary)));
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            View view = inflater.inflate(R.layout.simple_action_bar, new LinearLayout(getApplicationContext()), false);
            actionBar.setCustomView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            TextView title = actionBar.getCustomView().findViewById(R.id.toolbar_title);
            pp_actionBar = actionBar.getCustomView().findViewById(R.id.pp_actionBar);
            title.setText(R.string.bookmarks);
            ImageView close_conversation = actionBar.getCustomView().findViewById(R.id.close_conversation);
            if (close_conversation != null) {
                close_conversation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
            }
        } else {
            setTitle(R.string.bookmarks);
        }
        setContentView(R.layout.activity_bookmark);
        SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = sharedpreferences.getString(Helper.PREF_INSTANCE, null);
        Account account = new AccountDAO(getApplicationContext(), db).getUniqAccount(userId, instance);

        Helper.loadGiF(getApplicationContext(), account.getAvatar(), pp_actionBar);

        lv_status = findViewById(R.id.lv_status);

        mainLoader = findViewById(R.id.loader);
        textviewNoAction = findViewById(R.id.no_action);
        mainLoader.setVisibility(View.VISIBLE);
        new RetrieveFeedsAsyncTask(getApplicationContext(), RetrieveFeedsAsyncTask.Type.CACHE_BOOKMARKS, null, BookmarkActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_export_bookmarks:
                new SyncBookmarksAsyncTask(BookmarkActivity.this, SyncBookmarksAsyncTask.sync.EXPORT, BookmarkActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return true;
            case R.id.action_import_bookmarks:
                new SyncBookmarksAsyncTask(BookmarkActivity.this, SyncBookmarksAsyncTask.sync.IMPORT, BookmarkActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NotNull Menu menu) {
        getMenuInflater().inflate(R.menu.bookmarks, menu);
        return true;
    }


    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onRetrieveFeeds(APIResponse apiResponse) {
        mainLoader.setVisibility(View.GONE);
        FloatingActionButton delete_all = null;
        try {
            delete_all = findViewById(R.id.delete_all);
        } catch (Exception ignored) {
        }
        final boolean isOnWifi = Helper.isOnWIFI(BookmarkActivity.this);
        statuses = apiResponse.getStatuses();
        if (statuses != null && statuses.size() > 0) {
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(BookmarkActivity.this);
            statusListAdapter = new StatusListAdapter(RetrieveFeedsAsyncTask.Type.CACHE_BOOKMARKS, null, isOnWifi, this.statuses);
            lv_status.setAdapter(statusListAdapter);
            lv_status.setLayoutManager(mLayoutManager);
        } else {
            textviewNoAction.setVisibility(View.VISIBLE);
        }

        if (delete_all != null)
            delete_all.setOnClickListener(view -> {
                final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
                int style;
                if (theme == Helper.THEME_DARK) {
                    style = R.style.DialogDark;
                } else if (theme == Helper.THEME_BLACK) {
                    style = R.style.DialogBlack;
                } else {
                    style = R.style.Dialog;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(BookmarkActivity.this, style);
                builder.setTitle(R.string.delete_all);
                builder.setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(R.string.yes, (dialogConfirm, which) -> {
                            statuses = new ArrayList<>();
                            statuses.clear();
                            statusListAdapter = new StatusListAdapter(RetrieveFeedsAsyncTask.Type.CACHE_BOOKMARKS, null, isOnWifi, statuses);
                            lv_status.setAdapter(statusListAdapter);
                            statusListAdapter.notifyDataSetChanged();
                            textviewNoAction.setVisibility(View.VISIBLE);
                            new PostActionAsyncTask(BookmarkActivity.this, API.StatusAction.UNBOOKMARK).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            dialogConfirm.dismiss();
                        })
                        .setNegativeButton(R.string.no, (dialogConfirm, which) -> dialogConfirm.dismiss())
                        .show();
            });
    }

    @Override
    public void onRetrieveBookmarks(List<Status> bookmarks) {
        if( bookmarks != null) {
            statuses = new ArrayList<>();
            statuses.clear();
            statuses.addAll(bookmarks);
            final boolean isOnWifi = Helper.isOnWIFI(BookmarkActivity.this);
            statusListAdapter = new StatusListAdapter(RetrieveFeedsAsyncTask.Type.CACHE_BOOKMARKS, null, isOnWifi, statuses);
            lv_status.setAdapter(statusListAdapter);
            statusListAdapter.notifyDataSetChanged();
            if( statuses.size() == 0 ) {
                textviewNoAction.setVisibility(View.VISIBLE);
            }
        }
    }
}
