package com.jav.net.security.channel.base;

import com.jav.net.security.channel.*;
import com.jav.net.security.channel.joggle.IServerChannelStatusListener;

import java.nio.channels.SocketChannel;

/**
 * 服务模式的通道
 *
 * @author yyz
 */
public abstract class AbsSecurityServerReception extends SecurityChannelClient implements IServerChannelStatusListener {

    protected SecurityServerChannelImage mServerChannelImage;

    protected AbsSecurityServerReception(SecurityChannelContext context, SocketChannel channel) {
        super(context, channel);
    }

    @Override
    protected SecurityChanelMeter initSecurityChanelMeter(SecurityChannelContext context) {
        return new SecurityServerChanelMeter(context);
    }


    @Override
    protected void onRegistrarReady() {
        SecurityServerChanelMeter meter = getChanelMeter();
        mServerChannelImage = SecurityServerChannelImage.builderServerChannelImage(this);
        meter.regServerChannelImage(mServerChannelImage);
    }


    @Override
    protected void onCloseChannel() {
        super.onCloseChannel();
        SecurityServerChanelMeter meter = getChanelMeter();
        meter.unRegServerChannelImage(mServerChannelImage);
    }

}
