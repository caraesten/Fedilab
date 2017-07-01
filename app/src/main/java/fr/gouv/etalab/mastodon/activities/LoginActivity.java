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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import cz.msebera.android.httpclient.Header;
import fr.gouv.etalab.mastodon.asynctasks.UpdateAccountInfoAsyncTask;
import fr.gouv.etalab.mastodon.client.MastalabSSLSocketFactory;
import fr.gouv.etalab.mastodon.client.OauthClient;
import fr.gouv.etalab.mastodon.helper.Helper;
import mastodon.etalab.gouv.fr.mastodon.R;

import static fr.gouv.etalab.mastodon.helper.Helper.USER_AGENT;


/**
 * Created by Thomas on 23/04/2017.
 * Login activity class which handles the connection
 */

public class LoginActivity extends AppCompatActivity {

    private String client_id;
    private String client_secret;
    private TextView login_two_step;
    private static boolean client_id_for_webview = false;
    private String instance;
    private boolean addAccount = false;
    private EditText login_instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_LIGHT);
        if( theme == Helper.THEME_LIGHT){
            setTheme(R.style.AppTheme);
        }else {
            setTheme(R.style.AppThemeDark);
        }
        setContentView(R.layout.activity_login);

        final Button connectionButton = (Button) findViewById(R.id.login_button);
        login_instance = (EditText) findViewById(R.id.login_instance);
        connectionButton.setEnabled(false);
        login_two_step = (TextView) findViewById(R.id.login_two_step);
        login_two_step.setVisibility(View.GONE);
        login_two_step.setPaintFlags(login_two_step.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        login_two_step.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                client_id_for_webview = true;
                retrievesClientId();
            }
        });

        //For other instances
        TextView other_instance = (TextView) findViewById(R.id.other_instance);
        other_instance.setPaintFlags(other_instance.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        other_instance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, LoginActivity.class);
                intent.putExtra("addAccount", true);
                startActivity(intent);
                finish();
            }
        });

        Bundle b = getIntent().getExtras();
        if(b != null)
            addAccount = b.getBoolean("addAccount", false);

        if( addAccount )
            login_instance.setVisibility(View.VISIBLE);

        if( addAccount) {
            login_instance.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    connectionButton.setEnabled(false);
                    login_two_step.setVisibility(View.INVISIBLE);
                    if (!hasFocus) {
                        retrievesClientId();
                    }
                }
            });
        }
        if( addAccount)
            other_instance.setVisibility(View.GONE);
    }

    @Override
    protected void onResume(){
        super.onResume();
        Button connectionButton = (Button) findViewById(R.id.login_button);
        if( !addAccount ) {
            if (client_id_for_webview || !connectionButton.isEnabled()) {
                connectionButton.setEnabled(false);
                client_id_for_webview = false;
                retrievesClientId();
            }
        }else {
            if (login_instance.getText() != null && login_instance.getText().toString().length() > 0 && client_id_for_webview) {
                connectionButton.setEnabled(false);
                client_id_for_webview = false;
                retrievesClientId();
            }
        }
    }

    private void retrievesClientId(){
        final Button connectionButton = (Button) findViewById(R.id.login_button);
        if( login_instance.getText() != null && login_instance.getText().length() > 0 )
            instance = login_instance.getText().toString().trim();
        else
            instance = Helper.INSTANCE;

        String action = "/api/v1/apps";
        RequestParams parameters = new RequestParams();
        parameters.add(Helper.CLIENT_NAME, Helper.OAUTH_REDIRECT_HOST);
        parameters.add(Helper.REDIRECT_URIS, client_id_for_webview?Helper.REDIRECT_CONTENT_WEB:Helper.REDIRECT_CONTENT);
        parameters.add(Helper.SCOPES, Helper.OAUTH_SCOPES);
        parameters.add(Helper.WEBSITE,"https://" + Helper.getLiveInstance(getApplicationContext()));
        new OauthClient(instance).post(action, parameters, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                JSONObject resobj;
                try {
                    resobj = new JSONObject(response);
                    client_id = resobj.get(Helper.CLIENT_ID).toString();
                    client_secret = resobj.get(Helper.CLIENT_SECRET).toString();

                    String id = resobj.get(Helper.ID).toString();
                    SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString(Helper.CLIENT_ID, client_id);
                    editor.putString(Helper.CLIENT_SECRET, client_secret);
                    editor.putString(Helper.ID, id);
                    editor.apply();
                    connectionButton.setEnabled(true);
                    login_two_step.setVisibility(View.VISIBLE);
                    if( client_id_for_webview){
                        Intent i = new Intent(LoginActivity.this, WebviewConnectActivity.class);
                        i.putExtra("instance", instance);
                        startActivity(i);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                error.printStackTrace();
                Toast.makeText(LoginActivity.this,R.string.client_error, Toast.LENGTH_LONG).show();
            }
        });

        connectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectionButton.setEnabled(false);
                if( client_id_for_webview ){
                    client_id_for_webview = false;
                    retrievesClientId();
                    return;
                }
                AsyncHttpClient client = new AsyncHttpClient();
                RequestParams requestParams = new RequestParams();
                SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                requestParams.add(Helper.CLIENT_ID, sharedpreferences.getString(Helper.CLIENT_ID, null));
                requestParams.add(Helper.CLIENT_SECRET, sharedpreferences.getString(Helper.CLIENT_SECRET, null));
                requestParams.add("grant_type", "password");
                EditText login_uid = (EditText) findViewById(R.id.login_uid);
                EditText login_passwd = (EditText) findViewById(R.id.login_passwd);
                requestParams.add("username",login_uid.getText().toString().trim());
                requestParams.add("password",login_passwd.getText().toString().trim());
                requestParams.add("scope"," read write follow");
                client.setUserAgent(USER_AGENT);
                try {
                    MastalabSSLSocketFactory mastalabSSLSocketFactory = new MastalabSSLSocketFactory(MastalabSSLSocketFactory.getKeystore());
                    mastalabSSLSocketFactory.setHostnameVerifier(MastalabSSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                    client.setSSLSocketFactory(mastalabSSLSocketFactory);
                    client.post("https://" + instance+ "/oauth/token", requestParams, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            String response = new String(responseBody);
                            JSONObject resobj;
                            try {
                                resobj = new JSONObject(response);
                                String token = resobj.get("access_token").toString();
                                SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.putString(Helper.PREF_KEY_OAUTH_TOKEN, token);
                                editor.apply();
                                //Update the account with the token;
                                new UpdateAccountInfoAsyncTask(LoginActivity.this, token, instance).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            connectionButton.setEnabled(true);
                            error.printStackTrace();
                            Toast.makeText(getApplicationContext(),R.string.toast_error_login,Toast.LENGTH_LONG).show();
                        }
                    });

                } catch (NoSuchAlgorithmException | KeyManagementException | UnrecoverableKeyException | KeyStoreException e) {
                    e.printStackTrace();
                }

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
            startActivity(intent);
        }else if(id == R.id.action_privacy){
            Intent intent = new Intent(getApplicationContext(), PrivacyActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

}