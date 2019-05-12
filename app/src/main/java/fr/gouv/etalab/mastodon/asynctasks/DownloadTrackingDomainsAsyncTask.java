/* Copyright 2019 NickFreeman
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
package fr.gouv.etalab.mastodon.asynctasks;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import es.dmoral.toasty.Toasty;
import fr.gouv.etalab.mastodon.R;
import fr.gouv.etalab.mastodon.sqlite.DomainBlockDAO;
import fr.gouv.etalab.mastodon.sqlite.Sqlite;

/**
 * Created by NickFreeman on 11/05/2019.
 * Download the list of blocked tracking domains for build-in browser
 */

public class DownloadTrackingDomainsAsyncTask extends AsyncTask<Void, Void, Boolean> {

    private WeakReference<Context> context;
    private WeakReference<Button> update_tracking_domains;

    public DownloadTrackingDomainsAsyncTask(Context context, Button update_tracking_domains) {
        this.context = new WeakReference<>(context);
        this.update_tracking_domains = new WeakReference<>(update_tracking_domains);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URL("https://sebsauvage.net/hosts/hosts").openConnection();
            if (connection.getResponseCode() != 200)
                return false;
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            List<String> domains = new ArrayList<>();
            boolean canRecord = false;
            while ((line = br.readLine()) != null) {
                if (canRecord)
                    domains.add(line.replace("0.0.0.0 ", "").trim());
                else if (line.contains("# Blocked domains"))
                    canRecord = true;
            }
            br.close();
            connection.disconnect();

            SQLiteDatabase db = Sqlite.getInstance(context.get(), Sqlite.DB_NAME, null, Sqlite.DB_VERSION).open();
            new DomainBlockDAO(context.get(), db).set(domains);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPreExecute() {
        update_tracking_domains.get().setEnabled(false);
    }

    @Override
    protected void onPostExecute(Boolean success) {
        // Get a handler that can be used to post to the main thread
        Handler mainHandler = new Handler(context.get().getMainLooper());
        if (success)
            mainHandler.post(() -> Toasty.success(context.get(), context.get().getString(R.string.tracking_db_updated), Toast.LENGTH_LONG).show());
        else
            mainHandler.post(() -> Toasty.error(context.get(), context.get().getString(R.string.toast_error), Toast.LENGTH_LONG).show());
        update_tracking_domains.get().setEnabled(true);
    }
}