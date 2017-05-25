package fr.gouv.etalab.mastodon.client;

import android.content.Context;

import com.nostra13.universalimageloader.core.assist.ContentLengthInputStream;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by Thomas on 21/05/2017.
 * Patch for universal image loader to support TLS 1.1+
 */

public class PatchBaseImageDownloader extends BaseImageDownloader {

    private SSLSocketFactory sf;

    public PatchBaseImageDownloader(Context context) {
        super(context);

        initSSLSocketFactory();
    }

    private void initSSLSocketFactory() {
        sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
        try {
            sf = new MySSLSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public InputStream getStreamFromNetwork(String imageUri, Object extra) throws IOException {
        HttpURLConnection conn = createConnection(imageUri, extra);
        if (conn instanceof HttpsURLConnection) {
            ((HttpsURLConnection) conn).setSSLSocketFactory(sf);
        }
        return new ContentLengthInputStream(new BufferedInputStream(conn.getInputStream(), BUFFER_SIZE), conn.getContentLength());
    }

    private static class MySSLSocketFactory extends SSLSocketFactory {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        MySSLSocketFactory() throws Exception {
            super();
            TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            sslContext.init(null, new TrustManager[]{tm}, null);
        }

        @Override
        public String[] getDefaultCipherSuites() {
            return new String[0];
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return new String[0];
        }


        @Override
        public Socket createSocket(String host, int port) throws IOException {
            return enableTLSOnSocket(sslContext.getSocketFactory().createSocket(host, port));
        }

        @Override
        public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
            return enableTLSOnSocket(sslContext.getSocketFactory().createSocket(host, port, localHost, localPort));
        }

        @Override
        public Socket createSocket(InetAddress host, int port) throws IOException {
            return enableTLSOnSocket(sslContext.getSocketFactory().createSocket(host, port));
        }

        @Override
        public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
            return enableTLSOnSocket(sslContext.getSocketFactory().createSocket(address, port, localAddress, localPort));
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
            return enableTLSOnSocket(sslContext.getSocketFactory().createSocket(socket, host, port, autoClose));
        }

        @Override
        public Socket createSocket() throws IOException {
            return enableTLSOnSocket(sslContext.getSocketFactory().createSocket());
        }

        private Socket enableTLSOnSocket(Socket socket) {
            if(socket != null && (socket instanceof SSLSocket)) {
                ((SSLSocket)socket).setEnabledProtocols(new String[] {"TLSv1.1", "TLSv1.2"});
            }
            return socket;
        }

    }

}