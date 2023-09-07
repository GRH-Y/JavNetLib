package com.jav.net.nio;

import com.jav.common.log.LogDog;
import com.jav.common.state.joggle.IControlStateMachine;
import com.jav.net.base.BaseNetWork;
import com.jav.net.base.FactoryContext;
import com.jav.net.base.NetTaskStatus;
import com.jav.net.base.joggle.INetTaskComponent;
import com.jav.net.base.joggle.NetErrorType;

import java.io.IOException;
import java.nio.channels.NetworkChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

/**
 * base nio net work ,provide selector and nio work life cycle.
 *
 * @param <T>
 * @param <C>
 * @author yyz
 */
public abstract class AbsNioNetWork<T extends NioSelectionTask<?>, C extends NetworkChannel>
        extends BaseNetWork<T> {

    protected Selector mSelector;

    public AbsNioNetWork(FactoryContext context) {
        super(context);
    }

    @Override
    public void onWorkBegin() {
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
     * @param netTask network task
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


    protected void callChannelError(T netTask, NetErrorType errorType, Throwable e) {
        IControlStateMachine<Integer> stateMachine = netTask.getStatusMachine();
        if (!stateMachine.isAttachState(NetTaskStatus.FINISHING) && stateMachine.getState() != NetTaskStatus.INVALID) {
            try {
                netTask.onErrorChannel(errorType, e);
            } catch (Throwable e1) {
                e.printStackTrace();
            }
            // 有异常，结束任务
            INetTaskComponent<T> taskFactory = mFactoryContext.getNetTaskComponent();
            taskFactory.addUnExecTask(netTask);
        }
    }

    //------------------------------------------------------------------------------------------------------------------


    /**
     * 2.处理链接
     *
     * @param netTask 网络请求任务
     */
    @Override
    public void onConnectTask(T netTask) {
        C channel = (C) netTask.getChannel();
        IControlStateMachine<Integer> stateMachine = netTask.getStatusMachine();
        try {
            if (channel == null) {
                // 创建通道
                channel = onCreateChannel(netTask);
            }
            onInitChannel(netTask, channel);
            onRegisterChannel(netTask, channel);

            // 这里channel会有reg read 或者 reg connect 两种事件,都会等待触发,所以切换状态为IDLING,如果需要移除可以进行操作
            for (; ; ) {
                if (stateMachine.isAttachState(NetTaskStatus.FINISHING)) {
                    stateMachine.detachState(NetTaskStatus.RUN);
                    stateMachine.attachState(NetTaskStatus.IDLING);
                    break;
                } else if (stateMachine.getState() == NetTaskStatus.INVALID
                        || stateMachine.getState() == NetTaskStatus.IDLING
                        || stateMachine.updateState(NetTaskStatus.RUN, NetTaskStatus.IDLING)) {
                    break;
                }
                LogDog.w("## onConnectTask task state = " + stateMachine.getState() + " task = " + netTask);
            }
        } catch (Throwable e) {
            LogDog.e("## onConnectTask has error , url = " + netTask.getHost() + " port = " + netTask.getPort() + " task = " + netTask);
            stateMachine.detachState(NetTaskStatus.RUN);
            stateMachine.attachState(NetTaskStatus.IDLING);
            callChannelError(netTask, NetErrorType.CONNECT, e);
        }
    }

    /**
     * 3.获取准备好的任务
     */
    protected void onSelectEvent() {
        int count = 0;
        INetTaskComponent<T> taskFactory = mFactoryContext.getNetTaskComponent();
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
                try {
                    onSelectionKey(selectionKey);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void changeStateToClearCondition(IControlStateMachine<Integer> stateMachine) {
        if (stateMachine.getState() == NetTaskStatus.INVALID) {
            //当前任务已经完成销毁整个流程不能再修改状态
            return;
        }
        // 移除 run 状态
        while (stateMachine.isAttachState(NetTaskStatus.RUN)) {
            if (stateMachine.detachState(NetTaskStatus.RUN)) {
                break;
            }
        }
        // 添加 IDLING 状态
        while (!stateMachine.isAttachState(NetTaskStatus.IDLING)) {
            if (stateMachine.getState() == NetTaskStatus.INVALID || stateMachine.attachState(NetTaskStatus.IDLING)) {
                //当前状态是INVALID 则不再修改状态为IDLING，或者修改状态为IDLING成功
                break;
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
        if (netTask == null) {
            selectionKey.cancel();
            return;
        }
        netTask.setSelectionKey(selectionKey);
        IControlStateMachine<Integer> stateMachine = netTask.getStatusMachine();
        if (stateMachine.getState() == NetTaskStatus.INVALID) {
            selectionKey.cancel();
            return;
        }
        if (stateMachine.isAttachState(NetTaskStatus.FINISHING)) {
            changeStateToClearCondition(stateMachine);
            selectionKey.cancel();
            LogDog.e("## onSelectionKey status = " + stateMachine.getState() + " task = " + netTask);
            return;
        }

        boolean ret = stateMachine.updateState(NetTaskStatus.IDLING, NetTaskStatus.RUN);
        if (!ret) {
            LogDog.e("## updateState IDLING fails, state = " + stateMachine.getState());
            changeStateToClearCondition(stateMachine);
            selectionKey.cancel();
            return;
        }

        if (selectionKey.isValid() && selectionKey.isReadable() && stateMachine.getState() == NetTaskStatus.RUN) {
            onReadEvent(selectionKey);
        }
        if (selectionKey.isValid() && selectionKey.isWritable() && stateMachine.getState() == NetTaskStatus.RUN) {
            onWriteEvent(selectionKey);
        }
        if (selectionKey.isValid() && selectionKey.isConnectable() && stateMachine.getState() == NetTaskStatus.RUN) {
            onConnectEvent(selectionKey);
        }
        if (selectionKey.isValid() && selectionKey.isAcceptable() && stateMachine.getState() == NetTaskStatus.RUN) {
            onAcceptEvent(selectionKey);
        }

        changeStateToClearCondition(stateMachine);
    }

    @Override
    public boolean onDisconnectTask(T netTask) {
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
        try {
            netTask.onRecovery();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            return true;
        }
    }

    @Override
    public void onWorkEnd() {
        if (mSelector != null) {
            // 线程准备结束，释放所有链接
            for (SelectionKey selectionKey : mSelector.keys()) {
                T task = (T) selectionKey.attachment();
                if (task != null) {
                    onDisconnectTask(task);
                }
            }
            try {
                mSelector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        INetTaskComponent<T> taskFactory = mFactoryContext.getNetTaskComponent();
        taskFactory.clearAllQueue();
    }

    //------------------------------------------------------------------------------------------------------------------
}
