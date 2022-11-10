package com.jav.net.nio;

import com.jav.net.base.AbsNetEngine;
import com.jav.net.base.BaseNetWork;
import com.jav.net.base.joggle.INetFactory;
import com.jav.net.base.joggle.INetTaskComponent;

/**
 * 高性能的客户端工厂，提高网络任务处理
 *
 * @author yyz
 */
public class NioBalancedClientFactory extends NioClientFactory {

    public NioBalancedClientFactory() {
    }


    private static final class InnerClass {
        public static final NioBalancedClientFactory sFactory = new NioBalancedClientFactory();
    }

    public static INetFactory<NioClientTask> getFactory() {
        return InnerClass.sFactory;
    }

    public static void destroy() {
        InnerClass.sFactory.close();
    }

    @Override
    protected BaseNetWork initNetWork() {
        return new NioBalancedNetWork(getFactoryContext());
    }

    @Override
    protected AbsNetEngine initNetEngine() {
        return new BalancedEngine(getFactoryContext());
    }

    @Override
    public INetTaskComponent<NioClientTask> getNetTaskComponent() {
        return super.getNetTaskComponent();
    }
}
