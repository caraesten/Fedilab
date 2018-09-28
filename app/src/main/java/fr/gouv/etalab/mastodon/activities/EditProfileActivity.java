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


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveAccountInfoAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.UpdateCredentialAsyncTask;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Error;
import fr.gouv.etalab.mastodon.client.Entities.Version;
import fr.gouv.etalab.mastodon.client.Glide.GlideApp;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveAccountInterface;
import fr.gouv.etalab.mastodon.interfaces.OnUpdateCredentialInterface;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;




/**
 * Created by Thomas on 27/08/2017.
 * Edit profile activity
 */

public class EditProfileActivity extends BaseActivity implements OnRetrieveAccountInterface, OnUpdateCredentialInterface {



    private EditText set_profile_name, set_profile_description;
    private ImageView set_profile_picture, set_header_picture;
    private Button set_change_profile_picture, set_change_header_picture, set_profile_save;
    private TextView set_header_picture_overlay;
    private CheckBox set_lock_account;
    private static final int PICK_IMAGE_HEADER = 4565;
    private static final int PICK_IMAGE_PROFILE = 6545;
    private String profile_username, profile_note;
    private ByteArrayInputStream profile_picture, header_picture;
    private API.accountPrivacy profile_privacy;
    private Bitmap profile_picture_bmp, profile_header_bmp;
    private ImageView pp_actionBar;
    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_HEADER = 754;
    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_PICTURE = 755;
    private String avatarName, headerName;

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

