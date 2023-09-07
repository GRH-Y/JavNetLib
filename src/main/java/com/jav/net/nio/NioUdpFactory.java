package com.jav.net.nio;

import com.jav.net.base.AbsNetFactory;
import com.jav.net.base.BaseNetEngine;
import com.jav.net.base.BaseNetWork;
import com.jav.net.base.joggle.INetFactory;
import com.jav.net.base.joggle.ISSLComponent;

public class NioUdpFactory extends AbsNetFactory<NioUdpTask> {

    private static final class InnerClass {
        public static final NioUdpFactory sFactory = new NioUdpFactory();
    }

    public NioUdpFactory() {
    }

    public static INetFactory getFactory() {
        return InnerClass.sFactory;
    }

    public static void destroy() {
        InnerClass.sFactory.close();
    }

    @Override
    protected BaseNetEngine initNetEngine() {
        return new NioNetEngine(getFactoryContext());
    }

    @Override
    protected BaseNetWork<NioUdpTask> initNetWork() {
        return new NioUdpWork(getFactoryContext());
    }

    @Override
    protected ISSLComponent initSSLComponent() {
        return null;
    }


}
