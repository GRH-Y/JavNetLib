package com.currency.net.tcp;

import com.currency.net.base.AbsNetEngine;
import com.currency.net.base.BaseNetTask;
import com.currency.net.base.FactoryContext;
import com.currency.net.base.joggle.INetTaskContainer;

/**
 * 工厂的核心类
 *
 * @param <T>
 */
public class BioEngine<T extends BaseNetTask> extends AbsNetEngine {

    public BioEngine(FactoryContext context) {
        super(context);
    }

    @Override
    protected void onEngineRun() {
        BioNetWork netWork = mFactoryContext.getNetWork();
        INetTaskContainer taskFactory = mFactoryContext.getNetTaskContainer();
        //检测是否有新的任务添加
        netWork.onCheckConnectTask();
        //检查是否有任务
        netWork.onRWDataTask();
        //清除要结束的任务
        netWork.onCheckRemoverTask();

        if (mExecutor.isLoopState()) {
            if (netWork.getExecutorQueue().isEmpty() && taskFactory.isConnectQueueEmpty() && taskFactory.isDestroyQueueEmpty()) {
                mExecutor.waitTask(0);
            } else {
                mExecutor.waitTask(100);
            }
        }
    }

    @Override
    protected void onDestroyTask() {
        BioNetWork netWork = mFactoryContext.getNetWork();
        netWork.onRecoveryTaskAll();
    }
}
