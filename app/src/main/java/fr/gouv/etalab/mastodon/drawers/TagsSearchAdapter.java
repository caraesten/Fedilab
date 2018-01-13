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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import fr.gouv.etalab.mastodon.R;


/**
 * Created by Thomas on 09/10/2017.
 * Adapter for tags when searching
 */
public class TagsSearchAdapter extends ArrayAdapter<String> implements Filterable {

    private List<String> tags, tempTags, suggestions ;
    private LayoutInflater layoutInflater;

    public TagsSearchAdapter(Context context, List<String> tags){
        super(context, android.R.layout.simple_list_item_1, tags);
        this.tags = tags;
        this.tempTags = new ArrayList<>(tags);
        this.suggestions = new ArrayList<>(tags);
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return tags.size();
    }

    @Override
    public String getItem(int position) {
        return tags.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final String tag = tags.get(position);
        final ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.drawer_tag_search, parent, false);
            holder = new ViewHolder();
            holder.tag_name = convertView.findViewById(R.id.tag_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tag_name.setText(String.format("#%s", tag));

        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return searchFilter;
    }

    private Filter searchFilter = new Filter() {
        @Override
        public CharSequence convertResultToString(Object resultValue) {
            String tag = (String) resultValue;
            return "#" + tag;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (constraint != null) {
                suggestions.clear();
                suggestions.addAll(tempTags);
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
            ArrayList<String> c = (ArrayList<String>) results.values;
            if (results.count > 0) {
                clear();
                addAll(c);
                notifyDataSetChanged();
            } else{
                clear();
                notifyDataSetChanged();
            }
        }
    };


    private class ViewHolder {
        TextView tag_name;
    }


}