package connect.network.nio;


import java.nio.channels.SocketChannel;

/**
 * 高性能的发送者 (独立线程处理发送数据)
 */
public class NioHPCSender extends NioSender {

    public NioHPCSender() {
        SimpleSendTask.getInstance().open();
    }

    public NioHPCSender(SocketChannel channel) {
        super(channel);
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
