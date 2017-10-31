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
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnTranslatedInterface;

/**
 * Created by Thomas on 12/07/2017.
 * Google client API
 */
class GoogleTranslateQuery {

    private OnTranslatedInterface listener;


    GoogleTranslateQuery(OnTranslatedInterface listenner) {
        this.listener = listenner;
    }

    void getGoogleTextview(final Translate translate, final Status status, final String text, final String toLanguage) throws JSONException {

        GoogleTranslateClient.get(text, toLanguage, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                String str_response = new String(response);
                listener.onTranslatedTextview(translate, status, str_response,false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                listener.onTranslatedTextview(translate, status,  null, true);
            }

        });
    }


    void getGoogleTranslation(final Translate translate, final Helper.targetField target, final String text, final String toLanguage) throws JSONException {

        GoogleTranslateClient.get(text, toLanguage, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                String str_response = new String(response);
                listener.onTranslated(translate, target, str_response,false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                listener.onTranslated(translate, target,  null, true);
            }

        });
    }
}
