package com.jav.net.base.joggle;

import javax.net.ServerSocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

public interface ISSLFactory {

    SSLContext  getSSLContext();
    
    SSLSocketFactory getSSLSocketFactory();

    ServerSocketFactory getSSLServerSocketFactory();

    HostnameVerifier getHostnameVerifier();
}
