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


import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import app.fedilab.android.R;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.services.LiveNotificationDelayedService;
import app.fedilab.android.services.LiveNotificationService;
import app.fedilab.android.services.StopLiveNotificationReceiver;

import static android.content.Context.MODE_PRIVATE;


/**
 * Created by Thomas on 03/10/2019
 * Adapter for accounts and live notifications
 */
public class AccountLiveAdapter extends RecyclerView.Adapter {

    private Context context;
    private List<Account> accounts;

    public AccountLiveAdapter(List<Account> accounts) {
        this.accounts = accounts;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        return new ViewHolder(layoutInflater.inflate(R.layout.drawer_account_notification_settings, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

        ViewHolder holder = (ViewHolder) viewHolder;
        holder.account_acct.setText(accounts.get(i).getUsername() + "@" + accounts.get(i).getInstance());
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        boolean allowStream = sharedpreferences.getBoolean(Helper.SET_ALLOW_STREAM + accounts.get(i).getId() + accounts.get(i).getInstance(), true);

        holder.account_acct_live_notifications.setChecked(allowStream);
        holder.account_acct_live_notifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_ALLOW_STREAM +  accounts.get(i).getId() + accounts.get(i).getInstance(), holder.account_acct_live_notifications.isChecked());
                editor.apply();
                if (holder.account_acct_live_notifications.isChecked()) {
                    LiveNotificationDelayedService.totalAccount++;
                } else {
                    LiveNotificationDelayedService.totalAccount--;
                }
                Helper.startSreaming(context);
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


    class ViewHolder extends RecyclerView.ViewHolder {
        TextView account_acct;
        SwitchCompat account_acct_live_notifications;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            account_acct = itemView.findViewById(R.id.account_acct);
            account_acct_live_notifications = itemView.findViewById(R.id.account_acct_live_notifications);
        }
    }


}