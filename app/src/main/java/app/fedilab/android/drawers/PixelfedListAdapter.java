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
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;;
import com.github.stom79.mytransl.MyTransL;
import com.github.stom79.mytransl.client.HttpsConnectionException;
import com.github.stom79.mytransl.client.Results;
import com.github.stom79.mytransl.translate.Translate;
import com.smarteist.autoimageslider.IndicatorAnimations;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;
import com.varunest.sparkbutton.SparkButton;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import app.fedilab.android.activities.PixelfedComposeActivity;
import app.fedilab.android.activities.SlideMediaActivity;
import app.fedilab.android.activities.TootActivity;
import app.fedilab.android.activities.TootInfoActivity;
import app.fedilab.android.asynctasks.PostActionAsyncTask;
import app.fedilab.android.asynctasks.PostStatusAsyncTask;
import app.fedilab.android.asynctasks.RetrieveContextAsyncTask;
import app.fedilab.android.client.API;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Emojis;
import app.fedilab.android.client.Entities.Error;
import app.fedilab.android.client.Entities.Notification;
import app.fedilab.android.client.Entities.Status;
import app.fedilab.android.helper.CrossActions;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.helper.MastalabAutoCompleteTextView;
import app.fedilab.android.interfaces.OnPostStatusActionInterface;
import app.fedilab.android.interfaces.OnRetrieveContextInterface;
import app.fedilab.android.interfaces.OnRetrieveFeedsInterface;
import app.fedilab.android.interfaces.OnRetrieveSearcAccountshInterface;
import app.fedilab.android.interfaces.OnRetrieveSearchInterface;
import app.fedilab.android.jobs.ScheduledBoostsSyncJob;
import app.fedilab.android.sqlite.AccountDAO;
import app.fedilab.android.sqlite.Sqlite;
import app.fedilab.android.sqlite.StatusCacheDAO;
import app.fedilab.android.sqlite.StatusStoredDAO;
import app.fedilab.android.sqlite.TempMuteDAO;
import es.dmoral.toasty.Toasty;
import app.fedilab.android.R;
import app.fedilab.android.activities.MainActivity;
import app.fedilab.android.activities.ShowAccountActivity;
import app.fedilab.android.activities.ShowConversationActivity;
import app.fedilab.android.asynctasks.RetrieveFeedsAsyncTask;
import app.fedilab.android.asynctasks.UpdateAccountInfoAsyncTask;
import app.fedilab.android.interfaces.OnPostActionInterface;
import app.fedilab.android.interfaces.OnRetrieveEmojiInterface;

import static android.content.Context.MODE_PRIVATE;
import static app.fedilab.android.activities.BaseMainActivity.mutedAccount;
import static app.fedilab.android.activities.BaseMainActivity.social;
import static app.fedilab.android.helper.Helper.changeDrawableColor;


/**
 * Created by Thomas on 14/01/2019.
 * Adapter for pixelfed drawer
 */
public class PixelfedListAdapter extends RecyclerView.Adapter implements OnPostActionInterface, OnRetrieveEmojiInterface, OnPostStatusActionInterface, OnRetrieveSearchInterface, OnRetrieveSearcAccountshInterface, OnRetrieveContextInterface, OnRetrieveFeedsInterface {

    private Context context;
    private List<Status> statuses;
    private PixelfedListAdapter pixelfedListAdapter;
    private final int HIDDEN_STATUS = 0;
    private static final int DISPLAYED_STATUS = 1;
    private RetrieveFeedsAsyncTask.Type type;
    private MastalabAutoCompleteTextView comment_content;
    private String in_reply_to_status;
    private String visibility;
    private int theme;
    private long currentToId = -1;
    private Status tootReply;
    private boolean redraft = false;
    private Status toot;

