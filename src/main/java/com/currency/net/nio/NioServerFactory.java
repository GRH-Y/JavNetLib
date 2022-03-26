package com.currency.net.nio;


import com.currency.net.base.AbsNetEngine;
import com.currency.net.base.AbsNetFactory;
import com.currency.net.base.BaseNetWork;
import com.currency.net.base.joggle.INetFactory;
import com.currency.net.base.joggle.ISSLFactory;
import com.currency.net.ssl.SSLFactory;

/**
 * nio服务端工厂(单线程管理多个ServerSocket)
 *
 * @author yyz
 * @version 1.0
 */
public class NioServerFactory extends AbsNetFactory<NioServerTask> {

    public NioServerFactory() {
    }

    private static final class InnerClass {
        public static final NioServerFactory sFactory = new NioServerFactory();
    }

    public static INetFactory<NioServerTask> getFactory() {
        return InnerClass.sFactory;
    }

    public static void destroy() {
        InnerClass.sFactory.close();
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
