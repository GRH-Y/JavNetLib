package com.jav.net.base.joggle;

/**
 * 发送者的反馈回调
 *
 * @author yyz
 */
public interface ISenderFeedback<T> {

    /**
     * 发送数据回调反馈
     *
     * @param sender
     * @param data
     * @param e
     */
    void onSenderFeedBack(INetSender<T> sender, T data, Throwable e);
}
