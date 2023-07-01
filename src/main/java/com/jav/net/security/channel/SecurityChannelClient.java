package com.jav.net.security.channel;

import com.jav.net.nio.NioClientTask;
import com.jav.net.nio.NioReceiver;
import com.jav.net.nio.NioSender;

import java.nio.channels.SocketChannel;

/**
 * 客户模式的通道
 *
 * @author yyz
 */
public class SecurityChannelClient extends NioClientTask {

    /**
     * 通道运行状态信息
     */
    protected SecurityChanelMeter mChanelMeter;

    /**
     * 上下文配置
     */
    protected SecurityChannelContext mContext;

    public SecurityChannelClient(SecurityChannelContext context) {
        this.mContext = context;
        mChanelMeter = initSecurityChanelMeter(context);
    }

    public SecurityChannelClient(SecurityChannelContext context, SocketChannel channel) {
        super(channel, null);
        this.mContext = context;
        mChanelMeter = initSecurityChanelMeter(context);
    }

    /**
     * 初始化channel meter，channel meter主要是控制channel整个生命周期和提供业务接口
     *
     * @param context context 对象
     */
    protected SecurityChanelMeter initSecurityChanelMeter(SecurityChannelContext context) {
        return new SecurityClientChanelMeter(context);
    }

    protected SecurityReceiver initReceiver() {
        return new SecurityReceiver();
    }

    protected SecuritySender initSender() {
        return new SecurityProxySender();
    }

    /**
     * 获取channel meter
     *
     * @param <T>
     * @return
     */
    protected <T extends SecurityChanelMeter> T getChanelMeter() {
        return (T) mChanelMeter;
    }

    @Override
    protected void onBeReadyChannel(SocketChannel channel) {

        SecurityReceiver securityReceiver = mChanelMeter.getReceiver();
        if (securityReceiver == null) {
            securityReceiver = initReceiver();
            NioReceiver coreReceiver = securityReceiver.getCoreReceiver();
            setReceiver(coreReceiver);
        }

        SecuritySender securitySender = mChanelMeter.getSender();
        if (securitySender == null) {
            securitySender = initSender();
            NioSender coreSender = securitySender.getCoreSender();
            coreSender.setChannel(getSelectionKey(), channel);
            setSender(coreSender);
        }

        mChanelMeter.onChannelReady(this, securitySender, securityReceiver);
    }

    @Override
    protected void onCloseChannel() {
        super.onCloseChannel();
        mChanelMeter.onChannelInvalid();
    }

    /**
     * 注册器初始化完成就绪回调，提供外部注册当前通道
     */
    protected void onRegistrarReady() {
    }

    @Override
    protected void onRecovery() {
        super.onRecovery();
        reConnect();
    }

    protected void reConnect() {
        if (mContext.isServerMode()) {
            return;
        }
        mChanelMeter.onChannelReConnect(this);
    }
}
