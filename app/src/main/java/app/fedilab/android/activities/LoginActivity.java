/* Copyright 2017 Thomas Schneider
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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.elconfidencial.bubbleshowcase.BubbleShowCase;
import com.elconfidencial.bubbleshowcase.BubbleShowCaseBuilder;
import com.elconfidencial.bubbleshowcase.BubbleShowCaseListener;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

import app.fedilab.android.R;
import app.fedilab.android.asynctasks.UpdateAccountInfoAsyncTask;
import app.fedilab.android.client.API;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.client.Entities.InstanceNodeInfo;
import app.fedilab.android.client.GNUAPI;
import app.fedilab.android.client.HttpsConnection;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.sqlite.AccountDAO;
import app.fedilab.android.sqlite.Sqlite;
import es.dmoral.toasty.Toasty;


/**
 * Created by Thomas on 23/04/2017.
 * Login activity class which handles the connection
 */

public class LoginActivity extends BaseActivity {

    public static boolean admin;
    private static String client_id;
    private static String client_secret;
    private static boolean client_id_for_webview = false;
    private static String instance;
    private final int PICK_IMPORT = 5557;
    private AutoCompleteTextView login_instance;
    private EditText login_uid;
    private EditText login_passwd;
    private String oldSearch;
    private Button connectionButton, connect_button;
    private String actionToken;
    private String autofilledInstance;
    private UpdateAccountInfoAsyncTask.SOCIAL socialNetwork;
    private String basicAuth;
    private InstanceNodeInfo instanceNodeInfo;
    private ConstraintLayout step_login_credential, step_instance;
    private TextView instance_chosen;
    private Thread thread = null;

