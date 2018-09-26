package fr.gouv.etalab.mastodon.client;
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

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.*;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.client.Entities.*;
import fr.gouv.etalab.mastodon.client.Entities.Error;
import fr.gouv.etalab.mastodon.helper.Helper;


/**
 * Created by Thomas on 23/04/2017.
 * Manage Calls to the REST API
 * Modify the 16/11/2017 with httpsurlconnection
 */

public class API {



    private Account account;
    private Context context;
    private Results results;
    private Attachment attachment;
    private List<Account> accounts;
    private List<Status> statuses;
    private int tootPerPage, accountPerPage, notificationPerPage;
    private int actionCode;
    private String instance;
    private String prefKeyOauthTokenT;
    private APIResponse apiResponse;
    private Error APIError;
    private List<String> domains;

    public enum StatusAction{
        FAVOURITE,
        UNFAVOURITE,
        REBLOG,
        UNREBLOG,
        MUTE,
        MUTE_NOTIFICATIONS,
        UNMUTE,
        BLOCK,
        UNBLOCK,
        FOLLOW,
        UNFOLLOW,
        CREATESTATUS,
        UNSTATUS,
        AUTHORIZE,
        REJECT,
        REPORT,
        REMOTE_FOLLOW,
        PIN,
        UNPIN,
        ENDORSE,
        UNENDORSE,
        SHOW_BOOST,
        HIDE_BOOST,
        BLOCK_DOMAIN

    }
    public enum accountPrivacy {
        PUBLIC,
        LOCKED
    }
    public API(Context context) {
        this.context = context;
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        tootPerPage = sharedpreferences.getInt(Helper.SET_TOOTS_PER_PAGE, 40);
        accountPerPage = sharedpreferences.getInt(Helper.SET_ACCOUNTS_PER_PAGE, 40);
        notificationPerPage = sharedpreferences.getInt(Helper.SET_NOTIFICATIONS_PER_PAGE, 15);
        this.instance = Helper.getLiveInstance(context);
        this.prefKeyOauthTokenT = sharedpreferences.getString(Helper.PREF_KEY_OAUTH_TOKEN, null);
        apiResponse = new APIResponse();
        APIError = null;
    }

