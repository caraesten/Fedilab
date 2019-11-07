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
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import app.fedilab.android.R;
import app.fedilab.android.asynctasks.RetrieveNotificationChartsAsyncTask;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.NotificationCharts;
import app.fedilab.android.client.Entities.Status;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.interfaces.OnRetrieveNotificationChartsInterface;
import app.fedilab.android.sqlite.AccountDAO;
import app.fedilab.android.sqlite.NotificationCacheDAO;
import app.fedilab.android.sqlite.Sqlite;
import app.fedilab.android.sqlite.StatusCacheDAO;

import static app.fedilab.android.sqlite.StatusCacheDAO.NOTIFICATION_CACHE;


/**
 * Created by Thomas on 26/08/2019.
 * Charts for owner notifications activity
 */

public class OwnerNotificationChartsActivity extends BaseActivity implements OnRetrieveNotificationChartsInterface {


    LinearLayoutManager mLayoutManager;
    private Button settings_time_from, settings_time_to;
    private Date dateIni, dateEnd;
    private LineChart chart;
    private int theme;
    private RelativeLayout loader;
    private ImageButton validate;
    private String status_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        switch (theme) {
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
        Bundle b = getIntent().getExtras();
        status_id = null;
        if (b != null)
            status_id = b.getString("status_id");
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            View view = inflater.inflate(R.layout.simple_action_bar, new LinearLayout(getApplicationContext()), false);
            actionBar.setCustomView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            ImageView toolbar_close = actionBar.getCustomView().findViewById(R.id.close_conversation);
            ImageView pp_actionBar = actionBar.getCustomView().findViewById(R.id.pp_actionBar);
            TextView toolbar_title = actionBar.getCustomView().findViewById(R.id.toolbar_title);


            SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
            String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
            String instance = sharedpreferences.getString(Helper.PREF_INSTANCE, null);
            Account account = new AccountDAO(getApplicationContext(), db).getUniqAccount(userId, instance);
            if (account != null) {
                Helper.loadGiF(getApplicationContext(), account.getAvatar(), pp_actionBar);
            }

            toolbar_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            if (account != null) {
                toolbar_title.setText(getString(R.string.owner_charts) + " - " + account.getUsername() + "@" + account.getInstance());
            } else {
                toolbar_title.setText(R.string.owner_charts);
            }
        }
        setContentView(R.layout.activity_ower_charts);


