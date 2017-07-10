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
 * You should have received a copy of the GNU General Public License along with Thomas Schneider; if not,
 * see <http://www.gnu.org/licenses>. */
package fr.gouv.etalab.mastodon.activities;


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import fr.gouv.etalab.mastodon.asynctasks.RetrieveInstanceAsyncTask;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Instance;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveInstanceInterface;
import mastodon.etalab.gouv.fr.mastodon.R;


/**
 * Created by Thomas on 05/06/2017.
 * Instance activity
 */

public class InstanceActivity extends AppCompatActivity implements OnRetrieveInstanceInterface {

    private Button about_developer;
    private LinearLayout instance_container;
    private RelativeLayout loader;

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
        setContentView(R.layout.activity_instance);

        instance_container = (LinearLayout) findViewById(R.id.instance_container);
        loader = (RelativeLayout) findViewById(R.id.loader);
        instance_container.setVisibility(View.GONE);
        loader.setVisibility(View.VISIBLE);
        setTitle(getString(R.string.action_about_instance));
        new RetrieveInstanceAsyncTask(getApplicationContext(), InstanceActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
    public void onRetrieveInstance(APIResponse apiResponse) {
        instance_container.setVisibility(View.VISIBLE);
        loader.setVisibility(View.GONE);
        if( apiResponse.getError() != null){
            Toast.makeText(getApplicationContext(), R.string.toast_error, Toast.LENGTH_LONG).show();
            return;
        }
        final Instance instance = apiResponse.getInstance();
        TextView instance_title = (TextView) findViewById(R.id.instance_title);
        TextView instance_description = (TextView) findViewById(R.id.instance_description);
        TextView instance_version = (TextView) findViewById(R.id.instance_version);
        TextView instance_uri = (TextView) findViewById(R.id.instance_uri);
        FloatingActionButton instance_contact = (FloatingActionButton) findViewById(R.id.instance_contact);

        instance_title.setText(instance.getTitle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            instance_description.setText(Html.fromHtml(instance.getDescription(), Html.FROM_HTML_MODE_COMPACT));
        else
            //noinspection deprecation
            instance_description.setText(Html.fromHtml(instance.getDescription()));
        if( instance.getDescription() == null || instance.getDescription().trim().length() == 0 )
            instance_description.setText(getString(R.string.instance_no_description));
        instance_version.setText(instance.getVersion());
        instance_uri.setText(instance.getUri());
        if( instance.getEmail() == null){
            instance_contact.setVisibility(View.GONE);
        }

        instance_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto",instance.getEmail(), null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "[Mastodon] - " + instance.getUri());
                startActivity(Intent.createChooser(emailIntent, getString(R.string.send_email)));
            }
        });
    }
}
