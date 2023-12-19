package com.jav.net.base;

import com.jav.net.base.joggle.INetTaskComponent;
import com.jav.net.base.joggle.ISSLComponent;

/**
 * 网络工厂上下文,提供基本的对象访问
 *
 * @author yyz
 */
public class FactoryContext {

    private SelectorEventHubs mEventHubs;

    private BaseNetEngine mNetEngine;

    private NioNetWork mNetWork;

    private ISSLComponent mSSLFactory;

    private INetTaskComponent mNetTaskComponent;

    public void setSelectorEventHubs(SelectorEventHubs eventHubs) {
        this.mEventHubs = eventHubs;
    }

    public SelectorEventHubs getSelectorEventHubs() {
        return mEventHubs;
    }

    public <T extends BaseNetEngine> T getNetEngine() {
        return (T) mNetEngine;
    }

    public void setNetEngine(BaseNetEngine engine) {
        this.mNetEngine = engine;
    }

    public <T extends BaseNetWork> T getNetWork() {
        return (T) mNetWork;
    }

    public void setNetWork(NioNetWork work) {
        this.mNetWork = work;
    }

    public ISSLComponent getSSLFactory() {
        return mSSLFactory;
    }

    public void setSSLFactory(ISSLComponent sslFactory) {
        this.mSSLFactory = sslFactory;
    }

    public <T extends INetTaskComponent> T getNetTaskComponent() {
        return (T) mNetTaskComponent;
    }

    public void setNetTaskComponent(INetTaskComponent component) {
        this.mNetTaskComponent = component;
    }
}
