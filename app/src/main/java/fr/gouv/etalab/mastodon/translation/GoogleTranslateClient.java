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



class GoogleTranslateClient {

    private static final String BASE_URL = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&ie=UTF-8&oe=UTF-8&";


    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String text, String toLanguage, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(text, toLanguage), responseHandler);
    }

    private static String getAbsoluteUrl(String content, String toLanguage) {
        toLanguage = toLanguage.replace("null","");
        String lang = "tl=" + toLanguage + "&dt=t&";
        String text;
        try {
            text = "q=" + URLEncoder.encode(content, "utf-8") + "&";
        } catch (UnsupportedEncodingException e) {
            text = "q=" + content + "&";
        }
        String format = "format=html&";
        return BASE_URL + lang + format  + text ;
    }
}