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
 * You should have received a copy of the GNU General Public License along with Thomas Schneider; if not,
 * see <http://www.gnu.org/licenses>. */
package fr.gouv.etalab.mastodon.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;


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

import fr.gouv.etalab.mastodon.asynctasks.PostActionAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveSearchAccountsAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.UploadActionAsyncTask;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Attachment;
import fr.gouv.etalab.mastodon.client.Entities.Error;
import fr.gouv.etalab.mastodon.client.Entities.Mention;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.client.Entities.StoredStatus;
import fr.gouv.etalab.mastodon.drawers.AccountsSearchAdapter;
import fr.gouv.etalab.mastodon.drawers.DraftsListAdapter;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnPostActionInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveAttachmentInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveSearcAccountshInterface;
import fr.gouv.etalab.mastodon.jobs.ScheduledTootsSyncJob;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.StatusStoredDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import mastodon.etalab.gouv.fr.mastodon.R;

import static fr.gouv.etalab.mastodon.helper.Helper.changeDrawableColor;


/**
 * Created by Thomas on 01/05/2017.
 * Toot activity class
 */

public class TootActivity extends AppCompatActivity implements OnRetrieveSearcAccountshInterface, OnRetrieveAttachmentInterface, OnPostActionInterface {


    private int charsInCw;
    private int charsInToot;
    private int maxChar;
    private String visibility;
    private final int PICK_IMAGE = 56556;
    private RelativeLayout loading_picture;
    private ImageButton toot_picture;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private LinearLayout toot_picture_container;
    private ArrayList<Attachment> attachments;
    private boolean isSensitive = false;
    private ImageButton toot_visibility;
    private Button toot_it;
    private EditText toot_content, toot_cw_content;
    private LinearLayout toot_reply_content_container;
    private RelativeLayout toot_show_accounts;
    private ListView toot_lv_accounts;
    private BroadcastReceiver search_validate;
    private Status tootReply = null;
    private String sharedContent, sharedSubject;
    private CheckBox toot_sensitive;
    public long currentToId;

    private String pattern = "^.*(@([a-zA-Z0-9_]{2,}))$";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if( theme == Helper.THEME_LIGHT){
            setTheme(R.style.AppTheme);
        }else {
            setTheme(R.style.AppThemeDark);
        }
        setContentView(R.layout.activity_toot);

