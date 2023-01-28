package com.jav.net.base.joggle;

/**
 * 接收数据回调接口
 *
 * @author yyz
 */
public interface INetReceiver<T> {

    /**
     * 接收数据回调
     *
     * @param buf 缓存数据
     */
    void onReceiveFullData(T buf);


    /**
     * 异常回调
     *
     * @param e 异常
     */
    void onReceiveError(Throwable e);

}