    public static String redirectUserToAuthorizeAndLogin(Context context, UpdateAccountInfoAsyncTask.SOCIAL socialNetwork, String clientId, String instance) {
        String queryString = Helper.CLIENT_ID + "=" + clientId;
        queryString += "&" + Helper.REDIRECT_URI + "=" + Uri.encode(Helper.REDIRECT_CONTENT_WEB);
        queryString += "&" + Helper.RESPONSE_TYPE + "=code";
        if (admin) {
            queryString += "&" + Helper.SCOPE + "=" + Helper.OAUTH_SCOPES_ADMIN;
        } else {
            queryString += "&" + Helper.SCOPE + "=" + Helper.OAUTH_SCOPES;
        }
        return Helper.instanceWithProtocol(context, instance) + Helper.EP_AUTHORIZE + "?" + queryString;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getIntent().getExtras();
        socialNetwork = UpdateAccountInfoAsyncTask.SOCIAL.MASTODON;
        admin = false;
        if (b != null) {
            autofilledInstance = b.getString("instance", null);
            if (instanceNodeInfo != null) {
                socialNetwork = Helper.setSoftware(instanceNodeInfo.getName(), false);
            }
            admin = b.getBoolean("admin", false);
        }

        if (getIntent() != null && getIntent().getData() != null && getIntent().getData().toString().contains("mastalab://backtomastalab?code=")) {
            String url = getIntent().getData().toString();
            String[] val = url.split("code=");
            String code = val[1];
            final String action = "/oauth/token";
            final HashMap<String, String> parameters = new HashMap<>();
            parameters.put(Helper.CLIENT_ID, client_id);
            parameters.put(Helper.CLIENT_SECRET, client_secret);
            parameters.put(Helper.REDIRECT_URI, Helper.REDIRECT_CONTENT_WEB);
            parameters.put("grant_type", "authorization_code");
            parameters.put("code", code);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final String response = new HttpsConnection(LoginActivity.this, instance).post(Helper.instanceWithProtocol(LoginActivity.this, instance) + action, 30, parameters, null);
                        JSONObject resobj;
                        try {
                            resobj = new JSONObject(response);
                            String token = resobj.get("access_token").toString();
                            String refresh_token = null;
                            if (resobj.has("refresh_token"))
                                refresh_token = resobj.get("refresh_token").toString();
                            SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.putString(Helper.PREF_KEY_OAUTH_TOKEN, token);
                            editor.apply();
                            //Update the account with the token;
                            new UpdateAccountInfoAsyncTask(LoginActivity.this, token, client_id, client_secret, refresh_token, instance, socialNetwork).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        } catch (JSONException ignored) {
                            ignored.printStackTrace();
                        }
                    } catch (Exception ignored) {
                        ignored.printStackTrace();
                    }
                }
            }).start();
        } else {

            SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
            int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
            switch (theme) {
                case Helper.THEME_LIGHT:
                    setTheme(R.style.AppTheme_Fedilab);
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

            setContentView(R.layout.activity_login);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
                assert inflater != null;
                View view = inflater.inflate(R.layout.simple_bar, new LinearLayout(getApplicationContext()), false);
                view.setBackground(new ColorDrawable(ContextCompat.getColor(LoginActivity.this, R.color.cyanea_primary)));
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
                toolbar_title.setText(R.string.add_account);
            }


            TextView create_an_account_message = findViewById(R.id.create_an_account);
            TextView create_an_account_peertube = findViewById(R.id.create_an_account_peertube);
            SpannableString content_create = new SpannableString(getString(R.string.join_mastodon));
            content_create.setSpan(new UnderlineSpan(), 0, content_create.length(), 0);
            content_create.setSpan(new ForegroundColorSpan(ContextCompat.getColor(LoginActivity.this, R.color.cyanea_accent_reference)), 0, content_create.length(),
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            create_an_account_message.setText(content_create);
            create_an_account_message.setOnClickListener(v -> {
                Intent mainActivity = new Intent(LoginActivity.this, MastodonRegisterActivity.class);
                startActivity(mainActivity);
            });


            content_create = new SpannableString(getString(R.string.join_peertube));
            content_create.setSpan(new UnderlineSpan(), 0, content_create.length(), 0);
            content_create.setSpan(new ForegroundColorSpan(ContextCompat.getColor(LoginActivity.this, R.color.cyanea_accent_reference)), 0, content_create.length(),
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            create_an_account_peertube.setText(content_create);
            create_an_account_peertube.setOnClickListener(v -> {
                Intent mainActivity = new Intent(LoginActivity.this, PeertubeRegisterActivity.class);
                startActivity(mainActivity);
            });


            login_instance = findViewById(R.id.login_instance);
            login_uid = findViewById(R.id.login_uid);
            login_passwd = findViewById(R.id.login_passwd);
            connect_button = findViewById(R.id.connect_button);
            step_login_credential = findViewById(R.id.step_login_credential);
            instance_chosen = findViewById(R.id.instance_chosen);
            step_instance = findViewById(R.id.step_instance);
            connectionButton = findViewById(R.id.login_button);
            ImageView info_instance = findViewById(R.id.info_instance);
            ImageView main_logo = findViewById(R.id.main_logo);
            main_logo.setImageResource(Helper.getMainLogo(getApplicationContext()));
            socialNetwork = UpdateAccountInfoAsyncTask.SOCIAL.MASTODON;
            //Manage instances
            info_instance.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showcaseInstance(false);
                }
            });


            connect_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (login_instance.getText() == null || login_instance.getText().toString().length() == 0) {
                        TextInputLayout login_instance_layout = findViewById(R.id.login_instance_layout);
                        login_instance_layout.setError(getString(R.string.toast_error_instance));
                        login_instance_layout.setErrorEnabled(true);
                        return;
                    }
                    instance = login_instance.getText().toString().trim().toLowerCase();
                    connect_button.setEnabled(false);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            instanceNodeInfo = new API(LoginActivity.this).displayNodeInfo(instance);

                            runOnUiThread(new Runnable() {
                                public void run() {
                                    connect_button.setEnabled(true);
                                    if (instanceNodeInfo != null && instanceNodeInfo.getName() != null) {
                                        socialNetwork = Helper.setSoftware(instanceNodeInfo.getName(), false);
                                        if (instanceNodeInfo.getName().equals("PLEROMA") || instanceNodeInfo.getName().equals("MASTODON") || instanceNodeInfo.getName().equals("PIXELFED")) {
                                            client_id_for_webview = true;
                                            retrievesClientId();
                                        } else {
                                            client_id_for_webview = false;
                                            if (instanceNodeInfo.getName().equals("PEERTUBE")) {
                                                step_login_credential.setVisibility(View.VISIBLE);
                                                step_instance.setVisibility(View.GONE);
                                                instance_chosen.setText(instance);
                                                retrievesClientId();
                                            } else if (instanceNodeInfo.getName().equals("GNU") || instanceNodeInfo.getName().equals("FRIENDICA")) {
                                                step_login_credential.setVisibility(View.VISIBLE);
                                                step_instance.setVisibility(View.GONE);
                                                instance_chosen.setText(instance);
                                            }
                                        }
                                    } else if (instanceNodeInfo != null && instanceNodeInfo.isConnectionError()) {
                                        Toasty.error(getApplicationContext(), getString(R.string.connect_error), Toast.LENGTH_LONG).show();
                                    } else {
                                        Toasty.error(getApplicationContext(), getString(R.string.client_error), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });


                        }
                    }).start();
                }
            });

            showcaseInstance(true);

            login_instance.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    oldSearch = parent.getItemAtPosition(position).toString().trim();
                }
            });
            login_instance.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {

                    if (s.length() > 2) {

                        final String action = "/instances/search";
                        final HashMap<String, String> parameters = new HashMap<>();
                        String query = s.toString().trim();
                        if (query.startsWith("http://")){
                            query =query.replace("http://", "");
                        }
                        if (query.startsWith("https://")){
                            query =query.replace("https://", "");
                        }
                        parameters.put("q", query);
                        parameters.put("count", String.valueOf(1000));
                        parameters.put("name", String.valueOf(true));
                        if( thread != null && thread.isAlive()){
                            thread.interrupt();
                            thread = null;
                        }

                        if (oldSearch == null || !oldSearch.equals(s.toString().trim())) {
                            thread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        final String response = new HttpsConnection(LoginActivity.this, instance).get("https://instances.social/api/1.0" + action, 30, parameters, Helper.THEKINRAR_SECRET_TOKEN);
                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                String[] instances;
                                                try {
                                                    JSONObject jsonObject = new JSONObject(response);
                                                    JSONArray jsonArray = jsonObject.getJSONArray("instances");
                                                    int length = 0;
                                                    for (int i = 0; i < jsonArray.length(); i++) {
                                                        if (!jsonArray.getJSONObject(i).get("name").toString().contains("@"))
                                                            length++;
                                                    }
                                                    instances = new String[length];
                                                    int j = 0;
                                                    for (int i = 0; i < jsonArray.length(); i++) {
                                                        if (!jsonArray.getJSONObject(i).get("name").toString().contains("@")) {
                                                            instances[j] = jsonArray.getJSONObject(i).get("name").toString();
                                                            j++;
                                                        }
                                                    }
                                                    login_instance.setAdapter(null);
                                                    ArrayAdapter<String> adapter =
                                                            new ArrayAdapter<>(LoginActivity.this, android.R.layout.simple_list_item_1, instances);
                                                    login_instance.setAdapter(adapter);
                                                    if (login_instance.hasFocus() && !LoginActivity.this.isFinishing())
                                                        login_instance.showDropDown();
                                                    oldSearch = s.toString().trim();

                                                } catch (JSONException ignored) {
                                                }
                                            }
                                        });

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            thread.start();
                        }
                    }
                }
            });

            if (socialNetwork != UpdateAccountInfoAsyncTask.SOCIAL.GNU && socialNetwork != UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA)
                connectionButton.setEnabled(false);
            login_instance.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (socialNetwork != UpdateAccountInfoAsyncTask.SOCIAL.GNU && socialNetwork != UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA) {
                        connectionButton.setEnabled(false);
                    }
                    TextInputLayout login_instance_layout = findViewById(R.id.login_instance_layout);
                    if (!hasFocus) {
                        retrievesClientId();
                        if (login_instance.getText() == null || login_instance.getText().toString().length() == 0) {
                            login_instance_layout.setError(getString(R.string.toast_error_instance));
                            login_instance_layout.setErrorEnabled(true);
                        }
                    } else {
                        login_instance_layout.setErrorEnabled(false);
                    }
                }
            });


            final TextView login_issue = findViewById(R.id.login_issue);
            SpannableString content = new SpannableString(getString(R.string.issue_login_title));
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            login_issue.setText(content);
            login_issue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    int style;

                    SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
                    int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
                    if (theme == Helper.THEME_DARK) {
                        style = R.style.DialogDark;
                    } else if (theme == Helper.THEME_BLACK) {
                        style = R.style.DialogBlack;
                    } else {
                        style = R.style.Dialog;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this, style);
                    builder.setTitle(R.string.issue_login_title);
                    TextView message = new TextView(LoginActivity.this);
                    final SpannableString s =
                            new SpannableString(getText(R.string.issue_login_message));
                    Linkify.addLinks(s, Linkify.WEB_URLS);
                    message.setText(s);
                    message.setPadding((int) Helper.convertDpToPixel(10, LoginActivity.this), (int) Helper.convertDpToPixel(10, LoginActivity.this), (int) Helper.convertDpToPixel(10, LoginActivity.this), (int) Helper.convertDpToPixel(10, LoginActivity.this));
                    message.setMovementMethod(LinkMovementMethod.getInstance());
                    builder.setView(message);
                    builder.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.setIcon(android.R.drawable.ic_dialog_alert).show();
                }
            });
            if (autofilledInstance != null) {
                login_instance.setText(autofilledInstance.trim());
                retrievesClientId();
                login_uid.requestFocus();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (login_instance != null && login_instance.getText() != null && login_instance.getText().toString().length() > 0 && client_id_for_webview) {
            connectionButton.setEnabled(false);
            client_id_for_webview = false;
            retrievesClientId();
        }
    }

    private void retrievesClientId() {
        if (socialNetwork != UpdateAccountInfoAsyncTask.SOCIAL.GNU && socialNetwork != UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA) {
            String instanceFromField = login_instance.getText().toString().trim();
            if (instanceFromField.startsWith("http://")){
                instanceFromField =instanceFromField.replace("http://", "");
            }
            if (instanceFromField.startsWith("https://")){
                instanceFromField =instanceFromField.replace("https://", "");
            }
            String host = instanceFromField;
            try {
                URL url = new URL(instanceFromField);
                host = url.getHost();
            } catch (MalformedURLException ignored) {
            }
            try {
                instance = URLEncoder.encode(host, "utf-8");
            } catch (UnsupportedEncodingException e) {
                Toasty.error(getApplicationContext(), getString(R.string.client_error), Toast.LENGTH_LONG).show();
            }
            if (socialNetwork == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE)
                actionToken = "/api/v1/oauth-clients/local";
            else
                actionToken = "/api/v1/apps";
            final HashMap<String, String> parameters = new HashMap<>();
            parameters.put(Helper.CLIENT_NAME, Helper.CLIENT_NAME_VALUE);
            parameters.put(Helper.REDIRECT_URIS, client_id_for_webview ? Helper.REDIRECT_CONTENT_WEB : Helper.REDIRECT_CONTENT);
            if (socialNetwork != UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE) {
                if (admin) {
                    parameters.put(Helper.SCOPES, Helper.OAUTH_SCOPES_ADMIN);
                } else {
                    parameters.put(Helper.SCOPES, Helper.OAUTH_SCOPES);
                }
            } else {
                parameters.put(Helper.SCOPES, Helper.OAUTH_SCOPES_PEERTUBE);
            }

            parameters.put(Helper.WEBSITE, Helper.WEBSITE_VALUE);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String response;
                        if (socialNetwork == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE)
                            response = new HttpsConnection(LoginActivity.this, instance).get(Helper.instanceWithProtocol(getApplicationContext(), instance) + actionToken, 30, parameters, null);
                        else
                            response = new HttpsConnection(LoginActivity.this, instance).post(Helper.instanceWithProtocol(getApplicationContext(), instance) + actionToken, 30, parameters, null);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                JSONObject resobj;
                                try {
                                    resobj = new JSONObject(response);
                                    client_id = resobj.get(Helper.CLIENT_ID).toString();
                                    client_secret = resobj.get(Helper.CLIENT_SECRET).toString();
                                    String id = null;
                                    if (socialNetwork != UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE)
                                        id = resobj.get(Helper.ID).toString();
                                    manageClient(client_id, client_secret, id);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } catch (final Exception e) {
                        e.printStackTrace();

                        runOnUiThread(new Runnable() {
                            public void run() {

                                String message;
                                if (e.getLocalizedMessage() != null && e.getLocalizedMessage().trim().length() > 0) {
                                    if (e.getLocalizedMessage().length() < 100) {
                                        message = e.getLocalizedMessage();
                                    } else {
                                        message = getString(R.string.long_api_error, "\ud83d\ude05");
                                    }
                                } else if (e.getMessage() != null && e.getMessage().trim().length() > 0) {
                                    if (e.getLocalizedMessage().length() < 100) {
                                        message = e.getMessage();
                                    } else {
                                        message = getString(R.string.long_api_error, "\ud83d\ude05");
                                    }
                                } else
                                    message = getString(R.string.client_error);
                                Toasty.error(getApplicationContext(), message, Toast.LENGTH_LONG).show();

                            }
                        });
                    }
                }
            }).start();
        } else {
            connectionButton.setEnabled(true);
        }
        connectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectionButton.setEnabled(false);
                if (client_id_for_webview) {
                    client_id_for_webview = false;
                    retrievesClientId();
                    return;
                }

                final HashMap<String, String> parameters = new HashMap<>();
                SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
                if (socialNetwork != UpdateAccountInfoAsyncTask.SOCIAL.GNU && socialNetwork != UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA) {
                    parameters.put(Helper.CLIENT_ID, sharedpreferences.getString(Helper.CLIENT_ID, null));
                    parameters.put(Helper.CLIENT_SECRET, sharedpreferences.getString(Helper.CLIENT_SECRET, null));
                }
                parameters.put("grant_type", "password");
                try {
                    parameters.put("username", URLEncoder.encode(login_uid.getText().toString().trim().toLowerCase(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    parameters.put("username", login_uid.getText().toString().trim().toLowerCase());
                }
                try {
                    parameters.put("password", URLEncoder.encode(login_passwd.getText().toString(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    parameters.put("password", login_passwd.getText().toString());
                }
                String oauthUrl = null;
                if (socialNetwork == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON || socialNetwork == UpdateAccountInfoAsyncTask.SOCIAL.PIXELFED) {
                    parameters.put("scope", " read write follow");
                    oauthUrl = "/oauth/token";
                } else if (socialNetwork == UpdateAccountInfoAsyncTask.SOCIAL.PEERTUBE) {
                    parameters.put("scope", "user");
                    oauthUrl = "/api/v1/users/token";
                } else if (socialNetwork == UpdateAccountInfoAsyncTask.SOCIAL.GNU || socialNetwork == UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA) {
                    String instanceFromField = login_instance.getText().toString().trim();
                    String host;
                    try {
                        URL url = new URL(instanceFromField);
                        host = url.getHost();
                    } catch (MalformedURLException e) {
                        host = instanceFromField;
                    }
                    try {
                        instance = URLEncoder.encode(host, "utf-8");
                    } catch (UnsupportedEncodingException e) {
                    }
                    String username = login_uid.getText().toString().trim().toLowerCase();
                    String password = login_passwd.getText().toString();
                    oauthUrl = "/api/account/verify_credentials.json";
                    String userpass = username + ":" + password;
                    basicAuth = "Basic " + new String(android.util.Base64.encode(userpass.getBytes(), android.util.Base64.NO_WRAP));
                }
                String finalOauthUrl = oauthUrl;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String response;
                            if (socialNetwork != UpdateAccountInfoAsyncTask.SOCIAL.GNU && socialNetwork != UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA) {
                                response = new HttpsConnection(LoginActivity.this, instance).post(Helper.instanceWithProtocol(getApplicationContext(), instance) + finalOauthUrl, 30, parameters, null);
                            } else {
                                response = new HttpsConnection(LoginActivity.this, instance).get(Helper.instanceWithProtocol(getApplicationContext(), instance) + finalOauthUrl, 30, null, basicAuth);
                            }
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    JSONObject resobj;
                                    if (socialNetwork != UpdateAccountInfoAsyncTask.SOCIAL.GNU && socialNetwork != UpdateAccountInfoAsyncTask.SOCIAL.FRIENDICA) {
                                        try {
                                            resobj = new JSONObject(response);
                                            String token = resobj.get("access_token").toString();
                                            String refresh_token = null;
                                            if (resobj.has("refresh_token"))
                                                refresh_token = resobj.get("refresh_token").toString();
                                            SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sharedpreferences.edit();
                                            editor.putString(Helper.PREF_KEY_OAUTH_TOKEN, token);
                                            editor.apply();
                                            //Update the account with the token;
                                            new UpdateAccountInfoAsyncTask(LoginActivity.this, token, client_id, client_secret, refresh_token, instance, socialNetwork).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                        } catch (JSONException ignored) {
                                            ignored.printStackTrace();
                                        }
                                    } else {
                                        try {
                                            resobj = new JSONObject(response);
                                            Account account = GNUAPI.parseAccountResponse(LoginActivity.this, resobj);
                                            account.setToken(basicAuth);
                                            SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sharedpreferences.edit();
                                            editor.putString(Helper.PREF_KEY_OAUTH_TOKEN, basicAuth);
                                            account.setInstance(instance);

                                            SQLiteDatabase db = Sqlite.getInstance(LoginActivity.this, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                                            boolean userExists = new AccountDAO(LoginActivity.this, db).userExist(account);
                                            editor.putString(Helper.PREF_KEY_ID, account.getId());
                                            editor.putBoolean(Helper.PREF_IS_MODERATOR, account.isModerator());
                                            editor.putBoolean(Helper.PREF_IS_ADMINISTRATOR, account.isAdmin());
                                            editor.putString(Helper.PREF_INSTANCE, instance);
                                            editor.apply();
                                            if (userExists)
                                                new AccountDAO(LoginActivity.this, db).updateAccount(account);
                                            else {
                                                if (account.getUsername() != null && account.getCreated_at() != null)
                                                    new AccountDAO(LoginActivity.this, db).insertAccount(account);
                                            }
                                            editor.apply();

                                            Intent mainActivity = new Intent(LoginActivity.this, MainActivity.class);
                                            mainActivity.putExtra(Helper.INTENT_ACTION, Helper.ADD_USER_INTENT);
                                            startActivity(mainActivity);
                                            finish();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }
                            });
                        } catch (final Exception e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    connectionButton.setEnabled(true);
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            String message;
                                            if (e.getLocalizedMessage() != null && e.getLocalizedMessage().trim().length() > 0)
                                                message = e.getLocalizedMessage();
                                            else if (e.getMessage() != null && e.getMessage().trim().length() > 0)
                                                message = e.getMessage();
                                            else
                                                message = getString(R.string.client_error);
                                            Toasty.error(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            });
                        }
                    }
                }).start();
            }
        });
    }

    private void manageClient(String client_id, String client_secret, String id) {

        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(Helper.CLIENT_ID, client_id);
        editor.putString(Helper.CLIENT_SECRET, client_secret);
        editor.putString(Helper.ID, id);
        editor.apply();
        connectionButton.setEnabled(true);
        if (client_id_for_webview) {
            boolean embedded_browser = sharedpreferences.getBoolean(Helper.SET_EMBEDDED_BROWSER, true);
            if (embedded_browser) {
                Intent i = new Intent(LoginActivity.this, WebviewConnectActivity.class);
                i.putExtra("social", socialNetwork);
                i.putExtra("instance", instance);
                startActivity(i);
            } else {
                String url = redirectUserToAuthorizeAndLogin(getApplicationContext(), socialNetwork, client_id, instance);
                Helper.openBrowser(LoginActivity.this, url);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_login, menu);
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        boolean embedded_browser = sharedpreferences.getBoolean(Helper.SET_EMBEDDED_BROWSER, true);
        menu.findItem(R.id.action_custom_tabs).setChecked(!embedded_browser);
        boolean security_provider = sharedpreferences.getBoolean(Helper.SET_SECURITY_PROVIDER, true);
        menu.findItem(R.id.action_provider).setChecked(security_provider);
        return super.onCreateOptionsMenu(menu);
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
        } else if (id == R.id.action_privacy) {
            Intent intent = new Intent(getApplicationContext(), PrivacyActivity.class);
            startActivity(intent);
        } else if (id == R.id.action_proxy) {
            Intent intent = new Intent(getApplicationContext(), ProxyActivity.class);
            startActivity(intent);
        } else if (id == R.id.action_custom_tabs) {
            item.setChecked(!item.isChecked());
            SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putBoolean(Helper.SET_EMBEDDED_BROWSER, !item.isChecked());
            editor.apply();
            return false;
        } else if (id == R.id.action_provider) {
            item.setChecked(!item.isChecked());
            SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putBoolean(Helper.SET_SECURITY_PROVIDER, item.isChecked());
            editor.apply();
            return false;
        } else if (id == R.id.action_import_data) {
            if (ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(LoginActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        TootActivity.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                return true;
            }
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                intent.setType("*/*");
                String[] mimetypes = {"*/*"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                startActivityForResult(intent, PICK_IMPORT);
            } else {
                intent.setType("*/*");
                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                Intent chooserIntent = Intent.createChooser(intent, getString(R.string.toot_select_import));
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});
                startActivityForResult(chooserIntent, PICK_IMPORT);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMPORT && resultCode == RESULT_OK) {
            if (data == null || data.getData() == null) {
                Toasty.error(getApplicationContext(), getString(R.string.toot_select_file_error), Toast.LENGTH_LONG).show();
                return;
            }
            String filename = Helper.getFilePathFromURI(getApplicationContext(), data.getData());
            Sqlite.importDB(LoginActivity.this, filename);
        } else {
            Toasty.error(getApplicationContext(), getString(R.string.toot_select_file_error), Toast.LENGTH_LONG).show();
        }
    }

    private void showcaseInstance(final boolean loop) {
        BubbleShowCaseBuilder showCaseBuilder = new BubbleShowCaseBuilder(LoginActivity.this)
                .title(getString(R.string.instance))
                .description(getString(R.string.showcase_instance))
                .arrowPosition(BubbleShowCase.ArrowPosition.BOTTOM)
                .backgroundColor(ContextCompat.getColor(LoginActivity.this, R.color.mastodonC4))
                .textColor(Color.WHITE)
                .titleTextSize(14)
                .descriptionTextSize(12);
        if (loop)
            showCaseBuilder.showOnce("BUBBLE_SHOW_CASE_INSTANCE_ID");
        showCaseBuilder.listener(new BubbleShowCaseListener() {
            @Override
            public void onTargetClick(BubbleShowCase bubbleShowCase) {
                if (loop) {
                    bubbleShowCase.finishSequence();
                }
            }

            @Override
            public void onCloseActionImageClick(BubbleShowCase bubbleShowCase) {
                if (loop) {
                    bubbleShowCase.finishSequence();
                }
            }

            @Override
            public void onBubbleClick(BubbleShowCase bubbleShowCase) {

            }

            @Override
            public void onBackgroundDimClick(BubbleShowCase bubbleShowCase) {

            }

        })
                .targetView(login_instance)
                .show();
    }

}