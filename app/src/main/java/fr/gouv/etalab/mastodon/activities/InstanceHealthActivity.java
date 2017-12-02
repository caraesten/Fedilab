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



import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import org.json.JSONObject;

import java.util.HashMap;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.client.API;
import fr.gouv.etalab.mastodon.client.Entities.InstanceSocial;
import fr.gouv.etalab.mastodon.client.HttpsConnection;
import fr.gouv.etalab.mastodon.helper.Helper;

import static fr.gouv.etalab.mastodon.helper.Helper.withSuffix;


/**
 * Created by Thomas on 24/11/2017.
 * Instance health activity class
 */

public class InstanceHealthActivity extends AppCompatActivity {

    private InstanceSocial instanceSocial;
    private TextView name, values, checked_at, up, uptime;
    private String instance;
    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setTheme(R.style.AppThemeDark_NoActionBar);
        setContentView(R.layout.activity_instance_social);
        getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Bundle b = getIntent().getExtras();
        if( getSupportActionBar() != null)
            getSupportActionBar().hide();
        instance =  Helper.getLiveInstance(getApplicationContext());
        if(b != null)
            instance = b.getString("instance", Helper.getLiveInstance(getApplicationContext()));

        Button close = findViewById(R.id.close);
        name = findViewById(R.id.name);
        values = findViewById(R.id.values);
        checked_at = findViewById(R.id.checked_at);
        up = findViewById(R.id.up);
        uptime = findViewById(R.id.uptime);
        container = findViewById(R.id.container);


        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        TextView ref_instance = findViewById(R.id.ref_instance);
        SpannableString content = new SpannableString(ref_instance.getText().toString());
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        ref_instance.setText(content);
        ref_instance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://instances.social"));
                startActivity(browserIntent);
            }
        });

        checkInstance();
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


    private void checkInstance(){

        if( instance == null)
            return;
        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    HashMap<String, String> parameters = new HashMap<>();
                    parameters.put("name", instance.trim());
                    final String response = new HttpsConnection().get("https://instances.social/api/1.0/instances/show", 30, parameters, Helper.THEKINRAR_SECRET_TOKEN );
                    if( response != null)
                        instanceSocial = API.parseInstanceSocialResponse(getApplicationContext(), new JSONObject(response));
                    runOnUiThread(new Runnable() {
                        @SuppressLint({"SetTextI18n", "DefaultLocale"})
                        public void run() {
                            if( instanceSocial.getThumbnail() != null && !instanceSocial.getThumbnail().equals("null"))
                                Glide.with(getApplicationContext())
                                        .asBitmap()
                                        .load(instanceSocial.getThumbnail())
                                        .into(new SimpleTarget<Bitmap>() {
                                            @Override
                                            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                                Bitmap workingBitmap = Bitmap.createBitmap(resource);
                                                Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
                                                Canvas canvas = new Canvas(mutableBitmap);
                                                Paint p = new Paint(Color.BLACK);
                                                ColorFilter filter = new LightingColorFilter(0xFF7F7F7F, 0x00000000);
                                                p.setColorFilter(filter);
                                                canvas.drawBitmap(mutableBitmap, new Matrix(), p);
                                                BitmapDrawable background = new BitmapDrawable(getResources(), mutableBitmap);
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                                    container.setBackground(background);
                                                }else{
                                                    container.setBackgroundDrawable(background);
                                                }
                                            }
                                        });
                            name.setText(instanceSocial.getName());
                            if( instanceSocial.isUp()) {
                                up.setText("Is up!");
                                up.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.green));
                            }else {
                                up.setText("Is down!");
                                up.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
                            }
                            uptime.setText(String.format("Uptime: %.2f %%", (instanceSocial.getUptime()*100)));
                            checked_at.setText(String.format("Checked at: %s", Helper.dateToString(getApplicationContext(), instanceSocial.getChecked_at())));
                            values.setText(String.format("version: %s \n %s users - %s statuses", instanceSocial.getVersion(), withSuffix(instanceSocial.getUsers()), withSuffix(instanceSocial.getStatuses())));
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
