package app.fedilab.android.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;


import com.jaredrummler.cyanea.Cyanea;
import com.jaredrummler.cyanea.prefs.CyaneaTheme;


import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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


        final ListPreference listPreference = (ListPreference) findPreference("pref_theme_picker");
        List<String> array = Arrays.asList(getResources().getStringArray(R.array.settings_theme));
        CharSequence[] entries = array.toArray(new CharSequence[array.size()]);
        CharSequence[] entryValues = new CharSequence[3];
        final SharedPreferences sharedpref = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int theme = sharedpref.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        entryValues[0] = String.valueOf(Helper.THEME_LIGHT);
        entryValues[1] = String.valueOf(Helper.THEME_DARK);
        entryValues[2] = String.valueOf(Helper.THEME_BLACK);
        listPreference.setEntries(entries);
        listPreference.setDefaultValue(String.valueOf(theme));
        listPreference.setEntryValues(entryValues);


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

        if( key.compareTo("pref_theme_picker") == 0){
            String theme = sharedPreferences.getString("pref_theme_picker", null);
            List<CyaneaTheme> list = CyaneaTheme.Companion.from(Objects.requireNonNull(getActivity()).getAssets(), "themes/cyanea_themes.json");
            if( getActivity() != null && theme != null) {
                SharedPreferences sharedpreferences = getActivity().getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                int i = 0;
                if( theme.compareTo("2") == 0 ) {
                    editor.putInt(Helper.SET_THEME, Helper.THEME_LIGHT);
                }else  if( theme.compareTo("1") == 0 ) {
                    editor.putInt(Helper.SET_THEME, Helper.THEME_DARK);
                    i = 1;
                }else  if( theme.compareTo("3") == 0 ) {
                    editor.putInt(Helper.SET_THEME, Helper.THEME_BLACK);
                    i = 2;
                }
                editor.commit();
                list.get(i).apply(Cyanea.getInstance()).recreate(getActivity());
            }
        }
    }
}