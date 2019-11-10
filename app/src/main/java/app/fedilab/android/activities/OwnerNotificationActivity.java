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


import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import app.fedilab.android.R;
import app.fedilab.android.asynctasks.RetrieveNotificationStatsAsyncTask;
import app.fedilab.android.asynctasks.RetrieveNotificationsCacheAsyncTask;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Notification;
import app.fedilab.android.client.Entities.Statistics;
import app.fedilab.android.client.Entities.StatisticsNotification;
import app.fedilab.android.drawers.NotificationsListAdapter;
import app.fedilab.android.helper.FilterNotifications;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.interfaces.OnRetrieveCacheNotificationsInterface;
import app.fedilab.android.interfaces.OnRetrieveNotificationStatsInterface;
import app.fedilab.android.interfaces.OnRetrieveStatsInterface;
import app.fedilab.android.services.BackupNotificationInDataBaseService;
import app.fedilab.android.sqlite.AccountDAO;
import app.fedilab.android.sqlite.NotificationCacheDAO;
import app.fedilab.android.sqlite.Sqlite;
import es.dmoral.toasty.Toasty;


/**
 * Created by Thomas on 24/08/2019.
 * Show owner's notifications
 */

public class OwnerNotificationActivity extends BaseActivity implements OnRetrieveCacheNotificationsInterface, OnRetrieveNotificationStatsInterface {


    private ImageView pp_actionBar;
    private NotificationsListAdapter notificationsListAdapter;
    private String max_id;
    private List<Notification> notifications;
    private RelativeLayout mainLoader, nextElementLoader, textviewNoAction;
    private boolean firstLoad;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean swiped;
    private boolean flag_loading;
    LinearLayoutManager mLayoutManager;
    private int style;
    private Button settings_time_from, settings_time_to;
    private FilterNotifications filterNotifications;
    private Date dateIni, dateEnd;
    private View statsDialogView;
    private StatisticsNotification statistics;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        switch (theme) {
            case Helper.THEME_LIGHT:
                setTheme(R.style.AppTheme_NoActionBar_Fedilab);
                break;
            case Helper.THEME_BLACK:
                setTheme(R.style.AppThemeBlack_NoActionBar);
                break;
            default:
                setTheme(R.style.AppThemeDark_NoActionBar);
        }
        setContentView(R.layout.activity_ower_notifications);

