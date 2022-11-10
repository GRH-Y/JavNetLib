package com.jav.net.state.joggle;

/**
 * 状态更新监听器
 *
 * @author yyz
 */
public interface IStateChangeListener<T> {

    /**
     * 回调状态更新的最新值
     *
     * @param state 当前状态
     */
    void onStateChange(T state);
}
