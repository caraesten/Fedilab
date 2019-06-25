package app.fedilab.android.drawers;
/* Copyright 2019 Thomas Schneider
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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import app.fedilab.android.R;
import app.fedilab.android.activities.AccountReportActivity;
import app.fedilab.android.asynctasks.RetrieveAccountsAsyncTask;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.AccountAdmin;
import app.fedilab.android.client.Entities.Report;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.interfaces.OnRetrieveEmojiAccountInterface;


/**
 * Created by Thomas on 19/06/2019.
 * Adapter for account admins
 */
public class AccountsAdminListAdapter extends RecyclerView.Adapter implements OnRetrieveEmojiAccountInterface {

    private List<AccountAdmin> accountAdmins;
    private LayoutInflater layoutInflater;
    private RetrieveAccountsAsyncTask.Type action;
    private Context context;
    private AccountsAdminListAdapter accountsAdminListAdapter;
    private String targetedId;

    public AccountsAdminListAdapter(Context context, List<AccountAdmin> accountAdmins){
        this.context = context;
        this.accountAdmins = accountAdmins;
        layoutInflater = LayoutInflater.from(context);
        this.accountsAdminListAdapter = this;
    }




    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(layoutInflater.inflate(R.layout.drawer_account_admin, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        final AccountsAdminListAdapter.ViewHolder holder = (AccountsAdminListAdapter.ViewHolder) viewHolder;
        AccountAdmin accountAdmin = accountAdmins.get(position);
        Account account = accountAdmin.getAccount();


        account.makeAccountNameEmoji(context, AccountsAdminListAdapter.this, account);
        if( account.getdisplayNameSpan() == null || account.getdisplayNameSpan().toString().trim().equals("")) {
            if( account.getDisplay_name() != null && !account.getDisplay_name().trim().equals(""))
                holder.account_dn.setText(Helper.shortnameToUnicode(account.getDisplay_name(), true));
            else
                holder.account_dn.setText(account.getDisplay_name().replace("@",""));
        }else
            holder.account_dn.setText( account.getdisplayNameSpan(), TextView.BufferType.SPANNABLE);

        if( account.getdisplayNameSpan() == null || account.getdisplayNameSpan().toString().trim().equals("")) {
            if( account.getDisplay_name() != null && !account.getDisplay_name().trim().equals(""))
                holder.account_dn.setText(Helper.shortnameToUnicode(account.getDisplay_name(), true));
            else
                holder.account_dn.setText(account.getDisplay_name().replace("@",""));
        }else
            holder.account_dn.setText( account.getdisplayNameSpan(), TextView.BufferType.SPANNABLE);
        holder.account_un.setText(String.format("@%s",account.getUsername()));
        holder.account_ac.setText(account.getAcct());
        if( account.getDisplay_name().equals(account.getAcct()))
            holder.account_ac.setVisibility(View.GONE);
        else
            holder.account_ac.setVisibility(View.VISIBLE);

        holder.report_action_taken.setText(accountAdmin.getIp());
        Helper.loadGiF(context, account.getAvatar(), holder.account_pp);


        holder.main_container.setOnClickListener(view ->{
            Intent intent = new Intent(context, AccountReportActivity.class);
            Bundle b = new Bundle();
            b.putParcelable("targeted_account", accountAdmin);
            intent.putExtras(b);
            context.startActivity(intent);
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return accountAdmins.size();
    }

    private AccountAdmin getItemAt(int position){
        if( accountAdmins.size() > position)
            return accountAdmins.get(position);
        else
            return null;
    }

    @Override
    public void onRetrieveEmojiAccount(Account account) {
        notifyAccountChanged(account);
    }

    private void notifyAccountChanged(Account account){
        for (int i = 0; i < accountsAdminListAdapter.getItemCount(); i++) {
            //noinspection ConstantConditions
            if (accountsAdminListAdapter.getItemAt(i) != null && accountsAdminListAdapter.getItemAt(i).getAccount().getId().equals(account.getId())) {
                try {
                    accountsAdminListAdapter.notifyItemChanged(i);
                } catch (Exception ignored) {
                }
            }
        }
    }


    private class ViewHolder extends RecyclerView.ViewHolder{
        ImageView account_pp;
        TextView account_ac;
        TextView account_dn;
        TextView account_un;
        TextView report_action_taken;

        LinearLayout main_container;

        ViewHolder(View itemView) {
            super(itemView);
            account_pp = itemView.findViewById(R.id.account_pp);
            account_dn = itemView.findViewById(R.id.account_dn);
            account_ac = itemView.findViewById(R.id.account_ac);
            account_un = itemView.findViewById(R.id.account_un);
            report_action_taken = itemView.findViewById(R.id.report_action_taken);
            main_container = itemView.findViewById(R.id.main_container);
        }
    }

}