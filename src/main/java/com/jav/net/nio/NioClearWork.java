package com.jav.net.nio;

import com.jav.common.log.LogDog;
import com.jav.common.state.joggle.IControlStateMachine;
import com.jav.net.base.NetTaskStatus;
import com.jav.net.base.joggle.INetTaskComponent;
import com.jav.net.entity.FactoryContext;

import java.nio.channels.SocketChannel;

/**
 * NioClearWork 只处理断开连接移除请求的事件
 *
 * @author yyz
 */
public class NioClearWork<T extends NioClientTask, C extends SocketChannel> extends NioClientWork<T, C> {

    protected NioClearWork(FactoryContext context) {
        super(context);
    }

    @Override
    protected void init() {
        // 重写init方法，避免创建Selector
    }

    private boolean checkIdle() {
        INetTaskComponent container = mFactoryContext.getNetTaskComponent();
        return container.isDestroyQueueEmpty();
    }


    @Override
    protected void onDestroyTask() {
        if (checkIdle()) {
            // 没有任务则进入休眠
            FactoryContext context = getFactoryContext();
            NioNetEngine engine = context.getNetEngine();
            engine.pauseEngine();
            LogDog.w("## enter sleep !!! ");
        } else {
            super.onDestroyTask();
        }
    }


    @Override
    protected void destroyTaskImp(T netTask) {
        try {
            execRemoverTask(netTask);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    private void execRemoverTask(T netTask) {
        // 等待task状态进入IDLING再执行回收
        IControlStateMachine<Integer> stateMachine = netTask.getStatusMachine();
        if (stateMachine.getState() == NetTaskStatus.INVALID) {
            // 避免多次执行
            LogDog.w("## execRemoverTask task state is  INVALID !!!");
            return;
        }
        if (stateMachine.isAttachState(NetTaskStatus.IDLING)) {
            onDisconnectTask(netTask);
            while (!stateMachine.updateState(stateMachine.getState(), NetTaskStatus.INVALID)) {
                LogDog.w("## execRemoverTask updateState state = " + stateMachine.getState());
            }
            onRecoveryTask(netTask);
//                LogDog.i("## execRemoverTask task complete, task = " + netTask);
        } else {
            while (checkIdle() && stateMachine.isAttachState(NetTaskStatus.RUN)) {
                //如果当前队列没有任务则进入等待模式
                LogDog.w("##  The current task status is running, need wait = " + " task = " + netTask);
                stateMachine.enterWait();
                break;
            }
            if (stateMachine.getState() != NetTaskStatus.INVALID) {
                LogDog.w("## The current task status is running, " +
                        "put the task back in the queue and process it next time !  task = " + netTask);
                INetTaskComponent container = mFactoryContext.getNetTaskComponent();
                container.addUnExecTask(netTask);
            }
        }
//        LogDog.w("## execRemoverTask found error state = " + stateMachine.getState() + " task = " + netTask);
    }

}
