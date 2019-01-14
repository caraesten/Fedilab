package fr.gouv.etalab.mastodon.drawers;
/* Copyright 2019 Thomas Schneider
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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.activities.MainActivity;
import fr.gouv.etalab.mastodon.activities.MediaActivity;
import fr.gouv.etalab.mastodon.activities.ShowAccountActivity;
import fr.gouv.etalab.mastodon.activities.ShowConversationActivity;
import fr.gouv.etalab.mastodon.asynctasks.UpdateAccountInfoAsyncTask;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Attachment;
import fr.gouv.etalab.mastodon.client.Entities.Emojis;
import fr.gouv.etalab.mastodon.client.Entities.Error;
import fr.gouv.etalab.mastodon.client.Entities.Notification;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.client.Glide.GlideApp;
import fr.gouv.etalab.mastodon.helper.CrossActions;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnPostActionInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveEmojiInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveRepliesInterface;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import fr.gouv.etalab.mastodon.sqlite.StatusCacheDAO;


/**
 * Created by Thomas on 14/01/2019.
 * Adapter for pixelfed drawer
 */
public class PixelfedListAdapter extends RecyclerView.Adapter implements OnPostActionInterface, OnRetrieveEmojiInterface, OnRetrieveRepliesInterface {

    private Context context;
    private List<Status> statuses;
    private LayoutInflater layoutInflater;
    private PixelfedListAdapter pixelfedListAdapter;
    private final int HIDDEN_STATUS = 0;
    private static final int DISPLAYED_STATUS = 1;
    private List<String> timedMute;


    public PixelfedListAdapter(Context context, List<Status> statuses){
        super();
        this.context = context;
        this.statuses = statuses;
        layoutInflater = LayoutInflater.from(this.context);
        pixelfedListAdapter = this;
    }


    public void updateMuted(List<String> timedMute){
        this.timedMute = timedMute;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return statuses.size();
    }

    private Status getItemAt(int position){
        if( statuses.size() > position)
            return statuses.get(position);
        else
            return null;
    }

    @Override
    public void onRetrieveReplies(APIResponse apiResponse) {
        if( apiResponse.getError() != null || apiResponse.getStatuses() == null || apiResponse.getStatuses().size() == 0){
            return;
        }
        List<Status> modifiedStatus = apiResponse.getStatuses();
        notifyStatusChanged(modifiedStatus.get(0));
    }


    private class ViewHolderEmpty extends RecyclerView.ViewHolder{
        ViewHolderEmpty(View itemView) {
            super(itemView);
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
    }


    private class ViewHolderPixelfed extends RecyclerView.ViewHolder{
        ImageView art_media, art_pp;
        TextView art_username, art_acct;
        LinearLayout art_author;
        RelativeLayout status_show_more;
        ImageView show_more_button_art;
        ViewHolderPixelfed(View itemView) {
            super(itemView);
            art_media = itemView.findViewById(R.id.art_media);
            art_pp = itemView.findViewById(R.id.art_pp);
            art_username = itemView.findViewById(R.id.art_username);
            art_acct = itemView.findViewById(R.id.art_acct);
            art_author = itemView.findViewById(R.id.art_author);
            status_show_more = itemView.findViewById(R.id.status_show_more);
            show_more_button_art = itemView.findViewById(R.id.show_more_button_art);
        }
    }




    public Status getItem(int position){
        if( statuses.size() > position && position >= 0)
            return statuses.get(position);
        else return null;
    }

