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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.smarteist.autoimageslider.SliderViewAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import app.fedilab.android.R;
import app.fedilab.android.activities.MediaActivity;
import app.fedilab.android.client.Entities.Attachment;


public class SliderAdapter extends SliderViewAdapter<SliderAdapter.SliderAdapterVH> {

    private ArrayList<Attachment> attachments;
    private WeakReference<Context> contextWeakReference;

    SliderAdapter(WeakReference<Context> contextWeakReference, ArrayList<Attachment> attachments) {
        this.attachments = attachments;
        this.contextWeakReference = contextWeakReference;
    }

    @Override
    public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_slider_layout_item, parent, false);
        return new SliderAdapterVH(inflate);
    }

    @Override
    public void onBindViewHolder(SliderAdapterVH viewHolder, int position) {

        if( attachments.size() > 1) {
            viewHolder.textViewDescription.setText(String.format("%s/%s", (position + 1), attachments.size()));
        }

        Glide.with(viewHolder.imageViewBackground.getContext())
                .load(attachments.get(position).getPreview_url())
                .into(viewHolder.imageViewBackground);
        viewHolder.imageViewBackground.setContentDescription(attachments.get(position).getDescription());
        viewHolder.imageViewBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(contextWeakReference.get(), MediaActivity.class);
                Bundle b = new Bundle();

                intent.putParcelableArrayListExtra("mediaArray", attachments);
                b.putInt("position", (position+1));
                intent.putExtras(b);
                contextWeakReference.get().startActivity(intent);
            }
        });

    }

    @Override
    public int getCount() {
        //slider view count could be dynamic size
        return attachments.size();
    }

    class SliderAdapterVH extends SliderViewAdapter.ViewHolder {

        ImageView imageViewBackground;
        TextView textViewDescription;

        SliderAdapterVH(View itemView) {
            super(itemView);
            imageViewBackground = itemView.findViewById(R.id.iv_auto_image_slider);
            textViewDescription = itemView.findViewById(R.id.tv_auto_image_slider);
        }
    }
}