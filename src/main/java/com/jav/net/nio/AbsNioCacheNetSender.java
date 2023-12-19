package com.jav.net.nio;

import com.jav.net.base.AbsNetSender;
import com.jav.net.base.MultiBuffer;
import com.jav.net.component.CacheComponent;
import com.jav.net.component.joggle.ICacheComponent;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

/**
 * 带有缓存的Nio发送器
 *
 * @author yyz
 */
public abstract class AbsNioCacheNetSender<C, T> extends AbsNetSender<C, T> {

    /**
     * 缓存组件
     */
    protected ICacheComponent<T> mCacheComponent;


    public AbsNioCacheNetSender() {
        mCacheComponent = getCacheComponent();
        if (mCacheComponent == null) {
            mCacheComponent = new CacheComponent<>();
        }
    }

    public ICacheComponent<T> getCacheComponent() {
        return mCacheComponent;
    }

    /**
     * 发送数据
     *
     * @param data 要发送的数据
     */
    @Override
    public void sendData(T data) {
        if (data == null) {
            return;
        }
        if (mSelectionKey == null || !mSelectionKey.isValid()) {
            if (mFeedback != null) {
                mFeedback.onSenderFeedBack(this, data, new IOException("SelectionKey is null or isValid !"));
            }
            return;
        }
        boolean ret = mCacheComponent.addLastData(data);
        if (ret) {
            // 判断当前是否注册写事件监听，如果没有则添加
            synchronized (mCacheComponent) {
                if ((mSelectionKey.interestOps() & SelectionKey.OP_WRITE) != SelectionKey.OP_WRITE) {
                    mSelectionKey.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
                }
                mSelectionKey.selector().wakeup();
            }
//            LogDog.w("#AbsSender# interestOps write and read ,wakeup selector,cache size = " + mCacheComponent.size());
        } else {
            if (mFeedback != null) {
                mFeedback.onSenderFeedBack(this, data, null);
            }
        }
    }


    /**
     * 根据不同类型的数据处理
     *
     * @param data 要发送的数据
     * @return 返回状态码 @see AbsNetSender SEND_COMPLETE , SEND_FAIL or SEND_CHANNEL_BUSY
     * @throws Throwable 出错抛异常到上层处理
     */
    protected int onHandleSendData(T data) throws Throwable {
        int ret = SEND_COMPLETE;
        if (data instanceof MultiBuffer) {
            MultiBuffer buffer = (MultiBuffer) data;
            if (buffer.isClear()) {
                // 当前buf没有数据
                return SEND_COMPLETE;
            }
//            byte[] byteData = buffer.asByte();
            ByteBuffer[] sendDataBuf = buffer.getFreezeBuf();
            if (sendDataBuf == null) {
                // sendDataBuf 为null 说明是第一次处理数据，不为null说明上次数据发送不完整
                sendDataBuf = buffer.getDirtyBuf(true);
            }
//            StringBuilder dataStr = new StringBuilder();
//            int dataLength = byteData.length;
//            if (byteData.length > 80) {
//                dataLength = 80;
//            }
//            for (int index = 0; index < dataLength; index++) {
//                String hex = Integer.toHexString(byteData[index] & 0xff);
//                if (hex.length() == 1) {
//                    hex = "0" + hex;
//                }
//                dataStr.append(hex);
//            }
//            LogDog.d("#AbsSender# dataStr = " + dataStr);
            ret = sendDataImp(sendDataBuf);
            if (ret == SEND_CHANNEL_BUSY) {
                // 当前数据没有发送完，则临时记录起来
                buffer.setFreezeBuf(sendDataBuf);
                // 加入发送队列等待下次处理
                mCacheComponent.addFirstData(data);
                return ret;
            }
            buffer.setFreezeBuf(null);
            buffer.restoredBuf(sendDataBuf);
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
        T sendData = mCacheComponent.pollFirstData();
        Throwable exception = null;
        try {
            int ret = onHandleSendData(sendData);
            if (ret == SEND_CHANNEL_BUSY) {
                return;
            }
            if (ret == SEND_FAIL) {
                exception = new IOException("## failed to send data. The socket channel may be closed !!! ");
            }
        } catch (Throwable e) {
            exception = e;
        }
        if (mFeedback != null) {
            mFeedback.onSenderFeedBack(this, sendData, exception);
        }
        if (exception != null) {
            throw exception;
        }
        synchronized (mCacheComponent) {
            if (mCacheComponent.size() == 0) {
                // 当前没有数据可发送则取消写事件监听
                if ((mSelectionKey.interestOps() & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE) {
                    mSelectionKey.interestOps(SelectionKey.OP_READ);
                }
//                mSelectionKey.selector().wakeup();
            }
        }
    }


    /**
     * 具体实现发送数据
     *
     * @param data
     * @return
     * @throws IOException
     */
    protected abstract int sendDataImp(Object data) throws IOException;
}
