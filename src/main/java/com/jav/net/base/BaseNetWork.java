package com.jav.net.base;

import com.jav.net.base.joggle.INetTaskComponent;
import com.jav.net.entity.FactoryContext;
import com.jav.net.state.joggle.IStateMachine;

/**
 * 基本网络事务,提供事务的生命周期回调方法
 * init()
 * onCreateTask()
 * onSelectEvent()
 * onDestroyTask()
 * onRecoveryTaskAll()
 *
 * @param <T>
 * @author yyz
 */
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
     * 初始化操作,engine触发执行
     */
    protected void init() {
    }

    /**
     * 检查要链接任务,engine触发执行
     */
    protected void onCreateTask() {
        // 检测是否有新的任务添加
        INetTaskComponent<T> taskFactory = mFactoryContext.getNetTaskComponent();
        T netTask = taskFactory.pollConnectTask();
        if (netTask != null) {
            IStateMachine stateMachine = netTask.getStatusMachine();
            if (stateMachine.updateState(NetTaskStatus.LOAD, NetTaskStatus.RUN)) {
                createTaskImp(netTask);
            }
        }
    }

    protected void createTaskImp(T netTask) {
        onConnectTask(netTask);
    }


    /**
     * 执行事件,engine触发执行
     */
    protected void onSelectEvent() {
    }


    /**
     * 检查要移除任务,engine触发执行
     */
    protected void onDestroyTask() {
        INetTaskComponent<T> taskFactory = mFactoryContext.getNetTaskComponent();
        T netTask = taskFactory.pollDestroyTask();
        if (netTask != null) {
            destroyTaskImp(netTask);
        }
    }

    protected void destroyTaskImp(T netTask) {
        IStateMachine stateMachine = netTask.getStatusMachine();
        stateMachine.setStatus(NetTaskStatus.FINISHING);
        onDisconnectTask(netTask);
        stateMachine.setStatus(NetTaskStatus.INVALID);
        onRecoveryTask(netTask);
    }


    /**
     * 销毁所有任务,engine触发执行
     */
    protected void onDestroyTaskAll() {
    }


    //----------------------------------- on -------------------------------------------------

    /**
     * 准备链接
     *
     * @param netTask 网络请求任务
     */
    protected void onConnectTask(T netTask) {
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
