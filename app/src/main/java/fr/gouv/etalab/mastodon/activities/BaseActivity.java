package fr.gouv.etalab.mastodon.activities;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;

import org.conscrypt.Conscrypt;

import java.security.Security;

/**
 * Created by Thomas on 12/12/2017.
 * Base activity which updates security provider
 */

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {
    static {
        Security.insertProviderAt(Conscrypt.newProvider("GmsCore_OpenSSL"), 2);
        Security.addProvider(Conscrypt.newProvider());

    }

}
