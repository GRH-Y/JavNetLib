package com.jav.net.nio;

import com.jav.net.base.NetTaskStatus;
import com.jav.net.base.joggle.INetTaskComponent;
import com.jav.net.entity.FactoryContext;
import com.jav.net.state.StateResult;
import com.jav.net.state.joggle.IStateMachine;
import com.jav.net.state.joggle.IStateWait;

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
        execRemoverTask(netTask);
    }

    private void execRemoverTask(T netTask) {
        // 等待task状态进入IDLING再执行回收
        // LogDog.d("## execRemoverTask start netTask = " + netTask);
        IStateMachine<Integer> stateMachine = netTask.getStatusMachine();
        if (stateMachine.getStatus() < NetTaskStatus.RUN) {
            // 避免多次执行
            return;
        }
        StateResult result = stateMachine.updateState(NetTaskStatus.IDLING, NetTaskStatus.FINISHING, new IStateWait<Integer>() {
            @Override
            public boolean onWaitFactor(IStateMachine<Integer> machine, Integer setStatus, StateResult result) {
                // LogDog.d("## onWaitFactor current state = " + machine.getStatus() + " netTask = " + netTask);
                if (machine.getStatus() == NetTaskStatus.RUN) {
                    // 只有当前状态是run才进入等待
                    machine.enterWait();
                    return true;
                }
                // else if (machine.getStatus() <= NetTaskStatus.FINISHING) {
                //     // 如果该任务已经被处理,则不要继续重复执行.出现这种情况应该是再等待run的状态时再次添加进销毁队列
                //     result.setUserValue(true);
                //     return false;
                // }
                return false;
            }
        });

        if (result.getUpdateValue()) {
            // LogDog.d("## execRemoverTask end netTask = " + netTask);
            onDisconnectTask(netTask);
            stateMachine.setStatus(NetTaskStatus.INVALID);
            onRecoveryTask(netTask);
        }
    }

}
