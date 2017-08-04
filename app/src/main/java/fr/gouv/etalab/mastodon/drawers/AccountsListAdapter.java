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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.Html;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import fr.gouv.etalab.mastodon.asynctasks.RetrieveAccountsAsyncTask;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Error;
import fr.gouv.etalab.mastodon.client.PatchBaseImageDownloader;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnPostActionInterface;
import mastodon.etalab.gouv.fr.mastodon.R;
import fr.gouv.etalab.mastodon.activities.ShowAccountActivity;
import fr.gouv.etalab.mastodon.asynctasks.PostActionAsyncTask;


/**
 * Created by Thomas on 27/04/2017.
 * Adapter for accounts
 */
public class AccountsListAdapter extends BaseAdapter implements OnPostActionInterface {

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



    @Override
    public int getCount() {
        return accounts.size();
    }

    @Override
    public Object getItem(int position) {
        return accounts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ImageLoader imageLoader = ImageLoader.getInstance();
        File cacheDir = new File(context.getCacheDir(), context.getString(R.string.app_name));
        ImageLoaderConfiguration configImg = new ImageLoaderConfiguration.Builder(context)
                .imageDownloader(new PatchBaseImageDownloader(context))
                .threadPoolSize(5)
                .threadPriority(Thread.MIN_PRIORITY + 3)
                .denyCacheImageMultipleSizesInMemory()
                .diskCache(new UnlimitedDiskCache(cacheDir))
                .build();
        if( !imageLoader.isInited())
            imageLoader.init(configImg);
        DisplayImageOptions options = new DisplayImageOptions.Builder().displayer(new SimpleBitmapDisplayer()).cacheInMemory(false)
                .cacheOnDisk(true).resetViewBeforeLoading(true).build();
        final Account account = accounts.get(position);
        final ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.drawer_account, parent, false);
            holder = new ViewHolder();
            holder.account_pp = (ImageView) convertView.findViewById(R.id.account_pp);
            holder.account_dn = (TextView) convertView.findViewById(R.id.account_dn);
            holder.account_ac = (TextView) convertView.findViewById(R.id.account_ac);
            holder.account_un = (TextView) convertView.findViewById(R.id.account_un);
            holder.account_ds = (TextView) convertView.findViewById(R.id.account_ds);
            holder.account_sc = (TextView) convertView.findViewById(R.id.account_sc);
            holder.account_fgc = (TextView) convertView.findViewById(R.id.account_fgc);
            holder.account_frc = (TextView) convertView.findViewById(R.id.account_frc);
            holder.account_action_block = (FloatingActionButton) convertView.findViewById(R.id.account_action_block);
            holder.account_action_mute = (FloatingActionButton) convertView.findViewById(R.id.account_action_mute);

            holder.account_container = (LinearLayout) convertView.findViewById(R.id.account_container);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if( action == RetrieveAccountsAsyncTask.Type.BLOCKED)
            holder.account_action_block.setVisibility(View.VISIBLE);
        else if( action == RetrieveAccountsAsyncTask.Type.MUTED)
            holder.account_action_mute.setVisibility(View.VISIBLE);


        holder.account_action_mute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreOptionDialog(account);
            }
        });

        holder.account_action_block.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreOptionDialog(account);
            }
        });



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
            holder.account_ds.setText(Html.fromHtml(account.getNote(), Html.FROM_HTML_MODE_COMPACT));
        else
            //noinspection deprecation
            holder.account_ds.setText(Html.fromHtml(account.getNote()));
        holder.account_ds.setAutoLinkMask(Linkify.WEB_URLS);
        holder.account_sc.setText(String.valueOf(account.getStatuses_count()));
        holder.account_fgc.setText(String.valueOf(account.getFollowing_count()));
        holder.account_frc.setText(String.valueOf(account.getFollowers_count()));
        //Profile picture
        imageLoader.displayImage(account.getAvatar(), holder.account_pp, options);


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
        return convertView;
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
    }


    private class ViewHolder {
        ImageView account_pp;
        TextView account_ac;
        TextView account_dn;
        TextView account_un;
        TextView account_ds;
        TextView account_sc;
        TextView account_fgc;
        TextView account_frc;
        LinearLayout account_container;
        FloatingActionButton account_action_block;
        FloatingActionButton account_action_mute;
    }


    /**
     * More option for acccounts (unmute / unblock)
     * @param account Account current account
     */
    private void moreOptionDialog(final Account account){

        String[] stringArrayConf = context.getResources().getStringArray(R.array.more_action_confirm_account);
        final API.StatusAction doAction;

        AlertDialog.Builder dialog = new AlertDialog.Builder(context);

        if( action == RetrieveAccountsAsyncTask.Type.BLOCKED) {
            dialog.setMessage(stringArrayConf[1]);
            doAction = API.StatusAction.UNBLOCK;
        }else {
            dialog.setMessage(stringArrayConf[0]);
            doAction = API.StatusAction.UNMUTE;
        }
        dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog,int which) {
                dialog.dismiss();
            }
        });
        dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog,int which) {
                new PostActionAsyncTask(context, doAction, account.getId(), AccountsListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

}