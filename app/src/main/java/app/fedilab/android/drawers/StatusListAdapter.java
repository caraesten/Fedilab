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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.TooltipCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.github.stom79.mytransl.MyTransL;
import com.github.stom79.mytransl.client.HttpsConnectionException;
import com.github.stom79.mytransl.client.Results;
import com.github.stom79.mytransl.translate.Translate;
import com.varunest.sparkbutton.SparkButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.fedilab.android.activities.AccountReportActivity;
import app.fedilab.android.client.API;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Application;
import app.fedilab.android.client.Entities.Attachment;
import app.fedilab.android.client.Entities.Card;
import app.fedilab.android.client.Entities.Emojis;
import app.fedilab.android.client.Entities.Error;
import app.fedilab.android.client.Entities.ManageTimelines;
import app.fedilab.android.client.Entities.Notification;
import app.fedilab.android.client.Entities.Poll;
import app.fedilab.android.client.Entities.PollOptions;
import app.fedilab.android.client.Entities.Status;
import app.fedilab.android.client.Entities.TagTimeline;
import app.fedilab.android.helper.CrossActions;
import app.fedilab.android.helper.CustomTextView;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.jobs.ScheduledBoostsSyncJob;
import app.fedilab.android.sqlite.AccountDAO;
import app.fedilab.android.sqlite.Sqlite;
import app.fedilab.android.sqlite.StatusCacheDAO;
import app.fedilab.android.sqlite.StatusStoredDAO;
import app.fedilab.android.sqlite.TempMuteDAO;
import app.fedilab.android.sqlite.TimelineCacheDAO;
import app.fedilab.android.sqlite.TimelinesDAO;
import br.com.felix.horizontalbargraph.HorizontalBar;
import br.com.felix.horizontalbargraph.model.BarItem;
import es.dmoral.toasty.Toasty;
import app.fedilab.android.R;
import app.fedilab.android.activities.BaseMainActivity;
import app.fedilab.android.activities.CustomSharingActivity;
import app.fedilab.android.activities.MainActivity;
import app.fedilab.android.activities.MediaActivity;
import app.fedilab.android.activities.PeertubeActivity;
import app.fedilab.android.activities.ShowAccountActivity;
import app.fedilab.android.activities.ShowConversationActivity;
import app.fedilab.android.activities.TootActivity;
import app.fedilab.android.activities.TootInfoActivity;
import app.fedilab.android.asynctasks.ManageCachedStatusAsyncTask;
import app.fedilab.android.asynctasks.ManagePollAsyncTask;
import app.fedilab.android.asynctasks.PostActionAsyncTask;
import app.fedilab.android.asynctasks.RetrieveFeedsAsyncTask;
import app.fedilab.android.asynctasks.UpdateAccountInfoAsyncTask;
import app.fedilab.android.fragments.DisplayStatusFragment;
import app.fedilab.android.interfaces.OnPollInterface;
import app.fedilab.android.interfaces.OnPostActionInterface;
import app.fedilab.android.interfaces.OnRefreshCachedStatusInterface;
import app.fedilab.android.interfaces.OnRetrieveCardInterface;
import app.fedilab.android.interfaces.OnRetrieveEmojiInterface;
import app.fedilab.android.interfaces.OnRetrieveFeedsInterface;
import app.fedilab.android.interfaces.OnRetrieveRepliesInterface;
import jp.wasabeef.glide.transformations.BlurTransformation;

import static app.fedilab.android.activities.BaseMainActivity.mPageReferenceMap;
import static app.fedilab.android.activities.BaseMainActivity.social;
import static app.fedilab.android.activities.MainActivity.currentLocale;
import static app.fedilab.android.helper.Helper.changeDrawableColor;


/**
 * Created by Thomas on 24/04/2017.
 * Adapter for Status
 */
public class StatusListAdapter extends RecyclerView.Adapter implements OnPostActionInterface, OnRetrieveFeedsInterface, OnRetrieveEmojiInterface, OnRetrieveRepliesInterface, OnRetrieveCardInterface, OnPollInterface, OnRefreshCachedStatusInterface {

    private Context context;
    private List<Status> statuses;
    private LayoutInflater layoutInflater;
    private boolean isOnWifi;
    private StatusListAdapter statusListAdapter;
    private RetrieveFeedsAsyncTask.Type type;
    private String targetedId;
    private final int HIDDEN_STATUS = 0;
    private static final int DISPLAYED_STATUS = 1;
    static final int FOCUSED_STATUS = 2;
    private static final int COMPACT_STATUS = 3;
    private static final int CONSOLE_STATUS = 4;
    private int conversationPosition;
    private List<String> timedMute;
    private boolean redraft;
    private Status toot;
    private TagTimeline tagTimeline;
    public static boolean fetch_all_more = false;

    public StatusListAdapter(Context context, RetrieveFeedsAsyncTask.Type type, String targetedId, boolean isOnWifi, List<Status> statuses){
        super();
        this.context = context;
        this.statuses = statuses;
        this.isOnWifi = isOnWifi;
        layoutInflater = LayoutInflater.from(this.context);
        statusListAdapter = this;
        this.type = type;
        this.targetedId = targetedId;
        redraft = false;
    }

    public StatusListAdapter(Context context, TagTimeline tagTimeline, String targetedId, boolean isOnWifi, List<Status> statuses){
        super();
        this.context = context;
        this.statuses = statuses;
        this.isOnWifi = isOnWifi;
        layoutInflater = LayoutInflater.from(this.context);
        statusListAdapter = this;
        this.type = RetrieveFeedsAsyncTask.Type.TAG;
        this.targetedId = targetedId;
        redraft = false;
        this.tagTimeline = tagTimeline;
    }

