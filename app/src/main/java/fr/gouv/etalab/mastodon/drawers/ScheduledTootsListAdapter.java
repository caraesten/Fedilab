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
 * You should have received a copy of the GNU General Public License along with Mastalab; if not,
 * see <http://www.gnu.org/licenses>. */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import fr.gouv.etalab.mastodon.activities.MainActivity;
import fr.gouv.etalab.mastodon.activities.TootActivity;
import fr.gouv.etalab.mastodon.client.Entities.Application;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.client.Entities.StoredStatus;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.jobs.ApplicationJob;
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
    private RelativeLayout textviewNoAction;
    private int style;

    public ScheduledTootsListAdapter(Context context, List<StoredStatus> storedStatuses, RelativeLayout textviewNoAction){
        this.context = context;
        this.storedStatuses = storedStatuses;
        layoutInflater = LayoutInflater.from(this.context);
        scheduledTootsListAdapter = this;
        this.textviewNoAction = textviewNoAction;
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
        final int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
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

        //Delete scheduled toot
        holder.scheduled_toot_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( theme == Helper.THEME_DARK){
                    style = R.style.DialogDark;
                }else {
                    style = R.style.Dialog;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(context, style);
                builder.setMessage(status.getContent() + '\n' + Helper.dateToString(context, storedStatus.getCreation_date()));
                builder.setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(R.string.remove_scheduled)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new StatusStoredDAO(context, db).remove(storedStatus.getId());
                                storedStatuses.remove(storedStatus);
                                scheduledTootsListAdapter.notifyDataSetChanged();
                                if( storedStatuses.size() == 0 && textviewNoAction != null && textviewNoAction.getVisibility() == View.GONE)
                                        textviewNoAction.setVisibility(View.VISIBLE);
                                try {
                                    //Cancel the job
                                    ApplicationJob.cancelJob(storedStatus.getJobId());
                                }catch (Exception ignored){}
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

        if (storedStatus.getJobId() > 0) {
            holder.scheduled_toot_failed.setVisibility(View.GONE);
        }else {
            holder.scheduled_toot_failed.setVisibility(View.VISIBLE);
        }
        holder.scheduled_toot_media_count.setText(context.getString(R.string.media_count, status.getMedia_attachments().size()));
        holder.scheduled_toot_date_creation.setText(Helper.dateToString(context, storedStatus.getCreation_date()));
        holder.scheduled_toot_date.setText(Helper.dateToString(context, storedStatus.getScheduled_date()));
        holder.scheduled_toot_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( theme == Helper.THEME_DARK){
                    style = R.style.DialogDark;
                }else {
                    style = R.style.Dialog;
                }
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context, style);
                LayoutInflater inflater = ((MainActivity)context).getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.datetime_picker, null);
                SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
                int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
                if( theme == Helper.THEME_DARK){
                    changeDrawableColor(context, R.drawable.ic_skip_previous,R.color.dark_text);
                    changeDrawableColor(context, R.drawable.ic_skip_next,R.color.dark_text);
                    changeDrawableColor(context, R.drawable.ic_check,R.color.dark_text);
                }else {
                    changeDrawableColor(context, R.drawable.ic_skip_previous,R.color.black);
                    changeDrawableColor(context, R.drawable.ic_skip_next,R.color.black);
                    changeDrawableColor(context, R.drawable.ic_check,R.color.black);
                }
                dialogBuilder.setView(dialogView);
                final AlertDialog alertDialog = dialogBuilder.create();

                final DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.date_picker);
                final TimePicker timePicker = (TimePicker) dialogView.findViewById(R.id.time_picker);
                Button date_time_cancel = (Button) dialogView.findViewById(R.id.date_time_cancel);
                final ImageButton date_time_previous = (ImageButton) dialogView.findViewById(R.id.date_time_previous);
                final ImageButton date_time_next = (ImageButton) dialogView.findViewById(R.id.date_time_next);
                final ImageButton date_time_set = (ImageButton) dialogView.findViewById(R.id.date_time_set);

                //Buttons management
                date_time_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });
                date_time_next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        datePicker.setVisibility(View.GONE);
                        timePicker.setVisibility(View.VISIBLE);
                        date_time_previous.setVisibility(View.VISIBLE);
                        date_time_next.setVisibility(View.GONE);
                        date_time_set.setVisibility(View.VISIBLE);
                    }
                });
                date_time_previous.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        datePicker.setVisibility(View.VISIBLE);
                        timePicker.setVisibility(View.GONE);
                        date_time_previous.setVisibility(View.GONE);
                        date_time_next.setVisibility(View.VISIBLE);
                        date_time_set.setVisibility(View.GONE);
                    }
                });
                date_time_set.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int hour, minute;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            hour = timePicker.getHour();
                            minute = timePicker.getMinute();
                        }else {
                            //noinspection deprecation
                            hour = timePicker.getCurrentHour();
                            //noinspection deprecation
                            minute = timePicker.getCurrentMinute();
                        }
                        Calendar calendar = new GregorianCalendar(datePicker.getYear(),
                                datePicker.getMonth(),
                                datePicker.getDayOfMonth(),
                                hour,
                                minute);
                        long time = calendar.getTimeInMillis();
                        if( (time - new Date().getTime()) < 60000 ){
                            Toast.makeText(context, R.string.toot_scheduled_date, Toast.LENGTH_LONG).show();
                        }else {
                            //Schedules the toot to the new date
                            try {
                                //Removes the job
                                ApplicationJob.cancelJob(storedStatus.getJobId());
                                //Replace it by the new one
                                ScheduledTootsSyncJob.schedule(context, storedStatus.getId(), time);
                                StoredStatus storedStatusnew = new StatusStoredDAO(context, db).getStatus(storedStatus.getId());
                                //Date displayed is changed
                                storedStatus.setScheduled_date(storedStatusnew.getScheduled_date());
                                scheduledTootsListAdapter.notifyDataSetChanged();
                                //Notifiy all is ok
                                Toast.makeText(context,R.string.toot_scheduled, Toast.LENGTH_LONG).show();
                            }catch (Exception ignored){}
                            alertDialog.dismiss();
                        }
                    }
                });
                alertDialog.show();
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