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



import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import fr.gouv.etalab.mastodon.asynctasks.PostActionAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveRelationshipAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveRemoteAccountsAsyncTask;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Error;
import fr.gouv.etalab.mastodon.client.Entities.Relationship;
import fr.gouv.etalab.mastodon.client.Entities.Results;
import fr.gouv.etalab.mastodon.client.KinrarClient;
import fr.gouv.etalab.mastodon.client.PatchBaseImageDownloader;
import fr.gouv.etalab.mastodon.drawers.AccountSearchDevAdapter;
import fr.gouv.etalab.mastodon.helper.ExpandableHeightListView;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnPostActionInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveRelationshipInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveRemoteAccountInterface;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import mastodon.etalab.gouv.fr.mastodon.R;


/**
 * Created by Thomas on 22/08/2017.
 * Remote follow activity class
 */

public class RemoteFollowActivity extends AppCompatActivity implements OnRetrieveRemoteAccountInterface, OnRetrieveRelationshipInterface, OnPostActionInterface {


    private ImageView pp_actionBar;
    private AutoCompleteTextView rf_instance;
    private EditText rf_username;
    private TextView rf_no_result;
    private Button rf_search;
    private ExpandableHeightListView lv_account;
    private RelativeLayout loader;
    private boolean isLoadingInstance;
    private String instance_name, screen_name;
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
        setContentView(R.layout.activity_remote_follow);

