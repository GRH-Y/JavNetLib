package com.jav.net.base;

import com.jav.net.base.joggle.INetReceiver;

/**
 * 基本网络接收者
 *
 * @param <C>
 * @param <T>
 * @author yyz
 */
public abstract class AbsNetReceiver<C, T> {

    protected INetReceiver<T> mReceiver;

    /**
     * 设置数据接收
     * @param receiver
     */
    public void setDataReceiver(INetReceiver<T> receiver) {
        this.mReceiver = receiver;
    }

    /**
     * 接收数据就绪，由network触发回调
     *
     * @param channel
     * @throws Throwable
     */
    protected abstract void onReadNetData(C channel) throws Throwable;
}
