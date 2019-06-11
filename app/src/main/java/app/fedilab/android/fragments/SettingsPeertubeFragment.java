package app.fedilab.android.fragments;
/* Copyright 2018 Thomas Schneider
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
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;

import app.fedilab.android.helper.Helper;
import app.fedilab.android.R;


/**
 * Created by Thomas on 06/01/2019.
 * Fragment for peertube settings
 */
public class SettingsPeertubeFragment extends Fragment {


    private Context context;
    private int count1;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_peertube_settings, container, false);
        context = getContext();
        assert context != null;
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);

        int videoMode = sharedpreferences.getInt(Helper.SET_VIDEO_MODE, Helper.VIDEO_MODE_DIRECT);


        //Video mode
        final Spinner video_mode_spinner = rootView.findViewById(R.id.set_video_mode);
        ArrayAdapter<CharSequence> video_mode_spinnerAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.settings_video_mode, android.R.layout.simple_spinner_item);
        video_mode_spinner.setAdapter(video_mode_spinnerAdapter);
        if (videoMode == Helper.VIDEO_MODE_TORRENT)
            videoMode = Helper.VIDEO_MODE_DIRECT;
        int positionVideoMode = 0;
        if( videoMode == Helper.VIDEO_MODE_DIRECT)
            positionVideoMode = 1;
        video_mode_spinner.setSelection(positionVideoMode);
        video_mode_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if( count1 > 0 ) {
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    switch (position) {
                        /*case 0:
                            editor.putInt(Helper.SET_VIDEO_MODE, Helper.VIDEO_MODE_TORRENT);
                            editor.apply();
                            break;*/
                        case 0:
                            editor.putInt(Helper.SET_VIDEO_MODE, Helper.VIDEO_MODE_WEBVIEW);
                            editor.apply();
                            break;
                        case 1:
                            editor.putInt(Helper.SET_VIDEO_MODE, Helper.VIDEO_MODE_DIRECT);
                            editor.apply();
                            break;
                    }
                }
                count1++;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        boolean video_nsfw = sharedpreferences.getBoolean(Helper.SET_VIDEO_NSFW, false);
        final CheckBox set_video_nsfw = rootView.findViewById(R.id.set_video_nsfw);
        set_video_nsfw.setChecked(video_nsfw);

        set_video_nsfw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_VIDEO_NSFW, set_video_nsfw.isChecked());
                editor.apply();
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
