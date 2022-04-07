package com.currency.net.nio;

import com.currency.net.base.AbsNetEngine;
import com.currency.net.base.BaseNetWork;
import com.currency.net.base.joggle.INetFactory;

public class NioBalancedClientFactory extends NioClientFactory {

    public NioBalancedClientFactory() {
    }


    private static final class InnerClass {
        public static final NioBalancedClientFactory sFactory = new NioBalancedClientFactory();
    }

    public static INetFactory getFactory() {
        return InnerClass.sFactory;
    }

    public static void destroy() {
        InnerClass.sFactory.close();
    }

    @Override
    protected BaseNetWork initNetWork() {
        return new NioBalancedNetWork(getFactoryIntent());
    }

    @Override
    protected AbsNetEngine initNetEngine() {
        return new NioBalancedClientEngine(getFactoryIntent());
    }
}
