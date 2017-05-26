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
package fr.gouv.etalab.mastodon.activities;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.gouv.etalab.mastodon.asynctasks.PostActionAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveFeedsAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveSearchAccountsAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.UploadActionAsyncTask;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Attachment;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.drawers.AccountsSearchAdapter;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnPostActionInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveAttachmentInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveFeedsInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveSearcAccountshInterface;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import mastodon.etalab.gouv.fr.mastodon.R;


/**
 * Created by Thomas on 01/05/2017.
 * Toot activity class
 */

public class TootActivity extends AppCompatActivity implements OnRetrieveSearcAccountshInterface, OnRetrieveAttachmentInterface, OnPostActionInterface, OnRetrieveFeedsInterface {


    private String inReplyTo = null;
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
    private List<Attachment> attachments;
    private ImageButton toot_visibility;
    private Button toot_it;
    private EditText toot_content, toot_cw_content;
    private LinearLayout toot_reply_content_container;
    private TextView toot_reply_content;
    private RelativeLayout toot_show_accounts;
    private ListView toot_lv_accounts;
    private BroadcastReceiver search_validate;

    private String pattern = "^.*(@([a-zA-Z0-9_]{2,}))$";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toot);

        if( getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        Bundle b = getIntent().getExtras();
        if(b != null)
            inReplyTo = b.getString("inReplyTo", null);
        if( inReplyTo != null) {
            setTitle(R.string.toot_title_reply);
            new RetrieveFeedsAsyncTask(getApplicationContext(), RetrieveFeedsAsyncTask.Type.ONESTATUS, inReplyTo,null, TootActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }else {
            setTitle(R.string.toot_title);
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

        toot_it = (Button) findViewById(R.id.toot_it);
        Button toot_cw = (Button) findViewById(R.id.toot_cw);
        final TextView toot_space_left = (TextView) findViewById(R.id.toot_space_left);
        toot_visibility = (ImageButton) findViewById(R.id.toot_visibility);
        toot_picture = (ImageButton) findViewById(R.id.toot_picture);
        loading_picture = (RelativeLayout) findViewById(R.id.loading_picture);
        toot_picture_container = (LinearLayout) findViewById(R.id.toot_picture_container);
        toot_content = (EditText) findViewById(R.id.toot_content);
        toot_cw_content = (EditText) findViewById(R.id.toot_cw_content);
        toot_reply_content = (TextView) findViewById(R.id.toot_reply_content);
        toot_reply_content_container = (LinearLayout) findViewById(R.id.toot_reply_content_container);
        toot_show_accounts = (RelativeLayout) findViewById(R.id.toot_show_accounts);
        toot_lv_accounts = (ListView) findViewById(R.id.toot_lv_accounts);
        FloatingActionButton toot_close_accounts = (FloatingActionButton) findViewById(R.id.toot_close_accounts);
        SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
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
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder().displayer(new SimpleBitmapDisplayer()).cacheInMemory(false)
                .cacheOnDisk(true).resetViewBeforeLoading(true).build();

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
                toot.setMedia_attachments(attachments);
                if( toot_cw_content.getText().toString().trim().length() > 0)
                    toot.setSpoiler_text(toot_cw_content.getText().toString().trim());
                toot.setVisibility(visibility);
                toot.setContent(toot_content.getText().toString().trim());
                if( inReplyTo != null)
                    toot.setIn_reply_to_id(inReplyTo);
                new PostActionAsyncTask(getApplicationContext(), API.StatusAction.CREATESTATUS, null, toot, null, TootActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            }
        });

        toot_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, getString(R.string.toot_select_image));
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});
                startActivityForResult(chooserIntent, PICK_IMAGE);
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
    public void onDestroy(){
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(search_validate);
    }
    @Override
    public void onRetrieveAttachment(final Attachment attachment) {
        loading_picture.setVisibility(View.GONE);
        toot_picture_container.setVisibility(View.VISIBLE);
        if( attachment != null ){
            String url = attachment.getPreview_url();
            if( url == null || url.trim().equals(""))
                url = attachment.getUrl();

            final ImageView imageView = new ImageView(getApplicationContext());
            imageView.setId(Integer.parseInt(attachment.getId()));
            LinearLayout.LayoutParams imParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            imParams.setMargins(20, 5, 20, 5);
            imageLoader.displayImage(url, imageView, options);
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            toot_picture_container.addView(imageView, imParams);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showRemove(imageView.getId());
                }
            });
            attachments.add(attachment);
            if( attachments.size() < 4)
                toot_picture.setEnabled(true);
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
                List<Attachment> tmp_attachment = new ArrayList<>();
                tmp_attachment.addAll(attachments);
                attachments.removeAll(tmp_attachment);
                tmp_attachment.clear();
                View namebar = findViewById(viewId);
                ((ViewGroup) namebar.getParent()).removeView(namebar);
                dialog.dismiss();
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
    public void onPostAction(int statusCode, API.StatusAction statusAction, String userId) {
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
            Toast.makeText(TootActivity.this,R.string.toot_sent, Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(TootActivity.this,R.string.toast_error, Toast.LENGTH_LONG).show();
        }
        toot_it.setEnabled(true);
    }

    @Override
    public void onRetrieveFeeds(List<Status> statuses) {
        if( statuses != null && statuses.size() > 0 ){
            SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            boolean show_reply = sharedpreferences.getBoolean(Helper.SET_SHOW_REPLY, false);
            if( show_reply ){
                toot_reply_content_container.setVisibility(View.VISIBLE);
            }else {
                toot_reply_content_container.setVisibility(View.GONE);
            }
            String content = statuses.get(0).getContent();
            if(statuses.get(0).isReblogged())
                content = statuses.get(0).getReblog().getContent();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                toot_reply_content.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT));
            else
                //noinspection deprecation
                toot_reply_content.setText(Html.fromHtml(content));
            switch (statuses.get(0).getVisibility()){
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
        }
    }



    @Override
    public void onRetrieveSearchAccounts(List<Account> accounts) {
        if( accounts != null && accounts.size() > 0){
            AccountsSearchAdapter accountsListAdapter = new AccountsSearchAdapter(TootActivity.this, accounts);
            toot_lv_accounts.setAdapter(accountsListAdapter);
            accountsListAdapter.notifyDataSetChanged();
            toot_show_accounts.setVisibility(View.VISIBLE);
        }
    }
}
