package connect.network.base.joggle;


/**
 * 发送者接口
 *
 * @author yyz
 */
public interface INetSender {

    /**
     * 设置发送回调反馈
     * @param feedback
     */
    void setSenderFeedback(ISenderFeedback feedback);

    /**
     * 发送数据
     *
     * @param data
     */
    void sendData(byte[] data);

}
