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
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.media.ExifInterface;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.github.stom79.localepicker.CountryPicker;
import com.github.stom79.localepicker.CountryPickerListener;
import com.github.stom79.mytransl.MyTransL;
import com.github.stom79.mytransl.client.HttpsConnectionException;
import com.github.stom79.mytransl.translate.Translate;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import fr.gouv.etalab.mastodon.asynctasks.PostStatusAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveAccountsForReplyAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveEmojiAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveSearchAccountsAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveSearchAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.UpdateDescriptionAttachmentAsyncTask;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Attachment;
import fr.gouv.etalab.mastodon.client.Entities.Emojis;
import fr.gouv.etalab.mastodon.client.Entities.Error;
import fr.gouv.etalab.mastodon.client.Entities.Mention;
import fr.gouv.etalab.mastodon.client.Entities.Results;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.client.Entities.StoredStatus;
import fr.gouv.etalab.mastodon.client.Glide.GlideApp;
import fr.gouv.etalab.mastodon.client.HttpsConnection;
import fr.gouv.etalab.mastodon.drawers.CustomEmojiAdapter;
import fr.gouv.etalab.mastodon.drawers.EmojisSearchAdapter;
import fr.gouv.etalab.mastodon.interfaces.OnDownloadInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveEmojiInterface;
import fr.gouv.etalab.mastodon.sqlite.CustomEmojiDAO;
import fr.gouv.etalab.mastodon.client.Entities.Version;
import fr.gouv.etalab.mastodon.drawers.AccountsReplyAdapter;
import fr.gouv.etalab.mastodon.drawers.AccountsSearchAdapter;
import fr.gouv.etalab.mastodon.drawers.DraftsListAdapter;
import fr.gouv.etalab.mastodon.drawers.TagsSearchAdapter;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnPostStatusActionInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveAccountsReplyInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveAttachmentInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveSearcAccountshInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveSearchInterface;
import fr.gouv.etalab.mastodon.jobs.ScheduledTootsSyncJob;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import fr.gouv.etalab.mastodon.sqlite.StatusStoredDAO;
import fr.gouv.etalab.mastodon.R;

import static fr.gouv.etalab.mastodon.helper.Helper.HOME_TIMELINE_INTENT;
import static fr.gouv.etalab.mastodon.helper.Helper.INTENT_ACTION;
import static fr.gouv.etalab.mastodon.helper.Helper.changeDrawableColor;

/**
 * Created by Thomas on 01/05/2017.
 * Toot activity class
 */

public class TootActivity extends BaseActivity implements OnRetrieveSearcAccountshInterface, OnRetrieveAttachmentInterface, OnPostStatusActionInterface, OnRetrieveSearchInterface, OnRetrieveAccountsReplyInterface, OnRetrieveEmojiInterface, OnDownloadInterface {


