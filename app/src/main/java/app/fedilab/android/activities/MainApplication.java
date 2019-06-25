package app.fedilab.android.activities;
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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;
import androidx.core.content.ContextCompat;

import com.evernote.android.job.JobManager;
import com.franmontiel.localechanger.LocaleChanger;

import net.gotev.uploadservice.UploadService;

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import app.fedilab.android.helper.Helper;
import app.fedilab.android.jobs.ApplicationJob;
import app.fedilab.android.jobs.NotificationsSyncJob;
import es.dmoral.toasty.Toasty;
import app.fedilab.android.BuildConfig;
import app.fedilab.android.R;
import info.guardianproject.netcipher.NetCipher;
import info.guardianproject.netcipher.proxy.OrbotHelper;

import static app.fedilab.android.helper.Helper.initNetCipher;

/**
 * Created by Thomas on 29/04/2017.
 * Main application, jobs are launched here.
 */


public class MainApplication extends MultiDexApplication {


    private static MainApplication app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        //System.setProperty("java.net.preferIPv4Stack" , "true");
        JobManager.create(this).addJobCreator(new ApplicationJob());
        NotificationsSyncJob.schedule(false);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        SharedPreferences sharedpreferences = getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
        try {
            List<Locale> SUPPORTED_LOCALES = new ArrayList<>();

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
        }catch (Exception ignored){}
        //Initialize upload service
        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;

        boolean proxyEnabled = sharedpreferences.getBoolean(Helper.SET_PROXY_ENABLED, false);
        int type = sharedpreferences.getInt(Helper.SET_PROXY_TYPE, 0);
        if( proxyEnabled){
            String host = sharedpreferences.getString(Helper.SET_PROXY_HOST, "127.0.0.1");
            int port = sharedpreferences.getInt(Helper.SET_PROXY_PORT, 8118);
            SocketAddress sa = new InetSocketAddress(host, port);
            if( type == 0 ) {
                NetCipher.setProxy(new Proxy(Proxy.Type.HTTP, sa));
            }else{
                NetCipher.setProxy(new Proxy(Proxy.Type.SOCKS, sa));
            }
            final String login = sharedpreferences.getString(Helper.SET_PROXY_LOGIN, null);
            final String pwd = sharedpreferences.getString(Helper.SET_PROXY_PASSWORD, null);
            if( login != null) {
                Authenticator authenticator = new Authenticator() {
                    public PasswordAuthentication getPasswordAuthentication() {
                        assert pwd != null;
                        return (new PasswordAuthentication(login,
                                pwd.toCharArray()));
                    }
                };
                Authenticator.setDefault(authenticator);
            }
        }
        NetCipher.useGlobalProxy();
        if( OrbotHelper.isOrbotInstalled(getApplicationContext()) && OrbotHelper.isOrbotRunning(getApplicationContext())){
            NetCipher.useTor();
        }
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

    public static MainApplication getApp(){
        return app;
    }
}
