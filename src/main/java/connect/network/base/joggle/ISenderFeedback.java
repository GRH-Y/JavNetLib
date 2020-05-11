package connect.network.base.joggle;

import java.nio.ByteBuffer;

public interface ISenderFeedback {

    /**
     * 发送数据回调反馈
     * @param sender
     * @param data
     * @param e
     */
    void onSenderFeedBack(INetSender sender, ByteBuffer data, Throwable e);
}
