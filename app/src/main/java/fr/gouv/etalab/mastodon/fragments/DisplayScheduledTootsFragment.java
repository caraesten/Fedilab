package fr.gouv.etalab.mastodon.fragments;
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

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.List;
import fr.gouv.etalab.mastodon.asynctasks.RetrieveScheduledTootsAsyncTask;
import fr.gouv.etalab.mastodon.client.Entities.StoredStatus;
import fr.gouv.etalab.mastodon.drawers.ScheduledTootsListAdapter;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveScheduledTootsInterface;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;
import fr.gouv.etalab.mastodon.sqlite.StatusStoredDAO;
import fr.gouv.etalab.mastodon.R;
import static fr.gouv.etalab.mastodon.helper.Helper.changeDrawableColor;


/**
 * Created by Thomas on 16/07/2017.
 * Fragment to display scheduled toots
 */
public class DisplayScheduledTootsFragment extends Fragment implements OnRetrieveScheduledTootsInterface {


    private Context context;
    private AsyncTask<Void, Void, Void> asyncTask;
    private RelativeLayout mainLoader, textviewNoAction;
    private ListView lv_scheduled_toots;
    private TextView warning_battery_message;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_scheduled_toots, container, false);
        context = getContext();

        lv_scheduled_toots = rootView.findViewById(R.id.lv_scheduled_toots);

        mainLoader = rootView.findViewById(R.id.loader);
        warning_battery_message = rootView.findViewById(R.id.warning_battery_message);
        textviewNoAction = rootView.findViewById(R.id.no_action);
        mainLoader.setVisibility(View.VISIBLE);

        //Removes all scheduled toots that have sent
        SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        new StatusStoredDAO(context, db).removeAllSent();
        return rootView;
    }


    @Override
    public void onResume(){
        super.onResume();
        //Retrieves scheduled toots
        asyncTask = new RetrieveScheduledTootsAsyncTask(context, DisplayScheduledTootsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final PowerManager powerManager = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
            final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
            //Battery saver is one and user never asked to stop showing the message
            changeDrawableColor(context, R.drawable.ic_report, R.color.mastodonC4);
            changeDrawableColor(context, R.drawable.ic_cancel, R.color.mastodonC4);
            if( powerManager != null && powerManager.isPowerSaveMode() && sharedpreferences.getBoolean(Helper.SHOW_BATTERY_SAVER_MESSAGE,true)){
                warning_battery_message.setVisibility(View.VISIBLE);
            }else {
                warning_battery_message.setVisibility(View.GONE);
            }
            warning_battery_message.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    final int DRAWABLE_RIGHT = 2;
                    if(event.getAction() == MotionEvent.ACTION_UP) {
                        if(event.getRawX() >= (warning_battery_message.getRight() - warning_battery_message.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.putBoolean(Helper.SHOW_BATTERY_SAVER_MESSAGE, false);
                            editor.apply();
                            warning_battery_message.setVisibility(View.GONE);
                            return true;
                        }
                    }
                    return false;
                }
            });
            warning_battery_message.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("BatteryLife")
                @Override
                public void onClick(View v) {
                    try {
                        Intent battSaverIntent = new Intent();
                        battSaverIntent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$BatterySaverSettingsActivity"));
                        startActivityForResult(battSaverIntent, 0);
                    }catch (ActivityNotFoundException e){
                        try {
                            Intent batterySaverIntent;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                                batterySaverIntent = new Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS);
                                startActivity(batterySaverIntent);
                            }
                        }catch (ActivityNotFoundException ignored){}
                    }
                }
            });
        }else {
            warning_battery_message.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreate(Bundle saveInstance)
    {
        super.onCreate(saveInstance);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public void onDestroy() {
        super.onDestroy();
        if(asyncTask != null && asyncTask.getStatus() == AsyncTask.Status.RUNNING)
            asyncTask.cancel(true);
    }


    @Override
    public void onRetrieveScheduledToots(List<StoredStatus> storedStatuses) {

        mainLoader.setVisibility(View.GONE);
        if( storedStatuses != null && storedStatuses.size() > 0 ){
            ScheduledTootsListAdapter scheduledTootsListAdapter = new ScheduledTootsListAdapter(context, storedStatuses, textviewNoAction);
            lv_scheduled_toots.setAdapter(scheduledTootsListAdapter);
            textviewNoAction.setVisibility(View.GONE);
        }else {
            textviewNoAction.setVisibility(View.VISIBLE);
        }
    }
}
