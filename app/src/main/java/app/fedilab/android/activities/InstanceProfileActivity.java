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


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import app.fedilab.android.R;
import app.fedilab.android.asynctasks.RetrieveAccountsAsyncTask;
import app.fedilab.android.client.API;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.InstanceNodeInfo;
import app.fedilab.android.drawers.AccountsListAdapter;
import app.fedilab.android.helper.Helper;

import static androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY;


/**
 * Created by Thomas on 15/11/2019.
 * Instance info activity class
 */

public class InstanceProfileActivity extends BaseActivity {


    private TextView name, description, userCount, statusCount, instanceCount, software, version;
    private String instance;
    private RecyclerView lv_accounts;
    private LinearLayout instance_container;
    private ImageView back_ground_image;
    private RelativeLayout loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if (theme == Helper.THEME_LIGHT) {
            setTheme(R.style.Dialog);
        } else {
            setTheme(R.style.DialogDark);
        }
        setContentView(R.layout.activity_instance_profile);
        getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Bundle b = getIntent().getExtras();
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        if (b != null)
            instance = b.getString("instance",null);
        if( instance == null){
            finish();
        }
        Button close = findViewById(R.id.close);
        name = findViewById(R.id.name);
        description = findViewById(R.id.description);
        userCount = findViewById(R.id.user_count);
        statusCount = findViewById(R.id.status_count);
        instanceCount = findViewById(R.id.instance_count);
        instance_container = findViewById(R.id.instance_container);
        loader = findViewById(R.id.loader);
        back_ground_image = findViewById(R.id.back_ground_image);
        software = findViewById(R.id.software);
        version = findViewById(R.id.version);
        lv_accounts = findViewById(R.id.lv_accounts);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        checkInstance();
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


    private void checkInstance() {

        if (instance == null)
            return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InstanceNodeInfo instanceNodeInfo = new API(InstanceProfileActivity.this).instanceInfo(instance.trim());
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if( instanceNodeInfo == null){
                                finish();
                                return;
                            }
                            if (instanceNodeInfo.getThumbnail() != null && !instanceNodeInfo.getThumbnail().equals("null"))
                                Glide.with(getApplicationContext())
                                        .asBitmap()
                                        .load(instanceNodeInfo.getThumbnail())
                                        .into(back_ground_image);
                            name.setText(instanceNodeInfo.getNodeName());


                            SpannableString descriptionSpan;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                descriptionSpan = new SpannableString(Html.fromHtml(instanceNodeInfo.getNodeDescription(), FROM_HTML_MODE_LEGACY));
                            else
                                descriptionSpan = new SpannableString(Html.fromHtml(instanceNodeInfo.getNodeDescription()));
                            description.setText(descriptionSpan, TextView.BufferType.SPANNABLE);
                            userCount.setText(Helper.withSuffix((instanceNodeInfo.getNumberOfUsers())));
                            statusCount.setText(Helper.withSuffix((instanceNodeInfo.getNumberOfPosts())));
                            instanceCount.setText(Helper.withSuffix((instanceNodeInfo.getNumberOfInstance())));
                            software.setText(instanceNodeInfo.getName() + " - ");
                            version.setText(instanceNodeInfo.getVersion());
                            if( instanceNodeInfo.getStaffAccount() != null){
                                List<Account> accounts = new ArrayList<>();
                                accounts.add(instanceNodeInfo.getStaffAccount());
                                final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                                String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
                                AccountsListAdapter accountsListAdapter = new AccountsListAdapter(RetrieveAccountsAsyncTask.Type.FOLLOWERS, userId, accounts);
                                lv_accounts.setAdapter(accountsListAdapter);
                                final LinearLayoutManager mLayoutManager;
                                mLayoutManager = new LinearLayoutManager(InstanceProfileActivity.this);
                                lv_accounts.setLayoutManager(mLayoutManager);
                            }
                            instance_container.setVisibility(View.VISIBLE);
                            loader.setVisibility(View.GONE);
                        }
                    });

                } catch (Exception ignored) {
                }
            }
        }).start();
    }


}
