package app.fedilab.android.fragments;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import app.fedilab.android.R;
import app.fedilab.android.helper.Helper;


public class ColorSettingsFragment  extends PreferenceFragmentCompat {


    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.fragment_settings_color);

        Preference button = findPreference("reset_pref");
        FragmentActivity context = getActivity();
        int style;
        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        if (theme == Helper.THEME_DARK) {
            style = R.style.DialogDark;
        } else if (theme == Helper.THEME_BLACK) {
            style = R.style.DialogBlack;
        } else {
            style = R.style.Dialog;
        }
        PreferenceFragmentCompat preferenceFragmentCompat = this;
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context, style);
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
}