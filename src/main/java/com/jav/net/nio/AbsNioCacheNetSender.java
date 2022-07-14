package com.jav.net.nio;

import com.jav.net.base.AbsNetSender;
import com.jav.net.component.SenderCacheComponent;
import com.jav.net.component.joggle.ICacheComponent;
import com.jav.net.entity.MultiByteBuffer;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

/**
 * 带有缓存的Nio发送器
 */
public abstract class AbsNioCacheNetSender<T> extends AbsNetSender<T> {

    protected ICacheComponent<Object> mCacheComponent;

    protected SelectionKey mSelectionKey;

    public AbsNioCacheNetSender() {
        mCacheComponent = getCacheComponent();
        if (mCacheComponent == null) {
            mCacheComponent = new SenderCacheComponent();
        }
    }

    protected ICacheComponent getCacheComponent() {
        return mCacheComponent;
    }

    /**
     * 发送数据
     *
     * @param data
     */
    @Override
    public void sendData(T data) {
        if (data == null) {
            return;
        }
        if (mSelectionKey == null || !mSelectionKey.isValid()) {
            if (mFeedback != null) {
                mFeedback.onSenderFeedBack(this, data, null);
            }
            return;
        }
        boolean ret = mCacheComponent.addLastData(data);
        if (ret) {
            //判断当前是否注册写事件监听，如果没有则添加
            if ((mSelectionKey.interestOps() & SelectionKey.OP_WRITE) != SelectionKey.OP_WRITE) {
                mSelectionKey.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
            }
            mSelectionKey.selector().wakeup();
        } else {
            if (mFeedback != null) {
                mFeedback.onSenderFeedBack(this, data, null);
            }
        }
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
        if (data instanceof MultiByteBuffer) {
            MultiByteBuffer buffer = (MultiByteBuffer) data;
            ByteBuffer[] sendDataBuf = buffer.getTmpBuf();
            if (sendDataBuf == null) {
                // sendDataBuf 为null 说明是第一次处理数据，不为null说明上次数据发送不完整
                sendDataBuf = buffer.getUseBuf(true);
            }
            ret = sendDataImp(sendDataBuf);
            if (ret == SEND_CHANNEL_BUSY) {
                //当前数据没有发送完，则临时记录起来
                buffer.setTmpBuf(sendDataBuf);
                //加入发送队列等待下次处理
                mCacheComponent.addFirstData(data);
                return SEND_CHANNEL_BUSY;
            }
            buffer.setTmpBuf(null);
            buffer.setBackBuf(sendDataBuf);
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
        Object data = mCacheComponent.pollLastData();
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
        if (mCacheComponent.size() == 0) {
            try {
                //当前没有数据可发送则取消写事件监听
                mSelectionKey.interestOps(SelectionKey.OP_READ);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected boolean hasRemaining(ByteBuffer[] buffers) {
        for (ByteBuffer buffer : buffers) {
            if (buffer.hasRemaining()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 具体实现发送数据
     *
     * @param data
     * @return
     * @throws Throwable
     */
    protected abstract int sendDataImp(ByteBuffer[] data) throws Throwable;
}
