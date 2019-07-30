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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
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

import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Peertube;
import app.fedilab.android.helper.CrossActions;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.R;
import app.fedilab.android.activities.MainActivity;
import app.fedilab.android.activities.PeertubeActivity;
import app.fedilab.android.activities.PeertubeEditUploadActivity;
import app.fedilab.android.activities.ShowAccountActivity;
import app.fedilab.android.asynctasks.ManageListsAsyncTask;
import app.fedilab.android.asynctasks.UpdateAccountInfoAsyncTask;
import app.fedilab.android.interfaces.OnListActionInterface;



/**
 * Created by Thomas on 06/10/2018.
 * Adapter for peertube
 */
public class PeertubeAdapter extends RecyclerView.Adapter implements OnListActionInterface {

    private List<Peertube> peertubes;
    private LayoutInflater layoutInflater;
    private Context context;
    private String instance;
    private boolean ownVideos;

    public PeertubeAdapter(Context context, String instance, List<Peertube> peertubes){
        this.peertubes = peertubes;
        layoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.instance = instance;
        this.ownVideos = false;

    }

    public PeertubeAdapter(Context context, String instance, boolean ownVideos, List<Peertube> peertubes){
        this.peertubes = peertubes;
        layoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.instance = instance;
        this.ownVideos = ownVideos;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PeertubeAdapter.ViewHolder(layoutInflater.inflate(R.layout.drawer_peertube, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {


        final PeertubeAdapter.ViewHolder holder = (PeertubeAdapter.ViewHolder) viewHolder;
        final Peertube peertube = peertubes.get(position);
        if( peertube.getInstance() == null)
            peertube.setInstance(Helper.getLiveInstance(context));
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if( theme == Helper.THEME_LIGHT){
            holder.main_container.setBackgroundResource(R.color.mastodonC3__);
        }else if (theme == Helper.THEME_DARK){
            holder.main_container.setBackgroundResource(R.color.mastodonC1_);
        }else if (theme == Helper.THEME_BLACK){
            holder.main_container.setBackgroundResource(R.color.black);
        }
        Account account = peertube.getAccount();

        holder.peertube_account_name.setText(account.getAcct());
        holder.peertube_title.setText(peertube.getName());
        holder.peertube_duration.setText(context.getString(R.string.duration_video, Helper.secondsToString(peertube.getDuration())));
        holder.peertube_date.setText(String.format(" - %s", Helper.dateDiff(context, peertube.getCreated_at())));
        holder.peertube_views.setText(context.getString(R.string.number_view_video, Helper.withSuffix(peertube.getView())));


        Glide.with(holder.peertube_video_image.getContext())
                .load("https://" + peertube.getInstance() + peertube.getThumbnailPath())
                .into(holder.peertube_video_image);
        if (account.getAvatar() != null && !account.getAvatar().equals("null") && !account.getAvatar().startsWith("http"))
            account.setAvatar("https://" + peertube.getInstance() + account.getAvatar());
        Helper.loadGiF(context, account, holder.peertube_profile);


        if( peertube.getHeaderType() != null && peertube.getHeaderTypeValue() != null) {
            String type = peertube.getHeaderType();
            switch (type){
                case "tags":
                    holder.header_title.setText(String.format("#%s", peertube.getHeaderTypeValue()));
                    break;
                default:
                    holder.header_title.setText(peertube.getHeaderTypeValue());
                    break;
            }
            holder.header_title.setVisibility(View.VISIBLE);
        }else {
            holder.header_title.setVisibility(View.GONE);
        }

        if( !this.ownVideos) {
            holder.peertube_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //For remote peertube instance
                    if (!peertube.getInstance().equals(Helper.getLiveInstance(context)))
                        CrossActions.doCrossProfile(context, account);
                    else {
                        Intent intent = new Intent(context, ShowAccountActivity.class);
                        Bundle b = new Bundle();
                        b.putBoolean("peertubeaccount", true);
                        b.putParcelable("account", peertube.getAccount());

                        intent.putExtras(b);
                        context.startActivity(intent);
                    }
                }
            });
            holder.main_container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, PeertubeActivity.class);
                    Bundle b = new Bundle();
                    if ((instance == null || instance.trim().length() == 0) && MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE)
                        instance = Helper.getLiveInstance(context);
                    String finalUrl = "https://" + instance + peertube.getEmbedPath();
                    Pattern link = Pattern.compile("(https?:\\/\\/[\\da-z\\.-]+\\.[a-z\\.]{2,10})\\/videos\\/embed\\/(\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12})$");
                    Matcher matcherLink = link.matcher(finalUrl);
                    if (matcherLink.find()) {
                        String url = matcherLink.group(1) + "/videos/watch/" + matcherLink.group(2);
                        b.putString("peertubeLinkToFetch", url);
                        b.putString("peertube_instance", matcherLink.group(1).replace("https://", "").replace("http://", ""));
                        b.putString("video_id", matcherLink.group(2));
                    }
                    intent.putExtras(b);
                    context.startActivity(intent);
                }
            });
        }else{
            holder.main_container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, PeertubeEditUploadActivity.class);
                    Bundle b = new Bundle();
                    b.putString("video_id",peertube.getUuid());
                    intent.putExtras(b);
                    context.startActivity(intent);
                }
            });
        }

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
        LinearLayout main_container;
        ImageView peertube_profile, peertube_video_image;
        TextView peertube_account_name, peertube_views, peertube_duration;
        TextView peertube_title, peertube_date, header_title;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            peertube_account_name = itemView.findViewById(R.id.peertube_account_name);
            peertube_title = itemView.findViewById(R.id.peertube_title);
            peertube_video_image = itemView.findViewById(R.id.peertube_video_image);
            peertube_profile = itemView.findViewById(R.id.peertube_profile);
            peertube_date = itemView.findViewById(R.id.peertube_date);
            peertube_views = itemView.findViewById(R.id.peertube_views);
            peertube_duration = itemView.findViewById(R.id.peertube_duration);
            main_container = itemView.findViewById(R.id.main_container);
            header_title = itemView.findViewById(R.id.header_title);
        }
    }




}