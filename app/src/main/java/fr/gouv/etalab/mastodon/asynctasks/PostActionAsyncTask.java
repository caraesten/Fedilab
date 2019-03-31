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
package fr.gouv.etalab.mastodon.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;
import java.util.List;

import fr.gouv.etalab.mastodon.activities.MainActivity;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Error;
import fr.gouv.etalab.mastodon.client.Entities.Results;
import fr.gouv.etalab.mastodon.client.Entities.StoredStatus;
import fr.gouv.etalab.mastodon.client.GNUAPI;
import fr.gouv.etalab.mastodon.client.PeertubeAPI;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnPostActionInterface;


/**
 * Created by Thomas on 29/04/2017.
 * Makes actions for post calls
 */

public class PostActionAsyncTask extends AsyncTask<Void, Void, Void> {

    private OnPostActionInterface listener;
    private int statusCode;
    private API.StatusAction apiAction;
    private String targetedId, targetedComment;
    private String comment;
    private fr.gouv.etalab.mastodon.client.Entities.Status status;
    private Account account, remoteAccount;
    private fr.gouv.etalab.mastodon.client.Entities.Status remoteStatus;
    private WeakReference<Context> contextReference;
    private boolean muteNotifications;
    private Error error;
    private StoredStatus storedStatus;

    public PostActionAsyncTask(Context context, API.StatusAction apiAction, StoredStatus storedStatus, OnPostActionInterface onPostActionInterface){
        this.contextReference = new WeakReference<>(context);
        this.listener = onPostActionInterface;
        this.apiAction = apiAction;
        this.storedStatus = storedStatus;
    }

    public PostActionAsyncTask(Context context, API.StatusAction apiAction, String targetedId, OnPostActionInterface onPostActionInterface){
        this.contextReference = new WeakReference<>(context);
        this.listener = onPostActionInterface;
        this.apiAction = apiAction;
        this.targetedId = targetedId;
    }

    public PostActionAsyncTask(Context context, Account account, API.StatusAction apiAction, String targetedId, OnPostActionInterface onPostActionInterface){
        this.contextReference = new WeakReference<>(context);
        this.listener = onPostActionInterface;
        this.apiAction = apiAction;
        this.targetedId = targetedId;
        this.account = account;
    }

    public PostActionAsyncTask(Context context, Account account, fr.gouv.etalab.mastodon.client.Entities.Status remoteStatus, API.StatusAction apiAction, OnPostActionInterface onPostActionInterface){
        this.contextReference = new WeakReference<>(context);
        this.listener = onPostActionInterface;
        this.apiAction = apiAction;
        this.remoteStatus = remoteStatus;
        this.account = account;
    }

    public PostActionAsyncTask(Context context, Account account, Account remoteAccount, API.StatusAction apiAction, OnPostActionInterface onPostActionInterface){
        this.contextReference = new WeakReference<>(context);
        this.listener = onPostActionInterface;
        this.apiAction = apiAction;
        this.remoteAccount = remoteAccount;
        this.account = account;
    }

    public PostActionAsyncTask(Context context, API.StatusAction apiAction, String targetedId, fr.gouv.etalab.mastodon.client.Entities.Status status, String comment, OnPostActionInterface onPostActionInterface){
        contextReference = new WeakReference<>(context);
        this.listener = onPostActionInterface;
        this.apiAction = apiAction;
        this.targetedId = targetedId;
        this.comment = comment;
        this.status = status;
    }
    public PostActionAsyncTask(Context context, API.StatusAction apiAction, String targetedId, boolean muteNotifications, OnPostActionInterface onPostActionInterface){
        this.contextReference = new WeakReference<>(context);
        this.listener = onPostActionInterface;
        this.apiAction = apiAction;
        this.targetedId = targetedId;
        this.muteNotifications = muteNotifications;
    }


    public PostActionAsyncTask(Context context, String targetedId, String comment, String targetedComment, OnPostActionInterface onPostActionInterface){
        this.contextReference = new WeakReference<>(context);
        this.listener = onPostActionInterface;
        this.apiAction = API.StatusAction.PEERTUBEREPLY;
        this.targetedId = targetedId;
        this.comment = comment;
        this.targetedComment = targetedComment;
    }

