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


import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.client.Entities.Emojis;



/**
 * Created by Thomas on 01/11/2017.
 * Adapter for emojis when searching
 */
public class EmojisSearchAdapter extends ArrayAdapter<Emojis> implements Filterable {

    private List<Emojis> emojis, tempEmojis, suggestions ;
    private LayoutInflater layoutInflater;
    private Context context;

    public EmojisSearchAdapter(Context context, List<Emojis> emojis){
        super(context, android.R.layout.simple_list_item_1, emojis);
        this.emojis = emojis;
        this.context = context;
        this.tempEmojis = new ArrayList<>(emojis);
        this.suggestions = new ArrayList<>(emojis);
        layoutInflater = LayoutInflater.from(context);
    }


    @Override
    public int getCount() {
        return emojis.size();
    }

    @Override
    public Emojis getItem(int position) {
        return emojis.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {

        final Emojis emoji = emojis.get(position);
        final ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.drawer_emoji_search, parent, false);
            holder = new ViewHolder();
            holder.emoji_icon = convertView.findViewById(R.id.emoji_icon);
            holder.emoji_shortcode = convertView.findViewById(R.id.emoji_shortcode);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.emoji_shortcode.setText(String.format("%s", emoji.getShortcode()));
        //Profile picture
        Glide.with(context)
                .load(emoji.getUrl())
                .into(holder.emoji_icon);
        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return emojiFilter;
    }


    private Filter emojiFilter = new Filter() {
        @Override
        public CharSequence convertResultToString(Object resultValue) {
            Emojis emoji = (Emojis) resultValue;
            return emoji.getShortcode();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (constraint != null) {
                suggestions.clear();
                for (Emojis emoji : tempEmojis) {
                    suggestions.add(emoji);
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            } else {
                return new FilterResults();
            }
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            ArrayList<Emojis> c = (ArrayList<Emojis>) results.values;
            if (results.count > 0) {
                clear();
                for (Emojis cust : c) {
                    add(cust);
                    notifyDataSetChanged();
                }
            } else{
                clear();
                notifyDataSetChanged();
            }
        }
    };

    private class ViewHolder {
        ImageView emoji_icon;
        TextView emoji_shortcode;
    }


}