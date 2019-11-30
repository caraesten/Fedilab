package app.fedilab.android.drawers;
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

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import app.fedilab.android.R;
import app.fedilab.android.activities.HashTagActivity;
import app.fedilab.android.client.Entities.Trends;
import app.fedilab.android.client.Entities.TrendsHistory;


/**
 * Created by Thomas on 30/11/2019.
 * Adapter for Trends results
 */
public class TrendsAdapter extends BaseAdapter {

    private List<Trends> trends;
    private Context context;
    private LayoutInflater layoutInflater;

    public TrendsAdapter(Context context, List<Trends> trends) {
        this.context = context;
        this.trends = (trends != null) ? trends : new ArrayList<>();
        layoutInflater = LayoutInflater.from(context);
    }


    @Override
    public int getCount() {
        return trends.size();
    }

    @Override
    public Object getItem(int position) {
        return trends.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {


        final String tag = (String) getItem(position);
        final ViewHolderTag holder;
        View v = convertView;
        if (v == null) {
            v = layoutInflater.inflate(R.layout.drawer_tag_trends, parent, false);
            holder = new ViewHolderTag();
            holder.tag_name = v.findViewById(R.id.tag_name);
            holder.tag_stats = v.findViewById(R.id.tag_stats);
            holder.count = v.findViewById(R.id.count);
            holder.chart = v.findViewById(R.id.chart);
            v.setTag(holder);
        } else {
            holder = (ViewHolderTag) v.getTag();
        }
        Trends trend = trends.get(position);
        List<TrendsHistory> trendsHistory = trend.getTrendsHistory();
        int people = 0;
        int days = 0;
        int uses = 0;

        LinkedHashMap<Long, Integer> tendency = new LinkedHashMap<>();
        for(TrendsHistory _th : trendsHistory) {
            people += _th.getAccounts();
            days ++;
            uses += _th.getUses();
            tendency.put(_th.getDays(), _th.getUses());
        }
        people = people / days;
        uses = uses / days;
        holder.count.setText(uses);
        holder.tag_stats.setText(context.getString(R.string.talking_about, people));
        holder.tag_name.setText(String.format("#%s", tag));
        holder.tag_name.setPaintFlags(holder.tag_name.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        holder.tag_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, HashTagActivity.class);
                Bundle b = new Bundle();
                b.putString("tag", tag.trim());
                intent.putExtras(b);
                context.startActivity(intent);
            }
        });


        List<Entry> trendsEntry = new ArrayList<>();

        Iterator it = tendency.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            trendsEntry.add(new Entry((long) pair.getKey(), (int) pair.getValue()));
            it.remove();
        }
        LineDataSet dataSetBoosts = new LineDataSet(trendsEntry,context.getString(R.string.trending));
        dataSetBoosts.setColor(ContextCompat.getColor(context, R.color.colorAccent));
        dataSetBoosts.setValueTextSize(10f);
        dataSetBoosts.setValueTextColor(ContextCompat.getColor(context, R.color.colorAccent));
        dataSetBoosts.setFillColor(ContextCompat.getColor(context, R.color.colorAccent));
        dataSetBoosts.setDrawValues(false);
        dataSetBoosts.setDrawFilled(true);
        dataSetBoosts.setDrawCircles(false);
        dataSetBoosts.setDrawCircleHole(false);
        dataSetBoosts.setLineWidth(2f);
        dataSetBoosts.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        List<ILineDataSet> dataSets = new ArrayList<>();


        dataSets.add(dataSetBoosts);
        //X axis
        XAxis xAxis = holder.chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelRotationAngle(45);
        xAxis.setTextSize(14f);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);

        LineData data = new LineData(dataSets);
        holder.chart.setData(data);
        holder.chart.invalidate();
        return v;
    }



    private class ViewHolderTag {
        TextView tag_name;
        TextView tag_stats;
        TextView count;
        LineChart chart;
    }
}