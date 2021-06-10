package connect.network.nio;


import connect.network.base.BaseNetSender;
import connect.network.xhttp.XMultiplexCacheManger;
import connect.network.xhttp.utils.MultiLevelBuf;

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
        if (selectionKey == null || channel == null) {
            throw new NullPointerException("selectionKey or channel is null !!!");
        }
        this.selectionKey = selectionKey;
        this.channel = channel;
    }

    public boolean isSendDataEmpty() {
        return dataQueue.isEmpty();
    }

    public void clear() {
        for (Object obj : dataQueue) {
            if (obj instanceof MultiLevelBuf) {
                MultiLevelBuf buf = (MultiLevelBuf) obj;
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
        if (objData == null || !selectionKey.isValid()) {
            return;
        }
        if (objData instanceof byte[]) {
            byte[] data = (byte[]) objData;
            if (data.length > 0) {
                ByteBuffer buffer = ByteBuffer.allocateDirect(data.length);
                buffer.put(data);
                buffer.flip();
                try {
                    boolean ret = dataQueue.add(buffer);
                    //判断当前是否注册写事件监听，如果没有则添加
                    if (ret && ((selectionKey.interestOps() & SelectionKey.OP_WRITE) != SelectionKey.OP_WRITE)) {
                        selectionKey.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        } else if (objData instanceof MultiLevelBuf) {
            boolean ret = false;
            MultiLevelBuf buf = (MultiLevelBuf) objData;
            if (buf.isHasData()) {
                try {
                    ret = dataQueue.add(buf);
                    if (ret && ((selectionKey.interestOps() & SelectionKey.OP_WRITE) != SelectionKey.OP_WRITE)) {
                        selectionKey.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
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
            if (data.hasRemaining()) {
                boolean ret = dataQueue.add(data);
                if (ret && ((selectionKey.interestOps() & SelectionKey.OP_WRITE) != SelectionKey.OP_WRITE)) {
                    selectionKey.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
                }
            }
        } else {
            boolean ret = dataQueue.add(objData);
            if (ret && ((selectionKey.interestOps() & SelectionKey.OP_WRITE) != SelectionKey.OP_WRITE)) {
                selectionKey.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
            }
        }
    }

    protected int onHandleSendData(Object data) throws Throwable {
        int ret = SEND_COMPLETE;
        if (data instanceof ByteBuffer) {
            ret = sendDataImp((ByteBuffer) data);
            if (ret == SEND_CHANNEL_BUSY) {
                dataQueue.addFirst(data);
            }
        } else if (data instanceof MultiLevelBuf) {
            MultiLevelBuf buf = (MultiLevelBuf) data;
            ByteBuffer[] sendDataBuf = buf.getMarkBuf();
            if (sendDataBuf == null) {
                // sendDataBuf 为null 说明是第一次处理数据，不为null说明上次数据发送不完整
                sendDataBuf = buf.getUseBuf(true);
            }
            for (ByteBuffer buffer : sendDataBuf) {
                if (buffer.hasRemaining()) {
                    ret = sendDataImp(buffer);
                    if (ret == SEND_CHANNEL_BUSY) {
                        //当前数据没有发送完，则临时记录起来
                        buf.markBuf(sendDataBuf);
                        //加入发送队列等待下次处理
                        dataQueue.addFirst(data);
                        return SEND_CHANNEL_BUSY;
                    }
                }
            }
            buf.markBuf(null);
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
                    //当前没有数据可发送则取消写事件监听
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
