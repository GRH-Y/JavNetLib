package com.jav.net.base;

import com.jav.common.log.LogDog;
import com.jav.common.state.joggle.IControlStateMachine;
import com.jav.net.base.joggle.INetTaskComponent;
import com.jav.net.base.joggle.NetErrorType;

import java.io.IOException;
import java.nio.channels.NetworkChannel;
import java.nio.channels.SelectionKey;

/**
 * base nio net work ,provide selector and nio work life cycle.
 *
 * @param <C>
 * @author yyz
 */
public abstract class NioNetWork<T extends BaseNetTask, C extends NetworkChannel> extends BaseNetWork {


    public NioNetWork(FactoryContext context) {
        super(context);
    }


    //-------------------------------------------------on---------------------------------------------------------------

    protected abstract C onCreateChannel(T netTask) throws IOException;

    protected abstract void onInitChannel(T netTask, C channel) throws IOException;

    protected abstract void onRegisterChannel(T netTask, C channel) throws IOException;


    protected void onAcceptEvent(T netTask, C channel) {
    }

    protected void onConnectEvent(T netTask, C channel) {
    }

    protected void onReadEvent(T netTask, C channel) {
    }

    protected void onWriteEvent(T netTask, C channel) {
    }

    //------------------------------------------------------------------------------------------------------------------


    @Override
    protected void onWorkRun() {
        SelectorEventHubs eventHubs = mFactoryContext.getSelectorEventHubs();
        ChannelEventDepot eventDepot = eventHubs.getReadyChannelEvent(true);
        if (eventDepot == null) {
            return;
        }
        onHandleChannelEvent(eventDepot);
        eventHubs.doneChannelEvent(eventDepot);
    }


    /**
     * 处理链接
     *
     * @param netTask 网络请求任务
     */
    public void onConnect(BaseNetTask netTask) {
        C channel = (C) netTask.getChannel();
        try {
            if (channel == null) {
                // 创建通道
                channel = onCreateChannel((T) netTask);
            }
            onInitChannel((T) netTask, channel);
            onRegisterChannel((T) netTask, channel);
//            IControlStateMachine<Integer> stateMachine = netTask.getStatusMachine();
//            LogDog.w("## onConnectTask task state = " + stateMachine.getState() + " task = " + netTask);
        } catch (Throwable e) {
            LogDog.e("## onConnectTask has error , url = " + netTask.getHost() + " port = " + netTask.getPort()
                    + " task = " + netTask);
            callChannelError(netTask, NetErrorType.CONNECT, e);
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    protected void callChannelError(BaseNetTask netTask, NetErrorType errorType, Throwable e) {
        IControlStateMachine<Integer> stateMachine = netTask.getStatusMachine();
        if (!stateMachine.isAttachState(NetTaskStatus.FINISHING) && stateMachine.getState() != NetTaskStatus.INVALID) {
            try {
                netTask.onErrorChannel(errorType, e);
            } catch (Throwable e1) {
                e.printStackTrace();
            }
            // 有异常，结束任务
            INetTaskComponent<BaseNetTask> taskFactory = mFactoryContext.getNetTaskComponent();
            taskFactory.addUnExecTask(netTask);
        }
    }

    private void onHandleChannelEvent(ChannelEventDepot eventDepot) {
        C channel = (C) eventDepot.channel();
        T netTask = (T) eventDepot.attachment();
        int readyOps = eventDepot.readyOps();

        IControlStateMachine<Integer> stateMachine = netTask.getStatusMachine();
        if (stateMachine.getState() == NetTaskStatus.INVALID || stateMachine.isAttachState(NetTaskStatus.FINISHING)) {
            LogDog.e("## handle event illegal status = " + stateMachine.getState() + " task = " + netTask);
            return;
        }

        if (stateMachine.getState() == NetTaskStatus.RUN && (readyOps & SelectionKey.OP_READ) != 0) {
            onReadEvent(netTask, channel);
        }
        if (stateMachine.getState() == NetTaskStatus.RUN && (readyOps & SelectionKey.OP_WRITE) != 0) {
            onWriteEvent(netTask, channel);
        }
        if (stateMachine.getState() == NetTaskStatus.RUN && (readyOps & SelectionKey.OP_CONNECT) != 0) {
            onConnectEvent(netTask, channel);
        }
        if (stateMachine.getState() == NetTaskStatus.RUN && (readyOps & SelectionKey.OP_ACCEPT) != 0) {
            onAcceptEvent(netTask, channel);
        }

        eventDepot.depleteEvent(readyOps);
    }

    //------------------------------------------------------------------------------------------------------------------
}
