package com.jav.net.aio;

import com.jav.net.base.BaseNetTask;
import com.jav.net.base.BaseNetWork;
import com.jav.net.entity.FactoryContext;

public class AioNetWork<T extends BaseNetTask> extends BaseNetWork<T> {

    public AioNetWork(FactoryContext context) {
        super(context);
    }

    @Override
    protected void onCreateTask() {
        super.onCreateTask();
    }


    @Override
    protected void onDestroyTask() {
        super.onDestroyTask();
    }

    @Override
    protected void onDestroyTaskAll() {
        super.onDestroyTaskAll();
    }
}
