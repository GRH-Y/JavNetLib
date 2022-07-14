package com.jav.net.aio;

import com.jav.net.base.BaseNetTask;
import com.jav.net.base.BaseNetWork;
import com.jav.net.entity.FactoryContext;

public class AioNetWork<T extends BaseNetTask> extends BaseNetWork<T> {

    public AioNetWork(FactoryContext context) {
        super(context);
    }

    @Override
    protected void onCheckConnectTask() {
        super.onCheckConnectTask();
    }


    @Override
    protected void onCheckRemoverTask() {
        super.onCheckRemoverTask();
    }

    @Override
    protected void onRecoveryTaskAll() {
        super.onRecoveryTaskAll();
    }
}
