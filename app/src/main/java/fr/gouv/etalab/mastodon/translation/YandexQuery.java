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
import com.loopj.android.http.AsyncHttpResponseHandler;
import org.json.JSONException;
import cz.msebera.android.httpclient.Header;
import fr.gouv.etalab.mastodon.interfaces.OnTranslatedInterface;

/**
 * Created by Thomas on 03/07/2017.
 * Yandex client API
 */
public class YandexQuery {

    private OnTranslatedInterface listener;
    public YandexQuery(OnTranslatedInterface listenner) {
        this.listener = listenner;
    }

    public void getYandexTextview(final int position, final String text, final String toLanguage) throws JSONException {

        YandexClient.get(text, toLanguage, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                String str_response = new String(response);
                listener.onTranslatedTextview(position, str_response,false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                listener.onTranslatedTextview(position,  null, true);
            }

        });
    }
}
