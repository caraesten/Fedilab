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
package fr.gouv.etalab.mastodon.client;


import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import static fr.gouv.etalab.mastodon.helper.Helper.USER_AGENT;

/**
 * Created by Thomas on 23/04/2017.
 * Client to call urls
 */

public class OauthClient {

    private static AsyncHttpClient client = new AsyncHttpClient();
    private String instance;


    public OauthClient(String instance){
        this.instance = instance;
    }

    public void get(String action, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        try {
            client.setConnectTimeout(10000); //10s timeout
            client.setUserAgent(USER_AGENT);
            MastalabSSLSocketFactory mastalabSSLSocketFactory = new MastalabSSLSocketFactory(MastalabSSLSocketFactory.getKeystore());
            mastalabSSLSocketFactory.setHostnameVerifier(MastalabSSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            client.setSSLSocketFactory(mastalabSSLSocketFactory);
            client.get(getAbsoluteUrl(action), params, responseHandler);
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException | UnrecoverableKeyException e) {
            e.printStackTrace();
        }
    }

    public void post(String action, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        try {
            client.setConnectTimeout(10000); //10s timeout
            client.setUserAgent(USER_AGENT);
            MastalabSSLSocketFactory mastalabSSLSocketFactory = new MastalabSSLSocketFactory(MastalabSSLSocketFactory.getKeystore());
            mastalabSSLSocketFactory.setHostnameVerifier(MastalabSSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            client.setSSLSocketFactory(mastalabSSLSocketFactory);
            client.post(getAbsoluteUrl(action), params, responseHandler);
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException | UnrecoverableKeyException e) {
            e.printStackTrace();
        }

    }

    private String getAbsoluteUrl(String action) {
        return "https://" + instance + action;
    }


}