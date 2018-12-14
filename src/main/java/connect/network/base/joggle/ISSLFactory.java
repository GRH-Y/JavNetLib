package connect.network.base.joggle;

import javax.net.ServerSocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

public interface ISSLFactory {
    
    SSLSocketFactory getSSLSocketFactory();

    ServerSocketFactory getSSLServerSocketFactory();

    HostnameVerifier getHostnameVerifier();
}
