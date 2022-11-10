package com.jav.net.state.joggle;

import com.jav.net.state.StateResult;

/**
 * 状态机接口
 *
 * @param <T>
 * @author yyz
 */
public interface IStateMachine<T> {

    /**
     * 进入等待,状态发生变化会唤醒
     */
    void enterWait();

    /**
     * 获取状态
     *
     * @return
     */
    T getStatus();

    /**
     * 设置状态
     *
     * @param newStatus 新状态
     * @return true设置成功
     */
    boolean setStatus(T newStatus);

    /**
     * 在当前的状态附加多个状态
     *
     * @param attachStatus 要附加的状态
     */
    boolean attachState(T attachStatus);


    /**
     * 检查当前的状态是否附加该状态
     *
     * @param checkStatus 要检查的状态
     * @return true是含有
     */
    boolean isAttachState(T checkStatus);


    /**
     * 移除附加的状态
     *
     * @param detachState 要移除掉的附加状态
     */
    boolean detachState(T detachState);

    /**
     * 更新状态
     *
     * @param expectState 期望当前的状态
     * @param setState    要设置的状态
     * @return true设置成功
     */
    boolean updateState(T expectState, T setState);

    /**
     * 更新状态
     *
     * @param expectStatus 期望当前的状态
     * @param setStatus    要设置的状态
     * @param wait         如果更新失败则进入等待
     * @return 返回结果
     */
    StateResult updateState(T expectStatus, T setStatus, IStateWait wait);

    /**
     * 异步更新状态
     *
     * @param expectStatus 期望当前的状态
     * @param setStatus    要设置的状态
     * @param listener     如果状态发生改变回调监听器
     */
    void updateAsyncState(T expectStatus, T setStatus, IStateChangeListener<T> listener);
}
