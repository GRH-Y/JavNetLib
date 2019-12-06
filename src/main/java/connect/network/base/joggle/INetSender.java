package connect.network.base.joggle;



/**
 * 发送者接口
 *
 * @author yyz
 */
public interface INetSender {

    /**
     * 发送数据
     * @param data
     */
    void sendData(byte[] data);

}
