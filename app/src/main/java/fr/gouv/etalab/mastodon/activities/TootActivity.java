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

import android.app.Activity;
import android.support.v4.content.ContextCompat;
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
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
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
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.gouv.etalab.mastodon.asynctasks.PostStatusAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveSearchAccountsAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.UploadActionAsyncTask;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Attachment;
import fr.gouv.etalab.mastodon.client.Entities.Error;
import fr.gouv.etalab.mastodon.client.Entities.Mention;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.client.Entities.StoredStatus;
import fr.gouv.etalab.mastodon.client.PatchBaseImageDownloader;
import fr.gouv.etalab.mastodon.drawers.AccountsSearchAdapter;
import fr.gouv.etalab.mastodon.drawers.DraftsListAdapter;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnPostStatusActionInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveAttachmentInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveSearcAccountshInterface;
import fr.gouv.etalab.mastodon.jobs.ScheduledTootsSyncJob;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import fr.gouv.etalab.mastodon.sqlite.StatusStoredDAO;
import mastodon.etalab.gouv.fr.mastodon.R;

import static fr.gouv.etalab.mastodon.helper.Helper.HOME_TIMELINE_INTENT;
import static fr.gouv.etalab.mastodon.helper.Helper.INTENT_ACTION;
import static fr.gouv.etalab.mastodon.helper.Helper.changeDrawableColor;

/**
 * Created by Thomas on 01/05/2017.
 * Toot activity class
 */

public class TootActivity extends AppCompatActivity implements OnRetrieveSearcAccountshInterface, OnRetrieveAttachmentInterface, OnPostStatusActionInterface {


    private String visibility;
    private final int PICK_IMAGE = 56556;
    private ProgressBar loading_picture;
    private ImageButton toot_picture;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private LinearLayout toot_picture_container;
    private ArrayList<Attachment> attachments;
    private boolean isSensitive = false;
    private ImageButton toot_visibility;
    private Button toot_it;
    private AutoCompleteTextView toot_content;
    private EditText toot_cw_content;
    private LinearLayout toot_reply_content_container;
    private Status tootReply = null;
    private String sharedContent, sharedSubject;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
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
            View view = inflater.inflate(R.layout.toot_action_bar, null);
            actionBar.setCustomView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

