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
package app.fedilab.android.activities;


import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import app.fedilab.android.client.CustomSharingResponse;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Attachment;
import app.fedilab.android.client.Entities.Emojis;
import app.fedilab.android.client.Entities.Status;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.sqlite.AccountDAO;
import app.fedilab.android.sqlite.Sqlite;
import es.dmoral.toasty.Toasty;
import app.fedilab.android.R;
import app.fedilab.android.asynctasks.CustomSharingAsyncTask;
import app.fedilab.android.interfaces.OnCustomSharingInterface;


/**
 * Created by Curtis on 13/02/2019.
 * Share status metadata to remote content aggregators
 */

public class CustomSharingActivity extends BaseActivity implements OnCustomSharingInterface {

    private EditText set_custom_sharing_title, set_custom_sharing_keywords;
    private Button set_custom_sharing_save;
    private ImageView pp_actionBar;
    private String title, keywords, custom_sharing_url, encodedCustomSharingURL;
    private String bundle_url;
    private String bundle_source;
    private String bundle_id;
    private String bundle_content;
    private String bundle_thumbnailurl;
    private String bundle_creator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        switch (theme){
            case Helper.THEME_LIGHT:
                setTheme(R.style.AppTheme);
                break;
            case Helper.THEME_DARK:
                setTheme(R.style.AppThemeDark);
                break;
            case Helper.THEME_BLACK:
                setTheme(R.style.AppThemeBlack);
                break;
            default:
                setTheme(R.style.AppThemeDark);
        }

