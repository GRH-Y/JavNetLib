package com.jav.net.nio;

import com.jav.common.state.joggle.IControlStateMachine;
import com.jav.net.base.FactoryContext;
import com.jav.net.base.NetTaskStatus;
import com.jav.net.base.joggle.NetErrorType;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * NioAcceptWork 只处理读写请求的事件
 *
 * @author yyz
 */
public class NioReadWriteNetWork extends NioClientWork {

    private final List<NioClientTask> mWaiteRegNetTaskList;

    protected NioReadWriteNetWork(FactoryContext context) {
        super(context);
        mWaiteRegNetTaskList = new ArrayList<>();
    }

    public void registerReadWriteEvent(NioClientTask netTask) {
        synchronized (mWaiteRegNetTaskList) {
            mWaiteRegNetTaskList.add(netTask);
        }
        mSelector.wakeup();
    }

    private void regWaitNetTask() {
        synchronized (mWaiteRegNetTaskList) {
            for (NioClientTask netTask : mWaiteRegNetTaskList) {
                IControlStateMachine<Integer> stateMachine = netTask.getStatusMachine();
                if (stateMachine.isAttachState(NetTaskStatus.FINISHING)
                        || stateMachine.getState() == NetTaskStatus.INVALID) {
                    continue;
                }
                try {
                    registerEvent(netTask, netTask.getChannel());
                } catch (IOException e) {
                    stateMachine.detachState(NetTaskStatus.RUN);
                    stateMachine.attachState(NetTaskStatus.IDLING);
                    callChannelError(netTask, NetErrorType.CONNECT, e);
                }
            }
            mWaiteRegNetTaskList.clear();
        }
    }

    public int getPressureValue() {
        return mSelector.keys().size();
    }

    @Override
    protected void onSelectEvent() {
        int count = 0;
        try {
            count = mSelector.select();
        } catch (Exception e) {
            e.printStackTrace();
        }
        regWaitNetTask();
        if (count > 0) {
            for (Iterator<SelectionKey> iterator = mSelector.selectedKeys().iterator(); iterator.hasNext(); iterator.remove()) {
                SelectionKey selectionKey = iterator.next();
                onSelectionKey(selectionKey);
            }
        }
    }

}
