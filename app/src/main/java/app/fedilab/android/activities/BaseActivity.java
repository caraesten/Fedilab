package app.fedilab.android.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;

import com.franmontiel.localechanger.LocaleChanger;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.one.EmojiOneProvider;

import app.fedilab.android.helper.Helper;

/**
 * Created by Thomas on 12/12/2017.
 * Base activity which updates security provider
 */

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {
    static {
        Helper.installProvider();
        EmojiManager.install(new EmojiOneProvider());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleChanger.onConfigurationChanged();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        newBase = LocaleChanger.configureBaseContext(newBase);
        super.attachBaseContext(newBase);
    }
}
