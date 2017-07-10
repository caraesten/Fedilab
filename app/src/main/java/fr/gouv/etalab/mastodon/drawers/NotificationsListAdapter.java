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
 * You should have received a copy of the GNU General Public License along with Thomas Schneider; if not,
 * see <http://www.gnu.org/licenses>. */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

import java.util.List;

import fr.gouv.etalab.mastodon.activities.ShowAccountActivity;
import fr.gouv.etalab.mastodon.activities.ShowConversationActivity;
import fr.gouv.etalab.mastodon.activities.TootActivity;
import mastodon.etalab.gouv.fr.mastodon.R;
import fr.gouv.etalab.mastodon.client.Entities.Notification;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.helper.Helper;

import static fr.gouv.etalab.mastodon.helper.Helper.changeDrawableColor;


/**
 * Created by Thomas on 24/04/2017.
 * Adapter for Status
 */
public class NotificationsListAdapter extends BaseAdapter  {

    private Context context;
    private List<Notification> notifications;
    private LayoutInflater layoutInflater;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;

    public NotificationsListAdapter(Context context, List<Notification> notifications){
        this.context = context;
        this.notifications = notifications;
        layoutInflater = LayoutInflater.from(this.context);
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder().displayer(new SimpleBitmapDisplayer()).cacheInMemory(false)
                .cacheOnDisk(true).resetViewBeforeLoading(true).build();
    }



    @Override
    public int getCount() {
        return notifications.size();
    }

    @Override
    public Object getItem(int position) {
        return notifications.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final Notification notification = notifications.get(position);
        final ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.drawer_notification, parent, false);
            holder = new ViewHolder();
            holder.notification_status_container = (LinearLayout) convertView.findViewById(R.id.notification_status_container);
            holder.status_document_container = (LinearLayout) convertView.findViewById(R.id.status_document_container);
            holder.notification_status_content = (TextView) convertView.findViewById(R.id.notification_status_content);
            holder.notification_account_username = (TextView) convertView.findViewById(R.id.notification_account_username);
            holder.notification_type = (TextView) convertView.findViewById(R.id.notification_type);
            holder.notification_account_displayname = (TextView) convertView.findViewById(R.id.notification_account_displayname);
            holder.notification_account_profile = (ImageView) convertView.findViewById(R.id.notification_account_profile);
            holder.status_favorite_count = (TextView) convertView.findViewById(R.id.status_favorite_count);
            holder.status_reblog_count = (TextView) convertView.findViewById(R.id.status_reblog_count);
            holder.status_date = (TextView) convertView.findViewById(R.id.status_date);
            holder.status_reply = (ImageView) convertView.findViewById(R.id.status_reply);
            holder.status_privacy = (ImageView) convertView.findViewById(R.id.status_privacy);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final float scale = context.getResources().getDisplayMetrics().density;
        String type = notification.getType();
        String typeString = "";
        switch (type){
            case "mention":
                typeString = String.format("@%s %s", notification.getAccount().getAcct(),context.getString(R.string.notif_mention));
                break;
            case "reblog":
                typeString = String.format("@%s %s", notification.getAccount().getAcct(),context.getString(R.string.notif_reblog));
                break;
            case "favourite":
                typeString = String.format("@%s %s", notification.getAccount().getAcct(),context.getString(R.string.notif_favourite));
                break;
            case "follow":
                typeString = String.format("@%s %s", notification.getAccount().getAcct(),context.getString(R.string.notif_follow));
                break;
        }
        holder.notification_type.setText(typeString);



