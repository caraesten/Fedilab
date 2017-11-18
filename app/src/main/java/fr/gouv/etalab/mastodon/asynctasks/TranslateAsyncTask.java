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
package fr.gouv.etalab.mastodon.asynctasks;

import android.os.AsyncTask;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import fr.gouv.etalab.mastodon.client.HttpsConnection;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnTranslatedInterface;
import fr.gouv.etalab.mastodon.translation.Translate;


/**
 * Created by Thomas on 29/04/2017.
 * Makes actions for post calls
 */

public class TranslateAsyncTask extends AsyncTask<Void, Void, Void> {

    private OnTranslatedInterface listener;
    private static final String BASE_URL = "https://translate.yandex.net/api/v1.5/tr.json/translate?";
    private static final String YANDEX_KEY = "trnsl.1.1.20170703T074828Z.a95168c920f61b17.699437a40bbfbddc4cd57f345a75c83f0f30c420";
    private String text, toLanguage;
    private Translate translate;
    private Helper.targetField target;

    public enum typeInter{
        TRANSLATED,
        TRANSLATEDTEXTVIEW
    }
    private typeInter type;
    private  String str_response;
    private fr.gouv.etalab.mastodon.client.Entities.Status status;

    public TranslateAsyncTask(final Translate translate, Helper.targetField target, fr.gouv.etalab.mastodon.client.Entities.Status status, String text, String toLanguage, typeInter type, OnTranslatedInterface onTranslatedInterface){
        this.listener = onTranslatedInterface;
        this.type = type;
        this.text = text;
        this.toLanguage = toLanguage;
        this.translate = translate;
        this.target = target;
        this.status = status;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            str_response = new HttpsConnection().get(getAbsoluteUrl(text, toLanguage), 30, null, null );
        } catch (Exception e) {
            if( type == typeInter.TRANSLATED)
                listener.onTranslatedTextview(translate, status, null, true);
            else if( type == typeInter.TRANSLATEDTEXTVIEW)
                listener.onTranslated(translate, target, "", false);

        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if( type == typeInter.TRANSLATED)
            listener.onTranslated(translate, target, str_response, false);
        else if( type == typeInter.TRANSLATEDTEXTVIEW)
            listener.onTranslatedTextview(translate, status, str_response, false);
    }


    private static String getAbsoluteUrl(String content, String toLanguage) {
        String key  = "key=" + YANDEX_KEY + "&";
        toLanguage = toLanguage.replace("null","");
        String lang = "lang=" + toLanguage + "&";
        String text;
        try {
            text = "text=" + URLEncoder.encode(content, "utf-8") + "&";
        } catch (UnsupportedEncodingException e) {
            text = "text=" + content + "&";
            e.printStackTrace();
        }
        String format = "format=html&";
        return BASE_URL + key + lang +format  + text ;
    }
}
