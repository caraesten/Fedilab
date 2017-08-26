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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.IOException;
import fr.gouv.etalab.mastodon.interfaces.OnRetrieveRemoteAccountInterface;


/**
 * Created by Thomas on 22/07/2017.
 * Retrieves a remote account via its webpage
 */

public class RetrieveRemoteAccountsAsyncTask extends AsyncTask<Void, Void, Void> {

    private OnRetrieveRemoteAccountInterface listener;
    private String url;
    private String avatar, name, username, bio;
    private int statusCount, followingCount, followersCount;
    private boolean islocked;
    private boolean error = false;
    private String instance;

    public RetrieveRemoteAccountsAsyncTask(String username, String instance, OnRetrieveRemoteAccountInterface onRetrieveRemoteAccountInterface){
        this.url = "https://" + instance  + "/@" + username;
        this.listener = onRetrieveRemoteAccountInterface;
        this.instance = instance;
    }



    @Override
    protected Void doInBackground(Void... params) {
        String userAgent = "Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1667.0 Safari/537.36";
        try {
            Document document = Jsoup.connect(url).userAgent(userAgent).get();
            Elements avatarElement = document.getElementsByClass("avatar");
            avatar = avatarElement.get(0).getElementsByClass("u-photo").get(0).attr("src");
            avatar = "https://" + instance + avatar;
            Elements nameElement = document.getElementsByClass("name");
            name = nameElement.get(0).getElementsByClass("p-name").get(0).html();
            username = nameElement.get(0).getElementsByTag("span").get(1).html();
            islocked = nameElement.get(0).getElementsByClass("fa-lock") != null;

            Elements bioElement = document.getElementsByClass("bio");
            bio = bioElement.get(0).html();
            Elements countElement = document.getElementsByClass("counter-number");
            statusCount = Integer.parseInt(countElement.get(0).html());
            followingCount = Integer.parseInt(countElement.get(1).html());
            followersCount = Integer.parseInt(countElement.get(2).html());
        } catch (IOException | IndexOutOfBoundsException e) {
            error = true;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        listener.onRetrieveRemoteAccount(error, name, username, islocked, avatar, bio, statusCount, followingCount, followersCount);
    }

}
