package connect.network.base;

import connect.network.base.joggle.INetSender;
import connect.network.base.joggle.ISenderFeedback;

public abstract class BaseNetSender implements INetSender {

    public final static int SEND_COMPLETE = 0, SEND_FAIL = -1, SEND_CHANNEL_BUSY = 1;

    protected ISenderFeedback feedback;

    @Override
    public void setSenderFeedback(ISenderFeedback feedback) {
        this.feedback = feedback;
    }

}
