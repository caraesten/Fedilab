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

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.TextView;

import mastodon.etalab.gouv.fr.mastodon.R;


/**
 * Created by Thomas on 05/05/2017.
 * About activity
 */

public class AboutActivity extends AppCompatActivity {
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if( getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_about);
        TextView about_version = (TextView) findViewById(R.id.about_version);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            about_version.setText(getResources().getString(R.string.about_vesrion, version));
        } catch (PackageManager.NameNotFoundException ignored) {}

        TextView about_developer = (TextView) findViewById(R.id.about_developer);
        TextView about_license = (TextView) findViewById(R.id.about_license);
        TextView about_code = (TextView) findViewById(R.id.about_code);
        about_developer.setMovementMethod(LinkMovementMethod.getInstance());
        about_license.setMovementMethod(LinkMovementMethod.getInstance());
        about_code.setMovementMethod(LinkMovementMethod.getInstance());
        about_developer.setLinkTextColor(Color.BLUE);
        about_license.setLinkTextColor(Color.BLUE);
        about_code.setLinkTextColor(Color.BLUE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            about_developer.setText(Html.fromHtml(getString(R.string.about_developer), Html.FROM_HTML_MODE_COMPACT));
            about_license.setText(Html.fromHtml(getString(R.string.about_license), Html.FROM_HTML_MODE_COMPACT));
            about_code.setText(Html.fromHtml(getString(R.string.about_code), Html.FROM_HTML_MODE_COMPACT));
        }else {
            about_developer.setText(Html.fromHtml(getString(R.string.about_developer)));
            about_license.setText(Html.fromHtml(getString(R.string.about_license)));
            about_code.setText(Html.fromHtml(getString(R.string.about_code)));
        }
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
