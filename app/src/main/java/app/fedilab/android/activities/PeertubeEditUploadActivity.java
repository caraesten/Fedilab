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


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import app.fedilab.android.R;
import app.fedilab.android.asynctasks.PostActionAsyncTask;
import app.fedilab.android.asynctasks.PostPeertubeAsyncTask;
import app.fedilab.android.asynctasks.RetrievePeertubeChannelsAsyncTask;
import app.fedilab.android.asynctasks.RetrievePeertubeSingleAsyncTask;
import app.fedilab.android.client.API;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Error;
import app.fedilab.android.client.Entities.Peertube;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.interfaces.OnPostActionInterface;
import app.fedilab.android.interfaces.OnRetrievePeertubeInterface;
import es.dmoral.toasty.Toasty;
import mabbas007.tagsedittext.TagsEditText;

import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;
import static app.fedilab.android.asynctasks.RetrievePeertubeInformationAsyncTask.peertubeInformation;

public class PeertubeEditUploadActivity extends BaseActivity implements OnRetrievePeertubeInterface, OnPostActionInterface {


    HashMap<Integer, String> categoryToSend;
    HashMap<Integer, String> licenseToSend;
    HashMap<Integer, String> privacyToSend;
    HashMap<String, String> languageToSend;
    HashMap<String, String> channelToSend;
    private Button set_upload_submit;
    private Spinner set_upload_privacy, set_upload_categories, set_upload_licenses, set_upload_languages, set_upload_channel;
    private EditText p_video_title, p_video_description;
    private TagsEditText p_video_tags;
    private CheckBox set_upload_nsfw, set_upload_enable_comments;
    private LinkedHashMap<String, String> channels;
    private String videoId;
    private Account channel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        switch (theme) {
            case Helper.THEME_LIGHT:
                setTheme(R.style.AppTheme_Fedilab);
                break;
            case Helper.THEME_BLACK:
                setTheme(R.style.AppThemeBlack);
                break;
            default:
                setTheme(R.style.AppThemeDark);
        }
        Bundle b = getIntent().getExtras();

