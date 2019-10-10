package connect.network.nio;


import connect.network.base.joggle.ISender;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NioSender implements ISender {

    protected SocketChannel channel;
    protected NioClientTask clientTask;

    /**
     * 发送数据
     * @param data
     */
    @Override
    public void sendData(byte[] data) {
        sendDataNow(data);
    }

    /**
     * 发送数据
     * @param data
     */
    @Override
    public void sendDataNow(byte[] data) {
        if (data != null && channel != null && channel.isOpen()) {
            try {
                channel.write(ByteBuffer.wrap(data));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void setChannel(SocketChannel channel) {
        this.channel = channel;
    }

    protected void setClientTask(NioClientTask clientTask) {
        this.clientTask = clientTask;
    }

    public SocketChannel getChannel() {
        return channel;
    }

}
