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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputType;
import android.text.TextWatcher;
import android.util.Half;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
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
import com.smarteist.autoimageslider.IndicatorAnimations;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;
import com.varunest.sparkbutton.SparkButton;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import app.fedilab.android.activities.TootActivity;
import app.fedilab.android.asynctasks.PostStatusAsyncTask;
import app.fedilab.android.asynctasks.RetrieveContextAsyncTask;
import app.fedilab.android.client.API;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Attachment;
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
import app.fedilab.android.sqlite.AccountDAO;
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

import static android.content.Context.MODE_PRIVATE;
import static app.fedilab.android.activities.BaseMainActivity.social;
import static app.fedilab.android.helper.Helper.changeDrawableColor;


/**
 * Created by Thomas on 14/01/2019.
 * Adapter for pixelfed drawer
 */
public class PixelfedListAdapter extends RecyclerView.Adapter implements OnPostActionInterface, OnRetrieveEmojiInterface, OnRetrieveFeedsInterface, OnPostStatusActionInterface, OnRetrieveSearchInterface, OnRetrieveSearcAccountshInterface, OnRetrieveContextInterface {

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
    public void onRetrieveContext(app.fedilab.android.client.Entities.Context context, Error error) {
        if (context == null || context.getDescendants().size() == 0) {
            return;
        }
        List<Status> modifiedStatus = context.getDescendants();
        notifyStatusChanged(modifiedStatus.get(0));
    }



    @Override
    public void onRetrieveFeeds(APIResponse apiResponse) {
        if (apiResponse.getError() != null) {
            if (apiResponse.getError().getError() != null)
                Toasty.error(context, apiResponse.getError().getError(), Toast.LENGTH_LONG).show();
            else
                Toasty.error(context, context.getString(R.string.toast_error), Toast.LENGTH_LONG).show();
            return;
        }
        List<Status> statuses;
        String targetedId = apiResponse.getTargetedId();
        if (apiResponse.getResults() != null && apiResponse.getResults().getStatuses() != null)
            statuses = apiResponse.getResults().getStatuses();
        else
            statuses = apiResponse.getStatuses();
        for(Status tl: this.statuses){
            if( tl.getId().equals(targetedId)){
                tl.setComments(statuses);
                tl.setCommentsShown(true);
                notifyStatusChanged(tl);
                break;
            }
        }

    }

    @Override
    public void onPostStatusAction(APIResponse apiResponse) {
        if (apiResponse.getError() != null) {
            if (apiResponse.getError().getError().contains("422")) {
                Toasty.error(context, context.getString(R.string.toast_error_char_limit), Toast.LENGTH_SHORT).show();
                return;
            } else {
                Toasty.error(context, apiResponse.getError().getError(), Toast.LENGTH_SHORT).show();
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
        //For conversation
        if (context instanceof ShowConversationActivity) {
            ((ShowConversationActivity) context).addStatuses(apiResponse.getStatuses().get(0));
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
        ImageView pf_pp, pf_comment;
        SparkButton pf_fav, pf_share;
        TextView pf_username, pf_likes, pf_description, pf_date;
        CardView pf_cardview;
        LinearLayout pf_bottom_container;
        FrameLayout pixelfed_comments;
        RecyclerView lv_comments;

        ConstraintLayout quick_reply_container;
        MastalabAutoCompleteTextView quick_reply_text;
        ImageView quick_reply_switch_to_full;
        TextView toot_space_left;
        ImageView quick_reply_emoji;
        Button quick_reply_button;
        ImageView quick_reply_privacy;

        ViewHolderPixelfed(View itemView) {
            super(itemView);
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


            if( status.isCommentsShown()){
                holder.pixelfed_comments.setVisibility(View.VISIBLE);
                StatusListAdapter statusListAdapter = new StatusListAdapter(RetrieveFeedsAsyncTask.Type.PUBLIC,null, false, status.getComments());
                holder.lv_comments.setAdapter(statusListAdapter);
            }else{
                holder.pixelfed_comments.setVisibility(View.GONE);
            }
            if (status.getMedia_attachments() != null && status.getMedia_attachments().size() > 0){
                SliderAdapter sliderAdapter = new SliderAdapter(new WeakReference<>(context), false, status.getMedia_attachments());
                holder.imageSlider.setSliderAdapter(sliderAdapter);
                holder.imageSlider.setIndicatorAnimation(IndicatorAnimations.WORM);
                holder.imageSlider.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);

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
                sendToot(null);
                status.setShortReply(false);
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                assert imm != null;
                imm.hideSoftInputFromWindow(holder.quick_reply_button.getWindowToken(), 0);
            });
            theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
            holder.quick_reply_container.setVisibility(View.VISIBLE);
            holder.pf_description.setText(status.getContentSpan(), TextView.BufferType.SPANNABLE);
            holder.pf_date.setText(Helper.dateToString(status.getCreated_at()));
            holder.quick_reply_text.setHint(R.string.leave_a_comment);
            holder.quick_reply_button.setText(R.string.post);
            holder.pf_comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    status.setCommentsShown(!status.isCommentsShown());
                    in_reply_to_status = status.getReblog() != null ? status.getReblog().getId() : status.getId();
                    if (MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.PIXELFED) {
                        CrossActions.doCrossConversation(context, status);
                    } else {
                        if( status.isCommentsFetched()){
                            notifyStatusChanged(status);
                        }else{
                            status.setCommentsFetched(true);
                            new RetrieveContextAsyncTask(context, false, false, status.getId(),PixelfedListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                    }

                    if ( status.isCommentsShown()) {
                        holder.quick_reply_text.requestFocus();
                        EditText content_cw = new EditText(context);
                        content_cw.setText(status.getReblog() != null ? status.getReblog().getSpoiler_text() : status.getSpoiler_text());
                        TootActivity.manageMentions(context, social, userId,
                                holder.quick_reply_text, content_cw, holder.toot_space_left, status.getReblog() != null ? status.getReblog() : status);
                        TextWatcher textWatcher = TootActivity.initializeTextWatcher(context, social, holder.quick_reply_text, content_cw, holder.toot_space_left, null, null, PixelfedListAdapter.this, PixelfedListAdapter.this, PixelfedListAdapter.this);

                        comment_content = holder.quick_reply_text;
                        int newInputType = comment_content.getInputType() & (comment_content.getInputType() ^ InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
                        comment_content.setInputType(newInputType);
                        in_reply_to_status = status.getReblog() != null ? status.getReblog().getId() : status.getId();
                        tootReply = status;
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
                        holder.quick_reply_text.addTextChangedListener(textWatcher);

                    }
                    notifyStatusChanged(status);
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


    private void sendToot(String content_type) {

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
        if (content_type != null)
            toot.setContentType(content_type);
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

        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
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
        if (statusAction == API.StatusAction.PEERTUBEDELETECOMMENT) {
            int position = 0;
            for (Status status : statuses) {
                if (status.getId().equals(targetedId)) {
                    statuses.remove(status);
                    pixelfedListAdapter.notifyItemRemoved(position);
                    break;
                }
                position++;
            }
        }
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