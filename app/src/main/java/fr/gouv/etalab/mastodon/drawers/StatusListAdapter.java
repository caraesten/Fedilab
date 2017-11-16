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

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;

import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;


import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.activities.MediaActivity;
import fr.gouv.etalab.mastodon.activities.ShowAccountActivity;
import fr.gouv.etalab.mastodon.activities.ShowConversationActivity;
import fr.gouv.etalab.mastodon.activities.TootActivity;
import fr.gouv.etalab.mastodon.asynctasks.PostActionAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveFeedsAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveRepliesAsyncTask;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Attachment;
import fr.gouv.etalab.mastodon.client.Entities.Emojis;
import fr.gouv.etalab.mastodon.client.Entities.Error;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.translation.Translate;
import fr.gouv.etalab.mastodon.client.PatchBaseImageDownloader;
import fr.gouv.etalab.mastodon.helper.CrossActions;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnPostActionInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveEmojiInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveFeedsInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveRepliesInterface;
import fr.gouv.etalab.mastodon.interfaces.OnTranslatedInterface;

import static fr.gouv.etalab.mastodon.activities.MainActivity.currentLocale;
import static fr.gouv.etalab.mastodon.helper.Helper.THEME_DARK;
import static fr.gouv.etalab.mastodon.helper.Helper.changeDrawableColor;


/**
 * Created by Thomas on 24/04/2017.
 * Adapter for Status
 */
public class StatusListAdapter extends RecyclerView.Adapter implements OnPostActionInterface, OnTranslatedInterface, OnRetrieveFeedsInterface, OnRetrieveEmojiInterface, OnRetrieveRepliesInterface {

    private Context context;
    private List<Status> statuses;
    private LayoutInflater layoutInflater;
    private ImageLoader imageLoader;
    private boolean isOnWifi;
    private int translator;
    private int behaviorWithAttachments;
    private StatusListAdapter statusListAdapter;
    private RetrieveFeedsAsyncTask.Type type;
    private String targetedId;
    private final int DISPLAYED_STATUS = 1;
    private List<Status> pins;
    private int conversationPosition;

    public StatusListAdapter(Context context, RetrieveFeedsAsyncTask.Type type, String targetedId, boolean isOnWifi, int behaviorWithAttachments, int translator, List<Status> statuses){
        super();
        this.context = context;
        this.statuses = statuses;
        this.isOnWifi = isOnWifi;
        this.behaviorWithAttachments = behaviorWithAttachments;
        layoutInflater = LayoutInflater.from(this.context);
        statusListAdapter = this;
        this.type = type;
        this.targetedId = targetedId;
        this.translator = translator;
        pins = new ArrayList<>();
    }

    public StatusListAdapter(Context context, int position, String targetedId, boolean isOnWifi, int behaviorWithAttachments, int translator, List<Status> statuses){
        this.context = context;
        this.statuses = statuses;
        this.isOnWifi = isOnWifi;
        this.behaviorWithAttachments = behaviorWithAttachments;
        layoutInflater = LayoutInflater.from(this.context);
        statusListAdapter = this;
        this.type = RetrieveFeedsAsyncTask.Type.CONTEXT;
        this.conversationPosition = position;
        this.targetedId = targetedId;
        this.translator = translator;
        pins = new ArrayList<>();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return statuses.size();
    }

    @Override
    public void onRetrieveReplies(int position, APIResponse apiResponse) {
        if( apiResponse.getError() != null || apiResponse.getStatuses() == null || apiResponse.getStatuses().size() == 0){
            return;
        }
        List<Status> modifiedStatus = apiResponse.getStatuses();
        if( statuses !=null && statuses.size() > position && modifiedStatus != null && modifiedStatus.size() == 1){
            statuses.get(position).setReplies(modifiedStatus.get(0).getReplies());
            statusListAdapter.notifyItemChanged(position, modifiedStatus.get(0).getReplies());
        }
    }

    private class ViewHolderEmpty extends RecyclerView.ViewHolder{
        ViewHolderEmpty(View itemView) {
            super(itemView);
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        LinearLayout status_content_container;
        LinearLayout status_spoiler_container;
        TextView status_spoiler;
        Button status_spoiler_button;
        TextView status_content;
        TextView status_content_translated;
        LinearLayout status_content_translated_container;
        TextView status_account_username;
        TextView status_account_displayname;
        ImageView status_account_profile;
        ImageView status_account_profile_boost;
        ImageView status_account_profile_boost_by;
        TextView status_favorite_count;
        TextView status_reblog_count;
        TextView status_toot_date;
        Button status_show_more;
        ImageView status_more;
        LinearLayout status_document_container;
        ImageView status_prev1;
        ImageView status_prev2;
        ImageView status_prev3;
        ImageView status_prev4;
        ImageView status_prev1_play;
        ImageView status_prev2_play;
        ImageView status_prev3_play;
        ImageView status_prev4_play;
        RelativeLayout status_prev4_container;
        TextView status_reply;
        ImageView status_pin;
        ImageView status_privacy;
        FloatingActionButton status_translate;
        LinearLayout status_container2;
        LinearLayout status_container3;
        LinearLayout main_container;
        TextView yandex_translate;
        LinearLayout status_action_container;
        LinearLayout status_replies;
        LinearLayout status_replies_profile_pictures;
        ProgressBar loader_replies;

        ImageView new_element;

        public View getView(){
            return itemView;
        }

        ViewHolder(View itemView) {
            super(itemView);
            loader_replies = itemView.findViewById(R.id.loader_replies);
            status_document_container = itemView.findViewById(R.id.status_document_container);
            status_content = itemView.findViewById(R.id.status_content);
            status_content_translated = itemView.findViewById(R.id.status_content_translated);
            status_account_username = itemView.findViewById(R.id.status_account_username);
            status_account_displayname = itemView.findViewById(R.id.status_account_displayname);
            status_account_profile = itemView.findViewById(R.id.status_account_profile);
            status_account_profile_boost = itemView.findViewById(R.id.status_account_profile_boost);
            status_account_profile_boost_by = itemView.findViewById(R.id.status_account_profile_boost_by);
            status_favorite_count = itemView.findViewById(R.id.status_favorite_count);
            status_reblog_count = itemView.findViewById(R.id.status_reblog_count);
            status_pin = itemView.findViewById(R.id.status_pin);
            status_toot_date = itemView.findViewById(R.id.status_toot_date);
            status_show_more = itemView.findViewById(R.id.status_show_more);
            status_more = itemView.findViewById(R.id.status_more);
            status_prev1 = itemView.findViewById(R.id.status_prev1);
            status_prev2 = itemView.findViewById(R.id.status_prev2);
            status_prev3 = itemView.findViewById(R.id.status_prev3);
            status_prev4 = itemView.findViewById(R.id.status_prev4);
            status_prev1_play = itemView.findViewById(R.id.status_prev1_play);
            status_prev2_play = itemView.findViewById(R.id.status_prev2_play);
            status_prev3_play = itemView.findViewById(R.id.status_prev3_play);
            status_prev4_play = itemView.findViewById(R.id.status_prev4_play);
            status_container2 = itemView.findViewById(R.id.status_container2);
            status_container3 = itemView.findViewById(R.id.status_container3);
            status_prev4_container = itemView.findViewById(R.id.status_prev4_container);
            status_reply = itemView.findViewById(R.id.status_reply);
            status_privacy = itemView.findViewById(R.id.status_privacy);
            status_translate = itemView.findViewById(R.id.status_translate);
            status_content_translated_container = itemView.findViewById(R.id.status_content_translated_container);
            main_container = itemView.findViewById(R.id.main_container);
            status_spoiler_container = itemView.findViewById(R.id.status_spoiler_container);
            status_content_container = itemView.findViewById(R.id.status_content_container);
            status_spoiler = itemView.findViewById(R.id.status_spoiler);
            status_spoiler_button = itemView.findViewById(R.id.status_spoiler_button);
            yandex_translate = itemView.findViewById(R.id.yandex_translate);
            status_replies = itemView.findViewById(R.id.status_replies);
            status_replies_profile_pictures = itemView.findViewById(R.id.status_replies_profile_pictures);
            new_element = itemView.findViewById(R.id.new_element);
            status_action_container = itemView.findViewById(R.id.status_action_container);

        }
    }

