package app.fedilab.android.client;


import android.annotation.SuppressLint;
import android.content.SharedPreferences;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

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
 *
 */

public class TLSSocketFactory extends SSLSocketFactory {

    private SSLSocketFactory sSLSocketFactory;
    private SSLContext sslContext;
    private String instance;

    public TLSSocketFactory(String instance) throws KeyManagementException, NoSuchAlgorithmException {

        sslContext = SSLContext.getInstance("TLS");
        if( instance == null || !instance.endsWith(".onion")) {
            sslContext.init(null, null, null);
        }else{
            TrustManager tm = new X509TrustManager() {
                @SuppressLint("TrustAllX509TrustManager")
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @SuppressLint("TrustAllX509TrustManager")
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            sslContext.init(null, new TrustManager[] { tm }, null);
        }
        sSLSocketFactory = sslContext.getSocketFactory();
    }

    public SSLContext getSSLContext(){
        return this.sslContext;
    }

    public SSLEngine getSSLEngine(){
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
        if((socket instanceof SSLSocket)) {
            boolean security_provider = false;
            try {
                SharedPreferences sharedpreferences = MainApplication.getApp().getSharedPreferences(Helper.APP_PREFS, android.content.Context.MODE_PRIVATE);
                security_provider = sharedpreferences.getBoolean(Helper.SET_SECURITY_PROVIDER, true);
            }catch (Exception ignored){}
            if( security_provider)
                ((SSLSocket)socket).setEnabledProtocols(new String[] {"TLSv1.1", "TLSv1.2", "TLSv1.3"});
            else
                ((SSLSocket)socket).setEnabledProtocols(new String[] {"TLSv1.1", "TLSv1.2"});
        }
        return socket;
    }
}