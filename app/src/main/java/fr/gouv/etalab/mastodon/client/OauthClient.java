/* Copyright 2017 Thomas Schneider
 *
 * This file is a part of Mastodon Etalab for mastodon.etalab.gouv.fr
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Mastodon Etalab is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Thomas Schneider; if not,
 * see <http://www.gnu.org/licenses>. */
package fr.gouv.etalab.mastodon.client;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import fr.gouv.etalab.mastodon.helper.Helper;

/**
 * Created by Thomas on 23/04/2017.
 * Client to call urls
 */

public class OauthClient {

    private static final String BASE_URL = "https://" + Helper.INSTANCE;

    private static AsyncHttpClient client = new AsyncHttpClient();

    public void get(String action, HashMap<String, String> paramaters, AsyncHttpResponseHandler responseHandler) {
        client.setTimeout(5000);
        RequestParams params = hashToRequestParams(paramaters);
        client.get(getAbsoluteUrl(action), params, responseHandler);
    }

    public void post(String action, HashMap<String, String> paramaters, AsyncHttpResponseHandler responseHandler) {
        RequestParams params = hashToRequestParams(paramaters);
        client.post(getAbsoluteUrl(action), params, responseHandler);
    }

    private String getAbsoluteUrl(String action) {
        return BASE_URL + action;
    }

    /**
     * Convert HashMap<String,String> to RequestParams
     * @param params HashMap
     * @return RequestParams
     */
    private RequestParams hashToRequestParams(HashMap<String,String> params){
        RequestParams requestParams = new RequestParams();
        Iterator it = params.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            requestParams.add(pair.getKey().toString(), pair.getValue().toString());
            it.remove();
        }
        return requestParams;
    }
}
