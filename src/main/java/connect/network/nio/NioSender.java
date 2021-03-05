package connect.network.nio;


import connect.network.base.BaseNetSender;
import connect.network.xhttp.XMultiplexCacheManger;
import connect.network.xhttp.utils.MultilevelBuf;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

public class NioSender extends BaseNetSender {

    protected SocketChannel channel;
    protected SelectionKey selectionKey;

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


    /**
     * 发送数据
     *
     * @param objData
     */
    @Override
    public void sendData(Object objData) {
        if (objData == null) {
            return;
        }
        if (objData instanceof byte[]) {
            byte[] data = (byte[]) objData;
            if (selectionKey.isValid() && data.length > 0) {
                ByteBuffer buffer = ByteBuffer.allocateDirect(data.length);
                buffer.put(data);
                buffer.flip();
                try {
                    boolean ret = dataQueue.add(buffer);
                    if (ret && selectionKey.interestOps() != SelectionKey.OP_WRITE) {
                        selectionKey.interestOps(SelectionKey.OP_WRITE);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        } else if (objData instanceof MultilevelBuf) {
            boolean ret = false;
            MultilevelBuf buf = (MultilevelBuf) objData;
            if (selectionKey.isValid() && buf.isHasData()) {
                try {
                    ret = dataQueue.add(buf);
                    if (ret && selectionKey.interestOps() != SelectionKey.OP_WRITE) {
                        selectionKey.interestOps(SelectionKey.OP_WRITE);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            if (!ret) {
                XMultiplexCacheManger.getInstance().lose(buf);
            }
        } else if (objData instanceof ByteBuffer) {
            ByteBuffer data = (ByteBuffer) objData;
            if (selectionKey.isValid() && data.hasRemaining()) {
                boolean ret = dataQueue.add(data);
                if (ret && selectionKey.interestOps() != SelectionKey.OP_WRITE) {
                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                }
            }
        } else {
            if (selectionKey.isValid()) {
                boolean ret = dataQueue.add(objData);
                if (ret && selectionKey.interestOps() != SelectionKey.OP_WRITE) {
                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                }
            }
        }
    }

    @Override
    protected int onHandleSendData(Object data) throws Throwable {
        int ret = SEND_COMPLETE;
        if (data instanceof ByteBuffer) {
            ret = sendDataImp((ByteBuffer) data);
            if (ret == SEND_CHANNEL_BUSY) {
                dataQueue.addFirst(data);
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
                    }
                }
            }
            buf.setTmpCacheBuf(null);
            buf.setBackBuf(sendDataBuf);
        }
        return ret;
    }

    protected void onSendNetData() throws Throwable {
        Object data = dataQueue.pollFirst();
        Throwable exception = null;
        int ret = SEND_COMPLETE;

        while (data != null && exception == null) {
            try {
                ret = onHandleSendData(data);
                if (ret == SEND_CHANNEL_BUSY) {
                    return;
                }
            } catch (Throwable e) {
                exception = e;
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
