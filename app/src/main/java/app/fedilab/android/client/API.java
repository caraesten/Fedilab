package app.fedilab.android.client;
/* Copyright 2017 Thomas Schneider
 *
 * This file is a part of Fedilab
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Fedilab is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Fedilab; if not,
 * see <http://www.gnu.org/licenses>. */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;


import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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
import java.util.Locale;
import java.util.Map;

import app.fedilab.android.R;
import app.fedilab.android.activities.LoginActivity;
import app.fedilab.android.activities.MainActivity;
import app.fedilab.android.asynctasks.PostAdminActionAsyncTask;
import app.fedilab.android.asynctasks.RetrieveOpenCollectiveAsyncTask;
import app.fedilab.android.asynctasks.UpdateAccountInfoAsyncTask;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.AccountAdmin;
import app.fedilab.android.client.Entities.AccountCreation;
import app.fedilab.android.client.Entities.AdminAction;
import app.fedilab.android.client.Entities.Application;
import app.fedilab.android.client.Entities.Attachment;
import app.fedilab.android.client.Entities.Card;
import app.fedilab.android.client.Entities.Conversation;
import app.fedilab.android.client.Entities.Emojis;
import app.fedilab.android.client.Entities.Error;
import app.fedilab.android.client.Entities.Filters;
import app.fedilab.android.client.Entities.HowToVideo;
import app.fedilab.android.client.Entities.Instance;
import app.fedilab.android.client.Entities.InstanceNodeInfo;
import app.fedilab.android.client.Entities.InstanceReg;
import app.fedilab.android.client.Entities.InstanceSocial;
import app.fedilab.android.client.Entities.Mention;
import app.fedilab.android.client.Entities.NodeInfo;
import app.fedilab.android.client.Entities.Notification;
import app.fedilab.android.client.Entities.Peertube;
import app.fedilab.android.client.Entities.Poll;
import app.fedilab.android.client.Entities.PollOptions;
import app.fedilab.android.client.Entities.Relationship;
import app.fedilab.android.client.Entities.Report;
import app.fedilab.android.client.Entities.Results;
import app.fedilab.android.client.Entities.Schedule;
import app.fedilab.android.client.Entities.Status;
import app.fedilab.android.client.Entities.StoredStatus;
import app.fedilab.android.client.Entities.Tag;
import app.fedilab.android.fragments.DisplayNotificationsFragment;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.sqlite.AccountDAO;
import app.fedilab.android.sqlite.Sqlite;
import app.fedilab.android.sqlite.TimelineCacheDAO;


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
    private List<Conversation> conversations;
    private int tootPerPage, accountPerPage, notificationPerPage;
    private int actionCode;
    private String instance;
    private String prefKeyOauthTokenT;
    private APIResponse apiResponse;
    private Error APIError;
    private List<String> domains;

    public enum searchType{
        TAGS,
        STATUSES,
        ACCOUNTS
    }


    public enum adminAction{
        ENABLE,
        APPROVE,
        REJECT,
        NONE,
        SILENCE,
        DISABLE,
        UNSILENCE,
        SUSPEND,
        UNSUSPEND,
        ASSIGN_TO_SELF,
        UNASSIGN,
        REOPEN,
        RESOLVE,
        GET_ACCOUNTS,
        GET_ONE_ACCOUNT,
        GET_REPORTS,
        GET_ONE_REPORT
    }


    public enum StatusAction{
        FAVOURITE,
        UNFAVOURITE,
        REBLOG,
        UNREBLOG,
        MUTE,
        MUTE_NOTIFICATIONS,
        UNMUTE,
        MUTE_CONVERSATION,
        UNMUTE_CONVERSATION,
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
        BLOCK_DOMAIN,
        RATEVIDEO,
        PEERTUBECOMMENT,
        PEERTUBEREPLY,
        PEERTUBEDELETECOMMENT,
        PEERTUBEDELETEVIDEO,
        UPDATESERVERSCHEDULE,
        DELETESCHEDULED,
        REFRESHPOLL
    }


    public enum accountPrivacy {
        PUBLIC,
        LOCKED
    }


    public API(Context context) {
        this.context = context;
        if( context == null) {
            APIError = new Error();
            return;
        }
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        tootPerPage = Helper.TOOTS_PER_PAGE;
        accountPerPage = Helper.ACCOUNTS_PER_PAGE;
        notificationPerPage = Helper.NOTIFICATIONS_PER_PAGE;
        this.prefKeyOauthTokenT = sharedpreferences.getString(Helper.PREF_KEY_OAUTH_TOKEN, null);
        if( Helper.getLiveInstance(context) != null)
            this.instance = Helper.getLiveInstance(context);
        else {
            SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
            String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
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


    /**
     * Execute admin get actions
     * @param action type of the action
     * @param id String can for an account or a status
     * @return APIResponse
     */
    public APIResponse adminGet(adminAction action, String id, AdminAction adminAction){
        apiResponse = new APIResponse();
        HashMap<String, String> params = null;
        String endpoint = null;
        String url_action = null;
        switch (action){
            case GET_ACCOUNTS:
                params = new HashMap<>();
                if(MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON) {
                    if (adminAction.isLocal())
                        params.put("local", String.valueOf(adminAction.isLocal()));
                    if (adminAction.isRemote())
                        params.put("remote", String.valueOf(adminAction.isRemote()));
                    if (adminAction.isActive())
                        params.put("active", String.valueOf(adminAction.isActive()));
                    if (adminAction.isPending())
                        params.put("pending", String.valueOf(adminAction.isPending()));
                    if (adminAction.isDisabled())
                        params.put("disabled", String.valueOf(adminAction.isDisabled()));
                    if (adminAction.isSilenced())
                        params.put("silenced", String.valueOf(adminAction.isSilenced()));
                    if (adminAction.isSuspended())
                        params.put("suspended", String.valueOf(adminAction.isSuspended()));
                    endpoint = "/admin/accounts";
                }else if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA){
                    if (adminAction.isLocal())
                        params.put("filters","local");
                    if (adminAction.isRemote())
                        params.put("filters","external");
                    if (adminAction.isActive())
                        params.put("filters","active");
                    if (adminAction.isDisabled())
                        params.put("filters","deactivated");
                    endpoint = "/admin/users";
                }
                break;
            case GET_ONE_ACCOUNT:
                if(MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON) {
                    endpoint = String.format("/admin/accounts/%s", id);
                }else {
                    try {
                        id = URLEncoder.encode(id, "UTF-8");
                    } catch (UnsupportedEncodingException e) { }
                    params = new HashMap<>();
                    params.put("query", id);
                    endpoint = "/admin/users";

                }
                break;
            case GET_REPORTS:
                endpoint = "/admin/reports";
                if(MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON) {
                    if( !adminAction.isUnresolved()) {
                        params = new HashMap<>();
                        params.put("resolved", "present");
                    }
                }else if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA) {
                    if( adminAction.isUnresolved()) {
                        params = new HashMap<>();
                        params.put("state", "open");
                    }else{
                        params = new HashMap<>();
                        params.put("state", "resolved");
                    }
                }
                break;
            case GET_ONE_REPORT:
                endpoint = String.format("/admin/reports/%s", id);
                break;
        }
        if(MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON) {
            url_action = getAbsoluteUrl(endpoint);

        }else if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA) {
            url_action = Helper.instanceWithProtocol(this.context, this.instance) + "/api/pleroma" + endpoint;
        }
        try {
            String response = new HttpsConnection(context, this.instance).get(url_action, 10, params, prefKeyOauthTokenT);
            if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA){
                if( new JSONObject(response).has("users") ) {
                    response = new JSONArray(new JSONObject(response).getJSONArray("users").toString()).toString();
                }else if( new JSONObject(response).has("reports") ) {
                    response = new JSONArray(new JSONObject(response).getJSONArray("reports").toString()).toString();
                }
            }
            switch (action){
                case GET_ACCOUNTS:
                    List<AccountAdmin> accountAdmins = parseAccountAdminResponse(new JSONArray(response));
                    apiResponse.setAccountAdmins(accountAdmins);
                    break;
                case GET_ONE_ACCOUNT:

                    if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA) {
                        accountAdmins = parseAccountAdminResponse(new JSONArray(response));
                        if( accountAdmins != null && accountAdmins.size() > 0) {
                            Account accountpleroma = getAccount(accountAdmins.get(0).getId());
                            if (accountpleroma != null) {
                                accountAdmins.get(0).setAccount(accountpleroma);
                            }
                        }
                    }else{
                        AccountAdmin accountAdmin = parseAccountAdminResponse(context, new JSONObject(response));
                        accountAdmins = new ArrayList<>();
                        accountAdmins.add(accountAdmin);
                    }
                    apiResponse.setAccountAdmins(accountAdmins);
                    break;
                case GET_REPORTS:
                    List<Report> reports = parseReportAdminResponse(new JSONArray(response));
                    apiResponse.setReports(reports);
                    break;
                case GET_ONE_REPORT:
                    reports = new ArrayList<>();
                    Report report = parseReportAdminResponse(context, new JSONObject(response));
                    reports.add(report);
                    apiResponse.setReports(reports);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return apiResponse;
    }


    /**
     * Execute admin post actions
     * @param action type of the action
     * @param id String can for an account or a status
     * @return APIResponse
     */
    public APIResponse adminDo(adminAction action, String id, AdminAction adminAction){
        apiResponse = new APIResponse();
        String http_action = "POST";
        String endpoint = null;
        String url_action = null;
        HashMap<String, String> params = null;
        switch (action){
            case ENABLE:
                if(MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON) {
                    endpoint = String.format("/admin/accounts/%s/enable", id);
                }else if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA) {
                    http_action = "PATCH";
                    endpoint = String.format( "/admin/users/%s/toggle_activation", id);
                }
                break;
            case APPROVE:
                endpoint = String.format("/admin/accounts/%s/approve", id);
                break;
            case REJECT:
                endpoint = String.format("/admin/accounts/%s/reject", id);
                break;
            case UNSILENCE:
                endpoint = String.format("/admin/accounts/%s/unsilence", id);
                break;
            case UNSUSPEND:
                endpoint = String.format("/admin/accounts/%s/unsuspend", id);
                break;
            case ASSIGN_TO_SELF:
                endpoint = String.format("/admin/reports/%s/assign_to_self", id);
                break;
            case UNASSIGN:
                endpoint = String.format("/admin/reports/%s/unassign", id);
                break;
            case REOPEN:
                if(MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON) {
                    endpoint = String.format("/admin/reports/%s/reopen", id);
                }else if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA) {
                    endpoint = String.format("/admin/reports/%s", id);
                    params = new HashMap<>();
                    params.put("state","open");
                    http_action = "PUT";
                }
                break;
            case RESOLVE:
                if(MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON) {
                    endpoint = String.format("/admin/reports/%s/resolve", id);
                }else if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA) {
                    endpoint = String.format("/admin/reports/%s", id);
                    params = new HashMap<>();
                    params.put("state","resolved");
                    http_action = "PUT";
                }
                break;
            case NONE:
                params = new HashMap<>();
                params.put("type","none");
                endpoint = String.format("/admin/accounts/%s/action", id);
                params.put("send_email_notification", String.valueOf(adminAction.isSend_email_notification()));
                if( adminAction.getText() != null) {
                    params.put("text", adminAction.getText());
                }
                break;
            case DISABLE:

                if(MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON) {
                    params = new HashMap<>();
                    params.put("type","disable");
                    endpoint = String.format("/admin/accounts/%s/action", id);
                    params.put("send_email_notification", String.valueOf(adminAction.isSend_email_notification()));
                    if( adminAction.getText() != null) {
                        params.put("text", adminAction.getText());
                    }

                }else if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA) {
                    http_action = "PATCH";
                    endpoint = String.format( "/admin/users/%s/toggle_activation", id);
                }
                break;
            case SILENCE:

                params = new HashMap<>();
                params.put("type","silence");
                endpoint = String.format("/admin/accounts/%s/action", id);
                params.put("send_email_notification", String.valueOf(adminAction.isSend_email_notification()));
                if( adminAction.getText() != null) {
                    params.put("text", adminAction.getText());
                }
                break;
            case SUSPEND:
                if(MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON) {
                    params = new HashMap<>();
                    params.put("type", "suspend");
                    endpoint = String.format("/admin/accounts/%s/action", id);
                    params.put("send_email_notification", String.valueOf(adminAction.isSend_email_notification()));
                    if (adminAction.getText() != null) {
                        params.put("text", adminAction.getText());
                    }
                }else if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA) {
                    http_action = "DELETE";
                    endpoint = "/admin/users";
                    params = new HashMap<>();
                    params.put("nickname", id);
                }
                break;
        }
        if(MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON) {
            url_action = getAbsoluteUrl(endpoint);

        }else if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA) {
            url_action = Helper.instanceWithProtocol(this.context, this.instance) + "/api/pleroma" + endpoint;
        }
        try {
            String response = null;
            switch (http_action) {
                case "POST":
                    response = new HttpsConnection(context, this.instance).post(url_action, 10, params, prefKeyOauthTokenT);
                    break;
                case "PATCH":
                    response = new HttpsConnection(context, this.instance).patch(url_action, 10, params, null, null, null, null, prefKeyOauthTokenT);
                    break;
                case "PUT":
                    response = new HttpsConnection(context, this.instance).put(url_action, 10, params, prefKeyOauthTokenT);
                    break;
                case "DELETE":
                    new HttpsConnection(context, this.instance).delete(url_action, 10, params, prefKeyOauthTokenT);
                    break;
            }
            switch (action){
                case ENABLE:
                case APPROVE:
                case REJECT:
                case UNSILENCE:
               // case UNDISABLE:
                case UNSUSPEND:
                    List<AccountAdmin> accountAdmins = null;
                    try {
                        AccountAdmin accountAdmin = parseAccountAdminResponse(context, new JSONObject(response));
                        accountAdmin.setAction(action);
                        accountAdmins = new ArrayList<>();
                        accountAdmins.add(accountAdmin);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    apiResponse.setAccountAdmins(accountAdmins);
                    break;
                case ASSIGN_TO_SELF:
                case UNASSIGN:
                case REOPEN:
                case RESOLVE:
                    List<Report> reports = null;
                    Report report;
                    try {
                        reports = new ArrayList<>();
                        report = parseReportAdminResponse(context, new JSONObject(response));
                        reports.add(report);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    apiResponse.setReports(reports);

                    break;
                case NONE:
                    accountAdmins = new ArrayList<>();
                    AccountAdmin accountAdmin = new AccountAdmin();
                    accountAdmin.setAction(action);
                    accountAdmins.add(accountAdmin);
                    apiResponse.setAccountAdmins(accountAdmins);
                    break;
                case DISABLE:
                    accountAdmins = new ArrayList<>();
                    accountAdmin = new AccountAdmin();
                    accountAdmin.setDisabled(true);
                    accountAdmin.setAction(action);
                    accountAdmins.add(accountAdmin);
                    apiResponse.setAccountAdmins(accountAdmins);
                    break;
                case SILENCE:
                    accountAdmins = new ArrayList<>();
                    accountAdmin = new AccountAdmin();
                    accountAdmin.setSilenced(true);
                    accountAdmin.setAction(action);
                    accountAdmins.add(accountAdmin);
                    apiResponse.setAccountAdmins(accountAdmins);
                    break;
                case SUSPEND:
                    accountAdmins = new ArrayList<>();
                    accountAdmin = new AccountAdmin();
                    accountAdmin.setSuspended(true);
                    accountAdmin.setAction(action);
                    accountAdmins.add(accountAdmin);
                    apiResponse.setAccountAdmins(accountAdmins);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
            e.printStackTrace();
        }
        return apiResponse;
    }

    public InstanceNodeInfo getNodeInfo(String domain){

        //Try to guess URL scheme for the onion instance
        String scheme = "https";
        if( domain.endsWith(".onion")){
            try {
                new HttpsConnection(context, domain).get("http://" + domain, 30, null, null);
                SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(Helper.SET_ONION_SCHEME + domain, "http");
                scheme = "http";
                editor.apply();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (HttpsConnection.HttpsConnectionException e) {
                SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(Helper.SET_ONION_SCHEME + domain, "https");
                editor.apply();
            }
        }
        String response;
        InstanceNodeInfo instanceNodeInfo = new InstanceNodeInfo();
        try {
            response = new HttpsConnection(context, domain).get(scheme+"://" + domain + "/.well-known/nodeinfo", 30, null, null);
            JSONArray jsonArray = new JSONObject(response).getJSONArray("links");
            ArrayList<NodeInfo> nodeInfos = new ArrayList<>();
            try {
                int i = 0;
                while (i < jsonArray.length() ){

                    JSONObject resobj = jsonArray.getJSONObject(i);
                    NodeInfo nodeInfo = new NodeInfo();
                    nodeInfo.setHref(resobj.getString("href"));
                    nodeInfo.setRel(resobj.getString("rel"));
                    i++;
                    nodeInfos.add(nodeInfo);
                }
                if( nodeInfos.size() > 0){
                    NodeInfo nodeInfo = nodeInfos.get(nodeInfos.size()-1);
                    response = new HttpsConnection(context, this.instance).get(nodeInfo.getHref(), 30, null, null);
                    JSONObject resobj = new JSONObject(response);
                    JSONObject jsonObject = resobj.getJSONObject("software");
                    String name= null;
                    if( resobj.has("metadata") && resobj.getJSONObject("metadata").has("features") ) {
                        JSONArray features = resobj.getJSONObject("metadata").getJSONArray("features");
                        if( features != null && features.length() > 0){
                            for( int counter = 0; counter < features.length(); counter++ ) {
                                if( features.getString(counter).toUpperCase().equals("MASTODON_API") ) {
                                    name = "MASTODON";
                                    break;
                                }
                            }
                        }
                    }
                    if( name == null) {
                        name = jsonObject.getString("name").toUpperCase();
                        if (jsonObject.getString("name") != null) {
                            switch (jsonObject.getString("name").toUpperCase()) {
                                case "PLEROMA":
                                    name = "MASTODON";
                                    break;
                                case "GNUSOCIAL":
                                case "HUBZILLA":
                                case "REDMATRIX":
                                case "FRIENDICA":
                                    name = "GNU";
                                    break;
                            }
                        }
                    }
                    instanceNodeInfo.setName(name);
                    instanceNodeInfo.setVersion(jsonObject.getString("version"));
                    instanceNodeInfo.setOpenRegistrations(resobj.getBoolean("openRegistrations"));
                }
            } catch (JSONException e) {
                setDefaultError(e);
            }
        } catch (IOException e) {
            instanceNodeInfo.setConnectionError(true);
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (HttpsConnection.HttpsConnectionException e) {
            try {
                response = new HttpsConnection(context, this.instance).get(scheme+"://" + domain + "/api/v1/instance", 30, null, null);
                JSONObject jsonObject = new JSONObject(response);
                instanceNodeInfo.setName("MASTODON");
                instanceNodeInfo.setVersion(jsonObject.getString("version"));
                instanceNodeInfo.setOpenRegistrations(true);
            } catch (IOException e1) {
                instanceNodeInfo.setConnectionError(true);
                e1.printStackTrace();
            } catch (NoSuchAlgorithmException e1) {
                e1.printStackTrace();
            } catch (KeyManagementException e1) {
                e1.printStackTrace();
            } catch (HttpsConnection.HttpsConnectionException e1) {
                instanceNodeInfo.setName("GNU");
                instanceNodeInfo.setVersion("unknown");
                instanceNodeInfo.setOpenRegistrations(true);
                e1.printStackTrace();
            } catch (JSONException e1) {

                e1.printStackTrace();
            }
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return instanceNodeInfo;
    }


    public API(Context context, String instance, String token) {
        this.context = context;
        if( context == null) {
            apiResponse = new APIResponse();
            APIError = new Error();
            return;
        }
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        tootPerPage = Helper.TOOTS_PER_PAGE;
        accountPerPage = Helper.ACCOUNTS_PER_PAGE;
        notificationPerPage = Helper.NOTIFICATIONS_PER_PAGE;
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
            String response = new HttpsConnection(context, this.instance).get(getAbsoluteUrl("/instance"), 30, null, prefKeyOauthTokenT);
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
     * Get instance for registering an account *synchronously*
     * @return APIResponse
     */
    public APIResponse getInstanceReg(String category) {
        apiResponse = new APIResponse();
        try {
            String response = new HttpsConnection(context, null).get(String.format("https://api.joinmastodon.org/servers?category=%s", category));
            List<InstanceReg> instanceRegs = parseInstanceReg(new JSONArray(response));
            apiResponse.setInstanceRegs(instanceRegs);
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
    public APIResponse updateCredential(String display_name, String note, ByteArrayInputStream avatar, String avatarName, ByteArrayInputStream header, String headerName, accountPrivacy privacy, HashMap<String, String> customFields, boolean sensitive) {

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
        requestParams.put("source[sensitive]", String.valueOf(sensitive));
        try {
            new HttpsConnection(context, this.instance).patch(getAbsoluteUrl("/accounts/update_credentials"), 10, requestParams, avatar, avatarName, header, headerName, prefKeyOauthTokenT);
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
            if( context == null) {
                setError(500, new Throwable("An error occured!"));
                return null;
            }
            String response = new HttpsConnection(context, this.instance).get(getAbsoluteUrl("/accounts/verify_credentials"), 10, null, prefKeyOauthTokenT);
            account = parseAccountResponse(context, new JSONObject(response));
            if( account.getSocial().equals("PLEROMA")){
                isPleromaAdmin(account.getAcct());
            }
        } catch (HttpsConnection.HttpsConnectionException e) {
            e.printStackTrace();
            if( e.getStatusCode() == 401 || e.getStatusCode() == 403){
                SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                Account targetedAccount = new AccountDAO(context, db).getAccountByToken(prefKeyOauthTokenT);
                if( targetedAccount == null)
                    return null;
                HashMap<String, String> values = refreshToken(targetedAccount.getClient_id(), targetedAccount.getClient_secret(), targetedAccount.getRefresh_token());
                if( values.containsKey("access_token") && values.get("access_token") != null) {
                    targetedAccount.setToken(values.get("access_token"));
                    SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                    String token = sharedpreferences.getString(Helper.PREF_KEY_OAUTH_TOKEN, null);
                    //This account is currently logged in, the token is updated
                    if( prefKeyOauthTokenT.equals(token)){
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString(Helper.PREF_KEY_OAUTH_TOKEN, targetedAccount.getToken());
                        editor.apply();
                    }
                }if( values.containsKey("refresh_token") && values.get("refresh_token") != null)
                    targetedAccount.setRefresh_token(values.get("refresh_token"));
                new AccountDAO(context, db).updateAccountCredential(targetedAccount);
                String response;
                try {
                    response = new HttpsConnection(context, this.instance).get(getAbsoluteUrl("/accounts/verify_credentials"), 10, null, targetedAccount.getToken());
                    account = parseAccountResponse(context, new JSONObject(response));
                    if( account.getSocial().equals("PLEROMA")){
                        isPleromaAdmin(account.getAcct());
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (NoSuchAlgorithmException e1) {
                    e1.printStackTrace();
                } catch (KeyManagementException e1) {
                    e1.printStackTrace();
                } catch (JSONException e1) {
                    e1.printStackTrace();
                } catch (HttpsConnection.HttpsConnectionException e1) {
                    e1.printStackTrace();
                    setError(e.getStatusCode(), e);
                }
            }
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


    /***
     * Verifiy credential of the authenticated user *synchronously*
     * @return Account
     */
    private HashMap<String, String> refreshToken(String client_id, String client_secret, String refresh_token)  {
        account = new Account();
        HashMap<String, String> params = new HashMap<>();
        HashMap<String, String> newValues = new HashMap<>();
        params.put("grant_type", "refresh_token");
        params.put("client_id", client_id);
        params.put("client_secret", client_secret);
        params.put("refresh_token", refresh_token);
        try {
            String response = new HttpsConnection(context, this.instance).post(getAbsoluteUrl("/oauth/token"), 10, params, null);
            JSONObject resobj = new JSONObject(response);
            String token = resobj.get("access_token").toString();
            if( resobj.has("refresh_token"))
                refresh_token = resobj.get("refresh_token").toString();
            newValues.put("access_token",token);
            newValues.put("refresh_token",refresh_token);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (HttpsConnection.HttpsConnectionException e) {
            e.printStackTrace();
        }
        return newValues;
    }

    public APIResponse createAccount(AccountCreation accountCreation){
        apiResponse = new APIResponse();

        try {
            HashMap<String, String> params = new HashMap<>();
            params.put(Helper.CLIENT_NAME, Helper.CLIENT_NAME_VALUE);
            params.put(Helper.REDIRECT_URIS, Helper.REDIRECT_CONTENT);
            params.put(Helper.SCOPES, Helper.OAUTH_SCOPES);
            params.put(Helper.WEBSITE, Helper.WEBSITE_VALUE);
            String response = new HttpsConnection(context, this.instance).post(getAbsoluteUrl("/apps"), 30, params, null);
            JSONObject resobj = new JSONObject(response);
            String client_id = resobj.getString("client_id");
            String client_secret = resobj.getString("client_secret");

            params = new HashMap<>();
            params.put("grant_type", "client_credentials");
            params.put("client_id", client_id);
            params.put("client_secret", client_secret);
            params.put("scope", "read write");
            response = new HttpsConnection(context, this.instance).post("https://" + this.instance + "/oauth/token", 30, params, null);
            JSONObject res = new JSONObject(response);
            String app_token = res.getString("access_token");
            params = new HashMap<>();
            params.put("username", accountCreation.getUsername());
            params.put("email", accountCreation.getEmail());
            params.put("password", accountCreation.getPassword());
            params.put("agreement", "true");
            params.put("locale", Locale.getDefault().getLanguage());
            response = new HttpsConnection(context, this.instance).post(getAbsoluteUrl("/accounts"), 10, params, app_token);

            /*res = new JSONObject(response);
            String access_token = res.getString("access_token");
            prefKeyOauthTokenT = access_token;

            response = new HttpsConnection(context, this.instance).get(getAbsoluteUrl("/accounts/verify_credentials"), 10, null, prefKeyOauthTokenT);
            account = parseAccountResponse(context, new JSONObject(response));
            if( account.getSocial().equals("PLEROMA")){
                isPleromaAdmin(account.getAcct());
            }
            SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            account.setToken(access_token);
            account.setClient_id(client_id);
            account.setClient_secret(client_secret);
            account.setRefresh_token(null);
            account.setInstance(instance);
            SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
            boolean userExists = new AccountDAO(context, db).userExist(account);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(Helper.PREF_KEY_ID, account.getId());
            editor.putBoolean(Helper.PREF_IS_MODERATOR, account.isModerator());
            editor.putBoolean(Helper.PREF_IS_ADMINISTRATOR, account.isAdmin());
            editor.putString(Helper.PREF_INSTANCE, instance);
            editor.apply();
            if( userExists)
                new AccountDAO(context, db).updateAccountCredential(account);
            else {
                if( account.getUsername() != null && account.getCreated_at() != null)
                    new AccountDAO(context, db).insertAccount(account);
            }*/
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (HttpsConnection.HttpsConnectionException e) {
            setError(e.getStatusCode(), e);
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return apiResponse;
    }

    /**
     * Returns an account
     * @param accountId String account fetched
     * @return Account entity
     */
    public Account getAccount(String accountId) {

        account = new Account();
        try {
            String response = new HttpsConnection(context, this.instance).get(getAbsoluteUrl(String.format("/accounts/%s",accountId)), 10, null, prefKeyOauthTokenT);
            account = parseAccountResponse(context, new JSONObject(response));
            final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
            if( account.getSocial().equals("PLEROMA") && accountId.equals(userId)){
                isPleromaAdmin(account.getAcct());
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
            String response = new HttpsConnection(context, this.instance).get(getAbsoluteUrl("/accounts/relationships"), 10, params, prefKeyOauthTokenT);
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
            List<Relationship> relationships = new ArrayList<>();
            try {
                HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
                String response = httpsConnection.get(getAbsoluteUrl("/accounts/relationships"), 10, params, prefKeyOauthTokenT);
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
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String tag = sharedpreferences.getString(Helper.SET_FEATURED_TAG_ACTION, null);
        if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON && tag != null){
            params.put("tagged", tag.toLowerCase());
        }
        statuses = new ArrayList<>();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = httpsConnection.get(getAbsoluteUrl(String.format("/accounts/%s/statuses", accountId)), 10, params, prefKeyOauthTokenT);
            statuses = parseStatuses(context, new JSONArray(response));
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
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = httpsConnection.get(getAbsoluteUrl(String.format("/statuses/%s/reblogged_by", statusId)), 10, params, prefKeyOauthTokenT);
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
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = httpsConnection.get(getAbsoluteUrl(String.format("/statuses/%s/favourited_by", statusId)), 10, params, prefKeyOauthTokenT);
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
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = httpsConnection.get(getAbsoluteUrl(String.format("/statuses/%s", statusId)), 10, null, prefKeyOauthTokenT);
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
     * Retrieves one status *synchronously*
     *
     * @param statusId  String Id of the status
     * @return APIResponse
     */
    public APIResponse getStatusbyIdAndCache(String statusId) {
        statuses = new ArrayList<>();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = httpsConnection.get(getAbsoluteUrl(String.format("/statuses/%s", statusId)), 10, null, prefKeyOauthTokenT);
            Status status = parseStatuses(context, new JSONObject(response));
            SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
            Account account = new AccountDAO(context, db).getAccountByToken(prefKeyOauthTokenT);
            new TimelineCacheDAO(context, db).update(status.getId(), response, account.getId(), account.getInstance());
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
    public app.fedilab.android.client.Entities.Context getStatusContext(String statusId) {
        app.fedilab.android.client.Entities.Context statusContext = new app.fedilab.android.client.Entities.Context();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = httpsConnection.get(getAbsoluteUrl(String.format("/statuses/%s/context", statusId)), 10, null, prefKeyOauthTokenT);
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
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = httpsConnection.get(getAbsoluteUrl("/conversations"), 10, params, prefKeyOauthTokenT);
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
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = httpsConnection.get(getAbsoluteUrl("/timelines/direct"), 10, params, prefKeyOauthTokenT);
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
     * Retrieves home timeline from cache the account *synchronously*
     * @param max_id   String id max
     * @return APIResponse
     */
    public APIResponse getHomeTimelineCache(String max_id) {

        SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        statuses  = new TimelineCacheDAO(context, db).get(max_id);

        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        boolean remember_position_home = sharedpreferences.getBoolean(Helper.SET_REMEMBER_POSITION_HOME, true);
        if( remember_position_home ){
            if( statuses != null){
                Iterator<Status> i = statuses.iterator();
                List<String> ids = new ArrayList<>();
                while (i.hasNext()) {
                    Status s = i.next();
                    if( ids.contains(s.getId())) {
                        i.remove();
                        new TimelineCacheDAO(context, db).remove(s.getId());
                    }else{
                        ids.add(s.getId());
                    }
                }
            }
            if( statuses == null){
                return getHomeTimeline(max_id);
            }else{
                if( statuses.size() > 0) {
                    if( statuses.get(0).getId().matches("\\d+")){
                        apiResponse.setSince_id(String.valueOf(Long.parseLong(statuses.get(0).getId())+1));
                        apiResponse.setMax_id(String.valueOf(Long.parseLong(statuses.get(statuses.size() - 1).getId())-1));
                    }else{
                        apiResponse.setSince_id(statuses.get(0).getId());
                        apiResponse.setMax_id(statuses.get(statuses.size() - 1).getId());
                    }
                }
                apiResponse.setStatuses(statuses);
                return apiResponse;
            }
        }else{
            return getHomeTimeline(max_id);
        }

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
        params.put("limit",String.valueOf(limit));
        statuses = new ArrayList<>();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = httpsConnection.get(getAbsoluteUrl("/timelines/home"), 10, params, prefKeyOauthTokenT);
            apiResponse.setSince_id(httpsConnection.getSince_id());
            apiResponse.setMax_id(httpsConnection.getMax_id());
            statuses = parseStatusesForCache(context, new JSONArray(response));
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
        if( apiResponse == null)
            apiResponse = new APIResponse();
        apiResponse.setStatuses(statuses);
        return apiResponse;
    }


    /**
     * Retrieves public GNU timeline for the account *synchronously*
     * @param max_id   String id max
     * @return APIResponse
     */
    public APIResponse getGNUTimeline(String remoteInstance, String max_id) {

        HashMap<String, String> params = new HashMap<>();
        if (max_id != null)
            params.put("max_id", max_id);
        statuses = new ArrayList<>();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = httpsConnection.get("https://"+remoteInstance+"/api/statuses/public_timeline.json", 10, params, prefKeyOauthTokenT);
            statuses = GNUAPI.parseStatuses(context, new JSONArray(response));
            if( statuses.size() > 0) {
                apiResponse.setSince_id(String.valueOf(Long.parseLong(statuses.get(0).getId())+1));
                apiResponse.setMax_id(String.valueOf(Long.parseLong(statuses.get(statuses.size() - 1).getId())-1));
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (HttpsConnection.HttpsConnectionException e) {
            e.printStackTrace();
        }
        apiResponse.setStatuses(statuses);
        return apiResponse;
    }


    /**
     * Retrieves public pixelfed timeline for the account *synchronously*
     * @param max_id   String id max
     * @return APIResponse
     */
    public APIResponse getPixelfedTimeline(String remoteInstance, String max_id) {

        HashMap<String, String> params = new HashMap<>();
        if (max_id != null)
            params.put("page", max_id);
        statuses = new ArrayList<>();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = httpsConnection.get(getAbsoluteUrlRemote(remoteInstance, "/timelines/public/"), 10, params, prefKeyOauthTokenT);
            apiResponse.setSince_id(httpsConnection.getSince_id());
            apiResponse.setMax_id(httpsConnection.getMax_id());
            statuses = parseStatuses(context, new JSONArray(response));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (HttpsConnection.HttpsConnectionException e) {
            e.printStackTrace();
        }
        apiResponse.setStatuses(statuses);
        return apiResponse;
    }
    /**
     * Retrieves Peertube videos from an instance *synchronously*
     * @return APIResponse
     */
    public APIResponse getPeertubeChannel(String instance, String name) {

        List<Account> accounts = new ArrayList<>();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = httpsConnection.get(String.format("https://"+instance+"/api/v1/accounts/%s/video-channels", name), 10, null, null);
            JSONArray jsonArray = new JSONObject(response).getJSONArray("data");
            accounts = parseAccountResponsePeertube(context, instance, jsonArray);
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
     * Retrieves Peertube videos from an instance *synchronously*
     * @return APIResponse
     */
    public APIResponse getPeertubeChannelVideos(String instance, String name) {

        List<Peertube> peertubes = new ArrayList<>();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = httpsConnection.get(String.format("https://"+instance+"/api/v1/video-channels/%s/videos", name), 10, null, null);
            JSONArray jsonArray = new JSONObject(response).getJSONArray("data");
            peertubes = parsePeertube(instance, jsonArray);
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
        params.put("filter","local");
        params.put("sort","-publishedAt");
        params.put("count", "20");

        try {
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = httpsConnection.get("https://"+instance+"/api/v1/videos", 10, params, null);
            JSONArray jsonArray = new JSONObject(response).getJSONArray("data");
            peertubes = parsePeertube(instance, jsonArray);
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
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = httpsConnection.get(String.format("https://"+instance+"/api/v1/videos/%s", videoId), 10, null, null);
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
        if( query == null)
            return null;
        try {
            params.put("search", URLEncoder.encode(query, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            params.put("search", query);
        }

        List<Peertube> peertubes = new ArrayList<>();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = httpsConnection.get("https://"+instance+"/api/v1/search/videos", 10, params, null);
            JSONArray jsonArray = new JSONObject(response).getJSONArray("data");
            peertubes = parsePeertube(instance, jsonArray);
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
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = httpsConnection.get(String.format("https://"+instance+"/api/v1/videos/%s/comment-threads", videoId), 10, null, null);
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
     * Retrieves home timeline for the account *synchronously*
     * @return APIResponse
     */
    public APIResponse getHowTo() {

        List<HowToVideo> howToVideos = new ArrayList<>();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = httpsConnection.get("https://peertube.fedilab.app/api/v1/video-channels/fedilab_channel/videos", 10, null, null);
            JSONArray jsonArray = new JSONObject(response).getJSONArray("data");
            howToVideos = parseHowTos(jsonArray);
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
        apiResponse.setHowToVideos(howToVideos);
        return apiResponse;
    }


    /**
     * Retrieves Peertube videos from an instance *synchronously*
     * @return APIResponse
     */
    public APIResponse getMisskey(String instance, String max_id) {

        JSONObject params = new JSONObject();
        try {
            params.put("file", false);
            if( max_id != null)
                params.put("untilId",max_id);
            params.put("local",true);
            params.put("poll",false);
            params.put("renote",false);
            params.put("reply",false);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            statuses = new ArrayList<>();
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = httpsConnection.postMisskey("https://"+instance+"/api/notes", 10, params, null);
            statuses = parseNotes(context, instance, new JSONArray(response));
            if( statuses != null && statuses.size() > 0){
                apiResponse.setSince_id(statuses.get(0).getId());
                apiResponse.setMax_id(statuses.get(statuses.size() -1).getId());
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
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String url;
            if( instanceName == null)
                url = getAbsoluteUrl("/timelines/public");
            else
                url = getAbsoluteUrlRemoteInstance(instanceName);
            String response = httpsConnection.get(url, 10, params, prefKeyOauthTokenT);
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
     * Retrieves news coming from Fedilab's account *synchronously*
     * @param max_id String id max
     * @return APIResponse
     */
    public APIResponse getNews(String max_id){

        HashMap<String, String> params = null;
        if (max_id != null) {
            params = new HashMap<>();
            params.put("max_id", max_id);
        }

        statuses = new ArrayList<>();
        apiResponse = new APIResponse();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = httpsConnection.get("https://framapiaf.org/api/v1/timelines/tag/fedilab", 10, params, prefKeyOauthTokenT);
            apiResponse.setSince_id(httpsConnection.getSince_id());
            apiResponse.setMax_id(httpsConnection.getMax_id());
            List<Status> tmp_status = parseStatuses(context, new JSONArray(response));
            if( tmp_status != null && tmp_status.size() > 0){
                for(Status status: tmp_status){
                    if( status.getAccount().getAcct().equals("fedilab")){
                        statuses.add(status);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (HttpsConnection.HttpsConnectionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        apiResponse.setStatuses(statuses);
        return apiResponse;
    }

    /**
     * Retrieves discover timeline for the account *synchronously*
     * @param local boolean only local timeline
     * @param max_id String id max
     * @return APIResponse
     */
    public APIResponse getDiscoverTimeline(boolean local, String max_id){
        return getDiscoverTimeline(local, max_id, null, tootPerPage);
    }


    /**
     * Retrieves discover timeline for the account *synchronously*
     * @param local boolean only local timeline
     * @param max_id String id max
     * @param since_id String since the id
     * @param limit int limit  - max value 40
     * @return APIResponse
     */
    private APIResponse getDiscoverTimeline(boolean local, String max_id, String since_id, int limit){

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
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String url;
            url = getAbsoluteUr2l("/discover/posts");
            String response = httpsConnection.get(url, 10, params, prefKeyOauthTokenT);
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
        APIResponse apiResponse = getPublicTimelineTag(tag, local, true, max_id, since_id, tootPerPage, any, all, none, null);
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
        return getPublicTimelineTag(tag, local, false, max_id, null, tootPerPage, any, all, none, null);
    }

    /**
     * Retrieves public tag timeline *synchronously*
     * @param tag String
     * @param local boolean only local timeline
     * @param max_id String id max
     * @return APIResponse
     */
    @SuppressWarnings("SameParameterValue")
    public APIResponse getPublicTimelineTag(String tag, boolean local, String max_id, String instance){
        return getPublicTimelineTag(tag, local, false, max_id, null, tootPerPage, null, null, null, instance);
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
        return getPublicTimelineTag(tag, local, false, null, since_id, tootPerPage, any, all, none, null);
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
    private APIResponse getPublicTimelineTag(String tag, boolean local, boolean onlymedia, String max_id, String since_id, int limit, List<String> any, List<String> all, List<String> none, String instance){

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
            for (String a : any) {
                try {
                    a = URLEncoder.encode(a, "UTF-8");
                } catch (UnsupportedEncodingException ignored) {}
                parameters.append("any[]=").append(a).append("&");
            }
            parameters = new StringBuilder(parameters.substring(0, parameters.length() - 1).substring(6));
            params.put("any[]", parameters.toString());
        }
        if( all != null && all.size() > 0) {
            StringBuilder parameters = new StringBuilder();
            for (String a : all) {
                try {
                    a = URLEncoder.encode(a, "UTF-8");
                } catch (UnsupportedEncodingException ignored) {}
                parameters.append("all[]=").append(a).append("&");
            }
            parameters = new StringBuilder(parameters.substring(0, parameters.length() - 1).substring(6));
            params.put("all[]", parameters.toString());
        }
        if( none != null && none.size() > 0) {
            StringBuilder parameters = new StringBuilder();
            for (String a : none) {
                try {
                    a = URLEncoder.encode(a, "UTF-8");
                } catch (UnsupportedEncodingException ignored) {}
                parameters.append("none[]=").append(a).append("&");
            }
            parameters = new StringBuilder(parameters.substring(0, parameters.length() - 1).substring(7));
            params.put("none[]", parameters.toString());
        }
        params.put("limit",String.valueOf(limit));
        statuses = new ArrayList<>();
        if( tag == null)
            return null;
        try {
            String query = tag.trim();
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            if( MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE)
                try {
                    query = URLEncoder.encode(query, "UTF-8");
                } catch (UnsupportedEncodingException ignored) {}
            String response;
            if( instance == null)
                response = httpsConnection.get(getAbsoluteUrl(String.format("/timelines/tag/%s",query)), 10, params, prefKeyOauthTokenT);
            else
                response = httpsConnection.get(getAbsoluteUrlRemote(instance, String.format("/timelines/tag/%s",query)), 10, params, null);
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
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = httpsConnection.get(getAbsoluteUrl(action), 10, params, prefKeyOauthTokenT);
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
     * Retrieves opencollective accounts *synchronously*
     * @return APIResponse
     */
    public Results getOpencollectiveAccounts(RetrieveOpenCollectiveAsyncTask.Type type){

        results = new Results();
        accounts = new ArrayList<>();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = httpsConnection.get("https://opencollective.com/mastalab/members/all.json", 10, null, prefKeyOauthTokenT);
            accounts = parseOpencollectiveAccountResponse(context, type, new JSONArray(response));
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
        results.setAccounts(accounts);
        return results;
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
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = httpsConnection.get(getAbsoluteUrl("/domain_blocks"), 10, params, prefKeyOauthTokenT);
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
            httpsConnection = new HttpsConnection(context, this.instance);
            httpsConnection.delete(getAbsoluteUrl("/domain_blocks"), 10, params, prefKeyOauthTokenT);
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
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = httpsConnection.get(getAbsoluteUrl("/follow_requests"), 10, params, prefKeyOauthTokenT);
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
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = httpsConnection.get(getAbsoluteUrl("/favourites"), 10, params, prefKeyOauthTokenT);
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
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            httpsConnection.post(getAbsoluteUrl(String.format("/accounts/%s/mute", targetedId)), 10, params, prefKeyOauthTokenT);
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
                action = "/domain_blocks";
                params = new HashMap<>();
                params.put("domain", targetedId);
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
            case MUTE_CONVERSATION:
                action = String.format("/statuses/%s/mute", targetedId);
                break;
            case UNMUTE_CONVERSATION:
                action = String.format("/statuses/%s/unmute", targetedId);
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
                if( status.getScheduled_at() != null)
                    params.put("scheduled_at", status.getScheduled_at());
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
                HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
                String resp = httpsConnection.post(getAbsoluteUrl(action), 10, params, prefKeyOauthTokenT);
                actionCode = httpsConnection.getActionCode();
                if( statusAction == StatusAction.REBLOG || statusAction == StatusAction.UNREBLOG || statusAction == StatusAction.FAVOURITE || statusAction == StatusAction.UNFAVOURITE) {
                    Bundle b = new Bundle();
                    try {
                        Status status1 = parseStatuses(context, new JSONObject(resp));
                        b.putParcelable("status", status1);
                    } catch (JSONException ignored) {}
                    Intent intentBC = new Intent(Helper.RECEIVE_ACTION);
                    intentBC.putExtras(b);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intentBC);
                    SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                    Account account = new AccountDAO(context, db).getAccountByToken(prefKeyOauthTokenT);
                    Status indb = new TimelineCacheDAO(context, db).getSingle(targetedId);
                    if( indb != null) {
                        String response = httpsConnection.get(getAbsoluteUrl(String.format("/statuses/%s", targetedId)), 10, null, prefKeyOauthTokenT);
                        new TimelineCacheDAO(context, db).update(targetedId, response, account.getId(), account.getInstance());
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
            }
        }else{
            try {
                HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
                httpsConnection.delete(getAbsoluteUrl(action), 10, null, prefKeyOauthTokenT);
                actionCode = httpsConnection.getActionCode();
                SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                new TimelineCacheDAO(context, db).remove(targetedId);
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
     * scheduled action for a status
     * @param status Status object related to the status
     * @return APIResponse
     */
    public APIResponse scheduledAction(String call, Status status, String max_id, String targetedId){

        HashMap<String, String> params = new HashMap<>();
        if( call.equals("PUT")){
            if( status.getScheduled_at() != null)
                params.put("scheduled_at", status.getScheduled_at());
        }else if(call.equals("GET")){
            if( max_id != null )
                params.put("max_id", max_id);
        }
        List<StoredStatus> storedStatus = new ArrayList<>();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = null;
            int responseCode = -1;
            if( call.equals("GET"))
                response = httpsConnection.get(getAbsoluteUrl("/scheduled_statuses/"), 10, null, prefKeyOauthTokenT);
            else if( call.equals("PUT"))
                response = httpsConnection.put(getAbsoluteUrl(String.format("/scheduled_statuses/%s", targetedId)), 10, params, prefKeyOauthTokenT);
            else if( call.equals("DELETE"))
                responseCode = httpsConnection.delete(getAbsoluteUrl(String.format("/scheduled_statuses/%s",targetedId)), 10, null, prefKeyOauthTokenT);
            if(call.equals("GET")) {
                apiResponse.setSince_id(httpsConnection.getSince_id());
                apiResponse.setMax_id(httpsConnection.getMax_id());
            }
            if (response != null && call.equals("PUT")) {
                Schedule schedule = parseSimpleSchedule(context, new JSONObject(response));
                StoredStatus st = new StoredStatus();
                st.setCreation_date(status.getCreated_at());
                st.setId(-1);
                st.setJobId(-1);
                st.setScheduled_date(schedule.getScheduled_at());
                st.setStatusReply(null);
                st.setSent_date(null);
                final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
                st.setUserId(userId);
                st.setStatus(schedule.getStatus());
                storedStatus.add(st);
            }else if (response != null && call.equals("GET")) {
                List<Schedule> scheduleList = parseSchedule(context, new JSONArray(response));
                SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
                for(Schedule schedule: scheduleList){
                    StoredStatus st = new StoredStatus();
                    st.setCreation_date(null);
                    st.setScheduledServerdId(schedule.getId());
                    st.setJobId(-1);
                    st.setScheduled_date(schedule.getScheduled_at());
                    st.setStatusReply(null);
                    st.setSent_date(null);
                    st.setUserId(userId);
                    st.setStatus(schedule.getStatus());
                    storedStatus.add(st);
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
        apiResponse.setStoredStatuses(storedStatus);
        return apiResponse;
    }


    /**
     * Public api call to submit a vote
     * @param pollId
     * @param choices
     * @return
     */
    public Poll submiteVote(String pollId, int[] choices){
        JsonObject jsonObject = new JsonObject();
        JsonArray jchoices = new JsonArray();
        for(int choice : choices){
            jchoices.add(choice);
        }
        jsonObject.add("choices",jchoices);
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = httpsConnection.postJson(getAbsoluteUrl(String.format("/polls/%s/votes", pollId)), 10, jsonObject, prefKeyOauthTokenT);
            return parsePoll(context, new JSONObject(response));
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
        return null;
    }

    /**
     * Public api call to refresh a poll
     * @param status Status
     * @return Poll
     */
    public Poll getPoll(Status status){
        try {
            Poll _p = (status.getReblog() != null)?status.getReblog().getPoll():status.getPoll();
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = httpsConnection.get(getAbsoluteUrl(String.format("/polls/%s", _p.getId())), 10, null, prefKeyOauthTokenT);
            Poll poll = parsePoll(context, new JSONObject(response));
            Bundle b = new Bundle();
            status.setPoll(poll);
            b.putParcelable("status", status);
            Intent intentBC = new Intent(Helper.RECEIVE_ACTION);
            intentBC.putExtras(b);
            SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
            Status alreadyCached = new TimelineCacheDAO(context, db).getSingle(status.getId());
            Account account = new AccountDAO(context, db).getAccountByToken(prefKeyOauthTokenT);
            if (alreadyCached != null) {
                new TimelineCacheDAO(context, db).update(status.getId(), response, account.getId(), account.getInstance());
            }
            LocalBroadcastManager.getInstance(context).sendBroadcast(intentBC);
            return parsePoll(context, new JSONObject(response));
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
        return null;
    }


    /**
     * Posts a status
     * @param status Status object related to the status
     * @return APIResponse
     */
    public APIResponse postStatusAction(Status status){

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("status", status.getContent());
        if( status.getContentType() != null)
            jsonObject.addProperty("content_type", status.getContentType());
        if( status.getIn_reply_to_id() != null)
            jsonObject.addProperty("in_reply_to_id", status.getIn_reply_to_id());
        if( status.getMedia_attachments() != null && status.getMedia_attachments().size() > 0 ) {
            JsonArray mediaArray = new JsonArray();
            for(Attachment attachment: status.getMedia_attachments())
                mediaArray.add(attachment.getId());
            jsonObject.add("media_ids", mediaArray);
        }
        if( status.getScheduled_at() != null)
            jsonObject.addProperty("scheduled_at", status.getScheduled_at());
        if( status.isSensitive())
            jsonObject.addProperty("sensitive", Boolean.toString(status.isSensitive()));
        if( status.getSpoiler_text() != null)
            jsonObject.addProperty("spoiler_text", status.getSpoiler_text());
        if( status.getPoll() != null){
            JsonObject poll = new JsonObject();
            JsonArray options = new JsonArray();
            for(PollOptions option: status.getPoll().getOptionsList()){
                if( !option.getTitle().isEmpty())
                    options.add(option.getTitle());
            }
            poll.add("options",options);
            poll.addProperty("expires_in",status.getPoll().getExpires_in());
            poll.addProperty("multiple",status.getPoll().isMultiple());
            jsonObject.add("poll", poll);
        }
        jsonObject.addProperty("visibility", status.getVisibility());
        statuses = new ArrayList<>();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = httpsConnection.postJson(getAbsoluteUrl("/statuses"), 10, jsonObject, prefKeyOauthTokenT);
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
            new HttpsConnection(context, this.instance).post(getAbsoluteUrl(action), 10, params, prefKeyOauthTokenT);
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
        if( max_id != null )
            params.put("max_id", max_id);
        if( since_id != null )
            params.put("since_id", since_id);
        if( 0 > limit || limit > 30)
            limit = 30;
        params.put("limit",String.valueOf(limit));

        if( context == null){
            apiResponse = new APIResponse();
            Error error = new Error();
            apiResponse.setError(error);
            return apiResponse;
        }
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        boolean notif_follow, notif_add, notif_mention, notif_share, notif_poll;
        StringBuilder parameters = new StringBuilder();
        if( type == DisplayNotificationsFragment.Type.ALL){
            if( display) {
                notif_follow = sharedpreferences.getBoolean(Helper.SET_NOTIF_FOLLOW_FILTER, true);
                notif_add = sharedpreferences.getBoolean(Helper.SET_NOTIF_ADD_FILTER, true);
                notif_mention = sharedpreferences.getBoolean(Helper.SET_NOTIF_MENTION_FILTER, true);
                notif_share = sharedpreferences.getBoolean(Helper.SET_NOTIF_SHARE_FILTER, true);
                notif_poll = sharedpreferences.getBoolean(Helper.SET_NOTIF_POLL_FILTER, true);
            }else{
                notif_follow = sharedpreferences.getBoolean(Helper.SET_NOTIF_FOLLOW, true);
                notif_add = sharedpreferences.getBoolean(Helper.SET_NOTIF_ADD, true);
                notif_mention = sharedpreferences.getBoolean(Helper.SET_NOTIF_MENTION, true);
                notif_share = sharedpreferences.getBoolean(Helper.SET_NOTIF_SHARE, true);
                notif_poll = sharedpreferences.getBoolean(Helper.SET_NOTIF_POLL, true);
            }


            if( !notif_follow )
                parameters.append("exclude_types[]=").append("follow").append("&");
            if( !notif_add )
                parameters.append("exclude_types[]=").append("favourite").append("&");
            if( !notif_share )
                parameters.append("exclude_types[]=").append("reblog").append("&");
            if( !notif_mention )
                parameters.append("exclude_types[]=").append("mention").append("&");
            if( !notif_poll )
                parameters.append("exclude_types[]=").append("poll").append("&");
            if( parameters.length() > 0) {
                parameters = new StringBuilder(parameters.substring(0, parameters.length() - 1).substring(16));
                params.put("exclude_types[]", parameters.toString());
            }
        }else if(type == DisplayNotificationsFragment.Type.MENTION){
               parameters.append("exclude_types[]=").append("follow").append("&");
                parameters.append("exclude_types[]=").append("favourite").append("&");
                parameters.append("exclude_types[]=").append("reblog").append("&");
                parameters.append("exclude_types[]=").append("poll").append("&");
                parameters = new StringBuilder(parameters.substring(0, parameters.length() - 1).substring(16));
                params.put("exclude_types[]", parameters.toString());
        }else if(type == DisplayNotificationsFragment.Type.FAVORITE){
            parameters.append("exclude_types[]=").append("follow").append("&");
            parameters.append("exclude_types[]=").append("mention").append("&");
            parameters.append("exclude_types[]=").append("reblog").append("&");
            parameters.append("exclude_types[]=").append("poll").append("&");
            parameters = new StringBuilder(parameters.substring(0, parameters.length() - 1).substring(16));
            params.put("exclude_types[]", parameters.toString());
        }else if(type == DisplayNotificationsFragment.Type.BOOST){
            parameters.append("exclude_types[]=").append("follow").append("&");
            parameters.append("exclude_types[]=").append("mention").append("&");
            parameters.append("exclude_types[]=").append("favourite").append("&");
            parameters.append("exclude_types[]=").append("poll").append("&");
            parameters = new StringBuilder(parameters.substring(0, parameters.length() - 1).substring(16));
            params.put("exclude_types[]", parameters.toString());
        }else if(type == DisplayNotificationsFragment.Type.POLL){
            parameters.append("exclude_types[]=").append("reblog").append("&");
            parameters.append("exclude_types[]=").append("follow").append("&");
            parameters.append("exclude_types[]=").append("mention").append("&");
            parameters.append("exclude_types[]=").append("favourite").append("&");
            parameters = new StringBuilder(parameters.substring(0, parameters.length() - 1).substring(16));
            params.put("exclude_types[]", parameters.toString());
        }else if(type == DisplayNotificationsFragment.Type.FOLLOW){
            parameters.append("exclude_types[]=").append("reblog").append("&");
            parameters.append("exclude_types[]=").append("mention").append("&");
            parameters.append("exclude_types[]=").append("favourite").append("&");
            parameters.append("exclude_types[]=").append("poll").append("&");
            parameters = new StringBuilder(parameters.substring(0, parameters.length() - 1).substring(16));
            params.put("exclude_types[]", parameters.toString());
        }

        List<Notification> notifications = new ArrayList<>();

        try {
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = httpsConnection.get(getAbsoluteUrl("/notifications"), 10, params, prefKeyOauthTokenT);
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
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
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
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = httpsConnection.get(getAbsoluteUrl("/search"), 10, params, prefKeyOauthTokenT);
            results = parseResultsResponse(new JSONObject(response));
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
    public APIResponse search2(String query, searchType type, String offset) {
        apiResponse = new APIResponse();
        HashMap<String, String> params = new HashMap<>();
        if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE)
            params.put("q", query);
        else
            try {
                params.put("q", URLEncoder.encode(query, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                params.put("q", query);
            }
        if( offset != null)
            params.put("offset", offset);
        switch (type){
            case TAGS:
                params.put("type", "hashtags");
                break;
            case ACCOUNTS:
                params.put("type", "accounts");
                break;
            case STATUSES:
                params.put("type", "statuses");
                break;
        }
        params.put("limit", "20");
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = httpsConnection.get(getAbsoluteUr2l("/search"), 10, params, prefKeyOauthTokenT);
            results = parseResultsResponse(new JSONObject(response));
            apiResponse.setSince_id(httpsConnection.getSince_id());
            apiResponse.setMax_id(httpsConnection.getMax_id());
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
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = httpsConnection.get(getAbsoluteUrl("/accounts/search"), 10, params, prefKeyOauthTokenT);
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
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = httpsConnection.get(getAbsoluteUrl("/custom_emojis"), 10, null, prefKeyOauthTokenT);
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
        //Add custom emoji for Pleroma
        if(MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA){
            APIResponse apiResponsePleroma = getCustomPleromaEmoji();
            if( apiResponsePleroma != null && apiResponsePleroma.getEmojis() != null && apiResponsePleroma.getEmojis().size() > 0)
                emojis.addAll(apiResponsePleroma.getEmojis());
        }
        apiResponse.setEmojis(emojis);
        return apiResponse;
    }

    //Pleroma admin calls
    /**
     * Check if it's a Pleroma admin account and change in settings *synchronously*
     */
    private void isPleromaAdmin(String nickname) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        boolean isAdmin;
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = httpsConnection.get(String.format(Helper.getLiveInstanceWithProtocol(context)+"/api/pleroma/admin/permission_group/%s/admin",nickname), 10, null, prefKeyOauthTokenT);
            //Call didn't return a 404, so the account is admin
            isAdmin = true;
        } catch (Exception e) {
            isAdmin = false;
        }
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean(Helper.PREF_IS_ADMINISTRATOR, isAdmin);
        editor.apply();
    }

    /**
     * Retrieves Pleroma emoji *synchronously*
     *
     * @return APIResponse
     */
    public APIResponse getCustomPleromaEmoji() {
        List<Emojis> emojis = new ArrayList<>();
        try {
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = httpsConnection.get(Helper.getLiveInstanceWithProtocol(context)+"/api/pleroma/emoji", 10, null, prefKeyOauthTokenT);
            emojis = parsePleromaEmojis(new JSONObject(response));

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
            String response = new HttpsConnection(context, this.instance).get(getAbsoluteUrl("/filters"), 10, null, prefKeyOauthTokenT);
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
            String response = new HttpsConnection(context, this.instance).get(getAbsoluteUrl(String.format("/filters/%s", filterId)), 10, null, prefKeyOauthTokenT);
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
            String response = new HttpsConnection(context, this.instance).post(getAbsoluteUrl("/filters"), 10, params, prefKeyOauthTokenT);
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
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            httpsConnection.delete(getAbsoluteUrl(String.format("/filters/%s", filter.getId())), 10, null, prefKeyOauthTokenT);
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
            String response = new HttpsConnection(context, this.instance).put(getAbsoluteUrl(String.format("/filters/%s", filter.getId())), 10, params, prefKeyOauthTokenT);
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
        apiResponse = new APIResponse();
        List<app.fedilab.android.client.Entities.List> lists = new ArrayList<>();
        try {
            String response = new HttpsConnection(context, this.instance).get(getAbsoluteUrl("/lists"), 10, null, prefKeyOauthTokenT);
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

        List<app.fedilab.android.client.Entities.List> lists = new ArrayList<>();
        app.fedilab.android.client.Entities.List list;
        try {
            String response = new HttpsConnection(context, this.instance).get(getAbsoluteUrl(String.format("/accounts/%s/lists", userId)), 10, null, prefKeyOauthTokenT);
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
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = httpsConnection.get(getAbsoluteUrl(String.format("/timelines/list/%s",list_id)), 10, params, prefKeyOauthTokenT);
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
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = httpsConnection.get(getAbsoluteUrl(String.format("/lists/%s/accounts", listId)), 10, params, prefKeyOauthTokenT);
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

        List<app.fedilab.android.client.Entities.List> lists = new ArrayList<>();
        app.fedilab.android.client.Entities.List list;
        try {
            String response = new HttpsConnection(context, this.instance).get(getAbsoluteUrl(String.format("/lists/%s",id)), 10, null, prefKeyOauthTokenT);
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
            new HttpsConnection(context, this.instance).post(getAbsoluteUrl(String.format("/lists/%s/accounts", id)), 10, params, prefKeyOauthTokenT);
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
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            StringBuilder parameters = new StringBuilder();
            HashMap<String, String> params = new HashMap<>();
            for(String val: account_ids)
                parameters.append("account_ids[]=").append(val).append("&");
            if( parameters.length() > 0) {
                parameters = new StringBuilder(parameters.substring(0, parameters.length() - 1).substring(14));
                params.put("account_ids[]", parameters.toString());
            }
            httpsConnection.delete(getAbsoluteUrl(String.format("/lists/%s/accounts", id)), 10, params, prefKeyOauthTokenT);
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
        List<app.fedilab.android.client.Entities.List> lists = new ArrayList<>();
        app.fedilab.android.client.Entities.List list;
        try {
            String response = new HttpsConnection(context, this.instance).post(getAbsoluteUrl("/lists"), 10, params, prefKeyOauthTokenT);
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
     * Update a list by its id
     * @param id String, the id of the list
     * @param title String, the title of the list
     * @return APIResponse
     */
    public APIResponse updateList(String id, String title){

        HashMap<String, String> params = new HashMap<>();
        params.put("title",title);
        List<app.fedilab.android.client.Entities.List> lists = new ArrayList<>();
        app.fedilab.android.client.Entities.List list;
        try {
            String response = new HttpsConnection(context, this.instance).put(getAbsoluteUrl(String.format("/lists/%s", id)), 10, params, prefKeyOauthTokenT);
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
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            httpsConnection.delete(getAbsoluteUrl(String.format("/lists/%s", id)), 10, null, prefKeyOauthTokenT);
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
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = httpsConnection.get(getAbsoluteUrlCommunitywiki("/list"), 10, null, prefKeyOauthTokenT);

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
            HttpsConnection httpsConnection = new HttpsConnection(context, this.instance);
            String response = httpsConnection.get(getAbsoluteUrlCommunitywiki(String.format("/list/%s", name)), 10, null, prefKeyOauthTokenT);

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

            card.setType(resobj.get("type").toString());
            try {
                card.setAuthor_name(resobj.get("author_name").toString());
            }catch (Exception e){
                card.setAuthor_name(null);
            }
            try {
                card.setAuthor_url(resobj.get("author_url").toString());
            }catch (Exception e){
                card.setAuthor_url(null);
            }
            try {
                card.setHtml(resobj.get("html").toString());
            }catch (Exception e){
                card.setHtml(null);
            }
            try {
                card.setEmbed_url(resobj.get("embed_url").toString());
            }catch (Exception e){
                card.setEmbed_url(null);
            }
            try {
                card.setProvider_name(resobj.get("provider_name").toString());
            }catch (Exception e){
                card.setProvider_name(null);
            }
            try {
                card.setProvider_url(resobj.get("provider_url").toString());
            }catch (Exception e){
                card.setProvider_url(null);
            }
            try {
                card.setHeight(Integer.parseInt(resobj.get("height").toString()));
            }catch (Exception e){
                card.setHeight(0);
            }
            try {
                card.setWidth(Integer.parseInt(resobj.get("width").toString()));
            }catch (Exception e){
                card.setWidth(0);
            }
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
                list_tmp.add(jsonArray.getJSONObject(i).getString("name"));
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
    private List<Peertube> parsePeertube(String instance, JSONArray jsonArray){

        List<Peertube> peertubes = new ArrayList<>();
        try {
            int i = 0;
            while (i < jsonArray.length() ){
                JSONObject resobj = jsonArray.getJSONObject(i);
                Peertube peertube = parsePeertube(context, instance, resobj);
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
    public static Peertube parsePeertube(Context context, String instance, JSONObject resobj){
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
            peertube.setAccount(parseAccountResponsePeertube(context, instance, resobj.getJSONObject("account")));
            peertube.setInstance(instance);
            peertube.setView(Integer.parseInt(resobj.get("views").toString()));
            peertube.setLike(Integer.parseInt(resobj.get("likes").toString()));
            peertube.setDislike(Integer.parseInt(resobj.get("dislikes").toString()));
            peertube.setDuration(Integer.parseInt(resobj.get("duration").toString()));
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
            peertube.setAccount(parseAccountResponsePeertube(context, instance, resobj.getJSONObject("account")));
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
                    status.setAccount(parseAccountResponsePeertube(context, instance, comment.getJSONObject("account")));
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
     * Parse json response for several scheduled toots
     * @param jsonObject JSONObject
     * @return List<Status>
     */
    private static Schedule parseSimpleSchedule(Context context, JSONObject jsonObject){
        Schedule schedule = new Schedule();
        try {
            JSONObject resobj = jsonObject.getJSONObject("params");
            Status status = parseSchedule(context, resobj);
            List<Attachment> attachements = parseAttachmentResponse(jsonObject.getJSONArray("media_attachments"));
            status.setMedia_attachments((ArrayList<Attachment>) attachements);
            schedule.setStatus(status);
            schedule.setAttachmentList(attachements);
            schedule.setId(jsonObject.get("id").toString());
            schedule.setScheduled_at(Helper.mstStringToDate(context, jsonObject.get("scheduled_at").toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return schedule;
    }

    /**
     * Parse json response for several scheduled toots
     * @param jsonArray JSONArray
     * @return List<Status>
     */
    private static List<Schedule> parseSchedule(Context context, JSONArray jsonArray){

        List<Schedule> schedules = new ArrayList<>();
        try {
            int i = 0;
            while (i < jsonArray.length() ){
                Schedule schedule = new Schedule();
                JSONObject resobj = jsonArray.getJSONObject(i).getJSONObject("params");
                Status status = parseSchedule(context, resobj);
                List<Attachment> attachements = parseAttachmentResponse(jsonArray.getJSONObject(i).getJSONArray("media_attachments"));
                status.setMedia_attachments((ArrayList<Attachment>) attachements);
                schedule.setStatus(status);
                schedule.setAttachmentList(attachements);
                schedules.add(schedule);
                schedule.setId(jsonArray.getJSONObject(i).get("id").toString());
                schedule.setScheduled_at(Helper.mstStringToDate(context, jsonArray.getJSONObject(i).get("scheduled_at").toString()));
                i++;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return schedules;
    }


    /**
     * Parse json response for several status
     * @param jsonArray JSONArray
     * @return List<Status>
     */
    private List<Status> parseStatusesForCache(Context context, JSONArray jsonArray){

        List<Status> statuses = new ArrayList<>();
        try {
            int i = 0;
            while (i < jsonArray.length() ){

                JSONObject resobj = jsonArray.getJSONObject(i);
                Status status = parseStatuses(context, resobj);
                SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                Status alreadyCached = new TimelineCacheDAO(context, db).getSingle(status.getId());
                if (alreadyCached == null) {
                    Account account = new AccountDAO(context, db).getAccountByToken(prefKeyOauthTokenT);
                    new TimelineCacheDAO(context, db).insert(status.getId(), resobj.toString(), account.getId(), account.getInstance());
                }
                i++;
                statuses.add(status);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return statuses;
    }

    /**
     * Parse json response for several status
     * @param jsonArray JSONArray
     * @return List<Status>
     */
    private static List<Status> parseStatuses(Context context, JSONArray jsonArray){

        List<Status> statuses = new ArrayList<>();
        try {
            int i = 0;
            while (i < jsonArray.length() ){

                JSONObject resobj = jsonArray.getJSONObject(i);
                Status status = parseStatuses(context, resobj);
                i++;
                if( status != null) {
                    statuses.add(status);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return statuses;
    }


    /**
     * Parse a poll
     * @param context
     * @param resobj
     * @return
     */
    public static Poll parsePoll(Context context, JSONObject resobj){
        Poll poll = new Poll();
        try {
            poll.setId(resobj.getString("id"));
            poll.setExpires_at(Helper.mstStringToDate(context, resobj.getString("expires_at")));
            poll.setExpired(resobj.getBoolean("expired"));
            poll.setMultiple(resobj.getBoolean("multiple"));
            poll.setVotes_count(resobj.getInt("votes_count"));
            poll.setVoted(resobj.getBoolean("voted"));
            JSONArray options = resobj.getJSONArray("options");
            List<PollOptions> pollOptions = new ArrayList<>();
            for(int i = 0; i < options.length() ; i++){
                JSONObject option = options.getJSONObject(i);
                PollOptions pollOption = new PollOptions();
                pollOption.setTitle(option.getString("title"));
                pollOption.setVotes_count(option.getInt("votes_count"));
                pollOptions.add(pollOption);
            }
            poll.setOptionsList(pollOptions);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return poll;
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
            try {
                status.setLanguage(resobj.get("language").toString());
            }catch (Exception e){status.setLanguage("ja");}
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

            if( resobj.has("poll") && !resobj.isNull("poll")){
                Poll poll = new Poll();
                poll.setId(resobj.getJSONObject("poll").getString("id"));
                try {
                    poll.setExpires_at(Helper.mstStringToDate(context, resobj.getJSONObject("poll").getString("expires_at")));
                }catch (Exception e){
                    poll.setExpires_at(new Date());
                }
                poll.setExpired(resobj.getJSONObject("poll").getBoolean("expired"));
                poll.setMultiple(resobj.getJSONObject("poll").getBoolean("multiple"));
                poll.setVotes_count(resobj.getJSONObject("poll").getInt("votes_count"));
                poll.setVoted(resobj.getJSONObject("poll").getBoolean("voted"));
                JSONArray options = resobj.getJSONObject("poll").getJSONArray("options");
                List<PollOptions> pollOptions = new ArrayList<>();
                for(int i = 0; i < options.length() ; i++){
                    JSONObject option = options.getJSONObject(i);
                    PollOptions pollOption = new PollOptions();
                    pollOption.setTitle(option.getString("title"));
                    pollOption.setVotes_count(option.getInt("votes_count"));
                    pollOptions.add(pollOption);
                }
                poll.setOptionsList(pollOptions);
                status.setPoll(poll);
            }

        } catch (JSONException ignored) {} catch (ParseException e) {
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

            if(resobj.has("poll_limits")){
                HashMap<String, Integer> poll_limits = new HashMap<>();
                JSONObject polllimits = resobj.getJSONObject("poll_limits");
                poll_limits.put("min_expiration",polllimits.getInt("min_expiration"));
                poll_limits.put("max_options",polllimits.getInt("max_options"));
                poll_limits.put("max_option_chars",polllimits.getInt("max_option_chars"));
                poll_limits.put("max_expiration",polllimits.getInt("max_expiration"));
                instance.setPoll_limits(poll_limits);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return instance;
    }



    /**
     * Parse json response for several instance reg
     * @param jsonArray JSONArray
     * @return List<Status>
     */
    public List<InstanceReg> parseInstanceReg(JSONArray jsonArray){

        List<InstanceReg> instanceRegs = new ArrayList<>();
        try {
            int i = 0;
            while (i < jsonArray.length() ){
                JSONObject resobj = jsonArray.getJSONObject(i);
                InstanceReg instanceReg = parseInstanceReg(resobj);
                i++;
                instanceRegs.add(instanceReg);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return instanceRegs;
    }

    /**
     * Parse json response an unique instance for registering
     * @param resobj JSONObject
     * @return InstanceReg
     */
    private InstanceReg parseInstanceReg(JSONObject resobj){
        InstanceReg instanceReg = new InstanceReg();
        try {
            instanceReg.setDomain(resobj.getString("domain"));
            instanceReg.setVersion(resobj.getString("version"));
            instanceReg.setDescription(resobj.getString("description"));
            instanceReg.setLanguage(resobj.getString("language"));
            instanceReg.setCategory(resobj.getString("category"));
            instanceReg.setProxied_thumbnail(resobj.getString("proxied_thumbnail"));
            instanceReg.setTotal_users(resobj.getInt("total_users"));
            instanceReg.setLast_week_users(resobj.getInt("last_week_users"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return instanceReg;
    }



    /**
     * Parse Pleroma emojis
     * @param jsonObject JSONObject
     * @return List<Emojis> of emojis
     */
    private List<Emojis> parsePleromaEmojis(JSONObject jsonObject){
        List<Emojis> emojis = new ArrayList<>();
        Iterator<String> iter = jsonObject.keys();
        while (iter.hasNext()) {
            String shortcode = iter.next();
            try {
                String url = (String) jsonObject.get(shortcode);
                Emojis emojisObj = new Emojis();
                emojisObj.setVisible_in_picker(true);
                emojisObj.setShortcode(shortcode);
                emojisObj.setStatic_url(Helper.getLiveInstanceWithProtocol(context) + url);
                emojisObj.setUrl(Helper.getLiveInstanceWithProtocol(context) + url);
                emojis.add(emojisObj);
            } catch (JSONException ignored) { }
        }
        return emojis;
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
                if( emojis1.isVisible_in_picker())
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
            try {
                emojis.setVisible_in_picker((resobj.getBoolean("visible_in_picker")));
            }catch (Exception e){
                emojis.setVisible_in_picker(true);
            }
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
    private List<app.fedilab.android.client.Entities.List> parseLists(JSONArray jsonArray){
        List<app.fedilab.android.client.Entities.List> lists = new ArrayList<>();
        try {
            int i = 0;
            while (i < jsonArray.length() ) {
                JSONObject resobj = jsonArray.getJSONObject(i);
                app.fedilab.android.client.Entities.List list = parseList(resobj);
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
    private static app.fedilab.android.client.Entities.List parseList(JSONObject resobj){
        app.fedilab.android.client.Entities.List list = new app.fedilab.android.client.Entities.List();
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
                Account account = parseAccountResponsePeertube(context, instance, resobj);
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
     * @param resobj JSONObject
     * @return Account
     */
    private static Account parseAccountResponsePeertube(Context context, String instance, JSONObject resobj){
        Account account = new Account();
        try {
            account.setId(resobj.get("id").toString());
            account.setUsername(resobj.get("name").toString());
            account.setAcct(resobj.get("name").toString() + "@"+ resobj.get("host").toString());
            account.setDisplay_name(resobj.get("displayName").toString());
            account.setHost(resobj.get("host").toString());
            if( resobj.has("createdAt") )
                account.setCreated_at(Helper.mstStringToDate(context, resobj.get("createdAt").toString()));
            else
                account.setCreated_at(new Date());

            if( resobj.has("followersCount") )
                account.setFollowers_count(Integer.valueOf(resobj.get("followersCount").toString()));
            else
                account.setFollowers_count(0);
            if( resobj.has("followingCount"))
                account.setFollowing_count(Integer.valueOf(resobj.get("followingCount").toString()));
            else
                account.setFollowing_count(0);
            account.setStatuses_count(0);
            if( resobj.has("description") )
                account.setNote(resobj.get("description").toString());
            else
                account.setNote("");
            account.setUrl(resobj.get("url").toString());
            account.setSocial("PEERTUBE");
            if( resobj.has("avatar") && !resobj.get("avatar").toString().equals("null")){
                account.setAvatar("https://" + instance + resobj.getJSONObject("avatar").get("path"));
            }else
                account.setAvatar(null);
            account.setAvatar_static(resobj.get("avatar").toString());
        } catch (JSONException ignored) {} catch (ParseException e) {
            e.printStackTrace();
        }
        return account;
    }


    /**
     * Parse json response for list of reports for admins
     * @param jsonArray JSONArray
     * @return List<Report>
     */
    private List<Report> parseReportAdminResponse(JSONArray jsonArray){

        List<Report> reports = new ArrayList<>();
        try {
            int i = 0;
            while (i < jsonArray.length() ) {
                JSONObject resobj = jsonArray.getJSONObject(i);
                Report report = parseReportAdminResponse(context, resobj);
                reports.add(report);
                i++;
            }
        } catch (JSONException e) {
            setDefaultError(e);
        }
        return reports;
    }

    /**
     * Parse json response an unique report for admins
     * @param resobj JSONObject
     * @return AccountAdmin
     */
    private static Report parseReportAdminResponse(Context context, JSONObject resobj){

        Report report = new Report();
        try {
            report.setId(resobj.getString("id"));
            if( !resobj.isNull("action_taken")) {
                report.setAction_taken(resobj.getBoolean("action_taken"));
            }else if( !resobj.isNull("state")) {
                report.setAction_taken(!resobj.getString("state").equals("open"));
            }
            if( !resobj.isNull("comment")) {
                report.setComment(resobj.getString("comment"));
            }else if( !resobj.isNull("content")) {
                report.setComment(resobj.getString("content"));
            }


            report.setCreated_at(Helper.mstStringToDate(context, resobj.getString("created_at")));
            if( !resobj.isNull("updated_at")) {
                report.setUpdated_at(Helper.mstStringToDate(context, resobj.getString("updated_at")));
            }
            if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON){
                if( !resobj.isNull("account")) {
                    report.setAccount(parseAccountAdminResponse(context, resobj.getJSONObject("account")));
                }
                if( !resobj.isNull("target_account")) {
                    report.setTarget_account(parseAccountAdminResponse(context, resobj.getJSONObject("target_account")));
                }
                if( !resobj.isNull("assigned_account")) {
                    report.setAssigned_account(parseAccountAdminResponse(context, resobj.getJSONObject("assigned_account")));
                }
            }else if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA){

                if( !resobj.isNull("account")) {
                    Account account = parseAccountResponse(context, resobj.getJSONObject("account"));
                    AccountAdmin accountAdmin = new AccountAdmin();
                    accountAdmin.setId(account.getId());
                    accountAdmin.setUsername(account.getAcct());
                    accountAdmin.setAccount(account);
                    report.setTarget_account(accountAdmin);
                }

                if( !resobj.isNull("actor")) {
                    Account account = parseAccountResponse(context, resobj.getJSONObject("actor"));
                    AccountAdmin accountAdmin = new AccountAdmin();
                    accountAdmin.setId(account.getId());
                    accountAdmin.setUsername(account.getAcct());
                    accountAdmin.setAccount(account);
                    report.setAccount(accountAdmin);
                }

            }

            if( !resobj.isNull("action_taken_by_account")) {
                report.setAction_taken_by_account(parseAccountAdminResponse(context, resobj.getJSONObject("action_taken_by_account")));
            }
            report.setStatuses(parseStatuses(context, resobj.getJSONArray("statuses")));
        }catch (Exception ignored){ignored.printStackTrace();}
        return report;
    }


    /**
     * Parse json response for list of accounts for admins
     * @param jsonArray JSONArray
     * @return List<AccountAdmin>
     */
    private List<AccountAdmin> parseAccountAdminResponse(JSONArray jsonArray){

        List<AccountAdmin> accountAdmins = new ArrayList<>();
        try {
            int i = 0;
            while (i < jsonArray.length() ) {
                JSONObject resobj = jsonArray.getJSONObject(i);
                AccountAdmin accountAdmin = parseAccountAdminResponse(context, resobj);
                accountAdmins.add(accountAdmin);
                i++;
            }
        } catch (JSONException e) {
            setDefaultError(e);
        }
        return accountAdmins;
    }

    /**
     * Parse json response an unique account for admins
     * @param resobj JSONObject
     * @return Account
     */
    private static AccountAdmin parseAccountAdminResponse(Context context, JSONObject resobj){

        AccountAdmin accountAdmin = new AccountAdmin();
        try {
            accountAdmin.setId(resobj.get("id").toString());
            if( !resobj.isNull("username")) {
                accountAdmin.setUsername(resobj.getString("username"));
            }
            if( !resobj.isNull("nickname")) {
                accountAdmin.setUsername(resobj.getString("nickname"));
            }
            if( !resobj.isNull("created_at")) {
                accountAdmin.setCreated_at(Helper.mstStringToDate(context, resobj.getString("created_at")));
            }
            if( !resobj.isNull("email")) {
                accountAdmin.setEmail(resobj.getString("email"));
            }
            if( !resobj.isNull("role")) {
                accountAdmin.setRole(resobj.getString("role"));
            }
            if( !resobj.isNull("roles")) {
                if(resobj.getJSONObject("roles").getBoolean("admin")){
                    accountAdmin.setRole("admin");
                }else if(resobj.getJSONObject("roles").getBoolean("moderator")){
                    accountAdmin.setRole("moderator");
                }else{
                    accountAdmin.setRole("user");
                }
            }
            if( !resobj.isNull("ip")) {
                accountAdmin.setIp(resobj.getString("ip"));
            }
            if( !resobj.isNull("domain")) {
                accountAdmin.setDomain(resobj.getString("domain"));
            }

            if( !resobj.isNull("account")) {
                accountAdmin.setAccount(parseAccountResponse(context, resobj.getJSONObject("account")));
            }else{
                Account account = new Account();
                account.setId(accountAdmin.getId());
                account.setAcct(accountAdmin.getUsername());
                account.setDisplay_name(accountAdmin.getUsername());
                accountAdmin.setAccount(account);
            }
            if( !resobj.isNull("confirmed")) {
                accountAdmin.setConfirmed(resobj.getBoolean("confirmed"));
            }else{
                accountAdmin.setConfirmed(true);
            }
            if( !resobj.isNull("suspended")) {
                accountAdmin.setSuspended(resobj.getBoolean("suspended"));
            }else{
                accountAdmin.setSuspended(false);
            }
            if( !resobj.isNull("silenced")) {
                accountAdmin.setSilenced(resobj.getBoolean("silenced"));
            }else{
                accountAdmin.setSilenced(false);
            }
            if( !resobj.isNull("disabled")) {
                accountAdmin.setDisabled(resobj.getBoolean("disabled"));
            }else{
                if( !resobj.isNull("deactivated")) {
                    accountAdmin.setDisabled(resobj.getBoolean("deactivated"));
                }else{
                    accountAdmin.setDisabled(false);
                }
            }

            if( !resobj.isNull("approved")) {
                accountAdmin.setApproved(resobj.getBoolean("approved"));
            }else{
                accountAdmin.setApproved(true);
            }
        }catch (Exception ignored){ignored.printStackTrace();}
        return accountAdmin;
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
    private static Account parseAccountResponse(Context context, JSONObject resobj){

        Account account = new Account();
        try {
            account.setId(resobj.get("id").toString());
            account.setUuid(resobj.get("id").toString());
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

            try{
                account.setSocial(resobj.get("software").toString().toUpperCase());
            }catch (Exception ignored){
                account.setSocial("MASTODON");
            }
            try{
                if( resobj.has("pleroma") ) {
                    account.setSocial("PLEROMA");
                    try{
                        account.setModerator(resobj.getJSONObject("pleroma").getBoolean("is_moderator"));
                        account.setAdmin(resobj.getJSONObject("pleroma").getBoolean("is_admin"));
                    }catch (Exception ignored){
                        account.setModerator(false);
                        account.setAdmin(false);
                    }
                }
            }catch (Exception ignored){}

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
            if( resobj.has("source")){
                JSONObject source = resobj.getJSONObject("source");
                try{
                    if( source.has("privacy")) {
                        account.setPrivacy(source.getString("privacy"));
                    }else{
                        account.setPrivacy("public");
                    }
                    if( source.has("sensitive")) {
                        account.setSensitive(source.getBoolean("sensitive"));
                    }else{
                        account.setSensitive(false);
                    }

                }catch (Exception e){
                    account.setPrivacy("public");
                    account.setSensitive(false);
                    e.printStackTrace();
                }
            }
        } catch (JSONException ignored) {} catch (ParseException e) {
            e.printStackTrace();
        }
        return account;
    }


    private List<Account> parseOpencollectiveAccountResponse(Context context, RetrieveOpenCollectiveAsyncTask.Type type, JSONArray jsonArray){
        List<Account> accounts = new ArrayList<>();
        try {
            int i = 0;
            while (i < jsonArray.length() ) {
                JSONObject resobj = jsonArray.getJSONObject(i);
                Account account = parseOpencollectiveAccountResponse(context, type, resobj);
                if( type == RetrieveOpenCollectiveAsyncTask.Type.BACKERS && account.getSocial() != null && account.getSocial().equals("OPENCOLLECTIVE_BACKER"))
                    accounts.add(account);
                else if( type == RetrieveOpenCollectiveAsyncTask.Type.SPONSORS && account.getSocial() != null && account.getSocial().equals("OPENCOLLECTIVE_SPONSOR"))
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
    private static Account parseOpencollectiveAccountResponse(Context context, RetrieveOpenCollectiveAsyncTask.Type type, JSONObject resobj){

        Account account = new Account();
        try {
            account.setId(resobj.get("MemberId").toString());
            account.setUuid(resobj.get("MemberId").toString());
            account.setUsername(resobj.get("name").toString());
            account.setAcct(resobj.get("tier").toString());
            account.setDisplay_name(resobj.get("name").toString());
            account.setLocked(false);
            account.setCreated_at(Helper.opencollectivetStringToDate(context, resobj.get("createdAt").toString()));
            account.setFollowers_count(0);
            account.setFollowing_count(0);
            account.setStatuses_count(0);
            account.setNote(resobj.get("description").toString());
            account.setBot(false);
            account.setMoved_to_account(null);
            account.setUrl(resobj.get("profile").toString());
            account.setAvatar(resobj.get("image").toString());
            account.setAvatar_static(resobj.get("image").toString());
            account.setHeader(null);
            account.setHeader_static(null);
            if(resobj.get("role").toString().equals("BACKER"))
                account.setSocial("OPENCOLLECTIVE_BACKER");
            else if(resobj.get("role").toString().equals("SPONSOR"))
                account.setSocial("OPENCOLLECTIVE_SPONSOR");
            else
                account.setSocial("OPENCOLLECTIVE");

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
            try {
                relationship.setBlocked_by(resobj.getBoolean("blocked_by"));
            }catch (Exception ignored){
                relationship.setBlocked_by(false);
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
     * @return Context
     */
    private app.fedilab.android.client.Entities.Context parseContext(JSONObject jsonObject){

        app.fedilab.android.client.Entities.Context context = new app.fedilab.android.client.Entities.Context();
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
    public static Attachment parseAttachmentResponse(JSONObject resobj){

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
        return Helper.instanceWithProtocol(this.context, this.instance) + "/api/v1" + action;
    }
    private String getAbsoluteUr2l(String action) {
        return Helper.instanceWithProtocol(this.context, this.instance) + "/api/v2" + action;
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
