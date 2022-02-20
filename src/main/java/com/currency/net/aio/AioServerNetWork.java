package com.currency.net.aio;

import com.currency.net.base.BaseNetWork;
import com.currency.net.base.FactoryContext;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class AioServerNetWork<T extends AioServerTask> extends BaseNetWork<T> implements CompletionHandler<AsynchronousSocketChannel, T> {

    protected AioServerNetWork(FactoryContext context) {
        super(context);
    }

    @Override
    protected void onConnectTask(T netTask) {
        try {
            AsynchronousServerSocketChannel channel = AsynchronousServerSocketChannel.open(netTask.getChannelGroup());
            InetSocketAddress hostAddress = new InetSocketAddress(netTask.getHost(), netTask.getPort());
            netTask.onConfigChannel(channel);
            channel.bind(hostAddress);
            netTask.setChannel(channel);
            channel.accept(netTask, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void completed(AsynchronousSocketChannel result, T netTask) {
        netTask.onAcceptServerChannel(result);

    }

    @Override
    public void failed(Throwable exc, T attachment) {
        exc.printStackTrace();
        mFactoryContext.getNetTaskContainer().addUnExecTask(attachment);
    }
}