        setContentView(R.layout.activity_custom_sharing);

        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.simple_action_bar, null);
            actionBar.setCustomView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            TextView title = actionBar.getCustomView().findViewById(R.id.toolbar_title);
            pp_actionBar = actionBar.getCustomView().findViewById(R.id.pp_actionBar);
            title.setText(R.string.settings_title_custom_sharing);
            ImageView close_conversation = actionBar.getCustomView().findViewById(R.id.close_conversation);
            if( close_conversation != null){
                close_conversation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
            }
            if (theme == Helper.THEME_LIGHT){
                Toolbar toolbar = actionBar.getCustomView().findViewById(R.id.toolbar);
                Helper.colorizeToolbar(toolbar, R.color.black, CustomSharingActivity.this);
            }
        }else{
            setTitle(R.string.settings_title_custom_sharing);
        }
        SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        Account account = new AccountDAO(getApplicationContext(),db).getAccountByID(userId);
        String url = account.getAvatar();
        if( url.startsWith("/") ){
            url = Helper.getLiveInstanceWithProtocol(getApplicationContext()) + account.getAvatar();
        }

        Helper.loadGiF(getApplicationContext(), url, pp_actionBar);
        Bundle b = getIntent().getExtras();
        Status status = null;
        if(b != null) {
            status = b.getParcelable("status");
        }
        if( status == null){
            finish();
            return;
        }

        bundle_creator = status.getAccount().getAcct();
        bundle_url = status.getUrl();
        bundle_id = status.getUri();
        bundle_source = status.getAccount().getUrl();
        String bundle_tags = status.getTagsString();
        bundle_content = formatedContent(status.getContent(), status.getEmojis());
        if( status.getCard() != null && status.getCard().getImage() != null) {
            bundle_thumbnailurl = status.getCard().getImage();
        } else if (status.getMedia_attachments() != null && status.getMedia_attachments().size() > 0) {
            ArrayList<Attachment> mediaAttachments = status.getMedia_attachments();
            Attachment firstAttachment = mediaAttachments.get(0);
            bundle_thumbnailurl = firstAttachment.getPreview_url();
        } else {
            bundle_thumbnailurl = status.getAccount().getAvatar();
        }
        if (!bundle_creator.contains("@")) {
            bundle_creator = bundle_creator + "@" + account.getInstance();
        }
        set_custom_sharing_title = findViewById(R.id.set_custom_sharing_title);
        TextView set_custom_sharing_description = findViewById(R.id.set_custom_sharing_description);
        set_custom_sharing_keywords = findViewById(R.id.set_custom_sharing_keywords);
        set_custom_sharing_title.setEllipsize(TextUtils.TruncateAt.END);
        //set text on title, description, and keywords
        String[] lines = bundle_content.split("\n");
        //Remove tags in title
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            lines[0] = Html.fromHtml( lines[0], Html.FROM_HTML_MODE_LEGACY).toString();
        else
            //noinspection deprecation
            lines[0] = Html.fromHtml(lines[0]).toString();
        String newTitle = "";
        if (lines[0].length() > 60) {
            newTitle = lines[0].substring(0, 60) + '…';
        } else {
            newTitle = lines[0];
        }
        set_custom_sharing_title.setText(newTitle);
        String newDescription = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            newDescription = Html.fromHtml( bundle_content, Html.FROM_HTML_MODE_LEGACY).toString();
        else
            //noinspection deprecation
            newDescription = Html.fromHtml(bundle_content).toString();

        set_custom_sharing_description.setText(newDescription);
        set_custom_sharing_keywords.setText(bundle_tags);
        set_custom_sharing_save = findViewById(R.id.set_custom_sharing_save);
        set_custom_sharing_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // obtain title, description, keywords
                title = set_custom_sharing_title.getText().toString();
                keywords = set_custom_sharing_keywords.getText().toString();
                CharSequence comma_only = ",";
                CharSequence space_only = " ";
                CharSequence double_space = "  ";
                keywords = keywords.replace(comma_only,space_only);
                keywords = keywords.replace(double_space,space_only);
                // Create encodedCustomSharingURL
                custom_sharing_url = sharedpreferences.getString(Helper.SET_CUSTOM_SHARING_URL,
                        "http://example.net/add?token=YOUR_TOKEN&url=${url}&title=${title}" +
                                "&source=${source}&id=${id}&description=${description}&keywords=${keywords}&creator=${creator}&thumbnailurl=${thumbnailurl}");
                encodedCustomSharingURL = encodeCustomSharingURL();
                new CustomSharingAsyncTask(getApplicationContext(), encodedCustomSharingURL, CustomSharingActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCustomSharing(CustomSharingResponse customSharingResponse) {
        set_custom_sharing_save.setEnabled(true);
        if( customSharingResponse.getError() != null){
            Toasty.error(getApplicationContext(), getString(R.string.toast_error), Toast.LENGTH_LONG).show();
            return;
        }
        String response = customSharingResponse.getResponse();
        Toasty.success(getApplicationContext(), response, Toast.LENGTH_LONG).show();
        finish();
    }

    public String encodeCustomSharingURL() {
        Uri uri = Uri.parse(custom_sharing_url);
        String protocol = uri.getScheme();
        String server = uri.getAuthority();
        String path = uri.getPath();
        if (path != null) {
            path = path.replaceAll("/", "");
        }
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(protocol)
                .authority(server)
                .appendPath(path);
        Set<String> args = uri.getQueryParameterNames();
        boolean paramFound;
        for (String param_name : args) {
            paramFound = false;
            String param_value = uri.getQueryParameter(param_name);
            if(param_value != null)
                switch(param_value) {
                    case "${url}":
                        paramFound = true;
                        builder.appendQueryParameter(param_name, bundle_url);
                        break;
                    case "${title}":
                        paramFound = true;
                        builder.appendQueryParameter(param_name, title);
                        break;
                    case "${source}":
                        paramFound = true;
                        builder.appendQueryParameter(param_name, bundle_source);
                        break;
                    case "${id}":
                        paramFound = true;
                        builder.appendQueryParameter(param_name, bundle_id);
                        break;
                    case "${description}":
                        paramFound = true;
                        builder.appendQueryParameter(param_name, bundle_content);
                        break;
                    case "${keywords}":
                        paramFound = true;
                        builder.appendQueryParameter(param_name, keywords);
                        break;
                    case "${creator}":
                        paramFound = true;
                        builder.appendQueryParameter(param_name, bundle_creator);
                        break;
                    case "${thumbnailurl}":
                        paramFound = true;
                        builder.appendQueryParameter(param_name, bundle_thumbnailurl);
                        break;
                }
            if (!paramFound) {
                builder.appendQueryParameter(param_name, param_value);
            }
        }
        return builder.build().toString();
    }


    private String formatedContent(String content, List<Emojis> emojis){
        //Avoid null content
        if( content == null)
            return "";
        if( emojis == null || emojis.size() == 0)
            return content;
        for (Emojis emoji : emojis) {
            content = content.replaceAll(":"+emoji.getShortcode()+":","<img src='"+emoji.getUrl()+"' width=20 alt='"+emoji.getShortcode()+"'/>");
        }
        return content;
    }
}
