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
package fr.gouv.etalab.mastodon.activities;


import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
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

import java.util.Set;

import es.dmoral.toasty.Toasty;
import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.asynctasks.CustomSharingAsyncTask;
import fr.gouv.etalab.mastodon.client.CustomSharingResponse;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnCustomSharingInterface;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;

import static fr.gouv.etalab.mastodon.helper.Helper.THEME_LIGHT;


/**
 * Created by Curtis on 13/02/2019.
 * Share status metadata to remote content aggregators
 */

public class CustomSharingActivity extends BaseActivity implements OnCustomSharingInterface {

    private EditText set_custom_sharing_title, set_custom_sharing_description, set_custom_sharing_keywords;
    private Button set_custom_sharing_save;
    private ImageView pp_actionBar;
    private String title, description, keywords, custom_sharing_url, encodedCustomSharingURL;
    private String bundle_url, bundle_source, bundle_id, bundle_tags, bundle_content;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
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
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE);
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
            if (theme == THEME_LIGHT){
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
        if(b != null) {
            bundle_url = b.getString("url");
            bundle_id = b.getString("id");
            bundle_source = b.getString("source");
            bundle_tags = b.getString("tags");
            bundle_content = b.getString("content");
        }
        set_custom_sharing_title = findViewById(R.id.set_custom_sharing_title);
        set_custom_sharing_description = findViewById(R.id.set_custom_sharing_description);
        set_custom_sharing_keywords = findViewById(R.id.set_custom_sharing_keywords);
        set_custom_sharing_title.setEllipsize(TextUtils.TruncateAt.END);
        //set text on title, description, and keywords
        System.out.println("Content: " + bundle_content);
        String[] lines = bundle_content.split("\n");
        String newTitle = "";
        if (lines[0].length() > 60) {
            newTitle = lines[0].substring(0, 60) + '…';;
        } else {
            newTitle = lines[0];
        }
        set_custom_sharing_title.setText(newTitle);
        set_custom_sharing_description.setText(bundle_content);
        set_custom_sharing_keywords.setText(bundle_tags);
        set_custom_sharing_save = findViewById(R.id.set_custom_sharing_save);
        set_custom_sharing_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // obtain title, description, keywords
                title = set_custom_sharing_title.getText().toString();
                description = set_custom_sharing_description.getText().toString();
                keywords = set_custom_sharing_keywords.getText().toString();
                CharSequence comma_only = ",";
                CharSequence space_only = " ";
                CharSequence double_space = "  ";
                keywords = keywords.replace(comma_only,space_only);
                keywords = keywords.replace(double_space,space_only);
                // Create encodedCustomSharingURL
                custom_sharing_url = sharedpreferences.getString(Helper.SET_CUSTOM_SHARING_URL,
                        "http://example.net/add?user=fedilab&url=${url}&title=${title}&source=${source}&id=${id}&description=${description}&keywords=${keywords}");
                encodedCustomSharingURL = encodeCustomSharingURL(custom_sharing_url, bundle_url, bundle_id, bundle_source, title, description, keywords);
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

    public String encodeCustomSharingURL(String custom_sharing_url, String bundle_url, String bundle_id, String bundle_source, String title, String description, String keywords) {
        String url_user = "";
        String url_param_url = "";
        String url_param_title = "";
        String url_param_source = "";
        String url_param_id = "";
        String url_param_description = "";
        String url_param_keywords = "";
        Uri uri = Uri.parse(custom_sharing_url);
        String protocol = uri.getScheme();
        String server = uri.getAuthority();
        String path = uri.getPath();
        if (path != null) {
            path = path.replaceAll("/", "");
        }
        Set<String> args = uri.getQueryParameterNames();
        for (String param_name : args) {
            if (param_name.equals("user")) {
                url_user = uri.getQueryParameter("user");
            }
            String param_value = uri.getQueryParameter(param_name);
            switch(param_value) {
                case "${url}":
                    url_param_url = param_name;
                    break;
                case "${title}":
                    url_param_title = param_name;
                    break;
                case "${source}":
                    url_param_source = param_name;
                    break;
                case "${id}":
                    url_param_id = param_name;
                    break;
                case "${description}":
                    url_param_description = param_name;
                    break;
                case "${keywords}":
                    url_param_keywords = param_name;
                    break;
            }
        }
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(protocol)
                .authority(server)
                .appendPath(path);
        if (!url_user.equals("")) {
            builder.appendQueryParameter("user", url_user);
        }
        if (!url_param_url.equals("")) {
            builder.appendQueryParameter(url_param_url, bundle_url);
        }
        if (!url_param_title.equals("")) {
            builder.appendQueryParameter(url_param_title, title);
        }
        if (!url_param_source.equals("")) {
            builder.appendQueryParameter(url_param_source, bundle_source);
        }
        if (!url_param_id.equals("")) {
            builder.appendQueryParameter(url_param_id, bundle_id);
        }
        if (!url_param_description.equals("")) {
            builder.appendQueryParameter(url_param_description, description);
        }
        if (!url_param_keywords.equals("")) {
            builder.appendQueryParameter(url_param_keywords, keywords);
        }
        return builder.build().toString();
    }
}
