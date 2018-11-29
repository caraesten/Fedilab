/* Copyright 2018 Thomas Schneider
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

import android.content.Context;
import android.content.SharedPreferences;

import fr.gouv.etalab.mastodon.helper.Helper;

/**
 * Created by Thomas on 29/11/2018.
 * Manage preferences
 */
public class Preferences {

    private String userId;
    private boolean displayBookmarkButton, fullAttachement, isCompactMode, trans_forced, expand_cw, expand_media, display_card, display_video_preview, share_details;
    private int iconSizePercent, textSizePercent, theme, truncate_toots_size, timeout;


    public Preferences(Context context){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        displayBookmarkButton = sharedpreferences.getBoolean(Helper.SET_SHOW_BOOKMARK, false);
        fullAttachement = sharedpreferences.getBoolean(Helper.SET_FULL_PREVIEW, false);
        isCompactMode = sharedpreferences.getBoolean(Helper.SET_COMPACT_MODE, false);
        iconSizePercent = sharedpreferences.getInt(Helper.SET_ICON_SIZE, 130);
        textSizePercent = sharedpreferences.getInt(Helper.SET_TEXT_SIZE, 110);
        trans_forced = sharedpreferences.getBoolean(Helper.SET_TRANS_FORCED, false);
        theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        expand_cw = sharedpreferences.getBoolean(Helper.SET_EXPAND_CW, false);
        expand_media = sharedpreferences.getBoolean(Helper.SET_EXPAND_MEDIA, false);
        display_card = sharedpreferences.getBoolean(Helper.SET_DISPLAY_CARD, false);
        display_video_preview = sharedpreferences.getBoolean(Helper.SET_DISPLAY_VIDEO_PREVIEWS, true);
        truncate_toots_size = sharedpreferences.getInt(Helper.SET_TRUNCATE_TOOTS_SIZE, 0);
        timeout = sharedpreferences.getInt(Helper.SET_NSFW_TIMEOUT, 5);
        share_details = sharedpreferences.getBoolean(Helper.SET_SHARE_DETAILS, true);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isDisplayBookmarkButton() {
        return displayBookmarkButton;
    }

    public void setDisplayBookmarkButton(boolean displayBookmarkButton) {
        this.displayBookmarkButton = displayBookmarkButton;
    }

    public boolean isFullAttachement() {
        return fullAttachement;
    }

    public void setFullAttachement(boolean fullAttachement) {
        this.fullAttachement = fullAttachement;
    }

    public boolean isCompactMode() {
        return isCompactMode;
    }

    public void setCompactMode(boolean compactMode) {
        isCompactMode = compactMode;
    }

    public boolean isTrans_forced() {
        return trans_forced;
    }

    public void setTrans_forced(boolean trans_forced) {
        this.trans_forced = trans_forced;
    }

    public boolean isExpand_cw() {
        return expand_cw;
    }

    public void setExpand_cw(boolean expand_cw) {
        this.expand_cw = expand_cw;
    }

    public boolean isExpand_media() {
        return expand_media;
    }

    public void setExpand_media(boolean expand_media) {
        this.expand_media = expand_media;
    }

    public boolean isDisplay_card() {
        return display_card;
    }

    public void setDisplay_card(boolean display_card) {
        this.display_card = display_card;
    }

    public boolean isDisplay_video_preview() {
        return display_video_preview;
    }

    public void setDisplay_video_preview(boolean display_video_preview) {
        this.display_video_preview = display_video_preview;
    }

    public boolean isShare_details() {
        return share_details;
    }

    public void setShare_details(boolean share_details) {
        this.share_details = share_details;
    }

    public int getIconSizePercent() {
        return iconSizePercent;
    }

    public void setIconSizePercent(int iconSizePercent) {
        this.iconSizePercent = iconSizePercent;
    }

    public int getTextSizePercent() {
        return textSizePercent;
    }

    public void setTextSizePercent(int textSizePercent) {
        this.textSizePercent = textSizePercent;
    }

    public int getTheme() {
        return theme;
    }

    public void setTheme(int theme) {
        this.theme = theme;
    }

    public int getTruncate_toots_size() {
        return truncate_toots_size;
    }

    public void setTruncate_toots_size(int truncate_toots_size) {
        this.truncate_toots_size = truncate_toots_size;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
