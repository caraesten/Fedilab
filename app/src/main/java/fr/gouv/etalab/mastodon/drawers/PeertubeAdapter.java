package fr.gouv.etalab.mastodon.drawers;
/* Copyright 2018 Thomas Schneider
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
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.activities.PeertubeActivity;
import fr.gouv.etalab.mastodon.activities.WebviewActivity;
import fr.gouv.etalab.mastodon.asynctasks.ManageListsAsyncTask;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Peertube;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnListActionInterface;

import static fr.gouv.etalab.mastodon.helper.Helper.changeDrawableColor;


/**
 * Created by Thomas on 06/10/2018.
 * Adapter for peertube
 */
public class PeertubeAdapter extends RecyclerView.Adapter implements OnListActionInterface {

    private List<Peertube> peertubes;
    private LayoutInflater layoutInflater;
    private Context context;
    private String instance;

    public PeertubeAdapter(Context context, String instance, List<Peertube> peertubes){
        this.peertubes = peertubes;
        layoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.instance = instance;

    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PeertubeAdapter.ViewHolder(layoutInflater.inflate(R.layout.drawer_how_to_videos, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {


        final PeertubeAdapter.ViewHolder holder = (PeertubeAdapter.ViewHolder) viewHolder;
        final Peertube peertube = peertubes.get(position);


        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);

        if( theme == Helper.THEME_LIGHT){
            holder.how_to_container.setBackgroundResource(R.color.mastodonC3__);
            changeDrawableColor(context, R.drawable.ic_keyboard_arrow_right,R.color.black);
        }else if(theme == Helper.THEME_DARK){
            holder.how_to_container.setBackgroundResource(R.color.mastodonC1_);
            changeDrawableColor(context, R.drawable.ic_keyboard_arrow_right,R.color.dark_text);
        }else if(theme == Helper.THEME_BLACK) {
            holder.how_to_container.setBackgroundResource(R.color.black_2);
            changeDrawableColor(context, R.drawable.ic_keyboard_arrow_right,R.color.dark_text);
        }
        Drawable next = ContextCompat.getDrawable(context, R.drawable.ic_keyboard_arrow_right);
        holder.how_to_description.setText(peertube.getDescription());
        holder.how_to_title.setText(peertube.getName());
        assert next != null;
        final float scale = context.getResources().getDisplayMetrics().density;
        next.setBounds(0,0,(int) (30  * scale + 0.5f),(int) (30  * scale + 0.5f));
        holder.how_to_description.setCompoundDrawables(null, null, next, null);
        Glide.with(holder.how_to_image.getContext())
                .load("https://" + instance + peertube.getThumbnailPath())
                .into(holder.how_to_image);
        holder.how_to_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PeertubeActivity.class);
                Bundle b = new Bundle();
                String finalUrl = "https://"  + instance + peertube.getEmbedPath();
                b.putString("url", finalUrl);
                b.putBoolean("peertubeLink", true);
                Pattern link = Pattern.compile("(https?:\\/\\/[\\da-z\\.-]+\\.[a-z\\.]{2,10})\\/videos\\/embed\\/(\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12})$");
                Matcher matcherLink = link.matcher(finalUrl);
                if( matcherLink.find()) {
                    String url = matcherLink.group(1) + "/videos/watch/" + matcherLink.group(2);
                    String stream_url = peertube.getStreamURL();
                    b.putString("peertubeLinkToFetch", url);
                    b.putString("stream_url", stream_url);
                    b.putString("peertube_instance", matcherLink.group(1).replace("https://","").replace("http://",""));
                    b.putString("video_id", matcherLink.group(2));
                }
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
        return peertubes.size();
    }


    @Override
    public void onActionDone(ManageListsAsyncTask.action actionType, APIResponse apiResponse, int statusCode) {

    }

    class ViewHolder extends RecyclerView.ViewHolder{
        LinearLayout how_to_container;
        ImageView how_to_image;
        TextView how_to_description;
        TextView how_to_title;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            how_to_description = itemView.findViewById(R.id.how_to_description);
            how_to_title = itemView.findViewById(R.id.how_to_title);
            how_to_image = itemView.findViewById(R.id.how_to_image);
            how_to_container = itemView.findViewById(R.id.how_to_container);
        }
    }




}