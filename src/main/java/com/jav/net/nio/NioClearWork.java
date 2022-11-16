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
            return;
        }

        do {
            if (stateMachine.isAttachState(NetTaskStatus.IDLING)) {
                onDisconnectTask(netTask);
                while (!stateMachine.updateState(stateMachine.getState(), NetTaskStatus.INVALID)) {
                    LogDog.w("## execRemoverTask updateState state = " + stateMachine.getState());
                }
                onRecoveryTask(netTask);
                return;
            }
            LogDog.w("## execRemoverTask need wait = " + stateMachine.getState() + " task = " + netTask);
            while (stateMachine.isAttachState(NetTaskStatus.RUN)) {
                LogDog.w("## execRemoverTask enterWait state = " + stateMachine.getState() + " task = " + netTask);
                stateMachine.enterWait();
            }
        } while (true);

    }

}
