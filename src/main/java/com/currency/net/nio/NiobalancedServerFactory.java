package com.currency.net.nio;


import com.currency.net.base.AbsNetEngine;
import com.currency.net.base.AbsNetFactory;
import com.currency.net.base.BaseNetWork;
import com.currency.net.base.NetTaskComponent;
import com.currency.net.base.joggle.INetFactory;
import com.currency.net.base.joggle.INetTaskContainer;
import com.currency.net.base.joggle.ISSLFactory;
import com.currency.net.ssl.SSLFactory;

/**
 * nio服务端工厂(单线程管理多个ServerSocket)
 *
 * @author yyz
 * @version 1.0
 */
public class NioBalancedServerFactory extends AbsNetFactory<NioServerTask> {

    private volatile static NioBalancedServerFactory mFactory = null;

    public static synchronized INetFactory<NioServerTask> getFactory() {
        if (mFactory == null) {
            synchronized (NioClientFactory.class) {
                if (mFactory == null) {
                    mFactory = new NioBalancedServerFactory();
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
    protected AbsNetEngine initNetEngine() {
        return new NioNetEngine(getFactoryIntent());
    }

    @Override
    protected BaseNetWork<NioServerTask> initNetWork() {
        return new NioServerWork(getFactoryIntent());
    }

    @Override
    protected ISSLFactory initSSLFactory() {
        return new SSLFactory();
    }
}
