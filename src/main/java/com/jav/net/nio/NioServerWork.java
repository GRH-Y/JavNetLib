package com.jav.net.nio;

import com.jav.net.base.FactoryContext;
import com.jav.net.base.NioNetWork;
import com.jav.net.base.SelectorEventHubs;
import com.jav.net.base.joggle.NetErrorType;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * nio 服务端的事物
 *
 * @author yyz
 */
public class NioServerWork extends NioNetWork<NioServerTask, ServerSocketChannel> {

    protected NioServerWork(FactoryContext context) {
        super(context);
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
        // 回调channel准备就绪
        netTask.onBeReadyChannel(null, channel);
    }

    @Override
    protected void onRegisterChannel(NioServerTask netTask, ServerSocketChannel channel) throws IOException {
        // 注册监听链接事件
        SelectorEventHubs eventHubs = mFactoryContext.getSelectorEventHubs();
        SelectionKey key = eventHubs.registerAcceptEvent(channel, netTask);
        netTask.setSelectionKey(key);
    }

    @Override
    protected void onAcceptEvent(NioServerTask netTask, ServerSocketChannel serverSocketChannel) {
        try {
            SocketChannel channel = serverSocketChannel.accept();
            if (channel != null) {
                netTask.onAcceptServerChannel(channel);
            }
        } catch (Throwable e) {
            callChannelError(netTask, NetErrorType.ACCEPT, e);
        }
    }
}
