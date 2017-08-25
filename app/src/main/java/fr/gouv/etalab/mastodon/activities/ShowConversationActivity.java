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
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import fr.gouv.etalab.mastodon.asynctasks.RetrieveContextAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveFeedsAsyncTask;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Context;
import fr.gouv.etalab.mastodon.client.Entities.Error;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.client.PatchBaseImageDownloader;
import fr.gouv.etalab.mastodon.drawers.StatusListAdapter;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveContextInterface;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveFeedsInterface;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import mastodon.etalab.gouv.fr.mastodon.R;


/**
 * Created by Thomas on 04/05/2017.
 * Show conversation activity class
 */

public class ShowConversationActivity extends AppCompatActivity implements OnRetrieveFeedsInterface, OnRetrieveContextInterface {


    private String statusId;
    private Status initialStatus;
    public static int position;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView lv_status;
    private boolean isRefreshed;
    private TextView title;
    private ImageView pp_actionBar;

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
        setContentView(R.layout.activity_show_conversation);

        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.conversation_action_bar, null);
            actionBar.setCustomView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            title = (TextView) actionBar.getCustomView().findViewById(R.id.toolbar_title);
            pp_actionBar = (ImageView) actionBar.getCustomView().findViewById(R.id.pp_actionBar);
            title.setText(R.string.conversation);
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
            setTitle(R.string.conversation);
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
            public void onLoadingFailed(java.lang.String imageUri, android.view.View view, FailReason failReason){

            }});

        Bundle b = getIntent().getExtras();
        if(b != null)
            statusId = b.getString("statusId", null);
        if( statusId == null)
            finish();
        isRefreshed = false;

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        new RetrieveFeedsAsyncTask(getApplicationContext(), RetrieveFeedsAsyncTask.Type.ONESTATUS, statusId,null, false, ShowConversationActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        if( theme == Helper.THEME_LIGHT) {
            swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent,
                    R.color.colorPrimary,
                    R.color.colorPrimaryDark);
        }else {
            swipeRefreshLayout.setColorSchemeResources(R.color.colorAccentD,
                    R.color.colorPrimaryD,
                    R.color.colorPrimaryDarkD);
        }
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isRefreshed = true;
                new RetrieveFeedsAsyncTask(getApplicationContext(), RetrieveFeedsAsyncTask.Type.ONESTATUS, statusId,null, false, ShowConversationActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
        lv_status = (ListView) findViewById(R.id.lv_status);
        lv_status.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == android.view.MotionEvent.ACTION_UP) {

                    if (lv_status.getLastVisiblePosition() == lv_status.getAdapter().getCount() -1 &&  lv_status.getFirstVisiblePosition() > 0 &&
                            lv_status.getChildAt(lv_status.getChildCount() - 1).getBottom() <= lv_status.getHeight()) {

                        swipeRefreshLayout.setRefreshing(true);
                        ( new Handler()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                isRefreshed = true;
                                new RetrieveFeedsAsyncTask(getApplicationContext(), RetrieveFeedsAsyncTask.Type.ONESTATUS, statusId,null, false, ShowConversationActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            }
                        }, 1000);

                    }
                }
                return false;
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
    public void onRetrieveFeeds(APIResponse apiResponse, boolean refreshData) {
        if( apiResponse.getError() != null){
            final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
            boolean show_error_messages = sharedpreferences.getBoolean(Helper.SET_SHOW_ERROR_MESSAGES, true);
            if( show_error_messages)
                Toast.makeText(getApplicationContext(), apiResponse.getError().getError(),Toast.LENGTH_LONG).show();
            return;
        }
        List<Status> statuses = apiResponse.getStatuses();
        if( statuses != null && statuses.size() > 0 ){
            initialStatus = statuses.get(0);
            new RetrieveContextAsyncTask(getApplicationContext(), initialStatus.getId(), ShowConversationActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void onRetrieveFeeds(Context context, Error error) {
        swipeRefreshLayout.setRefreshing(false);
        if( error != null){
            final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
            boolean show_error_messages = sharedpreferences.getBoolean(Helper.SET_SHOW_ERROR_MESSAGES, true);
            if( show_error_messages)
                Toast.makeText(getApplicationContext(), error.getError(),Toast.LENGTH_LONG).show();
            return;
        }
        boolean isOnWifi = Helper.isOnWIFI(getApplicationContext());
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        int behaviorWithAttachments = sharedpreferences.getInt(Helper.SET_ATTACHMENT_ACTION, Helper.ATTACHMENT_ALWAYS);
        int positionSpinnerTrans = sharedpreferences.getInt(Helper.SET_TRANSLATOR, Helper.TRANS_YANDEX);
        position = 0;
        List<Status> statuses = new ArrayList<>();
        if( context.getAncestors() != null && context.getAncestors().size() > 0){
            for(Status status: context.getAncestors()){
                statuses.add(status);
                position++;
            }
        }
        statuses.add(initialStatus);
        if( context.getDescendants() != null && context.getDescendants().size() > 0){
            for(Status status: context.getDescendants()){
                statuses.add(status);
            }
        }
        RelativeLayout loader = (RelativeLayout) findViewById(R.id.loader);
        StatusListAdapter statusListAdapter = new StatusListAdapter(ShowConversationActivity.this, RetrieveFeedsAsyncTask.Type.CONTEXT, null, isOnWifi, behaviorWithAttachments, positionSpinnerTrans, statuses);
        lv_status.setAdapter(statusListAdapter);
        statusListAdapter.notifyDataSetChanged();
        loader.setVisibility(View.GONE);
        lv_status.setVisibility(View.VISIBLE);
        if( isRefreshed){
            position = statuses.size()-1;
            lv_status.setSelection(position);
        }else {
            lv_status.smoothScrollToPosition(position);
        }

    }

}
