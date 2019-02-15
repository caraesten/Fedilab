package fr.gouv.etalab.mastodon.fragments;
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
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Error;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchConfiguration;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2.NetworkType;
import com.tonyodev.fetch2.Priority;
import com.tonyodev.fetch2.Request;
import com.tonyodev.fetch2core.DownloadBlock;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.activities.MainActivity;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.DomainBlockDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;

import static android.app.Activity.RESULT_OK;
import static fr.gouv.etalab.mastodon.helper.Helper.BACK_TO_SETTINGS;
import static fr.gouv.etalab.mastodon.helper.Helper.INTENT_ACTION;
import static fr.gouv.etalab.mastodon.helper.Helper.SET_YANDEX_API_KEY;
import static fr.gouv.etalab.mastodon.helper.Helper.changeDrawableColor;


/**
 * Created by Thomas on 24/06/2017.
 * Fragment for settings, yes I didn't use PreferenceFragment :)
 */
public class SettingsFragment extends Fragment {


    private Context context;
    private static final int ACTIVITY_CHOOSE_FILE = 411;
    private TextView set_folder;
    private EditText your_api_key;
    private int count1, count2, count3, count4;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        context = getContext();
        assert context != null;
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);

        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        boolean auto_store = sharedpreferences.getBoolean(Helper.SET_AUTO_STORE, true);

        final CheckBox set_auto_store = rootView.findViewById(R.id.set_auto_store);
        set_auto_store.setChecked(auto_store);
        set_auto_store.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_AUTO_STORE, set_auto_store.isChecked());
                editor.apply();
            }
        });

        boolean display_content_after_fetch_more = sharedpreferences.getBoolean(Helper.SET_DISPLAY_CONTENT_AFTER_FM, true);
        final CheckBox set_display_content_after_fetch_more = rootView.findViewById(R.id.set_display_content_after_fetch_more);
        set_display_content_after_fetch_more.setChecked(display_content_after_fetch_more);
        set_display_content_after_fetch_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_DISPLAY_CONTENT_AFTER_FM, set_display_content_after_fetch_more.isChecked());
                editor.apply();
            }
        });

        count1 = 0;
        count2 = 0;
        count3 = 0;
        count4 = 0;


        final Spinner battery_layout_spinner = rootView.findViewById(R.id.battery_layout_spinner);
        ArrayAdapter<CharSequence> adapterBattery = ArrayAdapter.createFromResource(getContext(),
                R.array.battery_profiles, android.R.layout.simple_spinner_item);
        battery_layout_spinner.setAdapter(adapterBattery);
        int positionSpinner = sharedpreferences.getInt(Helper.SET_BATTERY_PROFILE, Helper.BATTERY_PROFILE_NORMAL) -1;
        battery_layout_spinner.setSelection(positionSpinner);
        battery_layout_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if( count2 > 0){
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    switch (position){
                        case 0:
                            editor.putInt(Helper.SET_BATTERY_PROFILE, Helper.BATTERY_PROFILE_NORMAL);
                            editor.apply();
                            break;
                        case 1:
                            editor.putInt(Helper.SET_BATTERY_PROFILE, Helper.BATTERY_PROFILE_MEDIUM);
                            editor.apply();
                            break;
                        case 2:
                            editor.putInt(Helper.SET_BATTERY_PROFILE, Helper.BATTERY_PROFILE_LOW);
                            editor.apply();
                            break;
                    }
                    Helper.changeBatteryProfile(context);
                    if( position < 2 ){
                        try {
                            ((MainActivity) context).startSreaming();
                        }catch (Exception ignored){ignored.printStackTrace();}
                    }else{
                        context.sendBroadcast(new Intent("StopLiveNotificationService"));
                    }
                }else {
                    count2++;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button update_tracking_domains = rootView.findViewById(R.id.update_tracking_domains);
        update_tracking_domains.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FetchConfiguration fetchConfiguration = new FetchConfiguration.Builder(context)
                        .setDownloadConcurrentLimit(1)
                        .build();

                update_tracking_domains.setEnabled(false);
                Fetch fetch = Fetch.Impl.getInstance(fetchConfiguration);

                String url = "https://sebsauvage.net/hosts/hosts";
                File dir = context.getCacheDir();
                String file = dir + "/tracking.txt";
                FetchListener fetchListener = new FetchListener() {
                    @Override
                    public void onWaitingNetwork(@NotNull Download download) {
                    }
                    @Override
                    public void onStarted(@NotNull Download download, @NotNull List<? extends DownloadBlock> list, int i) {
                    }
                    @Override
                    public void onResumed(@NotNull Download download) {
                    }
                    @Override
                    public void onRemoved(@NotNull Download download) {
                    }
                    @Override
                    public void onQueued(@NotNull Download download, boolean b) {
                    }
                    @Override
                    public void onProgress(@NotNull Download download, long l, long l1) {
                    }
                    @Override
                    public void onPaused(@NotNull Download download) {
                    }
                    @Override
                    public void onError(@NotNull Download download, @NotNull Error error, @Nullable Throwable throwable) {
                        Toasty.error(context, context.getString(R.string.toast_error),Toast.LENGTH_LONG).show();
                        update_tracking_domains.setEnabled(true);
                    }
                    @Override
                    public void onDownloadBlockUpdated(@NotNull Download download, @NotNull DownloadBlock downloadBlock, int i) {
                    }
                    @Override
                    public void onDeleted(@NotNull Download download) {
                    }
                    @Override
                    public void onCompleted(@NotNull Download download) {
                        boolean canRecord = false;
                        if( download.getFileUri().getPath() != null) {
                            File file = new File(download.getFileUri().getPath());
                            try {
                                BufferedReader br = new BufferedReader(new FileReader(file));
                                String line;
                                ArrayList<String> domains = new ArrayList<>();
                                while ((line = br.readLine()) != null) {
                                    if(!canRecord && line.contains("# Blocked domains"))
                                        canRecord = true;
                                    if( canRecord) {
                                        String domain = line.replaceAll("0.0.0.0 ","").trim();
                                        domains.add(domain);
                                    }
                                }
                                br.close();
                                AsyncTask.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                                        new DomainBlockDAO(context, db).insertAll(domains);
                                        // Get a handler that can be used to post to the main thread
                                        Handler mainHandler = new Handler(context.getMainLooper());

                                        Runnable myRunnable = new Runnable() {
                                            @Override
                                            public void run() {
                                                Toasty.success(context, context.getString(R.string.tracking_db_updated), Toast.LENGTH_LONG).show();
                                            }
                                        };
                                        mainHandler.post(myRunnable);

                                    }
                                });
                            }
                            catch (IOException e) {
                                //You'll need to add proper error handling here
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NotNull Download download) {
                    }
                    @Override
                    public void onAdded(@NotNull Download download) {
                    }
                };

                fetch.addListener(fetchListener);
                final Request request = new Request(url, file);
                request.setPriority(Priority.HIGH);
                request.setNetworkType(NetworkType.ALL);
                fetch.enqueue(request, updatedRequest -> {
                    //Request was successfully enqueued for download.
                }, error -> {
                    //An error occurred enqueuing the request.
                });
            }
        });

        //Manage download of attachments
        RadioGroup radioGroup = rootView.findViewById(R.id.set_attachment_group);
        int attachmentAction = sharedpreferences.getInt(Helper.SET_ATTACHMENT_ACTION, Helper.ATTACHMENT_ALWAYS);
        switch (attachmentAction){
            case Helper.ATTACHMENT_ALWAYS:
                radioGroup.check(R.id.set_attachment_always);
                break;
            case Helper.ATTACHMENT_WIFI:
                radioGroup.check(R.id.set_attachment_wifi);
                break;
            case Helper.ATTACHMENT_ASK:
                radioGroup.check(R.id.set_attachment_ask);
                break;
        }
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId) {
                    case R.id.set_attachment_always:
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putInt(Helper.SET_ATTACHMENT_ACTION, Helper.ATTACHMENT_ALWAYS);
                        editor.apply();
                        break;
                    case R.id.set_attachment_wifi:
                        editor = sharedpreferences.edit();
                        editor.putInt(Helper.SET_ATTACHMENT_ACTION, Helper.ATTACHMENT_WIFI);
                        editor.apply();
                        break;
                    case R.id.set_attachment_ask:
                        editor = sharedpreferences.edit();
                        editor.putInt(Helper.SET_ATTACHMENT_ACTION, Helper.ATTACHMENT_ASK);
                        editor.apply();
                        break;
                }
            }
        });


        boolean optimize_loading = sharedpreferences.getBoolean(Helper.SET_OPTIMIZE_LOADING, false);
        final CheckBox set_optimize_loading = rootView.findViewById(R.id.set_optimize_loading);
        set_optimize_loading.setChecked(optimize_loading);

        set_optimize_loading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_OPTIMIZE_LOADING, set_optimize_loading.isChecked());
                if( set_optimize_loading.isChecked()){
                    editor.putInt(Helper.SET_ACCOUNTS_PER_PAGE, 10);
                    editor.putInt(Helper.SET_TOOTS_PER_PAGE, 10);
                    editor.putInt(Helper.SET_NOTIFICATIONS_PER_PAGE, 10);
                }else {
                    editor.putInt(Helper.SET_ACCOUNTS_PER_PAGE, 40);
                    editor.putInt(Helper.SET_TOOTS_PER_PAGE, 40);
                    editor.putInt(Helper.SET_NOTIFICATIONS_PER_PAGE, 15);
                }
                editor.apply();
            }
        });

        boolean patch_provider = sharedpreferences.getBoolean(Helper.SET_SECURITY_PROVIDER, true);
        final CheckBox set_security_provider = rootView.findViewById(R.id.set_security_provider);
        set_security_provider.setChecked(patch_provider);

        set_security_provider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_SECURITY_PROVIDER, set_security_provider.isChecked());
                editor.apply();
            }
        });


        boolean display_card = sharedpreferences.getBoolean(Helper.SET_DISPLAY_CARD, false);

        final CheckBox set_display_card = rootView.findViewById(R.id.set_display_card);
        set_display_card.setChecked(display_card);
        set_display_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_DISPLAY_CARD, set_display_card.isChecked());
                editor.apply();
            }
        });


        boolean show_media_urls = sharedpreferences.getBoolean(Helper.SET_MEDIA_URLS, false);
        final CheckBox set_auto_add_media_url = rootView.findViewById(R.id.set_auto_add_media_url);
        set_auto_add_media_url.setChecked(show_media_urls);

        set_auto_add_media_url.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_MEDIA_URLS, set_auto_add_media_url.isChecked());
                editor.apply();
            }
        });

        boolean display_video_preview = sharedpreferences.getBoolean(Helper.SET_DISPLAY_VIDEO_PREVIEWS, true);
        final CheckBox set_display_video_preview = rootView.findViewById(R.id.set_display_video_preview);
        set_display_video_preview.setChecked(display_video_preview);

        set_display_video_preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_DISPLAY_VIDEO_PREVIEWS, set_display_video_preview.isChecked());
                editor.apply();
            }
        });


        boolean notif_validation = sharedpreferences.getBoolean(Helper.SET_NOTIF_VALIDATION, true);
        final CheckBox set_share_validation = rootView.findViewById(R.id.set_share_validation);
        set_share_validation.setChecked(notif_validation);

        set_share_validation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_NOTIF_VALIDATION, set_share_validation.isChecked());
                editor.apply();
            }
        });
        your_api_key = rootView.findViewById(R.id.translation_key);

        your_api_key.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                int translatore = sharedpreferences.getInt(Helper.SET_TRANSLATOR, Helper.TRANS_YANDEX);
                String store = null;
                if( translatore == Helper.TRANS_YANDEX)
                    store = Helper.SET_YANDEX_API_KEY;
                else if( translatore == Helper.TRANS_DEEPL)
                    store = Helper.SET_DEEPL_API_KEY;
                if( store != null)
                    if( s != null && s.length() > 0)
                        editor.putString(store, s.toString().trim());
                    else
                        editor.putString(store, null);
                editor.apply();
            }
        });

        boolean notif_validation_fav = sharedpreferences.getBoolean(Helper.SET_NOTIF_VALIDATION_FAV, false);
        final CheckBox set_share_validation_fav = rootView.findViewById(R.id.set_share_validation_fav);
        set_share_validation_fav.setChecked(notif_validation_fav);

        set_share_validation_fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_NOTIF_VALIDATION_FAV, set_share_validation_fav.isChecked());
                editor.apply();
            }
        });


        boolean expand_cw = sharedpreferences.getBoolean(Helper.SET_EXPAND_CW, false);
        final CheckBox set_expand_cw = rootView.findViewById(R.id.set_expand_cw);
        set_expand_cw.setChecked(expand_cw);

        set_expand_cw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_EXPAND_CW, set_expand_cw.isChecked());
                editor.apply();
            }
        });

        boolean display_emoji = sharedpreferences.getBoolean(Helper.SET_DISPLAY_EMOJI, true);
        final CheckBox set_display_emoji = rootView.findViewById(R.id.set_display_emoji);
        set_display_emoji.setChecked(display_emoji);

        set_display_emoji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_DISPLAY_EMOJI, set_display_emoji.isChecked());
                editor.apply();
            }
        });

        boolean expand_media = sharedpreferences.getBoolean(Helper.SET_EXPAND_MEDIA, false);
        final CheckBox set_expand_media = rootView.findViewById(R.id.set_expand_image);
        set_expand_media.setChecked(expand_media);

        set_expand_media.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_EXPAND_MEDIA, set_expand_media.isChecked());
                editor.apply();
            }
        });

        boolean old_direct_timeline = sharedpreferences.getBoolean(Helper.SET_OLD_DIRECT_TIMELINE, false);
        final CheckBox set_old_direct_timeline = rootView.findViewById(R.id.set_old_direct_timeline);
        set_old_direct_timeline.setChecked(old_direct_timeline);

        set_old_direct_timeline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_OLD_DIRECT_TIMELINE, set_old_direct_timeline.isChecked());
                editor.apply();
                if( getActivity() != null)
                    getActivity().recreate();
                Intent intent = new Intent(context, MainActivity.class);
                if(getActivity() != null)
                    getActivity().finish();
                startActivity(intent);
            }
        });

        int truncate_toots_size = sharedpreferences.getInt(Helper.SET_TRUNCATE_TOOTS_SIZE, 0);
        SeekBar set_truncate_size = rootView.findViewById(R.id.set_truncate_size);
        set_truncate_size.setMax(20);
        set_truncate_size.setProgress(truncate_toots_size);
        TextView set_truncate_toots = rootView.findViewById(R.id.set_truncate_toots);
        set_truncate_toots.setText(String.valueOf(truncate_toots_size));
        set_truncate_size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                set_truncate_toots.setText(String.valueOf(progress));
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putInt(Helper.SET_TRUNCATE_TOOTS_SIZE, progress);
                editor.apply();
            }
        });


        boolean follow_instance = sharedpreferences.getBoolean(Helper.SET_DISPLAY_FOLLOW_INSTANCE, true);
        final CheckBox set_follow_instance = rootView.findViewById(R.id.set_display_follow_instance);
        set_follow_instance.setChecked(follow_instance);

        set_follow_instance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_DISPLAY_FOLLOW_INSTANCE, set_follow_instance.isChecked());
                editor.apply();
                ((MainActivity) context).refreshButton();
            }
        });

        boolean display_bookmark = sharedpreferences.getBoolean(Helper.SET_SHOW_BOOKMARK, false);
        final CheckBox set_display_bookmark = rootView.findViewById(R.id.set_display_bookmarks);
        set_display_bookmark.setChecked(display_bookmark);

        set_display_bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_SHOW_BOOKMARK, set_display_bookmark.isChecked());
                editor.apply();
            }
        });

        boolean fit_preview = sharedpreferences.getBoolean(Helper.SET_FULL_PREVIEW, false);
        final CheckBox set_fit_preview = rootView.findViewById(R.id.set_fit_preview);
        set_fit_preview.setChecked(fit_preview);

        set_fit_preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_FULL_PREVIEW, set_fit_preview.isChecked());
                editor.apply();
            }
        });

        boolean compact_mode = sharedpreferences.getBoolean(Helper.SET_COMPACT_MODE, false);
        boolean console_mode = sharedpreferences.getBoolean(Helper.SET_CONSOLE_MODE, false);
        RadioGroup set_mode = rootView.findViewById(R.id.set_mode);
        if( compact_mode){
            set_mode.check(R.id.set_compact_mode);
        }else if(console_mode){
            set_mode.check(R.id.set_console_mode);
        }else {
            set_mode.check(R.id.set_normal_mode);
        }
        set_mode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId) {
                    case R.id.set_compact_mode:
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putBoolean(Helper.SET_COMPACT_MODE, true);
                        editor.putBoolean(Helper.SET_CONSOLE_MODE, false);
                        editor.apply();
                        break;
                    case R.id.set_console_mode:
                        editor = sharedpreferences.edit();
                        editor.putBoolean(Helper.SET_COMPACT_MODE, false);
                        editor.putBoolean(Helper.SET_CONSOLE_MODE, true);
                        editor.apply();
                        break;
                    case R.id.set_normal_mode:
                        editor = sharedpreferences.edit();
                        editor.putBoolean(Helper.SET_COMPACT_MODE, false);
                        editor.putBoolean(Helper.SET_CONSOLE_MODE, false);
                        editor.apply();
                        break;
                }
            }
        });

        boolean share_details = sharedpreferences.getBoolean(Helper.SET_SHARE_DETAILS, true);
        final CheckBox set_share_details = rootView.findViewById(R.id.set_share_details);
        set_share_details.setChecked(share_details);

        set_share_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_SHARE_DETAILS, set_share_details.isChecked());
                editor.apply();
            }
        });

        // Custom Sharing
        boolean custom_sharing = sharedpreferences.getBoolean(Helper.SET_CUSTOM_SHARING, true);
        final CheckBox set_custom_sharing = rootView.findViewById(R.id.set_custom_sharing);
        set_custom_sharing.setChecked(custom_sharing);

        set_custom_sharing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_CUSTOM_SHARING, set_custom_sharing.isChecked());
                editor.apply();
            }
        });

        String custom_sharing_url = sharedpreferences.getString(Helper.SET_CUSTOM_SHARING_URL,"");
        final EditText edit_custom_sharing_url = rootView.findViewById(R.id.custom_sharing_url);
        if (custom_sharing_url.equals("")) {
            custom_sharing_url = "http://my.site/add?user=fedilab&url=${url}&title=${title}&source=${source}&id=${id}&description=${description}&keywords=${keywords}";
        }
        edit_custom_sharing_url.setText(custom_sharing_url);


        edit_custom_sharing_url.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(Helper.SET_CUSTOM_SHARING_URL, s.toString().trim());
                editor.apply();
            }
        });

        boolean display_direct = sharedpreferences.getBoolean(Helper.SET_DISPLAY_DIRECT, true);
        final CheckBox set_display_direct = rootView.findViewById(R.id.set_display_direct);
        set_display_direct.setChecked(display_direct);

        set_display_direct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_DISPLAY_DIRECT, set_display_direct.isChecked());
                editor.apply();
                if( getActivity() != null)
                    getActivity().recreate();
                Intent intent = new Intent(context, MainActivity.class);
                if(getActivity() != null)
                    getActivity().finish();
                startActivity(intent);
            }
        });

        boolean display_local = sharedpreferences.getBoolean(Helper.SET_DISPLAY_LOCAL, true);
        final CheckBox set_display_local = rootView.findViewById(R.id.set_display_local);
        set_display_local.setChecked(display_local);

        set_display_local.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_DISPLAY_LOCAL, set_display_local.isChecked());
                editor.apply();
                if( getActivity() != null)
                    getActivity().recreate();
                Intent intent = new Intent(context, MainActivity.class);
                if(getActivity() != null)
                    getActivity().finish();
                startActivity(intent);
            }
        });

        boolean display_global = sharedpreferences.getBoolean(Helper.SET_DISPLAY_GLOBAL, true);
        final CheckBox set_display_global = rootView.findViewById(R.id.set_display_global);
        set_display_global.setChecked(display_global);

        set_display_global.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_DISPLAY_GLOBAL, set_display_global.isChecked());
                editor.apply();
                if( getActivity() != null)
                    getActivity().recreate();
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra(INTENT_ACTION, BACK_TO_SETTINGS);
                if(getActivity() != null)
                    getActivity().finish();
                startActivity(intent);
            }
        });


        boolean display_art = sharedpreferences.getBoolean(Helper.SET_DISPLAY_ART, true);
        final CheckBox set_display_art = rootView.findViewById(R.id.set_display_art);
        set_display_art.setChecked(display_art);

        set_display_art.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_DISPLAY_ART, set_display_art.isChecked());
                editor.apply();
                if( getActivity() != null)
                    getActivity().recreate();
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra(INTENT_ACTION, BACK_TO_SETTINGS);
                if(getActivity() != null)
                    getActivity().finish();
                startActivity(intent);
            }
        });

        boolean disableGif = sharedpreferences.getBoolean(Helper.SET_DISABLE_GIF, false);
        final CheckBox set_disable_gif = rootView.findViewById(R.id.set_disable_gif);
        set_disable_gif.setChecked(disableGif);
        set_disable_gif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_DISABLE_GIF, set_disable_gif.isChecked());
                editor.apply();
                if( getActivity() != null)
                    getActivity().recreate();
            }
        });


        boolean livenotif = sharedpreferences.getBoolean(Helper.SET_LIVE_NOTIFICATIONS, true);
        final CheckBox set_live_notif = rootView.findViewById(R.id.set_live_notify);
        set_live_notif.setChecked(livenotif);
        set_live_notif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_LIVE_NOTIFICATIONS, set_live_notif.isChecked());
                editor.putBoolean(Helper.SHOULD_CONTINUE_STREAMING, set_live_notif.isChecked());
                editor.apply();
                if( set_live_notif.isChecked() ){
                    try {
                        ((MainActivity) context).startSreaming();
                    }catch (Exception ignored){ignored.printStackTrace();}
                }else{
                    context.sendBroadcast(new Intent("StopLiveNotificationService"));
                }
            }
        });

        boolean keep_background_process = sharedpreferences.getBoolean(Helper.SET_KEEP_BACKGROUND_PROCESS, true);
        final CheckBox set_keep_background_process = rootView.findViewById(R.id.set_keep_background_process);
        set_keep_background_process.setChecked(keep_background_process);
        set_keep_background_process.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_KEEP_BACKGROUND_PROCESS, set_keep_background_process.isChecked());
                editor.apply();
            }
        });


        boolean capitalize = sharedpreferences.getBoolean(Helper.SET_CAPITALIZE, true);
        final CheckBox set_capitalize = rootView.findViewById(R.id.set_capitalize);
        set_capitalize.setChecked(capitalize);

        set_capitalize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_CAPITALIZE, set_capitalize.isChecked());
                editor.apply();
            }
        });


        final CheckBox set_embedded_browser = rootView.findViewById(R.id.set_embedded_browser);
        final LinearLayout set_javascript_container = rootView.findViewById(R.id.set_javascript_container);
        final CheckBox set_custom_tabs  = rootView.findViewById(R.id.set_custom_tabs);
        final SwitchCompat set_javascript = rootView.findViewById(R.id.set_javascript);
        boolean javascript = sharedpreferences.getBoolean(Helper.SET_JAVASCRIPT, true);
        boolean embedded_browser = sharedpreferences.getBoolean(Helper.SET_EMBEDDED_BROWSER, true);
        boolean custom_tabs = sharedpreferences.getBoolean(Helper.SET_CUSTOM_TABS, true);
        if( !embedded_browser){
            set_javascript_container.setVisibility(View.GONE);
            set_custom_tabs.setVisibility(View.VISIBLE);
        }else{
            set_javascript_container.setVisibility(View.VISIBLE);
            set_custom_tabs.setVisibility(View.GONE);
        }
        set_embedded_browser.setChecked(embedded_browser);
        set_embedded_browser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_EMBEDDED_BROWSER, set_embedded_browser.isChecked());
                editor.apply();
                if( !set_embedded_browser.isChecked()){
                    set_javascript_container.setVisibility(View.GONE);
                    set_custom_tabs.setVisibility(View.VISIBLE);
                }else{
                    set_javascript_container.setVisibility(View.VISIBLE);
                    set_custom_tabs.setVisibility(View.GONE);
                }
            }
        });

        set_javascript.setChecked(javascript);
        set_javascript.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_JAVASCRIPT, isChecked);
                editor.apply();
            }
        });

        set_custom_tabs.setChecked(custom_tabs);
        set_custom_tabs.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_CUSTOM_TABS, isChecked);
                editor.apply();
            }
        });

        final LinearLayout set_cookies_container = rootView.findViewById(R.id.set_cookies_container);
        final SwitchCompat set_cookies = rootView.findViewById(R.id.set_cookies);
        boolean cookies = sharedpreferences.getBoolean(Helper.SET_COOKIES, false);

        set_cookies.setChecked(cookies);
        set_cookies.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_COOKIES, isChecked);
                editor.apply();
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            set_cookies_container.setVisibility(View.VISIBLE);
        }else {
            set_cookies_container.setVisibility(View.GONE);
        }
        final String targeted_folder = sharedpreferences.getString(Helper.SET_FOLDER_RECORD, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());

        set_folder = rootView.findViewById(R.id.set_folder);
        set_folder.setText(targeted_folder);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            set_folder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    @SuppressLint("InlinedApi") Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
                }
            });
        }else {
            LinearLayout file_chooser = rootView.findViewById(R.id.file_chooser);
            file_chooser.setVisibility(View.GONE);
        }

        final Spinner set_night_mode = rootView.findViewById(R.id.set_night_mode);
        ArrayAdapter<CharSequence> adapterTheme = ArrayAdapter.createFromResource(getContext(),
                R.array.settings_theme, android.R.layout.simple_spinner_item);
        set_night_mode.setAdapter(adapterTheme);

        int positionSpinnerTheme;
        switch (sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK)){
            case Helper.THEME_DARK:
                positionSpinnerTheme = 0;
                break;
            case Helper.THEME_LIGHT:
                positionSpinnerTheme = 1;
                break;
            case Helper.THEME_BLACK:
                positionSpinnerTheme = 2;
                break;
            default:
                positionSpinnerTheme = 0;
        }
        set_night_mode.setSelection(positionSpinnerTheme);
        set_night_mode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if( count1 > 0 ) {
                    SharedPreferences.Editor editor = sharedpreferences.edit();

                    switch (position) {
                        case 0:
                            editor.putInt(Helper.SET_THEME, Helper.THEME_DARK);
                            editor.apply();
                            break;
                        case 1:
                            editor.putInt(Helper.SET_THEME, Helper.THEME_LIGHT);
                            editor.apply();
                            break;
                        case 2:
                            editor.putInt(Helper.SET_THEME, Helper.THEME_BLACK);
                            editor.apply();
                            break;
                    }
                    if (getActivity() != null)
                        getActivity().recreate();
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra(INTENT_ACTION, BACK_TO_SETTINGS);
                    startActivity(intent);
                }
                count1++;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        // NSFW Timeout
        SeekBar nsfwTimeoutSeekBar = rootView.findViewById(R.id.set_nsfw_timeout);
        final TextView set_nsfw_timeout_value = rootView.findViewById(R.id.set_nsfw_timeout_value);

        nsfwTimeoutSeekBar.setMax(30);

        int nsfwTimeout = sharedpreferences.getInt(Helper.SET_NSFW_TIMEOUT, 5);

        nsfwTimeoutSeekBar.setProgress(nsfwTimeout);
        set_nsfw_timeout_value.setText(String.valueOf(nsfwTimeout));

        nsfwTimeoutSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                set_nsfw_timeout_value.setText(String.valueOf(progress));

                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putInt(Helper.SET_NSFW_TIMEOUT, progress);
                editor.apply();
            }
        });





        LinearLayout toot_visibility_container = rootView.findViewById(R.id.toot_visibility_container);
        SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        String instance = sharedpreferences.getString(Helper.PREF_INSTANCE, Helper.getLiveInstance(context));
        final Account account = new AccountDAO(context, db).getUniqAccount(userId, instance);
        final ImageView set_toot_visibility = rootView.findViewById(R.id.set_toot_visibility);
        if( theme == Helper.THEME_DARK){
            changeDrawableColor(context, set_toot_visibility, R.color.dark_text);
        }else {
            changeDrawableColor(context, set_toot_visibility, R.color.white);
        }
        //Only displayed for non locked accounts
        if (account != null ) {
            String defaultVisibility = account.isLocked()?"private":"public";
            String tootVisibility = sharedpreferences.getString(Helper.SET_TOOT_VISIBILITY + "@" + account.getAcct() + "@" + account.getInstance(), defaultVisibility);
            switch (tootVisibility) {
                case "public":
                    set_toot_visibility.setImageResource(R.drawable.ic_public);
                    break;
                case "unlisted":
                    set_toot_visibility.setImageResource(R.drawable.ic_lock_open);
                    break;
                case "private":
                    set_toot_visibility.setImageResource(R.drawable.ic_lock_outline);
                    break;
                case "direct":
                    set_toot_visibility.setImageResource(R.drawable.ic_mail_outline);
                    break;
            }
        }else {
            toot_visibility_container.setVisibility(View.GONE);
        }

        set_toot_visibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
                int style;
                if (theme == Helper.THEME_DARK) {
                    style = R.style.DialogDark;
                } else if (theme == Helper.THEME_BLACK){
                    style = R.style.DialogBlack;
                }else {
                    style = R.style.Dialog;
                }
                AlertDialog.Builder dialog = new AlertDialog.Builder(context, style);
                dialog.setTitle(R.string.toot_visibility_tilte);
                final String[] stringArray = getResources().getStringArray(R.array.toot_visibility);
                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, stringArray);
                dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        dialog.dismiss();
                    }
                });

                dialog.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        String visibility = "public";

                        switch (position){
                            case 0:
                                visibility = "public";
                                set_toot_visibility.setImageResource(R.drawable.ic_public);
                                break;
                            case 1:
                                visibility = "unlisted";
                                set_toot_visibility.setImageResource(R.drawable.ic_lock_open);
                                break;
                            case 2:
                                visibility = "private";
                                set_toot_visibility.setImageResource(R.drawable.ic_lock_outline);
                                break;
                            case 3:
                                visibility = "direct";
                                set_toot_visibility.setImageResource(R.drawable.ic_mail_outline);
                                break;
                        }
                        if( account != null) {
                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.putString(Helper.SET_TOOT_VISIBILITY + "@" + account.getAcct() + "@" + account.getInstance(), visibility);
                            editor.apply();
                            Toasty.info(context, context.getString(R.string.toast_visibility_changed, "@" + account.getAcct() + "@" + account.getInstance()), Toast.LENGTH_SHORT).show();
                        }else {
                            Toasty.error(context, context.getString(R.string.toast_error),Toast.LENGTH_LONG).show();
                        }

                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });


        boolean trans_forced = sharedpreferences.getBoolean(Helper.SET_TRANS_FORCED, false);
        final CheckBox set_trans_forced = rootView.findViewById(R.id.set_trans_forced);
        set_trans_forced.setChecked(trans_forced);
        set_trans_forced.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_TRANS_FORCED, set_trans_forced.isChecked());
                editor.apply();
            }
        });


        boolean split_toot = sharedpreferences.getBoolean(Helper.SET_AUTOMATICALLY_SPLIT_TOOTS, false);
        final CheckBox set_split_toot = rootView.findViewById(R.id.set_automatically_split_toot);
        set_split_toot.setChecked(split_toot);
        set_split_toot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_AUTOMATICALLY_SPLIT_TOOTS, set_split_toot.isChecked());
                editor.apply();
            }
        });

        //Translators
        final Spinner translation_layout_spinner = rootView.findViewById(R.id.translation_layout_spinner);
        ArrayAdapter<CharSequence> adapterTrans = ArrayAdapter.createFromResource(getContext(),
                R.array.settings_translation, android.R.layout.simple_spinner_item);
        translation_layout_spinner.setAdapter(adapterTrans);

        int positionSpinnerTrans;
        switch (sharedpreferences.getInt(Helper.SET_TRANSLATOR, Helper.TRANS_YANDEX)){
            case Helper.TRANS_YANDEX:
                positionSpinnerTrans = 0;
                your_api_key.setVisibility(View.VISIBLE);
                your_api_key.setText(sharedpreferences.getString(Helper.SET_YANDEX_API_KEY, ""));
                break;
            case Helper.TRANS_DEEPL:
                positionSpinnerTrans = 1;
                your_api_key.setVisibility(View.VISIBLE);
                your_api_key.setText(sharedpreferences.getString(Helper.SET_DEEPL_API_KEY, ""));
                break;
            case Helper.TRANS_NONE:
                positionSpinnerTrans = 2;
                your_api_key.setVisibility(View.GONE);
                break;
            default:
                your_api_key.setVisibility(View.VISIBLE);
                positionSpinnerTrans = 0;
        }
        translation_layout_spinner.setSelection(positionSpinnerTrans);
        translation_layout_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if( count3 > 0 ) {
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    switch (position) {
                        case 0:
                            your_api_key.setVisibility(View.VISIBLE);
                            editor.putInt(Helper.SET_TRANSLATOR, Helper.TRANS_YANDEX);
                            editor.apply();
                            if (sharedpreferences.getString(Helper.SET_DEEPL_API_KEY, null) != null)
                                your_api_key.setText(sharedpreferences.getString(Helper.SET_DEEPL_API_KEY, ""));
                            break;
                        case 1:
                            your_api_key.setVisibility(View.VISIBLE);
                            editor.putInt(Helper.SET_TRANSLATOR, Helper.TRANS_DEEPL);
                            editor.apply();
                            if (sharedpreferences.getString(SET_YANDEX_API_KEY, null) != null)
                                your_api_key.setText(sharedpreferences.getString(SET_YANDEX_API_KEY, null));
                            break;
                        case 2:
                            your_api_key.setVisibility(View.GONE);
                            set_trans_forced.isChecked();
                            editor.putBoolean(Helper.SET_TRANS_FORCED, false);
                            editor.putInt(Helper.SET_TRANSLATOR, Helper.TRANS_NONE);
                            editor.apply();
                            break;
                    }
                    if( getActivity() != null)
                        getActivity().recreate();
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra(INTENT_ACTION, BACK_TO_SETTINGS);
                    startActivity(intent);
                }
                count3++;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Resize
        final Spinner resize_layout_spinner = rootView.findViewById(R.id.set_resize_picture);
        ArrayAdapter<CharSequence> adapterResize = ArrayAdapter.createFromResource(getContext(),
                R.array.settings_resize_picture, android.R.layout.simple_spinner_item);
        resize_layout_spinner.setAdapter(adapterResize);
        int positionSpinnerResize = sharedpreferences.getInt(Helper.SET_PICTURE_RESIZE, Helper.S_NO);
        resize_layout_spinner.setSelection(positionSpinnerResize);
        resize_layout_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if( count4 > 0) {
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putInt(Helper.SET_PICTURE_RESIZE, position);
                    editor.apply();
                }
                count4++;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        return rootView;
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;

        if(requestCode == ACTIVITY_CHOOSE_FILE) {
            Uri treeUri = data.getData();
            Uri docUri = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri,
                        DocumentsContract.getTreeDocumentId(treeUri));
            }
            try{
                String path = getPath(context, docUri);
                if( path == null )
                    path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(Helper.SET_FOLDER_RECORD, path);
                editor.apply();
                set_folder.setText(path);
            }catch (Exception e){
                Toasty.error(context, context.getString(R.string.toast_error),Toast.LENGTH_LONG).show();
            }

        }
    }


    @Override
    public void onCreate(Bundle saveInstance) {
        super.onCreate(saveInstance);
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    //From: https://gist.github.com/asifmujteba/d89ba9074bc941de1eaa#file-asfurihelper
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

}
