package fr.gouv.etalab.mastodon.drawers;
/* Copyright 2017 Thomas Schneider
 *
 * This file is a part of Mastodon Etalab for mastodon.etalab.gouv.fr
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Mastodon Etalab is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Thomas Schneider; if not,
 * see <http://www.gnu.org/licenses>. */

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.emojione.Emojione;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

import java.util.ArrayList;
import java.util.List;

import fr.gouv.etalab.mastodon.activities.HashTagActivity;
import fr.gouv.etalab.mastodon.activities.ShowAccountActivity;
import fr.gouv.etalab.mastodon.activities.ShowConversationActivity;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.helper.Helper;
import mastodon.etalab.gouv.fr.mastodon.R;


/**
 * Created by Thomas on 26/05/2017.
 * Adapter for Search results
 */
public class SearchListAdapter extends BaseAdapter {

    private Context context;
    private List<Status> statuses;
    private List<Account> accounts;
    private List<String> tags;


    private static final int STATUS_TYPE = 0;
    private static final int ACCOUNT_TYPE = 1;
    private static final int TAG_TYPE = 2;
    private LayoutInflater layoutInflater;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;


    public SearchListAdapter(Context context, List<Status> statuses, List<Account> accounts, List<String> tags){
        this.context = context;
        this.statuses = ( statuses != null)?statuses:new ArrayList<Status>();
        this.accounts = ( accounts != null)?accounts:new ArrayList<Account>();
        this.tags = ( tags != null)?tags:new ArrayList<String>();
        layoutInflater = LayoutInflater.from(context);
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder().displayer(new SimpleBitmapDisplayer()).cacheInMemory(false)
                .cacheOnDisk(true).resetViewBeforeLoading(true).build();
    }

    @Override
    public int getItemViewType(int position){
        if( position < statuses.size())
            return STATUS_TYPE;
        else if( position < statuses.size() + accounts.size() )
            return ACCOUNT_TYPE;
        else
            return TAG_TYPE;
    }

    @Override
    public int getCount() {
        return statuses.size() + accounts.size() + tags.size();
    }

