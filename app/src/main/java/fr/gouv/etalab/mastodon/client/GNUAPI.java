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
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

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
import java.util.HashMap;
import java.util.List;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.activities.MainActivity;
import fr.gouv.etalab.mastodon.asynctasks.UpdateAccountInfoAsyncTask;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Application;
import fr.gouv.etalab.mastodon.client.Entities.Attachment;
import fr.gouv.etalab.mastodon.client.Entities.Conversation;
import fr.gouv.etalab.mastodon.client.Entities.Error;
import fr.gouv.etalab.mastodon.client.Entities.Mention;
import fr.gouv.etalab.mastodon.client.Entities.Notification;
import fr.gouv.etalab.mastodon.client.Entities.Relationship;
import fr.gouv.etalab.mastodon.client.Entities.Results;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.fragments.DisplayNotificationsFragment;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;

import static fr.gouv.etalab.mastodon.helper.Helper.PREF_KEY_OAUTH_TOKEN;


/**
 * Created by Thomas on 02/02/2019.
 * Manage Calls to the REST API for GNU
 */

public class GNUAPI {



    private Account account;
    private Context context;
    private Attachment attachment;
    private List<Account> accounts;
    private List<Status> statuses;
    private int tootPerPage, accountPerPage, notificationPerPage;
    private int actionCode;
    private String instance;
    private String prefKeyOauthTokenT;
    private APIResponse apiResponse;
    private Error APIError;
    private String userId;

    public enum accountPrivacy {
        PUBLIC,
        LOCKED
    }
    public GNUAPI(Context context) {
        this.context = context;
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        tootPerPage = sharedpreferences.getInt(Helper.SET_TOOTS_PER_PAGE, 40);
        accountPerPage = sharedpreferences.getInt(Helper.SET_ACCOUNTS_PER_PAGE, 40);
        notificationPerPage = sharedpreferences.getInt(Helper.SET_NOTIFICATIONS_PER_PAGE, 15);
        this.prefKeyOauthTokenT = sharedpreferences.getString(PREF_KEY_OAUTH_TOKEN, null);
        userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        if( Helper.getLiveInstance(context) != null)
            this.instance = Helper.getLiveInstance(context);
        else {
            SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
            String instance = sharedpreferences.getString(Helper.PREF_INSTANCE, Helper.getLiveInstance(context));
            Account account = new AccountDAO(context, db).getUniqAccount(userId, instance);
            if( account == null) {
                APIError = new Error();
                APIError.setError(context.getString(R.string.toast_error));
                return;
            }
            this.instance = account.getInstance().trim();
        }
        apiResponse = new APIResponse();
        APIError = null;
    }

    public GNUAPI(Context context, String instance, String token) {
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
            this.prefKeyOauthTokenT = sharedpreferences.getString(PREF_KEY_OAUTH_TOKEN, null);
        apiResponse = new APIResponse();
        APIError = null;
    }




