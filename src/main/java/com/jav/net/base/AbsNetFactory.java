package com.jav.net.base;

import com.jav.net.base.joggle.INetFactory;
import com.jav.net.base.joggle.INetTaskComponent;

/**
 * 复用的工厂
 *
 * @param <T>
 * @author yyz
 */
public abstract class AbsNetFactory<T extends BaseNetTask> implements INetFactory<T> {

    protected final FactoryContext mFactoryContext;

    protected AbsNetFactory() {
        mFactoryContext = new FactoryContext();
    }

    /**
     * 初始化
     */
    private void init() {
        SelectorEventHubs selectorCreator = initSelectorCreator();
        if (selectorCreator == null) {
            throw new IllegalStateException("initSelectorCreator() This method returns value can not be null");
        }
        mFactoryContext.setSelectorEventHubs(selectorCreator);

        NioNetWork netWork = initNetWork();
        if (netWork == null) {
            throw new IllegalStateException("initNetWork() This method returns value can not be null");
        }
        mFactoryContext.setNetWork(netWork);

        BaseNetEngine netEngine = initNetEngine();
        if (netEngine == null) {
            throw new IllegalStateException("initNetEngine() This method returns value can not be null");
        }
        mFactoryContext.setNetEngine(netEngine);

        INetTaskComponent<T> netTaskComponent = initNetTaskComponent();
        if (netTaskComponent == null) {
            throw new IllegalStateException("initNetTaskComponent() This method returns value can not be null");
        }
        mFactoryContext.setNetTaskComponent(netTaskComponent);
    }

    //---------------------------------------- init start ----------------------------------------------


    /**
     * 初始化网络引擎，用于执行netWork
     *
     * @return 返回实例
     */
    abstract protected BaseNetEngine initNetEngine();

    /**
     * 初始化网络事物，处理网络任务的周期事件
     *
     * @return
     */
    abstract protected NioNetWork initNetWork();

    /**
     * 初始化网络任务组件
     *
     * @return
     */
    protected INetTaskComponent<T> initNetTaskComponent() {
        return new NetTaskComponent<>(mFactoryContext);
    }


    protected SelectorEventHubs initSelectorCreator() {
        return new SelectorEventHubs();
    }

    //------------------------------------------ init end -----------------------------------------------

    protected FactoryContext getFactoryContext() {
        return mFactoryContext;
    }


    @Override
    public INetFactory<T> open() {
        init();
        BaseNetEngine engine = mFactoryContext.getNetEngine();
        engine.startEngine();
        return this;
    }

    @Override
    public INetTaskComponent<T> getNetTaskComponent() {
        return mFactoryContext.getNetTaskComponent();
    }

    @Override
    public void close() {
        BaseNetEngine engine = mFactoryContext.getNetEngine();
        engine.stopEngine();
        INetTaskComponent<T> component = mFactoryContext.getNetTaskComponent();
        component.release();

    }

    @Override
    public boolean isOpen() {
        return mFactoryContext.getNetEngine().isEngineRunning();
    }

    //-----------------------------------------------------------------------------------------

}
