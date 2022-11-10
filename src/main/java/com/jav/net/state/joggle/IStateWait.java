package com.jav.net.state.joggle;

import com.jav.net.state.StateResult;

/**
 * 状态机当前状态不满足回调
 *
 * @author yyz
 */
public interface IStateWait<T> {

    /**
     * 不满足条件,等待回调
     *
     * @param machine   状态机
     * @param setStatus 期望目标状态
     * @param result    返回结果
     * @return 返回true 再次尝试更新状态
     */
    boolean onWaitFactor(IStateMachine<T> machine, T setStatus, StateResult result);
}
