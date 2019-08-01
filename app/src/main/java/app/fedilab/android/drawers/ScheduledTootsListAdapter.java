package app.fedilab.android.drawers;
/* Copyright 2017 Thomas Schneider
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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import app.fedilab.android.client.API;
import app.fedilab.android.client.Entities.Error;
import app.fedilab.android.client.Entities.Status;
import app.fedilab.android.client.Entities.StoredStatus;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.jobs.ApplicationJob;
import app.fedilab.android.jobs.ScheduledBoostsSyncJob;
import app.fedilab.android.jobs.ScheduledTootsSyncJob;
import app.fedilab.android.sqlite.BoostScheduleDAO;
import app.fedilab.android.sqlite.Sqlite;
import app.fedilab.android.sqlite.StatusStoredDAO;
import es.dmoral.toasty.Toasty;
import app.fedilab.android.R;
import app.fedilab.android.activities.MainActivity;
import app.fedilab.android.activities.ShowConversationActivity;
import app.fedilab.android.activities.TootActivity;
import app.fedilab.android.asynctasks.PostActionAsyncTask;
import app.fedilab.android.fragments.DisplayScheduledTootsFragment;
import app.fedilab.android.interfaces.OnPostActionInterface;

import static app.fedilab.android.helper.Helper.changeDrawableColor;


/**
 * Created by Thomas on 16/07/2017.
 * Adapter for scheduled toots
 */
public class ScheduledTootsListAdapter extends BaseAdapter implements OnPostActionInterface {

    private Context context;
    private List<StoredStatus> storedStatuses;
    private LayoutInflater layoutInflater;
    private ScheduledTootsListAdapter scheduledTootsListAdapter;
    private RelativeLayout textviewNoAction;
    private DisplayScheduledTootsFragment.typeOfSchedule type;

    public ScheduledTootsListAdapter(Context context, DisplayScheduledTootsFragment.typeOfSchedule type, List<StoredStatus> storedStatuses, RelativeLayout textviewNoAction){
        this.context = context;
        this.storedStatuses = storedStatuses;
        layoutInflater = LayoutInflater.from(this.context);
        scheduledTootsListAdapter = this;
        this.textviewNoAction = textviewNoAction;
        this.type = type;
    }



    @Override
    public int getCount() {
        return storedStatuses.size();
    }

    @Override
    public StoredStatus getItem(int position) {
        return storedStatuses.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final StoredStatus storedStatus = storedStatuses.get(position);
        final Status status = storedStatuses.get(position).getStatus();
        final ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.drawer_scheduled_toot, parent, false);
            holder = new ViewHolder();
            holder.scheduled_toot_pp= convertView.findViewById(R.id.scheduled_toot_pp);
            holder.scheduled_toot_title = convertView.findViewById(R.id.scheduled_toot_title);
            holder.scheduled_toot_date_creation = convertView.findViewById(R.id.scheduled_toot_date_creation);
            holder.scheduled_toot_media_count = convertView.findViewById(R.id.scheduled_toot_media_count);
            holder.scheduled_toot_failed = convertView.findViewById(R.id.scheduled_toot_failed);
            holder.scheduled_toot_delete = convertView.findViewById(R.id.scheduled_toot_delete);
            holder.scheduled_toot_privacy = convertView.findViewById(R.id.scheduled_toot_privacy);
            holder.scheduled_toot_date = convertView.findViewById(R.id.scheduled_toot_date);
            holder.scheduled_toot_container = convertView.findViewById(R.id.scheduled_toot_container);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        final int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if( theme == Helper.THEME_BLACK) {
            Helper.changeDrawableColor(context, R.drawable.ic_cancel,R.color.action_black);
            Helper.changeDrawableColor(context, R.drawable.ic_public,R.color.action_black);
            Helper.changeDrawableColor(context, R.drawable.ic_lock_open,R.color.action_black);
            Helper.changeDrawableColor(context, R.drawable.ic_lock_outline,R.color.action_black);
            Helper.changeDrawableColor(context, R.drawable.ic_mail_outline,R.color.action_black);
        }else if( theme == Helper.THEME_DARK){
            Helper.changeDrawableColor(context, R.drawable.ic_cancel,R.color.action_dark);
            Helper.changeDrawableColor(context, R.drawable.ic_public,R.color.action_dark);
            Helper.changeDrawableColor(context, R.drawable.ic_lock_open,R.color.action_dark);
            Helper.changeDrawableColor(context, R.drawable.ic_lock_outline,R.color.action_dark);
            Helper.changeDrawableColor(context, R.drawable.ic_mail_outline,R.color.action_dark);
        }else {
            Helper.changeDrawableColor(context, R.drawable.ic_cancel,R.color.action_light);
            Helper.changeDrawableColor(context, R.drawable.ic_public,R.color.action_light);
            Helper.changeDrawableColor(context, R.drawable.ic_lock_open,R.color.action_light);
            Helper.changeDrawableColor(context, R.drawable.ic_lock_outline,R.color.action_light);
            Helper.changeDrawableColor(context, R.drawable.ic_mail_outline,R.color.action_light);
        }

