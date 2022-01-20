package com.currency.net.base.joggle;


import com.currency.net.base.BaseNetTask;

public interface INetFactory<T extends BaseNetTask> {

    /**
     * 是否已启动
     *
     * @return true 为已启动
     */
    boolean isOpen();

    /**
     * 打开
     */
    INetTaskContainer<T> open();

    /**
     * 关闭
     */
    void close();

}
