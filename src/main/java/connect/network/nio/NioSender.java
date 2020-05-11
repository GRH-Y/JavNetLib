package connect.network.nio;


import connect.network.base.joggle.INetSender;
import connect.network.base.joggle.ISenderFeedback;
import connect.network.nio.buf.MultilevelBuf;
import util.IoEnvoy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NioSender implements INetSender {

    protected ISenderFeedback feedback;

    protected SocketChannel channel;

    public NioSender() {
    }

    public NioSender(SocketChannel channel) {
        setChannel(channel);
    }

    public void setChannel(SocketChannel channel) {
        this.channel = channel;
    }

    @Override
    public void setSenderFeedback(ISenderFeedback feedback) {
        this.feedback = feedback;
    }

    /**
     * 发送数据
     *
     * @param data
     */
    @Override
    public void sendData(byte[] data) {
        if (data != null && data.length > 0) {
            ByteBuffer buffer = ByteBuffer.allocateDirect(data.length);
            buffer.put(data);
            buffer.flip();
            sendData(buffer);
        }
    }

    public void sendData(MultilevelBuf buf) {
        if (buf != null) {
            ByteBuffer[] buffers = buf.getAllBuf();
            for (ByteBuffer buffer : buffers) {
                buffer.flip();
                sendData(buffer);
            }
        }
    }

    public void sendData(ByteBuffer data) {
        if (data != null && data.hasRemaining()) {
            Exception exception = null;
            try {
                sendDataImp(data);
            } catch (Exception e) {
                exception = e;
                e.printStackTrace();
            } finally {
                if (feedback != null) {
                    feedback.onSenderFeedBack(this, data, exception);
                }
            }
        }
    }

    protected void sendDataImp(ByteBuffer data) throws IOException {
        IoEnvoy.writeToFull(channel, data);
    }

}
