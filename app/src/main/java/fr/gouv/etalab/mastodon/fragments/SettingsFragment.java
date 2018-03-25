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
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import fr.gouv.etalab.mastodon.activities.MainActivity;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import fr.gouv.etalab.mastodon.R;

import static android.app.Activity.RESULT_OK;
import static fr.gouv.etalab.mastodon.helper.Helper.CHANGE_THEME_INTENT;
import static fr.gouv.etalab.mastodon.helper.Helper.INTENT_ACTION;
import static fr.gouv.etalab.mastodon.helper.Helper.changeDrawableColor;


/**
 * Created by Thomas on 24/06/2017.
 * Fragment for settings, yes I didn't use PreferenceFragment :)
 */
public class SettingsFragment extends Fragment {


    private Context context;
    private static final int ACTIVITY_CHOOSE_FILE = 411;
    private TextView set_folder;
    int count1, count2 = 0;

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



        boolean show_error_messages = sharedpreferences.getBoolean(Helper.SET_SHOW_ERROR_MESSAGES, true);
        final CheckBox set_show_error_messages = rootView.findViewById(R.id.set_show_error_messages);
        set_show_error_messages.setChecked(show_error_messages);

        set_show_error_messages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_SHOW_ERROR_MESSAGES, set_show_error_messages.isChecked());
                editor.apply();
            }
        });

        boolean show_media_urls = sharedpreferences.getBoolean(Helper.SET_MEDIA_URLS, true);
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

        boolean preview_reply = sharedpreferences.getBoolean(Helper.SET_PREVIEW_REPLIES, false);
        final CheckBox set_preview_reply = rootView.findViewById(R.id.set_preview_reply);
        final LinearLayout set_preview_reply_pp_container = rootView.findViewById(R.id.set_preview_reply_pp_container);
        final SwitchCompat set_preview_reply_pp = rootView.findViewById(R.id.set_preview_reply_pp);
        set_preview_reply.setChecked(preview_reply);

        set_preview_reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_PREVIEW_REPLIES, set_preview_reply.isChecked());
                editor.apply();
                if( !set_preview_reply.isChecked()){
                    set_preview_reply_pp_container.setVisibility(View.GONE);
                }else{
                    set_preview_reply_pp_container.setVisibility(View.VISIBLE);
                }
            }
        });

        if( !preview_reply){
            set_preview_reply_pp_container.setVisibility(View.GONE);
        }else{
            set_preview_reply_pp_container.setVisibility(View.VISIBLE);
        }
        boolean preview_reply_pp = sharedpreferences.getBoolean(Helper.SET_PREVIEW_REPLIES_PP, false);
        set_preview_reply_pp.setChecked(preview_reply_pp);
        set_preview_reply_pp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_PREVIEW_REPLIES_PP, isChecked);
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


        boolean display_bookmark = sharedpreferences.getBoolean(Helper.SET_SHOW_BOOKMARK, true);
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

        boolean multiaccount_actions = sharedpreferences.getBoolean(Helper.SET_ALLOW_CROSS_ACTIONS, true);
        final CheckBox set_multiaccount_actions = rootView.findViewById(R.id.set_multiaccount_actions);
        set_multiaccount_actions.setChecked(multiaccount_actions);

        set_multiaccount_actions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_ALLOW_CROSS_ACTIONS, set_multiaccount_actions.isChecked());
                editor.apply();
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
                intent.putExtra(INTENT_ACTION, CHANGE_THEME_INTENT);
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


        boolean displayBoost = sharedpreferences.getBoolean(Helper.SET_DISPLAY_BOOST_COUNT, true);
        final CheckBox set_disable_counts = rootView.findViewById(R.id.set_disable_counts);
        set_disable_counts.setChecked(displayBoost);
        set_disable_counts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_DISPLAY_BOOST_COUNT, set_disable_counts.isChecked());
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
                intent.putExtra(INTENT_ACTION, CHANGE_THEME_INTENT);
                startActivity(intent);
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

        final SwitchCompat set_night_mode = rootView.findViewById(R.id.set_night_mode);
        set_night_mode.setChecked(theme == Helper.THEME_DARK);
        set_night_mode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putInt(Helper.SET_THEME, isChecked?Helper.THEME_DARK:Helper.THEME_LIGHT);
                editor.apply();
                if( isChecked){
                    if( getActivity() != null)
                        getActivity().setTheme(R.style.AppThemeDark);
                }else {
                    if( getActivity() != null)
                        getActivity().setTheme(R.style.AppTheme);
                }
                getActivity().recreate();
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra(INTENT_ACTION, CHANGE_THEME_INTENT);
                startActivity(intent);

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
        String prefKeyOauthTokenT = sharedpreferences.getString(Helper.PREF_KEY_OAUTH_TOKEN, null);
        SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        final Account account = new AccountDAO(context, db).getAccountByToken(prefKeyOauthTokenT);
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
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
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
                            Toast.makeText(context, context.getString(R.string.toast_visibility_changed, "@" + account.getAcct() + "@" + account.getInstance()), Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(context, R.string.toast_error,Toast.LENGTH_SHORT).show();
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

        //Translators
        final Spinner translation_layout_spinner = rootView.findViewById(R.id.translation_layout_spinner);
        ArrayAdapter<CharSequence> adapterTrans = ArrayAdapter.createFromResource(getContext(),
                R.array.settings_translation, android.R.layout.simple_spinner_item);
        translation_layout_spinner.setAdapter(adapterTrans);

        int positionSpinnerTrans;
        switch (sharedpreferences.getInt(Helper.SET_TRANSLATOR, Helper.TRANS_YANDEX)){
            case Helper.TRANS_YANDEX:
               positionSpinnerTrans = 0;
                break;
            case Helper.TRANS_NONE:
                positionSpinnerTrans = 1;
                break;
            default:
                positionSpinnerTrans = 0;
        }
        translation_layout_spinner.setSelection(positionSpinnerTrans);
        translation_layout_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if( count2 > 0){
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    switch (position){
                        case 0:
                            editor.putInt(Helper.SET_TRANSLATOR, Helper.TRANS_YANDEX);
                            editor.apply();
                            break;
                        case 1:
                            set_trans_forced.isChecked();
                            editor.putBoolean(Helper.SET_TRANS_FORCED, false);
                            editor.putInt(Helper.SET_TRANSLATOR, Helper.TRANS_NONE);
                            editor.apply();
                            break;
                    }
                    if( getActivity() != null)
                        getActivity().recreate();
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra(INTENT_ACTION, CHANGE_THEME_INTENT);
                    startActivity(intent);
                }else {
                    count2++;
                }
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
        int positionSpinnerResize = sharedpreferences.getInt(Helper.SET_PICTURE_RESIZE, Helper.S_1MO);
        resize_layout_spinner.setSelection(positionSpinnerResize);
        resize_layout_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if( count1 > 0){
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putInt(Helper.SET_PICTURE_RESIZE, position);
                    editor.apply();
                }else {
                    count1++;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        return rootView;
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;
        if(requestCode == ACTIVITY_CHOOSE_FILE) {
            Uri treeUri = data.getData();
            Uri docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri,
                    DocumentsContract.getTreeDocumentId(treeUri));
            String path = getPath(context, docUri);
            if( path == null )
                path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
            final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(Helper.SET_FOLDER_RECORD, path);
            editor.apply();
            set_folder.setText(path);
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
