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
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cz.msebera.android.httpclient.Header;
import fr.gouv.etalab.mastodon.client.OauthClient;
import fr.gouv.etalab.mastodon.helper.Helper;
import mastodon.etalab.gouv.fr.mastodon.R;


/**
 * Created by Thomas on 23/04/2017.
 * Login activity class which handles the connection
 */

public class LoginActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button connectionButton = (Button) findViewById(R.id.login_button);
        connectionButton.setEnabled(false);

    }

    @Override
    protected void onResume(){
        super.onResume();
        Button connectionButton = (Button) findViewById(R.id.login_button);
        if( !connectionButton.isEnabled())
            retrievesClientId();
    }

    private void retrievesClientId(){
        final Button connectionButton = (Button) findViewById(R.id.login_button);
        final Intent webviewIntent = new Intent(this, WebviewActivity.class);
        String action = "/api/v1/apps";
        RequestParams parameters = new RequestParams();
        parameters.put(Helper.CLIENT_NAME, Helper.OAUTH_REDIRECT_HOST);
        parameters.put(Helper.REDIRECT_URIS,"https://" + Helper.INSTANCE + Helper.REDIRECT_CONTENT);
        parameters.put(Helper.SCOPES, Helper.OAUTH_SCOPES);
        parameters.put(Helper.WEBSITE,"https://" + Helper.INSTANCE);
        new OauthClient().post(action, parameters, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                JSONObject resobj;
                try {
                    resobj = new JSONObject(response);
                    String client_id = resobj.get(Helper.CLIENT_ID).toString();
                    String client_secret = resobj.get(Helper.CLIENT_SECRET).toString();

                    String id = resobj.get(Helper.ID).toString();
                    SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString(Helper.CLIENT_ID, client_id);
                    editor.putString(Helper.CLIENT_SECRET, client_secret);
                    editor.putString(Helper.ID, id);
                    editor.apply();
                    connectionButton.setEnabled(true);
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
                startActivity(webviewIntent);
                finish();
            }
        });
    }



}