package app.fedilab.android.drawers;
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
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import app.fedilab.android.sqlite.Sqlite;
import app.fedilab.android.sqlite.TagsCacheDAO;
import es.dmoral.toasty.Toasty;
import app.fedilab.android.R;


/**
 * Created by Thomas on 01/12/2018.
 * Adapter for tags when editing
 */
public class TagsEditAdapter extends RecyclerView.Adapter  {

    private Context context;
    private List<String> tags;
    private LayoutInflater layoutInflater;
    private TagsEditAdapter tagsEditAdapter;

    public TagsEditAdapter(Context context, List<String> tags){
        this.tags = tags;
        this.layoutInflater = LayoutInflater.from(context);
        this.context = context;
        tagsEditAdapter = this;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        return new ViewHolder(layoutInflater.inflate(R.layout.drawer_tag_edit, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        final String[] tag = {tags.get(viewHolder.getAdapterPosition())};
        ViewHolder holder = (ViewHolder) viewHolder;
        holder.tag_name.setText(String.format("#%s", tag[0]));
        SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        holder.save_tag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( holder.tag_name.getText() != null && holder.tag_name.getText().toString().trim().replaceAll("\\#","").length() > 0) {
                    String tagToInsert = holder.tag_name.getText().toString().trim().replaceAll("\\#","");
                    boolean isPresent = new TagsCacheDAO(context, db).isPresent(tagToInsert);
                    if( isPresent)
                        Toasty.warning(context, context.getString(R.string.tags_already_stored), Toast.LENGTH_LONG).show();
                    else {
                        new TagsCacheDAO(context, db).update(tag[0], tagToInsert);
                        Toasty.success(context, context.getString(R.string.tags_renamed), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        holder.delete_tag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.tag_name.clearFocus();
                new TagsCacheDAO(context, db).removeTag(tag[0]);
                tags.remove(tag[0]);
                tagsEditAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                Toasty.success(context, context.getString(R.string.tags_deleted), Toast.LENGTH_LONG).show();
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


    class ViewHolder extends RecyclerView.ViewHolder{
        TextView tag_name;
        ImageButton save_tag, delete_tag;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tag_name = itemView.findViewById(R.id.tag_name);
            delete_tag = itemView.findViewById(R.id.delete_tag);
            save_tag = itemView.findViewById(R.id.save_tag);
        }
    }


}