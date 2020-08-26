package connect.network.base.joggle;

import java.nio.ByteBuffer;

public interface ISenderFeedback {

    /**
     * 发送数据回调反馈
     *
     * @param sender
     * @param data
     */
    void onSenderFeedBack(INetSender sender, ByteBuffer data, Throwable e);

//    /**
//     * 发送数据出错回调
//     *
//     * @param e
//     */
//    void onSenderError(Throwable e);
}
