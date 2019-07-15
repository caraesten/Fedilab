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
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.dmoral.toasty.Toasty;
import app.fedilab.android.R;
import app.fedilab.android.activities.HashTagActivity;
import app.fedilab.android.activities.ShowAccountActivity;
import app.fedilab.android.helper.CrossActions;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.interfaces.OnRetrieveEmojiAccountInterface;

import static androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY;
import static app.fedilab.android.helper.Helper.THEME_BLACK;
import static app.fedilab.android.helper.Helper.THEME_DARK;
import static app.fedilab.android.helper.Helper.THEME_LIGHT;
import static app.fedilab.android.helper.Helper.hashtagPattern;


/**
 * Created by Thomas on 23/04/2017.
 * Manage accounts
 */

public class Account implements Parcelable {

    private String id;
    private String uuid;
    private String username;
    private SpannableString displayNameSpan;
    private String acct;
    private String display_name, stored_displayname;
    private boolean locked;
    private Date created_at;
    private Date updated_at;
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
    private String social;
    private String client_id;
    private String client_secret;
    private String refresh_token;
    private boolean isModerator = false;
    private boolean isAdmin = false;
    private String privacy = "public";
    private boolean sensitive = false;

    private String locale;
    private String invite_request;
    private String created_by_application_id;
    private String invited_by_account_id;




    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.uuid);
        dest.writeString(this.username);
        TextUtils.writeToParcel(this.displayNameSpan, dest, flags);
        dest.writeString(this.acct);
        dest.writeString(this.display_name);
        dest.writeString(this.stored_displayname);
        dest.writeByte(this.locked ? (byte) 1 : (byte) 0);
        dest.writeLong(this.created_at != null ? this.created_at.getTime() : -1);
        dest.writeLong(this.updated_at != null ? this.updated_at.getTime() : -1);
        dest.writeInt(this.followers_count);
        dest.writeInt(this.following_count);
        dest.writeInt(this.statuses_count);
        dest.writeString(this.followers_count_str);
        dest.writeString(this.following_count_str);
        dest.writeString(this.statuses_count_str);
        dest.writeString(this.note);
        TextUtils.writeToParcel(this.noteSpan, dest, flags);
        dest.writeString(this.url);
        dest.writeString(this.avatar);
        dest.writeString(this.avatar_static);
        dest.writeString(this.header);
        dest.writeString(this.header_static);
        dest.writeString(this.token);
        dest.writeString(this.instance);
        dest.writeByte(this.isFollowing ? (byte) 1 : (byte) 0);
        dest.writeInt(this.followType == null ? -1 : this.followType.ordinal());
        dest.writeByte(this.isMakingAction ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.moved_to_account, flags);
        dest.writeByte(this.muting_notifications ? (byte) 1 : (byte) 0);
        dest.writeInt(this.metaDataSize);
        dest.writeInt(this.metaDataSizeVerified);
        dest.writeSerializable(this.fields);
        dest.writeSerializable(this.fieldsVerified);
        dest.writeSerializable(this.fieldsSpan);
        dest.writeTypedList(this.emojis);
        dest.writeString(this.host);
        dest.writeByte(this.isBot ? (byte) 1 : (byte) 0);
        dest.writeString(this.social);
        dest.writeString(this.client_id);
        dest.writeString(this.client_secret);
        dest.writeString(this.refresh_token);
        dest.writeByte(this.isModerator ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isAdmin ? (byte) 1 : (byte) 0);
        dest.writeString(this.privacy);
        dest.writeByte(this.sensitive ? (byte) 1 : (byte) 0);
        dest.writeString(this.locale);
        dest.writeString(this.invite_request);
        dest.writeString(this.created_by_application_id);
        dest.writeString(this.invited_by_account_id);
    }

    public Account() {
    }

    protected Account(Parcel in) {
        this.id = in.readString();
        this.uuid = in.readString();
        this.username = in.readString();
        this.displayNameSpan = (SpannableString) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        this.acct = in.readString();
        this.display_name = in.readString();
        this.stored_displayname = in.readString();
        this.locked = in.readByte() != 0;
        long tmpCreated_at = in.readLong();
        this.created_at = tmpCreated_at == -1 ? null : new Date(tmpCreated_at);
        long tmpUpdated_at = in.readLong();
        this.updated_at = tmpUpdated_at == -1 ? null : new Date(tmpUpdated_at);
        this.followers_count = in.readInt();
        this.following_count = in.readInt();
        this.statuses_count = in.readInt();
        this.followers_count_str = in.readString();
        this.following_count_str = in.readString();
        this.statuses_count_str = in.readString();
        this.note = in.readString();
        this.noteSpan = (SpannableString) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        this.url = in.readString();
        this.avatar = in.readString();
        this.avatar_static = in.readString();
        this.header = in.readString();
        this.header_static = in.readString();
        this.token = in.readString();
        this.instance = in.readString();
        this.isFollowing = in.readByte() != 0;
        int tmpFollowType = in.readInt();
        this.followType = tmpFollowType == -1 ? null : followAction.values()[tmpFollowType];
        this.isMakingAction = in.readByte() != 0;
        this.moved_to_account = in.readParcelable(Account.class.getClassLoader());
        this.muting_notifications = in.readByte() != 0;
        this.metaDataSize = in.readInt();
        this.metaDataSizeVerified = in.readInt();
        this.fields = (LinkedHashMap<String, String>) in.readSerializable();
        this.fieldsVerified = (LinkedHashMap<String, Boolean>) in.readSerializable();
        this.fieldsSpan = (LinkedHashMap<SpannableString, SpannableString>) in.readSerializable();
        this.emojis = in.createTypedArrayList(Emojis.CREATOR);
        this.host = in.readString();
        this.isBot = in.readByte() != 0;
        this.social = in.readString();
        this.client_id = in.readString();
        this.client_secret = in.readString();
        this.refresh_token = in.readString();
        this.isModerator = in.readByte() != 0;
        this.isAdmin = in.readByte() != 0;
        this.privacy = in.readString();
        this.sensitive =in.readByte() != 0;
        this.locale = in.readString();
        this.invite_request = in.readString();
        this.created_by_application_id = in.readString();
        this.invited_by_account_id = in.readString();
    }

    public static final Creator<Account> CREATOR = new Creator<Account>() {
        @Override
        public Account createFromParcel(Parcel source) {
            return new Account(source);
        }

        @Override
        public Account[] newArray(int size) {
            return new Account[size];
        }
    };

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

    public String getSocial() {
        return social;
    }

    public void setSocial(String social) {
        this.social = social;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getClient_secret() {
        return client_secret;
    }

    public void setClient_secret(String client_secret) {
        this.client_secret = client_secret;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public boolean isModerator() {
        return isModerator;
    }

    public void setModerator(boolean moderator) {
        isModerator = moderator;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public Date getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(Date updated_at) {
        this.updated_at = updated_at;
    }

    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

    public boolean isSensitive() {
        return sensitive;
    }

    public void setSensitive(boolean sensitive) {
        this.sensitive = sensitive;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getInvite_request() {
        return invite_request;
    }

    public void setInvite_request(String invite_request) {
        this.invite_request = invite_request;
    }

    public String getCreated_by_application_id() {
        return created_by_application_id;
    }

    public void setCreated_by_application_id(String created_by_application_id) {
        this.created_by_application_id = created_by_application_id;
    }

    public String getInvited_by_account_id() {
        return invited_by_account_id;
    }

    public void setInvited_by_account_id(String invited_by_account_id) {
        this.invited_by_account_id = invited_by_account_id;
    }


    public enum followAction{
        FOLLOW,
        NOT_FOLLOW,
        BLOCK,
        MUTE,
        REQUEST_SENT,
        NOTHING
    }


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
                            public void onClick(@NonNull View textView) {
                                Intent intent = new Intent(context, ShowAccountActivity.class);
                                Bundle b = new Bundle();
                                b.putParcelable("account", idTar);
                                intent.putExtras(b);
                                context.startActivity(intent);
                            }
                            @Override
                            public void updateDrawState(@NonNull TextPaint ds) {
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
        ArrayList<Account> accountsMentionUnknown = new ArrayList<>();
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
                Pattern aLink = Pattern.compile("(<\\s?a\\s?href=\"https?:\\/\\/([\\da-z\\.-]+\\.[a-z\\.]{2,10})\\/(@[\\/\\w._-]*)\"\\s?[^.]*<\\s?\\/\\s?a\\s?>)");
                Matcher matcherALink = aLink.matcher((String)pair.getValue());
                while (matcherALink.find()){
                    String acct = matcherALink.group(3).replace("@","");
                    String instance = matcherALink.group(2);
                    Account accountMention = new Account();
                    accountMention.setAcct(acct);
                    accountMention.setInstance(instance);
                    accountsMentionUnknown.add(accountMention);
                }
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
                        public void onClick(@NonNull View textView) {
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                context.startActivity(intent);
                            }catch (Exception e){
                                Toasty.error(context, context.getString(R.string.toast_no_apps), Toast.LENGTH_LONG).show();
                            }
                        }
                        @Override
                        public void updateDrawState(@NonNull TextPaint ds) {
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
                        public void onClick(@NonNull View textView) {
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
                        public void updateDrawState(@NonNull TextPaint ds) {
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
            matcher = hashtagPattern.matcher(fieldSpan);
            while (matcher.find()){
                URLSpan[] urls = fieldSpan.getSpans(0, fieldSpan.length(), URLSpan.class);
                for(URLSpan span : urls)
                    fieldSpan.removeSpan(span);
                int matchStart = matcher.start(1);
                int matchEnd = matcher.end();
                final String tag = fieldSpan.toString().substring(matchStart, matchEnd);
                if( matchEnd <= fieldSpan.toString().length() && matchEnd >= matchStart)
                    fieldSpan.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(@NonNull View textView) {
                            Intent intent = new Intent(context, HashTagActivity.class);
                            Bundle b = new Bundle();
                            b.putString("tag", tag.substring(1));
                            intent.putExtras(b);
                            context.startActivity(intent);
                        }
                        @Override
                        public void updateDrawState(@NonNull TextPaint ds) {
                            super.updateDrawState(ds);
                            ds.setUnderlineText(false);
                            if (theme == THEME_DARK)
                                ds.setColor(ContextCompat.getColor(context, R.color.dark_link_toot));
                            else if (theme == THEME_BLACK)
                                ds.setColor(ContextCompat.getColor(context, R.color.black_link_toot));
                            else if (theme == THEME_LIGHT)
                                ds.setColor(ContextCompat.getColor(context, R.color.mastodonC4));
                        }
                    }, matchStart, matchEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }
            if( accountsMentionUnknown.size() > 0 ) {
                for(Account accountMention: accountsMentionUnknown){
                    String targetedAccount = "@" + accountMention.getAcct();
                    if (fieldSpan.toString().toLowerCase().contains(targetedAccount.toLowerCase())) {
                        //Accounts can be mentioned several times so we have to loop
                        for(int startPosition = -1 ; (startPosition = fieldSpan.toString().toLowerCase().indexOf(targetedAccount.toLowerCase(), startPosition + 1)) != -1 ; startPosition++){
                            URLSpan[] urls = fieldSpan.getSpans(0, fieldSpan.length(), URLSpan.class);
                            for(URLSpan span : urls)
                                fieldSpan.removeSpan(span);
                            int endPosition = startPosition + targetedAccount.length();
                            if( endPosition <= fieldSpan.toString().length() && endPosition >= startPosition)
                                fieldSpan.setSpan(new ClickableSpan() {
                                @Override
                                public void onClick(@NonNull View textView) {
                                    CrossActions.doCrossProfile(context,accountMention);
                                }
                                @Override
                                public void updateDrawState(@NonNull TextPaint ds) {
                                    super.updateDrawState(ds);
                                    ds.setUnderlineText(false);
                                    if (theme == THEME_DARK)
                                        ds.setColor(ContextCompat.getColor(context, R.color.dark_link_toot));
                                    else if (theme == THEME_BLACK)
                                        ds.setColor(ContextCompat.getColor(context, R.color.black_link_toot));
                                    else if (theme == THEME_LIGHT)
                                        ds.setColor(ContextCompat.getColor(context, R.color.mastodonC4));
                                }
                            },
                            startPosition, endPosition,
                            Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        }
                    }
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
                        public void onClick(@NonNull View textView) {
                            Helper.openBrowser(context, url);
                        }
                        @Override
                        public void updateDrawState(@NonNull TextPaint ds) {
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

    @NotNull
    public String toString(){
        return this.getAcct()+ " - " + this.getUrl();
    }


}
