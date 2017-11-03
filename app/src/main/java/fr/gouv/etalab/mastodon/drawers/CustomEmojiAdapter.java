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

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.util.List;

import fr.gouv.etalab.mastodon.client.Entities.Emojis;
import fr.gouv.etalab.mastodon.client.PatchBaseImageDownloader;
import mastodon.etalab.gouv.fr.mastodon.R;

/**
 * Created by Thomas on 03/11/2017.
 * Adapter to display custom emojis
 */
public class CustomEmojiAdapter extends ArrayAdapter {


    private Context context;
    private List<Emojis> emojis;
    private CustomEmojiAdapter customEmojiAdapter;

    public CustomEmojiAdapter(@NonNull Context context, int resource, List<Emojis> emojis) {
        super(context, resource, resource);
        this.context = context;
        this.emojis = emojis;
        customEmojiAdapter = this;
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
        ImageLoader imageLoader = ImageLoader.getInstance();
        File cacheDir = new File(context.getCacheDir(), context.getString(R.string.app_name));
        ImageLoaderConfiguration configImg = new ImageLoaderConfiguration.Builder(context)
                .imageDownloader(new PatchBaseImageDownloader(context))
                .threadPoolSize(5)
                .threadPriority(Thread.MIN_PRIORITY + 3)
                .denyCacheImageMultipleSizesInMemory()
                .diskCache(new UnlimitedDiskCache(cacheDir))
                .build();
        DisplayImageOptions optionNew = new DisplayImageOptions.Builder().displayer(new SimpleBitmapDisplayer()).cacheInMemory(false)
                .cacheOnDisk(true).resetViewBeforeLoading(true).build();
        if( !imageLoader.isInited())
            imageLoader.init(configImg);
        imageLoader.loadImage(emoji.getUrl(), optionNew, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                super.onLoadingComplete(imageUri, view, loadedImage);
                BitmapDrawable icon = new BitmapDrawable(context.getResources(), loadedImage);
                imageView.setImageDrawable(icon);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            }
        });
        return imageView;
    }

}