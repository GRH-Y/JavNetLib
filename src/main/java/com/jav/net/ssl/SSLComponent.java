package com.jav.net.ssl;

import com.jav.common.log.LogDog;
import com.jav.net.base.joggle.ISSLComponent;

import javax.net.ServerSocketFactory;
import javax.net.ssl.*;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class SSLComponent implements ISSLComponent {
    private final SSLContext mSSLContext;

    public SSLComponent() {
        try {
            mSSLContext = SSLContext.getDefault();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * TLS
     *
     * @param protocol TLSv1.2
     * @throws Exception
     */
    public SSLComponent(String protocol) throws Exception {
        mSSLContext = SSLContext.getInstance(protocol);
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        mSSLContext.init(null, null, new SecureRandom());
    }

    /**
     * @param protocol          "TLS"
     * @param keyManagerFactory "SunX509"
     * @param keyStoreType      "JKS"
     * @param keyStorePath      "C:\Program Files\Java\jdk1.8.0_161\jre\lib\security"
     * @param keyPassword       "changeit"
     */
    public SSLComponent(String protocol, String keyManagerFactory, String keyStoreType, String keyStorePath, String keyPassword) throws Exception {
        char[] password = keyPassword.toCharArray();

        mSSLContext = SSLContext.getInstance(protocol);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(keyManagerFactory);
//        TrustManagerFactory tmf = TrustManagerFactory.getInstance(keyManagerFactory);
        KeyStore ks = KeyStore.getInstance(keyStoreType);
//        KeyStore tks = KeyStore.getInstance(keyStoreType);

        ks.load(new FileInputStream(keyStorePath), password);
        kmf.init(ks, password);
//        tmf.init(tks);
        mSSLContext.init(kmf.getKeyManagers(), null, null);//tmf.getTrustManagers()
    }

    @Override
    public SSLContext getSSLContext() {
        return mSSLContext;
    }

    @Override
    public SSLSocketFactory getSSLSocketFactory() {
        return mSSLContext.getSocketFactory();
    }

    @Override
    public ServerSocketFactory getSSLServerSocketFactory() {
        return mSSLContext.getServerSocketFactory();
    }

    @Override
    public HostnameVerifier getHostnameVerifier() {
        return (string, sslSession) -> true;
    }

    private final X509TrustManager x509m = new X509TrustManager() {

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            LogDog.d("==> NioSSLFactory X509TrustManager getAcceptedIssuers !!!");
            return null;
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
            LogDog.d("==> NioSSLFactory X509TrustManager checkServerTrusted authType = " + authType);
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
            LogDog.d("==> NioSSLFactory X509TrustManager checkClientTrusted authType = " + authType);
        }
    };

}