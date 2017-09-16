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

import android.content.ClipData;
import android.content.ClipboardManager;
import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

import java.util.ArrayList;
import java.util.List;

import fr.gouv.etalab.mastodon.activities.MediaActivity;
import fr.gouv.etalab.mastodon.activities.ShowAccountActivity;
import fr.gouv.etalab.mastodon.activities.ShowConversationActivity;
import fr.gouv.etalab.mastodon.activities.TootActivity;
import fr.gouv.etalab.mastodon.asynctasks.PostActionAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.PostNotificationsAsyncTask;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Attachment;
import fr.gouv.etalab.mastodon.client.Entities.Error;
import fr.gouv.etalab.mastodon.interfaces.OnPostActionInterface;
import fr.gouv.etalab.mastodon.interfaces.OnPostNotificationsActionInterface;
import mastodon.etalab.gouv.fr.mastodon.R;
import fr.gouv.etalab.mastodon.client.Entities.Notification;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.helper.Helper;

import static fr.gouv.etalab.mastodon.helper.Helper.changeDrawableColor;


/**
 * Created by Thomas on 24/04/2017.
 * Adapter for Status
 */
public class NotificationsListAdapter extends BaseAdapter implements OnPostActionInterface, OnPostNotificationsActionInterface {

    private Context context;
    private List<Notification> notifications;
    private LayoutInflater layoutInflater;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private final int REBLOG = 1;
    private final int FAVOURITE = 2;
    private NotificationsListAdapter notificationsListAdapter;
    private int behaviorWithAttachments;
    private boolean isOnWifi;


    public NotificationsListAdapter(Context context, boolean isOnWifi, int behaviorWithAttachments, List<Notification> notifications){
        this.context = context;
        this.notifications = notifications;
        layoutInflater = LayoutInflater.from(this.context);
        imageLoader = ImageLoader.getInstance();
        notificationsListAdapter = this;
        this.isOnWifi = isOnWifi;
        this.behaviorWithAttachments = behaviorWithAttachments;
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
            holder.card_status_container = (CardView) convertView.findViewById(R.id.card_status_container);
            holder.notification_status_container = (LinearLayout) convertView.findViewById(R.id.notification_status_container);
            holder.status_document_container = (LinearLayout) convertView.findViewById(R.id.status_document_container);
            holder.notification_status_content = (TextView) convertView.findViewById(R.id.notification_status_content);
            holder.notification_account_username = (TextView) convertView.findViewById(R.id.notification_account_username);
            holder.notification_type = (TextView) convertView.findViewById(R.id.notification_type);
            holder.notification_account_profile = (ImageView) convertView.findViewById(R.id.notification_account_profile);
            holder.status_favorite_count = (TextView) convertView.findViewById(R.id.status_favorite_count);
            holder.status_reblog_count = (TextView) convertView.findViewById(R.id.status_reblog_count);
            holder.status_date = (TextView) convertView.findViewById(R.id.status_date);
            holder.status_reply = (ImageView) convertView.findViewById(R.id.status_reply);
            holder.status_privacy = (ImageView) convertView.findViewById(R.id.status_privacy);
            holder.notification_delete = (ImageView) convertView.findViewById(R.id.notification_delete);

            holder.status_show_more = (Button) convertView.findViewById(R.id.status_show_more);
            holder.status_prev1 = (ImageView) convertView.findViewById(R.id.status_prev1);
            holder.status_prev2 = (ImageView) convertView.findViewById(R.id.status_prev2);
            holder.status_prev3 = (ImageView) convertView.findViewById(R.id.status_prev3);
            holder.status_prev4 = (ImageView) convertView.findViewById(R.id.status_prev4);
            holder.status_prev1_play = (ImageView) convertView.findViewById(R.id.status_prev1_play);
            holder.status_prev2_play = (ImageView) convertView.findViewById(R.id.status_prev2_play);
            holder.status_prev3_play = (ImageView) convertView.findViewById(R.id.status_prev3_play);
            holder.status_prev4_play = (ImageView) convertView.findViewById(R.id.status_prev4_play);
            holder.status_container2 = (LinearLayout) convertView.findViewById(R.id.status_container2);
            holder.status_container3 = (LinearLayout) convertView.findViewById(R.id.status_container3);
            holder.status_prev4_container = (RelativeLayout) convertView.findViewById(R.id.status_prev4_container);
            holder.status_action_container = (LinearLayout) convertView.findViewById(R.id.status_action_container);
            holder.status_more = (ImageView) convertView.findViewById(R.id.status_more);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);

