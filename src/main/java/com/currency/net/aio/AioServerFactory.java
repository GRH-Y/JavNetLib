package com.currency.net.aio;

import com.currency.net.base.AbsNetEngine;
import com.currency.net.base.AbsNetFactory;
import com.currency.net.base.BaseNetWork;
import com.currency.net.base.joggle.INetFactory;
import com.currency.net.base.joggle.ISSLFactory;
import com.currency.net.ssl.SSLFactory;

public class AioServerFactory extends AbsNetFactory<AioServerTask> {

    private static AioServerFactory mFactory = null;

    public static synchronized INetFactory<AioServerTask> getFactory() {
        if (mFactory == null) {
            synchronized (AioServerFactory.class) {
                if (mFactory == null) {
                    mFactory = new AioServerFactory();
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
        return new AioEngine(mFactoryIntent);
    }

    @Override
    protected BaseNetWork<AioServerTask> initNetWork() {
        return new AioServerNetWork(mFactoryIntent);
    }

    @Override
    protected ISSLFactory initSSLFactory() {
        return new SSLFactory();
    }

}
