package fr.gouv.etalab.mastodon.client;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.load.model.stream.UrlLoader;

import java.io.InputStream;

import fr.gouv.etalab.mastodon.helper.Helper;


/**
 * Created by Thomas on 13/12/2017.
 */

public class HttpsUrlLoader implements ModelLoader<GlideUrl, InputStream> {



    @SuppressWarnings("WeakerAccess")
    public HttpsUrlLoader() {
    }

    @Nullable
    @Override
    public LoadData<InputStream> buildLoadData(GlideUrl glideUrl, int width, int height, Options options) {
        return new LoadData<>(glideUrl, new CustomStreamFetcher(glideUrl));
    }

    @Override
    public boolean handles(GlideUrl glideUrl) {
        return true;
    }


    public static class Factory implements ModelLoaderFactory<GlideUrl, InputStream> {


        @Override
        public ModelLoader<GlideUrl, InputStream> build(MultiModelLoaderFactory multiFactory) {
            return new HttpsUrlLoader();
        }

        @Override
        public void teardown() {

        }
    }
}