    public PixelfedListAdapter(RetrieveFeedsAsyncTask.Type type, List<Status> statuses) {
        super();
        this.statuses = statuses;
        this.type = type;
        pixelfedListAdapter = this;
    }




    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return statuses.size();
    }

    private Status getItemAt(int position) {
        if (statuses.size() > position)
            return statuses.get(position);
        else
            return null;
    }


    @Override
    public void onRetrieveContext(APIResponse apiResponse) {
        if (apiResponse.getError() != null ) {
            return;
        }
        List<Status> statuses = apiResponse.getContext().getDescendants();

        String targetedId = apiResponse.getTargetedId();
        int position = 0;
        for(Status tl: this.statuses){
            if( tl.getId().equals(targetedId)){
                this.statuses.get(position).setCommentsFetched(true);
                this.statuses.get(position).setComments(statuses);
                notifyStatusChanged(this.statuses.get(position));
                break;
            }
            position++;
        }
    }




    @Override
    public void onPostStatusAction(APIResponse apiResponse) {
        if (apiResponse.getError() != null) {
            if (apiResponse.getError().getError().contains("422")) {
                Toasty.error(context, context.getString(R.string.toast_error_char_limit), Toast.LENGTH_SHORT).show();
                return;
            } else {
                if(apiResponse.getError().getError().length() < 100) {
                    Toasty.error(context, apiResponse.getError().getError(), Toast.LENGTH_LONG).show();
                }else{
                    Toasty.error(context, context.getString(R.string.long_api_error,"\ud83d\ude05"), Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);

        comment_content = null;
        tootReply = null;
        currentToId = -1;
        if (apiResponse.getError() == null) {
            boolean display_confirm = sharedpreferences.getBoolean(Helper.SET_DISPLAY_CONFIRM, true);
            if (display_confirm) {
                Toasty.success(context, context.getString(R.string.toot_sent), Toast.LENGTH_LONG).show();
            }
        } else {
            if (apiResponse.getError().getStatusCode() == -33)
                Toasty.info(context, context.getString(R.string.toast_toot_saved_error), Toast.LENGTH_LONG).show();
        }

        if( apiResponse.getTargetedId() != null && apiResponse.getStatuses() != null && apiResponse.getStatuses().size() > 0){
            int position = 0;
            for(Status tl: this.statuses){
                if( tl.getId().equals(apiResponse.getTargetedId())){
                    List<Status> comments = this.statuses.get(position).getComments();
                    comments.add(comments.size(), apiResponse.getStatuses().get(0));
                    this.statuses.get(position).setComments(comments);
                    notifyStatusChanged(this.statuses.get(position));
                    break;
                }
                position++;
            }
        }


    }

    @Override
    public void onRetrieveSearchAccounts(APIResponse apiResponse) {
        if (apiResponse.getError() != null)
            return;

        final List<Account> accounts = apiResponse.getAccounts();
        if (accounts != null && accounts.size() > 0) {
            int currentCursorPosition = comment_content.getSelectionStart();
            AccountsSearchAdapter accountsListAdapter = new AccountsSearchAdapter(context, accounts);
            comment_content.setThreshold(1);
            comment_content.setAdapter(accountsListAdapter);
            final String oldContent = comment_content.getText().toString();
            if (oldContent.length() >= currentCursorPosition) {
                String[] searchA = oldContent.substring(0, currentCursorPosition).split("@");
                if (searchA.length > 0) {
                    final String search = searchA[searchA.length - 1];
                    comment_content.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Account account = accounts.get(position);
                            String deltaSearch = "";
                            int searchLength = 15;
                            if (currentCursorPosition < 15) { //Less than 15 characters are written before the cursor position
                                searchLength = currentCursorPosition;
                            }
                            if (currentCursorPosition - searchLength > 0 && currentCursorPosition < oldContent.length())
                                deltaSearch = oldContent.substring(currentCursorPosition - searchLength, currentCursorPosition);
                            else {
                                if (currentCursorPosition >= oldContent.length())
                                    deltaSearch = oldContent.substring(currentCursorPosition - searchLength, oldContent.length());
                            }
                            if (!search.equals(""))
                                deltaSearch = deltaSearch.replace("@" + search, "");
                            String newContent = oldContent.substring(0, currentCursorPosition - searchLength);
                            newContent += deltaSearch;
                            newContent += "@" + account.getAcct() + " ";
                            int newPosition = newContent.length();
                            if (currentCursorPosition < oldContent.length())
                                newContent += oldContent.substring(currentCursorPosition, oldContent.length());
                            comment_content.setText(newContent);
                            comment_content.setSelection(newPosition);
                            AccountsSearchAdapter accountsListAdapter = new AccountsSearchAdapter(context, new ArrayList<>());
                            comment_content.setThreshold(1);
                            comment_content.setAdapter(accountsListAdapter);
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onRetrieveContact(APIResponse apiResponse) {

    }

    @Override
    public void onRetrieveSearch(APIResponse apiResponse) {
        if (apiResponse == null || apiResponse.getResults() == null || comment_content == null)
            return;
        app.fedilab.android.client.Entities.Results results = apiResponse.getResults();
        int currentCursorPosition = comment_content.getSelectionStart();
        final List<String> tags = results.getHashtags();

        if (tags != null && tags.size() > 0) {
            TagsSearchAdapter tagsSearchAdapter = new TagsSearchAdapter(context, tags);
            comment_content.setThreshold(1);
            comment_content.setAdapter(tagsSearchAdapter);
            final String oldContent = comment_content.getText().toString();
            if (oldContent.length() < currentCursorPosition)
                return;
            String[] searchA = oldContent.substring(0, currentCursorPosition).split("#");
            if (searchA.length < 1)
                return;
            final String search = searchA[searchA.length - 1];
            comment_content.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (position >= tags.size())
                        return;
                    String tag = tags.get(position);
                    String deltaSearch = "";
                    int searchLength = 15;
                    if (currentCursorPosition < 15) { //Less than 15 characters are written before the cursor position
                        searchLength = currentCursorPosition;
                    }
                    if (currentCursorPosition - searchLength > 0 && currentCursorPosition < oldContent.length())
                        deltaSearch = oldContent.substring(currentCursorPosition - searchLength, currentCursorPosition);
                    else {
                        if (currentCursorPosition >= oldContent.length())
                            deltaSearch = oldContent.substring(currentCursorPosition - searchLength, oldContent.length());
                    }

                    if (!search.equals(""))
                        deltaSearch = deltaSearch.replace("#" + search, "");
                    String newContent = oldContent.substring(0, currentCursorPosition - searchLength);
                    newContent += deltaSearch;
                    newContent += "#" + tag + " ";
                    int newPosition = newContent.length();
                    if (currentCursorPosition < oldContent.length())
                        newContent += oldContent.substring(currentCursorPosition, oldContent.length());
                    comment_content.setText(newContent);
                    comment_content.setSelection(newPosition);
                    TagsSearchAdapter tagsSearchAdapter = new TagsSearchAdapter(context, new ArrayList<>());
                    comment_content.setThreshold(1);
                    comment_content.setAdapter(tagsSearchAdapter);
                }
            });
        }
    }

    @Override
    public void onRetrieveFeeds(APIResponse apiResponse) {
        if (apiResponse.getStatuses() != null && apiResponse.getStatuses().size() > 0) {

            SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
            long id = new StatusStoredDAO(context, db).insertStatus(toot, apiResponse.getStatuses().get(0));
            Intent intentToot = new Intent(context, PixelfedComposeActivity.class);
            Bundle b = new Bundle();
            b.putLong("restored", id);
            b.putBoolean("removed", true);
            intentToot.putExtras(b);
            context.startActivity(intentToot);
        }

    }


    private class ViewHolderEmpty extends RecyclerView.ViewHolder {
        ViewHolderEmpty(View itemView) {
            super(itemView);
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
    }


    private class ViewHolderPixelfed extends RecyclerView.ViewHolder {
        SliderView imageSlider;
        ImageView art_media, art_media_play, pf_pp, pf_comment;
        SparkButton pf_fav, pf_share;
        TextView pf_username, pf_likes, pf_description, pf_date;
        CardView pf_cardview;
        LinearLayout pf_bottom_container;
        LinearLayout pixelfed_comments;
        RecyclerView lv_comments;

        ConstraintLayout quick_reply_container;
        MastalabAutoCompleteTextView quick_reply_text;
        ImageView quick_reply_switch_to_full, status_more;
        TextView toot_space_left;
        ImageView quick_reply_emoji;
        Button quick_reply_button;
        ImageView quick_reply_privacy;
        ViewHolderPixelfed(View itemView) {
            super(itemView);
            art_media = itemView.findViewById(R.id.art_media);
            art_media_play = itemView.findViewById(R.id.art_media_play);
            imageSlider = itemView.findViewById(R.id.imageSlider);
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
            pixelfed_comments =  itemView.findViewById(R.id.pixelfed_comments);
            lv_comments =  itemView.findViewById(R.id.lv_comments);
            quick_reply_container = itemView.findViewById(R.id.quick_reply_container);
            status_more = itemView.findViewById(R.id.status_more);
            quick_reply_text = itemView.findViewById(R.id.quick_reply_text);
            quick_reply_switch_to_full = itemView.findViewById(R.id.quick_reply_switch_to_full);
            toot_space_left = itemView.findViewById(R.id.toot_space_left);
            quick_reply_emoji = itemView.findViewById(R.id.quick_reply_emoji);
            quick_reply_button = itemView.findViewById(R.id.quick_reply_button);
            quick_reply_privacy = itemView.findViewById(R.id.quick_reply_privacy);
        }
    }


    public Status getItem(int position) {
        if (statuses.size() > position && position >= 0)
            return statuses.get(position);
        else return null;
    }

    @Override
    public int getItemViewType(int position) {
        boolean show_boosts = false;
        boolean show_replies = false;
        if (context instanceof ShowAccountActivity) {
            show_boosts = ((ShowAccountActivity) context).showBoosts();
            show_replies = ((ShowAccountActivity) context).showReplies();
        }
        if (!Helper.filterToots(statuses.get(position), null, context instanceof ShowAccountActivity, show_boosts, show_replies))
            return HIDDEN_STATUS;
        else
            return DISPLAYED_STATUS;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(this.context);
        if (viewType == DISPLAYED_STATUS)
            return new ViewHolderPixelfed(layoutInflater.inflate(R.layout.drawer_pixelfed, parent, false));
        else
            return new ViewHolderEmpty(layoutInflater.inflate(R.layout.drawer_empty, parent, false));
    }


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int i) {
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        final String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        if (viewHolder.getItemViewType() != HIDDEN_STATUS) {
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


            holder.art_media.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, SlideMediaActivity.class);
                    Bundle b = new Bundle();
                    intent.putParcelableArrayListExtra("mediaArray", status.getMedia_attachments());
                    b.putInt("position", 1);
                    intent.putExtras(b);
                    context.startActivity(intent);
                }
            });

            holder.quick_reply_switch_to_full.setVisibility(View.GONE);
            if (status.isShortReply()) {
                holder.quick_reply_container.setVisibility(View.VISIBLE);
                holder.pixelfed_comments.setVisibility(View.VISIBLE);
                in_reply_to_status = status.getReblog() != null ? status.getReblog().getId() : status.getId();
                if( status.isCommentsFetched()){
                    StatusListAdapter statusListAdapter = new StatusListAdapter(0, status.getId(), true, status.getComments());
                    final LinearLayoutManager mLayoutManager;
                    mLayoutManager = new LinearLayoutManager(context);
                    holder.lv_comments.setLayoutManager(mLayoutManager);
                    holder.lv_comments.setAdapter(statusListAdapter);
                    mLayoutManager.scrollToPositionWithOffset(i, 0);
                }else{
                    status.setCommentsFetched(true);
                    new RetrieveContextAsyncTask(context, false, false, status.getId(),PixelfedListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }

                EditText content_cw = new EditText(context);
                content_cw.setText(status.getReblog() != null ? status.getReblog().getSpoiler_text() : status.getSpoiler_text());
                String content = TootActivity.manageMentions(context, userId,status.getReblog() != null ? status.getReblog() : status);
                TextWatcher textWatcher = PixelfedComposeActivity.initializeTextWatcher(context, social, holder.quick_reply_text, holder.toot_space_left, null, null, PixelfedListAdapter.this, PixelfedListAdapter.this, PixelfedListAdapter.this);
                holder.quick_reply_text.addTextChangedListener(textWatcher);
                holder.quick_reply_text.setText(content);
                comment_content = holder.quick_reply_text;
                holder.quick_reply_text.setFocusable(true);
                holder.quick_reply_text.requestFocus();
                holder.quick_reply_text.setSelection(content.length()); //Put cursor at the end
                int newInputType = comment_content.getInputType() & (comment_content.getInputType() ^ InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
                comment_content.setInputType(newInputType);
                in_reply_to_status = status.getReblog() != null ? status.getReblog().getId() : status.getId();
                tootReply = status;
                theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
                if (theme == Helper.THEME_DARK || theme == Helper.THEME_BLACK) {
                    changeDrawableColor(context, R.drawable.emoji_one_category_smileysandpeople, R.color.dark_text);
                    changeDrawableColor(context, R.drawable.ic_public_toot, R.color.dark_text);
                    changeDrawableColor(context, R.drawable.ic_lock_open_toot, R.color.dark_text);
                    changeDrawableColor(context, R.drawable.ic_lock_outline_toot, R.color.dark_text);
                    changeDrawableColor(context, R.drawable.ic_mail_outline_toot, R.color.dark_text);
                    changeDrawableColor(context, holder.quick_reply_switch_to_full, R.color.dark_text);
                    if (theme == Helper.THEME_DARK) {
                        holder.quick_reply_container.setBackgroundResource(R.drawable.quick_reply_background);
                        changeDrawableColor(context, R.drawable.quick_reply_background, R.color.quick_reply_background_dark);
                    } else {
                        holder.quick_reply_container.setBackgroundResource(R.drawable.quick_reply_background_black);
                        changeDrawableColor(context, R.drawable.quick_reply_background, R.color.quick_reply_background_black);
                    }
                } else {
                    holder.quick_reply_container.setBackgroundResource(R.drawable.quick_reply_background_light);
                    changeDrawableColor(context, R.drawable.emoji_one_category_smileysandpeople, R.color.black);
                    changeDrawableColor(context, R.drawable.ic_public_toot, R.color.black);
                    changeDrawableColor(context, R.drawable.ic_lock_open_toot, R.color.black);
                    changeDrawableColor(context, R.drawable.ic_lock_outline_toot, R.color.black);
                    changeDrawableColor(context, R.drawable.ic_mail_outline_toot, R.color.black);
                    changeDrawableColor(context, holder.quick_reply_switch_to_full, R.color.black);

                }

                final SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                String instance = sharedpreferences.getString(Helper.PREF_INSTANCE, null);
                Account account = new AccountDAO(context, db).getUniqAccount(userId, instance);

                String defaultVisibility = account.isLocked() ? "private" : "public";
                String settingsVisibility = sharedpreferences.getString(Helper.SET_TOOT_VISIBILITY + "@" + account.getAcct() + "@" + account.getInstance(), defaultVisibility);
                int initialTootVisibility = 0;
                int ownerTootVisibility = 0;
                switch (status.getReblog() != null ? status.getReblog().getVisibility() : status.getVisibility()) {
                    case "public":
                        initialTootVisibility = 4;
                        break;
                    case "unlisted":
                        initialTootVisibility = 3;
                        break;
                    case "private":
                        visibility = "private";
                        initialTootVisibility = 2;
                        break;
                    case "direct":
                        visibility = "direct";
                        initialTootVisibility = 1;
                        break;
                }
                switch (settingsVisibility) {
                    case "public":
                        ownerTootVisibility = 4;
                        break;
                    case "unlisted":
                        ownerTootVisibility = 3;
                        break;
                    case "private":
                        visibility = "private";
                        ownerTootVisibility = 2;
                        break;
                    case "direct":
                        visibility = "direct";
                        ownerTootVisibility = 1;
                        break;
                }
                int tootVisibility;
                if (ownerTootVisibility >= initialTootVisibility) {
                    tootVisibility = initialTootVisibility;
                } else {
                    tootVisibility = ownerTootVisibility;
                }
                switch (tootVisibility) {
                    case 4:
                        visibility = "public";
                        holder.quick_reply_privacy.setImageResource(R.drawable.ic_public_toot);
                        break;
                    case 3:
                        visibility = "unlisted";
                        holder.quick_reply_privacy.setImageResource(R.drawable.ic_lock_open_toot);
                        break;
                    case 2:
                        visibility = "private";
                        holder.quick_reply_privacy.setImageResource(R.drawable.ic_lock_outline_toot);
                        break;
                    case 1:
                        visibility = "direct";
                        holder.quick_reply_privacy.setImageResource(R.drawable.ic_mail_outline_toot);
                        break;
                }

            } else {
                holder.quick_reply_container.setVisibility(View.GONE);
                holder.pixelfed_comments.setVisibility(View.GONE);
            }


            holder.art_media_play.setVisibility(View.GONE);

            if (status.getMedia_attachments() != null && status.getMedia_attachments().size() > 1){
                SliderAdapter sliderAdapter = new SliderAdapter(new WeakReference<>((Activity)context), false, status.getMedia_attachments());
                holder.imageSlider.setSliderAdapter(sliderAdapter);
                holder.imageSlider.setIndicatorAnimation(IndicatorAnimations.WORM);
                holder.imageSlider.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
                holder.art_media.setVisibility(View.GONE);
                holder.imageSlider.setVisibility(View.VISIBLE);
            }else if(status.getMedia_attachments() != null ){
                holder.art_media.setVisibility(View.VISIBLE);
                holder.imageSlider.setVisibility(View.GONE);
                if( status.getMedia_attachments().get(0).getType().toLowerCase().equals("video")){
                    holder.art_media_play.setVisibility(View.VISIBLE);
                }
                String url;
                if(status.getMedia_attachments().get(0).getPreview_url().endsWith("no-preview.png") ){
                    url = status.getMedia_attachments().get(0).getUrl();
                }else{
                    url = status.getMedia_attachments().get(0).getPreview_url();
                }
                Glide.with(holder.itemView.getContext())
                        .asBitmap()
                        .load(url)
                        .thumbnail(0.1f)
                        .into(holder.art_media);
            }

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



            holder.quick_reply_switch_to_full.setVisibility(View.GONE);
            holder.toot_space_left.setVisibility(View.GONE);
            holder.quick_reply_emoji.setVisibility(View.GONE);
            holder.quick_reply_privacy.setVisibility(View.GONE);

            holder.quick_reply_button.setOnClickListener(view -> {
                sendToot();
                //status.setShortReply(false);
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                assert imm != null;
                imm.hideSoftInputFromWindow(holder.quick_reply_button.getWindowToken(), 0);
                notifyStatusChanged(status);
            });

            holder.pf_description.setText(status.getContentSpan(), TextView.BufferType.SPANNABLE);
            holder.pf_date.setText(Helper.longDateToString(status.getCreated_at()));
            holder.quick_reply_text.setHint(R.string.leave_a_comment);
            holder.quick_reply_button.setText(R.string.post);
            holder.pf_comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean currentValue = status.isShortReply();
                    for (Status s : statuses) {
                        if (s.isShortReply() && !s.getId().equals(status.getId())) {
                            s.setShortReply(false);
                            notifyStatusChanged(s);
                        }
                    }
                    status.setShortReply(!currentValue);
                    if( !status.isShortReply()){
                        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                        assert imm != null;
                        imm.hideSoftInputFromWindow(holder.quick_reply_text.getWindowToken(), 0);
                    }
                    notifyStatusChanged(status);
                }
            });


            final View attached = holder.status_more;
            holder.status_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popup = new PopupMenu(context, attached);
                    final boolean isOwner = status.getReblog() != null ? status.getReblog().getAccount().getId().equals(userId) : status.getAccount().getId().equals(userId);
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
                        if (social != UpdateAccountInfoAsyncTask.SOCIAL.MASTODON && social != UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA) {
                            popup.getMenu().findItem(R.id.action_stats).setVisible(false);
                        }
                    } else {
                        popup.getMenu().findItem(R.id.action_stats).setVisible(false);
                        popup.getMenu().findItem(R.id.action_redraft).setVisible(false);
                        //popup.getMenu().findItem(R.id.action_mute_conversation).setVisible(false);
                        popup.getMenu().findItem(R.id.action_remove).setVisible(false);
                        //Same instance
                        if (status.getAccount().getAcct().split("@").length < 2) {
                            popup.getMenu().findItem(R.id.action_block_domain).setVisible(false);
                        }else{  //Both accounts have an instance in acct (pixelfed fix)
                            String instanceAccount = status.getAccount().getAcct().split("@")[1];
                            if( Helper.getLiveInstance(context).compareTo(instanceAccount) == 0){
                                popup.getMenu().findItem(R.id.action_block_domain).setVisible(false);
                            }
                        }
                        stringArrayConf = context.getResources().getStringArray(R.array.more_action_confirm);
                    }
                    //TODO: fix and display that feature
                    popup.getMenu().findItem(R.id.action_admin).setVisible(false);
                    popup.getMenu().findItem(R.id.action_custom_sharing).setVisible(false);
                    popup.getMenu().findItem(R.id.action_mention).setVisible(false);
                    popup.getMenu().findItem(R.id.action_copy).setVisible(false);
                    popup.getMenu().findItem(R.id.action_stats).setVisible(false);
                    popup.getMenu().findItem(R.id.action_translate).setVisible(false);
                    popup.getMenu().findItem(R.id.action_redraft).setVisible(false);

                    final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
                    int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
                    int style;
                    if (theme == Helper.THEME_DARK) {
                        style = R.style.DialogDark;
                    } else if (theme == Helper.THEME_BLACK) {
                        style = R.style.DialogBlack;
                    } else {
                        style = R.style.Dialog;
                    }

                    boolean custom_sharing = sharedpreferences.getBoolean(Helper.SET_CUSTOM_SHARING, false);
                    if (custom_sharing && status.getVisibility().equals("public"))
                        popup.getMenu().findItem(R.id.action_custom_sharing).setVisible(true);
                    if (status.isBookmarked())
                        popup.getMenu().findItem(R.id.action_bookmark).setTitle(R.string.bookmark_remove);
                    else
                        popup.getMenu().findItem(R.id.action_bookmark).setTitle(R.string.bookmark_add);
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
                                        builderInner.setMessage(Html.fromHtml(status.getContent()));
                                    break;
                                case R.id.action_schedule_boost:
                                    scheduleBoost(status);
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
                                    if (status.isMuted())
                                        doAction = API.StatusAction.UNMUTE_CONVERSATION;
                                    else
                                        doAction = API.StatusAction.MUTE_CONVERSATION;

                                    new PostActionAsyncTask(context, doAction, status.getId(), PixelfedListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
                                                pixelfedListAdapter.notifyItemRemoved(position);
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
                                    int translator = sharedpreferences.getInt(Helper.SET_TRANSLATOR, Helper.TRANS_YANDEX);
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
                                case R.id.action_copy_link:
                                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

                                    ClipData clip = ClipData.newPlainText(Helper.CLIP_BOARD, status.getReblog() != null ? status.getReblog().getUrl() : status.getUrl());
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
                                    boolean share_details = sharedpreferences.getBoolean(Helper.SET_SHARE_DETAILS, true);
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
                                        new PostActionAsyncTask(context, doAction, targetedId, PixelfedListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
                                                if (status.getPoll() != null) {
                                                    toot.setPoll(status.getPoll());
                                                } else if (status.getReblog() != null && status.getReblog().getPoll() != null) {
                                                    toot.setPoll(status.getPoll());
                                                }
                                                new RetrieveFeedsAsyncTask(context, RetrieveFeedsAsyncTask.Type.ONESTATUS, status.getIn_reply_to_id(), null, false, false, PixelfedListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                            } else {
                                                toot = new Status();
                                                toot.setSensitive(status.isSensitive());
                                                toot.setMedia_attachments(status.getMedia_attachments());
                                                if (status.getSpoiler_text() != null && status.getSpoiler_text().length() > 0)
                                                    toot.setSpoiler_text(status.getSpoiler_text().trim());
                                                toot.setVisibility(status.getVisibility());
                                                toot.setContent(status.getContent());
                                                if (status.getPoll() != null) {
                                                    toot.setPoll(status.getPoll());
                                                } else if (status.getReblog() != null && status.getReblog().getPoll() != null) {
                                                    toot.setPoll(status.getPoll());
                                                }
                                                final SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                                                long id = new StatusStoredDAO(context, db).insertStatus(toot, null);
                                                Intent intentToot = new Intent(context, PixelfedComposeActivity.class);
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
                                        new PostActionAsyncTask(context, doAction, status.getId(), status, comment, PixelfedListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                    } else {
                                        String targetedId;
                                        if (item.getItemId() == R.id.action_block_domain) {
                                            targetedId = status.getAccount().getAcct().split("@")[1];
                                        } else {
                                            targetedId = status.getAccount().getId();
                                        }
                                        new PostActionAsyncTask(context, doAction, targetedId, PixelfedListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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




            holder.pf_date.setOnClickListener(view ->{
                Intent intent = new Intent(context, ShowConversationActivity.class);
                Bundle b = new Bundle();
                if (status.getReblog() == null)
                    b.putParcelable("status", status);
                else
                    b.putParcelable("status", status.getReblog());
                intent.putExtras(b);
                context.startActivity(intent);
            });


            if (status.getDisplayNameSpan() != null && status.getDisplayNameSpan().toString().trim().length() > 0)
                holder.pf_username.setText(status.getDisplayNameSpan(), TextView.BufferType.SPANNABLE);
            else
                holder.pf_username.setText(status.getAccount().getUsername());
            theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);


            if (theme == Helper.THEME_BLACK) {
                holder.pf_fav.setInActiveImageTint(R.color.action_black);
                holder.pf_share.setInActiveImageTint(R.color.action_black);
                Helper.changeDrawableColor(context, R.drawable.ic_pixelfed_favorite_border, R.color.action_black);
                Helper.changeDrawableColor(context, holder.pf_comment, R.color.action_black);
                Helper.changeDrawableColor(context, holder.status_more, R.color.action_black);
                holder.pf_cardview.setCardBackgroundColor(ContextCompat.getColor(context, R.color.black_3));
            } else if (theme == Helper.THEME_DARK) {
                holder.pf_fav.setInActiveImageTint(R.color.action_dark);
                holder.pf_share.setInActiveImageTint(R.color.action_dark);
                Helper.changeDrawableColor(context, holder.pf_comment, R.color.action_dark);
                Helper.changeDrawableColor(context, holder.status_more, R.color.action_dark);
                Helper.changeDrawableColor(context, R.drawable.ic_pixelfed_favorite_border, R.color.action_dark);
                holder.pf_cardview.setCardBackgroundColor(ContextCompat.getColor(context, R.color.mastodonC1_));
            } else {
                holder.pf_fav.setInActiveImageTint(R.color.action_light);
                holder.pf_share.setInActiveImageTint(R.color.action_light);
                Helper.changeDrawableColor(context, holder.pf_comment, R.color.action_light);
                Helper.changeDrawableColor(context, holder.status_more, R.color.action_light);
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
            holder.pf_share.setActiveImage(R.drawable.ic_repeat_boost);
            holder.pf_share.setInactiveImage(R.drawable.ic_repeat_boost);
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


    private void translateToot(Status status) {
        //Manages translations
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        int trans = sharedpreferences.getInt(Helper.SET_TRANSLATOR, Helper.TRANS_YANDEX);
        MyTransL.translatorEngine et = MyTransL.translatorEngine.YANDEX;
        String api_key = null;


        if (trans == Helper.TRANS_YANDEX) {
            et = MyTransL.translatorEngine.YANDEX;
        } else if (trans == Helper.TRANS_DEEPL) {
            et = MyTransL.translatorEngine.DEEPL;
        }
        final MyTransL myTransL = MyTransL.getInstance(et);
        myTransL.setObfuscation(true);
        if (trans == Helper.TRANS_YANDEX) {
            api_key = sharedpreferences.getString(Helper.SET_YANDEX_API_KEY, Helper.YANDEX_KEY);
            myTransL.setYandexAPIKey(api_key);
        } else if (trans == Helper.TRANS_DEEPL) {
            api_key = sharedpreferences.getString(Helper.SET_DEEPL_API_KEY, "");
            myTransL.setDeeplAPIKey(api_key);
        }


        if (!status.isTranslated()) {
            String statusToTranslate;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                statusToTranslate = Html.fromHtml(status.getReblog() != null ? status.getReblog().getContent() : status.getContent(), Html.FROM_HTML_MODE_LEGACY).toString();
            else
                //noinspection deprecation
                statusToTranslate = Html.fromHtml(status.getReblog() != null ? status.getReblog().getContent() : status.getContent()).toString();
            //TODO: removes the replaceAll once fixed with the lib
            myTransL.translate(statusToTranslate, myTransL.getLocale(), new Results() {
                @Override
                public void onSuccess(Translate translate) {
                    if (translate.getTranslatedContent() != null) {
                        status.setTranslated(true);
                        status.setTranslationShown(true);
                        status.setContentTranslated(translate.getTranslatedContent());
                        Status.transformTranslation(context, status);
                        Status.makeEmojisTranslation(context, PixelfedListAdapter.this, status);
                        notifyStatusChanged(status);
                    } else {
                        Toasty.error(context, context.getString(R.string.toast_error_translate), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFail(HttpsConnectionException e) {
                    e.printStackTrace();
                    Toasty.error(context, context.getString(R.string.toast_error_translate), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            status.setTranslationShown(!status.isTranslationShown());
            notifyStatusChanged(status);
        }
    }


    private void tootInformation(Status status) {
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


    private void sendToot() {

        if (comment_content.getText() == null) {
            Toasty.error(context, context.getString(R.string.toast_error), Toast.LENGTH_LONG).show();
            return;
        }
        if (comment_content.getText().toString().trim().length() == 0) {
            Toasty.error(context, context.getString(R.string.toot_error_no_content), Toast.LENGTH_LONG).show();
            return;
        }
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        final String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = Helper.getLiveInstance(context);
        String tootContent;
        tootContent = comment_content.getText().toString().trim();
        Status toot = new Status();
        toot.setSensitive(false);
        final SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        Account account = new AccountDAO(context, db).getUniqAccount(userId, instance);
        toot.setVisibility(visibility);
        toot.setIn_reply_to_id(in_reply_to_status);
        toot.setContent(tootContent);
        new PostStatusAsyncTask(context, social, account, toot, PixelfedListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    @Override
    public void onPostAction(int statusCode, API.StatusAction statusAction, String targetedId, Error error) {

        if (error != null) {
            Toasty.error(context, error.getError(), Toast.LENGTH_LONG).show();
            return;
        }
        Helper.manageMessageStatusCode(context, statusCode, statusAction);
        //When muting or blocking an account, its status are removed from the list
        List<Status> statusesToRemove = new ArrayList<>();
        if (statusAction == API.StatusAction.MUTE || statusAction == API.StatusAction.BLOCK) {
            for (Status status : statuses) {
                if (status.getAccount().getId().equals(targetedId))
                    statusesToRemove.add(status);
            }
            statuses.removeAll(statusesToRemove);
            pixelfedListAdapter.notifyDataSetChanged();
        } else if (statusAction == API.StatusAction.UNSTATUS) {
            int position = 0;
            for (Status status : statuses) {
                if (status.getId().equals(targetedId)) {
                    statuses.remove(status);
                    pixelfedListAdapter.notifyItemRemoved(position);
                    SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                    //Remove the status from cache also
                    try {
                        new StatusCacheDAO(context, db).remove(StatusCacheDAO.ARCHIVE_CACHE, status);
                    } catch (Exception ignored) {
                    }
                    break;
                }
                position++;
            }
        } else if (statusAction == API.StatusAction.PIN || statusAction == API.StatusAction.UNPIN) {
            int position = 0;
            for (Status status : statuses) {
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
    }



    private void timedMuteAction(Status status) {
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
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
        View dialogView = inflater.inflate(R.layout.datetime_picker, new LinearLayout(context), false);
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
                    if (mutedAccount != null && !mutedAccount.contains(account.getId()))
                        mutedAccount.add(targeted_id);
                    else if (mutedAccount == null) {
                        mutedAccount = new ArrayList<>();
                        mutedAccount.add(targeted_id);
                    }
                    Toasty.success(context, context.getString(R.string.timed_mute_date, status.getAccount().getAcct(), Helper.dateToString(date_mute)), Toast.LENGTH_LONG).show();
                    alertDialog.dismiss();
                    send_delete_statuses(targeted_id);
                }
            }
        });
        alertDialog.show();
    }

    private void send_delete_statuses(String targetedId) {
        //Delete in the current timeline
        List<Status> statusesToRemove = new ArrayList<>();
        for (Status status : statuses) {
            if (status.getAccount().getId().equals(targetedId))
                statusesToRemove.add(status);
        }
        statuses.removeAll(statusesToRemove);
        pixelfedListAdapter.notifyDataSetChanged();
        //Send an intent to delete in every timelines
        Bundle b = new Bundle();
        b.putString("receive_action", targetedId);
        Intent intentBC = new Intent(Helper.RECEIVE_ACTION);
        intentBC.putExtras(b);
    }


    private void scheduleBoost(Status status) {

        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
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
        View dialogViewBoost = inflaterBoost.inflate(R.layout.datetime_picker, new LinearLayout(context), false);
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

    public void notifyStatusChanged(Status status) {
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

    public void notifyStatusWithActionChanged(Status status) {
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
        if (status != null) {
            if (!fromTranslation) {
                status.setEmojiFound(true);
            } else {
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