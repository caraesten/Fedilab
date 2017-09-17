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

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


class YandexClient {

    private static final String BASE_URL = "https://translate.yandex.net/api/v1.5/tr.json/translate?";
    private static final String YANDEX_KEY = "trnsl.1.1.20170703T074828Z.a95168c920f61b17.699437a40bbfbddc4cd57f345a75c83f0f30c420";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String text, String toLanguage, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(text, toLanguage), responseHandler);
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
