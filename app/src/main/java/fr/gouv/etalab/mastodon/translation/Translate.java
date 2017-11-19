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
package fr.gouv.etalab.mastodon.translation;

import android.content.*;
import android.os.Build;
import android.text.Html;
import android.text.SpannableString;
import android.util.Patterns;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnTranslatedInterface;

import static fr.gouv.etalab.mastodon.activities.BaseMainActivity.currentLocale;

/**
 * Created by Thomas on 31/10/2017.
 * Manages translations
 */

public class Translate {

    private HashMap<String, String> urlConversion;
    private HashMap<String, String> tagConversion;
    private HashMap<String, String> mentionConversionOtherInstance;
    private HashMap<String, String> mentionConversion;
    private HashMap<String, String> blacklistConversion;


    private android.content.Context context;
    private OnTranslatedInterface listener;
    private Status status;
    private String targetedLanguage;
    private Helper.targetField targetField;

    public Translate(android.content.Context context, Status status, OnTranslatedInterface onTranslatedInterface){
        this.context = context;
        this.listener = onTranslatedInterface;
        this.status = status;
        targetedLanguage = currentLocale;
        this.targetField = Helper.targetField.SIMPLE;
    }

    public Translate(android.content.Context context, Helper.targetField targetField, String targetedLanguage, OnTranslatedInterface onTranslatedInterface){
        this.context = context;
        this.listener = onTranslatedInterface;
        this.targetedLanguage = targetedLanguage;
        this.targetField = targetField;
    }


    /***
     * Removes sensitive elements from translation,  ie: links, tags, mentions, and blacklisted words starting with %
     * @param content String
     */
    public void privacy (String content){
        this.tagConversion = new HashMap<>();
        this.mentionConversion = new HashMap<>();
        this.urlConversion = new HashMap<>();
        this.blacklistConversion = new HashMap<>();
        this.mentionConversionOtherInstance = new HashMap<>();
        try {
            SpannableString spannableString;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                spannableString = new SpannableString(Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY));
            else
                //noinspection deprecation
                spannableString = new SpannableString(Html.fromHtml(content));
            String text = spannableString.toString();
            Matcher matcher;

            int i = 0;
            //Same for mentions for other instances with __o0__, __o1__, etc.
            matcher = Helper.mentionOtherInstancePattern.matcher(text);
            while (matcher.find()){
                String key = "__o" + String.valueOf(i) + "__";
                String value = matcher.group(0);
                this.mentionConversionOtherInstance.put(key, value);
                if( value != null) {
                    this.mentionConversionOtherInstance.put(key, value);
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
                this.mentionConversion.put(key, value);
                if( value != null) {
                    this.mentionConversion.put(key, value);
                    text = text.replace(value, key);
                }
                i++;
            }
            //Extracts urls
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT)
                matcher = Patterns.WEB_URL.matcher(spannableString.toString());
            else
                matcher = Helper.urlPattern.matcher(spannableString.toString());
            i = 0;
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
                    this.urlConversion.put(key, value);
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
                this.tagConversion.put(key, value);
                if( value != null) {
                    this.tagConversion.put(key, value);
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
                this.blacklistConversion.put(key, value);
                if( value != null) {
                    this.blacklistConversion.put(key, value);
                    text = text.replace(value, key);
                }
                i++;
            }
            if( status == null) {
                new YandexQuery(this.listener).getYandexTranslation(this,targetField, content, this.targetedLanguage);
            }else {
                new YandexQuery(this.listener).getYandexTextview(this, this.status, content, this.targetedLanguage);
            }
        } catch (JSONException e) {
            Toast.makeText(context, R.string.toast_error_translate, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Replaces sensitive elements once translated by their original values
     * @param translatedResult String
     * @return String "decrypted content"
     */
    public String replace(String translatedResult){
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        int translator = sharedpreferences.getInt(Helper.SET_TRANSLATOR, Helper.TRANS_YANDEX);
        String aJsonString = null;
        try {
            aJsonString = yandexTranslateToText(translatedResult);
            if( aJsonString == null)
                return null;

            if( this.urlConversion != null) {
                Iterator itU = this.urlConversion.entrySet().iterator();
                while (itU.hasNext()) {
                    Map.Entry pair = (Map.Entry) itU.next();
                    aJsonString = aJsonString.replace(pair.getKey().toString(), pair.getValue().toString());
                    itU.remove();
                }
            }
            if( this.tagConversion != null) {
                Iterator itT = this.tagConversion.entrySet().iterator();
                while (itT.hasNext()) {
                    Map.Entry pair = (Map.Entry) itT.next();
                    aJsonString = aJsonString.replace(pair.getKey().toString(), pair.getValue().toString());
                    itT.remove();
                }
            }
            if( this.mentionConversionOtherInstance != null) {
                Iterator itM = this.mentionConversionOtherInstance.entrySet().iterator();
                while (itM.hasNext()) {
                    Map.Entry pair = (Map.Entry) itM.next();
                    aJsonString = aJsonString.replace(pair.getKey().toString(), pair.getValue().toString());
                    itM.remove();
                }
            }
            if( this.mentionConversion != null) {
                Iterator itM = this.mentionConversion.entrySet().iterator();
                while (itM.hasNext()) {
                    Map.Entry pair = (Map.Entry) itM.next();
                    aJsonString = aJsonString.replace(pair.getKey().toString(), pair.getValue().toString());
                    itM.remove();
                }
            }
            if( this.blacklistConversion != null) {
                Iterator itB = this.blacklistConversion.entrySet().iterator();
                while (itB.hasNext()) {
                    Map.Entry pair = (Map.Entry) itB.next();
                    aJsonString = aJsonString.replace(pair.getKey().toString(), pair.getValue().toString().substring(1));
                    itB.remove();
                }
            }
        } catch (JSONException | UnsupportedEncodingException | IllegalArgumentException e) {
            e.printStackTrace();
            Toast.makeText(context, R.string.toast_error_translate, Toast.LENGTH_LONG).show();
        }
        return aJsonString;
    }

    private String yandexTranslateToText(String text) throws JSONException, UnsupportedEncodingException{
        String aJsonString = null;
        if( text !=null) {
            JSONObject translationJson = new JSONObject(text);
            JSONArray aJsonArray = translationJson.getJSONArray("text");
            aJsonString = aJsonArray.get(0).toString();

        /* The one instance where I've seen this happen,
            the special tag was originally a hashtag ("__t1__"),
            that Yandex decided to change to a "__q1 - __".
         */
            aJsonString = aJsonString.replaceAll("__q(\\d+) - __", "__t$1__");

            // Noticed this in the very same toot
            aJsonString = aJsonString.replace("&amp;", "&");

            aJsonString = URLDecoder.decode(aJsonString, "UTF-8");
        }
        return aJsonString;
    }
}
