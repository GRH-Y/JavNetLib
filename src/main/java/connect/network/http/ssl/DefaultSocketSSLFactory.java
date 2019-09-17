package connect.network.http.ssl;

import connect.network.base.joggle.ISSLFactory;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

public class DefaultSocketSSLFactory implements ISSLFactory {

    @Override
    public SSLSocketFactory getSSLSocketFactory() {
        return (SSLSocketFactory) SocketFactory.getDefault();
    }

    @Override
    public ServerSocketFactory getSSLServerSocketFactory() {
        return null;
    }

    @Override
    public HostnameVerifier getHostnameVerifier() {
        return null;
    }
}
