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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.varunest.sparkbutton.SparkButton;

import java.util.ArrayList;
import java.util.List;

import app.fedilab.android.client.API;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Attachment;
import app.fedilab.android.client.Entities.Emojis;
import app.fedilab.android.client.Entities.Error;
import app.fedilab.android.client.Entities.Notification;
import app.fedilab.android.client.Entities.Status;
import app.fedilab.android.helper.CrossActions;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.sqlite.Sqlite;
import app.fedilab.android.sqlite.StatusCacheDAO;
import es.dmoral.toasty.Toasty;
import app.fedilab.android.R;
import app.fedilab.android.activities.MainActivity;
import app.fedilab.android.activities.MediaActivity;
import app.fedilab.android.activities.ShowAccountActivity;
import app.fedilab.android.activities.ShowConversationActivity;
import app.fedilab.android.asynctasks.RetrieveFeedsAsyncTask;
import app.fedilab.android.asynctasks.UpdateAccountInfoAsyncTask;
import app.fedilab.android.client.Glide.GlideApp;
import app.fedilab.android.interfaces.OnPostActionInterface;
import app.fedilab.android.interfaces.OnRetrieveEmojiInterface;
import app.fedilab.android.interfaces.OnRetrieveRepliesInterface;

import static app.fedilab.android.helper.Helper.changeDrawableColor;


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
    private RetrieveFeedsAsyncTask.Type type;

    public PixelfedListAdapter(Context context, RetrieveFeedsAsyncTask.Type type, List<Status> statuses){
        super();
        this.context = context;
        this.statuses = statuses;
        this.type = type;
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
        ImageView art_media, pf_pp, pf_comment;
        SparkButton pf_fav, pf_share;
        TextView pf_username, pf_likes, pf_description, pf_date;
        CardView pf_cardview;
        LinearLayout pf_bottom_container;
        ViewHolderPixelfed(View itemView) {
            super(itemView);
            art_media = itemView.findViewById(R.id.art_media);
            pf_pp = itemView.findViewById(R.id.pf_pp);
            pf_username = itemView.findViewById(R.id.pf_username);
            pf_likes = itemView.findViewById(R.id.pf_likes);
            pf_description = itemView.findViewById(R.id.pf_description);
            pf_date = itemView.findViewById(R.id.pf_date);
            pf_fav = itemView.findViewById(R.id.pf_fav);
            pf_comment = itemView.findViewById(R.id.pf_comment);
            pf_share = itemView.findViewById(R.id.pf_share);
            pf_cardview = itemView.findViewById(R.id.pf_cardview);
            pf_bottom_container = itemView.findViewById(R.id.pf_bottom_container);
        }
    }




    public Status getItem(int position){
        if( statuses.size() > position && position >= 0)
            return statuses.get(position);
        else return null;
    }

    @Override
    public int getItemViewType(int position) {

        if( !Helper.filterToots(context, statuses.get(position), null))
            return HIDDEN_STATUS;
        else
            return DISPLAYED_STATUS;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if( viewType == DISPLAYED_STATUS)
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
                        .apply(new RequestOptions().transforms(new FitCenter(), new RoundedCorners(270)))
                        .into(holder.pf_pp);

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

            holder.pf_likes.setText(context.getResources().getQuantityString(R.plurals.likes, status.getFavourites_count(), status.getFavourites_count()));
            holder.pf_pp.setOnClickListener(new View.OnClickListener() {
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
            holder.pf_description.setText(status.getContentSpan(), TextView.BufferType.SPANNABLE);
            holder.pf_date.setText(Helper.dateToString(status.getCreated_at()));
            holder.pf_comment.setOnClickListener(new View.OnClickListener() {
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
                holder.pf_username.setText(status.getDisplayNameSpan(), TextView.BufferType.SPANNABLE);
            else
                holder.pf_username.setText(status.getAccount().getUsername());
            int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);



            if (theme == Helper.THEME_BLACK) {
                holder.pf_fav.setInActiveImageTint(R.color.action_black);
                holder.pf_share.setInActiveImageTint(R.color.action_black);
                Helper.changeDrawableColor(context, R.drawable.ic_pixelfed_favorite_border, R.color.action_black);
                Helper.changeDrawableColor(context, holder.pf_comment, R.color.action_black);
                holder.pf_cardview.setCardBackgroundColor(ContextCompat.getColor(context, R.color.black_3));
            } else if (theme == Helper.THEME_DARK) {
                holder.pf_fav.setInActiveImageTint(R.color.action_dark);
                holder.pf_share.setInActiveImageTint(R.color.action_dark);
                Helper.changeDrawableColor(context, holder.pf_comment, R.color.action_dark);
                Helper.changeDrawableColor(context, R.drawable.ic_pixelfed_favorite_border, R.color.action_dark);
                holder.pf_cardview.setCardBackgroundColor(ContextCompat.getColor(context, R.color.mastodonC1_));
            } else {
                holder.pf_fav.setInActiveImageTint(R.color.action_light);
                holder.pf_share.setInActiveImageTint(R.color.action_light);
                Helper.changeDrawableColor(context, holder.pf_comment, R.color.action_light);
                Helper.changeDrawableColor(context, R.drawable.ic_pixelfed_favorite_border, R.color.action_light);
                holder.pf_cardview.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white));
            }


            holder.pf_fav.pressOnTouch(false);
            holder.pf_fav.setActiveImage(R.drawable.ic_pixelfed_favorite);
            holder.pf_fav.setInactiveImage(R.drawable.ic_pixelfed_favorite_border);
            holder.pf_fav.setDisableCircle(true);
            holder.pf_fav.setActiveImageTint(R.color.pixelfed_like);
            holder.pf_fav.setColors(R.color.pixelfed_like, R.color.pixelfed_like);

            holder.pf_share.pressOnTouch(false);
            holder.pf_share.setActiveImage(R.drawable.ic_pixelfed_share);
            holder.pf_share.setInactiveImage(R.drawable.ic_pixelfed_share);
            holder.pf_share.setDisableCircle(true);
            holder.pf_share.setActiveImageTint(R.color.boost_icon);
            holder.pf_share.setColors(R.color.boost_icon, R.color.boost_icon);

            if (!status.isFavAnimated()) {
                if (status.isFavourited() || (status.getReblog() != null && status.getReblog().isFavourited())) {
                    holder.pf_fav.setChecked(true);
                } else {
                    holder.pf_fav.setChecked(false);
                }
            } else {
                status.setFavAnimated(false);
                holder.pf_fav.setChecked(true);
                holder.pf_fav.setAnimationSpeed(1.0f);
                holder.pf_fav.playAnimation();
            }

            if (!status.isBoostAnimated()) {
                if (status.isReblogged() || (status.getReblog() != null && status.getReblog().isReblogged())) {
                    holder.pf_share.setChecked(true);
                } else {
                    holder.pf_share.setChecked(false);
                }
            } else {
                status.setBoostAnimated(false);
                holder.pf_share.setChecked(true);
                holder.pf_share.setAnimationSpeed(1.0f);
                holder.pf_share.playAnimation();
            }
            boolean confirmFav = sharedpreferences.getBoolean(Helper.SET_NOTIF_VALIDATION_FAV, false);
            holder.pf_fav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!status.isFavourited() && confirmFav)
                        status.setFavAnimated(true);
                    if (!status.isFavourited() && !confirmFav) {
                        status.setFavAnimated(true);
                        notifyStatusChanged(status);
                    }
                    CrossActions.doCrossAction(context, type, status, null, (status.isFavourited() || (status.getReblog() != null && status.getReblog().isFavourited())) ? API.StatusAction.UNFAVOURITE : API.StatusAction.FAVOURITE, pixelfedListAdapter, PixelfedListAdapter.this, true);
                }
            });
            boolean confirmBoost = sharedpreferences.getBoolean(Helper.SET_NOTIF_VALIDATION, false);
            holder.pf_share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!status.isReblogged() && confirmBoost)
                        status.setBoostAnimated(true);
                    if (!status.isReblogged() && !confirmBoost) {
                        status.setBoostAnimated(true);
                        notifyStatusChanged(status);
                    }
                    CrossActions.doCrossAction(context, type, status, null, (status.isReblogged() || (status.getReblog() != null && status.getReblog().isReblogged())) ? API.StatusAction.UNREBLOG : API.StatusAction.REBLOG, pixelfedListAdapter, PixelfedListAdapter.this, true);
                }
            });
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

    public void notifyStatusWithActionChanged(Status status){
        for (int i = 0; i < pixelfedListAdapter.getItemCount(); i++) {
            //noinspection ConstantConditions
            if (pixelfedListAdapter.getItemAt(i) != null && pixelfedListAdapter.getItemAt(i).getId().equals(status.getId())) {
                try {
                    statuses.set(i, status);
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