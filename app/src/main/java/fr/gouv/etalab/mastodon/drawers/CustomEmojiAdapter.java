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
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.List;

import fr.gouv.etalab.mastodon.client.Entities.Emojis;

/**
 * Created by Thomas on 03/11/2017.
 * Adapter to display custom emojis
 */
public class CustomEmojiAdapter extends ArrayAdapter {


    private Context context;
    private List<Emojis> emojis;

    public CustomEmojiAdapter(@NonNull Context context, int resource, List<Emojis> emojis) {
        super(context, resource, resource);
        this.context = context;
        this.emojis = emojis;
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
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        final ImageView imageView;
        Emojis emoji = emojis.get(position);
        if (convertView == null) {
            imageView = new ImageView(context);
            float density = context.getResources().getDisplayMetrics().density;
            imageView.setLayoutParams(new GridView.LayoutParams((int)(30*density), (int)(30*density)));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding((int)(2*density), (int)(5*density), (int)(2*density), (int)(5*density));
        } else {
            imageView = (ImageView) convertView;
        }
        Glide.with(context)
                .asBitmap()
                .load(emoji.getUrl())
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        BitmapDrawable icon = new BitmapDrawable(context.getResources(), resource);
                        imageView.setImageDrawable(icon);
                    }
                });
        return imageView;
    }

}