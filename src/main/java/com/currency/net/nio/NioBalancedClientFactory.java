package com.currency.net.nio;

import com.currency.net.base.AbsNetEngine;
import com.currency.net.base.BaseNetWork;
import com.currency.net.base.NetTaskComponent;
import com.currency.net.base.joggle.INetFactory;
import com.currency.net.base.joggle.INetTaskContainer;

public class NioBalancedClientFactory extends NioClientFactory {

    private volatile static INetFactory<NioClientTask> mFactory = null;

    public static synchronized INetFactory<NioClientTask> getFactory() {
        if (mFactory == null) {
            synchronized (NioBalancedClientFactory.class) {
                if (mFactory == null) {
                    mFactory = new NioBalancedClientFactory();
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
    protected INetTaskContainer initNetTaskFactory() {
        return new NetTaskComponent(getFactoryIntent());
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
