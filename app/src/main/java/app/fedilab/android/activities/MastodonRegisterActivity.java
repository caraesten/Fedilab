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

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.jaredrummler.materialspinner.MaterialSpinner;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import app.fedilab.android.R;
import app.fedilab.android.asynctasks.RetrieveInstanceRegAsyncTask;
import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.InstanceReg;
import app.fedilab.android.drawers.InstanceRegAdapter;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.interfaces.OnRetrieveInstanceInterface;
import es.dmoral.toasty.Toasty;

import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;

/**
 * Created by Thomas on 13/06/2019.
 * Register activity class
 */

public class MastodonRegisterActivity extends BaseActivity implements OnRetrieveInstanceInterface {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
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

        setContentView(R.layout.activity_register);
        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null ) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
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
            toolbar_title.setText(R.string.add_account);
            if (theme == Helper.THEME_LIGHT){
                Toolbar toolbar = actionBar.getCustomView().findViewById(R.id.toolbar);
                Helper.colorizeToolbar(toolbar, R.color.black, MastodonRegisterActivity.this);
            }
        }

        MaterialSpinner reg_category = findViewById(R.id.reg_category);
        Helper.changeMaterialSpinnerColor(MastodonRegisterActivity.this, reg_category);
        String[] categoriesA = {
                getString(R.string.category_general),
                getString(R.string.category_regional),
                getString(R.string.category_art),
                getString(R.string.category_journalism),
                getString(R.string.category_activism),
                "LGBTQ+",
                getString(R.string.category_games),
                getString(R.string.category_tech),
                getString(R.string.category_adult),
                getString(R.string.category_furry),
                getString(R.string.category_food)

        };
        String[] itemA = {
                "general",
                "regional",
                "art",
                "journalism",
                "activism",
                "lgbt",
                "games",
                "tech",
                "adult",
                "furry",
                "food",
        };
        ArrayAdapter<String> adcategories = new ArrayAdapter<>(MastodonRegisterActivity.this,
                android.R.layout.simple_spinner_dropdown_item, categoriesA);

        reg_category.setAdapter(adcategories);

        reg_category.setSelectedIndex(0);
        //Manage privacies
        reg_category.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                new RetrieveInstanceRegAsyncTask(MastodonRegisterActivity.this, itemA[position], MastodonRegisterActivity.this).executeOnExecutor(THREAD_POOL_EXECUTOR);

            }
        });

        new RetrieveInstanceRegAsyncTask(MastodonRegisterActivity.this, "general", MastodonRegisterActivity.this).executeOnExecutor(THREAD_POOL_EXECUTOR);
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    public void onRetrieveInstance(APIResponse apiResponse) {
        if( apiResponse.getError() != null ){
            Toasty.error(MastodonRegisterActivity.this, getString(R.string.toast_error_instance_reg), Toast.LENGTH_LONG).show();
            return;
        }
        List<InstanceReg> instanceRegs = apiResponse.getInstanceRegs();
        RecyclerView lv_instances = findViewById(R.id.reg_category_view);
        InstanceRegAdapter instanceRegAdapter = new InstanceRegAdapter(MastodonRegisterActivity.this, instanceRegs);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(MastodonRegisterActivity.this);
        lv_instances.setLayoutManager(mLayoutManager);
        lv_instances.setNestedScrollingEnabled(false);
        lv_instances.setAdapter(instanceRegAdapter);


    }
}