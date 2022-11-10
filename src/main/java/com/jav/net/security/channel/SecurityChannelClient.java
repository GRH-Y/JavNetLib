package com.jav.net.security.channel;

import com.jav.net.nio.NioClientTask;
import com.jav.net.nio.NioSender;

import java.nio.channels.SocketChannel;

/**
 * 安全通道客户端
 *
 * @author yyz
 */
public class SecurityChannelClient extends NioClientTask {

    /**
     * 通道运行状态信息
     */
    private SecurityChanelMeter mChanelMeter;

    public SecurityChannelClient() {
        initSecurityChanelMeter();
    }

    public SecurityChannelClient(SocketChannel channel) {
        super(channel, null);
        initSecurityChanelMeter();
    }

    private void initSecurityChanelMeter() {
        mChanelMeter = new SecurityChanelMeter();
    }

    protected SecurityChanelMeter getChanelMeter() {
        return mChanelMeter;
    }

    @Override
    protected void onBeReadyChannel(SocketChannel channel) {
        SecurityReceiver receiver = new SecurityReceiver();

        SecuritySender sender = new SecuritySender();
        NioSender coreSender = sender.getCoreSender();
        coreSender.setChannel(getSelectionKey(), channel);

        mChanelMeter.onChannelReady(this, sender, receiver);

        setReceiver(receiver);
        setSender(coreSender);
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
}
