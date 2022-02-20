package com.currency.net.nio;

import com.currency.net.base.FactoryContext;
import com.currency.net.base.NetTaskStatusCode;
import com.currency.net.base.joggle.INetTaskContainer;

import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * NioClearWork 只处理断开连接移除请求的事件
 */
public class NioClearWork<T extends NioClientTask, C extends SocketChannel> extends NioClientWork<T, C> {

    private final Queue<INetTaskContainer> mRWWorkQueue;

    protected NioClearWork(FactoryContext context) {
        super(context);
        mRWWorkQueue = new ConcurrentLinkedQueue<>();
    }

    @Override
    protected void init() {
        //重写init方法，避免创建Selector
    }


    public void bindRWNetWork(NioClientWork netWork) {
        FactoryContext context = netWork.getFactoryContext();
        mRWWorkQueue.offer(context.getNetTaskContainer());
    }

    @Override
    protected void onCheckRemoverTask() {
        INetTaskContainer container = mFactoryContext.getNetTaskContainer();
        if (container.isDestroyQueueEmpty()) {
            NioNetEngine netEngine = mFactoryContext.getNetEngine();
            netEngine.pauseEngine();
        } else {
            super.onCheckRemoverTask();
            checkRemoverRwTask();
        }
    }

    private void checkRemoverRwTask() {
        for (Iterator<INetTaskContainer> iterator = mRWWorkQueue.iterator(); iterator.hasNext(); ) {
            INetTaskContainer rwContainer = iterator.next();
            T netTask = (T) rwContainer.pollDestroyTask();
            if (netTask != null) {
                //这里处理NioReadWriteNetWork添加的netTask
                execRemoverTask(netTask);
            }
        }
    }

    @Override
    protected void removerTaskImp(T netTask) {
        execRemoverTask(netTask);
    }


    private void execRemoverTask(T netTask) {
        boolean isWait = false;
        int tryCount = 3;
        //等待task状态进入IDLING再执行回收
        while (!netTask.updateTaskStatus(NetTaskStatusCode.IDLING, NetTaskStatusCode.FINISH, isWait)) {
            isWait = tryCount <= 0;
            tryCount--;
        }
        onDisconnectTask(netTask);
        onRecoveryTask(netTask);
        netTask.setTaskStatus(NetTaskStatusCode.INVALID);
    }

    @Override
    protected void onRecoveryTaskAll() {
        super.onRecoveryTaskAll();
        mRWWorkQueue.clear();
    }
}
