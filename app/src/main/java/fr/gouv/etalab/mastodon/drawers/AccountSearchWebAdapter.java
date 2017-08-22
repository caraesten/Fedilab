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
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

import java.io.File;
import java.util.List;

import fr.gouv.etalab.mastodon.asynctasks.PostActionAsyncTask;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Error;
import fr.gouv.etalab.mastodon.client.PatchBaseImageDownloader;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnPostActionInterface;
import mastodon.etalab.gouv.fr.mastodon.R;

import static fr.gouv.etalab.mastodon.helper.Helper.changeDrawableColor;


/**
 * Created by Thomas on 22/08/2017.
 * Adapter for accounts from web
 */
public class AccountSearchWebAdapter extends BaseAdapter implements OnPostActionInterface {

    private List<Account> accounts;
    private LayoutInflater layoutInflater;
    private Context context;
    private ViewHolder holder;

    public AccountSearchWebAdapter(Context context, List<Account> accounts){
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

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.drawer_account_search_html, parent, false);
            holder = new ViewHolder();
            holder.account_pp = (ImageView) convertView.findViewById(R.id.account_pp);
            holder.account_dn = (TextView) convertView.findViewById(R.id.account_dn);
            holder.account_un = (TextView) convertView.findViewById(R.id.account_un);
            holder.account_ds = (TextView) convertView.findViewById(R.id.account_ds);
            holder.account_sc = (TextView) convertView.findViewById(R.id.account_sc);
            holder.account_fgc = (TextView) convertView.findViewById(R.id.account_fgc);
            holder.account_frc = (TextView) convertView.findViewById(R.id.account_frc);
            holder.account_follow = (FloatingActionButton) convertView.findViewById(R.id.account_follow);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        //Redraws icon for locked accounts
        final float scale = context.getResources().getDisplayMetrics().density;
        if( account != null && account.isLocked()){
            Drawable img = ContextCompat.getDrawable(context, R.drawable.ic_action_lock_closed);
            img.setBounds(0,0,(int) (20 * scale + 0.5f),(int) (20 * scale + 0.5f));
            holder.account_dn.setCompoundDrawables( null, null, img, null);
        }else{
            holder.account_dn.setCompoundDrawables( null, null, null, null);
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.account_ds.setText(Html.fromHtml(account.getNote(), Html.FROM_HTML_MODE_LEGACY));
            holder.account_dn.setText(Html.fromHtml(Helper.shortnameToUnicode(account.getDisplay_name(), true), Html.FROM_HTML_MODE_LEGACY));
            holder.account_un.setText(Html.fromHtml(Helper.shortnameToUnicode(account.getUsername(), true), Html.FROM_HTML_MODE_LEGACY));
        }else {
            //noinspection deprecation
            holder.account_ds.setText(Html.fromHtml(account.getNote()));
            holder.account_dn.setText(Html.fromHtml(Helper.shortnameToUnicode(account.getDisplay_name(), true)));
            holder.account_un.setText(Html.fromHtml(Helper.shortnameToUnicode(account.getUsername(), true)));
        }
        changeDrawableColor(context, R.drawable.ic_action_lock_closed,R.color.mastodonC4);
        holder.account_ds.setAutoLinkMask(Linkify.WEB_URLS);
        holder.account_sc.setText(String.valueOf(account.getStatuses_count()));
        holder.account_fgc.setText(String.valueOf(account.getFollowing_count()));
        holder.account_frc.setText(String.valueOf(account.getFollowers_count()));
        //Profile picture
        imageLoader.displayImage(account.getAvatar(), holder.account_pp, options);

        holder.account_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.account_follow.setEnabled(false);
                new PostActionAsyncTask(context, API.StatusAction.REMOTE_FOLLOW, account.getAcct(), AccountSearchWebAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        holder.account_follow.setVisibility(View.GONE);
        Toast.makeText(context, R.string.toast_follow, Toast.LENGTH_LONG).show();
    }


    private class ViewHolder {
        ImageView account_pp;
        TextView account_dn;
        TextView account_un;
        TextView account_ds;
        TextView account_sc;
        TextView account_fgc;
        TextView account_frc;
        FloatingActionButton account_follow;
    }



}