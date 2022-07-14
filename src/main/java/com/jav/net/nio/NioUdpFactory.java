package com.jav.net.nio;

import com.jav.net.base.AbsNetEngine;
import com.jav.net.base.AbsNetFactory;
import com.jav.net.base.BaseNetWork;
import com.jav.net.base.joggle.INetFactory;
import com.jav.net.base.joggle.ISSLFactory;

public class NioUdpFactory extends AbsNetFactory<NioUdpTask> {

    private static final class InnerClass {
        public static final NioUdpFactory sFactory = new NioUdpFactory();
    }

    public NioUdpFactory() {
    }

    public static INetFactory getFactory() {
        return NioUdpFactory.InnerClass.sFactory;
    }

    public static void destroy() {
        NioUdpFactory.InnerClass.sFactory.close();
    }

    @Override
    protected AbsNetEngine initNetEngine() {
        return new NioNetEngine(getFactoryIntent());
    }

    @Override
    protected BaseNetWork<NioUdpTask> initNetWork() {
        return new NioUdpWork(getFactoryIntent());
    }

    @Override
    protected ISSLFactory initSSLFactory() {
        return null;
    }


}
