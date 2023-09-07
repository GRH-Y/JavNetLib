package com.jav.net.base;

import com.jav.common.state.joggle.IControlStateMachine;
import com.jav.net.base.joggle.INetTaskComponent;

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
public abstract class BaseNetWork<T extends BaseNetTask> {


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
    abstract public void onWorkBegin();

    /**
     * 检查要链接任务,engine触发执行
     */
    public void onConnectNetTaskBegin() {
        // 检测是否有新的任务添加
        INetTaskComponent<T> taskFactory = mFactoryContext.getNetTaskComponent();
        T netTask = taskFactory.pollConnectTask();
        if (netTask != null) {
            IControlStateMachine<Integer> stateMachine = netTask.getStatusMachine();
            for (; ; ) {
                if (stateMachine.getState() == NetTaskStatus.INVALID) {
                    return;
                } else if (stateMachine.isAttachState(NetTaskStatus.FINISHING)) {
                    stateMachine.detachState(NetTaskStatus.LOAD);
                    stateMachine.attachState(NetTaskStatus.IDLING);
                    return;
                } else if (stateMachine.updateState(NetTaskStatus.LOAD, NetTaskStatus.RUN)) {
                    break;
                }
            }
            // LogDog.w("## onCreateTask task state = " + stateMachine.getState() + " task = " + netTask);
            onConnectTask(netTask);
        }
    }

    /**
     * 检查要移除任务,engine触发执行
     */
    public void onDisconnectNetTaskEnd() {
        INetTaskComponent<T> taskFactory = mFactoryContext.getNetTaskComponent();
        T netTask = taskFactory.pollDestroyTask();
        if (netTask != null) {
            IControlStateMachine<Integer> stateMachine = netTask.getStatusMachine();
            boolean ret = onDisconnectTask(netTask);
            if (ret) {
                stateMachine.updateState(stateMachine.getState(), NetTaskStatus.INVALID);
            }
        }
    }


    /**
     * 销毁所有任务,engine触发执行
     */
    abstract public void onWorkEnd();

    //----------------------------------- on -------------------------------------------------

    /**
     * 准备链接
     *
     * @param netTask 网络请求任务
     */
    abstract public void onConnectTask(T netTask);


    /**
     * 准备断开链接回调
     *
     * @param netTask 网络请求任务
     * @return 返回true则断开连接成功
     */
    abstract public boolean onDisconnectTask(T netTask);

}
