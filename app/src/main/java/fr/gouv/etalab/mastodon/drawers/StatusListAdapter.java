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

import android.Manifest;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.List;

import fr.gouv.etalab.mastodon.activities.MainActivity;
import fr.gouv.etalab.mastodon.activities.ShowConversationActivity;
import fr.gouv.etalab.mastodon.activities.TootActivity;
import fr.gouv.etalab.mastodon.client.Entities.Error;
import fr.gouv.etalab.mastodon.helper.Helper;
import mastodon.etalab.gouv.fr.mastodon.R;
import fr.gouv.etalab.mastodon.activities.ShowAccountActivity;
import fr.gouv.etalab.mastodon.asynctasks.PostActionAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveFeedsAsyncTask;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.Entities.Attachment;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.interfaces.OnPostActionInterface;

import static fr.gouv.etalab.mastodon.helper.Helper.EXTERNAL_STORAGE_REQUEST_CODE;


/**
 * Created by Thomas on 24/04/2017.
 * Adapter for Status
 */
public class StatusListAdapter extends BaseAdapter implements OnPostActionInterface {

    private Context context;
    private List<Status> statuses;
    private LayoutInflater layoutInflater;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private boolean isOnWifi;
    private int behaviorWithAttachments;
    private StatusListAdapter statusListAdapter;
    private final int REBLOG = 1;
    private final int FAVOURITE = 2;
    private ViewHolder holder;
    private RetrieveFeedsAsyncTask.Type type;

    public StatusListAdapter(Context context, RetrieveFeedsAsyncTask.Type type, boolean isOnWifi, int behaviorWithAttachments, List<Status> statuses){
        this.context = context;
        this.statuses = statuses;
        this.isOnWifi = isOnWifi;
        this.behaviorWithAttachments = behaviorWithAttachments;
        layoutInflater = LayoutInflater.from(this.context);
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder().displayer(new SimpleBitmapDisplayer()).cacheInMemory(false)
                .cacheOnDisk(true).resetViewBeforeLoading(true).build();
        statusListAdapter = this;
        this.type = type;
    }



    @Override
    public int getCount() {
        return statuses.size();
    }

