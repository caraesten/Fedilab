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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.smarteist.autoimageslider.IndicatorAnimations;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;
import com.varunest.sparkbutton.SparkButton;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import app.fedilab.android.R;
import app.fedilab.android.activities.SlideMediaActivity;
import app.fedilab.android.client.Entities.Attachment;
import app.fedilab.android.client.Entities.PixelFedStory;
import app.fedilab.android.client.Entities.PixelFedStoryItem;
import app.fedilab.android.helper.Helper;

;


/**
 * Created by Thomas on 02/11/2019.
 * Adapter for pixelfed stories drawer
 */
public class PixelfedStoriesListAdapter extends RecyclerView.Adapter {

    private Context context;
    private List<PixelFedStory> stories;
    private static final int DISPLAYED_STATUS = 1;
    private ArrayList<Attachment> attachments;

    public PixelfedStoriesListAdapter(List<PixelFedStory> stories) {
        super();
        this.stories = stories;
    }




    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return stories.size();
    }



    private class ViewHolderPixelfed extends RecyclerView.ViewHolder {
        SliderView imageSlider;
        ImageView art_media, art_media_play, pf_pp;
        SparkButton pf_share;
        TextView pf_username, pf_date;
        CardView pf_cardview;


        ViewHolderPixelfed(View itemView) {
            super(itemView);
            art_media = itemView.findViewById(R.id.art_media);
            art_media_play = itemView.findViewById(R.id.art_media_play);
            imageSlider = itemView.findViewById(R.id.imageSlider);
            pf_pp = itemView.findViewById(R.id.pf_pp);
            pf_username = itemView.findViewById(R.id.pf_username);
            pf_date = itemView.findViewById(R.id.pf_date);
            pf_share = itemView.findViewById(R.id.pf_share);
            pf_cardview = itemView.findViewById(R.id.pf_cardview);
        }
    }


    public PixelFedStory getItem(int position) {
        if (stories.size() > position && position >= 0)
            return stories.get(position);
        else return null;
    }

    @Override
    public int getItemViewType(int position) {
        return DISPLAYED_STATUS;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(this.context);
        return new ViewHolderPixelfed(layoutInflater.inflate(R.layout.drawer_pixelfed_story, parent, false));
    }


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int i) {
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        final String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        final ViewHolderPixelfed holder = (ViewHolderPixelfed) viewHolder;
        final PixelFedStory pixelFedStory = stories.get(viewHolder.getAdapterPosition());


        attachments = new ArrayList<>();

        for(PixelFedStoryItem pixelFedStoryItem: pixelFedStory.getPixelFedStoryItems()){
            Attachment attachment = new Attachment();

            if( pixelFedStoryItem.getPreview() != null){
                attachment.setPreview_url(pixelFedStoryItem.getPreview());
            }else{
                attachment.setPreview_url(pixelFedStoryItem.getSrc());
            }
            attachment.setUrl(pixelFedStoryItem.getSrc());
            attachment.setId(pixelFedStoryItem.getId());
            attachment.setId(pixelFedStoryItem.getId());
            attachment.setDescription(pixelFedStoryItem.getLinkText());
            attachment.setType(pixelFedStoryItem.getType());
        }

        Glide.with(context)
                .load(pixelFedStory.getPhoto())
                .apply(new RequestOptions().transforms(new FitCenter(), new RoundedCorners(270)))
                .into(holder.pf_pp);



        holder.art_media.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SlideMediaActivity.class);
                Bundle b = new Bundle();
                intent.putParcelableArrayListExtra("mediaArray", attachments);
                b.putInt("position", 1);
                intent.putExtras(b);
                context.startActivity(intent);
            }
        });




        holder.art_media_play.setVisibility(View.GONE);

        if (attachments != null && attachments.size() > 1){
            SliderAdapter sliderAdapter = new SliderAdapter(new WeakReference<>((Activity)context), false, attachments);
            holder.imageSlider.setSliderAdapter(sliderAdapter);
            holder.imageSlider.setIndicatorAnimation(IndicatorAnimations.WORM);
            holder.imageSlider.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
            holder.art_media.setVisibility(View.GONE);
            holder.imageSlider.setVisibility(View.VISIBLE);
        }else if(attachments != null ){
            holder.art_media.setVisibility(View.VISIBLE);
            holder.imageSlider.setVisibility(View.GONE);
            if( attachments.get(0).getType().toLowerCase().equals("video")){
                holder.art_media_play.setVisibility(View.VISIBLE);
            }
            String url;
            if(attachments.get(0).getPreview_url().endsWith("no-preview.png") ){
                url = attachments.get(0).getUrl();
            }else{
                url = attachments.get(0).getPreview_url();
            }
            Glide.with(holder.itemView.getContext())
                    .asBitmap()
                    .load(url)
                    .thumbnail(0.1f)
                    .into(holder.art_media);
        }



        holder.pf_date.setText(Helper.longDateToString(pixelFedStory.getLastUpdated()));


        holder.pf_username.setText(pixelFedStory.getName());
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);


        if (theme == Helper.THEME_BLACK) {
            holder.pf_share.setInActiveImageTint(R.color.action_black);
            Helper.changeDrawableColor(context, R.drawable.ic_share_media, R.color.action_black);
            holder.pf_cardview.setCardBackgroundColor(ContextCompat.getColor(context, R.color.black_3));
        } else if (theme == Helper.THEME_DARK) {
            holder.pf_share.setInActiveImageTint(R.color.action_dark);
            Helper.changeDrawableColor(context, R.drawable.ic_share_media, R.color.action_black);
            holder.pf_cardview.setCardBackgroundColor(ContextCompat.getColor(context, R.color.mastodonC1_));
        } else {
            holder.pf_share.setInActiveImageTint(R.color.action_light);
            Helper.changeDrawableColor(context, R.drawable.ic_share_media, R.color.action_black);
            holder.pf_cardview.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white));
        }

    }

}