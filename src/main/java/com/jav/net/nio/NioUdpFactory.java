package com.jav.net.nio;

import com.jav.net.base.AbsNetFactory;
import com.jav.net.base.BaseNetEngine;
import com.jav.net.base.NioNetWork;

public class NioUdpFactory extends AbsNetFactory<NioUdpTask> {

    @Override
    protected BaseNetEngine initNetEngine() {
        return new NioNetEngine(mFactoryContext.getNetWork());
    }

    @Override
    protected NioNetWork initNetWork() {
        return new NioUdpWork(getFactoryContext());
    }

}
