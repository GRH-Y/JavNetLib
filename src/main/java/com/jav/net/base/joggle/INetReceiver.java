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
     * @return 返回true 则回收buf
     */
    boolean onReceiveFullData(T buf, Throwable e);

}
