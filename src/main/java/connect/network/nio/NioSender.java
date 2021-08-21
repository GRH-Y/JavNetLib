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

    protected SocketChannel mChannel;
    protected SelectionKey mSelectionKey;
    private boolean mIsEnablePreStore = false;

    protected LinkedList<Object> mDataQueue = new LinkedList();

    public NioSender() {
    }

    public NioSender(SelectionKey selectionKey, SocketChannel channel) {
        setChannel(selectionKey, channel);
    }

    public void setChannel(SelectionKey selectionKey, SocketChannel channel) {
        if (selectionKey == null || channel == null) {
            throw new NullPointerException("selectionKey or channel is null !!!");
        }
        this.mSelectionKey = selectionKey;
        this.mChannel = channel;
    }

    public boolean isSendDataEmpty() {
        return mDataQueue.isEmpty();
    }

    public void clear() {
        for (Object obj : mDataQueue) {
            if (obj instanceof MultiLevelBuf) {
                MultiLevelBuf buf = (MultiLevelBuf) obj;
                XMultiplexCacheManger.getInstance().lose(buf);
            }
        }
        mDataQueue.clear();
        this.feedback = null;
    }

    public void enablePrestore() {
        mIsEnablePreStore = true;
    }

    public void disablePrestore() {
        mIsEnablePreStore = false;
        if (!mDataQueue.isEmpty() && mSelectionKey != null && (mSelectionKey.interestOps() & SelectionKey.OP_WRITE) != SelectionKey.OP_WRITE) {
            mSelectionKey.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
        }
    }


    /**
     * 发送数据
     *
     * @param objData
     */
    @Override
    public void sendData(Object objData) {
        if (objData == null || mSelectionKey == null && mIsEnablePreStore) {
            Object finalData = onCheckAndChangeData(objData);
            mDataQueue.add(finalData);
            return;
        }
        if (!mSelectionKey.isValid()) {
            return;
        }
        Object finalData = onCheckAndChangeData(objData);
        if (finalData != null) {
            try {
                mDataQueue.add(finalData);
                //判断当前是否注册写事件监听，如果没有则添加
                if ((mSelectionKey.interestOps() & SelectionKey.OP_WRITE) != SelectionKey.OP_WRITE) {
                    mSelectionKey.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    protected Object onCheckAndChangeData(Object objData) {
        if (objData instanceof byte[]) {
            byte[] data = (byte[]) objData;
            if (data.length > 0) {
                ByteBuffer buffer = ByteBuffer.allocateDirect(data.length);
                buffer.put(data);
                buffer.flip();
                return buffer;
            }
            return null;
        } else if (objData instanceof MultiLevelBuf) {
            MultiLevelBuf buf = (MultiLevelBuf) objData;
            if (buf.isHasData()) {
                return buf;
            } else {
                XMultiplexCacheManger.getInstance().lose(buf);
            }
            return null;
        } else if (objData instanceof ByteBuffer) {
            ByteBuffer data = (ByteBuffer) objData;
            return data.hasRemaining() ? data : null;
        }
        return null;
    }

    protected int onHandleSendData(Object data) throws Throwable {
        int ret = SEND_COMPLETE;
        if (data instanceof ByteBuffer) {
            ret = sendDataImp((ByteBuffer) data);
            if (ret == SEND_CHANNEL_BUSY) {
                mDataQueue.addFirst(data);
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
                        mDataQueue.addFirst(data);
                        return SEND_CHANNEL_BUSY;
                    }
                }
            }
            buf.markBuf(null);
            buf.setBackBuf(sendDataBuf);
        }
        return ret;
    }

    /**
     * NioClientWork回调执行读事件
     *
     * @throws Throwable
     */
    protected void onSendNetData() throws Throwable {
        Object data = mDataQueue.pollFirst();
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
                data = mDataQueue.pollFirst();
            }
        }
        if (exception == null) {
            if (ret == SEND_COMPLETE) {
                try {
                    //当前没有数据可发送则取消写事件监听
                    mSelectionKey.interestOps(SelectionKey.OP_READ);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            throw exception;
        }
    }


    protected int sendDataImp(ByteBuffer buffers) throws Throwable {
        if (mChannel == null || buffers == null || !mChannel.isConnected()) {
            return SEND_FAIL;
        }
        do {
            long ret = mChannel.write(buffers);
            if (ret < 0) {
                throw new IOException("## failed to send data. The socket channel may be closed !!! ");
            } else if (ret == 0 && buffers.hasRemaining() && mChannel.isConnected()) {
                return SEND_CHANNEL_BUSY;
            }
        } while (buffers.hasRemaining() && mChannel.isConnected());
        return SEND_COMPLETE;
    }
}
