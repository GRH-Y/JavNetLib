package com.jav.net.base;

import com.jav.net.base.joggle.INetSender;
import com.jav.net.base.joggle.ISenderFeedback;

public abstract class AbsNetSender<T> implements INetSender<T> {

    public final static int SEND_COMPLETE = 0, SEND_FAIL = -1, SEND_CHANNEL_BUSY = -2;

    protected ISenderFeedback mFeedback;

    @Override
    public void setSenderFeedback(ISenderFeedback feedback) {
        this.mFeedback = feedback;
    }

    /**
     * 写事件会触发该回调
     *
     * @throws Throwable
     */
    protected abstract void onSendNetData() throws Throwable;
}
