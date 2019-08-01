package app.fedilab.android.drawers;
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
import android.content.res.ColorStateList;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import app.fedilab.android.activities.GroupActivity;
import app.fedilab.android.client.API;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Error;
import app.fedilab.android.helper.CrossActions;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.sqlite.InstancesDAO;
import app.fedilab.android.sqlite.Sqlite;
import es.dmoral.toasty.Toasty;
import app.fedilab.android.R;
import app.fedilab.android.activities.MainActivity;
import app.fedilab.android.activities.ShowAccountActivity;
import app.fedilab.android.asynctasks.PostActionAsyncTask;
import app.fedilab.android.asynctasks.RetrieveAccountsAsyncTask;
import app.fedilab.android.asynctasks.UpdateAccountInfoAsyncTask;
import app.fedilab.android.interfaces.OnPostActionInterface;
import app.fedilab.android.interfaces.OnRetrieveEmojiAccountInterface;


/**
 * Created by Thomas on 27/04/2017.
 * Adapter for accounts
 */
public class AccountsListAdapter extends RecyclerView.Adapter implements OnPostActionInterface, OnRetrieveEmojiAccountInterface {

    private List<Account> accounts;
    private LayoutInflater layoutInflater;
    private RetrieveAccountsAsyncTask.Type action;
    private Context context;
    private AccountsListAdapter accountsListAdapter;
    private String targetedId;

