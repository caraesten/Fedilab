package fr.gouv.etalab.mastodon.drawers;
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
 * You should have received a copy of the GNU General Public License along with Thomas Schneider; if not,
 * see <http://www.gnu.org/licenses>. */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import java.util.List;
import java.util.Set;

import fr.gouv.etalab.mastodon.activities.TootActivity;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.client.Entities.StoredStatus;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.jobs.ScheduledTootsSyncJob;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import fr.gouv.etalab.mastodon.sqlite.StatusStoredDAO;
import mastodon.etalab.gouv.fr.mastodon.R;

import static fr.gouv.etalab.mastodon.helper.Helper.changeDrawableColor;


/**
 * Created by Thomas on 16/07/2017.
 * Adapter for scheduled toots
 */
public class ScheduledTootsListAdapter extends BaseAdapter  {

    private Context context;
    private List<StoredStatus> storedStatuses;
    private LayoutInflater layoutInflater;
    private ScheduledTootsListAdapter scheduledTootsListAdapter;


    public ScheduledTootsListAdapter(Context context, List<StoredStatus> storedStatuses){
        this.context = context;
        this.storedStatuses = storedStatuses;
        layoutInflater = LayoutInflater.from(this.context);
        scheduledTootsListAdapter = this;
    }



    @Override
    public int getCount() {
        return storedStatuses.size();
    }

    @Override
    public Object getItem(int position) {
        return storedStatuses.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final StoredStatus storedStatus = storedStatuses.get(position);
        final ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.drawer_scheduled_toot, parent, false);
            holder = new ViewHolder();
            holder.scheduled_toot_container = (CardView) convertView.findViewById(R.id.scheduled_toot_container);
            holder.scheduled_toot_title = (TextView) convertView.findViewById(R.id.scheduled_toot_title);
            holder.scheduled_toot_date_creation = (TextView) convertView.findViewById(R.id.scheduled_toot_date_creation);
            holder.scheduled_toot_media_count = (TextView) convertView.findViewById(R.id.scheduled_toot_media_count);
            holder.scheduled_toot_failed = (TextView) convertView.findViewById(R.id.scheduled_toot_failed);
            holder.scheduled_toot_delete = (ImageView) convertView.findViewById(R.id.scheduled_toot_delete);
            holder.scheduled_toot_privacy = (ImageView) convertView.findViewById(R.id.scheduled_toot_privacy);
            holder.scheduled_toot_date = (Button) convertView.findViewById(R.id.scheduled_toot_date);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if( theme == Helper.THEME_DARK){
            changeDrawableColor(context, R.drawable.ic_cancel,R.color.dark_text);
            changeDrawableColor(context, R.drawable.ic_action_globe,R.color.dark_text);
            changeDrawableColor(context, R.drawable.ic_action_lock_open,R.color.dark_text);
            changeDrawableColor(context, R.drawable.ic_action_lock_closed,R.color.dark_text);
            changeDrawableColor(context, R.drawable.ic_local_post_office,R.color.dark_text);
        }else {
            changeDrawableColor(context, R.drawable.ic_cancel,R.color.black);
            changeDrawableColor(context, R.drawable.ic_action_globe,R.color.black);
            changeDrawableColor(context, R.drawable.ic_action_lock_open,R.color.black);
            changeDrawableColor(context, R.drawable.ic_action_lock_closed,R.color.black);
            changeDrawableColor(context, R.drawable.ic_local_post_office,R.color.black);
        }

        final Status status = storedStatus.getStatus();

        switch (status.getVisibility()) {
            case "public":
                holder.scheduled_toot_privacy.setImageResource(R.drawable.ic_action_globe);
                break;
            case "unlisted":
                holder.scheduled_toot_privacy.setImageResource(R.drawable.ic_action_lock_open);
                break;
            case "private":
                holder.scheduled_toot_privacy.setImageResource(R.drawable.ic_action_lock_closed);
                break;
            case "direct":
                holder.scheduled_toot_privacy.setImageResource(R.drawable.ic_local_post_office);
                break;
        }
        final SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();

        final Job job = JobManager.instance().getJob(storedStatus.getJobId());

        //Delete scheduled toot
        holder.scheduled_toot_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(status.getContent() + '\n' + Helper.dateToString(context, storedStatus.getCreation_date()));
                builder.setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(R.string.remove_scheduled)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new StatusStoredDAO(context, db).remove(storedStatus.getId());
                                storedStatuses.remove(storedStatus);
                                scheduledTootsListAdapter.notifyDataSetChanged();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }

                        })
                        .show();
            }
        });
        if( job == null){
            holder.scheduled_toot_failed.setVisibility(View.GONE);
        }else {
            holder.scheduled_toot_failed.setVisibility(View.GONE);
        }
        holder.scheduled_toot_media_count.setText(context.getString(R.string.media_count, status.getMedia_attachments().size()));
        holder.scheduled_toot_date_creation.setText(Helper.dateToString(context, storedStatus.getCreation_date()));
        holder.scheduled_toot_date.setText(Helper.dateToString(context, storedStatus.getScheduled_date()));
        holder.scheduled_toot_date_creation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        holder.scheduled_toot_title.setText(status.getContent());



        holder.scheduled_toot_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentToot = new Intent(context, TootActivity.class);
                Bundle b = new Bundle();
                b.putLong("restored", storedStatus.getId());
                intentToot.putExtras(b);
                context.startActivity(intentToot);
            }
        });

        return convertView;
    }


    private class ViewHolder {
        CardView scheduled_toot_container;
        TextView scheduled_toot_title;
        TextView scheduled_toot_date_creation;
        TextView scheduled_toot_media_count;
        TextView scheduled_toot_failed;
        ImageView scheduled_toot_delete;
        ImageView scheduled_toot_privacy;
        Button scheduled_toot_date;
    }

}