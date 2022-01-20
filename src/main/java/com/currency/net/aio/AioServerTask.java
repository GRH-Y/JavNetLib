package com.currency.net.aio;

import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;

public class AioServerTask extends BaseAioChannelTask<AsynchronousServerSocketChannel> {

    public AsynchronousChannelGroup getChannelGroup() {
        return null;
    }

    protected void onAcceptServerChannel(AsynchronousServerSocketChannel channel) {
    }

}
