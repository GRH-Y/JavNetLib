package com.jav.net.base;

import com.jav.net.base.joggle.INetSender;
import com.jav.net.base.joggle.ISenderFeedback;

/**
 * 基本网络数据发送者
 *
 * @param <T>
 * @author yyz
 */
public abstract class AbsNetSender<T> implements INetSender<T> {

    /**
     * 发送完成
     */
    public final static int SEND_COMPLETE = 0;
    /**
     * 发送失败
     */
    public final static int SEND_FAIL = -1;
    /**
     * 通道繁忙,等待下一个事件再发送
     */
    public final static int SEND_CHANNEL_BUSY = -2;

    /**
     * 发送回调，每发送完数据包触发一次
     */
    protected ISenderFeedback<T> mFeedback;

    @Override
    public void setSenderFeedback(ISenderFeedback<T> feedback) {
        this.mFeedback = feedback;
    }

    /**
     * 写事件会触发该回调
     *
     * @throws Throwable
     */
    protected abstract void onSendNetData() throws Throwable;
}