        rf_instance = findViewById(R.id.rf_instance);
        rf_username = findViewById(R.id.rf_username);
        rf_search = findViewById(R.id.rf_search);
        loader = findViewById(R.id.loader);
        lv_account = findViewById(R.id.lv_account);
        rf_no_result = findViewById(R.id.rf_no_result);
        if( theme == Helper.THEME_LIGHT) {
            rf_search.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        }
        isLoadingInstance = false;
        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.conversation_action_bar, null);
            actionBar.setCustomView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            TextView title = actionBar.getCustomView().findViewById(R.id.toolbar_title);
            pp_actionBar = actionBar.getCustomView().findViewById(R.id.pp_actionBar);
            title.setText(R.string.remote_follow_menu);
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
            setTitle(R.string.remote_follow_menu);
        }
        SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        Account account = new AccountDAO(getApplicationContext(),db).getAccountByID(userId);
        String url = account.getAvatar();
        if( url.startsWith("/") ){
            url = "https://" + Helper.getLiveInstance(getApplicationContext()) + account.getAvatar();
        }
        ImageLoader imageLoader = ImageLoader.getInstance();
        File cacheDir = new File(getCacheDir(), getString(R.string.app_name));
        ImageLoaderConfiguration configImg = new ImageLoaderConfiguration.Builder(this)
                .imageDownloader(new PatchBaseImageDownloader(getApplicationContext()))
                .threadPoolSize(5)
                .threadPriority(Thread.MIN_PRIORITY + 3)
                .denyCacheImageMultipleSizesInMemory()
                .diskCache(new UnlimitedDiskCache(cacheDir))
                .build();
        imageLoader.init(configImg);
        DisplayImageOptions options = new DisplayImageOptions.Builder().displayer(new SimpleBitmapDisplayer()).cacheInMemory(false)
                .cacheOnDisk(true).resetViewBeforeLoading(true).build();
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
            public void onLoadingFailed(String imageUri, View view, FailReason failReason){

            }});


        rf_instance.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            @Override
            public void afterTextChanged(Editable s) {
                if( s.length() > 2 && !isLoadingInstance){
                    String action = "/instances/search";
                    RequestParams parameters = new RequestParams();
                    parameters.add("q", s.toString().trim());
                    parameters.add("count", String.valueOf(10));
                    parameters.add("name", String.valueOf(true));
                    isLoadingInstance = true;
                    new KinrarClient().get(action, parameters, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            isLoadingInstance = false;
                            String response = new String(responseBody);
                            String[] instances;
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                JSONArray jsonArray = jsonObject.getJSONArray("instances");
                                if( jsonArray != null){
                                    instances = new String[jsonArray.length()];
                                    for(int i = 0 ; i < jsonArray.length() ; i++){
                                        instances[i] = jsonArray.getJSONObject(i).get("name").toString();
                                    }
                                }else {
                                    instances = new String[]{};
                                }
                                rf_instance.setAdapter(null);
                                ArrayAdapter<String> adapter =
                                        new ArrayAdapter<>(RemoteFollowActivity.this, android.R.layout.simple_list_item_1, instances);
                                rf_instance.setAdapter(adapter);
                                if( rf_instance.hasFocus() && !RemoteFollowActivity.this.isFinishing())
                                    rf_instance.showDropDown();

                            } catch (JSONException ignored) {isLoadingInstance = false;}
                        }
                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            isLoadingInstance = false;
                        }
                    });
                }
            }
        });

        rf_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if( rf_instance.getText().toString().trim().equals("") || rf_username.getText().toString().trim().equals("")){
                    Toast.makeText(getApplicationContext(),R.string.toast_empty_search,Toast.LENGTH_LONG).show();
                    return;
                }
                rf_search.setEnabled(false);
                screen_name = rf_username.getText().toString().trim();
                instance_name = rf_instance.getText().toString().trim();
                lv_account.setVisibility(View.GONE);
                loader.setVisibility(View.VISIBLE);
                rf_no_result.setVisibility(View.GONE);
                if( screen_name.startsWith("@"))
                    screen_name = screen_name.substring(1);
                new RetrieveRemoteAccountsAsyncTask(getApplicationContext(), screen_name, instance_name, RemoteFollowActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                assert imm != null;
                imm.hideSoftInputFromWindow(rf_search.getWindowToken(), 0);
            }
        });
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
    public void onRetrieveRemoteAccount(Results results) {
        loader.setVisibility(View.GONE);
        rf_search.setEnabled(true);
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        if( results == null){
            boolean show_error_messages = sharedpreferences.getBoolean(Helper.SET_SHOW_ERROR_MESSAGES, true);
            if( show_error_messages)
                Toast.makeText(getApplicationContext(), R.string.toast_error,Toast.LENGTH_LONG).show();
            return;
        }
        List<Account> accounts = results.getAccounts();
        Account account;
        List<Account> selectedAccount = new ArrayList<>();
        if( accounts != null && accounts.size() > 0){
            account = accounts.get(0);
            selectedAccount.add(account);
            AccountSearchDevAdapter accountSearchWebAdapter = new AccountSearchDevAdapter(RemoteFollowActivity.this, selectedAccount);
            lv_account.setAdapter(accountSearchWebAdapter);
            lv_account.setVisibility(View.VISIBLE);
            new RetrieveRelationshipAsyncTask(getApplicationContext(), account.getId(),RemoteFollowActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void onRetrieveRelationship(Relationship relationship, Error error) {
        final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        if( error != null){
            boolean show_error_messages = sharedpreferences.getBoolean(Helper.SET_SHOW_ERROR_MESSAGES, true);
            if( show_error_messages)
                Toast.makeText(getApplicationContext(), error.getError(),Toast.LENGTH_LONG).show();
            return;
        }
        if( relationship == null)
            return;
        final FloatingActionButton account_follow = findViewById(R.id.account_follow);
        ShowAccountActivity.action doAction = null;
        String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        final String accountId = relationship.getId();
        if( accountId != null && accountId.equals(userId)){
            account_follow.setVisibility(View.GONE);
        }else if( relationship.isBlocking()){
            account_follow.setImageResource(R.drawable.ic_unlock_alt);
            doAction = ShowAccountActivity.action.UNBLOCK;
            account_follow.setVisibility(View.VISIBLE);
        }else if( relationship.isRequested()){
            account_follow.setVisibility(View.GONE);
            doAction = ShowAccountActivity.action.NOTHING;
        }else if( relationship.isFollowing()){
            account_follow.setImageResource(R.drawable.ic_user_times);
            doAction = ShowAccountActivity.action.UNFOLLOW;
            account_follow.setVisibility(View.VISIBLE);
        }else if( !relationship.isFollowing()){
            account_follow.setImageResource(R.drawable.ic_user_plus);
            doAction = ShowAccountActivity.action.FOLLOW;
            account_follow.setVisibility(View.VISIBLE);
        }else{
            account_follow.setVisibility(View.GONE);
            doAction = ShowAccountActivity.action.NOTHING;
        }
        final ShowAccountActivity.action finalDoAction = doAction;
        account_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( finalDoAction == ShowAccountActivity.action.NOTHING){
                    Toast.makeText(getApplicationContext(), R.string.nothing_to_do, Toast.LENGTH_LONG).show();
                }else if( finalDoAction == ShowAccountActivity.action.FOLLOW){
                    account_follow.setEnabled(false);
                    new PostActionAsyncTask(getApplicationContext(), API.StatusAction.FOLLOW, accountId, RemoteFollowActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }else if( finalDoAction == ShowAccountActivity.action.UNFOLLOW){
                    account_follow.setEnabled(false);
                    new PostActionAsyncTask(getApplicationContext(), API.StatusAction.UNFOLLOW, accountId, RemoteFollowActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }else if( finalDoAction == ShowAccountActivity.action.UNBLOCK){
                    account_follow.setEnabled(false);
                    new PostActionAsyncTask(getApplicationContext(), API.StatusAction.UNBLOCK, accountId, RemoteFollowActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        });
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
        Helper.manageMessageStatusCode(getApplicationContext(), statusCode, statusAction);
        new RetrieveRelationshipAsyncTask(getApplicationContext(), userId,RemoteFollowActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
