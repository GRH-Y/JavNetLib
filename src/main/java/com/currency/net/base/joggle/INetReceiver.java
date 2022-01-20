package com.currency.net.base.joggle;

/**
 * 读取数据回调
 *
 * @author yyz
 */
public interface INetReceiver<T> {

    void onReceiveFullData(T buf, Throwable e);
}
