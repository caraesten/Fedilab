package fr.gouv.etalab.mastodon.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import org.conscrypt.Conscrypt;

import java.security.Security;

/**
 * Created by Thomas on 13/12/2017.
 */

public class BaseService extends IntentService {

    static {
        Security.insertProviderAt(Conscrypt.newProvider("GmsCore_OpenSSL"), 2);
        Security.addProvider(Conscrypt.newProvider());

    }

    public BaseService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }
}