        int iconSizePercent = sharedpreferences.getInt(Helper.SET_ICON_SIZE, 130);
        int textSizePercent = sharedpreferences.getInt(Helper.SET_TEXT_SIZE, 110);


        final float scale = context.getResources().getDisplayMetrics().density;
        String type = notification.getType();
        String typeString = "";
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        Drawable imgH = null;
        switch (type){
            case "mention":
                holder.status_action_container.setVisibility(View.VISIBLE);
                if( notification.getAccount().getDisplay_name() != null && notification.getAccount().getDisplay_name().length() > 0)
                    typeString = String.format("%s %s", Helper.shortnameToUnicode(notification.getAccount().getDisplay_name(), true),context.getString(R.string.notif_mention));
                else
                    typeString = String.format("@%s %s", notification.getAccount().getAcct(),context.getString(R.string.notif_mention));
                if( theme == Helper.THEME_DARK){
                    holder.card_status_container.setCardBackgroundColor(ContextCompat.getColor(context, R.color.notif_dark_1));
                }else {
                    holder.card_status_container.setCardBackgroundColor(ContextCompat.getColor(context, R.color.notif_light_1));
                }
                imgH = null;
                break;
            case "reblog":
                holder.status_action_container.setVisibility(View.GONE);
                if( notification.getAccount().getDisplay_name() != null && notification.getAccount().getDisplay_name().length() > 0)
                    typeString = String.format("%s %s", Helper.shortnameToUnicode(notification.getAccount().getDisplay_name(), true),context.getString(R.string.notif_reblog));
                else
                    typeString = String.format("@%s %s", notification.getAccount().getAcct(),context.getString(R.string.notif_reblog));
                if( theme == Helper.THEME_DARK){
                    holder.card_status_container.setCardBackgroundColor(ContextCompat.getColor(context, R.color.notif_dark_2));
                }else {
                    holder.card_status_container.setCardBackgroundColor(ContextCompat.getColor(context, R.color.notif_light_2));
                }
                imgH = ContextCompat.getDrawable(context, R.drawable.ic_retweet_notif_header);
                break;
            case "favourite":
                holder.status_action_container.setVisibility(View.GONE);
                if( notification.getAccount().getDisplay_name() != null && notification.getAccount().getDisplay_name().length() > 0)
                    typeString = String.format("%s %s", Helper.shortnameToUnicode(notification.getAccount().getDisplay_name(), true),context.getString(R.string.notif_favourite));
                else
                    typeString = String.format("@%s %s", notification.getAccount().getAcct(),context.getString(R.string.notif_favourite));
                if( theme == Helper.THEME_DARK){
                    holder.card_status_container.setCardBackgroundColor(ContextCompat.getColor(context, R.color.notif_dark_3));
                }else {
                    holder.card_status_container.setCardBackgroundColor(ContextCompat.getColor(context, R.color.notif_light_3));
                }
                imgH = ContextCompat.getDrawable(context, R.drawable.ic_fav_notif_header);
                break;
            case "follow":
                holder.status_action_container.setVisibility(View.GONE);
                if( notification.getAccount().getDisplay_name() != null && notification.getAccount().getDisplay_name().length() > 0)
                    typeString = String.format("%s %s", Helper.shortnameToUnicode(notification.getAccount().getDisplay_name(), true),context.getString(R.string.notif_follow));
                else
                    typeString = String.format("@%s %s", notification.getAccount().getAcct(),context.getString(R.string.notif_follow));
                if( theme == Helper.THEME_DARK){
                    holder.card_status_container.setCardBackgroundColor(ContextCompat.getColor(context, R.color.notif_dark_4));
                }else {
                    holder.card_status_container.setCardBackgroundColor(ContextCompat.getColor(context, R.color.notif_light_4));
                }
                imgH = ContextCompat.getDrawable(context, R.drawable.ic_follow_notif_header);
                break;
        }
        changeDrawableColor(context, R.drawable.ic_retweet_notif_header,R.color.mastodonC4);
        changeDrawableColor(context, R.drawable.ic_fav_notif_header,R.color.mastodonC4);
        changeDrawableColor(context, R.drawable.ic_follow_notif_header,R.color.mastodonC4);
        holder.notification_type.setText(typeString);
        if( imgH != null) {
            holder.notification_type.setCompoundDrawablePadding((int)Helper.convertDpToPixel(5, context));
            imgH.setBounds(0, 0, (int) (20 * iconSizePercent / 100 * scale + 0.5f), (int) (20 * iconSizePercent / 100 * scale + 0.5f));
        }
        holder.notification_type.setCompoundDrawables( imgH, null, null, null);

