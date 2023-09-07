package com.jav.net.base.joggle;


/**
 * 发送者接口
 *
 * @author yyz
 */
public interface INetSender<T> {

    /**
     * 设置发送回调反馈
     *
     * @param feedback
     */
    void setSenderFeedback(ISenderFeedback<T> feedback);

    /**
     * 发送数据
     *
     * @param data
     */
    void sendData(T data);

}
