package com.currency.net.udp;

import com.currency.net.base.BaseNetTask;
import com.currency.net.base.BaseNetWork;
import com.currency.net.base.FactoryContext;
import com.currency.net.base.NetTaskStatusCode;
import com.currency.net.base.joggle.INetTaskContainer;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BioNetWork<T extends BaseNetTask> extends BaseNetWork<T> {

    /**
     * 正在执行任务的队列
     */
    protected final Queue<T> mExecutorQueue;

    protected BioNetWork(FactoryContext context) {
        super(context);
        mExecutorQueue = new ConcurrentLinkedQueue<>();
    }

    protected Queue<T> getExecutorQueue() {
        return mExecutorQueue;
    }

    //------------------------------------------------------------------------------------------------------------------

    @Override
    protected void onRWDataTask() {
        super.onRWDataTask();
    }

    @Override
    protected void onCheckConnectTask() {
        super.onCheckConnectTask();
    }

    @Override
    protected void connectTaskImp(T netTask) {
        super.connectTaskImp(netTask);
        if (netTask.getTaskStatus().equals(NetTaskStatusCode.RUN)) {
            mExecutorQueue.add(netTask);
        }
    }

    @Override
    protected void onCheckRemoverTask() {
        super.onCheckRemoverTask();
    }

    @Override
    protected void removerTaskImp(T netTask) {
        super.removerTaskImp(netTask);
        mExecutorQueue.remove(netTask);
    }

    @Override
    protected void onRecoveryTaskAll() {
        for (T task : mExecutorQueue) {
            try {
                onDisconnectTask(task);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        INetTaskContainer taskFactory = mFactoryContext.getNetTaskContainer();
        taskFactory.clearAllQueue();
        mExecutorQueue.clear();
    }
}
