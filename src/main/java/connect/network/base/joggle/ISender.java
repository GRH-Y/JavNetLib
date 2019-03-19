package connect.network.base.joggle;



/**
 * 发送者接口
 *
 * @author yyz
 */
public interface ISender {

    /**
     * 发送数据
     * @param data
     */
    void sendData(byte[] data);

    /**
     * 立即发送数据
     * @param data
     */
    void sendDataNow(byte[] data);

}
