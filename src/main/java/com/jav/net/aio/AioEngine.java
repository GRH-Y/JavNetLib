package com.jav.net.aio;

import com.jav.net.base.AbsNetEngine;
import com.jav.net.base.joggle.INetTaskComponent;
import com.jav.net.entity.FactoryContext;

public class AioEngine extends AbsNetEngine {

    public AioEngine(FactoryContext context) {
        super(context);
    }

    @Override
    protected void onEngineRun() {
        AioNetWork netWork = mFactoryContext.getNetWork();
        INetTaskComponent taskFactory = mFactoryContext.getNetTaskComponent();
        //检查是否有新的任务添加
        netWork.onCreateTask();
        //清除要结束的任务
        netWork.onDestroyTask();

        if (mExecutor.isLoopState() && taskFactory.isConnectQueueEmpty() && taskFactory.isDestroyQueueEmpty()) {
            //如果没有任务则休眠
            mExecutor.waitTask(0);
        }
    }

    @Override
    protected void onDestroyTask() {
        AioNetWork netWork = mFactoryContext.getNetWork();
        netWork.onDestroyTaskAll();
    }
}
