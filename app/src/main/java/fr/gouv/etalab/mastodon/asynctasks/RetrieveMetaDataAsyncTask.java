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
package fr.gouv.etalab.mastodon.asynctasks;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Patterns;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.regex.Matcher;

import fr.gouv.etalab.mastodon.helper.Helper;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveMetaDataInterface;


/**
 * Created by Thomas on 02/09/2017.
 * Retrieves metadata of a remote page
 */

public class RetrieveMetaDataAsyncTask extends AsyncTask<Void, Void, Void> {

    private OnRetrieveMetaDataInterface listener;
    private String url;
    private boolean error = false;
    private String image, title, description;

    public RetrieveMetaDataAsyncTask(String url, OnRetrieveMetaDataInterface onRetrieveRemoteAccountInterface){
        this.url = url;
        this.listener = onRetrieveRemoteAccountInterface;
    }

    @Override
    protected Void doInBackground(Void... params) {
        String userAgent = "Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1667.0 Safari/537.36";
        String potentialUrl = new String();
        try {
            Matcher matcher;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT)
                matcher = Patterns.WEB_URL.matcher(url);
            else
                matcher = Helper.urlPattern.matcher(url);
            while (matcher.find()){
                int matchStart = matcher.start(1);
                int matchEnd = matcher.end();
                if(matchStart < matchEnd && url.length() >= matchEnd)
                    potentialUrl = url.substring(matchStart, matchEnd);
            }
            // If we actually have a URL then make use of it.
            if (potentialUrl.length() > 0) {
                Document document = Jsoup.connect(potentialUrl).userAgent(userAgent).get();
                Elements metaOgTitle = document.select("meta[property=og:title]");
                if (metaOgTitle != null) {
                    title = metaOgTitle.attr("content");
                } else {
                    title = document.title();
                }
                Elements metaOgDescription = document.select("meta[property=og:description]");
                if (metaOgDescription != null) {
                    description = metaOgDescription.attr("content");
                } else {
                    description = "";
                }
                Elements metaOgImage = document.select("meta[property=og:image]");
                if (metaOgImage != null) {
                    image = metaOgImage.attr("content");
                }
            }
        } catch (IOException | IndexOutOfBoundsException e) {
            error = true;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveMetaData(error, image, title, description);
    }

}
