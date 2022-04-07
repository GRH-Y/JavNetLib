package com.currency.net.aio;

import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;

public class AioServerTask extends BaseAioChannelTask<AsynchronousServerSocketChannel> {

    public AsynchronousChannelGroup getChannelGroup() {
        return null;
    }

    protected void onAcceptServerChannel(AsynchronousSocketChannel channel) {
        AioClientTask clientTask = new AioClientTask(channel);
        AioClientFactory.getFactory().getNetTaskContainer().addExecTask(clientTask);
    }

}
