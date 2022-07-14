package com.jav.net.nio;


import com.jav.net.base.AbsNetEngine;
import com.jav.net.base.BaseNetWork;
import com.jav.net.base.joggle.INetFactory;

/**
 * nio服务端工厂(单线程管理多个ServerSocket)
 *
 * @author yyz
 * @version 1.0
 */
public class NioBalancedServerFactory extends NioServerFactory {

    public NioBalancedServerFactory() {
    }

    private static final class InnerClass {
        public static final NioBalancedServerFactory sFactory = new NioBalancedServerFactory();
    }

    public static INetFactory getFactory() {
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

}
