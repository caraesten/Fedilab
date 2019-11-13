package app.fedilab.android.fragments;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;


import com.jaredrummler.cyanea.Cyanea;
import com.jaredrummler.cyanea.prefs.CyaneaTheme;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import app.fedilab.android.R;
import app.fedilab.android.activities.MainActivity;
import app.fedilab.android.activities.SettingsActivity;
import app.fedilab.android.activities.TootActivity;
import app.fedilab.android.client.Entities.Account;
import app.fedilab.android.helper.Helper;
import app.fedilab.android.sqlite.AccountDAO;
import app.fedilab.android.sqlite.Sqlite;
import es.dmoral.toasty.Toasty;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;


public class ColorSettingsFragment  extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {


    private final int PICK_IMPORT_THEME = 5557;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.fragment_settings_color);
        createPref();
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

        if (key.equals("use_custom_theme")) {
            if( !sharedPreferences.getBoolean("use_custom_theme", false)){

                FragmentActivity context = getActivity();
                assert context != null;
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                dialogBuilder.setMessage(R.string.restore_default_theme);
                dialogBuilder.setPositiveButton(R.string.restore, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        reset();
                        dialog.dismiss();
                        restart();
                    }
                });
                dialogBuilder.setNegativeButton(R.string.store_before, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        exportColors();
                        reset();
                        dialog.dismiss();
                        restart();
                    }
                });
                dialogBuilder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("use_custom_theme", true);
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.setCancelable(false);
                alertDialog.show();
            }
            createPref();
        }
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


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMPORT_THEME && resultCode == RESULT_OK) {
            if (data == null || data.getData() == null) {
                Toasty.error(getActivity(), getString(R.string.theme_file_error), Toast.LENGTH_LONG).show();
                return;
            }
            if( data.getData() != null) {
                BufferedReader br = null;
                try {
                    InputStream inputStream = getActivity().getContentResolver().openInputStream(data.getData() );
                    assert inputStream != null;
                    br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                    String sCurrentLine;
                    int i = 0;
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    SharedPreferences.Editor editor = prefs.edit();

                    editor.putBoolean("use_custom_theme", true);
                    while ((sCurrentLine = br.readLine()) != null) {
                        if( i > 0 ){
                            String[] line = sCurrentLine.split(",");
                            if( line.length > 1 ) {
                                String key = line[0];
                                String value = line[1];
                                if( key.compareTo("base_theme") == 0){
                                    SharedPreferences sharedpreferences = getActivity().getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
                                    SharedPreferences.Editor appEditor = sharedpreferences.edit();
                                    if( value.compareTo("2") == 0 ) {
                                        appEditor.putInt(Helper.SET_THEME, Helper.THEME_LIGHT);
                                    }else  if( value.compareTo("1") == 0 ) {
                                        appEditor.putInt(Helper.SET_THEME, Helper.THEME_DARK);
                                    }else  if( value.compareTo("3") == 0 ) {
                                        appEditor.putInt(Helper.SET_THEME, Helper.THEME_BLACK);
                                    }
                                    appEditor.commit();
                                }else if( key.compareTo("pref_color_navigation_bar") == 0 || key.compareTo("pref_color_status_bar") == 0){
                                    editor.putBoolean(key, Boolean.valueOf(value));
                                }else{
                                    editor.putInt(key, Integer.valueOf(value));
                                }
                            }
                        }
                        i++;
                    }
                    editor.commit();
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                    dialogBuilder.setMessage(R.string.restart_message);
                    dialogBuilder.setTitle(R.string.apply_changes);
                    dialogBuilder.setPositiveButton(R.string.restart, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            restart();
                        }
                    });
                    dialogBuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertDialog = dialogBuilder.create();
                    alertDialog.setCancelable(false);
                    alertDialog.show();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (br != null)br.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

            }else{
                Toasty.error(getActivity(), getString(R.string.theme_file_error), Toast.LENGTH_LONG).show();
            }

        }
    }


    private void restart(){
        Intent mStartActivity = new Intent(getActivity(), MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(getActivity(), mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
        assert mgr != null;
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }

    private void createPref(){
        getPreferenceScreen().removeAll();
        addPreferencesFromResource(R.xml.fragment_settings_color);
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        FragmentActivity context = getActivity();
        assert context != null;
        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(context);
        ListPreference pref_theme_picker = (ListPreference) findPreference("pref_theme_picker");
        Preference theme_link_color = findPreference("theme_link_color");
        Preference theme_boost_header_color = findPreference("theme_boost_header_color");
        Preference theme_statuses_color = findPreference("theme_statuses_color");
        Preference theme_icons_color = findPreference("theme_icons_color");
        Preference theme_text_color = findPreference("theme_text_color");
        Preference theme_primary = findPreference("theme_primary");
        Preference theme_accent = findPreference("theme_accent");
        Preference pref_color_navigation_bar = findPreference("pref_color_navigation_bar");
        Preference pref_color_status_bar = findPreference("pref_color_status_bar");
        Preference pref_color_background = findPreference("pref_color_background");
        Preference pref_import = findPreference("pref_import");
        Preference pref_export = findPreference("pref_export");
        Preference reset_pref = findPreference("reset_pref");
        if( !sharedpreferences.getBoolean("use_custom_theme", false)){
            preferenceScreen.removePreference(theme_link_color);
            preferenceScreen.removePreference(theme_boost_header_color);
            preferenceScreen.removePreference(theme_statuses_color);
            preferenceScreen.removePreference(theme_icons_color);
            preferenceScreen.removePreference(theme_text_color);
            preferenceScreen.removePreference(theme_primary);
            preferenceScreen.removePreference(theme_accent);
            preferenceScreen.removePreference(pref_color_navigation_bar);
            preferenceScreen.removePreference(pref_color_status_bar);
            preferenceScreen.removePreference(pref_color_background);
            preferenceScreen.removePreference(reset_pref);
            preferenceScreen.removePreference(pref_export);

        }else{
            preferenceScreen.removePreference(pref_import);
        }
        List<String> array = Arrays.asList(getResources().getStringArray(R.array.settings_theme));
        CharSequence[] entries = array.toArray(new CharSequence[array.size()]);
        CharSequence[] entryValues = new CharSequence[3];
        final SharedPreferences sharedpref = context.getSharedPreferences(Helper.APP_PREFS, Context.MODE_PRIVATE);
        int theme = sharedpref.getInt(Helper.SET_THEME, Helper.THEME_DARK);
        entryValues[0] = String.valueOf(Helper.THEME_LIGHT);
        entryValues[1] = String.valueOf(Helper.THEME_DARK);
        entryValues[2] = String.valueOf(Helper.THEME_BLACK);
        pref_theme_picker.setEntries(entries);
        pref_theme_picker.setEntryValues(entryValues);
        pref_theme_picker.setDefaultValue(String.valueOf(theme));


        pref_export.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                exportColors();
                return true;
            }
        });


        pref_import.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            TootActivity.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    return true;
                }
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    intent.setType("*/*");
                    String[] mimetypes = {"*/*"};
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                    startActivityForResult(intent, PICK_IMPORT_THEME);
                } else {
                    intent.setType("*/*");
                    Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    Intent chooserIntent = Intent.createChooser(intent, getString(R.string.toot_select_import));
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});
                    startActivityForResult(chooserIntent, PICK_IMPORT_THEME);
                }
                return true;
            }
        });

        reset_pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                dialogBuilder.setMessage(R.string.reset_color);
                dialogBuilder.setPositiveButton(R.string.reset, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        reset();
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

    private void reset(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("theme_boost_header_color");
        editor.remove("theme_statuses_color");
        editor.remove("theme_link_color");
        editor.remove("theme_icons_color");
        editor.remove("pref_color_background");
        editor.remove("pref_color_status_bar");
        editor.remove("pref_color_navigation_bar");
        editor.remove("theme_accent");
        editor.remove("theme_text_color");
        editor.remove("theme_primary");
        editor.commit();
    }



    private void exportColors() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        try {
            String fileName = "Fedilab_color_export_" + Helper.dateFileToString(getActivity(), new Date()) + ".csv";
            String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
            String fullPath = filePath + "/" + fileName;
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(fullPath)), "UTF-8"));
            StringBuilder builder = new StringBuilder();
            builder.append("type").append(',');
            builder.append("value").append(',');
            builder.append('\n');
            int theme_boost_header_color = prefs.getInt("theme_boost_header_color", -1);
            int theme_statuses_color = prefs.getInt("theme_statuses_color", -1);
            int theme_link_color = prefs.getInt("theme_link_color", -1);
            int theme_icons_color = prefs.getInt("theme_icons_color", -1);
            int pref_color_background = prefs.getInt("pref_color_background", -1);
            boolean pref_color_navigation_bar = prefs.getBoolean("pref_color_navigation_bar", true);
            boolean pref_color_status_bar = prefs.getBoolean("pref_color_status_bar", true);
            int theme_accent = prefs.getInt("theme_accent", -1);
            int theme_text_color = prefs.getInt("theme_text_color", -1);
            int theme_primary = prefs.getInt("theme_primary", -1);
            SharedPreferences sharedpreferences = getActivity().getSharedPreferences(Helper.APP_PREFS, MODE_PRIVATE);
            int theme = sharedpreferences.getInt(Helper.SET_THEME, Helper.THEME_DARK);


            builder.append("base_theme").append(',');
            builder.append(theme).append(',');
            builder.append('\n');

            builder.append("theme_boost_header_color").append(',');
            builder.append(theme_boost_header_color).append(',');
            builder.append('\n');

            builder.append("theme_statuses_color").append(',');
            builder.append(theme_statuses_color).append(',');
            builder.append('\n');

            builder.append("theme_link_color").append(',');
            builder.append(theme_link_color).append(',');
            builder.append('\n');

            builder.append("theme_icons_color").append(',');
            builder.append(theme_icons_color).append(',');
            builder.append('\n');

            builder.append("pref_color_background").append(',');
            builder.append(pref_color_background).append(',');
            builder.append('\n');

            builder.append("pref_color_navigation_bar").append(',');
            builder.append(pref_color_navigation_bar).append(',');
            builder.append('\n');

            builder.append("pref_color_status_bar").append(',');
            builder.append(pref_color_status_bar).append(',');
            builder.append('\n');

            builder.append("theme_accent").append(',');
            builder.append(theme_accent).append(',');
            builder.append('\n');

            builder.append("theme_text_color").append(',');
            builder.append(theme_text_color).append(',');
            builder.append('\n');

            builder.append("theme_primary").append(',');
            builder.append(theme_primary).append(',');
            builder.append('\n');


            pw.write(builder.toString());
            pw.close();
            String message = getString(R.string.data_export_theme_success);
            Intent intentOpen = new Intent();
            intentOpen.setAction(android.content.Intent.ACTION_VIEW);
            Uri uri = Uri.parse("file://" + fullPath);
            intentOpen.setDataAndType(uri, "text/csv");
            String title = getString(R.string.data_export_theme);
            String userId = sharedpreferences.getString(Helper.PREF_KEY_ID, null);
            String instance = sharedpreferences.getString(Helper.PREF_INSTANCE, Helper.getLiveInstance(getActivity()));
            SQLiteDatabase db = Sqlite.getInstance(getActivity(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
            Account account = new AccountDAO(getActivity(), db).getUniqAccount(userId, instance);
            Helper.notify_user(getActivity(), account, intentOpen, BitmapFactory.decodeResource(getResources(),
                    Helper.getMainLogo(getActivity())), Helper.NotifType.BACKUP, title, message);
        } catch (Exception e) {
            e.printStackTrace();
            Toasty.error(getActivity(), getString(R.string.toast_error), Toast.LENGTH_LONG).show();
        }
    }
}