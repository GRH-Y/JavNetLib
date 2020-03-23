package connect.network.nio;


import connect.network.base.joggle.INetSender;
import util.IoEnvoy;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class NioSender implements INetSender {

    protected SocketChannel channel;

    public NioSender() {
    }

    public NioSender(SocketChannel channel) {
        this.channel = channel;
    }

    public void setChannel(SocketChannel channel) {
        this.channel = channel;
    }

    /**
     * 发送数据
     *
     * @param data
     */
    @Override
    public void sendData(byte[] data) throws IOException {
        sendDataImp(data);
    }

    protected void sendDataImp(byte[] data) throws IOException {
        if (data == null) {
            return;
        }
        channel.socket().setSendBufferSize(data.length);
        IoEnvoy.writeToFull(channel, data);
    }

}
