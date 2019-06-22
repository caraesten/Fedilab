package app.fedilab.android.activities;
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

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.constraintlayout.widget.Group;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import app.fedilab.android.R;
import app.fedilab.android.asynctasks.PostAdminActionAsyncTask;
import app.fedilab.android.client.API;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.AccountAdmin;
import app.fedilab.android.client.Entities.Report;
import app.fedilab.android.client.Entities.Status;
import app.fedilab.android.drawers.StatusReportAdapter;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.interfaces.OnAdminActionInterface;
import es.dmoral.toasty.Toasty;

public class AccountReportActivity extends BaseActivity implements OnAdminActionInterface {

    TextView permissions, username, email, email_status, login_status, joined, recent_ip;
    Button warn, disable, silence;
    private String account_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);

        setTheme(R.style.AppAdminTheme);
        Report report = null;
        AccountAdmin targeted_account = null;
        Bundle b = getIntent().getExtras();
        if (b != null) {
            account_id = b.getString("account_id", null);
            targeted_account = b.getParcelable("targeted_account");
            report =  b.getParcelable("report");
        }

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.simple_bar, null);
            actionBar.setCustomView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            ImageView toolbar_close = actionBar.getCustomView().findViewById(R.id.toolbar_close);
            TextView toolbar_title = actionBar.getCustomView().findViewById(R.id.toolbar_title);
            toolbar_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            toolbar_title.setText(String.format(getString(R.string.administration) + " %s", Helper.getLiveInstance(getApplicationContext())));
        }
        setContentView(R.layout.activity_admin_report);

        warn = findViewById(R.id.warn);
        disable = findViewById(R.id.disable);
        silence = findViewById(R.id.silence);

        permissions = findViewById(R.id.permissions);
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        email_status = findViewById(R.id.email_status);
        login_status = findViewById(R.id.login_status);
        joined = findViewById(R.id.joined);
        recent_ip = findViewById(R.id.recent_ip);

        CheckBox email_user = findViewById(R.id.email_user);
        EditText comment = findViewById(R.id.comment);

        if( account_id == null && report == null && targeted_account == null){
            Toasty.error(getApplicationContext(), getString(R.string.toast_error), Toast.LENGTH_LONG).show();
            finish();
        }

        if( account_id != null){
            new PostAdminActionAsyncTask(getApplicationContext(), API.adminAction.GET_ONE_ACCOUNT, account_id, null, AccountReportActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            return;
        }
        if( report != null) {
            targeted_account = report.getAccount();
            RecyclerView lv_statuses = findViewById(R.id.lv_statuses);

            ArrayList<String> contents = new ArrayList<>();
            for(Status status: report.getStatuses()){
                contents.add(status.getContent());
            }
            lv_statuses.setLayoutManager(new LinearLayoutManager(this));
            StatusReportAdapter adapter = new StatusReportAdapter(this, contents);
            lv_statuses.setAdapter(adapter);

            Group statuses_group = findViewById(R.id.statuses_group);
            statuses_group.setVisibility(View.VISIBLE);
        }
        fillReport(targeted_account);

    }

    @Override
    public void onAdminAction(APIResponse apiResponse) {
        if( apiResponse == null || apiResponse.getError() != null){
            Toasty.error(getApplicationContext(), getString(R.string.toast_error),Toast.LENGTH_LONG).show();
            return;
        }
        if( apiResponse.getAccountAdmins() != null && apiResponse.getAccountAdmins().size() > 0) {
            fillReport(apiResponse.getAccountAdmins().get(0));
        }
    }

    private void fillReport(AccountAdmin accountAdmin){

        if( accountAdmin == null){
            Toasty.error(getApplicationContext(), getString(R.string.toast_error), Toast.LENGTH_LONG).show();
            return;
        }
        switch (accountAdmin.getRole()){
            case "user":
                permissions.setText(getString(R.string.user));
                break;
            case "mod":
                permissions.setText(getString(R.string.moderator));
                break;
            case "admin":
                permissions.setText(getString(R.string.administrator));
                break;
        }

        username.setText(String.format("@%s", accountAdmin.getAccount().getAcct()));

        email.setText(accountAdmin.getEmail());
        email_status.setText(accountAdmin.isConfirmed()?getString(R.string.confirmed):getString(R.string.unconfirmed));

        if( accountAdmin.isDisabled()){
            login_status.setText(getString(R.string.disabled));
        }else if( accountAdmin.isSilenced()){
            login_status.setText(getString(R.string.silenced));
        }else if( accountAdmin.isSuspended()){
            login_status.setText(getString(R.string.suspended));
        }else{
            login_status.setText(getString(R.string.active));
        }

        joined.setText(Helper.dateToString(accountAdmin.getCreated_at()));
        recent_ip.setText(accountAdmin.getIp());
    }
}
