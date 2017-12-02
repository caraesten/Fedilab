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
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;

import java.util.List;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.activities.ShowAccountActivity;
import fr.gouv.etalab.mastodon.asynctasks.PostActionAsyncTask;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Error;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnPostActionInterface;



/**
 * Created by Thomas on 03/09/2017.
 * Adapter for accounts from web
 */
public class AccountSearchDevAdapter extends BaseAdapter implements OnPostActionInterface {

    private List<Account> accounts;
    private LayoutInflater layoutInflater;
    private Context context;
    private ViewHolder holder;

    public AccountSearchDevAdapter(Context context, List<Account> accounts){
        this.context = context;
        this.accounts = accounts;
        layoutInflater = LayoutInflater.from(context);
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


        final Account account = accounts.get(position);

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.drawer_account_search_dev, parent, false);
            holder = new ViewHolder();
            holder.account_pp = convertView.findViewById(R.id.account_pp);
            holder.account_dn = convertView.findViewById(R.id.account_dn);
            holder.account_un = convertView.findViewById(R.id.account_un);
            holder.account_follow = convertView.findViewById(R.id.account_follow);
            holder.acccount_container = convertView.findViewById(R.id.acccount_container);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        //Redraws icon for locked accounts
        final float scale = context.getResources().getDisplayMetrics().density;
        if( account != null && account.isLocked()){
            Drawable img = ContextCompat.getDrawable(context, R.drawable.ic_lock_outline);
            assert img != null;
            img.setBounds(0,0,(int) (20 * scale + 0.5f),(int) (20 * scale + 0.5f));
            holder.account_dn.setCompoundDrawables( null, null, img, null);
        }else{
            holder.account_dn.setCompoundDrawables( null, null, null, null);
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            assert account != null;
            holder.account_dn.setText(Helper.shortnameToUnicode(account.getDisplay_name(), true));
            holder.account_un.setText(String.format("@%s",account.getAcct()));
        }else {
            assert account != null;
            holder.account_dn.setText(Helper.shortnameToUnicode(account.getDisplay_name(), true));
            holder.account_un.setText(String.format("@%s",account.getAcct()));
        }
        Helper.changeDrawableColor(context, R.drawable.ic_lock_outline,R.color.mastodonC4);
        //Profile picture
        Glide.with(holder.account_pp.getContext())
                .load(account.getAvatar())
                .into(holder.account_pp);
        if( account.isFollowing()){
            holder.account_follow.setVisibility(View.GONE);
        }else{
            holder.account_follow.setVisibility(View.VISIBLE);
        }

        holder.account_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.account_follow.setEnabled(false);
                new PostActionAsyncTask(context, API.StatusAction.FOLLOW, account.getId(), AccountSearchDevAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
        holder.acccount_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ShowAccountActivity.class);
                Bundle b = new Bundle();
                b.putString("accountId", account.getId());
                intent.putExtras(b);
                context.startActivity(intent);
            }
        });

        return convertView;
    }

    @Override
    public void onPostAction(int statusCode, API.StatusAction statusAction, String userId, Error error) {
        if( error != null){
            final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            boolean show_error_messages = sharedpreferences.getBoolean(Helper.SET_SHOW_ERROR_MESSAGES, true);
            if( show_error_messages)
                Toast.makeText(context, error.getError(),Toast.LENGTH_LONG).show();
            holder.account_follow.setEnabled(true);
            return;
        }
        for( Account account: accounts){
            if(account.getId().equals(userId)) {
                account.setFollowing(true);
                notifyDataSetChanged();
                break;
            }
        }
        holder.account_follow.setVisibility(View.GONE);
        Toast.makeText(context, R.string.toast_follow, Toast.LENGTH_LONG).show();
    }


    private class ViewHolder {
        LinearLayout acccount_container;
        ImageView account_pp;
        TextView account_dn;
        TextView account_un;
        FloatingActionButton account_follow;
    }

}