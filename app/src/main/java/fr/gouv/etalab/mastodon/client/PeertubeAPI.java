package fr.gouv.etalab.mastodon.client;
/* Copyright 2019 Thomas Schneider
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

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Application;
import fr.gouv.etalab.mastodon.client.Entities.Attachment;
import fr.gouv.etalab.mastodon.client.Entities.Card;
import fr.gouv.etalab.mastodon.client.Entities.Conversation;
import fr.gouv.etalab.mastodon.client.Entities.Emojis;
import fr.gouv.etalab.mastodon.client.Entities.Error;
import fr.gouv.etalab.mastodon.client.Entities.Filters;
import fr.gouv.etalab.mastodon.client.Entities.HowToVideo;
import fr.gouv.etalab.mastodon.client.Entities.Instance;
import fr.gouv.etalab.mastodon.client.Entities.InstanceSocial;
import fr.gouv.etalab.mastodon.client.Entities.Mention;
import fr.gouv.etalab.mastodon.client.Entities.Notification;
import fr.gouv.etalab.mastodon.client.Entities.Peertube;
import fr.gouv.etalab.mastodon.client.Entities.Relationship;
import fr.gouv.etalab.mastodon.client.Entities.Results;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.client.Entities.Tag;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;


/**
 * Created by Thomas on 02/01/2019.
 * Manage Calls to the Peertube REST API
 */

public class PeertubeAPI {



    private Account account;
    private Context context;
    private Results results;
    private Attachment attachment;
    private List<Account> accounts;
    private List<Status> statuses;
    private List<Conversation> conversations;
    private int tootPerPage, accountPerPage, notificationPerPage;
    private int actionCode;
    private String instance;
    private String prefKeyOauthTokenT;
    private APIResponse apiResponse;
    private Error APIError;
    private List<String> domains;


    public PeertubeAPI(Context context) {
        this.context = context;
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        tootPerPage = sharedpreferences.getInt(Helper.SET_TOOTS_PER_PAGE, 40);
        accountPerPage = sharedpreferences.getInt(Helper.SET_ACCOUNTS_PER_PAGE, 40);
        notificationPerPage = sharedpreferences.getInt(Helper.SET_NOTIFICATIONS_PER_PAGE, 15);
        if( Helper.getLiveInstance(context) != null)
            this.instance = Helper.getLiveInstance(context);
        else {
            SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
            String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
            Account account = new AccountDAO(context, db).getAccountByID(userId);
            this.instance = account.getInstance().trim();
        }
        this.prefKeyOauthTokenT = sharedpreferences.getString(Helper.PREF_KEY_OAUTH_TOKEN, null);
        apiResponse = new APIResponse();
        APIError = null;
    }

    public PeertubeAPI(Context context, String instance, String token) {
        this.context = context;
        if( context == null) {
            apiResponse = new APIResponse();
            APIError = new Error();
            return;
        }
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        tootPerPage = sharedpreferences.getInt(Helper.SET_TOOTS_PER_PAGE, 40);
        accountPerPage = sharedpreferences.getInt(Helper.SET_ACCOUNTS_PER_PAGE, 40);
        notificationPerPage = sharedpreferences.getInt(Helper.SET_NOTIFICATIONS_PER_PAGE, 15);
        if( instance != null)
            this.instance = instance;
        else
            this.instance = Helper.getLiveInstance(context);

        if( token != null)
            this.prefKeyOauthTokenT = token;
        else
            this.prefKeyOauthTokenT = sharedpreferences.getString(Helper.PREF_KEY_OAUTH_TOKEN, null);
        apiResponse = new APIResponse();
        APIError = null;
    }


