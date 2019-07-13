/* Copyright 2019 Thomas Schneider
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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import app.fedilab.android.R;
import app.fedilab.android.asynctasks.PostActionAsyncTask;
import app.fedilab.android.asynctasks.RetrieveDomainsAsyncTask;
import app.fedilab.android.client.API;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.Error;
import app.fedilab.android.drawers.DomainsListAdapter;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.interfaces.OnPostActionInterface;
import app.fedilab.android.interfaces.OnRetrieveDomainsInterface;
import app.fedilab.android.sqlite.AccountDAO;
import app.fedilab.android.sqlite.Sqlite;
import es.dmoral.toasty.Toasty;


/**
 * Created by Thomas on 13/07/2019.
 * Muted instances activity
 */

public class MutedInstanceActivity extends BaseActivity implements OnRetrieveDomainsInterface, OnPostActionInterface {

    private boolean flag_loading;
    private AsyncTask<Void, Void, Void> asyncTask;
    private DomainsListAdapter domainsListAdapter;
    private String max_id;
    private List<String> domains;
    private RelativeLayout mainLoader, nextElementLoader, textviewNoAction;
    private boolean firstLoad;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean swiped;
    private RecyclerView lv_domains;
    private final int PICK_IMPORT_INSTANCE = 5326;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
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
        if( getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null ) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.simple_bar_muted_instance, null);
            actionBar.setCustomView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            ImageView toolbar_close = actionBar.getCustomView().findViewById(R.id.toolbar_close);
            TextView toolbar_title = actionBar.getCustomView().findViewById(R.id.toolbar_title);
            ImageView option = actionBar.getCustomView().findViewById(R.id.option);
            toolbar_close.setOnClickListener(v -> finish());
            option.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(MutedInstanceActivity.this, option);
                popup.getMenuInflater()
                        .inflate(R.menu.option_muted_instance, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_add_instances:
                                final SharedPreferences sharedpreferences1 = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                                int theme1 = sharedpreferences1.getInt(Helper.SET_THEME, Helper.THEME_DARK);
                                int style;
                                if (theme1 == Helper.THEME_DARK) {
                                    style = R.style.DialogDark;
                                } else if (theme1 == Helper.THEME_BLACK){
                                    style = R.style.DialogBlack;
                                }else {
                                    style = R.style.Dialog;
                                }
                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MutedInstanceActivity.this, style);
                                LayoutInflater inflater = getLayoutInflater();
                                @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.add_blocked_instance, null);
                                dialogBuilder.setView(dialogView);

                                EditText add_domain = dialogView.findViewById(R.id.add_domain);
                                dialogBuilder.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        if(add_domain.getText() != null && add_domain.getText().toString().trim().matches("^[\\da-zA-Z.-]+\\.[a-zA-Z.]{2,10}$")){
                                            new PostActionAsyncTask(MutedInstanceActivity.this, API.StatusAction.BLOCK_DOMAIN, add_domain.getText().toString().trim(), MutedInstanceActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                            dialog.dismiss();
                                        }else{
                                            Toasty.error(MutedInstanceActivity.this, getString(R.string.toast_empty_content)).show();
                                        }
                                    }
                                });
                                dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });
                                AlertDialog alertDialog = dialogBuilder.create();
                                alertDialog.setTitle(getString(R.string.block_domain));
                                if( alertDialog.getWindow() != null )
                                    alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                                alertDialog.show();
                                break;
                            case R.id.action_export_instances:
                                SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                                String instance = sharedpreferences.getString(Helper.PREF_INSTANCE, Helper.getLiveInstance(getApplicationContext()));
                                String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
                                Account account = new AccountDAO(getApplicationContext(), db).getUniqAccount(userId, instance);
                                Helper.exportInstanceBlock(MutedInstanceActivity.this,account.getAcct()+"_"+account.getInstance());
                                break;
                            case R.id.action_import_instances:
                                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    if (ContextCompat.checkSelfPermission(MutedInstanceActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                                            PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(MutedInstanceActivity.this,
                                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                                TootActivity.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                                        return true;
                                    }
                                }
                                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                intent.addCategory(Intent.CATEGORY_OPENABLE);
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                                    intent.setType("*/*");
                                    String[] mimetypes = {"*/*"};
                                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                                    startActivityForResult(intent, PICK_IMPORT_INSTANCE);
                                }else {
                                    intent.setType("*/*");
                                    Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    Intent chooserIntent = Intent.createChooser(intent, getString(R.string.toot_select_import));
                                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});
                                    startActivityForResult(chooserIntent, PICK_IMPORT_INSTANCE);
                                }
                                break;
                        }
                        return true;
                    }});
                popup.show();
            });
            toolbar_title.setText(R.string.blocked_domains);
            if (theme == Helper.THEME_LIGHT){
                Toolbar toolbar = actionBar.getCustomView().findViewById(R.id.toolbar);
                Helper.colorizeToolbar(toolbar, R.color.black, MutedInstanceActivity.this);
            }
        }
        setContentView(R.layout.activity_muted_instances);

        domains = new ArrayList<>();
        max_id = null;
        firstLoad = true;
        flag_loading = true;
        swiped = false;

        swipeRefreshLayout = findViewById(R.id.swipeContainer);
        lv_domains = findViewById(R.id.lv_domains);
        lv_domains.addItemDecoration(new DividerItemDecoration(MutedInstanceActivity.this, DividerItemDecoration.VERTICAL));
        mainLoader = findViewById(R.id.loader);
        nextElementLoader = findViewById(R.id.loading_next_domains);
        textviewNoAction = findViewById(R.id.no_action);
        mainLoader.setVisibility(View.VISIBLE);
        nextElementLoader.setVisibility(View.GONE);
        domainsListAdapter = new DomainsListAdapter(MutedInstanceActivity.this, this.domains, textviewNoAction);
        lv_domains.setAdapter(domainsListAdapter);

        final LinearLayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(MutedInstanceActivity.this);
        lv_domains.setLayoutManager(mLayoutManager);
        lv_domains.addOnScrollListener(new RecyclerView.OnScrollListener() {
            public void onScrolled(@NotNull RecyclerView recyclerView, int dx, int dy)
            {
                if(dy > 0) {
                    int visibleItemCount = mLayoutManager.getChildCount();
                    int totalItemCount = mLayoutManager.getItemCount();
                    int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                    if (firstVisibleItem + visibleItemCount == totalItemCount) {
                        if (!flag_loading) {
                            flag_loading = true;
                            asyncTask = new RetrieveDomainsAsyncTask(MutedInstanceActivity.this, max_id, MutedInstanceActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);                            nextElementLoader.setVisibility(View.VISIBLE);
                        }
                    } else {
                        nextElementLoader.setVisibility(View.GONE);
                    }
                }
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                max_id = null;
                domains = new ArrayList<>();
                firstLoad = true;
                flag_loading = true;
                swiped = true;
                asyncTask = new RetrieveDomainsAsyncTask(MutedInstanceActivity.this, max_id, MutedInstanceActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);            }
        });
        switch (theme){
            case Helper.THEME_LIGHT:
                swipeRefreshLayout.setColorSchemeResources(R.color.mastodonC4,
                        R.color.mastodonC2,
                        R.color.mastodonC3);
                swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(MutedInstanceActivity.this, R.color.white));
                break;
            case Helper.THEME_DARK:
                swipeRefreshLayout.setColorSchemeResources(R.color.mastodonC4__,
                        R.color.mastodonC4,
                        R.color.mastodonC4);
                swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(MutedInstanceActivity.this, R.color.mastodonC1_));
                break;
            case Helper.THEME_BLACK:
                swipeRefreshLayout.setColorSchemeResources(R.color.dark_icon,
                        R.color.mastodonC2,
                        R.color.mastodonC3);
                swipeRefreshLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(MutedInstanceActivity.this, R.color.black_3));
                break;
        }

        asyncTask = new RetrieveDomainsAsyncTask(MutedInstanceActivity.this, max_id, MutedInstanceActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        setTitle(R.string.blocked_domains);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(asyncTask != null && asyncTask.getStatus() == AsyncTask.Status.RUNNING)
            asyncTask.cancel(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRetrieveDomains(APIResponse apiResponse) {
        mainLoader.setVisibility(View.GONE);
        nextElementLoader.setVisibility(View.GONE);
        if( apiResponse.getError() != null){
            Toasty.error(MutedInstanceActivity.this, apiResponse.getError().getError(),Toast.LENGTH_LONG).show();
            swipeRefreshLayout.setRefreshing(false);
            swiped = false;
            flag_loading = false;
            return;
        }
        flag_loading = (apiResponse.getMax_id() == null );
        List<String> domains = apiResponse.getDomains();
        if( !swiped && firstLoad && (domains == null || domains.size() == 0))
            textviewNoAction.setVisibility(View.VISIBLE);
        else
            textviewNoAction.setVisibility(View.GONE);
        max_id = apiResponse.getMax_id();
        if( swiped ){
            domainsListAdapter = new DomainsListAdapter(MutedInstanceActivity.this,  this.domains, textviewNoAction);
            lv_domains.setAdapter(domainsListAdapter);
            swiped = false;
        }
        if( domains != null && domains.size() > 0) {
            this.domains.addAll(domains);
            domainsListAdapter.notifyDataSetChanged();
        }
        swipeRefreshLayout.setRefreshing(false);
        firstLoad = false;
    }

    @Override
    public void onRetrieveDomainsDeleted(int response) {

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMPORT_INSTANCE && resultCode == RESULT_OK) {
            if (data == null || data.getData() == null) {
                Toasty.error(getApplicationContext(),getString(R.string.toot_select_file_error),Toast.LENGTH_LONG).show();
                return;
            }
            try {
                InputStream is =  getContentResolver().openInputStream(data.getData());
                HashMap<String, String> resultList = new HashMap<>();
                BufferedReader reader = new BufferedReader(new InputStreamReader( is));
                String csvLine;
                while ((csvLine = reader.readLine()) != null) {
                    String[] row = csvLine.split(",");
                    if( row.length > 1) {
                        if( !row[0].equals("INSTANCE"))
                            resultList.put(row[0], row[1]);
                    }else if(row.length == 1){
                        if( !row[0].equals("INSTANCE"))
                            resultList.put(row[0], "");
                    }
                }
                Helper.importInstanceBlock(MutedInstanceActivity.this,resultList);
            } catch (Exception e) {
                Toasty.error(MutedInstanceActivity.this, getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
            }


        }
    }

    @Override
    public void onPostAction(int statusCode, API.StatusAction statusAction, String userId, Error error) {
        if( error != null){
            Toasty.error(MutedInstanceActivity.this, error.getError(),Toast.LENGTH_LONG).show();
            return;
        }
        Helper.manageMessageStatusCode(MutedInstanceActivity.this, statusCode, statusAction);
        this.domains.add(0,userId);
        domainsListAdapter.notifyItemInserted(0);
        if( this.domains.size() > 0){
            textviewNoAction.setVisibility(View.GONE);
        }
    }

}
