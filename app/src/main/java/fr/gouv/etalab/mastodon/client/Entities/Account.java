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
package fr.gouv.etalab.mastodon.client.Entities;

import android.app.Activity;
import android.content.*;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.activities.ShowAccountActivity;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveEmojiAccountInterface;

import static android.text.Html.FROM_HTML_MODE_LEGACY;

/**
 * Created by Thomas on 23/04/2017.
 * Manage accounts
 */

public class Account implements Parcelable {

    private String id;
    private String username;
    private SpannableString displayNameSpan;
    private String acct;
    private String display_name;
    private boolean locked;
    private Date created_at;
    private int followers_count;
    private int following_count;
    private int statuses_count;
    private String followers_count_str;
    private String following_count_str;
    private String statuses_count_str;
    private String note;
    private SpannableString noteSpan;
    private String url;
    private String avatar;
    private String avatar_static;
    private String header;
    private String header_static;
    private String token;
    private String instance;
    private boolean isFollowing;
    private followAction followType = followAction.NOTHING;
    private boolean isMakingAction = false;
    private Account moved_to_account;
    private boolean muting_notifications;
    private int metaDataSize;
    private HashMap<String, String> fields = new HashMap<>();
    private HashMap<String, SpannableString> fieldsSpan = new HashMap<>();
    private List<Emojis> emojis;
    private Account account;



    public followAction getFollowType() {
        return followType;
    }

    public void setFollowType(followAction followType) {
        this.followType = followType;
    }

    public boolean isMakingAction() {
        return isMakingAction;
    }

    public void setMakingAction(boolean makingAction) {
        isMakingAction = makingAction;
    }

    public Account getMoved_to_account() {
        return moved_to_account;
    }

    public void setMoved_to_account(Account moved_to_account) {
        this.moved_to_account = moved_to_account;
    }

    public boolean isMuting_notifications() {
        return muting_notifications;
    }

    public void setMuting_notifications(boolean muting_notifications) {
        this.muting_notifications = muting_notifications;
    }

    public void setFields(HashMap<String, String> fields) {
        this.fields = fields;
    }

    public HashMap<String, String> getFields() {
        return fields;
    }

    public void setFieldsSpan(HashMap<String, SpannableString> fieldsSpan) {
        this.fieldsSpan = fieldsSpan;
    }


    public HashMap<String, SpannableString> getFieldsSpan() {
        return fieldsSpan;
    }



    public enum followAction{
        FOLLOW,
        NOT_FOLLOW,
        BLOCK,
        MUTE,
        REQUEST_SENT,
        NOTHING
    }


    protected Account(Parcel in) {
        id = in.readString();
        username = in.readString();
        acct = in.readString();
        display_name = in.readString();
        locked = in.readByte() != 0;
        followers_count = in.readInt();
        following_count = in.readInt();
        statuses_count = in.readInt();
        note = in.readString();
        url = in.readString();
        avatar = in.readString();
        avatar_static = in.readString();
        header = in.readString();
        header_static = in.readString();
        token = in.readString();
        instance = in.readString();
        metaDataSize = in.readInt();
        for(int i = 0; i < metaDataSize; i++){
            String key = in.readString();
            String value = in.readString();
            fields.put(key,value);
        }
    }

    public Account(){
        this.account = this;
    }

