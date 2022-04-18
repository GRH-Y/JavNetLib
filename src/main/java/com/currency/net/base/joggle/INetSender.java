package com.currency.net.base.joggle;


import com.currency.net.base.SendPacket;

/**
 * 发送者接口
 *
 * @author yyz
 */
public interface INetSender {

    /**
     * 设置发送回调反馈
     *
     * @param feedback
     */
    void setSenderFeedback(ISenderFeedback feedback);

    /**
     * 发送数据
     *
     * @param sendPacket
     */
    void sendData(SendPacket sendPacket);

}