        setContentView(R.layout.activity_edit_profile);

        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.simple_action_bar, null);
            actionBar.setCustomView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            TextView title = actionBar.getCustomView().findViewById(R.id.toolbar_title);
            pp_actionBar = actionBar.getCustomView().findViewById(R.id.pp_actionBar);
            title.setText(R.string.settings_title_profile);
            ImageView close_conversation = actionBar.getCustomView().findViewById(R.id.close_conversation);
            if( close_conversation != null){
                close_conversation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
            }
        }else{
            setTitle(R.string.settings_title_profile);
        }
        SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        Account account = new AccountDAO(getApplicationContext(),db).getAccountByID(userId);
        String url = account.getAvatar();
        if( url.startsWith("/") ){
            url = Helper.getLiveInstanceWithProtocol(getApplicationContext()) + account.getAvatar();
        }


        Glide.with(getApplicationContext())
                .asBitmap()
                .load(url)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                        BitmapDrawable ppDrawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(resource, (int) Helper.convertDpToPixel(25, getApplicationContext()), (int) Helper.convertDpToPixel(25, getApplicationContext()), true));
                        if( pp_actionBar != null){
                            pp_actionBar.setImageDrawable(ppDrawable);
                        } else if( getSupportActionBar() != null){

                            getSupportActionBar().setIcon(ppDrawable);
                            getSupportActionBar().setDisplayShowHomeEnabled(true);
                        }
                    }
                });


        set_profile_name = findViewById(R.id.set_profile_name);
        set_profile_description = findViewById(R.id.set_profile_description);
        set_profile_picture = findViewById(R.id.set_profile_picture);
        set_header_picture = findViewById(R.id.set_header_picture);
        set_change_profile_picture = findViewById(R.id.set_change_profile_picture);
        set_change_header_picture = findViewById(R.id.set_change_header_picture);
        set_profile_save = findViewById(R.id.set_profile_save);
        set_header_picture_overlay = findViewById(R.id.set_header_picture_overlay);
        set_lock_account = findViewById(R.id.set_lock_account);

        String instance = sharedpreferences.getString(Helper.PREF_INSTANCE, Helper.getLiveInstance(getApplicationContext()));
        String instanceVersion = sharedpreferences.getString(Helper.INSTANCE_VERSION + userId + instance, null);
        Version currentVersion = new Version(instanceVersion);
        Version minVersion = new Version("2.3");
        if(currentVersion.compareTo(minVersion) == 1)
            set_lock_account.setVisibility(View.VISIBLE);
        else
            set_lock_account.setVisibility(View.GONE);
        set_profile_save.setEnabled(false);
        set_change_header_picture.setEnabled(false);
        set_change_profile_picture.setEnabled(false);
        set_profile_name.setEnabled(false);
        set_profile_description.setEnabled(false);
        set_lock_account.setEnabled(false);

        new RetrieveAccountInfoAsyncTask(getApplicationContext(), EditProfileActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        if( theme == Helper.THEME_LIGHT) {
            set_profile_save.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        }
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
    public void onRetrieveAccount(Account account, Error error) {
        if( error != null ){
            Toast.makeText(getApplicationContext(),R.string.toast_error, Toast.LENGTH_LONG).show();
            return;
        }
        set_profile_name.setText(account.getDisplay_name());
        set_profile_name.setSelection(set_profile_name.getText().length());

        final String content;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            content = Html.fromHtml(account.getNote(), Html.FROM_HTML_MODE_LEGACY).toString();
        else
            //noinspection deprecation
            content = Html.fromHtml(account.getNote()).toString();
        set_profile_description.setText(content);

        set_profile_save.setEnabled(true);
        set_change_header_picture.setEnabled(true);
        set_change_profile_picture.setEnabled(true);
        set_profile_name.setEnabled(true);
        set_profile_description.setEnabled(true);
        set_lock_account.setEnabled(true);
        if( account.isLocked())
            set_lock_account.setChecked(true);
        else
            set_lock_account.setChecked(false);
        set_profile_description.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if( s.length() > 160){
                    String content = s.toString().substring(0,160);
                    set_profile_description.setText(content);
                    set_profile_description.setSelection(set_profile_description.getText().length());
                    Toast.makeText(getApplicationContext(),R.string.note_no_space,Toast.LENGTH_LONG).show();
                }
            }
        });
        if ( account.getFields() != null && account.getFields().size() > 0){
            HashMap<String, String> fields = account.getFields();
            Iterator it = fields.entrySet().iterator();
            int i = 1;
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                String label = (String)pair.getKey();
                String value = (String)pair.getValue();
                EditText labelView;
                EditText valueView;
                switch(i){
                    case 1:
                        labelView = findViewById(R.id.cf_key_1);
                        valueView = findViewById(R.id.cf_val_1);
                        break;
                    case 2:
                        labelView = findViewById(R.id.cf_key_2);
                        valueView = findViewById(R.id.cf_val_2);
                        break;
                    case 3:
                        labelView = findViewById(R.id.cf_key_3);
                        valueView = findViewById(R.id.cf_val_3);
                        break;
                    case 4:
                        labelView = findViewById(R.id.cf_key_4);
                        valueView = findViewById(R.id.cf_val_4);
                        break;
                    default:
                        labelView = findViewById(R.id.cf_key_1);
                        valueView = findViewById(R.id.cf_val_1);
                        break;
                }
                labelView.setText(label);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    value = Html.fromHtml(value, Html.FROM_HTML_MODE_LEGACY).toString();
                else
                    //noinspection deprecation
                    value = Html.fromHtml(value).toString();
                valueView.setText(value);
                i++;
            }
        }

        set_profile_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if( s.length() > 30){
                    String content = s.toString().substring(0,30);
                    set_profile_name.setText(content);
                    set_profile_name.setSelection(set_profile_name.getText().length());
                    Toast.makeText(getApplicationContext(),R.string.username_no_space,Toast.LENGTH_LONG).show();
                }
            }
        });


        set_change_header_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    if (ContextCompat.checkSelfPermission(EditProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                            PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(EditProfileActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_HEADER);
                        return;
                    }
                }
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, getString(R.string.toot_select_image));
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});
                startActivityForResult(chooserIntent, PICK_IMAGE_HEADER);
            }
        });

        set_change_profile_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    if (ContextCompat.checkSelfPermission(EditProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                            PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(EditProfileActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_PICTURE);
                        return;
                    }
                }

                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, getString(R.string.toot_select_image));
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});
                startActivityForResult(chooserIntent, PICK_IMAGE_PROFILE);
            }
        });

        Glide.with(set_profile_picture.getContext())
                .load(account.getAvatar())
                .into(set_profile_picture);
        Glide.with(set_header_picture.getContext())
                .load(account.getHeader())
                .into(set_header_picture);
        if( account.getHeader() == null || account.getHeader().contains("missing.png"))
            set_header_picture_overlay.setVisibility(View.VISIBLE);


        set_profile_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(set_profile_name.getText() != null && !set_profile_name.getText().toString().equals(set_profile_name.getHint()))
                    profile_username = set_profile_name.getText().toString().trim();
                else
                    profile_username = null;

                if(set_profile_description.getText() != null && !set_profile_description.getText().toString().equals(set_profile_description.getHint()))
                    profile_note = set_profile_description.getText().toString().trim();
                else
                    profile_note = null;

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(EditProfileActivity.this);
                LayoutInflater inflater = EditProfileActivity.this.getLayoutInflater();
                @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.dialog_profile, null);
                dialogBuilder.setView(dialogView);

                ImageView back_ground_image = dialogView.findViewById(R.id.back_ground_image);
                ImageView dialog_profile_picture = dialogView.findViewById(R.id.dialog_profile_picture);
                TextView dialog_profile_name = dialogView.findViewById(R.id.dialog_profile_name);
                TextView dialog_profile_description = dialogView.findViewById(R.id.dialog_profile_description);

                if( profile_username != null)
                    dialog_profile_name.setText(profile_username);
                if( profile_note != null)
                    dialog_profile_description.setText(profile_note);
                if( profile_header_bmp != null) {
                    BitmapDrawable background = new BitmapDrawable(getApplicationContext().getResources(), profile_header_bmp);
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        //noinspection deprecation
                        back_ground_image.setBackgroundDrawable(background);
                    } else {
                        back_ground_image.setBackground(background);
                    }
                }else {
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        //noinspection deprecation
                        back_ground_image.setBackgroundDrawable(set_header_picture.getDrawable());
                    } else {
                        back_ground_image.setBackground(set_header_picture.getDrawable());
                    }
                }
                if( profile_picture_bmp != null) {
                    BitmapDrawable background = new BitmapDrawable(getApplicationContext().getResources(), profile_picture_bmp);
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        //noinspection deprecation
                        dialog_profile_picture.setBackgroundDrawable(background);
                    } else {
                        dialog_profile_picture.setBackground(background);
                    }
                }else {
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        //noinspection deprecation
                        dialog_profile_picture.setBackgroundDrawable(set_profile_picture.getDrawable());
                    } else {
                        dialog_profile_picture.setBackground(set_profile_picture.getDrawable());
                    }
                }
                profile_privacy = set_lock_account.isChecked()?API.accountPrivacy.LOCKED:API.accountPrivacy.PUBLIC;
                dialogBuilder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        set_profile_save.setEnabled(false);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                GlideApp.get(getApplicationContext()).clearDiskCache();
                            }
                        }).start();
                        GlideApp.get(getApplicationContext()).clearMemory();
                        HashMap<String, String> newCustomFields = new HashMap<>();

                        String key1, key2, key3, key4, val1, val2, val3, val4;
                        key1 = ((EditText)findViewById(R.id.cf_key_1)).getText().toString();
                        key2 = ((EditText)findViewById(R.id.cf_key_2)).getText().toString();
                        key3 = ((EditText)findViewById(R.id.cf_key_3)).getText().toString();
                        key4 = ((EditText)findViewById(R.id.cf_key_4)).getText().toString();

                        val1 = ((EditText)findViewById(R.id.cf_val_1)).getText().toString();
                        val2 = ((EditText)findViewById(R.id.cf_val_2)).getText().toString();
                        val3 = ((EditText)findViewById(R.id.cf_val_3)).getText().toString();
                        val4 = ((EditText)findViewById(R.id.cf_val_4)).getText().toString();

                        newCustomFields.put(key1,val1);
                        newCustomFields.put(key2,val2);
                        newCustomFields.put(key3,val3);
                        newCustomFields.put(key4,val4);

                        new UpdateCredentialAsyncTask(getApplicationContext(), newCustomFields, profile_username, profile_note, profile_picture, avatarName,  header_picture, headerName, profile_privacy, EditProfileActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                });
                dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.show();

            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_HEADER: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // We have the permission.
                    set_change_header_picture.callOnClick();
                }
                break;

            }
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE_PICTURE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // We have the permission.
                    set_change_profile_picture.callOnClick();
                }
                break;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_HEADER && resultCode == Activity.RESULT_OK) {
            if (data == null || data.getData() == null) {
                Toast.makeText(getApplicationContext(),R.string.toot_select_image_error,Toast.LENGTH_LONG).show();
                return;
            }
            Uri fileUri = data.getData();
            headerName = Helper.getFileName(EditProfileActivity.this, fileUri);
            try {
                InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(fileUri);
                assert inputStream != null;
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                Bitmap bmp = BitmapFactory.decodeStream(bufferedInputStream);
                profile_header_bmp = ThumbnailUtils.extractThumbnail(bmp, 700, 335);
                set_header_picture.setImageBitmap(profile_header_bmp);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            header_picture = Helper.compressImage(EditProfileActivity.this, fileUri, Helper.MediaType.MEDIA);
        }else if(requestCode == PICK_IMAGE_PROFILE && resultCode == Activity.RESULT_OK) {
            if (data == null || data.getData() == null) {
                Toast.makeText(getApplicationContext(),R.string.toot_select_image_error,Toast.LENGTH_LONG).show();
                return;
            }
            Uri fileUri = data.getData();
            avatarName = Helper.getFileName(EditProfileActivity.this, fileUri);
            try {
                InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(fileUri);
                assert inputStream != null;
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                Bitmap bmp = BitmapFactory.decodeStream(bufferedInputStream);
                profile_picture_bmp = ThumbnailUtils.extractThumbnail(bmp, 400, 400);
                set_profile_picture.setImageBitmap(profile_picture_bmp);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            profile_picture = Helper.compressImage(EditProfileActivity.this, fileUri, Helper.MediaType.PROFILE);
        }
    }

    @Override
    public void onUpdateCredential(APIResponse apiResponse) {
        set_profile_save.setEnabled(true);
        if( apiResponse.getError() != null){
            Toast.makeText(getApplicationContext(), R.string.toast_error, Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(getApplicationContext(), R.string.toast_update_credential_ok, Toast.LENGTH_LONG).show();

        Intent mStartActivity = new Intent(EditProfileActivity.this, BaseMainActivity.class);
        int mPendingIntentId = 45641;
        PendingIntent mPendingIntent = PendingIntent.getActivity(EditProfileActivity.this, mPendingIntentId, mStartActivity,
                PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) EditProfileActivity.this.getSystemService(Context.ALARM_SERVICE);
        assert mgr != null;
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
        finish();
    }

}
