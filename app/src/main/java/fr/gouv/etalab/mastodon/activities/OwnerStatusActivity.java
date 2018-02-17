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
import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveFeedsAsyncTask;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.drawers.StatusListAdapter;
import fr.gouv.etalab.mastodon.helper.FilterToots;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveFeedsInterface;
import fr.gouv.etalab.mastodon.services.BackupStatusInDataBaseService;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import fr.gouv.etalab.mastodon.sqlite.StatusCacheDAO;


/**
 * Created by Thomas on 17/02/2018.
 * Show owner's toots
 */

public class OwnerStatusActivity extends BaseActivity implements OnRetrieveFeedsInterface {


    private ImageView pp_actionBar;
    private StatusListAdapter statusListAdapter;
    private SharedPreferences sharedpreferences;
    private String max_id;
    private List<Status> statuses;
    private RelativeLayout mainLoader, nextElementLoader, textviewNoAction;
    private boolean firstLoad;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean swiped;
    private boolean flag_loading;
    LinearLayoutManager mLayoutManager;
    private int style;
    private Button settings_time_from, settings_time_to;
    private FilterToots filterToots;
    private Date dateIni, dateEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if( theme == Helper.THEME_LIGHT){
            setTheme(R.style.AppTheme_NoActionBar);
        }else {
            setTheme(R.style.AppThemeDark_NoActionBar);
        }
        setContentView(R.layout.activity_ower_status);

