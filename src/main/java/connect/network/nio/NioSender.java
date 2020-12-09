package connect.network.nio;


import connect.network.base.joggle.INetSender;
import connect.network.base.joggle.ISenderFeedback;
import connect.network.xhttp.XMultiplexCacheManger;
import connect.network.xhttp.utils.MultilevelBuf;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

public class NioSender implements INetSender {

    protected ISenderFeedback feedback;

    protected SocketChannel channel;

    protected SelectionKey selectionKey;

    public final static int SEND_COMPLETE = 0, SEND_FAIL = -1, SEND_CHANNEL_BUSY = 1;

    protected LinkedList<Object> dataQueue = new LinkedList();

    public NioSender() {
    }

    public NioSender(SelectionKey selectionKey, SocketChannel channel) {
        setChannel(selectionKey, channel);
    }

    public void setChannel(SelectionKey selectionKey, SocketChannel channel) {
        this.selectionKey = selectionKey;
        this.channel = channel;
    }

    public boolean isSendDataEmpty() {
        return dataQueue.isEmpty();
    }

    public void clear() {
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
        boolean ret = false;
        if (buf != null && selectionKey.isValid() && buf.isHasData()) {
            try {
                if (selectionKey.interestOps() != SelectionKey.OP_WRITE) {
                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                }
                ret = dataQueue.add(buf);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        if (!ret) {
            XMultiplexCacheManger.getInstance().lose(buf);
        }
    }

    public void sendData(ByteBuffer data) {
        if (data != null && selectionKey.isValid() && data.hasRemaining()) {
            if (selectionKey.interestOps() != SelectionKey.OP_WRITE) {
                selectionKey.interestOps(SelectionKey.OP_WRITE);
            }
            dataQueue.add(data);
        }
    }

    protected void doSendData() throws Throwable {
        Throwable exception = null;
        Object data = dataQueue.pollFirst();
        int ret = SEND_COMPLETE;
        while (data != null && exception == null) {
            try {
                if (data instanceof ByteBuffer) {
                    ret = sendDataImp((ByteBuffer) data);
                    if (ret == SEND_CHANNEL_BUSY) {
                        dataQueue.addFirst(data);
                        return;
                    }
                } else if (data instanceof MultilevelBuf) {
                    MultilevelBuf buf = (MultilevelBuf) data;
                    ByteBuffer[] sendDataBuf = buf.getTmpCacheBuf();
                    if (sendDataBuf == null) {
                        sendDataBuf = buf.getUseBuf(true);
                    }
                    for (ByteBuffer buffer : sendDataBuf) {
                        if (buffer.hasRemaining()) {
                            ret = sendDataImp(buffer);
                            if (ret == SEND_CHANNEL_BUSY) {
                                buf.setTmpCacheBuf(sendDataBuf);
                                dataQueue.addFirst(data);
                                return;
                            }
                        }
                    }
                    buf.setTmpCacheBuf(null);
                    buf.setBackBuf(sendDataBuf);
                }
            } catch (Throwable e) {
                exception = e;
                e.printStackTrace();
            }
            if (feedback != null && ret != SEND_CHANNEL_BUSY) {
                feedback.onSenderFeedBack(this, data, exception);
            }
            if (exception == null) {
                data = dataQueue.pollFirst();
            }
        }
        if (exception == null) {
            if (ret == SEND_COMPLETE) {
                try {
                    selectionKey.interestOps(SelectionKey.OP_READ);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            throw exception;
        }
    }


    protected int sendDataImp(ByteBuffer buffers) throws Throwable {
        if (channel == null || buffers == null || !channel.isConnected()) {
            return SEND_FAIL;
        }
        do {
            long ret = channel.write(buffers);
            if (ret < 0) {
                throw new IOException("## failed to send data. The socket channel may be closed !!! ");
            } else if (ret == 0 && buffers.hasRemaining() && channel.isConnected()) {
                return SEND_CHANNEL_BUSY;
            }
        } while (buffers.hasRemaining() && channel.isConnected());
        return SEND_COMPLETE;
    }

}