        switch (status.getVisibility()) {
            case "public":
                holder.scheduled_toot_privacy.setImageResource(R.drawable.ic_public);
                break;
            case "unlisted":
                holder.scheduled_toot_privacy.setImageResource(R.drawable.ic_lock_open);
                break;
            case "private":
                holder.scheduled_toot_privacy.setImageResource(R.drawable.ic_lock_outline);
                break;
            case "direct":
                holder.scheduled_toot_privacy.setImageResource(R.drawable.ic_mail_outline);
                break;
        }
        final SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        int style;
        if (theme == Helper.THEME_DARK) {
            style = R.style.DialogDark;
        } else if (theme == Helper.THEME_BLACK){
            style = R.style.DialogBlack;
        }else {
            style = R.style.Dialog;
        }
        //Delete scheduled toot
        holder.scheduled_toot_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context, style);

                String message;
                if( type == DisplayScheduledTootsFragment.typeOfSchedule.TOOT)
                    message = status.getContent() + '\n' + Helper.dateToString(storedStatus.getCreation_date());
                else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        message = Html.fromHtml(status.getContent(), Html.FROM_HTML_MODE_LEGACY).toString();
                    else
                        //noinspection deprecation
                        message = Html.fromHtml(status.getContent()).toString();
                    message += '\n' + Helper.dateToString(storedStatus.getScheduled_date());
                }
                builder.setMessage(message);
                builder.setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(R.string.remove_scheduled)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if( type != DisplayScheduledTootsFragment.typeOfSchedule.SERVER) {
                                    if (type == DisplayScheduledTootsFragment.typeOfSchedule.TOOT)
                                        new StatusStoredDAO(context, db).remove(storedStatus.getId());
                                    else if (type == DisplayScheduledTootsFragment.typeOfSchedule.BOOST)
                                        new BoostScheduleDAO(context, db).remove(storedStatus.getId());
                                    storedStatuses.remove(storedStatus);
                                    scheduledTootsListAdapter.notifyDataSetChanged();
                                    if (storedStatuses.size() == 0 && textviewNoAction != null && textviewNoAction.getVisibility() == View.GONE)
                                        textviewNoAction.setVisibility(View.VISIBLE);
                                    try {
                                        //Cancel the job
                                        ApplicationJob.cancelJob(storedStatus.getJobId());
                                    } catch (Exception ignored) {
                                    }
                                }else{
                                    new PostActionAsyncTask(context, API.StatusAction.DELETESCHEDULED, storedStatus, ScheduledTootsListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                    storedStatuses.remove(storedStatus);
                                    scheduledTootsListAdapter.notifyDataSetChanged();
                                    if (storedStatuses.size() == 0 && textviewNoAction != null && textviewNoAction.getVisibility() == View.GONE)
                                        textviewNoAction.setVisibility(View.VISIBLE);
                                }
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

        if( type != DisplayScheduledTootsFragment.typeOfSchedule.SERVER) {
            if (storedStatus.getJobId() > 0) {
                holder.scheduled_toot_failed.setVisibility(View.GONE);
            } else {
                holder.scheduled_toot_failed.setVisibility(View.VISIBLE);
            }
        }
        holder.scheduled_toot_media_count.setText(context.getString(R.string.media_count, status.getMedia_attachments().size()));
        holder.scheduled_toot_date_creation.setText(Helper.dateToString(storedStatus.getCreation_date()));
        holder.scheduled_toot_date.setText(Helper.dateToString(storedStatus.getScheduled_date()));
        if( type == DisplayScheduledTootsFragment.typeOfSchedule.BOOST){
            holder.scheduled_toot_media_count.setVisibility(View.GONE);
            holder.scheduled_toot_date_creation.setVisibility(View.GONE);
            holder.scheduled_toot_privacy.setVisibility(View.GONE);
            Helper.loadGiF(context, storedStatus.getStatus().getAccount().getAvatar_static(), storedStatus.getStatus().getAccount().getAvatar(), holder.scheduled_toot_pp);
        }else {
            holder.scheduled_toot_pp.setVisibility(View.GONE);
        }
        holder.scheduled_toot_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context, style);
                LayoutInflater inflater = ((MainActivity)context).getLayoutInflater();
                @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.datetime_picker, null);
                SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
                int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);

                if( theme == Helper.THEME_BLACK){
                    Helper.changeDrawableColor(context, R.drawable.ic_skip_previous,R.color.action_black);
                    Helper.changeDrawableColor(context, R.drawable.ic_skip_next,R.color.action_black);
                    Helper.changeDrawableColor(context, R.drawable.ic_check,R.color.action_black);
                }else if( theme == Helper.THEME_DARK){
                    Helper.changeDrawableColor(context, R.drawable.ic_skip_previous,R.color.action_dark);
                    Helper.changeDrawableColor(context, R.drawable.ic_skip_next,R.color.action_dark);
                    Helper.changeDrawableColor(context, R.drawable.ic_check,R.color.action_dark);
                }else {
                    Helper.changeDrawableColor(context, R.drawable.ic_skip_previous,R.color.action_light);
                    Helper.changeDrawableColor(context, R.drawable.ic_skip_next,R.color.action_light);
                    Helper.changeDrawableColor(context, R.drawable.ic_check,R.color.action_light);
                }
                dialogBuilder.setView(dialogView);
                final AlertDialog alertDialog = dialogBuilder.create();

                final DatePicker datePicker = dialogView.findViewById(R.id.date_picker);
                final TimePicker timePicker = dialogView.findViewById(R.id.time_picker);
                if (android.text.format.DateFormat.is24HourFormat(context))
                    timePicker.setIs24HourView(true);
                Button date_time_cancel = dialogView.findViewById(R.id.date_time_cancel);
                final ImageButton date_time_previous = dialogView.findViewById(R.id.date_time_previous);
                final ImageButton date_time_next = dialogView.findViewById(R.id.date_time_next);
                final ImageButton date_time_set = dialogView.findViewById(R.id.date_time_set);

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
                            Toasty.error(context, context.getString(R.string.toot_scheduled_date), Toast.LENGTH_LONG).show();
                        }else {
                            //Schedules the toot to the new date
                            if( type != DisplayScheduledTootsFragment.typeOfSchedule.SERVER) {
                                try {
                                    //Removes the job
                                    ApplicationJob.cancelJob(storedStatus.getJobId());
                                    //Replace it by the new one
                                    StoredStatus storedStatusnew = null;
                                    if (type == DisplayScheduledTootsFragment.typeOfSchedule.TOOT) {
                                        ScheduledTootsSyncJob.schedule(context, storedStatus.getId(), time);
                                        storedStatusnew = new StatusStoredDAO(context, db).getStatus(storedStatus.getId());
                                    } else if (type == DisplayScheduledTootsFragment.typeOfSchedule.BOOST) {
                                        ScheduledBoostsSyncJob.scheduleUpdate(context, storedStatus.getId(), time);
                                        storedStatusnew = new BoostScheduleDAO(context, db).getStatus(storedStatus.getId());
                                    }
                                    //Date displayed is changed
                                    assert storedStatusnew != null;
                                    storedStatus.setScheduled_date(storedStatusnew.getScheduled_date());
                                    scheduledTootsListAdapter.notifyDataSetChanged();
                                    //Notifiy all is ok
                                    if (type == DisplayScheduledTootsFragment.typeOfSchedule.TOOT)
                                        Toasty.success(context, context.getString(R.string.toot_scheduled), Toast.LENGTH_LONG).show();
                                    else
                                        Toasty.success(context, context.getString(R.string.boost_scheduled), Toast.LENGTH_LONG).show();
                                } catch (Exception ignored) {
                                }
                            }else{
                                int offset = TimeZone.getDefault().getRawOffset();
                                calendar.add(Calendar.MILLISECOND, -offset);
                                final String date = Helper.dateToString(new Date(calendar.getTimeInMillis()));
                                storedStatus.getStatus().setScheduled_at(date);
                                new PostActionAsyncTask(context, API.StatusAction.UPDATESERVERSCHEDULE, storedStatus, ScheduledTootsListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                Toasty.success(context, context.getString(R.string.boost_scheduled), Toast.LENGTH_LONG).show();
                            }
                            alertDialog.dismiss();
                        }
                    }
                });
                alertDialog.show();
            }
        });
        if( type == DisplayScheduledTootsFragment.typeOfSchedule.TOOT)
            holder.scheduled_toot_title.setText(status.getContent());
        else if( type == DisplayScheduledTootsFragment.typeOfSchedule.BOOST){
            Spanned message;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                message = Html.fromHtml(status.getContent(), Html.FROM_HTML_MODE_LEGACY);
            else
                //noinspection deprecation
                message = Html.fromHtml(status.getContent());
            holder.scheduled_toot_title.setText(message, TextView.BufferType.SPANNABLE);
        }



        if( type == DisplayScheduledTootsFragment.typeOfSchedule.TOOT)
            holder.scheduled_toot_container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intentToot = new Intent(context, TootActivity.class);
                    Bundle b = new Bundle();
                    b.putLong("restored", storedStatus.getId());
                    b.putBoolean("restoredScheduled", true);
                    intentToot.putExtras(b);
                    context.startActivity(intentToot);
                }
            });
        else if( type == DisplayScheduledTootsFragment.typeOfSchedule.BOOST)
            holder.scheduled_toot_container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intentToot = new Intent(context, ShowConversationActivity.class);
                    Bundle b = new Bundle();
                    b.putParcelable("status", storedStatus.getStatus());
                    intentToot.putExtras(b);
                    context.startActivity(intentToot);
                }
            });
        else if( type == DisplayScheduledTootsFragment.typeOfSchedule.SERVER)
            holder.scheduled_toot_container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intentToot = new Intent(context, TootActivity.class);
                    Bundle b = new Bundle();
                    if( storedStatus.getStatus().getSpoiler_text().equals("null"))
                        storedStatus.getStatus().setSpoiler_text("");
                    b.putParcelable("storedStatus", storedStatus);
                    intentToot.putExtras(b);
                    context.startActivity(intentToot);
                }
            });
        return convertView;
    }

    @Override
    public void onPostAction(int statusCode, API.StatusAction statusAction, String userId, Error error) {

    }

    private class ViewHolder {
        LinearLayout scheduled_toot_container;
        TextView scheduled_toot_title;
        TextView scheduled_toot_date_creation;
        TextView scheduled_toot_media_count;
        TextView scheduled_toot_failed;
        ImageView scheduled_toot_delete;
        ImageView scheduled_toot_privacy;
        ImageView scheduled_toot_pp;
        Button scheduled_toot_date;
    }

}