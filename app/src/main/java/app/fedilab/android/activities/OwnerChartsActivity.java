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


import android.annotation.SuppressLint;
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
import android.widget.EditText;
import android.widget.ImageView;
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

import com.github.mikephil.charting.charts.LineChart;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import app.fedilab.android.R;
import app.fedilab.android.asynctasks.RetrieveChartsAsyncTask;
import app.fedilab.android.asynctasks.RetrieveFeedsAsyncTask;
import app.fedilab.android.asynctasks.RetrieveStatsAsyncTask;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Charts;
import app.fedilab.android.client.Entities.Statistics;
import app.fedilab.android.client.Entities.Status;
import app.fedilab.android.drawers.StatusListAdapter;
import app.fedilab.android.helper.FilterToots;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.interfaces.OnRetrieveChartsInterface;
import app.fedilab.android.interfaces.OnRetrieveFeedsInterface;
import app.fedilab.android.interfaces.OnRetrieveStatsInterface;
import app.fedilab.android.services.BackupStatusInDataBaseService;
import app.fedilab.android.sqlite.AccountDAO;
import app.fedilab.android.sqlite.Sqlite;
import app.fedilab.android.sqlite.StatusCacheDAO;
import es.dmoral.toasty.Toasty;


/**
 * Created by Thomas on 28/07/2019.
 * Charts for owner activity
 */

public class OwnerChartsActivity extends BaseActivity implements OnRetrieveChartsInterface {


    LinearLayoutManager mLayoutManager;
    private int style;
    private Button settings_time_from, settings_time_to;
    private Date dateIni, dateEnd;
    private LineChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        switch (theme){
            case Helper.THEME_LIGHT:
                setTheme(R.style.AppTheme_NoActionBar_Fedilab);
                break;
            case Helper.THEME_DARK:
                setTheme(R.style.AppThemeDark_NoActionBar);
                break;
            case Helper.THEME_BLACK:
                setTheme(R.style.AppThemeBlack_NoActionBar);
                break;
            default:
                setTheme(R.style.AppThemeDark_NoActionBar);
        }
        setContentView(R.layout.activity_ower_charts);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if( theme == Helper.THEME_BLACK)
            toolbar.setBackgroundColor(ContextCompat.getColor(OwnerChartsActivity.this, R.color.black));
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null ){
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.simple_action_bar, null);
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
            ImageView pp_actionBar = actionBar.getCustomView().findViewById(R.id.pp_actionBar);
            if (theme == Helper.THEME_LIGHT){
                Helper.colorizeToolbar(actionBar.getCustomView().findViewById(R.id.toolbar), R.color.black, OwnerChartsActivity.this);
            }
            toolbarTitle.setText(getString(R.string.owner_charts));
        }


        chart = findViewById(R.id.chart);
        settings_time_from = findViewById(R.id.settings_time_from);
        settings_time_to = findViewById(R.id.settings_time_to);
        if( theme == Helper.THEME_DARK){
            style = R.style.DialogDark;
        }else  if( theme == Helper.THEME_BLACK){
            style = R.style.DialogBlack;
        }else {
            style = R.style.Dialog;
        }

        SQLiteDatabase db = Sqlite.getInstance(OwnerChartsActivity.this, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        dateIni = new StatusCacheDAO(OwnerChartsActivity.this, db).getSmallerDate(StatusCacheDAO.ARCHIVE_CACHE);
        dateEnd = new StatusCacheDAO(OwnerChartsActivity.this, db).getGreaterDate(StatusCacheDAO.ARCHIVE_CACHE);
        if(dateIni == null){
            dateIni = new Date();
        }
        if( dateEnd == null){
            dateEnd = new Date();
        }

        String dateInitString = Helper.shortDateToString(dateIni);
        String dateEndString = Helper.shortDateToString(dateEnd);

        settings_time_from.setText(dateInitString);
        settings_time_to.setText(dateEndString);

        new RetrieveChartsAsyncTask(OwnerChartsActivity.this, dateIni, dateEnd, OwnerChartsActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

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

            default:
                return super.onOptionsItemSelected(item);
        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onCharts(Charts charts) {

    }
}