    @Override
    public Object getItem(int position) {
        return statuses.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final Status status = statuses.get(position);
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.drawer_status, parent, false);
            holder = new ViewHolder();
            holder.status_document_container = (LinearLayout) convertView.findViewById(R.id.status_document_container);
            holder.status_content = (TextView) convertView.findViewById(R.id.status_content);
            holder.status_account_username = (TextView) convertView.findViewById(R.id.status_account_username);
            holder.status_account_displayname = (TextView) convertView.findViewById(R.id.status_account_displayname);
            holder.status_account_profile = (ImageView) convertView.findViewById(R.id.status_account_profile);
            holder.status_account_profile_boost = (ImageView) convertView.findViewById(R.id.status_account_profile_boost);
            holder.status_favorite_count = (TextView) convertView.findViewById(R.id.status_favorite_count);
            holder.status_reblog_count = (TextView) convertView.findViewById(R.id.status_reblog_count);
            holder.status_toot_date = (TextView) convertView.findViewById(R.id.status_toot_date);
            holder.status_show_more = (Button) convertView.findViewById(R.id.status_show_more);
            holder.status_more = (ImageView) convertView.findViewById(R.id.status_more);
            holder.status_reblog_user = (TextView) convertView.findViewById(R.id.status_reblog_user);
            holder.status_action_container = (LinearLayout) convertView.findViewById(R.id.status_action_container);
            holder.status_prev1 = (ImageView) convertView.findViewById(R.id.status_prev1);
            holder.status_prev2 = (ImageView) convertView.findViewById(R.id.status_prev2);
            holder.status_prev3 = (ImageView) convertView.findViewById(R.id.status_prev3);
            holder.status_prev4 = (ImageView) convertView.findViewById(R.id.status_prev4);
            holder.status_prev1_play = (ImageView) convertView.findViewById(R.id.status_prev1_play);
            holder.status_prev2_play = (ImageView) convertView.findViewById(R.id.status_prev2_play);
            holder.status_prev3_play = (ImageView) convertView.findViewById(R.id.status_prev3_play);
            holder.status_prev4_play = (ImageView) convertView.findViewById(R.id.status_prev4_play);
            holder.status_container2 = (LinearLayout) convertView.findViewById(R.id.status_container2);
            holder.status_container3 = (LinearLayout) convertView.findViewById(R.id.status_container3);
            holder.status_prev4_container = (RelativeLayout) convertView.findViewById(R.id.status_prev4_container);
            holder.status_reply = (ImageView) convertView.findViewById(R.id.status_reply);
            holder.status_privacy = (ImageView) convertView.findViewById(R.id.status_privacy);
            holder.main_container = (LinearLayout) convertView.findViewById(R.id.main_container);
            holder.status_spoiler_container = (LinearLayout) convertView.findViewById(R.id.status_spoiler_container);
            holder.status_content_container = (LinearLayout) convertView.findViewById(R.id.status_content_container);
            holder.status_spoiler = (TextView) convertView.findViewById(R.id.status_spoiler);
            holder.status_spoiler_button = (Button) convertView.findViewById(R.id.status_spoiler_button);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if( status.getSpoiler_text() != null && status.getSpoiler_text().trim().length() > 0 && !status.isSpoilerShown()){
            holder.status_content_container.setVisibility(View.GONE);
            holder.status_spoiler_container.setVisibility(View.VISIBLE);
            holder.status_spoiler_button.setVisibility(View.VISIBLE);
            holder.status_spoiler.setVisibility(View.VISIBLE);
        }else {
            holder.status_spoiler_button.setVisibility(View.GONE);
            holder.status_content_container.setVisibility(View.VISIBLE);
            if( status.getSpoiler_text() != null && status.getSpoiler_text().trim().length() > 0 )
                holder.status_spoiler_container.setVisibility(View.VISIBLE);
            else
                holder.status_spoiler_container.setVisibility(View.GONE);
        }
        if( status.getSpoiler_text() != null)
            holder.status_spoiler.setText(status.getSpoiler_text());
        //Spoiler opens
        holder.status_spoiler_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status.setSpoilerShown(true);
                holder.status_spoiler_button.setVisibility(View.GONE);
                statusListAdapter.notifyDataSetChanged();
            }
        });

        //Hides action bottom bar action when looking to status trough accounts
        if( type == RetrieveFeedsAsyncTask.Type.USER){
            holder.status_action_container.setVisibility(View.GONE);
        }
        final float scale = context.getResources().getDisplayMetrics().density;
        if( !status.getIn_reply_to_account_id().equals("null") || !status.getIn_reply_to_id().equals("null") ){
            Drawable img = ContextCompat.getDrawable(context, R.drawable.ic_reply);
            img.setBounds(0,0,(int) (20 * scale + 0.5f),(int) (15 * scale + 0.5f));
            holder.status_account_displayname.setCompoundDrawables( img, null, null, null);
        }else if( status.getReblog() != null){
            Drawable img = ContextCompat.getDrawable(context, R.drawable.ic_retweet_black);
            img.setBounds(0,0,(int) (20 * scale + 0.5f),(int) (15 * scale + 0.5f));
            holder.status_account_displayname.setCompoundDrawables( img, null, null, null);
        }else{
            holder.status_account_displayname.setCompoundDrawables( null, null, null, null);
        }
        //Click on a conversation
        if( type != RetrieveFeedsAsyncTask.Type.CONTEXT ){
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
        }else {
            if( position == ShowConversationActivity.position){
                holder.main_container.setBackgroundResource(R.color.blue_light);
            }else {
                holder.main_container.setBackgroundResource(R.color.white);
            }
        }

        final String content, displayName, username, ppurl;
        if( status.getReblog() != null){
            content = status.getReblog().getContent();
            displayName = Helper.shortnameToUnicode(status.getReblog().getAccount().getDisplay_name(), true);
            username = status.getReblog().getAccount().getUsername();
            holder.status_reblog_user.setText(displayName + " " +String.format("@%s",username));
            ppurl = status.getReblog().getAccount().getAvatar();
            holder.status_reblog_user.setVisibility(View.VISIBLE);
            holder.status_account_displayname.setText(context.getResources().getString(R.string.reblog_by, status.getAccount().getUsername()));
            holder.status_account_username.setText( "");
        }else {
            ppurl = status.getAccount().getAvatar();
            content = status.getContent();
            displayName = Helper.shortnameToUnicode(status.getAccount().getDisplay_name(), true);
            username = status.getAccount().getUsername();
            holder.status_reblog_user.setVisibility(View.GONE);
            holder.status_account_displayname.setText(displayName);
            holder.status_account_username.setText(String.format("@%s",username));
        }


        holder.status_reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, TootActivity.class);
                Bundle b = new Bundle();
                b.putParcelable("tootReply", status);
                intent.putExtras(b); //Put your id to your next Intent
                context.startActivity(intent);
            }
        });


        holder.status_content = Helper.clickableElements(context, holder.status_content,content,
                status.getReblog() != null?status.getReblog().getMentions():status.getMentions(),
                status.getReblog() != null?status.getReblog().getTags():status.getTags());

        holder.status_favorite_count.setText(String.valueOf(status.getFavourites_count()));
        holder.status_reblog_count.setText(String.valueOf(status.getReblogs_count()));
        holder.status_toot_date.setText(Helper.dateDiff(context, status.getCreated_at()));

        imageLoader.displayImage(ppurl, holder.status_account_profile, options);
        if( status.getReblog() != null) {
            imageLoader.displayImage(status.getAccount().getAvatar(), holder.status_account_profile_boost, options);
            holder.status_account_profile_boost.setVisibility(View.VISIBLE);
        }else{
            holder.status_account_profile_boost.setVisibility(View.GONE);
        }
        if( status.getReblog() == null) {
            if (status.getMedia_attachments().size() < 1) {
                holder.status_document_container.setVisibility(View.GONE);
                holder.status_show_more.setVisibility(View.GONE);
            } else {
                //If medias are loaded without any conditions or if device is on wifi
                if (!status.isSensitive() && (behaviorWithAttachments == Helper.ATTACHMENT_ALWAYS || (behaviorWithAttachments == Helper.ATTACHMENT_WIFI && isOnWifi))) {
                    loadAttachments(status);
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
                        loadAttachments(status);
                    }
                }
            }
        }else { //Attachments for reblogs
            if (status.getReblog().getMedia_attachments().size() < 1) {
                holder.status_document_container.setVisibility(View.GONE);
                holder.status_show_more.setVisibility(View.GONE);
            } else {
                //If medias are loaded without any conditions or if device is on wifi
                if (!status.getReblog().isSensitive() && (behaviorWithAttachments == Helper.ATTACHMENT_ALWAYS || (behaviorWithAttachments == Helper.ATTACHMENT_WIFI && isOnWifi))) {
                    loadAttachments(status.getReblog());
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
                        loadAttachments(status.getReblog());
                    }
                }
            }
        }

        switch (status.getVisibility()){
            case "public":
                holder.status_privacy.setImageResource(R.drawable.ic_action_globe);
                break;
            case "unlisted":
                holder.status_privacy.setImageResource(R.drawable.ic_action_lock_open);
                break;
            case "private":
                holder.status_privacy.setImageResource(R.drawable.ic_action_lock_closed);
                break;
            case "direct":
                holder.status_privacy.setImageResource(R.drawable.ic_local_post_office);
                break;
        }

        Drawable imgFav, imgReblog;
        if( status.isFavourited())
            imgFav = ContextCompat.getDrawable(context, R.drawable.ic_fav_yellow);
        else
            imgFav = ContextCompat.getDrawable(context, R.drawable.ic_fav_black);

        if( status.isReblogged())
            imgReblog = ContextCompat.getDrawable(context, R.drawable.ic_retweet_yellow);
        else
            imgReblog = ContextCompat.getDrawable(context, R.drawable.ic_retweet_black);

        imgFav.setBounds(0,0,(int) (20 * scale + 0.5f),(int) (20 * scale + 0.5f));
        imgReblog.setBounds(0,0,(int) (20 * scale + 0.5f),(int) (20 * scale + 0.5f));
        holder.status_favorite_count.setCompoundDrawables(imgFav, null, null, null);
        holder.status_reblog_count.setCompoundDrawables(imgReblog, null, null, null);

        holder.status_show_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadAttachments(status);
                holder.status_show_more.setVisibility(View.GONE);
                status.setAttachmentShown(true);
                statusListAdapter.notifyDataSetChanged();
            }
        });

        holder.status_favorite_count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                boolean confirmation = sharedpreferences.getBoolean(Helper.SET_NOTIF_VALIDATION, true);
                if( confirmation )
                    displayConfirmationDialog(FAVOURITE,status);
                else
                    favouriteAction(status);
            }
        });

        holder.status_reblog_count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                boolean confirmation = sharedpreferences.getBoolean(Helper.SET_NOTIF_VALIDATION, true);
                if( confirmation )
                    displayConfirmationDialog(REBLOG,status);
                else
                    favouriteAction(status);
            }
        });

        holder.status_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moreOptionDialog(status);
            }
        });


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




        //Profile picture
        return convertView;
    }

    /**
     * Favourites/Unfavourites a status
     * @param status Status
     */
    private void favouriteAction(Status status){
        if( status.isFavourited()){
            new PostActionAsyncTask(context, API.StatusAction.UNFAVOURITE, status.getId(), StatusListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            status.setFavourited(false);
        }else{
            new PostActionAsyncTask(context, API.StatusAction.FAVOURITE, status.getId(), StatusListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            status.setFavourited(true);
        }
        statusListAdapter.notifyDataSetChanged();
    }

    /**
     * Reblog/Unreblog a status
     * @param status Status
     */
    private void reblogAction(Status status){
        if( status.isReblogged()){
            new PostActionAsyncTask(context, API.StatusAction.UNREBLOG, status.getId(), StatusListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            status.setReblogged(false);
        }else{
            new PostActionAsyncTask(context, API.StatusAction.REBLOG, status.getId(), StatusListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            status.setReblogged(true);
        }
        statusListAdapter.notifyDataSetChanged();
    }



    private void loadAttachments(final Status status){
        List<Attachment> attachments = status.getMedia_attachments();
        if( attachments != null && attachments.size() > 0){
            int i = 0;
            if( attachments.size() == 1){
                holder.status_container2.setVisibility(View.GONE);
            }else if(attachments.size() == 2){
                holder.status_container2.setVisibility(View.VISIBLE);
                holder.status_container3.setVisibility(View.GONE);
            }else if( attachments.size() == 3){
                holder.status_container2.setVisibility(View.VISIBLE);
                holder.status_container3.setVisibility(View.VISIBLE);
                holder.status_prev4_container.setVisibility(View.GONE);
            }else {
                holder.status_prev4_container.setVisibility(View.VISIBLE);
            }
            for(final Attachment attachment: attachments){
                ImageView imageView;
                if( i == 0) {
                    imageView = holder.status_prev1;
                    if( attachment.getType().equals("image"))
                        holder.status_prev1_play.setVisibility(View.GONE);
                    else
                        holder.status_prev1_play.setVisibility(View.VISIBLE);
                }else if( i == 1) {
                    imageView = holder.status_prev2;
                    if( attachment.getType().equals("image"))
                        holder.status_prev2_play.setVisibility(View.GONE);
                    else
                        holder.status_prev2_play.setVisibility(View.VISIBLE);
                }else if(i == 2) {
                    imageView = holder.status_prev3;
                    if( attachment.getType().equals("image"))
                        holder.status_prev3_play.setVisibility(View.GONE);
                    else
                        holder.status_prev3_play.setVisibility(View.VISIBLE);
                }else {
                    imageView = holder.status_prev4;
                    if( attachment.getType().equals("image"))
                        holder.status_prev4_play.setVisibility(View.GONE);
                    else
                        holder.status_prev4_play.setVisibility(View.VISIBLE);
                }
                String url = attachment.getPreview_url();
                if( url == null || url.trim().equals(""))
                    url = attachment.getUrl();
                if( url.trim().contains("missing.png"))
                    continue;
                imageLoader.displayImage(url, imageView, options);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showPicture(attachment);
                    }
                });
                i++;
            }
            holder.status_document_container.setVisibility(View.VISIBLE);
        }else{
            holder.status_document_container.setVisibility(View.GONE);
        }
        holder.status_show_more.setVisibility(View.GONE);
    }

    private void showPicture(final Attachment attachment) {

        final AlertDialog.Builder alertadd = new AlertDialog.Builder(context);
        LayoutInflater factory = LayoutInflater.from(context);
        final View view = factory.inflate(R.layout.show_attachment, null);
        alertadd.setView(view);
        final RelativeLayout loader = (RelativeLayout) view.findViewById(R.id.loader);
        switch (attachment.getType()){
            case "image": {
                String url = attachment.getPreview_url();
                if(url == null || url.trim().equals(""))
                    url = attachment.getUrl();
                final ImageView imageView = (ImageView) view.findViewById(R.id.dialog_imageview);
                imageLoader.displayImage(url, imageView, options, new SimpleImageLoadingListener(){
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        super.onLoadingComplete(imageUri, view, loadedImage);
                        loader.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);
                    }
                    @Override
                    public void onLoadingFailed(java.lang.String imageUri, android.view.View view, FailReason failReason){
                        imageLoader.displayImage(attachment.getPreview_url(), imageView, options);
                        loader.setVisibility(View.GONE);
                    }
                });
                break;
            }
            case "gifv":
            case "video": {
                if( attachment.getRemote_url().contains(".gif") ){
                    view.findViewById(R.id.dialog_webview_container).setVisibility(View.VISIBLE);
                    WebView webView = (WebView) view.findViewById(R.id.dialog_webview);
                    webView.getSettings().setJavaScriptEnabled(false);
                    webView.clearCache(false);
                    webView.setScrollbarFadingEnabled(true);
                    webView.getSettings().setBuiltInZoomControls(false);
                    webView.getSettings().setSupportZoom(false);
                    webView.getSettings().setUseWideViewPort(false);
                    webView.setVerticalScrollBarEnabled(false);
                    webView.setHorizontalScrollBarEnabled(false);
                    webView.setInitialScale(0);
                    String url = attachment.getRemote_url();
                    if(url == null || url.trim().equals(""))
                        url = attachment.getUrl();
                    webView.loadUrl(url);
                    loader.setVisibility(View.GONE);
                }else {
                    String url = attachment.getRemote_url();
                    if(url == null || url.trim().equals(""))
                        url = attachment.getUrl();
                    Uri uri = Uri.parse(url);
                    VideoView videoView = (VideoView) view.findViewById(R.id.dialog_videoview);
                    videoView.setVisibility(View.VISIBLE);
                    videoView.setVideoURI(uri);
                    videoView.start();
                    videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            loader.setVisibility(View.GONE);
                        }
                    });
                }

                break;
            }
        }
        String urlDownload = attachment.getRemote_url();
        if( urlDownload == null || urlDownload.trim().equals(""))
            urlDownload = attachment.getUrl();
        final String finalUrlDownload = urlDownload;
        alertadd.setPositiveButton(R.string.download, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dlg, int sumthin) {
                if(Build.VERSION.SDK_INT >= 23 ){
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {
                        ActivityCompat.requestPermissions((MainActivity)context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_REQUEST_CODE);
                    } else {

                        Helper.manageDownloads(context, finalUrlDownload);
                    }
                }else{
                    Helper.manageDownloads(context, finalUrlDownload);
                }
                dlg.dismiss();
            }
        });
        alertadd.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dlg, int sumthin) {
                dlg.dismiss();
            }
        });

        alertadd.show();
    }

    @Override
    public void onPostAction(int statusCode, API.StatusAction statusAction, String targetedId, Error error) {

        if( error != null){
            final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
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
    }



    private class ViewHolder {
        LinearLayout status_content_container;
        LinearLayout status_spoiler_container;
        TextView status_spoiler;
        Button status_spoiler_button;

        TextView status_content;
        TextView status_account_username;
        TextView status_account_displayname;
        ImageView status_account_profile;
        ImageView status_account_profile_boost;
        TextView status_favorite_count;
        TextView status_reblog_count;
        TextView status_toot_date;
        TextView status_reblog_user;
        Button status_show_more;
        ImageView status_more;
        LinearLayout status_action_container;
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
        ImageView status_reply;
        ImageView status_privacy;
        LinearLayout status_container2;
        LinearLayout status_container3;
        LinearLayout main_container;
    }


    /**
     * Display a validation message
     * @param action int
     * @param status Status
     */
    private void displayConfirmationDialog(final int action, final Status status){

        String title = null;
        if( action == FAVOURITE){
            if( status.isFavourited())
                title = context.getString(R.string.favourite_remove);
            else
                title = context.getString(R.string.favourite_add);
        }else if( action == REBLOG ){
            if( status.isReblogged())
                title = context.getString(R.string.reblog_remove);
            else
                title = context.getString(R.string.reblog_add);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            builder.setMessage(Html.fromHtml(status.getContent(), Html.FROM_HTML_MODE_COMPACT));
        else
            //noinspection deprecation
            builder.setMessage(Html.fromHtml(status.getContent()));
        builder.setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(title)
            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if( action == REBLOG)
                        reblogAction(status);
                    else if( action == FAVOURITE)
                        favouriteAction(status);
                    dialog.dismiss();
                }
            })
            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }

            })
            .show();
    }

    /**
     * More option for status (report / remove status / Mute / Block)
     * @param status Status current status
     */
    private void moreOptionDialog(final Status status){


        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        final boolean isOwner = status.getAccount().getId().equals(userId);
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);
        builderSingle.setTitle(R.string.make_a_choice);
        final String[] stringArray, stringArrayConf;
        final API.StatusAction[] doAction;
        if( isOwner) {
            stringArray = context.getResources().getStringArray(R.array.more_action_owner);
            stringArrayConf = context.getResources().getStringArray(R.array.more_action_owner_confirm);
            doAction = new API.StatusAction[]{API.StatusAction.UNSTATUS};

        }else {
            stringArray = context.getResources().getStringArray(R.array.more_action);
            stringArrayConf = context.getResources().getStringArray(R.array.more_action_confirm);
            doAction = new API.StatusAction[]{API.StatusAction.MUTE,API.StatusAction.BLOCK,API.StatusAction.REPORT};
        }
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, stringArray);
        builderSingle.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AlertDialog.Builder builderInner = new AlertDialog.Builder(context);
                builderInner.setTitle(stringArrayConf[which]);
                if( isOwner) {
                    if( which == 0) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            builderInner.setMessage(Html.fromHtml(status.getContent(), Html.FROM_HTML_MODE_COMPACT));
                        else
                            //noinspection deprecation
                            builderInner.setMessage(Html.fromHtml(status.getContent()));
                    }else{
                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        String content;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            content = Html.fromHtml(status.getContent(), Html.FROM_HTML_MODE_COMPACT).toString();
                        else
                            //noinspection deprecation
                            content = Html.fromHtml(status.getContent()).toString();
                        ClipData clip = ClipData.newPlainText(Helper.CLIP_BOARD, content);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(context,R.string.clipboard,Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        return;
                    }
                }else {
                    if( which < 2 ){
                        builderInner.setMessage(status.getAccount().getAcct());
                    }else if( which < 3) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            builderInner.setMessage(Html.fromHtml(status.getContent(), Html.FROM_HTML_MODE_COMPACT));
                        else
                            //noinspection deprecation
                            builderInner.setMessage(Html.fromHtml(status.getContent()));
                    }else{
                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        String content;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            content = Html.fromHtml(status.getContent(), Html.FROM_HTML_MODE_COMPACT).toString();
                        else
                            //noinspection deprecation
                            content = Html.fromHtml(status.getContent()).toString();
                        ClipData clip = ClipData.newPlainText(Helper.CLIP_BOARD, content);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(context,R.string.clipboard,Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        return;
                    }
                }
                //Text for report
                EditText input = null;
                final int position = which;
                if( doAction[which] == API.StatusAction.REPORT){
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
                        API.StatusAction statusAction = doAction[position];
                        if(statusAction ==  API.StatusAction.REPORT || statusAction == API.StatusAction.CREATESTATUS){
                            String comment = null;
                            if( finalInput != null && finalInput.getText() != null)
                                comment = finalInput.getText().toString();
                            new PostActionAsyncTask(context, statusAction, status.getId(), status, comment, StatusListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }else{
                            String targetedId;
                            if( doAction[position] == API.StatusAction.FAVOURITE ||
                                doAction[position] == API.StatusAction.UNFAVOURITE ||
                                doAction[position] == API.StatusAction.REBLOG ||
                                doAction[position] == API.StatusAction.UNREBLOG ||
                                doAction[position] == API.StatusAction.UNSTATUS
                            )
                                targetedId = status.getId();
                            else
                                targetedId = status.getAccount().getId();
                            new PostActionAsyncTask(context, statusAction, targetedId, StatusListAdapter.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                        dialog.dismiss();
                    }
                });
                builderInner.show();
            }
        });
        builderSingle.show();
    }
}