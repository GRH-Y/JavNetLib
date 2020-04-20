package connect.network.nio;


import connect.network.base.joggle.INetSender;
import connect.network.nio.buf.MultilevelBuf;
import util.IoEnvoy;

import java.io.IOException;
import java.nio.ByteBuffer;
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

    public void sendData(MultilevelBuf buf) throws IOException {
        ByteBuffer[] buffers = buf.getAllBuf();
        for (ByteBuffer buffer : buffers) {
            buffer.flip();
            IoEnvoy.writeToFull(channel, buffer);
        }
    }

    protected void sendDataImp(byte[] data) throws IOException {
        if (channel != null && data != null) {
            IoEnvoy.writeToFull(channel, data);
        }
    }

}