    @Override
    public int getItemViewType(int position) {
        if( type == RetrieveFeedsAsyncTask.Type.HOME) {
            Status status = statuses.get(position);
            SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            int HIDDEN_STATUS = 0;
            if (status.getReblog() != null && !sharedpreferences.getBoolean(Helper.SET_SHOW_BOOSTS, true))
                return HIDDEN_STATUS;
            else if (status.getIn_reply_to_id() != null && !status.getIn_reply_to_id().equals("null") && !sharedpreferences.getBoolean(Helper.SET_SHOW_REPLIES, true)) {
                return HIDDEN_STATUS;
            }else
                return DISPLAYED_STATUS;
        }else {
            return DISPLAYED_STATUS;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if( viewType == DISPLAYED_STATUS)
            return new ViewHolder(layoutInflater.inflate(R.layout.drawer_status, parent, false));
        else
            return new ViewHolderEmpty(layoutInflater.inflate(R.layout.drawer_empty, parent, false));
    }

    /*
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position, List<Object> payload) {
        if( viewHolder.getItemViewType() == DISPLAYED_STATUS) {
            if (!payload.isEmpty()) {
                if (payload.get(0) instanceof Integer) {
                    final ViewHolder holder = (ViewHolder) viewHolder;
                    final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                    final Status status = statuses.get(position);
                    boolean showPreviewPP = sharedpreferences.getBoolean(Helper.SET_PREVIEW_REPLIES_PP, false);
                    if (showPreviewPP) {
                        ArrayList<String> addedPictures = new ArrayList<>();
                        holder.status_replies_profile_pictures.removeAllViews();
                        int i = 0;
                        for (Status replies : status.getReplies()) {
                            if (i > 10)
                                break;
                            if (!addedPictures.contains(replies.getAccount().getAcct())) {
                                DisplayImageOptions options = new DisplayImageOptions.Builder().displayer(new RoundedBitmapDisplayer(10)).cacheInMemory(false)
                                        .cacheOnDisk(true).resetViewBeforeLoading(true).build();
                                ImageView imageView = new ImageView(context);
                                imageView.setMaxHeight((int) Helper.convertDpToPixel(30, context));
                                imageView.setMaxWidth((int) Helper.convertDpToPixel(30, context));
                                imageLoader.displayImage(replies.getAccount().getAvatar(), imageView, options);
                                LinearLayout.LayoutParams imParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                imParams.setMargins(10, 5, 10, 5);
                                imParams.height = (int) Helper.convertDpToPixel(30, context);
                                imParams.width = (int) Helper.convertDpToPixel(30, context);
                                holder.status_replies_profile_pictures.addView(imageView, imParams);
                                i++;
                                addedPictures.add(replies.getAccount().getAcct());
                            }
                        }
                    }
                    if (status.getReplies() != null && status.getReplies().size() > 0)
                        holder.status_reply.setText(String.valueOf(status.getReplies().size()));
                    holder.status_replies.setVisibility(View.VISIBLE);
                    holder.loader_replies.setVisibility(View.GONE);
                }
            }
        }
    }*/

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {

        if( viewHolder.getItemViewType() == DISPLAYED_STATUS){
            final ViewHolder holder = (ViewHolder) viewHolder;
            final Status status = statuses.get(position);
            imageLoader = ImageLoader.getInstance();
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
            DisplayImageOptions options = new DisplayImageOptions.Builder().displayer(new RoundedBitmapDisplayer(10)).cacheInMemory(false)
                    .cacheOnDisk(true).resetViewBeforeLoading(true).build();

            final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            //Retrieves replies
            if( type == RetrieveFeedsAsyncTask.Type.HOME ) {
                boolean showPreview = sharedpreferences.getBoolean(Helper.SET_PREVIEW_REPLIES, false);
                //Retrieves attached replies to a toot
                if (showPreview && status.getReplies() == null) {
                    new RetrieveRepliesAsyncTask(context, position, status, StatusListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }


            final String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);

            holder.status_reply.setText("");
            //Display a preview for accounts that have replied *if enabled and only for home timeline*
            if( type == RetrieveFeedsAsyncTask.Type.HOME ) {
                boolean showPreview = sharedpreferences.getBoolean(Helper.SET_PREVIEW_REPLIES, false);
                if( showPreview){
                    boolean showPreviewPP = sharedpreferences.getBoolean(Helper.SET_PREVIEW_REPLIES_PP, false);
                    if(  status.getReplies() == null){
                        holder.loader_replies.setVisibility(View.VISIBLE);
                    }else if(status.getReplies().size() == 0){
                        holder.status_replies.setVisibility(View.GONE);
                        holder.loader_replies.setVisibility(View.GONE);
                    }else if(status.getReplies().size() > 0 ){
                        if(showPreviewPP) {
                            ArrayList<String> addedPictures = new ArrayList<>();
                            holder.status_replies_profile_pictures.removeAllViews();
                            int i = 0;
                            for (Status replies : status.getReplies()) {
                                if (i > 10)
                                    break;
                                if (!addedPictures.contains(replies.getAccount().getAcct())) {
                                    ImageView imageView = new ImageView(context);
                                    imageView.setMaxHeight((int) Helper.convertDpToPixel(30, context));
                                    imageView.setMaxWidth((int) Helper.convertDpToPixel(30, context));
                                    imageLoader.displayImage(replies.getAccount().getAvatar(), imageView, options);
                                    LinearLayout.LayoutParams imParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                    imParams.setMargins(10, 5, 10, 5);
                                    imParams.height = (int) Helper.convertDpToPixel(30, context);
                                    imParams.width = (int) Helper.convertDpToPixel(30, context);
                                    holder.status_replies_profile_pictures.addView(imageView, imParams);
                                    i++;
                                    addedPictures.add(replies.getAccount().getAcct());
                                }
                            }
                        }
                        if( status.getReplies() != null && status.getReplies().size() > 0 )
                            holder.status_reply.setText(String.valueOf(status.getReplies().size()));
                        holder.status_replies.setVisibility(View.VISIBLE);
                        holder.loader_replies.setVisibility(View.GONE);
                    }
                }else{
                    holder.loader_replies.setVisibility(View.GONE);
                    holder.status_replies.setVisibility(View.GONE);
                }
            }
            changeDrawableColor(context, R.drawable.ic_fiber_new,R.color.mastodonC4);
            if( status.isNew())
                holder.new_element.setVisibility(View.VISIBLE);
            else
                holder.new_element.setVisibility(View.GONE);
            int iconSizePercent = sharedpreferences.getInt(Helper.SET_ICON_SIZE, 130);
            int textSizePercent = sharedpreferences.getInt(Helper.SET_TEXT_SIZE, 110);
            boolean trans_forced = sharedpreferences.getBoolean(Helper.SET_TRANS_FORCED, false);
            holder.status_more.getLayoutParams().height = (int) Helper.convertDpToPixel((20*iconSizePercent/100), context);
            holder.status_more.getLayoutParams().width = (int) Helper.convertDpToPixel((20*iconSizePercent/100), context);
            holder.status_privacy.getLayoutParams().height = (int) Helper.convertDpToPixel((20*iconSizePercent/100), context);
            holder.status_privacy.getLayoutParams().width = (int) Helper.convertDpToPixel((20*iconSizePercent/100), context);
            holder.status_content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14*textSizePercent/100);
            holder.status_account_displayname.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14*textSizePercent/100);
            holder.status_account_username.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12*textSizePercent/100);
            holder.status_toot_date.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12*textSizePercent/100);
            holder.status_spoiler.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14*textSizePercent/100);
            holder.status_content_translated.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14*textSizePercent/100);
            switch (translator) {
                case Helper.TRANS_NONE:
                    holder.yandex_translate.setVisibility(View.GONE);
                    break;
                case Helper.TRANS_YANDEX:
                    holder.yandex_translate.setVisibility(View.VISIBLE);
                    break;
                default:
                    holder.yandex_translate.setVisibility(View.VISIBLE);
            }

            //Manages theme for icon colors
            int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
            if( theme == Helper.THEME_DARK){
                changeDrawableColor(context, R.drawable.ic_reply,R.color.dark_icon);
                changeDrawableColor(context, holder.status_more, R.color.dark_icon);
                changeDrawableColor(context, holder.status_privacy, R.color.dark_icon);
                changeDrawableColor(context, R.drawable.ic_repeat,R.color.dark_icon);
                changeDrawableColor(context, R.drawable.ic_star_border,R.color.dark_icon);
                changeDrawableColor(context, R.drawable.ic_pin_drop, R.color.dark_icon);
                changeDrawableColor(context, R.drawable.ic_photo,R.color.dark_text);
                changeDrawableColor(context, R.drawable.ic_remove_red_eye,R.color.dark_text);
                changeDrawableColor(context, R.drawable.ic_translate,R.color.dark_text);
            }else {
                changeDrawableColor(context, R.drawable.ic_reply,R.color.black);
                changeDrawableColor(context, R.drawable.ic_more_horiz,R.color.black);
                changeDrawableColor(context, holder.status_more, R.color.black);
                changeDrawableColor(context, holder.status_privacy, R.color.black);
                changeDrawableColor(context, R.drawable.ic_repeat,R.color.black);
                changeDrawableColor(context, R.drawable.ic_star_border,R.color.black);
                changeDrawableColor(context, R.drawable.ic_pin_drop, R.color.black);
                changeDrawableColor(context, R.drawable.ic_photo,R.color.mastodonC4);
                changeDrawableColor(context, R.drawable.ic_remove_red_eye,R.color.mastodonC4);
                changeDrawableColor(context, R.drawable.ic_translate,R.color.white);
            }

            //Redraws top icons (boost/reply)
            final float scale = context.getResources().getDisplayMetrics().density;
            if( status.getReblog() != null){
                Drawable img = ContextCompat.getDrawable(context, R.drawable.ic_repeat);
                img.setBounds(0,0,(int) (20 * iconSizePercent/100 * scale + 0.5f),(int) (15 * iconSizePercent/100 * scale + 0.5f));
                holder.status_account_displayname.setCompoundDrawables( img, null, null, null);
                holder.status_account_displayname.setVisibility(View.VISIBLE);
            }else{
                holder.status_account_displayname.setVisibility(View.GONE);
            }

            if( theme == THEME_DARK){
                holder.status_favorite_count.setTextColor(ContextCompat.getColor(context, R.color.dark_icon));
                holder.status_reblog_count.setTextColor(ContextCompat.getColor(context, R.color.dark_icon));
                holder.status_reply.setTextColor(ContextCompat.getColor(context, R.color.dark_icon));
                holder.status_toot_date.setTextColor(ContextCompat.getColor(context, R.color.dark_icon));
                holder.status_account_displayname.setTextColor(ContextCompat.getColor(context, R.color.dark_icon));
            }else {
                holder.status_favorite_count.setTextColor(ContextCompat.getColor(context, R.color.black));
                holder.status_reblog_count.setTextColor(ContextCompat.getColor(context, R.color.black));
                holder.status_toot_date.setTextColor(ContextCompat.getColor(context, R.color.black));
                holder.status_reply.setTextColor(ContextCompat.getColor(context, R.color.black));
                holder.status_account_displayname.setTextColor(ContextCompat.getColor(context, R.color.black));
            }

            String content;
            final String displayName;
            final String username;
            final String ppurl;
            String name;
            if( status.getReblog() != null){
                content = status.getReblog().getContent();
                displayName = Helper.shortnameToUnicode(status.getReblog().getAccount().getDisplay_name(), true);
                username = status.getReblog().getAccount().getUsername();
                holder.status_account_displayname.setText(String.format("%s @%s",displayName, username));
                ppurl = status.getReblog().getAccount().getAvatar();
                holder.status_account_displayname.setVisibility(View.VISIBLE);
                holder.status_account_displayname.setText(context.getResources().getString(R.string.reblog_by, status.getAccount().getUsername()));
                name = String.format("%s @%s",displayName,status.getReblog().getAccount().getAcct());
            }else {
                ppurl = status.getAccount().getAvatar();
                content = status.getContent();
                displayName = Helper.shortnameToUnicode(status.getAccount().getDisplay_name(), true);
                name = String.format("%s @%s",displayName,status.getAccount().getAcct());
            }

            Spannable wordtoSpan = new SpannableString(name);
            if( theme == THEME_DARK) {
                Pattern hashAcct;
                if( status.getReblog() != null)
                    hashAcct = Pattern.compile("\\s(@"+status.getReblog().getAccount().getAcct()+")");
                else
                    hashAcct = Pattern.compile("\\s(@"+status.getAccount().getAcct()+")");
                Matcher matcherAcct = hashAcct.matcher(wordtoSpan);
                while (matcherAcct.find()){
                    int matchStart = matcherAcct.start(1);
                    int matchEnd = matcherAcct.end();
                    if( wordtoSpan.length() >= matchEnd && matchStart < matchEnd)
                        wordtoSpan.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.dark_icon)), matchStart, matchEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                }
            }
            holder.status_account_username.setText(wordtoSpan);


            if( status.getContent_translated() != null && status.getContent_translated().length() > 0){
                holder.status_content_translated.setMovementMethod(null);
                SpannableString spannableStringTrans = Helper.clickableElements(context,status.getContent_translated(),
                        status.getReblog() != null?status.getReblog().getMentions():status.getMentions(),
                        status.getReblog() != null?status.getReblog().getEmojis():status.getEmojis(),
                        position,
                        true, StatusListAdapter.this);
                holder.status_content_translated.setText(spannableStringTrans, TextView.BufferType.SPANNABLE);
                holder.status_content_translated.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        holder.status_content_translated.setFocusableInTouchMode(true);
                        return false;
                    }
                });
                holder.status_content_translated.setMovementMethod(LinkMovementMethod.getInstance());
            }

            if( status.getContents() != null){
                holder.status_content.setText(status.getContents(), TextView.BufferType.SPANNABLE);
            }else{
                content = content.replaceAll("</p>","<br/><br/>");
                content = content.replaceAll("<p>","");
                if( content.endsWith("<br/><br/>") )
                    content = content.substring(0,content.length() -10);
                holder.status_content.setMovementMethod(null);
                final SpannableString spannableString = Helper.clickableElements(context,content,
                        status.getReblog() != null?status.getReblog().getMentions():status.getMentions(),
                        status.getReblog() != null?status.getReblog().getEmojis():status.getEmojis(),
                        position,
                        true, StatusListAdapter.this);
                holder.status_content.setText(spannableString, TextView.BufferType.SPANNABLE);
            }
            holder.status_content.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    holder.status_content.setFocusableInTouchMode(true);
                    return false;
                }
            });

            holder.status_content.setMovementMethod(LinkMovementMethod.getInstance());

            if( status.getReblog() == null)
                holder.status_favorite_count.setText(String.valueOf(status.getFavourites_count()));
            else
                holder.status_favorite_count.setText(String.valueOf(status.getReblog().getFavourites_count()));
            if( status.getReblog() == null)
                holder.status_reblog_count.setText(String.valueOf(status.getReblogs_count()));
            else
                holder.status_reblog_count.setText(String.valueOf(status.getReblog().getReblogs_count()));

            holder.status_toot_date.setText(Helper.dateDiff(context, status.getCreated_at()));


            if( status.getReblog() != null) {
                imageLoader.displayImage(ppurl, holder.status_account_profile_boost, options);
                imageLoader.displayImage(status.getAccount().getAvatar(), holder.status_account_profile_boost_by, options);
                holder.status_account_profile_boost.setVisibility(View.VISIBLE);
                holder.status_account_profile_boost_by.setVisibility(View.VISIBLE);
                holder.status_account_profile.setVisibility(View.GONE);
            }else{
                imageLoader.displayImage(ppurl, holder.status_account_profile, options);
                holder.status_account_profile_boost.setVisibility(View.GONE);
                holder.status_account_profile_boost_by.setVisibility(View.GONE);
                holder.status_account_profile.setVisibility(View.VISIBLE);
            }
            if( status.isTakingScreenShot()){
                holder.status_document_container.setVisibility(View.GONE);
                holder.status_content.setVisibility(View.VISIBLE);
                holder.status_content_translated_container.setVisibility(View.GONE);
                holder.status_spoiler_button.setVisibility(View.GONE);
                holder.status_content_container.setVisibility(View.VISIBLE);
                holder.status_translate.setVisibility(View.GONE);
                holder.status_show_more.setVisibility(View.GONE);
                holder.status_action_container.setVisibility(View.INVISIBLE);
            }else {
                holder.status_action_container.setVisibility(View.VISIBLE);
                if( trans_forced || (translator != Helper.TRANS_NONE && currentLocale != null && status.getLanguage() != null && !status.getLanguage().trim().equals(currentLocale))){
                    holder.status_translate.setVisibility(View.VISIBLE);
                }else {
                    holder.status_translate.setVisibility(View.GONE);
                }
                if( status.getReblog() == null) {
                    if (status.getSpoiler_text() != null && status.getSpoiler_text().trim().length() > 0 && !status.isSpoilerShown()) {
                        holder.status_content_container.setVisibility(View.GONE);
                        holder.status_spoiler_container.setVisibility(View.VISIBLE);
                        holder.status_spoiler_button.setVisibility(View.VISIBLE);
                        holder.status_spoiler.setVisibility(View.VISIBLE);
                    } else {
                        holder.status_spoiler_button.setVisibility(View.GONE);
                        holder.status_content_container.setVisibility(View.VISIBLE);
                        if (status.getSpoiler_text() != null && status.getSpoiler_text().trim().length() > 0)
                            holder.status_spoiler_container.setVisibility(View.VISIBLE);
                        else
                            holder.status_spoiler_container.setVisibility(View.GONE);
                    }
                }else {
                    if (status.getReblog().getSpoiler_text() != null && status.getReblog().getSpoiler_text().trim().length() > 0 && !status.isSpoilerShown()) {
                        holder.status_content_container.setVisibility(View.GONE);
                        holder.status_spoiler_container.setVisibility(View.VISIBLE);
                        holder.status_spoiler_button.setVisibility(View.VISIBLE);
                        holder.status_spoiler.setVisibility(View.VISIBLE);
                    } else {
                        holder.status_spoiler_button.setVisibility(View.GONE);
                        holder.status_content_container.setVisibility(View.VISIBLE);
                        if (status.getReblog().getSpoiler_text() != null && status.getReblog().getSpoiler_text().trim().length() > 0)
                            holder.status_spoiler_container.setVisibility(View.VISIBLE);
                        else
                            holder.status_spoiler_container.setVisibility(View.GONE);
                    }
                }

                if( status.getReblog() == null) {
                    if( status.getSpoiler_text() != null)
                        holder.status_spoiler.setText(status.getSpoiler_text());
                    if (status.getMedia_attachments().size() < 1) {
                        holder.status_document_container.setVisibility(View.GONE);
                        holder.status_show_more.setVisibility(View.GONE);
                    } else {
                        //If medias are loaded without any conditions or if device is on wifi
                        if (!status.isSensitive() && (behaviorWithAttachments == Helper.ATTACHMENT_ALWAYS || (behaviorWithAttachments == Helper.ATTACHMENT_WIFI && isOnWifi))) {
                            loadAttachments(status, holder);
                            holder.status_show_more.setVisibility(View.GONE);
                            status.setAttachmentShown(true);
                        } else {
                            //Text depending if toots is sensitive or not
                            String textShowMore = (status.isSensitive()) ? context.getString(R.string.load_sensitive_attachment) : context.getString(R.string.load_attachment);
                            holder.status_show_more.setText(textShowMore);
                            if (!status.isAttachmentShown()) {
                                holder.status_show_more.setVisibility(View.VISIBLE);
                                holder.status_document_container.setVisibility(View.GONE);
                            } else {
                                loadAttachments(status, holder);
                            }
                        }
                    }
                }else { //Attachments for reblogs
                    if( status.getReblog().getSpoiler_text() != null)
                        holder.status_spoiler.setText(status.getReblog().getSpoiler_text());
                    if (status.getReblog().getMedia_attachments().size() < 1) {
                        holder.status_document_container.setVisibility(View.GONE);
                        holder.status_show_more.setVisibility(View.GONE);
                    } else {
                        //If medias are loaded without any conditions or if device is on wifi
                        if (!status.getReblog().isSensitive() && (behaviorWithAttachments == Helper.ATTACHMENT_ALWAYS || (behaviorWithAttachments == Helper.ATTACHMENT_WIFI && isOnWifi))) {
                            loadAttachments(status.getReblog(), holder);
                            holder.status_show_more.setVisibility(View.GONE);
                            status.getReblog().setAttachmentShown(true);
                        } else {
                            //Text depending if toots is sensitive or not
                            String textShowMore = (status.getReblog().isSensitive()) ? context.getString(R.string.load_sensitive_attachment) : context.getString(R.string.load_attachment);
                            holder.status_show_more.setText(textShowMore);
                            if (!status.isAttachmentShown()) {
                                holder.status_show_more.setVisibility(View.VISIBLE);
                                holder.status_document_container.setVisibility(View.GONE);
                            } else {
                                loadAttachments(status.getReblog(), holder);
                            }
                        }
                    }
                }

                Typeface tf = Typeface.createFromAsset(context.getAssets(), "fonts/DroidSans-Regular.ttf");
                holder.status_content.setTypeface(tf);

                //Toot was translated and user asked to see it
                if( status.isTranslationShown()){
                    holder.status_content.setVisibility(View.GONE);
                    holder.status_content_translated_container.setVisibility(View.VISIBLE);
                    holder.status_content_translated.setTypeface(tf);
                }else { //Toot is not translated
                    holder.status_content.setVisibility(View.VISIBLE);
                    holder.status_content_translated_container.setVisibility(View.GONE);
                }

                switch (status.getVisibility()){
                    case "direct":
                    case "private":
                        holder.status_reblog_count.setVisibility(View.GONE);
                        break;
                    case "public":
                    case "unlisted":
                        holder.status_reblog_count.setVisibility(View.VISIBLE);
                        break;
                    default:
                        holder.status_reblog_count.setVisibility(View.VISIBLE);
                }

                switch (status.getVisibility()){
                    case "public":
                        holder.status_privacy.setImageResource(R.drawable.ic_public);
                        break;
                    case "unlisted":
                        holder.status_privacy.setImageResource(R.drawable.ic_lock_open);
                        break;
                    case "private":
                        holder.status_privacy.setImageResource(R.drawable.ic_lock_outline);
                        break;
                    case "direct":
                        holder.status_privacy.setImageResource(R.drawable.ic_mail_outline);
                        break;
                }

                Drawable imgFav, imgReblog, imgReply;
                if( status.isFavourited() || (status.getReblog() != null && status.getReblog().isFavourited())) {
                    changeDrawableColor(context, R.drawable.ic_star,R.color.marked_icon);
                    imgFav = ContextCompat.getDrawable(context, R.drawable.ic_star);
                }else {
                    if( theme == THEME_DARK)
                        changeDrawableColor(context, R.drawable.ic_star_border,R.color.dark_icon);
                    else
                        changeDrawableColor(context, R.drawable.ic_star_border,R.color.black);
                    imgFav = ContextCompat.getDrawable(context, R.drawable.ic_star_border);
                }

                if( status.isReblogged()|| (status.getReblog() != null && status.getReblog().isReblogged())) {
                    changeDrawableColor(context, R.drawable.ic_repeat_boost,R.color.boost_icon);
                    imgReblog = ContextCompat.getDrawable(context, R.drawable.ic_repeat_boost);
                }else {
                    if( theme == THEME_DARK)
                        changeDrawableColor(context, R.drawable.ic_repeat,R.color.dark_icon);
                    else
                        changeDrawableColor(context, R.drawable.ic_repeat,R.color.black);
                    imgReblog = ContextCompat.getDrawable(context, R.drawable.ic_repeat);
                }


                if( theme == THEME_DARK)
                    changeDrawableColor(context, R.drawable.ic_reply,R.color.dark_icon);
                else
                    changeDrawableColor(context, R.drawable.ic_reply,R.color.black);
                imgReply = ContextCompat.getDrawable(context, R.drawable.ic_reply);


                imgFav.setBounds(0,0,(int) (20 * iconSizePercent/100 * scale + 0.5f),(int) (20 * iconSizePercent/100 * scale + 0.5f));
                imgReblog.setBounds(0,0,(int) (20 * iconSizePercent/100 * scale + 0.5f),(int) (20 * iconSizePercent/100 * scale + 0.5f));
                imgReply.setBounds(0,0,(int) (20 * iconSizePercent/100 * scale + 0.5f),(int) (20 * iconSizePercent/100 * scale + 0.5f));

                holder.status_favorite_count.setCompoundDrawables(imgFav, null, null, null);
                holder.status_reblog_count.setCompoundDrawables(imgReblog, null, null, null);
                holder.status_reply.setCompoundDrawables(imgReply, null, null, null);


                boolean isOwner = status.getAccount().getId().equals(userId);

                // Pinning toots is only available on Mastodon 1._6_.0 instances.
                if (isOwner && Helper.canPin && (status.getVisibility().equals("public") || status.getVisibility().equals("unlisted")) && status.getReblog() == null) {
                    Drawable imgPin;
                    if( status.isPinned()|| (status.getReblog() != null && status.getReblog().isPinned())) {
                        changeDrawableColor(context, R.drawable.ic_pin_drop_p,R.color.marked_icon);
                        imgPin = ContextCompat.getDrawable(context, R.drawable.ic_pin_drop_p);
                    }else {
                        if( theme == THEME_DARK)
                            changeDrawableColor(context, R.drawable.ic_pin_drop,R.color.dark_icon);
                        else
                            changeDrawableColor(context, R.drawable.ic_pin_drop,R.color.black);
                        imgPin = ContextCompat.getDrawable(context, R.drawable.ic_pin_drop);
                    }
                    imgPin.setBounds(0,0,(int) (20 * iconSizePercent/100 * scale + 0.5f),(int) (20 * iconSizePercent/100 * scale + 0.5f));
                    holder.status_pin.setImageDrawable(imgPin);

                    holder.status_pin.setVisibility(View.VISIBLE);
                }
                else {
                    holder.status_pin.setVisibility(View.GONE);
                }
            }

            //Click on a conversation
            if( type != RetrieveFeedsAsyncTask.Type.CONTEXT ){
                holder.status_content.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, ShowConversationActivity.class);
                        Bundle b = new Bundle();
                        if( status.getReblog() == null)
                            b.putString("statusId", status.getId());
                        else
                            b.putString("statusId", status.getReblog().getId());
                        intent.putExtras(b);
                        context.startActivity(intent);
                    }
                });
                holder.main_container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, ShowConversationActivity.class);
                        Bundle b = new Bundle();
                        if( status.getReblog() == null)
                            b.putString("statusId", status.getId());
                        else
                            b.putString("statusId", status.getReblog().getId());
                        intent.putExtras(b);
                        context.startActivity(intent);
                    }
                });
                if( theme == Helper.THEME_LIGHT){
                    holder.main_container.setBackgroundResource(R.color.mastodonC3__);
                }else {
                    holder.main_container.setBackgroundResource(R.color.mastodonC1_);
                }
            }else {
                if( theme == Helper.THEME_LIGHT){
                    if( position == conversationPosition){
                        holder.main_container.setBackgroundResource(R.color.mastodonC3_);
                    }else {
                        holder.main_container.setBackgroundResource(R.color.mastodonC3__);
                    }
                }else {
                    if( position == conversationPosition){
                        holder.main_container.setBackgroundResource(R.color.mastodonC1___);
                    }else {
                        holder.main_container.setBackgroundResource(R.color.mastodonC1_);
                    }
                }
            }

            holder.status_reply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CrossActions.doCrossReply(context, status, type, true);
                }
            });

            holder.status_reply.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    CrossActions.doCrossReply(context, status, type, false);
                    return true;
                }
            });

            holder.status_favorite_count.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CrossActions.doCrossAction(context, status, (status.isFavourited()|| (status.getReblog() != null && status.getReblog().isFavourited()))? API.StatusAction.UNFAVOURITE:API.StatusAction.FAVOURITE, statusListAdapter, StatusListAdapter.this, true);
                }
            });

            holder.status_reblog_count.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CrossActions.doCrossAction(context, status, (status.isReblogged()|| (status.getReblog() != null && status.getReblog().isReblogged()))? API.StatusAction.UNREBLOG:API.StatusAction.REBLOG, statusListAdapter, StatusListAdapter.this, true);
                }
            });
            holder.status_pin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CrossActions.doCrossAction(context, status, (status.isPinned()|| (status.getReblog() != null && status.getReblog().isPinned()))? API.StatusAction.UNPIN:API.StatusAction.PIN, statusListAdapter, StatusListAdapter.this, true);
                }
            });


            holder.status_favorite_count.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    CrossActions.doCrossAction(context, status, (status.isFavourited()|| (status.getReblog() != null && status.getReblog().isFavourited()))? API.StatusAction.UNFAVOURITE:API.StatusAction.FAVOURITE, statusListAdapter, StatusListAdapter.this, false);
                    return true;
                }
            });

            holder.status_reblog_count.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    CrossActions.doCrossAction(context, status, (status.isReblogged()|| (status.getReblog() != null && status.getReblog().isReblogged()))? API.StatusAction.UNREBLOG:API.StatusAction.REBLOG, statusListAdapter, StatusListAdapter.this, false);
                    return true;
                }
            });
            holder.status_pin.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    CrossActions.doCrossAction(context, status, (status.isPinned()|| (status.getReblog() != null && status.getReblog().isPinned()))? API.StatusAction.UNPIN:API.StatusAction.PIN, statusListAdapter, StatusListAdapter.this, true);
                    return false;
                }
            });

            holder.status_translate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if( !status.isTranslated() ){
                        new Translate(context, status,StatusListAdapter.this).privacy(status.getContent());
                    }else {
                        status.setTranslationShown(!status.isTranslationShown());
                        statusListAdapter.notifyDataSetChanged();
                    }
                }
            });

            holder.yandex_translate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://translate.yandex.com/"));
                    context.startActivity(browserIntent);
                }
            });
            //Spoiler opens
            holder.status_spoiler_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    status.setSpoilerShown(true);
                    holder.status_spoiler_button.setVisibility(View.GONE);
                    statusListAdapter.notifyDataSetChanged();
                }
            });

            holder.status_show_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadAttachments(status, holder);
                    holder.status_show_more.setVisibility(View.GONE);
                    status.setAttachmentShown(true);
                    statusListAdapter.notifyDataSetChanged();

                /*
                    Added a Countdown Timer, so that Sensitive (NSFW)
                    images only get displayed for user set time,
                    giving the user time to click on them to expand them,
                    if they want. Images are then hidden again.
                    -> Default value is set to 5 seconds
                 */
                    final int timeout = sharedpreferences.getInt(Helper.SET_NSFW_TIMEOUT, 5);

                    if (timeout > 0) {

                        new CountDownTimer((timeout * 1000), 1000) {

                            public void onTick(long millisUntilFinished) {
                            }

                            public void onFinish() {
                                status.setAttachmentShown(false);
                                holder.status_show_more.setVisibility(View.VISIBLE);

                                statusListAdapter.notifyDataSetChanged();
                            }
                        }.start();
                    }
                }
            });



            final View attached = holder.status_more;
            holder.status_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popup = new PopupMenu(context, attached);
                    final boolean isOwner = status.getAccount().getId().equals(userId);
                    popup.getMenuInflater()
                            .inflate(R.menu.option_toot, popup.getMenu());
                    if( status.getVisibility().equals("private") || status.getVisibility().equals("direct")){
                        popup.getMenu().findItem(R.id.action_mention).setVisible(false);
                    }
                    final String[] stringArrayConf;
                    if( isOwner) {
                        popup.getMenu().findItem(R.id.action_block).setVisible(false);
                        popup.getMenu().findItem(R.id.action_mute).setVisible(false);
                        popup.getMenu().findItem(R.id.action_report).setVisible(false);
                        stringArrayConf =  context.getResources().getStringArray(R.array.more_action_owner_confirm);
                    }else {
                        popup.getMenu().findItem(R.id.action_remove).setVisible(false);
                        stringArrayConf =  context.getResources().getStringArray(R.array.more_action_confirm);
                    }
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            AlertDialog.Builder builderInner;
                            final API.StatusAction doAction;
                            switch (item.getItemId()) {
                                case R.id.action_remove:
                                    builderInner = new AlertDialog.Builder(context);
                                    builderInner.setTitle(stringArrayConf[0]);
                                    doAction = API.StatusAction.UNSTATUS;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                        builderInner.setMessage(Html.fromHtml(status.getContent(), Html.FROM_HTML_MODE_LEGACY));
                                    else
                                        //noinspection deprecation
                                        builderInner.setMessage(Html.fromHtml(status.getContent()));
                                    break;
                                case R.id.action_mute:
                                    builderInner = new AlertDialog.Builder(context);
                                    builderInner.setTitle(stringArrayConf[0]);
                                    doAction = API.StatusAction.MUTE;
                                    break;
                                case R.id.action_block:
                                    builderInner = new AlertDialog.Builder(context);
                                    builderInner.setTitle(stringArrayConf[1]);
                                    doAction = API.StatusAction.BLOCK;
                                    break;
                                case R.id.action_report:
                                    builderInner = new AlertDialog.Builder(context);
                                    builderInner.setTitle(stringArrayConf[2]);
                                    doAction = API.StatusAction.REPORT;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                        builderInner.setMessage(Html.fromHtml(status.getContent(), Html.FROM_HTML_MODE_LEGACY));
                                    else
                                        //noinspection deprecation
                                        builderInner.setMessage(Html.fromHtml(status.getContent()));
                                    break;
                                case R.id.action_copy:
                                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                    String content;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                        content = Html.fromHtml(status.getContent(), Html.FROM_HTML_MODE_LEGACY).toString();
                                    else
                                        //noinspection deprecation
                                        content = Html.fromHtml(status.getContent()).toString();
                                    ClipData clip = ClipData.newPlainText(Helper.CLIP_BOARD, content);
                                    if( clipboard != null) {
                                        clipboard.setPrimaryClip(clip);
                                        Toast.makeText(context, R.string.clipboard, Toast.LENGTH_LONG).show();
                                    }
                                    return true;
                                case R.id.action_share:
                                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                                    sendIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.shared_via));
                                    sendIntent.putExtra(Intent.EXTRA_TEXT, status.getUrl());
                                    sendIntent.setType("text/plain");
                                    context.startActivity(Intent.createChooser(sendIntent, context.getString(R.string.share_with)));
                                    return true;
                                case R.id.action_mention:
                                    status.setTakingScreenShot(true);
                                    statusListAdapter.notifyDataSetChanged();
                                    // Get a handler that can be used to post to the main thread
                                    final Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Bitmap bitmap = Helper.convertTootIntoBitmap(context, holder.getView());
                                            status.setTakingScreenShot(false);
                                            statusListAdapter.notifyDataSetChanged();
                                            Intent intent = new Intent(context, TootActivity.class);
                                            Bundle b = new Bundle();
                                            String fname = "tootmention_" + status.getId() +".jpg";
                                            File file = new File (context.getCacheDir() + "/", fname);
                                            if (file.exists ()) //noinspection ResultOfMethodCallIgnored
                                                file.delete ();
                                            try {
                                                FileOutputStream out = new FileOutputStream(file);
                                                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                                                out.flush();
                                                out.close();
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            b.putString("fileMention", fname);
                                            b.putString("tootMention", (status.getReblog() != null)?status.getReblog().getAccount().getAcct():status.getAccount().getAcct());
                                            b.putString("urlMention", (status.getReblog() != null)?status.getReblog().getUrl():status.getUrl());
                                            intent.putExtras(b);
                                            context.startActivity(intent);
                                        }
                                    }, 500);
                                    return true;
                                default:
                                    return true;
                            }

                            //Text for report
                            EditText input = null;
                            if( doAction == API.StatusAction.REPORT){
                                input = new EditText(context);
                                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT);
                                input.setLayoutParams(lp);
                                builderInner.setView(input);
                            }
                            builderInner.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,int which) {
                                    dialog.dismiss();
                                }
                            });
                            final EditText finalInput = input;
                            builderInner.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,int which) {
                                    if(doAction ==  API.StatusAction.UNSTATUS ){
                                        String targetedId = status.getId();
                                        new PostActionAsyncTask(context, doAction, targetedId, StatusListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                    }else if(doAction ==  API.StatusAction.REPORT ){
                                        String comment = null;
                                        if( finalInput.getText() != null)
                                            comment = finalInput.getText().toString();
                                        new PostActionAsyncTask(context, doAction, status.getId(), status, comment, StatusListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                    }else{
                                        String targetedId = status.getAccount().getId();
                                        new PostActionAsyncTask(context, doAction, targetedId, StatusListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                    }
                                    dialog.dismiss();
                                }
                            });
                            builderInner.show();
                            return true;
                        }
                    });
                    popup.show();
                }
            });


            holder.status_account_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if( targetedId == null || !targetedId.equals(status.getAccount().getId())){
                        Intent intent = new Intent(context, ShowAccountActivity.class);
                        Bundle b = new Bundle();
                        b.putString("accountId", status.getAccount().getId());
                        intent.putExtras(b);
                        context.startActivity(intent);
                    }
                }
            });

            holder.status_account_profile_boost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if( targetedId == null || !targetedId.equals(status.getReblog().getAccount().getId())){
                        Intent intent = new Intent(context, ShowAccountActivity.class);
                        Bundle b = new Bundle();
                        b.putString("accountId", status.getReblog().getAccount().getId());
                        intent.putExtras(b);
                        context.startActivity(intent);
                    }
                }
            });
        }
    }



    private void loadAttachments(final Status status, ViewHolder holder){
        List<Attachment> attachments = status.getMedia_attachments();
        if( attachments != null && attachments.size() > 0){
            int i = 0;
            holder.status_document_container.setVisibility(View.VISIBLE);
            if( attachments.size() == 1){
                holder.status_container2.setVisibility(View.GONE);
                if( attachments.get(0).getUrl().trim().contains("missing.png"))
                    holder.status_document_container.setVisibility(View.GONE);
            }else if(attachments.size() == 2){
                holder.status_container2.setVisibility(View.VISIBLE);
                holder.status_container3.setVisibility(View.GONE);
                holder.status_prev4_container.setVisibility(View.GONE);
                if( attachments.get(1).getUrl().trim().contains("missing.png"))
                    holder.status_container2.setVisibility(View.GONE);
            }else if( attachments.size() == 3){
                holder.status_container2.setVisibility(View.VISIBLE);
                holder.status_container3.setVisibility(View.VISIBLE);
                holder.status_prev4_container.setVisibility(View.GONE);
                if( attachments.get(2).getUrl().trim().contains("missing.png"))
                    holder.status_container3.setVisibility(View.GONE);
            }else {
                holder.status_container2.setVisibility(View.VISIBLE);
                holder.status_container3.setVisibility(View.VISIBLE);
                holder.status_prev4_container.setVisibility(View.VISIBLE);
                if( attachments.get(2).getUrl().trim().contains("missing.png"))
                    holder.status_prev4_container.setVisibility(View.GONE);
            }
            int position = 1;
            for(final Attachment attachment: attachments){
                ImageView imageView;
                if( i == 0) {
                    imageView = holder.status_prev1;
                    if( attachment.getType().equals("image") || attachment.getType().equals("unknown"))
                        holder.status_prev1_play.setVisibility(View.GONE);
                    else
                        holder.status_prev1_play.setVisibility(View.VISIBLE);
                }else if( i == 1) {
                    imageView = holder.status_prev2;
                    if( attachment.getType().equals("image") || attachment.getType().equals("unknown"))
                        holder.status_prev2_play.setVisibility(View.GONE);
                    else
                        holder.status_prev2_play.setVisibility(View.VISIBLE);
                }else if(i == 2) {
                    imageView = holder.status_prev3;
                    if( attachment.getType().equals("image") || attachment.getType().equals("unknown"))
                        holder.status_prev3_play.setVisibility(View.GONE);
                    else
                        holder.status_prev3_play.setVisibility(View.VISIBLE);
                }else {
                    imageView = holder.status_prev4;
                    if( attachment.getType().equals("image") || attachment.getType().equals("unknown"))
                        holder.status_prev4_play.setVisibility(View.GONE);
                    else
                        holder.status_prev4_play.setVisibility(View.VISIBLE);
                }
                String url = attachment.getPreview_url();
                if( url == null || url.trim().equals("") )
                    url = attachment.getUrl();
                else if( attachment.getType().equals("unknown"))
                    url = attachment.getRemote_url();
                DisplayImageOptions optionsAttachment = new DisplayImageOptions.Builder().displayer(new SimpleBitmapDisplayer()).cacheInMemory(false)
                        .cacheOnDisk(true).resetViewBeforeLoading(true).build();
                if( !url.trim().contains("missing.png"))
                    imageLoader.displayImage(url, imageView, optionsAttachment);
                final int finalPosition = position;
                if( attachment.getDescription() != null && !attachment.getDescription().equals("null"))
                    imageView.setContentDescription(attachment.getDescription());
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, MediaActivity.class);
                        Bundle b = new Bundle();
                        intent.putParcelableArrayListExtra("mediaArray", status.getMedia_attachments());
                        b.putInt("position", finalPosition);
                        intent.putExtras(b);
                        context.startActivity(intent);
                    }
                });
                i++;
                position++;
            }
        }else{
            holder.status_document_container.setVisibility(View.GONE);
        }
        holder.status_show_more.setVisibility(View.GONE);
    }



    @Override
    public void onRetrieveFeeds(APIResponse apiResponse) {
        if( apiResponse.getError() != null){
            final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
            boolean show_error_messages = sharedpreferences.getBoolean(Helper.SET_SHOW_ERROR_MESSAGES, true);
            if( show_error_messages)
                Toast.makeText(context, apiResponse.getError().getError(),Toast.LENGTH_LONG).show();
            return;
        }

        pins = apiResponse.getStatuses();

        for (Status haystack : statuses)
        {
            for (Status pin : pins) {

                if (haystack.getId().equals(pin.getId()))
                {
                    haystack.setPinned(true);
                    break;
                }
            }
        }
    }

    @Override
    public void onPostAction(int statusCode, API.StatusAction statusAction, String targetedId, Error error) {

        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);

        if( error != null){
            boolean show_error_messages = sharedpreferences.getBoolean(Helper.SET_SHOW_ERROR_MESSAGES, true);
            if( show_error_messages)
                Toast.makeText(context, error.getError(),Toast.LENGTH_LONG).show();
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
            statusListAdapter.notifyDataSetChanged();
        }else  if( statusAction == API.StatusAction.UNSTATUS ){
            for(Status status: statuses){
                if( status.getId().equals(targetedId))
                    statusesToRemove.add(status);
            }
            statuses.removeAll(statusesToRemove);
            statusListAdapter.notifyDataSetChanged();
        }
        else if ( statusAction == API.StatusAction.PIN || statusAction == API.StatusAction.UNPIN ) {
            Status toCheck = null;
            for (Status checkPin: statuses) {
                if (checkPin.getId().equals(targetedId)) {
                    toCheck = checkPin;
                    break;
                }
            }
            if (statusAction == API.StatusAction.PIN) {
                if (toCheck != null)
                    toCheck.setPinned(true);
            }
            else {
                if (toCheck != null)
                    toCheck.setPinned(false);
            }
            statusListAdapter.notifyDataSetChanged();
        }

        if( statusAction == API.StatusAction.REBLOG){
            for(Status status: statuses){
                if( status.getId().equals(targetedId)) {
                    status.setReblogs_count(status.getReblogs_count() + 1);
                    break;
                }
            }
            statusListAdapter.notifyDataSetChanged();
        }else if( statusAction == API.StatusAction.UNREBLOG){
            for(Status status: statuses){
                if( status.getId().equals(targetedId)) {
                    if( status.getReblogs_count() - 1 >= 0)
                        status.setReblogs_count(status.getReblogs_count() - 1);
                    break;
                }
            }
            statusListAdapter.notifyDataSetChanged();
        }else if( statusAction == API.StatusAction.FAVOURITE){
            for(Status status: statuses){
                if( status.getId().equals(targetedId)) {
                    status.setFavourites_count(status.getFavourites_count() + 1);
                    break;
                }
            }
            statusListAdapter.notifyDataSetChanged();
        }else if( statusAction == API.StatusAction.UNFAVOURITE){
            for(Status status: statuses){
                if( status.getId().equals(targetedId)) {
                    if( status.getFavourites_count() - 1 >= 0)
                        status.setFavourites_count(status.getFavourites_count() - 1);
                    break;
                }
            }
            statusListAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onRetrieveEmoji(int position, SpannableString spannableString, Boolean error) {
        statuses.get(position).setContents(spannableString);
        if( !statuses.get(position).isEmojiFound()) {
            statuses.get(position).setEmojiFound(true);
            statusListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onRetrieveSearchEmoji(List<Emojis> emojis) {

    }

    @Override
    public void onTranslatedTextview(Translate translate, Status status, String translatedResult, Boolean error) {
        if( error){
            Toast.makeText(context, R.string.toast_error_translate, Toast.LENGTH_LONG).show();
        }else {
            try {
                String aJsonString = translate.replace(translatedResult);
                if( aJsonString != null) {
                    status.setTranslated(true);
                    status.setTranslationShown(true);
                    status.setContent_translated(aJsonString);
                    statusListAdapter.notifyDataSetChanged();
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                Toast.makeText(context, R.string.toast_error_translate, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onTranslated(Translate translate, Helper.targetField targetField, String content, Boolean error) {
    }








}