package com.jav.net.aio;


import com.jav.net.base.joggle.INetTaskContainer;
import com.jav.net.base.joggle.ISSLFactory;
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
    protected void onCheckConnectTask() {
        super.onCheckConnectTask();
    }

    @Override
    protected void onConnectTask(T netTask) {
        try {
            AsynchronousSocketChannel channel = AsynchronousSocketChannel.open(netTask.onInitChannelGroup());
            //Disable the Nagle algorithm
            channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
            //Keep connection alive
            channel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
            //Re-use address
            channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            netTask.setChannel(channel);
            INetTaskContainer netTaskFactory = mFactoryContext.getNetTaskContainer();
            netTask.setNetTaskFactory(netTaskFactory);
            netTask.onConfigChannel(channel);
            channel.connect(new InetSocketAddress(netTask.getHost(), netTask.getPort()), netTask, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initSSLConnect(T netTask) {
        if (netTask.isTLS()) {
            ISSLFactory sslFactory = mFactoryContext.getSSLFactory();
            netTask.onCreateSSLContext(sslFactory);
        }
    }

    @Override
    protected void onCheckRemoverTask() {
        super.onCheckRemoverTask();
    }

    @Override
    protected void onRecoveryTaskAll() {
        super.onRecoveryTaskAll();
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
            task.onErrorChannel(exc);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            removerTaskImp(task);
        }
    }
}
