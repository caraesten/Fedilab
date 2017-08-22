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



import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
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
import fr.gouv.etalab.mastodon.asynctasks.RetrieveRemoteAccountsAsyncTask;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.KinrarClient;
import fr.gouv.etalab.mastodon.client.PatchBaseImageDownloader;
import fr.gouv.etalab.mastodon.drawers.AccountSearchWebAdapter;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveRemoteAccountInterface;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import mastodon.etalab.gouv.fr.mastodon.R;


/**
 * Created by Thomas on 22/08/2017.
 * Remote follow activity class
 */

public class RemoteFollowActivity extends AppCompatActivity implements OnRetrieveRemoteAccountInterface {


    private ImageView pp_actionBar;
    private AutoCompleteTextView rf_instance;
    private EditText rf_username;
    private TextView rf_no_result;
    private Button rf_search;
    private ListView lv_account;
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

        rf_instance = (AutoCompleteTextView) findViewById(R.id.rf_instance);
        rf_username = (EditText) findViewById(R.id.rf_username);
        rf_search = (Button) findViewById(R.id.rf_search);
        loader = (RelativeLayout) findViewById(R.id.loader);
        lv_account = (ListView) findViewById(R.id.lv_account);
        rf_no_result = (TextView) findViewById(R.id.rf_no_result);

        isLoadingInstance = false;
        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.conversation_action_bar, null);
            actionBar.setCustomView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            TextView title = (TextView) actionBar.getCustomView().findViewById(R.id.toolbar_title);
            pp_actionBar = (ImageView) actionBar.getCustomView().findViewById(R.id.pp_actionBar);
            title.setText(R.string.remote_follow_menu);
            ImageView close_conversation = (ImageView) actionBar.getCustomView().findViewById(R.id.close_conversation);
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
                if( s.length() > 2 && !isLoadingInstance && 1 ==2){
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
                new RetrieveRemoteAccountsAsyncTask(screen_name, instance_name, RemoteFollowActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
    public void onRetrieveRemoteAccount(boolean error, String name, String username, boolean locked, String avatar, String bio, int statusCount, int followingCount, int followersCount) {
        loader.setVisibility(View.GONE);
        if( error){
            rf_no_result.setVisibility(View.VISIBLE);
            Toast.makeText(getApplicationContext(), R.string.toast_error,Toast.LENGTH_LONG).show();
            return;
        }
        Account account = new Account();
        account.setInstance(instance_name);
        account.setAcct(screen_name + "@" + instance_name);
        account.setAvatar(avatar);
        account.setDisplay_name(username);
        account.setStatuses_count(statusCount);
        account.setFollowers_count(followersCount);
        account.setFollowing_count(followingCount);
        account.setUsername(name);
        account.setLocked(locked);
        account.setNote(bio);
        List<Account> selectedAccount = new ArrayList<>();
        selectedAccount.add(account);
        AccountSearchWebAdapter accountSearchWebAdapter = new AccountSearchWebAdapter(RemoteFollowActivity.this, selectedAccount);
        lv_account.setAdapter(accountSearchWebAdapter);
        lv_account.setVisibility(View.VISIBLE);

    }
}
