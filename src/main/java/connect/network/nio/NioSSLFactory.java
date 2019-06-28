package connect.network.nio;

import connect.network.base.joggle.ISSLFactory;
import util.LogDog;

import javax.net.ServerSocketFactory;
import javax.net.ssl.*;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

public class NioSSLFactory implements ISSLFactory {
    private SSLContext sslContext = null;

    public NioSSLFactory(String protocol) {
        try {
            sslContext = SSLContext.getInstance(protocol);
            sslContext.init(null, new TrustManager[]{x509m}, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param protocol          "TLS"
     * @param keyManagerFactory "SunX509"
     * @param keyStoreType      "JKS"
     * @param keyStorePath      "C:\Program Files\Java\jdk1.8.0_161\jre\lib\security"
     * @param keyPassword       "changeit"
     */
    public NioSSLFactory(String protocol, String keyManagerFactory, String keyStoreType, String keyStorePath, String keyPassword) {
        try {
            char[] passphrase = keyPassword.toCharArray();

            sslContext = SSLContext.getInstance(protocol);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(keyManagerFactory);
            KeyStore ks = KeyStore.getInstance(keyStoreType);

            ks.load(new FileInputStream(keyStorePath), passphrase);
            kmf.init(ks, passphrase);
            sslContext.init(kmf.getKeyManagers(), null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public SSLSocketFactory getSSLSocketFactory() {
        return sslContext.getSocketFactory();
    }

    @Override
    public ServerSocketFactory getSSLServerSocketFactory() {
        return SSLServerSocketFactory.getDefault();
    }

    @Override
    public HostnameVerifier getHostnameVerifier() {
        return (string, sslSession) -> true;
    }

    private X509TrustManager x509m = new X509TrustManager() {

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