    private String visibility;
    private final int PICK_IMAGE = 56556;
    private final int TAKE_PHOTO = 56532;
    private ImageButton toot_picture;
    private LinearLayout toot_picture_container;
    private ArrayList<Attachment> attachments;
    private boolean isSensitive = false;
    private ImageButton toot_visibility;
    private Button toot_it;
    private AutoCompleteTextView toot_content;
    private EditText toot_cw_content;
    private Status tootReply = null;
    private String tootMention = null;
    private String urlMention = null;
    private String fileMention = null;
    private String sharedContent, sharedSubject, sharedContentIni;
    private CheckBox toot_sensitive;
    public long currentToId;
    private long restored;
    private TextView title;
    private ImageView pp_actionBar;
    private ProgressBar pp_progress;
    private Toast mToast;
    private LinearLayout drawer_layout;
    private HorizontalScrollView picture_scrollview;
    private int currentCursorPosition, searchLength;
    private TextView toot_space_left;
    private String initialContent;
    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 754;
    private BroadcastReceiver receive_picture;
    private Account accountReply;
    private View popup_trans;
    private AlertDialog dialogTrans;
    private AlertDialog alertDialogEmoji;
    private String mentionAccount;
    private String idRedirect;
    private String userId, instance;
    private Account account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        instance = sharedpreferences.getString(Helper.PREF_INSTANCE, Helper.getLiveInstance(getApplicationContext()));
        final int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if( theme == Helper.THEME_LIGHT){
            setTheme(R.style.AppTheme);
        }else {
            setTheme(R.style.AppThemeDark);
        }
        setContentView(R.layout.activity_toot);

        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null ){
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.toot_action_bar, null);
            actionBar.setCustomView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

            ImageView close_toot = actionBar.getCustomView().findViewById(R.id.close_toot);
            close_toot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InputMethodManager inputMethodManager = (InputMethodManager)  getSystemService(Activity.INPUT_METHOD_SERVICE);
                    assert inputMethodManager != null;
                    inputMethodManager.hideSoftInputFromWindow(toot_content.getWindowToken(), 0);
                    finish();
                }
            });
            title = actionBar.getCustomView().findViewById(R.id.toolbar_title);
            pp_actionBar = actionBar.getCustomView().findViewById(R.id.pp_actionBar);
            pp_progress = actionBar.getCustomView().findViewById(R.id.pp_progress);

        }
        changeColor();
        //By default the toot is not restored so the id -1 is defined
        currentToId = -1;
        boolean restoredScheduled = false;


        toot_it = findViewById(R.id.toot_it);
        Button toot_cw = findViewById(R.id.toot_cw);
        toot_space_left = findViewById(R.id.toot_space_left);
        toot_visibility = findViewById(R.id.toot_visibility);
        toot_picture = findViewById(R.id.toot_picture);
        toot_picture_container = findViewById(R.id.toot_picture_container);
        toot_content = findViewById(R.id.toot_content);
        int newInputType = toot_content.getInputType() & (toot_content.getInputType() ^ InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
        toot_content.setInputType(newInputType);
        toot_cw_content = findViewById(R.id.toot_cw_content);
        picture_scrollview = findViewById(R.id.picture_scrollview);
        toot_sensitive = findViewById(R.id.toot_sensitive);
        drawer_layout = findViewById(R.id.drawer_layout);
        drawer_layout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = drawer_layout.getRootView().getHeight() - drawer_layout.getHeight();
                if (heightDiff > Helper.convertDpToPixel(200, getApplicationContext())) {
                    ViewGroup.LayoutParams params = toot_picture_container.getLayoutParams();
                    params.height = (int) Helper.convertDpToPixel(50, getApplicationContext());
                    params.width = (int) Helper.convertDpToPixel(50, getApplicationContext());
                    toot_picture_container.setLayoutParams(params);
                }else {
                    ViewGroup.LayoutParams params = toot_picture_container.getLayoutParams();
                    params.height = (int) Helper.convertDpToPixel(100, getApplicationContext());
                    params.width = (int) Helper.convertDpToPixel(100, getApplicationContext());
                    toot_picture_container.setLayoutParams(params);
                }
            }
        });

        Bundle b = getIntent().getExtras();
        ArrayList<Uri> sharedUri = new ArrayList<>();

        restored = -1;
        if(b != null) {
            tootReply = b.getParcelable("tootReply");
            accountReply = b.getParcelable("accountReply");
            tootMention = b.getString("tootMention", null);
            urlMention = b.getString("urlMention", null);
            fileMention = b.getString("fileMention", null);
            sharedContent = b.getString("sharedContent", null);
            sharedContentIni = b.getString("sharedContent", null);
            sharedSubject = b.getString("sharedSubject", null);
            mentionAccount = b.getString("mentionAccount", null);
            idRedirect =  b.getString("idRedirect", null);
            restoredScheduled = b.getBoolean("restoredScheduled", false);
            // ACTION_SEND route
            if (b.getInt("uriNumberMast", 0) == 1) {
                Uri fileUri = b.getParcelable("sharedUri");
                if (fileUri != null) {
                    sharedUri.add(fileUri);
                }
            }
            // ACTION_SEND_MULTIPLE route
            else if( b.getInt("uriNumberMast", 0) > 1) {
                ArrayList<Uri> fileUri = b.getParcelableArrayList("sharedUri");

                if (fileUri != null) {
                    sharedUri.addAll(fileUri);
                }
            }
            restored = b.getLong("restored", -1);
        }

        if(restoredScheduled){
            toot_it.setVisibility(View.GONE);
            invalidateOptionsMenu();
        }
        SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        String userIdReply;
        if( accountReply == null)
            userIdReply = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        else
            userIdReply = accountReply.getId();
        if( accountReply == null)
            account = new AccountDAO(getApplicationContext(),db).getAccountByID(userIdReply);
        else
            account = accountReply;

        if( tootReply != null) {
            tootReply();
        }else {
            if( title != null)
                title.setText(getString(R.string.toot_title));
            else
                setTitle(R.string.toot_title);
        }


        if( mentionAccount != null){
            toot_content.setText(String.format("@%s\n", mentionAccount));
            toot_content.setSelection(toot_content.getText().length());
            toot_space_left.setText(String.valueOf(toot_content.length()));
        }
        if( tootMention != null && urlMention != null && fileMention != null) {
            Bitmap pictureMention = BitmapFactory.decodeFile(getCacheDir() + "/" + fileMention);
            if (pictureMention != null) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                pictureMention.compress(Bitmap.CompressFormat.PNG, 0, bos);
                byte[] bitmapdata = bos.toByteArray();
                ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);
                toot_picture_container.setVisibility(View.VISIBLE);
                picture_scrollview.setVisibility(View.VISIBLE);
                toot_picture.setEnabled(false);
                toot_it.setEnabled(false);
                new HttpsConnection(TootActivity.this).upload(bs, TootActivity.this);
            }
            toot_content.setText(String.format("\n\nvia @%s\n\n%s\n\n", tootMention, urlMention));
            toot_space_left.setText(String.valueOf(toot_content.length()));
        }
        initialContent = toot_content.getText().toString();



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


        if( sharedContent != null ){ //Shared content

            if( sharedSubject != null){
                sharedContent = sharedSubject + "\n\n" + sharedContent;
            }
            receive_picture = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    final String image = intent.getStringExtra("image");
                    String title = intent.getStringExtra("title");
                    String description = intent.getStringExtra("description");
                    if( description != null && description.length() > 0){
                        if (sharedContentIni.startsWith("www."))
                            sharedContentIni = "http://" + sharedContentIni;
                        if( title != null && title.length() > 0)
                            sharedContent = title + "\n\n" + description + "\n\n" + sharedContentIni;
                        else
                            sharedContent = description + "\n\n" + sharedContentIni;
                        int selectionBefore = toot_content.getSelectionStart();
                        toot_content.setText(sharedContent);
                        if( selectionBefore >= 0 && selectionBefore < toot_content.length())
                            toot_content.setSelection(selectionBefore);
                        toot_space_left.setText(String.valueOf(toot_content.length()));
                    }
                    if( image != null){
                        new HttpsConnection(TootActivity.this).download(image, TootActivity.this);
                    }

                }
            };
            LocalBroadcastManager.getInstance(this).registerReceiver(receive_picture, new IntentFilter(Helper.RECEIVE_PICTURE));
            int selectionBefore = toot_content.getSelectionStart();
            toot_content.setText( String.format("\n%s", sharedContent));
            if( selectionBefore >= 0 && selectionBefore < toot_content.length())
                toot_content.setSelection(selectionBefore);
            toot_space_left.setText(String.valueOf(toot_content.length()));
        }
        attachments = new ArrayList<>();
        int charsInCw = 0;
        int charsInToot = 0;

        if (!sharedUri.isEmpty()) {
            uploadSharedImage(sharedUri);
        }


        if( tootReply == null) {
            String defaultVisibility = account.isLocked()?"private":"public";
            visibility = sharedpreferences.getString(Helper.SET_TOOT_VISIBILITY + "@" + account.getAcct() + "@" + account.getInstance(), defaultVisibility);
            switch (visibility) {
                case "public":
                    toot_visibility.setImageResource(R.drawable.ic_public_toot);
                    break;
                case "unlisted":
                    toot_visibility.setImageResource(R.drawable.ic_lock_open_toot);
                    break;
                case "private":
                    toot_visibility.setImageResource(R.drawable.ic_lock_outline_toot);
                    break;
                case "direct":
                    toot_visibility.setImageResource(R.drawable.ic_mail_outline_toot);
                    break;
            }
        }
        toot_sensitive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isSensitive = isChecked;
            }
        });

        toot_space_left.setText(String.valueOf(charsInToot + charsInCw));
        toot_cw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(toot_cw_content.getVisibility() == View.GONE) {
                    toot_cw_content.setVisibility(View.VISIBLE);
                    toot_cw_content.requestFocus();
                }else {
                    toot_cw_content.setVisibility(View.GONE);
                    toot_cw_content.setText("");
                    toot_content.requestFocus();
                }
            }
        });

        toot_visibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tootVisibilityDialog();
            }
        });

        toot_it.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toot_it.setEnabled(false);
                if(toot_content.getText().toString().trim().length() == 0){
                    Toast.makeText(getApplicationContext(),R.string.toot_error_no_content, Toast.LENGTH_LONG).show();
                    toot_it.setEnabled(true);
                    return;
                }
                Status toot = new Status();
                toot.setSensitive(isSensitive);
                toot.setMedia_attachments(attachments);
                if( toot_cw_content.getText().toString().trim().length() > 0)
                    toot.setSpoiler_text(toot_cw_content.getText().toString().trim());
                toot.setVisibility(visibility);
                if( tootReply != null)
                    toot.setIn_reply_to_id(tootReply.getId());
                toot.setContent(toot_content.getText().toString().trim());
                new PostStatusAsyncTask(getApplicationContext(), accountReply, toot, TootActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            }
        });

        toot_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    if (ContextCompat.checkSelfPermission(TootActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                            PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(TootActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                        return;
                    }
                }
                Intent intent;
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    intent.setType("*/*");
                    String[] mimetypes = {"image/*", "video/*"};
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                    startActivityForResult(intent, PICK_IMAGE);
                }else {
                    intent.setType("image/* video/*");
                    Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    Intent chooserIntent = Intent.createChooser(intent, getString(R.string.toot_select_image));
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});
                    startActivityForResult(chooserIntent, PICK_IMAGE);
                }

            }
        });
        String pattern = "^(.|\\s)*(@([a-zA-Z0-9_]{2,}))$";
        final Pattern sPattern = Pattern.compile(pattern);

        String patternTag = "^(.|\\s)*(#([\\w-]{2,}))$";
        final Pattern tPattern = Pattern.compile(patternTag);

        String patternEmoji = "^(.|\\s)*(:([\\w_]+))$";
        final Pattern ePattern = Pattern.compile(patternEmoji);

        toot_cw_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                int totalChar = toot_cw_content.length() + toot_content.length();
                toot_space_left.setText(String.valueOf(totalChar));
            }
        });

        toot_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            @Override
            public void afterTextChanged(Editable s) {
                if( toot_content.getSelectionStart() != 0)
                    currentCursorPosition = toot_content.getSelectionStart();
                if( s.toString().length() == 0 )
                    currentCursorPosition = 0;
                //Only check last 15 characters before cursor position to avoid lags
                if( currentCursorPosition < 15 ){ //Less than 15 characters are written before the cursor position
                    searchLength = currentCursorPosition;
                }else {
                    searchLength = 15;
                }
                int totalChar = toot_cw_content.length() + toot_content.length();
                toot_space_left.setText(String.valueOf(totalChar));
                if( currentCursorPosition- (searchLength-1) < 0 || currentCursorPosition == 0 || currentCursorPosition > s.toString().length())
                    return;
                Matcher m, mt;
                if( s.toString().charAt(0) == '@')
                    m = sPattern.matcher(s.toString().substring(currentCursorPosition- searchLength, currentCursorPosition));
                else
                    m = sPattern.matcher(s.toString().substring(currentCursorPosition- (searchLength-1), currentCursorPosition));
                if(m.matches()) {
                    String search = m.group(3);
                    if (pp_progress != null && pp_actionBar != null) {
                        pp_progress.setVisibility(View.VISIBLE);
                        pp_actionBar.setVisibility(View.GONE);
                    }
                    new RetrieveSearchAccountsAsyncTask(getApplicationContext(),search,TootActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }else{
                    if( s.toString().charAt(0) == '#')
                        mt = tPattern.matcher(s.toString().substring(currentCursorPosition- searchLength, currentCursorPosition));
                    else
                        mt = tPattern.matcher(s.toString().substring(currentCursorPosition- (searchLength-1), currentCursorPosition));
                    if(mt.matches()) {
                        String search = mt.group(3);
                        if (pp_progress != null && pp_actionBar != null) {
                            pp_progress.setVisibility(View.VISIBLE);
                            pp_actionBar.setVisibility(View.GONE);
                        }
                        new RetrieveSearchAsyncTask(getApplicationContext(),search,TootActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }else{
                        if( s.toString().charAt(0) == ':')
                            mt = ePattern.matcher(s.toString().substring(currentCursorPosition- searchLength, currentCursorPosition));
                        else
                            mt = ePattern.matcher(s.toString().substring(currentCursorPosition- (searchLength-1), currentCursorPosition));
                        if(mt.matches()) {
                            String shortcode = mt.group(3);
                            if (pp_progress != null && pp_actionBar != null) {
                                pp_progress.setVisibility(View.VISIBLE);
                                pp_actionBar.setVisibility(View.GONE);
                            }
                            new RetrieveEmojiAsyncTask(getApplicationContext(),shortcode,TootActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }else {
                            toot_content.dismissDropDown();
                        }
                    }
                }


                totalChar = toot_cw_content.length() + toot_content.length();
                toot_space_left.setText(String.valueOf(totalChar));
            }
        });


        if( restored != -1 ){
            restoreToot(restored);
        }
        if( theme == Helper.THEME_LIGHT) {
            toot_it.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if( receive_picture != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receive_picture);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // We have the permission.
                    toot_picture.callOnClick();
                }
                break;
            }
        }
    }
    public void showAToast (String message){
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        mToast.show();
    }


    // Handles uploading shared images
    public void uploadSharedImage(ArrayList<Uri> uri) {
        if (!uri.isEmpty()) {
            int count = 0;
            for(Uri fileUri: uri) {
                if (fileUri != null) {
                    if (count == 4) {
                        break;
                    }
                    picture_scrollview.setVisibility(View.VISIBLE);
                    try {
                        new asyncPicture(TootActivity.this, fileUri).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        count++;
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), R.string.toot_select_image_error, Toast.LENGTH_LONG).show();
                        toot_picture.setEnabled(true);
                        toot_it.setEnabled(true);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), R.string.toot_select_image_error, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    String mCurrentPhotoPath;
    File photoFile = null;


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            try {
                photoFile = createImageFile();
            } catch (IOException ignored) {Toast.makeText(getApplicationContext(),R.string.toot_select_image_error,Toast.LENGTH_LONG).show();}
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "fr.gouv.etalab.mastodon.fileProvider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, TAKE_PHOTO);
            }
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            picture_scrollview.setVisibility(View.VISIBLE);
            if (data == null || data.getData() == null) {
                Toast.makeText(getApplicationContext(),R.string.toot_select_image_error,Toast.LENGTH_LONG).show();
                return;
            }
            try {
                ContentResolver cr = getContentResolver();
                String mime = cr.getType(data.getData());
                if(mime != null && (mime.toLowerCase().contains("video") || mime.toLowerCase().contains("gif")) ) {
                    InputStream inputStream = getContentResolver().openInputStream(data.getData());
                    new HttpsConnection(TootActivity.this).upload(inputStream, TootActivity.this);
                } else if(mime != null && mime.toLowerCase().contains("image")) {
                    new asyncPicture(TootActivity.this, data.getData()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }else {
                    Toast.makeText(getApplicationContext(),R.string.toot_select_image_error,Toast.LENGTH_LONG).show();
                }
            } catch (FileNotFoundException e) {
                Toast.makeText(getApplicationContext(),R.string.toot_select_image_error,Toast.LENGTH_LONG).show();
                toot_picture.setEnabled(true);
                toot_it.setEnabled(true);
            }
        }else if(requestCode == Helper.REQ_CODE_SPEECH_INPUT && resultCode == Activity.RESULT_OK){
            if (null != data) {
                ArrayList<String> result = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                toot_content.setText(result.get(0));
                toot_content.setSelection(toot_content.getText().length());
            }
        }else if (requestCode == TAKE_PHOTO && resultCode == RESULT_OK) {
            new asyncPicture(TootActivity.this, data.getData()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private static class asyncPicture extends AsyncTask<Void, Void, Void> {

        ByteArrayInputStream bs;
        WeakReference<Activity> activityWeakReference;
        android.net.Uri uriFile;

        asyncPicture(Activity activity, android.net.Uri uri){
            this.activityWeakReference = new WeakReference<>(activity);
            this.uriFile = uri;
        }

        @Override
        protected Void doInBackground(Void... voids) {


            if( uriFile == null) {
                Toast.makeText(activityWeakReference.get(), R.string.toast_error, Toast.LENGTH_SHORT).show();
                return null;
            }
            Bitmap takenImage;
            try {
                takenImage = MediaStore.Images.Media.getBitmap(activityWeakReference.get().getContentResolver(), uriFile);
            } catch (IOException e) {
                Toast.makeText(activityWeakReference.get(), R.string.toast_error, Toast.LENGTH_SHORT).show();
                return null;
            }
            ExifInterface exif = null;
            try (InputStream inputStream = this.activityWeakReference.get().getContentResolver().openInputStream(uriFile)) {
                assert inputStream != null;
                exif = new ExifInterface(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Matrix matrix = null;
            if( takenImage != null ){
                int size = takenImage.getByteCount();
                if( exif != null) {
                    int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    int rotationDegree = 0;
                    if (rotation == ExifInterface.ORIENTATION_ROTATE_90) { rotationDegree = 90; }
                    else if (rotation == ExifInterface.ORIENTATION_ROTATE_180) {  rotationDegree = 180; }
                    else if (rotation == ExifInterface.ORIENTATION_ROTATE_270) {  rotationDegree =  270; }
                    matrix = new Matrix();
                    if (rotation != 0f) {matrix.preRotate(rotationDegree);}
                }

                SharedPreferences sharedpreferences = this.activityWeakReference.get().getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
                int resizeSet = sharedpreferences.getInt(Helper.SET_PICTURE_RESIZE, Helper.S_1MO);
                double resizeby = size;
                if( resizeSet == Helper.S_512KO){
                    resizeby = 4194304;
                }else if(resizeSet == Helper.S_1MO){
                    resizeby = 8388608;
                }else if(resizeSet == Helper.S_2MO){
                    resizeby = 16777216;
                }
                double resize = ((double)size)/resizeby;
                if( resize > 1 ){
                    ContentResolver cr = this.activityWeakReference.get().getContentResolver();
                    String mime = cr.getType(uriFile);
                    Bitmap newBitmap = Bitmap.createScaledBitmap(takenImage, (int) (takenImage.getWidth() / resize),
                            (int) (takenImage.getHeight() / resize), true);
                    Bitmap adjustedBitmap;
                    if( matrix != null)
                        adjustedBitmap = Bitmap.createBitmap(newBitmap, 0, 0, newBitmap.getWidth(), newBitmap.getHeight(), matrix, true);
                    else
                        adjustedBitmap = newBitmap;
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    if( mime !=null && (mime.contains("png") || mime.contains(".PNG")))
                        adjustedBitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
                    else
                        adjustedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
                    byte[] bitmapdata = bos.toByteArray();
                    bs = new ByteArrayInputStream(bitmapdata);
                }else {
                    try {
                        InputStream inputStream = this.activityWeakReference.get().getContentResolver().openInputStream(uriFile);
                        byte[] buff = new byte[8 * 1024];
                        int bytesRead;
                        ByteArrayOutputStream bao = new ByteArrayOutputStream();
                        assert inputStream != null;
                        while((bytesRead = inputStream.read(buff)) != -1) {
                            bao.write(buff, 0, bytesRead);
                        }
                        byte[] data = bao.toByteArray();
                        bs  = new ByteArrayInputStream(data);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }else {
                try {
                    InputStream inputStream = this.activityWeakReference.get().getContentResolver().openInputStream(uriFile);
                    byte[] buff = new byte[8 * 1024];
                    int bytesRead;
                    ByteArrayOutputStream bao = new ByteArrayOutputStream();
                    assert inputStream != null;
                    while((bytesRead = inputStream.read(buff)) != -1) {
                        bao.write(buff, 0, bytesRead);
                    }
                    byte[] data = bao.toByteArray();
                    bs  = new ByteArrayInputStream(data);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if( bs == null)
                return;
            ImageButton toot_picture;
            Button toot_it;
            LinearLayout toot_picture_container;
            toot_picture = this.activityWeakReference.get().findViewById(R.id.toot_picture);
            toot_it = this.activityWeakReference.get().findViewById(R.id.toot_it);
            toot_picture_container = this.activityWeakReference.get().findViewById(R.id.toot_picture_container);

            toot_picture_container.setVisibility(View.VISIBLE);
            toot_picture.setEnabled(false);
            toot_it.setEnabled(false);
            new HttpsConnection(this.activityWeakReference.get()).upload(bs, (TootActivity)this.activityWeakReference.get());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_view_reply:
                AlertDialog.Builder alert = new AlertDialog.Builder(TootActivity.this);
                alert.setTitle(R.string.toot_reply_content_title);
                final TextView input = new TextView(TootActivity.this);
                //Set the padding
                input.setPadding(30, 30, 30, 30);
                alert.setView(input);
                String content = tootReply.getContent();
                if(tootReply.getReblog() != null)
                    content = tootReply.getReblog().getContent();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    input.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY));
                else
                    //noinspection deprecation
                    input.setText(Html.fromHtml(content));
                alert.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });
                alert.setNegativeButton(R.string.accounts, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        new RetrieveAccountsForReplyAsyncTask(getApplicationContext(), tootReply.getReblog() != null?tootReply.getReblog():tootReply, TootActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        dialog.dismiss();
                    }
                });
                alert.show();
                return true;
            case R.id.action_translate:
                final CountryPicker picker = CountryPicker.newInstance("Select Country");  // dialog title
                final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
                final int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
                if( theme == Helper.THEME_LIGHT){
                    picker.setStyle(R.style.AppTheme, R.style.AlertDialog);
                }else {
                    picker.setStyle(R.style.AppThemeDark, R.style.AlertDialogDark);
                }
                if( toot_content.getText().length() == 0 && toot_cw_content.getText().length() == 0)
                    return true;
                String dateString = sharedpreferences.getString(Helper.LAST_TRANSLATION_TIME, null);
                if( dateString != null){
                    Date dateCompare = Helper.stringToDate(getApplicationContext(), dateString);
                    Date date = new Date();
                    if( date.before(dateCompare)) {
                        Toast.makeText(getApplicationContext(), R.string.please_wait, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                }
                picker.setListener(new CountryPickerListener() {
                    @SuppressLint("InflateParams")
                    @Override
                    public void onSelectCountry(String name, String locale, int flagDrawableResID) {
                        picker.dismiss();
                        AlertDialog.Builder transAlert = new AlertDialog.Builder(TootActivity.this);
                        transAlert.setTitle(R.string.translate_toot);

                        popup_trans = getLayoutInflater().inflate( R.layout.popup_translate, null );
                        transAlert.setView(popup_trans);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString(Helper.LAST_TRANSLATION_TIME, Helper.dateToString(getApplicationContext(), new Date( System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(Helper.SECONDES_BETWEEN_TRANSLATE))));
                        editor.apply();
                        TextView yandex_translate = popup_trans.findViewById(R.id.yandex_translate);
                        yandex_translate.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://translate.yandex.com/"));
                                startActivity(browserIntent);
                            }
                        });
                        MyTransL myTransL = MyTransL.getInstance(MyTransL.translatorEngine.YANDEX);
                        myTransL.setYandexAPIKey(Helper.YANDEX_KEY);
                        myTransL.setObfuscation(true);
                        myTransL.setTimeout(60);
                        myTransL.translate(toot_cw_content.getText().toString(), myTransL.getLocale(), new com.github.stom79.mytransl.client.Results() {
                            @Override
                            public void onSuccess(Translate translate) {
                                try {
                                    if( translate.getTranslatedContent() == null)
                                        return;
                                    if( popup_trans != null ) {
                                        ProgressBar trans_progress_cw = popup_trans.findViewById(R.id.trans_progress_cw);
                                        ProgressBar trans_progress_toot = popup_trans.findViewById(R.id.trans_progress_toot);
                                        if( trans_progress_cw != null)
                                            trans_progress_cw.setVisibility(View.GONE);
                                        LinearLayout trans_container = popup_trans.findViewById(R.id.trans_container);
                                        if( trans_container != null ){
                                            TextView cw_trans = popup_trans.findViewById(R.id.cw_trans);
                                            if( cw_trans != null) {
                                                cw_trans.setVisibility(View.VISIBLE);
                                                cw_trans.setText(translate.getTranslatedContent());
                                            }
                                        }else {
                                            Toast.makeText(getApplicationContext(), R.string.toast_error_translate, Toast.LENGTH_LONG).show();
                                        }
                                        if(trans_progress_cw != null && trans_progress_toot != null && trans_progress_cw.getVisibility() == View.GONE && trans_progress_toot.getVisibility() == View.GONE )
                                            if( dialogTrans.getButton(DialogInterface.BUTTON_NEGATIVE) != null)
                                                dialogTrans.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(true);
                                    }
                                } catch (IllegalArgumentException e) {
                                    Toast.makeText(getApplicationContext(), R.string.toast_error_translate, Toast.LENGTH_LONG).show();
                                }

                            }

                            @Override
                            public void onFail(HttpsConnectionException e) {

                            }
                        });
                        myTransL.translate(toot_content.getText().toString(), myTransL.getLocale(), new com.github.stom79.mytransl.client.Results() {
                            @Override
                            public void onSuccess(Translate translate) {
                                try {
                                    if( translate.getTranslatedContent() == null)
                                        return;
                                    if( popup_trans != null ) {
                                        ProgressBar trans_progress_cw = popup_trans.findViewById(R.id.trans_progress_cw);
                                        ProgressBar trans_progress_toot = popup_trans.findViewById(R.id.trans_progress_toot);
                                        if( trans_progress_toot != null)
                                            trans_progress_toot.setVisibility(View.GONE);
                                        LinearLayout trans_container = popup_trans.findViewById(R.id.trans_container);
                                        if( trans_container != null ){
                                            TextView toot_trans = popup_trans.findViewById(R.id.toot_trans);
                                           if(toot_trans != null){
                                                toot_trans.setVisibility(View.VISIBLE);
                                                toot_trans.setText(translate.getTranslatedContent());
                                            }
                                        }else {
                                            Toast.makeText(getApplicationContext(), R.string.toast_error_translate, Toast.LENGTH_LONG).show();
                                        }
                                        if(trans_progress_cw != null && trans_progress_toot != null && trans_progress_cw.getVisibility() == View.GONE && trans_progress_toot.getVisibility() == View.GONE )
                                            if( dialogTrans.getButton(DialogInterface.BUTTON_NEGATIVE) != null)
                                                dialogTrans.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(true);
                                    }
                                } catch (IllegalArgumentException e) {
                                    Toast.makeText(getApplicationContext(), R.string.toast_error_translate, Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onFail(HttpsConnectionException e) {

                            }
                        });
                        transAlert.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        });
                        transAlert.setNegativeButton(R.string.validate, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                TextView toot_trans = popup_trans.findViewById(R.id.toot_trans);
                                TextView cw_trans = popup_trans.findViewById(R.id.cw_trans);
                                if( toot_trans != null) {
                                    toot_content.setText(toot_trans.getText().toString());
                                    toot_content.setSelection(toot_content.getText().length());
                                }
                                if( cw_trans != null)
                                    toot_cw_content.setText(cw_trans.getText().toString());
                                dialog.dismiss();
                            }
                        });
                        dialogTrans = transAlert.create();
                        transAlert.show();

                        dialogTrans.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface dialog) {
                                Button negativeButton = ((AlertDialog) dialog)
                                        .getButton(AlertDialog.BUTTON_NEGATIVE);
                                if( negativeButton != null)
                                    negativeButton.setEnabled(false);
                            }
                        });

                    }
                });
                picker.show(getSupportFragmentManager(), "COUNTRY_PICKER");
                return true;
            case R.id.action_emoji:
                final List<Emojis>  emojis = new CustomEmojiDAO(getApplicationContext(), db).getAllEmojis();
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                int paddingPixel = 15;
                float density = getResources().getDisplayMetrics().density;
                int paddingDp = (int)(paddingPixel * density);
                builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setTitle(R.string.insert_emoji);
                if( emojis != null && emojis.size() > 0) {
                    GridView gridView = new GridView(TootActivity.this);
                    gridView.setAdapter(new CustomEmojiAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, emojis));
                    gridView.setNumColumns(5);
                    gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            toot_content.getText().insert(toot_content.getSelectionStart(), " :" + emojis.get(position).getShortcode()+": ");
                            alertDialogEmoji.dismiss();
                        }
                    });
                    gridView.setPadding(paddingDp,paddingDp,paddingDp,paddingDp);
                    builder.setView(gridView);
                }else{
                    TextView textView = new TextView(TootActivity.this);
                    textView.setText(getString(R.string.no_emoji));
                    textView.setPadding(paddingDp,paddingDp,paddingDp,paddingDp);
                    builder.setView(textView);
                }
                alertDialogEmoji = builder.show();


                return true;
            case R.id.action_photo_camera:
                dispatchTakePictureIntent();
                return true;
            case R.id.action_microphone:
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                        getString(R.string.speech_prompt));
                try {
                    startActivityForResult(intent, Helper.REQ_CODE_SPEECH_INPUT);
                } catch (ActivityNotFoundException a) {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.speech_not_supported),
                            Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_store:
                storeToot(true, true);
                return true;
            case R.id.action_restore:
                try{
                    final List<StoredStatus> drafts = new StatusStoredDAO(TootActivity.this, db).getAllDrafts();
                    if( drafts == null || drafts.size() == 0){
                        Toast.makeText(getApplicationContext(), R.string.no_draft, Toast.LENGTH_LONG).show();
                        return true;
                    }
                    AlertDialog.Builder builderSingle = new AlertDialog.Builder(TootActivity.this);
                    builderSingle.setTitle(getString(R.string.choose_toot));
                    final DraftsListAdapter draftsListAdapter = new DraftsListAdapter(TootActivity.this, drafts);
                    final int[] ids = new int[drafts.size()];
                    int i = 0;
                    for(StoredStatus draft: drafts){
                        ids[i] = draft.getId();
                        i++;
                    }
                    builderSingle.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builderSingle.setPositiveButton(R.string.delete_all, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, int which) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(TootActivity.this);
                            builder.setTitle(R.string.delete_all);
                            builder.setIcon(android.R.drawable.ic_dialog_alert)
                                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogConfirm, int which) {
                                            new StatusStoredDAO(getApplicationContext(), db).removeAllDrafts();
                                            dialogConfirm.dismiss();
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogConfirm, int which) {
                                            dialogConfirm.dismiss();
                                        }
                                    })
                                    .show();

                        }
                    });
                    builderSingle.setAdapter(draftsListAdapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int id = ids[which];
                            restoreToot(id);
                            dialog.dismiss();
                        }
                    });
                    builderSingle.show();
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(), R.string.toast_error, Toast.LENGTH_LONG).show();
                }
                return true;

            case R.id.action_schedule:
                if(toot_content.getText().toString().trim().length() == 0 ){
                    Toast.makeText(getApplicationContext(),R.string.toot_error_no_content, Toast.LENGTH_LONG).show();
                    return true;
                }
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(TootActivity.this);
                LayoutInflater inflater = this.getLayoutInflater();
                @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.datetime_picker, null);
                dialogBuilder.setView(dialogView);
                final AlertDialog alertDialog = dialogBuilder.create();

                final DatePicker datePicker = dialogView.findViewById(R.id.date_picker);
                final TimePicker timePicker = dialogView.findViewById(R.id.time_picker);
                timePicker.setIs24HourView(true);
                Button date_time_cancel = dialogView.findViewById(R.id.date_time_cancel);
                final ImageButton date_time_previous = dialogView.findViewById(R.id.date_time_previous);
                final ImageButton date_time_next = dialogView.findViewById(R.id.date_time_next);
                final ImageButton date_time_set = dialogView.findViewById(R.id.date_time_set);

                //Buttons management
                date_time_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });
                date_time_next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        datePicker.setVisibility(View.GONE);
                        timePicker.setVisibility(View.VISIBLE);
                        date_time_previous.setVisibility(View.VISIBLE);
                        date_time_next.setVisibility(View.GONE);
                        date_time_set.setVisibility(View.VISIBLE);
                    }
                });
                date_time_previous.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        datePicker.setVisibility(View.VISIBLE);
                        timePicker.setVisibility(View.GONE);
                        date_time_previous.setVisibility(View.GONE);
                        date_time_next.setVisibility(View.VISIBLE);
                        date_time_set.setVisibility(View.GONE);
                    }
                });
                date_time_set.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int hour, minute;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            hour = timePicker.getHour();
                            minute = timePicker.getMinute();
                        }else {
                            //noinspection deprecation
                            hour = timePicker.getCurrentHour();
                            //noinspection deprecation
                            minute = timePicker.getCurrentMinute();
                        }
                        Calendar calendar = new GregorianCalendar(datePicker.getYear(),
                                datePicker.getMonth(),
                                datePicker.getDayOfMonth(),
                                hour,
                                minute);
                        long time = calendar.getTimeInMillis();
                        if( (time - new Date().getTime()) < 60000 ){
                            Toast.makeText(getApplicationContext(), R.string.toot_scheduled_date, Toast.LENGTH_LONG).show();
                        }else {
                            //Store the toot as draft first
                            storeToot(false, false);
                            //Schedules the toot
                            ScheduledTootsSyncJob.schedule(getApplicationContext(), currentToId, time);
                            //Clear content
                            toot_content.setText("");
                            toot_cw_content.setText("");
                            toot_space_left.setText("0");
                            if( attachments != null) {
                                for (Attachment attachment : attachments) {
                                    View namebar = findViewById(Integer.parseInt(attachment.getId()));
                                    if (namebar != null && namebar.getParent() != null)
                                        ((ViewGroup) namebar.getParent()).removeView(namebar);
                                }
                                List<Attachment> tmp_attachment = new ArrayList<>();
                                tmp_attachment.addAll(attachments);
                                attachments.removeAll(tmp_attachment);
                                tmp_attachment.clear();
                            }
                            isSensitive = false;
                            toot_sensitive.setVisibility(View.GONE);
                            currentToId = -1;
                            Toast.makeText(TootActivity.this,R.string.toot_scheduled, Toast.LENGTH_LONG).show();
                            alertDialog.dismiss();
                        }
                    }
                });
                alertDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toot, menu);
        if( restored != -1 ){
            MenuItem itemRestore = menu.findItem(R.id.action_restore);
            if( itemRestore != null)
                itemRestore.setVisible(false);
            MenuItem itemSchedule = menu.findItem(R.id.action_schedule);
            if( itemSchedule != null)
                itemSchedule.setVisible(false);
        }
        MenuItem itemViewReply = menu.findItem(R.id.action_view_reply);
        if( tootReply == null){
            if( itemViewReply != null)
                itemViewReply.setVisible(false);
        }
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);

        String instanceVersion = sharedpreferences.getString(Helper.INSTANCE_VERSION + userId + instance, null);
        Version currentVersion = new Version(instanceVersion);
        Version minVersion = new Version("2.0");
        MenuItem itemEmoji = menu.findItem(R.id.action_emoji);
        SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        final List<Emojis>  emojis = new CustomEmojiDAO(getApplicationContext(), db).getAllEmojis();
        //Displays button only if custom emojis
        if (emojis != null && emojis.size() > 0 && (currentVersion.compareTo(minVersion) == 1 || currentVersion.equals(minVersion))) {
            itemEmoji.setVisible(true);
        }else{
            itemEmoji.setVisible(false);
        }
        if( accountReply != null){
            MenuItem itemRestore = menu.findItem(R.id.action_restore);
            if( itemRestore != null)
                itemRestore.setVisible(false);
            MenuItem itemSchedule = menu.findItem(R.id.action_schedule);
            if( itemSchedule != null)
                itemSchedule.setVisible(false);
            MenuItem itemStore= menu.findItem(R.id.action_store);
            if( itemStore != null)
                itemStore.setVisible(false);
        }
        return true;
    }

    @Override
    public void onRetrieveAttachment(final Attachment attachment, Error error) {
        if( error != null || attachment == null){
            final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            boolean show_error_messages = sharedpreferences.getBoolean(Helper.SET_SHOW_ERROR_MESSAGES, true);
            if( show_error_messages) {
                if( error != null)
                    Toast.makeText(getApplicationContext(), error.getError(), Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getApplicationContext(), R.string.toast_error, Toast.LENGTH_LONG).show();
            }
            if( attachments.size() == 0 )
                toot_picture_container.setVisibility(View.GONE);
            toot_picture.setEnabled(true);
            toot_it.setEnabled(true);
            return;
        }
        boolean alreadyAdded = false;
        int index = 0;
        for(Attachment attach_: this.attachments){
            if( attach_.getId().equals(attachment.getId())){
                alreadyAdded = true;
                break;
            }
            index++;
        }
        if( !alreadyAdded){
            toot_picture_container.setVisibility(View.VISIBLE);
            String url = attachment.getPreview_url();
            if (url == null || url.trim().equals(""))
                url = attachment.getUrl();

            final ImageView imageView = new ImageView(getApplicationContext());
            imageView.setId(Integer.parseInt(attachment.getId()));

            Glide.with(imageView.getContext())
                    .asBitmap()
                    .load(url)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                            imageView.setImageBitmap(resource);
                        }
                    });
            LinearLayout.LayoutParams imParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            imParams.setMargins(20, 5, 20, 5);
            imParams.height = (int) Helper.convertDpToPixel(100, getApplicationContext());
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            boolean show_media_urls = sharedpreferences.getBoolean(Helper.SET_MEDIA_URLS, true);
            if (show_media_urls) {
                //Adds the shorter text_url of attachment at the end of the toot 
                int selectionBefore = toot_content.getSelectionStart();
                toot_content.setText(String.format("%s\n\n%s",toot_content.getText().toString(), attachment.getText_url()));
                toot_space_left.setText(String.valueOf(toot_content.length()));
                //Moves the cursor
                toot_content.setSelection(selectionBefore);
            }
            toot_picture_container.addView(imageView, attachments.size(), imParams);
            imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    showRemove(imageView.getId());
                    return false;
                }
            });
            String instanceVersion = sharedpreferences.getString(Helper.INSTANCE_VERSION + userId + instance, null);
            if (instanceVersion != null) {
                Version currentVersion = new Version(instanceVersion);
                Version minVersion = new Version("2.0");
                if (currentVersion.compareTo(minVersion) == 1 || currentVersion.equals(minVersion)) {
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showAddDescription(attachment);
                        }
                    });
                }
            }
            attachments.add(attachment);
            if (attachments.size() < 4)
                toot_picture.setEnabled(true);
            toot_it.setEnabled(true);
            toot_sensitive.setVisibility(View.VISIBLE);
            picture_scrollview.setVisibility(View.VISIBLE);
        }else {
            if( attachments.size() > index && attachment.getDescription() != null) {
                attachments.get(index).setDescription(attachment.getDescription());
            }
        }
    }

    @Override
    public void onDownloaded(String pathToFile, String url, Error error) {
        picture_scrollview.setVisibility(View.VISIBLE);
        Bitmap pictureMention = BitmapFactory.decodeFile(pathToFile);
        if( pictureMention != null) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            pictureMention.compress(Bitmap.CompressFormat.PNG, 0, bos);
            byte[] bitmapdata = bos.toByteArray();
            ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);
            toot_picture_container.setVisibility(View.VISIBLE);
            toot_picture.setEnabled(false);
            toot_it.setEnabled(false);
            new HttpsConnection(TootActivity.this).upload(bs, TootActivity.this);
        }
    }

    @Override
    public void onUpdateProgress(int progress) {
        ProgressBar progressBar = findViewById(R.id.upload_progress);
        TextView toolbar_text = findViewById(R.id.toolbar_text);
        RelativeLayout progress_bar_container = findViewById(R.id.progress_bar_container);
        if( progress <= 100) {
            progressBar.setScaleY(3f);
            progress_bar_container.setVisibility(View.VISIBLE);
            progressBar.setProgress(progress);
            toolbar_text.setText(String.format("%s%%", progress));
        }else{
            progress_bar_container.setVisibility(View.GONE);
        }
    }

    private void showAddDescription(final Attachment attachment){
        AlertDialog.Builder builderInner = new AlertDialog.Builder(TootActivity.this);
        builderInner.setTitle(R.string.upload_form_description);

        @SuppressLint("InflateParams") View popup_media_description = getLayoutInflater().inflate( R.layout.popup_media_description, null );
        builderInner.setView(popup_media_description);

        //Text for report
        final EditText input = popup_media_description.findViewById(R.id.media_description);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(420)});
        final ImageView media_picture = popup_media_description.findViewById(R.id.media_picture);
        GlideApp.with(getApplicationContext())
                .asBitmap()
                .load(attachment.getUrl())
                .into(new SimpleTarget<Bitmap>() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                        media_picture.setImageBitmap(resource);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            media_picture.setImageAlpha(60);
                        }else {
                            media_picture.setAlpha(60);
                        }
                    }
                });

        builderInner.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        if( attachment.getDescription() != null && !attachment.getDescription().equals("null")) {
            input.setText(attachment.getDescription());
            input.setSelection(input.getText().length());
        }
        builderInner.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new UpdateDescriptionAttachmentAsyncTask(getApplicationContext(), attachment.getId(), input.getText().toString(), TootActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builderInner.create();
        alertDialog.show();
    }

    /**
     * Removes a media
     * @param viewId String
     */
    private void showRemove(final int viewId){

        AlertDialog.Builder dialog = new AlertDialog.Builder(TootActivity.this);

        dialog.setMessage(R.string.toot_delete_media);
        dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog,int which) {
                dialog.dismiss();
            }
        });
        dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog,int which) {
                View namebar = findViewById(viewId);
                for(Attachment attachment: attachments){
                    if( Integer.valueOf(attachment.getId()) == viewId){
                        attachments.remove(attachment);
                        final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                        boolean show_media_urls = sharedpreferences.getBoolean(Helper.SET_MEDIA_URLS, true);
                        if( show_media_urls) {
                            //Clears the text_url at the end of the toot  for this attachment
                            int selectionBefore = toot_content.getSelectionStart();
                            toot_content.setText(toot_content.getText().toString().replace(attachment.getText_url(), ""));
                            toot_space_left.setText(String.valueOf(toot_content.length()));
                            //Moves the cursor
                            if (selectionBefore >= 0 && selectionBefore < toot_content.length())
                                toot_content.setSelection(selectionBefore);
                        }
                        ((ViewGroup) namebar.getParent()).removeView(namebar);
                        break;
                    }
                }
                dialog.dismiss();
                if( attachments.size() == 0 ) {
                    toot_sensitive.setVisibility(View.GONE);
                    isSensitive = false;
                    toot_sensitive.setChecked(false);
                    picture_scrollview.setVisibility(View.GONE);
                }
                toot_picture.setEnabled(true);
            }
        });
        dialog.show();

    }


    private void tootVisibilityDialog(){

        AlertDialog.Builder dialog = new AlertDialog.Builder(TootActivity.this);
        dialog.setTitle(R.string.toot_visibility_tilte);
        final String[] stringArray = getResources().getStringArray(R.array.toot_visibility);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(TootActivity.this, android.R.layout.simple_list_item_1, stringArray);
        dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position) {
                dialog.dismiss();
            }
        });
        dialog.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position) {
                switch (position){
                    case 0:
                        visibility = "public";
                        toot_visibility.setImageResource(R.drawable.ic_public_toot);
                        break;
                    case 1:
                        visibility = "unlisted";
                        toot_visibility.setImageResource(R.drawable.ic_lock_open_toot);
                        break;
                    case 2:
                        visibility = "private";
                        toot_visibility.setImageResource(R.drawable.ic_lock_outline_toot);
                        break;
                    case 3:
                        visibility = "direct";
                        toot_visibility.setImageResource(R.drawable.ic_mail_outline_toot);
                        break;
                }

                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public void onPause(){
        super.onPause();
        final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        boolean storeToot = sharedpreferences.getBoolean(Helper.SET_AUTO_STORE, true);
        if( storeToot && accountReply == null)
            storeToot(true, false);
        else
            storeToot(false, false);
    }


    @Override
    public void onPostStatusAction(APIResponse apiResponse) {
        if( apiResponse.getError() != null){
            toot_it.setEnabled(true);
            if( apiResponse.getError().getError().contains("422")){
                showAToast(getString(R.string.toast_error_char_limit));
            }else {
                showAToast(apiResponse.getError().getError());
            }
            return;
        }
        if(restored != -1){
            SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
            new StatusStoredDAO(getApplicationContext(), db).remove(restored);
        }
        //Clear the toot
        toot_content.setText("");
        toot_cw_content.setText("");
        toot_space_left.setText("0");
        if( attachments != null) {
            for (Attachment attachment : attachments) {
                View namebar = findViewById(Integer.parseInt(attachment.getId()));
                if (namebar != null && namebar.getParent() != null)
                    ((ViewGroup) namebar.getParent()).removeView(namebar);
            }
            List<Attachment> tmp_attachment = new ArrayList<>();
            tmp_attachment.addAll(attachments);
            attachments.removeAll(tmp_attachment);
            tmp_attachment.clear();
        }
        isSensitive = false;
        toot_sensitive.setVisibility(View.GONE);
        currentToId = -1;
        Toast.makeText(TootActivity.this,R.string.toot_sent, Toast.LENGTH_LONG).show();
        toot_it.setEnabled(true);
        //It's a reply, so the user will be redirect to its answer
        if( tootReply != null){
            List<Status> statuses = apiResponse.getStatuses();
            if( statuses != null && statuses.size() > 0 ){
                Status status = statuses.get(0);
                if( status != null ) {
                    Intent intent = new Intent(getApplicationContext(), ShowConversationActivity.class);
                    Bundle b = new Bundle();
                    if( idRedirect == null)
                        b.putString("statusId", status.getId());
                    else
                        b.putString("statusId", idRedirect);
                    intent.putExtras(b);
                    startActivity(intent);
                    finish();
                }
            }
        }else {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra(INTENT_ACTION, HOME_TIMELINE_INTENT);
            startActivity(intent);
            finish();
        }

    }

    @Override
    public void onRetrieveSearchAccounts(APIResponse apiResponse) {
        if( pp_progress != null && pp_actionBar != null) {
            pp_progress.setVisibility(View.GONE);
            pp_actionBar.setVisibility(View.VISIBLE);
        }
        if( apiResponse.getError() != null)
            return;
        final List<Account> accounts = apiResponse.getAccounts();
        if( accounts != null && accounts.size() > 0){
            AccountsSearchAdapter accountsListAdapter = new AccountsSearchAdapter(TootActivity.this, accounts);
            toot_content.setThreshold(1);
            toot_content.setAdapter(accountsListAdapter);
            final String oldContent = toot_content.getText().toString();
            if( oldContent.length() >= currentCursorPosition) {
                String[] searchA = oldContent.substring(0, currentCursorPosition).split("@");
                if (searchA.length > 0) {
                    final String search = searchA[searchA.length - 1];
                    toot_content.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Account account = accounts.get(position);
                            String deltaSearch = "";
                            if (currentCursorPosition - searchLength > 0 && currentCursorPosition < oldContent.length())
                                deltaSearch = oldContent.substring(currentCursorPosition - searchLength, currentCursorPosition);
                            else {
                                if (currentCursorPosition >= oldContent.length())
                                    deltaSearch = oldContent.substring(currentCursorPosition - searchLength, oldContent.length());
                            }

                            if (!search.equals(""))
                                deltaSearch = deltaSearch.replace("@" + search, "");
                            String newContent = oldContent.substring(0, currentCursorPosition - searchLength);
                            newContent += deltaSearch;
                            newContent += "@" + account.getAcct() + " ";
                            int newPosition = newContent.length();
                            if (currentCursorPosition < oldContent.length() )
                                newContent += oldContent.substring(currentCursorPosition, oldContent.length());
                            toot_content.setText(newContent);
                            toot_space_left.setText(String.valueOf(toot_content.length()));
                            toot_content.setSelection(newPosition);
                            AccountsSearchAdapter accountsListAdapter = new AccountsSearchAdapter(TootActivity.this, new ArrayList<>());
                            toot_content.setThreshold(1);
                            toot_content.setAdapter(accountsListAdapter);
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onRetrieveEmoji(Status status, boolean fromTranslation) {

    }

    @Override
    public void onRetrieveSearchEmoji(final List<Emojis> emojis) {
        if( pp_progress != null && pp_actionBar != null) {
            pp_progress.setVisibility(View.GONE);
            pp_actionBar.setVisibility(View.VISIBLE);
        }
        if( emojis != null && emojis.size() > 0){
            EmojisSearchAdapter emojisSearchAdapter = new EmojisSearchAdapter(TootActivity.this, emojis);
            toot_content.setThreshold(1);
            toot_content.setAdapter(emojisSearchAdapter);
            final String oldContent = toot_content.getText().toString();
            String[] searchA = oldContent.substring(0,currentCursorPosition).split(":");
            final String search = searchA[searchA.length-1];
            toot_content.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String shortcode = emojis.get(position).getShortcode();
                    String deltaSearch = "";
                    if( currentCursorPosition-searchLength > 0 && currentCursorPosition < oldContent.length() )
                        deltaSearch = oldContent.substring(currentCursorPosition-searchLength, currentCursorPosition);
                    else {
                        if( currentCursorPosition >= oldContent.length() )
                            deltaSearch = oldContent.substring(currentCursorPosition-searchLength, oldContent.length());
                    }

                    if( !search.equals(""))
                        deltaSearch = deltaSearch.replace(":"+search,"");
                    String newContent = oldContent.substring(0,currentCursorPosition-searchLength);
                    newContent += deltaSearch;
                    newContent += ":" + shortcode + ": ";
                    int newPosition = newContent.length();
                    if( currentCursorPosition < oldContent.length() )
                        newContent +=   oldContent.substring(currentCursorPosition, oldContent.length()-1);
                    toot_content.setText(newContent);
                    toot_space_left.setText(String.valueOf(toot_content.length()));
                    toot_content.setSelection(newPosition);
                    EmojisSearchAdapter emojisSearchAdapter = new EmojisSearchAdapter(TootActivity.this, new ArrayList<>());
                    toot_content.setThreshold(1);
                    toot_content.setAdapter(emojisSearchAdapter);
                }
            });
        }
    }

    @Override
    public void onRetrieveSearch(Results results, Error error) {
        if( pp_progress != null && pp_actionBar != null) {
            pp_progress.setVisibility(View.GONE);
            pp_actionBar.setVisibility(View.VISIBLE);
        }
        if( results == null)
            return;

        final List<String> tags = results.getHashtags();
        if( tags != null && tags.size() > 0){
            TagsSearchAdapter tagsSearchAdapter = new TagsSearchAdapter(TootActivity.this, tags);
            toot_content.setThreshold(1);
            toot_content.setAdapter(tagsSearchAdapter);
            final String oldContent = toot_content.getText().toString();
            String[] searchA = oldContent.substring(0,currentCursorPosition).split("#");
            final String search = searchA[searchA.length-1];
            toot_content.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if( position >= tags.size() )
                        return;
                    String tag = tags.get(position);
                    String deltaSearch = "";
                    if( currentCursorPosition-searchLength > 0 && currentCursorPosition < oldContent.length() )
                        deltaSearch = oldContent.substring(currentCursorPosition-searchLength, currentCursorPosition);
                    else {
                        if( currentCursorPosition >= oldContent.length() )
                            deltaSearch = oldContent.substring(currentCursorPosition-searchLength, oldContent.length());
                    }

                    if( !search.equals(""))
                        deltaSearch = deltaSearch.replace("#"+search,"");
                    String newContent = oldContent.substring(0,currentCursorPosition-searchLength);
                    newContent += deltaSearch;
                    newContent += "#" + tag + " ";
                    int newPosition = newContent.length();
                    if( currentCursorPosition < oldContent.length() )
                        newContent +=   oldContent.substring(currentCursorPosition, oldContent.length()-1);
                    toot_content.setText(newContent);
                    toot_space_left.setText(String.valueOf(toot_content.length()));
                    toot_content.setSelection(newPosition);
                    TagsSearchAdapter tagsSearchAdapter = new TagsSearchAdapter(TootActivity.this, new ArrayList<>());
                    toot_content.setThreshold(1);
                    toot_content.setAdapter(tagsSearchAdapter);
                }
            });
        }
    }

    private void restoreToot(long id){
        SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        StoredStatus draft = new StatusStoredDAO(TootActivity.this, db).getStatus(id);
        Status status = draft.getStatus();
        //Retrieves attachments
        restored = id;
        attachments = status.getMedia_attachments();
        int childCount = toot_picture_container.getChildCount();
        ArrayList<ImageView> toRemove = new ArrayList<>();
        if( childCount > 0 ){
            for(int i = 0 ; i < childCount ; i++){
                if( toot_picture_container.getChildAt(i) instanceof ImageView)
                    toRemove.add((ImageView)toot_picture_container.getChildAt(i));
            }
            if( toRemove.size() > 0){
                for(ImageView imageView: toRemove)
                    toot_picture_container.removeView(imageView);
            }
            toRemove.clear();
        }
        if( attachments != null && attachments.size() > 0){
            toot_picture_container.setVisibility(View.VISIBLE);
            picture_scrollview.setVisibility(View.VISIBLE);
            int i = 0 ;
            for(final Attachment attachment: attachments){
                String url = attachment.getPreview_url();
                if( url == null || url.trim().equals(""))
                    url = attachment.getUrl();
                final ImageView imageView = new ImageView(getApplicationContext());
                imageView.setId(Integer.parseInt(attachment.getId()));

                LinearLayout.LayoutParams imParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                imParams.setMargins(20, 5, 20, 5);
                imParams.height = (int) Helper.convertDpToPixel(100, getApplicationContext());
                imageView.setAdjustViewBounds(true);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                toot_picture_container.addView(imageView, i, imParams);

                Glide.with(imageView.getContext())
                        .asBitmap()
                        .load(url)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                                imageView.setImageBitmap(resource);
                            }
                        });
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
                        String instanceVersion = sharedpreferences.getString(Helper.INSTANCE_VERSION + userId + instance, null);
                        if (instanceVersion != null) {
                            Version currentVersion = new Version(instanceVersion);
                            Version minVersion = new Version("2.0");
                            if (currentVersion.compareTo(minVersion) == 1 || currentVersion.equals(minVersion)) {
                                imageView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        showAddDescription(attachment);
                                    }
                                });
                            }
                        }
                    }
                });
                imageView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        showRemove(imageView.getId());
                        return false;
                    }
                });
                if( attachments.size() < 4)
                    toot_picture.setEnabled(true);
                toot_sensitive.setVisibility(View.VISIBLE);
                i++;
            }
        }else {
            toot_picture_container.setVisibility(View.GONE);
        }
        //Sensitive content
        toot_sensitive.setChecked(status.isSensitive());
        if( status.getSpoiler_text() != null && status.getSpoiler_text().length() > 0 ){
            toot_cw_content.setText(status.getSpoiler_text());
            toot_cw_content.setVisibility(View.VISIBLE);
        }else {
            toot_cw_content.setText("");
            toot_cw_content.setVisibility(View.GONE);
        }
        String content = status.getContent();
        toot_content.setText(content);
        toot_space_left.setText(String.valueOf(toot_content.length()));
        toot_content.setSelection(toot_content.getText().length());
        switch (status.getVisibility()){
            case "public":
                visibility = "public";
                toot_visibility.setImageResource(R.drawable.ic_public_toot);
                break;
            case "unlisted":
                visibility = "unlisted";
                toot_visibility.setImageResource(R.drawable.ic_lock_open_toot);
                break;
            case "private":
                visibility = "private";
                toot_visibility.setImageResource(R.drawable.ic_lock_outline_toot);
                break;
            case "direct":
                visibility = "direct";
                toot_visibility.setImageResource(R.drawable.ic_mail_outline_toot);
                break;
        }

        //The current id is set to the draft
        currentToId = draft.getId();
        tootReply = draft.getStatusReply();
        if( tootReply != null) {
            tootReply();

        }else {
            if( title != null)
                title.setText(getString(R.string.toot_title));
            else
                setTitle(R.string.toot_title);
        }
        invalidateOptionsMenu();
        initialContent = toot_content.getText().toString();
        toot_space_left.setText(String.valueOf(toot_content.getText().length() + toot_cw_content.getText().length()));
    }


    private void tootReply(){
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        if( title != null)
            title.setText(getString(R.string.toot_title_reply));
        else
            setTitle(R.string.toot_title_reply);
        String userIdReply;
        if( accountReply == null)
            userIdReply = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        else
            userIdReply = accountReply.getId();

        //If toot is not restored
        if( restored == -1 ){
            //Gets the default visibility, will be used if not set in settings
            String defaultVisibility = account.isLocked()?"private":"public";
            String settingsVisibility = sharedpreferences.getString(Helper.SET_TOOT_VISIBILITY + "@" + account.getAcct() + "@" + account.getInstance(), defaultVisibility);
            int initialTootVisibility = 0;
            int ownerTootVisibility = 0;
            switch (tootReply.getVisibility()){
                case "public":
                    initialTootVisibility = 4;
                    break;
                case "unlisted":
                    initialTootVisibility  = 3;
                    break;
                case "private":
                    visibility = "private";
                    initialTootVisibility = 2;
                    break;
                case "direct":
                    visibility = "direct";
                    initialTootVisibility = 1;
                    break;
            }
            switch (settingsVisibility){
                case "public":
                    ownerTootVisibility = 4;
                    break;
                case "unlisted":
                    ownerTootVisibility  = 3;
                    break;
                case "private":
                    visibility = "private";
                    ownerTootVisibility = 2;
                    break;
                case "direct":
                    visibility = "direct";
                    ownerTootVisibility = 1;
                    break;
            }
            int tootVisibility;
            if( ownerTootVisibility >= initialTootVisibility){
                tootVisibility = initialTootVisibility;
            }else {
                tootVisibility = ownerTootVisibility;
            }
            switch (tootVisibility){
                case 4:
                    visibility = "public";
                    toot_visibility.setImageResource(R.drawable.ic_public_toot);
                    break;
                case 3:
                    visibility = "unlisted";
                    toot_visibility.setImageResource(R.drawable.ic_lock_open_toot);
                    break;
                case 2:
                    visibility = "private";
                    toot_visibility.setImageResource(R.drawable.ic_lock_outline_toot);
                    break;
                case 1:
                    visibility = "direct";
                    toot_visibility.setImageResource(R.drawable.ic_mail_outline_toot);
                    break;
            }

            if( tootReply.getSpoiler_text() != null && tootReply.getSpoiler_text().length() > 0) {
                toot_cw_content.setText(tootReply.getSpoiler_text());
                toot_cw_content.setVisibility(View.VISIBLE);
            }
            //Retrieves mentioned accounts + OP and adds them at the beginin of the toot
            ArrayList<String> mentionedAccountsAdded = new ArrayList<>();
            int cursorReply = 0;
            if( tootReply.getAccount() != null && tootReply.getAccount().getAcct() != null && !tootReply.getAccount().getId().equals(userIdReply)) {
                toot_content.setText(String.format("@%s", tootReply.getAccount().getAcct()));
                mentionedAccountsAdded.add(tootReply.getAccount().getAcct());
                //Evaluate the cursor position => mention length + 1 char for carriage return
                cursorReply = toot_content.getText().toString().length() + 1;
            }
            if( tootReply.getMentions() != null ){
                //Put other accounts mentioned at the bottom
                boolean capitalize = sharedpreferences.getBoolean(Helper.SET_CAPITALIZE, true);
                if( capitalize)
                    toot_content.setText(String.format("%s", (toot_content.getText().toString() + "\n\n")));
                else
                    toot_content.setText(String.format("%s", (toot_content.getText().toString() + " ")));
                for(Mention mention : tootReply.getMentions()){
                    if(  mention.getAcct() != null && !mention.getId().equals(userIdReply) && !mentionedAccountsAdded.contains(mention.getAcct())) {
                        mentionedAccountsAdded.add(mention.getAcct());
                        String tootTemp = String.format("@%s ", mention.getAcct());
                        toot_content.setText(String.format("%s ", (toot_content.getText().toString() +  tootTemp.trim())));
                    }
                }

                toot_content.setText(toot_content.getText().toString().trim());
                if (toot_content.getText().toString().startsWith("@") ) {
                    if( capitalize )
                        toot_content.append("\n");
                    else
                        toot_content.append(" ");
                }
                toot_space_left.setText(String.valueOf(toot_content.length()));
                toot_content.requestFocus();

                if( capitalize) {
                    if (mentionedAccountsAdded.size() == 1) {
                        toot_content.setSelection(toot_content.getText().length()); //Put cursor at the end
                    } else {
                        if (cursorReply > 0 && cursorReply < toot_content.getText().length())
                            toot_content.setSelection(cursorReply);
                        else
                            toot_content.setSelection(toot_content.getText().length()); //Put cursor at the end
                    }
                }else {
                    toot_content.setSelection(toot_content.getText().length()); //Put cursor at the end
                }
            }

        }
        initialContent = toot_content.getText().toString();
    }



    private void storeToot(boolean message, boolean forced){
        //Nothing to store here....
        if( !forced) {
            if (toot_content.getText().toString().trim().length() == 0 && (attachments == null || attachments.size() < 1) && toot_cw_content.getText().toString().trim().length() == 0)
                return;
            if (initialContent.trim().equals(toot_content.getText().toString().trim()))
                return;
        }
        Status toot = new Status();
        toot.setSensitive(isSensitive);
        toot.setMedia_attachments(attachments);
        if( toot_cw_content.getText().toString().trim().length() > 0)
            toot.setSpoiler_text(toot_cw_content.getText().toString().trim());
        toot.setVisibility(visibility);
        toot.setContent(toot_content.getText().toString().trim());

        if( tootReply != null)
            toot.setIn_reply_to_id(tootReply.getId());
        SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        try{
            if( currentToId == -1 ) {
                currentToId = new StatusStoredDAO(TootActivity.this, db).insertStatus(toot, tootReply);

            }else{
                StoredStatus storedStatus = new StatusStoredDAO(TootActivity.this, db).getStatus(currentToId);
                if( storedStatus != null ){
                    new StatusStoredDAO(TootActivity.this, db).updateStatus(currentToId, toot);
                }else { //Might have been deleted, so it needs insertion
                    new StatusStoredDAO(TootActivity.this, db).insertStatus(toot, tootReply);
                }
            }
            if( message )
                Toast.makeText(getApplicationContext(), R.string.toast_toot_saved, Toast.LENGTH_LONG).show();
        }catch (Exception e){
            if( message)
                Toast.makeText(getApplicationContext(), R.string.toast_error, Toast.LENGTH_LONG).show();
        }
    }


    private void changeColor(){
        final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if( theme == Helper.THEME_DARK) {
            changeDrawableColor(TootActivity.this, R.drawable.ic_public_toot, R.color.dark_text);
            changeDrawableColor(TootActivity.this, R.drawable.ic_lock_open_toot, R.color.dark_text);
            changeDrawableColor(TootActivity.this, R.drawable.ic_lock_outline_toot, R.color.dark_text);
            changeDrawableColor(TootActivity.this, R.drawable.ic_mail_outline_toot, R.color.dark_text);
            changeDrawableColor(TootActivity.this, R.drawable.ic_insert_photo, R.color.dark_text);
            changeDrawableColor(TootActivity.this, R.drawable.ic_skip_previous, R.color.dark_text);
            changeDrawableColor(TootActivity.this, R.drawable.ic_skip_next, R.color.dark_text);
            changeDrawableColor(TootActivity.this, R.drawable.ic_check, R.color.dark_text);
        }else {
            changeDrawableColor(TootActivity.this, R.drawable.ic_public_toot, R.color.white);
            changeDrawableColor(TootActivity.this, R.drawable.ic_lock_open_toot, R.color.white);
            changeDrawableColor(TootActivity.this, R.drawable.ic_lock_outline_toot, R.color.white);
            changeDrawableColor(TootActivity.this, R.drawable.ic_mail_outline_toot, R.color.white);
            changeDrawableColor(TootActivity.this, R.drawable.ic_insert_photo, R.color.white);
            changeDrawableColor(TootActivity.this, R.drawable.ic_skip_previous, R.color.white);
            changeDrawableColor(TootActivity.this, R.drawable.ic_skip_next, R.color.white);
            changeDrawableColor(TootActivity.this, R.drawable.ic_check, R.color.white);
        }
    }


    @Override
    public void onRetrieveAccountsReply(ArrayList<Account> accounts) {
        final boolean[] checkedValues = new boolean[accounts.size()];
        int i = 0;
        for(Account account: accounts) {
            checkedValues[i] = toot_content.getText().toString().contains("@" + account.getAcct());
            i++;
        }
        final AlertDialog.Builder builderSingle = new AlertDialog.Builder(TootActivity.this);
        AccountsReplyAdapter accountsReplyAdapter = new AccountsReplyAdapter(TootActivity.this, accounts, checkedValues);
        builderSingle.setTitle(getString(R.string.select_accounts)).setAdapter(accountsReplyAdapter, null);
        builderSingle.setNegativeButton(R.string.validate, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                toot_content.setSelection(toot_content.getText().length());
            }
        });
        builderSingle.show();
    }

    public void changeAccountReply(boolean isChecked, String acct){
        if (isChecked) {
            if( !toot_content.getText().toString().contains(acct))
                toot_content.setText(String.format("%s %s",acct, toot_content.getText()));
        } else {
            toot_content.setText(toot_content.getText().toString().replaceAll("\\s*" +acct, ""));
        }
    }




}
