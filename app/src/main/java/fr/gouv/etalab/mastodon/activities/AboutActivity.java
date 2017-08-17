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

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import fr.gouv.etalab.mastodon.asynctasks.RetrieveDeveloperAccountsAsyncTask;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveSearcAccountshInterface;
import mastodon.etalab.gouv.fr.mastodon.R;


/**
 * Created by Thomas on 05/05/2017.
 * About activity
 */

public class AboutActivity extends AppCompatActivity implements OnRetrieveSearcAccountshInterface {

    private Button about_developer;

    @SuppressWarnings("deprecation")
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
        if( getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_about);
        TextView about_version = (TextView) findViewById(R.id.about_version);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            about_version.setText(getResources().getString(R.string.about_vesrion, version));
        } catch (PackageManager.NameNotFoundException ignored) {}

        about_developer = (Button) findViewById(R.id.about_developer);
        Button about_code = (Button) findViewById(R.id.about_code);
        Button about_license = (Button) findViewById(R.id.about_license);
        Button about_thekinrar = (Button) findViewById(R.id.about_thekinrar);

        about_code.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://bitbucket.org/tom79/mastodon_etalab/src"));
               startActivity(browserIntent);
           }
        });

        about_thekinrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://instances.social/"));
                startActivity(browserIntent);
            }
        });
        if(Helper.isLoggedIn(getApplicationContext())) {
            about_developer.setEnabled(false);
            new RetrieveDeveloperAccountsAsyncTask(getApplicationContext(),AboutActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        about_developer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://mastodon.etalab.gouv.fr/@tschneider"));
                startActivity(browserIntent);
            }
        });
        about_license.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.gnu.org/licenses/quick-guide-gplv3.fr.html"));
                startActivity(browserIntent);
            }
        });
        Button about_translation = (Button) findViewById(R.id.about_translation);
        about_translation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://translate.yandex.com/"));
                startActivity(browserIntent);
            }
        });

        TextView about_thanks = (TextView) findViewById(R.id.about_thanks_dev);
        String currentText = about_thanks.getText().toString();
        SpannableString spanned_thanks = new SpannableString(currentText);
        int startPosition = spanned_thanks.toString().indexOf("@PhotonQyv");
        int endPosition = startPosition + "@PhotonQyv".length();
        spanned_thanks.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://mastodon.xyz/@PhotonQyv"));
                    startActivity(browserIntent);
                }
                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                }
            }, startPosition, endPosition, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        about_thanks.setText(spanned_thanks, TextView.BufferType.SPANNABLE);
        about_thanks.setMovementMethod(LinkMovementMethod.getInstance());
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
    public void onRetrieveSearchAccounts(APIResponse apiResponse) {
        about_developer.setEnabled(true);
        final List<Account> accounts = apiResponse.getAccounts();
        if( accounts != null && accounts.size() > 0 && accounts.get(0) != null) {
            about_developer.setOnClickListener(null);
            about_developer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(AboutActivity.this, ShowAccountActivity.class);
                    Bundle b = new Bundle();
                    b.putString("accountId", accounts.get(0).getId());
                    intent.putExtras(b);
                    startActivity(intent);
                }
            });
        }
    }
}
