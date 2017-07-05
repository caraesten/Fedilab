package fr.gouv.etalab.mastodon.client;
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

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.lang.*;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;
import fr.gouv.etalab.mastodon.client.Entities.*;
import fr.gouv.etalab.mastodon.client.Entities.Error;
import fr.gouv.etalab.mastodon.helper.Helper;
import mastodon.etalab.gouv.fr.mastodon.R;

import static fr.gouv.etalab.mastodon.helper.Helper.USER_AGENT;


/**
 * Created by Thomas on 23/04/2017.
 * Manage Calls to the REST API
 */

public class API {




    private SyncHttpClient client = new SyncHttpClient();

    private Account account;
    private Context context;
    private Relationship relationship;
    private Results results;
    private fr.gouv.etalab.mastodon.client.Entities.Context statusContext;
    private Attachment attachment;
    private List<Account> accounts;
    private List<Status> statuses;
    private List<Notification> notifications;
    private int tootPerPage, accountPerPage, notificationPerPage;
    private int actionCode;
    private String instance;
    private Instance instanceEntity;
    private String prefKeyOauthTokenT;
    private APIResponse apiResponse;
    private Error APIError;

    public enum StatusAction{
        FAVOURITE,
        UNFAVOURITE,
        REBLOG,
        UNREBLOG,
        MUTE,
        UNMUTE,
        BLOCK,
        UNBLOCK,
        FOLLOW,
        UNFOLLOW,
        CREATESTATUS,
        UNSTATUS,
        AUTHORIZE,
        REJECT,
        REPORT
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
        get("/instance", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                instanceEntity = parseInstance(response);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject response){
                setError(statusCode, error);
            }
        });
        apiResponse.setInstance(instanceEntity);
        return apiResponse;
    }

    /***
     * Update credential of the authenticated user *synchronously*
     * @return APIResponse
     */
    public APIResponse updateCredential(String display_name, String note, String avatar, String header) {
        RequestParams requestParams = new RequestParams();

        if( display_name != null)
            requestParams.add("display_name",display_name);
        if( note != null)
            requestParams.add("note",note);
        if( avatar != null)
            requestParams.add("avatar",avatar);
        if( header != null)
            requestParams.add("header",header);
        patch("/accounts/update_credentials", requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject response){
                setError(statusCode, error);
            }
        });
        return apiResponse;
    }


    /***
     * Verifiy credential of the authenticated user *synchronously*
     * @return Account
     */
    public Account verifyCredentials() {
        account = new Account();
        get("/accounts/verify_credentials", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                account = parseAccountResponse(response);
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                try {
                    account = parseAccountResponse(response.getJSONObject(0));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject response){
                setError(statusCode, error);
            }
        });
        return account;
    }

    /**
     * Returns an account
     * @param accountId String account fetched
     * @return Account entity
     */
    public Account getAccount(String accountId) {

        account = new Account();
        get(String.format("/accounts/%s",accountId), null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                account = parseAccountResponse(response);
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                try {
                    account = parseAccountResponse(response.getJSONObject(0));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject response){
                setError(statusCode, error);
            }
        });
        return account;
    }


    /**
     * Returns a relationship between the authenticated account and an account
     * @param accountId String account fetched
     * @return Relationship entity
     */
    public Relationship getRelationship(String accountId) {

        relationship = new Relationship();
        RequestParams params = new RequestParams();
        params.put("id",accountId);
        get("/accounts/relationships", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                relationship = parseRelationshipResponse(response);
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                try {
                    relationship = parseRelationshipResponse(response.getJSONObject(0));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject response){
                setError(statusCode, error);
            }
        });
        return relationship;
    }

    /**
     * Retrieves status for the account *synchronously*
     *
     * @param accountId String Id of the account
     * @return APIResponse
     */
    public APIResponse getStatus(String accountId) {
        return getStatus(accountId, false, false, null, null, tootPerPage);
    }

    /**
     * Retrieves status for the account *synchronously*
     *
     * @param accountId String Id of the account
     * @param max_id    String id max
     * @return APIResponse
     */
    public APIResponse getStatus(String accountId, String max_id) {
        return getStatus(accountId, false, false, max_id, null, tootPerPage);
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
    private APIResponse getStatus(String accountId, boolean onlyMedia,
                                  boolean exclude_replies, String max_id, String since_id, int limit) {

        RequestParams params = new RequestParams();
        if( exclude_replies)
            params.put("exclude_replies", Boolean.toString(true));
        if (max_id != null)
            params.put("max_id", max_id);
        if (since_id != null)
            params.put("since_id", since_id);
        if (0 < limit || limit > 40)
            limit = 40;
        if( onlyMedia)
            params.put("only_media", Boolean.toString(true));
        params.put("limit", String.valueOf(limit));
        statuses = new ArrayList<>();
        get(String.format("/accounts/%s/statuses", accountId), params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Status status = parseStatuses(response);
                statuses.add(status);
                apiResponse.setSince_id(findSinceId(headers));
                apiResponse.setMax_id(findMaxId(headers));
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                apiResponse.setSince_id(findSinceId(headers));
                apiResponse.setMax_id(findMaxId(headers));
                statuses = parseStatuses(response);

            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject response){
                setError(statusCode, error);
            }
        });
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
        get(String.format("/statuses/%s", statusId), null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                apiResponse.setSince_id(findSinceId(headers));
                apiResponse.setMax_id(findMaxId(headers));
                Status status = parseStatuses(response);
                statuses.add(status);
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                apiResponse.setSince_id(findSinceId(headers));
                apiResponse.setMax_id(findMaxId(headers));
                statuses = parseStatuses(response);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject response){
                setError(statusCode, error);
            }
        });
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
        statusContext = new fr.gouv.etalab.mastodon.client.Entities.Context();
        get(String.format("/statuses/%s/context", statusId), null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                statusContext = parseContext(response);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject response){
                setError(statusCode, error);
            }
        });
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

        RequestParams params = new RequestParams();
        if (max_id != null)
            params.put("max_id", max_id);
        if (since_id != null)
            params.put("since_id", since_id);
        if (0 > limit || limit > 40)
            limit = 40;
        params.put("limit",String.valueOf(limit));
        statuses = new ArrayList<>();
        get("/timelines/home", params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Status status = parseStatuses(response);
                statuses.add(status);
                apiResponse.setSince_id(findSinceId(headers));
                apiResponse.setMax_id(findMaxId(headers));
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                statuses = parseStatuses(response);
                apiResponse.setSince_id(findSinceId(headers));
                apiResponse.setMax_id(findMaxId(headers));
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject response){
                setError(statusCode, error);
            }
        });
        apiResponse.setStatuses(statuses);
        return apiResponse;
    }


    /**
     * Retrieves public timeline for the account *synchronously*
     * @param local boolean only local timeline
     * @param max_id String id max
     * @return APIResponse
     */
    public APIResponse getPublicTimeline(boolean local, String max_id){
        return getPublicTimeline(local, max_id, null, tootPerPage);
    }
    /**
     * Retrieves public timeline for the account *synchronously*
     * @param local boolean only local timeline
     * @param max_id String id max
     * @param since_id String since the id
     * @param limit int limit  - max value 40
     * @return APIResponse
     */
    private APIResponse getPublicTimeline(boolean local, String max_id, String since_id, int limit){

        RequestParams params = new RequestParams();
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
        get("/timelines/public", params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Status status = parseStatuses(response);
                statuses.add(status);
                apiResponse.setSince_id(findSinceId(headers));
                apiResponse.setMax_id(findMaxId(headers));
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                statuses = parseStatuses(response);
                apiResponse.setSince_id(findSinceId(headers));
                apiResponse.setMax_id(findMaxId(headers));
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject response){
                setError(statusCode, error);
            }
        });
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
    private APIResponse getPublicTimelineTag(String tag, boolean local, String max_id, String since_id, int limit){

        RequestParams params = new RequestParams();
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

        get(String.format("/timelines/tag/%s",tag.trim()), params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Status status = parseStatuses(response);
                statuses.add(status);
                apiResponse.setSince_id(findSinceId(headers));
                apiResponse.setMax_id(findMaxId(headers));
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                statuses = parseStatuses(response);
                apiResponse.setSince_id(findSinceId(headers));
                apiResponse.setMax_id(findMaxId(headers));
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject response){
                setError(statusCode, error);
            }
        });
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
    private APIResponse getAccounts(String action, String max_id, String since_id, int limit){

        RequestParams params = new RequestParams();
        if( max_id != null )
            params.put("max_id", max_id);
        if( since_id != null )
            params.put("since_id", since_id);
        if( 0 > limit || limit > 40)
            limit = 40;
        params.put("limit",String.valueOf(limit));
        accounts = new ArrayList<>();
        get(action, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                apiResponse.setSince_id(findSinceId(headers));
                apiResponse.setMax_id(findMaxId(headers));
                Account account = parseAccountResponse(response);
                accounts.add(account);
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                apiResponse.setSince_id(findSinceId(headers));
                apiResponse.setMax_id(findMaxId(headers));
                accounts = parseAccountResponse(response);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject response){
                setError(statusCode, error);
            }
        });
        apiResponse.setAccounts(accounts);
        return apiResponse;
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
    private APIResponse getFollowRequest(String max_id, String since_id, int limit){

        RequestParams params = new RequestParams();
        if( max_id != null )
            params.put("max_id", max_id);
        if( since_id != null )
            params.put("since_id", since_id);
        if( 0 > limit || limit > 40)
            limit = 40;
        params.put("limit",String.valueOf(limit));
        accounts = new ArrayList<>();
        get("/follow_requests", params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                apiResponse.setSince_id(findSinceId(headers));
                apiResponse.setMax_id(findMaxId(headers));
                Account account = parseAccountResponse(response);
                accounts.add(account);
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                apiResponse.setSince_id(findSinceId(headers));
                apiResponse.setMax_id(findMaxId(headers));
                accounts = parseAccountResponse(response);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject response){
                setError(statusCode, error);
            }
        });
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
    private APIResponse getFavourites(String max_id, String since_id, int limit){

        RequestParams params = new RequestParams();
        if( max_id != null )
            params.put("max_id", max_id);
        if( since_id != null )
            params.put("since_id", since_id);
        if( 0 > limit || limit > 40)
            limit = 40;
        params.put("limit",String.valueOf(limit));
        statuses = new ArrayList<>();
        get("/favourites", params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Status status = parseStatuses(response);
                statuses.add(status);
                apiResponse.setSince_id(findSinceId(headers));
                apiResponse.setMax_id(findMaxId(headers));
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                statuses = parseStatuses(response);
                apiResponse.setSince_id(findSinceId(headers));
                apiResponse.setMax_id(findMaxId(headers));
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject response){
                setError(statusCode, error);
            }
        });
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
        RequestParams params = null;
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
            case UNFOLLOW:
                action = String.format("/accounts/%s/unfollow", targetedId);
                break;
            case BLOCK:
                action = String.format("/accounts/%s/block", targetedId);
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
                params = new RequestParams();
                params.put("account_id", status.getAccount().getId());
                params.put("comment", comment);
                params.put("status_ids[]", status.getId());
                break;
            case CREATESTATUS:
                params = new RequestParams();
                action = "/statuses";
                params.put("status", status.getContent());
                if( status.getIn_reply_to_id() != null)
                    params.put("in_reply_to_id", status.getIn_reply_to_id());
                if( status.getMedia_attachments() != null && status.getMedia_attachments().size() > 0 ) {
                    for(Attachment attachment: status.getMedia_attachments()) {
                        params.add("media_ids[]", attachment.getId());
                    }
                }
                if( status.isSensitive())
                    params.put("sensitive", Boolean.toString(status.isSensitive()));
                if( status.getSpoiler_text() != null)
                    params.put("spoiler_text", status.getSpoiler_text());
                params.put("visibility", status.getVisibility());
                break;
            default:
                return -1;
        }
        if(statusAction != StatusAction.UNSTATUS ) {
            post(action, 30000, params, new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    actionCode = statusCode;
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                    actionCode = statusCode;
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject response) {
                    actionCode = statusCode;
                    setError(statusCode, error);
                }
            });
        }else{
            delete(action, null, new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    actionCode = statusCode;
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                    actionCode = statusCode;
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject response) {
                    actionCode = statusCode;
                    setError(statusCode, error);
                }
            });
        }

        return actionCode;
    }


    /**
     * Retrieves notifications for the authenticated account since an id*synchronously*
     * @param since_id String since max
     * @return APIResponse
     */
    public APIResponse getNotificationsSince(String since_id){
        return getNotifications(null, since_id, notificationPerPage);
    }

    /**
     * Retrieves notifications for the authenticated account *synchronously*
     * @param max_id String id max
     * @return APIResponse
     */
    public APIResponse getNotifications(String max_id){
        return getNotifications(max_id, null, notificationPerPage);
    }
    /**
     * Retrieves notifications for the authenticated account *synchronously*
     * @param max_id String id max
     * @param since_id String since the id
     * @param limit int limit  - max value 40
     * @return APIResponse
     */
    private APIResponse getNotifications(String max_id, String since_id, int limit){

        RequestParams params = new RequestParams();
        if( max_id != null )
            params.put("max_id", max_id);
        if( since_id != null )
            params.put("since_id", since_id);
        if( 0 > limit || limit > 40)
            limit = 40;
        params.put("limit",String.valueOf(limit));
        notifications = new ArrayList<>();
        get("/notifications", params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Notification notification = parseNotificationResponse(response);
                notifications.add(notification);
                apiResponse.setSince_id(findSinceId(headers));
                apiResponse.setMax_id(findMaxId(headers));
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                notifications = parseNotificationResponse(response);
                apiResponse.setSince_id(findSinceId(headers));
                apiResponse.setMax_id(findMaxId(headers));
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject response){
                setError(statusCode, error);
            }
        });
        apiResponse.setNotifications(notifications);
        return apiResponse;
    }

    /**
     * Upload media
     * @param inputStream InputStream
     * @return Attachment
     */
    public Attachment uploadMedia(InputStream inputStream){

        RequestParams params = new RequestParams();
        params.put("file", inputStream);

        post("/media", 120000, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                attachment = parseAttachmentResponse(response);
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                try {
                    attachment = parseAttachmentResponse(response.getJSONObject(0));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject response){
                setError(statusCode, error);
            }
        });
        return attachment;
    }


    /**
     * Retrieves Accounts and feeds when searching *synchronously*
     *
     * @param query  String search
     * @return List<Account>
     */
    public Results search(String query) {

        RequestParams params = new RequestParams();
        params.add("q", query);
        //params.put("resolve","false");
        get("/search", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                results = parseResultsResponse(response);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject response){
                setError(statusCode, error);
            }
        });
        return results;
    }

    /**
     * Retrieves Developer account when searching (ie: via @...) *synchronously*
     *
     * @return APIResponse
     */
    public APIResponse searchDeveloper() {
        RequestParams params = new RequestParams();
        params.add("q", "tschneider");
        get("/accounts/search", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                accounts = new ArrayList<>();
                account = parseAccountResponse(response);
                accounts.add(account);
                apiResponse.setSince_id(findSinceId(headers));
                apiResponse.setMax_id(findMaxId(headers));
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                accounts = parseDeveloperResponse(response);
                apiResponse.setSince_id(findSinceId(headers));
                apiResponse.setMax_id(findMaxId(headers));
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject response){
                setError(statusCode, error);
            }
        });
        apiResponse.setAccounts(accounts);
        return apiResponse;
    }
    /**
     * Retrieves Accounts when searching (ie: via @...) *synchronously*
     *
     * @param query  String search
     * @return APIResponse
     */
    public APIResponse searchAccounts(String query) {

        RequestParams params = new RequestParams();
        params.add("q", query);
        //params.put("resolve","false");
        params.add("limit", "4");
        get("/accounts/search", params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                accounts = new ArrayList<>();
                account = parseAccountResponse(response);
                accounts.add(account);
                apiResponse.setSince_id(findSinceId(headers));
                apiResponse.setMax_id(findMaxId(headers));
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                accounts = parseAccountResponse(response);
                apiResponse.setSince_id(findSinceId(headers));
                apiResponse.setMax_id(findMaxId(headers));
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject response){
                setError(statusCode, error);
            }
        });
        apiResponse.setAccounts(accounts);
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
            results.setStatuses(parseStatuses(resobj.getJSONArray("statuses")));
            results.setHashtags(parseTags(resobj.getJSONArray("hashtags")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return results;
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
                Status status = parseStatuses(resobj);
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
    private Status parseStatuses(JSONObject resobj){
        Status status = new Status();
        try {
            status.setId(resobj.get("id").toString());
            status.setCreated_at(Helper.mstStringToDate(context, resobj.get("created_at").toString()));
            status.setIn_reply_to_id(resobj.get("in_reply_to_id").toString());
            status.setIn_reply_to_account_id(resobj.get("in_reply_to_account_id").toString());
            status.setSensitive(Boolean.parseBoolean(resobj.get("sensitive").toString()));
            status.setSpoiler_text(resobj.get("spoiler_text").toString());
            status.setVisibility(resobj.get("visibility").toString());
            status.setLanguage(resobj.get("language").toString());
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

            status.setAccount(parseAccountResponse(resobj.getJSONObject("account")));
            status.setContent(resobj.get("content").toString());
            status.setFavourites_count(Integer.valueOf(resobj.get("favourites_count").toString()));
            status.setReblogs_count(Integer.valueOf(resobj.get("reblogs_count").toString()));
            status.setReblogged(Boolean.valueOf(resobj.get("reblogged").toString()));
            status.setFavourited(Boolean.valueOf(resobj.get("favourited").toString()));
            try{
                status.setReblog(parseStatuses(resobj.getJSONObject("reblog")));
            }catch (Exception ignored){}
        } catch (JSONException e) {
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
            e.printStackTrace();
        }
        return instance;
    }

    /**
     * Parse json response an unique account
     * @param resobj JSONObject
     * @return Account
     */
    private Account parseAccountResponse(JSONObject resobj){

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
            account.setUrl(resobj.get("url").toString());
            account.setAvatar(resobj.get("avatar").toString());
            account.setAvatar_static(resobj.get("avatar_static").toString());
            account.setHeader(resobj.get("header").toString());
            account.setHeader_static(resobj.get("header_static").toString());
        } catch (JSONException e) {
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
                Account account = parseAccountResponse(resobj);
                accounts.add(account);
                i++;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return accounts;
    }


    /**
     * Parse json response for list of accounts which could contain the developer name
     * @param jsonArray JSONArray
     * @return List<Account>
     */
    private List<Account> parseDeveloperResponse(JSONArray jsonArray){

        List<Account> accounts = new ArrayList<>();
        try {
            int i = 0;
            Account account = null;
            while (i < jsonArray.length() ) {
                JSONObject resobj = jsonArray.getJSONObject(i);
                account = parseAccountResponse(resobj);
                if( account.getAcct().contains(Helper.INSTANCE))
                    accounts.add(account);
                i++;
            }
            if( accounts.size() == 0)
                accounts.add(account);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return accounts;
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
            e.printStackTrace();
        }
        return context;
    }

    /**
     * Parse json response an unique attachment
     * @param resobj JSONObject
     * @return Relationship
     */
    private Attachment parseAttachmentResponse(JSONObject resobj){

        Attachment attachment = new Attachment();
        try {
            attachment.setId(resobj.get("id").toString());
            attachment.setType(resobj.get("type").toString());
            attachment.setUrl(resobj.get("url").toString());
            try{
                attachment.setRemote_url(resobj.get("remote_url").toString());
            }catch (JSONException ignore){}
            try{
                attachment.setPreview_url(resobj.get("preview_url").toString());
            }catch (JSONException ignore){}
            try{
                attachment.setText_url(resobj.get("text_url").toString());
            }catch (JSONException ignore){}

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return attachment;
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
            relationship.setRequested(Boolean.valueOf(resobj.get("requested").toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return relationship;
    }

    /**
     * Parse json response an unique notification
     * @param resobj JSONObject
     * @return Account
     */
    private Notification parseNotificationResponse(JSONObject resobj){

        Notification notification = new Notification();
        try {
            notification.setId(resobj.get("id").toString());
            notification.setType(resobj.get("type").toString());
            notification.setCreated_at(Helper.mstStringToDate(context, resobj.get("created_at").toString()));
            notification.setAccount(parseAccountResponse(resobj.getJSONObject("account")));
            try{
                notification.setStatus(parseStatuses(resobj.getJSONObject("status")));
            }catch (Exception ignored){}
            notification.setCreated_at(Helper.mstStringToDate(context, resobj.get("created_at").toString()));
        } catch (JSONException e) {
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
                Notification notification = parseNotificationResponse(resobj);
                notifications.add(notification);
                i++;
            }
        } catch (JSONException e) {
            e.printStackTrace();
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
        APIError.setError(statusCode + " - " + error.getMessage());
        apiResponse.setError(APIError);
    }

    
    private void get(String action, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        try {
            client.setConnectTimeout(10000); //10s timeout
            client.setUserAgent(USER_AGENT);
            client.addHeader("Authorization", "Bearer "+prefKeyOauthTokenT);
            MastalabSSLSocketFactory mastalabSSLSocketFactory = new MastalabSSLSocketFactory(MastalabSSLSocketFactory.getKeystore());
            mastalabSSLSocketFactory.setHostnameVerifier(MastalabSSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            client.setSSLSocketFactory(mastalabSSLSocketFactory);
            client.get(getAbsoluteUrl(action), params, responseHandler);

        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException | UnrecoverableKeyException e) {
            Toast.makeText(context, R.string.toast_error,Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void post(String action, int timeout, RequestParams params, AsyncHttpResponseHandler responseHandler) {

        try {
            client.setConnectTimeout(timeout); //10s timeout
            client.setUserAgent(USER_AGENT);
            client.addHeader("Authorization", "Bearer "+prefKeyOauthTokenT);
            MastalabSSLSocketFactory mastalabSSLSocketFactory = new MastalabSSLSocketFactory(MastalabSSLSocketFactory.getKeystore());
            mastalabSSLSocketFactory.setHostnameVerifier(MastalabSSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            client.setSSLSocketFactory(mastalabSSLSocketFactory);
            client.post(getAbsoluteUrl(action), params, responseHandler);
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException | UnrecoverableKeyException e) {
            Toast.makeText(context, R.string.toast_error,Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void delete(String action, RequestParams params, AsyncHttpResponseHandler responseHandler){
        try {
            client.setConnectTimeout(10000); //10s timeout
            client.setUserAgent(USER_AGENT);
            client.addHeader("Authorization", "Bearer "+prefKeyOauthTokenT);
            MastalabSSLSocketFactory mastalabSSLSocketFactory = new MastalabSSLSocketFactory(MastalabSSLSocketFactory.getKeystore());
            mastalabSSLSocketFactory.setHostnameVerifier(MastalabSSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            client.setSSLSocketFactory(mastalabSSLSocketFactory);
            client.delete(getAbsoluteUrl(action), params, responseHandler);
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException | UnrecoverableKeyException e) {
            Toast.makeText(context, R.string.toast_error,Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void patch(String action, RequestParams params, AsyncHttpResponseHandler responseHandler){
        try {
            client.setConnectTimeout(10000); //10s timeout
            client.setUserAgent(USER_AGENT);
            client.addHeader("Authorization", "Bearer "+prefKeyOauthTokenT);
            MastalabSSLSocketFactory mastalabSSLSocketFactory = new MastalabSSLSocketFactory(MastalabSSLSocketFactory.getKeystore());
            mastalabSSLSocketFactory.setHostnameVerifier(MastalabSSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            client.setSSLSocketFactory(mastalabSSLSocketFactory);
            client.patch(getAbsoluteUrl(action), params, responseHandler);
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException | UnrecoverableKeyException e) {
            Toast.makeText(context, R.string.toast_error,Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public Error getError(){
        return APIError;
    }


    private String getAbsoluteUrl(String action) {
        return "https://" + this.instance + "/api/v1" + action;
    }

    /**
     * Find max_id in header
     * @param headers Header[]
     * @return String max_id if presents
     */
    private String findMaxId(Header[] headers){
        if( headers == null)
            return null;
        for(Header header: headers){
            if( header.toString().startsWith("Link: ")){
                Pattern pattern = Pattern.compile("max_id=([0-9]{1,}).*");
                Matcher matcher = pattern.matcher(header.toString());
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        }
        return null;
    }

    /**
     * Find since_id in header
     * @param headers Header[]
     * @return String since_id if presents
     */
    private String findSinceId(Header[] headers){
        if( headers == null)
            return null;
        for(Header header: headers){
            if( header.toString().startsWith("Link: ")){
                Pattern pattern = Pattern.compile("since_id=([0-9]{1,}).*");
                Matcher matcher = pattern.matcher(header.toString());
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        }
        return null;
    }

}
