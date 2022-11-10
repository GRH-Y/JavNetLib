package com.jav.net.state;

import com.jav.net.state.joggle.IState;
import com.jav.net.state.joggle.IStateChangeListener;
import com.jav.net.state.joggle.IStateMachine;
import com.jav.net.state.joggle.IStateWait;

import java.util.LinkedList;
import java.util.List;

/**
 * 状态机
 *
 * @param <T> 状态本体的实体
 * @param <V> 设置状态的实体
 * @author yyz
 */
public abstract class StateMachine<T, V> implements IStateMachine<V> {

    /**
     * 状态对象
     */
    protected final IState<T> mStateImp;

    /**
     * 状态监听器集合
     */
    protected List<IStateChangeListener<V>> mListenerList;

    public StateMachine(IState<T> state) {
        mStateImp = state;
        mListenerList = new LinkedList<>();
    }

    /**
     * 设置状态监听器
     *
     * @param mListener
     */
    public void regStateChangeListener(IStateChangeListener mListener) {
        if (mListener == null) {
            return;
        }
        synchronized (mListenerList) {
            mListenerList.add(mListener);
        }
    }

    public void unRegStateChangeListener(IStateChangeListener mListener) {
        if (mListener == null) {
            return;
        }
        synchronized (mListenerList) {
            mListenerList.remove(mListener);
        }
    }

    protected void notifyStateChange() {
        synchronized (mListenerList) {
            for (IStateChangeListener listener : mListenerList) {
                listener.onStateChange(getStatus());
            }
        }
    }


    protected void setUpdateValue(StateResult result, boolean isSuccess) {
        result.setUpdateValue(isSuccess);
    }

    /**
     * 进入等待
     */
    @Override
    public void enterWait() {
        synchronized (mStateImp) {
            try {
                mStateImp.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 唤醒等待
     */
    protected void notifyWait() {
        synchronized (mStateImp) {
            mStateImp.notifyAll();
        }
    }


    @Override
    public StateResult updateState(V expectStatus, V setStatus, IStateWait wait) {
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
            notifyStateChange();
        }
        return result;
    }

    @Override
    public void updateAsyncState(V expectStatus, V setStatus, IStateChangeListener<V> listener) {
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

    protected abstract void updateValue(V expectStatus, V setStatus, StateResult result);
}