        holder.status_privacy.getLayoutParams().height = (int) Helper.convertDpToPixel((20*iconSizePercent/100), context);
        holder.status_privacy.getLayoutParams().width = (int) Helper.convertDpToPixel((20*iconSizePercent/100), context);
        holder.status_reply.getLayoutParams().height = (int) Helper.convertDpToPixel((20*iconSizePercent/100), context);
        holder.status_reply.getLayoutParams().width = (int) Helper.convertDpToPixel((20*iconSizePercent/100), context);

        holder.notification_status_content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14*textSizePercent/100);
        holder.notification_type.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14*textSizePercent/100);
        holder.notification_account_username.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14*textSizePercent/100);
        holder.status_date.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12*textSizePercent/100);


        //Manages theme for icon colors
        if( theme == Helper.THEME_DARK){
            changeDrawableColor(context, R.drawable.ic_reply,R.color.dark_text);
            changeDrawableColor(context, R.drawable.ic_action_more,R.color.dark_text);
            changeDrawableColor(context, R.drawable.ic_action_globe,R.color.dark_text);
            changeDrawableColor(context, R.drawable.ic_action_lock_open,R.color.dark_text);
            changeDrawableColor(context, R.drawable.ic_action_lock_closed,R.color.dark_text);
            changeDrawableColor(context, R.drawable.ic_local_post_office,R.color.dark_text);
            changeDrawableColor(context, R.drawable.ic_retweet_black,R.color.dark_text);
            changeDrawableColor(context, R.drawable.ic_retweet,R.color.dark_text);
            changeDrawableColor(context, R.drawable.ic_fav_black,R.color.dark_text);
            changeDrawableColor(context, R.drawable.ic_photo,R.color.dark_text);
            changeDrawableColor(context, R.drawable.ic_remove_red_eye,R.color.dark_text);
            changeDrawableColor(context, R.drawable.ic_delete,R.color.dark_text);
        }else {
            changeDrawableColor(context, R.drawable.ic_reply,R.color.black);
            changeDrawableColor(context, R.drawable.ic_action_more,R.color.black);
            changeDrawableColor(context, R.drawable.ic_action_globe,R.color.black);
            changeDrawableColor(context, R.drawable.ic_action_lock_open,R.color.black);
            changeDrawableColor(context, R.drawable.ic_action_lock_closed,R.color.black);
            changeDrawableColor(context, R.drawable.ic_local_post_office,R.color.black);
            changeDrawableColor(context, R.drawable.ic_retweet_black,R.color.black);
            changeDrawableColor(context, R.drawable.ic_retweet,R.color.black);
            changeDrawableColor(context, R.drawable.ic_fav_black,R.color.black);
            changeDrawableColor(context, R.drawable.ic_photo,R.color.black);
            changeDrawableColor(context, R.drawable.ic_remove_red_eye,R.color.black);
            changeDrawableColor(context, R.drawable.ic_delete,R.color.black);
        }

        final Status status = notification.getStatus();
        if( status != null ){
            if( status.getMedia_attachments() == null || status.getMedia_attachments().size() < 1)
                holder.status_document_container.setVisibility(View.GONE);
            else
                holder.status_document_container.setVisibility(View.VISIBLE);
            if( (status.getIn_reply_to_account_id() != null && !status.getIn_reply_to_account_id().equals("null")) || (status.getIn_reply_to_id() != null && !status.getIn_reply_to_id().equals("null")) ){
                Drawable img = ContextCompat.getDrawable(context, R.drawable.ic_reply);
                img.setBounds(0,0,(int) (20 * iconSizePercent/100 * scale + 0.5f),(int) (15 * iconSizePercent/100 * scale + 0.5f));
                holder.notification_account_username.setCompoundDrawables( img, null, null, null);
            }else if( status.isReblogged()){
                Drawable img = ContextCompat.getDrawable(context, R.drawable.ic_retweet);
                img.setBounds(0,0,(int) (20 * iconSizePercent/100 * scale + 0.5f),(int) (15 * iconSizePercent/100 * scale + 0.5f));
                holder.notification_account_username.setCompoundDrawables( img, null, null, null);
            }else{
                holder.notification_account_username.setCompoundDrawables( null, null, null, null);
            }
            String content = status.getContent();
            content = content.replaceAll("</p>","<br/><br/>");
            content = content.replaceAll("<p>","");
            if( content.endsWith("<br/><br/>") )
                content = content.substring(0,content.length() -10);

            SpannableString spannableString = Helper.clickableElements(context, content,
                    status.getReblog() != null?status.getReblog().getMentions():status.getMentions(), true);
            holder.notification_status_content.setText(spannableString, TextView.BufferType.SPANNABLE);
            holder.notification_status_content.setMovementMethod(null);
            holder.notification_status_content.setMovementMethod(LinkMovementMethod.getInstance());
            holder.status_favorite_count.setText(String.valueOf(status.getFavourites_count()));
            holder.status_reblog_count.setText(String.valueOf(status.getReblogs_count()));
            holder.status_date.setText(Helper.dateDiff(context, status.getCreated_at()));

            //Adds attachment -> disabled, to enable them uncomment the line below
            //loadAttachments(status, holder);
            holder.notification_status_container.setVisibility(View.VISIBLE);
            holder.card_status_container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ShowConversationActivity.class);
                    Bundle b = new Bundle();
                    b.putString("statusId", status.getId());
                    intent.putExtras(b);
                    context.startActivity(intent);
                }
            });
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
            switch (status.getVisibility()){
                case "direct":
                case "private":
                    holder.status_reblog_count.setVisibility(View.GONE);
                    break;
                case "public":
                case "unlisted":
                    holder.status_reblog_count.setVisibility(View.VISIBLE);
                    break;
                default:
                    holder.status_reblog_count.setVisibility(View.VISIBLE);
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

            imgFav.setBounds(0,0,(int) (20 * iconSizePercent/100 * scale + 0.5f),(int) (20 * iconSizePercent/100 * scale + 0.5f));
            imgReblog.setBounds(0,0,(int) (20 * iconSizePercent/100 * scale + 0.5f),(int) (20 * iconSizePercent/100 * scale + 0.5f));
            holder.status_favorite_count.setCompoundDrawables(imgFav, null, null, null);
            holder.status_reblog_count.setCompoundDrawables(imgReblog, null, null, null);


            if( status.getReblog() == null) {
                if (status.getMedia_attachments().size() < 1) {
                    holder.status_document_container.setVisibility(View.GONE);
                    holder.status_show_more.setVisibility(View.GONE);
                } else {
                    //If medias are loaded without any conditions or if device is on wifi
                    if (!status.isSensitive() && (behaviorWithAttachments == Helper.ATTACHMENT_ALWAYS || (behaviorWithAttachments == Helper.ATTACHMENT_WIFI && isOnWifi))) {
                        loadAttachments(status, holder);
                        holder.status_show_more.setVisibility(View.GONE);
                        status.setAttachmentShown(true);
                    } else {
                        //Text depending if toots is sensitive or not
                        String textShowMore = (status.isSensitive()) ? context.getString(R.string.load_sensitive_attachment) : context.getString(R.string.load_attachment);
                        holder.status_show_more.setText(textShowMore);
                        if (!status.isAttachmentShown()) {
                            holder.status_show_more.setVisibility(View.VISIBLE);
                            holder.status_document_container.setVisibility(View.GONE);
                        } else {
                            loadAttachments(status, holder);
                        }
                    }
                }
            }else { //Attachments for reblogs
                if (status.getReblog().getMedia_attachments().size() < 1) {
                    holder.status_document_container.setVisibility(View.GONE);
                    holder.status_show_more.setVisibility(View.GONE);
                } else {
                    //If medias are loaded without any conditions or if device is on wifi
                    if (!status.getReblog().isSensitive() && (behaviorWithAttachments == Helper.ATTACHMENT_ALWAYS || (behaviorWithAttachments == Helper.ATTACHMENT_WIFI && isOnWifi))) {
                        loadAttachments(status.getReblog(), holder);
                        holder.status_show_more.setVisibility(View.GONE);
                        status.getReblog().setAttachmentShown(true);
                    } else {
                        //Text depending if toots is sensitive or not
                        String textShowMore = (status.getReblog().isSensitive()) ? context.getString(R.string.load_sensitive_attachment) : context.getString(R.string.load_attachment);
                        holder.status_show_more.setText(textShowMore);
                        if (!status.isAttachmentShown()) {
                            holder.status_show_more.setVisibility(View.VISIBLE);
                            holder.status_document_container.setVisibility(View.GONE);
                        } else {
                            loadAttachments(status.getReblog(), holder);
                        }
                    }
                }
            }
            holder.status_show_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadAttachments(status, holder);
                    holder.status_show_more.setVisibility(View.GONE);
                    status.setAttachmentShown(true);
                    notificationsListAdapter.notifyDataSetChanged();
                }
            });

        }else {
            holder.notification_status_container.setVisibility(View.GONE);
            holder.notification_account_username.setCompoundDrawables( null, null, null, null);
            holder.card_status_container.setOnClickListener(null);
        }


        holder.status_favorite_count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean confirmation = sharedpreferences.getBoolean(Helper.SET_NOTIF_VALIDATION_FAV, false);
                if( confirmation )
                    displayConfirmationDialog(FAVOURITE,status);
                else
                    favouriteAction(status);
            }
        });

        holder.status_reblog_count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean confirmation = sharedpreferences.getBoolean(Helper.SET_NOTIF_VALIDATION, true);
                if( confirmation )
                    displayConfirmationDialog(REBLOG,status);
                else
                    reblogAction(status);
            }
        });

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

        holder.notification_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayConfirmationNotificationDialog(notification);
            }
        });
        holder.notification_account_username.setText( String.format("@%s",notification.getAccount().getUsername()));

        holder.status_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreOptionDialog(status);
            }
        });

        if( theme == Helper.THEME_LIGHT) {
            holder.status_show_more.setTextColor(ContextCompat.getColor(context, R.color.white));
        }

        //Profile picture
        imageLoader.displayImage(notification.getAccount().getAvatar(), holder.notification_account_profile, options);
        return convertView;
    }

    /**
     * Display a validation message
     * @param action int
     * @param status Status
     */
    private void displayConfirmationDialog(final int action, final Status status){

        String title = null;
        if( action == FAVOURITE){
            if( status.isFavourited())
                title = context.getString(R.string.favourite_remove);
            else
                title = context.getString(R.string.favourite_add);
        }else if( action == REBLOG ){
            if( status.isReblogged())
                title = context.getString(R.string.reblog_remove);
            else
                title = context.getString(R.string.reblog_add);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            builder.setMessage(Html.fromHtml(status.getContent(), Html.FROM_HTML_MODE_LEGACY));
        else
            //noinspection deprecation
            builder.setMessage(Html.fromHtml(status.getContent()));
        builder.setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(title)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if( action == REBLOG)
                            reblogAction(status);
                        else if( action == FAVOURITE)
                            favouriteAction(status);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }

                })
                .show();
    }

    /**
     * Display a validation message for notification deletion
     * @param notification Notification
     */
    private void displayConfirmationNotificationDialog(final Notification notification){
        final ArrayList seletedItems = new ArrayList();

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(R.string.delete_notification_ask)
                .setMultiChoiceItems(new String[]{context.getString(R.string.delete_notification_ask_all)}, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
                        if (isChecked) {
                            //noinspection unchecked
                            seletedItems.add(indexSelected);
                        } else  {
                            if (seletedItems.contains(indexSelected))
                                seletedItems.remove(Integer.valueOf(indexSelected));
                        }

                    }
                }).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (seletedItems.size() > 0)
                            new PostNotificationsAsyncTask(context, null, NotificationsListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        else
                            new PostNotificationsAsyncTask(context, notification, NotificationsListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        dialog.dismiss();
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }

    /**
     * Favourites/Unfavourites a status
     * @param status Status
     */
    private void favouriteAction(Status status){
        if( status.isFavourited()){
            new PostActionAsyncTask(context, API.StatusAction.UNFAVOURITE, status.getId(), NotificationsListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            status.setFavourited(false);
        }else{
            new PostActionAsyncTask(context, API.StatusAction.FAVOURITE, status.getId(), NotificationsListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            status.setFavourited(true);
        }
        notificationsListAdapter.notifyDataSetChanged();
    }

    /**
     * Reblog/Unreblog a status
     * @param status Status
     */
    private void reblogAction(Status status){
        if( status.isReblogged()){
            new PostActionAsyncTask(context, API.StatusAction.UNREBLOG, status.getId(), NotificationsListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            status.setReblogged(false);
        }else{
            new PostActionAsyncTask(context, API.StatusAction.REBLOG, status.getId(), NotificationsListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            status.setReblogged(true);
        }
        notificationsListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPostAction(int statusCode, API.StatusAction statusAction, String targetedId, Error error) {
        if( error != null){
            final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            boolean show_error_messages = sharedpreferences.getBoolean(Helper.SET_SHOW_ERROR_MESSAGES, true);
            if( show_error_messages)
                Toast.makeText(context, error.getError(),Toast.LENGTH_LONG).show();
        }
        Helper.manageMessageStatusCode(context, statusCode, statusAction);
        //When muting or blocking an account, its status are removed from the list
        List<Notification> notificationsToRemove = new ArrayList<>();
        if( statusAction == API.StatusAction.MUTE || statusAction == API.StatusAction.BLOCK){
            for(Notification notification: notifications){
                if( notification.getType().equals("mention") && notification.getAccount().getId().equals(targetedId))
                    notificationsToRemove.add(notification);
            }
            notifications.removeAll(notificationsToRemove);
            notificationsListAdapter.notifyDataSetChanged();
        }
        if( statusAction == API.StatusAction.REBLOG){
            for(Notification notification: notifications){
                if( notification.getStatus().getId().equals(targetedId)) {
                    notification.getStatus().setReblogs_count(notification.getStatus().getReblogs_count() + 1);
                    break;
                }
            }
            notificationsListAdapter.notifyDataSetChanged();
        }else if( statusAction == API.StatusAction.UNREBLOG){
            for(Notification notification: notifications){
                if( notification.getStatus().getId().equals(targetedId)) {
                    notification.getStatus().setReblogs_count(notification.getStatus().getReblogs_count() - 1);
                    break;
                }
            }
            notificationsListAdapter.notifyDataSetChanged();
        }else if( statusAction == API.StatusAction.FAVOURITE){
            for(Notification notification: notifications){
                if( notification.getStatus().getId().equals(targetedId)) {
                    notification.getStatus().setFavourites_count(notification.getStatus().getFavourites_count() + 1);
                    break;
                }
            }
            notificationsListAdapter.notifyDataSetChanged();
        }else if( statusAction == API.StatusAction.UNFAVOURITE){
            for(Notification notification: notifications){
                if( notification.getStatus().getId().equals(targetedId)) {
                    notification.getStatus().setFavourites_count(notification.getStatus().getFavourites_count() - 1);
                    break;
                }
            }
            notificationsListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onPostNotificationsAction(APIResponse apiResponse, Notification notification) {
        if(apiResponse.getError() != null){
            Toast.makeText(context, R.string.toast_error,Toast.LENGTH_LONG).show();
            return;
        }
        if( notification != null){
            notifications.remove(notification);
            notificationsListAdapter.notifyDataSetChanged();
            Toast.makeText(context,R.string.delete_notification,Toast.LENGTH_LONG).show();
        }else{
            notifications.clear();
            notifications = new ArrayList<>();
            notificationsListAdapter.notifyDataSetChanged();
            Toast.makeText(context,R.string.delete_notification_all,Toast.LENGTH_LONG).show();
        }

    }

    private void loadAttachments(final Status status, ViewHolder holder){
        List<Attachment> attachments = status.getMedia_attachments();
        if( attachments != null && attachments.size() > 0){
            int i = 0;
            if( attachments.size() == 1){
                holder.status_container2.setVisibility(View.GONE);
                if( attachments.get(0).getUrl().trim().contains("missing.png"))
                    holder.status_document_container.setVisibility(View.GONE);
                else
                    holder.status_document_container.setVisibility(View.VISIBLE);
            }else if(attachments.size() == 2){
                holder.status_container2.setVisibility(View.VISIBLE);
                holder.status_container3.setVisibility(View.GONE);
                if( attachments.get(1).getUrl().trim().contains("missing.png"))
                    holder.status_container2.setVisibility(View.GONE);
                holder.status_document_container.setVisibility(View.VISIBLE);
            }else if( attachments.size() == 3){
                holder.status_container2.setVisibility(View.VISIBLE);
                holder.status_container3.setVisibility(View.VISIBLE);
                holder.status_prev4_container.setVisibility(View.GONE);
                if( attachments.get(2).getUrl().trim().contains("missing.png"))
                    holder.status_container3.setVisibility(View.GONE);
                holder.status_document_container.setVisibility(View.VISIBLE);
            }else {
                holder.status_prev4_container.setVisibility(View.VISIBLE);
                if( attachments.get(2).getUrl().trim().contains("missing.png"))
                    holder.status_prev4_container.setVisibility(View.GONE);
                holder.status_document_container.setVisibility(View.VISIBLE);
            }
            int position = 1;
            for(final Attachment attachment: attachments){
                ImageView imageView;
                if( i == 0) {
                    imageView = holder.status_prev1;
                    if( attachment.getType().equals("image"))
                        holder.status_prev1_play.setVisibility(View.GONE);
                    else
                        holder.status_prev1_play.setVisibility(View.VISIBLE);
                }else if( i == 1) {
                    imageView = holder.status_prev2;
                    if( attachment.getType().equals("image"))
                        holder.status_prev2_play.setVisibility(View.GONE);
                    else
                        holder.status_prev2_play.setVisibility(View.VISIBLE);
                }else if(i == 2) {
                    imageView = holder.status_prev3;
                    if( attachment.getType().equals("image"))
                        holder.status_prev3_play.setVisibility(View.GONE);
                    else
                        holder.status_prev3_play.setVisibility(View.VISIBLE);
                }else {
                    imageView = holder.status_prev4;
                    if( attachment.getType().equals("image"))
                        holder.status_prev4_play.setVisibility(View.GONE);
                    else
                        holder.status_prev4_play.setVisibility(View.VISIBLE);
                }
                String url = attachment.getPreview_url();
                if( url == null || url.trim().equals(""))
                    url = attachment.getUrl();
                if( !url.trim().contains("missing.png"))
                    imageLoader.displayImage(url, imageView, options);
                final int finalPosition = position;
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, MediaActivity.class);
                        Bundle b = new Bundle();
                        intent.putParcelableArrayListExtra("mediaArray", status.getMedia_attachments());
                        b.putInt("position", finalPosition);
                        intent.putExtras(b);
                        context.startActivity(intent);
                    }
                });
                i++;
                position++;
            }
        }else{
            holder.status_document_container.setVisibility(View.GONE);
        }
        holder.status_show_more.setVisibility(View.GONE);
    }


    private class ViewHolder {
        CardView card_status_container;
        TextView notification_status_content;
        TextView notification_type;
        TextView notification_account_username;
        ImageView notification_account_profile;
        ImageView notification_delete;
        TextView status_favorite_count;
        TextView status_reblog_count;
        TextView status_date;
        ImageView status_reply;
        LinearLayout status_document_container;
        LinearLayout status_action_container;
        Button status_show_more;
        ImageView status_prev1;
        ImageView status_prev2;
        ImageView status_prev3;
        ImageView status_prev4;
        ImageView status_prev1_play;
        ImageView status_prev2_play;
        ImageView status_prev3_play;
        ImageView status_prev4_play;
        ImageView status_more;
        RelativeLayout status_prev4_container;
        LinearLayout status_container2;
        LinearLayout status_container3;
        LinearLayout notification_status_container;
        ImageView status_privacy;
    }



    /**
     * More option for status (report / remove status / Mute / Block)
     * @param status Status current status
     */
    private void moreOptionDialog(final Status status){

        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        final boolean isOwner = status.getAccount().getId().equals(userId);
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);
        //builderSingle.setTitle(R.string.make_a_choice);
        final String[] stringArray, stringArrayConf;
        final API.StatusAction[] doAction;
        if( isOwner) {
            stringArray = context.getResources().getStringArray(R.array.more_action_owner);
            stringArrayConf = context.getResources().getStringArray(R.array.more_action_owner_confirm);
            doAction = new API.StatusAction[]{API.StatusAction.UNSTATUS};

        }else {
            stringArray = context.getResources().getStringArray(R.array.more_action);
            stringArrayConf = context.getResources().getStringArray(R.array.more_action_confirm);
            doAction = new API.StatusAction[]{API.StatusAction.MUTE,API.StatusAction.BLOCK,API.StatusAction.REPORT};
        }
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, stringArray);
        builderSingle.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AlertDialog.Builder builderInner = new AlertDialog.Builder(context);
                builderInner.setTitle(stringArrayConf[which]);
                if( isOwner) {
                    if( which == 0) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            builderInner.setMessage(Html.fromHtml(status.getContent(), Html.FROM_HTML_MODE_LEGACY));
                        else
                            //noinspection deprecation
                            builderInner.setMessage(Html.fromHtml(status.getContent()));
                    }else if( which == 1){
                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        String content;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            content = Html.fromHtml(status.getContent(), Html.FROM_HTML_MODE_LEGACY).toString();
                        else
                            //noinspection deprecation
                            content = Html.fromHtml(status.getContent()).toString();
                        ClipData clip = ClipData.newPlainText(Helper.CLIP_BOARD, content);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(context,R.string.clipboard,Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        return;
                    }else {
                        Intent sendIntent = new Intent(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.shared_via));
                        sendIntent.putExtra(Intent.EXTRA_TEXT, status.getUrl());
                        sendIntent.setType("text/plain");
                        context.startActivity(Intent.createChooser(sendIntent, context.getString(R.string.share_with)));
                        return;
                    }
                }else {
                    if( which < 2 ){
                        builderInner.setMessage(status.getAccount().getAcct());
                    }else if( which == 2) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            builderInner.setMessage(Html.fromHtml(status.getContent(), Html.FROM_HTML_MODE_LEGACY));
                        else
                            //noinspection deprecation
                            builderInner.setMessage(Html.fromHtml(status.getContent()));
                    }else if( which == 3 ){
                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        String content;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            content = Html.fromHtml(status.getContent(), Html.FROM_HTML_MODE_LEGACY).toString();
                        else
                            //noinspection deprecation
                            content = Html.fromHtml(status.getContent()).toString();
                        ClipData clip = ClipData.newPlainText(Helper.CLIP_BOARD, content);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(context,R.string.clipboard,Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        return;
                    }else {
                        Intent sendIntent = new Intent(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.shared_via));
                        sendIntent.putExtra(Intent.EXTRA_TEXT, status.getUrl());
                        sendIntent.setType("text/plain");
                        context.startActivity(Intent.createChooser(sendIntent, context.getString(R.string.share_with)));
                        return;
                    }
                }
                //Text for report
                EditText input = null;
                final int position = which;
                if( doAction[which] == API.StatusAction.REPORT){
                    input = new EditText(context);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    input.setLayoutParams(lp);
                    builderInner.setView(input);
                }
                builderInner.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int which) {
                        dialog.dismiss();
                    }
                });
                final EditText finalInput = input;
                builderInner.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int which) {
                        API.StatusAction statusAction = doAction[position];
                        if(statusAction ==  API.StatusAction.REPORT || statusAction == API.StatusAction.CREATESTATUS){
                            String comment = null;
                            if( finalInput != null && finalInput.getText() != null)
                                comment = finalInput.getText().toString();
                            new PostActionAsyncTask(context, statusAction, status.getId(), status, comment, NotificationsListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }else{
                            String targetedId;
                            if( doAction[position] == API.StatusAction.FAVOURITE ||
                                    doAction[position] == API.StatusAction.UNFAVOURITE ||
                                    doAction[position] == API.StatusAction.REBLOG ||
                                    doAction[position] == API.StatusAction.UNREBLOG ||
                                    doAction[position] == API.StatusAction.UNSTATUS
                                    )
                                targetedId = status.getId();
                            else
                                targetedId = status.getAccount().getId();
                            new PostActionAsyncTask(context, statusAction, targetedId, NotificationsListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                        dialog.dismiss();
                    }
                });
                builderInner.show();
            }
        });
        builderSingle.create().requestWindowFeature(Window.FEATURE_NO_TITLE);
        builderSingle.show();
    }

}