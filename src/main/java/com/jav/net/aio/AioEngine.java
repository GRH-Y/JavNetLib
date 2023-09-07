package com.jav.net.aio;

import com.jav.net.base.BaseNetEngine;
import com.jav.net.base.BaseNetTask;
import com.jav.net.base.FactoryContext;
import com.jav.net.base.joggle.INetTaskComponent;

public class AioEngine extends BaseNetEngine {

    public AioEngine(FactoryContext context) {
        super(context);
    }


    @Override
    protected void onDestroyStepExt() {
        INetTaskComponent<BaseNetTask> taskFactory = mFactoryContext.getNetTaskComponent();
        if (mExecutor.isLoopState() && taskFactory.isConnectQueueEmpty() && taskFactory.isDestroyQueueEmpty()) {
            //如果没有任务则休眠
            mExecutor.waitTask(0);
        }
    }
}
