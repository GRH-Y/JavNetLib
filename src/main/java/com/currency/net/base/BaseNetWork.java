package com.currency.net.base;

import com.currency.net.base.joggle.INetTaskContainer;

public class BaseNetWork<T extends BaseNetTask> {

    protected FactoryContext mFactoryContext;

    public BaseNetWork(FactoryContext context) {
        if (context == null) {
            throw new NullPointerException("FactoryContext can not be null !!!");
        }
        this.mFactoryContext = context;
    }

    public FactoryContext getFactoryContext() {
        return mFactoryContext;
    }

    //------------------------------------------------------------------------------------

    /**
     * 检查要链接任务
     */
    protected void onCheckConnectTask() {
        //检测是否有新的任务添加
        INetTaskContainer<T> taskFactory = mFactoryContext.getNetTaskContainer();
        T netTask = taskFactory.pollConnectTask();
        if (netTask != null) {
            netTask.setTaskStatus(NetTaskStatus.RUN);
            connectTaskImp(netTask);
        }
    }

    protected void connectTaskImp(T netTask) {
        onConnectTask(netTask);
    }


    /**
     * 检查要移除任务
     */
    protected void onCheckRemoverTask() {
        INetTaskContainer<T> taskFactory = mFactoryContext.getNetTaskContainer();
        T netTask = taskFactory.pollDestroyTask();
        if (netTask != null) {
            removerTaskImp(netTask);
        }
    }

    protected void removerTaskImp(T netTask) {
        netTask.setTaskStatus(NetTaskStatus.FINISH);
        onDisconnectTask(netTask);
        onRecoveryTask(netTask);
        netTask.setTaskStatus(NetTaskStatus.NONE);
    }


    /**
     * 销毁所有的链接任务
     */
    protected void onRecoveryTaskAll() {
    }


    //------------------------------------------------------------------------------------

    /**
     * 准备链接
     *
     * @param netTask 网络请求任务
     */
    protected void onConnectTask(T netTask) {
    }

    /**
     * 执行任务（读或者写）
     */
    protected void onRWDataTask() {
    }

    /**
     * 准备断开链接回调
     *
     * @param netTask 网络请求任务
     */
    protected void onDisconnectTask(T netTask) {
    }


    /**
     * 断开链接后回调
     *
     * @param netTask 网络请求任务
     */
    protected void onRecoveryTask(T netTask) {
        try {
            netTask.onRecovery();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
