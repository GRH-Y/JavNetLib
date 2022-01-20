package com.currency.net.aio;

import com.currency.net.base.BaseNetWork;
import com.currency.net.base.FactoryContext;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.CompletionHandler;

public class AioServerNetWork<T extends AioServerTask> extends BaseNetWork<T> implements CompletionHandler<Void, T> {

    protected AioServerNetWork(FactoryContext context) {
        super(context);
    }

    @Override
    protected void onConnectTask(T netTask) {
        try {
            AsynchronousServerSocketChannel channel = AsynchronousServerSocketChannel.open(netTask.getChannelGroup());
            InetSocketAddress hostAddress = new InetSocketAddress(netTask.getHost(), netTask.getPort());
            channel.bind(hostAddress);
            netTask.setChannel(channel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void completed(Void result, T attachment) {

    }

    @Override
    public void failed(Throwable exc, T attachment) {

    }
}
