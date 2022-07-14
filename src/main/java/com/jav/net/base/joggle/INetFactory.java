package com.jav.net.base.joggle;


public interface INetFactory {

    /**
     * 是否已启动
     *
     * @return true 为已启动
     */
    boolean isOpen();

    /**
     * 打开
     */
    void open();



    INetTaskContainer getNetTaskContainer();

    /**
     * 关闭
     */
    void close();

}
