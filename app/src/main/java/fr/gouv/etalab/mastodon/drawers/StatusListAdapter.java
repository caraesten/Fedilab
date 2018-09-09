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
import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.annotation.NonNull;
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
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.github.stom79.mytransl.MyTransL;
import com.github.stom79.mytransl.client.HttpsConnectionException;
import com.github.stom79.mytransl.client.Results;
import com.github.stom79.mytransl.translate.Translate;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.activities.BaseMainActivity;
import fr.gouv.etalab.mastodon.activities.MediaActivity;
import fr.gouv.etalab.mastodon.activities.ShowAccountActivity;
import fr.gouv.etalab.mastodon.activities.ShowConversationActivity;
import fr.gouv.etalab.mastodon.activities.TootActivity;
import fr.gouv.etalab.mastodon.asynctasks.PostActionAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveFeedsAsyncTask;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Application;
import fr.gouv.etalab.mastodon.client.Entities.Attachment;
import fr.gouv.etalab.mastodon.client.Entities.Card;
import fr.gouv.etalab.mastodon.client.Entities.Emojis;
import fr.gouv.etalab.mastodon.client.Entities.Error;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.fragments.DisplayStatusFragment;
import fr.gouv.etalab.mastodon.helper.CrossActions;
import fr.gouv.etalab.mastodon.helper.CustomTextView;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnPostActionInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveCardInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveEmojiInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveFeedsInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveRepliesInterface;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import fr.gouv.etalab.mastodon.sqlite.StatusCacheDAO;
import fr.gouv.etalab.mastodon.sqlite.StatusStoredDAO;
import fr.gouv.etalab.mastodon.sqlite.TempMuteDAO;
import static fr.gouv.etalab.mastodon.activities.MainActivity.currentLocale;
import static fr.gouv.etalab.mastodon.helper.Helper.THEME_BLACK;
import static fr.gouv.etalab.mastodon.helper.Helper.THEME_DARK;
import static fr.gouv.etalab.mastodon.helper.Helper.changeDrawableColor;
import static fr.gouv.etalab.mastodon.helper.Helper.getLiveInstance;


/**
 * Created by Thomas on 24/04/2017.
 * Adapter for Status
 */
public class StatusListAdapter extends RecyclerView.Adapter implements OnPostActionInterface, OnRetrieveFeedsInterface, OnRetrieveEmojiInterface, OnRetrieveRepliesInterface, OnRetrieveCardInterface {

