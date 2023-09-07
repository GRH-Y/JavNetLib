package com.jav.net.aio;

import com.jav.net.base.AbsNetFactory;
import com.jav.net.base.BaseNetEngine;
import com.jav.net.base.BaseNetWork;
import com.jav.net.base.joggle.INetFactory;
import com.jav.net.base.joggle.ISSLComponent;
import com.jav.net.ssl.SSLComponent;

public class AioServerFactory extends AbsNetFactory<AioServerTask> {

    private static final class InnerClass {
        public static final AioServerFactory sFactory = new AioServerFactory();
    }

    public static synchronized INetFactory<AioServerTask> getFactory() {
        return InnerClass.sFactory;
    }

    public static void destroy() {
        InnerClass.sFactory.close();
    }

    @Override
    protected BaseNetEngine initNetEngine() {
        return new AioEngine(mFactoryContext);
    }

    @Override
    protected BaseNetWork<AioServerTask> initNetWork() {
        return new AioServerNetWork(mFactoryContext);
    }

    @Override
    protected ISSLComponent initSSLComponent() {
        return new SSLComponent();
    }

}
