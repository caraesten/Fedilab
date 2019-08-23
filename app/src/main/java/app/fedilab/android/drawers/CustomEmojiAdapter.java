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
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.github.penfeizhou.animation.apng.APNGDrawable;
import com.github.penfeizhou.animation.apng.decode.APNGParser;
import com.github.penfeizhou.animation.gif.GifDrawable;
import com.github.penfeizhou.animation.gif.decode.GifParser;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

import app.fedilab.android.R;
import app.fedilab.android.client.Entities.Emojis;
import app.fedilab.android.helper.Helper;


/**
 * Created by Thomas on 03/11/2017.
 * Adapter to display custom emojis
 */
public class CustomEmojiAdapter extends ArrayAdapter<Emojis> {


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



        if( !emoji.isDrawableFound()  ) {
            emoji.setDrawableFound(true);
            Glide.with(context)
                    .asFile()
                    .load(emoji.getUrl())
                    .thumbnail(0.1f)
                    .into(new SimpleTarget<File>() {
                        @Override
                        public void onResourceReady(@NonNull File resourceFile, @Nullable Transition<? super File> transition) {
                            new transform(context, emoji,resourceFile, imageView).execute();
                        }
                    });
        }else{
            imageView.setImageDrawable(emoji.getDrawable());
        }
        return convertView;
    }



    private static class transform extends AsyncTask<Void, Void, Drawable> {

        private WeakReference<Context> contextWeakReference;
        private File resourceFile;
        private Emojis emoji;
        private WeakReference<ImageView> imageViewWeakReference;

        transform(Context context, Emojis emoji, File resource, ImageView imageView) {
            this.contextWeakReference = new WeakReference<>(context);
            this.resourceFile = resource;
            this.emoji = emoji;
            this.imageViewWeakReference = new WeakReference<>(imageView);
        }

        @Override
        protected Drawable doInBackground(Void... params) {
            Drawable resource;
            SharedPreferences sharedpreferences = contextWeakReference.get().getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            boolean disableAnimatedEmoji = sharedpreferences.getBoolean(Helper.SET_DISABLE_ANIMATED_EMOJI, false);
            if( !disableAnimatedEmoji) {
                if (GifParser.isGif(resourceFile.getAbsolutePath())) {
                    resource = GifDrawable.fromFile(resourceFile.getAbsolutePath());
                    emoji.setDrawable(resource);
                } else if (APNGParser.isAPNG(resourceFile.getAbsolutePath())) {
                    resource = APNGDrawable.fromFile(resourceFile.getAbsolutePath());
                    emoji.setDrawable(resource);
                } else {
                    resource = Drawable.createFromPath(resourceFile.getAbsolutePath());
                    emoji.setDrawable(resource);

                }
            }else{
                resource = Drawable.createFromPath(resourceFile.getAbsolutePath());
                emoji.setDrawable(resource);
            }
            return resource;
        }

        @Override
        protected void onPostExecute(Drawable result) {

            imageViewWeakReference.get().setImageDrawable(result);
        }
    }
}