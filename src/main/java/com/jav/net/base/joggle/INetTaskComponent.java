package com.jav.net.base.joggle;

import com.jav.net.base.BaseNetTask;

/**
 * 网络任务组件接口,用于添加网络任务,维护队列
 *
 * @param <T>
 * @author yyz
 */
public interface INetTaskComponent<T extends BaseNetTask> {

    /**
     * 添加nio任务
     *
     * @param task
     * @return true 添加成功
     */
    boolean addExecTask(T task);

    /**
     * 移除nio任务
     *
     * @param task
     * @return true 添加成功
     */
    boolean addUnExecTask(T task);


    /**
     * 释放资源
     */
    void release();
}