    public API(Context context, String instance, String token) {
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
    public APIResponse updateCredential(String display_name, String note, ByteArrayInputStream avatar, String avatarName, ByteArrayInputStream header, String headerName, accountPrivacy privacy, HashMap<String, String> customFields) {

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
            requestParams.put("locked",privacy==accountPrivacy.LOCKED?"true":"false");
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
            String response = new HttpsConnection(context).get(getAbsoluteUrl("/accounts/verify_credentials"), 60, null, prefKeyOauthTokenT);
            account = parseAccountResponse(context, new JSONObject(response));
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
     * @param accountId String account fetched
     * @return Relationship entity
     */
    public Relationship getRelationship(String accountId) {

        List<Relationship> relationships;
        Relationship relationship = null;
        HashMap<String, String> params = new HashMap<>();
        params.put("id",accountId);
        try {
            String response = new HttpsConnection(context).get(getAbsoluteUrl("/accounts/relationships"), 60, params, prefKeyOauthTokenT);
            relationships = parseRelationshipResponse(new JSONArray(response));
            if( relationships != null && relationships.size() > 0)
                relationship = relationships.get(0);
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
        return relationship;
    }




    /**
     * Returns a relationship between the authenticated account and an account
     * @param accounts ArrayList<Account> accounts fetched
     * @return Relationship entity
     */
    public APIResponse getRelationship(List<Account> accounts) {
        HashMap<String, String> params = new HashMap<>();
        if( accounts != null && accounts.size() > 0 ) {
            StringBuilder parameters = new StringBuilder();
            for(Account account: accounts)
                parameters.append("id[]=").append(account.getId()).append("&");
            parameters = new StringBuilder(parameters.substring(0, parameters.length() - 1).substring(5));
            params.put("id[]", parameters.toString());

        }
        List<Relationship> relationships = new ArrayList<>();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.get(getAbsoluteUrl("/accounts/relationships"), 60, params, prefKeyOauthTokenT);
            relationships = parseRelationshipResponse(new JSONArray(response));
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
        apiResponse.setRelationships(relationships);
        return apiResponse;
    }

    /**
     * Retrieves status for the account *synchronously*
     *
     * @param accountId String Id of the account
     * @return APIResponse
     */
    public APIResponse getStatus(String accountId) {
        return getStatus(accountId, false, false, false, null, null, tootPerPage);
    }

    /**
     * Retrieves status for the account *synchronously*
     *
     * @param accountId String Id of the account
     * @param max_id    String id max
     * @return APIResponse
     */
    public APIResponse getStatus(String accountId, String max_id) {
        return getStatus(accountId, false, false, false, max_id, null, tootPerPage);
    }

    /**
     * Retrieves status with media for the account *synchronously*
     *
     * @param accountId String Id of the account
     * @param max_id    String id max
     * @return APIResponse
     */
    public APIResponse getStatusWithMedia(String accountId, String max_id) {
        return getStatus(accountId, true, false, false, max_id, null, tootPerPage);
    }

    /**
     * Retrieves pinned status(es) *synchronously*
     *
     * @param accountId String Id of the account
     * @param max_id    String id max
     * @return APIResponse
     */
    public APIResponse getPinnedStatuses(String accountId, String max_id) {
        return getStatus(accountId, false, true, false, max_id, null, tootPerPage);
    }

    /**
     * Retrieves replies status(es) *synchronously*
     *
     * @param accountId String Id of the account
     * @param max_id    String id max
     * @return APIResponse
     */
    public APIResponse getAccountTLStatuses(String accountId, String max_id, boolean exclude_replies) {
        return getStatus(accountId, false, false, exclude_replies, max_id, null, tootPerPage);
    }

    /**
     * Retrieves status for the account *synchronously*
     *
     * @param accountId       String Id of the account
     * @param onlyMedia       boolean only with media
     * @param exclude_replies boolean excludes replies
     * @param max_id          String id max
     * @param since_id        String since the id
     * @param limit           int limit  - max value 40
     * @return APIResponse
     */
    @SuppressWarnings("SameParameterValue")
    private APIResponse getStatus(String accountId, boolean onlyMedia, boolean pinned,
                                  boolean exclude_replies, String max_id, String since_id, int limit) {

        HashMap<String, String> params = new HashMap<>();
        if (max_id != null)
            params.put("max_id", max_id);
        if (since_id != null)
            params.put("since_id", since_id);
        if (0 < limit || limit > 40)
            limit = 40;
        if( onlyMedia)
            params.put("only_media", Boolean.toString(true));
        if( pinned)
            params.put("pinned", Boolean.toString(true));
        params.put("exclude_replies", Boolean.toString(exclude_replies));
        params.put("limit", String.valueOf(limit));
        statuses = new ArrayList<>();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.get(getAbsoluteUrl(String.format("/accounts/%s/statuses", accountId)), 60, params, prefKeyOauthTokenT);
            statuses = parseStatuses(new JSONArray(response));
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
        apiResponse.setStatuses(statuses);
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
     * Retrieves home timeline for the account *synchronously*
     * @param max_id   String id max
     * @return APIResponse
     */
    public APIResponse getHomeTimeline( String max_id) {
        return getHomeTimeline(max_id, null, tootPerPage);
    }


    /**
     * Retrieves home timeline for the account since an Id value *synchronously*
     * @return APIResponse
     */
    public APIResponse getHomeTimelineSinceId(String since_id) {
        return getHomeTimeline(null, since_id, tootPerPage);
    }


    /**
     * Retrieves home timeline for the account *synchronously*
     * @param max_id   String id max
     * @param since_id String since the id
     * @param limit    int limit  - max value 40
     * @return APIResponse
     */
    private APIResponse getHomeTimeline(String max_id, String since_id, int limit) {

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
            String response = httpsConnection.get(getAbsoluteUrl("/timelines/home"), 60, params, prefKeyOauthTokenT);
            apiResponse.setSince_id(httpsConnection.getSince_id());
            apiResponse.setMax_id(httpsConnection.getMax_id());
            statuses = parseStatuses(new JSONArray(response));
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
     * Retrieves public timeline for the account *synchronously*
     * @param local boolean only local timeline
     * @param max_id String id max
     * @return APIResponse
     */
    public APIResponse getPublicTimeline(String instanceName, boolean local, String max_id){
        return getPublicTimeline(local, instanceName, max_id, null, tootPerPage);
    }

    /**
     * Retrieves public timeline for the account *synchronously*
     * @param local boolean only local timeline
     * @param max_id String id max
     * @return APIResponse
     */
    public APIResponse getPublicTimeline(boolean local, String max_id){
        return getPublicTimeline(local, null, max_id, null, tootPerPage);
    }

    /**
     * Retrieves public timeline for the account since an Id value *synchronously*
     * @param local boolean only local timeline
     * @param since_id String id since
     * @return APIResponse
     */
    public APIResponse getPublicTimelineSinceId(boolean local, String since_id) {
        return getPublicTimeline(local, null, null, since_id, tootPerPage);
    }

    /**
     * Retrieves instance timeline since an Id value *synchronously*
     * @param instanceName String instance name
     * @param since_id String id since
     * @return APIResponse
     */
    public APIResponse getInstanceTimelineSinceId(String instanceName, String since_id) {
        return getPublicTimeline(true, instanceName, null, since_id, tootPerPage);
    }

    /**
     * Retrieves public timeline for the account *synchronously*
     * @param local boolean only local timeline
     * @param max_id String id max
     * @param since_id String since the id
     * @param limit int limit  - max value 40
     * @return APIResponse
     */
    private APIResponse getPublicTimeline(boolean local, String instanceName, String max_id, String since_id, int limit){

        HashMap<String, String> params = new HashMap<>();
        if( local)
            params.put("local", Boolean.toString(true));
        if( max_id != null )
            params.put("max_id", max_id);
        if( since_id != null )
            params.put("since_id", since_id);
        if( 0 > limit || limit > 40)
            limit = 40;
        params.put("limit",String.valueOf(limit));
        statuses = new ArrayList<>();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String url;
            if( instanceName == null)
                url = getAbsoluteUrl("/timelines/public");
            else
                url = getAbsoluteUrlRemoteInstance(instanceName);
            String response = httpsConnection.get(url, 60, params, prefKeyOauthTokenT);
            apiResponse.setSince_id(httpsConnection.getSince_id());
            apiResponse.setMax_id(httpsConnection.getMax_id());
            statuses = parseStatuses(new JSONArray(response));
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
     * Retrieves public tag timeline *synchronously*
     * @param tag String
     * @param local boolean only local timeline
     * @param max_id String id max
     * @return APIResponse
     */
    @SuppressWarnings("SameParameterValue")
    public APIResponse getPublicTimelineTag(String tag, boolean local, String max_id){
        return getPublicTimelineTag(tag, local, max_id, null, tootPerPage);
    }
    /**
     * Retrieves public tag timeline *synchronously*
     * @param tag String
     * @param local boolean only local timeline
     * @param max_id String id max
     * @param since_id String since the id
     * @param limit int limit  - max value 40
     * @return APIResponse
     */
    @SuppressWarnings("SameParameterValue")
    private APIResponse getPublicTimelineTag(String tag, boolean local, String max_id, String since_id, int limit){

        HashMap<String, String> params = new HashMap<>();
        if( local)
            params.put("local", Boolean.toString(true));
        if( max_id != null )
            params.put("max_id", max_id);
        if( since_id != null )
            params.put("since_id", since_id);
        if( 0 > limit || limit > 40)
            limit = 40;
        params.put("limit",String.valueOf(limit));
        statuses = new ArrayList<>();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.get(getAbsoluteUrl(String.format("/timelines/tag/%s",tag.trim())), 60, params, prefKeyOauthTokenT);
            apiResponse.setSince_id(httpsConnection.getSince_id());
            apiResponse.setMax_id(httpsConnection.getMax_id());
            statuses = parseStatuses(new JSONArray(response));
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
     * Retrieves muted users by the authenticated account *synchronously*
     * @param max_id String id max
     * @return APIResponse
     */
    public APIResponse getMuted(String max_id){
        return getAccounts("/mutes", max_id, null, accountPerPage);
    }

    /**
     * Retrieves blocked users by the authenticated account *synchronously*
     * @param max_id String id max
     * @return APIResponse
     */
    public APIResponse getBlocks(String max_id){
        return getAccounts("/blocks", max_id, null, accountPerPage);
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
     * Retrieves blocked domains for the authenticated account *synchronously*
     * @param max_id String id max
     * @return APIResponse
     */
    @SuppressWarnings("SameParameterValue")
    public APIResponse getBlockedDomain(String max_id){

        HashMap<String, String> params = new HashMap<>();
        if( max_id != null )
            params.put("max_id", max_id);
        params.put("limit","80");
        domains = new ArrayList<>();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.get(getAbsoluteUrl("/domain_blocks"), 60, params, prefKeyOauthTokenT);
            apiResponse.setSince_id(httpsConnection.getSince_id());
            apiResponse.setMax_id(httpsConnection.getMax_id());
            domains = parseDomains(new JSONArray(response));
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
        apiResponse.setDomains(domains);
        return apiResponse;
    }


    /**
     * Delete a blocked domains for the authenticated account *synchronously*
     * @param domain String domain name
     */
    @SuppressWarnings("SameParameterValue")
    public int deleteBlockedDomain(String domain){

        HashMap<String, String> params = new HashMap<>();
        params.put("domain",domain);
        domains = new ArrayList<>();
        HttpsConnection httpsConnection;
        try {
            httpsConnection = new HttpsConnection(context);
            httpsConnection.delete(getAbsoluteUrl("/domain_blocks"), 60, params, prefKeyOauthTokenT);
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
     * Retrieves follow requests for the authenticated account *synchronously*
     * @param max_id String id max
     * @return APIResponse
     */
    public APIResponse getFollowRequest(String max_id){
        return getFollowRequest(max_id, null, accountPerPage);
    }
    /**
     * Retrieves follow requests for the authenticated account *synchronously*
     * @param max_id String id max
     * @param since_id String since the id
     * @param limit int limit  - max value 40
     * @return APIResponse
     */
    @SuppressWarnings("SameParameterValue")
    private APIResponse getFollowRequest(String max_id, String since_id, int limit){

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
            String response = httpsConnection.get(getAbsoluteUrl("/follow_requests"), 60, params, prefKeyOauthTokenT);
            apiResponse.setSince_id(httpsConnection.getSince_id());
            apiResponse.setMax_id(httpsConnection.getMax_id());
            accounts = parseAccountResponse(new JSONArray(response));
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
     * Retrieves favourited status for the authenticated account *synchronously*
     * @param max_id String id max
     * @return APIResponse
     */
    public APIResponse getFavourites(String max_id){
        return getFavourites(max_id, null, tootPerPage);
    }
    /**
     * Retrieves favourited status for the authenticated account *synchronously*
     * @param max_id String id max
     * @param since_id String since the id
     * @param limit int limit  - max value 40
     * @return APIResponse
     */
    @SuppressWarnings("SameParameterValue")
    private APIResponse getFavourites(String max_id, String since_id, int limit){

        HashMap<String, String> params = new HashMap<>();
        if( max_id != null )
            params.put("max_id", max_id);
        if( since_id != null )
            params.put("since_id", since_id);
        if( 0 > limit || limit > 40)
            limit = 40;
        params.put("limit",String.valueOf(limit));
        statuses = new ArrayList<>();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.get(getAbsoluteUrl("/favourites"), 60, params, prefKeyOauthTokenT);
            apiResponse.setSince_id(httpsConnection.getSince_id());
            apiResponse.setMax_id(httpsConnection.getMax_id());
            statuses = parseStatuses(new JSONArray(response));
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
     * Makes the post action for a status
     * @param statusAction Enum
     * @param targetedId String id of the targeted Id *can be this of a status or an account*
     * @return in status code - Should be equal to 200 when action is done
     */
    public int postAction(StatusAction statusAction, String targetedId){
        return postAction(statusAction, targetedId, null, null);
    }

    /**
     * Makes the post action for a status
     * @param targetedId String id of the targeted Id *can be this of a status or an account*
     * @param muteNotifications - boolean - notifications should be also muted
     * @return in status code - Should be equal to 200 when action is done
     */
    public int muteNotifications(String targetedId, boolean muteNotifications){

        HashMap<String, String> params = new HashMap<>();
        params.put("notifications", Boolean.toString(muteNotifications));
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            httpsConnection.post(getAbsoluteUrl(String.format("/accounts/%s/mute", targetedId)), 60, params, prefKeyOauthTokenT);
            actionCode = httpsConnection.getActionCode();
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
            e.printStackTrace();
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
     * Makes the post action
     * @param status Status object related to the status
     * @param comment String comment for the report
     * @return in status code - Should be equal to 200 when action is done
     */
    public int reportAction(Status status, String comment){
        return postAction(API.StatusAction.REPORT, null, status, comment);
    }

    public int statusAction(Status status){
        return postAction(StatusAction.CREATESTATUS, null, status, null);
    }

    /**
     * Makes the post action
     * @param statusAction Enum
     * @param targetedId String id of the targeted Id *can be this of a status or an account*
     * @param status Status object related to the status
     * @param comment String comment for the report
     * @return in status code - Should be equal to 200 when action is done
     */
    private int postAction(StatusAction statusAction, String targetedId, Status status, String comment ){

        String action;
        HashMap<String, String> params = null;
        switch (statusAction){
            case FAVOURITE:
                action = String.format("/statuses/%s/favourite", targetedId);
                break;
            case UNFAVOURITE:
                action = String.format("/statuses/%s/unfavourite", targetedId);
                break;
            case REBLOG:
                action = String.format("/statuses/%s/reblog", targetedId);
                break;
            case UNREBLOG:
                action = String.format("/statuses/%s/unreblog", targetedId);
                break;
            case FOLLOW:
                action = String.format("/accounts/%s/follow", targetedId);
                break;
            case REMOTE_FOLLOW:
                action = "/follows";
                params = new HashMap<>();
                params.put("uri", targetedId);
                break;
            case UNFOLLOW:
                action = String.format("/accounts/%s/unfollow", targetedId);
                break;
            case BLOCK:
                action = String.format("/accounts/%s/block", targetedId);
                break;
            case BLOCK_DOMAIN:
                action = String.format("/domain_blocks/%s", targetedId);
                break;
            case UNBLOCK:
                action = String.format("/accounts/%s/unblock", targetedId);
                break;
            case MUTE:
                action = String.format("/accounts/%s/mute", targetedId);
                break;
            case UNMUTE:
                action = String.format("/accounts/%s/unmute", targetedId);
                break;
            case PIN:
                action = String.format("/statuses/%s/pin", targetedId);
                break;
            case UNPIN:
                action = String.format("/statuses/%s/unpin", targetedId);
                break;
            case ENDORSE:
                action = String.format("/accounts/%s/pin", targetedId);
                break;
            case UNENDORSE:
                action = String.format("/accounts/%s/unpin", targetedId);
                break;
            case SHOW_BOOST:
                params = new HashMap<>();
                params.put("reblogs","true");
                action = String.format("/accounts/%s/follow", targetedId);
                break;
            case HIDE_BOOST:
                params = new HashMap<>();
                params.put("reblogs","false");
                action = String.format("/accounts/%s/follow", targetedId);
                break;
            case UNSTATUS:
                action = String.format("/statuses/%s", targetedId);
                break;
            case AUTHORIZE:
                action = String.format("/follow_requests/%s/authorize", targetedId);
                break;
            case REJECT:
                action = String.format("/follow_requests/%s/reject", targetedId);
                break;
            case REPORT:
                action = "/reports";
                params = new HashMap<>();
                params.put("account_id", status.getAccount().getId());
                params.put("comment", comment);
                params.put("status_ids[]", status.getId());
                break;
            case CREATESTATUS:
                params = new HashMap<>();
                action = "/statuses";
                try {
                    params.put("status", URLEncoder.encode(status.getContent(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    params.put("status", status.getContent());
                }
                if( status.getIn_reply_to_id() != null)
                    params.put("in_reply_to_id", status.getIn_reply_to_id());
                if( status.getMedia_attachments() != null && status.getMedia_attachments().size() > 0 ) {
                    StringBuilder parameters = new StringBuilder();
                    for(Attachment attachment: status.getMedia_attachments())
                        parameters.append("media_ids[]=").append(attachment.getId()).append("&");
                    parameters = new StringBuilder(parameters.substring(0, parameters.length() - 1).substring(12));
                    params.put("media_ids[]", parameters.toString());
                }
                if( status.isSensitive())
                    params.put("sensitive", Boolean.toString(status.isSensitive()));
                if( status.getSpoiler_text() != null)
                    try {
                        params.put("spoiler_text", URLEncoder.encode(status.getSpoiler_text(), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        params.put("spoiler_text", status.getSpoiler_text());
                    }
                params.put("visibility", status.getVisibility());
                break;
            default:
                return -1;
        }
        if(statusAction != StatusAction.UNSTATUS ) {

            try {
                HttpsConnection httpsConnection = new HttpsConnection(context);
                httpsConnection.post(getAbsoluteUrl(action), 60, params, prefKeyOauthTokenT);
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
        }else{
            try {
                HttpsConnection httpsConnection = new HttpsConnection(context);
                httpsConnection.delete(getAbsoluteUrl(action), 60, null, prefKeyOauthTokenT);
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
        }
        return actionCode;
    }


    /**
     * Posts a status
     * @param status Status object related to the status
     * @return APIResponse
     */
    public APIResponse postStatusAction(Status status){

        HashMap<String, String> params = new HashMap<>();
        try {
            params.put("status", URLEncoder.encode(status.getContent(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            params.put("status", status.getContent());
        }
        if( status.getIn_reply_to_id() != null)
            params.put("in_reply_to_id", status.getIn_reply_to_id());
        if( status.getMedia_attachments() != null && status.getMedia_attachments().size() > 0 ) {
            StringBuilder parameters = new StringBuilder();
            for(Attachment attachment: status.getMedia_attachments())
                parameters.append("media_ids[]=").append(attachment.getId()).append("&");
            parameters = new StringBuilder(parameters.substring(0, parameters.length() - 1).substring(12));
            params.put("media_ids[]", parameters.toString());
        }
        if( status.isSensitive())
            params.put("sensitive", Boolean.toString(status.isSensitive()));
        if( status.getSpoiler_text() != null)
            try {
                params.put("spoiler_text", URLEncoder.encode(status.getSpoiler_text(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                params.put("spoiler_text", status.getSpoiler_text());
            }
        params.put("visibility", status.getVisibility());
        statuses = new ArrayList<>();

        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.post(getAbsoluteUrl("/statuses"), 60, params, prefKeyOauthTokenT);
            apiResponse.setSince_id(httpsConnection.getSince_id());
            apiResponse.setMax_id(httpsConnection.getMax_id());
            Status statusreturned = parseStatuses(context, new JSONObject(response));
            statuses.add(statusreturned);
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
     * Posts a status
     * @param notificationId String, the current notification id, if null all notifications are deleted
     * @return APIResponse
     */
    public APIResponse postNoticationAction(String notificationId){

        String action;
        HashMap<String, String> params = new HashMap<>();
        if( notificationId == null)
            action = "/notifications/clear";
        else {
            params.put("id",notificationId);
            action = "/notifications/dismiss";
        }
        try {
            new HttpsConnection(context).post(getAbsoluteUrl(action), 60, params, prefKeyOauthTokenT);
        } catch (HttpsConnection.HttpsConnectionException e) {
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


    /**
     * Retrieves notifications for the authenticated account since an id*synchronously*
     * @param since_id String since max
     * @return APIResponse
     */
    public APIResponse getNotificationsSince(String since_id, boolean display){
        return getNotifications(null, since_id, notificationPerPage, display);
    }

    /**
     * Retrieves notifications for the authenticated account since an id*synchronously*
     * @param since_id String since max
     * @return APIResponse
     */
    @SuppressWarnings("SameParameterValue")
    public APIResponse getNotificationsSince(String since_id, int notificationPerPage, boolean display){
        return getNotifications(null, since_id, notificationPerPage, display);
    }

    /**
     * Retrieves notifications for the authenticated account *synchronously*
     * @param max_id String id max
     * @return APIResponse
     */
    public APIResponse getNotifications(String max_id, boolean display){
        return getNotifications(max_id, null, notificationPerPage, display);
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
        params.put("q", query);
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

        List<fr.gouv.etalab.mastodon.client.Entities.Filters> filters = new ArrayList<>();
        fr.gouv.etalab.mastodon.client.Entities.Filters filter;
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
            statuses = parseStatuses(new JSONArray(response));
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
     * Add an account in a list
     * @param id String, id of the list
     * @param account_ids String, account to add
     * @return APIResponse
     */
    //TODO: it is unclear what is returned here
    //TODO: improves doc https://github.com/tootsuite/documentation/blob/4bb149c73f40fa58fd7265a336703dd2d83efb1c/Using-the-API/API.md#addingremoving-accounts-tofrom-a-list
    public APIResponse addAccountToList(String id, String[] account_ids){

        HashMap<String, String> params = new HashMap<>();
        StringBuilder parameters = new StringBuilder();
        for(String val: account_ids)
            parameters.append("account_ids[]=").append(val).append("&");
        if( parameters.length() > 0) {
            parameters = new StringBuilder(parameters.substring(0, parameters.length() - 1).substring(14));
            params.put("account_ids[]", parameters.toString());
        }
        try {
            new HttpsConnection(context).post(getAbsoluteUrl(String.format("/lists/%s/accounts", id)), 60, params, prefKeyOauthTokenT);
        } catch (HttpsConnection.HttpsConnectionException e) {
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


    /**
     * Delete an account from a list
     * @param id String, the id of the list
     * @return APIResponse
     */
    public int deleteAccountFromList(String id, String[] account_ids){
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            StringBuilder parameters = new StringBuilder();
            HashMap<String, String> params = new HashMap<>();
            for(String val: account_ids)
                parameters.append("account_ids[]=").append(val).append("&");
            if( parameters.length() > 0) {
                parameters = new StringBuilder(parameters.substring(0, parameters.length() - 1).substring(14));
                params.put("account_ids[]", parameters.toString());
            }
            httpsConnection.delete(getAbsoluteUrl(String.format("/lists/%s/accounts", id)), 60, params, prefKeyOauthTokenT);
            actionCode = httpsConnection.getActionCode();
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
            e.printStackTrace();
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
     * Posts a list
     * @param title String, the title of the list
     * @return APIResponse
     */
    public APIResponse createList(String title){

        HashMap<String, String> params = new HashMap<>();
        params.put("title",title);
        List<fr.gouv.etalab.mastodon.client.Entities.List> lists = new ArrayList<>();
        fr.gouv.etalab.mastodon.client.Entities.List list;
        try {
            String response = new HttpsConnection(context).post(getAbsoluteUrl("/lists"), 60, params, prefKeyOauthTokenT);
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
     * Get card
     * @param statusId String, the id of the status
     * @return Card, the card (null if none)
     */
    public Card getCard(String statusId){

        Card card = null;
        try {
            String response = new HttpsConnection(context).get(getAbsoluteUrl(String.format("/statuses/%s/card", statusId)), 60, null, prefKeyOauthTokenT);
            card = parseCardResponse(new JSONObject(response));
        }catch (Exception ignored) {ignored.printStackTrace();}
        return card;
    }

    /**
     * Update a list by its id
     * @param id String, the id of the list
     * @param title String, the title of the list
     * @return APIResponse
     */
    public APIResponse updateList(String id, String title){

        HashMap<String, String> params = new HashMap<>();
        params.put("title",title);
        List<fr.gouv.etalab.mastodon.client.Entities.List> lists = new ArrayList<>();
        fr.gouv.etalab.mastodon.client.Entities.List list;
        try {
            String response = new HttpsConnection(context).put(getAbsoluteUrl(String.format("/lists/%s", id)), 60, params, prefKeyOauthTokenT);
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
     * Delete a list by its id
     * @param id String, the id of the list
     * @return APIResponse
     */
    public int deleteList(String id){
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            httpsConnection.delete(getAbsoluteUrl(String.format("/lists/%s", id)), 60, null, prefKeyOauthTokenT);
            actionCode = httpsConnection.getActionCode();
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
        }catch (Exception e) {
            setDefaultError(e);
        }
        return actionCode;
    }



    /**
     * Retrieves list from Communitywiki *synchronously*
     * @return APIResponse
     */
    public ArrayList<String> getCommunitywikiList() {
        ArrayList<String> list = new ArrayList<>();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.get(getAbsoluteUrlCommunitywiki("/list"), 60, null, prefKeyOauthTokenT);

            JSONArray jsonArray = new JSONArray(response);
            int len = jsonArray.length();
            for (int i=0;i<len;i++){
                list.add(jsonArray.get(i).toString());
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
        apiResponse.setStatuses(statuses);
        return list;
    }

    /**
     * Retrieves list from Communitywiki *synchronously*
     * @return APIResponse
     */
    public ArrayList<String> getCommunitywikiList(String name) {
        ArrayList<String> list = new ArrayList<>();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.get(getAbsoluteUrlCommunitywiki(String.format("/list/%s", name)), 60, null, prefKeyOauthTokenT);

            JSONArray jsonArray = new JSONArray(response);
            for(int i = 0; i < jsonArray.length(); i++){
                try {
                    list.add(jsonArray.getJSONObject(i).getString("acct"));
                } catch (JSONException ignored) {}
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
        apiResponse.setStatuses(statuses);
        return list;
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
            results.setStatuses(parseStatuses(resobj.getJSONArray("statuses")));
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
    private Card parseCardResponse(JSONObject resobj){

        Card card = new Card();
        try {
            card.setUrl(resobj.get("url").toString());
            card.setTitle(resobj.get("title").toString());
            card.setDescription(resobj.get("description").toString());
            card.setImage(resobj.get("image").toString());
            card.setHtml(resobj.get("html").toString());
            card.setType(resobj.get("type").toString());
        } catch (JSONException e) {
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
     * Parse json response for several status
     * @param jsonArray JSONArray
     * @return List<Status>
     */
    private List<Status> parseStatuses(JSONArray jsonArray){

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
            setDefaultError(e);
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
            status.setVisibility(resobj.get("visibility").toString());
            status.setLanguage(resobj.get("language").toString());
            status.setUrl(resobj.get("url").toString());
            //TODO: replace by the value
            status.setApplication(new Application());

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
        Filters filter = new fr.gouv.etalab.mastodon.client.Entities.Filters();
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
                HashMap<String, String> fieldsMap = new HashMap<>();
                if( fields != null){
                    for(int j = 0 ; j < fields.length() ; j++){
                        fieldsMap.put(fields.getJSONObject(j).getString("name"),fields.getJSONObject(j).getString("value"));
                    }
                }
                account.setFields(fieldsMap);
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
            context.setAncestors(parseStatuses(jsonObject.getJSONArray("ancestors")));
            context.setDescendants(parseStatuses(jsonObject.getJSONArray("descendants")));
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
            e.printStackTrace();
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


    private String getAbsoluteUrlRemoteInstance(String instanceName) {
        return "https://" + instanceName + "/api/v1/timelines/public?local=true";
    }

    private String getAbsoluteUrlCommunitywiki(String action) {
        return "https://communitywiki.org/trunk/api/v1"  + action;
    }
}