        filterToots = new FilterToots();

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(backupFinishedReceiver,
                        new IntentFilter(Helper.INTENT_BACKUP_FINISH));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null ){
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.toot_action_bar, null);
            actionBar.setCustomView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

            ImageView close_toot = actionBar.getCustomView().findViewById(R.id.close_toot);
            close_toot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            TextView toolbarTitle = actionBar.getCustomView().findViewById(R.id.toolbar_title);
            pp_actionBar = actionBar.getCustomView().findViewById(R.id.pp_actionBar);
            toolbarTitle.setText(getString(R.string.owner_cached_toots));
        }
        statuses = new ArrayList<>();
        RecyclerView lv_status = findViewById(R.id.lv_status);
        mainLoader =  findViewById(R.id.loader);
        nextElementLoader = findViewById(R.id.loading_next_status);
        textviewNoAction =  findViewById(R.id.no_action);
        mainLoader.setVisibility(View.VISIBLE);
        nextElementLoader.setVisibility(View.GONE);
        max_id = null;
        flag_loading = true;
        firstLoad = true;
        swiped = false;
        boolean isOnWifi = Helper.isOnWIFI(OwnerStatusActivity.this);
        int positionSpinnerTrans = sharedpreferences.getInt(Helper.SET_TRANSLATOR, Helper.TRANS_YANDEX);
        int behaviorWithAttachments = sharedpreferences.getInt(Helper.SET_ATTACHMENT_ACTION, Helper.ATTACHMENT_ALWAYS);
        lv_status.addItemDecoration(new DividerItemDecoration(OwnerStatusActivity.this, DividerItemDecoration.VERTICAL));
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        statusListAdapter = new StatusListAdapter(OwnerStatusActivity.this, RetrieveFeedsAsyncTask.Type.CACHE_STATUS, userId, isOnWifi, behaviorWithAttachments, positionSpinnerTrans, this.statuses);
        lv_status.setAdapter(statusListAdapter);
        mLayoutManager = new LinearLayoutManager(OwnerStatusActivity.this);
        lv_status.setLayoutManager(mLayoutManager);


        if( theme == Helper.THEME_DARK){
            style = R.style.DialogDark;
        }else {
            style = R.style.Dialog;
        }

        SQLiteDatabase db = Sqlite.getInstance(OwnerStatusActivity.this, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        Account account = new AccountDAO(OwnerStatusActivity.this,db).getAccountByID(userId);
        String url = account.getAvatar();
        if( url.startsWith("/") ){
            url = Helper.getLiveInstanceWithProtocol(OwnerStatusActivity.this) + account.getAvatar();
        }
        Glide.with(OwnerStatusActivity.this)
                .asBitmap()
                .load(url)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                        BitmapDrawable ppDrawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(resource, (int) Helper.convertDpToPixel(25, OwnerStatusActivity.this), (int) Helper.convertDpToPixel(25, OwnerStatusActivity.this), true));
                        if( pp_actionBar != null){
                            pp_actionBar.setImageDrawable(ppDrawable);
                        } else if( getSupportActionBar() != null){

                            getSupportActionBar().setIcon(ppDrawable);
                            getSupportActionBar().setDisplayShowHomeEnabled(true);
                        }
                    }
                });

        swipeRefreshLayout = findViewById(R.id.swipeContainer);
        new RetrieveFeedsAsyncTask(OwnerStatusActivity.this, filterToots, null, OwnerStatusActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        swipeRefreshLayout.setColorSchemeResources(R.color.mastodonC4,
                R.color.mastodonC2,
                R.color.mastodonC3);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                max_id = null;
                firstLoad = true;
                flag_loading = true;
                swiped = true;
                new RetrieveFeedsAsyncTask(OwnerStatusActivity.this, filterToots, null, OwnerStatusActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        lv_status.addOnScrollListener(new RecyclerView.OnScrollListener() {
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                if(dy > 0){
                    int visibleItemCount = mLayoutManager.getChildCount();
                    int totalItemCount = mLayoutManager.getItemCount();
                    if(firstVisibleItem + visibleItemCount == totalItemCount ) {
                        if(!flag_loading ) {
                            flag_loading = true;
                            new RetrieveFeedsAsyncTask(OwnerStatusActivity.this, filterToots, max_id, OwnerStatusActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            nextElementLoader.setVisibility(View.VISIBLE);
                        }
                    } else {
                        nextElementLoader.setVisibility(View.GONE);
                    }
                }
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_owner_cache, menu);
        return true;
    }

    private DatePickerDialog.OnDateSetListener iniDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year,
                                      int monthOfYear, int dayOfMonth) {
                    Calendar c = Calendar.getInstance();
                    c.set(year, monthOfYear, dayOfMonth, 0, 0);
                    dateIni = new Date(c.getTimeInMillis());
                    settings_time_from.setText(Helper.shortDateToString(new Date(c.getTimeInMillis())));
                }

            };
    private DatePickerDialog.OnDateSetListener endDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year,
                                      int monthOfYear, int dayOfMonth) {
                    Calendar c = Calendar.getInstance();
                    c.set(year, monthOfYear, dayOfMonth, 23, 59);

                    dateEnd = new Date(c.getTimeInMillis());
                    settings_time_to.setText(Helper.shortDateToString(new Date(c.getTimeInMillis())));
                }

            };
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_sync:
                Intent backupIntent = new Intent(OwnerStatusActivity.this, BackupStatusInDataBaseService.class);
                startService(backupIntent);
                return true;
            case R.id.action_filter:
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(OwnerStatusActivity.this);
                LayoutInflater inflater = this.getLayoutInflater();
                @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.filter_owner_toots, null);
                dialogBuilder.setView(dialogView);


                SQLiteDatabase db = Sqlite.getInstance(OwnerStatusActivity.this, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                if( dateIni == null)
                    dateIni = new StatusCacheDAO(OwnerStatusActivity.this, db).getSmallerDate(StatusCacheDAO.ARCHIVE_CACHE);
                if( dateEnd == null)
                    dateEnd = new StatusCacheDAO(OwnerStatusActivity.this, db).getGreaterDate(StatusCacheDAO.ARCHIVE_CACHE);
                if( dateIni == null || dateEnd == null)
                    return true;
                String dateInitString = Helper.shortDateToString(dateIni);
                String dateEndString = Helper.shortDateToString(dateEnd);

                //Initializes settings for filter
                settings_time_from = dialogView.findViewById(R.id.settings_time_from);
                settings_time_to = dialogView.findViewById(R.id.settings_time_to);

                final CheckBox filter_visibility_public = dialogView.findViewById(R.id.filter_visibility_public);
                final CheckBox filter_visibility_unlisted = dialogView.findViewById(R.id.filter_visibility_unlisted);
                final CheckBox filter_visibility_private = dialogView.findViewById(R.id.filter_visibility_private);
                final CheckBox filter_visibility_direct = dialogView.findViewById(R.id.filter_visibility_direct);

                filter_visibility_public.setChecked(filterToots.isV_public());
                filter_visibility_unlisted.setChecked(filterToots.isV_unlisted());
                filter_visibility_private.setChecked(filterToots.isV_private());
                filter_visibility_direct.setChecked(filterToots.isV_direct());

                final Spinner filter_boost = dialogView.findViewById(R.id.filter_boost);
                final Spinner filter_replies = dialogView.findViewById(R.id.filter_replies);
                final Spinner filter_media = dialogView.findViewById(R.id.filter_media);
                final Spinner filter_pinned = dialogView.findViewById(R.id.filter_pinned);

                filter_boost.setSelection(filterToots.getBoosts().ordinal());
                filter_replies.setSelection(filterToots.getReplies().ordinal());
                filter_media.setSelection(filterToots.getMedia().ordinal());
                filter_pinned.setSelection(filterToots.getPinned().ordinal());

                final EditText filter_keywords = dialogView.findViewById(R.id.filter_keywords);

                settings_time_from.setText(dateInitString);
                settings_time_to.setText(dateEndString);

                if( filterToots.getFilter() != null)
                    filter_keywords.setText(filterToots.getFilter());

                Calendar c = Calendar.getInstance();
                c.setTime(dateIni);
                int yearIni = c.get(Calendar.YEAR);
                int monthIni = c.get(Calendar.MONTH);
                int dayIni = c.get(Calendar.DAY_OF_MONTH);

                final DatePickerDialog dateIniPickerDialog = new DatePickerDialog(
                        OwnerStatusActivity.this, style, iniDateSetListener, yearIni, monthIni, dayIni);
                settings_time_from.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dateIniPickerDialog.show();
                    }
                });
                Calendar ce = Calendar.getInstance();
                c.setTime(dateEnd);
                int yearEnd = ce.get(Calendar.YEAR);
                int monthEnd = ce.get(Calendar.MONTH);
                int dayEnd = ce.get(Calendar.DAY_OF_MONTH);
                final DatePickerDialog dateEndPickerDialog = new DatePickerDialog(
                        OwnerStatusActivity.this, style, endDateSetListener, yearEnd, monthEnd, dayEnd);
                settings_time_to.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dateEndPickerDialog.show();
                    }
                });
                dialogBuilder
                        .setTitle(R.string.action_filter)
                        .setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                RelativeLayout no_result = findViewById(R.id.no_result);
                                no_result.setVisibility(View.GONE);

                                filterToots.setBoosts(FilterToots.typeFilter.values()[filter_boost.getSelectedItemPosition()]);
                                filterToots.setReplies(FilterToots.typeFilter.values()[filter_replies.getSelectedItemPosition()]);
                                filterToots.setMedia(FilterToots.typeFilter.values()[filter_media.getSelectedItemPosition()]);
                                filterToots.setPinned(FilterToots.typeFilter.values()[filter_pinned.getSelectedItemPosition()]);

                                filterToots.setV_public(filter_visibility_public.isChecked());
                                filterToots.setV_unlisted(filter_visibility_unlisted.isChecked());
                                filterToots.setV_private(filter_visibility_private.isChecked());
                                filterToots.setV_direct(filter_visibility_direct.isChecked());

                                filterToots.setDateIni(Helper.dateToString(OwnerStatusActivity.this,dateIni));
                                filterToots.setDateEnd(Helper.dateToString(OwnerStatusActivity.this,dateEnd));

                                if( filter_keywords.getText() != null && filter_keywords.getText().toString().trim().length() > 0)
                                    filterToots.setFilter(filter_keywords.getText().toString());
                                else
                                    filterToots.setFilter(null);
                                swipeRefreshLayout.setRefreshing(true);
                                max_id = null;
                                firstLoad = true;
                                flag_loading = true;
                                swiped = true;
                                new RetrieveFeedsAsyncTask(OwnerStatusActivity.this, filterToots, null, OwnerStatusActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                final AlertDialog alertDialog = dialogBuilder.create();

                alertDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRetrieveFeeds(APIResponse apiResponse) {
        mainLoader.setVisibility(View.GONE);
        nextElementLoader.setVisibility(View.GONE);
        //Discards 404 - error which can often happen due to toots which have been deleted
        if( apiResponse.getError() != null && apiResponse.getError().getStatusCode() != 404 ){
            boolean show_error_messages = sharedpreferences.getBoolean(Helper.SET_SHOW_ERROR_MESSAGES, true);
            if( show_error_messages )
                Toast.makeText(OwnerStatusActivity.this, apiResponse.getError().getError(),Toast.LENGTH_LONG).show();
            swipeRefreshLayout.setRefreshing(false);
            swiped = false;
            flag_loading = false;
            return;
        }
        int previousPosition = this.statuses.size();
        List<Status> statuses = apiResponse.getStatuses();
        max_id = apiResponse.getMax_id();
        flag_loading = (max_id == null );
        if( !swiped && firstLoad && (statuses == null || statuses.size() == 0))
            textviewNoAction.setVisibility(View.VISIBLE);
        else
            textviewNoAction.setVisibility(View.GONE);

        if( swiped ){
            if (previousPosition > 0) {
                for (int i = 0; i < previousPosition; i++) {
                    this.statuses.remove(0);
                }
                statusListAdapter.notifyItemRangeRemoved(0, previousPosition);
            }
            swiped = false;
        }
        if( statuses != null && statuses.size() > 0) {
            this.statuses.addAll(statuses);
            statusListAdapter.notifyItemRangeInserted(previousPosition, statuses.size());
        }else {
            if( textviewNoAction.getVisibility() != View.VISIBLE && firstLoad) {
                RelativeLayout no_result = findViewById(R.id.no_result);
                no_result.setVisibility(View.VISIBLE);
            }
        }
        swipeRefreshLayout.setRefreshing(false);
        firstLoad = false;

    }


    private BroadcastReceiver backupFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            max_id = null;
            firstLoad = true;
            flag_loading = true;
            swiped = true;
            new RetrieveFeedsAsyncTask(OwnerStatusActivity.this, filterToots, null, OwnerStatusActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(backupFinishedReceiver);
    }

}