        chart = findViewById(R.id.chart);
        settings_time_from = findViewById(R.id.settings_time_from);
        settings_time_to = findViewById(R.id.settings_time_to);
        loader = findViewById(R.id.loader);
        validate = findViewById(R.id.validate);
        LinearLayout date_container = findViewById(R.id.date_container);
        SQLiteDatabase db = Sqlite.getInstance(OwnerNotificationChartsActivity.this, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        if (status_id == null) {
            dateIni = new NotificationCacheDAO(OwnerNotificationChartsActivity.this, db).getSmallerDate();
            dateEnd = new NotificationCacheDAO(OwnerNotificationChartsActivity.this, db).getGreaterDate();
        } else {
            Status status = new StatusCacheDAO(getApplicationContext(), db).getStatus(status_id);
            if (status == null) {
                finish();
                return;
            } else {
                dateIni = status.getCreated_at();
                dateEnd = dateIni;
                date_container.setVisibility(View.GONE);
            }
        }


        int style;
        if (theme == Helper.THEME_DARK) {
            style = R.style.DialogDark;
        } else if (theme == Helper.THEME_BLACK) {
            style = R.style.DialogBlack;
        } else {
            style = R.style.Dialog;
        }
        Calendar c = Calendar.getInstance();
        if (dateIni != null) {
            c.setTime(dateIni);
        }
        int yearIni = c.get(Calendar.YEAR);
        int monthIni = c.get(Calendar.MONTH);
        int dayIni = c.get(Calendar.DAY_OF_MONTH);

        final DatePickerDialog dateIniPickerDialog = new DatePickerDialog(
                OwnerNotificationChartsActivity.this, style, iniDateSetListener, yearIni, monthIni, dayIni);
        settings_time_from.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateIniPickerDialog.show();
            }
        });

        if (dateIni != null) {
            Calendar ce = Calendar.getInstance();
            c.setTime(dateEnd);
            int yearEnd = ce.get(Calendar.YEAR);
            int monthEnd = ce.get(Calendar.MONTH);
            int dayEnd = ce.get(Calendar.DAY_OF_MONTH);
            final DatePickerDialog dateEndPickerDialog = new DatePickerDialog(
                    OwnerNotificationChartsActivity.this, style, endDateSetListener, yearEnd, monthEnd, dayEnd);
            settings_time_to.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dateEndPickerDialog.show();
                }
            });

            dateIniPickerDialog.getDatePicker().setMinDate(dateIni.getTime());
            dateIniPickerDialog.getDatePicker().setMaxDate(dateEnd.getTime());

            dateEndPickerDialog.getDatePicker().setMinDate(dateIni.getTime());
            dateEndPickerDialog.getDatePicker().setMaxDate(dateEnd.getTime());

            Calendar cal = Calendar.getInstance();
            cal.setTime(dateEnd);
            cal.add(Calendar.MONTH, -1);
            Date result = cal.getTime();
            if (result.after(dateIni))
                dateIni = result;

            if (dateIni == null) {
                dateIni = new Date();
            }
            if (dateEnd == null) {
                dateEnd = new Date();
            }


            CustomMarkerView mv = new CustomMarkerView(getApplicationContext(), R.layout.markerview);
            chart.setMarkerView(mv);

            validate.setOnClickListener(v -> {
                loadGraph(dateIni, dateEnd);
            });

            loadGraph(dateIni, dateEnd);
        }

    }

    @Override
    public void onCharts(NotificationCharts charts) {


        List<Entry> boostsEntry = new ArrayList<>();


        Iterator it = charts.getReblogs().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            boostsEntry.add(new Entry((long) pair.getKey(), (int) pair.getValue()));
            it.remove();
        }

        List<Entry> favEntry = new ArrayList<>();
        it = charts.getFavourites().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            favEntry.add(new Entry((long) pair.getKey(), (int) pair.getValue()));
            it.remove();
        }

        List<Entry> mentionEntry = new ArrayList<>();
        it = charts.getMentions().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            mentionEntry.add(new Entry((long) pair.getKey(), (int) pair.getValue()));
            it.remove();
        }


        List<Entry> followEntry = new ArrayList<>();
        it = charts.getFollows().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            followEntry.add(new Entry((long) pair.getKey(), (int) pair.getValue()));
            it.remove();
        }

        /*List<Entry> pollEntry = new ArrayList<>();
        it = charts.getFollows().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            pollEntry.add(new Entry((long)pair.getKey(), (int)pair.getValue()));
            it.remove();
        }*/


        LineDataSet dataSetBoosts = new LineDataSet(boostsEntry, getString(R.string.reblog));
        dataSetBoosts.setColor(ContextCompat.getColor(OwnerNotificationChartsActivity.this, R.color.chart_notif_boost));
        dataSetBoosts.setValueTextSize(10f);
        dataSetBoosts.setValueTextColor(ContextCompat.getColor(OwnerNotificationChartsActivity.this, R.color.chart_notif_boost));
        dataSetBoosts.setFillColor(ContextCompat.getColor(OwnerNotificationChartsActivity.this, R.color.chart_notif_boost));
        dataSetBoosts.setDrawValues(false);
        dataSetBoosts.setDrawFilled(true);
        dataSetBoosts.setDrawCircles(false);
        dataSetBoosts.setDrawCircleHole(false);
        dataSetBoosts.setLineWidth(2f);
        if (status_id == null) {
            dataSetBoosts.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        } else {
            dataSetBoosts.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        }

        LineDataSet dateSetFavorites = new LineDataSet(favEntry, getString(R.string.favourite));
        dateSetFavorites.setColor(ContextCompat.getColor(OwnerNotificationChartsActivity.this, R.color.chart_notif_fav));
        dateSetFavorites.setValueTextSize(10f);
        dateSetFavorites.setValueTextColor(ContextCompat.getColor(OwnerNotificationChartsActivity.this, R.color.chart_notif_fav));
        dateSetFavorites.setFillColor(ContextCompat.getColor(OwnerNotificationChartsActivity.this, R.color.chart_notif_fav));
        dateSetFavorites.setDrawValues(false);
        dateSetFavorites.setDrawFilled(true);
        dateSetFavorites.setDrawCircles(false);
        dateSetFavorites.setDrawCircleHole(false);
        dateSetFavorites.setLineWidth(2f);
        if (status_id == null) {
            dateSetFavorites.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        } else {
            dateSetFavorites.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        }

        LineDataSet dataSetMention = new LineDataSet(mentionEntry, getString(R.string.mention));
        dataSetMention.setColor(ContextCompat.getColor(OwnerNotificationChartsActivity.this, R.color.chart_notif_mention));
        dataSetMention.setValueTextSize(10f);
        dataSetMention.setValueTextColor(ContextCompat.getColor(OwnerNotificationChartsActivity.this, R.color.chart_notif_mention));
        dataSetMention.setFillColor(ContextCompat.getColor(OwnerNotificationChartsActivity.this, R.color.chart_notif_mention));
        dataSetMention.setDrawValues(false);
        dataSetMention.setDrawFilled(true);
        dataSetMention.setDrawCircles(false);
        dataSetMention.setDrawCircleHole(false);
        dataSetMention.setLineWidth(2f);
        if (status_id == null) {
            dataSetMention.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        } else {
            dataSetMention.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        }


        LineDataSet dataSetFollow = new LineDataSet(followEntry, getString(R.string.follow));
        dataSetFollow.setColor(ContextCompat.getColor(OwnerNotificationChartsActivity.this, R.color.chart_notif_follow));
        dataSetFollow.setValueTextSize(10f);
        dataSetFollow.setValueTextColor(ContextCompat.getColor(OwnerNotificationChartsActivity.this, R.color.chart_notif_follow));
        dataSetFollow.setFillColor(ContextCompat.getColor(OwnerNotificationChartsActivity.this, R.color.chart_notif_follow));
        dataSetFollow.setDrawValues(false);
        dataSetFollow.setDrawFilled(true);
        dataSetFollow.setDrawCircles(false);
        dataSetFollow.setDrawCircleHole(false);
        dataSetFollow.setLineWidth(2f);
        if (status_id == null) {
            dataSetFollow.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        } else {
            dataSetFollow.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        }



      /*  LineDataSet dataSetPolls = new LineDataSet(pollEntry, getString(R.string.poll));
        dataSetPolls.setColor(ContextCompat.getColor(OwnerNotificationChartsActivity.this, R.color.chart_notif_poll));
        dataSetPolls.setValueTextSize(10f);
        dataSetPolls.setValueTextColor(ContextCompat.getColor(OwnerNotificationChartsActivity.this, R.color.chart_notif_poll));
        dataSetPolls.setFillColor(ContextCompat.getColor(OwnerNotificationChartsActivity.this, R.color.chart_notif_poll));
        dataSetPolls.setDrawValues(false);
        dataSetPolls.setDrawFilled(true);
        dataSetPolls.setDrawCircles(false);
        dataSetPolls.setDrawCircleHole(false);
        dataSetPolls.setLineWidth(2f);
        if( status_id == null) {
            dataSetPolls.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        }*/


        List<ILineDataSet> dataSets = new ArrayList<>();


        dataSets.add(dataSetBoosts);
        dataSets.add(dateSetFavorites);
        dataSets.add(dataSetMention);
        dataSets.add(dataSetFollow);
        //dataSets.add(dataSetPolls);

        //X axis
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelRotationAngle(45);
        xAxis.setTextSize(14f);

        //Legend
        Legend legend = chart.getLegend();
        legend.setTextSize(12f);
        legend.setXEntrySpace(15f);
        //Left axis
        YAxis leftAxis = chart.getAxis(YAxis.AxisDependency.LEFT);
        leftAxis.setTextSize(14f);
        leftAxis.setAxisMinimum(charts.getMinYVal());
        leftAxis.setDrawAxisLine(true);
        leftAxis.setDrawGridLines(true);
        leftAxis.setDrawLabels(true);
        //Remove right axis
        chart.getAxis(YAxis.AxisDependency.RIGHT).setEnabled(false);


        Description description = chart.getDescription();
        description.setEnabled(false);

        //Update colors
        switch (theme) {
            case Helper.THEME_LIGHT:
                xAxis.setTextColor(Color.BLACK);
                dataSetBoosts.setValueTextColor(Color.BLACK);
                dateSetFavorites.setValueTextColor(Color.BLACK);
                dataSetFollow.setValueTextColor(Color.BLACK);
                dataSetMention.setValueTextColor(Color.BLACK);
                //  dataSetPolls.setValueTextColor(Color.BLACK);

                legend.setTextColor(Color.BLACK);
                leftAxis.setTextColor(Color.BLACK);
                break;
            case Helper.THEME_DARK:
            case Helper.THEME_BLACK:
                int color = ContextCompat.getColor(OwnerNotificationChartsActivity.this, R.color.dark_text);
                xAxis.setTextColor(color);
                dataSetBoosts.setValueTextColor(color);
                dateSetFavorites.setValueTextColor(color);
                dataSetFollow.setValueTextColor(color);
                dataSetMention.setValueTextColor(color);
                //  dataSetPolls.setValueTextColor(color);
                legend.setTextColor(color);
                leftAxis.setTextColor(color);
        }

        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);

        xAxis.setValueFormatter(new MyXAxisValueFormatter());
        LineData data = new LineData(dataSets);
        chart.setData(data);
        chart.setVisibility(View.VISIBLE);
        loader.setVisibility(View.GONE);
        validate.setEnabled(true);
        chart.invalidate();
    }

    public class CustomMarkerView extends MarkerView {
        private TextView tvContent;

        public CustomMarkerView(Context context, int layoutResource) {
            super(context, layoutResource);
            tvContent = findViewById(R.id.tvContent);
            tvContent.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
        }

        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            Date date = new Date(((long) e.getX()));
            tvContent.setText(String.valueOf(Helper.shortDateToString(date) + " - " + (int) e.getY()));
            super.refreshContent(e, highlight);
        }

        private MPPointF mOffset;

        @Override
        public MPPointF getOffset() {
            if (mOffset == null) {
                mOffset = new MPPointF(-(getWidth() / 2), -getHeight());
            }
            return mOffset;
        }
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

    private void loadGraph(Date dateIni, Date dateEnd) {
        String dateInitString = Helper.shortDateToString(dateIni);
        String dateEndString = Helper.shortDateToString(dateEnd);

        settings_time_from.setText(dateInitString);
        settings_time_to.setText(dateEndString);
        chart.setVisibility(View.GONE);
        loader.setVisibility(View.VISIBLE);
        validate.setEnabled(false);
        new RetrieveNotificationChartsAsyncTask(OwnerNotificationChartsActivity.this, status_id, dateIni, dateEnd, OwnerNotificationChartsActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public class MyXAxisValueFormatter extends ValueFormatter {
        private DateFormat mDataFormat;
        private Date mDate;

        MyXAxisValueFormatter() {
            if (status_id == null) {
                this.mDataFormat = new SimpleDateFormat("dd.MM", Locale.getDefault());
            } else {
                this.mDataFormat = new SimpleDateFormat("hh'h'", Locale.getDefault());
            }
            this.mDate = new Date();
        }

        @Override
        public String getFormattedValue(float value) {
            return getDateString((long) value);
        }

        private String getDateString(long timestamp) {
            try {
                mDate.setTime(timestamp);
                return mDataFormat.format(mDate);
            } catch (Exception ex) {
                return "xx";
            }
        }
    }

}
