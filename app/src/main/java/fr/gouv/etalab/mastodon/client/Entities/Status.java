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
import android.support.annotation.Nullable;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.activities.HashTagActivity;
import fr.gouv.etalab.mastodon.activities.MainActivity;
import fr.gouv.etalab.mastodon.activities.PeertubeActivity;
import fr.gouv.etalab.mastodon.activities.ShowAccountActivity;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveFeedsAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.UpdateAccountInfoAsyncTask;
import fr.gouv.etalab.mastodon.helper.CrossActions;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveEmojiInterface;

import static fr.gouv.etalab.mastodon.helper.Helper.THEME_BLACK;
import static fr.gouv.etalab.mastodon.helper.Helper.THEME_DARK;
import static fr.gouv.etalab.mastodon.helper.Helper.THEME_LIGHT;

/**
 * Created by Thomas on 23/04/2017.
 * Manage Status (ie: toots)
 */

public class Status implements Parcelable{

    private String id;
    private String uri;
    private String url;
    private Account account;
    private String in_reply_to_id;
    private String in_reply_to_account_id;
    private Status reblog;
    private Date created_at;
    private int reblogs_count;
    private int favourites_count;
    private int replies_count;
    private boolean reblogged;
    private boolean favourited;
    private boolean muted;
    private boolean pinned;
    private boolean sensitive;
    private boolean bookmarked;
    private String visibility;
    private boolean attachmentShown = false;
    private boolean spoilerShown = false;
    private ArrayList<Attachment> media_attachments;
    private Attachment art_attachment;
    private List<Mention> mentions;
    private List<Emojis> emojis;
    private List<Tag> tags;
    private Application application;
    private Card card;
    private String language;
    private boolean isTranslated = false;
    private boolean isEmojiFound = false;
    private boolean isEmojiTranslateFound = false;
    private boolean isClickable = false;
    private boolean isTranslationShown = false;
    private boolean isNew = false;
    private boolean isVisible = true;
    private boolean fetchMore = false;
    private String content, contentCW, contentTranslated;
    private SpannableString contentSpan, displayNameSpan, contentSpanCW, contentSpanTranslated;
    private RetrieveFeedsAsyncTask.Type type;
    private int itemViewType;
    private String conversationId;
    private boolean isExpanded = false;
    private int numberLines = -1;
    private boolean showSpoiler = false;

    public Status(){}
    private List<String> conversationProfilePicture;
    private String webviewURL = null;

