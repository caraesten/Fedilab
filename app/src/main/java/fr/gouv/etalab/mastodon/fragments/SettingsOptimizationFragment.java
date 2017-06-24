package fr.gouv.etalab.mastodon.fragments;
/* Copyright 2017 Thomas Schneider
 *
 * This file is a part of Mastodon Etalab for mastodon.etalab.gouv.fr
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Mastodon Etalab is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Thomas Schneider; if not,
 * see <http://www.gnu.org/licenses>. */
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import fr.gouv.etalab.mastodon.helper.Helper;
import mastodon.etalab.gouv.fr.mastodon.R;


/**
 * Created by Thomas on 25/04/2017.
 * Fragment for settings, yes I didn't use PreferenceFragment :)
 */
public class SettingsOptimizationFragment extends Fragment {


    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_settings_optimization, container, false);
        context = getContext();
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);



        //Status per page
        SeekBar statusSeekBar = (SeekBar) rootView.findViewById(R.id.set_toots_per_page);
        final TextView set_toots_page_value = (TextView) rootView.findViewById(R.id.set_toots_page_value);
        statusSeekBar.setMax(30);
        int tootPerPage = sharedpreferences.getInt(Helper.SET_TOOTS_PER_PAGE, 40);
        statusSeekBar.setProgress(tootPerPage-10);
        set_toots_page_value.setText(String.valueOf(tootPerPage));
        statusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int value = 10 + progress;
                set_toots_page_value.setText(String.valueOf(value));
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putInt(Helper.SET_TOOTS_PER_PAGE, value);
                editor.apply();
            }
        });

        //Accounts per page
        SeekBar accountsSeekBar = (SeekBar) rootView.findViewById(R.id.set_accounts_per_page);
        final TextView set_accounts_page_value = (TextView) rootView.findViewById(R.id.set_accounts_page_value);
        accountsSeekBar.setMax(30);
        int accountsPerPage = sharedpreferences.getInt(Helper.SET_ACCOUNTS_PER_PAGE, 40);
        accountsSeekBar.setProgress(accountsPerPage-10);
        set_accounts_page_value.setText(String.valueOf(accountsPerPage));
        accountsSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int value = 10 + progress;
                set_accounts_page_value.setText(String.valueOf(value));
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putInt(Helper.SET_ACCOUNTS_PER_PAGE, value);
                editor.apply();
            }
        });


        //Notifications per page
        SeekBar notificationsSeekBar = (SeekBar) rootView.findViewById(R.id.set_notifications_per_page);
        final TextView set_notifications_page_value = (TextView) rootView.findViewById(R.id.set_notifications_page_value);
        notificationsSeekBar.setMax(30);
        int notificationsPerPage = sharedpreferences.getInt(Helper.SET_NOTIFICATIONS_PER_PAGE, 40);
        notificationsSeekBar.setProgress(notificationsPerPage-10);
        set_notifications_page_value.setText(String.valueOf(notificationsPerPage));
        notificationsSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int value = 10 + progress;
                set_notifications_page_value.setText(String.valueOf(value));
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putInt(Helper.SET_NOTIFICATIONS_PER_PAGE, value);
                editor.apply();
            }
        });

        //Manage download of attachments
        RadioGroup radioGroup = (RadioGroup) rootView.findViewById(R.id.set_attachment_group);
        int attachmentAction = sharedpreferences.getInt(Helper.SET_ATTACHMENT_ACTION, Helper.ATTACHMENT_ALWAYS);
        switch (attachmentAction){
            case Helper.ATTACHMENT_ALWAYS:
                radioGroup.check(R.id.set_attachment_always);
                break;
            case Helper.ATTACHMENT_WIFI:
                radioGroup.check(R.id.set_attachment_wifi);
                break;
            case Helper.ATTACHMENT_ASK:
                radioGroup.check(R.id.set_attachment_ask);
                break;
        }
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId) {
                    case R.id.set_attachment_always:
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putInt(Helper.SET_ATTACHMENT_ACTION, Helper.ATTACHMENT_ALWAYS);
                        editor.apply();
                        break;
                    case R.id.set_attachment_wifi:
                        editor = sharedpreferences.edit();
                        editor.putInt(Helper.SET_ATTACHMENT_ACTION, Helper.ATTACHMENT_WIFI);
                        editor.apply();
                        break;
                    case R.id.set_attachment_ask:
                        editor = sharedpreferences.edit();
                        editor.putInt(Helper.SET_ATTACHMENT_ACTION, Helper.ATTACHMENT_ASK);
                        editor.apply();
                        break;
                }
            }
        });
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
