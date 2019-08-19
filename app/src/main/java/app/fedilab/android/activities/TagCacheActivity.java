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


import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import app.fedilab.android.drawers.TagsEditAdapter;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.sqlite.Sqlite;
import app.fedilab.android.sqlite.TagsCacheDAO;
import es.dmoral.toasty.Toasty;
import app.fedilab.android.R;


/**
 * Created by Thomas on 01/12/2018.
 * Tag cache activity class
 */

public class TagCacheActivity extends BaseActivity {

    private RecyclerView tag_list;
    private List<String> tags;
    private TagsEditAdapter tagsEditAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        switch (theme){
            case Helper.THEME_LIGHT:
                setTheme(R.style.AppTheme_NoActionBar_Fedilab);
                getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(TagCacheActivity.this, R.color.mastodonC3__));
                break;
            case Helper.THEME_DARK:
                setTheme(R.style.AppThemeDark_NoActionBar);
                getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(TagCacheActivity.this, R.color.mastodonC1));
                break;
            case Helper.THEME_BLACK:
                setTheme(R.style.AppThemeBlack_NoActionBar);
                getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(TagCacheActivity.this, R.color.black_3));
                break;
            default:
                setTheme(R.style.AppThemeDark_NoActionBar);
                getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(TagCacheActivity.this, R.color.mastodonC1));
        }
        getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        tags = new ArrayList<>();
        setContentView(R.layout.activity_tag_cache);

        tag_list = findViewById(R.id.tag_list);
        SQLiteDatabase db = Sqlite.getInstance(TagCacheActivity.this, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        EditText tag_add = findViewById(R.id.tag_add);
        ImageButton save_tag = findViewById(R.id.save_tag);

        save_tag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( tag_add.getText() != null && tag_add.getText().toString().trim().replaceAll("\\#","").length() > 0) {
                    String tagToInsert = tag_add.getText().toString().trim().replaceAll("\\#","");
                    boolean isPresent = new TagsCacheDAO(TagCacheActivity.this, db).isPresent(tagToInsert);
                    if( isPresent)
                        Toasty.warning(TagCacheActivity.this, getString(R.string.tags_already_stored), Toast.LENGTH_LONG).show();
                    else {
                        new TagsCacheDAO(TagCacheActivity.this, db).insert(tagToInsert);
                        int position = tags.size();
                        tags.add(tagToInsert);
                        Toasty.success(TagCacheActivity.this, getString(R.string.tags_stored), Toast.LENGTH_LONG).show();
                        tag_add.setText("");
                        tagsEditAdapter.notifyItemInserted(position);
                    }
                }
            }
        });

        setTitle(R.string.manage_tags);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                List<String> tagsTemp = new TagsCacheDAO(TagCacheActivity.this, db).getAll();
                if( tagsTemp != null)
                    tags = tagsTemp;
                if( tags != null){
                    tagsEditAdapter = new TagsEditAdapter(tags);
                    tag_list.setAdapter(tagsEditAdapter);
                    LinearLayoutManager mLayoutManager = new LinearLayoutManager(TagCacheActivity.this);
                    tag_list.setLayoutManager(mLayoutManager);
                }
            }
        });

    }
}
