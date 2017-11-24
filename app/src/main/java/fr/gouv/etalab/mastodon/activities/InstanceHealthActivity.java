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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import java.util.HashMap;
import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.client.HttpsConnection;
import fr.gouv.etalab.mastodon.helper.Helper;


/**
 * Created by Thomas on 24/11/2017.
 * Instance health activity class
 */

public class InstanceHealthActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if( theme == Helper.THEME_LIGHT){
            setTheme(R.style.AppTheme);
        }else {
            setTheme(R.style.AppThemeDark);
        }
        setContentView(R.layout.activity_remote_follow);

        checkInstance("mastodon.social");
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


    private void checkInstance(final String instance){
        if( instance == null)
            return;
        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    HashMap<String, String> parameters = new HashMap<>();
                    parameters.put("name", instance.trim());
                    final String response = new HttpsConnection().get("https://instances.social/api/1.0/instances/show", 30, parameters, Helper.THEKINRAR_SECRET_TOKEN );
                    Log.v(Helper.TAG,"resp: " + response);
                    runOnUiThread(new Runnable() {
                        public void run() {


                        }
                    });

                } catch (HttpsConnection.HttpsConnectionException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }



}
