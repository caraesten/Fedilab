package fr.gouv.etalab.mastodon.activities;
/* Copyright 2019 Thomas Schneider
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


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;

import es.dmoral.toasty.Toasty;
import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.asynctasks.RetrievePeertubeChannelsAsyncTask;
import fr.gouv.etalab.mastodon.asynctasks.RetrievePeertubeSingleAsyncTask;
import fr.gouv.etalab.mastodon.client.APIResponse;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Peertube;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrievePeertubeInterface;

import static fr.gouv.etalab.mastodon.helper.Helper.THEME_LIGHT;

public class PeertubeEditUploadActivity extends BaseActivity implements OnRetrievePeertubeInterface {


    private Button set_upload_submit;
    private Spinner set_upload_privacy, set_upload_channel;
    private TextView set_upload_file_name;
    private HashMap<String, String> channels;
    private String videoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
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
        Bundle b = getIntent().getExtras();

        if(b != null) {
            videoId = b.getString("video_id", null);
        }

        if( getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null ) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
            toolbar_title.setText(R.string.update_video);
            if (theme == THEME_LIGHT){
                Toolbar toolbar = actionBar.getCustomView().findViewById(R.id.toolbar);
                Helper.colorizeToolbar(toolbar, R.color.black, PeertubeEditUploadActivity.this);
            }
        }
        setContentView(R.layout.activity_peertube_edit);


        set_upload_file_name = findViewById(R.id.set_upload_file_name);
        set_upload_channel = findViewById(R.id.set_upload_channel);
        set_upload_privacy = findViewById(R.id.set_upload_privacy);
        set_upload_submit = findViewById(R.id.set_upload_submit);

        String peertubeInstance = Helper.getLiveInstance(getApplicationContext());
        new RetrievePeertubeSingleAsyncTask(PeertubeEditUploadActivity.this, peertubeInstance, videoId, PeertubeEditUploadActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        channels = new HashMap<>();
    }




    @Override
    public void onRetrievePeertube(APIResponse apiResponse) {
        if( apiResponse.getError() != null || apiResponse.getPeertubes() == null || apiResponse.getPeertubes().size() == 0){
            if ( apiResponse.getError().getError() != null)
                Toasty.error(PeertubeEditUploadActivity.this, apiResponse.getError().getError(), Toast.LENGTH_LONG).show();
            else
                Toasty.error(PeertubeEditUploadActivity.this, getString(R.string.toast_error), Toast.LENGTH_LONG).show();
            return;
        }

        //Peertube video
        Peertube peertube = apiResponse.getPeertubes().get(0);
        new RetrievePeertubeChannelsAsyncTask(PeertubeEditUploadActivity.this, PeertubeEditUploadActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        //TODO: hydrate form
    }

    @Override
    public void onRetrievePeertubeComments(APIResponse apiResponse) {

    }

    @Override
    public void onRetrievePeertubeChannels(APIResponse apiResponse) {
        if( apiResponse.getError() != null || apiResponse.getAccounts() == null || apiResponse.getAccounts().size() == 0){
            if ( apiResponse.getError().getError() != null)
                Toasty.error(PeertubeEditUploadActivity.this, apiResponse.getError().getError(), Toast.LENGTH_LONG).show();
            else
                Toasty.error(PeertubeEditUploadActivity.this, getString(R.string.toast_error), Toast.LENGTH_LONG).show();
            return;
        }

        //Populate channels
        List<Account> accounts = apiResponse.getAccounts();
        String[] channelName = new String[accounts.size()];
        int i = 0;
        for(Account account: accounts){
            channels.put(account.getUsername(),account.getId());
            channelName[i] = account.getUsername();
            i++;
        }
        ArrayAdapter<String> adapterChannel = new ArrayAdapter<>(PeertubeEditUploadActivity.this,
                android.R.layout.simple_spinner_dropdown_item, channelName);
        set_upload_channel.setAdapter(adapterChannel);

        //TODO: spinner must point in the right value
    }
}
