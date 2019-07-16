package connect.network.http.ssl;


import connect.network.base.joggle.ISSLFactory;
import util.StringEnvoy;

import javax.net.ServerSocketFactory;
import javax.net.ssl.*;

public class DefaultHttpSSLFactory implements ISSLFactory, HostnameVerifier {

    protected String hostname;

    public DefaultHttpSSLFactory() {
    }

    public DefaultHttpSSLFactory(String hostname) {
        if (StringEnvoy.isEmpty(hostname)) {
            throw new NullPointerException("hostname is null !!!");
        }
        this.hostname = hostname.replace("http://", "").replace("https://", "")
                .replace("/", "");
    }

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
        return this;
    }

    @Override
    public boolean verify(String hostname, SSLSession sslSession) {
        if (StringEnvoy.isEmpty(this.hostname)) {
            return true;
        } else {
            if (this.hostname.equals(hostname)) {
                return true;
            } else {
                HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
                return hv.verify(hostname, sslSession);
            }
        }
    }

}
