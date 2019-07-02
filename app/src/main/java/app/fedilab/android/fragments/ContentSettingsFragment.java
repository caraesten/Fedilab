package app.fedilab.android.fragments;
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
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

import app.fedilab.android.R;
import app.fedilab.android.animatemenu.interfaces.ScreenShotable;
import app.fedilab.android.helper.Helper;

import static android.content.Context.MODE_PRIVATE;
import static app.fedilab.android.fragments.ContentSettingsFragment.type.ADMIN;
import static app.fedilab.android.fragments.ContentSettingsFragment.type.ALL;
import static app.fedilab.android.fragments.ContentSettingsFragment.type.COMPOSE;
import static app.fedilab.android.fragments.ContentSettingsFragment.type.HIDDEN;
import static app.fedilab.android.fragments.ContentSettingsFragment.type.INTERFACE;
import static app.fedilab.android.fragments.ContentSettingsFragment.type.NOTIFICATIONS;
import static app.fedilab.android.fragments.ContentSettingsFragment.type.TIMELINES;

public class ContentSettingsFragment  extends Fragment implements ScreenShotable {


    private View containerView;
    protected int res;
    private Bitmap bitmap;
    private type type;
    private Context context;

    public enum type{
        CLOSE,
        TIMELINES,
        ADMIN,
        NOTIFICATIONS,
        INTERFACE,
        COMPOSE,
        HIDDEN,
        ALL
    }


    public static ContentSettingsFragment newInstance(int resId) {
        ContentSettingsFragment contentFragment = new ContentSettingsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Integer.class.getName(), resId);
        contentFragment.setArguments(bundle);
        return contentFragment;
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getContext();
        assert context != null;
        this.containerView = view.findViewById(R.id.container);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assert getArguments() != null;
        res = getArguments().getInt(Integer.class.getName());
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings_reveal, container, false);
        FrameLayout containerFrame = rootView.findViewById(R.id.container);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            type = (type)bundle.getSerializable("type");
        }

        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        switch (theme){
            case Helper.THEME_LIGHT:
                containerFrame.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                break;
            case Helper.THEME_DARK:
                containerFrame.setBackgroundColor(ContextCompat.getColor(context, R.color.mastodonC1));
                break;
            case Helper.THEME_BLACK:
                containerFrame.setBackgroundColor(ContextCompat.getColor(context, R.color.black));
                break;
            default:
                containerFrame.setBackgroundColor(ContextCompat.getColor(context, R.color.mastodonC1));
        }
        LinearLayout settings_timeline = rootView.findViewById(R.id.settings_timeline);
        LinearLayout settings_notifications = rootView.findViewById(R.id.settings_notifications);
        LinearLayout settings_admin = rootView.findViewById(R.id.settings_admin);
        LinearLayout settings_interface = rootView.findViewById(R.id.settings_interface);
        LinearLayout settings_compose = rootView.findViewById(R.id.settings_compose);
        LinearLayout settings_hidden = rootView.findViewById(R.id.settings_hidden);
        LinearLayout settings_to_do = rootView.findViewById(R.id.settings_to_do);

        if(type == null || type.equals(TIMELINES)){
            settings_timeline.setVisibility(View.VISIBLE);
        }else if( type == NOTIFICATIONS){
            settings_notifications.setVisibility(View.VISIBLE);
        }else if( type == ADMIN){
            settings_admin.setVisibility(View.VISIBLE);
        }else if(type == INTERFACE){
            settings_interface.setVisibility(View.VISIBLE);
        }else if(type == COMPOSE){
            settings_compose.setVisibility(View.VISIBLE);
        }else if( type == HIDDEN){
            settings_hidden.setVisibility(View.VISIBLE);
        }else if( type == ALL){
            settings_to_do.setVisibility(View.VISIBLE);
        }


        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void takeScreenShot() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                Bitmap bitmap = Bitmap.createBitmap(containerView.getWidth(),
                        containerView.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                containerView.draw(canvas);
                ContentSettingsFragment.this.bitmap = bitmap;
            }
        };

        thread.start();
    }

    @Override
    public Bitmap getBitmap() {
        return bitmap;
    }
}
