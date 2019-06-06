package app.fedilab.android.activities;
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


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
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
import android.widget.TextView;
import android.widget.Toast;

import com.jaredrummler.materialspinner.MaterialSpinner;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationAction;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadServiceSingleBroadcastReceiver;
import net.gotev.uploadservice.UploadStatusDelegate;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.helper.Helper;
import es.dmoral.toasty.Toasty;
import app.fedilab.android.R;
import app.fedilab.android.asynctasks.RetrievePeertubeChannelsAsyncTask;
import app.fedilab.android.interfaces.OnRetrievePeertubeInterface;

import static app.fedilab.android.asynctasks.RetrievePeertubeInformationAsyncTask.peertubeInformation;

public class PeertubeUploadActivity extends BaseActivity implements OnRetrievePeertubeInterface, UploadStatusDelegate {


    private final int PICK_IVDEO = 52378;
    private Button set_upload_file, set_upload_submit;
    private MaterialSpinner set_upload_privacy, set_upload_channel;
    private TextView set_upload_file_name;
    private HashMap<String, String> channels;
    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 724;
    private Uri uri;
    private String filename;
    private HashMap<Integer, String> privacyToSend;
    private HashMap<String, String> channelToSend;
    private UploadServiceSingleBroadcastReceiver uploadReceiver;

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
            toolbar_title.setText(R.string.upload_video);
            if (theme == Helper.THEME_LIGHT){
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

        Helper.changeMaterialSpinnerColor(PeertubeUploadActivity.this, set_upload_privacy);
        Helper.changeMaterialSpinnerColor(PeertubeUploadActivity.this, set_upload_channel);

        new RetrievePeertubeChannelsAsyncTask(PeertubeUploadActivity.this, PeertubeUploadActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        channels = new HashMap<>();

        uploadReceiver = new UploadServiceSingleBroadcastReceiver(this);
        uploadReceiver.register(this);

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

            uri = data.getData();
            String uriString = uri.toString();
            File myFile = new File(uriString);
            filename = null;
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
    public void onDestroy() {
        super.onDestroy();
        uploadReceiver.unregister(this);
    }

    @Override
    public void onRetrievePeertube(APIResponse apiResponse) {

    }

    @Override
    public void onRetrievePeertubeComments(APIResponse apiResponse) {

    }

    @Override
    public void onRetrievePeertubeChannels(APIResponse apiResponse) {
        if( apiResponse.getError() != null || apiResponse.getAccounts() == null || apiResponse.getAccounts().size() == 0){
            if ( apiResponse.getError() != null && apiResponse.getError().getError() != null)
                Toasty.error(PeertubeUploadActivity.this, apiResponse.getError().getError(), Toast.LENGTH_LONG).show();
            else
                Toasty.error(PeertubeUploadActivity.this, getString(R.string.toast_error), Toast.LENGTH_LONG).show();
            return;
        }

        //Populate channels
        List<Account> accounts = apiResponse.getAccounts();
        String[] channelName = new String[accounts.size()];
        String[] channelId= new String[accounts.size()];
        int i = 0;
        for(Account account: accounts){
            channels.put(account.getUsername(),account.getId());
            channelName[i] = account.getUsername();
            channelId[i] = account.getId();
            i++;
        }

        channelToSend = new HashMap<>();
        channelToSend.put(channelName[0], channelId[0]);
        ArrayAdapter<String> adapterChannel = new ArrayAdapter<>(PeertubeUploadActivity.this,
                android.R.layout.simple_spinner_dropdown_item, channelName);
        set_upload_channel.setAdapter(adapterChannel);

        LinkedHashMap<String, String> translations = null;
        if( peertubeInformation.getTranslations() != null)
            translations = new LinkedHashMap<>(peertubeInformation.getTranslations());

        LinkedHashMap<Integer, String> privaciesInit = new LinkedHashMap<>(peertubeInformation.getPrivacies());
        Map.Entry<Integer,String> entryInt = privaciesInit.entrySet().iterator().next();
        privacyToSend = new HashMap<>();
        privacyToSend.put(entryInt.getKey(), entryInt.getValue());
        LinkedHashMap<Integer, String> privacies = new LinkedHashMap<>(peertubeInformation.getPrivacies());
        //Populate privacies
        String[] privaciesA = new String[privacies.size()];
        Iterator it = privacies.entrySet().iterator();
        i = 0;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            if( translations == null || translations.size() == 0 || !translations.containsKey((String)pair.getValue()))
                privaciesA[i] =  (String)pair.getValue();
            else
                privaciesA[i] =  translations.get((String)pair.getValue());
            it.remove();
            i++;
        }

        ArrayAdapter<String> adapterPrivacies = new ArrayAdapter<>(PeertubeUploadActivity.this,
                android.R.layout.simple_spinner_dropdown_item, privaciesA);
        set_upload_privacy.setAdapter(adapterPrivacies);

        //Manage privacies
        set_upload_privacy.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                LinkedHashMap<Integer, String> privaciesCheck = new LinkedHashMap<>(peertubeInformation.getPrivacies());
                Iterator it = privaciesCheck.entrySet().iterator();
                int i = 0;
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry)it.next();
                    if( i == position){
                        privacyToSend = new HashMap<>();
                        privacyToSend.put((Integer)pair.getKey(), (String)pair.getValue());
                        break;
                    }
                    it.remove();
                    i++;
                }
            }
        });

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

        //Manage languages
        set_upload_channel.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                LinkedHashMap<String, String> channelsCheck = new LinkedHashMap<>(channels);
                Iterator it = channelsCheck.entrySet().iterator();
                int i = 0;
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry)it.next();
                    if( i == position){
                        channelToSend = new HashMap<>();
                        channelToSend.put((String)pair.getKey(), (String)pair.getValue());

                        break;
                    }
                    it.remove();
                    i++;
                }
            }
        });
        set_upload_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( uri != null) {
                    Map.Entry<String, String> channelM = channelToSend.entrySet().iterator().next();
                    String idChannel = channelM.getValue();
                    Map.Entry<Integer, String> privacyM = privacyToSend.entrySet().iterator().next();
                    Integer idPrivacy = privacyM.getKey();

                    try {
                        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
                        String token = sharedpreferences.getString(Helper.PREF_KEY_OAUTH_TOKEN, null);
                        UploadNotificationConfig uploadConfig = new UploadNotificationConfig();
                        Intent in = new Intent(getApplicationContext(), PeertubeEditUploadActivity.class );
                        PendingIntent clickIntent = PendingIntent.getActivity(getApplicationContext(), 1, in, PendingIntent.FLAG_UPDATE_CURRENT);
                        uploadConfig
                                .setClearOnActionForAllStatuses(true);


                        uploadConfig.getProgress().message = getString(R.string.uploading);
                        uploadConfig.getCompleted().message = getString(R.string.upload_video_success);
                        uploadConfig.getError().message = getString(R.string.toast_error);
                        uploadConfig.getCancelled().message = getString(R.string.toast_cancelled);
                        uploadConfig.getCompleted().actions.add(new UploadNotificationAction(R.drawable.ic_check, getString(R.string.video_uploaded_action), clickIntent));

                        String uploadId = UUID.randomUUID().toString();
                        uploadReceiver.setUploadID(uploadId);
                        new MultipartUploadRequest(PeertubeUploadActivity.this, uploadId, "https://" + Helper.getLiveInstance(PeertubeUploadActivity.this) + "/api/v1/videos/upload")
                                .addFileToUpload(uri.toString().replace("file://",""), "videofile")
                                .addHeader("Authorization", "Bearer " + token)
                                .setNotificationConfig(uploadConfig)
                                .addParameter("name", filename)
                                .addParameter("channelId", idChannel)
                                .addParameter("privacy", String.valueOf(idPrivacy))
                                .addParameter("nsfw", "false")
                                .addParameter("commentsEnabled", "true")
                                .addParameter("waitTranscoding", "true")
                                .setMaxRetries(2)
                                .startUpload();
                                finish();
                    } catch (Exception exc) {
                        exc.printStackTrace();
                    }
                }
            }
        });
    }



    @Override
    public void onProgress(Context context, UploadInfo uploadInfo) {
        // your code here
    }

    @Override
    public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse,
                        Exception exception) {
        // your code here
        exception.printStackTrace();
    }

    @Override
    public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
        try {
            JSONObject response = new JSONObject(serverResponse.getBodyAsString());
            String videoID = response.getJSONObject("video").get("id").toString();
            SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(Helper.VIDEO_ID, videoID);
            editor.commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCancelled(Context context, UploadInfo uploadInfo) {
        // your code here
    }
}
