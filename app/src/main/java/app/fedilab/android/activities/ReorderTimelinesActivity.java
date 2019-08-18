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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.ManageTimelines;
import app.fedilab.android.client.Entities.RemoteInstance;
import app.fedilab.android.client.HttpsConnection;
import app.fedilab.android.drawers.ReorderTabAdapter;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.helper.itemtouchhelper.OnStartDragListener;
import app.fedilab.android.helper.itemtouchhelper.OnUndoListener;
import app.fedilab.android.helper.itemtouchhelper.SimpleItemTouchHelperCallback;
import app.fedilab.android.sqlite.InstancesDAO;
import app.fedilab.android.sqlite.SearchDAO;
import app.fedilab.android.sqlite.Sqlite;
import app.fedilab.android.sqlite.TimelinesDAO;
import es.dmoral.toasty.Toasty;
import app.fedilab.android.R;
import app.fedilab.android.asynctasks.ManageListsAsyncTask;
import app.fedilab.android.asynctasks.UpdateAccountInfoAsyncTask;
import app.fedilab.android.interfaces.OnListActionInterface;


/**
 * Created by Thomas on 26/04/2019.
 * Reorder timelines activity
 */

public class ReorderTimelinesActivity extends BaseActivity implements OnStartDragListener, OnUndoListener, OnListActionInterface {

    public static boolean updated;
    private ItemTouchHelper touchHelper;
    private RelativeLayout undo_container;
    private TextView undo_message;
    private TextView undo_action;
    private  List<ManageTimelines> timelines;
    private ReorderTabAdapter adapter;
    private ManageTimelines timeline;
    private boolean isLoadingInstance;
    private String oldSearch;
    private int theme;
    private String instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
        theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
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
        isLoadingInstance = false;

        int style;
        if (theme == Helper.THEME_DARK) {
            style = R.style.DialogDark;
        } else if (theme == Helper.THEME_BLACK){
            style = R.style.DialogBlack;
        }else {
            style = R.style.Dialog;
        }


