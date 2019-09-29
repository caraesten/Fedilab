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
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.smarteist.autoimageslider.IndicatorAnimations;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;
import com.vanniktech.emoji.EmojiPopup;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadServiceSingleBroadcastReceiver;
import net.gotev.uploadservice.UploadStatusDelegate;

import org.apache.poi.util.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.fedilab.android.BuildConfig;
import app.fedilab.android.R;
import app.fedilab.android.asynctasks.PostActionAsyncTask;
import app.fedilab.android.asynctasks.PostStatusAsyncTask;
import app.fedilab.android.asynctasks.RetrieveEmojiAsyncTask;
import app.fedilab.android.asynctasks.RetrieveSearchAccountsAsyncTask;
import app.fedilab.android.asynctasks.RetrieveSearchAsyncTask;
import app.fedilab.android.asynctasks.UpdateAccountInfoAsyncTask;
import app.fedilab.android.client.API;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Attachment;
import app.fedilab.android.client.Entities.Emojis;
import app.fedilab.android.client.Entities.Error;
import app.fedilab.android.client.Entities.Notification;
import app.fedilab.android.client.Entities.Results;
import app.fedilab.android.client.Entities.Status;
import app.fedilab.android.client.Entities.StoredStatus;
import app.fedilab.android.client.Entities.Version;
import app.fedilab.android.drawers.AccountsReplyAdapter;
import app.fedilab.android.drawers.AccountsSearchAdapter;
import app.fedilab.android.drawers.EmojisSearchAdapter;
import app.fedilab.android.drawers.SliderAdapter;
import app.fedilab.android.drawers.TagsSearchAdapter;
import app.fedilab.android.helper.FileNameCleaner;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.helper.MastalabAutoCompleteTextView;
import app.fedilab.android.interfaces.OnDownloadInterface;
import app.fedilab.android.interfaces.OnPostActionInterface;
import app.fedilab.android.interfaces.OnPostStatusActionInterface;
import app.fedilab.android.interfaces.OnRetrieveAttachmentInterface;
import app.fedilab.android.interfaces.OnRetrieveEmojiInterface;
import app.fedilab.android.interfaces.OnRetrieveSearcAccountshInterface;
import app.fedilab.android.interfaces.OnRetrieveSearchInterface;
import app.fedilab.android.jobs.ScheduledTootsSyncJob;
import app.fedilab.android.sqlite.AccountDAO;
import app.fedilab.android.sqlite.Sqlite;
import app.fedilab.android.sqlite.StatusStoredDAO;
import es.dmoral.toasty.Toasty;
import static app.fedilab.android.helper.Helper.ALPHA;
import static app.fedilab.android.helper.Helper.MORSE;
import static app.fedilab.android.helper.Helper.changeDrawableColor;
import static app.fedilab.android.helper.Helper.countWithEmoji;


/**
 * Created by Thomas on 01/05/2017.
 * Toot activity class
 */

public class PixelfedComposeActivity extends BaseActivity implements UploadStatusDelegate, OnPostActionInterface, OnRetrieveSearcAccountshInterface, OnPostStatusActionInterface, OnRetrieveSearchInterface, OnRetrieveEmojiInterface, OnDownloadInterface, OnRetrieveAttachmentInterface {


    private String visibility;
    private final int PICK_IMAGE = 56556;
    private final int TAKE_PHOTO = 56532;
    private ArrayList<Attachment> attachments;
    private boolean isSensitive = false;
    private ImageButton toot_visibility;
    private Button toot_it;
    private MastalabAutoCompleteTextView toot_content;
    private CheckBox toot_sensitive;
    public long currentToId;
    private long restored;
    private TextView title;
    private ImageView pp_actionBar;
    private ProgressBar pp_progress;
    private Toast mToast;
    private TextView toot_space_left;
    private String initialContent;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 754;
    private Account accountReply;
    private String userId;
    private static String instance;
    private Account account;
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
    private int max_media_count;
    public static HashMap<String, Uri> filesMap;
    public static boolean autocomplete;
    private UploadServiceSingleBroadcastReceiver uploadReceiver;
    public static final int SEND_VOICE_MESSAGE = 1423;
    private UpdateAccountInfoAsyncTask.SOCIAL social;
    private Button upload_media;
    private LinearLayout pickup_picture;
    private static int searchDeep = 15;
    private SliderView imageSlider;
    private SliderAdapter sliderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        instance = sharedpreferences.getString(Helper.PREF_INSTANCE, Helper.getLiveInstance(getApplicationContext()));
        final int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        switch (theme) {
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
        } else if (theme == Helper.THEME_BLACK) {
            style = R.style.DialogBlack;
        } else {
            style = R.style.Dialog;
        }
        filesMap = new HashMap<>();
        social = MainActivity.social;


        autocomplete = false;
        setContentView(R.layout.activity_pixelfed_compose);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            View view = inflater.inflate(R.layout.toot_action_bar, new LinearLayout(getApplicationContext()), false);
            actionBar.setCustomView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            ImageView close_toot = actionBar.getCustomView().findViewById(R.id.close_toot);

