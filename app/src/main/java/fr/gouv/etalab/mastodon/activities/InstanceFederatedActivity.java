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
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveFeedsAsyncTask;
import fr.gouv.etalab.mastodon.client.Entities.Account;
import fr.gouv.etalab.mastodon.client.Entities.Status;
import fr.gouv.etalab.mastodon.client.HttpsConnection;
import fr.gouv.etalab.mastodon.fragments.DisplayStatusFragment;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.services.LiveNotificationService;
import fr.gouv.etalab.mastodon.sqlite.AccountDAO;
import fr.gouv.etalab.mastodon.sqlite.InstancesDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import static fr.gouv.etalab.mastodon.helper.Helper.INSTANCE_NAME;
import static fr.gouv.etalab.mastodon.helper.Helper.INTENT_ACTION;
import static fr.gouv.etalab.mastodon.helper.Helper.SEARCH_INSTANCE;
import static fr.gouv.etalab.mastodon.helper.Helper.THEME_BLACK;


public class InstanceFederatedActivity extends BaseActivity {

    private FloatingActionButton add_new;
    public static String currentLocale;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private static BroadcastReceiver receive_data, receive_federated_data, receive_local_data;
    private String userIdService;
    private AppBarLayout appBar;
    private String userId;
    private String instance;
    private PagerAdapter adapter;
    boolean isLoadingInstance = false;
    private AutoCompleteTextView instance_list;
    private String oldSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);




        final int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        switch (theme){
            case Helper.THEME_LIGHT:
                setTheme(R.style.AppTheme_NoActionBar);
                break;
            case Helper.THEME_DARK:
                setTheme(R.style.AppThemeDark_NoActionBar);
                break;
            case Helper.THEME_BLACK:
                setTheme(R.style.AppThemeBlack_NoActionBar);
                break;
            default:
                setTheme(R.style.AppThemeDark_NoActionBar);
        }
        setContentView(R.layout.activity_federated);


        FloatingActionButton federated_timeline_close = findViewById(R.id.federated_timeline_close);

        federated_timeline_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });



        FloatingActionButton add_new_instance = findViewById(R.id.add_new_instance);
        add_new_instance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(InstanceFederatedActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.search_instance, null);
                dialogBuilder.setView(dialogView);

                instance_list = dialogView.findViewById(R.id.search_instance);
                instance_list.setFilters(new InputFilter[]{new InputFilter.LengthFilter(60)});
                dialogBuilder.setPositiveButton(R.string.validate, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();

                        String instanceName = instance_list.getText().toString().trim();
                        //Already in db
                        List<String> s_ = new InstancesDAO(InstanceFederatedActivity.this, db).getInstanceByName(instanceName);
                        if( s_ == null)
                            s_ = new ArrayList<>();



                        new Thread(new Runnable(){
                            @Override
                            public void run() {
                                try {
                                    String response = new HttpsConnection(InstanceFederatedActivity.this).get("https://" + instanceName + "/api/v1/timelines/public?local=true", 10, null, null);
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            JSONObject resobj;
                                            try {
                                                new InstancesDAO(InstanceFederatedActivity.this, db).insertInstance(instanceName);

                                                resobj = new JSONObject(response);
                                                Intent intent = new Intent(InstanceFederatedActivity.this, InstanceFederatedActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                intent.putExtra(INTENT_ACTION, SEARCH_INSTANCE);
                                                intent.putExtra(INSTANCE_NAME, instanceName);
                                                startActivity(intent);
                                            } catch (JSONException ignored) {ignored.printStackTrace();}
                                        }
                                    });
                                } catch (final Exception e) {
                                    e.printStackTrace();
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), R.string.toast_instance_unavailable,Toast.LENGTH_LONG).show();
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
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
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
                        oldSearch = parent.getItemAtPosition(position).toString().trim();
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
                            parameters.put("count", String.valueOf(50));
                            parameters.put("name", String.valueOf(true));
                            isLoadingInstance = true;

                            if( oldSearch == null || !oldSearch.equals(s.toString().trim()))
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            final String response = new HttpsConnection(InstanceFederatedActivity.this).get("https://instances.social/api/1.0" + action, 30, parameters, Helper.THEKINRAR_SECRET_TOKEN);
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
                                                                if( !jsonArray.getJSONObject(i).get("name").toString().contains("@"))
                                                                    length++;
                                                            }
                                                            instances = new String[length];
                                                            int j = 0;
                                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                                if( !jsonArray.getJSONObject(i).get("name").toString().contains("@")) {
                                                                    instances[j] = jsonArray.getJSONObject(i).get("name").toString();
                                                                    j++;
                                                                }
                                                            }
                                                        } else {
                                                            instances = new String[]{};
                                                        }
                                                        instance_list.setAdapter(null);
                                                        ArrayAdapter<String> adapter =
                                                                new ArrayAdapter<>(InstanceFederatedActivity.this, android.R.layout.simple_list_item_1, instances);
                                                        instance_list.setAdapter(adapter);
                                                        if (instance_list.hasFocus() && !InstanceFederatedActivity.this.isFinishing())
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

        //Test if user is still log in
        if( ! Helper.isLoggedIn(getApplicationContext())) {
            //It is not, the user is redirected to the login page
            Intent myIntent = new Intent(InstanceFederatedActivity.this, LoginActivity.class);
            startActivity(myIntent);
            finish();
            return;
        }


        SQLiteDatabase db = Sqlite.getInstance(getApplicationContext(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        Helper.canPin = false;
        Helper.fillMapEmoji(getApplicationContext());
        //Here, the user is authenticated
        appBar = findViewById(R.id.appBar);
        Toolbar toolbar = findViewById(R.id.toolbar);
        if( theme == THEME_BLACK)
            toolbar.setBackgroundColor(ContextCompat.getColor(InstanceFederatedActivity.this, R.color.black));
        setSupportActionBar(toolbar);
        tabLayout = findViewById(R.id.tabLayout);


        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);



        //Display filter for notification when long pressing the tab
        final LinearLayout tabStrip = (LinearLayout) tabLayout.getChildAt(0);


        viewPager = findViewById(R.id.viewpager);

        adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));




        refreshInstanceTab();

        //Hide the default title
        if( getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().getThemedContext().setTheme(R.style.AppThemeBlack);
        }
        //Defines the current locale of the device in a static variable
        currentLocale = Helper.currentLocale(getApplicationContext());


        add_new = findViewById(R.id.add_new);


        userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
        instance = sharedpreferences.getString(Helper.PREF_INSTANCE, Helper.getLiveInstance(getApplicationContext()));

        Account account = new AccountDAO(getApplicationContext(), db).getAccountByID(userId);
        if( account == null){
            Helper.logout(getApplicationContext());
            Intent myIntent = new Intent(InstanceFederatedActivity.this, LoginActivity.class);
            startActivity(myIntent);
            finish();
            return;
        }


        ImageView iconbar = toolbar.findViewById(R.id.iconbar);

        Helper.loadPictureIcon(InstanceFederatedActivity.this, account.getAvatar(),iconbar);




        if( receive_data != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receive_data);
        receive_data = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle b = intent.getExtras();
                Helper.EventStreaming eventStreaming = (Helper.EventStreaming) intent.getSerializableExtra("eventStreaming");
                assert b != null;
                userIdService = b.getString("userIdService", null);
                if( userIdService != null && userIdService.equals(userId)) {

                }
            }
        };
        mamageNewIntent(getIntent());
       // LocalBroadcastManager.getInstance(this).registerReceiver(receive_data, new IntentFilter(Helper.RECEIVE_DATA));
    }

    public void refreshInstanceTab(){
        Helper.addInstanceTab(InstanceFederatedActivity.this, tabLayout, adapter);
    }


    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mamageNewIntent(intent);
    }


    /**
     * Manages new intents
     * @param intent Intent - intent related to a notification in top bar
     */
    private void mamageNewIntent(Intent intent){
        if( intent == null || intent.getExtras() == null )
            return;
        Bundle extras = intent.getExtras();
        if( extras.containsKey(INTENT_ACTION) ){
            if(extras.getInt(INTENT_ACTION) == SEARCH_INSTANCE){
                String instanceName = extras.getString(INSTANCE_NAME);
                if( instanceName != null){
                    adapter = new InstanceFederatedActivity.PagerAdapter
                            (getSupportFragmentManager(), tabLayout.getTabCount());
                    viewPager.setAdapter(adapter);
                    for(int i = 0; i < tabLayout.getTabCount() ; i++ ){
                        if( tabLayout.getTabAt(i).getText() != null && tabLayout.getTabAt(i).getText().equals(instanceName.trim())){
                            tabLayout.getTabAt(i).select();
                            break;
                        }

                    }
                }
            }
        }
        intent.replaceExtras(new Bundle());
        intent.setAction("");
        intent.setData(null);
        intent.setFlags(0);
    }


    @Override
    public void onStart(){
        super.onStart();
        if( receive_federated_data != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receive_federated_data);
        receive_federated_data = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle b = intent.getExtras();
                assert b != null;
                userIdService = b.getString("userIdService", null);
                if( userIdService != null && userIdService.equals(userId)) {
                    Status status = b.getParcelable("data");

                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(receive_federated_data, new IntentFilter(Helper.RECEIVE_FEDERATED_DATA));
    }

    @Override
    public void onStop(){
        super.onStop();
        if( receive_federated_data != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receive_federated_data);

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if( receive_data != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receive_data);
    }



    /**
     * Page Adapter for settings
     */
    public class PagerAdapter extends FragmentStatePagerAdapter  {
        int mNumOfTabs;

        private PagerAdapter(FragmentManager fm, int NumOfTabs) {
            super(fm);
            this.mNumOfTabs = NumOfTabs;
        }

        public void removeTabPage() {
            this.mNumOfTabs--;
            notifyDataSetChanged();
        }

        public void addTabPage(String title) {
            TabLayout.Tab tab = tabLayout.newTab();
            tab.setText(title);
            this.mNumOfTabs++;
            notifyDataSetChanged();
        }

        @Override
        public Fragment getItem(int position) {
            //Selection comes from another menu, no action to do
            DisplayStatusFragment statusFragment;
            Bundle bundle = new Bundle();
            statusFragment = new DisplayStatusFragment();
            bundle.putSerializable("type", RetrieveFeedsAsyncTask.Type.REMOTE_INSTANCE);
            bundle.putString("remote_instance", tabLayout.getTabAt(position).getText().toString());
            statusFragment.setArguments(bundle);
            return statusFragment;

        }


        @Override
        public int getCount() {
            return mNumOfTabs;
        }
    }




    @SuppressWarnings("ConstantConditions")
    public void updateTimeLine(RetrieveFeedsAsyncTask.Type type, int value){
        int position = tabLayout.getSelectedTabPosition();
        View tabLocal = tabLayout.getTabAt(position).getCustomView();
        assert tabLocal != null;
        TextView tabCounter = tabLocal.findViewById(R.id.tab_counter);
        tabCounter.setText(String.valueOf(value));
        if( value > 0){
            tabCounter.setVisibility(View.VISIBLE);
        }else {
            tabCounter.setVisibility(View.GONE);
        }
    }


    public void startSreaming(){
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        boolean liveNotifications = sharedpreferences.getBoolean(Helper.SET_LIVE_NOTIFICATIONS, true);
        if( liveNotifications) {
            ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            assert manager != null;
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (LiveNotificationService.class.getName().equals(service.service.getClassName())) {
                    return;
                }
            }
            Intent streamingIntent = new Intent(this, LiveNotificationService.class);
            startService(streamingIntent);
        }

    }

}