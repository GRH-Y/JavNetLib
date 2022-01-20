package com.currency.net.ssl;

import com.currency.net.base.joggle.ISSLFactory;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

public class DefaultSSLFactory implements ISSLFactory {

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
