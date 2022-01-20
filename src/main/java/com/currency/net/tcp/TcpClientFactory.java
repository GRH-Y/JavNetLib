package com.currency.net.tcp;

import com.currency.net.base.AbsNetEngine;
import com.currency.net.base.AbsNetFactory;
import com.currency.net.base.BaseNetWork;
import com.currency.net.base.joggle.ISSLFactory;
import com.currency.net.ssl.SSLFactory;

public class TcpClientFactory<T extends TcpClientTask> extends AbsNetFactory<T> {

    private static TcpClientFactory mFactory;

    @Override
    protected AbsNetEngine initNetEngine() {
        return new BioEngine(mFactoryIntent);
    }

    @Override
    protected BaseNetWork initNetWork() {
        return new BioClientWork<T>(mFactoryIntent);
    }

    @Override
    protected ISSLFactory initSSLFactory() {
        return new SSLFactory();
    }

    public synchronized static TcpClientFactory getFactory() {
        if (mFactory == null) {
            synchronized (TcpClientFactory.class) {
                if (mFactory == null) {
                    mFactory = new TcpClientFactory();
                }
            }
        }
        return mFactory;
    }

}
