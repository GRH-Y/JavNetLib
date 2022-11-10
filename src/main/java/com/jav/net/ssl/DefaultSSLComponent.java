package com.jav.net.ssl;

import com.jav.net.base.joggle.ISSLComponent;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

public class DefaultSSLComponent implements ISSLComponent {

    @Override
    public SSLContext getSSLContext() {
        return null;
    }

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
