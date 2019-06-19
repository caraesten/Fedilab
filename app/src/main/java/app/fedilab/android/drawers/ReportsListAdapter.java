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
import app.fedilab.android.asynctasks.RetrieveAccountsAsyncTask;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Report;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.interfaces.OnRetrieveEmojiAccountInterface;


/**
 * Created by Thomas on 19/06/2019.
 * Adapter for reports
 */
public class ReportsListAdapter extends RecyclerView.Adapter implements OnRetrieveEmojiAccountInterface {

    private List<Report> reports;
    private LayoutInflater layoutInflater;
    private RetrieveAccountsAsyncTask.Type action;
    private Context context;
    private ReportsListAdapter reportsListAdapter;
    private String targetedId;

    public ReportsListAdapter(Context context,  List<Report> reports){
        this.context = context;
        this.reports = reports;
        layoutInflater = LayoutInflater.from(context);
        this.reportsListAdapter = this;
    }




    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(layoutInflater.inflate(R.layout.drawer_report, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        final ReportsListAdapter.ViewHolder holder = (ReportsListAdapter.ViewHolder) viewHolder;
        Report report = reports.get(position);
        Account account = report.getAccount().getAccount();
        Account target_account = report.getTarget_account().getAccount();


        /*account.makeAccountNameEmoji(context, ReportsListAdapter.this, account);
        target_account.makeAccountNameEmoji(context, ReportsListAdapter.this, target_account);*/
        if( account.getdisplayNameSpan() == null || account.getdisplayNameSpan().toString().trim().equals("")) {
            if( account.getDisplay_name() != null && !account.getDisplay_name().trim().equals(""))
                holder.account_dn_reporter.setText(Helper.shortnameToUnicode(account.getDisplay_name(), true));
            else
                holder.account_dn_reporter.setText(account.getUsername().replace("@",""));
        }else
            holder.account_dn_reporter.setText( account.getdisplayNameSpan(), TextView.BufferType.SPANNABLE);

        if( target_account.getdisplayNameSpan() == null || target_account.getdisplayNameSpan().toString().trim().equals("")) {
            if( target_account.getDisplay_name() != null && !target_account.getDisplay_name().trim().equals(""))
                holder.account_dn.setText(Helper.shortnameToUnicode(target_account.getDisplay_name(), true));
            else
                holder.account_dn.setText(target_account.getUsername().replace("@",""));
        }else
            holder.account_dn.setText( target_account.getdisplayNameSpan(), TextView.BufferType.SPANNABLE);



        Helper.loadGiF(context, target_account.getAvatar(), holder.account_pp);
        Helper.loadGiF(context, account.getAvatar(), holder.account_pp_reporter);
        holder.account_un.setText(String.format("@%s",account.getUsername()));
        holder.account_ac.setText(account.getAcct());
        if( account.getUsername().equals(account.getAcct()))
            holder.account_ac.setVisibility(View.GONE);
        else
            holder.account_ac.setVisibility(View.VISIBLE);

        holder.report_comment.setText(report.getComment());

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    private Report getItemAt(int position){
        if( reports.size() > position)
            return reports.get(position);
        else
            return null;
    }

    @Override
    public void onRetrieveEmojiAccount(Account account) {
        notifyAccountChanged(account);
    }

    private void notifyAccountChanged(Account account){
        for (int i = 0; i < reportsListAdapter.getItemCount(); i++) {
            //noinspection ConstantConditions
            if (reportsListAdapter.getItemAt(i) != null && reportsListAdapter.getItemAt(i).getStatuses().get(0).getAccount().getId().equals(account.getId())) {
                try {
                    reportsListAdapter.notifyItemChanged(i);
                } catch (Exception ignored) {
                }
            }
        }
    }


    private class ViewHolder extends RecyclerView.ViewHolder{
        ImageView account_pp, account_pp_reporter;
        TextView account_ac;
        TextView account_dn, account_dn_reporter;
        TextView account_un;
        TextView report_comment;

        LinearLayout account_container;

        ViewHolder(View itemView) {
            super(itemView);
            account_pp = itemView.findViewById(R.id.account_pp);
            account_pp_reporter = itemView.findViewById(R.id.account_pp_reporter);
            account_dn = itemView.findViewById(R.id.account_dn);
            account_dn_reporter = itemView.findViewById(R.id.account_dn_reporter);
            account_ac = itemView.findViewById(R.id.account_ac);
            account_un = itemView.findViewById(R.id.account_un);
            report_comment = itemView.findViewById(R.id.report_comment);
            account_container = itemView.findViewById(R.id.account_container);
        }
    }

}