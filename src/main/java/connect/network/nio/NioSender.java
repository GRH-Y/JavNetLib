package connect.network.nio;


import connect.network.base.joggle.INetSender;
import connect.network.base.joggle.ISenderFeedback;
import connect.network.nio.buf.MultilevelBuf;
import connect.network.xhttp.XMultiplexCacheManger;
import util.IoEnvoy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NioSender implements INetSender {

    protected ISenderFeedback feedback;

    protected SocketChannel channel;

    protected SelectionKey selectionKey;

    protected Queue<Object> dataQueue = new ConcurrentLinkedQueue();

    public NioSender() {
    }

    public NioSender(SelectionKey selectionKey, SocketChannel channel) {
        setChannel(selectionKey, channel);
    }

    public void setChannel(SelectionKey selectionKey, SocketChannel channel) {
        this.selectionKey = selectionKey;
        this.channel = channel;
    }

    public void reset() {
        for (Object obj : dataQueue) {
            if (obj instanceof MultilevelBuf) {
                MultilevelBuf buf = (MultilevelBuf) obj;
                XMultiplexCacheManger.getInstance().lose(buf);
            }
        }
        dataQueue.clear();
        this.feedback = null;
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
        if (buf != null && selectionKey.isValid()) {
            try {
                if (selectionKey.interestOps() != SelectionKey.OP_WRITE) {
                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            dataQueue.add(buf);
        }
    }

    public void sendData(ByteBuffer data) {
        if (data != null && data.hasRemaining() && selectionKey.isValid()) {
            if (selectionKey.interestOps() != SelectionKey.OP_WRITE) {
                selectionKey.interestOps(SelectionKey.OP_WRITE);
            }
            dataQueue.add(data);
        }
    }

    protected void doSendData() {
        Exception exception = null;
        Object data = dataQueue.poll();
        while (data != null && exception == null) {
            try {
                if (data instanceof ByteBuffer) {
                    sendDataImp((ByteBuffer) data);
                } else if (data instanceof MultilevelBuf) {
                    MultilevelBuf buf = (MultilevelBuf) data;
                    ByteBuffer[] buffers = buf.getAllBuf();
                    for (ByteBuffer buffer : buffers) {
                        buffer.flip();
                        sendDataImp(buffer);
                    }
                }
            } catch (Exception e) {
                exception = e;
                e.printStackTrace();
            } finally {
                if (feedback != null) {
                    feedback.onSenderFeedBack(this, data, exception);
                }
                if (exception == null) {
                    data = dataQueue.poll();
                }
            }
        }
        if (exception == null) {
            try {
                selectionKey.interestOps(SelectionKey.OP_READ);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    protected void sendDataImp(ByteBuffer data) throws IOException {
        IoEnvoy.writeToFull(channel, data);
    }

}
