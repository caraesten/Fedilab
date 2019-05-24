/* Copyright 2017 Thomas Schneider
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
package app.fedilab.android.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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
import com.vanniktech.emoji.EmojiPopup;

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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.fedilab.android.client.API;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Attachment;
import app.fedilab.android.client.Entities.Emojis;
import app.fedilab.android.client.Entities.Error;
import app.fedilab.android.client.Entities.Mention;
import app.fedilab.android.client.Entities.Notification;
import app.fedilab.android.client.Entities.Poll;
import app.fedilab.android.client.Entities.PollOptions;
import app.fedilab.android.client.Entities.Results;
import app.fedilab.android.client.Entities.Status;
import app.fedilab.android.client.Entities.StoredStatus;
import app.fedilab.android.client.Entities.Tag;
import app.fedilab.android.client.Entities.Version;
import app.fedilab.android.client.HttpsConnection;
import app.fedilab.android.drawers.AccountsReplyAdapter;
import app.fedilab.android.drawers.AccountsSearchAdapter;
import app.fedilab.android.drawers.CustomEmojiAdapter;
import app.fedilab.android.drawers.DraftsListAdapter;
import app.fedilab.android.drawers.EmojisSearchAdapter;
import app.fedilab.android.drawers.TagsSearchAdapter;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.helper.MastalabAutoCompleteTextView;
import app.fedilab.android.jobs.ScheduledTootsSyncJob;
import app.fedilab.android.sqlite.AccountDAO;
import app.fedilab.android.sqlite.CustomEmojiDAO;
import app.fedilab.android.sqlite.Sqlite;
import app.fedilab.android.sqlite.StatusStoredDAO;
import es.dmoral.toasty.Toasty;
import app.fedilab.android.R;
import app.fedilab.android.asynctasks.PostActionAsyncTask;
import app.fedilab.android.asynctasks.PostStatusAsyncTask;
import app.fedilab.android.asynctasks.RetrieveAccountsForReplyAsyncTask;
import app.fedilab.android.asynctasks.RetrieveEmojiAsyncTask;
import app.fedilab.android.asynctasks.RetrieveSearchAccountsAsyncTask;
import app.fedilab.android.asynctasks.RetrieveSearchAsyncTask;
import app.fedilab.android.asynctasks.UpdateAccountInfoAsyncTask;
import app.fedilab.android.asynctasks.UpdateDescriptionAttachmentAsyncTask;
import app.fedilab.android.interfaces.OnDownloadInterface;
import app.fedilab.android.interfaces.OnPostActionInterface;
import app.fedilab.android.interfaces.OnPostStatusActionInterface;
import app.fedilab.android.interfaces.OnRetrieveAccountsReplyInterface;
import app.fedilab.android.interfaces.OnRetrieveAttachmentInterface;
import app.fedilab.android.interfaces.OnRetrieveEmojiInterface;
import app.fedilab.android.interfaces.OnRetrieveSearcAccountshInterface;
import app.fedilab.android.interfaces.OnRetrieveSearchInterface;

import static app.fedilab.android.helper.Helper.changeDrawableColor;

/**
 * Created by Thomas on 01/05/2017.
 * Toot activity class
 */

public class TootActivity extends BaseActivity implements OnPostActionInterface, OnRetrieveSearcAccountshInterface, OnRetrieveAttachmentInterface, OnPostStatusActionInterface, OnRetrieveSearchInterface, OnRetrieveAccountsReplyInterface, OnRetrieveEmojiInterface, OnDownloadInterface {


