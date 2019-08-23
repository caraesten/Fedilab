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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.github.penfeizhou.animation.apng.APNGDrawable;
import com.github.penfeizhou.animation.gif.GifDrawable;

import java.io.File;
import java.util.List;

import app.fedilab.android.R;
import app.fedilab.android.client.Entities.Emojis;
import app.fedilab.android.helper.Helper;

import static app.fedilab.android.helper.Helper.drawableToBitmap;

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
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.drawer_emoji_picker, parent, false);
            imageView = convertView.findViewById(R.id.img_custom_emoji);
        } else {
            imageView = (ImageView) convertView;
        }

        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        boolean disableAnimatedEmoji = sharedpreferences.getBoolean(Helper.SET_DISABLE_ANIMATED_EMOJI, false);

        Glide.with(context)
                .asFile()
                .load(emoji.getUrl())
                .listener(new RequestListener<File>()  {
                    @Override
                    public boolean onResourceReady(File resource, Object model, Target<File> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(new SimpleTarget<File>() {
                    @Override
                    public void onResourceReady(@NonNull File resourceFile, @Nullable Transition<? super File> transition) {
                        Drawable resource;
                        if( emoji.getUrl().endsWith(".gif")){
                            resource = GifDrawable.fromFile(resourceFile.getAbsolutePath());
                        }else{
                            resource = APNGDrawable.fromFile(resourceFile.getAbsolutePath());
                        }

                        if( !disableAnimatedEmoji) {
                            resource.setVisible(true, true);
                            imageView.setImageDrawable(resource);

                        }else{
                            Bitmap bitmap = drawableToBitmap(resource.getCurrent());
                            imageView.setImageBitmap(bitmap);
                        }
                    }
                });
        return convertView;
    }

}