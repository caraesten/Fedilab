package fr.gouv.etalab.mastodon.drawers;
/* Copyright 2019 Thomas Schneider
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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.activities.PeertubeActivity;
import fr.gouv.etalab.mastodon.activities.ShowAccountActivity;
import fr.gouv.etalab.mastodon.client.Entities.Notification;
import fr.gouv.etalab.mastodon.client.Entities.PeertubeAccountNotification;
import fr.gouv.etalab.mastodon.client.Entities.PeertubeNotification;
import fr.gouv.etalab.mastodon.client.Entities.PeertubeVideoNotification;
import fr.gouv.etalab.mastodon.fragments.DisplayPeertubeNotificationsFragment;
import fr.gouv.etalab.mastodon.helper.Helper;

import static fr.gouv.etalab.mastodon.helper.Helper.THEME_BLACK;
import static fr.gouv.etalab.mastodon.helper.Helper.THEME_DARK;



/**
 * Created by Thomas on 23/01/2019.
 * Adapter for Peertube notifications
 */

public class PeertubeNotificationsListAdapter extends RecyclerView.Adapter {

    private Context context;
    private List<PeertubeNotification> notifications;
    private LayoutInflater layoutInflater;
    private PeertubeNotificationsListAdapter peertubeNotificationsListAdapter;
    private PeertubeNotificationsListAdapter.ViewHolder holder;
    private int style;

