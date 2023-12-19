package com.jav.net.base;

import com.jav.common.log.LogDog;
import com.jav.common.state.joggle.IControlStateMachine;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * NioClearWork 只处理断开连接移除请求的事件
 *
 * @author yyz
 */
public class NioConnectWork extends BaseNetWork {

    private BaseNetEngine mEngine;

    /**
     * 等待创建连接队列
     */
    private Queue<BaseNetTask> mConnectCache;

    protected NioConnectWork(FactoryContext context) {
        super(context);
        mConnectCache = new ConcurrentLinkedQueue<>();
    }


    @Override
    protected void onWorkRun() {
        BaseNetTask netTask = mConnectCache.poll();
        if (netTask == null) {
            mEngine.pauseEngine();
        } else {
            execConnectTask(netTask);
        }
    }

    @Override
    protected void onWorkEnd() {
        mConnectCache.clear();
    }

    private void execConnectTask(BaseNetTask netTask) {
        // 检测是否有新的任务添加
        IControlStateMachine<Integer> stateMachine = netTask.getStatusMachine();
        for (; ; ) {
            if (stateMachine.getState() == NetTaskStatus.INVALID
                    || stateMachine.isAttachState(NetTaskStatus.FINISHING)) {
                return;
            } else if (stateMachine.updateState(NetTaskStatus.LOAD, NetTaskStatus.RUN)) {
                break;
            }
            LogDog.w("## onConnectNetTaskBegin task state = " + stateMachine.getState() + " task = " + netTask);
        }
        NioNetWork netWork = mFactoryContext.getNetWork();
        netWork.onConnect(netTask);
    }

    public boolean isContains(BaseNetTask task) {
        return mConnectCache.contains(task);
    }


    public boolean pushConnectTask(BaseNetTask task) {
        boolean ret = mConnectCache.offer(task);
        if (mEngine != null) {
            mEngine.resumeEngine();
        }
        return ret;
    }

    public void startWork() {
        if (mEngine == null) {
            mEngine = new BaseNetEngine(this);
            mEngine.startEngine();
        }
    }

    public void stopWork() {
        if (mEngine != null) {
            mEngine.stopEngine();
        }
    }

}
