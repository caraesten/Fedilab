package fr.gouv.etalab.mastodon.client;

import android.annotation.SuppressLint;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by Thomas on 29/08/2017.
 *
 */

public class TLSSocketFactory extends SSLSocketFactory {

    private final SSLContext sslContext = SSLContext.getInstance("TLS");

    public TLSSocketFactory() throws KeyManagementException, NoSuchAlgorithmException {

        X509TrustManager tm = new X509TrustManager() {
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
        sslContext.init(null, new TrustManager[]{tm}, null);

    }

    @Override
    public String[] getDefaultCipherSuites() {
        return sslContext.getSocketFactory().getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return sslContext.getSocketFactory().getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket() throws IOException {
        return enableTLSOnSocket(sslContext.getSocketFactory().createSocket());
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return enableTLSOnSocket(sslContext.getSocketFactory().createSocket(s, host, port, autoClose));
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return enableTLSOnSocket(sslContext.getSocketFactory().createSocket(host, port));
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
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

    private Socket enableTLSOnSocket(Socket socket) {
        if(socket != null && (socket instanceof SSLSocket)) {
            ((SSLSocket)socket).setEnabledProtocols(new String[] {"TLSv1.1", "TLSv1.2"});
        }
        return socket;
    }
}