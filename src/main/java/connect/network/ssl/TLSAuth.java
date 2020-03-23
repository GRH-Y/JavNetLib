package connect.network.ssl;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.security.KeyStore;

public class TLSAuth {

    public static SSLContext getSSLContext(String protocol, KeyManager[] kms, TrustManager[] tms) throws Exception {
        SSLContext sslContext = SSLContext.getInstance(protocol);
        sslContext.init(kms, tms, null);
        return sslContext;
    }

    public static TrustManager[] createrTrustManager(String clientTrustCerFile, String clientTrustCerPwd) throws Exception {
        //Trust Key Store
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream(clientTrustCerFile), clientTrustCerPwd.toCharArray());

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
        trustManagerFactory.init(keyStore);
        return trustManagerFactory.getTrustManagers();
    }

    public static KeyManager[] createrKeyManager(String clientCerFile, String clientCerPwd, String clientKeyPwd) throws Exception {
        //Trust Key Store
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream(clientCerFile), clientCerPwd.toCharArray());

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        keyManagerFactory.init(keyStore, clientKeyPwd.toCharArray());
        return keyManagerFactory.getKeyManagers();
    }
}
