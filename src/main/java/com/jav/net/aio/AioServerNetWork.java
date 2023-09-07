package com.jav.net.aio;

import com.jav.net.base.FactoryContext;
import com.jav.net.base.joggle.INetTaskComponent;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class AioServerNetWork extends AioNetWork<AioServerTask> implements CompletionHandler<AsynchronousSocketChannel, AioServerTask> {

    protected AioServerNetWork(FactoryContext context) {
        super(context);
    }

    @Override
    public void onConnectTask(AioServerTask netTask) {
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
    public void completed(AsynchronousSocketChannel result, AioServerTask netTask) {
        netTask.onAcceptServerChannel(result);

    }

    @Override
    public void failed(Throwable exc, AioServerTask attachment) {
        exc.printStackTrace();
        INetTaskComponent<AioServerTask> netTaskComponent = mFactoryContext.getNetTaskComponent();
        netTaskComponent.addUnExecTask(attachment);
    }
}
