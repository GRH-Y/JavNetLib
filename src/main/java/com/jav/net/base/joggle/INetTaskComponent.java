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
     * @return 0 立即成功, 1 等待销毁线程处理 , 2 添加移除任务失败
     */
    int addUnExecTask(T task);

    /**
     * 添加任务队列是否为空
     *
     * @return
     */
    boolean isConnectQueueEmpty();

    /**
     * 移除任务队列是否为空
     *
     * @return
     */
    boolean isDestroyQueueEmpty();

    /**
     * 获取添加队列的task
     *
     * @return
     */
    T pollConnectTask();

    /**
     * 获取移除队列的task
     *
     * @return
     */
    T pollDestroyTask();


    /**
     * 清除所有的队列数据
     */
    void clearAllQueue();
}
