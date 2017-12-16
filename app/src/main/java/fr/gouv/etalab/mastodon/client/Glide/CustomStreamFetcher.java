package fr.gouv.etalab.mastodon.client.Glide;

import android.support.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.GlideUrl;

import java.io.InputStream;

import fr.gouv.etalab.mastodon.client.HttpsConnection;

/**
 * Created by Thomas on 13/12/2017.
 * Custom stream fetcher which will use getPicture from HttpsConnection to get the inputstream
 */

public class CustomStreamFetcher implements DataFetcher<InputStream> {

    private GlideUrl url;

    CustomStreamFetcher(GlideUrl url) {
        this.url = url;
    }

    @Override
    public void loadData(Priority priority, DataCallback<? super InputStream> callback) {
        callback.onDataReady(new HttpsConnection().getPicture(url.toStringUrl()));
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
