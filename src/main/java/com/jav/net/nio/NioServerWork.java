package com.jav.net.nio;

import com.jav.common.log.LogDog;
import com.jav.net.base.joggle.INetTaskContainer;
import com.jav.net.entity.FactoryContext;

import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class NioServerWork<T extends NioServerTask, C extends ServerSocketChannel> extends AbsNioNetWork<T, C> {

    protected NioServerWork(FactoryContext context) {
        super(context);
    }


    protected C onCreateChannel(T netTask) {
        ServerSocketChannel channel = null;
        try {
            channel = ServerSocketChannel.open();
            channel.configureBlocking(false);
            channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            netTask.onConfigChannel(channel);
            channel.bind(new InetSocketAddress(netTask.getHost(), netTask.getPort()), netTask.getMaxConnect());
            netTask.setChannel(channel);
        } catch (Throwable e) {
            LogDog.e(" create server channel error = " + netTask.getHost() + " port = " + netTask.getPort());
            callBackInitStatusChannelError(netTask, e);
        }
        return (C) channel;
    }

    @Override
    protected void onInitChannel(T netTask, C channel) {
        try {
            if (!channel.isOpen() && channel.isRegistered()) {
                throw new IllegalStateException("channel is unavailable !!! ");
            }
            if (channel.isBlocking()) {
                // 设置为非阻塞
                channel.configureBlocking(false);
            }
            if (netTask.isTLS()) {
                //如果是TLS任务初始化SSLConnect
                initSSLConnect(netTask);
            }
            //回调channel准备就绪
            netTask.onBeReadyChannel(channel);

        } catch (Throwable e) {
            LogDog.e(" config server channel error = " + netTask.getHost() + " port = " + netTask.getPort());
            callBackInitStatusChannelError(netTask, e);
        }
    }

    @Override
    protected void onRegisterChannel(T netTask, C channel) {
        try {
            //注册监听链接事件
            SelectionKey selectionKey = channel.register(mSelector, SelectionKey.OP_ACCEPT, netTask);
            netTask.setSelectionKey(selectionKey);
        } catch (Throwable e) {
            LogDog.e(" register Channel OP_ACCEPT error = " + netTask.getHost() + " port = " + netTask.getPort());
            callBackInitStatusChannelError(netTask, e);
        }
    }

    @Override
    protected void onAcceptEvent(SelectionKey key) {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        if (serverSocketChannel == null) {
            return;
        }
        T netTask = (T) key.attachment();
        try {
            SocketChannel channel = serverSocketChannel.accept();
            netTask.onAcceptServerChannel(channel);
        } catch (Throwable e) {
            e.printStackTrace();
            INetTaskContainer taskFactory = mFactoryContext.getNetTaskContainer();
            taskFactory.addUnExecTask(netTask);
        }
    }
}
