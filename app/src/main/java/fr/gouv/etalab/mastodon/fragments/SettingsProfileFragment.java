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
import android.app.Activity;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import fr.gouv.etalab.mastodon.activities.MainActivity;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveAccountInfoAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.UpdateCredentialAsyncTask;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Error;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveAccountInterface;
import fr.gouv.etalab.mastodon.interfaces.OnUpdateCredentialInterface;
import mastodon.etalab.gouv.fr.mastodon.R;


/**
 * Created by Thomas on 04/06/2017.
 * Fragment for profile settings
 */
public class SettingsProfileFragment extends Fragment implements OnRetrieveAccountInterface, OnUpdateCredentialInterface {


    private Context context;
    private EditText set_profile_name, set_profile_description;
    private ImageView set_profile_picture, set_header_picture;
    private Button set_change_profile_picture, set_change_header_picture, set_profile_save;
    private TextView set_header_picture_overlay;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private static final int PICK_IMAGE_HEADER = 4565;
    private static final int PICK_IMAGE_PROFILE = 6545;
    private String profile_picture, header_picture, profile_username, profile_note;
    private Bitmap profile_picture_bmp, profile_header_bmp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_settings_profile, container, false);

        set_profile_name = (EditText) rootView.findViewById(R.id.set_profile_name);
        set_profile_description = (EditText) rootView.findViewById(R.id.set_profile_description);
        set_profile_picture = (ImageView) rootView.findViewById(R.id.set_profile_picture);
        set_header_picture = (ImageView) rootView.findViewById(R.id.set_header_picture);
        set_change_profile_picture = (Button) rootView.findViewById(R.id.set_change_profile_picture);
        set_change_header_picture = (Button) rootView.findViewById(R.id.set_change_header_picture);
        set_profile_save = (Button) rootView.findViewById(R.id.set_profile_save);
        set_header_picture_overlay = (TextView) rootView.findViewById(R.id.set_header_picture_overlay);

        set_profile_save.setEnabled(false);
        set_change_header_picture.setEnabled(false);
        set_change_profile_picture.setEnabled(false);
        set_profile_name.setEnabled(false);
        set_profile_description.setEnabled(false);
        context = getContext();
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder().displayer(new SimpleBitmapDisplayer()).cacheInMemory(false)
                .cacheOnDisk(true).resetViewBeforeLoading(true).build();
        new RetrieveAccountInfoAsyncTask(context, SettingsProfileFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        final int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if( theme == Helper.THEME_LIGHT) {
            set_profile_save.setTextColor(ContextCompat.getColor(context, R.color.white));
        }
        return rootView;
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


    @Override
    public void onRetrieveAccount(Account account, Error error) {
        if( error != null ){
            Toast.makeText(context,R.string.toast_error, Toast.LENGTH_LONG).show();
            return;
        }
        set_profile_name.setText(account.getDisplay_name());

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
                    Toast.makeText(context,R.string.note_no_space,Toast.LENGTH_LONG).show();
                }
            }
        });

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
                    Toast.makeText(context,R.string.username_no_space,Toast.LENGTH_LONG).show();
                }
            }
        });


        set_change_header_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, getString(R.string.toot_select_image));
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});
                startActivityForResult(chooserIntent, PICK_IMAGE_PROFILE);
            }
        });

        imageLoader.displayImage(account.getAvatar(), set_profile_picture, options);
        imageLoader.displayImage(account.getHeader(), set_header_picture, options);

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

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                LayoutInflater inflater = ((MainActivity) context).getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_profile, null);
                dialogBuilder.setView(dialogView);

                ImageView back_ground_image = (ImageView) dialogView.findViewById(R.id.back_ground_image);
                ImageView dialog_profile_picture = (ImageView) dialogView.findViewById(R.id.dialog_profile_picture);
                TextView dialog_profile_name = (TextView) dialogView.findViewById(R.id.dialog_profile_name);
                TextView dialog_profile_description = (TextView) dialogView.findViewById(R.id.dialog_profile_description);

                if( profile_username != null)
                    dialog_profile_name.setText(profile_username);
                if( profile_note != null)
                    dialog_profile_description.setText(profile_note);
                if( profile_header_bmp != null) {
                    BitmapDrawable background = new BitmapDrawable(context.getResources(), profile_header_bmp);
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
                    BitmapDrawable background = new BitmapDrawable(context.getResources(), profile_picture_bmp);
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
                dialogBuilder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        set_profile_save.setEnabled(false);
                        new UpdateCredentialAsyncTask(context, profile_username, profile_note, profile_picture, header_picture, SettingsProfileFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_HEADER && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Toast.makeText(context,R.string.toot_select_image_error,Toast.LENGTH_LONG).show();
                return;
            }
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(data.getData());
                BufferedInputStream bufferedInputStream;
                if (inputStream != null) {
                    bufferedInputStream = new BufferedInputStream(inputStream);
                }else {
                    Toast.makeText(context,R.string.toot_select_image_error,Toast.LENGTH_LONG).show();
                    return;
                }
                Bitmap bmp = BitmapFactory.decodeStream(bufferedInputStream);
                profile_header_bmp = Bitmap.createScaledBitmap(bmp, 700, 335, true);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                profile_header_bmp.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                set_header_picture.setImageBitmap(profile_header_bmp);
                byte[] byteArray = byteArrayOutputStream .toByteArray();
                header_picture = "data:image/png;base64, " + Base64.encodeToString(byteArray, Base64.DEFAULT);

            } catch (FileNotFoundException e) {
                Toast.makeText(context,R.string.toot_select_image_error,Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }else if(requestCode == PICK_IMAGE_PROFILE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Toast.makeText(context,R.string.toot_select_image_error,Toast.LENGTH_LONG).show();
                return;
            }
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(data.getData());
                BufferedInputStream bufferedInputStream;
                if (inputStream != null) {
                    bufferedInputStream = new BufferedInputStream(inputStream);
                }else {
                    Toast.makeText(context,R.string.toot_select_image_error,Toast.LENGTH_LONG).show();
                    return;
                }
                Bitmap bmp = BitmapFactory.decodeStream(bufferedInputStream);
                profile_picture_bmp = Bitmap.createScaledBitmap(bmp, 120, 120, true);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                profile_picture_bmp.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                set_profile_picture.setImageBitmap(profile_picture_bmp);
                byte[] byteArray = byteArrayOutputStream .toByteArray();
                profile_picture = "data:image/png;base64, " + Base64.encodeToString(byteArray, Base64.DEFAULT);
            } catch (FileNotFoundException e) {
                Toast.makeText(context,R.string.toot_select_image_error,Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onUpdateCredential(APIResponse apiResponse) {
        if( apiResponse.getError() != null){
            Toast.makeText(context, R.string.toast_error, Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(context, R.string.toast_update_credential_ok, Toast.LENGTH_LONG).show();
        set_profile_save.setEnabled(true);
    }
}
