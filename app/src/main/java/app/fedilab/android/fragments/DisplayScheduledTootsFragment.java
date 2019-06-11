package app.fedilab.android.fragments;
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
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import app.fedilab.android.client.APIResponse;
import app.fedilab.android.client.Entities.StoredStatus;
import app.fedilab.android.drawers.ScheduledTootsListAdapter;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.sqlite.BoostScheduleDAO;
import app.fedilab.android.sqlite.Sqlite;
import app.fedilab.android.sqlite.StatusStoredDAO;
import es.dmoral.toasty.Toasty;
import app.fedilab.android.R;
import app.fedilab.android.asynctasks.RetrieveFeedsAsyncTask;
import app.fedilab.android.asynctasks.RetrieveScheduledTootsAsyncTask;
import app.fedilab.android.interfaces.OnRetrieveFeedsInterface;
import app.fedilab.android.interfaces.OnRetrieveScheduledTootsInterface;

import static app.fedilab.android.helper.Helper.changeDrawableColor;


/**
 * Created by Thomas on 16/07/2017.
 * Fragment to display scheduled toots
 */
public class DisplayScheduledTootsFragment extends Fragment implements OnRetrieveScheduledTootsInterface, OnRetrieveFeedsInterface {


    private Context context;
    private AsyncTask<Void, Void, Void> asyncTask;
    private RelativeLayout mainLoader, textviewNoAction;
    private ListView lv_scheduled_toots;
    private TextView warning_battery_message;
    private typeOfSchedule type;
    private List<StoredStatus> storedStatuses;
    private boolean firstCall;
    private ScheduledTootsListAdapter scheduledTootsListAdapter;

    public enum typeOfSchedule{
        TOOT,
        BOOST,
        SERVER
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_scheduled_toots, container, false);
        context = getContext();
        Bundle bundle = this.getArguments();
        assert bundle != null;
        type = (typeOfSchedule) bundle.get("type");
        if( type == null)
            type = typeOfSchedule.TOOT;
        lv_scheduled_toots = rootView.findViewById(R.id.lv_scheduled_toots);
        firstCall = true;
        mainLoader = rootView.findViewById(R.id.loader);
        warning_battery_message = rootView.findViewById(R.id.warning_battery_message);
        textviewNoAction = rootView.findViewById(R.id.no_action);
        mainLoader.setVisibility(View.VISIBLE);
        storedStatuses = new ArrayList<>();
        //Removes all scheduled toots that have sent
        SQLiteDatabase db = Sqlite.getInstance(context, Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
        if( type == typeOfSchedule.TOOT)
            new StatusStoredDAO(context, db).removeAllSent();
        else if( type == typeOfSchedule.BOOST)
            new BoostScheduleDAO(context, db).removeAllSent();
        else if( type == typeOfSchedule.SERVER)
            asyncTask = new RetrieveFeedsAsyncTask(context, RetrieveFeedsAsyncTask.Type.SCHEDULED_TOOTS, null, DisplayScheduledTootsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        scheduledTootsListAdapter = new ScheduledTootsListAdapter(context, type, storedStatuses, textviewNoAction);
        lv_scheduled_toots.setAdapter(scheduledTootsListAdapter);
        return rootView;
    }

    @Override
    public void onRetrieveFeeds(APIResponse apiResponse) {
        if( apiResponse.getError() != null && apiResponse.getError().getStatusCode() != 404 ){
            Toasty.error(context, apiResponse.getError().getError(), Toast.LENGTH_LONG).show();
            return;
        }
        mainLoader.setVisibility(View.GONE);
        if(apiResponse.getStoredStatuses() != null && apiResponse.getStoredStatuses().size() > 0 ){
            storedStatuses.addAll(apiResponse.getStoredStatuses());
            textviewNoAction.setVisibility(View.GONE);
            lv_scheduled_toots.setVisibility(View.VISIBLE);
            scheduledTootsListAdapter.notifyDataSetChanged();
        }else if( firstCall){
            textviewNoAction.setVisibility(View.VISIBLE);
            lv_scheduled_toots.setVisibility(View.GONE);
        }
        firstCall = false;
    }

    @Override
    public void onResume(){
        super.onResume();
        if( type != null && type != typeOfSchedule.SERVER) {
            //Retrieves scheduled toots
            asyncTask = new RetrieveScheduledTootsAsyncTask(context, type, DisplayScheduledTootsFragment.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                final PowerManager powerManager = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
                final SharedPreferences sharedpreferences = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                //Battery saver is one and user never asked to stop showing the message
                Helper.changeDrawableColor(context, R.drawable.ic_report, R.color.mastodonC4);
                Helper.changeDrawableColor(context, R.drawable.ic_cancel, R.color.mastodonC4);
                if (powerManager != null && powerManager.isPowerSaveMode() && sharedpreferences.getBoolean(Helper.SHOW_BATTERY_SAVER_MESSAGE, true)) {
                    warning_battery_message.setVisibility(View.VISIBLE);
                } else {
                    warning_battery_message.setVisibility(View.GONE);
                }
                warning_battery_message.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        final int DRAWABLE_RIGHT = 2;
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            if (event.getRawX() >= (warning_battery_message.getRight() - warning_battery_message.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
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
                        } catch (ActivityNotFoundException e) {
                            try {
                                Intent batterySaverIntent;
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                                    batterySaverIntent = new Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS);
                                    startActivity(batterySaverIntent);
                                }
                            } catch (ActivityNotFoundException ignored) {
                            }
                        }
                    }
                });
            } else {
                warning_battery_message.setVisibility(View.GONE);
            }
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
            scheduledTootsListAdapter = new ScheduledTootsListAdapter(context, type, storedStatuses, textviewNoAction);
            lv_scheduled_toots.setAdapter(scheduledTootsListAdapter);
            textviewNoAction.setVisibility(View.GONE);
        }else {
            textviewNoAction.setVisibility(View.VISIBLE);
            if( type == typeOfSchedule.BOOST) {
                TextView no_action_text = textviewNoAction.findViewById(R.id.no_action_text);
                TextView no_action_text_subtitle = textviewNoAction.findViewById(R.id.no_action_text_subtitle);
                no_action_text.setText(context.getString(R.string.no_scheduled_boosts));

                Spanned message;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    message = Html.fromHtml(context.getString(R.string.no_scheduled_boosts_indications), Html.FROM_HTML_MODE_LEGACY);
                else
                    //noinspection deprecation
                    message = Html.fromHtml(context.getString(R.string.no_scheduled_boosts_indications));
                no_action_text_subtitle.setText(message, TextView.BufferType.SPANNABLE);
            }
        }
    }
}
