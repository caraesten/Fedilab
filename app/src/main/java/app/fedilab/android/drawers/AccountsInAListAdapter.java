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
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.helper.Helper;
import es.dmoral.toasty.Toasty;
import app.fedilab.android.R;
import app.fedilab.android.activities.ManageAccountsInListActivity;
import app.fedilab.android.activities.ShowAccountActivity;
import app.fedilab.android.asynctasks.ManageListsAsyncTask;
import app.fedilab.android.interfaces.OnListActionInterface;


/**
 * Created by Thomas on 15/12/2017.
 * Adapter for accounts in lists
 */
public class AccountsInAListAdapter extends RecyclerView.Adapter implements OnListActionInterface {

    private List<Account> accounts;
    private LayoutInflater layoutInflater;
    private Context context;
    private AccountsInAListAdapter accountsInAListAdapter;
    private type actionType;
    private String listId;


    public enum type{
        CURRENT,
        SEARCH
    }

    public AccountsInAListAdapter(Context context, type actionType, String listId, List<Account> accounts){
        this.context = context;
        this.accounts = accounts;
        layoutInflater = LayoutInflater.from(context);
        this.accountsInAListAdapter = this;
        this.actionType = actionType;
        this.listId = listId;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(layoutInflater.inflate(R.layout.drawer_account_list, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final AccountsInAListAdapter.ViewHolder holder = (AccountsInAListAdapter.ViewHolder) viewHolder;
        final Account account = accounts.get(position);



        holder.account_un.setText(Helper.shortnameToUnicode(account.getDisplay_name(), true));
        holder.account_ac.setText(account.getAcct());
        if( account.getDisplay_name().equals(account.getAcct()))
            holder.account_ac.setVisibility(View.GONE);
        else
            holder.account_ac.setVisibility(View.VISIBLE);
        //Profile picture
        Helper.loadGiF(context,  account.getAvatar_static(), account.getAvatar(), holder.account_pp);

        if( actionType == type.CURRENT){
            holder.account_action.setImageResource(R.drawable.ic_close);
            holder.account_action.setContentDescription(context.getString(R.string.remove_account));
        }else if(actionType == type.SEARCH){
            holder.account_action.setImageResource(R.drawable.ic_add);
            holder.account_action.setContentDescription(context.getString(R.string.add_account));
        }
        holder.account_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( actionType == type.CURRENT){
                    new ManageListsAsyncTask(context, ManageListsAsyncTask.action.DELETE_USERS, new String[]{account.getId()}, null, listId, null, AccountsInAListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    accounts.remove(account);
                    accountsInAListAdapter.notifyDataSetChanged();
                }else if(actionType == type.SEARCH){
                    new ManageListsAsyncTask(context, ManageListsAsyncTask.action.ADD_USERS, new String[]{account.getId()}, null, listId, null, AccountsInAListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    ((ManageAccountsInListActivity)context).addAccount(account);

                }
            }
        });
        holder.account_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ShowAccountActivity.class);
                Bundle b = new Bundle();
                b.putParcelable("account", account);
                intent.putExtras(b);
                context.startActivity(intent);

            }
        });
    }


    @Override
    public void onActionDone(ManageListsAsyncTask.action actionType, APIResponse apiResponse, int statusCode) {
       if( actionType == ManageListsAsyncTask.action.DELETE_USERS && statusCode != 200){
           Toasty.error(context, context.getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
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




    private class ViewHolder extends RecyclerView.ViewHolder{
        ImageView account_pp;
        TextView account_ac;
        TextView account_un;
        FloatingActionButton  account_action;
        LinearLayout account_container;
        ViewHolder(View itemView) {
            super(itemView);
            account_container = itemView.findViewById(R.id.account_container);
            account_pp = itemView.findViewById(R.id.account_pp);
            account_ac = itemView.findViewById(R.id.account_ac);
            account_un = itemView.findViewById(R.id.account_un);
            account_action = itemView.findViewById(R.id.account_action);
        }
    }

}