package com.jav.net.state.joggle;

/**
 * 状态接口
 *
 * @param <T>
 * @author yyz
 */
public interface IState<T> {

    /**
     * 获取状态实体
     *
     * @return 实体
     */
    T getStateEntity();

}
