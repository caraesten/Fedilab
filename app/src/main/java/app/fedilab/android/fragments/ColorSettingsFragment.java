package app.fedilab.android.fragments;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;


import java.util.HashMap;

import app.fedilab.android.R;
import app.fedilab.android.activities.SettingsActivity;
import app.fedilab.android.helper.Helper;


public class ColorSettingsFragment  extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {



    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.fragment_settings_color);

        Preference button = findPreference("reset_pref");
        FragmentActivity context = getActivity();
        assert context != null;
        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(context);

        PreferenceFragmentCompat preferenceFragmentCompat = this;
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                dialogBuilder.setMessage(R.string.reset_color);
                dialogBuilder.setPositiveButton(R.string.reset, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.remove("theme_boost_header_color");
                        editor.remove("theme_statuses_color");
                        editor.remove("theme_link_color");
                        editor.remove("theme_icons_color");
                        editor.remove("theme_background_color");
                        editor.remove("pref_color_background");
                        editor.remove("pref_color_navigation_bar");
                        editor.remove("theme_accent");
                        editor.remove("theme_text_color");
                        editor.remove("theme_primary");
                        editor.commit();
                        dialog.dismiss();
                        setPreferenceScreen(null);
                        addPreferencesFromResource(R.xml.fragment_settings_color);

                    }
                });
                dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.setCancelable(false);
                alertDialog.show();
                return true;
            }
        });

    }


    @Override
    public void onResume() {
        super.onResume();

        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        SettingsActivity.needRestart = true;
    }
}