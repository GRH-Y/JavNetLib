package com.jav.net.base;

/**
 * 基本网络事务,提供事务的生命周期回调方法
 * init()
 * onCreateTask()
 * onSelectEvent()
 * onDestroyTask()
 * onRecoveryTaskAll()
 *
 * @author yyz
 */
public class BaseNetWork {


    protected FactoryContext mFactoryContext;

    public BaseNetWork(FactoryContext context) {
        this.mFactoryContext = context;
    }

    //------------------------------------------------------------------------------------

    protected void onWorkBegin() {
    }


    protected void onWorkRun() {
    }

    /**
     * 销毁所有任务,engine触发执行
     */
    protected void onWorkEnd() {
    }


    //----------------------------------- on -------------------------------------------------


}
