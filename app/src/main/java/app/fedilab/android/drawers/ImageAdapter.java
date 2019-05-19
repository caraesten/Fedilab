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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import app.fedilab.android.client.Entities.Attachment;
import app.fedilab.android.client.Entities.Status;
import app.fedilab.android.R;
import app.fedilab.android.activities.MediaActivity;
import app.fedilab.android.activities.ShowAccountActivity;
import app.fedilab.android.activities.ShowConversationActivity;


/**
 * Created by Thomas on 05/09/2018.
 * Adapter to display media related to status
 */

public class ImageAdapter extends RecyclerView.Adapter {
    private Context context;
    private List<Status> statuses;
    private LayoutInflater layoutInflater;

    public ImageAdapter(Context context, List<Status> statuses) {
        this.context = context;
        this.statuses = statuses;
        this.layoutInflater = LayoutInflater.from(this.context);
    }

    public int getCount() {
        return statuses.size();
    }

    public Status getItem(int position) {
        return statuses.get(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(layoutInflater.inflate(R.layout.drawer_media, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        Status status = statuses.get(position);

        final ImageAdapter.ViewHolder holder = (ImageAdapter.ViewHolder) viewHolder;

        if( !((ShowAccountActivity)context).isFinishing())
            Glide.with(context).load(status.getArt_attachment().getPreview_url()).into(holder.imageView);
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MediaActivity.class);
                Bundle b = new Bundle();
                ArrayList<Attachment> attachmentsTmp = new ArrayList<>();
                for(Status status1: statuses){
                    attachmentsTmp.add(status1.getArt_attachment());
                }
                intent.putParcelableArrayListExtra("mediaArray", attachmentsTmp);
                b.putInt("position", (viewHolder.getAdapterPosition()+1));
                intent.putExtras(b);
                context.startActivity(intent);
            }
        });
        holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(context, ShowConversationActivity.class);
                Bundle b = new Bundle();
                b.putParcelable("status", status);
                intent.putExtras(b);
                context.startActivity(intent);
                return false;
            }
        });
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return statuses.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.media);
        }
    }
}