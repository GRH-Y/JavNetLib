package com.currency.net.nio;

import com.currency.net.base.AbsNetSender;
import com.currency.net.base.SendPacket;
import com.currency.net.entity.MultiByteBuffer;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.LinkedList;

/**
 * 带有缓存的Nio发送器
 */
public abstract class AbsNioCacheNetSender extends AbsNetSender {

    protected boolean mIsEnablePreStore = false;

    protected final LinkedList<Object> mDataQueue = new LinkedList();

    protected SelectionKey mSelectionKey;

    public boolean isCacheEmpty() {
        return mDataQueue.isEmpty();
    }

    public void clearCache() {
        if (mDataQueue.isEmpty()) {
            return;
        }
        for (Object obj : mDataQueue) {
            if (mFeedback != null) {
                mFeedback.onSenderFeedBack(this, obj, null);
            }
        }
        mDataQueue.clear();
    }

    public void enablePrestore() {
        mIsEnablePreStore = true;
    }

    public void disablePrestore() {
        if (mIsEnablePreStore) {
            mIsEnablePreStore = false;
            if (!mDataQueue.isEmpty() && mSelectionKey != null
                    && (mSelectionKey.interestOps() & SelectionKey.OP_WRITE) != SelectionKey.OP_WRITE) {
                mSelectionKey.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
            }
        }
    }

    /**
     * 发送数据
     *
     * @param sendPacket
     */
    @Override
    public void sendData(SendPacket sendPacket) {
        if (sendPacket == null) {
            return;
        }
        if (mSelectionKey == null) {
            if (mIsEnablePreStore) {
                Object finalData = onCheckAndChangeData(sendPacket.getSendData());
                if (finalData != null) {
                    mDataQueue.add(finalData);
                } else {
                    if (mFeedback != null) {
                        mFeedback.onSenderFeedBack(this, sendPacket.getSendData(), null);
                    }
                }
            }
            return;
        }
        if (!mSelectionKey.isValid()) {
            return;
        }
        Object finalData = onCheckAndChangeData(sendPacket.getSendData());
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
        } else {
            if (mFeedback != null) {
                mFeedback.onSenderFeedBack(this, sendPacket.getSendData(), null);
            }
        }
    }

    /**
     * 检查数据的类型和数据状态
     *
     * @param objData
     * @return
     */
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
        } else if (objData instanceof MultiByteBuffer) {
            MultiByteBuffer buf = (MultiByteBuffer) objData;
            if (buf.isClear()) {
                return buf;
            }
            return null;
        } else if (objData instanceof ByteBuffer) {
            ByteBuffer data = (ByteBuffer) objData;
            return data.hasRemaining() ? data : null;
        }
        return null;
    }

    /**
     * 根据不同类型的数据处理
     *
     * @param data
     * @return
     * @throws Throwable
     */
    protected int onHandleSendData(Object data) throws Throwable {
        int ret = SEND_COMPLETE;
        if (data instanceof ByteBuffer) {
            ret = sendDataImp((ByteBuffer) data);
            if (ret == SEND_CHANNEL_BUSY) {
                mDataQueue.addFirst(data);
            }
        } else if (data instanceof MultiByteBuffer) {
            MultiByteBuffer buf = (MultiByteBuffer) data;
            ByteBuffer[] sendDataBuf = buf.getTmpBuf();
            if (sendDataBuf == null) {
                // sendDataBuf 为null 说明是第一次处理数据，不为null说明上次数据发送不完整
                sendDataBuf = buf.getUseBuf(true);
            }
            for (ByteBuffer buffer : sendDataBuf) {
                if (buffer.hasRemaining()) {
                    ret = sendDataImp(buffer);
                    if (ret == SEND_CHANNEL_BUSY) {
                        //当前数据没有发送完，则临时记录起来
                        buf.setTmpBuf(sendDataBuf);
                        //加入发送队列等待下次处理
                        mDataQueue.addFirst(data);
                        return SEND_CHANNEL_BUSY;
                    }
                }
            }
            buf.setTmpBuf(null);
            buf.setBackBuf(sendDataBuf);
        }
        return ret;
    }

    /**
     * NioClientWork回调执行读事件
     *
     * @throws Throwable
     */
    @Override
    protected void onSendNetData() throws Throwable {
        Object data = mDataQueue.pollFirst();
        Throwable exception = null;

        try {
            int ret = onHandleSendData(data);
            if (ret == SEND_CHANNEL_BUSY) {
                return;
            }
        } catch (Throwable e) {
            exception = e;
        }
        if (mFeedback != null) {
            mFeedback.onSenderFeedBack(this, data, exception);
        }
        if (exception != null) {
            throw exception;
        }
        if (mDataQueue.isEmpty()) {
            try {
                //当前没有数据可发送则取消写事件监听
                mSelectionKey.interestOps(SelectionKey.OP_READ);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 具体实现发送数据
     *
     * @param data
     * @return
     * @throws Throwable
     */
    protected abstract int sendDataImp(ByteBuffer data) throws Throwable;
}
