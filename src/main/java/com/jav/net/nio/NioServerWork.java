package com.jav.net.nio;

import com.jav.net.base.joggle.NetErrorType;
import com.jav.net.entity.FactoryContext;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * nio 服务端的事物
 *
 * @param <T> 服务端的对象类型
 * @param <C> 服务端的通道类型
 * @author yyz
 */
public class NioServerWork<T extends NioServerTask, C extends ServerSocketChannel> extends AbsNioNetWork<T, C> {

    protected NioServerWork(FactoryContext context) {
        super(context);
    }


    @Override
    protected C onCreateChannel(T netTask) throws IOException {
        ServerSocketChannel channel = ServerSocketChannel.open();
        channel.configureBlocking(false);
        channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        netTask.onConfigChannel(channel);
        channel.bind(new InetSocketAddress(netTask.getHost(), netTask.getPort()), netTask.getMaxConnect());
        netTask.setChannel(channel);
        return (C) channel;
    }

    @Override
    protected void onInitChannel(T netTask, C channel) throws IOException {
        if (!channel.isOpen() && channel.isRegistered()) {
            throw new IllegalStateException("channel is unavailable !!! ");
        }
        if (channel.isBlocking()) {
            // 设置为非阻塞
            channel.configureBlocking(false);
        }
        if (netTask.isTls()) {
            // 如果是TLS任务初始化SSLConnect
            initSSLConnect(netTask);
        }
        // 回调channel准备就绪
        netTask.onBeReadyChannel(channel);
    }

    @Override
    protected void onRegisterChannel(T netTask, C channel) throws ClosedChannelException {
        // 注册监听链接事件
        SelectionKey selectionKey = channel.register(mSelector, SelectionKey.OP_ACCEPT, netTask);
        netTask.setSelectionKey(selectionKey);
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
            callChannelError(netTask, NetErrorType.ACCEPT, e);
        }
    }
}
