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

import android.content.*;
import android.os.Build;
import android.text.Html;
import android.text.SpannableString;
import android.util.Patterns;
import android.widget.Toast;

import org.json.JSONException;

import java.util.HashMap;
import java.util.regex.Matcher;

import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnTranslatedInterface;
import fr.gouv.etalab.mastodon.translation.GoogleTranslateQuery;
import fr.gouv.etalab.mastodon.translation.YandexQuery;
import mastodon.etalab.gouv.fr.mastodon.R;

import static fr.gouv.etalab.mastodon.activities.BaseMainActivity.currentLocale;

/**
 * Created by Thomas on 31/10/2017.
 * Manages translations
 */

public class Translate {

    private HashMap<String, String> urlConversion;
    private HashMap<String, String> tagConversion;
    private HashMap<String, String> mentionConversion;
    private HashMap<String, String> blacklistConversion;
    private Translate translate;
    private android.content.Context context;
    private OnTranslatedInterface listener;

    public Translate(android.content.Context context, OnTranslatedInterface onTranslatedInterface){
        this.translate = this;
        this.context = context;
        this.listener = onTranslatedInterface;
    }

    public Translate privacy (String content, String cw){
        tagConversion = new HashMap<>();
        mentionConversion = new HashMap<>();
        urlConversion = new HashMap<>();
        blacklistConversion = new HashMap<>();
        try {
            SpannableString spannableString;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                spannableString = new SpannableString(Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY));
            else
                //noinspection deprecation
                spannableString = new SpannableString(Html.fromHtml(content));
            String text = spannableString.toString();
            tagConversion = new HashMap<>();
            mentionConversion = new HashMap<>();
            urlConversion = new HashMap<>();
            blacklistConversion = new HashMap<>();
            Matcher matcher;
            //Extracts urls
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT)
                matcher = Patterns.WEB_URL.matcher(spannableString.toString());
            else
                matcher = Helper.urlPattern.matcher(spannableString.toString());
            int i = 0;
            //replaces them by a kind of variable which shouldn't be translated ie: __u0__, __u1__, etc.
            while (matcher.find()){
                String key = "__u" + String.valueOf(i) + "__";
                String value = matcher.group(0);
                int end = matcher.end();
                if (spannableString.length() > end && spannableString.charAt(end) == '/') {
                    text = spannableString.toString().substring(0, end).
                            concat(spannableString.toString().substring(end+1, spannableString.length()));
                }
                if( value != null) {
                    urlConversion.put(key, value);
                    text = text.replace(value, key);
                }
                i++;
            }
            i = 0;
            //Same for tags with __t0__, __t1__, etc.
            matcher = Helper.hashtagPattern.matcher(text);
            while (matcher.find()){
                String key = "__t" + String.valueOf(i) + "__";
                String value = matcher.group(0);
                tagConversion.put(key, value);
                if( value != null) {
                    tagConversion.put(key, value);
                    text = text.replace(value, key);
                }
                i++;
            }
            i = 0;
            //Same for mentions with __m0__, __m1__, etc.
            matcher = Helper.mentionPattern.matcher(text);
            while (matcher.find()){
                String key = "__m" + String.valueOf(i) + "__";
                String value = matcher.group(0);
                mentionConversion.put(key, value);
                if( value != null) {
                    mentionConversion.put(key, value);
                    text = text.replace(value, key);
                }
                i++;
            }
            i = 0;
            //Same for blacklisted words (ie: starting with %) with __b0__, __b1__, etc.
            matcher = Helper.blacklistPattern.matcher(text);
            while (matcher.find()){
                String key = "__b" + String.valueOf(i) + "__";
                String value = matcher.group(0);
                blacklistConversion.put(key, value);
                if( value != null) {
                    blacklistConversion.put(key, value);
                    text = text.replace(value, key);
                }
                i++;
            }
            final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
            int translator = sharedpreferences.getInt(Helper.SET_TRANSLATOR, Helper.TRANS_YANDEX);
            if (translator == Helper.TRANS_YANDEX) {
                if( content != null)
                    new YandexQuery(this.listener).getYandexTranslation(Helper.targetField.STATUS, content, currentLocale);
                if( cw != null)
                    new YandexQuery(this.listener).getYandexTranslation(Helper.targetField.CW, content, currentLocale);
            }else{
                while( text.charAt(text.length() -1) == '\n' && text.length() > 0)
                    text = text.substring(0, text.length() -1);
                text += ".";
                if( content != null)
                    new GoogleTranslateQuery(this.listener).getGoogleTranslation(Helper.targetField.STATUS, text, currentLocale);
                if( cw != null)
                    new GoogleTranslateQuery(this.listener).getGoogleTranslation(Helper.targetField.CW, text, currentLocale);
            }
        } catch (JSONException e) {
            Toast.makeText(context, R.string.toast_error_translate, Toast.LENGTH_LONG).show();
        }

        return translate;
    }


    public HashMap<String, String> getUrlConversion() {
        return urlConversion;
    }


    public HashMap<String, String> getTagConversion() {
        return tagConversion;
    }


    public HashMap<String, String> getMentionConversion() {
        return mentionConversion;
    }


    public HashMap<String, String> getBlacklistConversion() {
        return blacklistConversion;
    }
}
