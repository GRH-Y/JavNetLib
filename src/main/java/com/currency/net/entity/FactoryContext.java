package com.currency.net.entity;

import com.currency.net.base.AbsNetEngine;
import com.currency.net.base.BaseNetTask;
import com.currency.net.base.BaseNetWork;
import com.currency.net.base.joggle.INetTaskContainer;
import com.currency.net.base.joggle.ISSLFactory;

public class FactoryContext {

    private AbsNetEngine mNetEngine;

    private BaseNetWork mNetWork;

    private ISSLFactory mSSLFactory;

    private INetTaskContainer mNetTaskContainer;

    public <T extends AbsNetEngine> T getNetEngine() {
        return (T) mNetEngine;
    }

    public void setNetEngine(AbsNetEngine engine) {
        this.mNetEngine = engine;
    }

    public <T extends BaseNetWork> T getNetWork() {
        return (T) mNetWork;
    }

    public void setNetWork(BaseNetWork work) {
        this.mNetWork = work;
    }

    public ISSLFactory getSSLFactory() {
        return mSSLFactory;
    }

    public void setSSLFactory(ISSLFactory sslFactory) {
        this.mSSLFactory = sslFactory;
    }

    public <T extends BaseNetTask> INetTaskContainer<T> getNetTaskContainer() {
        return mNetTaskContainer;
    }

    public void setNetTaskContainer(INetTaskContainer container) {
        this.mNetTaskContainer = container;
    }
}
