package connect.network.http;


import connect.network.http.joggle.IHttpSSLFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

public class HttpSSLFactory implements IHttpSSLFactory {

    @Override
    public SSLSocketFactory getSSLSocketFactory() {
        return (SSLSocketFactory) SSLSocketFactory.getDefault();
    }

    @Override
    public HostnameVerifier getHostnameVerifier() {
        return (string, sslSession) -> string.equals(sslSession.getPeerHost());
    }

}
