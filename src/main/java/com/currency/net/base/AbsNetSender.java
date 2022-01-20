package com.currency.net.base;

import com.currency.net.base.joggle.INetSender;
import com.currency.net.base.joggle.ISenderFeedback;

public abstract class AbsNetSender implements INetSender {

    public final static int SEND_COMPLETE = 0, SEND_FAIL = -1, SEND_CHANNEL_BUSY = 1;

    protected ISenderFeedback feedback;

    @Override
    public void setSenderFeedback(ISenderFeedback feedback) {
        this.feedback = feedback;
    }

}
