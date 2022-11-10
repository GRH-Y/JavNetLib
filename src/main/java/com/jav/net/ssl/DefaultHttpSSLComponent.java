package com.jav.net.ssl;


import com.jav.common.util.StringEnvoy;
import com.jav.net.base.joggle.ISSLComponent;

import javax.net.ServerSocketFactory;
import javax.net.ssl.*;

public class DefaultHttpSSLComponent implements ISSLComponent, HostnameVerifier {

    protected String mHostname;

    public DefaultHttpSSLComponent() {
    }

    public DefaultHttpSSLComponent(String hostname) {
        if (StringEnvoy.isEmpty(hostname)) {
            throw new NullPointerException("hostname is null !!!");
        }
        this.mHostname = hostname.replace("http://", "").replace("https://", "")
                .replace("/", "");
    }

    @Override
    public SSLContext getSSLContext() {
        return null;
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
        if (StringEnvoy.isEmpty(this.mHostname)) {
            return true;
        } else {
            if (this.mHostname.equals(hostname)) {
                return true;
            } else {
                HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
                return hv.verify(hostname, sslSession);
            }
        }
    }

}
