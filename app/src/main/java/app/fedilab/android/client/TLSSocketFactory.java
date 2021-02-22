package app.fedilab.android.client;


import android.content.SharedPreferences;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import app.fedilab.android.activities.MainApplication;
import app.fedilab.android.helper.Helper;

/**
 * Created by Thomas on 29/08/2017.
 */

public class TLSSocketFactory extends SSLSocketFactory {

    private SSLSocketFactory sSLSocketFactory;
    private SSLContext sslContext;
    private boolean isOnion;

    public TLSSocketFactory(String instance) throws KeyManagementException, NoSuchAlgorithmException {


        if (instance == null || !instance.endsWith(".onion") || !instance.endsWith(".i2p")  ) {
            sslContext = SSLContext.getInstance("TLS");
            isOnion = false;
            sslContext.init(null, null, null);
        } else {
            sslContext = SSLContext.getInstance("SSL");
            isOnion = true;
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
            };
            sslContext.init(null, trustAllCerts, null);

        }
        sSLSocketFactory = sslContext.getSocketFactory();

    }

    public SSLContext getSSLContext() {
        return this.sslContext;
    }

    public SSLEngine getSSLEngine() {
        return this.sslContext.createSSLEngine();
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return sSLSocketFactory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return sSLSocketFactory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket() throws IOException {
        return enableTLSOnSocket(sSLSocketFactory.createSocket());
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return enableTLSOnSocket(sSLSocketFactory.createSocket(s, host, port, autoClose));
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return enableTLSOnSocket(sSLSocketFactory.createSocket(host, port));
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
        return enableTLSOnSocket(sSLSocketFactory.createSocket(host, port, localHost, localPort));
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return enableTLSOnSocket(sSLSocketFactory.createSocket(host, port));
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return enableTLSOnSocket(sSLSocketFactory.createSocket(address, port, localAddress, localPort));
    }

    private Socket enableTLSOnSocket(Socket socket) {
        if ((socket instanceof SSLSocket)) {
            if (!isOnion) {
                boolean security_provider = false;
                try {
                    SharedPreferences sharedpreferences = MainApplication.getApp().getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
                    security_provider = sharedpreferences.getBoolean(Helper.SET_SECURITY_PROVIDER, true);
                } catch (Exception ignored) {
                }
                if (security_provider)
                    ((SSLSocket) socket).setEnabledProtocols(new String[]{"TLSv1.1", "TLSv1.2", "TLSv1.3"});
                else
                    ((SSLSocket) socket).setEnabledProtocols(new String[]{"TLSv1.1", "TLSv1.2"});
            } else {
                ((SSLSocket) socket).setEnabledProtocols(new String[]{"TLSv1.1", "TLSv1.2",});
            }
        }
        return socket;
    }
}