    @Override
    public int getItemViewType(int position) {

        if( !Helper.filterToots(context, statuses.get(position), timedMute, null))
            return HIDDEN_STATUS;
        else
            return DISPLAYED_STATUS;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if( viewType != DISPLAYED_STATUS)
            return new ViewHolderPixelfed(layoutInflater.inflate(R.layout.drawer_pixelfed, parent, false));
        else
            return new ViewHolderEmpty(layoutInflater.inflate(R.layout.drawer_empty, parent, false));
    }



    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int i) {
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        final String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        if( viewHolder.getItemViewType() != HIDDEN_STATUS ) {
            final ViewHolderPixelfed holder = (ViewHolderPixelfed) viewHolder;
            final Status status = statuses.get(viewHolder.getAdapterPosition());
            if (!status.isClickable())
                Status.transform(context, status);
            if (!status.isEmojiFound())
                Status.makeEmojis(context, this, status);

            if (status.getAccount() != null && status.getAccount().getAvatar() != null)
                Glide.with(context)
                        .load(status.getAccount().getAvatar())
                        .apply(new RequestOptions().transforms(new FitCenter(), new RoundedCorners(10)))
                        .into(holder.art_pp);

            boolean expand_media = sharedpreferences.getBoolean(Helper.SET_EXPAND_MEDIA, false);
            if (status.getMedia_attachments() != null && status.getMedia_attachments().size() > 0)
                GlideApp.with(context)
                        .asBitmap()
                        .load(status.getMedia_attachments().get(0).getPreview_url())
                        .listener(new RequestListener<Bitmap>() {
                            @Override
                            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                if (status.isSensitive())
                                    notifyStatusChanged(status);
                                return false;
                            }

                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                                return false;
                            }
                        })
                        .into(holder.art_media);
            RelativeLayout.LayoutParams rel_btn = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, holder.art_media.getHeight());
            holder.status_show_more.setLayoutParams(rel_btn);
            if (expand_media || !status.isSensitive()) {
                status.setAttachmentShown(true);
                holder.status_show_more.setVisibility(View.GONE);
            } else {
                if (!status.isAttachmentShown()) {
                    holder.status_show_more.setVisibility(View.VISIBLE);
                } else {
                    holder.status_show_more.setVisibility(View.GONE);
                }
            }

            holder.show_more_button_art.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    status.setAttachmentShown(true);
                    notifyStatusChanged(status);
                }
            });
            holder.art_pp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.PIXELFED) {
                        CrossActions.doCrossProfile(context, status.getAccount());
                    } else {
                        Intent intent = new Intent(context, ShowAccountActivity.class);
                        Bundle b = new Bundle();
                        b.putParcelable("account", status.getAccount());
                        intent.putExtras(b);
                        context.startActivity(intent);
                    }
                }
            });

            holder.art_media.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MediaActivity.class);
                    Bundle b = new Bundle();
                    ArrayList<Attachment> attachments = new ArrayList<>();
                    if (status.getArt_attachment() != null)
                        attachments.add(status.getArt_attachment());
                    else if (status.getMedia_attachments() != null && status.getMedia_attachments().size() > 0)
                        attachments.add(status.getMedia_attachments().get(0));
                    intent.putParcelableArrayListExtra("mediaArray", attachments);
                    b.putInt("position", 0);
                    intent.putExtras(b);
                    context.startActivity(intent);
                }
            });
            holder.art_author.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.PIXELFED) {
                        CrossActions.doCrossConversation(context, status);
                    } else {
                        Intent intent = new Intent(context, ShowConversationActivity.class);
                        Bundle b = new Bundle();
                        b.putParcelable("status", status);
                        intent.putExtras(b);
                        context.startActivity(intent);
                    }
                }
            });

            if (status.getDisplayNameSpan() != null && status.getDisplayNameSpan().toString().trim().length() > 0)
                holder.art_username.setText(status.getDisplayNameSpan(), TextView.BufferType.SPANNABLE);
            else
                holder.art_username.setText(status.getAccount().getUsername());

            holder.art_acct.setText(String.format("@%s", status.getAccount().getAcct()));
        }

    }







    @Override
    public void onPostAction(int statusCode, API.StatusAction statusAction, String targetedId, Error error) {

        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        if( error != null){
            Toasty.error(context, error.getError(),Toast.LENGTH_LONG).show();
            return;
        }
        Helper.manageMessageStatusCode(context, statusCode, statusAction);
        //When muting or blocking an account, its status are removed from the list
        List<Status> statusesToRemove = new ArrayList<>();
        if( statusAction == API.StatusAction.MUTE || statusAction == API.StatusAction.BLOCK){
            for(Status status: statuses){
                if( status.getAccount().getId().equals(targetedId))
                    statusesToRemove.add(status);
            }
            statuses.removeAll(statusesToRemove);
            pixelfedListAdapter.notifyDataSetChanged();
        }else  if( statusAction == API.StatusAction.UNSTATUS ){
            int position = 0;
            for(Status status: statuses){
                if( status.getId().equals(targetedId)) {
                    statuses.remove(status);
                    pixelfedListAdapter.notifyItemRemoved(position);
                    SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                    //Remove the status from cache also
                    try {
                        new StatusCacheDAO(context, db).remove(StatusCacheDAO.ARCHIVE_CACHE,status);
                    }catch (Exception ignored){}
                    break;
                }
                position++;
            }
        }
        else if ( statusAction == API.StatusAction.PIN || statusAction == API.StatusAction.UNPIN ) {
            int position = 0;
            for (Status status: statuses) {
                if (status.getId().equals(targetedId)) {
                    if (statusAction == API.StatusAction.PIN)
                        status.setPinned(true);
                    else
                        status.setPinned(false);
                    pixelfedListAdapter.notifyItemChanged(position);
                    break;
                }
                position++;
            }
        }
        if( statusAction == API.StatusAction.PEERTUBEDELETECOMMENT){
            int position = 0;
            for(Status status: statuses){
                if( status.getId().equals(targetedId)) {
                    statuses.remove(status);
                    pixelfedListAdapter.notifyItemRemoved(position);
                    break;
                }
                position++;
            }
        }
    }

    public void notifyStatusChanged(Status status){
        for (int i = 0; i < pixelfedListAdapter.getItemCount(); i++) {
            //noinspection ConstantConditions
            if (pixelfedListAdapter.getItemAt(i) != null && pixelfedListAdapter.getItemAt(i).getId().equals(status.getId())) {
                try {
                    pixelfedListAdapter.notifyItemChanged(i);
                } catch (Exception ignored) {
                }
            }
        }
    }

    public void notifyStatusWithActionChanged(API.StatusAction statusAction, Status status){
        for (int i = 0; i < pixelfedListAdapter.getItemCount(); i++) {
            //noinspection ConstantConditions
            if (pixelfedListAdapter.getItemAt(i) != null && pixelfedListAdapter.getItemAt(i).getId().equals(status.getId())) {
                try {
                    int favCount = statuses.get(i).getFavourites_count();
                    int boostCount = statuses.get(i).getReblogs_count();
                    if( statusAction == API.StatusAction.REBLOG)
                        boostCount++;
                    else if( statusAction == API.StatusAction.UNREBLOG)
                        boostCount--;
                    else if( statusAction == API.StatusAction.FAVOURITE)
                        favCount++;
                    else if( statusAction == API.StatusAction.UNFAVOURITE)
                        favCount--;
                    if( boostCount < 0 )
                        boostCount = 0;
                    if( favCount < 0 )
                        favCount = 0;
                    statuses.get(i).setFavourited(status.isFavourited());
                    statuses.get(i).setFavourites_count(favCount);
                    statuses.get(i).setReblogged(status.isReblogged());
                    statuses.get(i).setReblogs_count(boostCount);
                    pixelfedListAdapter.notifyItemChanged(i);
                } catch (Exception ignored) {
                }
            }
        }
    }


    @Override
    public void onRetrieveEmoji(Status status, boolean fromTranslation) {
        if( status != null) {
            if( !fromTranslation) {
               status.setEmojiFound(true);
            }else {
              status.setEmojiTranslateFound(true);
            }
            notifyStatusChanged(status);
        }
    }

    @Override
    public void onRetrieveEmoji(Notification notification) {

    }

    @Override
    public void onRetrieveSearchEmoji(List<Emojis> emojis) {

    }

}