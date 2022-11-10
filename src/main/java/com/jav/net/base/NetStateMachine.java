package com.jav.net.base;


import com.jav.net.state.StateMachine;
import com.jav.net.state.StateResult;
import com.jav.net.state.joggle.IState;
import com.jav.net.state.joggle.IStateChangeListener;
import com.jav.net.state.joggle.IStateWait;

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
    public Integer getStatus() {
        AtomicInteger state = mStateImp.getStateEntity();
        return state.get();
    }


    @Override
    public boolean setStatus(Integer newState) {
        AtomicInteger state = mStateImp.getStateEntity();
        state.set(newState);
        boolean ret = state.get() == newState;
        if (ret) {
            notifyWait();
            notifyStateChange();
        }
        return ret;
    }

    @Override
    public boolean attachState(Integer status) {
        AtomicInteger state = mStateImp.getStateEntity();
        int currentStatus = state.get();
        state.set(currentStatus | status);
        boolean ret = state.get() == currentStatus + status;
        if (ret) {
            notifyWait();
            notifyStateChange();
        }
        return ret;
    }

    @Override
    public boolean isAttachState(Integer checkStatus) {
        AtomicInteger state = mStateImp.getStateEntity();
        int currentStatus = state.get();
        if (checkStatus == 0) {
            return currentStatus == checkStatus;
        }
        return (currentStatus & checkStatus) == checkStatus;
    }

    @Override
    public boolean detachState(Integer status) {
        AtomicInteger state = mStateImp.getStateEntity();
        int currentStatus = state.get();
        int unStatus = ~status;
        state.set(currentStatus & unStatus);
        boolean ret = currentStatus - state.get() == status;
        if (ret) {
            notifyWait();
            notifyStateChange();
        }
        return ret;
    }

    @Override
    public boolean updateState(Integer expectState, Integer setState) {
        StateResult result = updateState(expectState, setState, null);
        return result.getUpdateValue();
    }


    /**
     * 更新状态
     *
     * @param expectStatus 期望值
     * @param setStatus    设置值
     * @param wait         如果更新失败则进入等待
     * @return
     */
    @Override
    public StateResult updateState(Integer expectStatus, Integer setStatus, IStateWait wait) {
        StateResult result = new StateResult();
        updateValue(expectStatus, setStatus, result);
        for (boolean isTry = !result.getUpdateValue(); isTry && wait != null; ) {
            isTry = wait.onWaitFactor(this, setStatus, result);
            if (isTry) {
                // 尝试设置状态
                updateValue(expectStatus, setStatus, result);
            }
        }
        if (result.getUpdateValue()) {
            notifyWait();
        }
        if (result.getUpdateValue()) {
            notifyStateChange();
        }
        return result;
    }


    @Override
    public void updateAsyncState(Integer expectStatus, Integer setStatus, IStateChangeListener<Integer> listener) {
        StateResult result = new StateResult();
        updateValue(expectStatus, setStatus, result);

        if (listener != null) {
            if (result.getUpdateValue()) {
                // 如果更新值成功，则直接回调
                listener.onStateChange(getStatus());
            } else {
                // 如果更新值失败，则注册监听器
                regStateChangeListener(listener);
            }
        }
        if (result.getUpdateValue()) {
            notifyWait();
            notifyStateChange();
        }
    }

    @Override
    protected void updateValue(Integer expectStatus, Integer setStatus, StateResult result) {
        AtomicInteger state = mStateImp.getStateEntity();
        boolean ret = state.compareAndSet(expectStatus, setStatus);
        setUpdateValue(result, ret);
    }
}
