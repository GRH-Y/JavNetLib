package com.currency.net.aio;

import com.currency.net.base.BaseNetTask;
import com.currency.net.base.BaseNetWork;
import com.currency.net.entity.FactoryContext;

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