    private boolean isBoostAnimated = false, isFavAnimated = false;
    private String scheduled_at;
    private String contentType;
    private boolean isNotice = false;
    private Poll poll = null;

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.uri);
        dest.writeString(this.url);
        dest.writeParcelable(this.account, flags);
        dest.writeString(this.in_reply_to_id);
        dest.writeString(this.in_reply_to_account_id);
        dest.writeParcelable(this.reblog, flags);
        dest.writeLong(this.created_at != null ? this.created_at.getTime() : -1);
        dest.writeInt(this.reblogs_count);
        dest.writeInt(this.favourites_count);
        dest.writeInt(this.replies_count);
        dest.writeByte(this.reblogged ? (byte) 1 : (byte) 0);
        dest.writeByte(this.favourited ? (byte) 1 : (byte) 0);
        dest.writeByte(this.muted ? (byte) 1 : (byte) 0);
        dest.writeByte(this.pinned ? (byte) 1 : (byte) 0);
        dest.writeByte(this.sensitive ? (byte) 1 : (byte) 0);
        dest.writeByte(this.bookmarked ? (byte) 1 : (byte) 0);
        dest.writeString(this.visibility);
        dest.writeByte(this.attachmentShown ? (byte) 1 : (byte) 0);
        dest.writeByte(this.spoilerShown ? (byte) 1 : (byte) 0);
        dest.writeTypedList(this.media_attachments);
        dest.writeParcelable(this.art_attachment, flags);
        dest.writeTypedList(this.mentions);
        dest.writeTypedList(this.emojis);
        dest.writeTypedList(this.tags);
        dest.writeParcelable(this.application, flags);
        dest.writeParcelable(this.card, flags);
        dest.writeString(this.language);
        dest.writeByte(this.isTranslated ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isTranslationShown ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isNew ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isVisible ? (byte) 1 : (byte) 0);
        dest.writeByte(this.fetchMore ? (byte) 1 : (byte) 0);
        dest.writeString(this.content);
        dest.writeString(this.contentCW);
        dest.writeString(this.contentTranslated);
        TextUtils.writeToParcel(this.contentSpan, dest, flags);
        TextUtils.writeToParcel(this.displayNameSpan, dest, flags);
        TextUtils.writeToParcel(this.contentSpanCW, dest, flags);
        TextUtils.writeToParcel(this.contentSpanTranslated, dest, flags);
        dest.writeInt(this.type == null ? -1 : this.type.ordinal());
        dest.writeInt(this.itemViewType);
        dest.writeString(this.conversationId);
        dest.writeByte(this.isExpanded ? (byte) 1 : (byte) 0);
        dest.writeInt(this.numberLines);
        dest.writeStringList(this.conversationProfilePicture);
        dest.writeString(this.webviewURL);
        dest.writeByte(this.isBoostAnimated ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isFavAnimated ? (byte) 1 : (byte) 0);
        dest.writeString(this.scheduled_at);
        dest.writeString(this.contentType);
        dest.writeByte(this.showSpoiler ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isNotice ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.poll, flags);
    }

    protected Status(Parcel in) {
        this.id = in.readString();
        this.uri = in.readString();
        this.url = in.readString();
        this.account = in.readParcelable(Account.class.getClassLoader());
        this.in_reply_to_id = in.readString();
        this.in_reply_to_account_id = in.readString();
        this.reblog = in.readParcelable(Status.class.getClassLoader());
        long tmpCreated_at = in.readLong();
        this.created_at = tmpCreated_at == -1 ? null : new Date(tmpCreated_at);
        this.reblogs_count = in.readInt();
        this.favourites_count = in.readInt();
        this.replies_count = in.readInt();
        this.reblogged = in.readByte() != 0;
        this.favourited = in.readByte() != 0;
        this.muted = in.readByte() != 0;
        this.pinned = in.readByte() != 0;
        this.sensitive = in.readByte() != 0;
        this.bookmarked = in.readByte() != 0;
        this.visibility = in.readString();
        this.attachmentShown = in.readByte() != 0;
        this.spoilerShown = in.readByte() != 0;
        this.media_attachments = in.createTypedArrayList(Attachment.CREATOR);
        this.art_attachment = in.readParcelable(Attachment.class.getClassLoader());
        this.mentions = in.createTypedArrayList(Mention.CREATOR);
        this.emojis = in.createTypedArrayList(Emojis.CREATOR);
        this.tags = in.createTypedArrayList(Tag.CREATOR);
        this.application = in.readParcelable(Application.class.getClassLoader());
        this.card = in.readParcelable(Card.class.getClassLoader());
        this.language = in.readString();
        this.isTranslated = in.readByte() != 0;
        this.isTranslationShown = in.readByte() != 0;
        this.isNew = in.readByte() != 0;
        this.isVisible = in.readByte() != 0;
        this.fetchMore = in.readByte() != 0;
        this.content = in.readString();
        this.contentCW = in.readString();
        this.contentTranslated = in.readString();
        this.contentSpan = (SpannableString) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        this.displayNameSpan = (SpannableString) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        this.contentSpanCW = (SpannableString) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        this.contentSpanTranslated = (SpannableString) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        int tmpType = in.readInt();
        this.type = tmpType == -1 ? null : RetrieveFeedsAsyncTask.Type.values()[tmpType];
        this.itemViewType = in.readInt();
        this.conversationId = in.readString();
        this.isExpanded = in.readByte() != 0;
        this.numberLines = in.readInt();
        this.conversationProfilePicture = in.createStringArrayList();
        this.webviewURL = in.readString();
        this.isBoostAnimated = in.readByte() != 0;
        this.isFavAnimated = in.readByte() != 0;
        this.scheduled_at = in.readString();
        this.contentType = in.readString();
        this.showSpoiler = in.readByte() != 0;
        this.isNotice = in.readByte() != 0;
        this.poll = in.readParcelable(Poll.class.getClassLoader());
    }

    public static final Creator<Status> CREATOR = new Creator<Status>() {
        @Override
        public Status createFromParcel(Parcel source) {
            return new Status(source);
        }

        @Override
        public Status[] newArray(int size) {
            return new Status[size];
        }
    };


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getIn_reply_to_id() {
        return in_reply_to_id;
    }

    public void setIn_reply_to_id(String in_reply_to_id) {
        this.in_reply_to_id = in_reply_to_id;
    }

    public String getIn_reply_to_account_id() {
        return in_reply_to_account_id;
    }

    public void setIn_reply_to_account_id(String in_reply_to_account_id) {
        this.in_reply_to_account_id = in_reply_to_account_id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        //Remove UTM by default
        this.content = content.replaceAll("&amp;utm_\\w+=[0-9a-zA-Z._-]*", "");
        this.content = this.content.replaceAll("&utm_\\w+=[0-9a-zA-Z._-]*", "");
        this.content = this.content.replaceAll("\\?utm_\\w+=[0-9a-zA-Z._-]*", "?");
    }

    public Status getReblog() {
        return reblog;
    }

    public void setReblog(Status reblog) {
        this.reblog = reblog;
    }

    public int getReblogs_count() {
        return reblogs_count;
    }

    public void setReblogs_count(int reblogs_count) {
        this.reblogs_count = reblogs_count;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public int getFavourites_count() {
        return favourites_count;
    }

    public void setFavourites_count(int favourites_count) {
        this.favourites_count = favourites_count;
    }

    public SpannableString getDisplayNameSpan(){
        return this.displayNameSpan;
    }
    public void setDisplayNameSpan(SpannableString displayNameSpan){
        this.displayNameSpan = displayNameSpan;
    }

    public boolean isReblogged() {
        return reblogged;
    }

    public void setReblogged(boolean reblogged) {
        this.reblogged = reblogged;
    }

    public boolean isFavourited() {
        return favourited;
    }

    public void setFavourited(boolean favourited) {
        this.favourited = favourited;
    }

    public void setPinned(boolean pinned) { this.pinned = pinned; }

    public boolean isPinned() { return pinned; }

    public boolean isSensitive() {
        return sensitive;
    }

    public void setSensitive(boolean sensitive) {
        this.sensitive = sensitive;
    }

    public String getSpoiler_text() {
        return contentCW;
    }

    public void setSpoiler_text(String spoiler_text) {
        this.contentCW = spoiler_text;
    }


    public ArrayList<Attachment> getMedia_attachments() {
        return media_attachments;
    }

    public void setMedia_attachments(ArrayList<Attachment> media_attachments) {
        this.media_attachments = media_attachments;
    }

    public List<Mention> getMentions() {
        return mentions;
    }

    public void setMentions(List<Mention> mentions) {
        this.mentions = mentions;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public String getTagsString() {
        //iterate through tags and create comma delimited string of tag names
        String tag_names = "";
        for (Tag t : tags) {
            if (tag_names.equals("")) {
                tag_names = t.getName();
            } else {
                tag_names = tag_names + ", " + t.getName();
            }
        }
        return tag_names;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }


    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public boolean isAttachmentShown() {
        return attachmentShown;
    }

    public void setAttachmentShown(boolean attachmentShown) {
        this.attachmentShown = attachmentShown;
    }


    public boolean isSpoilerShown() {
        return spoilerShown;
    }

    public void setSpoilerShown(boolean spoilerShown) {
        this.spoilerShown = spoilerShown;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isTranslated() {
        return isTranslated;
    }

    public void setTranslated(boolean translated) {
        isTranslated = translated;
    }

    public boolean isTranslationShown() {
        return isTranslationShown;
    }

    public void setTranslationShown(boolean translationShown) {
        isTranslationShown = translationShown;
    }

    public String getContentTranslated() {
        return contentTranslated;
    }

    public void setContentTranslated(String content_translated) {
        this.contentTranslated = content_translated;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public List<Emojis> getEmojis() {
        return emojis;
    }

    public void setEmojis(List<Emojis> emojis) {
        this.emojis = emojis;
    }


    public boolean isEmojiFound() {
        return isEmojiFound;
    }





    public void setEmojiFound(boolean emojiFound) {
        isEmojiFound = emojiFound;
    }


    public static void transform(Context context, Status status){

        if( ((Activity)context).isFinishing() || status == null)
            return;
        SpannableString spannableStringContent, spannableStringCW;
        if( (status.getReblog() != null && status.getReblog().getContent() == null) || (status.getReblog() == null && status.getContent() == null))
            return;
        spannableStringContent = new SpannableString(status.getReblog() != null ?status.getReblog().getContent():status.getContent());
        String spoilerText = "";
        if( status.getReblog() != null && status.getReblog().getSpoiler_text() != null)
            spoilerText = status.getReblog().getSpoiler_text();
        else if( status.getSpoiler_text() != null)
            spoilerText = status.getSpoiler_text();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            spannableStringCW = new SpannableString(Html.fromHtml(spoilerText, Html.FROM_HTML_MODE_LEGACY));
        else
            //noinspection deprecation
            spannableStringCW = new SpannableString(Html.fromHtml(spoilerText));
        if( spannableStringContent.length() > 0)
            status.setContentSpan(treatment(context, spannableStringContent, status));
        if( spannableStringCW.length() > 0)
            status.setContentSpanCW(treatment(context, spannableStringCW, status));
        SpannableString displayNameSpan = new SpannableString(status.reblog!=null?status.getReblog().getAccount().getDisplay_name():status.getAccount().getDisplay_name());
        status.setDisplayNameSpan(displayNameSpan);
        status.setClickable(true);
    }


    public static void transformTranslation(Context context, Status status){

        if( ((Activity)context).isFinishing() || status == null)
            return;
        if( (status.getReblog() != null && status.getReblog().getContent() == null) || (status.getReblog() == null && status.getContent() == null))
            return;
        SpannableString spannableStringTranslated;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            spannableStringTranslated = new SpannableString(Html.fromHtml(status.getContentTranslated(), Html.FROM_HTML_MODE_LEGACY));
        else
            //noinspection deprecation
            spannableStringTranslated = new SpannableString(Html.fromHtml(status.getContentTranslated()));

        status.setContentSpanTranslated(treatment(context, spannableStringTranslated, status));
        String displayName;
        if( status.getReblog() != null){
            displayName = status.getReblog().getAccount().getDisplay_name();
        }else {
            displayName = status.getAccount().getDisplay_name();
        }
        SpannableString contentSpanTranslated = status.getContentSpanTranslated();
        Matcher matcherALink = Patterns.WEB_URL.matcher(contentSpanTranslated.toString());
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        while (matcherALink.find()){
            int matchStart = matcherALink.start();
            int matchEnd = matcherALink.end();
            final String url = contentSpanTranslated.toString().substring(matcherALink.start(1), matcherALink.end(1));
            if( matchEnd <= contentSpanTranslated.toString().length() && matchEnd >= matchStart)
                contentSpanTranslated.setSpan(new ClickableSpan() {
                            @Override
                            public void onClick(@NonNull View textView) {
                                String finalUrl = url;
                                if( !url.startsWith("http://") && ! url.startsWith("https://"))
                                    finalUrl = "http://" + url;
                                Helper.openBrowser(context, finalUrl);
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
                        },
                        matchStart, matchEnd,
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

        }
        status.setContentSpanTranslated(contentSpanTranslated);
        SpannableString displayNameSpan = new SpannableString(displayName);
        status.setDisplayNameSpan(displayNameSpan);
    }


    public static void makeEmojis(final Context context, final OnRetrieveEmojiInterface listener, Status status){

        if( ((Activity)context).isFinishing() )
            return;
        if( status.getAccount() == null)
            return;
        if(  status.getReblog() != null && status.getReblog().getEmojis() == null)
            return;
        if( status.getReblog() == null &&  status.getEmojis() == null)
            return;
        final List<Emojis> emojis = status.getReblog() != null ? status.getReblog().getEmojis() : status.getEmojis();
        final List<Emojis> emojisAccounts = status.getReblog() != null ?status.getReblog().getAccount().getEmojis():status.getAccount().getEmojis();

        status.getAccount().makeAccountNameEmoji(context, null, status.getAccount());

        SpannableString displayNameSpan = status.getDisplayNameSpan();
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
                                status.setDisplayNameSpan(displayNameSpan);
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
                                    listener.onRetrieveEmoji(status, false);
                                }
                            }
                        });

            }
        }
    }


    public static void makeEmojisTranslation(final Context context, final OnRetrieveEmojiInterface listener, Status status){

        if( ((Activity)context).isFinishing() )
            return;
        SpannableString spannableStringTranslated = null;
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        if( status.getContentTranslated() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                spannableStringTranslated = new SpannableString(Html.fromHtml(status.getContentTranslated(), Html.FROM_HTML_MODE_LEGACY));
            else
                //noinspection deprecation
                spannableStringTranslated = new SpannableString(Html.fromHtml(status.getContentTranslated()));
        }

        final List<Emojis> emojis = status.getReblog() != null ? status.getReblog().getEmojis() : status.getEmojis();
        if( emojis != null && emojis.size() > 0 ) {
            final int[] i = {0};
            for (final Emojis emoji : emojis) {
                final SpannableString finalSpannableStringTranslated = spannableStringTranslated;
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
                                    if( finalSpannableStringTranslated != null)
                                        status.setContentSpanTranslated(finalSpannableStringTranslated);
                                    listener.onRetrieveEmoji(status, true);
                                }
                                return false;
                            }
                        })
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                                final String targetedEmoji = ":" + emoji.getShortcode() + ":";

                                if (finalSpannableStringTranslated != null && finalSpannableStringTranslated.toString().contains(targetedEmoji)) {
                                    //emojis can be used several times so we have to loop
                                    for (int startPosition = -1; (startPosition = finalSpannableStringTranslated.toString().indexOf(targetedEmoji, startPosition + 1)) != -1; startPosition++) {
                                        final int endPosition = startPosition + targetedEmoji.length();
                                        if( endPosition <= finalSpannableStringTranslated.toString().length() && endPosition >= startPosition)
                                            finalSpannableStringTranslated.setSpan(
                                                new ImageSpan(context,
                                                        Bitmap.createScaledBitmap(resource, (int) Helper.convertDpToPixel(20, context),
                                                                (int) Helper.convertDpToPixel(20, context), false)), startPosition,
                                                endPosition, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                                    }
                                }
                                i[0]++;
                                if( i[0] ==  (emojis.size())) {
                                    if( finalSpannableStringTranslated != null)
                                        status.setContentSpanTranslated(finalSpannableStringTranslated);
                                    status.setEmojiTranslateFound(true);
                                    listener.onRetrieveEmoji(status, true);
                                }
                            }
                        });

            }
        }
    }



    private static SpannableString treatment(final Context context, final SpannableString spannableString, Status status){

        URLSpan[] urls = spannableString.getSpans(0, spannableString.length(), URLSpan.class);
        for(URLSpan span : urls)
            spannableString.removeSpan(span);
        List<Mention> mentions = status.getReblog() != null ? status.getReblog().getMentions() : status.getMentions();

        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);

        Matcher matcher;
        Pattern linkPattern = Pattern.compile("<a href=\"([^\"]*)\"[^>]*(((?!<\\/a).)*)<\\/a>");
        matcher = linkPattern.matcher(spannableString);
        HashMap<String, String> targetedURL = new HashMap<>();
        while (matcher.find()){
            String key;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                key = new SpannableString(Html.fromHtml(matcher.group(2), Html.FROM_HTML_MODE_LEGACY)).toString();
            else
                //noinspection deprecation
                key = new SpannableString(Html.fromHtml(matcher.group(2))).toString();
            key = key.substring(1);
            if( !key.startsWith("#") && !key.startsWith("@") && !key.trim().equals("") && !matcher.group(1).contains("search?tag=")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    targetedURL.put(key, Html.fromHtml(matcher.group(1), Html.FROM_HTML_MODE_LEGACY).toString());
                }
                else
                    targetedURL.put(key, Html.fromHtml(matcher.group(1)).toString());
            }
        }
        String currentInstance = Helper.getLiveInstance(context);
        //Get url to account that are unknown
        Pattern aLink = Pattern.compile("(<\\s?a\\s?href=\"https?:\\/\\/([\\da-z\\.-]+\\.[a-z\\.]{2,10})\\/(@[\\/\\w._-]*)\"\\s?[^.]*<\\s?\\/\\s?a\\s?>)");
        Matcher matcherALink = aLink.matcher(spannableString.toString());
        ArrayList<Account> accountsMentionUnknown = new ArrayList<>();
        while (matcherALink.find()){
            String acct = matcherALink.group(3).replace("@","");
            String instance = matcherALink.group(2);
            boolean addAccount = true;
            for(Mention mention: mentions){
                String[] accountMentionAcct = mention.getAcct().split("@");
                //Different isntance
                if( accountMentionAcct.length > 1){
                    if( mention.getAcct().equals(acct+"@"+instance))
                        addAccount = false;
                }else {
                    if( (mention.getUsername()+"@"+currentInstance).equals(acct+"@"+instance))
                        addAccount = false;
                }
            }
            if( addAccount) {
                Account account = new Account();
                account.setAcct(acct);
                account.setInstance(instance);
                accountsMentionUnknown.add(account);
            }
        }
        aLink = Pattern.compile("(<\\s?a\\s?href=\"(https?:\\/\\/[\\da-z\\.-]+\\.[a-z\\.]{2,10}[\\/]?[^\"@(\\/tags\\/)]*)\"\\s?[^.]*<\\s?\\/\\s?a\\s?>)");
        matcherALink = aLink.matcher(spannableString.toString());

        while (matcherALink.find()){
            int matchStart = matcherALink.start();
            int matchEnd = matcherALink.end();
            final String url = spannableString.toString().substring(matcherALink.start(1), matcherALink.end(1));

            if( matchEnd <= spannableString.toString().length() && matchEnd >= matchStart)
                spannableString.setSpan(new ClickableSpan() {
                            @Override
                            public void onClick(@NonNull View textView) {
                                String finalUrl = url;
                                if( !url.startsWith("http://") && ! url.startsWith("https://"))
                                    finalUrl = "http://" + url;
                                Helper.openBrowser(context, finalUrl);
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
                        },
                        matchStart, matchEnd,
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

        }
        SpannableString spannableStringT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            spannableStringT = new SpannableString(Html.fromHtml(spannableString.toString().replaceAll("^<p>","").replaceAll("<p>","<br/><br/>").replaceAll("</p>","").replaceAll("<br />","<br/>").replaceAll("[\\s]{2}","&nbsp;&nbsp;"), Html.FROM_HTML_MODE_LEGACY));
        else
            //noinspection deprecation
            spannableStringT = new SpannableString(Html.fromHtml(spannableString.toString().replaceAll("^<p>","").replaceAll("<p>","<br/><br/>").replaceAll("</p>","").replaceAll("<br />","<br/>").replaceAll("[\\s]{2}","&nbsp;&nbsp;")));

        URLSpan[] spans = spannableStringT.getSpans(0, spannableStringT.length(), URLSpan.class);
        for (URLSpan span : spans) {
            spannableStringT.removeSpan(span);
        }

        matcher = Helper.twitterPattern.matcher(spannableStringT);
        while (matcher.find()){
            int matchStart = matcher.start(2);
            int matchEnd = matcher.end();
            final String twittername = matcher.group(2);
            if( matchEnd <= spannableStringT.toString().length() && matchEnd >= matchStart)
                spannableStringT.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View textView) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/"+twittername.substring(1).replace("@twitter.com","")));
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
                            ds.setColor(ContextCompat.getColor(context, R.color.light_link_toot));
                    }
                }, matchStart, matchEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }

        if( accountsMentionUnknown.size() > 0 ) {
            for(Account account: accountsMentionUnknown){
                String targetedAccount = "@" + account.getAcct();
                if (spannableStringT.toString().toLowerCase().contains(targetedAccount.toLowerCase())) {
                    //Accounts can be mentioned several times so we have to loop
                    for(int startPosition = -1 ; (startPosition = spannableStringT.toString().toLowerCase().indexOf(targetedAccount.toLowerCase(), startPosition + 1)) != -1 ; startPosition++){
                        int endPosition = startPosition + targetedAccount.length();
                        if( endPosition <= spannableStringT.toString().length() && endPosition >= startPosition)
                            spannableStringT.setSpan(new ClickableSpan() {
                                 @Override
                                 public void onClick(@NonNull View textView) {
                                     CrossActions.doCrossProfile(context,account);
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
                             },
                            startPosition, endPosition,
                            Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    }
                }
            }
        }
        if( targetedURL.size() > 0 ){
            Iterator it = targetedURL.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                String key = (String) pair.getKey();
                String url = (String) pair.getValue();
                if (spannableStringT.toString().toLowerCase().contains(key.toLowerCase())) {
                    //Accounts can be mentioned several times so we have to loop
                    for(int startPosition = -1 ; (startPosition = spannableStringT.toString().toLowerCase().indexOf(key.toLowerCase(), startPosition + 1)) != -1 ; startPosition++){

                        int endPosition = startPosition + key.length();

                        if( endPosition <= spannableStringT.toString().length() && endPosition >= startPosition) {
                            spannableStringT.setSpan(new ClickableSpan() {
                                 @Override
                                 public void onClick(@NonNull View textView) {
                                     String finalUrl = url;
                                     Pattern link = Pattern.compile("https?:\\/\\/([\\da-z\\.-]+\\.[a-z\\.]{2,10})\\/(@[\\w._-]*[0-9]*)(\\/[0-9]{1,})?$");
                                     Matcher matcherLink = link.matcher(url);
                                     if( matcherLink.find()){
                                        if( matcherLink.group(3) != null && matcherLink.group(3).length() > 0 ){ //It's a toot
                                            CrossActions.doCrossConversation(context, finalUrl);
                                        }else{//It's an account
                                            Account account = status.getAccount();
                                            account.setAcct(matcherLink.group(2));
                                            account.setInstance(matcherLink.group(1));
                                            CrossActions.doCrossProfile(context, account);
                                        }

                                     }else  {
                                         link = Pattern.compile("(https?:\\/\\/[\\da-z\\.-]+\\.[a-z\\.]{2,10})\\/videos\\/watch\\/(\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12})$");
                                         matcherLink = link.matcher(url);
                                         if( matcherLink.find()){ //Peertubee video
                                             Intent intent = new Intent(context, PeertubeActivity.class);
                                             Bundle b = new Bundle();
                                             String url = matcherLink.group(1) + "/videos/watch/" + matcherLink.group(2);
                                             b.putString("peertubeLinkToFetch", url);
                                             b.putString("peertube_instance", matcherLink.group(1).replace("https://","").replace("http://",""));
                                             b.putString("video_id", matcherLink.group(2));
                                             intent.putExtras(b);
                                             context.startActivity(intent);
                                         }else {
                                             if( !url.startsWith("http://") && ! url.startsWith("https://"))
                                                 finalUrl = "http://" + url;
                                             Helper.openBrowser(context, finalUrl);
                                         }

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
                             },
                                    startPosition, endPosition,
                                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        }
                    }
                }
                it.remove();
            }
        }
        //Deals with mention to make them clickable
        if( mentions != null && mentions.size() > 0 ) {
            //Looping through accounts which are mentioned
            for (final Mention mention : mentions) {
                String targetedAccount = "@" + mention.getUsername();
                if(spannableStringT.toString().toLowerCase().contains(("@" + mention.getAcct()).toLowerCase())) {
                    targetedAccount = "@" + mention.getAcct();
                    if (spannableStringT.toString().toLowerCase().contains(targetedAccount.toLowerCase())) {
                        //Accounts can be mentioned several times so we have to loop
                        for (int startPosition = -1; (startPosition = spannableStringT.toString().toLowerCase().indexOf(targetedAccount.toLowerCase(), startPosition + 1)) != -1; startPosition++) {

                            int endPosition = startPosition + targetedAccount.length();
                            if (endPosition <= spannableStringT.toString().length() && endPosition >= startPosition)
                                spannableStringT.setSpan(new ClickableSpan() {
                                     @Override
                                     public void onClick(@NonNull View textView) {
                                         Intent intent = new Intent(context, ShowAccountActivity.class);
                                         Bundle b = new Bundle();
                                         b.putString("accountId", mention.getId());
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
                                             ds.setColor(ContextCompat.getColor(context, R.color.light_link_toot));
                                     }
                                 },
                                startPosition, endPosition,
                                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        }
                    }
                }else if (spannableStringT.toString().toLowerCase().contains(targetedAccount.toLowerCase())) {
                    //Accounts can be mentioned several times so we have to loop
                    for(int startPosition = -1 ; (startPosition = spannableStringT.toString().toLowerCase().indexOf(targetedAccount.toLowerCase(), startPosition + 1)) != -1 ; startPosition++){

                        int endPosition = startPosition + targetedAccount.length();
                        if( endPosition <= spannableStringT.toString().length() && endPosition >= startPosition)
                            spannableStringT.setSpan(new ClickableSpan() {
                                 @Override
                                 public void onClick(@NonNull View textView) {
                                     Intent intent = new Intent(context, ShowAccountActivity.class);
                                     Bundle b = new Bundle();
                                     b.putString("accountId", mention.getId());
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
                                         ds.setColor(ContextCompat.getColor(context, R.color.light_link_toot));
                                 }
                             },
                            startPosition, endPosition,
                            Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    }
                }
            }
        }
        matcher = Helper.hashtagPattern.matcher(spannableStringT);
        while (matcher.find()){
            int matchStart = matcher.start(1);
            int matchEnd = matcher.end();
            final String tag = spannableStringT.toString().substring(matchStart, matchEnd);
            if( matchEnd <= spannableStringT.toString().length() && matchEnd >= matchStart)
                spannableStringT.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View textView) {
                        if(MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA) {
                            Intent intent = new Intent(context, HashTagActivity.class);
                            Bundle b = new Bundle();
                            b.putString("tag", tag.substring(1));
                            intent.putExtras(b);
                            context.startActivity(intent);
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

        }
        return spannableStringT;
    }

    public SpannableString getContentSpan() {
        return contentSpan;
    }

    public void setContentSpan(SpannableString contentSpan) {
        this.contentSpan = contentSpan;
    }

    public SpannableString getContentSpanCW() {
        return contentSpanCW;
    }

    public void setContentSpanCW(SpannableString contentSpanCW) {
        this.contentSpanCW = contentSpanCW;
    }

    public SpannableString getContentSpanTranslated() {
        return contentSpanTranslated;
    }

    public void setContentSpanTranslated(SpannableString contentSpanTranslated) {
        this.contentSpanTranslated = contentSpanTranslated;
    }

    public void setClickable(boolean clickable) {
        isClickable = clickable;
    }
    public boolean isClickable() {
        return isClickable;
    }

    public boolean isEmojiTranslateFound() {
        return isEmojiTranslateFound;
    }

    public void setEmojiTranslateFound(boolean emojiTranslateFound) {
        isEmojiTranslateFound = emojiTranslateFound;
    }

    public boolean isFetchMore() {
        return fetchMore;
    }

    public void setFetchMore(boolean fetchMore) {
        this.fetchMore = fetchMore;
    }



    @Override
    public boolean equals(Object otherStatus) {
        return otherStatus != null && (otherStatus == this || otherStatus instanceof Status && this.getId().equals(((Status) otherStatus).getId()));
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public boolean isBookmarked() {
        return bookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        this.bookmarked = bookmarked;
    }

    public int getReplies_count() {
        return replies_count;
    }

    public void setReplies_count(int replies_count) {
        this.replies_count = replies_count;
    }

    public RetrieveFeedsAsyncTask.Type getType() {
        return type;
    }

    public void setType(RetrieveFeedsAsyncTask.Type type) {
        this.type = type;
    }

    public List<String> getConversationProfilePicture() {
        return conversationProfilePicture;
    }

    public void setConversationProfilePicture(List<String> conversationProfilePicture) {
        this.conversationProfilePicture = conversationProfilePicture;
    }

    public String getWebviewURL() {
        return webviewURL;
    }

    public void setWebviewURL(String webviewURL) {
        this.webviewURL = webviewURL;
    }

    public int getItemViewType() {
        return itemViewType;
    }

    public void setItemViewType(int itemViewType) {
        this.itemViewType = itemViewType;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public int getNumberLines() {
        return numberLines;
    }

    public void setNumberLines(int numberLines) {
        this.numberLines = numberLines;
    }

    public boolean isBoostAnimated() {
        return isBoostAnimated;
    }

    public void setBoostAnimated(boolean boostAnimated) {
        isBoostAnimated = boostAnimated;
    }

    public boolean isFavAnimated() {
        return isFavAnimated;
    }

    public void setFavAnimated(boolean favAnimated) {
        isFavAnimated = favAnimated;
    }

    public Attachment getArt_attachment() {
        return art_attachment;
    }

    public void setArt_attachment(Attachment art_attachment) {
        this.art_attachment = art_attachment;
    }

    @Override
    public int describeContents() {
        return 0;
    }


    public String getScheduled_at() {
        return scheduled_at;
    }

    public void setScheduled_at(String scheduled_at) {
        this.scheduled_at = scheduled_at;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public boolean isShowSpoiler() {
        return showSpoiler;
    }

    public void setShowSpoiler(boolean showSpoiler) {
        this.showSpoiler = showSpoiler;
    }


    public boolean isNotice() {
        return isNotice;
    }

    public void setNotice(boolean notice) {
        isNotice = notice;
    }

    public Poll getPoll() {
        return poll;
    }

    public void setPoll(Poll poll) {
        this.poll = poll;
    }
}