    public static final Creator<Account> CREATOR = new Creator<Account>() {
        @Override
        public Account createFromParcel(Parcel in) {
            return new Account(in);
        }

        @Override
        public Account[] newArray(int size) {
            return new Account[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public SpannableString getdisplayNameSpan() {
        return displayNameSpan;
    }

    public void setdisplayNameSpan(SpannableString displayNameSpan) {
        this.displayNameSpan = displayNameSpan;
    }

    public String getAcct() {
        return acct;
    }

    public void setAcct(String acct) {
        this.acct = acct;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public int getFollowers_count() {
        return followers_count;
    }

    public void setFollowers_count(int followers_count) {
        this.followers_count = followers_count;
    }

    public int getFollowing_count() {
        return following_count;
    }

    public void setFollowing_count(int following_count) {
        this.following_count = following_count;
    }

    public int getStatuses_count() {
        return statuses_count;
    }

    public void setStatuses_count(int statuses_count) {
        this.statuses_count = statuses_count;
    }

    public SpannableString getNoteSpan() {
        return noteSpan;
    }

    public void setNoteSpan(SpannableString noteSpan) {
        this.noteSpan = noteSpan;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getAvatar_static() {
        return avatar_static;
    }

    public void setAvatar_static(String avatar_static) {
        this.avatar_static = avatar_static;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getHeader_static() {
        return header_static;
    }

    public void setHeader_static(String header_static) {
        this.header_static = header_static;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public List<Emojis> getEmojis() {
        return emojis;
    }

    public void setEmojis(List<Emojis> emojis) {
        this.emojis = emojis;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(username);
        dest.writeString(acct);
        dest.writeString(display_name);
        dest.writeByte((byte) (locked ? 1 : 0));
        dest.writeInt(followers_count);
        dest.writeInt(following_count);
        dest.writeInt(statuses_count);
        dest.writeString(note);
        dest.writeString(url);
        dest.writeString(avatar);
        dest.writeString(avatar_static);
        dest.writeString(header);
        dest.writeString(header_static);
        dest.writeString(token);
        dest.writeString(instance);

        metaDataSize = fields.size();
        dest.writeInt(metaDataSize);
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeString(entry.getValue());
        }

    }

    public boolean isFollowing() {
        return isFollowing;
    }

    public void setFollowing(boolean following) {
        isFollowing = following;
    }

    public String getFollowers_count_str() {
        return followers_count_str;
    }

    public void setFollowers_count_str(String followers_count_str) {
        this.followers_count_str = followers_count_str;
    }

    public String getFollowing_count_str() {
        return following_count_str;
    }

    public void setFollowing_count_str(String following_count_str) {
        this.following_count_str = following_count_str;
    }

    public String getStatuses_count_str() {
        return statuses_count_str;
    }

    public void setStatuses_count_str(String statuses_count_str) {
        this.statuses_count_str = statuses_count_str;
    }

    /**
     * Makes the move to account clickable
     * @param context Context
     * @return SpannableString
     */
    public SpannableString moveToText(final android.content.Context context){
        SpannableString spannableString = null;
        if( this.getMoved_to_account() != null) {
            spannableString = new SpannableString(context.getString(R.string.account_moved_to, this.getAcct(), "@"+this.getMoved_to_account().getAcct()));
            int startPositionTar = spannableString.toString().indexOf("@"+this.getMoved_to_account().getAcct());
            int endPositionTar = startPositionTar + ("@"+this.getMoved_to_account().getAcct()).length();
            final String idTar = this.getMoved_to_account().getId();
            spannableString.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(View textView) {
                            Intent intent = new Intent(context, ShowAccountActivity.class);
                            Bundle b = new Bundle();
                            b.putString("accountId", idTar);
                            intent.putExtras(b);
                            context.startActivity(intent);
                        }
                        @Override
                        public void updateDrawState(TextPaint ds) {
                            super.updateDrawState(ds);
                        }
                    },
                    startPositionTar, endPositionTar,
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return spannableString;
    }


    public void makeEmojisAccount(final Context context, final OnRetrieveEmojiAccountInterface listener){

        if( ((Activity)context).isFinishing() )
            return;

        noteSpan = account.getNoteSpan();

        if( account.getDisplay_name() != null)
            displayNameSpan = new SpannableString(account.getDisplay_name());

        if( account.getFields() != null && account.getFields().size() > 0) {
            Iterator it = account.getFields().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                SpannableString fieldSpan;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    fieldSpan = new SpannableString(Html.fromHtml((String)pair.getValue(), FROM_HTML_MODE_LEGACY));
                else
                    //noinspection deprecation
                    fieldSpan = new SpannableString(Html.fromHtml((String)pair.getValue()));
                fieldsSpan.put((String) pair.getKey(), fieldSpan);
            }
            account.setFieldsSpan(fieldsSpan);
        }
        final List<Emojis> emojis = account.getEmojis();
        if( emojis != null && emojis.size() > 0 ) {
            final int[] i = {0};
            for (final Emojis emoji : emojis) {
                fields = account.getFields();
                Glide.with(context)
                        .asBitmap()
                        .load(emoji.getUrl())
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                                final String targetedEmoji = ":" + emoji.getShortcode() + ":";

                                if (noteSpan != null && noteSpan.toString().contains(targetedEmoji)) {
                                    //emojis can be used several times so we have to loop
                                    for (int startPosition = -1; (startPosition = noteSpan.toString().indexOf(targetedEmoji, startPosition + 1)) != -1; startPosition++) {
                                        final int endPosition = startPosition + targetedEmoji.length();
                                        if(endPosition <= noteSpan.toString().length() && endPosition >= startPosition)
                                            noteSpan.setSpan(
                                                    new ImageSpan(context,
                                                            Bitmap.createScaledBitmap(resource, (int) Helper.convertDpToPixel(20, context),
                                                                    (int) Helper.convertDpToPixel(20, context), false)), startPosition,
                                                    endPosition, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                                    }
                                }
                                if (displayNameSpan != null && displayNameSpan.toString().contains(targetedEmoji)) {
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
                                Iterator it = fieldsSpan.entrySet().iterator();
                                while (it.hasNext()) {
                                    Map.Entry pair = (Map.Entry)it.next();
                                    SpannableString fieldSpan = (SpannableString) pair.getValue();
                                    if (fieldSpan.toString().contains(targetedEmoji)) {
                                        //emojis can be used several times so we have to loop
                                        for (int startPosition = -1; (startPosition = fieldSpan.toString().indexOf(targetedEmoji, startPosition + 1)) != -1; startPosition++) {
                                            final int endPosition = startPosition + targetedEmoji.length();
                                            if(endPosition <= fieldSpan.toString().length() && endPosition >= startPosition)
                                                fieldSpan.setSpan(
                                                        new ImageSpan(context,
                                                                Bitmap.createScaledBitmap(resource, (int) Helper.convertDpToPixel(20, context),
                                                                        (int) Helper.convertDpToPixel(20, context), false)), startPosition,
                                                        endPosition, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                                        }
                                        fieldsSpan.put((String)pair.getKey(), fieldSpan);
                                    }
                                }

                                i[0]++;
                                if( i[0] ==  (emojis.size())) {
                                    if( noteSpan != null)
                                        account.setNoteSpan(noteSpan);
                                    account.setFieldsSpan(fieldsSpan);
                                    if( listener != null)
                                        listener.onRetrieveEmojiAccount(account);
                                }
                            }
                        });

            }
        }
    }


}
