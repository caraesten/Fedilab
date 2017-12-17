package fr.gouv.etalab.mastodon.services;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;


import fr.gouv.etalab.mastodon.helper.Helper;

/**
 * Created by Thomas on 13/12/2017.
 * BaseService class to install provider
 */

@SuppressLint("Registered")
public class BaseService extends IntentService {

    static {
        Helper.installProvider();
    }

    public BaseService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }
}
