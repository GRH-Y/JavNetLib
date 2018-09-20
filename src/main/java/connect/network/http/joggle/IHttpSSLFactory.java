package connect.network.http.joggle;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public interface IHttpSSLFactory {
    
    SSLSocketFactory getSSLSocketFactory() throws CertificateException, NoSuchAlgorithmException, KeyManagementException;

    HostnameVerifier getHostnameVerifier();
}