    private Context context;
    private List<Status> statuses;
    private LayoutInflater layoutInflater;
    private boolean isOnWifi;
    private int translator;
    private int behaviorWithAttachments;
    private StatusListAdapter statusListAdapter;
    private RetrieveFeedsAsyncTask.Type type;
    private String targetedId;
    private final int DISPLAYED_STATUS = 1;
    private final int FOCUSED_STATUS = 2;
    private final int COMPACT_STATUS = 3;
    private int conversationPosition;
    private List<String> timedMute;
    private boolean redraft;
    private Status status;
    private Status toot;
    public StatusListAdapter(Context context, List<String> timedMute, RetrieveFeedsAsyncTask.Type type, String targetedId, boolean isOnWifi, int behaviorWithAttachments, int translator, List<Status> statuses){
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
        this.timedMute = timedMute;
        redraft = false;
    }

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
        redraft = false;
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
        redraft = false;
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
        if( holder.getItemViewType() == DISPLAYED_STATUS || holder.getItemViewType() == COMPACT_STATUS) {
            final ViewHolder viewHolder = (ViewHolder) holder;
            // Bug workaround for losing text selection ability, see:
            // https://code.google.com/p/android/issues/detail?id=208169
            viewHolder.status_content.setEnabled(false);
            viewHolder.status_content.setEnabled(true);
            viewHolder.status_spoiler.setEnabled(false);
            viewHolder.status_spoiler.setEnabled(true);
        }
    }


    class ViewHolder extends RecyclerView.ViewHolder{
        LinearLayout status_content_container;
        LinearLayout status_spoiler_container;
        CustomTextView status_spoiler;
        Button status_spoiler_button;
        CustomTextView status_content;
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
        RelativeLayout status_horizontal_document_container;
        ImageView status_prev1;
        ImageView status_prev2;
        ImageView status_prev3;
        ImageView status_prev4;
        ImageView status_prev1_play;
        ImageView status_prev2_play;
        ImageView status_prev3_play;
        ImageView status_prev4_play;
        ImageView status_prev1_h;
        ImageView status_prev2_h;
        ImageView status_prev3_h;
        ImageView status_prev4_h;
        ImageView status_prev1_play_h;
        ImageView status_prev2_play_h;
        ImageView status_prev3_play_h;
        ImageView status_prev4_play_h;
        RelativeLayout status_prev4_container;
        TextView status_reply;
        ImageView status_pin;
        ImageView status_privacy;
        FloatingActionButton status_translate, status_bookmark;
        LinearLayout status_container2;
        LinearLayout status_container3;
        LinearLayout main_container;
        TextView yandex_translate;
        LinearLayout status_action_container;
        Button fetch_more;
        ImageView new_element;
        LinearLayout status_spoiler_mention_container;
        TextView status_mention_spoiler;
        LinearLayout status_cardview;
        ImageView status_cardview_image;
        TextView status_cardview_title, status_cardview_content, status_cardview_url;
        FrameLayout status_cardview_video;
        WebView status_cardview_webview;
        ImageView hide_preview, hide_preview_h;
        TextView status_toot_app;


        public View getView(){
            return itemView;
        }

        ViewHolder(View itemView) {
            super(itemView);
            fetch_more = itemView.findViewById(R.id.fetch_more);
            status_document_container = itemView.findViewById(R.id.status_document_container);
            status_horizontal_document_container = itemView.findViewById(R.id.status_horizontal_document_container);
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
            status_prev1_h = itemView.findViewById(R.id.status_prev1_h);
            status_prev2_h = itemView.findViewById(R.id.status_prev2_h);
            status_prev3_h = itemView.findViewById(R.id.status_prev3_h);
            status_prev4_h = itemView.findViewById(R.id.status_prev4_h);
            status_prev1_play_h = itemView.findViewById(R.id.status_prev1_play_h);
            status_prev2_play_h = itemView.findViewById(R.id.status_prev2_play_h);
            status_prev3_play_h = itemView.findViewById(R.id.status_prev3_play_h);
            status_prev4_play_h = itemView.findViewById(R.id.status_prev4_play_h);
            status_container2 = itemView.findViewById(R.id.status_container2);
            status_container3 = itemView.findViewById(R.id.status_container3);
            status_prev4_container = itemView.findViewById(R.id.status_prev4_container);
            status_reply = itemView.findViewById(R.id.status_reply);
            status_privacy = itemView.findViewById(R.id.status_privacy);
            status_translate = itemView.findViewById(R.id.status_translate);
            status_bookmark = itemView.findViewById(R.id.status_bookmark);
            status_content_translated_container = itemView.findViewById(R.id.status_content_translated_container);
            main_container = itemView.findViewById(R.id.main_container);
            status_spoiler_container = itemView.findViewById(R.id.status_spoiler_container);
            status_content_container = itemView.findViewById(R.id.status_content_container);
            status_spoiler = itemView.findViewById(R.id.status_spoiler);
            status_spoiler_button = itemView.findViewById(R.id.status_spoiler_button);
            yandex_translate = itemView.findViewById(R.id.yandex_translate);
            new_element = itemView.findViewById(R.id.new_element);
            status_action_container = itemView.findViewById(R.id.status_action_container);
            status_spoiler_mention_container = itemView.findViewById(R.id.status_spoiler_mention_container);
            status_mention_spoiler = itemView.findViewById(R.id.status_mention_spoiler);
            status_cardview = itemView.findViewById(R.id.status_cardview);
            status_cardview_image = itemView.findViewById(R.id.status_cardview_image);
            status_cardview_title = itemView.findViewById(R.id.status_cardview_title);
            status_cardview_content = itemView.findViewById(R.id.status_cardview_content);
            status_cardview_url = itemView.findViewById(R.id.status_cardview_url);
            status_cardview_video = itemView.findViewById(R.id.status_cardview_video);
            status_cardview_webview = itemView.findViewById(R.id.status_cardview_webview);
            hide_preview = itemView.findViewById(R.id.hide_preview);
            hide_preview_h = itemView.findViewById(R.id.hide_preview_h);
            status_toot_app = itemView.findViewById(R.id.status_toot_app);
        }
    }

    public Status getItem(int position){
        return statuses.get(position);
    }

    @Override
    public int getItemViewType(int position) {

        status = statuses.get(position);
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        boolean isCompactMode = sharedpreferences.getBoolean(Helper.SET_COMPACT_MODE, false);
        int HIDDEN_STATUS = 0;
        //If account related to status is null, the toot is hidden
        if( status.getAccount() == null )
            return HIDDEN_STATUS;
        String filter;
        if( type == RetrieveFeedsAsyncTask.Type.CACHE_BOOKMARKS)
            return isCompactMode?COMPACT_STATUS:DISPLAYED_STATUS;
        else if( type == RetrieveFeedsAsyncTask.Type.CONTEXT && position == conversationPosition)
            return FOCUSED_STATUS;
        else if( type == RetrieveFeedsAsyncTask.Type.HOME)
            filter = sharedpreferences.getString(Helper.SET_FILTER_REGEX_HOME, null);
        else if( type == RetrieveFeedsAsyncTask.Type.LOCAL)
            filter = sharedpreferences.getString(Helper.SET_FILTER_REGEX_LOCAL, null);
        else
            filter = sharedpreferences.getString(Helper.SET_FILTER_REGEX_PUBLIC, null);

        if( filter != null && filter.length() > 0){
            try {
                Pattern filterPattern = Pattern.compile("(" + filter + ")", Pattern.CASE_INSENSITIVE);
                String content;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    content = Html.fromHtml(status.getContent(), Html.FROM_HTML_MODE_LEGACY).toString();
                else
                    //noinspection deprecation
                    content = Html.fromHtml(status.getContent()).toString();
                Matcher matcher = filterPattern.matcher(content);
                if (matcher.find())
                    return HIDDEN_STATUS;
            }catch (Exception e){
                return DISPLAYED_STATUS;
            }
        }
        if( type == RetrieveFeedsAsyncTask.Type.HOME) {
            if (status.getReblog() != null && !sharedpreferences.getBoolean(Helper.SET_SHOW_BOOSTS, true))
                return HIDDEN_STATUS;
            else if (status.getIn_reply_to_id() != null && !status.getIn_reply_to_id().equals("null") && !sharedpreferences.getBoolean(Helper.SET_SHOW_REPLIES, true)) {
                return HIDDEN_STATUS;
            }else {
                if( timedMute != null && timedMute.size() > 0) {

                    if (timedMute.contains(status.getAccount().getId()))
                        return HIDDEN_STATUS;
                    else
                        return isCompactMode?COMPACT_STATUS:DISPLAYED_STATUS;
                }else {
                    return isCompactMode?COMPACT_STATUS:DISPLAYED_STATUS;
                }
            }
        }else {
            if( context instanceof ShowAccountActivity){
                if (status.getReblog() != null && !((ShowAccountActivity)context).showBoosts())
                    return HIDDEN_STATUS;
                else if( status.getIn_reply_to_id() != null && !status.getIn_reply_to_id().equals("null") && !((ShowAccountActivity)context).showReplies())
                    return HIDDEN_STATUS;
                else
                    return isCompactMode?COMPACT_STATUS:DISPLAYED_STATUS;
            }else
            return isCompactMode?COMPACT_STATUS:DISPLAYED_STATUS;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if( viewType == DISPLAYED_STATUS)
            return new ViewHolder(layoutInflater.inflate(R.layout.drawer_status, parent, false));
        else if(viewType == COMPACT_STATUS)
            return new ViewHolder(layoutInflater.inflate(R.layout.drawer_status_compact, parent, false));
        else if(viewType == FOCUSED_STATUS)
            return new ViewHolder(layoutInflater.inflate(R.layout.drawer_status_focused, parent, false));
        else
            return new ViewHolderEmpty(layoutInflater.inflate(R.layout.drawer_empty, parent, false));
    }


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int position) {

        if( viewHolder.getItemViewType() == DISPLAYED_STATUS || viewHolder.getItemViewType() == FOCUSED_STATUS || viewHolder.getItemViewType() == COMPACT_STATUS){
            final ViewHolder holder = (ViewHolder) viewHolder;
            final Status status = statuses.get(position);

            final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);


            final String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
            boolean displayBookmarkButton = sharedpreferences.getBoolean(Helper.SET_SHOW_BOOKMARK, true);
            boolean fullAttachement = sharedpreferences.getBoolean(Helper.SET_FULL_PREVIEW, false);

            if( type != RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE && getItemViewType(position) != COMPACT_STATUS  && displayBookmarkButton)
                holder.status_bookmark.setVisibility(View.VISIBLE);
            else
                holder.status_bookmark.setVisibility(View.GONE);

            holder.status_reply.setText("");
            //Display a preview for accounts that have replied *if enabled and only for home timeline*


            final SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
            Status statusBookmarked = new StatusCacheDAO(context, db).getStatus(StatusCacheDAO.BOOKMARK_CACHE, status.getId());
            if( statusBookmarked != null)
                status.setBookmarked(true);
            else
                status.setBookmarked(false);
            if( status.isBookmarked())
                holder.status_bookmark.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_bookmark));
            else
                holder.status_bookmark.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_bookmark_border));

            if( status.isNew())
                holder.new_element.setVisibility(View.VISIBLE);
            else
                holder.new_element.setVisibility(View.GONE);
            int iconSizePercent = sharedpreferences.getInt(Helper.SET_ICON_SIZE, 130);
            int textSizePercent = sharedpreferences.getInt(Helper.SET_TEXT_SIZE, 110);
            final boolean trans_forced = sharedpreferences.getBoolean(Helper.SET_TRANS_FORCED, false);
            holder.status_more.getLayoutParams().height = (int) Helper.convertDpToPixel((20*iconSizePercent/100), context);
            holder.status_more.getLayoutParams().width = (int) Helper.convertDpToPixel((20*iconSizePercent/100), context);
            holder.status_privacy.getLayoutParams().height = (int) Helper.convertDpToPixel((20*iconSizePercent/100), context);
            holder.status_privacy.getLayoutParams().width = (int) Helper.convertDpToPixel((20*iconSizePercent/100), context);
            boolean isCompactMode = sharedpreferences.getBoolean(Helper.SET_COMPACT_MODE, false);



           /* int status_account_profile = holder.status_account_profile.getWidth();
            int status_account_profile_boost = holder.status_account_profile_boost.getWidth();
            int status_account_profile_boost_by = holder.status_account_profile_boost_by.getWidth();
            RelativeLayout.LayoutParams lp_status_account_profile = new RelativeLayout.LayoutParams(status_account_profile*textSizePercent/110, status_account_profile*textSizePercent/110);
            holder.status_account_profile.setLayoutParams(lp_status_account_profile);
            RelativeLayout.LayoutParams lp_status_account_profile_boost = new RelativeLayout.LayoutParams(status_account_profile_boost*textSizePercent/110, status_account_profile*textSizePercent/110);
            holder.status_account_profile_boost.setLayoutParams(lp_status_account_profile_boost);
            RelativeLayout.LayoutParams lp_status_account_profile_boost_by = new RelativeLayout.LayoutParams(status_account_profile_boost_by*textSizePercent/110, status_account_profile*textSizePercent/110);
            holder.status_account_profile_boost_by.setLayoutParams(lp_status_account_profile_boost_by);
*/
            if( getItemViewType(position) == FOCUSED_STATUS ) {
                holder.status_content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16*textSizePercent/100);
                holder.status_account_displayname.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16 * textSizePercent / 100);
                holder.status_account_username.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14 * textSizePercent / 100);
                holder.status_toot_date.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14*textSizePercent/100);
                holder.status_content_translated.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16*textSizePercent/100);
            }else {
                holder.status_account_displayname.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14 * textSizePercent / 100);
                holder.status_account_username.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12 * textSizePercent / 100);
                holder.status_content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14*textSizePercent/100);
                holder.status_toot_date.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12*textSizePercent/100);
                holder.status_content_translated.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14*textSizePercent/100);
            }

            holder.status_spoiler.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14*textSizePercent/100);

            switch (translator) {
                case Helper.TRANS_NONE:
                    holder.yandex_translate.setVisibility(View.GONE);
                    break;
                case Helper.TRANS_YANDEX:
                    holder.yandex_translate.setVisibility(View.VISIBLE);
                    break;
                default:
                    holder.yandex_translate.setVisibility(View.GONE);
            }

            //Manages theme for icon colors
            int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
            if( theme == Helper.THEME_BLACK)
                changeDrawableColor(context, R.drawable.ic_fiber_new,R.color.dark_icon);
            else
                changeDrawableColor(context, R.drawable.ic_fiber_new,R.color.mastodonC4);

            if( getItemViewType(position) == COMPACT_STATUS )
                holder.status_privacy.setVisibility(View.GONE);
            else
                holder.status_privacy.setVisibility(View.VISIBLE);

            boolean expand_cw = sharedpreferences.getBoolean(Helper.SET_EXPAND_CW, false);
            if( theme == Helper.THEME_DARK || theme == Helper.THEME_BLACK){
                changeDrawableColor(context, R.drawable.ic_reply,R.color.dark_icon);
                changeDrawableColor(context, holder.status_more, R.color.dark_icon);
                changeDrawableColor(context, holder.status_privacy, R.color.dark_icon);
                changeDrawableColor(context, R.drawable.ic_repeat,R.color.dark_icon);
                changeDrawableColor(context, R.drawable.ic_star_border,R.color.dark_icon);
                changeDrawableColor(context, R.drawable.ic_plus_one,R.color.dark_icon);
                changeDrawableColor(context, R.drawable.ic_pin_drop, R.color.dark_icon);
                changeDrawableColor(context, R.drawable.ic_photo,R.color.dark_text);
                changeDrawableColor(context, R.drawable.ic_remove_red_eye,R.color.dark_text);
                changeDrawableColor(context, R.drawable.ic_translate,R.color.dark_text);

                holder.status_favorite_count.setTextColor(ContextCompat.getColor(context, R.color.dark_icon));
                holder.status_reblog_count.setTextColor(ContextCompat.getColor(context, R.color.dark_icon));
                holder.status_reply.setTextColor(ContextCompat.getColor(context, R.color.dark_icon));
                holder.status_toot_date.setTextColor(ContextCompat.getColor(context, R.color.dark_icon));
                holder.status_account_displayname.setTextColor(ContextCompat.getColor(context, R.color.dark_icon));
            }else {
                changeDrawableColor(context, R.drawable.ic_reply,R.color.black);
                changeDrawableColor(context, R.drawable.ic_more_horiz,R.color.black);
                changeDrawableColor(context, holder.status_more, R.color.black);
                changeDrawableColor(context, holder.status_privacy, R.color.black);
                changeDrawableColor(context, R.drawable.ic_repeat,R.color.black);
                changeDrawableColor(context, R.drawable.ic_plus_one,R.color.black);
                changeDrawableColor(context, R.drawable.ic_star_border,R.color.black);
                changeDrawableColor(context, R.drawable.ic_pin_drop, R.color.black);
                changeDrawableColor(context, R.drawable.ic_photo,R.color.mastodonC4);
                changeDrawableColor(context, R.drawable.ic_remove_red_eye,R.color.mastodonC4);
                changeDrawableColor(context, R.drawable.ic_translate,R.color.white);

                holder.status_favorite_count.setTextColor(ContextCompat.getColor(context, R.color.black));
                holder.status_reblog_count.setTextColor(ContextCompat.getColor(context, R.color.black));
                holder.status_toot_date.setTextColor(ContextCompat.getColor(context, R.color.black));
                holder.status_reply.setTextColor(ContextCompat.getColor(context, R.color.black));
                holder.status_account_displayname.setTextColor(ContextCompat.getColor(context, R.color.black));
            }

            //Redraws top icons (boost/reply)
            final float scale = context.getResources().getDisplayMetrics().density;
            if( status.getReblog() != null){
                Drawable img = ContextCompat.getDrawable(context, R.drawable.ic_repeat);
                assert img != null;
                img.setBounds(0,0,(int) (20 * iconSizePercent/100 * scale + 0.5f),(int) (15 * iconSizePercent/100 * scale + 0.5f));
                holder.status_account_displayname.setCompoundDrawables( img, null, null, null);
                holder.status_account_displayname.setVisibility(View.VISIBLE);
            }else{
                holder.status_account_displayname.setVisibility(View.GONE);
            }

            if( !status.isClickable())
                status.makeClickable(context);
            if( !status.isEmojiFound())
                status.makeEmojis(context, StatusListAdapter.this);




            holder.status_content.setText(status.getContentSpan(), TextView.BufferType.SPANNABLE);
            holder.status_spoiler.setText(status.getContentSpanCW(), TextView.BufferType.SPANNABLE);
            holder.status_account_displayname.setText(status.getDisplayNameSpan(), TextView.BufferType.SPANNABLE);
            holder.status_content.setMovementMethod(LinkMovementMethod.getInstance());
            holder.status_spoiler.setMovementMethod(LinkMovementMethod.getInstance());

            holder.status_translate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    translateToot(status);
                }
            });

            holder.status_bookmark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if( type != RetrieveFeedsAsyncTask.Type.CACHE_BOOKMARKS) {
                        status.setBookmarked(!status.isBookmarked());
                        if (status.isBookmarked()) {
                            new StatusCacheDAO(context, db).insertStatus(StatusCacheDAO.BOOKMARK_CACHE, status);
                            Toast.makeText(context, R.string.status_bookmarked, Toast.LENGTH_LONG).show();
                        } else {
                            new StatusCacheDAO(context, db).remove(StatusCacheDAO.BOOKMARK_CACHE, status);
                            Toast.makeText(context, R.string.status_unbookmarked, Toast.LENGTH_LONG).show();
                        }
                        notifyStatusChanged(status);
                    }else {
                        int position = 0;
                        for (Status statustmp : statuses) {
                            if (statustmp.getId().equals(status.getId())) {
                                statuses.remove(status);
                                statusListAdapter.notifyItemRemoved(position);
                                new StatusCacheDAO(context, db).remove(StatusCacheDAO.BOOKMARK_CACHE, statustmp);
                                Toast.makeText(context, R.string.status_unbookmarked, Toast.LENGTH_LONG).show();
                                break;
                            }
                            position++;
                        }
                    }
                }
            });
            holder.status_content_translated.setMovementMethod(LinkMovementMethod.getInstance());
            //-------- END -> Manages translations



            //Displays name & emoji in toot header
            final String ppurl;
            if( status.getReblog() != null){
                ppurl = status.getReblog().getAccount().getAvatar();
                holder.status_account_displayname.setVisibility(View.VISIBLE);
                holder.status_account_displayname.setText(context.getResources().getString(R.string.reblog_by, status.getAccount().getUsername()));
            }else {
                ppurl = status.getAccount().getAvatar();
            }
            //-------- END -> Displays name & emoji in toot header

            //Change the color in gray for accounts in DARK Theme only
            Spannable wordtoSpan = status.getDisplayNameSpan();
            if( theme == THEME_DARK || theme == Helper.THEME_BLACK) {
                Pattern hashAcct;
                if( status.getReblog() != null)
                    hashAcct = Pattern.compile("\\s(@"+status.getReblog().getAccount().getAcct()+")");
                else
                    hashAcct = Pattern.compile("\\s(@"+status.getAccount().getAcct()+")");
                if( wordtoSpan != null && hashAcct != null){
                    Matcher matcherAcct = hashAcct.matcher(wordtoSpan);
                    while (matcherAcct.find()){
                        int matchStart = matcherAcct.start(1);
                        int matchEnd = matcherAcct.end();
                        if( wordtoSpan.length() >= matchEnd && matchStart < matchEnd)
                            wordtoSpan.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.dark_icon)), matchStart, matchEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    }
                }
            }
            holder.status_account_username.setText(wordtoSpan);

            //-------- END -> Change the color in gray for accounts in DARK Theme only

            if( status.isFetchMore()) {
                holder.fetch_more.setVisibility(View.VISIBLE);
                holder.fetch_more.setEnabled(true);
            }else {
                holder.fetch_more.setVisibility(View.GONE);

            }

            holder.fetch_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                        status.setFetchMore(false);
                        holder.fetch_more.setEnabled(false);
                        holder.fetch_more.setVisibility(View.GONE);
                        DisplayStatusFragment homeFragment = ((BaseMainActivity) context).getHomeFragment();
                        if( homeFragment != null)
                            homeFragment.fetchMore(status.getId());
                }
            });

            holder.status_mention_spoiler.setText(Helper.makeMentionsClick(context,status.getMentions()), TextView.BufferType.SPANNABLE);
            holder.status_mention_spoiler.setMovementMethod(LinkMovementMethod.getInstance());

            if( getItemViewType(position) != COMPACT_STATUS ) {
                if( status.getReblog() == null)
                    holder.status_favorite_count.setText(String.valueOf(status.getFavourites_count()));
                else
                    holder.status_favorite_count.setText(String.valueOf(status.getReblog().getFavourites_count()));
                if (status.getReblog() == null)
                    holder.status_reblog_count.setText(String.valueOf(status.getReblogs_count()));
                else
                    holder.status_reblog_count.setText(String.valueOf(status.getReblog().getReblogs_count()));
            }
            if( getItemViewType(position) == FOCUSED_STATUS) {
                String fullDate_tmp = Helper.dateDiffFull(status.getCreated_at());
                String fullDate = fullDate_tmp.substring(0,1).toUpperCase() + fullDate_tmp.substring(1);
                holder.status_toot_date.setText(fullDate);
            }else {
                holder.status_toot_date.setText(Helper.dateDiff(context, status.getCreated_at()));
                Helper.absoluteDateTimeReveal(context, holder.status_toot_date, status.getCreated_at());
            }

            if( status.getReblog() != null) {
                Helper.loadGiF(context, ppurl, holder.status_account_profile_boost);
                Helper.loadGiF(context, status.getAccount().getAvatar(), holder.status_account_profile_boost_by);
                holder.status_account_profile_boost.setVisibility(View.VISIBLE);
                holder.status_account_profile_boost_by.setVisibility(View.VISIBLE);
                holder.status_account_profile.setVisibility(View.GONE);
            }else{
                Helper.loadGiF(context, ppurl, holder.status_account_profile);
                holder.status_account_profile_boost.setVisibility(View.GONE);
                holder.status_account_profile_boost_by.setVisibility(View.GONE);
                holder.status_account_profile.setVisibility(View.VISIBLE);
            }
            holder.status_action_container.setVisibility(View.VISIBLE);
            if( ( getItemViewType(position) != COMPACT_STATUS ) && (trans_forced || (translator != Helper.TRANS_NONE && currentLocale != null && status.getLanguage() != null && !status.getLanguage().trim().equals(currentLocale)))){
                holder.status_translate.setVisibility(View.VISIBLE);
            }else {
                holder.status_translate.setVisibility(View.GONE);
            }
            if( status.getReblog() == null) {
                if (status.getSpoiler_text() != null && status.getSpoiler_text().trim().length() > 0 ) {
                    holder.status_spoiler_container.setVisibility(View.VISIBLE);
                    if( !status.isSpoilerShown() && !expand_cw) {
                        holder.status_content_container.setVisibility(View.GONE);
                        holder.status_spoiler_mention_container.setVisibility(View.VISIBLE);
                        holder.status_spoiler_button.setText(context.getString(R.string.load_attachment_spoiler));
                    }else {
                        holder.status_content_container.setVisibility(View.VISIBLE);
                        holder.status_spoiler_mention_container.setVisibility(View.GONE);
                        holder.status_spoiler_button.setText(context.getString(R.string.load_attachment_spoiler_less));
                    }
                } else {
                    holder.status_spoiler_container.setVisibility(View.GONE);
                    holder.status_spoiler_mention_container.setVisibility(View.GONE);
                    holder.status_content_container.setVisibility(View.VISIBLE);
                }
            }else {
                if (status.getReblog().getSpoiler_text() != null && status.getReblog().getSpoiler_text().trim().length() > 0) {
                    holder.status_spoiler_container.setVisibility(View.VISIBLE);
                    if( !status.isSpoilerShown() && !expand_cw) {
                        holder.status_content_container.setVisibility(View.GONE);
                        holder.status_spoiler_mention_container.setVisibility(View.VISIBLE);
                        holder.status_spoiler_button.setText(context.getString(R.string.load_attachment_spoiler));
                    }else {
                        holder.status_content_container.setVisibility(View.VISIBLE);
                        holder.status_spoiler_mention_container.setVisibility(View.GONE);
                        holder.status_spoiler_button.setText(context.getString(R.string.load_attachment_spoiler_less));
                    }
                } else {
                    holder.status_spoiler_container.setVisibility(View.GONE);
                    holder.status_spoiler_mention_container.setVisibility(View.GONE);
                    holder.status_content_container.setVisibility(View.VISIBLE);
                }
            }
            if( status.getReblog() == null) {
                if (status.getMedia_attachments().size() < 1) {
                    if( fullAttachement )
                        holder.status_horizontal_document_container.setVisibility(View.GONE);
                    else
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
                            if( fullAttachement )
                                holder.status_horizontal_document_container.setVisibility(View.GONE);
                            else
                                holder.status_document_container.setVisibility(View.GONE);
                        } else {
                            loadAttachments(status, holder);
                        }
                    }
                }
            }else { //Attachments for reblogs

                if (status.getReblog().getMedia_attachments().size() < 1) {
                    if( fullAttachement )
                        holder.status_horizontal_document_container.setVisibility(View.GONE);
                    else
                        holder.status_document_container.setVisibility(View.GONE);
                    holder.status_show_more.setVisibility(View.GONE);
                } else {
                    //If medias are loaded without any conditions or if device is on wifi
                    if (!status.getReblog().isSensitive() && (behaviorWithAttachments == Helper.ATTACHMENT_ALWAYS || (behaviorWithAttachments == Helper.ATTACHMENT_WIFI && isOnWifi))) {
                        loadAttachments(status.getReblog(), holder);
                        holder.status_show_more.setVisibility(View.GONE);
                        status.setAttachmentShown(true);
                    } else {
                        //Text depending if toots is sensitive or not
                        String textShowMore = (status.getReblog().isSensitive()) ? context.getString(R.string.load_sensitive_attachment) : context.getString(R.string.load_attachment);
                        holder.status_show_more.setText(textShowMore);
                        if (!status.isAttachmentShown()) {
                            holder.status_show_more.setVisibility(View.VISIBLE);
                            if( fullAttachement )
                                holder.status_horizontal_document_container.setVisibility(View.GONE);
                            else
                                holder.status_document_container.setVisibility(View.GONE);
                        } else {
                            loadAttachments(status.getReblog(), holder);
                        }
                    }
                }
            }

            if(!fullAttachement)
                holder.hide_preview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        status.setAttachmentShown(!status.isAttachmentShown());
                        if( status.getReblog() != null)
                            status.getReblog().setSensitive(true);
                        else
                            status.setSensitive(true);

                        if( theme == Helper.THEME_DARK || theme == Helper.THEME_BLACK)
                            changeDrawableColor(context, R.drawable.ic_photo,R.color.dark_text);
                        else
                            changeDrawableColor(context, R.drawable.ic_photo,R.color.mastodonC4);
                        notifyStatusChanged(status);
                    }
                });
            else
                holder.hide_preview_h.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        status.setAttachmentShown(!status.isAttachmentShown());
                        if( status.getReblog() != null)
                            status.getReblog().setSensitive(true);
                        else
                            status.setSensitive(true);

                        if( theme == Helper.THEME_DARK || theme == Helper.THEME_BLACK)
                            changeDrawableColor(context, R.drawable.ic_photo,R.color.dark_text);
                        else
                            changeDrawableColor(context, R.drawable.ic_photo,R.color.mastodonC4);
                        notifyStatusChanged(status);
                    }
                });

            //Toot was translated and user asked to see it

            if( status.isTranslationShown() && status.getContentSpanTranslated() != null){
                holder.status_content_translated.setText(status.getContentSpanTranslated(), TextView.BufferType.SPANNABLE);
                holder.status_content.setVisibility(View.GONE);
                holder.status_content_translated_container.setVisibility(View.VISIBLE);
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
                if( theme == THEME_DARK || theme == THEME_BLACK)
                    changeDrawableColor(context, R.drawable.ic_star_border,R.color.dark_icon);
                else
                    changeDrawableColor(context, R.drawable.ic_star_border,R.color.black);
                imgFav = ContextCompat.getDrawable(context, R.drawable.ic_star_border);
            }

            if( status.isReblogged()|| (status.getReblog() != null && status.getReblog().isReblogged())) {
                changeDrawableColor(context, R.drawable.ic_repeat_boost,R.color.boost_icon);
                imgReblog = ContextCompat.getDrawable(context, R.drawable.ic_repeat_boost);
            }else {
                if( theme == THEME_DARK || theme == THEME_BLACK)
                    changeDrawableColor(context, R.drawable.ic_repeat,R.color.dark_icon);
                else
                    changeDrawableColor(context, R.drawable.ic_repeat,R.color.black);
                imgReblog = ContextCompat.getDrawable(context, R.drawable.ic_repeat);
            }


            if( theme == THEME_DARK || theme == THEME_BLACK)
                changeDrawableColor(context, R.drawable.ic_reply,R.color.dark_icon);
            else
                changeDrawableColor(context, R.drawable.ic_reply,R.color.black);
            imgReply = ContextCompat.getDrawable(context, R.drawable.ic_reply);


            assert imgFav != null;
            imgFav.setBounds(0,0,(int) (20 * iconSizePercent/100 * scale + 0.5f),(int) (20 * iconSizePercent/100 * scale + 0.5f));
            assert imgReblog != null;
            imgReblog.setBounds(0,0,(int) (20 * iconSizePercent/100 * scale + 0.5f),(int) (20 * iconSizePercent/100 * scale + 0.5f));
            assert imgReply != null;
            imgReply.setBounds(0,0,(int) (20 * iconSizePercent/100 * scale + 0.5f),(int) (20 * iconSizePercent/100 * scale + 0.5f));

            holder.status_favorite_count.setCompoundDrawables(imgFav, null, null, null);
            holder.status_reblog_count.setCompoundDrawables(imgReblog, null, null, null);

            if(isCompactMode && ((status.getReblog() == null && status.getReplies_count() > 1) || (status.getReblog() != null && status.getReblog().getReplies_count() > 1))){
                Drawable img = context.getResources().getDrawable( R.drawable.ic_plus_one );
                holder.status_reply.setCompoundDrawablesWithIntrinsicBounds( imgReply, null, img, null);
            }else{
                holder.status_reply.setCompoundDrawablesWithIntrinsicBounds( imgReply, null, null, null);
            }
            if( isCompactMode){
                if( ((status.getReblog() == null && status.getReplies_count() == 1) || (status.getReblog() != null && status.getReblog().getReplies_count() == 1)))
                    holder.status_reply.setText(String.valueOf( status.getReblog() != null? status.getReblog().getReplies_count():status.getReblog().getReplies_count()));
            }else {
                if( status.getReplies_count() > 0 || (status.getReblog() != null && status.getReblog().getReplies_count() > 0 ) )
                    holder.status_reply.setText(String.valueOf( status.getReblog() != null? status.getReblog().getReplies_count():status.getReblog().getReplies_count()));
            }

            boolean isOwner = status.getAccount().getId().equals(userId);

            // Pinning toots is only available on Mastodon 1._6_.0 instances.
            if (isOwner && Helper.canPin && (status.getVisibility().equals("public") || status.getVisibility().equals("unlisted")) && status.getReblog() == null) {
                Drawable imgPin;
                if( status.isPinned()|| (status.getReblog() != null && status.getReblog().isPinned())) {
                    changeDrawableColor(context, R.drawable.ic_pin_drop_p,R.color.marked_icon);
                    imgPin = ContextCompat.getDrawable(context, R.drawable.ic_pin_drop_p);
                }else {
                    if( theme == THEME_DARK || theme == THEME_BLACK)
                        changeDrawableColor(context, R.drawable.ic_pin_drop,R.color.dark_icon);
                    else
                        changeDrawableColor(context, R.drawable.ic_pin_drop,R.color.black);
                    imgPin = ContextCompat.getDrawable(context, R.drawable.ic_pin_drop);
                }
                assert imgPin != null;
                imgPin.setBounds(0,0,(int) (20 * iconSizePercent/100 * scale + 0.5f),(int) (20 * iconSizePercent/100 * scale + 0.5f));
                holder.status_pin.setImageDrawable(imgPin);

                holder.status_pin.setVisibility(View.VISIBLE);
            }
            else {
                holder.status_pin.setVisibility(View.GONE);
            }


            holder.status_content.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP && !view.hasFocus()) {
                        try{view.requestFocus();}catch (Exception ignored){}
                    }
                    return false;
                }
            });
            //Click on a conversation
            if( type != RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE && (getItemViewType(position) == DISPLAYED_STATUS || getItemViewType(position) == COMPACT_STATUS)) {
                holder.status_content.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, ShowConversationActivity.class);
                        Bundle b = new Bundle();
                        if (status.getReblog() == null)
                            b.putString("statusId", status.getId());
                        else
                            b.putString("statusId", status.getReblog().getId());
                        intent.putExtras(b);
                        if (type == RetrieveFeedsAsyncTask.Type.CONTEXT)
                            ((Activity) context).finish();
                        context.startActivity(intent);
                    }
                });
                holder.main_container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, ShowConversationActivity.class);
                        Bundle b = new Bundle();
                        if (status.getReblog() == null)
                            b.putString("statusId", status.getId());
                        else
                            b.putString("statusId", status.getReblog().getId());
                        intent.putExtras(b);
                        if (type == RetrieveFeedsAsyncTask.Type.CONTEXT)
                            ((Activity) context).finish();
                        context.startActivity(intent);
                    }
                });
            }
            if( theme == Helper.THEME_LIGHT){
                holder.main_container.setBackgroundResource(R.color.mastodonC3__);
            }else if (theme == Helper.THEME_DARK){
                holder.main_container.setBackgroundResource(R.color.mastodonC1_);
            }else if (theme == Helper.THEME_BLACK){
                holder.main_container.setBackgroundResource(R.color.black);
            }
            if( type == RetrieveFeedsAsyncTask.Type.CONTEXT ){

                if( position == conversationPosition){
                    if( theme == Helper.THEME_LIGHT)
                        holder.main_container.setBackgroundResource(R.color.mastodonC3_);
                    else if( theme == Helper.THEME_DARK)
                        holder.main_container.setBackgroundResource(R.color.mastodonC1___);
                    else if( theme == Helper.THEME_BLACK)
                        holder.main_container.setBackgroundResource(R.color.black_2);
                    if( status.getCard() != null){

                        holder.status_cardview_content.setText(status.getCard().getDescription());
                        holder.status_cardview_title.setText(status.getCard().getTitle());
                        holder.status_cardview_url.setText(status.getCard().getUrl());
                        if( status.getCard().getImage() != null && status.getCard().getImage().length() > 10) {
                            holder.status_cardview_image.setVisibility(View.VISIBLE);
                            if( !((ShowConversationActivity)context).isFinishing())
                                Glide.with(holder.status_cardview_image.getContext())
                                        .load(status.getCard().getImage())
                                        .into(holder.status_cardview_image);
                        }else
                            holder.status_cardview_image.setVisibility(View.GONE);
                        if( !status.getCard().getType().equals("video")) {
                            holder.status_cardview.setVisibility(View.VISIBLE);
                            holder.status_cardview_video.setVisibility(View.GONE);
                            holder.status_cardview.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Helper.openBrowser(context, status.getCard().getUrl());
                                }
                            });
                        }else {
                            holder.status_cardview.setVisibility(View.GONE);
                            holder.status_cardview_video.setVisibility(View.VISIBLE);
                            holder.status_cardview_webview.getSettings().setJavaScriptEnabled(true);
                            String html = status.getCard().getHtml();
                            String src = status.getCard().getUrl();
                            if( html != null){
                                Matcher matcher = Pattern.compile("src=\"([^\"]+)\"").matcher(html);
                                if( matcher.find())
                                    src = matcher.group(1);
                            }
                            final String finalSrc = src;
                            holder.status_cardview_webview.setWebViewClient(new WebViewClient() {
                                @Override
                                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                                    holder.status_cardview_video.setVisibility(View.GONE);
                                }
                            });
                            holder.status_cardview_webview.loadUrl(finalSrc);
                        }
                    }else {
                        holder.status_cardview.setVisibility(View.GONE);
                        holder.status_cardview_video.setVisibility(View.GONE);
                    }

                }else {
                    holder.status_cardview.setVisibility(View.GONE);
                    holder.status_cardview_video.setVisibility(View.GONE);
                    if( theme == Helper.THEME_LIGHT)
                        holder.main_container.setBackgroundResource(R.color.mastodonC3__);
                    else if( theme == Helper.THEME_DARK)
                        holder.main_container.setBackgroundResource(R.color.mastodonC1_);
                    else if (theme == Helper.THEME_BLACK)
                        holder.main_container.setBackgroundResource(R.color.black);
                }
            }

            holder.status_reply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CrossActions.doCrossReply(context, status, type, true);
                }
            });


            holder.status_favorite_count.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CrossActions.doCrossAction(context, type, status, null, (status.isFavourited()|| (status.getReblog() != null && status.getReblog().isFavourited()))? API.StatusAction.UNFAVOURITE:API.StatusAction.FAVOURITE, statusListAdapter, StatusListAdapter.this, true);
                }
            });

            holder.status_reblog_count.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CrossActions.doCrossAction(context, type, status, null, (status.isReblogged()|| (status.getReblog() != null && status.getReblog().isReblogged()))? API.StatusAction.UNREBLOG:API.StatusAction.REBLOG, statusListAdapter, StatusListAdapter.this, true);
                }
            });
            holder.status_pin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CrossActions.doCrossAction(context, type, status, null, (status.isPinned()|| (status.getReblog() != null && status.getReblog().isPinned()))? API.StatusAction.UNPIN:API.StatusAction.PIN, statusListAdapter, StatusListAdapter.this, true);
                }
            });

            if( !status.getVisibility().equals("direct"))
            holder.status_favorite_count.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    CrossActions.doCrossAction(context, type, status, null, API.StatusAction.FAVOURITE, statusListAdapter, StatusListAdapter.this, false);
                    return true;
                }
            });
            if( !status.getVisibility().equals("direct"))
            holder.status_reblog_count.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    CrossActions.doCrossAction(context, type, status, null, API.StatusAction.REBLOG, statusListAdapter, StatusListAdapter.this, false);
                    return true;
                }
            });
            if( !status.getVisibility().equals("direct"))
                holder.status_reply.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        CrossActions.doCrossReply(context, status, type, false);
                        return true;
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
                    status.setSpoilerShown(!status.isSpoilerShown());
                    notifyStatusChanged(status);
                }
            });

            holder.status_show_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadAttachments(status, holder);
                    status.setAttachmentShown(true);
                    notifyStatusChanged(status);
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
                                notifyStatusChanged(status);
                            }
                        }.start();
                    }
                }
            });


            if( type == RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE)
                holder.status_more.setVisibility(View.GONE);

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
                    if( status.isBookmarked())
                        popup.getMenu().findItem(R.id.action_bookmark).setTitle(R.string.bookmark_remove);
                    else
                        popup.getMenu().findItem(R.id.action_bookmark).setTitle(R.string.bookmark_add);
                    final String[] stringArrayConf;
                    if( isOwner) {
                        popup.getMenu().findItem(R.id.action_block).setVisible(false);
                        popup.getMenu().findItem(R.id.action_mute).setVisible(false);
                        popup.getMenu().findItem(R.id.action_report).setVisible(false);
                        popup.getMenu().findItem(R.id.action_timed_mute).setVisible(false);
                        stringArrayConf =  context.getResources().getStringArray(R.array.more_action_owner_confirm);
                    }else {
                        popup.getMenu().findItem(R.id.action_redraft).setVisible(false);
                        popup.getMenu().findItem(R.id.action_remove).setVisible(false);
                        stringArrayConf =  context.getResources().getStringArray(R.array.more_action_confirm);
                        if( type != RetrieveFeedsAsyncTask.Type.HOME){
                            popup.getMenu().findItem(R.id.action_timed_mute).setVisible(false);
                        }
                    }
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            AlertDialog.Builder builderInner;
                            final API.StatusAction doAction;
                            switch (item.getItemId()) {
                                case R.id.action_redraft:
                                    builderInner = new AlertDialog.Builder(context);
                                    builderInner.setTitle(stringArrayConf[1]);
                                    redraft = true;
                                    doAction = API.StatusAction.UNSTATUS;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                        builderInner.setMessage(Html.fromHtml(status.getContent(), Html.FROM_HTML_MODE_LEGACY));
                                    else
                                        //noinspection deprecation
                                        builderInner.setMessage(Html.fromHtml(status.getContent()));
                                break;
                                case R.id.action_open_browser:
                                    Helper.openBrowser(context, status.getReblog()!=null?status.getReblog().getUrl():status.getUrl());
                                    return true;
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
                                case R.id.action_bookmark:
                                    if( type != RetrieveFeedsAsyncTask.Type.CACHE_BOOKMARKS) {
                                        status.setBookmarked(!status.isBookmarked());
                                        if (status.isBookmarked()) {
                                            new StatusCacheDAO(context, db).insertStatus(StatusCacheDAO.BOOKMARK_CACHE, status);
                                            Toast.makeText(context, R.string.status_bookmarked, Toast.LENGTH_LONG).show();
                                        } else {
                                            new StatusCacheDAO(context, db).remove(StatusCacheDAO.BOOKMARK_CACHE, status);
                                            Toast.makeText(context, R.string.status_unbookmarked, Toast.LENGTH_LONG).show();
                                        }
                                        notifyStatusChanged(status);
                                    }else {
                                        int position = 0;
                                        for (Status statustmp : statuses) {
                                            if (statustmp.getId().equals(status.getId())) {
                                                statuses.remove(status);
                                                statusListAdapter.notifyItemRemoved(position);
                                                new StatusCacheDAO(context, db).remove(StatusCacheDAO.BOOKMARK_CACHE, statustmp);
                                                Toast.makeText(context, R.string.status_unbookmarked, Toast.LENGTH_LONG).show();
                                                break;
                                            }
                                            position++;
                                        }
                                    }
                                    return true;
                                case R.id.action_timed_mute:
                                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                                    LayoutInflater inflater = ((Activity)context).getLayoutInflater();
                                    @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.datetime_picker, null);
                                    dialogBuilder.setView(dialogView);
                                    final AlertDialog alertDialog = dialogBuilder.create();
                                    final DatePicker datePicker = dialogView.findViewById(R.id.date_picker);
                                    final TimePicker timePicker = dialogView.findViewById(R.id.time_picker);
                                    timePicker.setIs24HourView(true);
                                    Button date_time_cancel = dialogView.findViewById(R.id.date_time_cancel);
                                    final ImageButton date_time_previous = dialogView.findViewById(R.id.date_time_previous);
                                    final ImageButton date_time_next = dialogView.findViewById(R.id.date_time_next);
                                    final ImageButton date_time_set = dialogView.findViewById(R.id.date_time_set);

                                    //Buttons management
                                    date_time_cancel.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            alertDialog.dismiss();
                                        }
                                    });
                                    date_time_next.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            datePicker.setVisibility(View.GONE);
                                            timePicker.setVisibility(View.VISIBLE);
                                            date_time_previous.setVisibility(View.VISIBLE);
                                            date_time_next.setVisibility(View.GONE);
                                            date_time_set.setVisibility(View.VISIBLE);
                                        }
                                    });
                                    date_time_previous.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            datePicker.setVisibility(View.VISIBLE);
                                            timePicker.setVisibility(View.GONE);
                                            date_time_previous.setVisibility(View.GONE);
                                            date_time_next.setVisibility(View.VISIBLE);
                                            date_time_set.setVisibility(View.GONE);
                                        }
                                    });
                                    date_time_set.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            int hour, minute;
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                hour = timePicker.getHour();
                                                minute = timePicker.getMinute();
                                            }else {
                                                //noinspection deprecation
                                                hour = timePicker.getCurrentHour();
                                                //noinspection deprecation
                                                minute = timePicker.getCurrentMinute();
                                            }
                                            Calendar calendar = new GregorianCalendar(datePicker.getYear(),
                                                    datePicker.getMonth(),
                                                    datePicker.getDayOfMonth(),
                                                    hour,
                                                    minute);
                                            long time = calendar.getTimeInMillis();
                                            if( (time - new Date().getTime()) < 60000 ){
                                                Toast.makeText(context, R.string.timed_mute_date_error, Toast.LENGTH_LONG).show();
                                            }else {
                                                //Store the toot as draft first
                                                String targeted_id = status.getAccount().getId();
                                                Date date_mute = new Date(time);
                                                SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                                                String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
                                                Account account = new AccountDAO(context, db).getAccountByID(userId);
                                                new TempMuteDAO(context, db).insert(account, targeted_id, new Date(time));
                                                if( timedMute != null && !timedMute.contains(account.getId()))
                                                    timedMute.add(targeted_id);
                                                else if (timedMute == null){
                                                    timedMute = new ArrayList<>();
                                                    timedMute.add(targeted_id);
                                                }
                                                Toast.makeText(context,context.getString(R.string.timed_mute_date,status.getAccount().getAcct(),Helper.dateToString(date_mute)), Toast.LENGTH_LONG).show();
                                                alertDialog.dismiss();
                                                notifyDataSetChanged();
                                            }
                                        }
                                    });
                                    alertDialog.show();
                                    return true;
                                case R.id.action_block:
                                    builderInner = new AlertDialog.Builder(context);
                                    builderInner.setTitle(stringArrayConf[1]);
                                    doAction = API.StatusAction.BLOCK;
                                    break;
                                case R.id.action_translate:
                                    translateToot(status);
                                    return true;
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
                                    final String content;
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
                                    String url;

                                    if( status.getReblog() != null) {
                                        if( status.getReblog().getUri().startsWith("http"))
                                            url = status.getReblog().getUri();
                                        else
                                            url = status.getReblog().getUrl();
                                    }else {
                                        if( status.getUri().startsWith("http"))
                                            url = status.getUri();
                                        else
                                            url = status.getUrl();
                                    }
                                    String extra_text;
                                    boolean share_details = sharedpreferences.getBoolean(Helper.SET_SHARE_DETAILS, true);
                                    if( share_details) {
                                        extra_text = (status.getReblog() != null) ? status.getReblog().getAccount().getAcct() : status.getAccount().getAcct();
                                        if (extra_text.split("@").length == 1)
                                            extra_text = "@" + extra_text + "@" + Helper.getLiveInstance(context);
                                        else
                                            extra_text = "@" + extra_text;
                                        extra_text += " " + Helper.shortnameToUnicode(":link:", true) + " " + url + "\r\n-\n";
                                        final String contentToot;
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                            contentToot = Html.fromHtml((status.getReblog() != null) ? status.getReblog().getContent() : status.getContent(), Html.FROM_HTML_MODE_LEGACY).toString();
                                        else
                                            //noinspection deprecation
                                            contentToot = Html.fromHtml((status.getReblog() != null) ? status.getReblog().getContent() : status.getContent()).toString();
                                        extra_text += contentToot;
                                    }else {
                                        extra_text = url;
                                    }
                                    sendIntent.putExtra(Intent.EXTRA_TEXT, extra_text);
                                    sendIntent.setType("text/plain");
                                    context.startActivity(Intent.createChooser(sendIntent, context.getString(R.string.share_with)));
                                    return true;
                                case R.id.action_mention:
                                    // Get a handler that can be used to post to the main thread
                                    final Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            String name = "@"+(status.getReblog()!=null?status.getReblog().getAccount().getAcct():status.getAccount().getAcct());
                                            if( name.split("@", -1).length - 1 == 1)
                                                name = name + "@" + getLiveInstance(context);
                                            Bitmap bitmap = Helper.convertTootIntoBitmap(context, name, holder.status_content);
                                            Intent intent = new Intent(context, TootActivity.class);
                                            Bundle b = new Bundle();
                                            String fname = "tootmention_" + status.getId() +".jpg";
                                            File file = new File (context.getCacheDir() + "/", fname);
                                            if (file.exists ()) //noinspection ResultOfMethodCallIgnored
                                                file.delete ();
                                            try {
                                                FileOutputStream out = new FileOutputStream(file);
                                                assert bitmap != null;
                                                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                                                out.flush();
                                                out.close();
                                            } catch (Exception ignored) {}
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
                                        if( redraft ){
                                            if( status.getIn_reply_to_id() != null && !status.getIn_reply_to_id().trim().equals("null")){
                                                toot = new Status();
                                                toot.setIn_reply_to_id(status.getIn_reply_to_id());
                                                toot.setSensitive(status.isSensitive());
                                                toot.setMedia_attachments(status.getMedia_attachments());
                                                if( status.getSpoiler_text() != null && status.getSpoiler_text().length() > 0)
                                                    toot.setSpoiler_text(status.getSpoiler_text().trim());
                                                toot.setContent(status.getContent());
                                                toot.setVisibility(status.getVisibility());
                                                new RetrieveFeedsAsyncTask(context, RetrieveFeedsAsyncTask.Type.ONESTATUS, status.getIn_reply_to_id(), null, false, false, StatusListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                            }else{
                                                toot = new Status();
                                                toot.setSensitive(status.isSensitive());
                                                toot.setMedia_attachments(status.getMedia_attachments());
                                                if( status.getSpoiler_text() != null && status.getSpoiler_text().length() > 0)
                                                    toot.setSpoiler_text(status.getSpoiler_text().trim());
                                                toot.setVisibility(status.getVisibility());
                                                toot.setContent(status.getContent());
                                                long id = new StatusStoredDAO(context, db).insertStatus(toot, null);
                                                Intent intentToot = new Intent(context, TootActivity.class);
                                                Bundle b = new Bundle();
                                                b.putLong("restored", id);
                                                b.putBoolean("removed", true);
                                                intentToot.putExtras(b);
                                                context.startActivity(intentToot);
                                            }
                                        }
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


            if( type != RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE) {
                holder.status_account_profile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (targetedId == null || !targetedId.equals(status.getAccount().getId())) {
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
                        if (targetedId == null || !targetedId.equals(status.getReblog().getAccount().getId())) {
                            Intent intent = new Intent(context, ShowAccountActivity.class);
                            Bundle b = new Bundle();
                            b.putString("accountId", status.getReblog().getAccount().getId());
                            intent.putExtras(b);
                            context.startActivity(intent);
                        }
                    }
                });
            }else{
                holder.status_account_profile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (targetedId == null || !targetedId.equals(status.getAccount().getId())) {
                            Account account = status.getAccount();
                            Pattern instanceHost = Pattern.compile("https?:\\/\\/([\\da-z\\.-]+\\.[a-z\\.]{2,6})");
                            Matcher matcher = instanceHost.matcher(status.getUrl());
                            String instance = null;
                            while (matcher.find()){
                                instance = matcher.group(1);
                            }
                            account.setInstance(instance);
                            CrossActions.doCrossProfile(context, account);
                        }
                    }
                });
                holder.status_account_profile_boost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (targetedId == null || !targetedId.equals(status.getReblog().getAccount().getId())) {
                            Account account = status.getReblog().getAccount();
                            Pattern instanceHost = Pattern.compile("https?:\\/\\/([\\da-z\\.-]+\\.[a-z\\.]{2,6})");
                            Matcher matcher = instanceHost.matcher(status.getUrl());
                            String instance = null;
                            while (matcher.find()){
                                instance = matcher.group(1);
                            }
                            account.setInstance(instance);
                            CrossActions.doCrossProfile(context, account);
                        }
                    }
                });
            }

            if( status.getApplication() != null && getItemViewType(position) == FOCUSED_STATUS){
                Application application = status.getApplication();
                holder.status_toot_app.setText(application.getName());
                holder.status_toot_app.setVisibility(View.VISIBLE);
                if( application.getWebsite() != null && !application.getWebsite().trim().equals("null"))
                holder.status_toot_app.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Helper.openBrowser(context, application.getWebsite());
                    }
                });
            }else {
                holder.status_toot_app.setVisibility(View.GONE);
            }
        }
    }



    private void loadAttachments(final Status status, final ViewHolder holder){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        boolean fullAttachement = sharedpreferences.getBoolean(Helper.SET_FULL_PREVIEW, false);
        List<Attachment> attachments = status.getMedia_attachments();

        if( attachments != null && attachments.size() > 0){
            int i = 0;
            if(fullAttachement)
                holder.status_horizontal_document_container.setVisibility(View.VISIBLE);
            else
                holder.status_document_container.setVisibility(View.VISIBLE);
            if( attachments.size() == 1 ){
                if( !fullAttachement)
                    holder.status_container2.setVisibility(View.GONE);
                else {
                    holder.status_prev1_h.setVisibility(View.VISIBLE);
                    holder.status_prev2_h.setVisibility(View.GONE);
                    holder.status_prev3_h.setVisibility(View.GONE);
                    holder.status_prev4_h.setVisibility(View.GONE);
                }
                if( attachments.get(0).getUrl().trim().contains("missing.png"))
                    if(fullAttachement)
                        holder.status_horizontal_document_container.setVisibility(View.GONE);
                    else
                        holder.status_document_container.setVisibility(View.GONE);
            }else if(attachments.size() == 2){
                if( !fullAttachement) {
                    holder.status_container2.setVisibility(View.VISIBLE);
                    holder.status_container3.setVisibility(View.GONE);
                    holder.status_prev4_container.setVisibility(View.GONE);
                    if (attachments.get(1).getUrl().trim().contains("missing.png"))
                        holder.status_container2.setVisibility(View.GONE);
                }else {
                    holder.status_prev1_h.setVisibility(View.VISIBLE);
                    holder.status_prev2_h.setVisibility(View.VISIBLE);
                    holder.status_prev3_h.setVisibility(View.GONE);
                    holder.status_prev4_h.setVisibility(View.GONE);
                    if (attachments.get(1).getUrl().trim().contains("missing.png"))
                        holder.status_prev2_h.setVisibility(View.GONE);
                }
            }else if( attachments.size() == 3 ){
                if( !fullAttachement) {
                    holder.status_container2.setVisibility(View.VISIBLE);
                    holder.status_container3.setVisibility(View.VISIBLE);
                    holder.status_prev4_container.setVisibility(View.GONE);
                    if (attachments.get(2).getUrl().trim().contains("missing.png"))
                        holder.status_container3.setVisibility(View.GONE);
                }else {
                    holder.status_prev1_h.setVisibility(View.VISIBLE);
                    holder.status_prev2_h.setVisibility(View.VISIBLE);
                    holder.status_prev3_h.setVisibility(View.VISIBLE);
                    holder.status_prev4_h.setVisibility(View.GONE);
                    if (attachments.get(2).getUrl().trim().contains("missing.png"))
                        holder.status_prev3_h.setVisibility(View.GONE);
                }
            }else {
                if(!fullAttachement) {
                    holder.status_container2.setVisibility(View.VISIBLE);
                    holder.status_container3.setVisibility(View.VISIBLE);
                    holder.status_prev4_container.setVisibility(View.VISIBLE);
                    if (attachments.get(2).getUrl().trim().contains("missing.png"))
                        holder.status_prev4_container.setVisibility(View.GONE);
                }else {
                    holder.status_prev1_h.setVisibility(View.VISIBLE);
                    holder.status_prev2_h.setVisibility(View.VISIBLE);
                    holder.status_prev3_h.setVisibility(View.VISIBLE);
                    holder.status_prev4_h.setVisibility(View.VISIBLE);
                    if (attachments.get(2).getUrl().trim().contains("missing.png"))
                        holder.status_prev3_h.setVisibility(View.GONE);
                }
            }
            int position = 1;
            for(final Attachment attachment: attachments){
                ImageView imageView;
                if( i == 0) {
                    imageView = fullAttachement?holder.status_prev1_h:holder.status_prev1;
                    if( attachment.getType().equals("image") || attachment.getType().equals("unknown"))
                        if( fullAttachement)
                            holder.status_prev1_play_h.setVisibility(View.GONE);
                        else
                            holder.status_prev1_play.setVisibility(View.GONE);
                    else
                        if( fullAttachement)
                            holder.status_prev1_play_h.setVisibility(View.VISIBLE);
                        else
                            holder.status_prev1_play.setVisibility(View.VISIBLE);
                }else if( i == 1) {
                    imageView = fullAttachement?holder.status_prev2_h:holder.status_prev2;
                    if( attachment.getType().equals("image") || attachment.getType().equals("unknown"))
                        if( fullAttachement)
                            holder.status_prev2_play_h.setVisibility(View.GONE);
                        else
                            holder.status_prev2_play.setVisibility(View.GONE);
                    else
                        if( fullAttachement)
                            holder.status_prev2_play_h.setVisibility(View.VISIBLE);
                        else
                            holder.status_prev2_play.setVisibility(View.VISIBLE);
                }else if(i == 2) {
                    imageView = fullAttachement?holder.status_prev3_h:holder.status_prev3;
                    if( attachment.getType().equals("image") || attachment.getType().equals("unknown"))
                        if( fullAttachement)
                            holder.status_prev3_play_h.setVisibility(View.GONE);
                        else
                            holder.status_prev3_play.setVisibility(View.GONE);
                    else
                        if( fullAttachement)
                            holder.status_prev3_play_h.setVisibility(View.VISIBLE);
                        else
                            holder.status_prev3_play.setVisibility(View.VISIBLE);
                }else {
                    imageView = fullAttachement?holder.status_prev4_h:holder.status_prev4;
                    if( attachment.getType().equals("image") || attachment.getType().equals("unknown"))
                        if( fullAttachement)
                            holder.status_prev4_play_h.setVisibility(View.GONE);
                        else
                            holder.status_prev4_play.setVisibility(View.GONE);
                    else
                        if( fullAttachement)
                            holder.status_prev4_play_h.setVisibility(View.VISIBLE);
                        else
                            holder.status_prev4_play.setVisibility(View.VISIBLE);
                }
                String url = attachment.getPreview_url();

                if( url == null || url.trim().equals("") )
                    url = attachment.getUrl();
                else if( attachment.getType().equals("unknown"))
                    url = attachment.getRemote_url();

                if( fullAttachement){
                    imageView.setImageBitmap(null);
                    if( !url.trim().contains("missing.png") && !((Activity)context).isFinishing() )
                        Glide.with(imageView.getContext())
                                .asBitmap()
                                .load(url)
                                .thumbnail(0.1f)
                                .into(new SimpleTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                                        DrawableTransitionOptions.withCrossFade();
                                        int width = resource.getWidth();
                                        int height = resource.getHeight();

                                        if( height < Helper.convertDpToPixel(200, context)){
                                            double ratio = ((double)Helper.convertDpToPixel(200, context) / (double)height);
                                            width = (int)(ratio * width);
                                            height = (int) Helper.convertDpToPixel(200, context);
                                            resource = Bitmap.createScaledBitmap(resource, width, height, false);
                                        }
                                        //Allow to put full width for preview for single attachment -> disabled for the moment
                                        /*int tootWidth = holder.status_content_container.getWidth();

                                        if( width < tootWidth && attachments.size() == 1){
                                            double ratio = ((double)tootWidth/ (double)width);
                                            height = (int)(ratio * (double)height);
                                            width = tootWidth;
                                            holder.status_horizontal_document_container.getLayoutParams().height = height;
                                            resource = Bitmap.createScaledBitmap(resource, width, height, false);
                                        }else{
                                            holder.status_horizontal_document_container.getLayoutParams().height = (int)Helper.convertDpToPixel(200, context);
                                        }*/
                                        imageView.setImageBitmap(resource);
                                    }
                                });
                }else {
                    if (!url.trim().contains("missing.png") && !((Activity) context).isFinishing())
                        Glide.with(imageView.getContext())
                                .load(url)
                                .thumbnail(0.1f)
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(imageView);
                }
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
            if(fullAttachement)
                holder.status_horizontal_document_container.setVisibility(View.GONE);
            else
                holder.status_document_container.setVisibility(View.GONE);
        }
        holder.status_show_more.setVisibility(View.GONE);


    }



    @Override
    public void onRetrieveFeeds(APIResponse apiResponse) {

        if( apiResponse.getStatuses() != null && apiResponse.getStatuses().size() > 0){
            SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
            long id = new StatusStoredDAO(context, db).insertStatus(toot, apiResponse.getStatuses().get(0));
            Intent intentToot = new Intent(context, TootActivity.class);
            Bundle b = new Bundle();
            b.putLong("restored", id);
            b.putBoolean("removed", true);
            intentToot.putExtras(b);
            context.startActivity(intentToot);
        }

    }



    @Override
    public void onRetrieveAccount(Card card) {
        if( conversationPosition < this.statuses.size() && card != null)
            this.statuses.get(conversationPosition).setCard(card);
        if( conversationPosition < this.statuses.size())
            statusListAdapter.notifyItemChanged(conversationPosition);
    }


    @Override
    public void onPostAction(int statusCode, API.StatusAction statusAction, String targetedId, Error error) {

        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        if( error != null){
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
            int position = 0;
            for(Status status: statuses){
                if( status.getId().equals(targetedId)) {
                    statuses.remove(status);
                    statusListAdapter.notifyItemRemoved(position);
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
                    statusListAdapter.notifyItemChanged(position);
                    break;
                }
                position++;
            }
        }

        if( statusAction == API.StatusAction.REBLOG){
            int position = 0;
            for(Status status: statuses){
                if( status.getId().equals(targetedId)) {
                    status.setReblogs_count(status.getReblogs_count() + 1);
                    statusListAdapter.notifyItemChanged(position);
                    break;
                }
                position++;
            }
        }else if( statusAction == API.StatusAction.UNREBLOG){
            int position = 0;
            for(Status status: statuses){
                if( status.getId().equals(targetedId)) {
                    if( status.getReblogs_count() - 1 >= 0)
                        status.setReblogs_count(status.getReblogs_count() - 1);
                    statusListAdapter.notifyItemChanged(position);
                    SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                    //Remove the status from cache also
                    try {
                        new StatusCacheDAO(context, db).remove(StatusCacheDAO.ARCHIVE_CACHE,status);
                    }catch (Exception ignored){}
                    break;
                }
                position++;
            }
        }else if( statusAction == API.StatusAction.FAVOURITE){
            int position = 0;
            for(Status status: statuses){
                if( status.getId().equals(targetedId)) {
                    status.setFavourites_count(status.getFavourites_count() + 1);
                    statusListAdapter.notifyItemChanged(position);
                    break;
                }
                position++;
            }
            statusListAdapter.notifyDataSetChanged();
        }else if( statusAction == API.StatusAction.UNFAVOURITE){
            int position = 0;
            for(Status status: statuses){
                if( status.getId().equals(targetedId)) {
                    if( status.getFavourites_count() - 1 >= 0)
                        status.setFavourites_count(status.getFavourites_count() - 1);
                    statusListAdapter.notifyItemChanged(position);
                    break;
                }
                position++;
            }
        }
    }

    private void notifyStatusChanged(Status status){
        for (int i = 0; i < statusListAdapter.getItemCount(); i++) {
            //noinspection ConstantConditions
            if (statusListAdapter.getItemAt(i) != null && statusListAdapter.getItemAt(i).getId().equals(status.getId())) {
                try {
                    statusListAdapter.notifyItemChanged(i);
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

       /* if( !fromTranslation) {
            if (!status.isEmojiFound()) {
                for (int i = 0; i < statusListAdapter.getItemCount(); i++) {
                    //noinspection ConstantConditions
                    if (statusListAdapter.getItemAt(i) != null && statusListAdapter.getItemAt(i).getId().equals(status.getId())) {
                        //noinspection ConstantConditions
                        statusListAdapter.getItemAt(i).setEmojiFound(true);
                        try {
                            statusListAdapter.notifyItemChanged(i);
                        } catch (Exception ignored) {}
                    }
                }
            }
        }else {
            if (!status.isEmojiTranslateFound()) {
                for (int i = 0; i < statusListAdapter.getItemCount(); i++) {
                    //noinspection ConstantConditions
                    if (statusListAdapter.getItemAt(i) != null && statusListAdapter.getItemAt(i).getId().equals(status.getId())) {
                        //noinspection ConstantConditions
                        statusListAdapter.getItemAt(i).setEmojiTranslateFound(true);
                        try {
                            statusListAdapter.notifyItemChanged(i);
                        } catch (Exception ignored) {}
                    }
                }
            }
        }*/
    }

    @Override
    public void onRetrieveSearchEmoji(List<Emojis> emojis) {

    }


    private void translateToot(Status status){
        //Manages translations
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int trans = sharedpreferences.getInt(Helper.SET_TRANSLATOR, Helper.TRANS_YANDEX);
        MyTransL.translatorEngine et = MyTransL.translatorEngine.YANDEX;
        String api_key = null;



        if( trans == Helper.TRANS_YANDEX) {
            et = MyTransL.translatorEngine.YANDEX;
        }else if( trans == Helper.TRANS_DEEPL) {
            et = MyTransL.translatorEngine.DEEPL;
        }
        final MyTransL myTransL = MyTransL.getInstance(et);
        myTransL.setObfuscation(true);
        if( trans == Helper.TRANS_YANDEX) {
            api_key = sharedpreferences.getString(Helper.SET_YANDEX_API_KEY, Helper.YANDEX_KEY);
            myTransL.setYandexAPIKey(api_key);
        }else if( trans == Helper.TRANS_DEEPL) {
            api_key = sharedpreferences.getString(Helper.SET_DEEPL_API_KEY, "");
            myTransL.setDeeplAPIKey(api_key);
        }


        if( !status.isTranslated() ){
            String statusToTranslate;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                statusToTranslate = Html.fromHtml(status.getReblog() != null ?status.getReblog().getContent():status.getContent(), Html.FROM_HTML_MODE_LEGACY).toString();
            else
                //noinspection deprecation
                statusToTranslate = Html.fromHtml(status.getReblog() != null ?status.getReblog().getContent():status.getContent()).toString();
            //TODO: removes the replaceAll once fixed with the lib
            myTransL.translate(statusToTranslate, myTransL.getLocale(), new Results() {
                @Override
                public void onSuccess(Translate translate) {
                    if( translate.getTranslatedContent() != null) {
                        status.setTranslated(true);
                        status.setTranslationShown(true);
                        status.setContentTranslated(translate.getTranslatedContent());
                        status.makeClickableTranslation(context);
                        status.makeEmojisTranslation(context, StatusListAdapter.this);
                        notifyStatusChanged(status);
                    }else {
                        Toast.makeText(context, R.string.toast_error_translate, Toast.LENGTH_LONG).show();
                    }
                }
                @Override
                public void onFail(HttpsConnectionException e) {
                    e.printStackTrace();
                    Toast.makeText(context, R.string.toast_error_translate, Toast.LENGTH_LONG).show();
                }
            });
        }else {
            status.setTranslationShown(!status.isTranslationShown());
            notifyStatusChanged(status);
        }
    }
}