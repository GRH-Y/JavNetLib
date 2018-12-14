package connect.network.nio;

import connect.network.base.joggle.ISSLFactory;

import javax.net.ServerSocketFactory;
import javax.net.ssl.*;
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

    @Override
    public SSLSocketFactory getSSLSocketFactory() {
        return sslContext.getSocketFactory();
    }

    @Override
    public ServerSocketFactory getSSLServerSocketFactory() {
        return null;
    }

    @Override
    public HostnameVerifier getHostnameVerifier() {
        return (string, sslSession) -> true;
    }

    private X509TrustManager x509m = new X509TrustManager() {

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }
    };

}
