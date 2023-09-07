package com.jav.net.nio;

import com.jav.common.log.LogDog;
import com.jav.common.state.joggle.IControlStateMachine;
import com.jav.net.base.BaseNetWork;
import com.jav.net.base.FactoryContext;
import com.jav.net.base.NetTaskStatus;
import com.jav.net.base.joggle.INetTaskComponent;

/**
 * NioClearWork 只处理断开连接移除请求的事件
 *
 * @author yyz
 */
public class NioClearWork extends BaseNetWork<NioClientTask> {

    protected NioClearWork(FactoryContext context) {
        super(context);
    }

    @Override
    public void onWorkBegin() {

    }

    private boolean checkIdle() {
        INetTaskComponent<NioClientTask> container = mFactoryContext.getNetTaskComponent();
        return container.isDestroyQueueEmpty();
    }


    @Override
    public void onDisconnectNetTaskEnd() {
        if (checkIdle()) {
            // 没有任务则进入休眠
            FactoryContext context = getFactoryContext();
            NioNetEngine engine = context.getNetEngine();
            engine.pauseEngine();
            LogDog.w("## enter sleep !!! ");
        } else {
            super.onDisconnectNetTaskEnd();
        }
    }

    @Override
    public void onWorkEnd() {
        INetTaskComponent<NioClientTask> container = mFactoryContext.getNetTaskComponent();
        container.clearAllQueue();
    }

    @Override
    public void onConnectTask(NioClientTask netTask) {

    }

    @Override
    public boolean onDisconnectTask(NioClientTask netTask) {
        return execDisconnectTask(netTask);
    }


    private boolean execDisconnectTask(NioClientTask netTask) {
        // 等待task状态进入IDLING再执行回收
        IControlStateMachine<Integer> stateMachine = netTask.getStatusMachine();
        if (stateMachine.getState() == NetTaskStatus.INVALID) {
            // 避免多次执行
            LogDog.w("## disconnect task state is  INVALID !!!");
            return true;
        }
        if (stateMachine.isAttachState(NetTaskStatus.IDLING)) {
            disconnectImp(netTask);
        } else {
            while (checkIdle() && stateMachine.isAttachState(NetTaskStatus.RUN)) {
                //如果当前队列没有任务则进入等待模式
                LogDog.w("##  The current task status = " + stateMachine.getState() + " need wait " + " task = " + netTask);
                stateMachine.enterWait();
                break;
            }
            if (stateMachine.isAttachState(NetTaskStatus.IDLING)) {
                disconnectImp(netTask);
            } else if (stateMachine.getState() != NetTaskStatus.INVALID) {
                LogDog.w("## put the task back in the queue and process it next time ,task = " + netTask);
                INetTaskComponent<NioClientTask> container = mFactoryContext.getNetTaskComponent();
                container.addUnExecTask(netTask);
                return false;
            }
        }
        return true;
//        LogDog.w("## execRemoverTask found error state = " + stateMachine.getState() + " task = " + netTask);
    }

    private void disconnectImp(NioClientTask netTask) {
        IControlStateMachine<Integer> stateMachine = netTask.getStatusMachine();
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
                if (netTask.getChannel().isConnected()) {
                    try {
                        netTask.getChannel().shutdownInput();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    try {
                        netTask.getChannel().shutdownOutput();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
                try {
                    netTask.getChannel().close();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            netTask.onRecovery();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        stateMachine.updateState(stateMachine.getState(), NetTaskStatus.INVALID);
        LogDog.i("## disconnect task complete, task = " + netTask);
    }

}