    private String visibility;
    private final int PICK_IMAGE = 56556;
    private final int TAKE_PHOTO = 56532;
    private ImageButton toot_picture;
    private LinearLayout toot_picture_container;
    private ArrayList<Attachment> attachments;
    private boolean isSensitive = false;
    private ImageButton toot_visibility;
    private Button toot_it;
    private MastalabAutoCompleteTextView toot_content;
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
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 754;
    private Account accountReply;
    private View popup_trans;
    private AlertDialog dialogTrans;
    private AlertDialog alertDialogEmoji;
    private String mentionAccount;
    private Status idRedirect;
    private String userId, instance;
    private Account account;
    private ArrayList<String> splitToot;
    private int stepSpliToot;
    private boolean removed;
    private boolean restoredScheduled;
    static boolean active = false;
    private int style;
    private StoredStatus scheduledstatus;
    private boolean isScheduled;
    private List<Boolean> checkedValues;
    private List<Account> contacts;
    private ListView lv_accounts_search;
    private RelativeLayout loader;
    private String contentType;
    private int max_media_count;
    public static HashMap<String, Uri> filesMap;
    private Poll poll;
    private ImageButton poll_action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        instance = sharedpreferences.getString(Helper.PREF_INSTANCE, Helper.getLiveInstance(getApplicationContext()));
        final int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
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
        if (theme == Helper.THEME_DARK) {
            style = R.style.DialogDark;
        } else if (theme == Helper.THEME_BLACK){
            style = R.style.DialogBlack;
        }else {
            style = R.style.Dialog;
        }
        filesMap = new HashMap<>();
        if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA){
            max_media_count = 9999;
        }else{
            max_media_count = 4;
        }
        setContentView(R.layout.activity_toot);
        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null ) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.toot_action_bar, null);
            actionBar.setCustomView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            ImageView close_toot = actionBar.getCustomView().findViewById(R.id.close_toot);

            close_toot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    assert inputMethodManager != null;
                    inputMethodManager.hideSoftInputFromWindow(toot_content.getWindowToken(), 0);
                    finish();
                }
            });
            if (theme == Helper.THEME_LIGHT){
                Toolbar toolbar = actionBar.getCustomView().findViewById(R.id.toolbar);
                Helper.colorizeToolbar(toolbar, R.color.black, TootActivity.this);
            }
            title = actionBar.getCustomView().findViewById(R.id.toolbar_title);
            pp_actionBar = actionBar.getCustomView().findViewById(R.id.pp_actionBar);
            pp_progress = actionBar.getCustomView().findViewById(R.id.pp_progress);

        }
        changeColor();
        //By default the toot is not restored so the id -1 is defined
        currentToId = -1;
        restoredScheduled = false;
        contentType = null;
        checkedValues = new ArrayList<>();
        contacts = new ArrayList<>();
        toot_it = findViewById(R.id.toot_it);
        Button toot_cw = findViewById(R.id.toot_cw);
        toot_space_left = findViewById(R.id.toot_space_left);
        toot_visibility = findViewById(R.id.toot_visibility);
        if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.GNU || MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA)
            toot_visibility.setVisibility(View.GONE);
        toot_picture = findViewById(R.id.toot_picture);
        toot_picture_container = findViewById(R.id.toot_picture_container);
        toot_content = findViewById(R.id.toot_content);
        int newInputType = toot_content.getInputType() & (toot_content.getInputType() ^ InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
        toot_content.setInputType(newInputType);
        toot_cw_content = findViewById(R.id.toot_cw_content);
        picture_scrollview = findViewById(R.id.picture_scrollview);
        toot_sensitive = findViewById(R.id.toot_sensitive);
        drawer_layout = findViewById(R.id.drawer_layout);
        ImageButton toot_emoji = findViewById(R.id.toot_emoji);
        poll_action = findViewById(R.id.poll_action);
        isScheduled = false;
        if( sharedpreferences.getBoolean(Helper.SET_DISPLAY_EMOJI, true)) {
            final EmojiPopup emojiPopup = EmojiPopup.Builder.fromRootView(drawer_layout).build(toot_content);

            toot_emoji.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    emojiPopup.toggle(); // Toggles visibility of the Popup.
                }
            });
        }else {
            toot_emoji.setVisibility(View.GONE);
        }


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
        SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        restored = -1;
        if(b != null) {
            tootReply = b.getParcelable("tootReply");
            scheduledstatus = b.getParcelable("storedStatus");
            String accountReplyToken = b.getString("accountReplyToken", null);
            accountReply = null;
            if( accountReplyToken != null){
                accountReply = new AccountDAO(getApplicationContext(),db).getAccountByToken(accountReplyToken);
            }
            tootMention = b.getString("tootMention", null);
            urlMention = b.getString("urlMention", null);
            fileMention = b.getString("fileMention", null);
            sharedContent = b.getString("sharedContent", null);
            sharedContentIni = b.getString("sharedContent", null);
            sharedSubject = b.getString("sharedSubject", null);
            mentionAccount = b.getString("mentionAccount", null);
            idRedirect =  b.getParcelable("idRedirect");
            removed = b.getBoolean("removed");
            visibility = b.getString("visibility", null);
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
        if( scheduledstatus != null)
            toot_it.setText(R.string.modify);
        if(restoredScheduled){
            toot_it.setVisibility(View.GONE);
            invalidateOptionsMenu();
        }

        String userIdReply;
        if( accountReply == null)
            userIdReply = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        else
            userIdReply = accountReply.getId();
        if( accountReply == null)
            account = new AccountDAO(getApplicationContext(),db).getAccountByID(userIdReply);
        else
            account = accountReply;


        if( MainActivity.social == null){

            //Update the static variable which manages account type
            if( account.getSocial() == null || account.getSocial().equals("MASTODON"))
                MainActivity.social = UpdateAccountInfoAsyncTask.SOCIAL.MASTODON;
            else if( account.getSocial().equals("PEERTUBE"))
                MainActivity.social = UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE;
            else if( account.getSocial().equals("PIXELFED"))
                MainActivity.social = UpdateAccountInfoAsyncTask.SOCIAL.PIXELFED;
            else if( account.getSocial().equals("PLEROMA"))
                MainActivity.social = UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA;
            else if( account.getSocial().equals("GNU"))
                MainActivity.social = UpdateAccountInfoAsyncTask.SOCIAL.GNU;
            else if( account.getSocial().equals("FRIENDICA"))
                MainActivity.social = UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA;
        }

        switch (MainActivity.social){
            case GNU:
                toot_it.setText(getText(R.string.queet_it));
                break;
            case PLEROMA:
                toot_it.setText(getText(R.string.submit));
                break;
            case FRIENDICA:
                toot_it.setText(getText(R.string.share));
                break;
            default:
                toot_it.setText(getText(R.string.toot_it));
        }

        if( tootReply != null) {
            tootReply();
        }else {
            if( title != null) {
                if(MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.GNU)
                    title.setText(getString(R.string.queet_title));
                else
                    title.setText(getString(R.string.toot_title));

            }
            else {
                if(MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.GNU)
                    setTitle(R.string.queet_title);
                else
                    setTitle(R.string.toot_title);
            }
        }

        toot_content.requestFocus();
        if( mentionAccount != null){
            toot_content.setText(String.format("@%s\n", mentionAccount));
            toot_content.setSelection(toot_content.getText().length());
            toot_space_left.setText(String.valueOf(toot_content.length()));
        }
        if( tootMention != null && urlMention != null) {
            if (fileMention != null) {
                Bitmap pictureMention = BitmapFactory.decodeFile(getCacheDir() + "/" + fileMention);
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        pictureMention.compress(Bitmap.CompressFormat.PNG, 0, bos);
                        byte[] bitmapdata = bos.toByteArray();
                        ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                toot_picture_container.setVisibility(View.VISIBLE);
                                picture_scrollview.setVisibility(View.VISIBLE);
                                toot_picture.setEnabled(false);
                                toot_it.setEnabled(false);
                                new HttpsConnection(TootActivity.this).upload(bs, fileMention, accountReply!=null?accountReply.getToken():null, TootActivity.this);
                            }
                        });

                    }
                });
            }
            toot_content.setText(String.format("\n\nvia @%s\n\n%s\n\n", tootMention, urlMention));
            toot_space_left.setText(String.valueOf(toot_content.length()));
        }


        initialContent = toot_content.getText().toString();



        String url = account.getAvatar();
        if( url.startsWith("/") ){
            url = Helper.getLiveInstanceWithProtocol(getApplicationContext()) + account.getAvatar();
        }
        Helper.loadGiF(getApplicationContext(), url, pp_actionBar);


        if( sharedContent != null ){ //Shared content

            if( sharedSubject != null){
                sharedContent = sharedSubject + "\n\n" + sharedContent;
            }
            if( b != null) {
                final String image = b.getString("image");
                String title = b.getString("title");
                String description = b.getString("description");
                if (description != null && description.length() > 0) {
                    if (sharedContentIni.startsWith("www."))
                        sharedContentIni = "http://" + sharedContentIni;
                    if (title != null && title.length() > 0)
                        sharedContent = title + "\n\n" + description + "\n\n" + sharedContentIni;
                    else
                        sharedContent = description + "\n\n" + sharedContentIni;
                    int selectionBefore = toot_content.getSelectionStart();
                    toot_content.setText(sharedContent);
                    if (selectionBefore >= 0 && selectionBefore < toot_content.length())
                        toot_content.setSelection(selectionBefore);
                    toot_space_left.setText(String.valueOf(toot_content.length()));
                }
                if (image != null) {
                    new HttpsConnection(TootActivity.this).download(image, TootActivity.this);
                }
                int selectionBefore = toot_content.getSelectionStart();
                toot_content.setText(String.format("\n%s", sharedContent));
                if (selectionBefore >= 0 && selectionBefore < toot_content.length())
                    toot_content.setSelection(selectionBefore);
                toot_space_left.setText(String.valueOf(toot_content.length()));
            }
        }


        attachments = new ArrayList<>();
        int charsInCw = 0;
        int charsInToot = 0;

        if (!sharedUri.isEmpty()) {
            uploadSharedImage(sharedUri);
        }


        if( tootReply == null) {
            if( visibility == null) {
                String defaultVisibility = account.isLocked() ? "private" : "public";
                visibility = sharedpreferences.getString(Helper.SET_TOOT_VISIBILITY + "@" + account.getAcct() + "@" + account.getInstance(), defaultVisibility);
            }
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
                sendToot(null, null);
            }
        });

        if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA)
            toot_it.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    PopupMenu popup = new PopupMenu(TootActivity.this, toot_it);
                    popup.getMenuInflater()
                            .inflate(R.menu.main_content_type, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()){
                                case R.id.action_plain_text:
                                    contentType = "text/plain";
                                    break;
                                case R.id.action_html:
                                    contentType = "text/html";
                                    break;
                                case R.id.action_markdown:
                                    contentType = "text/markdown";
                                    break;
                                case R.id.action_bbcode:
                                    contentType = "text/bbcode";
                                    break;
                            }
                            popup.dismiss();
                            sendToot(null, contentType);
                            return false;
                        }
                    });
                    popup.show();
                    return false;
                }
            });

        toot_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    intent.setType("*/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    String[] mimetypes = {"image/*", "video/*"};
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                    startActivityForResult(intent, PICK_IMAGE);
                }else {
                    intent.setType("image/* video/*");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    }
                    Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
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

        if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON || MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA)
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
                            new RetrieveSearchAsyncTask(getApplicationContext(),search,true, TootActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

        if( scheduledstatus != null)
            restoreServerSchedule(scheduledstatus.getStatus());

        if( restored != -1 ){
            restoreToot(restored);
        }

        poll_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayPollPopup();
            }
        });

        toot_space_left.setText(String.valueOf(toot_content.length()));
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
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
        mToast = Toasty.error(this, message, Toast.LENGTH_SHORT);
        mToast.show();
    }


    // Handles uploading shared images
    public void uploadSharedImage(ArrayList<Uri> uri) {
        if (!uri.isEmpty()) {
            int count = 0;
            for(Uri fileUri: uri) {
                if (fileUri != null) {
                    if (count == max_media_count) {
                        break;
                    }
                    picture_scrollview.setVisibility(View.VISIBLE);
                    try {
                        new asyncPicture(TootActivity.this, accountReply, fileUri).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        count++;
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toasty.error(getApplicationContext(),getString(R.string.toot_select_image_error),Toast.LENGTH_LONG).show();
                        toot_picture.setEnabled(true);
                        toot_it.setEnabled(true);
                    }
                } else {
                    Toasty.error(getApplicationContext(),getString(R.string.toot_select_image_error),Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    String mCurrentPhotoPath;
    File photoFile = null;
    Uri photoFileUri = null;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            try {
                photoFile = createImageFile();
            } catch (IOException ignored) {
                Toasty.error(getApplicationContext(),getString(R.string.toot_select_image_error),Toast.LENGTH_LONG).show();}
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoFileUri = FileProvider.getUriForFile(this,
                        "app.fedilab.android.fileProvider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoFileUri);
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
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            picture_scrollview.setVisibility(View.VISIBLE);
            if (data == null){
                Toasty.error(getApplicationContext(),getString(R.string.toot_select_image_error),Toast.LENGTH_LONG).show();
                return;
            }

            ClipData clipData = data.getClipData();
            if (data.getData() == null && clipData == null) {
                Toasty.error(getApplicationContext(),getString(R.string.toot_select_image_error),Toast.LENGTH_LONG).show();
                return;
            }
            if( clipData != null ){
                ArrayList<Uri> mArrayUri = new ArrayList<>();
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    ClipData.Item item = clipData.getItemAt(i);
                    Uri uri = item.getUri();
                    mArrayUri.add(uri);
                }
                uploadSharedImage(mArrayUri);
            }else{
                try {
                    String filename =  Helper.getFileName(TootActivity.this, data.getData());
                    ContentResolver cr = getContentResolver();
                    String mime = cr.getType(data.getData());
                    if(mime != null && (mime.toLowerCase().contains("video") || mime.toLowerCase().contains("gif")) ) {
                        InputStream inputStream = getContentResolver().openInputStream(data.getData());
                        new HttpsConnection(TootActivity.this).upload(inputStream, filename, accountReply!=null?accountReply.getToken():null, TootActivity.this);
                    } else if(mime != null && mime.toLowerCase().contains("image")) {
                        new asyncPicture(TootActivity.this, accountReply, data.getData()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }else {
                        Toasty.error(getApplicationContext(),getString(R.string.toot_select_image_error),Toast.LENGTH_LONG).show();
                    }
                } catch (FileNotFoundException e) {
                    Toasty.error(getApplicationContext(),getString(R.string.toot_select_image_error),Toast.LENGTH_LONG).show();
                    toot_picture.setEnabled(true);
                    toot_it.setEnabled(true);
                }
            }

        }else if(requestCode == Helper.REQ_CODE_SPEECH_INPUT && resultCode == RESULT_OK){
            if (null != data) {
                ArrayList<String> result = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                toot_content.setText(result.get(0));
                toot_content.setSelection(toot_content.getText().length());
            }
        }else if (requestCode == TAKE_PHOTO && resultCode == RESULT_OK) {
            new asyncPicture(TootActivity.this, accountReply, photoFileUri).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void onPostAction(int statusCode, API.StatusAction statusAction, String userId, Error error) {
        if( error != null){
            Toasty.error(getApplicationContext(),getString(R.string.toast_error),Toast.LENGTH_LONG).show();
        }else {
            Toasty.success(getApplicationContext(),getString(R.string.toot_scheduled),Toast.LENGTH_LONG).show();
            resetForNextToot();
        }
    }



    static class asyncPicture extends AsyncTask<Void, Void, Void> {

        ByteArrayInputStream bs;
        WeakReference<Activity> activityWeakReference;
        android.net.Uri uriFile;
        Account accountReply;
        boolean error = false;

        asyncPicture(Activity activity, Account accountReply, android.net.Uri uri){
            this.activityWeakReference = new WeakReference<>(activity);
            this.uriFile = uri;
            this.accountReply = accountReply;
        }

        @Override
        protected  void onPreExecute(){
            if( uriFile == null) {
                Toasty.error(activityWeakReference.get(), activityWeakReference.get().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                error = true;
            }
        }


        @Override
        protected Void doInBackground(Void... voids) {
            if( error)
                return null;
            bs = Helper.compressImage(activityWeakReference.get(), uriFile, Helper.MediaType.MEDIA);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if( !error) {
                if (bs == null)
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
                String filename = Helper.getFileName(this.activityWeakReference.get(), uriFile);
                filesMap.put(filename, uriFile);
                new HttpsConnection(this.activityWeakReference.get()).upload(bs, filename, accountReply != null ? accountReply.getToken() : null, (TootActivity) this.activityWeakReference.get());
            }
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle extras = intent.getExtras();
        if (extras != null && extras.getString("imageUri") != null) {
            Uri imageUri = Uri.parse(extras.getString("imageUri"));
            picture_scrollview.setVisibility(View.VISIBLE);
            if( imageUri == null) {
                Toasty.error(getApplicationContext(),getString(R.string.toot_select_image_error),Toast.LENGTH_LONG).show();
                return;
            }
            try {
                String filename =  Helper.getFileName(TootActivity.this, imageUri);
                ContentResolver cr = getContentResolver();
                String mime = cr.getType(imageUri);
                if(mime != null && (mime.toLowerCase().contains("video") || mime.toLowerCase().contains("gif")) ) {
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    new HttpsConnection(TootActivity.this).upload(inputStream, filename, accountReply!=null?accountReply.getToken():null, TootActivity.this);
                } else if(mime != null && mime.toLowerCase().contains("image")) {
                    new asyncPicture(TootActivity.this, accountReply, intent.getData()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }else {
                    Toasty.error(getApplicationContext(),getString(R.string.toot_select_image_error),Toast.LENGTH_LONG).show();
                }
            } catch (FileNotFoundException e) {
                Toasty.error(getApplicationContext(),getString(R.string.toot_select_image_error),Toast.LENGTH_LONG).show();
                toot_picture.setEnabled(true);
                toot_it.setEnabled(true);
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        int style;
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if (theme == Helper.THEME_DARK) {
            style = R.style.DialogDark;
        } else if (theme == Helper.THEME_BLACK){
            style = R.style.DialogBlack;
        }else {
            style = R.style.Dialog;
        }

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_view_reply:
                AlertDialog.Builder alert = new AlertDialog.Builder(TootActivity.this, style);
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

            case R.id.action_poll:
                displayPollPopup();
                return false;
            case R.id.action_translate:
                final CountryPicker picker = CountryPicker.newInstance(getString(R.string.which_language));  // dialog title
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
                        Toasty.info(getApplicationContext(), getString(R.string.please_wait), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                }
                picker.setListener(new CountryPickerListener() {
                    @SuppressLint("InflateParams")
                    @Override
                    public void onSelectCountry(String name, String locale, int flagDrawableResID) {
                        picker.dismiss();
                        AlertDialog.Builder transAlert = new AlertDialog.Builder(TootActivity.this, style);
                        transAlert.setTitle(R.string.translate_toot);

                        popup_trans = getLayoutInflater().inflate( R.layout.popup_translate, null );
                        transAlert.setView(popup_trans);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString(Helper.LAST_TRANSLATION_TIME, Helper.dateToString(new Date( System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(Helper.SECONDES_BETWEEN_TRANSLATE))));
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
                        if( toot_cw_content.getText().toString().length() > 0)
                            myTransL.translate(toot_cw_content.getText().toString(), locale, new com.github.stom79.mytransl.client.Results() {
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
                                                Toasty.error(getApplicationContext(),getString(R.string.toast_error_translate),Toast.LENGTH_LONG).show();
                                            }
                                            if(trans_progress_cw != null && trans_progress_toot != null && trans_progress_cw.getVisibility() == View.GONE && trans_progress_toot.getVisibility() == View.GONE )
                                                if( dialogTrans.getButton(DialogInterface.BUTTON_NEGATIVE) != null)
                                                    dialogTrans.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(true);
                                        }
                                    } catch (IllegalArgumentException e) {
                                        Toasty.error(getApplicationContext(),getString(R.string.toast_error_translate),Toast.LENGTH_LONG).show();
                                    }

                                }

                                @Override
                                public void onFail(HttpsConnectionException e) {
                                        e.printStackTrace();
                                }
                            });
                        else {
                            ProgressBar trans_progress_cw = popup_trans.findViewById(R.id.trans_progress_cw);
                            trans_progress_cw.setVisibility(View.GONE);
                        }
                        if( toot_content.getText().toString().length() > 0)
                            myTransL.translate(toot_content.getText().toString(), locale, new com.github.stom79.mytransl.client.Results() {
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
                                                Toasty.error(getApplicationContext(),getString(R.string.toast_error_translate),Toast.LENGTH_LONG).show();
                                            }
                                            if(trans_progress_cw != null && trans_progress_toot != null && trans_progress_cw.getVisibility() == View.GONE && trans_progress_toot.getVisibility() == View.GONE )
                                                if( dialogTrans.getButton(DialogInterface.BUTTON_NEGATIVE) != null)
                                                    dialogTrans.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(true);
                                        }
                                    } catch (IllegalArgumentException e) {
                                        Toasty.error(getApplicationContext(),getString(R.string.toast_error_translate),Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onFail(HttpsConnectionException e) {
                                    e.printStackTrace();
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
                final AlertDialog.Builder builder = new AlertDialog.Builder(this, style);
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
            case R.id.action_contacts:

                AlertDialog.Builder builderSingle = new AlertDialog.Builder(TootActivity.this, style);

                builderSingle.setTitle(getString(R.string.select_accounts));
                LayoutInflater inflater = getLayoutInflater();
                @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.popup_contact, null);

                loader = dialogView.findViewById(R.id.loader);
                EditText search_account = dialogView.findViewById(R.id.search_account);
                lv_accounts_search = dialogView.findViewById(R.id.lv_accounts_search);
                loader.setVisibility(View.VISIBLE);
                new RetrieveSearchAccountsAsyncTask(TootActivity.this, "a",  true,TootActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                search_account.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (count > 0) {
                            search_account.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_close, 0);
                        }else{
                            search_account.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_search, 0);
                        }
                    }
                    @Override
                    public void afterTextChanged(Editable s) {
                        if( s != null && s.length() > 0){
                            new RetrieveSearchAccountsAsyncTask(TootActivity.this, s.toString(),  true,TootActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                    }
                });
                search_account.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        final int DRAWABLE_RIGHT = 2;
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            if (search_account.length() > 0 && event.getRawX() >= (search_account.getRight() - search_account.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                                search_account.setText("");
                                new RetrieveSearchAccountsAsyncTask(TootActivity.this, "a",  true,TootActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            }
                        }

                        return false;
                    }
                });
                builderSingle.setView(dialogView);
                builderSingle.setNegativeButton(R.string.validate, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        toot_content.setSelection(toot_content.getText().length());
                    }
                });
                builderSingle.show();

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
                    Toasty.info(getApplicationContext(),
                            getString(R.string.speech_not_supported),
                            Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_store:
                storeToot(true, true);
                return true;
            case R.id.action_tags:
                Intent intentTags = new Intent(TootActivity.this, TagCacheActivity.class);
                intentTags.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intentTags);
                return true;
            case R.id.action_restore:
                try{
                    final List<StoredStatus> drafts = new StatusStoredDAO(TootActivity.this, db).getAllDrafts();
                    if( drafts == null || drafts.size() == 0){
                        Toasty.info(getApplicationContext(), getString(R.string.no_draft), Toast.LENGTH_LONG).show();
                        return true;
                    }
                    builderSingle = new AlertDialog.Builder(TootActivity.this, style);
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
                            AlertDialog.Builder builder = new AlertDialog.Builder(TootActivity.this, style);
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
                    Toasty.error(getApplicationContext(), getString(R.string.toast_error), Toast.LENGTH_LONG).show();
                }
                return true;

            case R.id.action_schedule:
                if(toot_content.getText().toString().trim().length() == 0 ){
                    Toasty.error(getApplicationContext(),getString(R.string.toot_error_no_content), Toast.LENGTH_LONG).show();
                    return true;
                }
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(TootActivity.this, style);
                inflater = this.getLayoutInflater();
                dialogView = inflater.inflate(R.layout.datetime_picker, null);
                dialogBuilder.setView(dialogView);
                final AlertDialog alertDialog = dialogBuilder.create();

                final DatePicker datePicker = dialogView.findViewById(R.id.date_picker);
                final TimePicker timePicker = dialogView.findViewById(R.id.time_picker);
                if (android.text.format.DateFormat.is24HourFormat(getApplicationContext()))
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
                        final long[] time = {calendar.getTimeInMillis()};

                        if( (time[0] - new Date().getTime()) < 60000 ){
                            Toasty.warning(getApplicationContext(), getString(R.string.toot_scheduled_date), Toast.LENGTH_LONG).show();
                        }else {
                            SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
                            String instanceVersion = sharedpreferences.getString(Helper.INSTANCE_VERSION + userId + instance, null);
                            Version currentVersion = new Version(instanceVersion);
                            Version minVersion = new Version("2.7");
                            if (currentVersion.compareTo(minVersion) == 1 || currentVersion.equals(minVersion)) {
                                AlertDialog.Builder builderSingle = new AlertDialog.Builder(TootActivity.this, style);
                                builderSingle.setTitle(getString(R.string.choose_schedule));
                                builderSingle.setNegativeButton(R.string.device_schedule, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        deviceSchedule(time[0]);
                                        dialog.dismiss();
                                    }
                                });
                                builderSingle.setPositiveButton(R.string.server_schedule, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(final DialogInterface dialog, int which) {
                                        int offset = TimeZone.getDefault().getRawOffset();
                                        calendar.add(Calendar.MILLISECOND, -offset);
                                        final String date = Helper.dateToString(new Date(calendar.getTimeInMillis()));
                                        serverSchedule(date);
                                    }
                                });
                                builderSingle.show();

                            } else {
                                deviceSchedule(time[0]);
                            }

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


    private void sendToot(String timestamp, String content_type){
        toot_it.setEnabled(false);
        if(toot_content.getText().toString().trim().length() == 0 && attachments.size() == 0){
            Toasty.error(getApplicationContext(),getString(R.string.toot_error_no_content),Toast.LENGTH_LONG).show();
            toot_it.setEnabled(true);
            return;
        }
        /*if( poll != null && visibility.equals("direct")){
            Toasty.error(getApplicationContext(),getString(R.string.poll_not_private),Toast.LENGTH_LONG).show();
            toot_it.setEnabled(true);
            return;
        }*/
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        boolean split_toot = sharedpreferences.getBoolean(Helper.SET_AUTOMATICALLY_SPLIT_TOOTS, false);
        int split_toot_size = sharedpreferences.getInt(Helper.SET_AUTOMATICALLY_SPLIT_TOOTS_SIZE+userId+instance, Helper.SPLIT_TOOT_SIZE);

        String tootContent;
        if( toot_cw_content.getText() != null && toot_cw_content.getText().toString().trim().length() > 0 )
            split_toot_size -= toot_cw_content.getText().toString().trim().length();
        if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA || !split_toot || (toot_content.getText().toString().trim().length()  < split_toot_size)){
            tootContent = toot_content.getText().toString().trim();
        }else{
            splitToot = Helper.splitToots(toot_content.getText().toString().trim(), split_toot_size);
            tootContent = splitToot.get(0);
            stepSpliToot = 1;
        }
        Status toot = new Status();
        if(content_type != null)
            toot.setContentType(content_type);
        toot.setSensitive(isSensitive);

        if( toot_cw_content.getText().toString().trim().length() > 0)
            toot.setSpoiler_text(toot_cw_content.getText().toString().trim());
        toot.setVisibility(visibility);
        if( tootReply != null)
            toot.setIn_reply_to_id(tootReply.getId());
        toot.setContent(tootContent);
        if( poll != null) {
            toot.setPoll(poll);
        }else{
            toot.setMedia_attachments(attachments);
        }
        if( timestamp == null)
            if( scheduledstatus == null)
                new PostStatusAsyncTask(getApplicationContext(), accountReply, toot, TootActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            else {
                toot.setScheduled_at(Helper.dateToString(scheduledstatus.getScheduled_date()));
                scheduledstatus.setStatus(toot);
                isScheduled = true;
                new PostActionAsyncTask(getApplicationContext(), API.StatusAction.DELETESCHEDULED, scheduledstatus, TootActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                new PostStatusAsyncTask(getApplicationContext(), accountReply, toot, TootActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        else {
            toot.setScheduled_at(timestamp);
            new PostStatusAsyncTask(getApplicationContext(), accountReply, toot, TootActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

    }


    private void serverSchedule(String time){
        sendToot(time, null);
        isScheduled = true;
        resetForNextToot();
    }

    private void deviceSchedule(long time){
        //Store the toot as draft first
        storeToot(false, false);
        isScheduled = true;
        //Schedules the toot
        ScheduledTootsSyncJob.schedule(getApplicationContext(), currentToId, time);
        resetForNextToot();
    }


    private void resetForNextToot(){
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
        Toasty.info(TootActivity.this,getString(R.string.toot_scheduled), Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toot, menu);
        if( restored != -1 ){
            MenuItem itemRestore = menu.findItem(R.id.action_restore);
            if( itemRestore != null)
                itemRestore.setVisible(false);
            MenuItem itemSchedule = menu.findItem(R.id.action_schedule);
            if( restoredScheduled )
                itemSchedule.setVisible(false);
        }
        MenuItem itemViewReply = menu.findItem(R.id.action_view_reply);
        if( tootReply == null){
            if( itemViewReply != null)
                itemViewReply.setVisible(false);
        }
        if( MainActivity.social != UpdateAccountInfoAsyncTask.SOCIAL.MASTODON){
            MenuItem itemPoll = menu.findItem(R.id.action_poll);
            if( itemPoll != null)
                itemPoll.setVisible(false);
        }
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if( theme == Helper.THEME_LIGHT)
            Helper.colorizeIconMenu(menu, R.color.black);
        changeColor();
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
        if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.GNU || MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA){
            MenuItem itemContacts = menu.findItem(R.id.action_contacts);
            if( itemContacts != null)
                itemContacts.setVisible(false);
        }
        return true;
    }

    @Override
    public void onRetrieveAttachment(final Attachment attachment, String fileName, Error error) {
        if( error != null || attachment == null){
            if( error != null)
                Toasty.error(getApplicationContext(), error.getError(), Toast.LENGTH_LONG).show();
            else
                Toasty.error(getApplicationContext(), getString(R.string.toast_error), Toast.LENGTH_LONG).show();
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

            if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.GNU || MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA){
                if( fileName != null && filesMap.containsKey(fileName)){
                    Uri uri = filesMap.get(fileName);
                    Glide.with(imageView.getContext())
                            .asBitmap()
                            .load(uri)
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                                    imageView.setImageBitmap(resource);
                                }
                            });
                }

            }else {
                Glide.with(imageView.getContext())
                        .asBitmap()
                        .load(url)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                                imageView.setImageBitmap(resource);
                            }
                        });
            }



            LinearLayout.LayoutParams imParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            imParams.setMargins(20, 5, 20, 5);
            imParams.height = (int) Helper.convertDpToPixel(100, getApplicationContext());
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
            boolean show_media_urls = sharedpreferences.getBoolean(Helper.SET_MEDIA_URLS, false);
            if (show_media_urls) {
                //Adds the shorter text_url of attachment at the end of the toot 
                int selectionBefore = toot_content.getSelectionStart();
                toot_content.setText(String.format("%s\n\n%s",toot_content.getText().toString(), attachment.getText_url()));
                toot_space_left.setText(String.valueOf(toot_content.length()));
                //Moves the cursor
                toot_content.setSelection(selectionBefore);
            }
            imageView.setTag(attachment.getId());
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
            addBorder();
            if (attachments.size() < max_media_count)
                toot_picture.setEnabled(true);
            toot_it.setEnabled(true);
            toot_sensitive.setVisibility(View.VISIBLE);
            if( account.isSensitive()){
                toot_sensitive.setChecked(true);
            }
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
            String filename = pathToFile.substring(pathToFile.lastIndexOf("/") + 1);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            if( filename.contains(".png") || filename.contains(".PNG"))
                pictureMention.compress(Bitmap.CompressFormat.PNG, 0, bos);
            else
                pictureMention.compress(Bitmap.CompressFormat.JPEG, 80, bos);
            byte[] bitmapdata = bos.toByteArray();
            ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);
            toot_picture_container.setVisibility(View.VISIBLE);
            toot_picture.setEnabled(false);
            toot_it.setEnabled(false);
            new HttpsConnection(TootActivity.this).upload(bs, filename, accountReply!=null?accountReply.getToken():null, TootActivity.this);
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
        AlertDialog.Builder builderInner = new AlertDialog.Builder(TootActivity.this, style);
        builderInner.setTitle(R.string.upload_form_description);

        @SuppressLint("InflateParams") View popup_media_description = getLayoutInflater().inflate( R.layout.popup_media_description, null );
        builderInner.setView(popup_media_description);

        //Text for report
        final EditText input = popup_media_description.findViewById(R.id.media_description);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(420)});
        final ImageView media_picture = popup_media_description.findViewById(R.id.media_picture);
        Glide.with(getApplicationContext())
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
                new UpdateDescriptionAttachmentAsyncTask(getApplicationContext(), attachment.getId(), input.getText().toString(), accountReply,TootActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                attachment.setDescription(input.getText().toString());
                addBorder();
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

        AlertDialog.Builder dialog = new AlertDialog.Builder(TootActivity.this, style);

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
                        final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
                        boolean show_media_urls = sharedpreferences.getBoolean(Helper.SET_MEDIA_URLS, false);
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

        AlertDialog.Builder dialog = new AlertDialog.Builder(TootActivity.this, style);
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
        final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        boolean storeToot = sharedpreferences.getBoolean(Helper.SET_AUTO_STORE, true);
        if( storeToot && accountReply == null)
            storeToot(true, false);
        else if( storeToot)
            storeToot(false, false);
    }


    @Override
    public void onPostStatusAction(APIResponse apiResponse) {
        if( apiResponse.getError() != null ){
            toot_it.setEnabled(true);
            if( apiResponse.getError().getError().contains("422")){
                showAToast(getString(R.string.toast_error_char_limit));
                return;
            }else if( apiResponse.getError().getStatusCode() == -33){
                storeToot(false, true);
            } else {
                showAToast(apiResponse.getError().getError());
                return;
            }

        }
        final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        boolean split_toot = sharedpreferences.getBoolean(Helper.SET_AUTOMATICALLY_SPLIT_TOOTS, false);
        int split_toot_size = sharedpreferences.getInt(Helper.SET_AUTOMATICALLY_SPLIT_TOOTS_SIZE+userId+instance, Helper.SPLIT_TOOT_SIZE);

        int cwSize = toot_cw_content.getText().toString().trim().length();
        int size = toot_content.getText().toString().trim().length() + cwSize;

        if( split_toot && (size  >= split_toot_size) && stepSpliToot < splitToot.size()){
            String tootContent = splitToot.get(stepSpliToot);
            stepSpliToot += 1;
            Status toot = new Status();
            toot.setSensitive(isSensitive);
            toot.setMedia_attachments(attachments);
            if( toot_cw_content.getText().toString().trim().length() > 0)
                toot.setSpoiler_text(toot_cw_content.getText().toString().trim());
            toot.setVisibility(visibility);
            if( apiResponse.getStatuses() != null && apiResponse.getStatuses().size() > 0)
                toot.setIn_reply_to_id(apiResponse.getStatuses().get(0).getId());
            toot.setContent(tootContent);
            new PostStatusAsyncTask(getApplicationContext(), accountReply, toot, TootActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            return;

        }
        if(restored != -1){
            SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
            new StatusStoredDAO(getApplicationContext(), db).remove(restored);
        }else if(currentToId != -1){
            SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
            new StatusStoredDAO(getApplicationContext(), db).remove(currentToId);
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
        if(apiResponse.getError() == null) {
            if (scheduledstatus == null && !isScheduled)
                Toasty.success(TootActivity.this, getString(R.string.toot_sent), Toast.LENGTH_LONG).show();
            else
                Toasty.success(TootActivity.this, getString(R.string.toot_scheduled), Toast.LENGTH_LONG).show();
        }else {
            if(apiResponse.getError().getStatusCode() == -33)
                Toasty.info(TootActivity.this, getString(R.string.toast_toot_saved_error), Toast.LENGTH_LONG).show();
        }
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
                        b.putParcelable("status", status);
                    else {
                        b.putParcelable("status", idRedirect);
                    }
                    intent.putExtras(b);
                    startActivity(intent);
                    finish();
                }
            }
        }else {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra(Helper.INTENT_ACTION, Helper.HOME_TIMELINE_INTENT);
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
    public void onRetrieveContact(APIResponse apiResponse) {
        if( apiResponse.getError() != null || apiResponse.getAccounts() == null)
            return;
        this.contacts = new ArrayList<>();
        this.checkedValues = new ArrayList<>();
        this.contacts.addAll(apiResponse.getAccounts());
        for(Account account: contacts) {
            this.checkedValues.add(toot_content.getText().toString().contains("@" + account.getAcct()));
        }
        this.loader.setVisibility(View.GONE);
        AccountsReplyAdapter contactAdapter = new AccountsReplyAdapter(TootActivity.this, this.contacts, this.checkedValues);
        this.lv_accounts_search.setAdapter(contactAdapter);
    }

    @Override
    public void onRetrieveEmoji(Status status, boolean fromTranslation) {

    }

    @Override
    public void onRetrieveEmoji(Notification notification) {

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
    public void onRetrieveSearch(APIResponse apiResponse) {
        if( pp_progress != null && pp_actionBar != null) {
            pp_progress.setVisibility(View.GONE);
            pp_actionBar.setVisibility(View.VISIBLE);
        }
        if( apiResponse == null || apiResponse.getResults() == null)
            return;
        Results results = apiResponse.getResults();
        final List<String> tags = results.getHashtags();
        if( tags != null && tags.size() > 0){
            TagsSearchAdapter tagsSearchAdapter = new TagsSearchAdapter(TootActivity.this, tags);
            toot_content.setThreshold(1);
            toot_content.setAdapter(tagsSearchAdapter);
            final String oldContent = toot_content.getText().toString();
            if( oldContent.length() < currentCursorPosition)
                return;
            String[] searchA = oldContent.substring(0,currentCursorPosition).split("#");
            if( searchA.length < 1)
                return;
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
        if( draft == null)
            return;
        Status status = draft.getStatus();
        //Retrieves attachments
        if( removed ){
            new StatusStoredDAO(TootActivity.this, db).remove(draft.getId());
        }
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
        String content = status.getContent();
        Pattern mentionLink = Pattern.compile("(<\\s?a\\s?href=\"https?:\\/\\/([\\da-z\\.-]+\\.[a-z\\.]{2,10})\\/(@[\\/\\w._-]*)\"\\s?[^.]*<\\s?\\/\\s?a\\s?>)");
        Matcher matcher = mentionLink.matcher(content);
        if (matcher.find()) {
            content = matcher.replaceAll("$3@$2");
        }

        if( removed ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                content = Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY).toString();
            else
                //noinspection deprecation
                content = Html.fromHtml(content).toString();
        }
        if( status.getPoll() != null) {
            poll = status.getPoll();
            poll_action.setVisibility(View.VISIBLE);
            toot_picture.setVisibility(View.GONE);
            picture_scrollview.setVisibility(View.GONE);
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
                imageView.setTag(attachment.getId());
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
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
                addBorder();
                if( attachments.size() < max_media_count)
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
            if( title != null){
                if(MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.GNU)
                    title.setText(getString(R.string.queet_title));
                else
                    title.setText(getString(R.string.toot_title));
            }
            else {
                if (MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.GNU)
                    setTitle(R.string.queet_title);
                else
                    setTitle(R.string.toot_title);
            }
        }
        invalidateOptionsMenu();
        initialContent = toot_content.getText().toString();
        toot_space_left.setText(String.valueOf(toot_content.getText().length() + toot_cw_content.getText().length()));
    }


    private void restoreServerSchedule(Status status){

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
        String content = status.getContent();
        Pattern mentionLink = Pattern.compile("(<\\s?a\\s?href=\"https?:\\/\\/([\\da-z\\.-]+\\.[a-z\\.]{2,10})\\/(@[\\/\\w._-]*)\"\\s?[^.]*<\\s?\\/\\s?a\\s?>)");
        Matcher matcher = mentionLink.matcher(content);
        if (matcher.find()) {
            content = matcher.replaceAll("$3@$2");
        }
        if( removed ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                content = Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY).toString();
            else
                //noinspection deprecation
                content = Html.fromHtml(content).toString();
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
                imageView.setTag(attachment.getId());
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
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
                addBorder();
                if( attachments.size() < max_media_count)
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

        if( title != null) {
            if (MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.GNU)
                title.setText(getString(R.string.queet_title));
            else
                title.setText(getString(R.string.toot_title));
        }
        else {
            if (MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.GNU)
                setTitle(R.string.queet_title);
            else
                setTitle(R.string.toot_title);
        }
        invalidateOptionsMenu();
        initialContent = toot_content.getText().toString();
        toot_space_left.setText(String.valueOf(toot_content.getText().length() + toot_cw_content.getText().length()));
    }

    private void tootReply(){
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        if( title != null) {
            if (MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.GNU)
                title.setText(getString(R.string.queet_title_reply));
            else
                title.setText(getString(R.string.toot_title_reply));
        }
        else {
            if (MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.GNU)
                setTitle(R.string.queet_title_reply);
            else
                setTitle(R.string.toot_title_reply);
        }
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
            if (settingsVisibility != null) {
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
            boolean forwardTags = sharedpreferences.getBoolean(Helper.SET_FORWARD_TAGS_IN_REPLY, false);
            if( tootReply != null && forwardTags && tootReply.getTags() != null && tootReply.getTags().size() > 0){
                int currentCursorPosition = toot_content.getSelectionStart();
                toot_content.setText(toot_content.getText() +"\n");
                for(Tag tag: tootReply.getTags()){
                    toot_content.setText(toot_content.getText() +" #"+tag.getName());
                }
                toot_content.setSelection(currentCursorPosition);
                toot_space_left.setText(String.valueOf(toot_content.length()));
            }

        }
        initialContent = toot_content.getText().toString();
    }


    private void displayPollPopup(){
        AlertDialog.Builder alertPoll = new AlertDialog.Builder(TootActivity.this, style);
        alertPoll.setTitle(R.string.create_poll);
        View view = getLayoutInflater().inflate(R.layout.popup_poll, null);
        alertPoll.setView(view);
        Spinner poll_choice = view.findViewById(R.id.poll_choice);
        Spinner poll_duration = view.findViewById(R.id.poll_duration);
        EditText choice_1 = view.findViewById(R.id.choice_1);
        EditText choice_2 = view.findViewById(R.id.choice_2);
        EditText choice_3 = view.findViewById(R.id.choice_3);
        EditText choice_4 = view.findViewById(R.id.choice_4);
        ArrayAdapter<CharSequence> pollduration = ArrayAdapter.createFromResource(TootActivity.this,
                R.array.poll_duration, android.R.layout.simple_spinner_item);

        ArrayAdapter<CharSequence> pollchoice = ArrayAdapter.createFromResource(TootActivity.this,
                R.array.poll_choice_type, android.R.layout.simple_spinner_item);
        poll_choice.setAdapter(pollchoice);
        poll_duration.setAdapter(pollduration);
        poll_duration.setSelection(4);
        poll_choice.setSelection(0);
        if( poll != null){
            int i = 1;
            for(PollOptions pollOptions: poll.getOptionsList()){
                switch (i){
                    case 1:
                        if( pollOptions.getTitle() != null)
                            choice_1.setText(pollOptions.getTitle());
                        break;
                    case 2:
                        if( pollOptions.getTitle() != null)
                            choice_2.setText(pollOptions.getTitle());
                        break;
                    case 3:
                        if( pollOptions.getTitle() != null)
                            choice_3.setText(pollOptions.getTitle());
                        break;
                    case 4:
                        if( pollOptions.getTitle() != null)
                            choice_4.setText(pollOptions.getTitle());
                        break;
                }
                i++;
            }
            switch (poll.getExpires_in()){
                case 300:
                    poll_duration.setSelection(0);
                    break;
                case 1800:
                    poll_duration.setSelection(1);
                    break;
                case 3600:
                    poll_duration.setSelection(2);
                    break;
                case 21600:
                    poll_duration.setSelection(3);
                    break;
                case 86400:
                    poll_duration.setSelection(4);
                    break;
                case 259200:
                    poll_duration.setSelection(5);
                    break;
                case 604800:
                    poll_duration.setSelection(6);
                    break;
            }
            if( poll.isMultiple())
                poll_choice.setSelection(1);
            else
                poll_choice.setSelection(0);


        }
        alertPoll.setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if( poll != null)
                    poll = null;
                poll_action.setVisibility(View.GONE);
                toot_picture.setVisibility(View.VISIBLE);
                if( attachments != null && attachments.size() > 0)
                    picture_scrollview.setVisibility(View.VISIBLE);
                dialog.dismiss();
            }
        });

        alertPoll.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                int poll_duration_pos = poll_duration.getSelectedItemPosition();

                int poll_choice_pos = poll_choice.getSelectedItemPosition();
                String choice1 = choice_1.getText().toString().trim();
                String choice2 = choice_2.getText().toString().trim();
                String choice3 = choice_3.getText().toString().trim();
                String choice4 = choice_4.getText().toString().trim();

                if( choice1.isEmpty() && choice2.isEmpty()){
                    Toasty.error(getApplicationContext(), getString(R.string.poll_invalid_choices), Toast.LENGTH_SHORT).show();
                }else{
                    poll = new Poll();
                    poll.setMultiple(poll_choice_pos != 0);
                    int expire = 0;
                    switch (poll_duration_pos){
                        case 0:
                            expire = 300;
                            break;
                        case 1:
                            expire = 1800;
                            break;
                        case 2:
                            expire = 3600;
                            break;
                        case 3:
                            expire = 21600;
                            break;
                        case 4:
                            expire = 86400;
                            break;
                        case 5:
                            expire = 259200;
                            break;
                        case 6:
                            expire = 604800;
                            break;
                        default:
                            expire = 864000;
                    }
                    poll.setExpires_in(expire);

                    List<PollOptions> pollOptions = new ArrayList<>();
                    PollOptions pollOption1 = new PollOptions();
                    pollOption1.setTitle(choice1);
                    pollOptions.add(pollOption1);

                    PollOptions pollOption2 = new PollOptions();
                    pollOption2.setTitle(choice2);
                    pollOptions.add(pollOption2);

                    PollOptions pollOption3 = new PollOptions();
                    pollOption3.setTitle(choice3);
                    pollOptions.add(pollOption3);

                    PollOptions pollOption4 = new PollOptions();
                    pollOption4.setTitle(choice4);
                    pollOptions.add(pollOption4);
                    poll.setOptionsList(pollOptions);

                    dialog.dismiss();
                }
                poll_action.setVisibility(View.VISIBLE);
                toot_picture.setVisibility(View.GONE);
                picture_scrollview.setVisibility(View.GONE);

            }
        });
        alertPoll.show();
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

        if( poll != null)
            toot.setPoll(poll);
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
                Toasty.success(getApplicationContext(), getString(R.string.toast_toot_saved), Toast.LENGTH_LONG).show();
        }catch (Exception e){
            if( message)
                Toasty.error(getApplicationContext(), getString(R.string.toast_error), Toast.LENGTH_LONG).show();
        }
    }
    private void changeColor(){
        final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if( theme == Helper.THEME_DARK || theme == Helper.THEME_BLACK) {
            changeDrawableColor(TootActivity.this, R.drawable.ic_public_toot, R.color.dark_text);
            changeDrawableColor(TootActivity.this, R.drawable.ic_lock_open_toot, R.color.dark_text);
            changeDrawableColor(TootActivity.this, R.drawable.ic_lock_outline_toot, R.color.dark_text);
            changeDrawableColor(TootActivity.this, R.drawable.ic_mail_outline_toot, R.color.dark_text);
            changeDrawableColor(TootActivity.this, R.drawable.ic_insert_photo, R.color.dark_text);
            changeDrawableColor(TootActivity.this, R.drawable.ic_skip_previous, R.color.dark_text);
            changeDrawableColor(TootActivity.this, R.drawable.ic_skip_next, R.color.dark_text);
            changeDrawableColor(TootActivity.this, R.drawable.ic_check, R.color.dark_text);
            changeDrawableColor(TootActivity.this, R.drawable.emoji_one_category_smileysandpeople, R.color.dark_text);
            //bottom action
            changeDrawableColor(TootActivity.this, findViewById(R.id.toot_picture), R.color.dark_text);
            changeDrawableColor(TootActivity.this, findViewById(R.id.poll_action), R.color.dark_text);
            changeDrawableColor(TootActivity.this, findViewById(R.id.toot_visibility), R.color.dark_text);
            changeDrawableColor(TootActivity.this, findViewById(R.id.toot_emoji), R.color.dark_text);
            Helper.changeButtonTextColor(TootActivity.this, findViewById(R.id.toot_cw), R.color.dark_text);

        }else {
            changeDrawableColor(TootActivity.this, R.drawable.ic_public_toot, R.color.white);
            changeDrawableColor(TootActivity.this, R.drawable.ic_lock_open_toot, R.color.white);
            changeDrawableColor(TootActivity.this, R.drawable.ic_lock_outline_toot, R.color.white);
            changeDrawableColor(TootActivity.this, R.drawable.ic_mail_outline_toot, R.color.white);
            changeDrawableColor(TootActivity.this, R.drawable.ic_insert_photo, R.color.white);
            changeDrawableColor(TootActivity.this, R.drawable.ic_skip_previous, R.color.white);
            changeDrawableColor(TootActivity.this, R.drawable.ic_skip_next, R.color.white);
            changeDrawableColor(TootActivity.this, R.drawable.ic_check, R.color.white);
            changeDrawableColor(TootActivity.this, R.drawable.emoji_one_category_smileysandpeople, R.color.black);
            //bottom action
            changeDrawableColor(TootActivity.this, findViewById(R.id.toot_picture), R.color.black);
            changeDrawableColor(TootActivity.this, findViewById(R.id.poll_action), R.color.black);
            changeDrawableColor(TootActivity.this, findViewById(R.id.toot_visibility), R.color.black);
            changeDrawableColor(TootActivity.this, findViewById(R.id.toot_emoji), R.color.black);
            Helper.changeButtonTextColor(TootActivity.this, findViewById(R.id.toot_cw), R.color.black);

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
        final AlertDialog.Builder builderSingle = new AlertDialog.Builder(TootActivity.this, style);
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

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }

    private void addBorder(){
        for (int i = 0; i < toot_picture_container.getChildCount(); i++) {
            View v = toot_picture_container.getChildAt(i);
            if (v instanceof ImageView) {
                for(Attachment attachment: attachments){
                    if(attachment.getType().toLowerCase().equals("image"))
                    if( v.getTag().toString().trim().equals(attachment.getId().trim())){
                        int borderSize = (int) Helper.convertDpToPixel(1, TootActivity.this);
                        int borderSizeTop = (int) Helper.convertDpToPixel(6, TootActivity.this);
                        v.setPadding(borderSize,borderSizeTop,borderSize,borderSizeTop);
                        if( attachment.getDescription() == null ||attachment.getDescription().trim().equals("null") || attachment.getDescription().trim().equals("")) {
                            v.setBackgroundColor( ContextCompat.getColor(TootActivity.this, R.color.red_1));
                        }else
                            v.setBackgroundColor(ContextCompat.getColor(TootActivity.this, R.color.green_1));
                    }
                }

            }
        }


    }

}
