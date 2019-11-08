package connect.network.nio;


/**
 * 高性能的发送者 (独立线程处理发送数据)
 */
public class NioHPCSender extends NioSender {

    public NioHPCSender(NioClientTask clientTask) {
        super(clientTask);
        SimpleSendTask.getInstance().open();
    }

    /**
     * 发送数据
     *
     * @param data
     */
    @Override
    public void sendData(byte[] data) {
        SimpleSendTask.getInstance().sendData(this, data);
    }

}
