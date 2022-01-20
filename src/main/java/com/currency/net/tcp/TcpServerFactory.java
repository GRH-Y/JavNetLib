package com.currency.net.tcp;

import com.currency.net.base.AbsNetEngine;
import com.currency.net.base.AbsNetFactory;
import com.currency.net.base.BaseNetWork;
import com.currency.net.base.joggle.ISSLFactory;
import com.currency.net.ssl.SSLFactory;

public class TcpServerFactory extends AbsNetFactory<TcpServerTask> {

    private static TcpServerFactory mFactory;

    @Override
    protected AbsNetEngine initNetEngine() {
        return new BioEngine(getFactoryIntent());
    }

    @Override
    protected BaseNetWork initNetWork() {
        return new BioServerWork(getFactoryIntent());
    }

    @Override
    protected ISSLFactory initSSLFactory() {
        return new SSLFactory();
    }


    public static synchronized TcpServerFactory getFactory() {
        if (mFactory == null) {
            synchronized (TcpServerFactory.class) {
                if (mFactory == null) {
                    mFactory = new TcpServerFactory();
                }
            }
        }
        return mFactory;
    }
}
