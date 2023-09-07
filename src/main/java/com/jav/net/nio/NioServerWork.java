package com.jav.net.nio;

import com.jav.net.base.FactoryContext;
import com.jav.net.base.joggle.ISSLComponent;
import com.jav.net.base.joggle.NetErrorType;

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
 * @author yyz
 */
public class NioServerWork extends AbsNioNetWork<NioServerTask, ServerSocketChannel> {

    protected NioServerWork(FactoryContext context) {
        super(context);
    }

    protected void initSSLConnect(NioServerTask netTask) {
        if (netTask.isTls()) {
            ISSLComponent sslFactory = mFactoryContext.getSSLFactory();
            netTask.onCreateSSLContext(sslFactory);
        }
    }

    @Override
    protected ServerSocketChannel onCreateChannel(NioServerTask netTask) throws IOException {
        ServerSocketChannel channel = ServerSocketChannel.open();
        channel.configureBlocking(false);
        channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        netTask.onConfigChannel(channel);
        channel.bind(new InetSocketAddress(netTask.getHost(), netTask.getPort()), netTask.getMaxConnect());
        netTask.setChannel(channel);
        return channel;
    }

    @Override
    protected void onInitChannel(NioServerTask netTask, ServerSocketChannel channel) throws IOException {
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
    protected void onRegisterChannel(NioServerTask netTask, ServerSocketChannel channel) throws ClosedChannelException {
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
        NioServerTask netTask = (NioServerTask) key.attachment();
        try {
            SocketChannel channel = serverSocketChannel.accept();
            netTask.onAcceptServerChannel(channel);
        } catch (Throwable e) {
            callChannelError(netTask, NetErrorType.ACCEPT, e);
        }
    }
}
