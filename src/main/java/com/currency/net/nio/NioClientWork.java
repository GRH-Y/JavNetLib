package com.currency.net.nio;


import com.currency.net.base.FactoryContext;
import com.currency.net.base.SocketChannelCloseException;
import com.currency.net.base.joggle.INetTaskContainer;
import com.currency.net.ssl.TLSHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class NioClientWork<T extends NioClientTask, C extends SocketChannel> extends AbsNioNetWork<T, C> {

    protected NioClientWork(FactoryContext context) {
        super(context);
    }

    //---------------------------------------------------------------------------------------------------------------

    /**
     * 创建通道
     *
     * @param netTask
     * @return
     */
    protected C onCreateChannel(T netTask) throws IOException {
        InetSocketAddress address = new InetSocketAddress(netTask.getHost(), netTask.getPort());
        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
        channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        channel.setOption(StandardSocketOptions.SO_LINGER, 0);
        channel.setOption(StandardSocketOptions.TCP_NODELAY, false);
        netTask.onConfigChannel(channel);
        channel.connect(address);
        netTask.setChannel(channel);
        return (C) channel;
    }

    @Override
    protected void onInitChannel(T netTask, C channel) throws IOException {
        if (channel.isBlocking()) {
            // 设置为非阻塞
            channel.configureBlocking(false);
        }
    }

    @Override
    protected void onRegisterChannel(T netTask, C channel) throws ClosedChannelException {
        if (channel.isConnected()) {
            initSSLConnect(netTask);
            SelectionKey selectionKey = channel.register(mSelector, SelectionKey.OP_READ, netTask);
            netTask.setSelectionKey(selectionKey);
            netTask.onBeReadyChannel(channel);
            netTask.setChannel(channel);
        } else {
            SelectionKey selectionKey = channel.register(mSelector, SelectionKey.OP_CONNECT, netTask);
            netTask.setSelectionKey(selectionKey);
        }
    }

    @Override
    protected void onConnectEvent(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        if (channel == null) {
            return;
        }
        T netTask = (T) key.attachment();
        try {
            boolean isConnect = channel.finishConnect();
            if (isConnect) {
                initSSLConnect(netTask);
                channel.register(mSelector, SelectionKey.OP_READ, netTask);
                netTask.setSelectionKey(key);
                netTask.onBeReadyChannel(channel);
                netTask.setChannel(channel);
            }
        } catch (Throwable e) {
            callBackInitStatusChannelError(netTask, e);
        }
    }

    @Override
    protected void onReadEvent(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        if (channel == null) {
            return;
        }
        T netTask = (T) key.attachment();
        NioReceiver receive = netTask.getReceiver();
        if (receive != null) {
            try {
                receive.onReadNetData(channel);
            } catch (Throwable e) {
                hasErrorToUnExecTask(netTask, e);
            }
        }
    }

    @Override
    protected void onWriteEvent(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        if (channel == null) {
            return;
        }
        T netTask = (T) key.attachment();
        NioSender sender = netTask.getSender();
        if (sender != null) {
            try {
                sender.onSendNetData();
            } catch (Throwable e) {
                hasErrorToUnExecTask(netTask, e);
            }
        }
    }

    protected void hasErrorToUnExecTask(T netTask, Throwable e) {
        INetTaskContainer taskFactory = mFactoryContext.getNetTaskContainer();
        taskFactory.addUnExecTask(netTask);
        if (!(e instanceof SocketChannelCloseException)) {
            e.printStackTrace();
        }
    }


    /**
     * 准备断开链接回调
     *
     * @param netTask 网络请求任务
     */
    @Override
    protected void onDisconnectTask(T netTask) {
        super.onDisconnectTask(netTask);
        TLSHandler tlsHandler = netTask.getTlsHandler();
        if (tlsHandler != null) {
            tlsHandler.release();
        }
    }

}
