package fr.gouv.etalab.mastodon.drawers;
/* Copyright 2019 Thomas Schneider
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
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.activities.HashTagActivity;


/**
 * Created by Thomas on 31/03/2019.
 * Adapter for tags results
 */
public class SearchTagsAdapter extends RecyclerView.Adapter {

    private Context context;
    private List<String> tags;

    private LayoutInflater layoutInflater;

    public SearchTagsAdapter(Context context, List<String> tags){
        this.context = context;
        this.tags = ( tags != null)?tags:new ArrayList<>();
        layoutInflater = LayoutInflater.from(context);
    }


    public String getItem(int position) {
       return tags.get(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        return new ViewHolder(layoutInflater.inflate(R.layout.drawer_tag_search_tab, parent, false));
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tag_name;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tag_name = itemView.findViewById(R.id.tag_name);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        final ViewHolder holder = (ViewHolder) viewHolder;
        final String tag = getItem(i);

        holder.tag_name.setText(String.format("#%s",tag));
        holder.tag_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, HashTagActivity.class);
                Bundle b = new Bundle();
                b.putString("tag", tag.trim());
                intent.putExtras(b);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }



}