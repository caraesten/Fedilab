package fr.gouv.etalab.mastodon.fragments;
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

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import android.widget.TimePicker;
import android.widget.Toast;


import fr.gouv.etalab.mastodon.helper.Helper;
import mastodon.etalab.gouv.fr.mastodon.R;

import static fr.gouv.etalab.mastodon.helper.Helper.compareDate;


/**
 * Created by Thomas on 29/04/2017.
 * Fragment for settings, yes I didn't use PreferenceFragment :)
 */
public class SettingsNotificationsFragment extends Fragment {


    private Context context;
    private int style;
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_settings_notifications, container, false);
        context = getContext();
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);

        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if( theme == Helper.THEME_DARK){
            style = R.style.DialogDark;
        }else {
            style = R.style.Dialog;
        }
        boolean notif_follow = sharedpreferences.getBoolean(Helper.SET_NOTIF_FOLLOW, true);
        boolean notif_add = sharedpreferences.getBoolean(Helper.SET_NOTIF_ADD, true);
        boolean notif_ask = sharedpreferences.getBoolean(Helper.SET_NOTIF_ASK, true);
        boolean notif_mention = sharedpreferences.getBoolean(Helper.SET_NOTIF_MENTION, true);
        boolean notif_share = sharedpreferences.getBoolean(Helper.SET_NOTIF_SHARE, true);

        boolean notif_wifi = sharedpreferences.getBoolean(Helper.SET_WIFI_ONLY, false);
        boolean notif_silent = sharedpreferences.getBoolean(Helper.SET_NOTIF_SILENT, false);

        boolean notif_hometimeline = sharedpreferences.getBoolean(Helper.SET_NOTIF_HOMETIMELINE, true);

        final String time_from = sharedpreferences.getString(Helper.SET_TIME_FROM, "07:00");
        final String time_to = sharedpreferences.getString(Helper.SET_TIME_TO, "22:00");


        final CheckBox set_notif_follow = (CheckBox) rootView.findViewById(R.id.set_notif_follow);
        final CheckBox set_notif_follow_add = (CheckBox) rootView.findViewById(R.id.set_notif_follow_add);
        final CheckBox set_notif_follow_ask = (CheckBox) rootView.findViewById(R.id.set_notif_follow_ask);
        final CheckBox set_notif_follow_mention = (CheckBox) rootView.findViewById(R.id.set_notif_follow_mention);
        final CheckBox set_notif_follow_share = (CheckBox) rootView.findViewById(R.id.set_notif_follow_share);

        final CheckBox set_notif_hometimeline = (CheckBox) rootView.findViewById(R.id.set_notif_hometimeline);

        final SwitchCompat switchCompatWIFI = (SwitchCompat) rootView.findViewById(R.id.set_wifi_only);
        final SwitchCompat switchCompatSilent = (SwitchCompat) rootView.findViewById(R.id.set_silence);

        final Button settings_time_from = (Button) rootView.findViewById(R.id.settings_time_from);
        final Button settings_time_to = (Button) rootView.findViewById(R.id.settings_time_to);

        settings_time_from.setText(time_from);
        settings_time_to.setText(time_to);


        settings_time_from.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] datetime = time_from.split(":");
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), style, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        String hours = (String.valueOf(hourOfDay).length() == 1) ? "0"+String.valueOf(hourOfDay):String.valueOf(hourOfDay);
                        String minutes = (String.valueOf(minute).length() == 1) ? "0"+String.valueOf(minute):String.valueOf(minute);
                        String newDate = hours + ":" + minutes;
                        if( compareDate(context, newDate, false) ) {
                            editor.putString(Helper.SET_TIME_FROM, newDate);
                            editor.apply();
                            settings_time_from.setText(newDate);
                        }else {
                            String ateRef = sharedpreferences.getString(Helper.SET_TIME_TO, "22:00");
                            Toast.makeText(context, context.getString(R.string.settings_time_lower, ateRef), Toast.LENGTH_LONG).show();
                        }
                    }
                }, Integer.valueOf(datetime[0]), Integer.valueOf(datetime[1]), true);
                timePickerDialog.setTitle(context.getString(R.string.settings_hour_init));
                timePickerDialog.show();
            }
        });

        settings_time_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] datetime = time_to.split(":");
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),style, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        String hours = (String.valueOf(hourOfDay).length() == 1) ? "0" + String.valueOf(hourOfDay) : String.valueOf(hourOfDay);
                        String minutes = (String.valueOf(minute).length() == 1) ? "0" + String.valueOf(minute) : String.valueOf(minute);
                        String newDate = hours + ":" + minutes;
                        if (compareDate(context, newDate, true)) {
                            editor.putString(Helper.SET_TIME_TO, newDate);
                            editor.apply();
                            settings_time_to.setText(newDate);
                        } else {
                            String ateRef = sharedpreferences.getString(Helper.SET_TIME_FROM, "07:00");
                            Toast.makeText(context, context.getString(R.string.settings_time_greater, ateRef), Toast.LENGTH_LONG).show();
                        }
                    }
                }, Integer.valueOf(datetime[0]), Integer.valueOf(datetime[1]), true);
                timePickerDialog.setTitle(context.getString(R.string.settings_hour_end));
                timePickerDialog.show();
            }
        });

        set_notif_follow.setChecked(notif_follow);
        set_notif_follow_add.setChecked(notif_add);
        set_notif_follow_ask.setChecked(notif_ask);
        set_notif_follow_mention.setChecked(notif_mention);
        set_notif_follow_share.setChecked(notif_share);
        set_notif_hometimeline.setChecked(notif_hometimeline);

        switchCompatWIFI.setChecked(notif_wifi);
        switchCompatSilent.setChecked(notif_silent);

        set_notif_hometimeline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_NOTIF_HOMETIMELINE, set_notif_hometimeline.isChecked());
                editor.apply();
            }
        });
        set_notif_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_NOTIF_FOLLOW, set_notif_follow.isChecked());
                editor.apply();
            }
        });
        set_notif_follow_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_NOTIF_ADD, set_notif_follow_add.isChecked());
                editor.apply();
            }
        });
        set_notif_follow_ask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_NOTIF_ASK, set_notif_follow_ask.isChecked());
                editor.apply();
            }
        });
        set_notif_follow_mention.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_NOTIF_MENTION, set_notif_follow_mention.isChecked());
                editor.apply();
            }
        });
        set_notif_follow_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_NOTIF_SHARE, set_notif_follow_share.isChecked());
                editor.apply();
            }
        });


        switchCompatWIFI.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save the state here
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_WIFI_ONLY, isChecked);
                editor.apply();

            }
        });
        switchCompatSilent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Save the state here
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_NOTIF_SILENT, isChecked);
                editor.apply();
            }
        });
        if( theme == Helper.THEME_LIGHT) {
            settings_time_from.setTextColor(ContextCompat.getColor(context, R.color.white));
            settings_time_to.setTextColor(ContextCompat.getColor(context, R.color.white));
        }
        return rootView;
    }



    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }




}