        if (b != null) {
            videoId = b.getString("video_id", null);
        }
        if (videoId == null) {
            videoId = sharedpreferences.getString(Helper.VIDEO_ID, null);
        }
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            View view = inflater.inflate(R.layout.simple_bar, new LinearLayout(getApplicationContext()), false);
            view.setBackground(new ColorDrawable(ContextCompat.getColor(PeertubeEditUploadActivity.this, R.color.cyanea_primary)));
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
            toolbar_title.setText(R.string.update_video);
        }
        setContentView(R.layout.activity_peertube_edit);


        set_upload_submit = findViewById(R.id.set_upload_submit);
        Button set_upload_delete = findViewById(R.id.set_upload_delete);
        set_upload_privacy = findViewById(R.id.set_upload_privacy);
        set_upload_channel = findViewById(R.id.set_upload_channel);
        set_upload_categories = findViewById(R.id.set_upload_categories);
        set_upload_licenses = findViewById(R.id.set_upload_licenses);
        set_upload_languages = findViewById(R.id.set_upload_languages);
        p_video_title = findViewById(R.id.p_video_title);
        p_video_description = findViewById(R.id.p_video_description);
        p_video_tags = findViewById(R.id.p_video_tags);
        set_upload_nsfw = findViewById(R.id.set_upload_nsfw);
        set_upload_enable_comments = findViewById(R.id.set_upload_enable_comments);


        set_upload_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builderInner;
                SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
                int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
                int style;
                if (theme == Helper.THEME_DARK) {
                    style = R.style.DialogDark;
                } else if (theme == Helper.THEME_BLACK) {
                    style = R.style.DialogBlack;
                } else {
                    style = R.style.Dialog;
                }
                builderInner = new AlertDialog.Builder(PeertubeEditUploadActivity.this, style);
                builderInner.setMessage(getString(R.string.delete_video_confirmation));
                builderInner.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builderInner.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new PostActionAsyncTask(getApplicationContext(), API.StatusAction.PEERTUBEDELETEVIDEO, videoId, PeertubeEditUploadActivity.this).executeOnExecutor(THREAD_POOL_EXECUTOR);
                        dialog.dismiss();
                    }
                });
                builderInner.show();
            }
        });
        //Get params from the API
        LinkedHashMap<Integer, String> categories = new LinkedHashMap<>(peertubeInformation.getCategories());
        LinkedHashMap<Integer, String> licences = new LinkedHashMap<>(peertubeInformation.getLicences());
        LinkedHashMap<Integer, String> privacies = new LinkedHashMap<>(peertubeInformation.getPrivacies());
        LinkedHashMap<String, String> languages = new LinkedHashMap<>(peertubeInformation.getLanguages());
        LinkedHashMap<String, String> translations = null;
        if (peertubeInformation.getTranslations() != null)
            translations = new LinkedHashMap<>(peertubeInformation.getTranslations());
        //Populate catgories
        String[] categoriesA = new String[categories.size()];
        Iterator it = categories.entrySet().iterator();
        int i = 0;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            if (translations == null || translations.size() == 0 || !translations.containsKey(pair.getValue()))
                categoriesA[i] = (String) pair.getValue();
            else
                categoriesA[i] = translations.get(pair.getValue());
            it.remove();
            i++;
        }
        ArrayAdapter<String> adapterCatgories = new ArrayAdapter<>(PeertubeEditUploadActivity.this,
                android.R.layout.simple_spinner_dropdown_item, categoriesA);
        set_upload_categories.setAdapter(adapterCatgories);


        //Populate licenses
        String[] licensesA = new String[licences.size()];
        it = licences.entrySet().iterator();
        i = 0;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            if (translations == null || translations.size() == 0 || !translations.containsKey(pair.getValue()))
                licensesA[i] = (String) pair.getValue();
            else
                licensesA[i] = translations.get(pair.getValue());
            it.remove();
            i++;
        }
        ArrayAdapter<String> adapterLicenses = new ArrayAdapter<>(PeertubeEditUploadActivity.this,
                android.R.layout.simple_spinner_dropdown_item, licensesA);
        set_upload_licenses.setAdapter(adapterLicenses);


        //Populate languages
        String[] languagesA = new String[languages.size()];
        it = languages.entrySet().iterator();
        i = 0;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            if (translations == null || translations.size() == 0 || !translations.containsKey(pair.getValue()))
                languagesA[i] = (String) pair.getValue();
            else
                languagesA[i] = translations.get(pair.getValue());
            it.remove();
            i++;
        }
        ArrayAdapter<String> adapterLanguages = new ArrayAdapter<>(PeertubeEditUploadActivity.this,
                android.R.layout.simple_spinner_dropdown_item, languagesA);
        set_upload_languages.setAdapter(adapterLanguages);


        //Populate languages
        String[] privaciesA = new String[privacies.size()];
        it = privacies.entrySet().iterator();
        i = 0;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            if (translations == null || translations.size() == 0 || !translations.containsKey(pair.getValue()))
                privaciesA[i] = (String) pair.getValue();
            else
                privaciesA[i] = translations.get(pair.getValue());
            it.remove();
            i++;
        }
        ArrayAdapter<String> adapterPrivacies = new ArrayAdapter<>(PeertubeEditUploadActivity.this,
                android.R.layout.simple_spinner_dropdown_item, privaciesA);
        set_upload_privacy.setAdapter(adapterPrivacies);


        String peertubeInstance = Helper.getLiveInstance(getApplicationContext());
        new RetrievePeertubeSingleAsyncTask(PeertubeEditUploadActivity.this, peertubeInstance, videoId, PeertubeEditUploadActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        channels = new LinkedHashMap<>();


    }


    @Override
    public void onRetrievePeertube(APIResponse apiResponse) {
        if (apiResponse.getError() != null || apiResponse.getPeertubes() == null || apiResponse.getPeertubes().size() == 0) {
            if (apiResponse.getError() != null && apiResponse.getError().getError() != null)
                Toasty.error(getApplicationContext(), apiResponse.getError().getError(), Toast.LENGTH_LONG).show();
            else
                Toasty.error(getApplicationContext(), getString(R.string.toast_error), Toast.LENGTH_LONG).show();
            set_upload_submit.setEnabled(true);
            return;
        }

        //Peertube video
        Peertube peertube = apiResponse.getPeertubes().get(0);

        if (peertube.isUpdate()) {
            Toasty.success(getApplicationContext(), getString(R.string.toast_peertube_video_updated), Toast.LENGTH_LONG).show();
            peertube.setUpdate(false);
            set_upload_submit.setEnabled(true);
        } else {
            new RetrievePeertubeChannelsAsyncTask(PeertubeEditUploadActivity.this, PeertubeEditUploadActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        languageToSend = peertube.getLanguage();
        licenseToSend = peertube.getLicense();
        privacyToSend = peertube.getPrivacy();
        categoryToSend = peertube.getCategory();


        if (languageToSend == null) {
            LinkedHashMap<String, String> languages = new LinkedHashMap<>(peertubeInformation.getLanguages());
            Map.Entry<String, String> entryString = languages.entrySet().iterator().next();
            languageToSend = new HashMap<>();
            languageToSend.put(entryString.getKey(), entryString.getValue());
        }

        if (licenseToSend == null) {
            LinkedHashMap<Integer, String> licences = new LinkedHashMap<>(peertubeInformation.getLicences());
            Map.Entry<Integer, String> entryInt = licences.entrySet().iterator().next();
            licenseToSend = new HashMap<>();
            licenseToSend.put(entryInt.getKey(), entryInt.getValue());
        }

        if (categoryToSend == null) {
            LinkedHashMap<Integer, String> categories = new LinkedHashMap<>(peertubeInformation.getCategories());
            Map.Entry<Integer, String> entryInt = categories.entrySet().iterator().next();
            categoryToSend = new HashMap<>();
            categoryToSend.put(entryInt.getKey(), entryInt.getValue());
        }
        if (privacyToSend == null) {
            LinkedHashMap<Integer, String> privacies = new LinkedHashMap<>(peertubeInformation.getPrivacies());
            Map.Entry<Integer, String> entryInt = privacies.entrySet().iterator().next();
            privacyToSend = new HashMap<>();
            privacyToSend.put(entryInt.getKey(), entryInt.getValue());
        }

        String language = null;

        if (languageToSend != null) {
            Map.Entry<String, String> entryString = languageToSend.entrySet().iterator().next();
            language = entryString.getValue();
        }

        String license = null;
        if (licenseToSend != null) {
            Map.Entry<Integer, String> entryInt = licenseToSend.entrySet().iterator().next();
            license = entryInt.getValue();
        }

        String privacy = null;
        if (privacyToSend != null) {
            Map.Entry<Integer, String> entryInt = privacyToSend.entrySet().iterator().next();
            privacy = entryInt.getValue();
        }

        String category = null;
        if (categoryToSend != null) {
            Map.Entry<Integer, String> entryInt = categoryToSend.entrySet().iterator().next();
            category = entryInt.getValue();
        }

        channel = peertube.getChannel();
        String title = peertube.getName();
        boolean commentEnabled = peertube.isCommentsEnabled();
        boolean isNSFW = peertube.isSensitive();

        set_upload_enable_comments.setChecked(commentEnabled);
        set_upload_nsfw.setChecked(isNSFW);

        p_video_title.setText(title);
        p_video_description.setText(peertube.getDescription());


        LinkedHashMap<Integer, String> categories = new LinkedHashMap<>(peertubeInformation.getCategories());
        LinkedHashMap<Integer, String> licences = new LinkedHashMap<>(peertubeInformation.getLicences());
        LinkedHashMap<Integer, String> privacies = new LinkedHashMap<>(peertubeInformation.getPrivacies());
        LinkedHashMap<String, String> languages = new LinkedHashMap<>(peertubeInformation.getLanguages());
        LinkedHashMap<String, String> translations = null;
        if (peertubeInformation.getTranslations() != null)
            translations = new LinkedHashMap<>(peertubeInformation.getTranslations());


        int languagePosition = 0;
        if (languages.containsValue(language)) {
            Iterator it = languages.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                if (pair.getValue().equals(language))
                    break;
                it.remove();
                languagePosition++;
            }
        }
        int privacyPosition = 0;
        if (privacy != null && privacies.containsValue(privacy)) {
            Iterator it = privacies.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                if (pair.getValue().equals(privacy))
                    break;
                it.remove();
                privacyPosition++;
            }
        }
        int licensePosition = 0;
        if (license != null && licences.containsValue(license)) {
            Iterator it = licences.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                if (pair.getValue().equals(license))
                    break;
                it.remove();
                licensePosition++;
            }
        }
        int categoryPosition = 0;
        if (category != null && categories.containsValue(category)) {
            Iterator it = categories.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                if (pair.getValue().equals(category))
                    break;
                it.remove();
                categoryPosition++;
            }
        }

        //Manage privacies
        set_upload_privacy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LinkedHashMap<Integer, String> privaciesCheck = new LinkedHashMap<>(peertubeInformation.getPrivacies());
                Iterator it = privaciesCheck.entrySet().iterator();
                int i = 0;
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    if (i == position) {
                        privacyToSend = new HashMap<>();
                        privacyToSend.put((Integer) pair.getKey(), (String) pair.getValue());
                        break;
                    }
                    it.remove();
                    i++;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        set_upload_licenses.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LinkedHashMap<Integer, String> licensesCheck = new LinkedHashMap<>(peertubeInformation.getLicences());
                Iterator it = licensesCheck.entrySet().iterator();
                int i = 0;
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    if (i == position) {
                        licenseToSend = new HashMap<>();
                        licenseToSend.put((Integer) pair.getKey(), (String) pair.getValue());
                        break;
                    }
                    it.remove();
                    i++;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //Manage categories
        set_upload_categories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LinkedHashMap<Integer, String> categoriesCheck = new LinkedHashMap<>(peertubeInformation.getCategories());
                Iterator it = categoriesCheck.entrySet().iterator();
                int i = 0;
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    if (i == position) {
                        categoryToSend = new HashMap<>();
                        categoryToSend.put((Integer) pair.getKey(), (String) pair.getValue());
                        break;
                    }
                    it.remove();
                    i++;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Manage languages
        set_upload_languages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LinkedHashMap<String, String> languagesCheck = new LinkedHashMap<>(peertubeInformation.getLanguages());
                Iterator it = languagesCheck.entrySet().iterator();
                int i = 0;
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    if (i == position) {
                        languageToSend = new HashMap<>();
                        languageToSend.put((String) pair.getKey(), (String) pair.getValue());
                        break;
                    }
                    it.remove();
                    i++;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //Manage languages
        set_upload_channel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LinkedHashMap<String, String> channelsCheck = new LinkedHashMap<>(channels);
                Iterator it = channelsCheck.entrySet().iterator();
                int i = 0;
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    if (i == position) {
                        channelToSend = new HashMap<>();
                        channelToSend.put((String) pair.getKey(), (String) pair.getValue());

                        break;
                    }
                    it.remove();
                    i++;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        set_upload_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = p_video_title.getText().toString().trim();
                String description = p_video_description.getText().toString().trim();
                boolean isNSFW = set_upload_nsfw.isChecked();
                boolean commentEnabled = set_upload_enable_comments.isChecked();
                peertube.setName(title);
                peertube.setDescription(description);
                peertube.setSensitive(isNSFW);
                peertube.setCommentsEnabled(commentEnabled);
                peertube.setCategory(categoryToSend);
                peertube.setLicense(licenseToSend);
                peertube.setLanguage(languageToSend);
                peertube.setChannelForUpdate(channelToSend);
                peertube.setPrivacy(privacyToSend);
                List<String> tags = p_video_tags.getTags();
                peertube.setTags(tags);
                set_upload_submit.setEnabled(false);
                new PostPeertubeAsyncTask(PeertubeEditUploadActivity.this, peertube, PeertubeEditUploadActivity.this).executeOnExecutor(THREAD_POOL_EXECUTOR);
            }
        });

        set_upload_privacy.setSelection(privacyPosition);
        set_upload_languages.setSelection(languagePosition);
        set_upload_licenses.setSelection(licensePosition);
        set_upload_categories.setSelection(categoryPosition);

        List<String> tags = peertube.getTags();
        if (tags != null && tags.size() > 0) {
            String[] tagsA = tags.toArray(new String[tags.size()]);
            p_video_tags.setTags(tagsA);
        }

    }

    @Override
    public void onRetrievePeertubeComments(APIResponse apiResponse) {

    }

    @Override
    public void onRetrievePeertubeChannels(APIResponse apiResponse) {
        if (apiResponse.getError() != null || apiResponse.getAccounts() == null || apiResponse.getAccounts().size() == 0) {
            if (apiResponse.getError().getError() != null)
                Toasty.error(getApplicationContext(), apiResponse.getError().getError(), Toast.LENGTH_LONG).show();
            else
                Toasty.error(getApplicationContext(), getString(R.string.toast_error), Toast.LENGTH_LONG).show();
            return;
        }

        //Populate channels
        List<Account> accounts = apiResponse.getAccounts();
        String[] channelName = new String[accounts.size()];
        int i = 0;
        for (Account account : accounts) {
            channels.put(account.getUsername(), account.getId());
            channelName[i] = account.getUsername();
            i++;
        }
        ArrayAdapter<String> adapterChannel = new ArrayAdapter<>(PeertubeEditUploadActivity.this,
                android.R.layout.simple_spinner_dropdown_item, channelName);
        set_upload_channel.setAdapter(adapterChannel);

        int channelPosition = 0;
        if (channels.containsKey(channel.getUsername())) {
            LinkedHashMap<String, String> channelsIterator = new LinkedHashMap<>(channels);
            Iterator it = channelsIterator.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                if (pair.getKey().equals(channel.getUsername())) {
                    channelToSend = new HashMap<>();
                    channelToSend.put((String) pair.getKey(), (String) pair.getValue());
                    break;
                }
                it.remove();
                channelPosition++;
            }
        }
        set_upload_channel.setSelection(channelPosition);

        set_upload_submit.setEnabled(true);
    }

    @Override
    public void onPostAction(int statusCode, API.StatusAction statusAction, String userId, Error error) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra(Helper.INTENT_ACTION, Helper.RELOAD_MYVIDEOS);
        startActivity(intent);
        finish();
    }
}
