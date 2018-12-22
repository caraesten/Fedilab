package fr.gouv.etalab.mastodon.activities;
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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.support.v4.content.ContextCompat;

import com.evernote.android.job.JobManager;
import com.franmontiel.localechanger.LocaleChanger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;
import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.jobs.ApplicationJob;
import fr.gouv.etalab.mastodon.jobs.NotificationsSyncJob;

/**
 * Created by Thomas on 29/04/2017.
 * Main application, jobs are launched here.
 */


public class MainApplication extends MultiDexApplication {


    @Override
    public void onCreate() {
        super.onCreate();
        JobManager.create(this).addJobCreator(new ApplicationJob());
        NotificationsSyncJob.schedule(false);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        try {
            List<Locale> SUPPORTED_LOCALES = new ArrayList<>();
            SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
            String defaultLocaleString = sharedpreferences.getString(Helper.SET_DEFAULT_LOCALE_NEW, null);
            if( defaultLocaleString != null){
                Locale defaultLocale;
                if( defaultLocaleString.equals("zh-CN"))
                    defaultLocale = Locale.SIMPLIFIED_CHINESE;
                else if( defaultLocaleString.equals("zh-TW"))
                    defaultLocale = Locale.TRADITIONAL_CHINESE;
                else
                    defaultLocale = new Locale(defaultLocaleString);
                SUPPORTED_LOCALES.add(defaultLocale);
            }else {
                SUPPORTED_LOCALES.add(Locale.getDefault());
            }
            LocaleChanger.initialize(getApplicationContext(), SUPPORTED_LOCALES);
        }catch (Exception ignored){ignored.printStackTrace();}

        Toasty.Config.getInstance()
                .setErrorColor(ContextCompat.getColor(getApplicationContext(), R.color.toasty_background))
                .setInfoColor(ContextCompat.getColor(getApplicationContext(), R.color.toasty_background))
                .setSuccessColor(ContextCompat.getColor(getApplicationContext(), R.color.toasty_background))
                .setWarningColor(ContextCompat.getColor(getApplicationContext(), R.color.toasty_background))
                .setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.toasty_text))
                .apply();
                    Toasty.Config.getInstance().apply();
                }


    @Override
    protected void attachBaseContext(Context base)
    {
        super.attachBaseContext(base);
        MultiDex.install(MainApplication.this);
    }
}