    public AccountsListAdapter(Context context, RetrieveAccountsAsyncTask.Type action, String targetedId, List<Account> accounts){
        this.context = context;
        this.accounts = accounts;
        layoutInflater = LayoutInflater.from(context);
        this.action = action;
        this.accountsListAdapter = this;
        this.targetedId = targetedId;
    }




    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(layoutInflater.inflate(R.layout.drawer_account, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        final AccountsListAdapter.ViewHolder holder = (AccountsListAdapter.ViewHolder) viewHolder;
        final Account account = accounts.get(position);


        API.StatusAction doAction = null;
        if(MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON || MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA) {
            holder.account_mute_notification.hide();
            if (action == RetrieveAccountsAsyncTask.Type.BLOCKED)
                account.setFollowType(Account.followAction.BLOCK);
            else if (action == RetrieveAccountsAsyncTask.Type.MUTED)
                account.setFollowType(Account.followAction.MUTE);

            if (action == RetrieveAccountsAsyncTask.Type.CHANNELS)
                account.setFollowType(Account.followAction.NOT_FOLLOW);
            holder.account_follow.setBackgroundTintList(ColorStateList.valueOf( ContextCompat.getColor(context, R.color.mastodonC4)));
            if (account.getFollowType() == Account.followAction.NOTHING) {
                holder.account_follow.hide();
                holder.account_follow_request.setVisibility(View.GONE);
                doAction = null;
            } else if (account.getFollowType() == Account.followAction.REQUEST_SENT) {
                holder.account_follow.hide();
                holder.account_follow_request.setVisibility(View.VISIBLE);
                doAction = null;
            } else if (account.getFollowType() == Account.followAction.FOLLOW) {
                holder.account_follow.setBackgroundTintList(ColorStateList.valueOf( ContextCompat.getColor(context, R.color.unfollow)));
                holder.account_follow.setImageResource(R.drawable.ic_user_times);
                doAction = API.StatusAction.UNFOLLOW;
                holder.account_follow.show();
                holder.account_follow_request.setVisibility(View.GONE);
            } else if (account.getFollowType() == Account.followAction.NOT_FOLLOW) {
                holder.account_follow.setImageResource(R.drawable.ic_user_plus);
                doAction = API.StatusAction.FOLLOW;
                holder.account_follow.show();
                holder.account_follow_request.setVisibility(View.GONE);
            } else if (account.getFollowType() == Account.followAction.BLOCK) {
                holder.account_follow.setImageResource(R.drawable.ic_lock_open);
                doAction = API.StatusAction.UNBLOCK;
                holder.account_follow.show();
                holder.account_follow_request.setVisibility(View.GONE);
            } else if (account.getFollowType() == Account.followAction.MUTE) {

                if (account.isMuting_notifications())
                    holder.account_mute_notification.setImageResource(R.drawable.ic_notifications_active);
                else
                    holder.account_mute_notification.setImageResource(R.drawable.ic_notifications_off);

                holder.account_mute_notification.show();
                holder.account_follow.setImageResource(R.drawable.ic_volume_up);
                doAction = API.StatusAction.UNMUTE;
                holder.account_follow.show();
                holder.account_follow_request.setVisibility(View.GONE);
                final int positionFinal = position;
                holder.account_mute_notification.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        account.setMuting_notifications(!account.isMuting_notifications());
                        new PostActionAsyncTask(context, API.StatusAction.MUTE_NOTIFICATIONS, account.getId(), account.isMuting_notifications(), AccountsListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        accountsListAdapter.notifyItemChanged(positionFinal);
                    }
                });
            }


            if (action != RetrieveAccountsAsyncTask.Type.CHANNELS) {
                holder.account_container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (holder.account_info.getVisibility() == View.VISIBLE)
                            holder.account_info.setVisibility(View.GONE);
                        else
                            holder.account_info.setVisibility(View.VISIBLE);
                    }
                });
            } else {
                holder.account_container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
            }
        }else {
            holder.account_follow.hide();
        }

        account.makeAccountNameEmoji(context, AccountsListAdapter.this, account);
        if( account.getdisplayNameSpan() == null || account.getdisplayNameSpan().toString().trim().equals("")) {
            if( account.getDisplay_name() != null && !account.getDisplay_name().trim().equals(""))
                holder.account_dn.setText(Helper.shortnameToUnicode(account.getDisplay_name(), true));
            else
                holder.account_dn.setText(account.getUsername().replace("@",""));
        }else
            holder.account_dn.setText( account.getdisplayNameSpan(), TextView.BufferType.SPANNABLE);
        holder.account_un.setText(String.format("@%s",account.getUsername()));
        holder.account_ac.setText(account.getAcct());
        if( account.getUsername().equals(account.getAcct()))
            holder.account_ac.setVisibility(View.GONE);
        else
            holder.account_ac.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            holder.account_ds.setText(Html.fromHtml(account.getNote(), Html.FROM_HTML_MODE_LEGACY));
        else
            //noinspection deprecation
            holder.account_ds.setText(Html.fromHtml(account.getNote()));
        holder.account_ds.setAutoLinkMask(Linkify.WEB_URLS);
        holder.account_sc.setText(Helper.withSuffix(account.getStatuses_count()));
        holder.account_fgc.setText(Helper.withSuffix(account.getFollowing_count()));
        holder.account_frc.setText(Helper.withSuffix(account.getFollowers_count()));
        //Profile picture
        Helper.loadGiF(context, account.getAvatar_static(), account.getAvatar(), holder.account_pp);
        if( account.isMakingAction()){
            holder.account_follow.setEnabled(false);
        }else {
            holder.account_follow.setEnabled(true);
        }
        //Follow button
        API.StatusAction finalDoAction = doAction;
        holder.account_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( action != RetrieveAccountsAsyncTask.Type.CHANNELS) {
                    if (finalDoAction != null) {
                        account.setMakingAction(true);
                        new PostActionAsyncTask(context, finalDoAction, account.getId(), AccountsListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                }else {
                    CrossActions.followPeertubeChannel(context, account, AccountsListAdapter.this);
                }
            }
        });
        if( action != RetrieveAccountsAsyncTask.Type.GROUPS ) {
            holder.account_pp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE || action != RetrieveAccountsAsyncTask.Type.CHANNELS) {
                        //Avoid to reopen details about the current account
                        if (targetedId == null || !targetedId.equals(account.getId())) {
                            Intent intent = new Intent(context, ShowAccountActivity.class);
                            Bundle b = new Bundle();
                            if (MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE) {
                                b.putBoolean("peertubeaccount", true);
                                b.putBoolean("ischannel", true);
                                b.putString("targetedid", account.getAcct());
                            }
                            b.putParcelable("account", account);
                            intent.putExtras(b);
                            context.startActivity(intent);
                        }
                    } else {
                        CrossActions.doCrossProfile(context, account);
                    }

                }
            });
        }else{
            holder.account_container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, GroupActivity.class);
                    Bundle b = new Bundle();
                    b.putString("groupname", account.getUsername());
                    intent.putExtras(b);
                    context.startActivity(intent);
                }
            });
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }

    private Account getItemAt(int position){
        if( accounts.size() > position)
            return accounts.get(position);
        else
            return null;
    }

    @Override
    public void onPostAction(int statusCode, API.StatusAction statusAction, String targetedId, Error error) {
        if( error != null){
            Toasty.error(context, error.getError(),Toast.LENGTH_LONG).show();
            return;
        }
        Helper.manageMessageStatusCode(context, statusCode, statusAction);
        //When unmuting or unblocking an account, it is removed from the list
        List<Account> accountsToRemove = new ArrayList<>();
        if( statusAction == API.StatusAction.UNMUTE || statusAction == API.StatusAction.UNBLOCK){
            for(Account account: accounts){
                if( account.getId().equals(targetedId))
                    accountsToRemove.add(account);
            }
            accounts.removeAll(accountsToRemove);
            accountsListAdapter.notifyDataSetChanged();
        }
        if( statusAction == API.StatusAction.FOLLOW){
            if( action == RetrieveAccountsAsyncTask.Type.CHANNELS){
                SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                new InstancesDAO(context, db).insertInstance(accounts.get(0).getAcct().split("@")[1], accounts.get(0).getAcct().split("@")[0], "PEERTUBE_CHANNEL");
            }else{
                for(Account account: accounts){
                    if( account.getId().equals(targetedId)) {
                        account.setFollowType(Account.followAction.FOLLOW);
                        account.setMakingAction(false);
                    }
                }
                accountsListAdapter.notifyDataSetChanged();
            }
        }
        if( statusAction == API.StatusAction.UNFOLLOW){
            for(Account account: accounts){
                if( account.getId().equals(targetedId)) {
                    account.setFollowType(Account.followAction.NOT_FOLLOW);
                    account.setMakingAction(false);
                }
            }
            accountsListAdapter.notifyDataSetChanged();
        }
    }

    public void notifyAccountChanged(Account account){
        for (int i = 0; i < accountsListAdapter.getItemCount(); i++) {
            //noinspection ConstantConditions
            if (accountsListAdapter.getItemAt(i) != null && accountsListAdapter.getItemAt(i).getId().equals(account.getId())) {
                try {
                    accountsListAdapter.notifyItemChanged(i);
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    public void onRetrieveEmojiAccount(Account account) {

        notifyAccountChanged(account);
    }


    private class ViewHolder extends RecyclerView.ViewHolder{
        ImageView account_pp;
        TextView account_ac;
        TextView account_dn;
        TextView account_un;
        TextView account_ds;
        TextView account_sc;
        TextView account_fgc;
        TextView account_frc;
        LinearLayout account_info;
        FloatingActionButton account_follow, account_mute_notification;
        TextView account_follow_request;
        LinearLayout account_container;

        ViewHolder(View itemView) {
            super(itemView);
            account_pp = itemView.findViewById(R.id.account_pp);
            account_dn = itemView.findViewById(R.id.account_dn);
            account_ac = itemView.findViewById(R.id.account_ac);
            account_un = itemView.findViewById(R.id.account_un);
            account_ds = itemView.findViewById(R.id.account_ds);
            account_sc = itemView.findViewById(R.id.account_sc);
            account_fgc = itemView.findViewById(R.id.account_fgc);
            account_frc = itemView.findViewById(R.id.account_frc);
            account_follow = itemView.findViewById(R.id.account_follow);
            account_info = itemView.findViewById(R.id.account_info);
            account_mute_notification = itemView.findViewById(R.id.account_mute_notification);
            account_follow_request = itemView.findViewById(R.id.account_follow_request);
            account_container = itemView.findViewById(R.id.account_container);
        }
    }

}