package com.jav.net.aio;

import com.jav.net.base.AbsNetEngine;
import com.jav.net.base.joggle.INetTaskContainer;
import com.jav.net.entity.FactoryContext;

public class AioEngine extends AbsNetEngine {

    public AioEngine(FactoryContext context) {
        super(context);
    }

    @Override
    protected void onEngineRun() {
        AioNetWork netWork = mFactoryContext.getNetWork();
        INetTaskContainer taskFactory = mFactoryContext.getNetTaskContainer();
        //检测是否有新的任务添加
        netWork.onCheckConnectTask();
        //清除要结束的任务
        netWork.onCheckRemoverTask();

        if (mExecutor.isLoopState() && taskFactory.isConnectQueueEmpty() && taskFactory.isDestroyQueueEmpty()) {
            //如果没有任务则休眠
            mExecutor.waitTask(0);
        }
    }

    @Override
    protected void onDestroyTask() {
        AioNetWork netWork = mFactoryContext.getNetWork();
        netWork.onRecoveryTaskAll();
    }
}
