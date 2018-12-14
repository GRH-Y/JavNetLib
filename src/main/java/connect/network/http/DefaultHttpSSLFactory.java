package connect.network.http;


import connect.network.base.joggle.ISSLFactory;

import javax.net.ServerSocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;

public class DefaultHttpSSLFactory implements ISSLFactory {


    @Override
    public SSLSocketFactory getSSLSocketFactory() {
        return (SSLSocketFactory) SSLSocketFactory.getDefault();
    }

    @Override
    public ServerSocketFactory getSSLServerSocketFactory() {
        return SSLServerSocketFactory.getDefault();
    }


    @Override
    public HostnameVerifier getHostnameVerifier() {
//        return (string, sslSession) -> string.equals(sslSession.getPeerHost());
        return (string, sslSession) -> true;
    }

}
