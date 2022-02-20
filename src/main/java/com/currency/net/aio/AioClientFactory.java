package com.currency.net.aio;

import com.currency.net.base.AbsNetEngine;
import com.currency.net.base.AbsNetFactory;
import com.currency.net.base.BaseNetWork;
import com.currency.net.base.joggle.INetFactory;
import com.currency.net.base.joggle.ISSLFactory;
import com.currency.net.ssl.SSLFactory;

public class AioClientFactory extends AbsNetFactory<AioClientTask> {

    private volatile static INetFactory<AioClientTask> mFactory = null;

    public static synchronized INetFactory<AioClientTask> getFactory() {
        if (mFactory == null) {
            synchronized (AioClientFactory.class) {
                if (mFactory == null) {
                    mFactory = new AioClientFactory();
                }
            }
        }
        return mFactory;
    }

    public static void destroy() {
        if (mFactory != null) {
            mFactory.close();
            mFactory = null;
        }
    }

    @Override
    protected AbsNetEngine initNetEngine() {
        return new AioEngine(getFactoryIntent());
    }

    @Override
    protected BaseNetWork<AioClientTask> initNetWork() {
        return new AioClientNetWork(getFactoryIntent());
    }

    @Override
    protected ISSLFactory initSSLFactory() {
        return new SSLFactory();
    }
}
