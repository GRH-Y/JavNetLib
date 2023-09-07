package com.jav.net.aio;

import com.jav.net.base.BaseNetTask;
import com.jav.net.base.BaseNetWork;
import com.jav.net.base.FactoryContext;

public class AioNetWork<T extends BaseNetTask> extends BaseNetWork<T> {

    public AioNetWork(FactoryContext context) {
        super(context);
    }

    @Override
    public void onWorkBegin() {

    }

    @Override
    public void onWorkEnd() {

    }

    @Override
    public void onConnectTask(T netTask) {

    }

    @Override
    public boolean onDisconnectTask(T netTask) {
        return true;
    }

}
