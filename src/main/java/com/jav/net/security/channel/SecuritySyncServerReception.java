package com.jav.net.security.channel;

import java.nio.channels.SocketChannel;

/**
 * 同步服务的接待服务端
 *
 * @author yyz
 */
public class SecuritySyncServerReception extends SecurityChannelClient {

    protected SecuritySyncServerReception(SecurityChannelContext context) {
        super(context);
    }

    protected SecuritySyncServerReception(SecurityChannelContext context, SocketChannel channel) {
        super(context, channel);
    }

    @Override
    protected SecurityChanelMeter initSecurityChanelMeter(SecurityChannelContext context) {
        return context.getSyncMeter();
    }

    @Override
    protected SecuritySender initSender() {
        return new SecuritySyncSender();
    }

    @Override
    protected void onBeReadyChannel(SocketChannel channel) {
        super.onBeReadyChannel(channel);
        if (getHost() != null) {
            SecuritySyncMeter syncMeter = mContext.getSyncMeter();
            SecuritySyncSender syncSender = syncMeter.getSender();
            long loadCount = syncMeter.getLocalServerLoadCount();
            syncSender.requestSyncData(mContext.getMachineId(), mContext.getSyncPort(), loadCount);
        }
    }
}
