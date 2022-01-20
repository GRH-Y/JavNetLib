package com.currency.net.nio;

import com.currency.net.base.FactoryContext;
import com.currency.net.base.NetTaskStatus;
import com.currency.net.base.SocketChannelCloseException;
import com.currency.net.base.joggle.INetTaskContainer;
import log.LogDog;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * NioAcceptWork 只处理读写请求的事件
 */
public class NioReadWriteNetWork<T extends NioClientTask, C extends SocketChannel> extends NioClientWork<T, C> {

    private NioClearWork mClearWork;

    protected NioReadWriteNetWork(FactoryContext context, NioClearWork clearWork) {
        super(context);
        this.mClearWork = clearWork;
    }


    public void registerReadWriteEvent(T netTask) {
        SelectionKey selectionKey = netTask.getSelectionKey();
        selectionKey.cancel();
        netTask.addTaskStatus(NetTaskStatus.ASSIGN);
        FactoryContext context = getFactoryContext();
        INetTaskContainer container = context.getNetTaskContainer();
        boolean ret = container.addExecTask(netTask);
        if (ret) {
            mSelector.wakeup();
            LogDog.e("--> NioReadWriteNetWork registerReadWriteEvent success !!!");
        } else {
            netTask.delTaskStatus(NetTaskStatus.READY_END);
            netTask.delTaskStatus(NetTaskStatus.FINISH);
            notifyClearWork(netTask);
            LogDog.e("--> NioReadWriteNetWork registerReadWriteEvent fails !!!");
        }
    }


    public int getPressureValue() {
        return mSelector.selectedKeys().size();
    }

    @Override
    protected void onRWDataTask() {
        int count = 0;
        INetTaskContainer taskFactory = mFactoryContext.getNetTaskContainer();
        if (taskFactory.isConnectQueueEmpty()) {
            try {
                count = mSelector.select();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                count = mSelector.selectNow();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (count > 0) {
            LogDog.e("--> NioReadWriteNetWork onRWDataTask count = " + count);
            for (Iterator<SelectionKey> iterator = mSelector.selectedKeys().iterator(); iterator.hasNext(); iterator.remove()) {
                SelectionKey selectionKey = iterator.next();
                onSelectionKey(selectionKey);
            }
        }
    }

    @Override
    protected void hasErrorToUnExecTask(T netTask, Throwable e) {
        if (!(e instanceof SocketChannelCloseException)) {
            e.printStackTrace();
        }
        notifyClearWork(netTask);
    }

    @Override
    protected void callBackInitStatusChannelError(T netTask, Throwable e) {
        try {
            netTask.onErrorChannel(e);
        } catch (Throwable e1) {
            e.printStackTrace();
        } finally {
            //该通道有异常，结束任务
            notifyClearWork(netTask);
        }
    }

    private void notifyClearWork(T netTask) {
        SelectionKey selectionKey = netTask.getSelectionKey();
        if (selectionKey == null) {
            return;
        }
        selectionKey.cancel();
        FactoryContext context = mClearWork.getFactoryContext();
        INetTaskContainer taskFactory = context.getNetTaskContainer();
        taskFactory.addUnExecTask(netTask);
        NioNetEngine netEngine = context.getNetEngine();
        netEngine.resumeEngine();
    }
}
