package com.jav.net.aio;

import com.jav.net.base.AbsNetEngine;
import com.jav.net.base.AbsNetFactory;
import com.jav.net.base.BaseNetWork;
import com.jav.net.base.joggle.INetFactory;
import com.jav.net.base.joggle.ISSLComponent;
import com.jav.net.nio.NioUdpFactory;
import com.jav.net.ssl.SSLComponent;

public class AioClientFactory extends AbsNetFactory<AioClientTask> {


    private static final class InnerClass {
        public static final AioClientFactory sFactory = new AioClientFactory();
    }

    public static synchronized INetFactory getFactory() {
        return InnerClass.sFactory;
    }

    public static void destroy() {
        InnerClass.sFactory.close();
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
