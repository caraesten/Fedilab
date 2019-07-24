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


import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;


import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


import app.fedilab.android.R;
import app.fedilab.android.asynctasks.RetrieveFeedsAsyncTask;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Status;
import app.fedilab.android.drawers.StatusListAdapter;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.interfaces.OnRetrieveFeedsInterface;
import es.dmoral.toasty.Toasty;

import static app.fedilab.android.helper.Helper.THEME_BLACK;
import static app.fedilab.android.helper.Helper.THEME_LIGHT;

/**
 * Created by Thomas on 22/02/2019.
 * Show group timeline
 */

public class GroupActivity extends BaseActivity implements OnRetrieveFeedsInterface {


    public static int position;
    private StatusListAdapter statusListAdapter;
    private String max_id;
    private List<Status> statuses;
    private RelativeLayout mainLoader, nextElementLoader, textviewNoAction;
    private boolean firstLoad;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String groupname;
    private int tootsPerPage;
    private boolean flag_loading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        switch (theme){
            case THEME_LIGHT:
                setTheme(R.style.AppTheme_NoActionBar);
                break;
            case Helper.THEME_DARK:
                setTheme(R.style.AppThemeDark_NoActionBar);
                break;
            case THEME_BLACK:
                setTheme(R.style.AppThemeBlack_NoActionBar);
                break;
            default:
                setTheme(R.style.AppThemeDark_NoActionBar);
        }

        setContentView(R.layout.activity_group);
        Toolbar toolbar = findViewById(R.id.toolbar);
        if( theme == THEME_BLACK)
            toolbar.setBackgroundColor(ContextCompat.getColor(GroupActivity.this, R.color.black));
        setSupportActionBar(toolbar);

        if( getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle b = getIntent().getExtras();
        if(b != null)
            groupname = b.getString("groupname", null);
        if( groupname == null)
            finish();
        statuses = new ArrayList<>();
        max_id = null;
        flag_loading = true;
        firstLoad = true;
        boolean isOnWifi = Helper.isOnWIFI(getApplicationContext());
        swipeRefreshLayout = findViewById(R.id.swipeContainer);


        final RecyclerView lv_status = findViewById(R.id.lv_status);
        //lv_status.addItemDecoration(new DividerItemDecoration(GroupActivity.this, DividerItemDecoration.VERTICAL));
        tootsPerPage = Helper.TOOTS_PER_PAGE;
        mainLoader = findViewById(R.id.loader);
        nextElementLoader = findViewById(R.id.loading_next_status);
        textviewNoAction = findViewById(R.id.no_action);
        mainLoader.setVisibility(View.VISIBLE);
        nextElementLoader.setVisibility(View.GONE);
        statusListAdapter = new StatusListAdapter(GroupActivity.this, RetrieveFeedsAsyncTask.Type.GNU_GROUP_TIMELINE, null, isOnWifi, this.statuses);
        lv_status.setAdapter(statusListAdapter);
        setTitle(String.format("!%s", groupname));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                max_id = null;
                statuses = new ArrayList<>();
                firstLoad = true;
                flag_loading = true;
                new RetrieveFeedsAsyncTask(getApplicationContext(), RetrieveFeedsAsyncTask.Type.GNU_GROUP_TIMELINE, groupname,null, max_id, GroupActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
        final LinearLayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(this);
        lv_status.setLayoutManager(mLayoutManager);
        lv_status.addOnScrollListener(new RecyclerView.OnScrollListener() {
            public void onScrolled(@NotNull RecyclerView recyclerView, int dx, int dy)
            {
                if(dy > 0){
                    int visibleItemCount = mLayoutManager.getChildCount();
                    int totalItemCount = mLayoutManager.getItemCount();
                    int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                    if(firstVisibleItem + visibleItemCount == totalItemCount ) {
                        if(!flag_loading ) {
                            flag_loading = true;
                            new RetrieveFeedsAsyncTask(getApplicationContext(), RetrieveFeedsAsyncTask.Type.GNU_GROUP_TIMELINE, groupname,null, max_id, GroupActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                            nextElementLoader.setVisibility(View.VISIBLE);
                        }
                    } else {
                        nextElementLoader.setVisibility(View.GONE);
                    }
                }
            }
        });
        new RetrieveFeedsAsyncTask(getApplicationContext(), RetrieveFeedsAsyncTask.Type.GNU_GROUP_TIMELINE, groupname,null, max_id, GroupActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if( theme == THEME_LIGHT)
            Helper.colorizeIconMenu(menu, R.color.black);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRetrieveFeeds(APIResponse apiResponse) {

        mainLoader.setVisibility(View.GONE);
        nextElementLoader.setVisibility(View.GONE);
        if( apiResponse.getError() != null){
            Toasty.error(getApplicationContext(), apiResponse.getError().getError(),Toast.LENGTH_LONG).show();
            return;
        }
        List<Status> statuses = apiResponse.getStatuses();
        if( firstLoad && (statuses == null || statuses.size() == 0))
            textviewNoAction.setVisibility(View.VISIBLE);
        else
            textviewNoAction.setVisibility(View.GONE);
        if( statuses != null && statuses.size() > 1)
            max_id =statuses.get(statuses.size()-1).getId();
        else
            max_id = null;
        if( statuses != null) {
            this.statuses.addAll(statuses);
            statusListAdapter.notifyDataSetChanged();
        }
        swipeRefreshLayout.setRefreshing(false);
        firstLoad = false;
        flag_loading = statuses != null && statuses.size() < tootsPerPage;
    }

}
