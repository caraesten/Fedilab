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
package app.fedilab.android.client.Entities;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;


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
import java.util.Date;
import java.util.List;

import app.fedilab.android.R;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.interfaces.OnRetrieveEmojiInterface;

import static app.fedilab.android.helper.Helper.drawableToBitmap;

/**
 * Created by Thomas on 23/04/2017.
 */

public class Notification implements Parcelable {

    private String id;
    private String type;
    private Date created_at;
    private Account account;
    private Status status;
    private boolean notificationAnimated = false;
    private boolean isEmojiFound = false;

    protected Notification(Parcel in) {
        id = in.readString();
        type = in.readString();
        account = in.readParcelable(Account.class.getClassLoader());
        status = in.readParcelable(Status.class.getClassLoader());
    }

    public Notification(){};

    public static final Creator<Notification> CREATOR = new Creator<Notification>() {
        @Override
        public Notification createFromParcel(Parcel in) {
            return new Notification(in);
        }

        @Override
        public Notification[] newArray(int size) {
            return new Notification[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(type);
        dest.writeParcelable(account, flags);
        dest.writeParcelable(status, flags);
    }

    @Override
    public boolean equals(Object otherNotifications) {
        return otherNotifications != null && (otherNotifications == this || otherNotifications instanceof Notification && this.getId().equals(((Notification) otherNotifications).getId()));
    }


    public static void makeEmojis(final Context context, final OnRetrieveEmojiInterface listener, Notification notification){

        if( ((Activity)context).isFinishing() )
            return;
        Status status = notification.getStatus();
        if (status == null)
            return;
        if( status.getReblog() == null &&  status.getEmojis() == null)
            return;
        final java.util.List<Emojis> emojis = status.getReblog() != null ? status.getReblog().getEmojis() : status.getEmojis();
        if( status.getReblog() != null && status.getReblog().getAccount() == null)
            return;
        if( status.getReblog() == null && status.getAccount() == null)
            return;
        final List<Emojis> emojisAccounts = status.getAccount().getEmojis();

        String typeString = "";
        switch (notification.getType()){
            case "mention":
                if( notification.getAccount().getDisplay_name() != null && notification.getAccount().getDisplay_name().length() > 0)
                    typeString = String.format("%s %s", Helper.shortnameToUnicode(notification.getAccount().getDisplay_name(), true),context.getString(R.string.notif_mention));
                else
                    typeString = String.format("@%s %s", notification.getAccount().getUsername(),context.getString(R.string.notif_mention));
                break;
            case "poll":
                typeString = context.getString(R.string.notif_poll);;
                break;
            case "reblog":
                if( notification.getAccount().getDisplay_name() != null && notification.getAccount().getDisplay_name().length() > 0)
                    typeString = String.format("%s %s", Helper.shortnameToUnicode(notification.getAccount().getDisplay_name(), true),context.getString(R.string.notif_reblog));
                else
                    typeString = String.format("@%s %s", notification.getAccount().getUsername(),context.getString(R.string.notif_reblog));
                break;
            case "favourite":
                if( notification.getAccount().getDisplay_name() != null && notification.getAccount().getDisplay_name().length() > 0)
                    typeString = String.format("%s %s", Helper.shortnameToUnicode(notification.getAccount().getDisplay_name(), true),context.getString(R.string.notif_favourite));
                else
                    typeString = String.format("@%s %s", notification.getAccount().getUsername(),context.getString(R.string.notif_favourite));
                break;
            case "follow":
                if( notification.getAccount().getDisplay_name() != null && notification.getAccount().getDisplay_name().length() > 0)
                    typeString = String.format("%s %s", Helper.shortnameToUnicode(notification.getAccount().getDisplay_name(), true),context.getString(R.string.notif_follow));
                else
                    typeString = String.format("@%s %s", notification.getAccount().getUsername(),context.getString(R.string.notif_follow));
                break;
        }
        SpannableString displayNameSpan = new SpannableString(typeString);
        SpannableString contentSpan = status.getContentSpan();
        SpannableString contentSpanCW = status.getContentSpanCW();
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        boolean disableAnimatedEmoji = sharedpreferences.getBoolean(Helper.SET_DISABLE_ANIMATED_EMOJI, false);

        if( emojisAccounts != null && emojisAccounts.size() > 0 ) {
            final int[] j = {0};
            for (final Emojis emoji : emojisAccounts) {
                Glide.with(context)
                    .asDrawable()
                    .load(emoji.getUrl())
                    .listener(new RequestListener<Drawable>()  {
                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                            j[0]++;
                            if( j[0] ==  (emojisAccounts.size())) {
                                listener.onRetrieveEmoji(notification);
                            }
                            return false;
                        }
                    })
                    .into(new SimpleTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            final String targetedEmoji = ":" + emoji.getShortcode() + ":";
                            if (displayNameSpan.toString().contains(targetedEmoji)) {
                                //emojis can be used several times so we have to loop
                                for (int startPosition = -1; (startPosition = displayNameSpan.toString().indexOf(targetedEmoji, startPosition + 1)) != -1; startPosition++) {
                                    final int endPosition = startPosition + targetedEmoji.length();
                                    if(endPosition <= displayNameSpan.toString().length() && endPosition >= startPosition) {
                                        resource.setBounds(0,0,(int) Helper.convertDpToPixel(20, context),(int) Helper.convertDpToPixel(20, context));
                                        resource.setVisible(true, true);
                                        ImageSpan imageSpan = new ImageSpan(resource);
                                        displayNameSpan.setSpan(
                                                imageSpan, startPosition,
                                                endPosition, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                                    }
                                }
                            }
                            j[0]++;
                            if( j[0] ==  (emojisAccounts.size())) {
                                notification.getAccount().setdisplayNameSpan(displayNameSpan);
                                listener.onRetrieveEmoji(notification);
                            }
                        }
                    });
            }
        }

        if( emojis != null && emojis.size() > 0 ) {
            final int[] i = {0};
            for (final Emojis emoji : emojis) {
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
                                i[0]++;
                                if( i[0] ==  (emojis.size())) {
                                    listener.onRetrieveEmoji(status,false);
                                }
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
                                final String targetedEmoji = ":" + emoji.getShortcode() + ":";
                                if (contentSpan != null && contentSpan.toString().contains(targetedEmoji)) {
                                    //emojis can be used several times so we have to loop
                                    for (int startPosition = -1; (startPosition = contentSpan.toString().indexOf(targetedEmoji, startPosition + 1)) != -1; startPosition++) {
                                        final int endPosition = startPosition + targetedEmoji.length();
                                        if( endPosition <= contentSpan.toString().length() && endPosition >= startPosition) {
                                            ImageSpan imageSpan;
                                            if( !disableAnimatedEmoji) {
                                                resource.setBounds(0, 0, (int) Helper.convertDpToPixel(20, context), (int) Helper.convertDpToPixel(20, context));
                                                resource.setVisible(true, true);
                                                imageSpan = new ImageSpan(resource);
                                            }else{
                                                resource.setVisible(true, true);
                                                Bitmap bitmap = drawableToBitmap(resource);
                                                imageSpan = new ImageSpan(context,
                                                        Bitmap.createScaledBitmap(bitmap, (int) Helper.convertDpToPixel(20, context),
                                                                (int) Helper.convertDpToPixel(20, context), false));
                                            }
                                            contentSpan.setSpan(
                                                    imageSpan, startPosition,
                                                    endPosition, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                                        }
                                    }
                                }
                                if (contentSpanCW != null && contentSpanCW.toString().contains(targetedEmoji)) {
                                    //emojis can be used several times so we have to loop
                                    for (int startPosition = -1; (startPosition = contentSpanCW.toString().indexOf(targetedEmoji, startPosition + 1)) != -1; startPosition++) {
                                        final int endPosition = startPosition + targetedEmoji.length();
                                        if( endPosition <= contentSpanCW.toString().length() && endPosition >= startPosition) {
                                            ImageSpan imageSpan;
                                            if( !disableAnimatedEmoji) {
                                                resource.setBounds(0, 0, (int) Helper.convertDpToPixel(20, context), (int) Helper.convertDpToPixel(20, context));
                                                resource.setVisible(true, true);
                                                imageSpan = new ImageSpan(resource);
                                            }else {
                                                resource.setVisible(true, true);
                                                Bitmap bitmap = drawableToBitmap(resource);
                                                imageSpan = new ImageSpan(context,
                                                        Bitmap.createScaledBitmap(bitmap, (int) Helper.convertDpToPixel(20, context),
                                                                (int) Helper.convertDpToPixel(20, context), false));
                                            }
                                            contentSpanCW.setSpan(
                                                    imageSpan, startPosition,
                                                    endPosition, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                                        }
                                    }
                                }
                                i[0]++;
                                if( i[0] ==  (emojis.size())) {
                                    status.setContentSpan(contentSpan);
                                    status.setContentSpanCW(contentSpanCW);
                                    status.setEmojiFound(true);
                                    listener.onRetrieveEmoji(notification);
                                }
                            }
                        });

            }
        }
    }

    public boolean isNotificationAnimated() {
        return notificationAnimated;
    }

    public void setNotificationAnimated(boolean notificationAnimated) {
        this.notificationAnimated = notificationAnimated;
    }

    public boolean isEmojiFound() {
        return isEmojiFound;
    }

    public void setEmojiFound(boolean emojiFound) {
        isEmojiFound = emojiFound;
    }
}
