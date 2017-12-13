package fr.gouv.etalab.mastodon.activities;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.conscrypt.Conscrypt;

import java.security.Provider;
import java.security.Security;

import fr.gouv.etalab.mastodon.helper.Helper;

/**
 * Created by Thomas on 12/12/2017.
 * Base activity which updates security provider
 */

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {
    static {
        boolean isGmsCore_OpenSSL = false;
        Provider[] providers = Security.getProviders();
        for(Provider provider: providers){
            if( provider.getName().equals("GmsCore_OpenSSL"))
                isGmsCore_OpenSSL = true;
        }
        if( !isGmsCore_OpenSSL)
            Security.addProvider(Security.getProvider("GmsCore_OpenSSL"));
        Security.insertProviderAt(Conscrypt.newProvider("GmsCore_OpenSSL"), 1);
    }

}
