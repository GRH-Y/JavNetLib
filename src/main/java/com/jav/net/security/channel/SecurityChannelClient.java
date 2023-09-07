package com.jav.net.security.channel;

import com.jav.net.base.MultiBuffer;
import com.jav.net.base.SocketChannelCloseException;
import com.jav.net.base.joggle.INetSender;
import com.jav.net.base.joggle.ISenderFeedback;
import com.jav.net.base.joggle.NetErrorType;
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
            coreSender.setSenderFeedback(new ISenderFeedback<MultiBuffer>() {
                @Override
                public void onSenderFeedBack(INetSender<MultiBuffer> sender, MultiBuffer data, Throwable e) {
                    if (e != null) {
                        SecurityChannelBoot.getInstance().stopChannel(SecurityChannelClient.this);
                    }
                }
            });
            setSender(coreSender);
        }

        mChanelMeter.onChannelReady(securitySender, securityReceiver);
    }

    @Override
    protected void onCloseChannel() {
        super.onCloseChannel();
        mChanelMeter.onChannelInvalid();
    }

    @Override
    protected void onErrorChannel(NetErrorType errorType, Throwable throwable) {
        if (!(throwable instanceof SocketChannelCloseException)) {
            throwable.printStackTrace();
        }
    }
}