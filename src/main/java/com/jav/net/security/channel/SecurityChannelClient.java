package com.jav.net.security.channel;

import com.jav.net.base.AbsNetSender;
import com.jav.net.base.SocketChannelCloseException;
import com.jav.net.base.joggle.NetErrorType;
import com.jav.net.nio.NioClientTask;
import com.jav.net.nio.NioReceiver;
import com.jav.net.nio.NioSender;

import java.nio.channels.SelectionKey;
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
        super(channel);
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
        return new SecurityProxySender(new NioSender());
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
    protected void onBeReadyChannel(SelectionKey selectionKey, SocketChannel channel) {
        SecuritySender securitySender = initSender();
        AbsNetSender coreSender = securitySender.getCoreSender();
        coreSender.setChannel(selectionKey, channel);
        setSender((NioSender) coreSender);

        SecurityReceiver securityReceiver = initReceiver();
        NioReceiver coreReceiver = securityReceiver.getCoreReceiver();
        setReceiver(coreReceiver);

        mChanelMeter.onChannelReady(securitySender, securityReceiver);
    }

    @Override
    protected void onCloseChannel() {
        mChanelMeter.onChannelInvalid();
    }

    @Override
    protected void onErrorChannel(NetErrorType errorType, Throwable throwable) {
        if (!(throwable instanceof SocketChannelCloseException)) {
            throwable.printStackTrace();
        }
    }
}