    public PeertubeNotificationsListAdapter(Context context, List<PeertubeNotification> notifications){
        this.context = context;
        this.notifications = notifications;
        layoutInflater = LayoutInflater.from(this.context);
        peertubeNotificationsListAdapter = this;

    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(layoutInflater.inflate(R.layout.drawer_notification, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {

        holder = (PeertubeNotificationsListAdapter.ViewHolder) viewHolder;
        PeertubeNotification notification = notifications.get(position);
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if (theme == THEME_DARK ){
            holder.main_container_trans.setBackgroundColor(ContextCompat.getColor(context, R.color.notif_dark_1));
            holder.main_container_trans.setAlpha(.5f);
        }else if( theme == THEME_BLACK){
            holder.main_container_trans.setBackgroundColor(ContextCompat.getColor(context, R.color.notif_black_1));
            holder.main_container_trans.setAlpha(.5f);
        }else{
            holder.main_container_trans.setBackgroundColor(ContextCompat.getColor(context, R.color.notif_light_1));
            holder.main_container_trans.setAlpha(.5f);
        }
        if (theme == Helper.THEME_DARK) {
            style = R.style.DialogDark;
        } else if (theme == Helper.THEME_BLACK){
            style = R.style.DialogBlack;
        }else {
            style = R.style.Dialog;
        }
        //Follow Notification
        PeertubeAccountNotification accountAction = null;
        PeertubeVideoNotification videoAction = null;
        if( notification.getPeertubeActorFollow() != null){

            Helper.loadGiF(context, notification.getPeertubeActorFollow().getFollower().getAvatar(), holder.peertube_notif_pp);
            accountAction =notification.getPeertubeActorFollow().getFollower();
            String type = notification.getPeertubeActorFollow().getFollowing().getType();
            String message;
            if( type.equals("account")){
                message = context.getString(R.string.peertube_follow_channel, accountAction.getDisplayName(), notification.getPeertubeActorFollow().getFollowing().getDisplayName());
            }else{
                message = context.getString(R.string.peertube_follow_account, accountAction.getDisplayName());
            }
            holder.peertube_notif_message.setText(message);
            PeertubeAccountNotification finalAccountAction1 = accountAction;
            holder.peertube_notif_pp.setOnClickListener(v -> {
                Intent intent = new Intent(context, ShowAccountActivity.class);
                Bundle b = new Bundle();
                b.putBoolean("peertubeaccount", true);
                b.putString("accountId", finalAccountAction1.getName());
                intent.putExtras(b);
                context.startActivity(intent);
            });
        }else if(  notification.getPeertubeComment() != null){ //Comment Notification
            Helper.loadGiF(context, notification.getPeertubeComment().getPeertubeAccountNotification().getAvatar(), holder.peertube_notif_pp);
            accountAction = notification.getPeertubeComment().getPeertubeAccountNotification();
            videoAction  = notification.getPeertubeComment().getPeertubeVideoNotification();
            holder.peertube_notif_message.setText(context.getString(R.string.peertube_comment_on_video,accountAction.getDisplayName(), videoAction.getName()));
            PeertubeVideoNotification finalVideoAction1 = videoAction;
            holder.peertube_notif_message.setOnClickListener(v -> {
                Intent intent = new Intent(context, PeertubeActivity.class);
                Bundle b = new Bundle();
                b.putString("video_id", finalVideoAction1.getUuid());
                intent.putExtras(b);
                context.startActivity(intent);
            });
        }else {//Other Notifications
            if (notification.getPeertubeVideoNotification() != null && notification.getPeertubeVideoNotification().getPeertubeAccountNotification() != null){
                Helper.loadGiF(context, notification.getPeertubeVideoNotification().getPeertubeAccountNotification().getAvatar(), holder.peertube_notif_pp);
                accountAction = notification.getPeertubeVideoNotification().getPeertubeAccountNotification();
                videoAction  = notification.getPeertubeVideoNotification();
                String message = "";
                if (notification.getType() == DisplayPeertubeNotificationsFragment.MY_VIDEO_PUBLISHED){
                    message = context.getString(R.string.peertube_video_published, videoAction.getName());
                }else if(notification.getType() == DisplayPeertubeNotificationsFragment.MY_VIDEO_IMPORT_ERROR){
                    message = context.getString(R.string.peertube_video_import_error, videoAction.getName());
                }else if(notification.getType() == DisplayPeertubeNotificationsFragment.MY_VIDEO_IMPORT_SUCCESS){
                    message = context.getString(R.string.peertube_video_import_success, videoAction.getName());
                }else if(notification.getType() == DisplayPeertubeNotificationsFragment.NEW_VIDEO_FROM_SUBSCRIPTION){
                    message = context.getString(R.string.peertube_video_from_subscription, accountAction.getDisplayName(), videoAction.getName());
                }else if(notification.getType() == DisplayPeertubeNotificationsFragment.BLACKLIST_ON_MY_VIDEO){
                    message = context.getString(R.string.peertube_video_blacklist, videoAction.getName());
                }else if(notification.getType() == DisplayPeertubeNotificationsFragment.UNBLACKLIST_ON_MY_VIDEO){
                    message = context.getString(R.string.peertube_video_unblacklist, videoAction.getName());
                }
                holder.peertube_notif_message.setText(message);
                PeertubeVideoNotification finalVideoAction = videoAction;
                holder.peertube_notif_message.setOnClickListener(v -> {
                    Intent intent = new Intent(context, PeertubeActivity.class);
                    Bundle b = new Bundle();
                    b.putString("video_id", finalVideoAction.getUuid());
                    intent.putExtras(b);
                    context.startActivity(intent);
                });
            }
        }

        PeertubeAccountNotification finalAccountAction = accountAction;
        holder.peertube_notif_pp.setOnClickListener(v -> {
            if( finalAccountAction != null){
                Intent intent = new Intent(context, ShowAccountActivity.class);
                Bundle b = new Bundle();
                b.putBoolean("peertubeaccount", true);
                b.putString("accountId", finalAccountAction.getName());
                intent.putExtras(b);
                context.startActivity(intent);
            }
        });
    }

    private void notifyNotificationChanged(Notification notification){
        for (int i = 0; i < peertubeNotificationsListAdapter.getItemCount(); i++) {
            if (peertubeNotificationsListAdapter.getItemAt(i) != null && peertubeNotificationsListAdapter.getItemAt(i).getId().equals(notification.getId())) {
                try {
                    peertubeNotificationsListAdapter.notifyItemChanged(i);
                } catch (Exception ignored) { }
            }
        }
    }



    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }


    private PeertubeNotification getItemAt(int position){
        if( notifications.size() > position)
            return notifications.get(position);
        else
            return null;
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView peertube_notif_pp;
        TextView peertube_notif_message, peertube_notif_date;
        RelativeLayout main_container_trans;
        public View getView(){
            return itemView;
        }

        public ViewHolder(View itemView) {
            super(itemView);
            peertube_notif_pp = itemView.findViewById(R.id.peertube_notif_pp);
            peertube_notif_message = itemView.findViewById(R.id.peertube_notif_message);
            peertube_notif_date = itemView.findViewById(R.id.peertube_notif_date);
            main_container_trans = itemView.findViewById(R.id.container_trans);
        }
    }

}