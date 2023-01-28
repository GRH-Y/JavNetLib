package com.jav.net.aio;


import com.jav.net.base.joggle.INetTaskComponent;
import com.jav.net.base.joggle.ISSLComponent;
import com.jav.net.base.joggle.NetErrorType;
import com.jav.net.entity.FactoryContext;
import com.jav.net.ssl.TLSHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class AioClientNetWork<T extends AioClientTask> extends AioNetWork<T> implements CompletionHandler<Void, T> {


    protected AioClientNetWork(FactoryContext context) {
        super(context);

    }

    @Override
    protected void onCreateTask() {
        super.onCreateTask();
    }

    @Override
    protected void onConnectTask(T netTask) {
        try {
            AsynchronousSocketChannel channel = AsynchronousSocketChannel.open(netTask.onInitChannelGroup());
            // Disable the Nagle algorithm
            channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
            // Keep connection alive
            channel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
            // Re-use address
            channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            netTask.setChannel(channel);
            INetTaskComponent netTaskFactory = mFactoryContext.getNetTaskComponent();
            netTask.setNetTaskFactory(netTaskFactory);
            netTask.onConfigChannel(channel);
            channel.connect(new InetSocketAddress(netTask.getHost(), netTask.getPort()), netTask, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initSSLConnect(T netTask) {
        if (netTask.isTls()) {
            ISSLComponent sslFactory = mFactoryContext.getSSLFactory();
            netTask.onCreateSSLContext(sslFactory);
        }
    }

    @Override
    protected void onDestroyTask() {
        super.onDestroyTask();
    }

    @Override
    protected void onDestroyTaskAll() {
        super.onDestroyTaskAll();
    }

    @Override
    protected void onDisconnectTask(T netTask) {
        try {
            netTask.onCloseChannel();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                AsynchronousSocketChannel channel = netTask.getChannel();
                try {
                    channel.shutdownInput();
                } catch (Exception e) {
                }
                try {
                    channel.shutdownOutput();
                } catch (Exception e) {
                }
                channel.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                TLSHandler tlsHandler = netTask.getTlsHandler();
                if (tlsHandler != null) {
                    tlsHandler.release();
                }
            }
        }
    }

    @Override
    public void completed(Void result, T task) {
        try {
            initSSLConnect(task);
            task.onBeReadyChannel(task.getChannel());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void failed(Throwable exc, T task) {
        try {
            task.onErrorChannel(NetErrorType.OTHER, exc);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            destroyTaskImp(task);
        }
    }
}