    public StatusListAdapter(Context context, int position, String targetedId, boolean isOnWifi, List<Status> statuses){
        this.context = context;
        this.statuses = statuses;
        this.isOnWifi = isOnWifi;
        layoutInflater = LayoutInflater.from(this.context);
        statusListAdapter = this;
        this.type = RetrieveFeedsAsyncTask.Type.CONTEXT;
        this.conversationPosition = position;
        this.targetedId = targetedId;
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

    @Override
    public void onPoll(Status status, Poll poll) {
        if( status.getReblog() != null){
            status.getReblog().setPoll(poll);
        }else{
            status.setPoll(poll);
        }
        notifyStatusChanged(status);
    }

    @Override
    public void onRefresh(Status refreshedStatus) {
        if( refreshedStatus.getCreated_at() == null){
            SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
            new TimelineCacheDAO(context, db).remove(refreshedStatus.getId());
            new PostActionAsyncTask(context, API.StatusAction.UNSTATUS, refreshedStatus.getId(), StatusListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        statusListAdapter.notifyStatusWithActionChanged(refreshedStatus);
    }


    private class ViewHolderEmpty extends RecyclerView.ViewHolder{
        ViewHolderEmpty(View itemView) {
            super(itemView);
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if( type != RetrieveFeedsAsyncTask.Type.ART && type != RetrieveFeedsAsyncTask.Type.PIXELFED && (tagTimeline == null || !tagTimeline.isART()) && (holder.getItemViewType() == DISPLAYED_STATUS || holder.getItemViewType() == COMPACT_STATUS|| holder.getItemViewType() == CONSOLE_STATUS)) {
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
        TextView status_account_displayname, status_account_displayname_owner;
        ImageView status_account_profile;
        ImageView status_account_profile_boost;
        ImageView status_account_profile_boost_by;
        TextView status_reply_count;
        TextView status_favorite_count;
        TextView status_reblog_count;
        TextView status_toot_date;
        RelativeLayout status_show_more;
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
        LinearLayout conversation_pp_2_container, conversation_pp_3_container;
        ImageView status_prev1_play_h;
        ImageView status_prev2_play_h;
        ImageView status_prev3_play_h;
        ImageView status_prev4_play_h;
        ImageView conversation_pp_1;
        ImageView conversation_pp_2;
        ImageView conversation_pp_3;
        ImageView conversation_pp_4;
        LinearLayout conversation_pp;
        RelativeLayout status_prev4_container;
        ImageView status_reply;
        ImageView status_pin;
        ImageView status_remove;
        ImageView status_privacy;
        ImageView status_translate;
        ImageView status_bookmark;
        LinearLayout status_container2;
        LinearLayout status_container3;
        LinearLayout main_container;
        TextView yandex_translate;
        ConstraintLayout status_action_container;
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
        RelativeLayout webview_preview;
        ImageView webview_preview_card;
        LinearLayout left_buttons;
        Button status_show_more_content;
        SparkButton spark_button_fav, spark_button_reblog;
        RelativeLayout horizontal_second_image;

        LinearLayout status_peertube_container;
        TextView status_peertube_reply, status_peertube_delete, show_more_content;
        ImageView cached_status, status_account_bot;
        ImageButton fedilab_features;
        ImageButton custom_feature_translate;
        ImageButton custom_feature_bookmark;
        ImageButton custom_feature_timed_mute;
        ImageButton custom_feature_schedule;
        ImageButton custom_feature_mention;
        ImageButton custom_feature_cache;
        ConstraintLayout fedilab_features_panel;
        //Poll
        LinearLayout poll_container, single_choice, multiple_choice, rated;
        RadioGroup radio_group;

        TextView number_votes, remaining_time;
        Button submit_vote, refresh_poll;

        public View getView(){
            return itemView;
        }

        ViewHolder(View itemView) {
            super(itemView);
            fetch_more =  itemView.findViewById(R.id.fetch_more);
            webview_preview_card = itemView.findViewById(R.id.webview_preview_card);
            webview_preview = itemView.findViewById(R.id.webview_preview);
            status_horizontal_document_container = itemView.findViewById(R.id.status_horizontal_document_container);
            status_document_container = itemView.findViewById(R.id.status_document_container);
            status_horizontal_document_container = itemView.findViewById(R.id.status_horizontal_document_container);
            status_content = itemView.findViewById(R.id.status_content);
            status_content_translated = itemView.findViewById(R.id.status_content_translated);
            status_account_username = itemView.findViewById(R.id.status_account_username);
            status_account_displayname = itemView.findViewById(R.id.status_account_displayname);
            status_account_displayname_owner = itemView.findViewById(R.id.status_account_displayname_owner);
            status_account_profile = itemView.findViewById(R.id.status_account_profile);
            status_account_profile_boost = itemView.findViewById(R.id.status_account_profile_boost);
            status_account_profile_boost_by = itemView.findViewById(R.id.status_account_profile_boost_by);
            status_reply_count = itemView.findViewById(R.id.status_reply_count);
            status_favorite_count = itemView.findViewById(R.id.status_favorite_count);
            status_reblog_count = itemView.findViewById(R.id.status_reblog_count);
            status_pin = itemView.findViewById(R.id.status_pin);
            status_remove = itemView.findViewById(R.id.status_remove);
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
            show_more_content = itemView.findViewById(R.id.show_more_content);
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
            conversation_pp = itemView.findViewById(R.id.conversation_pp);
            conversation_pp_1 = itemView.findViewById(R.id.conversation_pp_1);
            conversation_pp_2 = itemView.findViewById(R.id.conversation_pp_2);
            conversation_pp_3 = itemView.findViewById(R.id.conversation_pp_3);
            conversation_pp_4 = itemView.findViewById(R.id.conversation_pp_4);
            conversation_pp_2_container = itemView.findViewById(R.id.conversation_pp_2_container);
            conversation_pp_3_container = itemView.findViewById(R.id.conversation_pp_3_container);
            left_buttons =  itemView.findViewById(R.id.left_buttons);
            status_show_more_content = itemView.findViewById(R.id.status_show_more_content);
            spark_button_fav =  itemView.findViewById(R.id.spark_button_fav);
            spark_button_reblog =  itemView.findViewById(R.id.spark_button_reblog);
            horizontal_second_image = itemView.findViewById(R.id.horizontal_second_image);

            status_peertube_container = itemView.findViewById(R.id.status_peertube_container);
            status_peertube_reply = itemView.findViewById(R.id.status_peertube_reply);
            status_peertube_delete = itemView.findViewById(R.id.status_peertube_delete);
            fedilab_features = itemView.findViewById(R.id.fedilab_features);
            fedilab_features_panel = itemView.findViewById(R.id.fedilab_features_panel);
            custom_feature_translate = itemView.findViewById(R.id.custom_feature_translate);
            custom_feature_bookmark = itemView.findViewById(R.id.custom_feature_bookmark);
            custom_feature_timed_mute = itemView.findViewById(R.id.custom_feature_timed_mute);
            custom_feature_schedule = itemView.findViewById(R.id.custom_feature_schedule);
            custom_feature_mention = itemView.findViewById(R.id.custom_feature_mention);
            custom_feature_cache = itemView.findViewById(R.id.custom_feature_cache);
            poll_container = itemView.findViewById(R.id.poll_container);
            single_choice = itemView.findViewById(R.id.single_choice);
            multiple_choice = itemView.findViewById(R.id.multiple_choice);
            rated = itemView.findViewById(R.id.rated);
            radio_group = itemView.findViewById(R.id.radio_group);
            number_votes = itemView.findViewById(R.id.number_votes);
            remaining_time = itemView.findViewById(R.id.remaining_time);
            submit_vote = itemView.findViewById(R.id.submit_vote);
            refresh_poll = itemView.findViewById(R.id.refresh_poll);
            cached_status = itemView.findViewById(R.id.cached_status);
            status_account_bot = itemView.findViewById(R.id.status_account_bot);


        }
    }

    public Status getItem(int position){
        if( statuses.size() > position && position >= 0)
            return statuses.get(position);
        else return null;
    }

    @Override
    public int getItemViewType(int position) {

        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        boolean isCompactMode = sharedpreferences.getBoolean(Helper.SET_COMPACT_MODE, false);
        boolean isConsoleMode = sharedpreferences.getBoolean(Helper.SET_CONSOLE_MODE, false);
        if( !isConsoleMode && type == RetrieveFeedsAsyncTask.Type.CONTEXT && position == conversationPosition)
            return FOCUSED_STATUS;
        else if( type != RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE && !Helper.filterToots(context, statuses.get(position), timedMute, type))
            return HIDDEN_STATUS;
        else {
            if( isCompactMode)
                return COMPACT_STATUS;
            else if( isConsoleMode)
                return  CONSOLE_STATUS;
            else
                return DISPLAYED_STATUS;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       if( viewType == DISPLAYED_STATUS)
            return new ViewHolder(layoutInflater.inflate(R.layout.drawer_status, parent, false));
        else if(viewType == COMPACT_STATUS)
            return new ViewHolder(layoutInflater.inflate(R.layout.drawer_status_compact, parent, false));
       else if(viewType == CONSOLE_STATUS)
           return new ViewHolder(layoutInflater.inflate(R.layout.drawer_status_console, parent, false));
        else if(viewType == FOCUSED_STATUS)
            return new ViewHolder(layoutInflater.inflate(R.layout.drawer_status_focused, parent, false));
        else
            return new ViewHolderEmpty(layoutInflater.inflate(R.layout.drawer_empty, parent, false));
    }



    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int i) {
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        final String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);

        if( viewHolder.getItemViewType() != HIDDEN_STATUS ) {

            final ViewHolder holder = (ViewHolder) viewHolder;
            final Status status = statuses.get(i);
            if( status == null)
                return;

            //TODO:It sounds that sometimes this value is null - need deeper investigation
            if (status.getVisibility() == null) {
                status.setVisibility("public");
            }
            if (status.getReblog() != null && status.getReblog().getVisibility() == null) {
                status.getReblog().setVisibility("public");
            }
            status.setItemViewType(viewHolder.getItemViewType());


            boolean displayBookmarkButton = sharedpreferences.getBoolean(Helper.SET_SHOW_BOOKMARK, false);
            boolean fullAttachement = sharedpreferences.getBoolean(Helper.SET_FULL_PREVIEW, false);
            boolean isCompactMode = sharedpreferences.getBoolean(Helper.SET_COMPACT_MODE, false);
            boolean isConsoleMode = sharedpreferences.getBoolean(Helper.SET_CONSOLE_MODE, false);
            int iconSizePercent = sharedpreferences.getInt(Helper.SET_ICON_SIZE, 130);
            int textSizePercent = sharedpreferences.getInt(Helper.SET_TEXT_SIZE, 110);
            final boolean trans_forced = sharedpreferences.getBoolean(Helper.SET_TRANS_FORCED, false);
            int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
            boolean expand_cw = sharedpreferences.getBoolean(Helper.SET_EXPAND_CW, false);
            boolean expand_media = sharedpreferences.getBoolean(Helper.SET_EXPAND_MEDIA, false);
            boolean display_card = sharedpreferences.getBoolean(Helper.SET_DISPLAY_CARD, false);
            boolean display_video_preview = sharedpreferences.getBoolean(Helper.SET_DISPLAY_VIDEO_PREVIEWS, true);
            int truncate_toots_size = sharedpreferences.getInt(Helper.SET_TRUNCATE_TOOTS_SIZE, 0);

            boolean share_details = sharedpreferences.getBoolean(Helper.SET_SHARE_DETAILS, true);
            boolean confirmFav = sharedpreferences.getBoolean(Helper.SET_NOTIF_VALIDATION_FAV, false);
            boolean confirmBoost = sharedpreferences.getBoolean(Helper.SET_NOTIF_VALIDATION, true);

            boolean isModerator = sharedpreferences.getBoolean(Helper.PREF_IS_MODERATOR, false);
            boolean isAdmin = sharedpreferences.getBoolean(Helper.PREF_IS_ADMINISTRATOR, false);

            boolean fedilab_features_button = sharedpreferences.getBoolean(Helper.SET_DISPLAY_FEDILAB_FEATURES_BUTTON, true);

            boolean new_badge = sharedpreferences.getBoolean(Helper.SET_DISPLAY_NEW_BADGE, true);
            boolean bot_icon = sharedpreferences.getBoolean(Helper.SET_DISPLAY_BOT_ICON, true);

            int translator = sharedpreferences.getInt(Helper.SET_TRANSLATOR, Helper.TRANS_YANDEX);
            int behaviorWithAttachments = sharedpreferences.getInt(Helper.SET_ATTACHMENT_ACTION, Helper.ATTACHMENT_ALWAYS);

            if (status.getReblog() == null) {
                if (bot_icon && status.getAccount().isBot()) {
                    holder.status_account_bot.setVisibility(View.VISIBLE);
                } else {
                    holder.status_account_bot.setVisibility(View.GONE);
                }
            } else {
                if (bot_icon && status.getReblog().getAccount().isBot()) {
                    holder.status_account_bot.setVisibility(View.VISIBLE);
                } else {
                    holder.status_account_bot.setVisibility(View.GONE);
                }
            }



            //Display a preview for accounts that have replied *if enabled and only for home timeline*
            if (social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON) {
                holder.rated.setVisibility(View.GONE);
                holder.multiple_choice.setVisibility(View.GONE);
                holder.single_choice.setVisibility(View.GONE);
                holder.submit_vote.setVisibility(View.GONE);
                Poll poll;
                if( status.getReblog() != null) {
                    poll = status.getReblog().getPoll();
                }else {
                    poll = status.getPoll();
                }
                if( poll != null && poll.getOptionsList() != null){
                    if( poll.isVoted() || poll.isExpired()){
                        holder.rated.setVisibility(View.VISIBLE);
                        List<BarItem> items = new ArrayList<>();
                        int greaterValue = 0;
                        for(PollOptions pollOption: poll.getOptionsList()){
                            if( pollOption.getVotes_count() > greaterValue)
                                greaterValue = pollOption.getVotes_count();
                        }
                        for(PollOptions pollOption: poll.getOptionsList()){
                            double value = ((double)(pollOption.getVotes_count()* 100) / (double)poll.getVotes_count()) ;
                            if( pollOption.getVotes_count() == greaterValue) {
                                BarItem bar = new BarItem(pollOption.getTitle(), value, "%", ContextCompat.getColor(context, R.color.mastodonC4), Color.WHITE);
                                bar.setRounded(true);
                                bar.setHeight1(30);
                                items.add(bar);
                            }else {
                                BarItem bar;
                                if( theme == Helper.THEME_LIGHT)
                                    bar = new BarItem(pollOption.getTitle(), value, "%", ContextCompat.getColor(context, R.color.mastodonC2), Color.BLACK);
                                else
                                    bar = new BarItem(pollOption.getTitle(), value, "%", ContextCompat.getColor(context, R.color.mastodonC2), Color.WHITE);
                                bar.setRounded(true);
                                bar.setHeight1(30);
                                items.add(bar);
                            }
                        }
                        holder.rated.removeAllViews();
                        HorizontalBar horizontalBar = new HorizontalBar(context);
                        horizontalBar.hasAnimation(true).addAll(items).build();
                        holder.rated.addView(horizontalBar);
                    }else {
                        if( poll.isMultiple()){

                            if((holder.multiple_choice).getChildCount() > 0)
                                (holder.multiple_choice).removeAllViews();
                            for(PollOptions pollOption: poll.getOptionsList()){
                                CheckBox cb = new CheckBox(context);
                                cb.setText(pollOption.getTitle());
                                holder.multiple_choice.addView(cb);
                            }
                            holder.multiple_choice.setVisibility(View.VISIBLE);

                        }else {
                            if((holder.radio_group).getChildCount() > 0)
                                (holder.radio_group).removeAllViews();
                            for(PollOptions pollOption: poll.getOptionsList()){
                                RadioButton rb = new RadioButton(context);
                                rb.setText(pollOption.getTitle());
                                holder.radio_group.addView(rb);
                            }
                            holder.single_choice.setVisibility(View.VISIBLE);
                        }
                        holder.submit_vote.setVisibility(View.VISIBLE);
                        holder.submit_vote.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int [] choice;
                                if( poll.isMultiple()){
                                    ArrayList<Integer> choices = new ArrayList<>();
                                    int choicesCount = holder.multiple_choice.getChildCount();
                                    for( int i = 0 ; i < choicesCount ; i++){
                                        if( holder.multiple_choice.getChildAt(i) != null && holder.multiple_choice.getChildAt(i) instanceof CheckBox){
                                            if(((CheckBox) holder.multiple_choice.getChildAt(i)).isChecked()){
                                                choices.add(i);
                                            }
                                        }
                                    }
                                    choice = new int[choices.size()];
                                    Iterator<Integer> iterator = choices.iterator();
                                    for (int i = 0; i < choice.length; i++) {
                                        choice[i] = iterator.next().intValue();
                                    }
                                    if( choice.length == 0)
                                        return;
                                }else{
                                    choice = new int[1];
                                    choice[0] = -1;
                                    int choicesCount = holder.radio_group.getChildCount();
                                    for( int i = 0 ; i < choicesCount ; i++){
                                        if( holder.radio_group.getChildAt(i) != null && holder.radio_group.getChildAt(i) instanceof RadioButton){
                                            if(((RadioButton) holder.radio_group.getChildAt(i)).isChecked()){
                                                choice[0] = i;
                                            }
                                        }
                                    }
                                    if( choice[0] == -1)
                                        return;
                                }
                                new ManagePollAsyncTask(context, ManagePollAsyncTask.type_s.SUBMIT, status, choice, StatusListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            }
                        });
                    }

                    holder.refresh_poll.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new ManagePollAsyncTask(context, ManagePollAsyncTask.type_s.REFRESH, status, null, StatusListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                    });
                    holder.poll_container.setVisibility(View.VISIBLE);
                    holder.number_votes.setText(context.getResources().getQuantityString(R.plurals.number_of_vote,poll.getVotes_count(),poll.getVotes_count()));
                    holder.remaining_time.setText(context.getString(R.string.poll_finish_at, Helper.dateToStringPoll(poll.getExpires_at())));
                }else {
                    holder.poll_container.setVisibility(View.GONE);
                }
            }

            if (social == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE) {
                holder.status_action_container.setVisibility(View.GONE);
                holder.status_peertube_container.setVisibility(View.VISIBLE);
                holder.status_peertube_reply.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builderInner;
                        int style;
                        if (theme == Helper.THEME_DARK) {
                            style = R.style.DialogDark;
                        } else if (theme == Helper.THEME_BLACK) {
                            style = R.style.DialogBlack;
                        } else {
                            style = R.style.Dialog;
                        }
                        builderInner = new AlertDialog.Builder(context, style);
                        builderInner.setTitle(R.string.comment);
                        EditText input = new EditText(context);
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        input.setLayoutParams(lp);
                        builderInner.setView(input);
                        builderInner.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builderInner.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String comment = input.getText().toString();
                                if (comment.trim().length() > 0) {

                                    new PostActionAsyncTask(context, PeertubeActivity.video_id, comment, status.getId(), StatusListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                    dialog.dismiss();
                                }
                            }
                        });
                        builderInner.show();
                    }
                });
                if (status.getAccount().getId().equals(userId))
                    holder.status_peertube_delete.setVisibility(View.VISIBLE);
                else
                    holder.status_peertube_delete.setVisibility(View.GONE);
                holder.status_peertube_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builderInner;
                        int style;
                        if (theme == Helper.THEME_DARK) {
                            style = R.style.DialogDark;
                        } else if (theme == Helper.THEME_BLACK) {
                            style = R.style.DialogBlack;
                        } else {
                            style = R.style.Dialog;
                        }
                        builderInner = new AlertDialog.Builder(context, style);
                        builderInner.setTitle(R.string.delete_comment);
                        builderInner.setMessage(R.string.delete_comment_confirm);
                        builderInner.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builderInner.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new PostActionAsyncTask(context, API.StatusAction.PEERTUBEDELETECOMMENT, PeertubeActivity.video_id, null, status.getId(), StatusListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                dialog.dismiss();
                            }
                        });
                        builderInner.show();
                    }
                });

            }

            if (status.isNew() && new_badge){
                if (theme == Helper.THEME_BLACK)
                    holder.new_element.setImageResource(R.drawable.ic_fiber_new_dark);
                holder.new_element.setVisibility(View.VISIBLE);
            }
            else
                holder.new_element.setVisibility(View.GONE);


            holder.status_reply.getLayoutParams().height = (int) Helper.convertDpToPixel((20 * iconSizePercent / 100), context);
            holder.status_reply.getLayoutParams().width = (int) Helper.convertDpToPixel((20 * iconSizePercent / 100), context);
            holder.status_more.getLayoutParams().height = (int) Helper.convertDpToPixel((20 * iconSizePercent / 100), context);
            holder.status_more.getLayoutParams().width = (int) Helper.convertDpToPixel((20 * iconSizePercent / 100), context);
            holder.status_privacy.getLayoutParams().height = (int) Helper.convertDpToPixel((20 * iconSizePercent / 100), context);
            holder.status_privacy.getLayoutParams().width = (int) Helper.convertDpToPixel((20 * iconSizePercent / 100), context);


            if (getItemViewType(viewHolder.getAdapterPosition()) == FOCUSED_STATUS) {
                holder.status_content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16 * textSizePercent / 100);
                holder.status_account_displayname.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16 * textSizePercent / 100);
                holder.status_account_username.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14 * textSizePercent / 100);
                holder.status_toot_date.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14 * textSizePercent / 100);
                holder.status_content_translated.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16 * textSizePercent / 100);
            } else {
                holder.status_account_displayname.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14 * textSizePercent / 100);
                holder.status_account_username.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12 * textSizePercent / 100);
                holder.status_content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14 * textSizePercent / 100);
                holder.status_toot_date.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12 * textSizePercent / 100);
                holder.status_content_translated.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14 * textSizePercent / 100);
            }

            holder.status_spoiler.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14 * textSizePercent / 100);

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



            Helper.changeDrawableColor(context, R.drawable.ic_http, R.color.mastodonC4);
            if (getItemViewType(viewHolder.getAdapterPosition()) == COMPACT_STATUS || getItemViewType(viewHolder.getAdapterPosition()) == CONSOLE_STATUS )
                holder.status_privacy.setVisibility(View.GONE);
            else
                holder.status_privacy.setVisibility(View.VISIBLE);


            Helper.changeDrawableColor(context, R.drawable.video_preview, R.color.white);
            if (theme == Helper.THEME_BLACK) {
                Helper.changeDrawableColor(context, holder.cached_status, R.color.action_dark);
                Helper.changeDrawableColor(context, holder.status_remove, R.color.action_dark);
                Helper.changeDrawableColor(context, holder.status_reply, R.color.action_black);
                Helper.changeDrawableColor(context, holder.status_more, R.color.action_black);
                Helper.changeDrawableColor(context, holder.status_privacy, R.color.action_black);
                Helper.changeDrawableColor(context, R.drawable.ic_repeat, R.color.action_black);
                Helper.changeDrawableColor(context, R.drawable.ic_conversation, R.color.action_black);
                Helper.changeDrawableColor(context, R.drawable.ic_plus_one, R.color.action_black);
                Helper.changeDrawableColor(context, R.drawable.ic_pin_drop, R.color.action_black);
                holder.status_reply_count.setTextColor(ContextCompat.getColor(context, R.color.action_black));
                holder.status_favorite_count.setTextColor(ContextCompat.getColor(context, R.color.action_black));
                holder.status_reblog_count.setTextColor(ContextCompat.getColor(context, R.color.action_black));


                Helper.changeDrawableColor(context, R.drawable.ic_photo, R.color.dark_text);
                Helper.changeDrawableColor(context, R.drawable.ic_remove_red_eye, R.color.dark_text);
                Helper.changeDrawableColor(context, R.drawable.ic_repeat_head_toot, R.color.black_text_toot_header);


                Helper.changeDrawableColor(context, R.drawable.ic_fetch_more, R.color.dark_icon);
                holder.status_cardview_title.setTextColor(ContextCompat.getColor(context, R.color.black_text_toot_header));
                holder.status_cardview_content.setTextColor(ContextCompat.getColor(context, R.color.dark_icon));
                holder.status_cardview_url.setTextColor(ContextCompat.getColor(context, R.color.black_text_toot_header));

                Helper.changeDrawableColor(context, R.drawable.ic_bookmark, R.color.black);
                Helper.changeDrawableColor(context, R.drawable.ic_bookmark_border, R.color.black);
                Helper.changeDrawableColor(context, R.drawable.ic_translate, R.color.black);
                holder.status_cardview.setBackgroundResource(R.drawable.card_border_black);
            } else if (theme == Helper.THEME_DARK) {
                Helper.changeDrawableColor(context, holder.cached_status, R.color.action_dark);
                Helper.changeDrawableColor(context, holder.status_remove, R.color.action_dark);
                Helper.changeDrawableColor(context, holder.status_reply, R.color.action_dark);
                Helper.changeDrawableColor(context, holder.status_more, R.color.action_dark);
                Helper.changeDrawableColor(context, R.drawable.ic_repeat, R.color.action_dark);
                Helper.changeDrawableColor(context, holder.status_privacy, R.color.action_dark);
                Helper.changeDrawableColor(context, R.drawable.ic_plus_one, R.color.action_dark);
                Helper.changeDrawableColor(context, R.drawable.ic_pin_drop, R.color.action_dark);
                Helper.changeDrawableColor(context, R.drawable.ic_conversation, R.color.action_dark);
                holder.status_reply_count.setTextColor(ContextCompat.getColor(context, R.color.action_dark));
                holder.status_favorite_count.setTextColor(ContextCompat.getColor(context, R.color.action_dark));
                holder.status_reblog_count.setTextColor(ContextCompat.getColor(context, R.color.action_dark));
                Helper.changeDrawableColor(context, R.drawable.ic_audio_wave, R.color.action_dark);
                Helper.changeDrawableColor(context, R.drawable.ic_repeat_head_toot, R.color.dark_text_toot_header);

                Helper.changeDrawableColor(context, R.drawable.ic_photo, R.color.mastodonC4);
                Helper.changeDrawableColor(context, R.drawable.ic_remove_red_eye, R.color.mastodonC4);
                Helper.changeDrawableColor(context, R.drawable.ic_fetch_more, R.color.mastodonC4);


                holder.status_cardview_title.setTextColor(ContextCompat.getColor(context, R.color.dark_text_toot_header));
                holder.status_cardview_content.setTextColor(ContextCompat.getColor(context, R.color.dark_icon));
                holder.status_cardview_url.setTextColor(ContextCompat.getColor(context, R.color.dark_text_toot_header));
                holder.status_cardview.setBackgroundResource(R.drawable.card_border_dark);
                Helper.changeDrawableColor(context, R.drawable.ic_bookmark, R.color.mastodonC1);
                Helper.changeDrawableColor(context, R.drawable.ic_bookmark_border, R.color.mastodonC1);
                Helper.changeDrawableColor(context, R.drawable.ic_translate, R.color.mastodonC1);
            } else {
                Helper.changeDrawableColor(context, holder.cached_status, R.color.action_light);
                Helper.changeDrawableColor(context, holder.status_remove, R.color.action_light);
                Helper.changeDrawableColor(context, R.drawable.ic_fetch_more, R.color.action_light);
                Helper.changeDrawableColor(context, holder.status_reply, R.color.action_light);
                Helper.changeDrawableColor(context, R.drawable.ic_conversation, R.color.action_light);
                Helper.changeDrawableColor(context, R.drawable.ic_more_horiz, R.color.action_light);
                Helper.changeDrawableColor(context, holder.status_more, R.color.action_light);
                Helper.changeDrawableColor(context, holder.status_privacy, R.color.action_light);
                Helper.changeDrawableColor(context, R.drawable.ic_repeat, R.color.action_light);
                Helper.changeDrawableColor(context, R.drawable.ic_plus_one, R.color.action_light);
                Helper.changeDrawableColor(context, R.drawable.ic_pin_drop, R.color.action_light);
                holder.status_reply_count.setTextColor(ContextCompat.getColor(context, R.color.action_light));
                holder.status_favorite_count.setTextColor(ContextCompat.getColor(context, R.color.action_light));
                holder.status_reblog_count.setTextColor(ContextCompat.getColor(context, R.color.action_light));

                holder.status_cardview.setBackgroundResource(R.drawable.card_border_light);
                Helper.changeDrawableColor(context, R.drawable.ic_photo, R.color.mastodonC4);
                Helper.changeDrawableColor(context, R.drawable.ic_remove_red_eye, R.color.mastodonC4);

                Helper.changeDrawableColor(context, R.drawable.ic_repeat_head_toot, R.color.action_light_header);


                holder.status_cardview_title.setTextColor(ContextCompat.getColor(context, R.color.light_black));
                holder.status_cardview_content.setTextColor(ContextCompat.getColor(context, R.color.light_black));
                holder.status_cardview_url.setTextColor(ContextCompat.getColor(context, R.color.light_black));

                Helper.changeDrawableColor(context, R.drawable.ic_bookmark, R.color.white);
                Helper.changeDrawableColor(context, R.drawable.ic_bookmark_border, R.color.white);
                Helper.changeDrawableColor(context, R.drawable.ic_translate, R.color.white);
            }
            if (theme == Helper.THEME_DARK) {
                holder.status_account_displayname.setTextColor(ContextCompat.getColor(context, R.color.dark_text_toot_header));
                holder.status_toot_date.setTextColor(ContextCompat.getColor(context, R.color.dark_text_toot_header));
            } else if (theme == Helper.THEME_BLACK) {
                holder.status_account_displayname.setTextColor(ContextCompat.getColor(context, R.color.black_text_toot_header));
                holder.status_toot_date.setTextColor(ContextCompat.getColor(context, R.color.black_text_toot_header));
            } else if (theme == Helper.THEME_LIGHT) {
                holder.status_account_displayname.setTextColor(ContextCompat.getColor(context, R.color.action_light_header));
                holder.status_toot_date.setTextColor(ContextCompat.getColor(context, R.color.light_black));
            }



            if( holder.status_bookmark != null) {
                if (status.isBookmarked())
                    holder.status_bookmark.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_bookmark));
                else
                    holder.status_bookmark.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_bookmark_border));
                if (type != RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE && !isCompactMode && !isConsoleMode && displayBookmarkButton)
                    holder.status_bookmark.setVisibility(View.VISIBLE);
                else
                    holder.status_bookmark.setVisibility(View.GONE);

                holder.status_bookmark.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bookmark(status);
                    }
                });
                holder.status_bookmark.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        CrossActions.doCrossBookmark(context, status, statusListAdapter);
                        return false;
                    }
                });
            }

            if( holder.cached_status != null && (holder.getItemViewType() == DISPLAYED_STATUS && !fedilab_features_button)) {
                if (status.iscached()) {
                    holder.cached_status.setVisibility(View.VISIBLE);
                } else {
                    holder.cached_status.setVisibility(View.GONE);
                }
                holder.cached_status.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new ManageCachedStatusAsyncTask(context, status.getId(), StatusListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                });
            }

            if (holder.fedilab_features != null && !fedilab_features_button)
                holder.fedilab_features.setVisibility(View.GONE);

            if (holder.fedilab_features != null && fedilab_features_button) {
                TooltipCompat.setTooltipText(holder.fedilab_features, context.getString(R.string.app_features));
                holder.fedilab_features.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean state = status.isCustomFeaturesDisplayed();
                        closePanels();
                        status.setCustomFeaturesDisplayed(!state);
                        notifyStatusChanged(status);
                    }
                });

                if (status.isCustomFeaturesDisplayed()) {
                    holder.fedilab_features_panel.setVisibility(View.VISIBLE);
                } else {
                    holder.fedilab_features_panel.setVisibility(View.GONE);
                }
                if (theme == Helper.THEME_LIGHT) {
                    holder.fedilab_features_panel.setBackgroundColor(ContextCompat.getColor(context, R.color.custom_features_panel_background_light));
                }

                if( !status.iscached()){
                    holder.custom_feature_cache.setVisibility(View.GONE);
                }

                if (status.isBookmarked())
                    holder.custom_feature_bookmark.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_bookmark_white_full));
                else
                    holder.custom_feature_bookmark.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_bookmark_white));

                TooltipCompat.setTooltipText(holder.custom_feature_translate, context.getString(R.string.translate));
                holder.custom_feature_translate.setOnClickListener(view -> {
                    translateToot(status);
                    status.setCustomFeaturesDisplayed(false);
                    notifyStatusChanged(status);
                });

                holder.custom_feature_bookmark.setOnClickListener(view -> {
                    bookmark(status);
                    status.setCustomFeaturesDisplayed(false);
                    notifyStatusChanged(status);
                });
                holder.custom_feature_bookmark.setOnLongClickListener(view -> {
                    CrossActions.doCrossBookmark(context, status, statusListAdapter);
                    status.setCustomFeaturesDisplayed(false);
                    notifyStatusChanged(status);
                    return false;
                });

                if (type == RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE){
                    holder.custom_feature_timed_mute.setVisibility(View.GONE);
                    holder.custom_feature_schedule.setVisibility(View.GONE);
                    holder.custom_feature_cache.setVisibility(View.GONE);
                }
                TooltipCompat.setTooltipText(holder.custom_feature_timed_mute, context.getString(R.string.timed_mute));
                holder.custom_feature_timed_mute.setOnClickListener(view -> {
                    timedMuteAction(status);
                    status.setCustomFeaturesDisplayed(false);
                    notifyStatusChanged(status);
                });

                TooltipCompat.setTooltipText(holder.custom_feature_schedule, context.getString(R.string.schedule_boost));
                holder.custom_feature_schedule.setOnClickListener(view -> {
                    scheduleBoost(status);
                    status.setCustomFeaturesDisplayed(false);
                    notifyStatusChanged(status);
                });

                TooltipCompat.setTooltipText(holder.custom_feature_mention, context.getString(R.string.mention_status));
                holder.custom_feature_mention.setOnClickListener(view -> {
                    mention(status);
                    status.setCustomFeaturesDisplayed(false);
                    notifyStatusChanged(status);
                });

                TooltipCompat.setTooltipText(holder.custom_feature_cache, context.getString(R.string.refresh_cache));
                holder.custom_feature_cache.setOnClickListener(view -> {
                    new ManageCachedStatusAsyncTask(context, status.getId(), StatusListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    status.setCustomFeaturesDisplayed(false);
                    notifyStatusChanged(status);
                });
            }



            //Redraws top icons (boost/reply)
            final float scale = context.getResources().getDisplayMetrics().density;
            holder.spark_button_fav.pressOnTouch(false);
            holder.spark_button_reblog.pressOnTouch(false);
            holder.spark_button_fav.setActiveImage(R.drawable.ic_star);
            holder.spark_button_fav.setInactiveImage(R.drawable.ic_star_border);
            holder.spark_button_fav.setDisableCircle(true);
            holder.spark_button_reblog.setDisableCircle(true);
            holder.spark_button_fav.setActiveImageTint(R.color.marked_icon);
            holder.spark_button_reblog.setActiveImageTint(R.color.boost_icon);
            if (theme == Helper.THEME_DARK) {
                holder.spark_button_fav.setInActiveImageTint(R.color.action_dark);
                holder.spark_button_reblog.setInActiveImageTint(R.color.action_dark);
            } else if (theme == Helper.THEME_BLACK) {
                holder.spark_button_fav.setInActiveImageTint(R.color.action_black);
                holder.spark_button_reblog.setInActiveImageTint(R.color.action_black);
            } else {
                holder.spark_button_fav.setInActiveImageTint(R.color.action_light);
                holder.spark_button_reblog.setInActiveImageTint(R.color.action_light);
            }
            holder.spark_button_fav.setColors(R.color.marked_icon, R.color.marked_icon);
            holder.spark_button_fav.setImageSize((int) (20 * iconSizePercent / 100 * scale + 0.5f));
            holder.spark_button_fav.setMinimumWidth((int) Helper.convertDpToPixel((20 * iconSizePercent / 100 * scale + 0.5f), context));

            holder.spark_button_reblog.setColors(R.color.boost_icon, R.color.boost_icon);
            holder.spark_button_reblog.setImageSize((int) (20 * iconSizePercent / 100 * scale + 0.5f));
            holder.spark_button_reblog.setMinimumWidth((int) Helper.convertDpToPixel((20 * iconSizePercent / 100 * scale + 0.5f), context));

            Drawable imgConversation = null;
            if (type != RetrieveFeedsAsyncTask.Type.CONTEXT && ((status.getIn_reply_to_account_id() != null && status.getIn_reply_to_account_id().equals(status.getAccount().getId()))
                    || (status.getReblog() != null && status.getReblog().getIn_reply_to_account_id() != null && status.getReblog().getIn_reply_to_account_id().equals(status.getReblog().getAccount().getId())))) {
                imgConversation = ContextCompat.getDrawable(context, R.drawable.ic_conversation);
                imgConversation.setBounds(0, 0, (int) (15 * iconSizePercent / 100 * scale + 0.5f), (int) (15 * iconSizePercent / 100 * scale + 0.5f));
            }
            if (status.getReblog() != null) {
                Drawable img = ContextCompat.getDrawable(context, R.drawable.ic_repeat_head_toot);
                assert img != null;
                img.setBounds(0, 0, (int) (20 * iconSizePercent / 100 * scale + 0.5f), (int) (15 * iconSizePercent / 100 * scale + 0.5f));
                holder.status_account_displayname.setCompoundDrawables(img, null, null, null);
                holder.status_toot_date.setCompoundDrawables(imgConversation, null, null, null);
            } else {
                holder.status_account_displayname.setCompoundDrawables(null, null, null, null);
                holder.status_toot_date.setCompoundDrawables(imgConversation, null, null , null);
            }
            if( expand_media && status.isSensitive() || (status.getReblog() != null && status.getReblog().isSensitive())) {
                Helper.changeDrawableColor(context, holder.hide_preview, R.color.red_1);
                Helper.changeDrawableColor(context, holder.hide_preview_h, R.color.red_1);
            }else {
                Helper.changeDrawableColor(context, holder.hide_preview, R.color.white);
                Helper.changeDrawableColor(context, holder.hide_preview_h, R.color.white);
            }

            if (!status.isClickable())
                Status.transform(context, status);
            if (!status.isEmojiFound())
                Status.makeEmojis(context, this, status);
            holder.status_content.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP && !view.hasFocus()) {
                        try {
                            view.requestFocus();
                        } catch (Exception ignored) {
                        }
                    }
                    return false;
                }
            });
            holder.status_spoiler.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP && !view.hasFocus()) {
                        try {
                            view.requestFocus();
                        } catch (Exception ignored) {
                        }
                    }
                    return false;
                }
            });
            //Click on a conversation

            if ((getItemViewType(viewHolder.getAdapterPosition()) == DISPLAYED_STATUS || getItemViewType(viewHolder.getAdapterPosition()) == COMPACT_STATUS || getItemViewType(viewHolder.getAdapterPosition()) == CONSOLE_STATUS)) {
                holder.status_spoiler.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (type != RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE) {
                            Intent intent = new Intent(context, ShowConversationActivity.class);
                            int position = closePanels();
                            if(  holder.getAdapterPosition() == position)
                                return;
                            Bundle b = new Bundle();
                            if( social == UpdateAccountInfoAsyncTask.SOCIAL.GNU || social == UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA)
                                b.putString("conversationId", status.getConversationId());
                            if (status.getReblog() == null)
                                b.putParcelable("status", status);
                            else
                                b.putParcelable("status", status.getReblog());
                            intent.putExtras(b);
                            if (type == RetrieveFeedsAsyncTask.Type.CONTEXT)
                                ((Activity) context).finish();
                            context.startActivity(intent);
                        } else {
                            if (social != UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE)
                                CrossActions.doCrossConversation(context, status);
                        }
                    }
                });
                holder.status_content.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (type != RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE) {
                            Intent intent = new Intent(context, ShowConversationActivity.class);
                            int position = closePanels();
                            if(  holder.getAdapterPosition() == position)
                                return;
                            Bundle b = new Bundle();
                            if( social == UpdateAccountInfoAsyncTask.SOCIAL.GNU || social == UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA)
                                b.putString("conversationId", status.getConversationId());
                            if (status.getReblog() == null)
                                b.putParcelable("status", status);
                            else
                                b.putParcelable("status", status.getReblog());
                            intent.putExtras(b);
                            if (type == RetrieveFeedsAsyncTask.Type.CONTEXT)
                                ((Activity) context).finish();
                            context.startActivity(intent);
                        } else {
                            if (social != UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE)
                                CrossActions.doCrossConversation(context, status);
                        }
                    }
                });
                holder.main_container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (type != RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE) {
                            Intent intent = new Intent(context, ShowConversationActivity.class);
                            int position = closePanels();
                            if(  holder.getAdapterPosition() == position)
                                return;
                            Bundle b = new Bundle();
                            if( social == UpdateAccountInfoAsyncTask.SOCIAL.GNU || social == UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA)
                                b.putString("conversationId", status.getConversationId());
                            if (status.getReblog() == null)
                                b.putParcelable("status", status);
                            else
                                b.putParcelable("status", status.getReblog());
                            intent.putExtras(b);
                            if (type == RetrieveFeedsAsyncTask.Type.CONTEXT)
                                ((Activity) context).finish();
                            context.startActivity(intent);
                        } else {
                            if (social != UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE)
                                CrossActions.doCrossConversation(context, status);
                        }
                    }
                });
            }

            if( holder.status_translate != null) {
                holder.status_translate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        translateToot(status);
                    }
                });
                boolean differentLanguage;
                if (status.getReblog() == null)
                    differentLanguage = status.getLanguage() != null && !status.getLanguage().trim().equals(currentLocale);
                else
                    differentLanguage = status.getReblog().getLanguage() != null && !status.getReblog().getLanguage().trim().equals(currentLocale);
                if ((getItemViewType(viewHolder.getAdapterPosition()) != COMPACT_STATUS) &&  getItemViewType(viewHolder.getAdapterPosition()) != CONSOLE_STATUS && (trans_forced || (translator != Helper.TRANS_NONE && currentLocale != null && differentLanguage))) {
                    if (status.getSpoiler_text() != null && status.getSpoiler_text().length() > 0) {
                        if (status.isSpoilerShown() || expand_cw  || getItemViewType(viewHolder.getAdapterPosition()) == FOCUSED_STATUS) {
                            holder.status_translate.setVisibility(View.VISIBLE);
                        } else {
                            holder.status_translate.setVisibility(View.GONE);
                        }
                    } else if (status.getReblog() != null && status.getReblog().getSpoiler_text() != null && status.getReblog().getSpoiler_text().length() > 0) {
                        if (status.isSpoilerShown() || expand_cw   || getItemViewType(viewHolder.getAdapterPosition()) == FOCUSED_STATUS) {
                            holder.status_translate.setVisibility(View.VISIBLE);
                        } else {
                            holder.status_translate.setVisibility(View.GONE);
                        }
                    } else {
                        holder.status_translate.setVisibility(View.VISIBLE);
                    }
                } else {
                    holder.status_translate.setVisibility(View.GONE);
                }
            }

            if( isConsoleMode){
                String starting = "";
                String acct = status.getAccount().getAcct();
                String acctReblog = null;
                if( !acct.contains("@"))
                    acct += "@" + Helper.getLiveInstance(context);
                if( status.getReblog() != null){
                    acctReblog = status.getReblog().getAccount().getAcct();
                    if( !acctReblog.contains("@"))
                        acctReblog += "@" + Helper.getLiveInstance(context);
                }
                SpannableString acctSpan = new SpannableString(acct+":~$");
                SpannableString acctReblogSpan = null;
                if( acctReblog != null)
                    acctReblogSpan = new SpannableString( " <" + acctReblog + ">");

                if (theme == Helper.THEME_LIGHT)
                    acctSpan.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.console_marker)), (acctSpan.length()-3), acctSpan.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                else if (theme == Helper.THEME_DARK)
                    acctSpan.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.console_marker)), (acctSpan.length()-3), acctSpan.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                else if (theme == Helper.THEME_BLACK)
                    acctSpan.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.console_marker)), (acctSpan.length()-3), acctSpan.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

                URLSpan[] urls = acctSpan.getSpans(0, (acctSpan.length()-3), URLSpan.class);
                for(URLSpan span : urls)
                    acctSpan.removeSpan(span);
                acctSpan.setSpan(new ClickableSpan() {
                             @Override
                             public void onClick(@NonNull View textView) {
                                 Intent intent = new Intent(context, ShowAccountActivity.class);
                                 Bundle b = new Bundle();
                                 b.putParcelable("account", status.getAccount());
                                 intent.putExtras(b);
                                 context.startActivity(intent);
                             }
                             @Override
                             public void updateDrawState(@NonNull TextPaint ds) {
                                 super.updateDrawState(ds);
                                 ds.setUnderlineText(false);
                                 if (theme == Helper.THEME_DARK)
                                     ds.setColor(ContextCompat.getColor(context, R.color.console_name));
                                 else if (theme == Helper.THEME_BLACK)
                                     ds.setColor(ContextCompat.getColor(context, R.color.console_name));
                                 else if (theme == Helper.THEME_LIGHT)
                                     ds.setColor(ContextCompat.getColor(context, R.color.console_name));
                             }
                         },
                        0, (acctSpan.length()-3),
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                SpannableString startingSpan = new SpannableString(starting);
                if( acctReblogSpan != null) {
                    for(URLSpan span : urls)
                        acctReblogSpan.removeSpan(span);
                    acctReblogSpan.setSpan(new ClickableSpan() {
                             @Override
                             public void onClick(@NonNull View textView) {
                                 Intent intent = new Intent(context, ShowAccountActivity.class);
                                 Bundle b = new Bundle();
                                 b.putParcelable("account", status.getReblog().getAccount());
                                 intent.putExtras(b);
                                 context.startActivity(intent);
                             }
                             @Override
                             public void updateDrawState(@NonNull TextPaint ds) {
                                 super.updateDrawState(ds);
                                 ds.setUnderlineText(false);
                                 if (theme == Helper.THEME_DARK)
                                     ds.setColor(ContextCompat.getColor(context, R.color.console_reblog_name));
                                 else if (theme == Helper.THEME_BLACK)
                                     ds.setColor(ContextCompat.getColor(context, R.color.console_reblog_name));
                                 else if (theme == Helper.THEME_LIGHT)
                                     ds.setColor(ContextCompat.getColor(context, R.color.console_reblog_name));
                             }
                         },
                            2, acctReblogSpan.length()-1,
                            Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    if (theme == Helper.THEME_LIGHT)
                        acctReblogSpan.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.console_marker)), 1, 2, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    else if (theme == Helper.THEME_DARK)
                        acctReblogSpan.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.console_marker)),  1, 2,Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    else if (theme == Helper.THEME_BLACK)
                        acctReblogSpan.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.console_marker)),  1, 2, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    if (theme == Helper.THEME_LIGHT)
                        acctReblogSpan.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.console_marker)), acctReblogSpan.length()-1, acctReblogSpan.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    else if (theme == Helper.THEME_DARK)
                        acctReblogSpan.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.console_marker)),  acctReblogSpan.length()-1, acctReblogSpan.length(),Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    else if (theme == Helper.THEME_BLACK)
                        acctReblogSpan.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.console_marker)),  acctReblogSpan.length()-1, acctReblogSpan.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

                    startingSpan = new SpannableString(TextUtils.concat(acctSpan, " ", acctReblogSpan));
                }else
                    startingSpan = acctSpan;
                if( status.getReblog() == null && status.getSpoiler_text() != null && status.getSpoiler_text().length() > 0) {
                    holder.status_spoiler.setText(TextUtils.concat(startingSpan, " ", status.getContentSpanCW()), TextView.BufferType.SPANNABLE);
                    holder.status_content.setText(status.getContentSpan(), TextView.BufferType.SPANNABLE);
                }else if( status.getReblog() != null && status.getReblog().getSpoiler_text() != null && status.getReblog().getSpoiler_text().length() > 0) {
                    holder.status_spoiler.setText(TextUtils.concat(startingSpan, " ", status.getContentSpanCW()), TextView.BufferType.SPANNABLE);
                    holder.status_content.setText(status.getContentSpan(), TextView.BufferType.SPANNABLE);
                } else {
                    holder.status_spoiler.setText(status.getContentSpanCW(), TextView.BufferType.SPANNABLE);
                    holder.status_content.setText(TextUtils.concat(startingSpan, " ", status.getContentSpan()!=null?status.getContentSpan():""), TextView.BufferType.SPANNABLE);
                }

            }else {
                holder.status_content.setText(status.getContentSpan(), TextView.BufferType.SPANNABLE);
                holder.status_spoiler.setText(status.getContentSpanCW(), TextView.BufferType.SPANNABLE);
            }



            holder.status_content.setMovementMethod(LinkMovementMethod.getInstance());
            holder.status_spoiler.setMovementMethod(LinkMovementMethod.getInstance());
            if (truncate_toots_size > 0) {
                holder.status_content.setMaxLines(truncate_toots_size);
                if (status.getNumberLines() == -1) {
                    status.setNumberLines(-2);
                    holder.status_show_more_content.setVisibility(View.GONE);
                    holder.status_content.post(new Runnable() {
                        @Override
                        public void run() {
                            status.setNumberLines(holder.status_content.getLineCount());
                            if (status.getNumberLines() > truncate_toots_size) {
                                notifyStatusChanged(status);
                            }
                        }
                    });
                } else if (status.getNumberLines() > truncate_toots_size) {
                    holder.status_show_more_content.setVisibility(View.VISIBLE);
                    if (status.isExpanded()) {
                        holder.status_content.setMaxLines(Integer.MAX_VALUE);
                        holder.status_show_more_content.setText(R.string.hide_toot_truncate);
                    } else {
                        holder.status_content.setMaxLines(truncate_toots_size);
                        holder.status_show_more_content.setText(R.string.display_toot_truncate);
                    }
                } else {
                    holder.status_show_more_content.setVisibility(View.GONE);
                }
            }
            holder.status_show_more_content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    status.setExpanded(!status.isExpanded());
                    notifyStatusChanged(status);
                }
            });


            holder.status_content_translated.setMovementMethod(LinkMovementMethod.getInstance());
            //-------- END -> Manages translations

            if (status.getAccount() == null) {
                final SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                String instance = sharedpreferences.getString(Helper.PREF_INSTANCE, null);
                Account account = new AccountDAO(context, db).getUniqAccount(userId, instance);
                status.setAccount(account);
            }
            //Displays name & emoji in toot header
            final String ppurl;
            if (status.getReblog() != null) {
                ppurl = status.getReblog().getAccount().getAvatar();
                holder.status_account_displayname.setVisibility(View.VISIBLE);
                holder.status_account_displayname.setText(context.getResources().getString(R.string.reblog_by, status.getAccount().getUsername()));
                holder.status_account_displayname.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, ShowAccountActivity.class);
                        Bundle b = new Bundle();
                        b.putParcelable("account", status.getAccount());
                        intent.putExtras(b);
                        context.startActivity(intent);
                    }
                });
                if (status.getReblog().getAccount().getDisplay_name().length() > 0)
                    holder.status_account_displayname_owner.setText(status.getDisplayNameSpan(), TextView.BufferType.SPANNABLE);
                else
                    holder.status_account_displayname_owner.setText(status.getReblog().getAccount().getAcct().replace("@", ""));
                holder.status_account_displayname_owner.setVisibility(View.VISIBLE);

            } else {
                ppurl = status.getAccount().getAvatar();
                holder.status_account_displayname.setVisibility(View.GONE);
                if (status.getAccount().getdisplayNameSpan() == null || status.getAccount().getdisplayNameSpan().toString().trim().length() == 0)
                    holder.status_account_displayname_owner.setText(status.getAccount().getUsername().replace("@", ""), TextView.BufferType.SPANNABLE);
                else
                    holder.status_account_displayname_owner.setText(status.getAccount().getdisplayNameSpan(), TextView.BufferType.SPANNABLE);
            }
            //-------- END -> Displays name & emoji in toot header

            //Change the color in gray for accounts in DARK Theme only
            Spannable wordtoSpan;
            Pattern hashAcct;
            if (status.getReblog() != null) {
                wordtoSpan = new SpannableString("@" + status.getReblog().getAccount().getAcct());
                hashAcct = Pattern.compile("(@" + status.getReblog().getAccount().getAcct() + ")");
            } else {
                wordtoSpan = new SpannableString("@" + status.getAccount().getAcct());
                hashAcct = Pattern.compile("(@" + status.getAccount().getAcct() + ")");
            }
            if (hashAcct != null) {
                Matcher matcherAcct = hashAcct.matcher(wordtoSpan);
                while (matcherAcct.find()) {
                    int matchStart = matcherAcct.start(1);
                    int matchEnd = matcherAcct.end();
                    if (wordtoSpan.length() >= matchEnd && matchStart < matchEnd) {
                        if (theme == Helper.THEME_LIGHT)
                            wordtoSpan.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.action_light_header)), matchStart, matchEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        else if (theme == Helper.THEME_DARK)
                            wordtoSpan.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.dark_text_toot_header)), matchStart, matchEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        else if (theme == Helper.THEME_BLACK)
                            wordtoSpan.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.black_text_toot_header)), matchStart, matchEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    }

                }
            }
            holder.status_account_username.setText(wordtoSpan);

            //-------- END -> Change the color in gray for accounts in DARK Theme only

            if (status.isFetchMore()) {
                holder.fetch_more.setVisibility(View.VISIBLE);
                holder.fetch_more.setEnabled(true);
                holder.fetch_more.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        status.setFetchMore(false);
                        holder.fetch_more.setEnabled(false);
                        holder.fetch_more.setVisibility(View.GONE);
                        if( context instanceof BaseMainActivity) {
                            SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                            List<ManageTimelines> timelines = new TimelinesDAO(context, db).getDisplayedTimelines();
                            for(ManageTimelines tl: timelines) {
                                if( tl.getType() == ManageTimelines.Type.HOME) {
                                    DisplayStatusFragment homeFragment = (DisplayStatusFragment) mPageReferenceMap.get(tl.getPosition());
                                    if (homeFragment != null)
                                        homeFragment.fetchMore(status.getId());
                                    break;
                                }
                            }
                        }else{
                            Toasty.error(context, context.getString(R.string.toast_error), Toast.LENGTH_LONG).show();
                        }
                    }
                });
                holder.fetch_more.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        status.setFetchMore(false);
                        holder.fetch_more.setEnabled(false);
                        holder.fetch_more.setVisibility(View.GONE);
                        if( context instanceof BaseMainActivity) {
                            SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                            List<ManageTimelines> timelines = new TimelinesDAO(context, db).getDisplayedTimelines();
                            for(ManageTimelines tl: timelines) {
                                if( tl.getType() == ManageTimelines.Type.HOME) {
                                    DisplayStatusFragment homeFragment = (DisplayStatusFragment) mPageReferenceMap.get(tl.getPosition());
                                    if (homeFragment != null) {
                                        fetch_all_more = true;
                                        homeFragment.fetchMore(status.getId());
                                    }
                                    break;
                                }
                            }
                        }else{
                            Toasty.error(context, context.getString(R.string.toast_error), Toast.LENGTH_LONG).show();
                        }
                        return false;
                    }
                });
            } else {
                holder.fetch_more.setVisibility(View.GONE);

            }

            if (status.getMentions() != null)
                holder.status_mention_spoiler.setText(Helper.makeMentionsClick(context, status.getMentions()), TextView.BufferType.SPANNABLE);
            holder.status_mention_spoiler.setMovementMethod(LinkMovementMethod.getInstance());

            if ((isCompactMode || isConsoleMode)  && ((status.getReblog() == null && status.getReplies_count() > 1) || (status.getReblog() != null && status.getReblog().getReplies_count() > 1))) {
                Drawable img = context.getResources().getDrawable(R.drawable.ic_plus_one);
                holder.status_reply_count.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null);
            }

            if (getItemViewType(viewHolder.getAdapterPosition()) != COMPACT_STATUS && getItemViewType(viewHolder.getAdapterPosition()) != CONSOLE_STATUS) {
                if (status.getReblog() == null) {
                    if (status.getReplies_count() > 0)
                        holder.status_reply_count.setText(String.valueOf(status.getReplies_count()));
                    else
                        holder.status_reply_count.setText("");
                }
                else {
                    if (status.getReblog().getReplies_count() > 0)
                        holder.status_reply_count.setText(String.valueOf(status.getReblog().getReplies_count()));
                    else
                        holder.status_reply_count.setText("");
                }

                if (status.getReblog() == null) {
                    if (status.getFavourites_count() > 0)
                        holder.status_favorite_count.setText(String.valueOf(status.getFavourites_count()));
                    else
                        holder.status_favorite_count.setText("");
                }
                else {
                    if (status.getReblog().getFavourites_count() > 0)
                        holder.status_favorite_count.setText(String.valueOf(status.getReblog().getFavourites_count()));
                    else
                        holder.status_favorite_count.setText("");
                }

                if (status.getReblog() == null) {
                    if (status.getReblogs_count() > 0)
                        holder.status_reblog_count.setText(String.valueOf(status.getReblogs_count()));
                    else
                        holder.status_reblog_count.setText("");
                }
                else {
                    if (status.getReblog().getReblogs_count() > 0)
                        holder.status_reblog_count.setText(String.valueOf(status.getReblog().getReblogs_count()));
                    else
                        holder.status_reblog_count.setText("");
                }
            }
            if (getItemViewType(viewHolder.getAdapterPosition()) == FOCUSED_STATUS) {
                String fullDate_tmp = Helper.dateDiffFull(status.getCreated_at());
                String fullDate = "";
                if (!fullDate_tmp.equals(""))
                    fullDate = fullDate_tmp.substring(0, 1).toUpperCase() + fullDate_tmp.substring(1);
                holder.status_toot_date.setText(fullDate);
            } else {
                holder.status_toot_date.setText(Helper.dateDiff(context, status.getCreated_at()));
                Helper.absoluteDateTimeReveal(context, holder.status_toot_date, status.getCreated_at());
            }

            if (status.getReblog() != null) {
                Helper.loadGiF(context, ppurl, holder.status_account_profile_boost);
                Helper.loadGiF(context, status.getAccount().getAvatar(), holder.status_account_profile_boost_by);
                holder.status_account_profile_boost.setVisibility(View.VISIBLE);
                holder.status_account_profile_boost_by.setVisibility(View.VISIBLE);
                holder.status_account_profile.setVisibility(View.GONE);
            } else {
                Helper.loadGiF(context, ppurl, holder.status_account_profile);
                holder.status_account_profile_boost.setVisibility(View.GONE);
                holder.status_account_profile_boost_by.setVisibility(View.GONE);
                holder.status_account_profile.setVisibility(View.VISIBLE);
            }
            if (type == RetrieveFeedsAsyncTask.Type.CONVERSATION && status.getConversationProfilePicture() != null) {
                holder.status_account_profile.setVisibility(View.GONE);
                holder.conversation_pp.setVisibility(View.VISIBLE);
                if (status.getConversationProfilePicture().size() == 1) {
                    holder.conversation_pp_1.setVisibility(View.VISIBLE);
                    holder.conversation_pp_1.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    holder.conversation_pp_2_container.setVisibility(View.GONE);
                    holder.conversation_pp_3_container.setVisibility(View.GONE);
                    holder.conversation_pp_2.setVisibility(View.GONE);
                    holder.conversation_pp_3.setVisibility(View.GONE);
                    holder.conversation_pp_4.setVisibility(View.GONE);
                    Glide.with(context)
                            .load(status.getConversationProfilePicture().get(0))
                            .apply(new RequestOptions().transforms(new FitCenter(), new RoundedCorners(10)))
                            .into(holder.conversation_pp_1);
                } else if (status.getConversationProfilePicture().size() == 2) {
                    holder.conversation_pp_2_container.setVisibility(View.VISIBLE);
                    holder.conversation_pp_3_container.setVisibility(View.GONE);
                    holder.conversation_pp_1.setVisibility(View.VISIBLE);
                    holder.conversation_pp_2.setVisibility(View.VISIBLE);
                    holder.conversation_pp_3.setVisibility(View.GONE);
                    holder.conversation_pp_4.setVisibility(View.GONE);
                    Helper.loadGiF(context, status.getConversationProfilePicture().get(0), holder.conversation_pp_1);
                    Helper.loadGiF(context, status.getConversationProfilePicture().get(1), holder.conversation_pp_2);
                } else if (status.getConversationProfilePicture().size() == 3) {
                    holder.conversation_pp_4.setVisibility(View.GONE);
                    holder.conversation_pp_1.setVisibility(View.VISIBLE);
                    holder.conversation_pp_2.setVisibility(View.VISIBLE);
                    holder.conversation_pp_3.setVisibility(View.VISIBLE);
                    holder.conversation_pp_4.setVisibility(View.GONE);
                    holder.conversation_pp_2_container.setVisibility(View.VISIBLE);
                    holder.conversation_pp_3_container.setVisibility(View.VISIBLE);
                    Helper.loadGiF(context, status.getConversationProfilePicture().get(0), holder.conversation_pp_1);
                    Helper.loadGiF(context, status.getConversationProfilePicture().get(1), holder.conversation_pp_2);
                    Helper.loadGiF(context, status.getConversationProfilePicture().get(2), holder.conversation_pp_3);
                } else if (status.getConversationProfilePicture().size() == 4) {
                    holder.conversation_pp_1.setVisibility(View.VISIBLE);
                    holder.conversation_pp_2.setVisibility(View.VISIBLE);
                    holder.conversation_pp_3.setVisibility(View.VISIBLE);
                    holder.conversation_pp_4.setVisibility(View.VISIBLE);
                    holder.conversation_pp_2_container.setVisibility(View.VISIBLE);
                    holder.conversation_pp_3_container.setVisibility(View.VISIBLE);
                    Helper.loadGiF(context, status.getConversationProfilePicture().get(0), holder.conversation_pp_1);
                    Helper.loadGiF(context, status.getConversationProfilePicture().get(1), holder.conversation_pp_2);
                    Helper.loadGiF(context, status.getConversationProfilePicture().get(2), holder.conversation_pp_3);
                    Helper.loadGiF(context, status.getConversationProfilePicture().get(3), holder.conversation_pp_4);
                }
            }


            /*if (expand_cw)
                holder.status_spoiler_button.setVisibility(View.GONE);*/
            String contentCheck = "";
            String content = status.getReblog() == null?status.getContent():status.getReblog().getContent();
            if( content != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    contentCheck = Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY).toString();
                else
                    contentCheck = Html.fromHtml(content).toString();
            }
            if (status.getReblog() == null) {
                if (status.getSpoiler_text() != null && status.getSpoiler_text().trim().length() > 0) {
                    holder.status_spoiler_container.setVisibility(View.VISIBLE);
                    if (!status.isSpoilerShown() && (!expand_cw || status.isAutoHiddenCW())) {
                        holder.status_content_container.setVisibility(View.GONE);
                        if (status.getMentions().size() > 0)
                            holder.status_spoiler_mention_container.setVisibility(View.VISIBLE);
                        else
                            holder.status_spoiler_mention_container.setVisibility(View.GONE);
                        holder.status_spoiler_button.setText(context.getString(R.string.load_attachment_spoiler));
                    } else {
                        if( !status.isAutoHiddenCW()) {
                            holder.status_content_container.setVisibility(View.VISIBLE);
                            holder.status_spoiler_mention_container.setVisibility(View.GONE);
                            holder.status_spoiler_button.setText(context.getString(R.string.load_attachment_spoiler_less));
                        }else{
                            holder.status_content_container.setVisibility(View.GONE);
                            if (status.getMentions().size() > 0)
                                holder.status_spoiler_mention_container.setVisibility(View.VISIBLE);
                            else
                                holder.status_spoiler_mention_container.setVisibility(View.GONE);
                            holder.status_spoiler_button.setText(context.getString(R.string.load_attachment_spoiler));
                        }
                    }
                } else {
                    holder.status_spoiler_container.setVisibility(View.GONE);
                    holder.status_spoiler_mention_container.setVisibility(View.GONE);
                    holder.status_content_container.setVisibility(View.VISIBLE);
                }
            } else {
                if (status.getReblog().getSpoiler_text() != null && status.getReblog().getSpoiler_text().trim().length() > 0) {
                    holder.status_spoiler_container.setVisibility(View.VISIBLE);
                    if (!status.isSpoilerShown() && (!expand_cw || status.isAutoHiddenCW())) {
                        holder.status_content_container.setVisibility(View.GONE);
                        if (status.getMentions().size() > 0)
                            holder.status_spoiler_mention_container.setVisibility(View.VISIBLE);
                        else
                            holder.status_spoiler_mention_container.setVisibility(View.GONE);
                        holder.status_spoiler_button.setText(context.getString(R.string.load_attachment_spoiler));
                    } else {
                        if( !status.isAutoHiddenCW()) {
                            holder.status_content_container.setVisibility(View.VISIBLE);
                            holder.status_spoiler_mention_container.setVisibility(View.GONE);
                            holder.status_spoiler_button.setText(context.getString(R.string.load_attachment_spoiler_less));
                        }else {
                            holder.status_content_container.setVisibility(View.GONE);
                            if (status.getMentions().size() > 0)
                                holder.status_spoiler_mention_container.setVisibility(View.VISIBLE);
                            else
                                holder.status_spoiler_mention_container.setVisibility(View.GONE);
                            holder.status_spoiler_button.setText(context.getString(R.string.load_attachment_spoiler));
                        }
                    }
                } else {
                    holder.status_spoiler_container.setVisibility(View.GONE);
                    holder.status_spoiler_mention_container.setVisibility(View.GONE);
                    holder.status_content_container.setVisibility(View.VISIBLE);
                }
            }
            boolean blur_sensitive = sharedpreferences.getBoolean(Helper.SET_BLUR_SENSITIVE, true);

            if (status.getReblog() == null) {
                if (status.getMedia_attachments().size() < 1) {
                    holder.status_horizontal_document_container.setVisibility(View.GONE);
                    holder.status_document_container.setVisibility(View.GONE);
                    holder.status_show_more.setVisibility(View.GONE);
                } else {
                    //If medias are loaded without any conditions or if device is on wifi
                    if(behaviorWithAttachments != Helper.ATTACHMENT_ASK ) {
                        if (expand_media || !status.isSensitive() && (behaviorWithAttachments == Helper.ATTACHMENT_ALWAYS || (behaviorWithAttachments == Helper.ATTACHMENT_WIFI && isOnWifi))) {
                            loadAttachments(status, holder, false);
                            holder.status_show_more.setVisibility(View.GONE);
                            status.setAttachmentShown(true);
                        } else {
                            //Text depending if toots is sensitive or not
                            String textShowMore = (status.isSensitive()) ? context.getString(R.string.load_sensitive_attachment) : context.getString(R.string.set_attachment_action);
                            holder.show_more_content.setText(textShowMore);
                            if (!status.isAttachmentShown()) {
                                holder.status_show_more.setVisibility(View.VISIBLE);
                                if (fullAttachement)
                                    holder.status_horizontal_document_container.setVisibility(View.GONE);
                                else
                                    holder.status_document_container.setVisibility(View.GONE);
                                if(blur_sensitive && (behaviorWithAttachments == Helper.ATTACHMENT_ALWAYS || (behaviorWithAttachments == Helper.ATTACHMENT_WIFI && isOnWifi))){
                                    loadAttachments(status, holder, true);
                                }
                            } else {
                                loadAttachments(status, holder, false);
                            }
                        }
                    }else {
                        //Text depending if toots is sensitive or not
                        String textShowMore = (status.isSensitive()) ? context.getString(R.string.load_sensitive_attachment) : context.getString(R.string.set_attachment_action);
                        holder.show_more_content.setText(textShowMore);
                        if (!status.isAttachmentShown()) {
                            holder.status_show_more.setVisibility(View.VISIBLE);
                            if (fullAttachement)
                                holder.status_horizontal_document_container.setVisibility(View.GONE);
                            else
                                holder.status_document_container.setVisibility(View.GONE);
                        } else {
                            loadAttachments(status, holder, false);
                        }
                    }
                }
            } else { //Attachments for reblogs

                if (status.getReblog().getMedia_attachments().size() < 1) {
                    holder.status_horizontal_document_container.setVisibility(View.GONE);
                    holder.status_document_container.setVisibility(View.GONE);
                    holder.status_show_more.setVisibility(View.GONE);
                } else {
                    if(behaviorWithAttachments != Helper.ATTACHMENT_ASK ) {
                        //If medias are loaded without any conditions or if device is on wifi
                        if (expand_media || !status.getReblog().isSensitive() && (behaviorWithAttachments == Helper.ATTACHMENT_ALWAYS || (behaviorWithAttachments == Helper.ATTACHMENT_WIFI && isOnWifi))) {
                            loadAttachments(status, holder, false);
                            holder.status_show_more.setVisibility(View.GONE);
                            status.setAttachmentShown(true);
                        } else {
                            //Text depending if toots is sensitive or not
                            String textShowMore = (status.getReblog().isSensitive()) ? context.getString(R.string.load_sensitive_attachment) : context.getString(R.string.set_attachment_action);
                            holder.show_more_content.setText(textShowMore);
                            if (!status.isAttachmentShown()) {
                                holder.status_show_more.setVisibility(View.VISIBLE);
                                if (fullAttachement)
                                    holder.status_horizontal_document_container.setVisibility(View.GONE);
                                else
                                    holder.status_document_container.setVisibility(View.GONE);
                                if(blur_sensitive && (behaviorWithAttachments == Helper.ATTACHMENT_ALWAYS || (behaviorWithAttachments == Helper.ATTACHMENT_WIFI && isOnWifi))){
                                    loadAttachments(status, holder, true);
                                }
                            } else {
                                loadAttachments(status, holder, false);
                            }
                        }
                    }else{
                        //Text depending if toots is sensitive or not
                        String textShowMore = (status.getReblog().isSensitive()) ? context.getString(R.string.load_sensitive_attachment) : context.getString(R.string.set_attachment_action);
                        holder.show_more_content.setText(textShowMore);
                        if (!status.isAttachmentShown()) {
                            holder.status_show_more.setVisibility(View.VISIBLE);
                            if (fullAttachement)
                                holder.status_horizontal_document_container.setVisibility(View.GONE);
                            else
                                holder.status_document_container.setVisibility(View.GONE);
                        } else {
                            loadAttachments(status, holder, false);
                        }
                    }
                }
            }
            holder.status_show_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
            if (theme == Helper.THEME_BLACK) {
                Helper.changeDrawableColor(context, R.drawable.ic_photo, R.color.dark_text);
                Helper.changeDrawableColor(context, R.drawable.ic_more_toot_content, R.color.dark_text);
            } else {
                Helper.changeDrawableColor(context, R.drawable.ic_photo, R.color.mastodonC4);
                Helper.changeDrawableColor(context, R.drawable.ic_more_toot_content, R.color.mastodonC4);
            }
            if (!fullAttachement)
                holder.hide_preview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        status.setAttachmentShown(!status.isAttachmentShown());
                        if (status.getReblog() != null)
                            status.getReblog().setSensitive(true);
                        else
                            status.setSensitive(true);
                        notifyStatusChanged(status);
                    }
                });
            else
                holder.hide_preview_h.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        status.setAttachmentShown(!status.isAttachmentShown());
                        if (status.getReblog() != null)
                            status.getReblog().setSensitive(true);
                        else
                            status.setSensitive(true);
                        notifyStatusChanged(status);
                    }
                });

            //Toot was translated and user asked to see it

            if (status.isTranslationShown() && status.getContentSpanTranslated() != null) {
                holder.status_content_translated.setText(status.getContentSpanTranslated(), TextView.BufferType.SPANNABLE);
                holder.status_content_translated_container.setVisibility(View.VISIBLE);
            } else { //Toot is not translated
                holder.status_content_translated_container.setVisibility(View.GONE);
            }

            switch (status.getVisibility()) {
                case "direct":
                    holder.status_reblog_count.setVisibility(View.GONE);
                    holder.spark_button_reblog.setVisibility(View.GONE);
                    break;
                case "private":
                    boolean isOwner = status.getAccount().getId().equals(userId);
                    if (isOwner) {
                        holder.status_reblog_count.setVisibility(View.VISIBLE);
                        holder.spark_button_reblog.setVisibility(View.VISIBLE);
                    } else {
                        holder.status_reblog_count.setVisibility(View.GONE);
                        holder.spark_button_reblog.setVisibility(View.GONE);
                    }
                    break;
                case "public":
                case "unlisted":
                    holder.status_reblog_count.setVisibility(View.VISIBLE);
                    holder.spark_button_reblog.setVisibility(View.VISIBLE);
                    break;
                default:
                    holder.status_reblog_count.setVisibility(View.VISIBLE);
                    holder.spark_button_reblog.setVisibility(View.VISIBLE);
            }

            switch (status.getVisibility()) {
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


            if (!status.isFavAnimated()) {
                if (status.isFavourited() || (status.getReblog() != null && status.getReblog().isFavourited())) {
                    holder.spark_button_fav.setChecked(true);
                } else {
                    holder.spark_button_fav.setChecked(false);
                }
            } else {
                status.setFavAnimated(false);
                holder.spark_button_fav.setChecked(true);
                holder.spark_button_fav.setAnimationSpeed(1.0f);
                holder.spark_button_fav.playAnimation();
            }
            if (!status.isBoostAnimated()) {
                if (status.isReblogged() || (status.getReblog() != null && status.getReblog().isReblogged())) {
                    holder.spark_button_reblog.setChecked(true);
                } else {
                    holder.spark_button_reblog.setChecked(false);
                }
            } else {
                status.setBoostAnimated(false);
                holder.spark_button_reblog.setChecked(true);
                holder.spark_button_reblog.setAnimationSpeed(1.0f);
                holder.spark_button_reblog.playAnimation();
            }

            if (theme == Helper.THEME_DARK)
                Helper.changeDrawableColor(context, R.drawable.ic_reply, R.color.action_dark);
            else if (theme == Helper.THEME_BLACK)
                Helper.changeDrawableColor(context, R.drawable.ic_reply, R.color.action_black);
            else
                Helper.changeDrawableColor(context, R.drawable.ic_reply, R.color.action_light);

            boolean isOwner = status.getAccount().getId().equals(userId);

            // Pinning toots is only available on Mastodon 1._6_.0 instances.
            if (isOwner && Helper.canPin && (status.getVisibility().equals("public") || status.getVisibility().equals("unlisted")) && status.getReblog() == null) {
                Drawable imgPin;
                if (status.isPinned() || (status.getReblog() != null && status.getReblog().isPinned())) {
                    Helper.changeDrawableColor(context, R.drawable.ic_pin_drop_p, R.color.marked_icon);
                    imgPin = ContextCompat.getDrawable(context, R.drawable.ic_pin_drop_p);
                } else {
                    if (theme == Helper.THEME_DARK)
                        Helper.changeDrawableColor(context, R.drawable.ic_pin_drop, R.color.action_dark);
                    else if (theme == Helper.THEME_BLACK)
                        Helper.changeDrawableColor(context, R.drawable.ic_pin_drop, R.color.action_black);
                    else
                        Helper.changeDrawableColor(context, R.drawable.ic_pin_drop, R.color.action_light);
                    imgPin = ContextCompat.getDrawable(context, R.drawable.ic_pin_drop);
                }
                assert imgPin != null;
                imgPin.setBounds(0, 0, (int) (20 * iconSizePercent / 100 * scale + 0.5f), (int) (20 * iconSizePercent / 100 * scale + 0.5f));
                holder.status_pin.setImageDrawable(imgPin);

                holder.status_pin.setVisibility(View.VISIBLE);
            } else {
                holder.status_pin.setVisibility(View.GONE);
            }

            if( (isAdmin || isModerator) && !isCompactMode && !isConsoleMode && getItemViewType(viewHolder.getAdapterPosition()) != FOCUSED_STATUS){
                holder.status_remove.setVisibility(View.VISIBLE);
            }else {
                holder.status_remove.setVisibility(View.GONE);
            }

            if( status.getReblog() == null){
                if (status.getWebviewURL() != null) {
                    String  url = status.getWebviewURL().replaceAll("&amp;","&");
                    holder.status_cardview_webview.loadUrl(url);
                    holder.status_cardview_webview.setVisibility(View.VISIBLE);
                    holder.status_cardview_video.setVisibility(View.VISIBLE);
                    holder.webview_preview.setVisibility(View.GONE);
                } else {
                    holder.status_cardview_webview.setVisibility(View.GONE);
                    holder.status_cardview_video.setVisibility(View.GONE);
                    holder.webview_preview.setVisibility(View.VISIBLE);
                }
            }else{
                if (status.getReblog().getWebviewURL() != null) {
                    String  url = status.getReblog().getWebviewURL().replaceAll("&amp;","&");
                    holder.status_cardview_webview.loadUrl(url);
                    holder.status_cardview_webview.setVisibility(View.VISIBLE);
                    holder.status_cardview_video.setVisibility(View.VISIBLE);
                    holder.webview_preview.setVisibility(View.GONE);
                } else {
                    holder.status_cardview_webview.setVisibility(View.GONE);
                    holder.status_cardview_video.setVisibility(View.GONE);
                    holder.webview_preview.setVisibility(View.VISIBLE);
                }
            }



            if ((type == RetrieveFeedsAsyncTask.Type.CONTEXT && viewHolder.getAdapterPosition() == conversationPosition) || display_card || display_video_preview) {

                if (type == RetrieveFeedsAsyncTask.Type.CONTEXT & viewHolder.getAdapterPosition() == conversationPosition)
                    holder.status_cardview_content.setVisibility(View.VISIBLE);
                else
                    holder.status_cardview_content.setVisibility(View.GONE);

                if (viewHolder.getAdapterPosition() == conversationPosition || display_card || display_video_preview) {
                    Card card = status.getReblog() != null ? status.getReblog().getCard() : status.getCard();
                    if (card != null) {
                        holder.status_cardview_content.setText(card.getDescription());
                        holder.status_cardview_title.setText(card.getTitle());
                        holder.status_cardview_url.setText(card.getUrl());
                        if (card.getImage() != null && card.getImage().length() > 10) {
                            holder.status_cardview_image.setVisibility(View.VISIBLE);
                            if (!((Activity) context).isFinishing())
                                Glide.with(holder.status_cardview_image.getContext())
                                        .load(card.getImage())
                                        .apply(new RequestOptions().transforms(new CenterCrop(), new RoundedCorners((int) Helper.convertDpToPixel(3, context))))
                                        .into(holder.status_cardview_image);
                        } else
                            holder.status_cardview_image.setVisibility(View.GONE);
                        if (!card.getType().toLowerCase().equals("video") && (display_card || viewHolder.getAdapterPosition() == conversationPosition)) {
                            holder.status_cardview.setVisibility(View.VISIBLE);
                            holder.status_cardview_video.setVisibility(View.GONE);
                            holder.status_cardview.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Helper.openBrowser(context, card.getUrl());
                                }
                            });
                        } else if (card.getType().toLowerCase().equals("video") && (display_video_preview || viewHolder.getAdapterPosition() == conversationPosition)) {
                            Glide.with(holder.status_cardview_image.getContext())
                                    .load(card.getImage())
                                    .apply(new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(10)))
                                    .into(holder.webview_preview_card);
                            holder.status_cardview.setVisibility(View.GONE);
                            holder.status_cardview_video.setVisibility(View.VISIBLE);
                            holder.status_cardview_webview.getSettings().setJavaScriptEnabled(true);
                            String html = card.getHtml();
                            String src = card.getUrl();
                            if (html != null) {
                                Matcher matcher = Pattern.compile("src=\"([^\"]+)\"").matcher(html);
                                if (matcher.find())
                                    src = matcher.group(1);
                            }
                            final String finalSrc = src;
                            holder.status_cardview_webview.setWebViewClient(new WebViewClient() {
                                @Override
                                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                                    holder.status_cardview_video.setVisibility(View.GONE);
                                }
                            });
                            holder.webview_preview.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    status.setWebviewURL(finalSrc);
                                    notifyStatusChanged(status);
                                }
                            });
                        }
                    } else {
                        holder.status_cardview.setVisibility(View.GONE);
                        holder.status_cardview_video.setVisibility(View.GONE);
                    }

                } else {
                    holder.status_cardview.setVisibility(View.GONE);
                    holder.status_cardview_video.setVisibility(View.GONE);
                }
            } else {
                holder.status_cardview.setVisibility(View.GONE);
                holder.status_cardview_video.setVisibility(View.GONE);
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
                    if (!status.isFavourited() && confirmFav)
                        status.setFavAnimated(true);
                    if (!status.isFavourited() && !confirmFav) {
                        status.setFavAnimated(true);
                        notifyStatusChanged(status);
                    }
                    CrossActions.doCrossAction(context, type, status, null, (status.isFavourited() || (status.getReblog() != null && status.getReblog().isFavourited())) ? API.StatusAction.UNFAVOURITE : API.StatusAction.FAVOURITE, statusListAdapter, StatusListAdapter.this, true);
                }
            });
            holder.spark_button_fav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!status.isFavourited() && confirmFav)
                        status.setFavAnimated(true);
                    if (!status.isFavourited() && !confirmFav) {
                        status.setFavAnimated(true);
                        notifyStatusChanged(status);
                    }
                    CrossActions.doCrossAction(context, type, status, null, (status.isFavourited() || (status.getReblog() != null && status.getReblog().isFavourited())) ? API.StatusAction.UNFAVOURITE : API.StatusAction.FAVOURITE, statusListAdapter, StatusListAdapter.this, true);
                }
            });

            holder.status_reblog_count.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!status.isReblogged() && confirmBoost)
                        status.setBoostAnimated(true);
                    if (!status.isReblogged() && !confirmBoost) {
                        status.setBoostAnimated(true);
                        notifyStatusChanged(status);
                    }
                    CrossActions.doCrossAction(context, type, status, null, (status.isReblogged() || (status.getReblog() != null && status.getReblog().isReblogged())) ? API.StatusAction.UNREBLOG : API.StatusAction.REBLOG, statusListAdapter, StatusListAdapter.this, true);
                }
            });
            holder.spark_button_reblog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!status.isReblogged() && confirmBoost)
                        status.setBoostAnimated(true);
                    if (!status.isReblogged() && !confirmBoost) {
                        status.setBoostAnimated(true);
                        notifyStatusChanged(status);
                    }
                    CrossActions.doCrossAction(context, type, status, null, (status.isReblogged() || (status.getReblog() != null && status.getReblog().isReblogged())) ? API.StatusAction.UNREBLOG : API.StatusAction.REBLOG, statusListAdapter, StatusListAdapter.this, true);
                }
            });
            holder.status_pin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CrossActions.doCrossAction(context, type, status, null, (status.isPinned() || (status.getReblog() != null && status.getReblog().isPinned())) ? API.StatusAction.UNPIN : API.StatusAction.PIN, statusListAdapter, StatusListAdapter.this, true);
                }
            });
            int style;
            if (theme == Helper.THEME_DARK) {
                style = R.style.DialogDark;
            } else if (theme == Helper.THEME_BLACK) {
                style = R.style.DialogBlack;
            } else {
                style = R.style.Dialog;
            }

            holder.status_remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String[] stringArrayConf = context.getResources().getStringArray(R.array.more_action_owner_confirm);
                    AlertDialog.Builder builderInner = new AlertDialog.Builder(context, style);
                    builderInner.setTitle(stringArrayConf[0]);
                    API.StatusAction doAction = API.StatusAction.UNSTATUS;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        builderInner.setMessage(Html.fromHtml(status.getContent(), Html.FROM_HTML_MODE_LEGACY));
                    else
                        //noinspection deprecation
                        builderInner.setMessage(Html.fromHtml(status.getContent()));


                    //Text for report
                    EditText input = null;
                    if (doAction == API.StatusAction.REPORT) {
                        input = new EditText(context);
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        input.setLayoutParams(lp);
                        builderInner.setView(input);
                    }
                    builderInner.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    final EditText finalInput = input;
                    builderInner.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String targetedId = status.getId();
                            new PostActionAsyncTask(context, doAction, targetedId, StatusListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            if (redraft) {
                                if (status.getIn_reply_to_id() != null && !status.getIn_reply_to_id().trim().equals("null")) {
                                    toot = new Status();
                                    toot.setIn_reply_to_id(status.getIn_reply_to_id());
                                    toot.setSensitive(status.isSensitive());
                                    toot.setMedia_attachments(status.getMedia_attachments());
                                    if (status.getSpoiler_text() != null && status.getSpoiler_text().length() > 0)
                                        toot.setSpoiler_text(status.getSpoiler_text().trim());
                                    toot.setContent(status.getContent());
                                    toot.setVisibility(status.getVisibility());
                                    new RetrieveFeedsAsyncTask(context, RetrieveFeedsAsyncTask.Type.ONESTATUS, status.getIn_reply_to_id(), null, false, false, StatusListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                } else {
                                    toot = new Status();
                                    toot.setSensitive(status.isSensitive());
                                    toot.setMedia_attachments(status.getMedia_attachments());
                                    if (status.getSpoiler_text() != null && status.getSpoiler_text().length() > 0)
                                        toot.setSpoiler_text(status.getSpoiler_text().trim());
                                    toot.setVisibility(status.getVisibility());
                                    toot.setContent(status.getContent());
                                    final SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                                    long id = new StatusStoredDAO(context, db).insertStatus(toot, null);
                                    Intent intentToot = new Intent(context, TootActivity.class);
                                    Bundle b = new Bundle();
                                    b.putLong("restored", id);
                                    b.putBoolean("removed", true);
                                    intentToot.putExtras(b);
                                    context.startActivity(intentToot);
                                }
                            }
                            dialog.dismiss();
                        }
                    });
                    builderInner.show();
                }
            });
            if (!status.getVisibility().equals("direct"))
                holder.spark_button_fav.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        CrossActions.doCrossAction(context, type, status, null, API.StatusAction.FAVOURITE, statusListAdapter, StatusListAdapter.this, false);
                        return true;
                    }
                });
            if (!status.getVisibility().equals("direct"))
                holder.spark_button_reblog.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        CrossActions.doCrossAction(context, type, status, null, API.StatusAction.REBLOG, statusListAdapter, StatusListAdapter.this, false);
                        return true;
                    }
                });
            if (!status.getVisibility().equals("direct"))
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
                    if( expand_cw && !status.isSpoilerShown() ){
                        status.setAutoHiddenCW(true);
                    }else{
                        status.setAutoHiddenCW(false);
                    }
                    status.setSpoilerShown(!status.isSpoilerShown());
                    notifyStatusChanged(status);
                }
            });


            if (type == RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE)
                holder.status_more.setVisibility(View.GONE);

            final View attached = holder.status_more;
            holder.status_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popup = new PopupMenu(context, attached);
                    final boolean isOwner = status.getAccount().getId().equals(userId);
                    popup.getMenuInflater()
                            .inflate(R.menu.option_toot, popup.getMenu());
                    if (status.getVisibility().equals("private") || status.getVisibility().equals("direct")) {
                        popup.getMenu().findItem(R.id.action_mention).setVisible(false);
                    }
                    if (status.isBookmarked())
                        popup.getMenu().findItem(R.id.action_bookmark).setTitle(R.string.bookmark_remove);
                    else
                        popup.getMenu().findItem(R.id.action_bookmark).setTitle(R.string.bookmark_add);
                    if (status.isMuted())
                        popup.getMenu().findItem(R.id.action_mute_conversation).setTitle(R.string.unmute_conversation);
                    else
                        popup.getMenu().findItem(R.id.action_mute_conversation).setTitle(R.string.mute_conversation);


                    final String[] stringArrayConf;
                    if (status.getVisibility().equals("direct") || (status.getVisibility().equals("private") && !isOwner))
                        popup.getMenu().findItem(R.id.action_schedule_boost).setVisible(false);
                    if (isOwner) {
                        popup.getMenu().findItem(R.id.action_block).setVisible(false);
                        popup.getMenu().findItem(R.id.action_mute).setVisible(false);
                        popup.getMenu().findItem(R.id.action_report).setVisible(false);
                        popup.getMenu().findItem(R.id.action_timed_mute).setVisible(false);
                        popup.getMenu().findItem(R.id.action_block_domain).setVisible(false);
                        stringArrayConf = context.getResources().getStringArray(R.array.more_action_owner_confirm);
                    } else {
                        popup.getMenu().findItem(R.id.action_redraft).setVisible(false);
                        //popup.getMenu().findItem(R.id.action_mute_conversation).setVisible(false);
                        if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA && (isAdmin || isModerator)) {
                            popup.getMenu().findItem(R.id.action_remove).setVisible(true);
                        }else {
                            popup.getMenu().findItem(R.id.action_remove).setVisible(false);
                        }
                        //Same instance
                        if (status.getAccount().getAcct().split("@").length < 2)
                            popup.getMenu().findItem(R.id.action_block_domain).setVisible(false);
                        stringArrayConf = context.getResources().getStringArray(R.array.more_action_confirm);
                    }
                    if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.GNU ||  MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA){
                        popup.getMenu().findItem(R.id.action_info).setVisible(false);
                        popup.getMenu().findItem(R.id.action_report).setVisible(false);
                        popup.getMenu().findItem(R.id.action_block_domain).setVisible(false);
                        popup.getMenu().findItem(R.id.action_mute_conversation).setVisible(false);
                    }
                    if (holder.getItemViewType() == DISPLAYED_STATUS && fedilab_features_button) {
                        popup.getMenu().findItem(R.id.action_translate).setVisible(false);
                        popup.getMenu().findItem(R.id.action_bookmark).setVisible(false);
                        popup.getMenu().findItem(R.id.action_timed_mute).setVisible(false);
                        popup.getMenu().findItem(R.id.action_schedule_boost).setVisible(false);
                        popup.getMenu().findItem(R.id.action_mention).setVisible(false);
                    }
                    if( MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.MASTODON){
                        popup.getMenu().findItem(R.id.action_admin).setVisible(false);
                    }else{
                        boolean display_admin_statuses = sharedpreferences.getBoolean(Helper.SET_DISPLAY_ADMIN_STATUSES + userId + Helper.getLiveInstance(context), false);
                        if( !display_admin_statuses){
                            popup.getMenu().findItem(R.id.action_admin).setVisible(false);
                        }
                    }

                    boolean custom_sharing = sharedpreferences.getBoolean(Helper.SET_CUSTOM_SHARING, false);
                    if( custom_sharing && status.getVisibility().equals("public"))
                        popup.getMenu().findItem(R.id.action_custom_sharing).setVisible(true);
                    MenuItem itemBookmark = popup.getMenu().findItem(R.id.action_bookmark);
                    if (itemBookmark.getActionView() != null)
                        itemBookmark.getActionView().setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                CrossActions.doCrossBookmark(context, status, statusListAdapter);
                                return true;
                            }
                        });
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            AlertDialog.Builder builderInner;
                            final API.StatusAction doAction;
                            switch (item.getItemId()) {
                                case R.id.action_redraft:
                                    builderInner = new AlertDialog.Builder(context, style);
                                    builderInner.setTitle(stringArrayConf[1]);
                                    redraft = true;
                                    doAction = API.StatusAction.UNSTATUS;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                        builderInner.setMessage(Html.fromHtml(status.getContent(), Html.FROM_HTML_MODE_LEGACY));
                                    else
                                        //noinspection deprecation
                                        builderInner.setMessage(Html.fromHtml(status.getContent()));
                                    break;
                                case R.id.action_schedule_boost:
                                    scheduleBoost(status);
                                    return true;
                                case R.id.action_admin:
                                    String account_id = status.getReblog() != null ? status.getReblog().getAccount().getId() : status.getAccount().getId();
                                    Intent intent = new Intent(context, AccountReportActivity.class);
                                    Bundle b = new Bundle();
                                    b.putString("account_id", account_id);
                                    intent.putExtras(b);
                                    context.startActivity(intent);
                                    return true;
                                case R.id.action_info:
                                    tootInformation(status);
                                    return true;
                                case R.id.action_open_browser:
                                    Helper.openBrowser(context, status.getReblog() != null ? status.getReblog().getUrl() : status.getUrl());
                                    return true;
                                case R.id.action_remove:
                                    builderInner = new AlertDialog.Builder(context, style);
                                    builderInner.setTitle(stringArrayConf[0]);
                                    doAction = API.StatusAction.UNSTATUS;
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                        builderInner.setMessage(Html.fromHtml(status.getContent(), Html.FROM_HTML_MODE_LEGACY));
                                    else
                                        //noinspection deprecation
                                        builderInner.setMessage(Html.fromHtml(status.getContent()));
                                    break;
                                case R.id.action_block_domain:
                                    builderInner = new AlertDialog.Builder(context, style);
                                    builderInner.setTitle(stringArrayConf[3]);
                                    doAction = API.StatusAction.BLOCK_DOMAIN;
                                    String domain = status.getAccount().getAcct().split("@")[1];
                                    builderInner.setMessage(context.getString(R.string.block_domain_confirm_message, domain));
                                    break;
                                case R.id.action_mute:
                                    builderInner = new AlertDialog.Builder(context, style);
                                    builderInner.setTitle(stringArrayConf[0]);
                                    builderInner.setMessage(status.getAccount().getAcct());
                                    doAction = API.StatusAction.MUTE;
                                    break;
                                case R.id.action_mute_conversation:
                                    if( status.isMuted())
                                        doAction = API.StatusAction.UNMUTE_CONVERSATION;
                                    else
                                        doAction = API.StatusAction.MUTE_CONVERSATION;

                                    new PostActionAsyncTask(context, doAction, status.getId(), StatusListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                    return true;
                                case R.id.action_bookmark:
                                    if (type != RetrieveFeedsAsyncTask.Type.CACHE_BOOKMARKS) {
                                        status.setBookmarked(!status.isBookmarked());
                                        final SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                                        try {
                                            if (status.isBookmarked()) {
                                                new StatusCacheDAO(context, db).insertStatus(StatusCacheDAO.BOOKMARK_CACHE, status);
                                                Toasty.success(context, context.getString(R.string.status_bookmarked), Toast.LENGTH_LONG).show();
                                            } else {
                                                new StatusCacheDAO(context, db).remove(StatusCacheDAO.BOOKMARK_CACHE, status);
                                                Toasty.success(context, context.getString(R.string.status_unbookmarked), Toast.LENGTH_LONG).show();
                                            }
                                            notifyStatusChanged(status);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            Toasty.error(context, context.getString(R.string.toast_error), Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        int position = 0;
                                        for (Status statustmp : statuses) {
                                            if (statustmp.getId().equals(status.getId())) {
                                                statuses.remove(status);
                                                statusListAdapter.notifyItemRemoved(position);
                                                final SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                                                new StatusCacheDAO(context, db).remove(StatusCacheDAO.BOOKMARK_CACHE, statustmp);
                                                Toasty.success(context, context.getString(R.string.status_unbookmarked), Toast.LENGTH_LONG).show();
                                                break;
                                            }
                                            position++;
                                        }
                                    }
                                    return true;
                                case R.id.action_timed_mute:
                                    timedMuteAction(status);
                                    return true;
                                case R.id.action_block:
                                    builderInner = new AlertDialog.Builder(context, style);
                                    builderInner.setTitle(stringArrayConf[1]);
                                    doAction = API.StatusAction.BLOCK;
                                    break;
                                case R.id.action_translate:
                                    if (translator == Helper.TRANS_NONE)
                                        Toasty.info(context, R.string.toast_error_translations_disabled, Toast.LENGTH_SHORT).show();
                                    else
                                        translateToot(status);
                                    return true;
                                case R.id.action_report:
                                    builderInner = new AlertDialog.Builder(context, style);
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
                                    if (clipboard != null) {
                                        clipboard.setPrimaryClip(clip);
                                        Toasty.info(context, context.getString(R.string.clipboard), Toast.LENGTH_LONG).show();
                                    }
                                    return true;
                                case R.id.action_copy_link:
                                    clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

                                    clip = ClipData.newPlainText(Helper.CLIP_BOARD, status.getReblog()!=null?status.getReblog().getUrl():status.getUrl());
                                    if (clipboard != null) {
                                        clipboard.setPrimaryClip(clip);
                                        Toasty.info(context, context.getString(R.string.clipboard_url), Toast.LENGTH_LONG).show();
                                    }
                                    return true;
                                case R.id.action_share:
                                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                                    sendIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.shared_via));
                                    String url;

                                    if (status.getReblog() != null) {
                                        if (status.getReblog().getUri().startsWith("http"))
                                            url = status.getReblog().getUri();
                                        else
                                            url = status.getReblog().getUrl();
                                    } else {
                                        if (status.getUri().startsWith("http"))
                                            url = status.getUri();
                                        else
                                            url = status.getUrl();
                                    }
                                    String extra_text;

                                    if (share_details) {
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
                                    } else {
                                        extra_text = url;
                                    }
                                    sendIntent.putExtra(Intent.EXTRA_TEXT, extra_text);
                                    sendIntent.setType("text/plain");
                                    context.startActivity(Intent.createChooser(sendIntent, context.getString(R.string.share_with)));
                                    return true;
                                case R.id.action_custom_sharing:
                                    Intent intentCustomSharing = new Intent(context, CustomSharingActivity.class);
                                    Bundle bCustomSharing = new Bundle();
                                    if (status.getReblog() != null) {
                                        bCustomSharing.putParcelable("status", status.getReblog());
                                    } else {
                                        bCustomSharing.putParcelable("status", status);
                                    }
                                    intentCustomSharing.putExtras(bCustomSharing);
                                    context.startActivity(intentCustomSharing);
                                    return true;
                                case R.id.action_mention:
                                    mention(status);
                                    return true;
                                default:
                                    return true;
                            }

                            //Text for report
                            EditText input = null;
                            if (doAction == API.StatusAction.REPORT) {
                                input = new EditText(context);
                                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT);
                                input.setLayoutParams(lp);
                                builderInner.setView(input);
                            }
                            builderInner.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            final EditText finalInput = input;
                            builderInner.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (doAction == API.StatusAction.UNSTATUS) {
                                        String targetedId = status.getId();
                                        new PostActionAsyncTask(context, doAction, targetedId, StatusListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                        if (redraft) {
                                            if (status.getIn_reply_to_id() != null && !status.getIn_reply_to_id().trim().equals("null")) {
                                                toot = new Status();
                                                toot.setIn_reply_to_id(status.getIn_reply_to_id());
                                                toot.setSensitive(status.isSensitive());
                                                toot.setMedia_attachments(status.getMedia_attachments());
                                                if (status.getSpoiler_text() != null && status.getSpoiler_text().length() > 0)
                                                    toot.setSpoiler_text(status.getSpoiler_text().trim());
                                                toot.setContent(status.getContent());
                                                toot.setVisibility(status.getVisibility());
                                                if( status.getPoll() != null){
                                                    toot.setPoll(status.getPoll());
                                                }else if(status.getReblog() != null &&  status.getReblog().getPoll() != null ) {
                                                    toot.setPoll(status.getPoll());
                                                }
                                                new RetrieveFeedsAsyncTask(context, RetrieveFeedsAsyncTask.Type.ONESTATUS, status.getIn_reply_to_id(), null, false, false, StatusListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                            } else {
                                                toot = new Status();
                                                toot.setSensitive(status.isSensitive());
                                                toot.setMedia_attachments(status.getMedia_attachments());
                                                if (status.getSpoiler_text() != null && status.getSpoiler_text().length() > 0)
                                                    toot.setSpoiler_text(status.getSpoiler_text().trim());
                                                toot.setVisibility(status.getVisibility());
                                                toot.setContent(status.getContent());
                                                if( status.getPoll() != null){
                                                    toot.setPoll(status.getPoll());
                                                }else if(status.getReblog() != null &&  status.getReblog().getPoll() != null ) {
                                                    toot.setPoll(status.getPoll());
                                                }
                                                final SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                                                long id = new StatusStoredDAO(context, db).insertStatus(toot, null);
                                                Intent intentToot = new Intent(context, TootActivity.class);
                                                Bundle b = new Bundle();
                                                b.putLong("restored", id);
                                                b.putBoolean("removed", true);
                                                intentToot.putExtras(b);
                                                context.startActivity(intentToot);
                                            }
                                        }
                                    } else if (doAction == API.StatusAction.REPORT) {
                                        String comment = null;
                                        if (finalInput.getText() != null)
                                            comment = finalInput.getText().toString();
                                        new PostActionAsyncTask(context, doAction, status.getId(), status, comment, StatusListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                    } else {
                                        String targetedId;
                                        if (item.getItemId() == R.id.action_block_domain) {
                                            targetedId = status.getAccount().getAcct().split("@")[1];
                                        } else {
                                            targetedId = status.getAccount().getId();
                                        }
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


            if (type != RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE) {
                holder.status_account_profile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (targetedId == null || !targetedId.equals(status.getAccount().getId())) {
                            Intent intent = new Intent(context, ShowAccountActivity.class);
                            Bundle b = new Bundle();
                            b.putParcelable("account", status.getAccount());
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
                            b.putParcelable("account", status.getReblog().getAccount());
                            intent.putExtras(b);
                            context.startActivity(intent);
                        }
                    }
                });
            } else {
                holder.status_account_profile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (targetedId == null || !targetedId.equals(status.getAccount().getId())) {
                            Account account = status.getAccount();
                            Pattern instanceHost = Pattern.compile("https?:\\/\\/([\\da-z\\.-]+\\.[a-z\\.]{2,10})");
                            Matcher matcher = instanceHost.matcher(status.getUrl());
                            String instance = null;
                            while (matcher.find()) {
                                instance = matcher.group(1);
                            }
                            account.setInstance(instance);
                            if(MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE)
                                CrossActions.doCrossProfile(context, account);
                            else {
                                Intent intent = new Intent(context, ShowAccountActivity.class);
                                Bundle b = new Bundle();
                                b.putParcelable("account", status.getAccount());
                                b.putBoolean("peertubeaccount", true);
                                intent.putExtras(b);
                                context.startActivity(intent);
                            }
                        }
                    }
                });
                holder.status_account_profile_boost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (targetedId == null || !targetedId.equals(status.getReblog().getAccount().getId())) {
                            Account account = status.getReblog().getAccount();
                            Pattern instanceHost = Pattern.compile("https?:\\/\\/([\\da-z\\.-]+\\.[a-z\\.]{2,10})");
                            Matcher matcher = instanceHost.matcher(status.getUrl());
                            String instance = null;
                            while (matcher.find()) {
                                instance = matcher.group(1);
                            }
                            account.setInstance(instance);
                            if(MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE)
                                CrossActions.doCrossProfile(context, account);
                            else {
                                Intent intent = new Intent(context, ShowAccountActivity.class);
                                Bundle b = new Bundle();
                                b.putParcelable("account", status.getAccount());
                                b.putBoolean("peertubeaccount", true);
                                intent.putExtras(b);
                                context.startActivity(intent);
                            }
                        }
                    }
                });
            }

            if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON){
                if (getItemViewType(viewHolder.getAdapterPosition()) == FOCUSED_STATUS && status.getApplication() != null && status.getApplication().getName() != null && status.getApplication().getName().length() > 0) {
                    Application application = status.getApplication();
                    holder.status_toot_app.setText(application.getName());
                    if (application.getWebsite() != null && !application.getWebsite().trim().equals("null") && application.getWebsite().trim().length() > 0) {
                        holder.status_toot_app.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Helper.openBrowser(context, application.getWebsite());
                            }
                        });
                    }
                    holder.status_toot_app.setVisibility(View.VISIBLE);
                } else {
                    holder.status_toot_app.setVisibility(View.GONE);
                }
            }else{
                holder.status_toot_app.setVisibility(View.GONE);
            }
        }

    }



    private void loadAttachments(final Status status, final ViewHolder holder, boolean blur){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        boolean fullAttachement = sharedpreferences.getBoolean(Helper.SET_FULL_PREVIEW, false);
        List<Attachment> attachments;
        if( status.getReblog() != null)
            attachments = status.getReblog().getMedia_attachments();
        else
            attachments = status.getMedia_attachments();
        if( !blur)
            holder.status_show_more.setVisibility(View.GONE);
        else
            holder.status_show_more.setVisibility(View.VISIBLE);
        if( attachments != null && attachments.size() > 0){
            int i = 0;
            holder.horizontal_second_image.setVisibility(View.VISIBLE);
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
                    holder.horizontal_second_image.setVisibility(View.GONE);
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
                RelativeLayout container = holder.status_horizontal_document_container;
                if( i == 0) {
                    imageView = fullAttachement?holder.status_prev1_h:holder.status_prev1;
                    if( attachment.getType().toLowerCase().equals("image") || attachment.getType().toLowerCase().equals("unknown"))
                        if( fullAttachement)
                            holder.status_prev1_play_h.setVisibility(View.GONE);
                        else
                            holder.status_prev1_play.setVisibility(View.GONE);
                    else {
                        if( attachment.getType().toLowerCase().equals("video") ||  attachment.getType().toLowerCase().equals("audio")) {
                            holder.status_prev1_play_h.setImageResource(R.drawable.ic_video_preview);
                            holder.status_prev1_play.setImageResource(R.drawable.ic_video_preview);
                        }else if( attachment.getType().toLowerCase().equals("gifv")) {
                            holder.status_prev1_play.setImageResource(R.drawable.ic_gif_preview);
                            holder.status_prev1_play_h.setImageResource(R.drawable.ic_gif_preview);
                        }else if(attachment.getType().toLowerCase().equals("web")){
                            holder.status_prev1_play.setImageResource(R.drawable.ic_http);
                        }
                        if (fullAttachement)
                            holder.status_prev1_play_h.setVisibility(View.VISIBLE);
                        else
                            holder.status_prev1_play.setVisibility(View.VISIBLE);
                    }
                }else if( i == 1) {
                    imageView = fullAttachement?holder.status_prev2_h:holder.status_prev2;
                    if( attachment.getType().toLowerCase().equals("image") || attachment.getType().toLowerCase().equals("unknown"))
                        if( fullAttachement)
                            holder.status_prev2_play_h.setVisibility(View.GONE);
                        else
                            holder.status_prev2_play.setVisibility(View.GONE);
                    else {
                        if( attachment.getType().toLowerCase().equals("video") || attachment.getType().toLowerCase().equals("audio")) {
                            holder.status_prev2_play_h.setImageResource(R.drawable.ic_video_preview);
                            holder.status_prev2_play.setImageResource(R.drawable.ic_video_preview);
                        }else if( attachment.getType().toLowerCase().equals("gifv")) {
                            holder.status_prev2_play_h.setImageResource(R.drawable.ic_gif_preview);
                            holder.status_prev2_play.setImageResource(R.drawable.ic_gif_preview);
                        }else if(attachment.getType().toLowerCase().equals("web")){
                            holder.status_prev1_play.setImageResource(R.drawable.ic_http);
                        }
                        if (fullAttachement)
                            holder.status_prev2_play_h.setVisibility(View.VISIBLE);
                        else
                            holder.status_prev2_play.setVisibility(View.VISIBLE);
                    }
                }else if(i == 2) {
                    imageView = fullAttachement?holder.status_prev3_h:holder.status_prev3;
                    if( attachment.getType().toLowerCase().equals("image") || attachment.getType().toLowerCase().equals("unknown"))
                        if( fullAttachement)
                            holder.status_prev3_play_h.setVisibility(View.GONE);
                        else
                            holder.status_prev3_play.setVisibility(View.GONE);
                    else {
                        if( attachment.getType().toLowerCase().equals("video") || attachment.getType().toLowerCase().equals("audio")) {
                            holder.status_prev3_play_h.setImageResource(R.drawable.ic_video_preview);
                            holder.status_prev3_play.setImageResource(R.drawable.ic_video_preview);
                        }else if( attachment.getType().toLowerCase().equals("gifv")) {
                            holder.status_prev3_play_h.setImageResource(R.drawable.ic_gif_preview);
                            holder.status_prev3_play.setImageResource(R.drawable.ic_gif_preview);
                        }else if(attachment.getType().toLowerCase().equals("web")){
                            holder.status_prev1_play.setImageResource(R.drawable.ic_http);
                        }
                        if (fullAttachement)
                            holder.status_prev3_play_h.setVisibility(View.VISIBLE);
                        else
                            holder.status_prev3_play.setVisibility(View.VISIBLE);
                    }
                }else {
                    imageView = fullAttachement?holder.status_prev4_h:holder.status_prev4;
                    if( attachment.getType().toLowerCase().equals("image") || attachment.getType().toLowerCase().equals("unknown"))
                        if( fullAttachement)
                            holder.status_prev4_play_h.setVisibility(View.GONE);
                        else
                            holder.status_prev4_play.setVisibility(View.GONE);
                    else {
                        if( attachment.getType().toLowerCase().equals("video") || attachment.getType().toLowerCase().equals("audio")) {
                            holder.status_prev4_play_h.setImageResource(R.drawable.ic_video_preview);
                            holder.status_prev4_play.setImageResource(R.drawable.ic_video_preview);
                        }else if( attachment.getType().toLowerCase().equals("gifv")) {
                            holder.status_prev4_play_h.setImageResource(R.drawable.ic_gif_preview);
                            holder.status_prev4_play.setImageResource(R.drawable.ic_gif_preview);
                        }else if(attachment.getType().toLowerCase().equals("web")){
                            holder.status_prev1_play.setImageResource(R.drawable.ic_http);
                        }
                        if (fullAttachement)
                            holder.status_prev4_play_h.setVisibility(View.VISIBLE);
                        else
                            holder.status_prev4_play.setVisibility(View.VISIBLE);
                    }
                }
                String url = attachment.getPreview_url();

                if( url == null || url.trim().equals("") )
                    url = attachment.getUrl();
                else if( attachment.getType().toLowerCase().equals("unknown"))
                    url = attachment.getRemote_url();

                if( fullAttachement){
                    imageView.setImageBitmap(null);
                    if( !url.trim().contains("missing.png") && !((Activity)context).isFinishing() ) {
                        if( !blur) {
                            Glide.with(imageView.getContext())
                                    .asBitmap()
                                    .load( !attachment.getType().toLowerCase().equals("audio")?url:R.drawable.ic_audio_wave)
                                    .thumbnail(0.1f)
                                    .apply(new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(10)))
                                    .into(new SimpleTarget<Bitmap>() {
                                        @Override
                                        public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                                            DrawableTransitionOptions.withCrossFade();
                                            int width = resource.getWidth();
                                            int height = resource.getHeight();

                                            if (height < Helper.convertDpToPixel(200, context)) {
                                                double ratio = ((double) Helper.convertDpToPixel(200, context) / (double) height);
                                                width = (int) (ratio * width);
                                                height = (int) Helper.convertDpToPixel(200, context);
                                                resource = Bitmap.createScaledBitmap(resource, width, height, false);
                                            }
                                            imageView.setImageBitmap(resource);
                                            status.setMedia_height(container.getHeight());
                                        }
                                    });
                        }else{
                            Glide.with(imageView.getContext())
                                    .asBitmap()
                                    .load(!attachment.getType().toLowerCase().equals("audio")?url:R.drawable.ic_audio_wave)
                                    .thumbnail(0.1f)
                                    .apply(new RequestOptions().transforms(new BlurTransformation(50,3), new RoundedCorners(10)))
                                    .into(new SimpleTarget<Bitmap>() {
                                        @Override
                                        public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                                            DrawableTransitionOptions.withCrossFade();
                                            int width = resource.getWidth();
                                            int height = resource.getHeight();

                                            if (height < Helper.convertDpToPixel(200, context)) {
                                                double ratio = ((double) Helper.convertDpToPixel(200, context) / (double) height);
                                                width = (int) (ratio * width);
                                                height = (int) Helper.convertDpToPixel(200, context);
                                                resource = Bitmap.createScaledBitmap(resource, width, height, false);
                                            }
                                            imageView.setImageBitmap(resource);
                                        }
                                    });
                        }
                    }
                }else {
                    if (!url.trim().contains("missing.png") && !((Activity) context).isFinishing()) {
                        if( !blur) {
                            Glide.with(imageView.getContext())
                                    .load(!attachment.getType().toLowerCase().equals("audio")?url:R.drawable.ic_audio_wave)
                                    .thumbnail(0.1f)
                                    .apply(new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(10)))
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .into(imageView);
                        }else{
                            Glide.with(imageView.getContext())
                                    .load(!attachment.getType().toLowerCase().equals("audio")?url:R.drawable.ic_audio_wave)
                                    .thumbnail(0.1f)
                                    .apply(new RequestOptions().transforms(new BlurTransformation(50,3), new RoundedCorners(10)))
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .into(imageView);
                        }
                    }
                }
                final int finalPosition = position;
                if( attachment.getDescription() != null && !attachment.getDescription().equals("null"))
                    imageView.setContentDescription(attachment.getDescription());
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if( status.isAttachmentShown()) {

                            ArrayList<Attachment> attachmentArrayList;
                            if( status.getReblog() == null)
                                attachmentArrayList = status.getMedia_attachments();
                            else
                                attachmentArrayList = status.getReblog().getMedia_attachments();
                            if (attachment.getType().equals("web")) {
                                Helper.openBrowser(context, attachment.getUrl());
                            } else {
                                Intent intent = new Intent(context, MediaActivity.class);
                                Bundle b = new Bundle();
                                intent.putParcelableArrayListExtra("mediaArray", attachmentArrayList);
                                b.putInt("position", finalPosition);
                                intent.putExtras(b);
                                context.startActivity(intent);
                            }
                        }else{
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
                    }
                });
                boolean long_press_media = sharedpreferences.getBoolean(Helper.SET_LONG_PRESS_MEDIA, true);
                if( long_press_media) {
                    imageView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            String myDir = sharedpreferences.getString(Helper.SET_FOLDER_RECORD, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
                            String fileName = URLUtil.guessFileName(attachment.getUrl(), null, null);
                            Helper.download(context, myDir + "/" + fileName, attachment.getUrl());
                            return true;
                        }
                    });
                }
                i++;
                position++;
            }
        }else{
            holder.status_horizontal_document_container.setVisibility(View.GONE);
            holder.status_document_container.setVisibility(View.GONE);
            holder.status_show_more.setVisibility(View.GONE);
        }

    }

    private int closePanels(){
        int position = -1;
        if( statuses != null && statuses.size() > 0){
            for(Status status: statuses){
                position++;
                if( status.isCustomFeaturesDisplayed()) {
                    status.setCustomFeaturesDisplayed(false);
                    notifyItemChanged(position);
                    break;
                }
            }
        }
        return position;
    }

    private void timedMuteAction(Status status){
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        int style;
        if (theme == Helper.THEME_DARK) {
            style = R.style.DialogDark;
        } else if (theme == Helper.THEME_BLACK) {
            style = R.style.DialogBlack;
        } else {
            style = R.style.Dialog;
        }
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context, style);
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
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
                } else {
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
                if ((time - new Date().getTime()) < 60000) {
                    Toasty.error(context, context.getString(R.string.timed_mute_date_error), Toast.LENGTH_LONG).show();
                } else {
                    //Store the toot as draft first
                    String targeted_id = status.getAccount().getId();
                    Date date_mute = new Date(time);
                    SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                    String instance = sharedpreferences.getString(Helper.PREF_INSTANCE, null);
                    String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
                    Account account = new AccountDAO(context, db).getUniqAccount(userId, instance);
                    new TempMuteDAO(context, db).insert(account, targeted_id, new Date(time));
                    if (timedMute != null && !timedMute.contains(account.getId()))
                        timedMute.add(targeted_id);
                    else if (timedMute == null) {
                        timedMute = new ArrayList<>();
                        timedMute.add(targeted_id);
                    }
                    Toasty.success(context, context.getString(R.string.timed_mute_date, status.getAccount().getAcct(), Helper.dateToString(date_mute)), Toast.LENGTH_LONG).show();
                    alertDialog.dismiss();
                    notifyDataSetChanged();
                }
            }
        });
        alertDialog.show();
    }

    private void scheduleBoost(Status status){

        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        int style;
        if (theme == Helper.THEME_DARK) {
            style = R.style.DialogDark;
        } else if (theme == Helper.THEME_BLACK) {
            style = R.style.DialogBlack;
        } else {
            style = R.style.Dialog;
        }

        AlertDialog.Builder dialogBuilderBoost = new AlertDialog.Builder(context, style);
        LayoutInflater inflaterBoost = ((Activity) context).getLayoutInflater();
        @SuppressLint("InflateParams") View dialogViewBoost = inflaterBoost.inflate(R.layout.datetime_picker, null);
        dialogBuilderBoost.setView(dialogViewBoost);
        final AlertDialog alertDialogBoost = dialogBuilderBoost.create();

        final DatePicker datePickerBoost = dialogViewBoost.findViewById(R.id.date_picker);
        final TimePicker timePickerBoost = dialogViewBoost.findViewById(R.id.time_picker);
        timePickerBoost.setIs24HourView(true);
        Button date_time_cancelBoost = dialogViewBoost.findViewById(R.id.date_time_cancel);
        final ImageButton date_time_previousBoost = dialogViewBoost.findViewById(R.id.date_time_previous);
        final ImageButton date_time_nextBoost = dialogViewBoost.findViewById(R.id.date_time_next);
        final ImageButton date_time_setBoost = dialogViewBoost.findViewById(R.id.date_time_set);

        //Buttons management
        date_time_cancelBoost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialogBoost.dismiss();
            }
        });
        date_time_nextBoost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerBoost.setVisibility(View.GONE);
                timePickerBoost.setVisibility(View.VISIBLE);
                date_time_previousBoost.setVisibility(View.VISIBLE);
                date_time_nextBoost.setVisibility(View.GONE);
                date_time_setBoost.setVisibility(View.VISIBLE);
            }
        });
        date_time_previousBoost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerBoost.setVisibility(View.VISIBLE);
                timePickerBoost.setVisibility(View.GONE);
                date_time_previousBoost.setVisibility(View.GONE);
                date_time_nextBoost.setVisibility(View.VISIBLE);
                date_time_setBoost.setVisibility(View.GONE);
            }
        });
        date_time_setBoost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour, minute;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    hour = timePickerBoost.getHour();
                    minute = timePickerBoost.getMinute();
                } else {
                    //noinspection deprecation
                    hour = timePickerBoost.getCurrentHour();
                    //noinspection deprecation
                    minute = timePickerBoost.getCurrentMinute();
                }
                Calendar calendar = new GregorianCalendar(datePickerBoost.getYear(),
                        datePickerBoost.getMonth(),
                        datePickerBoost.getDayOfMonth(),
                        hour,
                        minute);
                long time = calendar.getTimeInMillis();
                if ((time - new Date().getTime()) < 60000) {
                    Toasty.warning(context, context.getString(R.string.toot_scheduled_date), Toast.LENGTH_LONG).show();
                } else {
                    //Schedules the toot
                    ScheduledBoostsSyncJob.schedule(context, status, time);
                    //Clear content
                    Toasty.info(context, context.getString(R.string.boost_scheduled), Toast.LENGTH_LONG).show();
                    alertDialogBoost.dismiss();
                }
            }
        });
        alertDialogBoost.show();
    }

    private void mention(Status status){
        // Get a handler that can be used to post to the main thread
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                                            /*String name = "@" + (status.getReblog() != null ? status.getReblog().getAccount().getAcct() : status.getAccount().getAcct());
                                            if (name.split("@", -1).length - 1 == 1)
                                                name = name + "@" + getLiveInstance(context);
                                            Bitmap bitmap = Helper.convertTootIntoBitmap(context, name, holder.status_content);*/
                Intent intent = new Intent(context, TootActivity.class);
                Bundle b = new Bundle();
                                            /*String fname = "tootmention_" + status.getId() + ".jpg";
                                            File file = new File(context.getCacheDir() + "/", fname);
                                            if (file.exists()) //noinspection ResultOfMethodCallIgnored
                                                file.delete();
                                            try {
                                                FileOutputStream out = new FileOutputStream(file);
                                                assert bitmap != null;
                                                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                                                out.flush();
                                                out.close();
                                            } catch (Exception ignored) {
                                            }
                                            b.putString("fileMention", fname);*/
                b.putString("tootMention", (status.getReblog() != null) ? status.getReblog().getAccount().getAcct() : status.getAccount().getAcct());
                b.putString("urlMention", (status.getReblog() != null) ? status.getReblog().getUrl() : status.getUrl());
                intent.putExtras(b);
                context.startActivity(intent);
            }
        }, 500);
    }

    private void tootInformation(Status status){
        Intent intent = new Intent(context, TootInfoActivity.class);
        Bundle b = new Bundle();
        if (status.getReblog() != null) {
            b.putString("toot_id", status.getReblog().getId());
            b.putInt("toot_reblogs_count", status.getReblog().getReblogs_count());
            b.putInt("toot_favorites_count", status.getReblog().getFavourites_count());
        } else {
            b.putString("toot_id", status.getId());
            b.putInt("toot_reblogs_count", status.getReblogs_count());
            b.putInt("toot_favorites_count", status.getFavourites_count());
        }
        intent.putExtras(b);
        context.startActivity(intent);
    }

    private void bookmark(Status status){
        if (type != RetrieveFeedsAsyncTask.Type.CACHE_BOOKMARKS) {
            status.setBookmarked(!status.isBookmarked());
            try {
                final SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                if (status.isBookmarked()) {
                    new StatusCacheDAO(context, db).insertStatus(StatusCacheDAO.BOOKMARK_CACHE, status);
                    Toasty.success(context, context.getString(R.string.status_bookmarked), Toast.LENGTH_LONG).show();
                } else {
                    new StatusCacheDAO(context, db).remove(StatusCacheDAO.BOOKMARK_CACHE, status);
                    Toasty.success(context, context.getString(R.string.status_unbookmarked), Toast.LENGTH_LONG).show();
                }
                notifyStatusChanged(status);
            } catch (Exception e) {
                e.printStackTrace();
                Toasty.error(context, context.getString(R.string.toast_error), Toast.LENGTH_LONG).show();
            }
        } else {
            int position = 0;
            for (Status statustmp : statuses) {
                if (statustmp.getId().equals(status.getId())) {
                    statuses.remove(status);
                    statusListAdapter.notifyItemRemoved(position);
                    final SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                    new StatusCacheDAO(context, db).remove(StatusCacheDAO.BOOKMARK_CACHE, statustmp);
                    Toasty.success(context, context.getString(R.string.status_unbookmarked), Toast.LENGTH_LONG).show();
                    break;
                }
                position++;
            }
        }
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
            statusListAdapter.notifyDataSetChanged();
        }else if( statusAction == API.StatusAction.MUTE_CONVERSATION ){
            for(Status status: statuses){
                if( status.getId().equals(targetedId)) {
                    status.setMuted(true);
                    notifyStatusChanged(status);
                    break;
                }
            }
        }else if( statusAction == API.StatusAction.UNMUTE_CONVERSATION ){
            for(Status status: statuses){
                if( status.getId().equals(targetedId)) {
                    status.setMuted(false);
                    notifyStatusChanged(status);
                    break;
                }
            }
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
        if( statusAction == API.StatusAction.PEERTUBEDELETECOMMENT){
            int position = 0;
            for(Status status: statuses){
                if( status.getId().equals(targetedId)) {
                    statuses.remove(status);
                    statusListAdapter.notifyItemRemoved(position);
                    break;
                }
                position++;
            }
        }
    }

    public void notifyStatusChanged(Status status){
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

    public void notifyStatusWithActionChanged(Status status){
        if( status == null)
            return;
        for (int i = 0; i < statusListAdapter.getItemCount(); i++) {
            //noinspection ConstantConditions
            if (statusListAdapter.getItemAt(i) != null && statusListAdapter.getItemAt(i).getId().equals(status.getId())) {
                try {
                    statuses.set(i, status);
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
    }

    @Override
    public void onRetrieveEmoji(Notification notification) {

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
                        Status.transformTranslation(context,  status);
                        Status.makeEmojisTranslation(context, StatusListAdapter.this, status);
                        notifyStatusChanged(status);
                    }else {
                        Toasty.error(context, context.getString(R.string.toast_error_translate), Toast.LENGTH_LONG).show();
                    }
                }
                @Override
                public void onFail(HttpsConnectionException e) {
                    e.printStackTrace();
                    Toasty.error(context, context.getString(R.string.toast_error_translate), Toast.LENGTH_LONG).show();
                }
            });
        }else {
            status.setTranslationShown(!status.isTranslationShown());
            notifyStatusChanged(status);
        }
    }

    public void setConversationPosition(int position){
        this.conversationPosition = position;
    }
}