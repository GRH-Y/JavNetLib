package com.jav.net.base;

import com.jav.net.base.joggle.INetFactory;
import com.jav.net.base.joggle.INetTaskContainer;
import com.jav.net.base.joggle.ISSLFactory;
import com.jav.net.entity.FactoryContext;

/**
 * 复用的工厂
 *
 * @param <T>
 */
public abstract class AbsNetFactory<T extends BaseNetTask> implements INetFactory {

    protected final FactoryContext mFactoryIntent;

    protected AbsNetFactory() {
        mFactoryIntent = new FactoryContext();

        ISSLFactory sslFactory = initSSLFactory();
        mFactoryIntent.setSSLFactory(sslFactory);

        BaseNetWork<T> netWork = initNetWork();
        if (netWork == null) {
            throw new IllegalStateException("initNetWork() This method returns value can not be null");
        }
        mFactoryIntent.setNetWork(netWork);

        AbsNetEngine netEngine = initNetEngine();
        if (netEngine == null) {
            throw new IllegalStateException("initNetEngine() This method returns value can not be null");
        }
        mFactoryIntent.setNetEngine(netEngine);

        INetTaskContainer<T> netTaskFactory = initNetTaskFactory();
        if (netTaskFactory == null) {
            throw new IllegalStateException("initTaskQueueFactory() This method returns value can not be null");
        }
        mFactoryIntent.setNetTaskContainer(netTaskFactory);
    }

    //---------------------------------------- init start ----------------------------------------------

    abstract protected AbsNetEngine initNetEngine();

    abstract protected BaseNetWork<T> initNetWork();

    abstract protected ISSLFactory initSSLFactory();

    protected INetTaskContainer<T> initNetTaskFactory() {
        return new NetTaskComponent<>(mFactoryIntent);
    }

    //------------------------------------------ init end -----------------------------------------------

    protected FactoryContext getFactoryIntent() {
        return mFactoryIntent;
    }

    //-----------------------------------------------------------------------------------------

    @Override
    public void open() {
        mFactoryIntent.getNetEngine().startEngine();
    }

    @Override
    public INetTaskContainer getNetTaskContainer() {
        return mFactoryIntent.getNetTaskContainer();
    }

    @Override
    public void close() {
        mFactoryIntent.getNetEngine().stopEngine();
    }

    @Override
    public boolean isOpen() {
        return mFactoryIntent.getNetEngine().isEngineRunning();
    }
}
