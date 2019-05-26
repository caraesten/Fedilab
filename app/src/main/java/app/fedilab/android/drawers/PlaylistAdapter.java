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
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import app.fedilab.android.R;
import app.fedilab.android.activities.PlaylistsActivity;
import app.fedilab.android.client.Entities.Playlist;
import app.fedilab.android.helper.Helper;


/**
 * Created by Thomas on 26/05/2019.
 * Adapter for playlists
 */
public class PlaylistAdapter extends BaseAdapter {

    private List<Playlist> playlists;
    private LayoutInflater layoutInflater;
    private Context context;

    public PlaylistAdapter(Context context, List<Playlist> lists, RelativeLayout textviewNoAction){
        this.playlists = lists;
        layoutInflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public int getCount() {
        return playlists.size();
    }

    @Override
    public Object getItem(int position) {
        return playlists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final Playlist playlist = playlists.get(position);
        final ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.drawer_search, parent, false);
            holder = new ViewHolder();
            holder.search_title = convertView.findViewById(R.id.search_keyword);
            holder.search_container = convertView.findViewById(R.id.search_container);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);

        if( theme == Helper.THEME_LIGHT){
            holder.search_container.setBackgroundResource(R.color.mastodonC3__);
            Helper.changeDrawableColor(context, R.drawable.ic_keyboard_arrow_right,R.color.black);
        }else if(theme == Helper.THEME_DARK){
            holder.search_container.setBackgroundResource(R.color.mastodonC1_);
            Helper.changeDrawableColor(context, R.drawable.ic_keyboard_arrow_right,R.color.dark_text);
        }else if(theme == Helper.THEME_BLACK) {
            holder.search_container.setBackgroundResource(R.color.black_2);
            Helper.changeDrawableColor(context, R.drawable.ic_keyboard_arrow_right,R.color.dark_text);
        }
        Drawable next = ContextCompat.getDrawable(context, R.drawable.ic_keyboard_arrow_right);
        holder.search_title.setText(playlist.getDisplayName());
        assert next != null;
        final float scale = context.getResources().getDisplayMetrics().density;
        next.setBounds(0,0,(int) (30  * scale + 0.5f),(int) (30  * scale + 0.5f));
        holder.search_title.setCompoundDrawables(null, null, next, null);

        holder.search_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PlaylistsActivity.class);
                Bundle b = new Bundle();
                b.putParcelable("playlist", playlist);
                intent.putExtras(b);
                context.startActivity(intent);
            }
        });
        return convertView;
    }

    private class ViewHolder {
        LinearLayout search_container;
        TextView search_title;
    }


}