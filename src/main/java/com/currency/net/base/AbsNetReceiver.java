package com.currency.net.base;

import com.currency.net.base.joggle.INetReceiver;

public abstract class AbsNetReceiver<C, T> {

    protected INetReceiver<T> mReceiverCallBack;

    public void setDataReceiver(INetReceiver<T> receiver) {
        this.mReceiverCallBack = receiver;
    }

    protected abstract void onReadNetData(C channel) throws Throwable;
}