            close_toot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    assert inputMethodManager != null;
                    inputMethodManager.hideSoftInputFromWindow(toot_content.getWindowToken(), 0);
                    boolean storeToot = sharedpreferences.getBoolean(Helper.SET_AUTO_STORE, true);
                    if (!storeToot) {
                        if (toot_content.getText().toString().trim().length() == 0 && (attachments == null || attachments.size() < 1)) {
                            finish();
                        } else if (initialContent.trim().equals(toot_content.getText().toString().trim())) {
                            finish();
                        } else {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(PixelfedComposeActivity.this, style);
                            dialogBuilder.setMessage(R.string.save_draft);
                            dialogBuilder.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    if (accountReply == null) {
                                        storeToot(true, false);
                                    } else {
                                        storeToot(false, false);
                                    }
                                    dialog.dismiss();
                                    finish();
                                }
                            });
                            dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    finish();
                                }
                            });
                            AlertDialog alertDialog = dialogBuilder.create();
                            alertDialog.setCancelable(false);
                            alertDialog.show();
                        }

                    } else {
                        finish();
                    }
                }
            });
            if (theme == Helper.THEME_LIGHT) {
                Toolbar toolbar = actionBar.getCustomView().findViewById(R.id.toolbar);
                Helper.colorizeToolbar(toolbar, R.color.black, PixelfedComposeActivity.this);
            }
            title = actionBar.getCustomView().findViewById(R.id.toolbar_title);
            pp_actionBar = actionBar.getCustomView().findViewById(R.id.pp_actionBar);
            pp_progress = actionBar.getCustomView().findViewById(R.id.pp_progress);

        }
        changeColor();


        //By default the toot is not restored so the id -1 is defined
        currentToId = -1;
        restoredScheduled = false;
        String contentType = null;
        checkedValues = new ArrayList<>();
        contacts = new ArrayList<>();
        toot_it = findViewById(R.id.toot_it);
        attachments = new ArrayList<>();
        imageSlider = findViewById(R.id.imageSlider);
        sliderAdapter = new SliderAdapter(new WeakReference<>(PixelfedComposeActivity.this), true, attachments);
        imageSlider.setIndicatorAnimation(IndicatorAnimations.WORM);
        imageSlider.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
        imageSlider.setSliderAdapter(sliderAdapter);
        upload_media = findViewById(R.id.upload_media);
        toot_space_left = findViewById(R.id.toot_space_left);
        toot_visibility = findViewById(R.id.toot_visibility);
        pickup_picture = findViewById(R.id.pickup_picture);
        toot_content = findViewById(R.id.toot_content);
        int newInputType = toot_content.getInputType() & (toot_content.getInputType() ^ InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
        toot_content.setInputType(newInputType);

        //There is no media the button is hidden
        upload_media.setVisibility(View.INVISIBLE);
        toot_sensitive = findViewById(R.id.toot_sensitive);
        LinearLayout drawer_layout = findViewById(R.id.drawer_layout);
        ImageButton toot_emoji = findViewById(R.id.toot_emoji);

        isScheduled = false;
        if (sharedpreferences.getBoolean(Helper.SET_DISPLAY_EMOJI, true)) {
            final EmojiPopup emojiPopup = EmojiPopup.Builder.fromRootView(drawer_layout).build(toot_content);

            toot_emoji.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    emojiPopup.toggle(); // Toggles visibility of the Popup.
                }
            });
        } else {
            toot_emoji.setVisibility(View.GONE);
        }


        Bundle b = getIntent().getExtras();
        ArrayList<Uri> sharedUri = new ArrayList<>();
        SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        restored = -1;
        if (b != null) {
            scheduledstatus = b.getParcelable("storedStatus");
            String accountReplyToken = b.getString("accountReplyToken", null);
            accountReply = null;
            if (accountReplyToken != null) {
                String[] val = accountReplyToken.split("\\|");
                if (val.length == 2) {
                    accountReply = new AccountDAO(getApplicationContext(), db).getUniqAccount(val[0], val[1]);
                }
            }
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
            else if (b.getInt("uriNumberMast", 0) > 1) {
                ArrayList<Uri> fileUri = b.getParcelableArrayList("sharedUri");

                if (fileUri != null) {
                    sharedUri.addAll(fileUri);
                }
            }
            restored = b.getLong("restored", -1);
        }
        if (scheduledstatus != null)
            toot_it.setText(R.string.modify);
        if (restoredScheduled) {
            toot_it.setVisibility(View.GONE);
            invalidateOptionsMenu();
        }
        String userIdReply, instanceReply;
        if (accountReply == null) {
            userIdReply = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
            instanceReply = sharedpreferences.getString(Helper.PREF_INSTANCE, null);
        } else {
            userIdReply = accountReply.getId();
            instanceReply = accountReply.getInstance();
        }
        if (accountReply == null)
            account = new AccountDAO(getApplicationContext(), db).getUniqAccount(userIdReply, instanceReply);
        else
            account = accountReply;

        max_media_count = 4;

        setTitle(R.string.compose);
        toot_content.requestFocus();


        Helper.loadGiF(getApplicationContext(), account.getAvatar(), pp_actionBar);



        if (visibility == null) {
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

        toot_sensitive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isSensitive = isChecked;
            }
        });


        toot_space_left.setText(String.valueOf(countLength(social, toot_content)));


        toot_visibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tootVisibilityDialog();
            }
        });

        toot_it.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToot(null);
            }
        });


        pickup_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(PixelfedComposeActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(PixelfedComposeActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    return;
                }
                Intent intent;
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    intent.setType("*/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    String[] mimetypes = {"image/*", "video/*", "audio/mpeg", "audio/opus", "audio/flac", "audio/wav", "audio/ogg"};
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                    startActivityForResult(intent, PICK_IMAGE);
                } else {
                    intent.setType("image/* video/* audio/mpeg audio/opus audio/flac audio/wav audio/ogg");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    }
                    Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    Intent chooserIntent = Intent.createChooser(intent, getString(R.string.toot_select_image));
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});
                    startActivityForResult(chooserIntent, PICK_IMAGE);
                }

            }
        });

        upload_media.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(PixelfedComposeActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(PixelfedComposeActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    return;
                }
                Intent intent;
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    intent.setType("*/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    String[] mimetypes = {"image/*", "video/*", "audio/mpeg", "audio/opus", "audio/flac", "audio/wav", "audio/ogg"};
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                    startActivityForResult(intent, PICK_IMAGE);
                } else {
                    intent.setType("image/* video/* audio/mpeg audio/opus audio/flac audio/wav audio/ogg");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    }
                    Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    Intent chooserIntent = Intent.createChooser(intent, getString(R.string.toot_select_image));
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});
                    startActivityForResult(chooserIntent, PICK_IMAGE);
                }

            }
        });


        TextWatcher textWatcher = initializeTextWatcher(getApplicationContext(), social, toot_content, toot_space_left, pp_actionBar, pp_progress, PixelfedComposeActivity.this, PixelfedComposeActivity.this, PixelfedComposeActivity.this);
        if (social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON || social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA)
            toot_content.addTextChangedListener(textWatcher);


        if (scheduledstatus != null)
            restoreServerSchedule(scheduledstatus.getStatus());

        if (restored != -1) {
            restoreToot(restored);
        }

        toot_space_left.setText(String.valueOf(countLength(social, toot_content)));

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(imageReceiver,
                        new IntentFilter(Helper.INTENT_SEND_MODIFIED_IMAGE));

        uploadReceiver = new UploadServiceSingleBroadcastReceiver(this);
        uploadReceiver.register(this);

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(add_new_media,
                        new IntentFilter(Helper.INTENT_ADD_UPLOADED_MEDIA));

    }

    public static TextWatcher initializeTextWatcher(Context context, UpdateAccountInfoAsyncTask.SOCIAL social,
                                                    MastalabAutoCompleteTextView toot_content,TextView toot_space_left,
                                                    ImageView pp_actionBar, ProgressBar pp_progress,
                                                    OnRetrieveSearchInterface listener, OnRetrieveSearcAccountshInterface listenerAccount, OnRetrieveEmojiInterface listenerEmoji
    ) {

        String pattern = "(.|\\s)*(@[\\w_-]+@[a-z0-9.\\-]+|@[\\w_-]+)";
        final Pattern sPattern = Pattern.compile(pattern);

        String patternTag = "^(.|\\s)*(#([\\w-]{2,}))$";
        final Pattern tPattern = Pattern.compile(patternTag);

        String patternEmoji = "^(.|\\s)*(:([\\w_]+))$";
        final Pattern ePattern = Pattern.compile(patternEmoji);
        final int[] currentCursorPosition = {toot_content.getSelectionStart()};
        final String[] newContent = {null};
        final int[] searchLength = {searchDeep};
        TextWatcher textw = null;
        TextWatcher finalTextw = textw;
        textw = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (autocomplete) {
                    toot_content.removeTextChangedListener(finalTextw);
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            String fedilabHugsTrigger = ":fedilab_hugs:";
                            String fedilabMorseTrigger = ":fedilab_morse:";

                            if (s.toString().contains(fedilabHugsTrigger)) {
                                newContent[0] = s.toString().replaceAll(fedilabHugsTrigger, "");
                                int currentLength = countLength(social, toot_content);
                                int toFill = 150 - currentLength;
                                if (toFill <= 0) {
                                    return;
                                }


                                StringBuilder hugs = new StringBuilder();
                                for (int i = 0; i < toFill; i++) {
                                    hugs.append(new String(Character.toChars(0x1F917)));
                                }

                                Handler mainHandler = new Handler(Looper.getMainLooper());

                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        newContent[0] = newContent[0] + hugs.toString();
                                        toot_content.setText(newContent[0]);
                                        toot_content.setSelection(toot_content.getText().length());
                                        // toot_content.addTextChangedListener(finalTextw);
                                        autocomplete = false;
                                        toot_space_left.setText(String.valueOf(countLength(social, toot_content)));
                                    }
                                };
                                mainHandler.post(myRunnable);
                            } else if (s.toString().contains(fedilabMorseTrigger)) {
                                newContent[0] = s.toString().replaceAll(fedilabMorseTrigger, "").trim();
                                List<String> mentions = new ArrayList<>();
                                String mentionPattern = "@[a-z0-9_]+(@[a-z0-9\\.\\-]+[a-z0-9]+)?";
                                final Pattern mPattern = Pattern.compile(mentionPattern, Pattern.CASE_INSENSITIVE);
                                Matcher matcherMentions = mPattern.matcher(newContent[0]);
                                while (matcherMentions.find()) {
                                    mentions.add(matcherMentions.group());
                                }
                                for (String mention : mentions) {
                                    newContent[0] = newContent[0].replace(mention, "");
                                }
                                newContent[0] = Normalizer.normalize(newContent[0], Normalizer.Form.NFD);
                                newContent[0] = newContent[0].replaceAll("[^\\p{ASCII}]", "");

                                HashMap<String, String> ALPHA_TO_MORSE = new HashMap<>();
                                for (int i = 0; i < ALPHA.length && i < MORSE.length; i++) {
                                    ALPHA_TO_MORSE.put(ALPHA[i], MORSE[i]);
                                }
                                StringBuilder builder = new StringBuilder();
                                String[] words = newContent[0].trim().split(" ");

                                for (String word : words) {
                                    for (int i = 0; i < word.length(); i++) {
                                        String morse = ALPHA_TO_MORSE.get(word.substring(i, i + 1).toLowerCase());
                                        builder.append(morse).append(" ");
                                    }

                                    builder.append("  ");
                                }
                                newContent[0] = "";
                                for (String mention : mentions) {
                                    newContent[0] += mention + " ";
                                }
                                newContent[0] += builder.toString();

                                Handler mainHandler = new Handler(Looper.getMainLooper());

                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        toot_content.setText(newContent[0]);
                                        toot_content.setSelection(toot_content.getText().length());
                                        autocomplete = false;
                                        toot_space_left.setText(String.valueOf(countLength(social, toot_content)));
                                    }
                                };
                                mainHandler.post(myRunnable);
                            }
                        }
                    };
                    thread.start();
                    return;
                }

                if (toot_content.getSelectionStart() != 0)
                    currentCursorPosition[0] = toot_content.getSelectionStart();
                if (s.toString().length() == 0)
                    currentCursorPosition[0] = 0;
                //Only check last 15 characters before cursor position to avoid lags
                if (currentCursorPosition[0] < searchDeep) { //Less than 15 characters are written before the cursor position
                    searchLength[0] = currentCursorPosition[0];
                } else {
                    searchLength[0] = searchDeep;
                }


                int totalChar = countLength(social, toot_content);
                toot_space_left.setText(String.valueOf(totalChar));
                if (currentCursorPosition[0] - (searchLength[0] - 1) < 0 || currentCursorPosition[0] == 0 || currentCursorPosition[0] > s.toString().length())
                    return;

                String patternh = "^(.|\\s)*(:fedilab_hugs:)$";
                final Pattern hPattern = Pattern.compile(patternh);
                Matcher mh = hPattern.matcher((s.toString().substring(currentCursorPosition[0] - searchLength[0], currentCursorPosition[0])));

                if (mh.matches()) {
                    autocomplete = true;
                    return;
                }

                String patternM = "^(.|\\s)*(:fedilab_morse:)$";
                final Pattern mPattern = Pattern.compile(patternM);
                Matcher mm = mPattern.matcher((s.toString().substring(currentCursorPosition[0] - searchLength[0], currentCursorPosition[0])));
                if (mm.matches()) {
                    autocomplete = true;
                    return;
                }
                String[] searchInArray =(s.toString().substring(currentCursorPosition[0] - searchLength[0], currentCursorPosition[0])).split("\\s");
                if( searchInArray.length < 1){
                    return;
                }
                String searchIn = searchInArray[searchInArray.length-1];
                Matcher m, mt;
                m = sPattern.matcher(searchIn);
                if (m.matches()) {
                    String search = m.group();
                    if (pp_progress != null && pp_actionBar != null) {
                        pp_progress.setVisibility(View.VISIBLE);
                        pp_actionBar.setVisibility(View.GONE);
                    }
                    new RetrieveSearchAccountsAsyncTask(context, search, listenerAccount).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    mt = tPattern.matcher(searchIn);
                    if (mt.matches()) {
                        String search = mt.group(3);
                        if (pp_progress != null && pp_actionBar != null) {
                            pp_progress.setVisibility(View.VISIBLE);
                            pp_actionBar.setVisibility(View.GONE);
                        }
                        new RetrieveSearchAsyncTask(context, search, true, listener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        mt = ePattern.matcher(searchIn);
                        if (mt.matches()) {
                            String shortcode = mt.group(3);
                            if (pp_progress != null && pp_actionBar != null) {
                                pp_progress.setVisibility(View.VISIBLE);
                                pp_actionBar.setVisibility(View.GONE);
                            }
                            new RetrieveEmojiAsyncTask(context, shortcode, listenerEmoji).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        } else {
                            toot_content.dismissDropDown();
                        }
                    }
                }


                totalChar = countLength(social, toot_content);
                toot_space_left.setText(String.valueOf(totalChar));
            }
        };
        return textw;
    }


    private BroadcastReceiver imageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String imgpath = intent.getStringExtra("imgpath");
            if (imgpath != null) {
                prepareUpload(PixelfedComposeActivity.this, Uri.fromFile(new File(imgpath)), null, uploadReceiver);
            }
        }
    };

    private BroadcastReceiver add_new_media = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            JSONObject response = null;
            ArrayList<String> successfullyUploadedFiles = null;
            try {
                response = new JSONObject(intent.getStringExtra("response"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            successfullyUploadedFiles = intent.getStringArrayListExtra("uploadInfo");
            addNewMedia(response, successfullyUploadedFiles);
        }
    };


    private void addNewMedia(JSONObject response, ArrayList<String> successfullyUploadedFiles) {

        Attachment attachment;
        attachment = API.parseAttachmentResponse(response);
        boolean alreadyAdded = false;
        int index = 0;
        for (Attachment attach_ : attachments) {
            if (attach_.getId().equals(attachment.getId())) {
                alreadyAdded = true;
                break;
            }
            index++;
        }
        if( attachment.getPreview_url().contains("no-preview.png") && successfullyUploadedFiles != null && successfullyUploadedFiles.size() > 0){
            attachment.setPreview_url(successfullyUploadedFiles.get(0));
        }
        if (!alreadyAdded) {
            attachments.add(attachment);
            sliderAdapter.notifyDataSetChanged();
            imageSlider.setVisibility(View.VISIBLE);
            pickup_picture.setVisibility(View.GONE);
            upload_media.setVisibility(View.VISIBLE);
            if (attachments.size() < max_media_count)
                upload_media.setEnabled(true);
            toot_it.setEnabled(true);
            toot_sensitive.setVisibility(View.VISIBLE);
            if (account.isSensitive()) {
                toot_sensitive.setChecked(true);
            }
            imageSlider.setCurrentPagePosition(imageSlider.getChildCount());
        } else {
            if (attachments.size() > index && attachment.getDescription() != null) {
                attachments.get(index).setDescription(attachment.getDescription());
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // We have the permission.
                upload_media.callOnClick();
            }
        }
    }

    public void showAToast(String message) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toasty.error(getApplicationContext(), message, Toast.LENGTH_SHORT);
        mToast.show();
    }


    // Handles uploading shared images
    public void uploadSharedImage(ArrayList<Uri> uri) {
        if (!uri.isEmpty()) {
            int count = 0;
            for (Uri fileUri : uri) {
                if (fileUri != null) {
                    if (count == max_media_count) {
                        break;
                    }
                    try {
                        prepareUpload(PixelfedComposeActivity.this, fileUri, null, uploadReceiver);
                        count++;
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toasty.error(getApplicationContext(), getString(R.string.toot_select_image_error), Toast.LENGTH_LONG).show();
                        upload_media.setEnabled(true);
                        toot_it.setEnabled(true);
                    }
                } else {
                    Toasty.error(getApplicationContext(), getString(R.string.toot_select_image_error), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    String mCurrentPhotoPath;
    File photoFile = null;
    public static Uri photoFileUri = null;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            try {
                photoFile = createImageFile();
            } catch (IOException ignored) {
                Toasty.error(getApplicationContext(), getString(R.string.toot_select_image_error), Toast.LENGTH_LONG).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                if (BuildConfig.DONATIONS) {
                    photoFileUri = FileProvider.getUriForFile(this,
                            "fr.gouv.etalab.mastodon.fileProvider",
                            photoFile);
                } else {
                    photoFileUri = FileProvider.getUriForFile(this,
                            "app.fedilab.android.fileProvider",
                            photoFile);
                }
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
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        boolean photo_editor = sharedpreferences.getBoolean(Helper.SET_PHOTO_EDITOR, true);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            if (data == null) {
                Toasty.error(getApplicationContext(), getString(R.string.toot_select_image_error), Toast.LENGTH_LONG).show();
                return;
            }

            ClipData clipData = data.getClipData();
            if (data.getData() == null && clipData == null) {
                Toasty.error(getApplicationContext(), getString(R.string.toot_select_image_error), Toast.LENGTH_LONG).show();
                return;
            }
            if (clipData != null) {
                ArrayList<Uri> mArrayUri = new ArrayList<>();
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    ClipData.Item item = clipData.getItemAt(i);
                    Uri uri = item.getUri();
                    mArrayUri.add(uri);
                }
                uploadSharedImage(mArrayUri);
            } else {
                String filename = Helper.getFileName(PixelfedComposeActivity.this, data.getData());
                ContentResolver cr = getContentResolver();
                String mime = cr.getType(data.getData());
                if (mime != null && (mime.toLowerCase().contains("video") || mime.toLowerCase().contains("gif"))) {
                    prepareUpload(PixelfedComposeActivity.this, data.getData(), filename, uploadReceiver);
                } else if (mime != null && mime.toLowerCase().contains("image")) {
                    if (photo_editor) {
                        Intent intent = new Intent(PixelfedComposeActivity.this, PhotoEditorActivity.class);
                        Bundle b = new Bundle();
                        intent.putExtra("imageUri", data.getData().toString());
                        intent.putExtras(b);
                        startActivity(intent);
                    } else {
                        prepareUpload(PixelfedComposeActivity.this, data.getData(), filename, uploadReceiver);
                    }
                } else if (mime != null && mime.toLowerCase().contains("audio")) {
                    prepareUpload(PixelfedComposeActivity.this, data.getData(), filename, uploadReceiver);
                } else {
                    Toasty.error(getApplicationContext(), getString(R.string.toot_select_image_error), Toast.LENGTH_LONG).show();
                }
            }

        } else if (requestCode == SEND_VOICE_MESSAGE && resultCode == RESULT_OK) {
            Uri uri = Uri.fromFile(new File(getCacheDir() + "/fedilab_recorded_audio.wav"));
            prepareUpload(PixelfedComposeActivity.this, uri, "fedilab_recorded_audio.wav", uploadReceiver);
        } else if (requestCode == TAKE_PHOTO && resultCode == RESULT_OK) {
            if (photo_editor) {
                Intent intent = new Intent(PixelfedComposeActivity.this, PhotoEditorActivity.class);
                Bundle b = new Bundle();
                intent.putExtra("imageUri", photoFileUri.toString());
                intent.putExtras(b);
                startActivity(intent);
            } else {
                prepareUpload(PixelfedComposeActivity.this, photoFileUri, null, uploadReceiver);
            }
        }
    }


    private void prepareUpload(Activity activity, Uri uri, String filename, UploadServiceSingleBroadcastReceiver uploadReceiver) {
        if (uploadReceiver == null) {
            uploadReceiver = new UploadServiceSingleBroadcastReceiver(PixelfedComposeActivity.this);
            uploadReceiver.register(this);
        }
        new asyncPicture(activity, social, uri, filename, uploadReceiver).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    static class asyncPicture extends AsyncTask<Void, Void, Void> {

        String commpressedFilePath = null;
        WeakReference<Activity> activityWeakReference;
        Uri uriFile;
        boolean error = false;
        UploadServiceSingleBroadcastReceiver uploadReceiver;
        String filename;
        UpdateAccountInfoAsyncTask.SOCIAL social;

        asyncPicture(Activity activity, UpdateAccountInfoAsyncTask.SOCIAL social, Uri uri, String filename, UploadServiceSingleBroadcastReceiver uploadReceiver) {
            this.activityWeakReference = new WeakReference<>(activity);
            this.uriFile = uri;
            this.uploadReceiver = uploadReceiver;
            this.filename = filename;
            this.social = social;
        }

        @Override
        protected void onPreExecute() {
            if (uriFile == null) {
                Toasty.error(activityWeakReference.get(), activityWeakReference.get().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                error = true;
            }
            activityWeakReference.get().findViewById(R.id.compression_loader).setVisibility(View.VISIBLE);

        }


        @Override
        protected Void doInBackground(Void... voids) {
            if (error) {
                return null;
            }
            commpressedFilePath = Helper.compressImagePath(activityWeakReference.get(), uriFile);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            activityWeakReference.get().findViewById(R.id.compression_loader).setVisibility(View.GONE);
            if (!error) {
                if( commpressedFilePath != null){
                    uriFile = Uri.fromFile(new File(commpressedFilePath));
                }
                Button upload_media = this.activityWeakReference.get().findViewById(R.id.upload_media);
                Button toot_it = this.activityWeakReference.get().findViewById(R.id.toot_it);
                upload_media.setEnabled(false);
                toot_it.setEnabled(false);
                if (filename == null) {
                    filename = Helper.getFileName(this.activityWeakReference.get(), uriFile);
                }
                filesMap.put(filename, uriFile);
                upload(activityWeakReference.get(), uriFile, filename, uploadReceiver);
            }
        }
    }


    static private void upload(Activity activity, Uri inUri, String fname, UploadServiceSingleBroadcastReceiver uploadReceiver) {
        String uploadId = UUID.randomUUID().toString();
        if (uploadReceiver != null) {
            uploadReceiver.setUploadID(uploadId);
        }
        Uri uri;
        InputStream tempInput = null;
        FileOutputStream tempOut = null;
        String filename = inUri.toString().substring(inUri.toString().lastIndexOf("/"));
        int suffixPosition = filename.lastIndexOf(".");
        String suffix = "";
        if (suffixPosition > 0) suffix = filename.substring(suffixPosition);
        try {
            File file;
            tempInput = activity.getContentResolver().openInputStream(inUri);
            if (fname.startsWith("fedilabins_")) {
                file = File.createTempFile("fedilabins_randomTemp1", suffix, activity.getCacheDir());
            } else {
                file = File.createTempFile("randomTemp1", suffix, activity.getCacheDir());
            }

            filesMap.put(file.getAbsolutePath(), inUri);
            tempOut = new FileOutputStream(file.getAbsoluteFile());
            byte[] buff = new byte[1024];
            int read;
            assert tempInput != null;
            while ((read = tempInput.read(buff)) > 0) {
                tempOut.write(buff, 0, read);
            }
            if (BuildConfig.DONATIONS) {
                uri = FileProvider.getUriForFile(activity,
                        "fr.gouv.etalab.mastodon.fileProvider",
                        file);
            } else {
                uri = FileProvider.getUriForFile(activity,
                        "app.fedilab.android.fileProvider",
                        file);
            }
            tempInput.close();
            tempOut.close();
        } catch (IOException e) {
            e.printStackTrace();
            uri = inUri;
        } finally {
            IOUtils.closeQuietly(tempInput);
            IOUtils.closeQuietly(tempOut);
        }

        try {
            final String fileName = FileNameCleaner.cleanFileName(fname);
            SharedPreferences sharedpreferences = activity.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            String scheme = sharedpreferences.getString(Helper.SET_ONION_SCHEME + Helper.getLiveInstance(activity), "https");
            String token = sharedpreferences.getString(Helper.PREF_KEY_OAUTH_TOKEN, null);
            int maxUploadRetryTimes = sharedpreferences.getInt(Helper.MAX_UPLOAD_IMG_RETRY_TIMES, 3);
            String url = scheme + "://" + Helper.getLiveInstance(activity) + "/api/v1/media";
            UploadNotificationConfig uploadConfig = new UploadNotificationConfig();
            uploadConfig
                    .setClearOnActionForAllStatuses(true);
            uploadConfig.getProgress().message = activity.getString(R.string.uploading);
            uploadConfig.getCompleted().autoClear = true;
            MultipartUploadRequest request = new MultipartUploadRequest(activity, uploadId, url);
            request.addHeader("Authorization", "Bearer " + token);
            request.setNotificationConfig(uploadConfig);
            request.addFileToUpload(uri.toString().replace("file://", ""), "file");
            request.addParameter("filename", fileName).setMaxRetries(maxUploadRetryTimes)
                    .startUpload();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPostAction(int statusCode, API.StatusAction statusAction, String userId, Error error) {
        if (error != null) {
            Toasty.error(getApplicationContext(), getString(R.string.toast_error), Toast.LENGTH_LONG).show();
        } else {
            Toasty.success(getApplicationContext(), getString(R.string.toot_scheduled), Toast.LENGTH_LONG).show();
            resetForNextToot();
        }
    }


    @Override
    public void onProgress(Context context, UploadInfo uploadInfo) {
        // your code here
    }

    @Override
    public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse,
                        Exception exception) {
        Toasty.error(getApplicationContext(), getString(R.string.toast_error), Toast.LENGTH_LONG).show();
        if (attachments.size() == 0) {
            pickup_picture.setVisibility(View.VISIBLE);
            imageSlider.setVisibility(View.GONE);
        }
        upload_media.setEnabled(true);
        toot_it.setEnabled(true);
    }

    @Override
    public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
        JSONObject response = null;
        try {
            response = new JSONObject(serverResponse.getBodyAsString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        addNewMedia(response, uploadInfo.getSuccessfullyUploadedFiles());
    }

    @Override
    public void onCancelled(Context context, UploadInfo uploadInfo) {
        // your code here
    }



    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle extras = intent.getExtras();
        if (extras != null && extras.getString("imageUri") != null) {
            Uri imageUri = Uri.parse(extras.getString("imageUri"));
            if (imageUri == null) {
                Toasty.error(getApplicationContext(), getString(R.string.toot_select_image_error), Toast.LENGTH_LONG).show();
                return;
            }
            String filename = Helper.getFileName(PixelfedComposeActivity.this, imageUri);

            prepareUpload(PixelfedComposeActivity.this, imageUri, filename, uploadReceiver);
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
        } else if (theme == Helper.THEME_BLACK) {
            style = R.style.DialogBlack;
        } else {
            style = R.style.Dialog;
        }

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_photo_camera:
                dispatchTakePictureIntent();
                return true;
            case R.id.action_store:
                storeToot(true, true);
                return true;
            case R.id.action_schedule:
                if (toot_content.getText().toString().trim().length() == 0) {
                    Toasty.error(getApplicationContext(), getString(R.string.toot_error_no_content), Toast.LENGTH_LONG).show();
                    return true;
                }
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(PixelfedComposeActivity.this, style);
                LayoutInflater inflater = this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.datetime_picker, null);
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
                        } else {
                            hour = timePicker.getCurrentHour();
                            minute = timePicker.getCurrentMinute();
                        }
                        Calendar calendar = new GregorianCalendar(datePicker.getYear(),
                                datePicker.getMonth(),
                                datePicker.getDayOfMonth(),
                                hour,
                                minute);
                        final long[] time = {calendar.getTimeInMillis()};

                        if ((time[0] - new Date().getTime()) < 60000) {
                            Toasty.warning(getApplicationContext(), getString(R.string.toot_scheduled_date), Toast.LENGTH_LONG).show();
                        } else {
                            SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
                            String instanceVersion = sharedpreferences.getString(Helper.INSTANCE_VERSION + userId + instance, null);
                            Version currentVersion = new Version(instanceVersion);
                            Version minVersion = new Version("2.7");
                            if (currentVersion.compareTo(minVersion) == 1 || currentVersion.equals(minVersion)) {
                                AlertDialog.Builder builderSingle = new AlertDialog.Builder(PixelfedComposeActivity.this, style);
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


    private void sendToot(String timestamp) {
        toot_it.setEnabled(false);
        if (toot_content.getText().toString().trim().length() == 0 && attachments.size() == 0) {
            Toasty.error(getApplicationContext(), getString(R.string.toot_error_no_content), Toast.LENGTH_LONG).show();
            toot_it.setEnabled(true);
            return;
        }


        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);

        String tootContent = toot_content.getText().toString().trim();

        Status toot = new Status();
        toot.setSensitive(isSensitive);
        toot.setVisibility(visibility);
        toot.setMedia_attachments(attachments);
        toot.setContent(tootContent);
        if (timestamp == null)
            if (scheduledstatus == null)
                new PostStatusAsyncTask(getApplicationContext(), social, account, toot, PixelfedComposeActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            else {
                toot.setScheduled_at(Helper.dateToString(scheduledstatus.getScheduled_date()));
                scheduledstatus.setStatus(toot);
                isScheduled = true;
                new PostActionAsyncTask(getApplicationContext(), API.StatusAction.DELETESCHEDULED, scheduledstatus, PixelfedComposeActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                new PostStatusAsyncTask(getApplicationContext(), social, account, toot, PixelfedComposeActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        else {
            toot.setScheduled_at(timestamp);
            new PostStatusAsyncTask(getApplicationContext(), social, account, toot, PixelfedComposeActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

    }


    private void serverSchedule(String time) {
        sendToot(time);
        isScheduled = true;
        resetForNextToot();
    }

    private void deviceSchedule(long time) {
        //Store the toot as draft first
        storeToot(false, false);
        isScheduled = true;
        //Schedules the toot
        ScheduledTootsSyncJob.schedule(getApplicationContext(), currentToId, time);
        resetForNextToot();
    }


    private void resetForNextToot() {
        //Clear content
        toot_content.setText("");
        toot_space_left.setText("0");
        if (attachments != null) {
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
        Toasty.info(getApplicationContext(), getString(R.string.toot_scheduled), Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_compose_pixelfed, menu);
        if (restored != -1) {
            MenuItem itemRestore = menu.findItem(R.id.action_restore);
            if (itemRestore != null)
                itemRestore.setVisible(false);
            MenuItem itemSchedule = menu.findItem(R.id.action_schedule);
            if (restoredScheduled)
                itemSchedule.setVisible(false);
        }

        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if (theme == Helper.THEME_LIGHT)
            Helper.colorizeIconMenu(menu, R.color.black);
        changeColor();
        return true;
    }


    @Override
    public void onDownloaded(String pathToFile, String url, Error error) {

        if (error == null && pathToFile != null) {
            Uri uri = Uri.fromFile(new File(pathToFile));
            String filename = FileNameCleaner.cleanFileName(url);
            upload_media.setEnabled(false);
            toot_it.setEnabled(false);
            upload(PixelfedComposeActivity.this, uri, filename, uploadReceiver);
        }
    }

    @Override
    public void onRetrieveAttachment(Attachment attachment, String fileName, Error error) {

    }

    @Override
    public void onUpdateProgress(int progress) {
        ProgressBar progressBar = findViewById(R.id.upload_progress);
        TextView toolbar_text = findViewById(R.id.toolbar_text);
        RelativeLayout progress_bar_container = findViewById(R.id.progress_bar_container);
        if (progress <= 100) {
            progressBar.setScaleY(3f);
            progress_bar_container.setVisibility(View.VISIBLE);
            progressBar.setProgress(progress);
            toolbar_text.setText(String.format("%s%%", progress));
        } else {
            progress_bar_container.setVisibility(View.GONE);
        }
    }




    private void tootVisibilityDialog() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(PixelfedComposeActivity.this, style);
        dialog.setTitle(R.string.toot_visibility_tilte);
        final String[] stringArray = getResources().getStringArray(R.array.toot_visibility);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(PixelfedComposeActivity.this, android.R.layout.simple_list_item_1, stringArray);
        dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position) {
                dialog.dismiss();
            }
        });
        dialog.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position) {
                switch (position) {
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
    protected void onResume() {
        super.onResume();
    }


    @Override
    public void onPause() {
        super.onPause();
        final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        boolean storeToot = sharedpreferences.getBoolean(Helper.SET_AUTO_STORE, true);
        if (storeToot && accountReply == null) {
            storeToot(true, false);
        } else if (storeToot) {
            storeToot(false, false);
        }
    }

    @Override
    public void onBackPressed() {
        final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        boolean storeToot = sharedpreferences.getBoolean(Helper.SET_AUTO_STORE, true);
        if (!storeToot) {
            if (toot_content.getText().toString().trim().length() == 0 && (attachments == null || attachments.size() < 1)) {
                finish();
            } else if (initialContent.trim().equals(toot_content.getText().toString().trim())) {
                finish();
            } else {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(PixelfedComposeActivity.this, style);
                dialogBuilder.setMessage(R.string.save_draft);
                dialogBuilder.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (accountReply == null) {
                            storeToot(true, false);
                        } else {
                            storeToot(false, false);
                        }
                        dialog.dismiss();
                        finish();
                    }
                });
                dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        finish();
                    }
                });
                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.setCancelable(false);
                alertDialog.show();
            }
        } else {
            super.onBackPressed();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(imageReceiver);
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(add_new_media);
        uploadReceiver.unregister(this);
    }

    @Override
    public void onPostStatusAction(APIResponse apiResponse) {
        if (apiResponse.getError() != null) {
            toot_it.setEnabled(true);
            if (apiResponse.getError().getError().contains("422")) {
                showAToast(getString(R.string.toast_error_char_limit));
                return;
            } else if (apiResponse.getError().getStatusCode() == -33) {
                storeToot(false, true);
            } else {
                showAToast(apiResponse.getError().getError());
                return;
            }

        }
        final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);

        if (apiResponse.getError() == null || apiResponse.getError().getStatusCode() != -33) {
            if (restored != -1) {
                SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                new StatusStoredDAO(getApplicationContext(), db).remove(restored);
            } else if (currentToId != -1) {
                SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                new StatusStoredDAO(getApplicationContext(), db).remove(currentToId);
            }
        }
        //Clear the toot
        toot_content.setText("");
        toot_space_left.setText("0");
        if (attachments != null) {
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
        if (apiResponse.getError() == null) {
            if (scheduledstatus == null && !isScheduled) {
                boolean display_confirm = sharedpreferences.getBoolean(Helper.SET_DISPLAY_CONFIRM, true);
                if (display_confirm) {
                    Toasty.success(getApplicationContext(), getString(R.string.toot_sent), Toast.LENGTH_LONG).show();
                }
            } else
                Toasty.success(getApplicationContext(), getString(R.string.toot_scheduled), Toast.LENGTH_LONG).show();
        } else {
            if (apiResponse.getError().getStatusCode() == -33)
                Toasty.info(getApplicationContext(), getString(R.string.toast_toot_saved_error), Toast.LENGTH_LONG).show();
        }
        toot_it.setEnabled(true);
        //It's a reply, so the user will be redirect to its answer
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra(Helper.INTENT_ACTION, Helper.HOME_TIMELINE_INTENT);
        startActivity(intent);
        finish();

    }

    @Override
    public void onRetrieveSearchAccounts(APIResponse apiResponse) {
        if (pp_progress != null && pp_actionBar != null) {
            pp_progress.setVisibility(View.GONE);
            pp_actionBar.setVisibility(View.VISIBLE);
        }
        if (apiResponse.getError() != null)
            return;
        final List<Account> accounts = apiResponse.getAccounts();
        if (accounts != null && accounts.size() > 0) {
            int currentCursorPosition = toot_content.getSelectionStart();
            AccountsSearchAdapter accountsListAdapter = new AccountsSearchAdapter(PixelfedComposeActivity.this, accounts);
            toot_content.setThreshold(1);
            toot_content.setAdapter(accountsListAdapter);
            final String oldContent = toot_content.getText().toString();
            if (oldContent.length() >= currentCursorPosition) {
                String[] searchA = oldContent.substring(0, currentCursorPosition).split("@");
                if (searchA.length > 0) {
                    final String search = searchA[searchA.length - 1];
                    toot_content.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Account account = accounts.get(position);
                            String deltaSearch = "";
                            int searchLength = searchDeep;
                            if (currentCursorPosition < searchDeep) { //Less than 15 characters are written before the cursor position
                                searchLength = currentCursorPosition;
                            }
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
                            if (currentCursorPosition < oldContent.length())
                                newContent += oldContent.substring(currentCursorPosition, oldContent.length());
                            toot_content.setText(newContent);
                            toot_space_left.setText(String.valueOf(countLength(social, toot_content)));
                            toot_content.setSelection(newPosition);
                            AccountsSearchAdapter accountsListAdapter = new AccountsSearchAdapter(PixelfedComposeActivity.this, new ArrayList<>());
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
        if (apiResponse.getError() != null || apiResponse.getAccounts() == null)
            return;
        this.contacts = new ArrayList<>();
        this.checkedValues = new ArrayList<>();
        this.contacts.addAll(apiResponse.getAccounts());
        for (Account account : contacts) {
            this.checkedValues.add(toot_content.getText().toString().contains("@" + account.getAcct()));
        }
        this.loader.setVisibility(View.GONE);
        AccountsReplyAdapter contactAdapter = new AccountsReplyAdapter(this.contacts, this.checkedValues);
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
        if (pp_progress != null && pp_actionBar != null) {
            pp_progress.setVisibility(View.GONE);
            pp_actionBar.setVisibility(View.VISIBLE);
        }

        if (emojis != null && emojis.size() > 0) {
            int currentCursorPosition = toot_content.getSelectionStart();
            EmojisSearchAdapter emojisSearchAdapter = new EmojisSearchAdapter(PixelfedComposeActivity.this, emojis);
            toot_content.setThreshold(1);
            toot_content.setAdapter(emojisSearchAdapter);
            final String oldContent = toot_content.getText().toString();
            String[] searchA = oldContent.substring(0, currentCursorPosition).split(":");
            if (searchA.length > 0) {
                final String search = searchA[searchA.length - 1];
                toot_content.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String shortcode = emojis.get(position).getShortcode();
                        String deltaSearch = "";
                        int searchLength = searchDeep;
                        if (currentCursorPosition < searchDeep) { //Less than 15 characters are written before the cursor position
                            searchLength = currentCursorPosition;
                        }
                        if (currentCursorPosition - searchLength > 0 && currentCursorPosition < oldContent.length())
                            deltaSearch = oldContent.substring(currentCursorPosition - searchLength, currentCursorPosition);
                        else {
                            if (currentCursorPosition >= oldContent.length())
                                deltaSearch = oldContent.substring(currentCursorPosition - searchLength, oldContent.length());
                        }

                        if (!search.equals(""))
                            deltaSearch = deltaSearch.replace(":" + search, "");
                        String newContent = oldContent.substring(0, currentCursorPosition - searchLength);
                        newContent += deltaSearch;
                        newContent += ":" + shortcode + ": ";
                        int newPosition = newContent.length();
                        if (currentCursorPosition < oldContent.length())
                            newContent += oldContent.substring(currentCursorPosition, oldContent.length());
                        toot_content.setText(newContent);
                        toot_space_left.setText(String.valueOf(countLength(social, toot_content)));
                        toot_content.setSelection(newPosition);
                        EmojisSearchAdapter emojisSearchAdapter = new EmojisSearchAdapter(PixelfedComposeActivity.this, new ArrayList<>());
                        toot_content.setThreshold(1);
                        toot_content.setAdapter(emojisSearchAdapter);

                    }
                });
            }
        }
    }


    @Override
    public void onRetrieveSearch(APIResponse apiResponse) {

        if (pp_progress != null && pp_actionBar != null) {
            pp_progress.setVisibility(View.GONE);
            pp_actionBar.setVisibility(View.VISIBLE);
        }
        if (apiResponse == null || apiResponse.getResults() == null)
            return;
        Results results = apiResponse.getResults();
        final List<String> tags = results.getHashtags();
        if (tags != null && tags.size() > 0) {
            int currentCursorPosition = toot_content.getSelectionStart();
            TagsSearchAdapter tagsSearchAdapter = new TagsSearchAdapter(PixelfedComposeActivity.this, tags);
            toot_content.setThreshold(1);
            toot_content.setAdapter(tagsSearchAdapter);
            final String oldContent = toot_content.getText().toString();
            if (oldContent.length() < currentCursorPosition)
                return;
            String[] searchA = oldContent.substring(0, currentCursorPosition).split("#");
            if (searchA.length < 1)
                return;
            final String search = searchA[searchA.length - 1];
            toot_content.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (position >= tags.size())
                        return;
                    String tag = tags.get(position);
                    String deltaSearch = "";
                    int searchLength = searchDeep;
                    if (currentCursorPosition < searchDeep) { //Less than 15 characters are written before the cursor position
                        searchLength = currentCursorPosition;
                    }
                    if (currentCursorPosition - searchLength > 0 && currentCursorPosition < oldContent.length())
                        deltaSearch = oldContent.substring(currentCursorPosition - searchLength, currentCursorPosition);
                    else {
                        if (currentCursorPosition >= oldContent.length())
                            deltaSearch = oldContent.substring(currentCursorPosition - searchLength, oldContent.length());
                    }

                    if (!search.equals(""))
                        deltaSearch = deltaSearch.replace("#" + search, "");
                    String newContent = oldContent.substring(0, currentCursorPosition - searchLength);
                    newContent += deltaSearch;
                    newContent += "#" + tag + " ";
                    int newPosition = newContent.length();
                    if (currentCursorPosition < oldContent.length())
                        newContent += oldContent.substring(currentCursorPosition, oldContent.length());
                    toot_content.setText(newContent);
                    toot_space_left.setText(String.valueOf(countLength(social, toot_content)));
                    toot_content.setSelection(newPosition);
                    TagsSearchAdapter tagsSearchAdapter = new TagsSearchAdapter(PixelfedComposeActivity.this, new ArrayList<>());
                    toot_content.setThreshold(1);
                    toot_content.setAdapter(tagsSearchAdapter);

                }
            });
        }
    }

    private void restoreToot(long id) {
        SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        StoredStatus draft = new StatusStoredDAO(PixelfedComposeActivity.this, db).getStatus(id);
        if (draft == null)
            return;
        Status status = draft.getStatus();
        //Retrieves attachments
        if (removed) {
            new StatusStoredDAO(PixelfedComposeActivity.this, db).remove(draft.getId());
        }
        restored = id;
        attachments = status.getMedia_attachments();

        ArrayList<ImageView> toRemove = new ArrayList<>();

        String content = status.getContent();
        Pattern mentionLink = Pattern.compile("(<\\s?a\\s?href=\"https?:\\/\\/([\\da-z\\.-]+\\.[a-z\\.]{2,10})\\/(@[\\/\\w._-]*)\"\\s?[^.]*<\\s?\\/\\s?a\\s?>)");
        Matcher matcher = mentionLink.matcher(content);
        if (matcher.find()) {
            content = matcher.replaceAll("$3@$2");
        }

        if (removed) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                content = Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY).toString();
            else
                content = Html.fromHtml(content).toString();
        }
        if (attachments != null && attachments.size() > 0) {
            sliderAdapter = new SliderAdapter(new WeakReference<>(PixelfedComposeActivity.this), true, attachments);
            imageSlider.setIndicatorAnimation(IndicatorAnimations.WORM);
            imageSlider.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
            imageSlider.setSliderAdapter(sliderAdapter);
            imageSlider.setVisibility(View.VISIBLE);
            pickup_picture.setVisibility(View.GONE);
            upload_media.setVisibility(View.VISIBLE);
            if (attachments.size() < max_media_count)
                upload_media.setEnabled(true);
            toot_it.setEnabled(true);
            toot_sensitive.setVisibility(View.VISIBLE);
            if (account.isSensitive()) {
                toot_sensitive.setChecked(true);
            }
        } else {
            imageSlider.setVisibility(View.GONE);
            pickup_picture.setVisibility(View.VISIBLE);
        }
        //Sensitive content
        toot_sensitive.setChecked(status.isSensitive());

        toot_content.setText(content);
        toot_space_left.setText(String.valueOf(countLength(social, toot_content)));
        toot_content.setSelection(toot_content.getText().length());

        switch (status.getVisibility()) {
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
        if (title != null) {
            if (social == UpdateAccountInfoAsyncTask.SOCIAL.GNU)
                title.setText(getString(R.string.queet_title));
            else
                title.setText(getString(R.string.toot_title));
        } else {
            if (social == UpdateAccountInfoAsyncTask.SOCIAL.GNU)
                setTitle(R.string.queet_title);
            else
                setTitle(R.string.toot_title);
        }
        invalidateOptionsMenu();
        initialContent = toot_content.getText().toString();
        toot_space_left.setText(String.valueOf(countLength(social, toot_content)));
    }


    public void redraw(){
        int position = imageSlider.getCurrentPagePosition();
        if( position > attachments.size()){
            position = attachments.size();
        }
        sliderAdapter = new SliderAdapter(new WeakReference<>(PixelfedComposeActivity.this), true, attachments);
        imageSlider.setIndicatorAnimation(IndicatorAnimations.WORM);
        imageSlider.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
        imageSlider.setSliderAdapter(sliderAdapter);
        imageSlider.setCurrentPagePosition(position);
    }

    private void restoreServerSchedule(Status status) {

        attachments = status.getMedia_attachments();
        ArrayList<ImageView> toRemove = new ArrayList<>();

        String content = status.getContent();
        Pattern mentionLink = Pattern.compile("(<\\s?a\\s?href=\"https?:\\/\\/([\\da-z\\.-]+\\.[a-z\\.]{2,10})\\/(@[\\/\\w._-]*)\"\\s?[^.]*<\\s?\\/\\s?a\\s?>)");
        Matcher matcher = mentionLink.matcher(content);
        if (matcher.find()) {
            content = matcher.replaceAll("$3@$2");
        }
        if (removed) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                content = Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY).toString();
            else
                content = Html.fromHtml(content).toString();
        }
        if (attachments != null && attachments.size() > 0) {
            int i = 0;
            for (final Attachment attachment : attachments) {
                String url = attachment.getPreview_url();
                if (url == null || url.trim().equals(""))
                    url = attachment.getUrl();
                final ImageView imageView = new ImageView(getApplicationContext());
                imageView.setId(Integer.parseInt(attachment.getId()));

                LinearLayout.LayoutParams imParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                imParams.setMargins(20, 5, 20, 5);
                imParams.height = (int) Helper.convertDpToPixel(100, getApplicationContext());
                imageView.setAdjustViewBounds(true);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);

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

                imageView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {

                        return false;
                    }
                });
                if (attachments.size() < max_media_count)
                    upload_media.setEnabled(true);
                toot_sensitive.setVisibility(View.VISIBLE);
                i++;
            }
        } else {
            imageSlider.setVisibility(View.GONE);
            pickup_picture.setVisibility(View.VISIBLE);
        }
        //Sensitive content
        toot_sensitive.setChecked(status.isSensitive());
        toot_content.setText(content);
        toot_space_left.setText(String.valueOf(countLength(social, toot_content)));
        toot_content.setSelection(toot_content.getText().length());
        switch (status.getVisibility()) {
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

        if (title != null) {
            if (social == UpdateAccountInfoAsyncTask.SOCIAL.GNU)
                title.setText(getString(R.string.queet_title));
            else
                title.setText(getString(R.string.toot_title));
        } else {
            if (social == UpdateAccountInfoAsyncTask.SOCIAL.GNU)
                setTitle(R.string.queet_title);
            else
                setTitle(R.string.toot_title);
        }
        invalidateOptionsMenu();
        initialContent = toot_content.getText().toString();
        toot_space_left.setText(String.valueOf(countLength(social, toot_content)));
    }

    private void storeToot(boolean message, boolean forced) {
        //Nothing to store here....
        String currentContent;
        currentContent = toot_content.getText().toString().trim();
        if (!forced) {
            if (currentContent.length() == 0 && (attachments == null || attachments.size() < 1) )
                return;
            if (initialContent == null || initialContent.trim().equals(currentContent))
                return;
        }
        Status toot = new Status();
        toot.setSensitive(isSensitive);
        toot.setMedia_attachments(attachments);
        toot.setVisibility(visibility);
        toot.setContent(currentContent);


        SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        try {
            if (currentToId == -1) {
                currentToId = new StatusStoredDAO(PixelfedComposeActivity.this, db).insertStatus(toot, null);

            } else {
                StoredStatus storedStatus = new StatusStoredDAO(PixelfedComposeActivity.this, db).getStatus(currentToId);
                if (storedStatus != null) {
                    new StatusStoredDAO(PixelfedComposeActivity.this, db).updateStatus(currentToId, toot);
                } else { //Might have been deleted, so it needs insertion
                    new StatusStoredDAO(PixelfedComposeActivity.this, db).insertStatus(toot, null);
                }
            }
            if (message)
                Toasty.success(getApplicationContext(), getString(R.string.toast_toot_saved), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            if (message)
                Toasty.error(getApplicationContext(), getString(R.string.toast_error), Toast.LENGTH_LONG).show();
        }
    }

    private void changeColor() {
        final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if (theme == Helper.THEME_DARK || theme == Helper.THEME_BLACK) {
            changeDrawableColor(PixelfedComposeActivity.this, R.drawable.ic_public_toot, R.color.dark_text);
            changeDrawableColor(PixelfedComposeActivity.this, R.drawable.ic_lock_open_toot, R.color.dark_text);
            changeDrawableColor(PixelfedComposeActivity.this, R.drawable.ic_lock_outline_toot, R.color.dark_text);
            changeDrawableColor(PixelfedComposeActivity.this, R.drawable.ic_mail_outline_toot, R.color.dark_text);
            changeDrawableColor(PixelfedComposeActivity.this, R.drawable.ic_insert_photo, R.color.dark_text);
            changeDrawableColor(PixelfedComposeActivity.this, R.drawable.ic_skip_previous, R.color.dark_text);
            changeDrawableColor(PixelfedComposeActivity.this, R.drawable.ic_skip_next, R.color.dark_text);
            changeDrawableColor(PixelfedComposeActivity.this, R.drawable.ic_check, R.color.dark_text);
            changeDrawableColor(PixelfedComposeActivity.this, R.drawable.emoji_one_category_smileysandpeople, R.color.dark_text);
            //bottom action
            changeDrawableColor(PixelfedComposeActivity.this, findViewById(R.id.poll_action), R.color.dark_text);
            changeDrawableColor(PixelfedComposeActivity.this, findViewById(R.id.toot_visibility), R.color.dark_text);
            changeDrawableColor(PixelfedComposeActivity.this, findViewById(R.id.toot_emoji), R.color.dark_text);
            Helper.changeButtonTextColor(PixelfedComposeActivity.this, findViewById(R.id.toot_cw), R.color.dark_text);

        } else {
            changeDrawableColor(PixelfedComposeActivity.this, R.drawable.ic_public_toot, R.color.white);
            changeDrawableColor(PixelfedComposeActivity.this, R.drawable.ic_lock_open_toot, R.color.white);
            changeDrawableColor(PixelfedComposeActivity.this, R.drawable.ic_lock_outline_toot, R.color.white);
            changeDrawableColor(PixelfedComposeActivity.this, R.drawable.ic_mail_outline_toot, R.color.white);
            changeDrawableColor(PixelfedComposeActivity.this, R.drawable.ic_insert_photo, R.color.white);
            changeDrawableColor(PixelfedComposeActivity.this, R.drawable.ic_skip_previous, R.color.white);
            changeDrawableColor(PixelfedComposeActivity.this, R.drawable.ic_skip_next, R.color.white);
            changeDrawableColor(PixelfedComposeActivity.this, R.drawable.ic_check, R.color.white);
            changeDrawableColor(PixelfedComposeActivity.this, R.drawable.emoji_one_category_smileysandpeople, R.color.black);
            //bottom action
            changeDrawableColor(PixelfedComposeActivity.this, findViewById(R.id.poll_action), R.color.black);
            changeDrawableColor(PixelfedComposeActivity.this, findViewById(R.id.toot_visibility), R.color.black);
            changeDrawableColor(PixelfedComposeActivity.this, findViewById(R.id.toot_emoji), R.color.black);
            Helper.changeButtonTextColor(PixelfedComposeActivity.this, findViewById(R.id.toot_cw), R.color.black);

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


    public static int countLength(UpdateAccountInfoAsyncTask.SOCIAL social, MastalabAutoCompleteTextView toot_content) {
        if (toot_content == null) {
            return -1;
        }
        String content = toot_content.getText().toString();
        String contentCount = content;
        if (social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON) {
            contentCount = contentCount.replaceAll("(^|[^/\\w])@(([a-z0-9_]+)@[a-z0-9.\\-]+[a-z0-9]+)", "$1@$3");
            Matcher matcherALink = Patterns.WEB_URL.matcher(contentCount);
            while (matcherALink.find()) {
                final String url = matcherALink.group(1);
                assert url != null;
                contentCount = contentCount.replace(url, "abcdefghijklmnopkrstuvw");
            }
        }

        return contentCount.length() - countWithEmoji(content);
    }

}
