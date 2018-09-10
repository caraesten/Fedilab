package fr.gouv.etalab.mastodon.drawers;
/* Copyright 2018 Thomas Schneider
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import java.util.List;
import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.helper.CrossActions;


/**
 * Created by Thomas on 10/09/2018.
 * Adapter for who to follow list
 */
public class WhoToFollowAccountsAdapter extends BaseAdapter {

    private List<String> lists;
    private LayoutInflater layoutInflater;
    private Context context;

    public WhoToFollowAccountsAdapter(Context context, List<String> lists){
        this.lists = lists;
        layoutInflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public int getCount() {
        return lists.size();
    }

    @Override
    public Object getItem(int position) {
        return lists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        String item = lists.get(position);
        final ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.drawer_who_to_follow_account, parent, false);
            holder = new ViewHolder();
            holder.account_to_follow = convertView.findViewById(R.id.account_to_follow);
            holder.account_to_follow = convertView.findViewById(R.id.account_to_follow);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.account_to_follow.setText(item);

        holder.account_to_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Account account = new Account();
                String[] val = item.split("@");
                if( val.length > 1){
                    account.setAcct(val[0]);
                    account.setInstance(val[1]);
                    CrossActions.doCrossProfile(context, account);
                }
            }
        });

        return convertView;
    }


    private class ViewHolder {
        CheckBox account_to_follow_check;
        TextView account_to_follow;
    }


}