        final Status status = notification.getStatus();
        if( status != null){
            if( status.getMedia_attachments().size() < 1)
                holder.status_document_container.setVisibility(View.GONE);
            else
                holder.status_document_container.setVisibility(View.VISIBLE);

            if( !status.getIn_reply_to_account_id().equals("null") || !status.getIn_reply_to_id().equals("null") ){
                Drawable img = ContextCompat.getDrawable(context, R.drawable.ic_reply);
                img.setBounds(0,0,(int) (20 * scale + 0.5f),(int) (15 * scale + 0.5f));
                holder.notification_account_displayname.setCompoundDrawables( img, null, null, null);
            }else if( status.isReblogged()){
                Drawable img = ContextCompat.getDrawable(context, R.drawable.ic_retweet);
                img.setBounds(0,0,(int) (20 * scale + 0.5f),(int) (15 * scale + 0.5f));
                holder.notification_account_displayname.setCompoundDrawables( img, null, null, null);
            }else{
                holder.notification_account_displayname.setCompoundDrawables( null, null, null, null);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                holder.notification_status_content.setText(Html.fromHtml(status.getContent(), Html.FROM_HTML_MODE_COMPACT));
            else
                //noinspection deprecation
                holder.notification_status_content.setText(Html.fromHtml(status.getContent()));
            holder.notification_status_content.setAutoLinkMask(Linkify.WEB_URLS);
            holder.status_favorite_count.setText(String.valueOf(status.getFavourites_count()));
            holder.status_reblog_count.setText(String.valueOf(status.getReblogs_count()));
            holder.status_date.setText(Helper.dateDiff(context, status.getCreated_at()));

            //Manages theme for icon colors
            final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
            if( theme == Helper.THEME_DARK){
                changeDrawableColor(context, R.drawable.ic_reply,R.color.dark_text);
                changeDrawableColor(context, R.drawable.ic_action_more,R.color.dark_text);
                changeDrawableColor(context, R.drawable.ic_action_globe,R.color.dark_text);
                changeDrawableColor(context, R.drawable.ic_action_lock_open,R.color.dark_text);
                changeDrawableColor(context, R.drawable.ic_action_lock_closed,R.color.dark_text);
                changeDrawableColor(context, R.drawable.ic_local_post_office,R.color.dark_text);
                changeDrawableColor(context, R.drawable.ic_retweet_black,R.color.dark_text);
                changeDrawableColor(context, R.drawable.ic_fav_black,R.color.dark_text);
                changeDrawableColor(context, R.drawable.ic_photo,R.color.dark_text);
                changeDrawableColor(context, R.drawable.ic_remove_red_eye,R.color.dark_text);
            }else {
                changeDrawableColor(context, R.drawable.ic_reply,R.color.black);
                changeDrawableColor(context, R.drawable.ic_action_more,R.color.black);
                changeDrawableColor(context, R.drawable.ic_action_globe,R.color.black);
                changeDrawableColor(context, R.drawable.ic_action_lock_open,R.color.black);
                changeDrawableColor(context, R.drawable.ic_action_lock_closed,R.color.black);
                changeDrawableColor(context, R.drawable.ic_local_post_office,R.color.black);
                changeDrawableColor(context, R.drawable.ic_retweet_black,R.color.black);
                changeDrawableColor(context, R.drawable.ic_fav_black,R.color.black);
                changeDrawableColor(context, R.drawable.ic_photo,R.color.black);
                changeDrawableColor(context, R.drawable.ic_remove_red_eye,R.color.black);
            }

            //Adds attachment -> disabled, to enable them uncomment the line below
            //loadAttachments(status, holder);
            holder.notification_status_container.setVisibility(View.VISIBLE);
            holder.notification_status_content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ShowConversationActivity.class);
                    Bundle b = new Bundle();
                    b.putString("statusId", status.getId());
                    intent.putExtras(b);
                    context.startActivity(intent);
                }
            });
            switch (status.getVisibility()){
                case "public":
                    holder.status_privacy.setImageResource(R.drawable.ic_action_globe);
                    break;
                case "unlisted":
                    holder.status_privacy.setImageResource(R.drawable.ic_action_lock_open);
                    break;
                case "private":
                    holder.status_privacy.setImageResource(R.drawable.ic_action_lock_closed);
                    break;
                case "direct":
                    holder.status_privacy.setImageResource(R.drawable.ic_local_post_office);
                    break;
            }
            Drawable imgFav, imgReblog;
            if( status.isFavourited())
                imgFav = ContextCompat.getDrawable(context, R.drawable.ic_fav_yellow);
            else
                imgFav = ContextCompat.getDrawable(context, R.drawable.ic_fav_black);

            if( status.isReblogged())
                imgReblog = ContextCompat.getDrawable(context, R.drawable.ic_retweet_yellow);
            else
                imgReblog = ContextCompat.getDrawable(context, R.drawable.ic_retweet_black);

            imgFav.setBounds(0,0,(int) (20 * scale + 0.5f),(int) (20 * scale + 0.5f));
            imgReblog.setBounds(0,0,(int) (20 * scale + 0.5f),(int) (20 * scale + 0.5f));
            holder.status_favorite_count.setCompoundDrawables(imgFav, null, null, null);
            holder.status_reblog_count.setCompoundDrawables(imgReblog, null, null, null);
        }else {
            holder.notification_status_container.setVisibility(View.GONE);
        }



        holder.notification_account_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ShowAccountActivity.class);
                Bundle b = new Bundle();
                b.putString("accountId", notification.getAccount().getId());
                intent.putExtras(b);
                context.startActivity(intent);
            }
        });
        holder.status_reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, TootActivity.class);
                Bundle b = new Bundle();
                b.putParcelable("tootReply", notification.getStatus());
                intent.putExtras(b);
                context.startActivity(intent);
            }
        });

        holder.notification_account_displayname.setText(Helper.shortnameToUnicode(notification.getAccount().getDisplay_name(), true));
        holder.notification_account_username.setText( String.format("@%s",notification.getAccount().getUsername()));
        //Profile picture
        imageLoader.displayImage(notification.getAccount().getAvatar(), holder.notification_account_profile, options);
        return convertView;
    }


    private class ViewHolder {
        TextView notification_status_content;
        TextView notification_type;
        TextView notification_account_username;
        TextView notification_account_displayname;
        ImageView notification_account_profile;
        TextView status_favorite_count;
        TextView status_reblog_count;
        TextView status_date;
        ImageView status_reply;
        LinearLayout status_document_container;
        LinearLayout notification_status_container;
        ImageView status_privacy;
    }

}