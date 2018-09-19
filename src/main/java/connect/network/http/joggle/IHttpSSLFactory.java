package connect.network.http.joggle;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

public interface IHttpSSLFactory {
    
    SSLSocketFactory getSSLSocketFactory();

    HostnameVerifier getHostnameVerifier();
}
