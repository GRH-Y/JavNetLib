package com.jav.net.aio;

import com.jav.net.base.AbsNetEngine;
import com.jav.net.base.AbsNetFactory;
import com.jav.net.base.BaseNetWork;
import com.jav.net.base.joggle.INetFactory;
import com.jav.net.base.joggle.ISSLFactory;
import com.jav.net.ssl.SSLFactory;

public class AioServerFactory extends AbsNetFactory<AioServerTask> {

    private volatile static AioServerFactory mFactory = null;

    public static synchronized INetFactory getFactory() {
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
