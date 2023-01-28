package com.jav.net.security.channel;

import com.jav.net.security.channel.joggle.ChannelStatus;
import com.jav.net.security.channel.joggle.ISecurityProxySender;

/**
 * 基本的SecurityChannelImage,提供通道状态监控和代理数据发送者
 *
 * @author yyz
 */
public class SecurityChannelImage {

    /**
     * 通道状态监听器
     */
    private ChannelStatus mCruStatus;

    /**
     * 发送者的代理接口
     */
    protected ISecurityProxySender mSender;

    /**
     * 设置发送者的代理接口
     *
     * @param sender 代理接口
     */
    protected void setProxySender(ISecurityProxySender sender) {
        this.mSender = sender;
    }

    /**
     * 回调通道当前状态
     *
     * @param status 状态
     */
    protected void updateStatus(ChannelStatus status) {
        synchronized (this) {
            this.mCruStatus = status;
        }
    }


    /**
     * 获取当前通道的状态
     *
     * @return 通道状态
     */
    public ChannelStatus getCruStatus() {
        synchronized (this) {
            return mCruStatus;
        }
    }

}