            ImageView close_toot = (ImageView) actionBar.getCustomView().findViewById(R.id.close_toot);
            close_toot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InputMethodManager inputMethodManager = (InputMethodManager)  getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(toot_content.getWindowToken(), 0);
                    finish();
                }
            });
            title = (TextView) actionBar.getCustomView().findViewById(R.id.toolbar_title);
            pp_actionBar = (ImageView) actionBar.getCustomView().findViewById(R.id.pp_actionBar);
            pp_progress = (ProgressBar) actionBar.getCustomView().findViewById(R.id.pp_progress);

        }

        //By default the toot is not restored so the id -1 is defined
        currentToId = -1;
        imageLoader = ImageLoader.getInstance();
        File cacheDir = new File(getCacheDir(), getString(R.string.app_name));
        ImageLoaderConfiguration configImg = new ImageLoaderConfiguration.Builder(this)
                .imageDownloader(new PatchBaseImageDownloader(getApplicationContext()))
                .threadPoolSize(5)
                .threadPriority(Thread.MIN_PRIORITY + 3)
                .denyCacheImageMultipleSizesInMemory()
                .diskCache(new UnlimitedDiskCache(cacheDir))
                .build();
        imageLoader.init(configImg);
        options = new DisplayImageOptions.Builder().displayer(new SimpleBitmapDisplayer()).cacheInMemory(false)
                .cacheOnDisk(true).resetViewBeforeLoading(true).build();

        toot_it = (Button) findViewById(R.id.toot_it);
        Button toot_cw = (Button) findViewById(R.id.toot_cw);
        toot_space_left = (TextView) findViewById(R.id.toot_space_left);
        toot_visibility = (ImageButton) findViewById(R.id.toot_visibility);
        toot_picture = (ImageButton) findViewById(R.id.toot_picture);
        loading_picture = (ProgressBar) findViewById(R.id.loading_picture);
        toot_picture_container = (LinearLayout) findViewById(R.id.toot_picture_container);
        toot_content = (AutoCompleteTextView) findViewById(R.id.toot_content);
        toot_cw_content = (EditText) findViewById(R.id.toot_cw_content);
        toot_reply_content_container = (LinearLayout) findViewById(R.id.toot_reply_content_container);
        picture_scrollview = (HorizontalScrollView) findViewById(R.id.picture_scrollview);
        toot_sensitive = (CheckBox) findViewById(R.id.toot_sensitive);
        //search_small_container = (LinearLayout) findViewById(R.id.search_small_container);

        drawer_layout = (LinearLayout) findViewById(R.id.drawer_layout);

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
        restored = -1;
        if(b != null) {
            tootReply = b.getParcelable("tootReply");
            sharedContent = b.getString("sharedContent", null);
            sharedSubject = b.getString("sharedSubject", null);
            restored = b.getLong("restored", -1);
        }
        if( restored != -1 ){
            toot_it.setVisibility(View.GONE);
            invalidateOptionsMenu();
        }
        if( tootReply != null) {
            tootReply();
        }else {
            if( title != null)
                title.setText(getString(R.string.toot_title));
            else
                setTitle(R.string.toot_title);
        }
        SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        Account account = new AccountDAO(getApplicationContext(),db).getAccountByID(userId);
        String url = account.getAvatar();
        if( url.startsWith("/") ){
            url = "https://" + Helper.getLiveInstance(getApplicationContext()) + account.getAvatar();
        }
        imageLoader.loadImage(url, options, new SimpleImageLoadingListener(){
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                super.onLoadingComplete(imageUri, view, loadedImage);
                BitmapDrawable ppDrawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(loadedImage, (int) Helper.convertDpToPixel(25, getApplicationContext()), (int) Helper.convertDpToPixel(25, getApplicationContext()), true));
                if( pp_actionBar != null){
                    pp_actionBar.setImageDrawable(ppDrawable);
                } else if( getSupportActionBar() != null){

                    getSupportActionBar().setIcon(ppDrawable);
                    getSupportActionBar().setDisplayShowHomeEnabled(true);
                }
            }
            @Override
            public void onLoadingFailed(java.lang.String imageUri, android.view.View view, FailReason failReason){

            }});

        if( sharedContent != null ){ //Shared content
            if( sharedSubject != null){
                sharedContent = sharedSubject + "\n\n" + sharedContent;
            }
            toot_content.setText( String.format("\n%s", sharedContent));
        }
        attachments = new ArrayList<>();
        int charsInCw = 0;
        int charsInToot = 0;

        boolean isAccountPrivate = account.isLocked();

        if(isAccountPrivate){
            visibility = "private";
            toot_visibility.setImageResource(R.drawable.ic_action_lock_closed);
        }else {
            if( tootReply == null){
                visibility = sharedpreferences.getString(Helper.SET_TOOT_VISIBILITY + "@" + account.getAcct() + "@" + account.getInstance(), "public");
                switch (visibility) {
                    case "public":
                        toot_visibility.setImageResource(R.drawable.ic_action_globe);
                        break;
                    case "unlisted":
                        toot_visibility.setImageResource(R.drawable.ic_action_lock_open);
                        break;
                    case "private":
                        toot_visibility.setImageResource(R.drawable.ic_action_lock_closed);
                        break;
                    case "direct":
                        toot_visibility.setImageResource(R.drawable.ic_local_post_office);
                        break;
                }
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

                if( tootReply != null) {
                    toot.setIn_reply_to_id(tootReply.getId());

                    /*
                       Strip the first appearance of " . "
                        that we added to get capitalisation,
                        from toot before posting it. Makes
                        the end toot cleaner.
                     */
                    String preToot = toot_content.getText().toString().trim();
                    String postToot = preToot.replaceFirst(" \\. ", "");

                    toot.setContent(postToot.trim());
                }else {
                    toot.setContent(toot_content.getText().toString().trim());
                }

                new PostStatusAsyncTask(getApplicationContext(), toot, TootActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            }
        });

        toot_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("*/*");
                    String[] mimetypes = {"image/*", "video/*"};
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                    startActivityForResult(intent, PICK_IMAGE);
                }else {
                    Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    getIntent.setType("image/*");

                    Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    pickIntent.setType("image/*");

                    Intent chooserIntent = Intent.createChooser(getIntent, getString(R.string.toot_select_image));
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});
                    startActivityForResult(chooserIntent, PICK_IMAGE);
                }

            }
        });
        String pattern = "^(.|\\s)*(@([a-zA-Z0-9_]{2,}))$";
        final Pattern sPattern = Pattern.compile(pattern);

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
                if( currentCursorPosition- (searchLength-1) < 0 || currentCursorPosition == 0 || currentCursorPosition > s.toString().length())
                    return;
                Matcher m;

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
                }else{toot_content.dismissDropDown();}
                int totalChar = toot_cw_content.length() + toot_content.length();
                toot_space_left.setText(String.valueOf(totalChar));
            }
        });
        //Allow scroll of the EditText though it's embedded in a scrollview
        toot_content.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v.getId() == R.id.toot_content) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_UP:
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                }
                return false;
            }
        });
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
        if( restored != -1 ){
            restoreToot(restored);
        }
        changeColor();
        if( theme == Helper.THEME_LIGHT) {
            toot_it.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        }
    }


    public void showAToast (String message){
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        mToast.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            picture_scrollview.setVisibility(View.VISIBLE);
            if (data == null) {
                Toast.makeText(getApplicationContext(),R.string.toot_select_image_error,Toast.LENGTH_LONG).show();
                return;
            }
            try {
                InputStream inputStream = getContentResolver().openInputStream(data.getData());
                loading_picture.setVisibility(View.VISIBLE);
                toot_picture.setEnabled(false);
                new UploadActionAsyncTask(getApplicationContext(),inputStream,TootActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch (FileNotFoundException e) {
                Toast.makeText(getApplicationContext(),R.string.toot_select_image_error,Toast.LENGTH_LONG).show();
                loading_picture.setVisibility(View.GONE);
                toot_picture.setEnabled(true);
                e.printStackTrace();
            }
        }else if(requestCode == Helper.REQ_CODE_SPEECH_INPUT && resultCode == Activity.RESULT_OK){
            if (null != data) {
                ArrayList<String> result = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                toot_content.setText(result.get(0));
                toot_content.setSelection(toot_content.getText().length());
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
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
                storeToot(true);
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
                View dialogView = inflater.inflate(R.layout.datetime_picker, null);
                dialogBuilder.setView(dialogView);
                final AlertDialog alertDialog = dialogBuilder.create();

                final DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.date_picker);
                final TimePicker timePicker = (TimePicker) dialogView.findViewById(R.id.time_picker);
                Button date_time_cancel = (Button) dialogView.findViewById(R.id.date_time_cancel);
                final ImageButton date_time_previous = (ImageButton) dialogView.findViewById(R.id.date_time_previous);
                final ImageButton date_time_next = (ImageButton) dialogView.findViewById(R.id.date_time_next);
                final ImageButton date_time_set = (ImageButton) dialogView.findViewById(R.id.date_time_set);

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
                            storeToot(false);
                            //Schedules the toot
                            ScheduledTootsSyncJob.schedule(getApplicationContext(), currentToId, time);
                            //Clear content
                            toot_content.setText("");
                            toot_cw_content.setText("");
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
                changeColor();
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
        return true;
    }

    @Override
    public void onRetrieveAttachment(final Attachment attachment, Error error) {
        loading_picture.setVisibility(View.GONE);
        if( error != null){
            final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            boolean show_error_messages = sharedpreferences.getBoolean(Helper.SET_SHOW_ERROR_MESSAGES, true);
            if( show_error_messages)
                Toast.makeText(getApplicationContext(), error.getError(),Toast.LENGTH_LONG).show();
            if( attachments.size() == 0 )
                toot_picture_container.setVisibility(View.GONE);
            return;
        }
        toot_picture_container.setVisibility(View.VISIBLE);
        if( attachment != null ){
            String url = attachment.getPreview_url();
            if( url == null || url.trim().equals(""))
                url = attachment.getUrl();

            final ImageView imageView = new ImageView(getApplicationContext());
            imageView.setId(Integer.parseInt(attachment.getId()));
            imageLoader.displayImage(url, imageView, options);
            LinearLayout.LayoutParams imParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            imParams.setMargins(20, 5, 20, 5);
            imParams.height = (int) Helper.convertDpToPixel(100, getApplicationContext());
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            boolean show_media_urls = sharedpreferences.getBoolean(Helper.SET_MEDIA_URLS, true);
            if( show_media_urls) {
                //Adds the url at the end of the toot 
                int selectionBefore = toot_content.getSelectionStart();
                toot_content.setText(toot_content.getText().toString() + "\n" + attachment.getUrl());
                //Moves the cursor
                if (selectionBefore >= 0)
                    toot_content.setSelection(selectionBefore);
            }
            toot_picture_container.addView(imageView, attachments.size(), imParams);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showRemove(imageView.getId());
                }
            });
            attachments.add(attachment);
            if( attachments.size() < 4)
                toot_picture.setEnabled(true);
            toot_sensitive.setVisibility(View.VISIBLE);
            picture_scrollview.setVisibility(View.VISIBLE);
        }
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
                            //Adds the url at the end of the toot 
                            int selectionBefore = toot_content.getSelectionStart();
                            toot_content.setText(toot_content.getText().toString().replace(attachment.getUrl(), ""));
                            //Moves the cursor
                            if (selectionBefore >= 0)
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
                        toot_visibility.setImageResource(R.drawable.ic_action_globe);
                        break;
                    case 1:
                        visibility = "unlisted";
                        toot_visibility.setImageResource(R.drawable.ic_action_lock_open);
                        break;
                    case 2:
                        visibility = "private";
                        toot_visibility.setImageResource(R.drawable.ic_action_lock_closed);
                        break;
                    case 3:
                        visibility = "direct";
                        toot_visibility.setImageResource(R.drawable.ic_local_post_office);
                        break;
                }
                changeColor();
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
        if( storeToot)
            storeToot(true);
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
        //Clear the toot
        toot_content.setText("");
        toot_cw_content.setText("");
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
                    b.putString("statusId", status.getId());
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
        if( apiResponse.getError() != null){
            final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            boolean show_error_messages = sharedpreferences.getBoolean(Helper.SET_SHOW_ERROR_MESSAGES, true);
            if( show_error_messages)
                Toast.makeText(getApplicationContext(), apiResponse.getError().getError(),Toast.LENGTH_LONG).show();
            return;
        }

        final List<Account> accounts = apiResponse.getAccounts();
        if( accounts != null && accounts.size() > 0){
            AccountsSearchAdapter accountsListAdapter = new AccountsSearchAdapter(TootActivity.this, accounts);
            toot_content.setThreshold(1);
            toot_content.setAdapter(accountsListAdapter);
            final String oldContent = toot_content.getText().toString();
            String[] searchA = oldContent.substring(0,currentCursorPosition).split("@");
            final String search = searchA[searchA.length-1];
            toot_content.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Account account = accounts.get(position);
                    String deltaSearch = "";
                    if( currentCursorPosition-searchLength > 0 && currentCursorPosition < oldContent.length() )
                        deltaSearch = oldContent.substring(currentCursorPosition-searchLength, currentCursorPosition);
                    else {
                        if( currentCursorPosition >= oldContent.length() )
                            deltaSearch = oldContent.substring(currentCursorPosition-searchLength, oldContent.length());
                    }

                    if( !search.equals(""))
                        deltaSearch = deltaSearch.replace("@"+search,"");
                    String newContent = oldContent.substring(0,currentCursorPosition-searchLength);
                    newContent += deltaSearch;
                    newContent += "@" + account.getAcct() + " ";
                    int newPosition = newContent.length();
                    if( currentCursorPosition < oldContent.length() - 1)
                        newContent +=   oldContent.substring(currentCursorPosition, oldContent.length()-1);
                    toot_content.setText(newContent);
                    toot_content.setSelection(newPosition);
                    AccountsSearchAdapter accountsListAdapter = new AccountsSearchAdapter(TootActivity.this, new ArrayList<Account>());
                    toot_content.setThreshold(1);
                    toot_content.setAdapter(accountsListAdapter);
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
                    toRemove.add((ImageView) toot_picture_container.getChildAt(i));
            }
            if( toRemove.size() > 0){
                for(ImageView imageView: toRemove)
                    toot_picture_container.removeView(imageView);
            }
            toRemove.clear();
        }
        loading_picture.setVisibility(View.GONE);
        if( attachments != null && attachments.size() > 0){
            toot_picture_container.setVisibility(View.VISIBLE);
            picture_scrollview.setVisibility(View.VISIBLE);
            int i = 0 ;
            for(Attachment attachment: attachments){
                String url = attachment.getPreview_url();
                if( url == null || url.trim().equals(""))
                    url = attachment.getUrl();
                final ImageView imageView = new ImageView(getApplicationContext());
                imageView.setId(Integer.parseInt(attachment.getId()));
                imageLoader.displayImage(url, imageView, options);
                LinearLayout.LayoutParams imParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                imParams.setMargins(20, 5, 20, 5);
                imParams.height = (int) Helper.convertDpToPixel(100, getApplicationContext());
                imageView.setAdjustViewBounds(true);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                toot_picture_container.addView(imageView, i, imParams);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showRemove(imageView.getId());
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
        toot_content.setSelection(toot_content.getText().length());
        switch (status.getVisibility()){
            case "public":
                visibility = "public";
                toot_visibility.setImageResource(R.drawable.ic_action_globe);
                break;
            case "unlisted":
                visibility = "unlisted";
                toot_visibility.setImageResource(R.drawable.ic_action_lock_open);
                break;
            case "private":
                visibility = "private";
                toot_visibility.setImageResource(R.drawable.ic_action_lock_closed);
                break;
            case "direct":
                visibility = "direct";
                toot_visibility.setImageResource(R.drawable.ic_local_post_office);
                break;
        }
        changeColor();
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
        toot_space_left.setText(String.valueOf(toot_content.getText().length() + toot_cw_content.getText().length()));
    }


    private void tootReply(){
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        if( title != null)
            title.setText(getString(R.string.toot_title_reply));
        else
            setTitle(R.string.toot_title_reply);
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);


        FloatingActionButton ic_show = (FloatingActionButton) findViewById(R.id.toot_show_reply);

        ic_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                alert.show();
            }
        });
        toot_reply_content_container.setVisibility(View.VISIBLE);
        switch (tootReply.getVisibility()){
            case "public":
                visibility = "public";
                toot_visibility.setImageResource(R.drawable.ic_action_globe);
                break;
            case "unlisted":
                visibility = "unlisted";
                toot_visibility.setImageResource(R.drawable.ic_action_lock_open);
                break;
            case "private":
                visibility = "private";
                toot_visibility.setImageResource(R.drawable.ic_action_lock_closed);
                break;
            case "direct":
                visibility = "direct";
                toot_visibility.setImageResource(R.drawable.ic_local_post_office);
                break;
        }
        changeColor();
        //If toot is not restored
        if( restored == -1 ){
            //Retrieves mentioned accounts + OP and adds them at the beginin of the toot
            ArrayList<String> mentionedAccountsAdded = new ArrayList<>();
            if( tootReply.getAccount() != null && tootReply.getAccount().getAcct() != null && !tootReply.getAccount().getId().equals(userId)) {
                toot_content.setText(String.format("@%s ", tootReply.getAccount().getAcct()));
                mentionedAccountsAdded.add(tootReply.getAccount().getAcct());
            }
            if( tootReply.getMentions() != null ){
                for(Mention mention : tootReply.getMentions()){
                    if(  mention.getAcct() != null && !mention.getId().equals(userId) && !mentionedAccountsAdded.contains(mention.getAcct())) {
                        mentionedAccountsAdded.add(mention.getAcct());
                        String tootTemp = String.format("@%s ", mention.getAcct());
                        toot_content.setText(String.format("%s ", (toot_content.getText().toString() + " " + tootTemp)));
                    }
                }
            }

            if (toot_content.getText().toString().startsWith("@")) {
                //Put a "<space>dot<space>" at the end of all mentioned account to force capitalization
                toot_content.append(" . ");
            }

            toot_content.setSelection(toot_content.getText().length()); //Put cursor at the end
        }
    }



    private void storeToot(boolean message){
        //Nothing to store here....
        if(toot_content.getText().toString().trim().length() == 0 && (attachments == null || attachments.size() <1) && toot_cw_content.getText().toString().trim().length() == 0)
            return;

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
            changeDrawableColor(TootActivity.this, R.drawable.ic_action_globe, R.color.dark_text);
            changeDrawableColor(TootActivity.this, R.drawable.ic_action_lock_open, R.color.dark_text);
            changeDrawableColor(TootActivity.this, R.drawable.ic_action_lock_closed, R.color.dark_text);
            changeDrawableColor(TootActivity.this, R.drawable.ic_local_post_office, R.color.dark_text);
            changeDrawableColor(TootActivity.this, R.drawable.ic_action_camera, R.color.dark_text);
            changeDrawableColor(TootActivity.this, R.drawable.ic_skip_previous, R.color.dark_text);
            changeDrawableColor(TootActivity.this, R.drawable.ic_skip_next, R.color.dark_text);
            changeDrawableColor(TootActivity.this, R.drawable.ic_check, R.color.dark_text);
        }else {
            changeDrawableColor(TootActivity.this, R.drawable.ic_action_globe, R.color.white);
            changeDrawableColor(TootActivity.this, R.drawable.ic_action_lock_open, R.color.white);
            changeDrawableColor(TootActivity.this, R.drawable.ic_action_lock_closed, R.color.white);
            changeDrawableColor(TootActivity.this, R.drawable.ic_local_post_office, R.color.white);
            changeDrawableColor(TootActivity.this, R.drawable.ic_action_camera, R.color.white);
            changeDrawableColor(TootActivity.this, R.drawable.ic_skip_previous, R.color.white);
            changeDrawableColor(TootActivity.this, R.drawable.ic_skip_next, R.color.white);
            changeDrawableColor(TootActivity.this, R.drawable.ic_check, R.color.white);
        }
    }


}
