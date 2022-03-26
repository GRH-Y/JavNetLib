package com.currency.net.nio;

import com.currency.net.base.AbsNetEngine;
import com.currency.net.base.AbsNetFactory;
import com.currency.net.base.BaseNetWork;
import com.currency.net.base.joggle.INetFactory;
import com.currency.net.base.joggle.ISSLFactory;

public class NioUdpFactory extends AbsNetFactory<NioUdpTask> {

    private static final class InnerClass {
        public static final NioUdpFactory sFactory = new NioUdpFactory();
    }

    public NioUdpFactory() {
    }

    public static INetFactory<NioUdpTask> getFactory() {
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
