package com.jav.net.nio;

import com.jav.common.log.LogDog;
import com.jav.net.base.BaseNetWork;
import com.jav.net.base.NetTaskStatus;
import com.jav.net.base.joggle.INetTaskComponent;
import com.jav.net.base.joggle.ISSLComponent;
import com.jav.net.entity.FactoryContext;
import com.jav.net.state.joggle.IStateMachine;

import java.io.IOException;
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

    @Override
    protected void init() {
        try {
            // use time 310ms
            mSelector = Selector.open();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Callback for the beginning of a network task,
     * If the link is empty, it is generally used to create a link.
     *
     * @param netTask
     * @return
     * @throws IOException
     */
    protected abstract C onCreateChannel(T netTask) throws IOException;

    /**
     * The channel used to initialize the link.
     *
     * @param netTask
     * @param channel
     * @throws IOException
     */
    protected abstract void onInitChannel(T netTask, C channel) throws IOException;

    /**
     * Register events that need to be handled for this channel
     *
     * @param netTask
     * @param channel
     * @throws IOException
     */
    protected abstract void onRegisterChannel(T netTask, C channel) throws IOException;


    protected void onAcceptEvent(SelectionKey key) {
    }

    protected void onConnectEvent(SelectionKey key) {
    }

    protected void onReadEvent(SelectionKey key) {
    }

    protected void onWriteEvent(SelectionKey key) {
    }

    //------------------------------------------------------------------------------------------------------------------

    protected Selector getSelector() {
        return mSelector;
    }


    protected void initSSLConnect(T netTask) {
        if (netTask.isTls()) {
            ISSLComponent sslFactory = mFactoryContext.getSSLFactory();
            netTask.onCreateSSLContext(sslFactory);
        }
    }

    protected void callChannelError(T netTask, Throwable e) {
        try {
            netTask.onErrorChannel(e);
        } catch (Throwable e1) {
            e.printStackTrace();
        } finally {
            // 有异常，结束任务
            INetTaskComponent taskFactory = mFactoryContext.getNetTaskComponent();
            taskFactory.addUnExecTask(netTask);
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    /**
     * 1.检查要链接任务
     */
    @Override
    protected void onCreateTask() {
        super.onCreateTask();
    }

    /**
     * 2.处理链接
     *
     * @param netTask 网络请求任务
     */
    @Override
    protected void onConnectTask(T netTask) {
        C channel = (C) netTask.getChannel();
        try {
            if (channel == null) {
                // 创建通道
                channel = onCreateChannel(netTask);
            }
            onInitChannel(netTask, channel);
            onRegisterChannel(netTask, channel);
        } catch (Throwable e) {
            LogDog.e("## onConnectTask has error , url = " + netTask.getHost() + " port = " + netTask.getPort());
            callChannelError(netTask, e);
        }
    }

    /**
     * 3.获取准备好的任务
     */
    @Override
    protected void onSelectEvent() {
        int count = 0;
        INetTaskComponent taskFactory = mFactoryContext.getNetTaskComponent();
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

    /**
     * 4.处理销毁的任务
     */
    @Override
    protected void onDestroyTask() {
        super.onDestroyTask();
    }

    @Override
    protected void destroyTaskImp(T netTask) {
        Set<SelectionKey> sets = mSelector.keys();
        if (sets.contains(netTask.mSelectionKey)) {
            super.destroyTaskImp(netTask);
        }
    }

    @Override
    protected void onDisconnectTask(T netTask) {
        try {
            netTask.onCloseChannel();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (netTask.getSelectionKey() != null) {
                try {
                    netTask.getSelectionKey().cancel();
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


    /**
     * 处理通道事件
     *
     * @param selectionKey
     */
    protected void onSelectionKey(SelectionKey selectionKey) {
        if (!selectionKey.isValid()) {
            return;
        }
        T netTask = (T) selectionKey.attachment();
        IStateMachine<Integer> stateMachine = netTask.getStatusMachine();
        if (stateMachine.getStatus() <= NetTaskStatus.LOAD) {
            selectionKey.cancel();
            LogDog.e("## AbsNioNetWork onSelectionKey status code = " + stateMachine.getStatus());
            return;
        }
        stateMachine.updateState(NetTaskStatus.IDLING, NetTaskStatus.RUN);

        boolean isAcceptable = selectionKey.isAcceptable();
        boolean isCanConnect = selectionKey.isConnectable();
        boolean isCanRead = selectionKey.isReadable();
        boolean isCanWrite = selectionKey.isWritable();

        if (isCanRead) {
            onReadEvent(selectionKey);
        }
        if (isCanWrite) {
            onWriteEvent(selectionKey);
        }
        if (isCanConnect) {
            onConnectEvent(selectionKey);
        }
        if (isAcceptable) {
            onAcceptEvent(selectionKey);
        }
        stateMachine.updateState(NetTaskStatus.RUN, NetTaskStatus.IDLING);
    }

    @Override
    protected void onDestroyTaskAll() {
        if (mSelector != null) {
            // 线程准备结束，释放所有链接
            for (SelectionKey selectionKey : mSelector.keys()) {
                T task = (T) selectionKey.attachment();
                if (task != null) {
                    destroyTaskImp(task);
                }
            }
        }
        INetTaskComponent taskFactory = mFactoryContext.getNetTaskComponent();
        taskFactory.clearAllQueue();
    }

    //------------------------------------------------------------------------------------------------------------------
}
