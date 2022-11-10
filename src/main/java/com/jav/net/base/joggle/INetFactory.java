package com.jav.net.base.joggle;

import com.jav.net.base.BaseNetTask;

/**
 * 网络工厂，控制开启,关闭和获取任务容器控制添加网络任务
 *
 * @author yyz
 */
public interface INetFactory<T extends BaseNetTask> {

    /**
     * 是否已启动
     *
     * @return true 为已启动
     */
    boolean isOpen();

    /**
     * 打开
     *
     * @return 返回接口
     */
    INetFactory<T> open();


    /**
     * 获取task组件
     *
     * @return
     */
    INetTaskComponent<T> getNetTaskComponent();

    /**
     * 关闭
     */
    void close();

}
