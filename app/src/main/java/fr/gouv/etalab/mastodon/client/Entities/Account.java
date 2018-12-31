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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import es.dmoral.toasty.Toasty;
import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.activities.ShowAccountActivity;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveEmojiAccountInterface;

import static android.support.v4.text.HtmlCompat.FROM_HTML_MODE_LEGACY;
import static fr.gouv.etalab.mastodon.helper.Helper.THEME_BLACK;
import static fr.gouv.etalab.mastodon.helper.Helper.THEME_DARK;
import static fr.gouv.etalab.mastodon.helper.Helper.THEME_LIGHT;


/**
 * Created by Thomas on 23/04/2017.
 * Manage accounts
 */

public class Account implements Parcelable {

    private String id;
    private String username;
    private SpannableString displayNameSpan;
    private String acct;
    private String display_name, stored_displayname;
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
    private int metaDataSizeVerified;
    private LinkedHashMap<String, String> fields;
    private LinkedHashMap<String, Boolean> fieldsVerified;
    private LinkedHashMap<SpannableString, SpannableString> fieldsSpan;
    private List<Emojis> emojis;
    private String host;
    private boolean isBot;

    protected Account(Parcel in) {
        id = in.readString();
        username = in.readString();
        emojis = in.readArrayList(Emojis.class.getClassLoader());
        moved_to_account = in.readParcelable(Account.class.getClassLoader());
        acct = in.readString();
        display_name = in.readString();
        host =  in.readString();
        displayNameSpan = (SpannableString) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        noteSpan = (SpannableString) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        locked = in.readByte() != 0;
        isBot = in.readByte() != 0;
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
            if( fields == null)
                fields = new LinkedHashMap<>();
            String key = in.readString();
            String value = in.readString();
            fields.put(key,value);
        }
        metaDataSizeVerified = in.readInt();
        for(int i = 0; i < metaDataSizeVerified; i++){
            if( fieldsVerified == null)
                fieldsVerified = new LinkedHashMap<>();
            String key = in.readString();
            Boolean value = in.readByte() != 0;
            fieldsVerified.put(key,value);
        }

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(username);
        dest.writeList(emojis);
        dest.writeParcelable(moved_to_account, flags);
        dest.writeString(acct);
        dest.writeString(display_name);
        dest.writeString(host);
        TextUtils.writeToParcel(displayNameSpan, dest, flags);
        TextUtils.writeToParcel(noteSpan, dest, flags);
        dest.writeByte((byte) (locked ? 1 : 0));
        dest.writeByte((byte) (isBot ? 1 : 0));
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
        if( fields != null) {
            metaDataSize = fields.size();
            dest.writeInt(metaDataSize);
            for (Map.Entry<String, String> entry : fields.entrySet()) {
                dest.writeString(entry.getKey());
                dest.writeString(entry.getValue());
            }
        }
        if( fieldsVerified != null) {
            metaDataSizeVerified = fieldsVerified.size();
            dest.writeInt(metaDataSizeVerified);
            for (Map.Entry<String, Boolean> entry : fieldsVerified.entrySet()) {
                dest.writeString(entry.getKey());
                dest.writeByte((byte) (entry.getValue() ? 1 : 0));
            }
        }
    }

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

    public void setFields(LinkedHashMap<String, String> fields) {
        this.fields = fields;
    }

    public LinkedHashMap<String, String> getFields() {
        return fields;
    }

    public void setFieldsSpan(LinkedHashMap<SpannableString, SpannableString> fieldsSpan) {
        this.fieldsSpan = fieldsSpan;
    }


    public LinkedHashMap<SpannableString, SpannableString> getFieldsSpan() {
        return fieldsSpan;
    }

    public LinkedHashMap<String, Boolean> getFieldsVerified() {
        return fieldsVerified;
    }

    public void setFieldsVerified(LinkedHashMap<String, Boolean> fieldsVerified) {
        this.fieldsVerified = fieldsVerified;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public boolean isBot() {
        return isBot;
    }

    public void setBot(boolean bot) {
        isBot = bot;
    }

    public String getStored_displayname() {
        return stored_displayname;
    }

    public void setStored_displayname(String stored_displayname) {
        this.stored_displayname = stored_displayname;
    }


    public enum followAction{
        FOLLOW,
        NOT_FOLLOW,
        BLOCK,
        MUTE,
        REQUEST_SENT,
        NOTHING
    }




    public Account(){ }

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
            final Account idTar = this.getMoved_to_account();
            if( endPositionTar <= spannableString.toString().length() && endPositionTar >= startPositionTar)
                spannableString.setSpan(new ClickableSpan() {
                            @Override
                            public void onClick(View textView) {
                                Intent intent = new Intent(context, ShowAccountActivity.class);
                                Bundle b = new Bundle();
                                b.putParcelable("account", idTar);
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

    public void makeEmojisAccountProfile(final Context context, final OnRetrieveEmojiAccountInterface listener, Account account){
        if( ((Activity)context).isFinishing() )
            return;
        if( fields == null)
            fields = new LinkedHashMap<>();
        if( fieldsSpan == null)
            fieldsSpan = new LinkedHashMap<>();
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
                fieldsSpan.put(new SpannableString((String)pair.getKey()), fieldSpan);
            }
            account.setFieldsSpan(fieldsSpan);
        }
        Iterator it = fieldsSpan.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            SpannableString fieldSpan = (SpannableString) pair.getValue();
            SpannableString keySpan = (SpannableString) pair.getKey();
            Matcher matcher = Helper.xmppPattern.matcher(fieldSpan);
            SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
            while (matcher.find()){
                URLSpan[] urls = fieldSpan.getSpans(0, fieldSpan.length(), URLSpan.class);
                for(URLSpan span : urls)
                    fieldSpan.removeSpan(span);
                int matchStart = matcher.start(0);
                int matchEnd = matcher.end();
                final String url = fieldSpan.toString().substring(matchStart, matchEnd);
                if( matchEnd <= fieldSpan.toString().length() && matchEnd >= matchStart) {
                    fieldSpan.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(View textView) {
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                context.startActivity(intent);
                            }catch (Exception e){
                                Toasty.error(context, context.getString(R.string.toast_no_apps), Toast.LENGTH_LONG).show();
                            }
                        }
                        @Override
                        public void updateDrawState(TextPaint ds) {
                            super.updateDrawState(ds);
                            ds.setUnderlineText(false);
                            if (theme == THEME_DARK)
                                ds.setColor(ContextCompat.getColor(context, R.color.dark_link_toot));
                            else if (theme == THEME_BLACK)
                                ds.setColor(ContextCompat.getColor(context, R.color.black_link_toot));
                            else if (theme == THEME_LIGHT)
                                ds.setColor(ContextCompat.getColor(context, R.color.light_link_toot));
                        }
                    }, matchStart, matchEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    fieldsSpan.put(keySpan, fieldSpan);
                }

            }
            matcher = android.util.Patterns.EMAIL_ADDRESS.matcher(fieldSpan);
            while (matcher.find()){
                URLSpan[] urls = fieldSpan.getSpans(0, fieldSpan.length(), URLSpan.class);
                for(URLSpan span : urls)
                    fieldSpan.removeSpan(span);
                int matchStart = matcher.start(0);
                int matchEnd = matcher.end();
                final String email = fieldSpan.toString().substring(matchStart, matchEnd);
                if( matchEnd <= fieldSpan.toString().length() && matchEnd >= matchStart) {
                    fieldSpan.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(View textView) {
                            try {
                                final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                                emailIntent.setType("plain/text");
                                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{email});
                                context.startActivity(Intent.createChooser(emailIntent, context.getString(R.string.send_email)));
                            }catch (Exception e){
                                Toasty.error(context, context.getString(R.string.toast_no_apps), Toast.LENGTH_LONG).show();
                            }
                        }
                        @Override
                        public void updateDrawState(TextPaint ds) {
                            super.updateDrawState(ds);
                            ds.setUnderlineText(false);
                            if (theme == THEME_DARK)
                                ds.setColor(ContextCompat.getColor(context, R.color.dark_link_toot));
                            else if (theme == THEME_BLACK)
                                ds.setColor(ContextCompat.getColor(context, R.color.black_link_toot));
                            else if (theme == THEME_LIGHT)
                                ds.setColor(ContextCompat.getColor(context, R.color.light_link_toot));
                        }
                    }, matchStart, matchEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    fieldsSpan.put(keySpan, fieldSpan);
                }

            }
        }

        it = fieldsSpan.entrySet().iterator();
        fieldsVerified = account.getFieldsVerified();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            SpannableString fieldSpan = (SpannableString) pair.getValue();
            SpannableString keySpan = (SpannableString) pair.getKey();
            SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            Matcher matcher;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT)
                matcher = Patterns.WEB_URL.matcher(fieldSpan);
            else
                matcher = Helper.urlPattern.matcher(fieldSpan);
            while (matcher.find()){
                URLSpan[] urls = fieldSpan.getSpans(0, fieldSpan.length(), URLSpan.class);
                for(URLSpan span : urls)
                    fieldSpan.removeSpan(span);
                int matchStart = matcher.start(0);
                int matchEnd = matcher.end();
                final String url = fieldSpan.toString().substring(matchStart, matchEnd);
                if( matchEnd <= fieldSpan.toString().length() && matchEnd >= matchStart) {
                    int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
                    fieldSpan.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(View textView) {
                            Helper.openBrowser(context, url);
                        }
                        @Override
                        public void updateDrawState(TextPaint ds) {
                            super.updateDrawState(ds);
                            ds.setUnderlineText(false);

                            if (theme == THEME_DARK)
                                ds.setColor(ContextCompat.getColor(context, R.color.dark_link_toot));
                            else if (theme == THEME_BLACK)
                                ds.setColor(ContextCompat.getColor(context, R.color.black_link_toot));
                            else if (theme == THEME_LIGHT)
                                ds.setColor(ContextCompat.getColor(context, R.color.light_link_toot));

                        }
                    }, matchStart, matchEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    fieldsSpan.put(keySpan, fieldSpan);
                }

            }
        }



        final List<Emojis> emojis = account.getEmojis();
        if( emojis != null && emojis.size() > 0 ) {

            final int[] i = {0};
            for (final Emojis emoji : emojis) {
                fields = account.getFields();
                try {
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
                                            if (endPosition <= noteSpan.toString().length() && endPosition >= startPosition)
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
                                            if (endPosition <= displayNameSpan.toString().length() && endPosition >= startPosition)
                                                displayNameSpan.setSpan(
                                                        new ImageSpan(context,
                                                                Bitmap.createScaledBitmap(resource, (int) Helper.convertDpToPixel(20, context),
                                                                        (int) Helper.convertDpToPixel(20, context), false)), startPosition,
                                                        endPosition, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                                        }
                                    }
                                    Iterator it = fieldsSpan.entrySet().iterator();
                                    while (it.hasNext()) {
                                        Map.Entry pair = (Map.Entry) it.next();
                                        SpannableString fieldSpan = (SpannableString) pair.getValue();
                                        SpannableString keySpan = (SpannableString) pair.getKey();
                                        if (fieldSpan.toString().contains(targetedEmoji)) {
                                            //emojis can be used several times so we have to loop
                                            for (int startPosition = -1; (startPosition = fieldSpan.toString().indexOf(targetedEmoji, startPosition + 1)) != -1; startPosition++) {
                                                final int endPosition = startPosition + targetedEmoji.length();
                                                if (endPosition <= fieldSpan.toString().length() && endPosition >= startPosition)
                                                    fieldSpan.setSpan(
                                                            new ImageSpan(context,
                                                                    Bitmap.createScaledBitmap(resource, (int) Helper.convertDpToPixel(20, context),
                                                                            (int) Helper.convertDpToPixel(20, context), false)), startPosition,
                                                            endPosition, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                                            }
                                            fieldsSpan.put((SpannableString) pair.getKey(), fieldSpan);
                                        }else
                                            fieldsSpan.put(keySpan, fieldSpan);
                                    }

                                    i[0]++;
                                    if (i[0] == (emojis.size())) {
                                        if (noteSpan != null)
                                            account.setNoteSpan(noteSpan);
                                        account.setFieldsSpan(fieldsSpan);
                                        if (listener != null)
                                            listener.onRetrieveEmojiAccount(account);
                                    }
                                }
                            });
                }catch (Exception ignored){}

            }
        }else {
            if (listener != null)
                listener.onRetrieveEmojiAccount(account);
        }

    }


    public void makeAccountNameEmoji(final Context context, final OnRetrieveEmojiAccountInterface listener, Account account){
        if( ((Activity)context).isFinishing() )
            return;
        if( account.getDisplay_name() != null)
            displayNameSpan = new SpannableString(account.getDisplay_name());
        final List<Emojis> emojis = account.getEmojis();
        if( emojis != null && emojis.size() > 0 ) {
            final int[] i = {0};
            for (final Emojis emoji : emojis) {
                try {
                    Glide.with(context)
                            .asBitmap()
                            .load(emoji.getUrl())
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                                    final String targetedEmoji = ":" + emoji.getShortcode() + ":";
                                    if (displayNameSpan != null && displayNameSpan.toString().contains(targetedEmoji)) {
                                        //emojis can be used several times so we have to loop
                                        for (int startPosition = -1; (startPosition = displayNameSpan.toString().indexOf(targetedEmoji, startPosition + 1)) != -1; startPosition++) {
                                            final int endPosition = startPosition + targetedEmoji.length();
                                            if (endPosition <= displayNameSpan.toString().length() && endPosition >= startPosition)
                                                displayNameSpan.setSpan(
                                                        new ImageSpan(context,
                                                                Bitmap.createScaledBitmap(resource, (int) Helper.convertDpToPixel(20, context),
                                                                        (int) Helper.convertDpToPixel(20, context), false)), startPosition,
                                                        endPosition, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                                        }
                                    }
                                    i[0]++;
                                    if (i[0] == (emojis.size())) {
                                        if (listener != null)
                                            listener.onRetrieveEmojiAccount(account);
                                    }
                                }
                            });
                }catch (Exception ignored){}

            }
        }
    }


}
