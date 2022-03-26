package com.currency.net.base;

import com.currency.net.base.joggle.INetSender;
import com.currency.net.base.joggle.ISenderFeedback;

public abstract class AbsNetSender implements INetSender {

    public final static int SEND_COMPLETE = 0, SEND_FAIL = -1, SEND_CHANNEL_BUSY = -2;

    protected ISenderFeedback mFeedback;

    @Override
    public void setSenderFeedback(ISenderFeedback feedback) {
        this.mFeedback = feedback;
    }

    /**
     * 读事件会触发该回调
     * @throws Throwable
     */
    protected abstract void onSendNetData() throws Throwable;

}
