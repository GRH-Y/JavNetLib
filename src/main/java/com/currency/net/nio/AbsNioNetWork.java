package com.currency.net.nio;

import com.currency.net.base.BaseNetWork;
import com.currency.net.base.FactoryContext;
import com.currency.net.base.NetTaskStatus;
import com.currency.net.base.joggle.INetTaskContainer;
import com.currency.net.base.joggle.ISSLFactory;
import log.LogDog;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NetworkChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

public abstract class AbsNioNetWork<T extends BaseNioSelectionTask, C extends NetworkChannel> extends BaseNetWork<T> {

    protected Selector mSelector;

    public AbsNioNetWork(FactoryContext context) {
        super(context);
    }

    protected void init() {
        if (mSelector == null) {
            try {
                //use time 310ms
                mSelector = Selector.open();
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    protected abstract C onCreateChannel(T netTask) throws IOException;

    protected abstract void onInitChannel(T netTask, C channel) throws IOException;

    protected abstract void onRegisterChannel(T netTask, C channel) throws ClosedChannelException;

    protected void onAcceptEvent(SelectionKey key) {
    }

    protected void onConnectEvent(SelectionKey key) {
    }

    protected void onReadEvent(SelectionKey key) {
    }

    protected void onWriteEvent(SelectionKey key) {
    }

    protected Selector getSelector() {
        return mSelector;
    }

    /**
     * 检查要链接任务
     */
    @Override
    protected void onCheckConnectTask() {
        super.onCheckConnectTask();
    }

    @Override
    protected void onConnectTask(T netTask) {
        C channel = (C) netTask.getChannel();
        try {
            if (channel == null) {
                //创建通道
                channel = onCreateChannel(netTask);
            }
            onInitChannel(netTask, channel);
            onRegisterChannel(netTask, channel);
        } catch (Throwable e) {
            LogDog.e("## onConnectTask has error , url = " + netTask.getHost() + " port = " + netTask.getPort());
            callBackInitStatusChannelError(netTask, e);
        }
    }

    /**
     * 获取准备好的任务
     */
    @Override
    protected void onRWDataTask() {
        int count = 0;
        INetTaskContainer taskFactory = mFactoryContext.getNetTaskContainer();
        if (taskFactory.isConnectQueueEmpty() && taskFactory.isDestroyQueueEmpty()) {
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
            for (Iterator<SelectionKey> iterator = mSelector.selectedKeys().iterator(); iterator.hasNext(); iterator.remove()) {
                SelectionKey selectionKey = iterator.next();
                onSelectionKey(selectionKey);
            }
        }
    }

    @Override
    protected void onCheckRemoverTask() {
        super.onCheckRemoverTask();
    }

    @Override
    protected void removerTaskImp(T netTask) {
        Set<SelectionKey> sets = mSelector.keys();
        if (sets.contains(netTask.mSelectionKey)) {
            super.removerTaskImp(netTask);
        }
    }

    protected void initSSLConnect(T netTask) {
        if (netTask.isTLS()) {
            ISSLFactory sslFactory = mFactoryContext.getSSLFactory();
            netTask.onCreateSSLContext(sslFactory);
        }
    }

    protected void callBackInitStatusChannelError(T netTask, Throwable e) {
        try {
            netTask.onErrorChannel(e);
        } catch (Throwable e1) {
            e.printStackTrace();
        } finally {
            INetTaskContainer taskFactory = mFactoryContext.getNetTaskContainer();
            //该通道有异常，结束任务
            taskFactory.addUnExecTask(netTask);
        }
    }

    @Override
    protected void onDisconnectTask(T netTask) {
        try {
            netTask.onCloseChannel();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (netTask.mSelectionKey != null) {
                try {
                    netTask.mSelectionKey.cancel();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            if (netTask.getChannel() != null) {
                try {
                    netTask.getChannel().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    /**
     * 处理通道事件
     *
     * @param selectionKey
     */
    protected void onSelectionKey(SelectionKey selectionKey) {
        T netTask = (T) selectionKey.attachment();
        if (netTask.isHasStatus(NetTaskStatus.FINISH)) {
            return;
        }
        boolean isAcceptable = selectionKey.isValid() && selectionKey.isAcceptable();
        boolean isCanConnect = selectionKey.isValid() && selectionKey.isConnectable();
        boolean isCanRead = selectionKey.isValid() && selectionKey.isReadable();
        boolean isCanWrite = selectionKey.isValid() && selectionKey.isWritable();
        if (isCanRead) {
            netTask.addTaskStatus(NetTaskStatus.READ);
            onReadEvent(selectionKey);
            netTask.delTaskStatus(NetTaskStatus.READ);
        }
        if (isCanWrite) {
            netTask.addTaskStatus(NetTaskStatus.WRITE);
            onWriteEvent(selectionKey);
            netTask.delTaskStatus(NetTaskStatus.WRITE);
        }
        if (isCanConnect) {
            onConnectEvent(selectionKey);
        }
        if (isAcceptable) {
            onAcceptEvent(selectionKey);
        }
        netTask.addTaskStatus(NetTaskStatus.IDLING);
    }

    @Override
    protected void onRecoveryTaskAll() {
        if (mSelector != null) {
            //线程准备结束，释放所有链接
            for (SelectionKey selectionKey : mSelector.keys()) {
                if (selectionKey.isValid()) {
                    T task = (T) selectionKey.attachment();
                    removerTaskImp(task);
                }
            }
            try {
                mSelector.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mSelector = null;
        }
        INetTaskContainer taskFactory = mFactoryContext.getNetTaskContainer();
        taskFactory.clearAllQueue();
    }

    //------------------------------------------------------------------------------------------------------------------
}