        filterNotifications = new FilterNotifications();

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(backupFinishedReceiver,
                        new IntentFilter(Helper.INTENT_BACKUP_FINISH));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            View view = inflater.inflate(R.layout.toot_action_bar, new LinearLayout(getApplicationContext()), false);
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
            toolbarTitle.setText(getString(R.string.owner_cached_notifications));
        }
        notifications = new ArrayList<>();
        RecyclerView lv_notifications = findViewById(R.id.lv_notifications);
        mainLoader = findViewById(R.id.loader);
        nextElementLoader = findViewById(R.id.loading_next_status);
        textviewNoAction = findViewById(R.id.no_action);
        mainLoader.setVisibility(View.VISIBLE);
        nextElementLoader.setVisibility(View.GONE);
        max_id = null;
        flag_loading = true;
        firstLoad = true;
        swiped = false;
        boolean isOnWifi = Helper.isOnWIFI(OwnerNotificationActivity.this);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = sharedpreferences.getString(Helper.PREF_INSTANCE, null);
        int behaviorWithAttachments = sharedpreferences.getInt(Helper.SET_ATTACHMENT_ACTION, Helper.ATTACHMENT_ALWAYS);
        notificationsListAdapter = new NotificationsListAdapter(isOnWifi, behaviorWithAttachments, this.notifications);
        lv_notifications.setAdapter(notificationsListAdapter);
        mLayoutManager = new LinearLayoutManager(OwnerNotificationActivity.this);
        lv_notifications.setLayoutManager(mLayoutManager);


        if (theme == Helper.THEME_DARK) {
            style = R.style.DialogDark;
        } else if (theme == Helper.THEME_BLACK) {
            style = R.style.DialogBlack;
        } else {
            style = R.style.Dialog;
        }

        SQLiteDatabase db = Sqlite.getInstance(OwnerNotificationActivity.this, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        Account account = new AccountDAO(OwnerNotificationActivity.this, db).getUniqAccount(userId, instance);

        Helper.loadGiF(getApplicationContext(), account.getAvatar(), pp_actionBar);

        swipeRefreshLayout = findViewById(R.id.swipeContainer);
        new RetrieveNotificationsCacheAsyncTask(OwnerNotificationActivity.this, filterNotifications, null, OwnerNotificationActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                max_id = null;
                firstLoad = true;
                flag_loading = true;
                swiped = true;
                new RetrieveNotificationsCacheAsyncTask(OwnerNotificationActivity.this, filterNotifications, null, OwnerNotificationActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

        lv_notifications.addOnScrollListener(new RecyclerView.OnScrollListener() {
            public void onScrolled(@NotNull RecyclerView recyclerView, int dx, int dy) {
                int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                if (dy > 0) {
                    int visibleItemCount = mLayoutManager.getChildCount();
                    int totalItemCount = mLayoutManager.getItemCount();
                    if (firstVisibleItem + visibleItemCount == totalItemCount) {
                        if (!flag_loading) {
                            flag_loading = true;
                            new RetrieveNotificationsCacheAsyncTask(OwnerNotificationActivity.this, filterNotifications, max_id, OwnerNotificationActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
    public boolean onCreateOptionsMenu(@NotNull Menu menu) {
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
                Intent backupIntent = new Intent(OwnerNotificationActivity.this, BackupNotificationInDataBaseService.class);
                startService(backupIntent);
                statistics = null;
                return true;
            case R.id.action_stats:
                SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
                if (theme == Helper.THEME_DARK) {
                    style = R.style.DialogDark;
                } else if (theme == Helper.THEME_BLACK) {
                    style = R.style.DialogBlack;
                } else {
                    style = R.style.Dialog;
                }
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(OwnerNotificationActivity.this, style);
                LayoutInflater inflater = this.getLayoutInflater();
                statsDialogView = inflater.inflate(R.layout.stats_owner_notifications, null);
                dialogBuilder.setView(statsDialogView);
                dialogBuilder
                        .setTitle(R.string.action_stats)
                        .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                dialogBuilder.create().show();
                if (statistics == null) {
                    new RetrieveNotificationStatsAsyncTask(getApplicationContext(), OwnerNotificationActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    displayStats();
                }
                return true;
            case R.id.action_filter:
                sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
                if (theme == Helper.THEME_DARK) {
                    style = R.style.DialogDark;
                } else if (theme == Helper.THEME_BLACK) {
                    style = R.style.DialogBlack;
                } else {
                    style = R.style.Dialog;
                }
                dialogBuilder = new AlertDialog.Builder(OwnerNotificationActivity.this, style);
                inflater = this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.filter_owner_notifications, new LinearLayout(getApplicationContext()), false);
                dialogBuilder.setView(dialogView);


                SQLiteDatabase db = Sqlite.getInstance(OwnerNotificationActivity.this, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                if (dateIni == null)
                    dateIni = new NotificationCacheDAO(OwnerNotificationActivity.this, db).getSmallerDate();
                if (dateEnd == null)
                    dateEnd = new NotificationCacheDAO(OwnerNotificationActivity.this, db).getGreaterDate();
                if (dateIni == null || dateEnd == null)
                    return true;
                String dateInitString = Helper.shortDateToString(dateIni);
                String dateEndString = Helper.shortDateToString(dateEnd);

                //Initializes settings for filter
                settings_time_from = dialogView.findViewById(R.id.settings_time_from);
                settings_time_to = dialogView.findViewById(R.id.settings_time_to);


                settings_time_from.setText(dateInitString);
                settings_time_to.setText(dateEndString);

                final CheckBox filter_boosts = dialogView.findViewById(R.id.filter_boosts);
                final CheckBox filter_fav = dialogView.findViewById(R.id.filter_fav);
                final CheckBox filter_mention = dialogView.findViewById(R.id.filter_mention);
                final CheckBox filter_follow = dialogView.findViewById(R.id.filter_follow);
                final CheckBox filter_poll = dialogView.findViewById(R.id.filter_poll);

                filter_boosts.setChecked(filterNotifications.isBoost());
                filter_fav.setChecked(filterNotifications.isFavorite());
                filter_mention.setChecked(filterNotifications.isMention());
                filter_follow.setChecked(filterNotifications.isFollow());
                filter_poll.setChecked(filterNotifications.isPoll());


                Calendar c = Calendar.getInstance();
                c.setTime(dateIni);
                int yearIni = c.get(Calendar.YEAR);
                int monthIni = c.get(Calendar.MONTH);
                int dayIni = c.get(Calendar.DAY_OF_MONTH);

                final DatePickerDialog dateIniPickerDialog = new DatePickerDialog(
                        OwnerNotificationActivity.this, style, iniDateSetListener, yearIni, monthIni, dayIni);
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
                        OwnerNotificationActivity.this, style, endDateSetListener, yearEnd, monthEnd, dayEnd);
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

                                filterNotifications.setBoost(filter_boosts.isChecked());
                                filterNotifications.setFavorite(filter_fav.isChecked());
                                filterNotifications.setMention(filter_mention.isChecked());
                                filterNotifications.setFollow(filter_follow.isChecked());
                                filterNotifications.setPoll(filter_poll.isChecked());


                                swipeRefreshLayout.setRefreshing(true);
                                max_id = null;
                                firstLoad = true;
                                flag_loading = true;
                                swiped = true;
                                new RetrieveNotificationsCacheAsyncTask(OwnerNotificationActivity.this, filterNotifications, null, OwnerNotificationActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                dialog.dismiss();
                            }
                        })
                        .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                dialogBuilder.create().show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private BroadcastReceiver backupFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            max_id = null;
            firstLoad = true;
            flag_loading = true;
            swiped = true;
            new RetrieveNotificationsCacheAsyncTask(OwnerNotificationActivity.this, filterNotifications, null, OwnerNotificationActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(backupFinishedReceiver);
    }

    private void displayStats() {
        if (statsDialogView != null) {
            ScrollView stats_container = statsDialogView.findViewById(R.id.stats_container);
            RelativeLayout loader = statsDialogView.findViewById(R.id.loader);

            TextView total_notifications = statsDialogView.findViewById(R.id.total_notifications);
            TextView number_boosts = statsDialogView.findViewById(R.id.number_boosts);
            TextView number_favourites = statsDialogView.findViewById(R.id.number_favourites);
            TextView number_mentions = statsDialogView.findViewById(R.id.number_mentions);
            TextView number_follows = statsDialogView.findViewById(R.id.number_follows);
            TextView number_polls = statsDialogView.findViewById(R.id.number_polls);


            TextView frequency = statsDialogView.findViewById(R.id.frequency);
            TextView last_toot_date = statsDialogView.findViewById(R.id.last_toot_date);
            TextView first_toot_date = statsDialogView.findViewById(R.id.first_toot_date);


            ImageButton charts = statsDialogView.findViewById(R.id.charts);
            charts.setOnClickListener(w -> {
                Intent intent = new Intent(OwnerNotificationActivity.this, OwnerNotificationChartsActivity.class);
                startActivity(intent);
            });

            total_notifications.setText(String.valueOf(statistics.getTotal_notification()));
            number_boosts.setText(String.valueOf(statistics.getNumber_reblog()));
            number_favourites.setText(String.valueOf(statistics.getNumber_favourite()));
            number_mentions.setText(String.valueOf(statistics.getNumber_mentions()));
            number_follows.setText(String.valueOf(statistics.getNumber_follow()));
            number_polls.setText(String.valueOf(statistics.getNumber_poll()));


            first_toot_date.setText(Helper.dateToString(statistics.getFirstTootDate()));
            last_toot_date.setText(Helper.dateToString(statistics.getLastTootDate()));
            DecimalFormat df = new DecimalFormat("#.##");
            frequency.setText(getString(R.string.notification_per_day, df.format(statistics.getFrequency())));


            stats_container.setVisibility(View.VISIBLE);
            loader.setVisibility(View.GONE);

        } else {
            Toasty.error(getApplicationContext(), getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRetrieveNotifications(APIResponse apiResponse) {
        mainLoader.setVisibility(View.GONE);
        nextElementLoader.setVisibility(View.GONE);
        //Discards 404 - error which can often happen due to toots which have been deleted
        if (apiResponse.getError() != null && apiResponse.getError().getStatusCode() != 404) {
            Toasty.error(getApplicationContext(), apiResponse.getError().getError(), Toast.LENGTH_LONG).show();
            swipeRefreshLayout.setRefreshing(false);
            swiped = false;
            flag_loading = false;
            return;
        }
        int previousPosition = this.notifications.size();
        List<Notification> notifications = apiResponse.getNotifications();
        max_id = apiResponse.getMax_id();
        flag_loading = (max_id == null);
        if (!swiped && firstLoad && (notifications == null || notifications.size() == 0))
            textviewNoAction.setVisibility(View.VISIBLE);
        else
            textviewNoAction.setVisibility(View.GONE);

        if (swiped) {
            if (previousPosition > 0) {
                for (int i = 0; i < previousPosition; i++) {
                    this.notifications.remove(0);
                }
                notificationsListAdapter.notifyItemRangeRemoved(0, previousPosition);
            }
            swiped = false;
        }
        if (notifications != null && notifications.size() > 0) {
            this.notifications.addAll(notifications);
            notificationsListAdapter.notifyItemRangeInserted(previousPosition, notifications.size());
        } else {
            if (textviewNoAction.getVisibility() != View.VISIBLE && firstLoad) {
                RelativeLayout no_result = findViewById(R.id.no_result);
                no_result.setVisibility(View.VISIBLE);
            }
        }
        swipeRefreshLayout.setRefreshing(false);
        firstLoad = false;
    }

    @Override
    public void onStats(StatisticsNotification statistics) {
        this.statistics = statistics;
        displayStats();
    }
}
