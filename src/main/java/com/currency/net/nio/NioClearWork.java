package com.currency.net.nio;

import com.currency.net.base.FactoryContext;
import com.currency.net.base.NetTaskStatus;
import com.currency.net.base.joggle.INetTaskContainer;
import log.LogDog;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * NioClearWork 只处理断开连接移除请求的事件
 */
public class NioClearWork<T extends NioClientTask, C extends SocketChannel> extends NioClientWork<T, C> {

    private Queue<NioClientWork> mRWWorkQueue;
    private NioClientWork mBalancedNetWork;

    protected NioClearWork(FactoryContext context) {
        super(context);
        mRWWorkQueue = new ConcurrentLinkedQueue<>();
    }

    @Override
    protected void init() {
    }

    public void bindBalancedNetWork(NioClientWork netWork) {
        mBalancedNetWork = netWork;
    }

    public void bindRWNetWork(NioClientWork netWork) {
        mRWWorkQueue.offer(netWork);
    }

    public void unBindRWNetWork(NioClientWork netWork) {
        try {
            mRWWorkQueue.remove(netWork);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        Iterator<NioClientWork> iterator = mRWWorkQueue.iterator();
        while (iterator.hasNext()) {
            NioClientWork rwNetwork = iterator.next();
            FactoryContext context = rwNetwork.getFactoryContext();
            INetTaskContainer rwContainer = context.getNetTaskContainer();
            T netTask = (T) rwContainer.pollDestroyTask();
            if (netTask != null) {
                //这里处理NioReadWriteNetWork添加的netTask
                execRemoverTask(netTask);
            }
        }
    }

    @Override
    protected void removerTaskImp(T netTask) {
        LogDog.e("--> NioClearWork removerTaskImp  !!!");
        //这里回调可以确定是NioBalancedNetWork添加的netTask
//        if (isIllegalNetTask(netTask)) {
        execRemoverTask(netTask);
//        } else {
//            LogDog.e("--> NioClearWork isIllegalNetTask status = " + netTask.getTaskStatus());
//        }
    }

    private boolean isIllegalNetTask(T netTask) {
        Selector selector = mBalancedNetWork.getSelector();
        Set<SelectionKey> sets = selector.keys();
        if (sets.contains(netTask.mSelectionKey)) {
            return true;
        }
        return false;
    }

    private void execRemoverTask(T netTask) {
        if (!netTask.isHasStatus(NetTaskStatus.FINISH)) {
            NetTaskStatus waitStatus = new NetTaskStatus(NetTaskStatus.IDLING.getCode() | NetTaskStatus.RUN.getCode());
            netTask.waitAndSetTaskStatus(waitStatus, NetTaskStatus.FINISH);
        }
        onDisconnectTask(netTask);
        onRecoveryTask(netTask);
        netTask.setTaskStatus(NetTaskStatus.NONE);
    }

    @Override
    protected void onRecoveryTaskAll() {
        super.onRecoveryTaskAll();
        mRWWorkQueue.clear();
        mBalancedNetWork = null;
    }
}