        if( getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //By default the toot is not restored so the id -1 is defined
        currentToId = -1;
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder().displayer(new SimpleBitmapDisplayer()).cacheInMemory(false)
                .cacheOnDisk(true).resetViewBeforeLoading(true).build();

        toot_it = (Button) findViewById(R.id.toot_it);
        Button toot_cw = (Button) findViewById(R.id.toot_cw);
        final TextView toot_space_left = (TextView) findViewById(R.id.toot_space_left);
        toot_visibility = (ImageButton) findViewById(R.id.toot_visibility);
        toot_picture = (ImageButton) findViewById(R.id.toot_picture);
        loading_picture = (RelativeLayout) findViewById(R.id.loading_picture);
        toot_picture_container = (LinearLayout) findViewById(R.id.toot_picture_container);
        toot_content = (EditText) findViewById(R.id.toot_content);
        toot_cw_content = (EditText) findViewById(R.id.toot_cw_content);
        TextView toot_reply_content = (TextView) findViewById(R.id.toot_reply_content);
        toot_reply_content_container = (LinearLayout) findViewById(R.id.toot_reply_content_container);
        toot_show_accounts = (RelativeLayout) findViewById(R.id.toot_show_accounts);
        toot_lv_accounts = (ListView) findViewById(R.id.toot_lv_accounts);
        toot_sensitive = (CheckBox) findViewById(R.id.toot_sensitive);


        if( theme == Helper.THEME_DARK){
            changeDrawableColor(TootActivity.this, R.drawable.ic_action_globe,R.color.dark_text);
            changeDrawableColor(TootActivity.this, R.drawable.ic_action_lock_open,R.color.dark_text);
            changeDrawableColor(TootActivity.this, R.drawable.ic_action_lock_closed,R.color.dark_text);
            changeDrawableColor(TootActivity.this, R.drawable.ic_local_post_office,R.color.dark_text);

            changeDrawableColor(TootActivity.this, R.drawable.ic_action_globe,R.color.dark_text);
            changeDrawableColor(TootActivity.this, R.drawable.ic_action_camera,R.color.dark_text);
        }else {
            changeDrawableColor(TootActivity.this, R.drawable.ic_action_globe,R.color.black);
            changeDrawableColor(TootActivity.this, R.drawable.ic_action_lock_open,R.color.black);
            changeDrawableColor(TootActivity.this, R.drawable.ic_action_lock_closed,R.color.black);
            changeDrawableColor(TootActivity.this, R.drawable.ic_local_post_office,R.color.black);

            changeDrawableColor(TootActivity.this, R.drawable.ic_action_globe,R.color.black);
            changeDrawableColor(TootActivity.this, R.drawable.ic_action_camera,R.color.black);
        }

        final LinearLayout drawer_layout = (LinearLayout) findViewById(R.id.drawer_layout);

        /*drawer_layout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = drawer_layout.getRootView().getHeight() - drawer_layout.getHeight();
                if (heightDiff > 100) {
                    ViewGroup.LayoutParams params = toot_picture_container.getLayoutParams();
                    params.height = (int) Helper.convertDpToPixel(20, getApplicationContext());
                    params.width = (int) Helper.convertDpToPixel(20, getApplicationContext());
                    toot_picture_container.setLayoutParams(params);
                } else {
                    ViewGroup.LayoutParams params = toot_picture_container.getLayoutParams();
                    params.height = (int) Helper.convertDpToPixel(100, getApplicationContext());
                    params.width = (int) Helper.convertDpToPixel(100, getApplicationContext());
                    toot_picture_container.setLayoutParams(params);
                }
            }
        });*/

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
        if(b != null) {
            tootReply = b.getParcelable("tootReply");
            sharedContent = b.getString("sharedContent", null);
            sharedSubject = b.getString("sharedSubject", null);
        }
        if( tootReply != null) {
            setTitle(R.string.toot_title_reply);
            String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
            boolean show_reply = sharedpreferences.getBoolean(Helper.SET_SHOW_REPLY, false);
            if( show_reply ){
                toot_reply_content_container.setVisibility(View.VISIBLE);
            }else {
                toot_reply_content_container.setVisibility(View.GONE);
            }
            String content = tootReply.getContent();
            if(tootReply.getReblog() != null)
                content = tootReply.getReblog().getContent();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                toot_reply_content.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT));
            else
                //noinspection deprecation
                toot_reply_content.setText(Html.fromHtml(content));
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
            //Retrieves mentioned accounts + OP and adds them at the beginin of the toot
            if( tootReply.getAccount() != null && tootReply.getAccount().getAcct() != null && !tootReply.getAccount().getId().equals(userId)) {
                toot_content.setText(String.format("@%s ", tootReply.getAccount().getAcct()));
            }
            if( tootReply.getMentions() != null ){
                for(Mention mention : tootReply.getMentions()){
                    if(  mention.getAcct() != null && !mention.getId().equals(userId)) {
                        String tootTemp = String.format("@%s ", mention.getAcct());
                        toot_content.setText(String.format("%s ", (toot_content.getText().toString() + " " + tootTemp)));
                    }
                }
            }
            toot_content.setSelection(toot_content.getText().length()); //Put cursor at the end
        }else {
            setTitle(R.string.toot_title);
        }

        if( sharedContent != null ){ //Shared content
            if( sharedSubject != null){
                sharedContent = sharedSubject + "\n\n" + sharedContent;
            }
            toot_content.setText( String.format("\n%s", sharedContent));
        }
        attachments = new ArrayList<>();
        charsInCw = 0;
        charsInToot = 0;
        maxChar = 500;

        //Register LocalBroadcast to receive selected accounts after search
        search_validate = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String acct = intent.getStringExtra("acct");
                if( acct != null){
                    acct = "@" + acct;
                    String content = toot_content.getText().toString();
                    String[] splitContent = content.split("@");
                    String newContent = "";
                    for(int i = 0 ; i < (splitContent.length -1) ; i++){
                        newContent += splitContent[i];
                    }
                    newContent += acct + " ";
                    toot_content.setText(newContent);
                    toot_content.setSelection(toot_content.getText().length());
                }
                toot_show_accounts.setVisibility(View.GONE);
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(search_validate, new IntentFilter(Helper.SEARCH_VALIDATE_ACCOUNT));


        FloatingActionButton toot_close_accounts = (FloatingActionButton) findViewById(R.id.toot_close_accounts);
        SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        Account account = new AccountDAO(getApplicationContext(),db).getAccountByID(userId);
        boolean isAccountPrivate = account.isLocked();

        FloatingActionButton ic_close = (FloatingActionButton) findViewById(R.id.toot_close_reply);

        toot_close_accounts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toot_show_accounts.setVisibility(View.GONE);
            }
        });

        ic_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toot_reply_content_container.setVisibility(View.GONE);
            }
        });

        if(isAccountPrivate){
            visibility = "private";
            toot_visibility.setImageResource(R.drawable.ic_action_lock_closed);
        }else {
            visibility = "public";
            toot_visibility.setImageResource(R.drawable.ic_action_globe);
        }

        toot_sensitive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isSensitive = isChecked;
            }
        });

        toot_space_left.setText(String.valueOf((maxChar - (charsInToot + charsInCw))));
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
                toot.setContent(toot_content.getText().toString().trim());
                if( tootReply != null)
                    toot.setIn_reply_to_id(tootReply.getId());
                new PostActionAsyncTask(getApplicationContext(), API.StatusAction.CREATESTATUS, null, toot, null, TootActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

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

        toot_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {

                Pattern sPattern = Pattern.compile(pattern);
                Matcher m = sPattern.matcher(s.toString());
                if(m.matches()) {
                    String search = m.group(2);
                    new RetrieveSearchAccountsAsyncTask(getApplicationContext(),search,TootActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }else{
                    toot_show_accounts.setVisibility(View.GONE);
                }
                if( s.length() + charsInCw > maxChar){
                    String content = s.toString().substring(0,(maxChar - charsInCw));
                    toot_content.setText(content);
                    charsInToot = content.length();
                    toot_content.setSelection(toot_content.getText().length());
                    Toast.makeText(getApplicationContext(),R.string.toot_no_space,Toast.LENGTH_LONG).show();
                }
                int totalChar = toot_cw_content.length() + toot_content.length();
                toot_space_left.setText(String.valueOf((maxChar - totalChar)));
            }
        });

        toot_cw_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if( s.length() + charsInToot > maxChar){
                    String content = s.toString().substring(0,(maxChar - charsInToot));
                    toot_cw_content.setText(content);
                    toot_cw_content.setSelection(toot_cw_content.getText().length());
                    Toast.makeText(getApplicationContext(),R.string.toot_no_space,Toast.LENGTH_LONG).show();
                }
                int totalChar = toot_cw_content.length() + toot_content.length();
                toot_space_left.setText(String.valueOf((maxChar - totalChar)));
            }
        });

    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
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
                            StoredStatus draft = new StatusStoredDAO(TootActivity.this, db).getStatus(id);
                            Status status = draft.getStatus();
                            //Retrieves attachments
                            attachments = status.getMedia_attachments();
                            toot_picture_container.removeAllViews();
                            loading_picture.setVisibility(View.GONE);
                            if( attachments != null && attachments.size() > 0){
                                toot_picture_container.setVisibility(View.VISIBLE);
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
                            //The current id is set to the draft
                            currentToId = draft.getId();
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
                final Button date_time_previous = (Button) dialogView.findViewById(R.id.date_time_previous);
                final Button date_time_next = (Button) dialogView.findViewById(R.id.date_time_next);
                final Button date_time_set = (Button) dialogView.findViewById(R.id.date_time_set);

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
                            ScheduledTootsSyncJob.schedule(getApplicationContext(), true, currentToId, time);
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

                alertDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toot, menu);
        return true;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(search_validate);
    }
    @Override
    public void onRetrieveAttachment(final Attachment attachment, Error error) {
        loading_picture.setVisibility(View.GONE);
        toot_picture_container.setVisibility(View.VISIBLE);
        if( error != null){
            final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            boolean show_error_messages = sharedpreferences.getBoolean(Helper.SET_SHOW_ERROR_MESSAGES, true);
            if( show_error_messages)
                Toast.makeText(getApplicationContext(), error.getError(),Toast.LENGTH_LONG).show();
            return;
        }
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
                        ((ViewGroup) namebar.getParent()).removeView(namebar);
                        break;
                    }
                }
                dialog.dismiss();
                if( attachments.size() == 0 ) {
                    toot_sensitive.setVisibility(View.GONE);
                    isSensitive = false;
                    toot_sensitive.setChecked(false);
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
    public void onPostAction(int statusCode, API.StatusAction statusAction, String userId, Error error) {
        if( error != null){
            final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            boolean show_error_messages = sharedpreferences.getBoolean(Helper.SET_SHOW_ERROR_MESSAGES, true);
            if( show_error_messages)
                Toast.makeText(getApplicationContext(), error.getError(),Toast.LENGTH_LONG).show();
            return;
        }
        if( statusCode == 200){
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
        }else {
            Toast.makeText(TootActivity.this,R.string.toast_error, Toast.LENGTH_LONG).show();
        }
        toot_it.setEnabled(true);
    }


    @Override
    public void onRetrieveSearchAccounts(APIResponse apiResponse) {
        if( apiResponse.getError() != null){
            final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            boolean show_error_messages = sharedpreferences.getBoolean(Helper.SET_SHOW_ERROR_MESSAGES, true);
            if( show_error_messages)
                Toast.makeText(getApplicationContext(), apiResponse.getError().getError(),Toast.LENGTH_LONG).show();
            return;
        }
        List<Account> accounts = apiResponse.getAccounts();
        if( accounts != null && accounts.size() > 0){
            AccountsSearchAdapter accountsListAdapter = new AccountsSearchAdapter(TootActivity.this, accounts);
            toot_lv_accounts.setAdapter(accountsListAdapter);
            accountsListAdapter.notifyDataSetChanged();
            toot_show_accounts.setVisibility(View.VISIBLE);
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
                currentToId = new StatusStoredDAO(TootActivity.this, db).insertStatus(toot);

            }else{
                StoredStatus storedStatus = new StatusStoredDAO(TootActivity.this, db).getStatus(currentToId);
                if( storedStatus != null ){
                    new StatusStoredDAO(TootActivity.this, db).updateStatus(currentToId, toot);
                }else { //Might have been deleted, so it needs insertion
                    new StatusStoredDAO(TootActivity.this, db).insertStatus(toot);
                }
            }
            if( message )
                Toast.makeText(getApplicationContext(), R.string.toast_toot_saved, Toast.LENGTH_LONG).show();
        }catch (Exception e){
            if( message)
                Toast.makeText(getApplicationContext(), R.string.toast_error, Toast.LENGTH_LONG).show();
        }
    }

}
