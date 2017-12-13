package fr.gouv.etalab.mastodon.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.conscrypt.Conscrypt;

import java.security.Security;

/**
 * Created by Thomas on 12/12/2017.
 */

public class BaseActivity extends AppCompatActivity {
    static {
        Security.insertProviderAt(Conscrypt.newProvider("GmsCore_OpenSSL"), 2);
        Security.addProvider(Conscrypt.newProvider());

    }

}
