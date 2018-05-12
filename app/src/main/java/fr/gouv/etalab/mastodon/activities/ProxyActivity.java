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



import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.helper.Helper;


/**
 * Created by Thomas on 19/01/2018.
 * Proxy activity class
 */

public class ProxyActivity extends BaseActivity {

    private int count2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        switch (theme){
            case Helper.THEME_LIGHT:
                setTheme(R.style.AppTheme_NoActionBar);
                getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(ProxyActivity.this, R.color.mastodonC3__));
                break;
            case Helper.THEME_DARK:
                setTheme(R.style.AppThemeDark_NoActionBar);
                getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(ProxyActivity.this, R.color.mastodonC1));
                break;
            case Helper.THEME_BLACK:
                setTheme(R.style.AppThemeBlack_NoActionBar);
                getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(ProxyActivity.this, R.color.black_3));
                break;
            default:
                setTheme(R.style.AppThemeDark_NoActionBar);
                getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(ProxyActivity.this, R.color.mastodonC1));
        }
        setContentView(R.layout.activity_proxy);
        getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if( getSupportActionBar() != null)
            getSupportActionBar().hide();

        //Enable proxy
        boolean enable_proxy = sharedpreferences.getBoolean(Helper.SET_PROXY_ENABLED, false);
        final CheckBox set_enable_proxy = findViewById(R.id.enable_proxy);
        set_enable_proxy.setChecked(enable_proxy);
        set_enable_proxy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Helper.SET_PROXY_ENABLED, set_enable_proxy.isChecked());
                editor.apply();
            }
        });

        Button save = findViewById(R.id.set_proxy_save);

        final EditText host = findViewById(R.id.host);
        final EditText port = findViewById(R.id.port);
        final EditText proxy_login = findViewById(R.id.proxy_login);
        final EditText proxy_password = findViewById(R.id.proxy_password);

        String hostVal = sharedpreferences.getString(Helper.SET_PROXY_HOST, "127.0.0.1");
        int portVal = sharedpreferences.getInt(Helper.SET_PROXY_PORT, 8118);
        final String login = sharedpreferences.getString(Helper.SET_PROXY_LOGIN, null);
        final String pwd = sharedpreferences.getString(Helper.SET_PROXY_PASSWORD, null);
        if( hostVal.length() > 0)
            host.setText(hostVal);
        port.setText(String.valueOf(portVal));
        if( login != null && login.length() > 0)
            proxy_login.setText(login);
        if( pwd != null && proxy_password.length() > 0)
            proxy_password.setText(pwd);
        count2 = 0;
        final Spinner proxy_type = findViewById(R.id.type);
        ArrayAdapter<CharSequence> adapterTrans = ArrayAdapter.createFromResource(ProxyActivity.this,
                R.array.proxy_type_choice, android.R.layout.simple_spinner_item);
        proxy_type.setAdapter(adapterTrans);


        proxy_type.setSelection(sharedpreferences.getInt(Helper.SET_PROXY_TYPE, 0));
        proxy_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if( count2 > 0){
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putInt(Helper.SET_PROXY_TYPE, position);
                    editor.apply();
                }else {
                    count2++;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String hostVal = host.getText().toString().trim();
                String portVal = port.getText().toString().trim();
                String proxy_loginVal = proxy_login.getText().toString().trim();
                String proxy_passwordVal = proxy_password.getText().toString().trim();
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(Helper.SET_PROXY_HOST, hostVal);
                editor.putInt(Helper.SET_PROXY_PORT, Integer.parseInt(portVal));
                editor.putString(Helper.SET_PROXY_LOGIN, proxy_loginVal);
                editor.putString(Helper.SET_PROXY_PASSWORD, proxy_passwordVal);
                editor.apply();
                finish();
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





}