    @Override
    protected Void doInBackground(Void... params) {

        if(MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON || MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA || MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PIXELFED) {
            //Remote action
            API api;
            if (account != null)
                api = new API(contextReference.get(), account.getInstance(), account.getToken());
            else
                api = new API(contextReference.get());
            if (remoteStatus != null) {
                String uri;
                if (remoteStatus.getReblog() != null) {
                    if (remoteStatus.getReblog().getUri().startsWith("http"))
                        uri = remoteStatus.getReblog().getUri();
                    else
                        uri = remoteStatus.getReblog().getUrl();
                } else {
                    if (remoteStatus.getUri().startsWith("http"))
                        uri = remoteStatus.getUri();
                    else
                        uri = remoteStatus.getUrl();
                }
                APIResponse search = api.search(uri);
                if (search != null && search.getResults() != null) {
                    List<fr.gouv.etalab.mastodon.client.Entities.Status> remoteStatuses = search.getResults().getStatuses();
                    if (remoteStatuses != null && remoteStatuses.size() > 0) {
                        fr.gouv.etalab.mastodon.client.Entities.Status statusTmp = remoteStatuses.get(0);
                        this.targetedId = statusTmp.getId();
                        statusCode = api.postAction(apiAction, targetedId);
                    }
                }
            } else if (remoteAccount != null) {
                String searchString = remoteAccount.getAcct().contains("@") ? "@" + remoteAccount.getAcct() : "@" + remoteAccount.getAcct() + "@" + Helper.getLiveInstance(contextReference.get());
                APIResponse search = api.search(searchString);
                if (search != null && search.getResults() != null) {
                    List<Account> accounts = search.getResults().getAccounts();
                    if (accounts != null && accounts.size() > 0) {
                        Account accountTmp = accounts.get(0);
                        this.targetedId = accountTmp.getId();
                        statusCode = api.postAction(apiAction, targetedId);
                    }
                }
            } else {
                if (apiAction == API.StatusAction.REPORT)
                    statusCode = api.reportAction(status, comment);
                else if (apiAction == API.StatusAction.CREATESTATUS)
                    statusCode = api.statusAction(status);
                else if(apiAction == API.StatusAction.UPDATESERVERSCHEDULE) {
                    api.scheduledAction("PUT", storedStatus.getStatus(), null, storedStatus.getScheduledServerdId());
                }
                else if(apiAction == API.StatusAction.DELETESCHEDULED) {
                    api.scheduledAction("DELETE", null, null, storedStatus.getScheduledServerdId());
                }else if (apiAction == API.StatusAction.MUTE_NOTIFICATIONS)
                    statusCode = api.muteNotifications(targetedId, muteNotifications);
                else
                    statusCode = api.postAction(apiAction, targetedId);
            }
            error = api.getError();
        }else if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE){
            //Remote action
            PeertubeAPI peertubeAPI;
            if (account != null)
                peertubeAPI = new PeertubeAPI(contextReference.get(), account.getInstance(), account.getToken());
            else
                peertubeAPI = new PeertubeAPI(contextReference.get());

            if( apiAction == API.StatusAction.FOLLOW || apiAction == API.StatusAction.UNFOLLOW)
                statusCode = peertubeAPI.postAction(apiAction, targetedId);
            else if( apiAction == API.StatusAction.RATEVIDEO )
                statusCode = peertubeAPI.postRating(targetedId, comment);
            else if(  apiAction == API.StatusAction.PEERTUBECOMMENT)
                statusCode = peertubeAPI.postComment(targetedId, comment);
            else if(  apiAction == API.StatusAction.PEERTUBEREPLY)
                statusCode = peertubeAPI.postReply(targetedId, comment, targetedComment);
            else if( apiAction == API.StatusAction.PEERTUBEDELETECOMMENT) {
                statusCode = peertubeAPI.deleteComment(targetedId, comment);
                targetedId = comment;
            } else if( apiAction == API.StatusAction.PEERTUBEDELETEVIDEO) {
                statusCode = peertubeAPI.deleteVideo(targetedId);
            }
            error = peertubeAPI.getError();
        }else if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.GNU || MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA){
            GNUAPI gnuapi;
            if (account != null)
                gnuapi = new GNUAPI(contextReference.get(), account.getInstance(), account.getToken());
            else
                gnuapi = new GNUAPI(contextReference.get());
            if (apiAction == API.StatusAction.REPORT)
                statusCode = gnuapi.reportAction(status, comment);
            else if (apiAction == API.StatusAction.CREATESTATUS)
                statusCode = gnuapi.statusAction(status);
            else if (apiAction == API.StatusAction.MUTE_NOTIFICATIONS)
                statusCode = gnuapi.muteNotifications(targetedId, muteNotifications);
            else
                statusCode = gnuapi.postAction(apiAction, targetedId);
            error = gnuapi.getError();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onPostAction(statusCode, apiAction, targetedId, error);
    }

}