        if( getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null ) {
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            View view = inflater.inflate(R.layout.simple_bar_add,  new LinearLayout(getApplicationContext()), false);
            actionBar.setCustomView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            ImageView toolbar_close = actionBar.getCustomView().findViewById(R.id.toolbar_close);
            TextView toolbar_title = actionBar.getCustomView().findViewById(R.id.toolbar_title);
            ImageView add_remote_instance = actionBar.getCustomView().findViewById(R.id.add_remote_instance);
            toolbar_close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            if( MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.MASTODON || MainActivity.social == UpdateAccountInfoAsyncTask.SOCIAL.PLEROMA){
                add_remote_instance.setVisibility(View.VISIBLE);
            }else{
                add_remote_instance.setVisibility(View.GONE);
            }
            add_remote_instance.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ReorderTimelinesActivity.this, style);
                    LayoutInflater inflater = getLayoutInflater();
                    @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.search_instance, null);
                    dialogBuilder.setView(dialogView);

                    AutoCompleteTextView instance_list = dialogView.findViewById(R.id.search_instance);
                    //Manage download of attachments
                    RadioGroup radioGroup = dialogView.findViewById(R.id.set_attachment_group);

                    instance_list.setFilters(new InputFilter[]{new InputFilter.LengthFilter(60)});
                    dialogBuilder.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                            String instanceName = instance_list.getText().toString().trim();
                            new Thread(new Runnable(){
                                @Override
                                public void run() {
                                    try {
                                        if(radioGroup.getCheckedRadioButtonId() == R.id.mastodon_instance)
                                            new HttpsConnection(ReorderTimelinesActivity.this, null).get("https://" + instanceName + "/api/v1/timelines/public?local=true", 10, null, null);
                                        else  if( radioGroup.getCheckedRadioButtonId() == R.id.peertube_instance)
                                            new HttpsConnection(ReorderTimelinesActivity.this, null).get("https://" + instanceName + "/api/v1/videos/", 10, null, null);
                                        else  if( radioGroup.getCheckedRadioButtonId() == R.id.pixelfed_instance) {
                                            new HttpsConnection(ReorderTimelinesActivity.this, null).get("https://" + instanceName + "/api/v1/timelines/public", 10, null, null);
                                        }else  if( radioGroup.getCheckedRadioButtonId() == R.id.misskey_instance) {
                                            new HttpsConnection(ReorderTimelinesActivity.this, null).post("https://" + instanceName + "/api/notes/local-timeline", 10, null, null);
                                        }else  if( radioGroup.getCheckedRadioButtonId() == R.id.gnu_instance) {
                                            new HttpsConnection(ReorderTimelinesActivity.this, null).get("https://" + instanceName + "/api/statuses/public_timeline.json", 10, null, null);
                                        }

                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                dialog.dismiss();
                                                if(radioGroup.getCheckedRadioButtonId() == R.id.mastodon_instance) {
                                                    new InstancesDAO(ReorderTimelinesActivity.this, db).insertInstance(instanceName, "MASTODON");
                                                }else  if( radioGroup.getCheckedRadioButtonId() == R.id.peertube_instance) {
                                                    new InstancesDAO(ReorderTimelinesActivity.this, db).insertInstance(instanceName, "PEERTUBE");
                                                } else  if( radioGroup.getCheckedRadioButtonId() == R.id.pixelfed_instance) {
                                                    new InstancesDAO(ReorderTimelinesActivity.this, db).insertInstance(instanceName, "PIXELFED");
                                                } else  if( radioGroup.getCheckedRadioButtonId() == R.id.misskey_instance) {
                                                    new InstancesDAO(ReorderTimelinesActivity.this, db).insertInstance(instanceName, "MISSKEY");
                                                }else  if( radioGroup.getCheckedRadioButtonId() == R.id.gnu_instance) {
                                                    new InstancesDAO(ReorderTimelinesActivity.this, db).insertInstance(instanceName, "GNU");
                                                }
                                                if( timelines != null && adapter != null) {
                                                    List<RemoteInstance> instance = new InstancesDAO(ReorderTimelinesActivity.this, db).getInstanceByName(instanceName);
                                                    if( instance != null && instance.size() > 0 ) {
                                                        ManageTimelines manageTimelines = new ManageTimelines();
                                                        manageTimelines.setRemoteInstance(instance.get(0));
                                                        manageTimelines.setPosition(timelines.size());
                                                        manageTimelines.setDisplayed(true);
                                                        manageTimelines.setType(ManageTimelines.Type.INSTANCE);
                                                        timelines.add(manageTimelines);
                                                        adapter.notifyItemInserted((timelines.size() - 1));
                                                    }
                                                    updated = true;
                                                }
                                            }
                                        });
                                    } catch (final Exception e) {
                                        e.printStackTrace();
                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                Toasty.warning(getApplicationContext(), getString(R.string.toast_instance_unavailable), Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                }
                            }).start();
                        }
                    });
                    AlertDialog alertDialog = dialogBuilder.create();
                    alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            //Hide keyboard
                            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                            assert imm != null;
                            imm.hideSoftInputFromWindow(instance_list.getWindowToken(), 0);
                        }
                    });
                    if( alertDialog.getWindow() != null )
                        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                    alertDialog.show();

                    instance_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick (AdapterView<?> parent, View view, int position, long id) {
                            String oldSearch = parent.getItemAtPosition(position).toString().trim();
                        }
                    });
                    instance_list.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            Pattern host = Pattern.compile("([\\da-z\\.-]+\\.[a-z\\.]{2,12})");
                            Matcher matcher = host.matcher(s.toString().trim());
                            if( s.toString().trim().length() == 0 || !matcher.find()) {
                                alertDialog.getButton(
                                        AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                            } else {
                                // Something into edit text. Enable the button.
                                alertDialog.getButton(
                                        AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                            }
                            if (s.length() > 2 && !isLoadingInstance) {
                                final String action = "/instances/search";
                                final HashMap<String, String> parameters = new HashMap<>();
                                parameters.put("q", s.toString().trim());
                                parameters.put("count", String.valueOf(1000));
                                parameters.put("name", String.valueOf(true));
                                isLoadingInstance = true;

                                if( oldSearch == null || !oldSearch.equals(s.toString().trim()))
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                final String response = new HttpsConnection(ReorderTimelinesActivity.this, null).get("https://instances.social/api/1.0" + action, 30, parameters, Helper.THEKINRAR_SECRET_TOKEN);
                                                runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        isLoadingInstance = false;
                                                        String[] instances;
                                                        try {
                                                            JSONObject jsonObject = new JSONObject(response);
                                                            JSONArray jsonArray = jsonObject.getJSONArray("instances");
                                                            if (jsonArray != null) {
                                                                int length = 0;
                                                                for (int i = 0; i < jsonArray.length(); i++) {
                                                                    if( !jsonArray.getJSONObject(i).get("name").toString().contains("@") && jsonArray.getJSONObject(i).get("up").toString().equals("true"))
                                                                        length++;
                                                                }
                                                                instances = new String[length];
                                                                int j = 0;
                                                                for (int i = 0; i < jsonArray.length(); i++) {
                                                                    if( !jsonArray.getJSONObject(i).get("name").toString().contains("@") && jsonArray.getJSONObject(i).get("up").toString().equals("true")) {
                                                                        instances[j] = jsonArray.getJSONObject(i).get("name").toString();
                                                                        j++;
                                                                    }
                                                                }
                                                            } else {
                                                                instances = new String[]{};
                                                            }
                                                            instance_list.setAdapter(null);
                                                            ArrayAdapter<String> adapter =
                                                                    new ArrayAdapter<>(ReorderTimelinesActivity.this, android.R.layout.simple_list_item_1, instances);
                                                            instance_list.setAdapter(adapter);
                                                            if (instance_list.hasFocus() && !ReorderTimelinesActivity.this.isFinishing())
                                                                instance_list.showDropDown();
                                                            oldSearch = s.toString().trim();

                                                        } catch (JSONException ignored) {
                                                            isLoadingInstance = false;
                                                        }
                                                    }
                                                });

                                            } catch (HttpsConnection.HttpsConnectionException e) {
                                                isLoadingInstance = false;
                                            } catch (Exception e) {
                                                isLoadingInstance = false;
                                            }
                                        }
                                    }).start();
                                else
                                    isLoadingInstance = false;
                            }
                        }
                    });
                }
            });
            toolbar_title.setText(R.string.action_reorder_timeline);
            if (theme == Helper.THEME_LIGHT){
                Toolbar toolbar = actionBar.getCustomView().findViewById(R.id.toolbar);
                Helper.colorizeToolbar(toolbar, R.color.black, ReorderTimelinesActivity.this);
            }
        }
        setContentView(R.layout.activity_reorder_tabs);



        updated = false;
        RecyclerView lv_reorder_tabs = findViewById(R.id.lv_reorder_tabs);

        SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        timelines = new TimelinesDAO(getApplicationContext(), db).getAllTimelines();
        adapter = new ReorderTabAdapter(getApplicationContext(), timelines, ReorderTimelinesActivity.this, ReorderTimelinesActivity.this);

        ItemTouchHelper.Callback callback =
                new SimpleItemTouchHelperCallback(adapter);
        touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(lv_reorder_tabs);
        undo_message = findViewById(R.id.undo_message);
        undo_action = findViewById(R.id.undo_action);
        undo_container = findViewById(R.id.undo_container);
        lv_reorder_tabs.setAdapter(adapter);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        lv_reorder_tabs.setLayoutManager(mLayoutManager);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        touchHelper.startDrag(viewHolder);
    }


    @Override
    public void onUndo(ManageTimelines manageTimelines, int position) {
        if (theme == Helper.THEME_LIGHT)
            undo_container.setBackgroundColor(getResources().getColor(R.color.mastodonC3));
        undo_container.setVisibility(View.VISIBLE);
        switch (manageTimelines.getType()){
            case TAG:
                undo_message.setText(R.string.reorder_tag_removed);
                break;
            case INSTANCE:
                undo_message.setText(R.string.reorder_instance_removed);
                break;
            case LIST:
                undo_message.setText(R.string.reorder_list_deleted);
                break;
        }
        undo_action.setPaintFlags(undo_action.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                undo_container.setVisibility(View.GONE);
                SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
                switch (manageTimelines.getType()){
                    case TAG:
                        new SearchDAO(getApplicationContext(), db).remove(manageTimelines.getTagTimeline().getName());
                        new TimelinesDAO(getApplicationContext(), db).remove(manageTimelines);
                        break;
                    case INSTANCE:
                        new InstancesDAO(getApplicationContext(), db).remove(manageTimelines.getRemoteInstance().getHost());
                        new TimelinesDAO(getApplicationContext(), db).remove(manageTimelines);
                        break;
                    case LIST:
                        timeline = manageTimelines;
                        new ManageListsAsyncTask(getApplicationContext(), ManageListsAsyncTask.action.DELETE_LIST,null, null, manageTimelines.getListTimeline().getId(), null, ReorderTimelinesActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        new TimelinesDAO(getApplicationContext(), db).remove(timeline);
                        break;
                }
                updated = true;
            }
        };
        Handler handler = new Handler();
        handler.postDelayed(runnable, 4000);
        undo_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timelines.add(position, manageTimelines);
                adapter.notifyItemInserted(position);
                undo_container.setVisibility(View.GONE);
                handler.removeCallbacks(runnable);
            }
        });
    }

    @Override
    public void onStop(){
        super.onStop();
        if( updated ) {
            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            intent.putExtra(Helper.INTENT_ACTION, Helper.REFRESH_TIMELINE);
            startActivity(intent);
            updated = false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onActionDone(ManageListsAsyncTask.action actionType, APIResponse apiResponse, int statusCode) {
    }
}
