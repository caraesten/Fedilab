package fr.gouv.etalab.mastodon.client;

import android.content.Context;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.annotation.GlideModule;

import java.io.InputStream;

import fr.gouv.etalab.mastodon.helper.Helper;

/**
 * Created by Thomas on 13/12/2017.
 */

@GlideModule
public final class TLSLibraryGlideModule extends AppGlideModule {

    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        registry.replace(GlideUrl.class, InputStream.class, new HttpsUrlLoader.Factory());
    }

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {

    }
}