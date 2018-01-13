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
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;



/**
 * Created by Thomas on 05/05/2017.
 * Adapter for accounts when searching
 */
public class AccountsSearchAdapter extends ArrayAdapter<Account> implements Filterable {

    private List<Account> accounts, tempAccounts, suggestions ;
    private LayoutInflater layoutInflater;
    private boolean owner;
    private Context context;

    public AccountsSearchAdapter(Context context, List<Account> accounts){
        super(context, android.R.layout.simple_list_item_1, accounts);
        this.accounts = accounts;
        this.context = context;
        this.tempAccounts = new ArrayList<>(accounts);
        this.suggestions = new ArrayList<>(accounts);
        layoutInflater = LayoutInflater.from(context);
        this.owner = false;
    }
    public AccountsSearchAdapter(Context context, List<Account> accounts, boolean owner){
        super(context, android.R.layout.simple_list_item_1, accounts);
        this.accounts = accounts;
        this.context = context;
        this.tempAccounts = new ArrayList<>(accounts);
        this.suggestions = new ArrayList<>(accounts);
        layoutInflater = LayoutInflater.from(context);
        this.owner = owner;
    }


    @Override
    public int getCount() {
        return accounts.size();
    }

    @Override
    public Account getItem(int position) {
        return accounts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {

        final Account account = accounts.get(position);
        final ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.drawer_account_search, parent, false);
            holder = new ViewHolder();
            holder.account_pp = convertView.findViewById(R.id.account_pp);
            holder.account_dn = convertView.findViewById(R.id.account_dn);
            holder.account_un = convertView.findViewById(R.id.account_un);

            holder.account_container = convertView.findViewById(R.id.account_container);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if( owner) {
            final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
            String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
            Account currentAccount = new AccountDAO(context, db).getAccountByID(userId);
            String instance = (account.getInstance() !=null)?account.getInstance():currentAccount.getInstance();
            holder.account_un.setText(String.format("@%s", account.getUsername() + "@" + instance));
            holder.account_dn.setVisibility(View.GONE);
        }else {
            holder.account_un.setText(String.format("@%s", account.getAcct()));
            holder.account_dn.setText(Helper.shortnameToUnicode(account.getDisplay_name(), true));
            holder.account_dn.setVisibility(View.VISIBLE);
        }
        //Profile picture
        Glide.with(holder.account_pp.getContext())
                .load(account.getAvatar())
                .into(holder.account_pp);
        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return accountFilter;
    }


    private Filter accountFilter = new Filter() {
        @Override
        public CharSequence convertResultToString(Object resultValue) {
            Account account = (Account) resultValue;
            return "@" + account.getAcct();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (constraint != null) {
                suggestions.clear();
                suggestions.addAll(tempAccounts);
                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            } else {
                return new FilterResults();
            }
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            ArrayList<Account> c = (ArrayList<Account>) results.values;
            if (results.count > 0) {
                clear();
                addAll(c);
                notifyDataSetChanged();
            } else{
                clear();
                notifyDataSetChanged();
            }
        }
    };

    private class ViewHolder {
        ImageView account_pp;
        TextView account_dn;
        TextView account_un;
        LinearLayout account_container;
    }


}