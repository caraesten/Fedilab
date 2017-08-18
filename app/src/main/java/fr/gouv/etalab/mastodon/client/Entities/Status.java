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


import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Thomas on 23/04/2017.
 * Manage Status (ie: toots)
 */

public class Status implements Parcelable {

    private String id;
    private String uri;
    private String url;
    private Account account;
    private String in_reply_to_id;
    private String in_reply_to_account_id;
    private Status reblog;
    private String content;
    private String content_translated;
    private Date created_at;
    private int reblogs_count;
    private int favourites_count;
    private boolean reblogged;
    private boolean favourited;
    private boolean sensitive;
    private String spoiler_text;
    private String visibility;
    private boolean attachmentShown = false;
    private boolean spoilerShown = false;
    private ArrayList<Attachment> media_attachments;
    private List<Status> replies;
    private List<Mention> mentions;
    private List<Tag> tags;
    private Application application;
    private String language;
    private boolean isTranslated = false;
    private boolean isTranslationShown = false;

    protected Status(Parcel in) {
        id = in.readString();
        uri = in.readString();
        url = in.readString();
        in_reply_to_id = in.readString();
        in_reply_to_account_id = in.readString();
        reblog = in.readParcelable(Status.class.getClassLoader());
        account = in.readParcelable(Account.class.getClassLoader());
        mentions = in.readArrayList(Mention.class.getClassLoader());
        content = in.readString();
        content_translated = in.readString();
        reblogs_count = in.readInt();
        favourites_count = in.readInt();
        reblogged = in.readByte() != 0;
        favourited = in.readByte() != 0;
        sensitive = in.readByte() != 0;
        spoiler_text = in.readString();
        visibility = in.readString();
        language = in.readString();
        attachmentShown = in.readByte() != 0;
        spoilerShown = in.readByte() != 0;
        isTranslated = in.readByte() != 0;
        isTranslationShown = in.readByte() != 0;
    }

    public Status(){}

    public static final Creator<Status> CREATOR = new Creator<Status>() {
        @Override
        public Status createFromParcel(Parcel in) {
            return new Status(in);
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
        this.content = content;
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

    public boolean isSensitive() {
        return sensitive;
    }

    public void setSensitive(boolean sensitive) {
        this.sensitive = sensitive;
    }

    public String getSpoiler_text() {
        return spoiler_text;
    }

    public void setSpoiler_text(String spoiler_text) {
        this.spoiler_text = spoiler_text;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(uri);
        dest.writeString(url);
        dest.writeString(in_reply_to_id);
        dest.writeString(in_reply_to_account_id);
        dest.writeParcelable(reblog, flags);
        dest.writeParcelable(account, flags);
        dest.writeList(mentions);
        dest.writeString(content);
        dest.writeString(content_translated);
        dest.writeInt(reblogs_count);
        dest.writeInt(favourites_count);
        dest.writeByte((byte) (reblogged ? 1 : 0));
        dest.writeByte((byte) (favourited ? 1 : 0));
        dest.writeByte((byte) (sensitive ? 1 : 0));
        dest.writeString(spoiler_text);
        dest.writeString(visibility);
        dest.writeString(language);
        dest.writeByte((byte) (attachmentShown ? 1 : 0));
        dest.writeByte((byte) (spoilerShown ? 1 : 0));
        dest.writeByte((byte) (isTranslated ? 1 : 0));
        dest.writeByte((byte) (isTranslationShown ? 1 : 0));
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

    public String getContent_translated() {
        return content_translated;
    }

    public void setContent_translated(String content_translated) {
        this.content_translated = content_translated;
    }

    public List<Status> getReplies() {
        return replies;
    }

    public void setReplies(List<Status> replies) {
        this.replies = replies;
    }
}
