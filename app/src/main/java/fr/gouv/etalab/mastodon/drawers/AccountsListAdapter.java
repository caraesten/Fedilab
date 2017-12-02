package fr.gouv.etalab.mastodon.drawers;
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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveAccountsAsyncTask;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Error;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnPostActionInterface;
import fr.gouv.etalab.mastodon.activities.ShowAccountActivity;
import fr.gouv.etalab.mastodon.asynctasks.PostActionAsyncTask;

import static fr.gouv.etalab.mastodon.helper.Helper.withSuffix;


/**
 * Created by Thomas on 27/04/2017.
 * Adapter for accounts
 */
public class AccountsListAdapter extends RecyclerView.Adapter implements OnPostActionInterface {

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

    private API.StatusAction doAction;



    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(layoutInflater.inflate(R.layout.drawer_account, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final AccountsListAdapter.ViewHolder holder = (AccountsListAdapter.ViewHolder) viewHolder;
        final Account account = accounts.get(position);


        if( action == RetrieveAccountsAsyncTask.Type.BLOCKED)
            account.setFollowType(Account.followAction.BLOCK);
        else if( action == RetrieveAccountsAsyncTask.Type.MUTED)
            account.setFollowType(Account.followAction.MUTE);

        if (account.getFollowType() == Account.followAction.NOTHING){
            holder.account_follow.setVisibility(View.GONE);
            holder.account_follow_request.setVisibility(View.GONE);
            doAction = null;
        }else if( account.getFollowType() == Account.followAction.REQUEST_SENT){
            holder.account_follow.setVisibility(View.GONE);
            holder.account_follow_request.setVisibility(View.VISIBLE);
            doAction = null;
        }else if( account.getFollowType() == Account.followAction.FOLLOW){
            holder.account_follow.setImageResource(R.drawable.ic_user_times);
            doAction = API.StatusAction.UNFOLLOW;
            holder.account_follow.setVisibility(View.VISIBLE);
            holder.account_follow_request.setVisibility(View.GONE);
        }else if( account.getFollowType() == Account.followAction.NOT_FOLLOW){
            holder.account_follow.setImageResource(R.drawable.ic_user_plus);
            doAction = API.StatusAction.FOLLOW;
            holder.account_follow.setVisibility(View.VISIBLE);
            holder.account_follow_request.setVisibility(View.GONE);
        }else if( account.getFollowType() == Account.followAction.BLOCK){
            holder.account_follow.setImageResource(R.drawable.ic_lock_open);
            doAction = API.StatusAction.UNBLOCK;
            holder.account_follow.setVisibility(View.VISIBLE);
            holder.account_follow_request.setVisibility(View.GONE);
        }else if( account.getFollowType() == Account.followAction.MUTE){
            holder.account_follow.setImageResource(R.drawable.ic_volume_mute);
            doAction = API.StatusAction.UNMUTE;
            holder.account_follow.setVisibility(View.VISIBLE);
            holder.account_follow_request.setVisibility(View.GONE);
        }


        holder.account_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( holder.account_ds.getVisibility() == View.VISIBLE)
                    holder.account_ds.setVisibility(View.GONE);
                else
                    holder.account_ds.setVisibility(View.VISIBLE);
            }
        });
        holder.account_dn.setText(Helper.shortnameToUnicode(account.getDisplay_name(), true));
        holder.account_un.setText(String.format("@%s",account.getUsername()));
        holder.account_ac.setText(account.getAcct());
        if( account.getDisplay_name().equals(account.getAcct()))
            holder.account_ac.setVisibility(View.GONE);
        else
            holder.account_ac.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            holder.account_ds.setText(Html.fromHtml(account.getNote(), Html.FROM_HTML_MODE_LEGACY));
        else
            //noinspection deprecation
            holder.account_ds.setText(Html.fromHtml(account.getNote()));
        holder.account_ds.setAutoLinkMask(Linkify.WEB_URLS);
        holder.account_sc.setText(withSuffix(account.getStatuses_count()));
        holder.account_fgc.setText(withSuffix(account.getFollowing_count()));
        holder.account_frc.setText(withSuffix(account.getFollowers_count()));
        //Profile picture
        Glide.with(holder.account_pp.getContext())
                .load(account.getAvatar())
                .into(holder.account_pp);

        if( account.isMakingAction()){
            holder.account_follow.setEnabled(false);
        }else {
            holder.account_follow.setEnabled(true);
        }
        //Follow button
        holder.account_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( doAction != null) {
                    account.setMakingAction(true);
                    new PostActionAsyncTask(context, doAction, account.getId(), AccountsListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        });
        holder.account_pp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Avoid to reopen details about the current account
                if( targetedId == null || !targetedId.equals(account.getId())){
                    Intent intent = new Intent(context, ShowAccountActivity.class);
                    Bundle b = new Bundle();
                    b.putString("accountId", account.getId());
                    intent.putExtras(b);
                    context.startActivity(intent);
                }

            }
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }



    @Override
    public void onPostAction(int statusCode, API.StatusAction statusAction, String targetedId, Error error) {
        if( error != null){
            final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            boolean show_error_messages = sharedpreferences.getBoolean(Helper.SET_SHOW_ERROR_MESSAGES, true);
            if( show_error_messages)
                Toast.makeText(context, error.getError(),Toast.LENGTH_LONG).show();
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
            for(Account account: accounts){
                if( account.getId().equals(targetedId))
                    account.setFollowType(Account.followAction.FOLLOW);
            }
            accountsListAdapter.notifyDataSetChanged();
        }
        if( statusAction == API.StatusAction.UNFOLLOW){
            for(Account account: accounts){
                if( account.getId().equals(targetedId))
                    account.setFollowType(Account.followAction.NOT_FOLLOW);
            }
            accountsListAdapter.notifyDataSetChanged();
        }
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
        FloatingActionButton account_follow;
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
            account_follow_request = itemView.findViewById(R.id.account_follow_request);
            account_container = itemView.findViewById(R.id.account_container);
        }
    }

}