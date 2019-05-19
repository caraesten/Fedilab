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
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import java.util.Date;
import java.util.List;

import app.fedilab.android.helper.Helper;
import app.fedilab.android.interfaces.OnRetrieveEmojiInterface;

/**
 * Created by Thomas on 23/04/2017.
 */

public class Notification implements Parcelable {

    private String id;
    private String type;
    private Date created_at;
    private Account account;
    private Status status;

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
        final List<Emojis> emojisAccounts = status.getReblog() != null ?status.getReblog().getAccount().getEmojis():status.getAccount().getEmojis();

        status.getAccount().makeAccountNameEmoji(context, null, status.getAccount());

       // SpannableString displayNameSpan = status.getDisplayNameSpan();
        SpannableString contentSpan = status.getContentSpan();
        SpannableString contentSpanCW = status.getContentSpanCW();
        if( emojisAccounts != null)
            emojis.addAll(emojisAccounts);
        if( emojis != null && emojis.size() > 0 ) {
            final int[] i = {0};
            for (final Emojis emoji : emojis) {
                Glide.with(context)
                        .asBitmap()
                        .load(emoji.getUrl())
                        .listener(new RequestListener<Bitmap>()  {
                            @Override
                            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
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
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                                final String targetedEmoji = ":" + emoji.getShortcode() + ":";
                                if (contentSpan != null && contentSpan.toString().contains(targetedEmoji)) {
                                    //emojis can be used several times so we have to loop
                                    for (int startPosition = -1; (startPosition = contentSpan.toString().indexOf(targetedEmoji, startPosition + 1)) != -1; startPosition++) {
                                        final int endPosition = startPosition + targetedEmoji.length();
                                        if( endPosition <= contentSpan.toString().length() && endPosition >= startPosition)
                                            contentSpan.setSpan(
                                                    new ImageSpan(context,
                                                            Bitmap.createScaledBitmap(resource, (int) Helper.convertDpToPixel(20, context),
                                                                    (int) Helper.convertDpToPixel(20, context), false)), startPosition,
                                                    endPosition, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                                    }
                                }
                                /*if (displayNameSpan != null && displayNameSpan.toString().contains(targetedEmoji)) {
                                    //emojis can be used several times so we have to loop
                                    for (int startPosition = -1; (startPosition = displayNameSpan.toString().indexOf(targetedEmoji, startPosition + 1)) != -1; startPosition++) {
                                        final int endPosition = startPosition + targetedEmoji.length();
                                        if(endPosition <= displayNameSpan.toString().length() && endPosition >= startPosition)
                                            displayNameSpan.setSpan(
                                                    new ImageSpan(context,
                                                            Bitmap.createScaledBitmap(resource, (int) Helper.convertDpToPixel(20, context),
                                                                    (int) Helper.convertDpToPixel(20, context), false)), startPosition,
                                                    endPosition, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                                    }
                                }
                                status.setDisplayNameSpan(displayNameSpan);*/
                                if (contentSpanCW != null && contentSpanCW.toString().contains(targetedEmoji)) {
                                    //emojis can be used several times so we have to loop
                                    for (int startPosition = -1; (startPosition = contentSpanCW.toString().indexOf(targetedEmoji, startPosition + 1)) != -1; startPosition++) {
                                        final int endPosition = startPosition + targetedEmoji.length();
                                        if( endPosition <= contentSpan.toString().length() && endPosition >= startPosition)
                                            contentSpanCW.setSpan(
                                                    new ImageSpan(context,
                                                            Bitmap.createScaledBitmap(resource, (int) Helper.convertDpToPixel(20, context),
                                                                    (int) Helper.convertDpToPixel(20, context), false)), startPosition,
                                                    endPosition, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
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

}
