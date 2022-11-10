package com.jav.net.aio;

import com.jav.net.base.AbsNetEngine;
import com.jav.net.base.AbsNetFactory;
import com.jav.net.base.BaseNetWork;
import com.jav.net.base.joggle.INetFactory;
import com.jav.net.base.joggle.ISSLComponent;
import com.jav.net.ssl.SSLComponent;

public class AioClientFactory extends AbsNetFactory<AioClientTask> {

    private volatile static INetFactory mFactory = null;

    public static synchronized INetFactory getFactory() {
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
        return new AioEngine(getFactoryContext());
    }

    @Override
    protected BaseNetWork<AioClientTask> initNetWork() {
        return new AioClientNetWork(getFactoryContext());
    }

    @Override
    protected ISSLComponent initSSLComponent() {
        return new SSLComponent();
    }
}
