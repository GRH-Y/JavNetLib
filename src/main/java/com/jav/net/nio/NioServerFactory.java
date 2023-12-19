package com.jav.net.nio;


import com.jav.net.base.AbsNetFactory;
import com.jav.net.base.BaseNetEngine;
import com.jav.net.base.NioNetWork;
import com.jav.net.base.joggle.INetTaskComponent;

/**
 * nio服务端工厂(单线程管理多个ServerSocket)
 *
 * @author yyz
 * @version 1.0
 */
public class NioServerFactory extends AbsNetFactory<NioServerTask> {

    @Override
    protected BaseNetEngine initNetEngine() {
        return new NioNetEngine(mFactoryContext.getNetWork());
    }

    @Override
    protected NioNetWork initNetWork() {
        return new NioServerWork(getFactoryContext());
    }

    @Override
    public INetTaskComponent<NioServerTask> getNetTaskComponent() {
        return super.getNetTaskComponent();
    }
}