    /***
     * Update credential of the authenticated user *synchronously*
     * @return APIResponse
     */
    public APIResponse updateCredential(String display_name, String note, ByteArrayInputStream avatar, String avatarName, ByteArrayInputStream header, String headerName, accountPrivacy privacy, HashMap<String, String> customFields) {

        HashMap<String, String> requestParams = new HashMap<>();
        if( display_name != null)
            try {
                requestParams.put("name",URLEncoder.encode(display_name, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                requestParams.put("name",display_name);
            }
        if( note != null)
            try {
                requestParams.put("description",URLEncoder.encode(note, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                requestParams.put("description",note);
            }
        if( privacy != null)
            requestParams.put("locked",privacy== accountPrivacy.LOCKED?"true":"false");
        try {
            if( requestParams.size() > 0)
            new HttpsConnection(context).patch(getAbsoluteUrl("/accounts/update_profile"), 60, requestParams, avatar, null, null, null, prefKeyOauthTokenT);
            if( avatar!= null && avatarName != null)
                new HttpsConnection(context).patch(getAbsoluteUrl("/accounts/update_profile_image"), 60, null, avatar, avatarName, null, null, prefKeyOauthTokenT);

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
            String response = new HttpsConnection(context).get(getAbsoluteUrl("/account/verify_credentials.json"), 60, null, prefKeyOauthTokenT);
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
        HashMap<String, String> params = new HashMap<>();
        params.put("user_id",accountId);
        try {
            String response = new HttpsConnection(context).get(getAbsoluteUrl("/users/show.json"), 60, params, prefKeyOauthTokenT);
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
     * Returns a relationship between the authenticated account and an account
     * @param accountId String account fetched
     * @return Relationship entity
     */
    public Relationship getRelationship(String accountId) {


        Relationship relationship = null;
        HashMap<String, String> params = new HashMap<>();

        try {
            String response;
            if(MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.GNU) {
                params.put("target_id",accountId);
                response = new HttpsConnection(context).get(getAbsoluteUrl("/friendships/show.json"), 60, params, prefKeyOauthTokenT);
                relationship = parseRelationshipResponse(new JSONObject(response));
            }else if(MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA) {
                params.put("user_id",accountId);
                response = new HttpsConnection(context).get(getAbsoluteUrl("/users/show.json"), 60, params, prefKeyOauthTokenT);
                JSONObject resobj = new JSONObject(response);
                try {
                    relationship = new Relationship();
                    relationship.setId(resobj.get("id").toString());
                    relationship.setFollowing(Boolean.valueOf(resobj.get("following").toString()));
                    relationship.setFollowed_by(Boolean.valueOf(resobj.get("follow_request_sent").toString()));
                    relationship.setBlocking(Boolean.valueOf(resobj.get("statusnet_blocking").toString()));
                    try {
                        relationship.setMuting(Boolean.valueOf(resobj.get("muting").toString()));
                    }catch (Exception ignored){
                        relationship.setMuting(false);
                    }
                    try {
                        relationship.setMuting_notifications(!Boolean.valueOf(resobj.get("notifications_enabled").toString()));
                    }catch (Exception ignored){
                        relationship.setMuting_notifications(false);
                    }
                    relationship.setEndorsed(false);
                    relationship.setShowing_reblogs(true);
                    relationship.setRequested(false);
                } catch (JSONException e) {
                    setDefaultError(e);
                }
                return relationship;
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
                parameters.append("target_id[]=").append(account.getId()).append("&");
            parameters = new StringBuilder(parameters.substring(0, parameters.length() - 1).substring(12));
            params.put("target_id[]", parameters.toString());
            List<Relationship> relationships = new ArrayList<>();
            try {
                HttpsConnection httpsConnection = new HttpsConnection(context);
                String response = httpsConnection.get(getAbsoluteUrl("/friendships/show.json"), 60, params, prefKeyOauthTokenT);
                relationships = parseRelationshipResponse(new JSONArray(response));
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
            apiResponse.setRelationships(relationships);
        }

        return apiResponse;
    }

    /**
     * Retrieves status for the account *synchronously*
     *
     * @param accountId String Id of the account
     * @return APIResponse
     */
    public APIResponse getStatus(String accountId) {
        return getStatus(accountId, false, null, null, tootPerPage);
    }

    /**
     * Retrieves status for the account *synchronously*
     *
     * @param accountId String Id of the account
     * @param max_id    String id max
     * @return APIResponse
     */
    public APIResponse getStatus(String accountId, String max_id) {
        return getStatus(accountId, false, max_id, null, tootPerPage);
    }

    /**
     * Retrieves status with media for the account *synchronously*
     *
     * @param accountId String Id of the account
     * @param max_id    String id max
     * @return APIResponse
     */
    public APIResponse getStatusWithMedia(String accountId, String max_id) {
        return getStatus(accountId, false, max_id, null, tootPerPage);
    }

    /**
     * Retrieves pinned status(es) *synchronously*
     *
     * @param accountId String Id of the account
     * @param max_id    String id max
     * @return APIResponse
     */
    public APIResponse getPinnedStatuses(String accountId, String max_id) {
        return getStatus(accountId, false, max_id, null, tootPerPage);
    }

    /**
     * Retrieves replies status(es) *synchronously*
     *
     * @param accountId String Id of the account
     * @param max_id    String id max
     * @return APIResponse
     */
    public APIResponse getAccountTLStatuses(String accountId, String max_id, boolean exclude_replies) {
        return getStatus(accountId, exclude_replies, max_id, null, tootPerPage);
    }

    /**
     * Retrieves status for the account *synchronously*
     *
     * @param accountId       String Id of the account
     * @param exclude_replies boolean excludes replies
     * @param max_id          String id max
     * @param since_id        String since the id
     * @param limit           int limit  - max value 40
     * @return APIResponse
     */
    @SuppressWarnings("SameParameterValue")
    private APIResponse getStatus(String accountId,boolean exclude_replies, String max_id, String since_id, int limit) {

        HashMap<String, String> params = new HashMap<>();
        if (max_id != null)
            params.put("max_id", max_id);
        if (since_id != null)
            params.put("since_id", since_id);
        if (0 < limit || limit > 40)
            limit = 40;
        params.put("user_id", accountId);
        params.put("exclude_replies", Boolean.toString(exclude_replies));
        params.put("count", String.valueOf(limit));
        statuses = new ArrayList<>();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.get(getAbsoluteUrl("/statuses/user_timeline.json"), 60, params, prefKeyOauthTokenT);
            statuses = parseStatuses(context, new JSONArray(response));
            if( statuses.size() > 0) {
                apiResponse.setSince_id(String.valueOf(Long.parseLong(statuses.get(0).getId())+1));
                apiResponse.setMax_id(String.valueOf(Long.parseLong(statuses.get(statuses.size() - 1).getId())-1));
            }
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
    public fr.gouv.etalab.mastodon.client.Entities.Context getStatusContext(String statusId, boolean directtimeline) {
        fr.gouv.etalab.mastodon.client.Entities.Context statusContext = new fr.gouv.etalab.mastodon.client.Entities.Context();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response;
            if( !directtimeline)
                response = httpsConnection.get(getAbsoluteUrl(String.format("/statusnet/conversation/%s.json", statusId)), 60, null, prefKeyOauthTokenT);
            else {
                HashMap<String, String> params = new HashMap<>();
                params.put("uri", statusId);
                response = httpsConnection.get(getAbsoluteUrl("/direct_messages/conversation.json"), 60, params, prefKeyOauthTokenT);
            }
            statuses = parseStatuses(context, new JSONArray(response));
            if( statuses != null && statuses.size() > 0){
                ArrayList<Status> descendants = new ArrayList<>();
                statusContext.setAncestors(statuses);
                statusContext.setDescendants(descendants);
            }

        } catch (HttpsConnection.HttpsConnectionException e) {
            if(e.getStatusCode() != 404)
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
        List<Conversation> conversations = new ArrayList<>();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.get(getAbsoluteUrl("/direct_messages.json"), 60, params, prefKeyOauthTokenT);
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
        params.put("count",String.valueOf(limit));
        statuses = new ArrayList<>();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.get(getAbsoluteUrl("/direct_messages.json"), 60, params, prefKeyOauthTokenT);
            statuses = parseStatuses(context, new JSONArray(response));
            if( statuses.size() > 0) {
                apiResponse.setSince_id(String.valueOf(Long.parseLong(statuses.get(0).getId())+1));
                apiResponse.setMax_id(String.valueOf(Long.parseLong(statuses.get(statuses.size() - 1).getId())-1));
            }
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
     * Retrieves home timeline for the account *synchronously*
     * @param max_id   String id max
     * @return APIResponse
     */
    public APIResponse getHomeTimeline( String max_id) {
        return getHomeTimeline(max_id, null, null, tootPerPage);
    }


    /**
     * Retrieves home timeline for the account since an Id value *synchronously*
     * @return APIResponse
     */
    public APIResponse getHomeTimelineSinceId(String since_id) {
        return getHomeTimeline(null, since_id, null, tootPerPage);
    }

    /**
     * Retrieves home timeline for the account from a min Id value *synchronously*
     * @return APIResponse
     */
    public APIResponse getHomeTimelineMinId(String min_id) {
        return getHomeTimeline(null, null, min_id, tootPerPage);
    }


    /**
     * Retrieves home timeline for the account *synchronously*
     * @param max_id   String id max
     * @param since_id String since the id
     * @param limit    int limit  - max value 40
     * @return APIResponse
     */
    private APIResponse getHomeTimeline(String max_id, String since_id, String min_id, int limit) {

        HashMap<String, String> params = new HashMap<>();
        if (max_id != null)
            params.put("max_id", max_id);
        if (since_id != null)
            params.put("since_id", since_id);
        if (min_id != null)
            params.put("min_id", min_id);
        if (0 > limit || limit > 80)
            limit = 80;
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        //Current user
        statuses = new ArrayList<>();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.get(getAbsoluteUrl("/statuses/home_timeline.json"), 60, params, prefKeyOauthTokenT);
            statuses = parseStatuses(context, new JSONArray(response));
            if( statuses.size() > 0) {
                apiResponse.setSince_id(String.valueOf(Long.parseLong(statuses.get(0).getId())+1));
                apiResponse.setMax_id(String.valueOf(Long.parseLong(statuses.get(statuses.size() - 1).getId())-1));
            }
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
        params.put("count",String.valueOf(limit));
        statuses = new ArrayList<>();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String url;
            if(local)
                url = getAbsoluteUrl("/statuses/public_timeline.json");
            else
                url = getAbsoluteUrl("/statuses/public_and_external_timeline.json");
            String response = httpsConnection.get(url, 60, params, prefKeyOauthTokenT);
            statuses = parseStatuses(context, new JSONArray(response));
            if( statuses.size() > 0) {
                apiResponse.setSince_id(String.valueOf(Long.parseLong(statuses.get(0).getId())+1));
                apiResponse.setMax_id(String.valueOf(Long.parseLong(statuses.get(statuses.size() - 1).getId())-1));
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
        return apiResponse;
    }





    public APIResponse getCustomArtTimeline(boolean local, String tag, String max_id, List<String> any, List<String> all, List<String> none){
        return getArtTimeline(local, tag, max_id, null, any, all, none);
    }

    public APIResponse getArtTimeline(boolean local, String max_id, List<String> any, List<String> all, List<String> none){
        return getArtTimeline(local, null, max_id, null, any, all, none);
    }

    public APIResponse getCustomArtTimelineSinceId(boolean local, String tag, String since_id, List<String> any, List<String> all, List<String> none){
        return getArtTimeline(local, tag, null, since_id, any, all, none);
    }

    public APIResponse getArtTimelineSinceId(boolean local, String since_id, List<String> any, List<String> all, List<String> none){
        return getArtTimeline(local, null, null, since_id, any, all, none);
    }
    /**
     * Retrieves art timeline
     * @param local boolean only local timeline
     * @param max_id String id max
     * @return APIResponse
     */
    private APIResponse getArtTimeline(boolean local, String tag, String max_id, String since_id, List<String> any, List<String> all, List<String> none){
        if( tag == null)
            tag = "mastoart";
        APIResponse apiResponse = getPublicTimelineTag(tag, local, true, max_id, since_id, tootPerPage, any, all, none);
        APIResponse apiResponseReply = new APIResponse();
        if( apiResponse != null){
            apiResponseReply.setMax_id(apiResponse.getMax_id());
            apiResponseReply.setSince_id(apiResponse.getSince_id());
            apiResponseReply.setStatuses(new ArrayList<>());
            if( apiResponse.getStatuses() != null && apiResponse.getStatuses().size() > 0){
                for( Status status: apiResponse.getStatuses()){
                    if( status.getMedia_attachments() != null ) {
                        String statusSerialized = Helper.statusToStringStorage(status);
                        for (Attachment attachment : status.getMedia_attachments()) {
                            Status newStatus = Helper.restoreStatusFromString(statusSerialized);
                            if (newStatus == null)
                                break;
                            newStatus.setArt_attachment(attachment);
                            apiResponseReply.getStatuses().add(newStatus);
                        }
                    }
                }
            }
        }
        return apiResponseReply;
    }

    /**
     * Retrieves public tag timeline *synchronously*
     * @param tag String
     * @param local boolean only local timeline
     * @param max_id String id max
     * @return APIResponse
     */
    @SuppressWarnings("SameParameterValue")
    public APIResponse getPublicTimelineTag(String tag, boolean local, String max_id, List<String> any, List<String> all, List<String> none){
        return getPublicTimelineTag(tag, local, false, max_id, null, tootPerPage, any, all, none);
    }

    /**
     * Retrieves public tag timeline *synchronously*
     * @param tag String
     * @param local boolean only local timeline
     * @param since_id String since id
     * @return APIResponse
     */
    @SuppressWarnings("SameParameterValue")
    public APIResponse getPublicTimelineTagSinceId(String tag, boolean local, String since_id, List<String> any, List<String> all, List<String> none){
        return getPublicTimelineTag(tag, local, false, null, since_id, tootPerPage, any, all, none);
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
    private APIResponse getPublicTimelineTag(String tag, boolean local, boolean onlymedia, String max_id, String since_id, int limit, List<String> any, List<String> all, List<String> none){

        HashMap<String, String> params = new HashMap<>();
        if( local)
            params.put("local", Boolean.toString(true));
        if( max_id != null )
            params.put("max_id", max_id);
        if( since_id != null )
            params.put("since_id", since_id);
        if( 0 > limit || limit > 40)
            limit = 40;
        if( onlymedia)
            params.put("only_media", Boolean.toString(true));

        if( any != null && any.size() > 0) {
            StringBuilder parameters = new StringBuilder();
            for (String a : any)
                parameters.append("any[]=").append(a).append("&");
            parameters = new StringBuilder(parameters.substring(0, parameters.length() - 1).substring(6));
            params.put("any[]", parameters.toString());
        }
        if( all != null && all.size() > 0) {
            StringBuilder parameters = new StringBuilder();
            for (String a : all)
                parameters.append("all[]=").append(a).append("&");
            parameters = new StringBuilder(parameters.substring(0, parameters.length() - 1).substring(6));
            params.put("all[]", parameters.toString());
        }
        if( none != null && none.size() > 0) {
            StringBuilder parameters = new StringBuilder();
            for (String a : none)
                parameters.append("none[]=").append(a).append("&");
            parameters = new StringBuilder(parameters.substring(0, parameters.length() - 1).substring(7));
            params.put("none[]", parameters.toString());
        }
        params.put("limit",String.valueOf(limit));
        statuses = new ArrayList<>();
        if( tag == null)
            return null;
        try {
            String query = tag.trim();
            HttpsConnection httpsConnection = new HttpsConnection(context);
            if( MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE)
                try {
                    query = URLEncoder.encode(query, "UTF-8");
                } catch (UnsupportedEncodingException ignored) {}
            String response = httpsConnection.get(getAbsoluteUrl(String.format("/timelines/tag/%s",query)), 60, params, prefKeyOauthTokenT);
            statuses = parseStatuses(context, new JSONArray(response));
            if( statuses.size() > 0) {
                apiResponse.setSince_id(String.valueOf(Long.parseLong(statuses.get(0).getId())+1));
                apiResponse.setMax_id(String.valueOf(Long.parseLong(statuses.get(statuses.size() - 1).getId())-1));
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
        return apiResponse;
    }


    /**
     * Retrieves muted users by the authenticated account *synchronously*
     * @param max_id String id max
     * @return APIResponse
     */
    public APIResponse getMuted(String max_id){
        return getAccounts("/mutes/users/list.json",null, max_id, null, accountPerPage);
    }

    /**
     * Retrieves blocked users by the authenticated account *synchronously*
     * @param max_id String id max
     * @return APIResponse
     */
    public APIResponse getBlocks(String max_id){
        return getAccounts("/blocks/list.json",null,  max_id, null, accountPerPage);
    }


    /**
     * Retrieves following for the account specified by targetedId  *synchronously*
     * @param targetedId String targetedId
     * @param max_id String id max
     * @return APIResponse
     */
    public APIResponse getFollowing(String targetedId, String max_id){
        return getAccounts("/statuses/friends.json",targetedId, max_id, null, accountPerPage);
    }

    /**
     * Retrieves followers for the account specified by targetedId  *synchronously*
     * @param targetedId String targetedId
     * @param max_id String id max
     * @return APIResponse
     */
    public APIResponse getFollowers(String targetedId, String max_id){
        return getAccounts("/statuses/followers.json",targetedId, max_id, null, accountPerPage);
    }

    /**
     * Retrieves blocked users by the authenticated account *synchronously*
     * @param max_id String id max
     * @param since_id String since the id
     * @param limit int limit  - max value 40
     * @return APIResponse
     */
    @SuppressWarnings("SameParameterValue")
    private APIResponse getAccounts(String action, String targetedId, String max_id, String since_id, int limit){

        HashMap<String, String> params = new HashMap<>();
        if( max_id != null )
            params.put("max_id", max_id);
        if( since_id != null )
            params.put("since_id", since_id);
        if( 0 > limit || limit > 40)
            limit = 40;
        params.put("limit",String.valueOf(limit));
        if( targetedId != null)
            params.put("user_id",targetedId);
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
        params.put("count",String.valueOf(limit));
        statuses = new ArrayList<>();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.get(getAbsoluteUrl("/favorites.json"), 60, params, prefKeyOauthTokenT);
            statuses = parseStatuses(context, new JSONArray(response));
            if( statuses.size() > 0) {
                apiResponse.setSince_id(String.valueOf(Long.parseLong(statuses.get(0).getId())+1));
                apiResponse.setMax_id(String.valueOf(Long.parseLong(statuses.get(statuses.size() - 1).getId())-1));
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
        return apiResponse;
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
        return postAction(API.StatusAction.CREATESTATUS, null, status, null);
    }

    /**
     * Makes the post action
     * @param statusAction Enum
     * @param targetedId String id of the targeted Id *can be this of a status or an account*
     * @param status Status object related to the status
     * @param comment String comment for the report
     * @return in status code - Should be equal to 200 when action is done
     */
    private int postAction(API.StatusAction statusAction, String targetedId, Status status, String comment ){

        String action;
        HashMap<String, String> params = null;
        switch (statusAction){
            case FAVOURITE:
                if(MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.GNU) {
                    action = "/favorites/create.json";
                    params = new HashMap<>();
                    params.put("id", targetedId);
                }else {
                    action = "/friendica/activity/like.json";
                    params = new HashMap<>();
                    params.put("id", targetedId);
                }
                break;
            case UNFAVOURITE:
                if(MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.GNU) {
                    action = "/favorites/destroy.json";
                    params = new HashMap<>();
                    params.put("id", targetedId);
                }else {
                    action = "/friendica/activity/unlike.json";
                    params = new HashMap<>();
                    params.put("id", targetedId);
                }
                break;
            case REBLOG:
                action = String.format("/statuses/retweet/%s.json", targetedId);
                break;
            case UNREBLOG:
                action = String.format("/statuses/unretweet/%s.json", targetedId);
                break;
            case FOLLOW:
                action = "/friendships/create.json";
                params = new HashMap<>();
                params.put("user_id", targetedId);
                break;
            case REMOTE_FOLLOW:
                action = "/follows";
                params = new HashMap<>();
                params.put("uri", targetedId);
                break;
            case UNFOLLOW:
                action = "/friendships/destroy.json";
                params = new HashMap<>();
                params.put("user_id", targetedId);
                break;
            case BLOCK:
                action = "/blocks/create.json";
                params = new HashMap<>();
                params.put("user_id", targetedId);
                break;
            case UNBLOCK:
                action ="/blocks/destroy.json";
                params = new HashMap<>();
                params.put("user_id", targetedId);
                break;
            case MUTE:
                action = "/mutes/users/create.json";
                params = new HashMap<>();
                params.put("user_id", targetedId);
                break;
            case UNMUTE:
                action = "/mutes/users/destroy.json";
                params = new HashMap<>();
                params.put("user_id", targetedId);
                break;
            case UNSTATUS:
                if( !status.getVisibility().equals("direct"))
                    action = String.format("/statuses/destroy/%s.json", targetedId);
                else {
                    action = "/direct_messages/destroy.json";
                    params = new HashMap<>();
                    params.put("id", targetedId);
                }
                break;
            case CREATESTATUS:
                params = new HashMap<>();
                if(! status.getVisibility().equals("direct"))
                    action = "/statuses/update.json";
                else
                    action = "/direct_messages/new.json";
                if( !status.getVisibility().equals("direct")) {
                    try {
                        params.put("status", URLEncoder.encode(status.getContent(), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        params.put("status", status.getContent());
                    }
                }else{
                    try {
                        params.put("text", URLEncoder.encode(status.getContent(), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        params.put("text", status.getContent());
                    }
                }
                if( status.getContentType() != null)
                    params.put("content_type", status.getContentType());
                if( status.getIn_reply_to_id() != null) {
                    if( !status.getVisibility().equals("direct"))
                        params.put("in_reply_to_status_id", status.getIn_reply_to_id());
                    else
                        params.put("replyto", status.getConversationId());
                }
                if( status.getMedia_attachments() != null && status.getMedia_attachments().size() > 0 ) {
                    StringBuilder parameters = new StringBuilder();
                    for(Attachment attachment: status.getMedia_attachments())
                        parameters.append(attachment.getId()).append(",");
                    parameters = new StringBuilder(parameters.substring(0, parameters.length() - 1));
                    params.put("media_ids", parameters.toString());
                }
                if( status.isSensitive())
                    params.put("possibly_sensitive", Boolean.toString(status.isSensitive()));
            break;
            default:
                return -1;
        }

        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String resp = httpsConnection.post(getAbsoluteUrl(action), 60, params, prefKeyOauthTokenT);
            actionCode = httpsConnection.getActionCode();
            if( statusAction == API.StatusAction.REBLOG || statusAction == API.StatusAction.UNREBLOG || statusAction == API.StatusAction.FAVOURITE || statusAction == API.StatusAction.UNFAVOURITE) {
                Bundle b = new Bundle();
                try {
                    Status status1 = parseStatuses(context, new JSONObject(resp));
                    b.putParcelable("status", status1);
                } catch (JSONException ignored) {}
                Intent intentBC = new Intent(Helper.RECEIVE_ACTION);
                intentBC.putExtras(b);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intentBC);
            }
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
     * Posts a status
     * @param status Status object related to the status
     * @return APIResponse
     */
    public APIResponse postStatusAction(Status status){

        HashMap<String, String> params = new HashMap<>();
        if( status.getContentType() != null)
            params.put("content_type", status.getContentType());
        if( !status.getVisibility().equals("direct")) {
            try {
                params.put("status", URLEncoder.encode(status.getContent(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                params.put("status", status.getContent());
            }
        }else{
            try {
                params.put("text", URLEncoder.encode(status.getContent(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                params.put("text", status.getContent());
            }
            SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            //Current user
            String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
            SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
            Account currentAccount = new AccountDAO(context, db).getAccountByUserIDInstance(userId, Helper.getLiveInstance(context));
            params.put("user_id", currentAccount.getId());
            params.put("screen_name", currentAccount.getAcct());

        }
        if( status.getIn_reply_to_id() != null)
            if( !status.getVisibility().equals("direct"))
                params.put("in_reply_to_status_id", status.getIn_reply_to_id());
            else
                params.put("replyto", status.getIn_reply_to_id());
        if( status.getMedia_attachments() != null && status.getMedia_attachments().size() > 0 ) {
            StringBuilder parameters = new StringBuilder();
            for(Attachment attachment: status.getMedia_attachments())
                parameters.append(attachment.getId()).append(",");
            parameters = new StringBuilder(parameters.substring(0, parameters.length() - 1));
            params.put("media_ids", parameters.toString());
        }
        if( status.isSensitive())
            params.put("possibly_sensitive", Boolean.toString(status.isSensitive()));
        statuses = new ArrayList<>();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response;
            if( !status.getVisibility().equals("direct"))
                response = httpsConnection.post(getAbsoluteUrl("/statuses/update.json"), 60, params, prefKeyOauthTokenT);
            else
                response = httpsConnection.post(getAbsoluteUrl("/direct_messages/new.json"), 60, params, prefKeyOauthTokenT);
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
     * Retrieves notifications for the authenticated account since an id*synchronously*
     * @param since_id String since max
     * @return APIResponse
     */
    public APIResponse getNotificationsSince(DisplayNotificationsFragment.Type type, String since_id, boolean display){
        return getNotifications(type, null, since_id, notificationPerPage, display);
    }

    /**
     * Retrieves notifications for the authenticated account since an id*synchronously*
     * @param since_id String since max
     * @return APIResponse
     */
    @SuppressWarnings("SameParameterValue")
    public APIResponse getNotificationsSince(DisplayNotificationsFragment.Type type, String since_id, int notificationPerPage, boolean display){
        return getNotifications(type, null, since_id, notificationPerPage, display);
    }

    /**
     * Retrieves notifications for the authenticated account *synchronously*
     * @param max_id String id max
     * @return APIResponse
     */
    public APIResponse getNotifications(DisplayNotificationsFragment.Type type, String max_id, boolean display){
        return getNotifications(type, max_id, null, notificationPerPage, display);
    }


    /**
     * Retrieves notifications for the authenticated account *synchronously*
     * @param max_id String id max
     * @param since_id String since the id
     * @param limit int limit  - max value 40
     * @return APIResponse
     */
    private APIResponse getNotifications(DisplayNotificationsFragment.Type type, String max_id, String since_id, int limit, boolean display){

        HashMap<String, String> params = new HashMap<>();
        String stringType = null;
        if( max_id != null )
            params.put("max_id", max_id);
        if( since_id != null )
            params.put("since_id", since_id);
        if( 0 > limit || limit > 30)
            limit = 30;
        params.put("count",String.valueOf(limit));
        List<Notification> notifications = new ArrayList<>();
        String url = null;
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        //Current user
        SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = sharedpreferences.getString(Helper.PREF_INSTANCE, Helper.getLiveInstance(context));
        Account account = new AccountDAO(context, db).getUniqAccount(userId, instance);
        if(type == DisplayNotificationsFragment.Type.MENTION){
            params.put("name",account.getAcct());
            url = getAbsoluteUrl("/statuses/mentions_timeline.json");
            stringType = "mention";
        }else if(type == DisplayNotificationsFragment.Type.BOOST){
            url = getAbsoluteUrl("/statuses/retweets_of_me.json");
            stringType = "reblog";
        }else if(type == DisplayNotificationsFragment.Type.FOLLOW){
            url = getAbsoluteUrl("/statuses/followers.json");
            stringType = "follow";
        }
        if( url == null){
            Error error = new Error();
            error.setStatusCode(500);
            error.setError(context.getString(R.string.toast_error));
            apiResponse.setError(error);
            return apiResponse;
        }
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.get(url, 60, params, prefKeyOauthTokenT);
            apiResponse.setSince_id(httpsConnection.getSince_id());
            apiResponse.setMax_id(httpsConnection.getMax_id());
           if(type == DisplayNotificationsFragment.Type.FOLLOW){
               List<Account> accounts = parseAccountResponse(new JSONArray(response));
               if( accounts != null)
                   for(Account st: accounts ){
                       Notification notification = new Notification();
                       notification.setType(stringType);
                       notification.setId(st.getId());
                       notification.setStatus(null);
                       notification.setAccount(account);
                       notifications.add(notification);
                   }
            }else {
               List<Status> statuses = parseStatuses(context, new JSONArray(response));
               if( statuses != null)
                   for(Status st: statuses ){
                       Notification notification = new Notification();
                       notification.setType(stringType);
                       notification.setId(st.getId());
                       notification.setStatus(st);
                       notification.setAccount(st.getAccount());
                       notifications.add(notification);
                   }
           }

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
     * @return Results
     */
    public APIResponse search(String query) {
        Results results = new Results();
        HashMap<String, String> params = new HashMap<>();
        apiResponse = new APIResponse();
        if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE)
            params.put("q", query);
        else
            try {
                params.put("q", URLEncoder.encode(query, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                params.put("q", query);
            }
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.get(getAbsoluteUrl("/users/search.json"), 60, params, prefKeyOauthTokenT);
            List<Account> accounts = parseAccountResponse(new JSONArray(response));
            results.setAccounts(accounts);
            apiResponse.setResults(results);
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
        return apiResponse;
    }

    /**
     * Retrieves Accounts and feeds when searching *synchronously*
     *
     * @param query  String search
     * @return Results
     */
    public APIResponse search(String query, String max_id) {

        HashMap<String, String> params = new HashMap<>();
        try {
            query = URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException ignored) {}
        if (max_id != null)
            params.put("max_id", max_id);
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context);
            String response = httpsConnection.get(getAbsoluteUrl("/statusnet/tags/timeline/"+query.trim().toLowerCase().replaceAll("\\#","")+".json"), 60, params, null);
            List<Status> statuses = parseStatuses(context, new JSONArray(response));
            if( statuses.size() > 0) {
                apiResponse.setSince_id(String.valueOf(Long.parseLong(statuses.get(0).getId())+1));
                apiResponse.setMax_id(String.valueOf(Long.parseLong(statuses.get(statuses.size() - 1).getId())-1));
            }
            apiResponse.setStatuses(statuses);
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
        return apiResponse;
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
    private static Status parseStatuses(Context context, JSONObject resobj){
        Status status = new Status();
        try {
            status.setId(resobj.get("id").toString());
            try {
                status.setUri(resobj.get("uri").toString());
            }catch (Exception ignored){
                status.setUri(resobj.get("id").toString());
            }
            status.setCreated_at(Helper.mstStringToDate(context, resobj.get("created_at").toString()));
            if( resobj.has("in_reply_to_status_id"))
             status.setIn_reply_to_id(resobj.get("in_reply_to_status_id").toString());
            if( resobj.has("in_reply_to_user_id"))
                status.setIn_reply_to_account_id(resobj.get("in_reply_to_user_id").toString());
            status.setSensitive(false);
            status.setSpoiler_text(null);
            if( !resobj.has("sender"))
                status.setVisibility("public");
            else
                status.setVisibility("direct");
            if( resobj.has("geo"))
                status.setLanguage(resobj.isNull("geo")?null:resobj.getString("geo"));
            if( resobj.has("external_url"))
                status.setUrl(resobj.get("external_url").toString());
            //Retrieves attachments
            try {
                JSONArray arrayAttachement = resobj.getJSONArray("attachments");
                ArrayList<Attachment> attachments = new ArrayList<>(parseAttachmentResponse(arrayAttachement));
                status.setMedia_attachments(attachments);
            }catch (Exception ignored){ status.setMedia_attachments(new ArrayList<>());}

            status.setCard(null);
            try {
                status.setConversationId(resobj.get("statusnet_conversation_id").toString());
            }catch (Exception ignored){
                if( resobj.has("friendica_parent_uri"))
                    status.setConversationId(resobj.get("friendica_parent_uri").toString());
                else
                    status.setConversationId(resobj.get("id").toString());
            }
            //Retrieves mentions
            List<Mention> mentions = new ArrayList<>();
            if( resobj.has("attentions")) {
                JSONArray arrayMention = resobj.getJSONArray("attentions");
                if (arrayMention != null) {
                    for (int j = 0; j < arrayMention.length(); j++) {
                        JSONObject menObj = arrayMention.getJSONObject(j);
                        Mention mention = new Mention();
                        mention.setId(menObj.get("id").toString());
                        mention.setUrl(menObj.get("profileurl").toString());
                        mention.setAcct(menObj.get("screen_name").toString());
                        mention.setUsername(menObj.get("fullname").toString());
                        mentions.add(mention);
                    }
                }
                status.setMentions(mentions);
            }else{
                status.setMentions(new ArrayList<>());
            }

            //Retrieves tags
            status.setTags(null);
            //Retrieves emjis
            status.setEmojis(new ArrayList<>());
            //Retrieve Application
            Application application = new Application();
            try {
                if(resobj.getJSONObject("source") != null){
                    application.setName(resobj.getJSONObject("source").toString());
                    application.setWebsite(resobj.getJSONObject("source_link").toString());
                }
            }catch (Exception e){
                application = new Application();
            }
            status.setApplication(application);
            if( resobj.has("user"))
                status.setAccount(parseAccountResponse(context, resobj.getJSONObject("user")));
            else if( resobj.has("sender"))
                status.setAccount(parseAccountResponse(context, resobj.getJSONObject("sender")));
            if( resobj.has("statusnet_html"))
                status.setContent(resobj.get("statusnet_html").toString());
            else if( resobj.has("text"))
                status.setContent(resobj.get("text").toString());
            if(resobj.has("fave_num"))
                status.setFavourites_count(Integer.valueOf(resobj.get("fave_num").toString()));
            else
                status.setFavourites_count(0);
            if(resobj.has("repeat_num"))
                status.setReblogs_count(Integer.valueOf(resobj.get("repeat_num").toString()));
            else
                status.setReblogs_count(0);
            status.setReplies_count(0);
            try {
                status.setReblogged(Boolean.valueOf(resobj.get("repeated").toString()));
            }catch (Exception e){
                status.setReblogged(false);
            }
            try {
                status.setFavourited(Boolean.valueOf(resobj.get("favorited").toString()));
            }catch (Exception e){
                status.setFavourited(false);
            }
            if( resobj.has("friendica_activities") && resobj.getJSONObject("friendica_activities").has("like")){
                status.setFavourited(false);
                JSONArray jsonArray = resobj.getJSONObject("friendica_activities").getJSONArray("like");
                SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject like = jsonArray.getJSONObject(i);
                    if( like.getString("id").equals(userId)) {
                        status.setFavourited(true);
                        break;
                    }
                }

            }
            status.setMuted(false);
            status.setPinned(false);
            try{
                status.setReblog(parseStatuses(context, resobj.getJSONObject("retweeted_status")));
            }catch (Exception ignored){ status.setReblog(null);}
            if( status.getContent().contains(status.getUri()))
                status.setNotice(true);
        } catch (JSONException ignored) {ignored.printStackTrace();} catch (ParseException e) {
            e.printStackTrace();

        }
        return status;
    }


    /**
     * Parse json response for unique schedule
     * @param resobj JSONObject
     * @return Status
     */
    @SuppressWarnings("InfiniteRecursion")
    private static Status parseSchedule(Context context, JSONObject resobj){
        Status status = new Status();
        try {
            status.setIn_reply_to_id(resobj.get("in_reply_to_id").toString());
            status.setSensitive(Boolean.parseBoolean(resobj.get("sensitive").toString()));
            status.setSpoiler_text(resobj.get("spoiler_text").toString());
            try {
                status.setVisibility(resobj.get("visibility").toString());
            }catch (Exception e){status.setVisibility("public");}
            status.setContent(resobj.get("text").toString());
        } catch (JSONException ignored) {}
        return status;
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
     * Parse json response an unique account
     * @param resobj JSONObject
     * @return Account
     */
    @SuppressWarnings("InfiniteRecursion")
    public static Account parseAccountResponse(Context context, JSONObject resobj){

        Account account = new Account();
        try {
            account.setId(resobj.get("id").toString());
            if( resobj.has("ostatus_uri"))
                account.setUuid(resobj.get("ostatus_uri").toString());
            else
                account.setUuid(resobj.get("id").toString());
            account.setUsername(resobj.get("screen_name").toString());
            account.setAcct(resobj.get("screen_name").toString());
            account.setDisplay_name(resobj.get("name").toString());
            account.setLocked(Boolean.parseBoolean(resobj.get("protected").toString()));
            account.setCreated_at(Helper.mstStringToDate(context, resobj.get("created_at").toString()));
            account.setFollowers_count(Integer.valueOf(resobj.get("followers_count").toString()));
            account.setFollowing_count(Integer.valueOf(resobj.get("friends_count").toString()));
            account.setStatuses_count(Integer.valueOf(resobj.get("statuses_count").toString()));
            account.setNote(resobj.get("description").toString());
            account.setBot(false);
            account.setMoved_to_account(null);
            account.setUrl(resobj.get("url").toString());
            account.setAvatar(resobj.get("profile_image_url_https").toString());
            account.setAvatar_static(resobj.get("profile_image_url_https").toString());
            if( !resobj.isNull("background_image")) {
                account.setHeader(resobj.get("background_image").toString());
                account.setHeader_static(resobj.get("background_image").toString());
            }else{
                account.setHeader("null");
                account.setHeader_static("null");
            }

            if( resobj.has("cid"))
                account.setSocial("FRIENDICA");
            else
                account.setSocial("GNU");
            account.setEmojis(new ArrayList<>());
        } catch (JSONException ignored) {} catch (ParseException e) {
            e.printStackTrace();
        }
        return account;
    }


    /**
     * Parse json response an unique relationship
     * @param resobjIni JSONObject
     * @return Relationship
     */
    private Relationship parseRelationshipResponse(JSONObject resobjIni){


        Relationship relationship = new Relationship();
        try {
            JSONObject resobj = resobjIni.getJSONObject("relationship").getJSONObject("source");
            relationship.setId(resobj.get("id").toString());
            relationship.setFollowing(Boolean.valueOf(resobj.get("following").toString()));
            relationship.setFollowed_by(Boolean.valueOf(resobj.get("followed_by").toString()));
            relationship.setBlocking(Boolean.valueOf(resobj.get("blocking").toString()));
            try {
                relationship.setMuting(Boolean.valueOf(resobj.get("muting").toString()));
            }catch (Exception ignored){
                    relationship.setMuting(false);
                }
            try {
                relationship.setMuting_notifications(!Boolean.valueOf(resobj.get("notifications_enabled").toString()));
            }catch (Exception ignored){
                relationship.setMuting_notifications(false);
            }
            relationship.setEndorsed(false);
            relationship.setShowing_reblogs(true);
            relationship.setRequested(false);
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
     * Parse json response for list of relationship
     * @param jsonArray JSONArray
     * @return List<Relationship>
     */
    private static List<Attachment> parseAttachmentResponse(JSONArray jsonArray){

        List<Attachment> attachments = new ArrayList<>();
        try {
            int i = 0;
            while (i < jsonArray.length() ) {
                JSONObject resobj = jsonArray.getJSONObject(i);
                Attachment attachment = parseAttachmentResponse(resobj);
                attachments.add(attachment);
                i++;
            }
        } catch (JSONException ignored) { }
        return attachments;
    }
    /**
     * Parse json response an unique attachment
     * @param resobj JSONObject
     * @return Relationship
     */
    static Attachment parseAttachmentResponse(JSONObject resobj){

        Attachment attachment = new Attachment();
        try {
            if(resobj.has("id") )
                attachment.setId(resobj.get("id").toString());
            if( resobj.has("url") ){
                attachment.setUrl(resobj.getString("url"));
                attachment.setPreview_url(resobj.getString("url"));
                attachment.setRemote_url(resobj.getString("url"));
            }

            if( attachment.getUrl().endsWith("png") || attachment.getUrl().endsWith("jpg") || attachment.getUrl().endsWith("jpeg")){
                attachment.setType("image");
            }else if( attachment.getUrl().endsWith("gif") ||  attachment.getUrl().endsWith("apng") ){
                attachment.setType("gifv");
            }else if( attachment.getUrl().endsWith("mp4") ){
                attachment.setType("video");
            }else{
                attachment.setType("web");
            }

            try {
                attachment.setDescription(resobj.get("description").toString());
            }catch (JSONException ignore){}
            try{
                attachment.setRemote_url(resobj.get("url").toString());
            }catch (JSONException ignore){}
            try{
                attachment.setPreview_url(resobj.get("thumb_url").toString());
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
     * Parse json response an unique attachment
     * @param resobj JSONObject
     * @return Relationship
     */
    static Attachment parseUploadedAttachmentResponse(JSONObject resobj){

        Attachment attachment = new Attachment();
        try {
            if(resobj.has("media_id") )
                attachment.setId(resobj.get("media_id").toString());
            if( resobj.has("image") && resobj.getJSONObject("image").has("image_type"))
                attachment.setType("Image");
            else if(resobj.has("image") && resobj.getJSONObject("gif").has("image_type"))
                attachment.setType("GifV");
            else
                attachment.setType("video");

            try{
                attachment.setRemote_url(resobj.get("url").toString());
            }catch (JSONException ignore){}
            try{
                attachment.setPreview_url(resobj.get("thumb_url").toString());
            }catch (JSONException ignore){}

            if( resobj.has("image") && resobj.getJSONObject("image").has("friendica_preview_url")){
                attachment.setUrl(resobj.getJSONObject("image").getString("friendica_preview_url"));
                attachment.setPreview_url(resobj.getJSONObject("image").getString("friendica_preview_url"));
                attachment.setRemote_url(resobj.getJSONObject("image").getString("friendica_preview_url"));
            }
            if( resobj.has("media_url"))
                attachment.setUrl(resobj.get("media_url").toString());
            try {
                attachment.setDescription(resobj.get("description").toString());
            }catch (JSONException ignore){}

            try{
                attachment.setMeta(resobj.get("meta").toString());
            }catch (JSONException ignore){}
            try{
                attachment.setText_url(resobj.get("text_url").toString());
            }catch (JSONException ignore){}

        } catch (JSONException ignored) {ignored.printStackTrace();}
        return attachment;
    }

    /**
     * Parse json response an unique notification
     * @param resobj JSONObject
     * @return Account
     */
    private Notification parseNotificationResponse(Context context, JSONObject resobj){

        Notification notification = new Notification();
        try {
            notification.setId(resobj.get("id").toString());
            notification.setType(resobj.get("ntype").toString());
            notification.setCreated_at(Helper.mstStringToDate(context, resobj.get("created_at").toString()));
            notification.setAccount(parseAccountResponse(context, resobj.getJSONObject("from_profile")));
            try{
                notification.setStatus(parseStatuses(context, resobj.getJSONObject("notice")));
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
        return Helper.instanceWithProtocol(this.instance) + "/api" + action;
    }


}
