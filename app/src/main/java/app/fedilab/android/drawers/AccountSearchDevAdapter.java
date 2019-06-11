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
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.content.ContextCompat;
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

import app.fedilab.android.client.API;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Error;
import app.fedilab.android.helper.Helper;
import es.dmoral.toasty.Toasty;
import app.fedilab.android.R;
import app.fedilab.android.activities.ShowAccountActivity;
import app.fedilab.android.asynctasks.PostActionAsyncTask;
import app.fedilab.android.interfaces.OnPostActionInterface;



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
        if( account.isLocked()){
            Drawable img = ContextCompat.getDrawable(context, R.drawable.ic_lock_outline);
            assert img != null;
            img.setBounds(0,0,(int) (20 * scale + 0.5f),(int) (20 * scale + 0.5f));
            holder.account_dn.setCompoundDrawables( null, null, img, null);
        }else{
            holder.account_dn.setCompoundDrawables( null, null, null, null);
        }


        if( !account.getSocial().contains("OPENCOLLECTIVE")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                holder.account_dn.setText(Helper.shortnameToUnicode(account.getDisplay_name(), true));
                holder.account_un.setText(String.format("@%s", account.getAcct()));
            } else {
                holder.account_dn.setText(Helper.shortnameToUnicode(account.getDisplay_name(), true));
                holder.account_un.setText(String.format("@%s", account.getAcct()));
            }
            if( account.isFollowing()){
                holder.account_follow.hide();
            }else{
                holder.account_follow.show();
            }
        }else{
            holder.account_dn.setText(Helper.shortnameToUnicode(account.getDisplay_name(), true));
            holder.account_un.setText(account.getAcct());
            holder.account_follow.hide();
        }
        Helper.changeDrawableColor(context, R.drawable.ic_lock_outline,R.color.mastodonC4);
        //Profile picture

        if( account.getAvatar().startsWith("http")) {
            Glide.with(holder.account_pp.getContext())
                    .load(account.getAvatar())
                    .into(holder.account_pp);
        }else if(account.getSocial().contains("OPENCOLLECTIVE")){
            Glide.with(holder.account_pp.getContext())
                    .load(R.drawable.missing)
                    .into(holder.account_pp);
        }


        if( !account.getSocial().contains("OPENCOLLECTIVE")) {

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
                    b.putParcelable("account", account);
                    intent.putExtras(b);
                    context.startActivity(intent);
                }
            });
        }else{
            holder.acccount_container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   Helper.openBrowser(context, account.getUrl());
                }
            });
        }

        return convertView;
    }

    @Override
    public void onPostAction(int statusCode, API.StatusAction statusAction, String userId, Error error) {
        if( error != null){
            Toasty.error(context, error.getError(),Toast.LENGTH_LONG).show();
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
        holder.account_follow.hide();
        Toasty.success(context, context.getString(R.string.toast_follow), Toast.LENGTH_LONG).show();
    }


    private class ViewHolder {
        LinearLayout acccount_container;
        ImageView account_pp;
        TextView account_dn;
        TextView account_un;
        FloatingActionButton account_follow;
    }

}