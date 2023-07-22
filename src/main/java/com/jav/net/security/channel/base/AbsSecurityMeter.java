package com.jav.net.security.channel.base;


import com.jav.common.cryption.joggle.EncryptionType;
import com.jav.net.security.channel.SecurityReceiver;
import com.jav.net.security.channel.SecuritySender;

/**
 * ChanelMeter 通道辅助，向外提供服务
 *
 * @author yyz
 */
public abstract class AbsSecurityMeter {


    /**
     * 通道当前的状态
     */
    private volatile ChannelStatus mCruStatus = ChannelStatus.NONE;

    /**
     * 数据发送者
     */
    protected SecuritySender mRealSender;

    /**
     * 数据接收者
     */
    protected SecurityReceiver mRealReceiver;


    /**
     * 默认是RSA加密方式
     *
     * @return
     */
    protected abstract EncryptionType initEncryptionType();


    public <T extends SecuritySender> T getSender() {
        return (T) mRealSender;
    }


    public <T extends SecurityReceiver> T getReceiver() {
        return (T) mRealReceiver;
    }


    /**
     * 获取当前通道状态
     *
     * @return 返回当前通道状态
     */
    protected ChannelStatus getCruStatus() {
        synchronized (this) {
            return mCruStatus;
        }
    }


    /**
     * 更新当前通道状态
     *
     * @param status 新的状态
     */
    protected void updateCurStatus(ChannelStatus status) {
        synchronized (this) {
            mCruStatus = status;
        }
    }


    /**
     * 扩展通道就绪回调
     */
    protected void onExtChannelReady() {
    }


    /**
     * 通道建立链接后回调
     *
     * @param sender   当前通道客户端的数据发送者
     * @param receiver 当前通道客户端的数据接收者
     */
    protected void onChannelReady(SecuritySender sender, SecurityReceiver receiver) {
        mRealSender = sender;
        mRealReceiver = receiver;
    }


    /**
     * 通道失效回调
     */
    protected void onChannelInvalid() {
        updateCurStatus(ChannelStatus.INVALID);
    }

}
