package fr.gouv.etalab.mastodon.activities;
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


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import es.dmoral.toasty.Toasty;
import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.asynctasks.RetrievePeertubeChannelsAsyncTask;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrievePeertubeInterface;

import static fr.gouv.etalab.mastodon.helper.Helper.THEME_LIGHT;

public class PeertubeUploadActivity extends BaseActivity implements OnRetrievePeertubeInterface {


    private final int PICK_IVDEO = 52378;
    private Button set_upload_file, set_upload_submit;
    private Spinner set_upload_privacy, set_upload_channel;
    private TextView set_upload_file_name;
    private HashMap<String, String> channels;
    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 724;

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
        if( getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null ) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.simple_bar, null);
            actionBar.setCustomView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            ImageView toolbar_close = actionBar.getCustomView().findViewById(R.id.toolbar_close);
            TextView toolbar_title = actionBar.getCustomView().findViewById(R.id.toolbar_title);
            toolbar_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            toolbar_title.setText(R.string.action_about);
            if (theme == THEME_LIGHT){
                Toolbar toolbar = actionBar.getCustomView().findViewById(R.id.toolbar);
                Helper.colorizeToolbar(toolbar, R.color.black, PeertubeUploadActivity.this);
            }
        }
        setContentView(R.layout.activity_peertube_upload);

        set_upload_file = findViewById(R.id.set_upload_file);
        set_upload_file_name = findViewById(R.id.set_upload_file_name);
        set_upload_channel = findViewById(R.id.set_upload_channel);
        set_upload_privacy = findViewById(R.id.set_upload_privacy);
        set_upload_submit = findViewById(R.id.set_upload_submit);

        new RetrievePeertubeChannelsAsyncTask(PeertubeUploadActivity.this, PeertubeUploadActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        channels = new HashMap<>();
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IVDEO && resultCode == Activity.RESULT_OK) {
            if (data == null || data.getData() == null) {
                Toasty.error(getApplicationContext(),getString(R.string.toot_select_image_error),Toast.LENGTH_LONG).show();
                return;
            }
            set_upload_submit.setEnabled(true);

            Uri uri = data.getData();
            String uriString = uri.toString();
            File myFile = new File(uriString);
            String filename = null;
            if (uriString.startsWith("content://")) {
                Cursor cursor = null;
                try {
                    cursor = getContentResolver().query(uri, null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        filename = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                } finally {
                    assert cursor != null;
                    cursor.close();
                }
            } else if (uriString.startsWith("file://")) {
                filename = myFile.getName();
            }
            if( filename != null) {
                set_upload_file_name.setVisibility(View.VISIBLE);
                set_upload_file_name.setText(filename);
            }

        }
    }

    @Override
    public void onRetrievePeertube(APIResponse apiResponse) {
        if( apiResponse.getError() != null || apiResponse.getAccounts() == null || apiResponse.getAccounts().size() == 0){
            if ( apiResponse.getError().getError() != null)
                Toasty.error(PeertubeUploadActivity.this, apiResponse.getError().getError(), Toast.LENGTH_LONG).show();
            else
                Toasty.error(PeertubeUploadActivity.this, getString(R.string.toast_error), Toast.LENGTH_LONG).show();
            return;
        }

        //Populate channels
        List<Account> accounts = apiResponse.getAccounts();
        String[] channelName = new String[accounts.size()];
        int i = 0;
        for(Account account: accounts){
            channels.put(account.getUsername(),account.getId());
            channelName[i] = account.getUsername();
            i++;
        }
        ArrayAdapter<String> adapterChannel = new ArrayAdapter<>(PeertubeUploadActivity.this,
                android.R.layout.simple_spinner_dropdown_item, channelName);
        set_upload_channel.setAdapter(adapterChannel);

        //Populate privacy
        String[] privacyName = new String[3];
        privacyName[0] = getString(R.string.v_public);
        privacyName[1] = getString(R.string.v_unlisted);
        privacyName[2] = getString(R.string.v_private);
        ArrayAdapter<String> adapterPrivacy = new ArrayAdapter<>(PeertubeUploadActivity.this,
                android.R.layout.simple_spinner_dropdown_item, privacyName);
        set_upload_privacy.setAdapter(adapterPrivacy);

        set_upload_file.setEnabled(true);

        set_upload_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    if (ContextCompat.checkSelfPermission(PeertubeUploadActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                            PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(PeertubeUploadActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                        return;
                    }
                }
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    intent.setType("*/*");
                    String[] mimetypes = {"video/*"};
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                    startActivityForResult(intent, PICK_IVDEO);
                }else {
                    intent.setType("video/*");
                    Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    Intent chooserIntent = Intent.createChooser(intent, getString(R.string.toot_select_image));
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});
                    startActivityForResult(chooserIntent, PICK_IVDEO);
                }

            }
        });
    }

    @Override
    public void onRetrievePeertubeComments(APIResponse apiResponse) {

    }
}