    /***
     * Get info on the current Instance *synchronously*
     * @return APIResponse
     */
    public APIResponse getInstance() {
        try {
            String response = new HttpsConnection(context).get(getAbsoluteUrl("/instance"), 30, null, prefKeyOauthTokenT);
            Instance instanceEntity = parseInstance(new JSONObject(response));
            apiResponse.setInstance(instanceEntity);
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return apiResponse;
    }



    /***
     * Update credential of the authenticated user *synchronously*
     * @return APIResponse
     */
    public APIResponse updateCredential(String display_name, String note, ByteArrayInputStream avatar, String avatarName, ByteArrayInputStream header, String headerName, API.accountPrivacy privacy, HashMap<String, String> customFields) {

        HashMap<String, String> requestParams = new HashMap<>();
        if( display_name != null)
            try {
                requestParams.put("display_name",URLEncoder.encode(display_name, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                requestParams.put("display_name",display_name);
            }
        if( note != null)
            try {
                requestParams.put("note",URLEncoder.encode(note, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                requestParams.put("note",note);
            }
        if( privacy != null)
            requestParams.put("locked",privacy== API.accountPrivacy.LOCKED?"true":"false");
        int i = 0;
        if( customFields != null && customFields.size() > 0){
            Iterator it = customFields.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                requestParams.put("fields_attributes["+i+"][name]",(String)pair.getKey());
                requestParams.put("fields_attributes["+i+"][value]",(String)pair.getValue());
                it.remove();
                i++;
            }
        }
        try {
            new HttpsConnection(context).patch(getAbsoluteUrl("/accounts/update_credentials"), 60, requestParams, avatar, avatarName, header, headerName, prefKeyOauthTokenT);
        } catch (HttpsConnection.HttpsConnectionException e) {
            e.printStackTrace();
            setError(e.getStatusCode(), e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return apiResponse;
    }

    /***
     * Verifiy credential of the authenticated user *synchronously*
     * @return Account
     */
    public Account verifyCredentials() {
        account = new Account();
        try {
            String response = new HttpsConnection(context).get(getAbsoluteUrl("/users/me"), 60, null, prefKeyOauthTokenT);
            JSONObject accountObject = new JSONObject(response).getJSONObject("account");
            account = parseAccountResponsePeertube(context, accountObject);
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return account;
    }

    /**
     * Returns an account
     * @param accountId String account fetched
     * @return Account entity
     */
    public Account getAccount(String accountId) {

        account = new Account();
        try {
            String response = new HttpsConnection(context).get(getAbsoluteUrl(String.format("/accounts/%s",accountId)), 60, null, prefKeyOauthTokenT);
            account = parseAccountResponse(context, new JSONObject(response));
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return account;
    }



    /**
     * Returns a relationship between the authenticated account and an account
     * @param uri String accounts fetched
     * @return Relationship entity
     */
    public boolean isFollowing(String uri) {
        HashMap<String, String> params = new HashMap<>();

        params.put("uris", uri);

        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.get(getAbsoluteUrl("/users/me/subscriptions/exist"), 60, params, prefKeyOauthTokenT);
            return new JSONObject(response).getBoolean(uri);
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * Retrieves videos for the account *synchronously*
     *
     * @param acct String Id of the account
     * @param max_id    String id max
     * @return APIResponse
     */
    public APIResponse getVideos(String acct, String max_id) {
        return getVideos(acct, max_id, null, tootPerPage);
    }



    /**
     * Retrieves status for the account *synchronously*
     *
     * @param acct       String Id of the account
     * @param max_id          String id max
     * @param since_id        String since the id
     * @param limit           int limit  - max value 40
     * @return APIResponse
     */
    @SuppressWarnings("SameParameterValue")
    private APIResponse getVideos(String acct, String max_id, String since_id, int limit) {

        HashMap<String, String> params = new HashMap<>();
        if (max_id != null)
            params.put("start", max_id);
        if (since_id != null)
            params.put("since_id", since_id);
        if (0 < limit || limit > 40)
            limit = 40;
        params.put("count", String.valueOf(limit));
        List<Peertube> peertubes = new ArrayList<>();
        try {

            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.get(getAbsoluteUrl(String.format("/accounts/%s/videos", acct)), 60, params, prefKeyOauthTokenT);

            JSONArray jsonArray = new JSONObject(response).getJSONArray("data");
            peertubes = parsePeertube(jsonArray);

        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        apiResponse.setPeertubes(peertubes);
        return apiResponse;
    }


    /**
     * Retrieves videos channel for the account *synchronously*
     *
     * @param acct String Id of the account
     * @param max_id    String id max
     * @return APIResponse
     */
    public APIResponse getVideosChannel(String acct, String max_id) {
        return getVideosChannel(acct, max_id, null, tootPerPage);
    }

    /**
     * Retrieves status for the account *synchronously*
     *
     * @param acct       String Id of the account
     * @param max_id          String id max
     * @param since_id        String since the id
     * @param limit           int limit  - max value 40
     * @return APIResponse
     */
    @SuppressWarnings("SameParameterValue")
    private APIResponse getVideosChannel(String acct, String max_id, String since_id, int limit) {

        HashMap<String, String> params = new HashMap<>();
        if (max_id != null)
            params.put("start", max_id);
        if (since_id != null)
            params.put("since_id", since_id);
        if (0 < limit || limit > 40)
            limit = 40;
        params.put("count", String.valueOf(limit));
        List<Peertube> peertubes = new ArrayList<>();
        try {

            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.get(getAbsoluteUrl(String.format("/video-channels/%s/videos", acct)), 60, params, prefKeyOauthTokenT);

            JSONArray jsonArray = new JSONObject(response).getJSONArray("data");
            peertubes = parsePeertube(jsonArray);

        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        apiResponse.setPeertubes(peertubes);
        return apiResponse;
    }


    /**
     * Retrieves accounts that reblogged the status *synchronously*
     *
     * @param statusId       String Id of the status
     * @param max_id          String id max
     * @return APIResponse
     */
    @SuppressWarnings("SameParameterValue")
    public APIResponse getRebloggedBy(String statusId, String max_id) {

        HashMap<String, String> params = new HashMap<>();
        if (max_id != null)
            params.put("max_id", max_id);
        params.put("limit", "80");
        accounts = new ArrayList<>();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.get(getAbsoluteUrl(String.format("/statuses/%s/reblogged_by", statusId)), 60, params, prefKeyOauthTokenT);
            accounts = parseAccountResponse(new JSONArray(response));
            apiResponse.setSince_id(httpsConnection.getSince_id());
            apiResponse.setMax_id(httpsConnection.getMax_id());
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        apiResponse.setAccounts(accounts);
        return apiResponse;
    }


    /**
     * Retrieves accounts that favourited the status *synchronously*
     *
     * @param statusId       String Id of the status
     * @param max_id          String id max
     * @return APIResponse
     */
    @SuppressWarnings("SameParameterValue")
    public APIResponse getFavouritedBy(String statusId, String max_id) {

        HashMap<String, String> params = new HashMap<>();
        if (max_id != null)
            params.put("max_id", max_id);
        params.put("limit", "80");
        accounts = new ArrayList<>();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.get(getAbsoluteUrl(String.format("/statuses/%s/favourited_by", statusId)), 60, params, prefKeyOauthTokenT);
            accounts = parseAccountResponse(new JSONArray(response));
            apiResponse.setSince_id(httpsConnection.getSince_id());
            apiResponse.setMax_id(httpsConnection.getMax_id());
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        apiResponse.setAccounts(accounts);
        return apiResponse;
    }


    /**
     * Retrieves one status *synchronously*
     *
     * @param statusId  String Id of the status
     * @return APIResponse
     */
    public APIResponse getStatusbyId(String statusId) {
        statuses = new ArrayList<>();

        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.get(getAbsoluteUrl(String.format("/statuses/%s", statusId)), 60, null, prefKeyOauthTokenT);
            Status status = parseStatuses(context, new JSONObject(response));
            statuses.add(status);
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        apiResponse.setStatuses(statuses);
        return apiResponse;
    }

    /**
     * Retrieves the context of status with replies *synchronously*
     *
     * @param statusId  Id of the status
     * @return List<Status>
     */
    public fr.gouv.etalab.mastodon.client.Entities.Context getStatusContext(String statusId) {
        fr.gouv.etalab.mastodon.client.Entities.Context statusContext = new fr.gouv.etalab.mastodon.client.Entities.Context();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.get(getAbsoluteUrl(String.format("/statuses/%s/context", statusId)), 60, null, prefKeyOauthTokenT);
            statusContext = parseContext(new JSONObject(response));
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return statusContext;
    }


    /**
     * Retrieves direct timeline for the account *synchronously*
     * @param max_id   String id max
     * @return APIResponse
     */
    public APIResponse getDirectTimeline( String max_id) {
        return getDirectTimeline(max_id, null, tootPerPage);
    }

    /**
     * Retrieves conversation timeline for the account *synchronously*
     * @param max_id   String id max
     * @return APIResponse
     */
    public APIResponse getConversationTimeline( String max_id) {
        return getConversationTimeline(max_id, null, tootPerPage);
    }

    /**
     * Retrieves direct timeline for the account since an Id value *synchronously*
     * @return APIResponse
     */
    public APIResponse getConversationTimelineSinceId(String since_id) {
        return getConversationTimeline(null, since_id, tootPerPage);
    }

    /**
     * Retrieves conversation timeline for the account *synchronously*
     * @param max_id   String id max
     * @param since_id String since the id
     * @param limit    int limit  - max value 40
     * @return APIResponse
     */
    private APIResponse getConversationTimeline(String max_id, String since_id, int limit) {

        HashMap<String, String> params = new HashMap<>();
        if (max_id != null)
            params.put("max_id", max_id);
        if (since_id != null)
            params.put("since_id", since_id);
        if (0 > limit || limit > 80)
            limit = 80;
        params.put("limit",String.valueOf(limit));
        conversations = new ArrayList<>();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.get(getAbsoluteUrl("/conversations"), 60, params, prefKeyOauthTokenT);
            apiResponse.setSince_id(httpsConnection.getSince_id());
            apiResponse.setMax_id(httpsConnection.getMax_id());
            conversations = parseConversations(new JSONArray(response));
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        apiResponse.setConversations(conversations);
        return apiResponse;
    }

    /**
     * Retrieves direct timeline for the account since an Id value *synchronously*
     * @return APIResponse
     */
    public APIResponse getDirectTimelineSinceId(String since_id) {
        return getDirectTimeline(null, since_id, tootPerPage);
    }

    /**
     * Retrieves direct timeline for the account *synchronously*
     * @param max_id   String id max
     * @param since_id String since the id
     * @param limit    int limit  - max value 40
     * @return APIResponse
     */
    private APIResponse getDirectTimeline(String max_id, String since_id, int limit) {

        HashMap<String, String> params = new HashMap<>();
        if (max_id != null)
            params.put("max_id", max_id);
        if (since_id != null)
            params.put("since_id", since_id);
        if (0 > limit || limit > 80)
            limit = 80;
        params.put("limit",String.valueOf(limit));
        statuses = new ArrayList<>();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.get(getAbsoluteUrl("/timelines/direct"), 60, params, prefKeyOauthTokenT);
            apiResponse.setSince_id(httpsConnection.getSince_id());
            apiResponse.setMax_id(httpsConnection.getMax_id());
            statuses = parseStatuses(context, new JSONArray(response));
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        apiResponse.setStatuses(statuses);
        return apiResponse;
    }


    /**
     * Retrieves subscription videos *synchronously*
     * @param max_id   String id max
     * @return APIResponse
     */
    public APIResponse getSubscriptionsTL( String max_id) {
        return getTL("/users/me/subscriptions/videos",null,null, max_id, null, null, tootPerPage);
    }

    /**
     * Retrieves overview videos *synchronously*
     * @param max_id   String id max
     * @return APIResponse
     */
    public APIResponse getOverviewTL( String max_id) {
        return getTL("/overviews/videos",null,null, max_id, null, null, tootPerPage);
    }

    /**
     * Retrieves trending videos *synchronously*
     * @param max_id   String id max
     * @return APIResponse
     */
    public APIResponse getTrendingTL( String max_id) {
        return getTL("/videos/","trending", null,max_id, null, null, tootPerPage);
    }

    /**
     * Retrieves trending videos *synchronously*
     * @param max_id   String id max
     * @return APIResponse
     */
    public APIResponse getRecentlyAddedTL( String max_id) {
        return getTL("/videos/","-publishedAt",null,max_id, null, null, tootPerPage);
    }

    /**
     * Retrieves trending videos *synchronously*
     * @param max_id   String id max
     * @return APIResponse
     */
    public APIResponse getLocalTL( String max_id) {
        return getTL("/videos/",null, "local",max_id, null, null, tootPerPage);
    }

    /**
     * Retrieves home timeline for the account since an Id value *synchronously*
     * @return APIResponse
     */
    public APIResponse getSubscriptionsTLSinceId(String since_id) {
        return getTL("/users/me/subscriptions/videos",null,null,null, since_id, null, tootPerPage);
    }

    /**
     * Retrieves home timeline for the account from a min Id value *synchronously*
     * @return APIResponse
     */
    public APIResponse getSubscriptionsTLMinId(String min_id) {
        return getTL("/users/me/subscriptions/videos",null, null,null, null, min_id, tootPerPage);
    }


    /**
     * Retrieves home timeline for the account *synchronously*
     * @param max_id   String id max
     * @param since_id String since the id
     * @param limit    int limit  - max value 40
     * @return APIResponse
     */
    private APIResponse getTL(String action, String sort, String filter, String max_id, String since_id, String min_id, int limit) {

        HashMap<String, String> params = new HashMap<>();
        if (max_id != null)
            params.put("start", max_id);
        if (since_id != null)
            params.put("since_id", since_id);
        if (min_id != null)
            params.put("min_id", min_id);
        if (0 > limit || limit > 80)
            limit = 80;
        params.put("count",String.valueOf(limit));
        if( sort != null)
            params.put("sort",sort);
        else
            params.put("sort","publishedAt");
        if( filter != null)
            params.put("filter",filter);

        List<Peertube> peertubes = new ArrayList<>();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.get(getAbsoluteUrl(action), 60, params, prefKeyOauthTokenT);
            if( !action.equals("/overviews/videos")) {
                JSONArray values = new JSONObject(response).getJSONArray("data");
                peertubes = parsePeertube(values);
            }else {
                JSONArray videoA = new JSONObject(response).getJSONArray("categories");
                JSONArray values = videoA.getJSONObject(0).getJSONArray("videos");
                List<Peertube> peertubes1 = parsePeertube(values);
                peertubes.addAll(peertubes1);
                values = videoA.getJSONObject(1).getJSONArray("videos");
                List<Peertube> peertubes2 = parsePeertube(values);
                peertubes.addAll(peertubes2);

                videoA = new JSONObject(response).getJSONArray("channels");
                values = videoA.getJSONObject(0).getJSONArray("videos");
                List<Peertube> peertubes3 = parsePeertube(values);
                peertubes.addAll(peertubes3);
                values = videoA.getJSONObject(1).getJSONArray("videos");
                List<Peertube> peertubes4 = parsePeertube(values);
                peertubes.addAll(peertubes4);

                videoA = new JSONObject(response).getJSONArray("tags");
                values = videoA.getJSONObject(0).getJSONArray("videos");
                List<Peertube> peertubes5 = parsePeertube(values);
                peertubes.addAll(peertubes5);
                values = videoA.getJSONObject(1).getJSONArray("videos");
                List<Peertube> peertubes6 = parsePeertube(values);
                peertubes.addAll(peertubes6);

            }
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        apiResponse.setPeertubes(peertubes);
        return apiResponse;
    }




    /**
     * Retrieves Peertube videos from an instance *synchronously*
     * @return APIResponse
     */
    public APIResponse getPeertubeChannel(String instance, String name) {

        List<Account> accounts = new ArrayList<>();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.get(String.format("https://"+instance+"/api/v1/accounts/%s/video-channels", name), 60, null, null);
            JSONArray jsonArray = new JSONObject(response).getJSONArray("data");
            accounts = parseAccountResponsePeertube(context, instance, jsonArray);
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        apiResponse.setAccounts(accounts);
        return apiResponse;
    }


    /**
     * Retrieves Peertube videos from an instance *synchronously*
     * @return APIResponse
     */
    public APIResponse getPeertubeChannelVideos(String instance, String name) {

        List<Peertube> peertubes = new ArrayList<>();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.get(String.format("https://"+instance+"/api/v1/video-channels/%s/videos", name), 60, null, null);
            JSONArray jsonArray = new JSONObject(response).getJSONArray("data");
            peertubes = parsePeertube(jsonArray);
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        apiResponse.setPeertubes(peertubes);
        return apiResponse;
    }

    /**
     * Retrieves Peertube videos from an instance *synchronously*
     * @return APIResponse
     */
    public APIResponse getPeertube(String instance, String max_id) {

        List<Peertube> peertubes = new ArrayList<>();
        HashMap<String, String> params = new HashMap<>();
        if( max_id == null)
            max_id = "0";
        params.put("start", max_id);
        params.put("count", "50");
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.get("https://"+instance+"/api/v1/videos", 60, params, null);
            JSONArray jsonArray = new JSONObject(response).getJSONArray("data");
            peertubes = parsePeertube(jsonArray);
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        apiResponse.setPeertubes(peertubes);
        return apiResponse;
    }

    /**
     * Retrieves Peertube videos from an instance *synchronously*
     * @return APIResponse
     */
    public APIResponse getSinglePeertube(String instance, String videoId) {


        Peertube peertube = null;
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.get(String.format("https://"+instance+"/api/v1/videos/%s", videoId), 60, null, null);
            JSONObject jsonObject = new JSONObject(response);
            peertube = parseSinglePeertube(context, instance, jsonObject);
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        List<Peertube> peertubes = new ArrayList<>();
        peertubes.add(peertube);
        apiResponse.setPeertubes(peertubes);
        return apiResponse;
    }

    /**
     * Retrieves peertube search *synchronously*
     *
     * @param query  String search
     * @return APIResponse
     */
    public APIResponse searchPeertube(String instance, String query) {
        HashMap<String, String> params = new HashMap<>();
        params.put("count", "50");
        try {
            params.put("search", URLEncoder.encode(query, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            params.put("search", query);
        }
        List<Peertube> peertubes = new ArrayList<>();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.get("https://"+instance+"/api/v1/search/videos", 60, params, null);
            JSONArray jsonArray = new JSONObject(response).getJSONArray("data");
            peertubes = parsePeertube(jsonArray);
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        apiResponse.setPeertubes(peertubes);
        return apiResponse;
    }
    /**
     * Retrieves Peertube videos from an instance *synchronously*
     * @return APIResponse
     */
    public APIResponse getSinglePeertubeComments(String instance, String videoId) {
        statuses = new ArrayList<>();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.get(String.format("https://"+instance+"/api/v1/videos/%s/comment-threads", videoId), 60, null, null);
            JSONObject jsonObject = new JSONObject(response);
            statuses = parseSinglePeertubeComments(context, instance, jsonObject);
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        apiResponse.setStatuses(statuses);
        return apiResponse;
    }



    /**
     * Retrieves following for the account specified by targetedId  *synchronously*
     * @param targetedId String targetedId
     * @param max_id String id max
     * @return APIResponse
     */
    public APIResponse getFollowing(String targetedId, String max_id){
        return getAccounts(String.format("/accounts/%s/following",targetedId),max_id, null, accountPerPage);
    }

    /**
     * Retrieves followers for the account specified by targetedId  *synchronously*
     * @param targetedId String targetedId
     * @param max_id String id max
     * @return APIResponse
     */
    public APIResponse getFollowers(String targetedId, String max_id){
        return getAccounts(String.format("/accounts/%s/followers",targetedId),max_id, null, accountPerPage);
    }

    /**
     * Retrieves blocked users by the authenticated account *synchronously*
     * @param max_id String id max
     * @param since_id String since the id
     * @param limit int limit  - max value 40
     * @return APIResponse
     */
    @SuppressWarnings("SameParameterValue")
    private APIResponse getAccounts(String action, String max_id, String since_id, int limit){

        HashMap<String, String> params = new HashMap<>();
        if( max_id != null )
            params.put("max_id", max_id);
        if( since_id != null )
            params.put("since_id", since_id);
        if( 0 > limit || limit > 40)
            limit = 40;
        params.put("limit",String.valueOf(limit));
        accounts = new ArrayList<>();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.get(getAbsoluteUrl(action), 60, params, prefKeyOauthTokenT);
            apiResponse.setSince_id(httpsConnection.getSince_id());
            apiResponse.setMax_id(httpsConnection.getMax_id());
            accounts = parseAccountResponse(new JSONArray(response));
            if( accounts != null && accounts.size() == 1 ){
                if(accounts.get(0).getAcct() == null){
                    Throwable error = new Throwable(context.getString(R.string.toast_error));
                    setError(500, error);
                }
            }
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        apiResponse.setAccounts(accounts);
        return apiResponse;
    }



    /**
     * Retrieves rating of user on a video  *synchronously*
     * @param id String id
     * @return APIResponse
     */
    @SuppressWarnings("SameParameterValue")
    public String getRating(String id){
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.get(getAbsoluteUrl(String.format("/users/me/videos/%s/rating",id)), 60, null, prefKeyOauthTokenT);
            return new JSONObject(response).get("rating").toString();
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Makes the post action for a status
     * @param statusAction Enum
     * @param targetedId String id of the targeted Id *can be this of a status or an account*
     * @return in status code - Should be equal to 200 when action is done
     */
    public int postAction(API.StatusAction statusAction, String targetedId){
        return postAction(statusAction, targetedId, null, null);
    }


    public int postRating(String targetedId, String actionMore){
        return postAction(API.StatusAction.RATEVIDEO, targetedId, actionMore, null);
    }

    public int postComment(String targetedId, String actionMore){
        return postAction(API.StatusAction.PEERTUBECOMMENT, targetedId, actionMore, null);
    }

    public int postReply(String targetedId, String actionMore, String targetedComment){
        return postAction(API.StatusAction.PEERTUBEREPLY, targetedId, actionMore, targetedComment);
    }

    public int deleteComment(String targetedId, String targetedComment){
        return postAction(API.StatusAction.PEERTUBEDELETECOMMENT, targetedId, null, targetedComment);
    }
    /**
     * Makes the post action
     * @param statusAction Enum
     * @param targetedId String id of the targeted Id *can be this of a status or an account*
     * @param actionMore String another action
     * @param targetedComment String another action
     * @return in status code - Should be equal to 200 when action is done
     */
    private int postAction(API.StatusAction statusAction, String targetedId, String actionMore, String targetedComment){

        String action;
        String actionCall = "POST";
        HashMap<String, String> params = null;
        switch (statusAction){
            case FOLLOW:
                action = "/users/me/subscriptions";
                params = new HashMap<>();
                params.put("uri", targetedId);
                break;
            case UNFOLLOW:
                action = String.format("/users/me/subscriptions/%s", targetedId);
                actionCall = "DELETE";
                break;
            case RATEVIDEO:
                action = String.format("/videos/%s/rate", targetedId);
                params = new HashMap<>();
                params.put("rating", actionMore);
                actionCall = "PUT";
                break;
            case PEERTUBECOMMENT:
                action = String.format("/videos/%s/comment-threads", targetedId);
                params = new HashMap<>();
                params.put("text", actionMore);
                break;
            case PEERTUBEDELETECOMMENT:
                action = String.format("/videos/%s/comments/%s", targetedId, targetedComment);
                actionCall = "DELETE";
                break;
            case PEERTUBEREPLY:
                action = String.format("/videos/%s/comment/%s", targetedId, targetedComment);
                params = new HashMap<>();
                params.put("text", actionMore);
                break;
            default:
                return -1;
        }
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            if( actionCall.equals("POST"))
                httpsConnection.post(getAbsoluteUrl(action), 60, params, prefKeyOauthTokenT);
            else if( actionCall.equals("DELETE"))
                httpsConnection.delete(getAbsoluteUrl(action), 60, params, prefKeyOauthTokenT);
            else if( actionCall.equals("PUT"))
                httpsConnection.put(getAbsoluteUrl(action), 60, params, prefKeyOauthTokenT);
            actionCode = httpsConnection.getActionCode();
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return actionCode;
    }



    /**
     * Retrieves notifications for the authenticated account *synchronously*
     * @param max_id String id max
     * @param since_id String since the id
     * @param limit int limit  - max value 40
     * @return APIResponse
     */
    private APIResponse getNotifications(String max_id, String since_id, int limit, boolean display){

        HashMap<String, String> params = new HashMap<>();
        if( max_id != null )
            params.put("max_id", max_id);
        if( since_id != null )
            params.put("since_id", since_id);
        if( 0 > limit || limit > 30)
            limit = 30;
        params.put("limit",String.valueOf(limit));

        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        boolean notif_follow, notif_add, notif_mention, notif_share;
        if( display) {
            notif_follow = sharedpreferences.getBoolean(Helper.SET_NOTIF_FOLLOW_FILTER, true);
            notif_add = sharedpreferences.getBoolean(Helper.SET_NOTIF_ADD_FILTER, true);
            notif_mention = sharedpreferences.getBoolean(Helper.SET_NOTIF_MENTION_FILTER, true);
            notif_share = sharedpreferences.getBoolean(Helper.SET_NOTIF_SHARE_FILTER, true);
        }else{
            notif_follow = sharedpreferences.getBoolean(Helper.SET_NOTIF_FOLLOW, true);
            notif_add = sharedpreferences.getBoolean(Helper.SET_NOTIF_ADD, true);
            notif_mention = sharedpreferences.getBoolean(Helper.SET_NOTIF_MENTION, true);
            notif_share = sharedpreferences.getBoolean(Helper.SET_NOTIF_SHARE, true);
        }
        StringBuilder parameters = new StringBuilder();

        if( !notif_follow )
            parameters.append("exclude_types[]=").append("follow").append("&");
        if( !notif_add )
            parameters.append("exclude_types[]=").append("favourite").append("&");
        if( !notif_share )
            parameters.append("exclude_types[]=").append("reblog").append("&");
        if( !notif_mention )
            parameters.append("exclude_types[]=").append("mention").append("&");
        if( parameters.length() > 0) {
            parameters = new StringBuilder(parameters.substring(0, parameters.length() - 1).substring(16));
            params.put("exclude_types[]", parameters.toString());
        }


        List<Notification> notifications = new ArrayList<>();

        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.get(getAbsoluteUrl("/notifications"), 60, params, prefKeyOauthTokenT);
            apiResponse.setSince_id(httpsConnection.getSince_id());
            apiResponse.setMax_id(httpsConnection.getMax_id());
            notifications = parseNotificationResponse(new JSONArray(response));
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        apiResponse.setNotifications(notifications);
        return apiResponse;
    }




    /**
     * Changes media description
     * @param mediaId String
     *  @param description String
     * @return Attachment
     */
    public Attachment updateDescription(String mediaId, String description){

        HashMap<String, String> params = new HashMap<>();
        try {
            params.put("description", URLEncoder.encode(description, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            params.put("description", description);
        }
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.put(getAbsoluteUrl(String.format("/media/%s", mediaId)), 240, params, prefKeyOauthTokenT);
            attachment = parseAttachmentResponse(new JSONObject(response));
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return attachment;
    }

    /**
     * Retrieves Accounts and feeds when searching *synchronously*
     *
     * @param query  String search
     * @return List<Account>
     */
    public Results search(String query) {

        HashMap<String, String> params = new HashMap<>();
        try {
            params.put("q", URLEncoder.encode(query, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            params.put("q", query);
        }
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.get(getAbsoluteUrl("/search"), 60, params, prefKeyOauthTokenT);
            results = parseResultsResponse(new JSONObject(response));
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return results;
    }


    /**
     * Retrieves Accounts when searching (ie: via @...) *synchronously*
     * Not limited to following
     * @param query  String search
     * @return APIResponse
     */
    @SuppressWarnings("SameParameterValue")
    public APIResponse searchAccounts(String query, int count) {
        return searchAccounts(query, count, false);
    }

    /**
     * Retrieves Accounts when searching (ie: via @...) *synchronously*
     * @param query  String search
     * @param count  int limit
     * @param following  boolean following only
     * @return APIResponse
     */
    public APIResponse searchAccounts(String query, int count, boolean following) {

        HashMap<String, String> params = new HashMap<>();
        params.put("q", query);
        if( count < 5)
            count = 5;
        if( count > 40 )
            count = 40;
        if( following)
            params.put("following", Boolean.toString(true));
        params.put("limit", String.valueOf(count));

        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.get(getAbsoluteUrl("/accounts/search"), 60, params, prefKeyOauthTokenT);
            accounts = parseAccountResponse(new JSONArray(response));
            apiResponse.setSince_id(httpsConnection.getSince_id());
            apiResponse.setMax_id(httpsConnection.getMax_id());
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        apiResponse.setAccounts(accounts);
        return apiResponse;
    }



    /**
     * Retrieves Accounts when searching (ie: via @...) *synchronously*
     *
     * @return APIResponse
     */
    public APIResponse getCustomEmoji() {
        List<Emojis> emojis = new ArrayList<>();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.get(getAbsoluteUrl("/custom_emojis"), 60, null, prefKeyOauthTokenT);
            emojis = parseEmojis(new JSONArray(response));
            apiResponse.setSince_id(httpsConnection.getSince_id());
            apiResponse.setMax_id(httpsConnection.getMax_id());

        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        apiResponse.setEmojis(emojis);
        return apiResponse;
    }


    /**
     * Get filters for the user
     * @return APIResponse
     */
    public APIResponse getFilters(){

        List<Filters> filters = null;
        try {
            String response = new HttpsConnection(context).get(getAbsoluteUrl("/filters"), 60, null, prefKeyOauthTokenT);
            filters = parseFilters(new JSONArray(response));
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        apiResponse.setFilters(filters);
        return apiResponse;
    }

    /**
     * Get a Filter by its id
     * @return APIResponse
     */
    @SuppressWarnings("unused")
    public APIResponse getFilters(String filterId){

        List<Filters> filters = new ArrayList<>();
        Filters filter;
        try {
            String response = new HttpsConnection(context).get(getAbsoluteUrl(String.format("/filters/%s", filterId)), 60, null, prefKeyOauthTokenT);
            filter = parseFilter(new JSONObject(response));
            filters.add(filter);
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        apiResponse.setFilters(filters);
        return apiResponse;
    }


    /**
     * Create a filter
     * @param filter Filter
     * @return APIResponse
     */
    public APIResponse addFilters(Filters filter){
        HashMap<String, String> params = new HashMap<>();
        params.put("phrase", filter.getPhrase());
        StringBuilder parameters = new StringBuilder();
        for(String context: filter.getContext())
            parameters.append("context[]=").append(context).append("&");
        if( parameters.length() > 0) {
            parameters = new StringBuilder(parameters.substring(0, parameters.length() - 1).substring(10));
            params.put("context[]", parameters.toString());
        }
        params.put("irreversible", String.valueOf(filter.isIrreversible()));
        params.put("whole_word", String.valueOf(filter.isWhole_word()));
        params.put("expires_in", String.valueOf(filter.getExpires_in()));
        ArrayList<Filters> filters = new ArrayList<>();
        try {
            String response = new HttpsConnection(context).post(getAbsoluteUrl("/filters"), 60, params, prefKeyOauthTokenT);
            Filters resfilter = parseFilter(new JSONObject(response));
            filters.add(resfilter);
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        apiResponse.setFilters(filters);
        return apiResponse;
    }

    /**
     * Delete a filter
     * @param filter Filter
     * @return APIResponse
     */
    public int deleteFilters(Filters filter){

        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            httpsConnection.delete(getAbsoluteUrl(String.format("/filters/%s", filter.getId())), 60, null, prefKeyOauthTokenT);
            actionCode = httpsConnection.getActionCode();
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return actionCode;
    }

    /**
     * Delete a filter
     * @param filter Filter
     * @return APIResponse
     */
    public APIResponse updateFilters(Filters filter){
        HashMap<String, String> params = new HashMap<>();
        params.put("phrase", filter.getPhrase());
        StringBuilder parameters = new StringBuilder();
        for(String context: filter.getContext())
            parameters.append("context[]=").append(context).append("&");
        if( parameters.length() > 0) {
            parameters = new StringBuilder(parameters.substring(0, parameters.length() - 1).substring(10));
            params.put("context[]", parameters.toString());
        }
        params.put("irreversible", String.valueOf(filter.isIrreversible()));
        params.put("whole_word", String.valueOf(filter.isWhole_word()));
        params.put("expires_in", String.valueOf(filter.getExpires_in()));
        ArrayList<Filters> filters = new ArrayList<>();
        try {
            String response = new HttpsConnection(context).put(getAbsoluteUrl(String.format("/filters/%s", filter.getId())), 60, params, prefKeyOauthTokenT);
            Filters resfilter = parseFilter(new JSONObject(response));
            filters.add(resfilter);
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        apiResponse.setFilters(filters);
        return apiResponse;
    }

    /**
     * Get lists for the user
     * @return APIResponse
     */
    public APIResponse getLists(){

        List<fr.gouv.etalab.mastodon.client.Entities.List> lists = new ArrayList<>();
        try {
            String response = new HttpsConnection(context).get(getAbsoluteUrl("/lists"), 60, null, prefKeyOauthTokenT);
            lists = parseLists(new JSONArray(response));
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        apiResponse.setLists(lists);
        return apiResponse;
    }

    /**
     * Get lists for a user by its id
     * @return APIResponse
     */
    @SuppressWarnings("unused")
    public APIResponse getLists(String userId){

        List<fr.gouv.etalab.mastodon.client.Entities.List> lists = new ArrayList<>();
        fr.gouv.etalab.mastodon.client.Entities.List list;
        try {
            String response = new HttpsConnection(context).get(getAbsoluteUrl(String.format("/accounts/%s/lists", userId)), 60, null, prefKeyOauthTokenT);
            list = parseList(new JSONObject(response));
            lists.add(list);
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        apiResponse.setLists(lists);
        return apiResponse;
    }

    /**
     * Retrieves list timeline  *synchronously*
     * @param list_id   String id of the list
     * @param max_id   String id max
     * @param since_id String since the id
     * @param limit    int limit  - max value 40
     * @return APIResponse
     */
    public APIResponse getListTimeline(String list_id, String max_id, String since_id, int limit) {

        HashMap<String, String> params = new HashMap<>();
        if (max_id != null)
            params.put("max_id", max_id);
        if (since_id != null)
            params.put("since_id", since_id);
        if (0 > limit || limit > 80)
            limit = 80;
        params.put("limit",String.valueOf(limit));
        statuses = new ArrayList<>();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.get(getAbsoluteUrl(String.format("/timelines/list/%s",list_id)), 60, params, prefKeyOauthTokenT);
            apiResponse.setSince_id(httpsConnection.getSince_id());
            apiResponse.setMax_id(httpsConnection.getMax_id());
            statuses = parseStatuses(context, new JSONArray(response));
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        apiResponse.setStatuses(statuses);
        return apiResponse;
    }


    /**
     * Get accounts in a list for a user
     * @param listId String, id of the list
     * @param limit int, limit of results
     * @return APIResponse
     */
    @SuppressWarnings("SameParameterValue")
    public APIResponse getAccountsInList(String listId, int limit){

        HashMap<String, String> params = new HashMap<>();
        if( limit < 0)
            limit = 0;
        if( limit > 50 )
            limit = 50;
        params.put("limit",String.valueOf(limit));
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.get(getAbsoluteUrl(String.format("/lists/%s/accounts", listId)), 60, params, prefKeyOauthTokenT);
            accounts = parseAccountResponse(new JSONArray(response));
            apiResponse.setSince_id(httpsConnection.getSince_id());
            apiResponse.setMax_id(httpsConnection.getMax_id());
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        apiResponse.setAccounts(accounts);
        return apiResponse;
    }



    /**
     * Get a list
     * @param id String, id of the list
     * @return APIResponse
     */
    @SuppressWarnings("unused")
    public APIResponse getList(String id){

        List<fr.gouv.etalab.mastodon.client.Entities.List> lists = new ArrayList<>();
        fr.gouv.etalab.mastodon.client.Entities.List list;
        try {
            String response = new HttpsConnection(context).get(getAbsoluteUrl(String.format("/lists/%s",id)), 60, null, prefKeyOauthTokenT);
            list = parseList(new JSONObject(response));
            lists.add(list);
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        apiResponse.setLists(lists);
        return apiResponse;
    }




    /**
     * Parse json response an unique account
     * @param resobj JSONObject
     * @return Account
     */
    private Results parseResultsResponse(JSONObject resobj){

        Results results = new Results();
        try {
            results.setAccounts(parseAccountResponse(resobj.getJSONArray("accounts")));
            results.setStatuses(parseStatuses(context, resobj.getJSONArray("statuses")));
            results.setHashtags(parseTags(resobj.getJSONArray("hashtags")));
        } catch (JSONException e) {
            setDefaultError(e);
        }
        return results;
    }

    /**
     * Parse json response an unique Car
     * @param resobj JSONObject
     * @return Card
     */
    private static Card parseCardResponse(JSONObject resobj){

        Card card = new Card();
        try {
            card.setUrl(resobj.get("url").toString());
            card.setTitle(resobj.get("title").toString());
            card.setDescription(resobj.get("description").toString());
            card.setImage(resobj.get("image").toString());
            card.setHtml(resobj.get("html").toString());
            card.setType(resobj.get("type").toString());
            try {
                card.setAuthor_name(resobj.get("author_name").toString());
            }catch (Exception e){
                e.printStackTrace();
                card.setAuthor_name(null);
            }
            try {
                card.setAuthor_url(resobj.get("author_url").toString());
            }catch (Exception e){
                e.printStackTrace();
                card.setAuthor_url(null);
            }
            try {
                card.setEmbed_url(resobj.get("embed_url").toString());
            }catch (Exception e){
                e.printStackTrace();
                card.setEmbed_url(null);
            }
            try {
                card.setProvider_name(resobj.get("provider_name").toString());
            }catch (Exception e){
                e.printStackTrace();
                card.setProvider_name(null);
            }
            try {
                card.setProvider_url(resobj.get("provider_url").toString());
            }catch (Exception e){
                e.printStackTrace();
                card.setProvider_url(null);
            }
            try {
                card.setHeight(Integer.parseInt(resobj.get("height").toString()));
            }catch (Exception e){
                e.printStackTrace();
                card.setHeight(0);
            }
            try {
                card.setWidth(Integer.parseInt(resobj.get("width").toString()));
            }catch (Exception e){
                e.printStackTrace();
                card.setWidth(0);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            card = null;
        }
        return card;
    }

    /**
     * Parse json response an unique instance social result
     * @param resobj JSONObject
     * @return InstanceSocial
     */
    public static InstanceSocial parseInstanceSocialResponse(Context context, JSONObject resobj){

        InstanceSocial instanceSocial = new InstanceSocial();
        try {


            instanceSocial.setUptime(Float.parseFloat(resobj.get("uptime").toString()));
            instanceSocial.setUp(Boolean.parseBoolean(resobj.get("up").toString()));

            instanceSocial.setConnections(Long.parseLong(resobj.get("connections").toString()));
            instanceSocial.setDead(Boolean.parseBoolean(resobj.get("dead").toString()));


            instanceSocial.setId(resobj.get("id").toString());

            instanceSocial.setInfo(resobj.get("info").toString());
            instanceSocial.setVersion(resobj.get("version").toString());
            instanceSocial.setName(resobj.get("name").toString());
            instanceSocial.setObs_rank(resobj.get("obs_rank").toString());
            instanceSocial.setThumbnail(resobj.get("thumbnail").toString());
            instanceSocial.setIpv6(Boolean.parseBoolean(resobj.get("ipv6").toString()));
            instanceSocial.setObs_score(Integer.parseInt(resobj.get("obs_score").toString()));
            instanceSocial.setOpen_registrations(Boolean.parseBoolean(resobj.get("open_registrations").toString()));

            instanceSocial.setUsers(Long.parseLong(resobj.get("users").toString()));
            instanceSocial.setStatuses(Long.parseLong(resobj.get("statuses").toString()));

            instanceSocial.setHttps_rank(resobj.get("https_rank").toString());
            instanceSocial.setHttps_score(Integer.parseInt(resobj.get("https_score").toString()));
            instanceSocial.setAdded_at(Helper.mstStringToDate(context, resobj.get("added_at").toString()));
            instanceSocial.setChecked_at(Helper.mstStringToDate(context, resobj.get("checked_at").toString()));
            instanceSocial.setUpdated_at(Helper.mstStringToDate(context, resobj.get("updated_at").toString()));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return instanceSocial;
    }


    /**
     * Parse Domains
     * @param jsonArray JSONArray
     * @return List<String> of domains
     */
    private List<String> parseDomains(JSONArray jsonArray){
        List<String> list_tmp = new ArrayList<>();
        for(int i = 0; i < jsonArray.length(); i++){
            try {
                list_tmp.add(jsonArray.getString(i));
            } catch (JSONException ignored) {}
        }
        return  list_tmp;
    }


    /**
     * Parse Tags
     * @param jsonArray JSONArray
     * @return List<String> of tags
     */
    private List<String> parseTags(JSONArray jsonArray){
        List<String> list_tmp = new ArrayList<>();
        for(int i = 0; i < jsonArray.length(); i++){
            try {
                list_tmp.add(jsonArray.getString(i));
            } catch (JSONException ignored) {}
        }
        return  list_tmp;
    }
    /**
     * Parse json response for several howto
     * @param jsonArray JSONArray
     * @return List<HowToVideo>
     */
    private List<HowToVideo> parseHowTos(JSONArray jsonArray){

        List<HowToVideo> howToVideos = new ArrayList<>();
        try {
            int i = 0;
            while (i < jsonArray.length() ){

                JSONObject resobj = jsonArray.getJSONObject(i);
                HowToVideo howToVideo = parseHowTo(context, resobj);
                i++;
                howToVideos.add(howToVideo);
            }

        } catch (JSONException e) {
            setDefaultError(e);
        }
        return howToVideos;
    }

    /**
     * Parse json response for several howto
     * @param jsonArray JSONArray
     * @return List<Peertube>
     */
    private List<Peertube> parsePeertube(JSONArray jsonArray){

        List<Peertube> peertubes = new ArrayList<>();
        try {
            int i = 0;
            while (i < jsonArray.length() ){
                JSONObject resobj = jsonArray.getJSONObject(i);
                Peertube peertube = parsePeertube(context, resobj);
                i++;
                peertubes.add(peertube);
            }

        } catch (JSONException e) {
            setDefaultError(e);
        }
        return peertubes;
    }

    /**
     * Parse json response for unique how to
     * @param resobj JSONObject
     * @return Peertube
     */
    public static Peertube parsePeertube(Context context,JSONObject resobj){
        Peertube peertube = new Peertube();
        try {
            peertube.setId(resobj.get("id").toString());
            peertube.setCache(resobj);
            peertube.setUuid(resobj.get("uuid").toString());
            peertube.setName(resobj.get("name").toString());
            peertube.setDescription(resobj.get("description").toString());
            peertube.setEmbedPath(resobj.get("embedPath").toString());
            peertube.setPreviewPath(resobj.get("previewPath").toString());
            peertube.setThumbnailPath(resobj.get("thumbnailPath").toString());
            peertube.setAccount(parseAccountResponsePeertube(context, resobj.getJSONObject("account")));
            peertube.setView(Integer.parseInt(resobj.get("views").toString()));
            peertube.setLike(Integer.parseInt(resobj.get("likes").toString()));
            peertube.setDislike(Integer.parseInt(resobj.get("dislikes").toString()));
            peertube.setDuration(Integer.parseInt(resobj.get("duration").toString()));
            peertube.setSensitive(Boolean.parseBoolean(resobj.get("nsfw").toString()));
            peertube.setCategory(resobj.getJSONObject("category").get("label").toString());
            peertube.setLicense(resobj.getJSONObject("licence").get("label").toString());
            peertube.setLanguage(resobj.getJSONObject("language").get("label").toString());
            try {
                peertube.setCreated_at(Helper.mstStringToDate(context, resobj.get("createdAt").toString()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return peertube;
    }

    /**
     * Parse json response for unique how to
     * @param resobj JSONObject
     * @return Peertube
     */
    private static Peertube parseSinglePeertube(Context context, String instance, JSONObject resobj){
        Peertube peertube = new Peertube();
        try {
            peertube.setId(resobj.get("id").toString());
            peertube.setUuid(resobj.get("uuid").toString());
            peertube.setName(resobj.get("name").toString());
            peertube.setCache(resobj);
            peertube.setInstance(instance);
            peertube.setHost(resobj.getJSONObject("account").get("host").toString());
            peertube.setDescription(resobj.get("description").toString());
            peertube.setEmbedPath(resobj.get("embedPath").toString());
            peertube.setPreviewPath(resobj.get("previewPath").toString());
            peertube.setThumbnailPath(resobj.get("thumbnailPath").toString());
            peertube.setView(Integer.parseInt(resobj.get("views").toString()));
            peertube.setLike(Integer.parseInt(resobj.get("likes").toString()));
            peertube.setCommentsEnabled(Boolean.parseBoolean(resobj.get("commentsEnabled").toString()));
            peertube.setDislike(Integer.parseInt(resobj.get("dislikes").toString()));
            peertube.setDuration(Integer.parseInt(resobj.get("duration").toString()));
            peertube.setAccount(parseAccountResponsePeertube(context, resobj.getJSONObject("account")));
            try {
                peertube.setCreated_at(Helper.mstStringToDate(context, resobj.get("createdAt").toString()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            JSONArray files = resobj.getJSONArray("files");
            ArrayList<String> resolutions = new ArrayList<>();
            for(int j = 0 ; j < files.length() ; j++){
                JSONObject attObj = files.getJSONObject(j);
                resolutions.add(attObj.getJSONObject("resolution").get("id").toString());
            }
            peertube.setResolution(resolutions);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return peertube;
    }

    /**
     * Parse json response for peertube comments
     * @param resobj JSONObject
     * @return Peertube
     */
    private static List<Status> parseSinglePeertubeComments(Context context, String instance, JSONObject resobj){
        List<Status> statuses = new ArrayList<>();
        try {
            JSONArray jsonArray = resobj.getJSONArray("data");
                int i = 0;
                while (i < jsonArray.length() ){
                    Status status = new Status();
                    JSONObject comment = jsonArray.getJSONObject(i);
                    status.setId(comment.get("id").toString());
                    status.setUri(comment.get("url").toString());
                    status.setUrl(comment.get("url").toString());
                    status.setSensitive(false);
                    status.setSpoiler_text("");
                    status.setContent(comment.get("text").toString());
                    status.setIn_reply_to_id(comment.get("inReplyToCommentId").toString());
                    status.setAccount(parseAccountResponsePeertube(context, comment.getJSONObject("account")));
                    status.setCreated_at(Helper.mstStringToDate(context, comment.get("createdAt").toString()));
                    status.setMentions(new ArrayList<>());
                    status.setEmojis(new ArrayList<>());
                    status.setMedia_attachments(new ArrayList<>());
                    status.setVisibility("public");
                    i++;
                    statuses.add(status);
                }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return statuses;
    }

    /**
     * Parse json response for unique how to
     * @param resobj JSONObject
     * @return HowToVideo
     */
    private static HowToVideo parseHowTo(Context context, JSONObject resobj){
        HowToVideo howToVideo = new HowToVideo();
        try {
            howToVideo.setId(resobj.get("id").toString());
            howToVideo.setUuid(resobj.get("uuid").toString());
            howToVideo.setName(resobj.get("name").toString());
            howToVideo.setDescription(resobj.get("description").toString());
            howToVideo.setEmbedPath(resobj.get("embedPath").toString());
            howToVideo.setPreviewPath(resobj.get("previewPath").toString());
            howToVideo.setThumbnailPath(resobj.get("thumbnailPath").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return howToVideo;
    }

    /**
     * Parse json response for several conversations
     * @param jsonArray JSONArray
     * @return List<Conversation>
     */
    private List<Conversation> parseConversations(JSONArray jsonArray){

        List<Conversation> conversations = new ArrayList<>();
        try {
            int i = 0;
            while (i < jsonArray.length() ){

                JSONObject resobj = jsonArray.getJSONObject(i);
                Conversation conversation = parseConversation(context, resobj);
                i++;
                conversations.add(conversation);
            }

        } catch (JSONException e) {
            setDefaultError(e);
        }
        return conversations;
    }

    /**
     * Parse json response for unique conversation
     * @param resobj JSONObject
     * @return Conversation
     */
    @SuppressWarnings("InfiniteRecursion")
    private Conversation parseConversation(Context context, JSONObject resobj) {
        Conversation conversation = new Conversation();
        try {
            conversation.setId(resobj.get("id").toString());
            conversation.setUnread(Boolean.parseBoolean(resobj.get("unread").toString()));
            conversation.setAccounts(parseAccountResponse(resobj.getJSONArray("accounts")));
            conversation.setLast_status(parseStatuses(context, resobj.getJSONObject("last_status")));
        }catch (JSONException ignored) {}
        return conversation;
    }
    /**
     * Parse json response for several status
     * @param jsonArray JSONArray
     * @return List<Status>
     */
    public static List<Status> parseStatuses(Context context, JSONArray jsonArray){

        List<Status> statuses = new ArrayList<>();
        try {
            int i = 0;
            while (i < jsonArray.length() ){

                JSONObject resobj = jsonArray.getJSONObject(i);
                Status status = parseStatuses(context, resobj);
                i++;
                statuses.add(status);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return statuses;
    }

    /**
     * Parse json response for unique status
     * @param resobj JSONObject
     * @return Status
     */
    @SuppressWarnings("InfiniteRecursion")
    public static Status parseStatuses(Context context, JSONObject resobj){
        Status status = new Status();
        try {
            status.setId(resobj.get("id").toString());
            status.setUri(resobj.get("uri").toString());
            status.setCreated_at(Helper.mstStringToDate(context, resobj.get("created_at").toString()));
            status.setIn_reply_to_id(resobj.get("in_reply_to_id").toString());
            status.setIn_reply_to_account_id(resobj.get("in_reply_to_account_id").toString());
            status.setSensitive(Boolean.parseBoolean(resobj.get("sensitive").toString()));
            status.setSpoiler_text(resobj.get("spoiler_text").toString());
            try {
                status.setVisibility(resobj.get("visibility").toString());
            }catch (Exception e){status.setVisibility("public");}
            status.setLanguage(resobj.get("language").toString());
            status.setUrl(resobj.get("url").toString());
            //Retrieves attachments
            JSONArray arrayAttachement = resobj.getJSONArray("media_attachments");
            ArrayList<Attachment> attachments = new ArrayList<>();
            if( arrayAttachement != null){
                for(int j = 0 ; j < arrayAttachement.length() ; j++){
                    JSONObject attObj = arrayAttachement.getJSONObject(j);
                    Attachment attachment = new Attachment();
                    attachment.setId(attObj.get("id").toString());
                    attachment.setPreview_url(attObj.get("preview_url").toString());
                    attachment.setRemote_url(attObj.get("remote_url").toString());
                    attachment.setType(attObj.get("type").toString());
                    attachment.setText_url(attObj.get("text_url").toString());
                    attachment.setUrl(attObj.get("url").toString());
                    try {
                        attachment.setDescription(attObj.get("description").toString());
                    }catch (JSONException ignore){}
                    attachments.add(attachment);
                }
            }
            try {

                status.setCard(parseCardResponse(resobj.getJSONObject("card")));
            }catch (Exception e){status.setCard(null);}


            status.setMedia_attachments(attachments);
            //Retrieves mentions
            List<Mention> mentions = new ArrayList<>();
            JSONArray arrayMention = resobj.getJSONArray("mentions");
            if( arrayMention != null){
                for(int j = 0 ; j < arrayMention.length() ; j++){
                    JSONObject menObj = arrayMention.getJSONObject(j);
                    Mention mention = new Mention();
                    mention.setId(menObj.get("id").toString());
                    mention.setUrl(menObj.get("url").toString());
                    mention.setAcct(menObj.get("acct").toString());
                    mention.setUsername(menObj.get("username").toString());
                    mentions.add(mention);
                }
            }
            status.setMentions(mentions);
            //Retrieves tags
            List<Tag> tags = new ArrayList<>();
            JSONArray arrayTag = resobj.getJSONArray("tags");
            if( arrayTag != null){
                for(int j = 0 ; j < arrayTag.length() ; j++){
                    JSONObject tagObj = arrayTag.getJSONObject(j);
                    Tag tag = new Tag();
                    tag.setName(tagObj.get("name").toString());
                    tag.setUrl(tagObj.get("url").toString());
                    tags.add(tag);
                }
            }
            status.setTags(tags);

            //Retrieves emjis
            List<Emojis> emojiList = new ArrayList<>();
            try {
                JSONArray emojisTag = resobj.getJSONArray("emojis");
                if( emojisTag != null){
                    for(int j = 0 ; j < emojisTag.length() ; j++){
                        JSONObject emojisObj = emojisTag.getJSONObject(j);
                        Emojis emojis = parseEmojis(emojisObj);
                        emojiList.add(emojis);
                    }
                }
                status.setEmojis(emojiList);
            }catch (Exception e){
                status.setEmojis(new ArrayList<>());
            }

            //Retrieve Application
            Application application = new Application();
            try {
                if(resobj.getJSONObject("application") != null){
                    application.setName(resobj.getJSONObject("application").getString("name"));
                    application.setWebsite(resobj.getJSONObject("application").getString("website"));
                }
            }catch (Exception e){
                application = new Application();
            }
            status.setApplication(application);

            status.setAccount(parseAccountResponse(context, resobj.getJSONObject("account")));
            status.setContent(resobj.get("content").toString());
            status.setFavourites_count(Integer.valueOf(resobj.get("favourites_count").toString()));
            status.setReblogs_count(Integer.valueOf(resobj.get("reblogs_count").toString()));
            try{
                status.setReplies_count(Integer.valueOf(resobj.get("replies_count").toString()));
            }catch (Exception e){
                status.setReplies_count(-1);
            }
            try {
                status.setReblogged(Boolean.valueOf(resobj.get("reblogged").toString()));
            }catch (Exception e){
                status.setReblogged(false);
            }
            try {
                status.setFavourited(Boolean.valueOf(resobj.get("favourited").toString()));
            }catch (Exception e){
                status.setFavourited(false);
            }
            try {
                status.setMuted(Boolean.valueOf(resobj.get("muted").toString()));
            }catch (Exception e){
                status.setMuted(false);
            }
            try {
                status.setPinned(Boolean.valueOf(resobj.get("pinned").toString()));
            }catch (JSONException e){
                status.setPinned(false);
            }
            try{
                status.setReblog(parseStatuses(context, resobj.getJSONObject("reblog")));
            }catch (Exception ignored){}
        } catch (JSONException ignored) {} catch (ParseException e) {
            e.printStackTrace();
        }
        return status;
    }


    /**
     * Parse json response for several notes (Misskey)
     * @param jsonArray JSONArray
     * @return List<Status>
     */
    public static List<Status> parseNotes(Context context, String instance, JSONArray jsonArray){

        List<Status> statuses = new ArrayList<>();
        try {
            int i = 0;
            while (i < jsonArray.length() ){

                JSONObject resobj = jsonArray.getJSONObject(i);
                Status status = parseNotes(context, instance, resobj);
                i++;
                statuses.add(status);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return statuses;
    }

    /**
     * Parse json response for unique note (misskey)
     * @param resobj JSONObject
     * @return Status
     */
    @SuppressWarnings("InfiniteRecursion")
    public static Status parseNotes(Context context, String instance, JSONObject resobj){
        Status status = new Status();
        try {
            status.setId(resobj.get("id").toString());
            status.setUri("https://" + instance + "/notes/" + resobj.get("id").toString());
            status.setCreated_at(Helper.mstStringToDate(context, resobj.get("createdAt").toString()));
            status.setIn_reply_to_id(resobj.get("replyId").toString());
            status.setSensitive(false);
            if(resobj.get("cw") != null && !resobj.get("cw").toString().equals("null"))
                status.setSpoiler_text(resobj.get("cw").toString());
            try {
                status.setVisibility(resobj.get("visibility").toString());
            }catch (Exception e){status.setVisibility("public"); e.printStackTrace();}
            status.setUrl("https://" + instance + "/notes/" + resobj.get("id").toString());
            //Retrieves attachments
            JSONArray arrayAttachement = resobj.getJSONArray("media");
            ArrayList<Attachment> attachments = new ArrayList<>();
            if( arrayAttachement != null){
                for(int j = 0 ; j < arrayAttachement.length() ; j++){
                    JSONObject attObj = arrayAttachement.getJSONObject(j);
                    Attachment attachment = new Attachment();
                    attachment.setId(attObj.get("id").toString());
                    attachment.setPreview_url(attObj.get("thumbnailUrl").toString());
                    attachment.setRemote_url(attObj.get("url").toString());
                    if( attObj.get("type").toString().contains("/")){
                        attachment.setType(attObj.get("type").toString().split("/")[0]);
                    }else
                        attachment.setType(attObj.get("type").toString());
                    attachment.setText_url(attObj.get("url").toString());
                    attachment.setUrl(attObj.get("url").toString());
                    if(attObj.get("isSensitive").toString().equals("true")){
                        status.setSensitive(true);
                    }
                    try {
                        attachment.setDescription(attObj.get("comment").toString());
                    }catch (JSONException ignore){ignore.printStackTrace();}
                    attachments.add(attachment);
                }
            }
            try {
                status.setCard(parseCardResponse(resobj.getJSONObject("card")));
            }catch (Exception e){status.setCard(null);}

            status.setMedia_attachments(attachments);
            //Retrieves mentions
            List<Mention> mentions = new ArrayList<>();

            status.setAccount(parseMisskeyAccountResponse(context, instance, resobj.getJSONObject("user")));
            status.setContent(resobj.get("text").toString());
            try{
                status.setReplies_count(Integer.valueOf(resobj.get("repliesCount").toString()));
            }catch (Exception e){
                status.setReplies_count(-1);
            }
            try {
                status.setFavourited(Boolean.valueOf(resobj.get("isFavorited").toString()));
            }catch (Exception e){
                status.setFavourited(false);
            }
            try{
                if(resobj.getJSONObject("renoteId")  != null &&  !resobj.getJSONObject("renoteId").toString().equals("null"))
                    status.setReblog(parseStatuses(context, resobj.getJSONObject("renote")));
            }catch (Exception ignored){}

            status.setMentions(mentions);
            //Retrieves tags
            List<Tag> tags = new ArrayList<>();
            JSONArray arrayTag = resobj.getJSONArray("tags");
            if( arrayTag != null){
                for(int j = 0 ; j < arrayTag.length() ; j++){
                    JSONObject tagObj = arrayTag.getJSONObject(j);
                    Tag tag = new Tag();
                    tag.setName(tagObj.get("name").toString());
                    tag.setUrl(tagObj.get("url").toString());
                    tags.add(tag);
                }
            }
            status.setTags(tags);

            //Retrieves emjis
            List<Emojis> emojiList = new ArrayList<>();
            try {
                JSONArray emojisTag = resobj.getJSONArray("emojis");
                if( emojisTag != null){
                    for(int j = 0 ; j < emojisTag.length() ; j++){
                        JSONObject emojisObj = emojisTag.getJSONObject(j);
                        Emojis emojis = parseMisskeyEmojis(emojisObj);
                        emojiList.add(emojis);
                    }
                }
                status.setEmojis(emojiList);
            }catch (Exception e){
                status.setEmojis(new ArrayList<>());
            }

            //Retrieve Application
            Application application = new Application();
            try {
                if(resobj.getJSONObject("application") != null){
                    application.setName(resobj.getJSONObject("application").getString("name"));
                    application.setWebsite(resobj.getJSONObject("application").getString("website"));
                }
            }catch (Exception e){
                application = new Application();
            }
            status.setApplication(application);


        } catch (JSONException ignored) {} catch (ParseException e) {
            e.printStackTrace();
        }
        return status;
    }
    /**
     * Parse json response an unique instance
     * @param resobj JSONObject
     * @return Instance
     */
    private Instance parseInstance(JSONObject resobj){

        Instance instance = new Instance();
        try {
            instance.setUri(resobj.get("uri").toString());
            instance.setTitle(resobj.get("title").toString());
            instance.setDescription(resobj.get("description").toString());
            instance.setEmail(resobj.get("email").toString());
            instance.setVersion(resobj.get("version").toString());
        } catch (JSONException e) {
            setDefaultError(e);
        }
        return instance;
    }



    /**
     * Parse emojis
     * @param jsonArray JSONArray
     * @return List<Emojis> of emojis
     */
    private List<Emojis> parseEmojis(JSONArray jsonArray){
        List<Emojis> emojis = new ArrayList<>();
        try {
            int i = 0;
            while (i < jsonArray.length() ) {
                JSONObject resobj = jsonArray.getJSONObject(i);
                Emojis emojis1 = parseEmojis(resobj);
                emojis.add(emojis1);
                i++;
            }
        } catch (JSONException e) {
            setDefaultError(e);
        }
        return emojis;
    }


    /**
     * Parse json response for emoji
     * @param resobj JSONObject
     * @return Emojis
     */
    private static Emojis parseEmojis(JSONObject resobj){
        Emojis emojis = new Emojis();
        try {
            emojis.setShortcode(resobj.get("shortcode").toString());
            emojis.setStatic_url(resobj.get("static_url").toString());
            emojis.setUrl(resobj.get("url").toString());
        }catch (Exception ignored){}
        return emojis;
    }


    /**
     * Parse emojis
     * @param jsonArray JSONArray
     * @return List<Emojis> of emojis
     */
    private List<Emojis> parseMisskeyEmojis(JSONArray jsonArray){
        List<Emojis> emojis = new ArrayList<>();
        try {
            int i = 0;
            while (i < jsonArray.length() ) {
                JSONObject resobj = jsonArray.getJSONObject(i);
                Emojis emojis1 = parseMisskeyEmojis(resobj);
                emojis.add(emojis1);
                i++;
            }
        } catch (JSONException e) {
            setDefaultError(e);
        }
        return emojis;
    }


    /**
     * Parse json response for emoji
     * @param resobj JSONObject
     * @return Emojis
     */
    private static Emojis parseMisskeyEmojis(JSONObject resobj){
        Emojis emojis = new Emojis();
        try {
            emojis.setShortcode(resobj.get("name").toString());
            emojis.setStatic_url(resobj.get("url").toString());
            emojis.setUrl(resobj.get("url").toString());
        }catch (Exception ignored){}
        return emojis;
    }


    /**
     * Parse Filters
     * @param jsonArray JSONArray
     * @return List<Filters> of filters
     */
    private List<Filters> parseFilters(JSONArray jsonArray){
        List<Filters> filters = new ArrayList<>();
        try {
            int i = 0;
            while (i < jsonArray.length() ) {
                JSONObject resobj = jsonArray.getJSONObject(i);
                Filters filter = parseFilter(resobj);
                if( filter != null)
                    filters.add(filter);
                i++;
            }
        } catch (JSONException e) {
            setDefaultError(e);
        }
        return filters;
    }

    /**
     * Parse json response for filter
     * @param resobj JSONObject
     * @return Filter
     */
    private Filters parseFilter(JSONObject resobj){
        Filters filter = new Filters();
        try {

            filter.setId(resobj.get("id").toString());
            if( resobj.get("phrase").toString() == null)
                return null;
            filter.setPhrase(resobj.get("phrase").toString());
            if( resobj.get("expires_at") != null &&  !resobj.get("expires_at").toString().equals("null"))
                filter.setSetExpires_at(Helper.mstStringToDate(context, resobj.get("expires_at").toString()));
            filter.setWhole_word(Boolean.parseBoolean(resobj.get("whole_word").toString()));
            filter.setIrreversible(Boolean.parseBoolean(resobj.get("irreversible").toString()));
            String contextString = resobj.get("context").toString();
            contextString = contextString.replaceAll("\\[","");
            contextString = contextString.replaceAll("]","");
            contextString = contextString.replaceAll("\"","");
            if( contextString != null) {
                String[] context = contextString.split(",");
                if( contextString.length() > 0 ){
                    ArrayList<String> finalContext = new ArrayList<>();
                    for(String c: context)
                        finalContext.add(c.trim());
                    filter.setContext(finalContext);
                }
            }
            return filter;
        }catch (Exception ignored){ return null;}

    }


    /**
     * Parse Lists
     * @param jsonArray JSONArray
     * @return List<List> of lists
     */
    private List<fr.gouv.etalab.mastodon.client.Entities.List> parseLists(JSONArray jsonArray){
        List<fr.gouv.etalab.mastodon.client.Entities.List> lists = new ArrayList<>();
        try {
            int i = 0;
            while (i < jsonArray.length() ) {
                JSONObject resobj = jsonArray.getJSONObject(i);
                fr.gouv.etalab.mastodon.client.Entities.List list = parseList(resobj);
                lists.add(list);
                i++;
            }
        } catch (JSONException e) {
            setDefaultError(e);
        }
        return lists;
    }


    /**
     * Parse json response for emoji
     * @param resobj JSONObject
     * @return Emojis
     */
    private static fr.gouv.etalab.mastodon.client.Entities.List parseList(JSONObject resobj){
        fr.gouv.etalab.mastodon.client.Entities.List list = new fr.gouv.etalab.mastodon.client.Entities.List();
        try {
            list.setId(resobj.get("id").toString());
            list.setTitle(resobj.get("title").toString());
        }catch (Exception ignored){}
        return list;
    }

    private List<Account> parseAccountResponsePeertube(Context context, String instance, JSONArray jsonArray){
        List<Account> accounts = new ArrayList<>();
        try {
            int i = 0;
            while (i < jsonArray.length() ) {
                JSONObject resobj = jsonArray.getJSONObject(i);
                Account account = parseAccountResponsePeertube(context, resobj);
                accounts.add(account);
                i++;
            }
        } catch (JSONException e) {
            setDefaultError(e);
        }
        return accounts;
    }

    /**
     * Parse json response an unique peertube account
     * @param accountObject JSONObject
     * @return Account
     */
    private static Account parseAccountResponsePeertube(Context context, JSONObject accountObject){
        Account account = new Account();
        try {
            account.setId(accountObject.get("id").toString());
            account.setUuid(accountObject.get("uuid").toString());
            account.setUsername(accountObject.get("name").toString());
            account.setAcct(accountObject.get("name").toString()+"@" + accountObject.get("host"));
            account.setDisplay_name(accountObject.get("name").toString());
            account.setHost(accountObject.get("host").toString());


            if( accountObject.has("createdAt") )
                account.setCreated_at(Helper.mstStringToDate(context, accountObject.get("createdAt").toString()));
            else
                account.setCreated_at(new Date());
            if( accountObject.has("followersCount") )
                account.setFollowers_count(Integer.valueOf(accountObject.get("followersCount").toString()));
            else
                account.setFollowers_count(0);
            if( accountObject.has("followingCount"))
                account.setFollowing_count(Integer.valueOf(accountObject.get("followingCount").toString()));
            else
                account.setFollowing_count(0);
            account.setStatuses_count(0);
            if( accountObject.has("description") )
                account.setNote(accountObject.get("description").toString());
            else
                account.setNote("");

            account.setUrl(accountObject.get("url").toString());
            if( accountObject.has("avatar") && !accountObject.isNull("avatar")){
                account.setAvatar(accountObject.getJSONObject("avatar").get("path").toString());
            }else
                account.setAvatar(null);
            account.setHeader("null");
            account.setHeader_static("null");
            account.setAvatar_static(accountObject.get("avatar").toString());
        } catch (JSONException ignored) {ignored.printStackTrace();} catch (ParseException e) {
            e.printStackTrace();
        }
        return account;
    }

    /**
     * Parse json response an unique account
     * @param resobj JSONObject
     * @return Account
     */
    @SuppressWarnings("InfiniteRecursion")
    private static Account parseAccountResponse(Context context, JSONObject resobj){

        Account account = new Account();
        try {
            account.setId(resobj.get("id").toString());
            account.setUsername(resobj.get("username").toString());
            account.setAcct(resobj.get("acct").toString());
            account.setDisplay_name(resobj.get("display_name").toString());
            account.setLocked(Boolean.parseBoolean(resobj.get("locked").toString()));
            account.setCreated_at(Helper.mstStringToDate(context, resobj.get("created_at").toString()));
            account.setFollowers_count(Integer.valueOf(resobj.get("followers_count").toString()));
            account.setFollowing_count(Integer.valueOf(resobj.get("following_count").toString()));
            account.setStatuses_count(Integer.valueOf(resobj.get("statuses_count").toString()));
            account.setNote(resobj.get("note").toString());
            try {
                account.setBot(Boolean.parseBoolean(resobj.get("bot").toString()));
            }catch (Exception e){
                account.setBot(false);
            }
            try{
                account.setMoved_to_account(parseAccountResponse(context, resobj.getJSONObject("moved")));
            }catch (Exception ignored){account.setMoved_to_account(null);}
            account.setUrl(resobj.get("url").toString());
            account.setAvatar(resobj.get("avatar").toString());
            account.setAvatar_static(resobj.get("avatar_static").toString());
            account.setHeader(resobj.get("header").toString());
            account.setHeader_static(resobj.get("header_static").toString());
            try {
                JSONArray fields = resobj.getJSONArray("fields");
                LinkedHashMap<String, String> fieldsMap = new LinkedHashMap<>();
                LinkedHashMap<String, Boolean> fieldsMapVerified = new LinkedHashMap<>();
                if( fields != null){
                    for(int j = 0 ; j < fields.length() ; j++){
                        fieldsMap.put(fields.getJSONObject(j).getString("name"),fields.getJSONObject(j).getString("value"));
                        try {
                            fieldsMapVerified.put(fields.getJSONObject(j).getString("name"),(fields.getJSONObject(j).getString("verified_at")!= null && !fields.getJSONObject(j).getString("verified_at").equals("null")));
                        }catch (Exception e){
                            fieldsMapVerified.put(fields.getJSONObject(j).getString("name"),false);
                        }

                    }
                }
                account.setFields(fieldsMap);
                account.setFieldsVerified(fieldsMapVerified);
            }catch (Exception ignored){}

            //Retrieves emjis
            List<Emojis> emojiList = new ArrayList<>();
            try {
                JSONArray emojisTag = resobj.getJSONArray("emojis");
                if( emojisTag != null){
                    for(int j = 0 ; j < emojisTag.length() ; j++){
                        JSONObject emojisObj = emojisTag.getJSONObject(j);
                        Emojis emojis = parseEmojis(emojisObj);
                        emojiList.add(emojis);
                    }
                }
                account.setEmojis(emojiList);
            }catch (Exception e){
                account.setEmojis(new ArrayList<>());
            }
        } catch (JSONException ignored) {} catch (ParseException e) {
            e.printStackTrace();
        }
        return account;
    }


    /**
     * Parse json response an unique account
     * @param resobj JSONObject
     * @return Account
     */
    @SuppressWarnings("InfiniteRecursion")
    private static Account parseMisskeyAccountResponse(Context context, String instance, JSONObject resobj){

        Account account = new Account();
        try {
            account.setId(resobj.get("id").toString());
            account.setUsername(resobj.get("username").toString());
            String host = resobj.get("host").toString();
            String acct;
            if( host == null || host.equals("null"))
                acct = resobj.get("username").toString();
            else
                acct = resobj.get("username").toString() + "@" + host;
            account.setAcct(acct);
            account.setDisplay_name(resobj.get("name").toString());
            account.setCreated_at(new Date());

            account.setUrl("https://" + instance + "/@"+account.getUsername());
            account.setAvatar(resobj.get("avatarUrl").toString());
            account.setAvatar_static(resobj.get("avatarUrl").toString());
            try {
                account.setBot(Boolean.parseBoolean(resobj.get("isBot").toString()));
            }catch (Exception e){
                account.setBot(false);
            }
            //Retrieves emjis
            List<Emojis> emojiList = new ArrayList<>();
            try {
                JSONArray emojisTag = resobj.getJSONArray("emojis");
                if( emojisTag != null){
                    for(int j = 0 ; j < emojisTag.length() ; j++){
                        JSONObject emojisObj = emojisTag.getJSONObject(j);
                        Emojis emojis = parseEmojis(emojisObj);
                        emojiList.add(emojis);
                    }
                }
                account.setEmojis(emojiList);
            }catch (Exception e){
                account.setEmojis(new ArrayList<>());
            }
        } catch (JSONException ignored) {}
        return account;
    }
    /**
     * Parse json response for list of accounts
     * @param jsonArray JSONArray
     * @return List<Account>
     */
    private List<Account> parseAccountResponse(JSONArray jsonArray){

        List<Account> accounts = new ArrayList<>();
        try {
            int i = 0;
            while (i < jsonArray.length() ) {
                JSONObject resobj = jsonArray.getJSONObject(i);
                Account account = parseAccountResponse(context, resobj);
                accounts.add(account);
                i++;
            }
        } catch (JSONException e) {
            setDefaultError(e);
        }
        return accounts;
    }


    /**
     * Parse json response an unique relationship
     * @param resobj JSONObject
     * @return Relationship
     */
    private Relationship parseRelationshipResponse(JSONObject resobj){

        Relationship relationship = new Relationship();
        try {
            relationship.setId(resobj.get("id").toString());
            relationship.setFollowing(Boolean.valueOf(resobj.get("following").toString()));
            relationship.setFollowed_by(Boolean.valueOf(resobj.get("followed_by").toString()));
            relationship.setBlocking(Boolean.valueOf(resobj.get("blocking").toString()));
            relationship.setMuting(Boolean.valueOf(resobj.get("muting").toString()));
            try {
                relationship.setMuting_notifications(Boolean.valueOf(resobj.get("muting_notifications").toString()));
            }catch (Exception ignored){
                relationship.setMuting_notifications(true);
            }
            try {
                relationship.setEndorsed(Boolean.valueOf(resobj.get("endorsed").toString()));
            }catch (Exception ignored){
                relationship.setMuting_notifications(false);
            }
            try {
                relationship.setShowing_reblogs(Boolean.valueOf(resobj.get("showing_reblogs").toString()));
            }catch (Exception ignored){
                relationship.setMuting_notifications(false);
            }
            relationship.setRequested(Boolean.valueOf(resobj.get("requested").toString()));
        } catch (JSONException e) {
            setDefaultError(e);
        }
        return relationship;
    }


    /**
     * Parse json response for list of relationship
     * @param jsonArray JSONArray
     * @return List<Relationship>
     */
    private List<Relationship> parseRelationshipResponse(JSONArray jsonArray){

        List<Relationship> relationships = new ArrayList<>();
        try {
            int i = 0;
            while (i < jsonArray.length() ) {
                JSONObject resobj = jsonArray.getJSONObject(i);
                Relationship relationship = parseRelationshipResponse(resobj);
                relationships.add(relationship);
                i++;
            }
        } catch (JSONException e) {
            setDefaultError(e);
        }
        return relationships;
    }

    /**
     * Parse json response for the context
     * @param jsonObject JSONObject
     * @return fr.gouv.etalab.mastodon.client.Entities.Context
     */
    private fr.gouv.etalab.mastodon.client.Entities.Context parseContext(JSONObject jsonObject){

        fr.gouv.etalab.mastodon.client.Entities.Context context = new fr.gouv.etalab.mastodon.client.Entities.Context();
        try {
            context.setAncestors(parseStatuses(this.context, jsonObject.getJSONArray("ancestors")));
            context.setDescendants(parseStatuses(this.context, jsonObject.getJSONArray("descendants")));
        } catch (JSONException e) {
            setDefaultError(e);
        }
        return context;
    }

    /**
     * Parse json response an unique attachment
     * @param resobj JSONObject
     * @return Relationship
     */
    static Attachment parseAttachmentResponse(JSONObject resobj){

        Attachment attachment = new Attachment();
        try {
            attachment.setId(resobj.get("id").toString());
            attachment.setType(resobj.get("type").toString());
            attachment.setUrl(resobj.get("url").toString());
            try {
                attachment.setDescription(resobj.get("description").toString());
            }catch (JSONException ignore){}
            try{
                attachment.setRemote_url(resobj.get("remote_url").toString());
            }catch (JSONException ignore){}
            try{
                attachment.setPreview_url(resobj.get("preview_url").toString());
            }catch (JSONException ignore){}
            try{
                attachment.setMeta(resobj.get("meta").toString());
            }catch (JSONException ignore){}
            try{
                attachment.setText_url(resobj.get("text_url").toString());
            }catch (JSONException ignore){}

        } catch (JSONException ignored) {}
        return attachment;
    }



    /**
     * Parse json response an unique notification
     * @param resobj JSONObject
     * @return Account
     */
    public static Notification parseNotificationResponse(Context context, JSONObject resobj){

        Notification notification = new Notification();
        try {
            notification.setId(resobj.get("id").toString());
            notification.setType(resobj.get("type").toString());
            notification.setCreated_at(Helper.mstStringToDate(context, resobj.get("created_at").toString()));
            notification.setAccount(parseAccountResponse(context, resobj.getJSONObject("account")));
            try{
                notification.setStatus(parseStatuses(context, resobj.getJSONObject("status")));
            }catch (Exception ignored){}
            notification.setCreated_at(Helper.mstStringToDate(context, resobj.get("created_at").toString()));
        } catch (JSONException ignored) {} catch (ParseException e) {
            e.printStackTrace();
        }
        return notification;
    }

    /**
     * Parse json response for list of notifications
     * @param jsonArray JSONArray
     * @return List<Notification>
     */
    private List<Notification> parseNotificationResponse(JSONArray jsonArray){

        List<Notification> notifications = new ArrayList<>();
        try {
            int i = 0;
            while (i < jsonArray.length() ) {

                JSONObject resobj = jsonArray.getJSONObject(i);
                Notification notification = parseNotificationResponse(context, resobj);
                notifications.add(notification);
                i++;
            }
        } catch (JSONException e) {
            setDefaultError(e);
        }
        return notifications;
    }



    /**
     * Set the error message
     * @param statusCode int code
     * @param error Throwable error
     */
    private void setError(int statusCode, Throwable error){
        APIError = new Error();
        APIError.setStatusCode(statusCode);
        String message = statusCode + " - " + error.getMessage();
        try {
            JSONObject jsonObject = new JSONObject(error.getMessage());
            String errorM = jsonObject.get("error").toString();
            message = "Error " + statusCode + " : " + errorM;
        } catch (JSONException e) {
            if(error.getMessage().split(".").length > 0) {
                String errorM = error.getMessage().split(".")[0];
                message = "Error " + statusCode + " : " + errorM;
            }
        }
        APIError.setError(message);
        apiResponse.setError(APIError);
    }

    private void setDefaultError(Exception e){
        APIError = new Error();
        if( e.getLocalizedMessage() != null && e.getLocalizedMessage().trim().length() > 0)
            APIError.setError(e.getLocalizedMessage());
        else if( e.getMessage() != null && e.getMessage().trim().length() > 0)
            APIError.setError(e.getMessage());
        else
            APIError.setError(context.getString(R.string.toast_error));
        apiResponse.setError(APIError);
    }


    public Error getError(){
        return APIError;
    }


    private String getAbsoluteUrl(String action) {
        return Helper.instanceWithProtocol(this.instance) + "/api/v1" + action;
    }
    private String getAbsoluteUrlRemote(String remote, String action) {
        return "https://" + remote + "/api/v1" + action;
    }

    private String getAbsoluteUrlRemoteInstance(String instanceName) {
        return "https://" + instanceName + "/api/v1/timelines/public?local=true";
    }

    private String getAbsoluteUrlCommunitywiki(String action) {
        return "https://communitywiki.org/trunk/api/v1"  + action;
    }

}
