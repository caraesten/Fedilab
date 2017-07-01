package fr.gouv.etalab.mastodon.fragments;
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
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import fr.gouv.etalab.mastodon.helper.Helper;
import mastodon.etalab.gouv.fr.mastodon.R;

import static android.app.Activity.RESULT_OK;


/**
 * Created by Thomas on 24/06/2017.
 * Fragment for settings, yes I didn't use PreferenceFragment :)
 */
public class SettingsFragment extends Fragment {


    private Context context;
    private static final int ACTIVITY_CHOOSE_FILE = 411;
    private TextView set_folder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        context = getContext();
        final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);


        boolean show_reply = sharedpreferences.getBoolean(Helper.SET_SHOW_REPLY, false);
        final CheckBox set_show_reply = (CheckBox) rootView.findViewById(R.id.set_show_reply);
        set_show_reply.setChecked(show_reply);

        set_show_reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_SHOW_REPLY, set_show_reply.isChecked());
                editor.apply();
            }
        });

        boolean show_error_messages = sharedpreferences.getBoolean(Helper.SET_SHOW_ERROR_MESSAGES, true);
        final CheckBox set_show_error_messages = (CheckBox) rootView.findViewById(R.id.set_show_error_messages);
        set_show_error_messages.setChecked(show_error_messages);

        set_show_error_messages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_SHOW_ERROR_MESSAGES, set_show_error_messages.isChecked());
                editor.apply();
            }
        });


        boolean notif_validation = sharedpreferences.getBoolean(Helper.SET_NOTIF_VALIDATION, true);
        final CheckBox set_share_validation = (CheckBox) rootView.findViewById(R.id.set_share_validation);
        set_share_validation.setChecked(notif_validation);

        set_share_validation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_NOTIF_VALIDATION, set_share_validation.isChecked());
                editor.apply();
            }
        });


        final CheckBox set_embedded_browser = (CheckBox) rootView.findViewById(R.id.set_embedded_browser);
        final LinearLayout set_javascript_container = (LinearLayout) rootView.findViewById(R.id.set_javascript_container);
        final SwitchCompat set_javascript = (SwitchCompat) rootView.findViewById(R.id.set_javascript);
        boolean javascript = sharedpreferences.getBoolean(Helper.SET_JAVASCRIPT, true);
        boolean embedded_browser = sharedpreferences.getBoolean(Helper.SET_EMBEDDED_BROWSER, true);
        if( !embedded_browser){
            set_javascript_container.setVisibility(View.GONE);
        }else{
            set_javascript_container.setVisibility(View.VISIBLE);
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
                }else{
                    set_javascript_container.setVisibility(View.VISIBLE);
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

        final LinearLayout set_cookies_container = (LinearLayout) rootView.findViewById(R.id.set_cookies_container);
        final SwitchCompat set_cookies = (SwitchCompat) rootView.findViewById(R.id.set_cookies);
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
        final String targeted_folder = sharedpreferences.getString(Helper.SET_FOLDER_RECORD, Environment.DIRECTORY_DOWNLOADS);

        set_folder = (TextView) rootView.findViewById(R.id.set_folder);
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
            LinearLayout file_chooser = (LinearLayout) rootView.findViewById(R.id.file_chooser);
            file_chooser.setVisibility(View.GONE);
        }

        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_LIGHT);
        final SwitchCompat set_night_mode = (SwitchCompat) rootView.findViewById(R.id.set_night_mode);
        set_night_mode.setChecked(theme == Helper.THEME_DARK);
        set_night_mode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putInt(Helper.SET_THEME, isChecked?Helper.THEME_DARK:Helper.THEME_LIGHT);
                editor.apply();
                if( isChecked){
                    getActivity().setTheme(R.style.AppThemeDark);
                }else {
                    getActivity().setTheme(R.style.AppTheme);
                }
                getActivity().recreate();
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
                path = Environment.DIRECTORY_DOWNLOADS;
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