    @Override
    public Object getItem(int position) {
        if( position < statuses.size())
            return statuses.get(position);
        else if( position < statuses.size() + accounts.size() )
            return accounts.get(position - statuses.size());
        else
            return tags.get(position - (statuses.size() + accounts.size()));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount(){
        return 3;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        int type = getItemViewType(position);
        if( type == STATUS_TYPE){
            View v = convertView;
            final Status status = (Status) getItem(position);
            final ViewHolderStatus holder;
            if (v == null) {
                v = layoutInflater.inflate(R.layout.drawer_status_main_search, parent, false);
                holder = new ViewHolderStatus();
                holder.status_content = (TextView) v.findViewById(R.id.status_content);
                holder.status_account_username = (TextView) v.findViewById(R.id.status_account_username);
                holder.status_account_displayname = (TextView) v.findViewById(R.id.status_account_displayname);
                holder.status_account_profile = (ImageView) v.findViewById(R.id.status_account_profile);
                holder.status_toot_date = (TextView) v.findViewById(R.id.status_toot_date);
                holder.status_reblog_user = (TextView) v.findViewById(R.id.status_reblog_user);
                holder.main_container = (LinearLayout) v.findViewById(R.id.main_container);
                holder.status_search_title = (TextView) v.findViewById(R.id.status_search_title);
                v.setTag(holder);
            } else {
                holder = (ViewHolderStatus) v.getTag();
            }
            if( isFirstTypeItem(type, position) )
                holder.status_search_title.setVisibility(View.VISIBLE);
            else
                holder.status_search_title.setVisibility(View.GONE);
            final float scale = context.getResources().getDisplayMetrics().density;
            if( !status.getIn_reply_to_account_id().equals("null") || !status.getIn_reply_to_id().equals("null") ){
                Drawable img = ContextCompat.getDrawable(context, R.drawable.ic_reply);
                img.setBounds(0,0,(int) (20 * scale + 0.5f),(int) (15 * scale + 0.5f));
                holder.status_account_displayname.setCompoundDrawables( img, null, null, null);
            }else if( status.getReblog() != null){
                Drawable img = ContextCompat.getDrawable(context, R.drawable.ic_retweet_header);
                img.setBounds(0,0,(int) (20 * scale + 0.5f),(int) (15 * scale + 0.5f));
                holder.status_account_displayname.setCompoundDrawables( img, null, null, null);
            }else{
                holder.status_account_displayname.setCompoundDrawables( null, null, null, null);
            }
            //Click on a conversation
            holder.status_content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ShowConversationActivity.class);
                    Bundle b = new Bundle();
                    b.putString("statusId", status.getId()); //Your id
                    intent.putExtras(b); //Put your id to your next Intent
                    context.startActivity(intent);
                }
            });

            final String content, displayName, username, ppurl;
            if( status.getReblog() != null){
                content = status.getReblog().getContent();
                displayName = Emojione.shortnameToUnicode(status.getReblog().getAccount().getDisplay_name(), true);
                username = status.getReblog().getAccount().getUsername();
                holder.status_reblog_user.setText(displayName + " " +String.format("@%s",username));
                ppurl = status.getReblog().getAccount().getAvatar();
                holder.status_reblog_user.setVisibility(View.VISIBLE);
                holder.status_account_displayname.setText(context.getResources().getString(R.string.reblog_by, status.getAccount().getAcct()));
                holder.status_account_username.setText( "");
            }else {
                ppurl = status.getAccount().getAvatar();
                content = status.getContent();
                displayName = Emojione.shortnameToUnicode(status.getAccount().getDisplay_name(), true);
                username = status.getAccount().getUsername();
                holder.status_reblog_user.setVisibility(View.GONE);
                holder.status_account_displayname.setText(displayName);
                holder.status_account_username.setText( String.format("@%s",username));
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                holder.status_content.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT));
            else
                //noinspection deprecation
                holder.status_content.setText(Html.fromHtml(content));
            holder.status_content.setAutoLinkMask(Linkify.WEB_URLS);
            holder.status_toot_date.setText(Helper.dateDiff(context, status.getCreated_at()));

            imageLoader.displayImage(ppurl, holder.status_account_profile, options);


            holder.status_account_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ShowAccountActivity.class);
                    Bundle b = new Bundle();
                    if( status.getReblog() == null)
                        b.putString("accountId", status.getAccount().getId());
                    else
                        b.putString("accountId", status.getReblog().getAccount().getId());
                    intent.putExtras(b);
                    context.startActivity(intent);
                }
            });
            return v;
        }else if( type == ACCOUNT_TYPE ){
            View v = convertView;
            final Account account = (Account) getItem(position);
            final ViewHolderAccounts holder;
            if (v == null) {
                v = layoutInflater.inflate(R.layout.drawer_account_main_search, parent, false);
                holder = new ViewHolderAccounts();
                holder.account_pp = (ImageView) v.findViewById(R.id.account_pp);
                holder.account_dn = (TextView) v.findViewById(R.id.account_dn);
                holder.account_ac = (TextView) v.findViewById(R.id.account_ac);
                holder.account_un = (TextView) v.findViewById(R.id.account_un);

                holder.account_sc = (TextView) v.findViewById(R.id.account_sc);
                holder.account_fgc = (TextView) v.findViewById(R.id.account_fgc);
                holder.account_frc = (TextView) v.findViewById(R.id.account_frc);
                holder.account_search_title = (TextView) v.findViewById(R.id.account_search_title);
                v.setTag(holder);
            } else {
                holder = (ViewHolderAccounts) v.getTag();
            }

            if( isFirstTypeItem(type, position) )
                holder.account_search_title.setVisibility(View.VISIBLE);
            else
                holder.account_search_title.setVisibility(View.GONE);

            holder.account_dn.setText(account.getDisplay_name());
            holder.account_un.setText(String.format("@%s",account.getUsername()));
            holder.account_ac.setText(account.getAcct());
            if( account.getDisplay_name().equals(account.getAcct()))
                holder.account_ac.setVisibility(View.GONE);
            else
                holder.account_ac.setVisibility(View.VISIBLE);
            holder.account_sc.setText(String.valueOf(account.getStatuses_count()));
            holder.account_fgc.setText(String.valueOf(account.getFollowing_count()));
            holder.account_frc.setText(String.valueOf(account.getFollowers_count()));
            //Profile picture
            imageLoader.displayImage(account.getAvatar(), holder.account_pp, options);


            holder.account_pp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ShowAccountActivity.class);
                    Bundle b = new Bundle();
                    b.putString("accountId", account.getId());
                    intent.putExtras(b);
                    context.startActivity(intent);
                }
            });
            return v;
        }else{
            final String tag = (String) getItem(position);
            final ViewHolderTag holder;
            View v = convertView;
            if (v == null) {
                v = layoutInflater.inflate(R.layout.drawer_tag, parent, false);
                holder = new ViewHolderTag();
                holder.tag_name = (TextView) v.findViewById(R.id.tag_name);
                holder.tag_search_title = (TextView) v.findViewById(R.id.tag_search_title);
                v.setTag(holder);
            } else {
                holder = (ViewHolderTag) v.getTag();
            }
            if( isFirstTypeItem(type, position) )
                holder.tag_search_title.setVisibility(View.VISIBLE);
            else
                holder.tag_search_title.setVisibility(View.GONE);
            holder.tag_name.setText(String.format("#%s",tag));
            holder.tag_name.setPaintFlags(holder.tag_name.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            holder.tag_name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, HashTagActivity.class);
                    Bundle b = new Bundle();
                    b.putString("tag", tag.trim());
                    intent.putExtras(b);
                    context.startActivity(intent);
                }
            });
            return v;
        }
    }

    private boolean isFirstTypeItem(int type, int position){
        if( position == 0 && type == STATUS_TYPE)
            return true;
        else if( position == statuses.size() && type == ACCOUNT_TYPE )
            return true;
        else if( position ==  (statuses.size() + accounts.size()) && type == TAG_TYPE )
            return true;
        return false;
    }

    private class ViewHolderStatus {
        TextView status_content;
        TextView status_account_username;
        TextView status_account_displayname;
        ImageView status_account_profile;
        TextView status_toot_date;
        TextView status_reblog_user;
        LinearLayout main_container;
        TextView status_search_title;
    }


    private class ViewHolderAccounts {
        ImageView account_pp;
        TextView account_ac;
        TextView account_dn;
        TextView account_un;
        TextView account_sc;
        TextView account_fgc;
        TextView account_frc;
        TextView account_search_title;
    }

    private class ViewHolderTag {
        TextView tag_name;
        TextView tag_search_title;
    }
}