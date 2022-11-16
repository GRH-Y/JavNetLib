package com.jav.net.base;


import com.jav.common.state.StateMachine;
import com.jav.common.state.joggle.IState;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * NetStateMachine 网络任务状态机
 *
 * @author yyz
 * @version 1.0
 * @see NetTaskStatus
 */
public class NetStateMachine extends StateMachine<AtomicInteger, Integer> {


    public NetStateMachine(IState<AtomicInteger> state) {
        super(state);
    }

    /**
     * 获取当前的状态值
     *
     * @return 状态值
     */
    @Override
    public Integer getState() {
        AtomicInteger state = mStateImp.getStateEntity();
        return state.get();
    }

    @Override
    public boolean isAttachState(Integer state) {
        int currentState = getState();
        return (currentState & state) == state;
    }


    @Override
    public boolean updateState(Integer expectState, Integer setState) {
        AtomicInteger state = mStateImp.getStateEntity();
        boolean ret = state.compareAndSet(expectState, setState);
        if (ret) {
            notifyStateChange();
            notifyWait();
        }
        return ret;
    }

    @Override
    public boolean attachState(Integer attachStatus) {
        AtomicInteger state = mStateImp.getStateEntity();
        int newState = state.addAndGet(attachStatus);
        boolean ret = (newState & attachStatus) == attachStatus;
        if (ret) {
            notifyStateChange();
            notifyWait();
        }
        return ret;
    }

    @Override
    public boolean detachState(Integer detachState) {
        AtomicInteger state = mStateImp.getStateEntity();
        int newState = state.addAndGet(-detachState);
        boolean ret = (newState & detachState) == 0;
        if (ret) {
            notifyStateChange();
            notifyWait();
        }
        return ret;
    }

}
