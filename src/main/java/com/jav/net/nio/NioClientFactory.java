package com.jav.net.nio;


import com.jav.net.base.AbsNetFactory;
import com.jav.net.base.BaseNetEngine;
import com.jav.net.base.NioNetWork;

/**
 * nio客户端工厂接口创建者
 *
 * @author yyz
 * @version 1.0
 */
public class NioClientFactory extends AbsNetFactory<NioClientTask> {

    private int mWorkCount = 1;

    public NioClientFactory(int workCount) {
        mWorkCount = workCount;
    }

    public NioClientFactory() {
    }


    //--------------------------- start init-----------------------------------------------


    @Override
    protected NioNetWork initNetWork() {
        return new NioClientWork(mFactoryContext);
    }

    @Override
    protected BaseNetEngine initNetEngine() {
        return new NioNetEngine(mFactoryContext.getNetWork(), mWorkCount);
    }

    //--------------------------- end init-----------------------------------------------

}
