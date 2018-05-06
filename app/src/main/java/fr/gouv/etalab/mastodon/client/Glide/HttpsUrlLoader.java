package fr.gouv.etalab.mastodon.client.Glide;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;

import java.io.InputStream;
import java.lang.ref.WeakReference;



/**
 * Created by Thomas on 13/12/2017.
 * Custom UrlLoader
 */

public class HttpsUrlLoader implements ModelLoader<GlideUrl, InputStream> {

    private static WeakReference<Context> contextWeakReference;

    HttpsUrlLoader(Context context) {
        contextWeakReference = new WeakReference<>(context);}

    @Nullable
    @Override
    public LoadData<InputStream> buildLoadData(@NonNull GlideUrl glideUrl, int width, int height, @NonNull Options options) {
        return new LoadData<>(glideUrl, new CustomStreamFetcher(contextWeakReference.get(), glideUrl));
    }

    @Override
    public boolean handles(@NonNull GlideUrl glideUrl) {
        return true;
    }


    public static class Factory implements ModelLoaderFactory<GlideUrl, InputStream> {

        Factory(Context context){
            contextWeakReference = new WeakReference<>(context);
        }
        @NonNull
        @Override
        public ModelLoader<GlideUrl, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new HttpsUrlLoader(contextWeakReference.get());
        }
        @Override
        public void teardown() {

        }
    }
}
