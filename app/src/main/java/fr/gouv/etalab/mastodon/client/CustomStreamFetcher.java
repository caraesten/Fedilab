package fr.gouv.etalab.mastodon.client;

import android.support.annotation.NonNull;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.GlideUrl;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import fr.gouv.etalab.mastodon.helper.Helper;

/**
 * Created by Thomas on 13/12/2017.
 */

public class CustomStreamFetcher implements DataFetcher<InputStream> {

    private GlideUrl url;

    public CustomStreamFetcher(GlideUrl url) {
        this.url = url;
    }

    @Override
    public void loadData(Priority priority, DataCallback<? super InputStream> callback) {
        new HttpsConnection().getPicture(url.toStringUrl());
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void cancel() {

    }

    @NonNull
    @Override
    public Class<InputStream> getDataClass() {
        return InputStream.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        return DataSource.REMOTE;
    }
}
